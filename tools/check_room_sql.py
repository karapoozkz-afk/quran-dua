"""Recreates the Room schema in sqlite3, imports quran.json the way RoomQuranRepository does,
and runs every @Query from Daos.kt with real data."""
import json, re, sqlite3, sys, time, unicodedata
import pathlib
ROOT = pathlib.Path(__file__).resolve().parent.parent
DAOS = ROOT / 'shared/src/commonMain/kotlin/app/qurandua/shared/db/Daos.kt'
ASSET = ROOT / 'androidApp/src/main/assets/content/quran.json'
SCHEMA = """
CREATE TABLE surah (number INTEGER NOT NULL, nameArabic TEXT NOT NULL, transliteration TEXT NOT NULL, revelation TEXT NOT NULL, ayahCount INTEGER NOT NULL, PRIMARY KEY(number));
CREATE TABLE surah_name (number INTEGER NOT NULL, lang TEXT NOT NULL, name TEXT NOT NULL, PRIMARY KEY(number, lang));
CREATE TABLE ayah (surah INTEGER NOT NULL, ayah INTEGER NOT NULL, arabic TEXT NOT NULL, arabicPlain TEXT NOT NULL, transliteration TEXT NOT NULL, PRIMARY KEY(surah, ayah));
CREATE TABLE ayah_translation (surah INTEGER NOT NULL, ayah INTEGER NOT NULL, lang TEXT NOT NULL, text TEXT NOT NULL, textNorm TEXT NOT NULL, PRIMARY KEY(surah, ayah, lang));
CREATE INDEX index_ayah_translation_lang ON ayah_translation (lang);
CREATE TABLE favorite (itemId TEXT NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(itemId));
CREATE TABLE bookmark (surah INTEGER NOT NULL, ayah INTEGER NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(surah, ayah));
CREATE TABLE setting (key TEXT NOT NULL, value TEXT NOT NULL, PRIMARY KEY(key));
"""
def is_diacritic(c):  # approximates TextNormalizer.isArabicDiacritic
    return 'ؐ' <= c <= 'ؚ' or 'ً' <= c <= 'ٟ' or c == 'ٰ' or 'ۖ' <= c <= 'ۭ'
def normalize(text):  # mirror of TextNormalizer.normalize, good enough for SQL checks
    out = []
    for c in text.lower():
        if is_diacritic(c): continue
        c = {'ё':'е','ٱ':'ا','أ':'ا','إ':'ا','آ':'ا','ى':'ي','ة':'ه','ؤ':'و','ئ':'ي'}.get(c, c)
        out.append(c if c.isalnum() else ' ')
    return ' '.join(''.join(out).split())

src = open(DAOS, encoding='utf-8').read()
queries = {}
for m in re.finditer(r'@Query\(\s*(?:"""(.*?)"""|"(.*?)")\s*\)\s*(?:abstract\s+)?(?:suspend\s+)?fun\s+(\w+)', src, re.S):
    queries[m.group(3)] = re.sub(r'\s+', ' ', m.group(1) or m.group(2)).strip()
print(f'{len(queries)} queries found:', ', '.join(queries))

db = sqlite3.connect(':memory:'); db.executescript(SCHEMA)
def run(name, **params):
    sql = queries[name]
    names = re.findall(r':(\w+)', sql)
    missing = set(names) - set(params)
    assert not missing, (name, missing)
    return db.execute(re.sub(r':(\w+)', '?', sql), [params[n] for n in names]).fetchall()

asset = json.load(open(ASSET, encoding='utf-8'))
langs = asset['langs']; cols = {l: 2 + i for i, l in enumerate(langs)}
t = time.time()
with db:
    for s in asset['surahs']:
        db.execute('INSERT OR REPLACE INTO surah VALUES (?,?,?,?,?)', (s['n'], s['ar'], s['tr'], s['type'], len(s['ayahs'])))
        for lang, name in s['names'].items():
            db.execute('INSERT OR REPLACE INTO surah_name VALUES (?,?,?)', (s['n'], lang, name))
        for i, row in enumerate(s['ayahs'], 1):
            db.execute('INSERT OR REPLACE INTO ayah VALUES (?,?,?,?,?)', (s['n'], i, row[0], normalize(row[0]), row[1]))
            for lang, c in cols.items():
                db.execute('INSERT OR REPLACE INTO ayah_translation VALUES (?,?,?,?,?)', (s['n'], i, lang, row[c], normalize(row[c])))
print(f'import {time.time()-t:.1f}s, langs {langs}')

fails = []
def check(cond, msg):
    if not cond: fails.append(msg)
check(run('ayahCount')[0][0] == 6236, 'ayahCount')
check(run('translationCount')[0][0] == 6236 * len(langs), 'translationCount')
for lang in langs + ['ar']:
    rows = run('surahs', lang=lang)
    check(len(rows) == 114, f'surahs {lang}')
    check(sum(r[4] for r in rows) == 6236, f'surah ayahCount sum {lang}')
    named = sum(1 for r in rows if r[5])
    print(f'  surahs({lang}): 114 rows, {named} with localized name (others fall back to transliteration)')
    ay = run('ayahs', surah=2, lang=lang)
    check(len(ay) == 286 and [r[1] for r in ay] == list(range(1, 287)), f'ayahs order {lang}')
    if lang != 'ar':
        check(all(r[4] for r in ay), f'ayahs translation missing {lang}')
    one = run('surah', number=36, lang=lang)
    check(len(one) == 1 and one[0][0] == 36, f'surah(36) {lang}')
check(run('surah', number=115, lang='ru') == [], 'surah(115) should be empty')
for lang, term in [('ru', 'терпе'), ('en', 'patien'), ('kk', 'сабыр'), ('tr', 'sabr'), ('id', 'sabar'), ('ur', 'صبر'), ('ru', normalize('الصبر'))]:
    t = time.time(); rows = run('searchTerm', term=normalize(term), lang=lang, limit=50)
    print(f'  searchTerm({term!r}, {lang}): {len(rows)} rows in {1000*(time.time()-t):.0f} ms, first {rows[0][:2] if rows else None}')
    check(rows, f'search {lang} {term}')
# user tables
db.execute("INSERT OR REPLACE INTO favorite VALUES ('debt_worry', 2)"); db.execute("INSERT OR REPLACE INTO favorite VALUES ('rain', 5)")
check([r[0] for r in run('favoriteIds')] == ['rain', 'debt_worry'], 'favoriteIds order')
check(run('isFavorite', itemId='rain')[0][0] == 1 and run('isFavorite', itemId='x')[0][0] == 0, 'isFavorite')
run('deleteFavorite', itemId='rain'); check(run('isFavorite', itemId='rain')[0][0] == 0, 'deleteFavorite')
db.execute('INSERT OR REPLACE INTO bookmark VALUES (2, 255, 1)')
check(run('isBookmarked', surah=2, ayah=255)[0][0] == 1, 'isBookmarked'); run('deleteBookmark', surah=2, ayah=255)
check(run('bookmarks') == [], 'deleteBookmark')
db.execute("INSERT OR REPLACE INTO setting VALUES ('language','tr')"); check(run('settingsOnce') == [('language', 'tr')], 'settings')
untested = set(queries) - {'ayahCount','translationCount','surahs','surah','ayahs','searchTerm','favoriteIds','isFavorite','deleteFavorite','isBookmarked','deleteBookmark','bookmarks','settings','settingsOnce'}
check(not untested, f'queries without a check: {untested}')
run('settings')
print('FAIL:\n' + '\n'.join(fails) if fails else 'ALL ROOM QUERIES OK'); sys.exit(1 if fails else 0)
