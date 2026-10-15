#!/usr/bin/env python3
"""Walks the debug app on a running emulator, checks it does not crash, and saves store screenshots.

Used by the "screenshots" job in .github/workflows/build.yml. Needs adb on PATH and an emulator
with a 16:9 screen (Google Play rejects screenshots whose long side is more than twice the short one).

    python3 tools/screenshots.py --apk androidApp/build/outputs/apk/debug/androidApp-debug.apk --out screenshots
"""
import argparse
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

PKG = "app.bibliya.debug"
ACTIVITY = "app.bibliya.android.MainActivity"

# What to tap on each screen, by visible text or accessibility label, per interface language.
TEXT = {
    "ru": {
        "home": "Главная", "situations": "Что читать", "bible": "Библия", "saved": "Сохранённое", "more": "Ещё",
        "example": "мне тревожно", "situation": "Тревога и страх", "open": "Открыть главу", "back": "Назад",
        "plan_start": "Начать план", "plan": "Библия за год", "dark": "Тёмная", "book": "Псалтирь", "chapter": "22",
    },
    "en": {
        "home": "Home", "situations": "What to read", "bible": "Bible", "saved": "Saved", "more": "More",
        "example": "I feel anxious", "situation": "Anxiety and fear", "open": "Open chapter", "back": "Back",
        "plan_start": "Start the plan", "plan": "Bible in a year", "dark": "Dark", "book": "Psalms", "chapter": "23",
    },
}


def adb(*args, check=True, capture=True):
    r = subprocess.run(["adb", *args], capture_output=capture, text=True)
    if check and r.returncode != 0:
        raise RuntimeError(f"adb {' '.join(args)} failed: {r.stderr.strip()}")
    return r.stdout if capture else ""


def shell(cmd, check=True):
    return adb("shell", cmd, check=check)


class Driver:
    def __init__(self, out):
        self.out = out
        os.makedirs(out, exist_ok=True)

    def alive(self):
        return shell(f"pidof {PKG}", check=False).strip() != ""

    def assert_alive(self, where):
        if not self.alive():
            crash = shell("logcat -d -b crash", check=False)
            raise RuntimeError(f"App is not running after: {where}\n{crash[-4000:]}")

    def dump(self):
        for _ in range(5):
            shell("rm -f /sdcard/ui.xml", check=False)
            shell("uiautomator dump /sdcard/ui.xml", check=False)
            xml = adb("exec-out", "cat /sdcard/ui.xml", check=False)
            if xml.strip().startswith("<?xml"):
                return ET.fromstring(xml)
            time.sleep(1)
        raise RuntimeError("uiautomator dump failed")

    def find(self, label, exact=True):
        root = self.dump()
        for node in root.iter("node"):
            for attr in ("text", "content-desc"):
                value = node.get(attr, "")
                if (value == label) if exact else (label in value):
                    m = re.findall(r"\d+", node.get("bounds", ""))
                    if len(m) == 4:
                        x1, y1, x2, y2 = map(int, m)
                        return (x1 + x2) // 2, (y1 + y2) // 2
        return None

    def wait_for(self, label, timeout=40, exact=True):
        end = time.time() + timeout
        while time.time() < end:
            pos = self.find(label, exact)
            if pos:
                return pos
            self.assert_alive(f"waiting for '{label}'")
            time.sleep(1)
        raise RuntimeError(f"'{label}' did not appear on screen")

    def tap(self, label, exact=True, timeout=40):
        x, y = self.wait_for(label, timeout, exact)
        shell(f"input tap {x} {y}")
        time.sleep(1.2)
        self.assert_alive(f"tapping '{label}'")

    def scroll_to(self, label, tries=25):
        for _ in range(tries):
            if self.find(label):
                return
            shell("input swipe 540 1500 540 700 300")
            time.sleep(0.6)
        raise RuntimeError(f"could not scroll to '{label}'")

    def back(self):
        shell("input keyevent KEYCODE_BACK")
        time.sleep(1)
        self.assert_alive("pressing back")

    def shot(self, name):
        time.sleep(1.5)
        path = os.path.join(self.out, f"{name}.png")
        with open(path, "wb") as f:
            f.write(subprocess.run(["adb", "exec-out", "screencap", "-p"], capture_output=True, check=True).stdout)
        print(f"saved {path}")


def demo_status_bar():
    """A clean status bar: fixed time, full battery, no notification icons."""
    shell("settings put global sysui_demo_allowed 1")
    for cmd in (
        "-e command enter",
        "-e command clock -e hhmm 0900",
        "-e command battery -e level 100 -e plugged false",
        "-e command network -e wifi show -e level 4 -e mobile show -e datatype none -e level 4",
        "-e command notifications -e visible false",
    ):
        shell(f"am broadcast -a com.android.systemui.demo {cmd}", check=False)


def fresh_start(lang):
    shell(f"pm clear {PKG}")
    shell(f"pm grant {PKG} android.permission.POST_NOTIFICATIONS", check=False)
    # Per-app language (Android 13+): the app follows it like a phone set to that language.
    shell(f"cmd locale set-app-locales {PKG} --locales {'ru-RU' if lang == 'ru' else 'en-US'}")
    shell("logcat -c", check=False)
    shell(f"am start -W -n {PKG}/{ACTIVITY}")


def walk(d, lang):
    t = TEXT[lang]
    fresh_start(lang)
    d.wait_for(t["home"], timeout=60)
    d.shot(f"{lang}-1-home")

    # "What should I read?" search with a natural phrase.
    d.tap(t["example"])
    d.wait_for(t["situation"], exact=False)
    d.shot(f"{lang}-2-search")
    d.back()

    # Situations grid, then one situation with its passages.
    d.tap(t["situations"])
    d.shot(f"{lang}-3-situations")
    d.tap(t["situation"])
    d.shot(f"{lang}-4-situation")

    # The full chapter in the reader (waits for the first-launch import of the Bible text).
    d.tap(t["open"])
    time.sleep(8)
    d.shot(f"{lang}-5-reader")
    d.back()
    d.back()

    # Bible in a year.
    d.tap(t["home"])
    d.scroll_to(t["plan_start"])
    d.tap(t["plan_start"])
    d.tap(t["plan"])
    d.shot(f"{lang}-6-plan")
    d.back()

    # Books list and a psalm, read from the Bible tab.
    d.tap(t["bible"])
    d.shot(f"{lang}-7-books")
    d.scroll_to(t["book"])
    d.tap(t["book"])
    d.scroll_to(t["chapter"])
    d.tap(t["chapter"])
    time.sleep(3)
    d.shot(f"{lang}-8-psalm")
    d.back()
    d.back()

    # Saved and settings; then dark theme on the home screen.
    d.tap(t["saved"])
    d.shot(f"{lang}-9-saved")
    d.tap(t["more"])
    d.shot(f"{lang}-10-more")
    d.tap(t["dark"])
    d.tap(t["home"])
    d.shot(f"{lang}-11-home-dark")
    d.assert_alive("the whole walk")


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--apk", required=True)
    p.add_argument("--out", default="screenshots")
    p.add_argument("--langs", default="ru,en")
    a = p.parse_args()

    adb("install", "-r", "-g", a.apk)
    demo_status_bar()
    d = Driver(a.out)
    failed = []
    for lang in a.langs.split(","):
        try:
            walk(d, lang)
        except Exception as e:  # keep going so the other language still gets its screenshots
            print(f"::error::{lang}: {e}")
            d.shot(f"{lang}-FAILED")
            failed.append(lang)
    with open(os.path.join(a.out, "logcat.txt"), "w") as f:
        f.write(shell("logcat -d -v time", check=False))
    if failed:
        sys.exit(1)


if __name__ == "__main__":
    main()
