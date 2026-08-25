# Decision log

Running record of what was decided, why, and what is still open. Details live in
the linked documents — this file is the index and the reasoning, not a diary.

---

## Where things stand — 2026-08-25

| Track | Status |
|---|---|
| **Hardware supplier** | J-Style **parked, not closed**. Both candidate bands failed validation; continuous raw PPG needs firmware customization at USD 15k NRE / 5,000 MOQ — premature for Phase 0. Awaiting their answer on a pilot batch. |
| **Path 2 — fans' own watches** | **Phase 1 validated in the field.** Live heart rate over Web Bluetooth, 1.004 readings/s, self-healing reconnection, R-R intervals. |
| **Backend** | Railway trial had expired and paused all services; upgraded to Hobby, `/health` responding again. |
| **Frontend** | Deployed on Vercel via its native git integration, on **tumtum.cc**. Preview builds work per branch. |
| **Brand** | MVP v0.1 manual adopted and live: black canvas, Acid Lime, Instrument Sans, the official Chosmos wordmark. Mutation skins **parked**. |
| **Share loop** | Card 01 built to the manual, at Story size and inside the safe areas. Shared links open a public page and render a preview. |
| **Pilot (Tasha & Tracie, 2026-09-25)** | On track and **decoupled from the supplier decision**. |

### Open items

1. ~~**Merge PR #11**~~ — done, along with #12 through #17. `main` carries
   everything; nothing is queued.
2. ~~**End-to-end save test**~~ and ~~**detection accuracy**~~ — both done
   2026-08-25, both passed.
3. **J-Style reply** — sent 2026-08-25; awaiting their answer on a pilot batch
   with the NRE credited against a future volume order.
4. **Veepoo** — contacted 2026-08-18, still the primary alternative if they
   confirm native raw PPG / R-R streaming.
5. ~~**Deployment protection**~~ — resolved 2026-08-25: it was never a
   protection setting, only the wrong URL. The field test runs on
   **https://tumtum.cc**.
6. **Pilot logistics** — who the 3–5 people are, which event, who carries the
   strap. The product side is closed; what remains is organising.
7. **Mutation skins** — parked 2026-08-25. Masking a texture inside the master
   works and is built; the textures need to be fine enough to read inside a
   letterform. Nothing depends on this.
8. **Confirm Railway actually redeploys on a push to `main`.** The GitHub
   Actions deploy job has never worked (see below), so this has always been
   implicit. Verify before relying on it.
9. **CI and deploy cleanup** — no ESLint config, no `test` script, 25
   unformatted backend files, and a `Deploy` workflow that has failed 12/12
   times. All pre-date this work; all worth one dedicated PR.

---

## 2026-08-25 — the brand lands, and the share loop turns out to be broken

The MVP v0.1 brand manual arrived and replaced everything visual: black canvas,
Acid Lime as the primary accent, Toxic Yellow second, Instrument Sans, the
official Chosmos wordmark. Nothing from the red/cyan palette survived. Adopted
across 40 files, then the wordmark from the approved vector, checked before use.

**Three things a find-and-replace would have got wrong.** Error styling shared a
token with brand emphasis — Acid Lime cannot mean "this failed", so failures
moved to a functional red outside the palette. White on Acid Lime is 1.19:1, so
every accent surface took black text. And the card's curve fill turned out to be
a latent bug: drawn with an alpha component onto an RGB surface, where Pillow
discards it, so the intended 12% wash had always been a solid slab — and
swapping the palette would have made it tint red, from the red channel of a
green.

**The mutation skins were rejected on measurement, not taste.** Nine
auto-traced files, each carrying an opaque cream halo, 271 to 2,569 paths
against the master's one, and up to 27% proportion drift. Clipping them to the
master silhouette was tried and cuts the letters apart. The route that works is
the inverse — mask a letter-free texture inside the master — and it is built and
verified to zero pixels of deviation. Parked anyway: a texture scaled to cover
the whole wordmark reads as a blob. `shared/brand/README.md` holds the detail.

### The share link had never worked

Building the link preview surfaced something larger. Every shared card pointed
at `/cards/{id}` — **a route that did not exist**. A card posted to WhatsApp
took the person to a login wall. The missing preview was the smaller half of the
problem.

There is now a public card page, a deliberately narrow public endpoint that
returns only what the image already shows, and Open Graph metadata pointing at a
**separate 1200×630 layout** — a 9:16 card dropped into a preview slot is
cropped by every platform, and squeezing the portrait layout into 630px
collapsed the text into itself.

### And the card was posting under Instagram's interface

Card 01 was built to the manual — the data dominates, no chart, wordmark small.
Then a question about sizing prompted a check nobody had run: a Story is
displayed inside someone else's UI, and the card put its wordmark and its entire
event footer exactly where the profile header and reply bar sit. Posted, it lost
the brand, the event, the date and the handle — leaving a number with no context
on the one surface the product depends on for growth.

### A process note worth keeping

Three separate commits missed their merge today because they were pushed after
the PR was opened, and the merge took the earlier head. One was only recovered
from the reflog. **Push, then open the PR** — and after any merge, diff the
branch against `main` before assuming it landed.

---

## 2026-08-25 — tumtum.cc is live, and the pilot's last infrastructure item closes

The domain was already owned; it is now pointed at the production deployment,
apex canonical with `www` redirecting to it. Verified end to end from an
anonymous tab on the A17: `tumtum.cc` opened with no Vercel login wall, sign-in
worked, a two-minute capture saved, and the experience view rendered.

**What this settles.** The 2026-09-25 field test needed a URL that 3–5 people
could open without a Vercel account. Preview URLs are behind Vercel
authentication by design — that was the friction all day, and it was never a
protection setting to fight, just the wrong URL. Production was always open.

The apex is canonical deliberately: a shared card link reads `tumtum.cc/cards/…`
rather than `www.tumtum.cc/cards/…`, and flipping that later would strand every
link already in circulation.

Backend CORS lists both apex and www. Checked that a lookalike suffix, plain
http and an unexpected subdomain are all still rejected.

**Left alone on purpose:** the domain's MX records point at Google Workspace.
Anyone tidying "leftover" DNS would take the email down with them.

### One open product question

The two-minute verification capture reported a peak of 88 bpm lasting 6 s, just
past the 5 s minimum. The curve shows a real rise of roughly 26 bpm from a
resting ~62, so this is the detector finding something that genuinely happened —
standing, moving, the strap settling — not inventing a peak out of a flat line.

It does raise a question the pilot has not answered: **what does the app show
someone whose heart genuinely does not move all night?** The detector always
returns the largest relative rise in whatever it is given, so a truly
uneventful session would still be presented as a "Pico de Emoção". Worth
deciding before 2026-09-25 whether such a session should say so instead — a
minimum absolute excursion below which the honest answer is "your heart stayed
calm tonight". Untested either way; nobody has yet captured a session flat
enough to find out.

---

## 2026-08-25 — Path 2 phase 1 gate: met

A session went end to end on the deployed app: Polar H10 → Web Bluetooth
capture (306 readings, 100% coverage, quality Aprovado) → crash recovery from
localStorage → save → peak detection (**1 peak, 145 bpm at 10:29, 30 s**) →
experience view with the curve and a card offered.

**The pilot no longer depends on a hardware supplier.** That was the whole point
of opening Path 2 when J-Style closed, and it is now demonstrated rather than
assumed.

**Detection accuracy confirmed the same day.** A clean 7-minute two-effort run
returned exactly two peaks, 136 bpm at 11:22 and 146 bpm at 11:25, matching both
hand-noted effort endings to the minute. The protocol was designed against the
detector's real parameters by simulating five candidates 20 times each rather
than guessing a duration — worth repeating whenever the algorithm's windows
change, since a 7-minute test against a 300 s baseline window is not obviously
safe until you check.

**Phase 1 is closed on both counts: the pipeline runs, and the detector is
calibrated.**

Four defects found and fixed in the process (PR #11). The one worth remembering
beyond this bug: **a 500 carried no CORS header**, so every server-side error
this app has ever produced reached the user as "the server is unreachable" —
including this one, which sent the investigation to the wrong layer for a while.
Starlette raises its 500 outside all user middleware; a catch-all
`exception_handler` does not fix it, because that is served outside CORS too.
The trap has to be a middleware registered before `CORSMiddleware`.

→ `docs/path-2-roadmap.md` phase 1 · PR #11

---

## 2026-08-25 — The Deploy workflow has never worked

Merging PR #10 to `main` was supposed to deploy the frontend and the backend.
The `Deploy` workflow ran and failed — as it has on **every one of its 12 runs
since April 2026**. Both jobs, every time:

| Job | Failure |
|---|---|
| Deploy Frontend (Vercel) | `Input required and not supplied: vercel-token` — the `VERCEL_TOKEN` repository secret was never set |
| Deploy Backend (Railway) | `Unexpected input(s) 'railway_token'` and `railway: not found` — `bervProject/railway-deploy` changed its interface and no longer takes a token input or ships the CLI |

Neither is a regression. The pipeline was written, committed, and never once
succeeded.

**What has actually been deploying.** Vercel, through its own GitHub App —
which is why PR #10 got a working preview URL while the Vercel job in the same
run was failing. Railway is the open question: yesterday's recovery needed a
manual redeploy, which suggests its GitHub integration may not be connected at
all.

**Why this matters beyond tidiness.** Every deploy has been implicit, so
"merged to main" has never meant "live". That assumption is exactly what would
break on 2026-09-25 with people standing in a venue.

**Resolved 2026-08-25, later the same day.** The Railway dashboard settled the
open question: its deployment of the PR #10 merge is listed as **"via GitHub"**,
so Railway's own integration works and the Actions job was never needed.
Vercel's job was redundant with its GitHub App the same way. `deploy.yml` is
**deleted** — repairing a pipeline that duplicates two working integrations, and
that has never once succeeded, would only give us a second thing to keep alive.

The CI cleanup landed with it. See the entry above.

---

## 2026-08-25 — J-Style: park, do not close

They confirmed our findings in writing and offered firmware customization:
**USD 15,000 NRE, 5,000-unit MOQ**.

**Decision: park.** The decisive number is the MOQ, not the NRE — at USD 40–80
per unit that is USD 200–400k of inventory committed *before* Phase 0 validates
that people want this at all. Firmware customization also runs 2–4 months, past
the pilot, and the continuous-streaming firmware does not exist yet, so
technical risk survives payment until an acceptance test proves otherwise.

Two questions asked before archiving: whether a 50–100 unit pilot batch with the
custom firmware is possible, and whether the NRE can be credited against a
future volume order. **The reply went out on 2026-08-25**, putting both as a
single combined proposal rather than two separate doors, and deliberately
omitting the graceful-exit paragraph from the draft — so the offer stays on the
table instead of handing over a way to decline it. Awaiting her answer.

Nothing in the 2026-09-25 pilot depends on that answer. Path 2 carries it.

→ `docs/jstyle-v8-evaluation.md` §6b · `docs/jstyle-email-draft.md`

---

## 2026-08-24 — Two verdicts in one day

### The V8 failed all three acceptance criteria

Eight bench runs on the Samsung A17, closing with a Polar H10-referenced
protocol: amplitude ratio **0.34** (needs ≥0.85), peak delay **+42 to +54 s**
or never reached (needs ≤5 s), MAE **17.5** (needs ≤5). Through 30 s of maximum
effort the device reported a flat 78–82 bpm while the reference climbed to 128.

Raw PPG proved **not reproducible**: zero packets across three independent
command configurations and 32 measurement commands.

**Method note worth keeping:** the two series were aligned by *absolute wall
clock*, deliberately rejecting MAE-minimising cross-correlation. With a device
whose defect *is* a systematic lag, that method absorbs the lag into the fitted
offset and hides it — it would have reported a spurious +19 to +25 s "clock
offset". Any future candidate is aligned the same way.

→ `docs/jstyle-v8-evaluation.md`

### Path 2, Phase 1 built and validated

Live capture over the standard Bluetooth Heart Rate Service (0x180D), reached
with **no native code**. Validated by replaying the 539 real GATT frames the
Polar emitted during the protocol: this parser and `tumtum_ble.py` agree on
100% of them, including all 835 R-R intervals. Then in the field: 240 readings
in 239 s, reconnection recovering on its own after the strap was removed.

**Decision that shaped the roadmap:** take the free win before investing in
native. A PWA cannot read HealthKit or Health Connect — but it *can* speak
standard BLE on Android Chrome, which validates the whole pipeline end to end
and, with the file import, carries the 25/09 pilot without app-store review.

→ `docs/path-2-roadmap.md`

### Three defects only a real device exposed

- **Tailwind had never compiled in any deployment.** No `postcss.config.js`, so
  Next.js never ran the PostCSS plugin: `@tailwind` directives were emitted
  verbatim, no utility classes generated, and the build succeeded anyway. The
  app had rendered as unstyled HTML since April.
- **Browsers that expose `navigator.bluetooth` without implementing the chooser**
  (Samsung Internet) left the UI on "Conectando..." forever. Now detected up
  front, with an 8-second watchdog behind it.
- **The elapsed clock restarted on every reconnection** — the monitor captures
  its state callback once, so the guard read a frozen `startedAt`.

### Infrastructure archaeology

- The **GitHub Actions deploy workflow has never worked** — 11 runs, all failed,
  since April. Deployment happens through Vercel's own git integration, which is
  why the app was live regardless.
- **Railway's trial had expired**, pausing all three services. Upgraded to Hobby
  rather than migrating: free tiers sleep after ~15 minutes idle, and a
  50-second cold start during a live event is unacceptable.

---

## 2026-08-20/21 — The SDK arrives, and the second path opens

J-Style confirmed in writing that the motion filter is firmware and **cannot be
disabled** — no fast-response mode. That closed Path A (processed BPM).

Analysis of the ~150 MB SDK drop found that the V8 Android SDK exposes **raw PPG
streaming** (`setECGRealtimeDuringHRVEnabled(true)` during an HRV measurement),
which would bypass the firmware algorithm entirely. That became Path B, and the
spike kit was built to test it: an instrumented vendor demo, a CI-built APK
published to the `spike-apk` release, and `analyze_ppg.py`.

Also found: the SDK is a frame assembler only, its documentation is
machine-translated and self-contradictory, and R-R constants exist in every
parser with no method to activate them.

---

## 2026-08-17 — Both bands fail, and the strategy splits

GATT inspection: neither the V8 nor the 2208A exposes the standard Heart Rate
Service (0x180D). The V8's Phase 1 protocol failed against the Polar H10, with
the diagnosis that mattered: **accelerometer-conditioned motion gating**.
Stationary, the firmware treats a fast rise as artefact and clamps the ramp;
in motion it releases tracking. MAE < 2 in steady state — the sensor is good,
the firmware is not.

**Two decisions came out of this:**

1. **The stationary condition became eliminatory** in the validation protocol.
   It is the product's core case: an emotional peak with the body still.
2. **Path 2 was born** — heart rate from fans' own watches — explicitly to
   decouple the pilot from the supplier crisis.

Veepoo contacted 2026-08-18 as the alternative with native raw data.

---

## Reusable knowledge

- **The validation protocol**: three blocks of 60 s rest / 30 s maximum effort /
  90 s seated recovery, against a Polar H10, in **two conditions** (stationary
  and moving). Criteria: amplitude ratio ≥ 0.85, peak delay ≤ 5 s, MAE ≤ 5.
  Stationary is eliminatory. Every candidate source passes through it — a band,
  a watch, or our own capture layer.
- **Alignment uses absolute clocks**, never MAE minimisation. See 2026-08-24.
- **J-Style protocol facts** (measurement duration in seconds, 8–9 s sensor
  warm-up, a start command being a no-op while a session is active) are recorded
  in `docs/jstyle-v8-evaluation.md` §4.3, should that relationship resume.
