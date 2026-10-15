#!/usr/bin/env python3
"""Builds the app's offline content assets.

Inputs
  content-src/situations.json   life situations ("What to read?"), ru + en
  content-src/passages.json     Bible passages per situation: references only, never typed-in text
  content-src/votd.json         verses of the day (references only)
  tools/books.py                book list, names and order
  <dataset>/RusSynodal.json     Russian Synodal translation, public domain
  <dataset>/KJV.json            King James Version, public domain
  (both from https://github.com/scrollmapper/bible_databases, formats/json)

Outputs (androidApp/src/main/assets/content/)
  books.json                    books with names and order in each language
  bible_ru.json, bible_en.json  full text, compact: {book id: [[verse, verse, ...] per chapter]}
  situations.json               validated
  passages.json                 passages with their text cut from the Bible text by reference
  votd.json                     verses of the day with text
  plan.json                     "Bible in a year": 365 days of chapters, balanced by length

The text of a passage is never typed by hand: the script cuts it out of the translation by
reference, so a typo in Scripture is impossible. A reference whose chapter is numbered
differently in the two translations (mostly Psalms) must give its English reference explicitly.

Usage
  git clone --depth 1 --filter=blob:none --sparse https://github.com/scrollmapper/bible_databases sm
  (cd sm && git sparse-checkout set formats/json && git checkout)
  python3 tools/build_content.py --dataset sm/formats/json
"""
import argparse
import json
import pathlib
import re
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from books import ALIASES, BOOKS, EN_NT_ORDER  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "content-src"
OUT = ROOT / "androidApp/src/main/assets/content"
LANGS = ("ru", "en")
PLAN_DAYS = 365

errors = []


def err(msg):
    errors.append(msg)


def load_json(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def write_json(name, data):
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / name).write_text(json.dumps(data, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")


NOTE_RE = re.compile(r"<note>(.*?)</note>", re.S)
TITLE_RE = re.compile(r"\s*<title[^>]*>.*?</title>\s*", re.S)


def clean_ru(text):
    """Synodal text from the dataset: a hyphen between spaces is a dash; footnotes are split off."""
    notes = NOTE_RE.findall(text)
    text = NOTE_RE.sub("", text)
    # A psalm superscription that leaked onto the end of the previous psalm's last verse.
    text = TITLE_RE.sub(" ", text)
    text = text.replace(" - ", " — ")
    text = re.sub(r"\s+", " ", text).strip()
    if "<" in text or ">" in text:
        err(f"markup left in Synodal text: {text[:80]}")
    return text, [n.strip() for n in notes]


HEBREW_LETTER_RE = re.compile(r"^[\u05D0-\u05EA]\s+[A-Z]+\.\s+")


def clean_en(text):
    # Psalm 119's acrostic headings ("נ NUN.") are section titles, not verse text.
    text = HEBREW_LETTER_RE.sub("", text)
    text = re.sub(r"\s+", " ", text).strip()
    if "<" in text or ">" in text:
        err(f"markup left in KJV text: {text[:80]}")
    return text, []


def load_translation(path, clean, wanted):
    """{book id: [[verse text, ...] per chapter]}, {"book c:v": note}"""
    data = load_json(path)
    by_name = {b["name"]: b for b in data["books"]}
    books, notes = {}, {}
    for book_id, name in wanted:
        b = by_name.get(name)
        if b is None:
            err(f"{path.name}: book {name} missing")
            continue
        chapters = []
        for expected, ch in enumerate(b["chapters"], start=1):
            if ch["chapter"] != expected:
                err(f"{path.name}: {name} chapter {ch['chapter']} out of order")
            verses = []
            for n, v in enumerate(ch["verses"], start=1):
                if v["verse"] != n:
                    err(f"{path.name}: {name} {expected}:{v['verse']} out of order")
                text, vnotes = clean(v["text"])
                if n == 1 and text[:1].islower():
                    # Psalm superscriptions start lowercase in the dataset ("псалом Давида.").
                    text = text[0].upper() + text[1:]
                if not text:
                    # Synodal Psalm 114:9 is printed joined to verse 8; the reader skips empty verses.
                    print(f"note: {path.name}: {name} {expected}:{n} is empty", file=sys.stderr)
                verses.append(text)
                if vnotes:
                    notes[f"{book_id} {expected}:{n}"] = " ".join(vnotes)
            chapters.append(verses)
        books[book_id] = chapters
    return books, notes


REF_RE = re.compile(r"^([0-9a-z]{3}) (\d+):(\d+)(?:-(\d+))?$")


def parse_ref(ref):
    m = REF_RE.match(ref)
    if not m:
        err(f"bad reference {ref!r}")
        return None
    book, ch, v1, v2 = m.group(1), int(m.group(2)), int(m.group(3)), m.group(4)
    v2 = int(v2) if v2 else v1
    if v2 < v1:
        err(f"bad range {ref!r}")
    return book, ch, v1, v2


def cut(bible, ref, where):
    parsed = parse_ref(ref)
    if not parsed:
        return []
    book, ch, v1, v2 = parsed
    chapters = bible.get(book)
    if chapters is None:
        err(f"{where}: book {book} not in this translation")
        return []
    if ch > len(chapters):
        err(f"{where}: {ref} past the last chapter")
        return []
    verses = chapters[ch - 1]
    if v2 > len(verses):
        err(f"{where}: {ref} past the last verse ({len(verses)})")
        return []
    # Verse numbers as strings: the app reads each verse as a list of strings.
    return [[str(n), verses[n - 1]] for n in range(v1, v2 + 1)]


def refs_for(item, bibles, where):
    """The reference in each translation; English must be explicit where numbering differs."""
    ru_ref = item["ref"]
    en_ref = item.get("refEn")
    parsed = parse_ref(ru_ref)
    if parsed and en_ref is None:
        book, ch, _, _ = parsed
        ru_ch, en_ch = bibles["ru"].get(book, []), bibles["en"].get(book, [])
        if book == "psa" or ch > len(en_ch) or len(ru_ch[ch - 1]) != len(en_ch[ch - 1]):
            err(f"{where}: {ru_ref} is numbered differently in KJV; give refEn")
        en_ref = ru_ref
    return {"ru": ru_ref, "en": en_ref}


def check_localized(obj, field, where, required=True):
    value = obj.get(field)
    if value is None:
        if required:
            err(f"{where}: missing {field}")
        return
    for lang in LANGS:
        if not str(value.get(lang, "")).strip():
            err(f"{where}: {field}.{lang} is empty")


def build_plan(bible):
    """Bible in a year: the 66 canonical books in English order, chapters split into balanced days."""
    order = [b[0] for b in BOOKS if b[6] == "ot" and not b[7]] + EN_NT_ORDER
    chapters = [(book, n + 1, sum(len(v) for v in [verses])) for book in order
                for n, verses in enumerate(bible[book])]
    # Weigh by characters, not verses: a verse of Leviticus and a verse of a short psalm differ a lot.
    weights = [sum(len(t) for t in bible[book][ch - 1]) for book, ch, _ in chapters]
    total = sum(weights)
    days, current, acc, done = [], [], 0.0, 0.0
    for (book, ch, _), w in zip(chapters, weights):
        current.append((book, ch))
        acc += w
        target = total * (len(days) + 1) / PLAN_DAYS
        if done + acc >= target and len(days) < PLAN_DAYS - 1:
            days.append(current)
            done += acc
            current, acc = [], 0.0
    if current:
        days.append(current)
    # Merge runs of consecutive chapters of one book into [book, from, to].
    plan = []
    for day in days:
        segs = []
        for book, ch in day:
            if segs and segs[-1][0] == book and segs[-1][2] == ch - 1:
                segs[-1][2] = ch
            else:
                segs.append([book, ch, ch])
        plan.append(segs)
    if len(plan) != PLAN_DAYS:
        err(f"plan has {len(plan)} days, expected {PLAN_DAYS}")
    return plan


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dataset", required=True, help="scrollmapper/bible_databases formats/json")
    parser.add_argument("--show-psalms", action="store_true", help="print Psalm references side by side")
    args = parser.parse_args()
    ds = pathlib.Path(args.dataset)

    ru, ru_notes = load_translation(ds / "RusSynodal.json", clean_ru, [(b[0], b[1]) for b in BOOKS])
    en, _ = load_translation(ds / "KJV.json", clean_en, [(b[0], b[1]) for b in BOOKS if not b[7]])
    bibles = {"ru": ru, "en": en}

    books = []
    for n, (bid, _, ru_name, ru_short, en_name, en_short, testament, dc) in enumerate(BOOKS):
        books.append({
            "id": bid, "t": testament, "dc": dc, "order": n,
            "orderEn": (EN_NT_ORDER.index(bid) + 1000) if bid in EN_NT_ORDER else (None if dc else n),
            "names": {"ru": ru_name, "en": en_name},
            "short": {"ru": ru_short, "en": en_short},
            "aliases": ALIASES.get(bid, []),
            "chapters": {"ru": len(ru.get(bid, [])), "en": len(en.get(bid, []))},
        })

    situations = load_json(SRC / "situations.json")
    sit_ids = set()
    for s in situations:
        where = f"situation {s.get('id')}"
        if s["id"] in sit_ids:
            err(f"{where}: duplicate id")
        sit_ids.add(s["id"])
        check_localized(s, "title", where)
        check_localized(s, "subtitle", where)
        check_localized(s, "notice", where, required=False)
        for lang in LANGS:
            if not s.get("keywords", {}).get(lang):
                err(f"{where}: no {lang} keywords")

    passages_out, seen = [], set()
    used_situations = set()
    for p in load_json(SRC / "passages.json"):
        where = f"passage {p.get('id')}"
        if p["id"] in seen:
            err(f"{where}: duplicate id")
        seen.add(p["id"])
        check_localized(p, "title", where)
        check_localized(p, "note", where, required=False)
        for s in p["sit"]:
            if s not in sit_ids:
                err(f"{where}: unknown situation {s}")
            used_situations.add(s)
        refs = refs_for(p, bibles, where)
        text = {lang: cut(bibles[lang], refs[lang], f"{where} ({lang})") for lang in LANGS}
        if len(text["ru"]) != len(text["en"]):
            print(f"note: {where} has {len(text['ru'])} ru verses and {len(text['en'])} en verses", file=sys.stderr)
        if args.show_psalms and refs["ru"].startswith("psa"):
            print(f"\n== {p['id']}  {refs['ru']} | {refs['en']}")
            print("  ru:", " ".join(t for _, t in text["ru"])[:240])
            print("  en:", " ".join(t for _, t in text["en"])[:240])
        out = {"id": p["id"], "sit": p["sit"], "title": p["title"], "ref": refs, "text": text}
        if "note" in p:
            out["note"] = p["note"]
        passages_out.append(out)
    for s in sit_ids - used_situations:
        err(f"situation {s} has no passages")

    votd_out = []
    for i, v in enumerate(load_json(SRC / "votd.json")):
        where = f"votd #{i} {v.get('ref')}"
        refs = refs_for(v, bibles, where)
        text = {lang: cut(bibles[lang], refs[lang], where) for lang in LANGS}
        if args.show_psalms and refs["ru"].startswith("psa"):
            print(f"\n== votd {refs['ru']} | {refs['en']}")
            print("  ru:", " ".join(t for _, t in text["ru"])[:200])
            print("  en:", " ".join(t for _, t in text["en"])[:200])
        votd_out.append({"ref": refs, "text": text})
    if len({json.dumps(v["ref"]) for v in votd_out}) != len(votd_out):
        err("votd has duplicates")

    plan = build_plan(en)

    if errors:
        print("\n".join(errors), file=sys.stderr)
        sys.exit(f"{len(errors)} error(s)")

    write_json("books.json", books)
    write_json("bible_ru.json", {
        "lang": "ru", "name": "Синодальный перевод",
        "credit": "Синодальный перевод (1876), общественное достояние. Текст: CrossWire / scrollmapper bible_databases.",
        "books": ru, "notes": ru_notes,
    })
    write_json("bible_en.json", {
        "lang": "en", "name": "King James Version",
        "credit": "King James Version (1769), public domain. Text: CrossWire / scrollmapper bible_databases.",
        "books": en, "notes": {},
    })
    write_json("situations.json", situations)
    write_json("passages.json", passages_out)
    write_json("votd.json", votd_out)
    write_json("plan.json", plan)
    ru_verses = sum(len(c) for b in ru.values() for c in b)
    en_verses = sum(len(c) for b in en.values() for c in b)
    print(f"books {len(books)}, ru verses {ru_verses}, en verses {en_verses}, notes {len(ru_notes)}, "
          f"situations {len(situations)}, passages {len(passages_out)}, votd {len(votd_out)}, plan days {len(plan)}")


if __name__ == "__main__":
    main()
