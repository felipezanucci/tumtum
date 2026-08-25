# Brand assets

## `tumtum-wordmark.svg` — the master. Use this one.

The official Chosmos wordmark. Verified before it entered the repo: one path,
one solid fill, no `<text>` element, no background rect, no filters, and a
viewBox tight to the artwork within a single unit.

Consumed as:

- `frontend/components/brand/Wordmark.tsx` — inlined with `fill="currentColor"`,
  so black and white both come from this one file. The forbidden Acid Lime and
  Toxic Yellow versions are deliberately not reachable by passing a class.
- `backend/app/assets/brand/tumtum-wordmark-white.png` — pre-rendered for card
  compositing, scaled by height so the proportions are never touched.

Nothing may redraw, stretch or slant it.

## The nine mutation skins — removed 2026-08-25

They were briefly in this folder and are gone. Kept here is the measurement,
so nobody commissions the same thing twice.

What was wrong with them:

| Check | Master | The nine skins |
|---|---|---|
| Paths per file | 1 | 271 – 2,569 |
| Opaque near-white geometry around the letters | none | **all nine** |
| Aspect ratio | 6.62:1 | 4.82:1 – 6.38:1 (−3.7% to −27.3%) |

Composited over a saturated background, every skin shows a ragged cream halo:
opaque leftover background, not transparency. The path counts and the drifted
proportions are the signature of a raster image auto-traced back into vector,
which the brand manual excludes as a master source — and the letterforms are
not the master's, so clipping them to the master silhouette does not rescue
them either. That was tried; it cuts the letters apart.

**The route that works** is the inverse: an image generator produces a square
full-bleed *texture* containing no letters at all, and the texture is applied
inside the master letterforms as a mask — `scripts/build_wordmark_skins.py`,
verified to reproduce the master silhouette to zero pixels. Neon is the
exception: its identity is the glow outside the letters, so it belongs in code
as a coloured fill plus a blurred copy of the master path.

**Parked 2026-08-25 before any skin shipped.** The masking works, but a skin is
only as good as the texture's scale: a pattern sized to cover the whole wordmark
reads as a blob, because each stripe ends up the size of a letter. The texture
has to be fine enough to read *inside* a single letterform. Worth revisiting
after the 2026-09-25 pilot — the manual treats Base/Solid as the institutional
default, so nothing depends on this.
