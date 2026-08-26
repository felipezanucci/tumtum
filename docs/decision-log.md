# Decision log

Running record of what was decided, why, and what is still open. Details live in
the linked documents — this file is the index and the reasoning, not a diary.

---

## Where things stand — 2026-08-25

| Track | Status |
|---|---|
| **Hardware supplier** | J-Style **parked, not closed**. Both candidate bands failed validation; continuous raw PPG needs firmware customization at USD 15k NRE / 5,000 MOQ — premature for Phase 0. Awaiting their answer on a pilot batch. |
| **Android app (hybrid)** | **The sleep test passed.** 26,999 readings in 27,000 seconds — 99.996%, one per second for 7.5 hours with the screen off — on 7% of battery. Samsung did not kill the service. The capture path for the pilot is open; what remains is the ~27k-point upload and Health Connect. |
| **Path 2 — fans' own watches** | **Phase 1 validated in the field, and rehearsed on the phone.** A 25-minute capture recorded 1,504 readings in 1,504 seconds — one per second, nothing lost. Reconnection that never gives up, R-R intervals. |
| **Backend** | Railway trial had expired and paused all services; upgraded to Hobby, `/health` responding again. |
| **Frontend** | Deployed on Vercel via its native git integration, on **tumtum.cc**. Preview builds work per branch. |
| **Brand** | MVP v0.1 manual adopted and live: black canvas, Acid Lime, Instrument Sans, the official Chosmos wordmark. Mutation skins **parked**. |
| **Share loop** | Card 01 built to the manual, at Story size and inside the safe areas, generated from a real capture. Sharing opens the system sheet **with the image attached**. |
| **Polar as fallback** | **Working end to end.** A real Polar Flow export imports; the average it computes matches the one Polar wrote into the file. Beat → Flow sync is manual — pull down and hold. |
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
9. ~~**CI and deploy cleanup**~~ — done 2026-08-25 (#12), and the backend half
   finished 2026-08-25 (#20): its test step ended in `|| echo "No tests found
   yet"`, so nothing there could ever turn a check red.
10. **Realness Festival, 2026-08-29** — a six-hour drag festival in São Paulo,
    and the first capture longer than a few minutes. Prepared; see below. What
    is left is Felipe's: create the event, rehearse for five minutes at home
    recording on both the Polar app and TumTum, and send the Polar CSV so the
    importer can be checked against a file the device actually wrote rather
    than one reproduced from its documented shape.
11. **The screen wake lock is the one fix still unconfirmed.** It cannot be
    reproduced here and the rehearsal did not isolate it: the phone was being
    handled throughout. What was confirmed is that a capture survives leaving
    the app and coming back. What is still untested is the screen staying lit
    on its own.
12. **The end-of-night upload is 1.33 MB in a single request.** Measured, not
    changed. If it fails on festival cellular nothing is lost — the snapshot
    survives and the button can be pressed again — so chunking it was judged
    not worth a contract change four days out. Revisit if it actually fails.

---

## 2026-08-26 — the night answers: 26,999 of 27,000

The sleep test the whole Android path was gated on. Strap on at 22:50 with 17
readings and 94% battery, phone dark on the nightstand, no charger. At 06:20:
**27,016 readings and 87%.** That is 26,999 readings in 27,000 seconds —
99.996%, one per second for seven and a half hours with the screen off — and
under 1% of battery per hour, so a six-hour festival costs about 6%.

**The biggest risk of the Android path died overnight.** Samsung's app-killing
— aggressive, undocumented, the thing that could only be tested by living
through a night — did not touch a connectedDevice foreground service. The
same capture in the browser requires the screen lit end to end; this ran dark
in a bedroom on a three-day-old codebase.

One navigation trap surfaced before bed and is now closed: the site's "Ao
vivo" tab, reached from inside the app's WebView, is a dead end — a WebView
has no Web Bluetooth, so that page could only say "your browser can't do
this" about the one thing the surrounding app does natively. The frame now
closes itself on that route, landing on the native capture screen.

Still open on this path: the ~27,000-point upload (the morning's own data is
the test), and Health Connect for people with watches instead of straps.

---

## 2026-08-25 (night) — the hypothesis is reframed, and an app is born from it

**The question changed shape.** Planning a bigger test began as a supplier
question — which bands to buy — and ended somewhere better: the hypothesis is
not "does capture work" but **"do people find the delivery valuable"** — do
they open their night, light up, and share it unprompted. Capture is the
input, and every minute a participant spends nursing a phone contaminates the
measurement of the thing that matters. Ten Verity Senses at ~R$1k each were
considered and rejected: R$10k to validate a product whose Phase 0 premise is
*no custom hardware* validates the wrong thing.

**The zero-cost path exists but its friction lands in the wrong place.**
People's own watches record offline for free, and the importer already reads
their exports — but Apple Health exports the person's entire health archive
(measured: DOMParser at 200 MB takes ~10 s on a server and likely kills a
phone tab), and the friction arrives on the morning after, exactly where the
delight was supposed to be. That argument — Felipe's — is what justified the
app.

**The app was built in one evening because its scope refused everything else.**
A recorder, not an app: connect, capture with the screen dark, upload. Four
Kotlin files, zero runtime dependencies, the parser proven by ten CI tests
that need no device, and the BLE client a faithful port of the web one —
carrying every lesson the web version paid for (reconnection that never gives
up, elapsed-time offsets, the non-standard-sensor dead end). The CI pipeline
the J-Style spike left behind builds the APK; installation is by file.

**The full experience lives inside the app without being built twice.** The
"complete experience" requirement is met the hybrid way: ending a capture
opens the site's own screens — curve, peaks, card, share — framed in a
WebView with the native token handed over. One product, one brain, two
shells. Rebuilding those screens natively would mean fixing every one of
today's fourteen web defects twice, forever.

**The first hands-on found three defects in twenty minutes, all one family.**
A first-launch crash (Android 14 refuses a connectedDevice foreground service
before the Bluetooth permission exists — the service now starts with the
capture, not the app); the upload's outcome overwritten one frame after being
written, so a successful send read as a dead button; and a sessions count
with no sessions list, so a person could not confirm their capture arrived.
The ninth, tenth and eleventh cases of the day's one bug class: **the app
stating something false about its own state.** A crash reporter now turns the
next silent death into a sentence on screen.

**What gates the pilot on this path:** the six-hour sleep test — does Samsung
let the service live through a night — and Health Connect, which is what
removes the export friction for people with watches. Both are next, neither
is before Saturday.

---

## 2026-08-25 — the phone answers back

The rehearsal on the A17 found four defects in an afternoon. Not one would have
been found by any amount of reasoning here, and the two that mattered most were
both cases of the app **lying about its own state** rather than failing.

**A control that never fired.** Leaving event mode took four or five attempts.
The exit listened for `pointerleave` and cancelled the hold there, and a finger
held for over a second always wanders — the target was 37px tall. Reproduced by
driving a pointer with drift: at 15px both old and new fire, at 40px only the
new one does, which is exactly "sometimes it works". The control now captures
the pointer, and it is a 160px circle with a ring that fills, because the old
one gave no sign it had been pressed: a silently cancelled hold and a hold that
never registered looked identical.

**A capture that looked dead and was not.** Returning to the app showed a frozen
elapsed time and reading count, reported as the capture stopping in the
background. It had not: 1,504 readings in 1,504 seconds over the whole session,
where a minute in another app would have cost sixty. Android freezes a
background page's timers, and event mode ticks every 30 seconds — that is where
the redraw saving comes from — so returning showed values up to half a minute
old. Both now refresh the instant the page becomes visible. **The capture was
never the problem; being unable to tell a live one from a dead one is.**

**A share that carried everything except the card.** The platform list rendered
below a portrait card, off the bottom of a phone screen, so the button appeared
inert. On a phone there was nothing to show anyway — every entry opened the same
system sheet. It now opens that sheet directly, and hands it the image: it had
been sharing a link, and Instagram given a URL has nothing to post.

**A card three hours wrong.** A moment felt at 16h31 read "19h31". Stored in UTC
correctly, formatted straight out of it — on the one line that gets posted
publicly. It also would have put a festival ending after midnight UTC on the
wrong day.

**The Polar fallback is real.** A genuine export imports: 153 readings, none
discarded, and the average computed matches the average Polar wrote into the
file header — a check that the right column was read, not merely a column. The
file also showed the anchor logic was right by luck: it took the first cell
resembling a clock, and `Duration` (00:02:31) resembles one as much as `Start
time` does. Now read by column name. Beat → Flow sync turns out to be manual,
which is worth knowing on a Saturday rather than a Sunday.

**A working rule, learned three times.** Three separate pieces of work missed
their merge because a pull request was opened, merged within minutes, and then
pushed to. Editing the merged request's description to describe the new work
made it worse: a merged record claiming content it did not have. **Push
everything first, open the request last, and anything pushed afterwards gets a
new request — never an edit to an old one.**

---

## 2026-08-25 — preparing for six hours, and what that exposed

A six-hour festival on 29 August became the first capture longer than a few
minutes, and asking what would survive it turned up six defects. Every one had
been there since the feature was built; none had been reached by a test lasting
minutes.

**A browser cannot capture Bluetooth with the screen off.** Android freezes the
page and the connection goes with it. That is a platform limit, not something to
engineer around in Phase 0 — the real fix is a native or Wear OS capture, which
is Phase 2 of the roadmap. What follows is everything that limit implies.

**The wake lock was taken once and never taken back.** The Screen Wake Lock spec
releases it whenever the document becomes hidden, so glancing at a message
during an event silently ended the capture a minute later, when the screen slept
and the tab froze. This could not be reproduced here — headless Chromium does
not change visibility state when a tab is backgrounded, and the CDP command that
would force it has been removed — so the fix rests on the spec and wants
confirming on the phone.

**Event mode** cuts the screen to near-black and throttles the redraw: 43,200
renders over six hours down to 5,039. No reading is dropped; only the drawing
stops. Leaving it is a press and hold, because the phone spends the night in a
pocket and a tap-anywhere exit would drop back to the full screen, where the
next accidental touch could land on "Encerrar" and end the capture for good.

**A successful capture could not be opened.** Nothing downsampled the series, so
21,600 readings went to the phone as a megabyte of JSON and became one smoothed
SVG path — for a chart a thousand pixels wide. Now thinned to ~1,800 points,
keeping each bucket's minimum and maximum rather than sampling evenly: peaks are
stored separately and drawn on top, so an even sample could have drawn a lower
summit than the marker standing on it.

**The Polar app is the only capture path with none of our constraints** — it is
native and needs no screen — and its export could not be imported at all. The
parser accepted the first header row whose labels looked plausible, and a Polar
file opens with a summary block whose columns are named "Start time" and
"Average heart rate (bpm)". A header is now judged by what it yields: the
candidate whose rows parse in the longest unbroken run wins, because readings
are written one after another and a summary block is one row and then something
else. Polar also counts samples from zero, so elapsed times are anchored to the
date and start time above them.

That makes the plan for Saturday **both at once**: Polar Flow recording the
night as the guarantee, TumTum running as the experiment. It also produces
something we have never had — our capture and a reference device over the same
six hours, to compare.

**An expired account looked signed in.** The screen checked that a token
existed, not that it worked. They last 24 hours, so one saved days before an
event still reads as an account, and the capture would only discover otherwise
at the save — the one moment with something to lose. It now asks the server, and
treats only a refusal as signed out, since capture needs no backend at all. A
token valid at the door can still expire by the encore, so the screen reads the
expiry the token carries and says so before the capture starts.

**Reconnection gave up after eight attempts**, about 2.2 minutes. On a crowded
floor that ended the night. It now keeps trying every 30 seconds, indefinitely.

**A general lesson worth keeping.** Five of these six were invisible at the
scale we had tested at, and each was found by asking what six hours would do
rather than by running six hours. Cheap arithmetic on a real duration — renders,
bytes, points, token lifetime — found more than any amount of using the app for
two minutes would have.

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
