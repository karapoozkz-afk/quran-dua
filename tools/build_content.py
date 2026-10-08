#!/usr/bin/env python3
"""Builds the app's offline content assets.

Inputs
  content-src/situations.json          life situations ("What to read?"), ru + en
  content-src/duas.json                duas and info cards, ru + en; Quranic items reference ayahs
  content-src/i18n/<lang>.json         translations of situations and duas into further languages
  content-src/quran-translations/*.json  extra Quran translations (one array of [surah, ayah, text])
  content-src/charities.json           verified charities for the donation screen
  <dataset>/dist/*.json                quran-json 3.1.2 (npm), CC BY-SA 4.0

Outputs (androidApp/src/main/assets/content/)
  situations.json, duas.json           validated, all languages merged, Quranic Arabic filled in
  quran.json                           114 surahs: Arabic (Uthmani), transliteration, translations
  charities.json                       only organisations that carry every required field

Usage
  npm pack quran-json@3.1.2 && tar xzf quran-json-3.1.2.tgz
  python3 tools/build_content.py --dataset package
"""
import argparse
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "content-src"
OUT = ROOT / "androidApp/src/main/assets/content"
BASE_LANGS = ("ru", "en")
# Languages whose transliteration is Cyrillic and can reuse the Russian one.
CYRILLIC_TRANSLIT = {"kk": "ru", "ky": "ru", "tg": "ru"}
# Languages whose overlay must supply its own transliteration for every dua.
OWN_TRANSLIT = {"es", "uz"}
GRADES = {"quran", "sahih", "hasan", "athar", "daif", "disputed", "nosource"}
KINDS = {"dua", "info"}
CHARITY_FIELDS = ("id", "name", "country", "registrationNumber", "website", "donateUrl", "purposes",
                  "verifiedOn", "verifiedBy")
CHARITY_PURPOSES = {"zakat", "sadaqah", "mosque", "orphans", "water", "food", "education", "general"}

errors = []


def fix_translit(fixes, surah, ayah, text):
    """Applies a known fix, and fails if the upstream text changed so the fix is no longer needed."""
    if (surah, ayah) not in fixes:
        return text
    before, after = fixes[(surah, ayah)]
    if text != before:
        raise SystemExit(f"translit fix for {surah}:{ayah} is stale: {text!r}")
    return after


def err(msg):
    errors.append(msg)


def load_json(path):
    return json.loads(path.read_text(encoding="utf-8"))


def load_dataset(ds):
    dist = ds / "dist"
    ar = load_json(dist / "quran.json")
    tr = load_json(dist / "quran_transliteration.json")
    ru = load_json(dist / "quran_ru.json")
    en = load_json(dist / "quran_en.json")
    assert len(ar) == len(tr) == len(ru) == len(en) == 114
    return ar, tr, ru, en


def load_extra_quran_translations():
    """{lang: (credit, {(surah, ayah): text})}"""
    extra = {}
    for path in sorted((SRC / "quran-translations").glob("*.json")):
        data = load_json(path)
        texts = {(s, a): t for s, a, t in data["ayahs"]}
        if len(texts) != 6236 or any(not t.strip() for t in texts.values()):
            err(f"{path.name}: expected 6236 non-empty ayahs, got {len(texts)}")
        extra[data["lang"]] = (data["credit"], texts)
    return extra


def build_quran(ar, tr, ru, en, extra):
    langs = ["ru", "en"] + sorted(extra)
    credits = {
        "ru": "Эльмир Кулиев, Tanzil.net",
        "en": "Saheeh International, Tanzil.net",
        **{lang: credit for lang, (credit, _) in extra.items()},
    }
    surahs = []
    total = 0
    # Known typos in the upstream transliteration (checked against the Tanzil.net original).
    translit_fixes = {(114, 6): ("Mina aljinnati wa alnnasm", "Mina aljinnati wa alnnasi")}
    for i in range(114):
        a, t, r, e = ar[i], tr[i], ru[i], en[i]
        n = a["total_verses"]
        assert len(a["verses"]) == len(t["verses"]) == len(r["verses"]) == len(e["verses"]) == n, i
        ayahs = []
        for k in range(n):
            row = [
                a["verses"][k]["text"],
                fix_translit(translit_fixes, i + 1, k + 1, t["verses"][k]["transliteration"]),
                r["verses"][k]["translation"],
                e["verses"][k]["translation"],
            ]
            for lang in langs[2:]:
                row.append(extra[lang][1].get((i + 1, k + 1), ""))
            ayahs.append(row)
        total += n
        surahs.append({
            "n": a["id"], "ar": a["name"], "tr": a["transliteration"], "type": a["type"],
            "names": {"ru": r["translation"], "en": e["translation"]},
            "ayahs": ayahs,
        })
    assert total == 6236, total
    return {
        "langs": langs,
        "credits": credits,
        "meta": {
            "arabic": "Uthmani text, The Noble Qur'an Encyclopedia (quranenc.com)",
            "transliteration": "Tanzil.net English transliteration",
            "dataset": "quran-json 3.1.2 by Risan Bagja Pradana, CC BY-SA 4.0",
        },
        "surahs": surahs,
    }


def quran_words(ar, ref):
    s, a = ref["s"], ref["a"]
    if not (1 <= s <= 114) or not (1 <= a <= ar[s - 1]["total_verses"]):
        err(f"bad Quran ref {s}:{a}")
        return ""
    words = ar[s - 1]["verses"][a - 1]["text"].split(" ")
    if "w" in ref:
        lo, hi = ref["w"]
        if not (0 <= lo <= hi < len(words)):
            err(f"bad word range {s}:{a} {ref['w']} (verse has {len(words)} words)")
            return ""
        words = words[lo:hi + 1]
    return " ".join(words)


def check_localized(item_id, field, value, langs, required=True):
    if value is None:
        if required:
            err(f"{item_id}: missing {field}")
        return
    for lang in langs:
        v = value.get(lang)
        if isinstance(v, list):
            if not v:
                err(f"{item_id}: {field}.{lang} is empty")
        elif not str(v or "").strip():
            err(f"{item_id}: {field}.{lang} is empty")


def digits(text):
    return re.findall(r"\d+", text)


def merge_overlays(situations, duas):
    """Folds content-src/i18n/<lang>.json into the localized maps. Returns the extra languages."""
    sit_by_id = {s["id"]: s for s in situations}
    dua_by_id = {d["id"]: d for d in duas}
    langs = []
    for path in sorted((SRC / "i18n").glob("*.json")):
        lang = path.stem
        data = load_json(path)
        langs.append(lang)
        if set(data.get("situations", {})) != set(sit_by_id):
            err(f"i18n/{path.name}: situation ids differ from situations.json")
        if set(data.get("duas", {})) != set(dua_by_id):
            err(f"i18n/{path.name}: dua ids differ from duas.json")
        for sid, tr in data.get("situations", {}).items():
            s = sit_by_id.get(sid)
            if s is None:
                continue
            for field in ("title", "subtitle", "notice"):
                if field in s:
                    s[field][lang] = tr.get(field, "")
            s["keywords"][lang] = tr.get("keywords", [])
        for did, tr in data.get("duas", {}).items():
            d = dua_by_id.get(did)
            if d is None:
                continue
            for field in ("title", "translation", "source", "note"):
                if field in d:
                    d[field][lang] = tr.get(field, "")
            # References must survive translation untouched.
            if digits(d["source"]["ru"]) != digits(d["source"][lang]):
                err(f"{did}: source numbers differ in {lang}: {d['source'][lang]!r}")
            base = CYRILLIC_TRANSLIT.get(lang)
            if "translit" in d and base:
                d["translit"][lang] = d["translit"][base]
            # An overlay may carry its own transliteration (e.g. Spanish spelling).
            if "translit" in d and str(tr.get("translit", "")).strip():
                d["translit"][lang] = tr["translit"]
            elif "translit" in d and lang in OWN_TRANSLIT:
                err(f"{did}: translit.{lang} missing in i18n/{path.name}")
    return langs


def load_charities():
    path = SRC / "charities.json"
    if not path.exists():
        return []
    data = load_json(path)
    published = []
    for c in data.get("organizations", []):
        missing = [f for f in CHARITY_FIELDS if not c.get(f)]
        if missing:
            # Not an error: an organisation without full details is simply not shown.
            print(f"charity {c.get('id', '?')} skipped, missing: {', '.join(missing)}", file=sys.stderr)
            continue
        bad = set(c["purposes"]) - CHARITY_PURPOSES
        if bad:
            err(f"charity {c['id']}: unknown purposes {sorted(bad)}")
        if not c["donateUrl"].startswith("https://"):
            err(f"charity {c['id']}: donateUrl must be https")
        published.append(c)
    return published


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dataset", required=True, type=pathlib.Path,
                    help="unpacked quran-json 3.1.2 npm package directory")
    args = ap.parse_args()

    ar, tr, ru, en = load_dataset(args.dataset)
    extra_quran = load_extra_quran_translations()
    situations = load_json(SRC / "situations.json")
    duas = load_json(SRC / "duas.json")
    langs = list(BASE_LANGS) + merge_overlays(situations, duas)

    sit_ids = [s["id"] for s in situations]
    if len(set(sit_ids)) != len(sit_ids):
        err("duplicate situation ids")
    for s in situations:
        check_localized(s["id"], "title", s.get("title"), langs)
        check_localized(s["id"], "subtitle", s.get("subtitle"), langs)
        check_localized(s["id"], "keywords", s.get("keywords"), langs)
        if "notice" in s:
            check_localized(s["id"], "notice", s["notice"], langs)

    seen = set()
    used = {sid: 0 for sid in sit_ids}
    out_duas = []
    for d in duas:
        i = d.get("id", "?")
        if i in seen:
            err(f"duplicate dua id {i}")
        seen.add(i)
        kind = d.get("kind", "dua")
        if kind not in KINDS:
            err(f"{i}: unknown kind {kind}")
        if d.get("grade") not in GRADES:
            err(f"{i}: unknown grade {d.get('grade')}")
        for sid in d.get("situations", []):
            if sid not in used:
                err(f"{i}: unknown situation {sid}")
            else:
                used[sid] += 1
        if not d.get("situations"):
            err(f"{i}: no situations")
        check_localized(i, "title", d.get("title"), langs)
        check_localized(i, "translation", d.get("translation"), langs)
        check_localized(i, "source", d.get("source"), langs)
        if "note" in d:
            check_localized(i, "note", d["note"], langs)

        item = {
            "id": i, "kind": kind, "grade": d["grade"], "situations": d["situations"],
            "title": d["title"], "translation": d["translation"], "source": d["source"],
        }
        if "quran" in d:
            if d["grade"] != "quran":
                err(f"{i}: Quranic item must have grade 'quran'")
            item["arabic"] = " ".join(quran_words(ar, r) for r in d["quran"])
            first = d["quran"][0]
            item["quranRef"] = {"s": first["s"], "a": first["a"]}
            item["quranAyahs"] = [{"s": r["s"], "a": r["a"]} for r in d["quran"]]
        elif "arabic" in d:
            item["arabic"] = d["arabic"]
        elif kind == "dua":
            err(f"{i}: dua without Arabic text")
        if kind == "dua":
            check_localized(i, "translit", d.get("translit"), BASE_LANGS)
        if "translit" in d:
            item["translit"] = d["translit"]
        for opt in ("note", "repeat", "quranLink"):
            if opt in d:
                item[opt] = d[opt]
        out_duas.append(item)

    for sid, n in used.items():
        if n == 0:
            err(f"situation {sid} has no items")

    charities = load_charities()

    if errors:
        print("\n".join(errors), file=sys.stderr)
        sys.exit(1)

    OUT.mkdir(parents=True, exist_ok=True)
    dump = dict(ensure_ascii=False, separators=(",", ":"))
    (OUT / "situations.json").write_text(json.dumps(situations, **dump), encoding="utf-8")
    (OUT / "duas.json").write_text(json.dumps(out_duas, **dump), encoding="utf-8")
    (OUT / "charities.json").write_text(json.dumps(charities, **dump), encoding="utf-8")
    quran = build_quran(ar, tr, ru, en, extra_quran)
    (OUT / "quran.json").write_text(json.dumps(quran, **dump), encoding="utf-8")
    print(f"OK: {len(situations)} situations, {len(out_duas)} items, content languages {langs}, "
          f"Quran translations {quran['langs']}, {len(charities)} charities -> {OUT}")


if __name__ == "__main__":
    main()
