# Skin textures

Square, full-bleed images of a material. **No letters, no logo, no border, no
drop shadow** — the shape comes from the master wordmark, never from here.

Drop a file in, run the builder, and it becomes a skin:

```
python scripts/build_wordmark_skins.py
```

The file name becomes the skin name: `zebra.png` produces
`backend/app/assets/brand/tumtum-wordmark-zebra.png`, the texture masked to the
master letterforms with everything outside them transparent.

## Why it is built this way

The manual calls mutation a surface treatment on a fixed silhouette, never a
redesign. Masking makes that literal — the shape is taken from the approved
vector on every build, so a skin cannot drift from the master and cannot carry a
halo. Verified: the generated silhouette matches the master to **zero pixels**.

The alternative — asking an image model for a *wordmark* in a material and
tracing the result back to vector — produced nine files that could not be
shipped. `../README.md` records what was wrong with them.

## Neon is not built here

Its identity is the glow *outside* the letters, which a mask by definition
removes. It belongs in code: a coloured fill plus a blurred copy of the master
path.
