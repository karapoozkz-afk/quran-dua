#!/usr/bin/env python3
"""Draws the prayer-position pictograms used in the lessons.

Each pose is a side-view figure facing the qibla (to the right) on a light card, built from joints and
drawn as thick round strokes: no face, no detail, the same style for every position. Women wear a long dress and a
khimar. One script writes both outputs, so the app and the web demo never drift:
  androidApp/src/main/res/drawable/pose_<key>.xml   (VectorDrawable)
  art/poses/<key>.svg                               (for the demo and docs)

Original drawings made for this project; no third-party artwork. Run: python3 tools/gen_poses.py
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FLOOR = 186

# Joints: head center, shoulder, elbow, wrist, hip, knee, ankle, toe. Proportions: torso 50, thigh 36,
# shin 34, upper arm 26, forearm 26, head r 11.
STAND = dict(head=(100, 38), s=(100, 61), e=(101, 88), w=(103, 113), h=(100, 110), k=(100, 146), a=(100, 178), t=(113, 180))
POSES = {
    "stand": STAND,
    "takbir": {**STAND, "e": (117, 74), "w": (113, 42)},
    "qiyam": {**STAND, "e": (103, 88), "w": (114, 103)},
    "ruku": dict(head=(135, 104), s=(120, 110), e=(99, 125), w=(77, 138), h=(70, 110), k=(71, 146), a=(70, 178), t=(83, 180)),
    "sujud": dict(head=(142, 172), s=(121, 154), e=(110, 168), w=(138, 182), h=(80, 138), k=(94, 177), a=(60, 179), t=(53, 183)),
    "jalsa": dict(head=(88, 76), s=(87, 99), e=(96, 127), w=(116, 150), h=(84, 150), k=(122, 175), a=(82, 177), t=(70, 182)),
}
POSES["salam"] = POSES["jalsa"]

# Women: hands to the shoulders at takbir, folded on the chest, a shallower ruku, a gathered sujud
# with the forearms on the floor, and sitting on the left hip with the feet out to the right.
POSES["takbir_w"] = {**STAND, "e": (117, 84), "w": (121, 60)}
POSES["qiyam_w"] = {**STAND, "e": (104, 92), "w": (117, 84)}
POSES["ruku_w"] = dict(head=(132, 92), s=(117, 100), e=(102, 120), w=(86, 136), h=(72, 112), k=(80, 146), a=(72, 178), t=(85, 180))
POSES["sujud_w"] = dict(head=(132, 174), s=(112, 160), e=(108, 180), w=(130, 182), h=(76, 150), k=(96, 178), a=(64, 180), t=(56, 183))
POSES["jalsa_w"] = dict(head=(84, 82), s=(83, 105), e=(92, 132), w=(112, 152), h=(80, 156), k=(118, 176), a=(130, 180), t=(140, 182))

WOMEN = {k for k in POSES if k.endswith("_w")}
SALAM_ARROWS = [
    "M100,56 Q118,46 128,60", "M122,57 L128,60 L130,53",  # to the right
    "M76,56 Q58,46 48,60", "M54,57 L48,60 L46,53",  # to the left
]


def circle(cx, cy, r):
    return f"M{cx - r},{cy} a{r},{r} 0 1,0 {2 * r},0 a{r},{r} 0 1,0 {-2 * r},0 Z"


def line(*pts):
    return "M" + " L".join(f"{x},{y}" for x, y in pts)


def flare(h, a, top, bottom):
    """A skirt panel from the hip to the ankle, wider at the hem."""
    (hx, hy), (ax, ay) = h, a
    dx, dy = ax - hx, ay - hy
    n = (dx * dx + dy * dy) ** 0.5
    px, py = -dy / n, dx / n
    pts = [(hx + px * top, hy + py * top), (ax + px * bottom, ay + py * bottom),
           (ax - px * bottom, ay - py * bottom), (hx - px * top, hy - py * top)]
    return "M" + " L".join(f"{x:.1f},{y:.1f}" for x, y in pts) + " Z"


def straight(p):
    """True when the leg is roughly straight (standing, ruku), so the dress hangs as a flared skirt."""
    (hx, hy), (kx, ky), (ax, ay) = p["h"], p["k"], p["a"]
    return abs((kx - hx) * (ay - hy) - (ky - hy) * (ax - hx)) < 400


def layers(key):
    """(kind, d, width, color): kind is 'fill' or 'stroke'; color is 'ink', 'mat' or 'gap'.
    A 'gap' stroke under a limb, in the card colour, keeps an arm readable in front of the body."""
    p = POSES[key]
    woman = key in WOMEN
    out = [("fill", f"M20,{FLOOR + 1} h160 v7 h-160 Z", 0, "mat")]  # prayer mat
    if woman:
        if straight(p):
            out.append(("fill", flare(p["h"], p["a"], 12, 19), 0, "ink"))
        else:
            out.append(("stroke", line(p["h"], p["k"], p["a"]), 21, "ink"))
        out.append(("stroke", line(p["s"], p["h"]), 25, "ink"))
    else:
        out.append(("stroke", line(p["h"], p["k"], p["a"]), 14, "ink"))
        out.append(("stroke", line(p["s"], p["h"]), 22, "ink"))
    out.append(("stroke", line(p["a"], p["t"]), 9, "ink"))
    hx, hy = p["head"]
    sx, sy = p["s"]
    if woman:  # khimar: head and a drape down to the shoulders
        out.append(("stroke", line((hx, hy), ((hx + sx) / 2, (hy + sy) / 2), (sx, sy)), 26, "ink"))
    arm = line(p["s"], p["e"], p["w"])
    out.append(("stroke", arm, 19, "gap"))
    out.append(("stroke", arm, 11, "ink"))
    out.append(("fill", circle(hx, hy, 12 if not woman else 14), 0, "ink"))
    if key == "salam":
        out += [("stroke", d, 4, "mat") for d in SALAM_ARROWS]
    return out


COLORS = {"ink": ("#1D5C4A", 1.0), "mat": ("#1D5C4A", 0.3), "gap": ("#F4EFE3", 1.0)}
BACKGROUND = "#F4EFE3"


def svg(key):
    parts = []
    for kind, d, w, c in layers(key):
        col, a = COLORS[c]
        if kind == "fill":
            parts.append(f'<path d="{d}" fill="{col}" fill-opacity="{a}"/>')
        else:
            parts.append(f'<path d="{d}" fill="none" stroke="{col}" stroke-opacity="{a}" stroke-width="{w}" stroke-linecap="round" stroke-linejoin="round"/>')
    bg = f'<rect width="200" height="200" rx="16" fill="{BACKGROUND}"/>'
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200" role="img">{bg}{"".join(parts)}</svg>\n'


def vector(key):
    parts = []
    for kind, d, w, c in layers(key):
        col, a = COLORS[c]
        if kind == "fill":
            parts.append(f'    <path android:pathData="{d}" android:fillColor="{col}" android:fillAlpha="{a}"/>')
        else:
            parts.append(
                f'    <path android:pathData="{d}" android:strokeColor="{col}" android:strokeAlpha="{a}"\n'
                f'        android:strokeWidth="{w}" android:strokeLineCap="round" android:strokeLineJoin="round"/>'
            )
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- Generated by tools/gen_poses.py; edit the script, not this file. -->\n"
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="200dp" android:height="200dp" android:viewportWidth="200" android:viewportHeight="200">\n'
        f'    <path android:pathData="M16,0 h168 a16,16 0 0 1 16,16 v168 a16,16 0 0 1 -16,16 h-168 a16,16 0 0 1 -16,-16 v-168 a16,16 0 0 1 16,-16 Z" android:fillColor="{BACKGROUND}"/>\n'
        + "\n".join(parts) + "\n</vector>\n"
    )


def main():
    drawable = ROOT / "androidApp/src/main/res/drawable"
    art = ROOT / "art/poses"
    drawable.mkdir(parents=True, exist_ok=True)
    art.mkdir(parents=True, exist_ok=True)
    for key in POSES:
        (drawable / f"pose_{key}.xml").write_text(vector(key))
        (art / f"{key}.svg").write_text(svg(key))
    print(f"{len(POSES)} poses written")


if __name__ == "__main__":
    main()
