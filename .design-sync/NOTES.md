# Claude Design sync — repo notes

## The shape this repo actually is

`frontend` is a **private Next.js app**, not a published component library. The
converter's package shape assumes a built `dist/` with a `.d.ts` tree; this repo
has neither and never will, because `next build` emits an app. Everything below
exists to bridge that gap **without changing a single component**.

Four obstacles were hit and solved, in this order — a future sync that breaks
will almost certainly be one of them regressing:

1. **No package entry.** `node_modules/tumtum-frontend` does not exist (npm
   won't self-install), so the build crashed reading its `package.json`.
   Fixed with `cfg.entry` pointing at a generated barrel, `frontend/.ds-entry.ts`.
2. **Default exports.** Components are `export default function Button(...)`,
   Next.js style. `export * from` re-exports nothing from those, so the first
   build found **0 components**. The barrel names them
   (`export { default as Button } from ...`) and also `export *`s the files that
   use named exports. **Regenerate the barrel whenever a component is added** —
   see the snippet at the bottom.
3. **Tailwind, not a stylesheet.** `app/globals.css` is 18 lines of `@tailwind`
   directives; all real styling is utilities compiled at build time. Shipping it
   would have shipped no styles at all. `cfg.cssEntry` points at
   `frontend/.ds-styles.css`, **which must be compiled before every build**:
   `npx tailwindcss -i app/globals.css -o .ds-styles.css --minify`.
4. **`process is not defined`.** The app reads `process.env.NEXT_PUBLIC_*`; the
   browser has no `process`, so the first module to touch it threw and took the
   whole bundle down — all 23 components rendered empty. `ds-process-shim.ts`
   defines inert values and is deliberately **the first import in the barrel**,
   because ESM evaluates imports in declaration order. Do not reorder it.
   (Its filename has no leading dot on purpose: esbuild fails to resolve
   `./.ds-process-shim`, treating the dotted name as its own extension.)

## Also worth knowing

- **`dtsPropsFor` carries all 23 contracts by hand.** ts-morph cannot trace prop
  types through a default re-export, so without it every `.d.ts` emitted
  `[key: string]: unknown` — a contract that tells the design agent nothing and
  makes it misuse every component. 13 were extracted from `interface <Name>Props`
  (with local type aliases resolved to their real unions); the other 10 use
  inline destructured types and were written out from the signatures.
  **A prop added to a component does not reach the sync until this is updated.**
- **Playwright must be 1.56.0.** The render check needs a chromium matching the
  one preinstalled at `/opt/pw-browsers` (build 1194). 1.55 wants 1187 and
  fails. Install with `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`; never run
  `npx playwright install`.
- **Previews render on a white page.** Every component here is drawn for the
  black canvas `styles.css` sets on `<body>`, so each authored preview wraps its
  content in `bg-tumtum-black p-6`. Without it `Input` and `PasswordInput` are
  white-on-white and the render check calls them blank.

## Re-sync risks — what can silently go stale

- **The barrel and `componentSrcMap` are snapshots.** A new component appears in
  neither, so it is simply absent from the sync with no error. Regenerate both.
- **`dtsPropsFor` duplicates the source.** A renamed or added prop leaves the
  synced contract quietly wrong — worse than absent, because the agent trusts it.
- **`.ds-styles.css` is a build artifact.** Forget to recompile and the sync
  ships yesterday's utilities; a new class used by a component renders unstyled.
- **The real Instrument Sans face is not shipped.** `next/font/google` fetches
  it at build time into `.next/static/media/` under hashed names, so there is no
  stable file to bundle. `globals.css` now declares
  `--font-instrument-sans: 'Instrument Sans', system-ui, …` so previews render
  sans instead of falling all the way to serif — but **the previews are not in
  the brand face**. Proper fix: vendor the woff2 files and point `cfg.extraFonts`
  at an `@font-face` sheet.
- **Only 7 previews are authored** (Button, Badge, Card, Input, PasswordInput,
  Loading, Wordmark). The other 16 ship the honest floor card. Highest value
  still unauthored: `HRCurve`, `PeakMarker`, `TimelineBar`, `SoloCard`, `Modal`,
  `Avatar` — the pieces of the night screen, which is the product's payoff.
- **Nothing has been uploaded.** `DesignSync` could not authorize from a
  claude.ai/code session. The project does not exist yet, so `config.json` has
  no `projectId` and the next run is still a first-time sync.

## Regenerating the barrel and the maps

```bash
cd frontend && npx tailwindcss -i app/globals.css -o .ds-styles.css --minify
# then regenerate .ds-entry.ts and cfg.componentSrcMap / cfg.dtsPropsFor
# from components/**/*.tsx before running package-build.mjs
```
