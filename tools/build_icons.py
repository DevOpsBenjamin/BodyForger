"""Generate every Android launcher icon for BodyForger from the forge-hammer mark.

    python3 tools/build_icons.py

The SVG stays the local source of truth (`assets/ic_launcher_master.svg`); Android only
ever receives PNGs. This script is macOS-only: it rasterises through `qlmanage`.

`qlmanage` is a thumbnailer, not a rasteriser, and always flattens onto opaque white.
Transparency is therefore recovered by rendering twice, once over white and once over
black. For a colour C with coverage a:

    over white: Cw = C*a + (1 - a)        over black: Cb = C*a

so a = 1 - (Cw - Cb) and C = Cb / a. The inversion is exact, antialiased edges included,
and averaging the three channels absorbs rounding noise.

Outputs, for every density and both modules:

  * `ic_launcher.png`            - legacy icon, rounded square, transparent corners
  * `ic_launcher_round.png`      - circular variant, transparent outside the disc
  * `ic_launcher_foreground.png` - foreground layer of the adaptive icon
  * `ic_launcher_monochrome.png` - silhouette for Android 13+ themed icons

An adaptive icon is drawn on 108 dp but only 72 dp are shown, and only the central 66 dp
are guaranteed. So that launchers display exactly the artwork approved at 512, the 512
canvas is centred inside a 768-unit box: 512/768 = 72/108. The hammer keeps its original
proportions and its 57 dp diagonal stays inside the 66 dp safe circle.
"""
from __future__ import annotations

import math
import re
import shutil
import struct
import subprocess
import sys
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
WORK = ROOT / "build" / "icons-tmp"

BACKGROUND = "#050608"
CORNER_RADIUS = 112          # out of 512, i.e. 21.9% - the radius of the approved artwork

# Forge hammer, drawn in the 512 frame, before the pose transform is applied.
PATH_D = ("M 140 152 L 230 132 L 324 132 Q 340 132 340 148 L 340 212 Q 340 228 324 228 "
          "L 302 228 L 298 396 Q 310 402 310 418 L 310 430 Q 310 446 294 446 L 246 446 "
          "Q 230 446 230 430 L 230 418 Q 230 402 242 396 L 238 228 L 230 228 L 140 208 "
          "Q 130 201 130 180 Q 130 159 140 152 Z")
POSE = "translate(21,-33) rotate(-15 235 289)"

# Green core over the hammer eye, neon yellow radiating outwards. The ellipse is
# stretched along the handle so the gradient reads as concentric rather than diagonal.
GRADIENT = """  <defs>
    <radialGradient id="g" gradientUnits="userSpaceOnUse" cx="266" cy="238" r="170"
        gradientTransform="translate(266 238) scale(1 1.45) translate(-266 -238)">
      <stop offset="0"    stop-color="#4FD95E"/>
      <stop offset="0.28" stop-color="#8CEC3A"/>
      <stop offset="0.56" stop-color="#F5FF00"/>
      <stop offset="1"    stop-color="#FBFF6E"/>
    </radialGradient>
  </defs>"""

DENSITIES = {"mdpi": 1.0, "hdpi": 1.5, "xhdpi": 2.0, "xxhdpi": 3.0, "xxxhdpi": 4.0}
LEGACY_DP, ADAPTIVE_DP = 48, 108
MODULES = ("app-mobile", "app-wear")

FULL_BLEED = "0 0 512 512"
ADAPTIVE = "-128 -128 768 768"      # 512 centred in 768: 72 dp visible out of 108


# --------------------------------------------------------------------------- SVG

def compose(view_box: str, underlay: str, shape: str, fill: str, defs: str) -> str:
    """Assemble one SVG. `underlay` spans the whole box: it carries the white or black
    backing from which the alpha channel is later derived."""
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{view_box}" '
            f'width="512" height="512">\n{underlay}\n{defs}\n{shape}\n'
            f'  <g transform="{POSE}"><path d="{PATH_D}" fill="{fill}"/></g>\n</svg>\n')


def underlay_for(view_box: str, colour: str) -> str:
    x, y, width, height = view_box.split()
    return f'  <rect x="{x}" y="{y}" width="{width}" height="{height}" fill="{colour}"/>'


VARIANTS = {
    # name                    view box      background shape                                    fill         base dp
    "ic_launcher": (
        FULL_BLEED,
        f'  <rect width="512" height="512" rx="{CORNER_RADIUS}" fill="{BACKGROUND}"/>',
        "url(#g)", GRADIENT, LEGACY_DP),
    "ic_launcher_round": (
        FULL_BLEED,
        f'  <circle cx="256" cy="256" r="256" fill="{BACKGROUND}"/>',
        "url(#g)", GRADIENT, LEGACY_DP),
    "ic_launcher_foreground": (
        ADAPTIVE, "", "url(#g)", GRADIENT, ADAPTIVE_DP),
    "ic_launcher_monochrome": (
        ADAPTIVE, "", "#FFFFFF", "", ADAPTIVE_DP),
}


# --------------------------------------------------------------------- PNG codec

def read_png(path: Path):
    blob = path.read_bytes()
    if blob[:8] != b"\x89PNG\r\n\x1a\n":
        sys.exit("%s is not a PNG" % path)
    pos, idat, meta = 8, bytearray(), None
    while pos < len(blob):
        (length,) = struct.unpack(">I", blob[pos:pos + 4])
        kind = blob[pos + 4:pos + 8]
        if kind == b"IHDR":
            width, height, depth, colour, _, _, interlace = struct.unpack(
                ">IIBBBBB", blob[pos + 8:pos + 21])
            if depth != 8 or interlace:
                sys.exit("unsupported PNG: %d-bit, interlace %d" % (depth, interlace))
            meta = (width, height, {0: 1, 2: 3, 4: 2, 6: 4}[colour])
        elif kind == b"IDAT":
            idat += blob[pos + 8:pos + 8 + length]
        elif kind == b"IEND":
            break
        pos += 12 + length
    width, height, channels = meta
    raw = zlib.decompress(bytes(idat))
    stride, previous, rows, at = width * channels, bytearray(width * channels), [], 0
    for _ in range(height):
        filt, line = raw[at], bytearray(raw[at + 1:at + 1 + stride])
        at += 1 + stride
        for i in range(stride):
            a = line[i - channels] if i >= channels else 0
            b = previous[i]
            c = previous[i - channels] if i >= channels else 0
            if filt == 1:
                line[i] = (line[i] + a) & 0xFF
            elif filt == 2:
                line[i] = (line[i] + b) & 0xFF
            elif filt == 3:
                line[i] = (line[i] + (a + b) // 2) & 0xFF
            elif filt == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                line[i] = (line[i] + (a if pa <= pb and pa <= pc
                                      else b if pb <= pc else c)) & 0xFF
        rows.append(bytes(line))
        previous = line
    return width, height, channels, rows


def write_png(path: Path, width: int, height: int, rows) -> None:
    def chunk(kind: bytes, data: bytes) -> bytes:
        return (struct.pack(">I", len(data)) + kind + data
                + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF))

    raw = b"".join(b"\x00" + bytes(row) for row in rows)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(b"\x89PNG\r\n\x1a\n"
                     + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
                     + chunk(b"IDAT", zlib.compress(raw, 9))
                     + chunk(b"IEND", b""))


# ------------------------------------------------------------------ rasterising

def rasterise(svg_text: str, name: str, pixels: int) -> Path:
    WORK.mkdir(parents=True, exist_ok=True)
    source = WORK / (name + ".svg")
    source.write_text(svg_text, encoding="utf-8")
    out_dir = WORK / name
    if out_dir.exists():
        shutil.rmtree(out_dir)
    out_dir.mkdir()
    subprocess.run(["qlmanage", "-t", "-s", str(pixels), "-o", str(out_dir), str(source)],
                   capture_output=True)
    produced = out_dir / (source.name + ".png")
    if not produced.exists():
        sys.exit("qlmanage produced nothing for %s at %d px" % (name, pixels))
    width, height, _, _ = read_png(produced)
    if (width, height) != (pixels, pixels):
        sys.exit("%s is %dx%d instead of %d" % (produced, width, height, pixels))
    return produced


def rasterise_rgba(view_box: str, shape: str, fill: str, defs: str,
                   name: str, pixels: int):
    """Render over white, then over black, and rebuild the alpha channel."""
    width, height, cw, rows_white = read_png(rasterise(
        compose(view_box, underlay_for(view_box, "#FFFFFF"), shape, fill, defs),
        name + "-w", pixels))
    _, _, cb, rows_black = read_png(rasterise(
        compose(view_box, underlay_for(view_box, "#000000"), shape, fill, defs),
        name + "-b", pixels))

    out = []
    for y in range(height):
        line_white, line_black, row = rows_white[y], rows_black[y], bytearray()
        for x in range(width):
            over_white = line_white[x * cw:x * cw + 3]
            over_black = line_black[x * cb:x * cb + 3]
            # a = 1 - (Cw - Cb), averaged over the three channels
            alpha = 255 - sum(over_white[i] - over_black[i] for i in range(3)) // 3
            alpha = max(0, min(255, alpha))
            if alpha == 0:
                row += b"\x00\x00\x00\x00"
            else:
                for i in range(3):
                    row.append(max(0, min(255, round(over_black[i] * 255.0 / alpha))))
                row.append(alpha)
        out.append(row)
    return width, height, out


# -------------------------------------------------------------------- geometry

def sample_path(d: str):
    """Walk the outline, sampling 24 points along every quadratic segment."""
    tokens = re.findall(r"[MLQZ]|-?\d+\.?\d*", d)
    out, cursor, i = [], (0.0, 0.0), 0
    while i < len(tokens):
        command = tokens[i]; i += 1
        if command == "Z":
            continue
        if command in ("M", "L"):
            cursor = (float(tokens[i]), float(tokens[i + 1])); i += 2
            out.append(cursor)
        elif command == "Q":
            cx, cy = float(tokens[i]), float(tokens[i + 1])
            ex, ey = float(tokens[i + 2]), float(tokens[i + 3]); i += 4
            sx, sy = cursor
            for step in range(1, 25):
                t = step / 24.0; u = 1 - t
                out.append((u * u * sx + 2 * u * t * cx + t * t * ex,
                            u * u * sy + 2 * u * t * cy + t * t * ey))
            cursor = (ex, ey)
    return out


def hammer_coverage(view_box: str) -> float:
    """Percentage of the canvas the hammer fills, via the shoelace formula."""
    points = sample_path(PATH_D)
    area = abs(sum(points[i][0] * points[(i + 1) % len(points)][1]
                   - points[(i + 1) % len(points)][0] * points[i][1]
                   for i in range(len(points)))) / 2.0
    side = float(view_box.split()[2])
    return 100.0 * area / (side * side)


def audit_geometry() -> None:
    angle = math.radians(-15.0)
    cos_a, sin_a = math.cos(angle), math.sin(angle)
    points = [(235.0 + (x - 235.0) * cos_a - (y - 289.0) * sin_a + 21.0,
               289.0 + (x - 235.0) * sin_a + (y - 289.0) * cos_a - 33.0)
              for x, y in sample_path(PATH_D)]

    reach = max(math.hypot(x - 256, y - 256) for x, y in points)
    x0, x1 = min(p[0] for p in points), max(p[0] for p in points)
    y0, y1 = min(p[1] for p in points), max(p[1] for p in points)
    diagonal_dp = math.hypot(x1 - x0, y1 - y0) * (72.0 / 512.0)

    print("Geometry")
    print("  furthest point from centre     : %.1f / 256  (%.0f%% of the disc)"
          % (reach, 100 * reach / 256))
    print("  diagonal on the adaptive canvas: %.1f dp  (safe zone: 66 dp)" % diagonal_dp)
    if reach >= 250:
        sys.exit("the hammer overflows the circular background")
    if diagonal_dp > 66:
        sys.exit("the hammer leaves the adaptive icon safe zone")


# --------------------------------------------------------------------- checking

def check_alpha(name: str, width: int, height: int, rows, floor: float) -> float:
    """Sanity-check the rebuilt alpha channel.

    The corner must be fully transparent and the artwork must cover a plausible share
    of the canvas. Probing one exact pixel does not work: the centre of the canvas
    falls on the handle outline, so it is only half covered."""
    corner = rows[0][3]
    if corner != 0:
        sys.exit("%s: opaque corner (alpha %d), transparency was lost" % (name, corner))

    opaque = sum(1 for row in rows for x in range(width) if row[x * 4 + 3] == 255)
    coverage = 100.0 * opaque / (width * height)
    if coverage < floor:
        sys.exit("%s: %.1f%% opaque pixels, below the %.1f%% floor" % (name, coverage, floor))
    return coverage


def main() -> int:
    audit_geometry()
    print("\nRendering")
    written = 0

    for name, (view_box, shape, fill, defs, base_dp) in VARIANTS.items():
        for density, factor in DENSITIES.items():
            pixels = int(round(base_dp * factor))
            width, height, rows = rasterise_rgba(view_box, shape, fill, defs, name, pixels)
            # The floor is derived from the hammer's real area rather than guessed: an
            # adaptive layer covers only 5.4% of its 768 canvas, and antialiasing eats
            # into that. Below 60% of the expected value, the render has failed.
            floor = 0.6 * hammer_coverage(view_box) if shape == "" else 60.0
            coverage = check_alpha("%s/%s" % (name, density), width, height, rows, floor)
            for module in MODULES:
                write_png(ROOT / module / "src/main/res" / ("mipmap-" + density)
                          / (name + ".png"), width, height, rows)
                written += 1
            print("  %-24s %-8s %4d px   %5.1f%% opaque" % (name, density, pixels, coverage))

    # Play Store artwork is a full square with no rounding: Google applies its own mask.
    store = ROOT / "assets" / "playstore_icon_512.png"
    view_box, _, fill, defs, _ = VARIANTS["ic_launcher"]
    width, height, rows = rasterise_rgba(
        view_box, f'  <rect width="512" height="512" fill="{BACKGROUND}"/>',
        fill, defs, "playstore", 512)
    write_png(store, width, height, rows)
    print("\n  playstore_icon_512.png   512 px, %.0f KB" % (store.stat().st_size / 1024))

    # Local sources. The Play Store SVG must stay the exact twin of its PNG.
    for filename, shape in (
            ("ic_launcher_master.svg", VARIANTS["ic_launcher"][1]),
            ("playstore_icon_512.svg", f'  <rect width="512" height="512" fill="{BACKGROUND}"/>')):
        (ROOT / "assets" / filename).write_text(
            compose(FULL_BLEED, "", shape, "url(#g)", GRADIENT), encoding="utf-8")
        print("  %-24s local source" % filename)

    print("\n%d PNGs written (%s)" % (written, ", ".join(MODULES)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
