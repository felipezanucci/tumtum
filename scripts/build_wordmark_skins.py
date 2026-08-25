#!/usr/bin/env python3
"""Build the mutation skins by masking a texture inside the master wordmark.

The manual calls mutation a surface treatment on a fixed silhouette, never a
redesign. Doing it this way makes that literal: the shape comes from the
approved vector every time, so a skin cannot drift from the master and cannot
carry a halo, because everything outside the letterforms is transparent by
construction.

Textures are plain square images with no letters in them. Generating a
*wordmark* with an image model and tracing it back to vector is what produced
the unusable set documented in shared/brand/README.md.

    python scripts/build_wordmark_skins.py [--width 2400]
"""

import argparse
import sys
from pathlib import Path

import cairosvg
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
MASTER = ROOT / "shared" / "brand" / "tumtum-wordmark.svg"
TEXTURES = ROOT / "shared" / "brand" / "textures"
OUT = ROOT / "backend" / "app" / "assets" / "brand"


def silhouette(width: int) -> Image.Image:
    """The master letterforms as an alpha mask, rendered from the vector."""
    png = cairosvg.svg2png(url=str(MASTER), output_width=width)
    return Image.open(__import__("io").BytesIO(png)).convert("RGBA").getchannel("A")


def cover(texture: Image.Image, size: tuple[int, int]) -> Image.Image:
    """Scale the texture to fill the box without distorting it, then centre-crop."""
    tw, th = texture.size
    scale = max(size[0] / tw, size[1] / th)
    resized = texture.resize((round(tw * scale), round(th * scale)), Image.LANCZOS)
    left = (resized.width - size[0]) // 2
    top = (resized.height - size[1]) // 2
    return resized.crop((left, top, left + size[0], top + size[1]))


def build(texture_path: Path, width: int) -> Path:
    mask = silhouette(width)
    skin = cover(Image.open(texture_path).convert("RGB"), mask.size)
    skin.putalpha(mask)
    OUT.mkdir(parents=True, exist_ok=True)
    out = OUT / f"tumtum-wordmark-{texture_path.stem}.png"
    skin.save(out, optimize=True)
    return out


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--width", type=int, default=2400)
    args = ap.parse_args()

    textures = sorted(p for p in TEXTURES.glob("*") if p.suffix.lower() in {".png", ".jpg", ".jpeg"})
    if not textures:
        print(f"no textures in {TEXTURES}", file=sys.stderr)
        return 1
    for t in textures:
        out = build(t, args.width)
        print(f"  {t.name:<20} -> {out.name}  ({out.stat().st_size // 1024} KB)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
