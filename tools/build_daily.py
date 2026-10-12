#!/usr/bin/env python3
"""Builds the morning and evening hadith reminders (androidApp/src/main/assets/content/daily.json).

Inputs
  content-src/daily/morning.json, evening.json   365 picks each: {"c": "bukhari"|"muslim", "n", "title", "summary"}
                                                 (title and summary in Russian, our own words)
  content-src/daily/i18n/<lang>.json             {"<c>:<n>": {"title", "summary"}} in further languages
  <hadith>/<edition>.json                        fawazahmed0/hadith-api editions (public domain repository)

The hadith text itself is cut from the published translations by number and never typed by hand. Only Sahih
al-Bukhari and Sahih Muslim are allowed, so every hadith is sahih.

Usage
  for e in rus eng tur fra ind urd; do for c in bukhari muslim; do
    curl -sSo hadith/$e-$c.json https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/$e-$c.min.json
  done; done
  python3 tools/build_daily.py --hadith hadith
"""
import argparse
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "content-src/daily"
OUT = ROOT / "androidApp/src/main/assets/content/daily.json"
COLLECTIONS = ("bukhari", "muslim")
# App language -> dataset edition prefix. Other languages read the Russian or English text (DailySunnah.kt).
EDITIONS = {"ru": "rus", "en": "eng", "tr": "tur", "fr": "fra", "id": "ind", "ur": "urd"}
DAYS = 365
TITLE_MAX = 40
SUMMARY_MAX = 140
RU_TEXT_MAX = 900
RU_TEXT_MAX_SLEEP = 1600

# The dataset lacks the in-book reference of a few Muslim hadiths; filled from their neighbours
# (6543 is 45:42 and 6546 is 45:44, so 6544 is 45:43).
MUSLIM_REF_FIX = {6544: {"book": 45, "hadith": 43}}

errors = []


def err(msg):
    errors.append(msg)


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def load_editions(hadith_dir):
    texts, refs = {}, {}
    for lang, prefix in EDITIONS.items():
        for c in COLLECTIONS:
            data = load(hadith_dir / f"{prefix}-{c}.json")
            for h in data["hadiths"]:
                text = " ".join(h["text"].split())
                if text:
                    texts[(lang, c, h["hadithnumber"])] = text
                if lang == "en":
                    refs[(c, h["hadithnumber"])] = h.get("reference") or {}
    return texts, refs


def build_slot(name, texts, refs, overlays):
    picks = load(SRC / f"{name}.json")
    if len(picks) != DAYS:
        err(f"{name}: {len(picks)} entries, need {DAYS}")
    seen = set()
    out = []
    for i, p in enumerate(picks, 1):
        where = f"{name} #{i} ({p.get('c')}:{p.get('n')})"
        c, n = p.get("c"), p.get("n")
        if c not in COLLECTIONS or not isinstance(n, int):
            err(f"{where}: collection must be bukhari or muslim with a numeric n")
            continue
        if (c, n) in seen:
            err(f"{where}: duplicate")
        seen.add((c, n))
        ru = texts.get(("ru", c, n))
        if not ru or not texts.get(("en", c, n)):
            err(f"{where}: no Russian or English text in the dataset")
            continue
        limit = RU_TEXT_MAX_SLEEP if p.get("theme") == "sleep" else RU_TEXT_MAX
        if len(ru) > limit:
            err(f"{where}: Russian text is {len(ru)} characters (max {limit})")
        title, summary = p.get("title", ""), p.get("summary", "")
        if not title or len(title) > TITLE_MAX:
            err(f"{where}: title empty or over {TITLE_MAX} characters")
        if not summary or len(summary) > SUMMARY_MAX:
            err(f"{where}: summary empty or over {SUMMARY_MAX} characters")
        item = {
            "collection": c, "number": n,
            "title": {"ru": title}, "summary": {"ru": summary},
            "text": {lang: texts[(lang, c, n)] for lang in EDITIONS if (lang, c, n) in texts},
        }
        if c == "muslim":
            ref = MUSLIM_REF_FIX.get(n) or refs.get((c, n), {})
            if not ref.get("book") or not ref.get("hadith"):
                err(f"{where}: no book/hadith reference for Sahih Muslim")
            item["book"], item["hadith"] = ref.get("book", 0), ref.get("hadith", 0)
        key = f"{c}:{n}"
        for lang, overlay in overlays.items():
            t = overlay.get(key)
            if not t or not t.get("title") or not t.get("summary"):
                err(f"{where}: i18n/{lang}.json has no title/summary")
                continue
            item["title"][lang] = t["title"]
            item["summary"][lang] = t["summary"]
        out.append(item)
    return out, seen


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--hadith", required=True, type=pathlib.Path, help="directory with hadith-api edition files")
    args = ap.parse_args()
    texts, refs = load_editions(args.hadith)
    overlays = {p.stem: load(p) for p in sorted((SRC / "i18n").glob("*.json"))} if (SRC / "i18n").exists() else {}
    morning, m = build_slot("morning", texts, refs, overlays)
    evening, e = build_slot("evening", texts, refs, overlays)
    for c, n in sorted(m & e):
        err(f"{c}:{n} is in both the morning and the evening list")
    if errors:
        print("\n".join(errors[:60]), file=sys.stderr)
        print(f"{len(errors)} errors", file=sys.stderr)
        sys.exit(1)
    OUT.write_text(json.dumps({"morning": morning, "evening": evening}, ensure_ascii=False, separators=(",", ":")),
                   encoding="utf-8")
    print(f"OK: {len(morning)} morning, {len(evening)} evening, title languages {['ru'] + sorted(overlays)}, "
          f"{OUT.stat().st_size // 1024} KB -> {OUT}")


if __name__ == "__main__":
    main()
