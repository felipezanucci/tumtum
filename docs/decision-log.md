# Decision log

Running record of what was decided, why, and what is still open. Details live in
the linked documents — this file is the index and the reasoning, not a diary.

---

## Where things stand — 2026-08-27

| Track | Status |
|---|---|
| **Hardware supplier** | J-Style **broke their own MOQ.** Arena's 2026-08-28 reply offers **10–50 units** of the customized raw-PPG V8 at USD 80/unit — the pilot batch Draft 4 argued for — with **NRE USD 30,000** (double the previous 15k, and the rebate ladder gone). She accepts our Polar protocol as the objective acceptance test, proposes agreeing criteria before development, and says explicitly there is no need to rush until Phase 0 results. **Draft 5 written, not sent:** bank the concession, decide nothing, plant three structural questions for after 25/09. Still no NRE and no volume before the pilot. *(History: pilot batch refused; MOQ 5,000 → 3,000; NRE 15k with a rebate ladder paying back only from 10,000 units — declined on timing. Arena then asked for "more vision"; Draft 4 went out 2026-08-26.)* |
| **Android app (native)** | **No longer a WebView shell.** Sign-in that knows its own token's expiry, an event chosen before capturing, a retry that retries, a native night (curve + moments, drawn on a Canvas) and a native card with the system share sheet. Capture itself is untouched: 26,999/27,000 readings overnight, screen off, 7% battery, upload at quality 100%. Every build is now signed with a committed key, so the app updates in place instead of demanding an uninstall. **Health Connect is built to the screen (v0.2, Etapas 1–3)** — what remains is a watch in a hand: the device test, and the density measurement the screen itself now performs. **0.1 stays on Felipe's phone until after the festival.** |
| **Path 2 — fans' own watches** | **Health Connect half-measured, 29/08: Samsung writes heart rate all night, no gap** — nine consecutive hourly records from a Fit3, ordinary sleep, no workout. The exercise-only fear is dead. Cadence still unknown: Health Connect's browser shows records, not the samples inside them, so only the 0.2 screen can count them. **Phase 1 validated in the field, and rehearsed on the phone.** A 25-minute capture recorded 1,504 readings in 1,504 seconds — one per second, nothing lost. Reconnection that never gives up, R-R intervals. |
| **Backend** | Railway trial had expired and paused all services; upgraded to Hobby, `/health` responding again. |
| **Frontend** | Deployed on Vercel via its native git integration, on **tumtum.cc**. Preview builds work per branch. **`/` is a real landing page and is live** — the whole public loop was driven end to end on the real deployment (form → API → table, count 0 → 1) — information and sales, with a working waitlist; the app screens live under `(app)` and are what the Android WebView loads. |
| **Brand** | MVP v0.1 manual adopted and live: black canvas, Acid Lime, Instrument Sans, the official Chosmos wordmark. Mutation skins **parked**. |
| **Share loop** | Card 01 built to the manual, at Story size and inside the safe areas, generated from a real capture. Sharing opens the system sheet **with the image attached**. |
| **Polar as fallback** | **Working end to end.** A real Polar Flow export imports; the average it computes matches the one Polar wrote into the file. Beat → Flow sync is manual — pull down and hold. **This is now the only fallback** — the browser capture path was retired 2026-08-26. |
| **Pilot (Tasha & Tracie, 2026-09-25)** | On track and **decoupled from the supplier decision**. |

### Open items

1. ~~**Merge PR #11**~~ — done, along with #12 through #17. `main` carries
   everything; nothing is queued.
2. ~~**End-to-end save test**~~ and ~~**detection accuracy**~~ — both done
   2026-08-25, both passed.
3. **J-Style** — the decline was sent 2026-08-26; Arena reopened the case the
   same day asking for the project's vision, and **Draft 4 went out the same
   day**, putting the pilot batch back on the table as shared technical
   de-risking. **Now waiting on her.** The firmware question — does custom Raw
   PPG remove the motion-conditioned clamp? — is the one that decides
   everything, and it is the question the letter asks her to answer. If she
   presses on demand instead, the prepared answer is the paragraph Felipe cut,
   preserved verbatim in `docs/jstyle-email-draft.md`; do not improvise a
   forecast in its place.
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
8. ~~**Confirm Railway actually redeploys on a push to `main`.**~~ Answered
   2026-08-26: **it does.** `/api/waitlist/count` went from `Not Found` to
   `{"total":0}` across the #38 merge with nobody touching the dashboard. The
   GitHub Actions deploy job still does not work and still is not what deploys
   this; Railway's own git integration is.
9. ~~**CI and deploy cleanup**~~ — done 2026-08-25 (#12), and the backend half
   finished 2026-08-25 (#20): its test step ended in `|| echo "No tests found
   yet"`, so nothing there could ever turn a check red.
10. **Realness Festival, 2026-08-29** — a six-hour drag festival in São Paulo,
    and the first capture longer than a few minutes. Prepared; see below. What
    is left is Felipe's: create the event, rehearse for five minutes at home
    recording on both the Polar app and TumTum, and send the Polar CSV so the
    importer can be checked against a file the device actually wrote rather
    than one reproduced from its documented shape.
11. ~~**Password reset does not exist.**~~ Built 2026-08-26 on Resend, via
    `mail.tumtum.cc`, which is the default in `config.py` — so **only
    `RESEND_API_KEY` needs setting on Railway**. Without it every send fails —
    loudly in the logs, silently to the person, since the reply is identical
    either way by design. **Still open and related:** `auth.py` compares email
    addresses case-sensitively, so `Felipe@` and `felipe@` are different
    accounts. The reset lookup works around it with `func.lower`; the column
    itself still needs normalisation plus a migration of existing rows, which
    is not a change to make days before a field test.
12. **An audit of every empty state is worth doing** after the festival. Two
    were found lying on 2026-08-26 by pulling one thread; nobody has checked
    the rest. The rule: an empty state is a claim about the world, so any list
    that can fail to load must distinguish "nothing there" from "I could not
    ask".
13. **The screen wake lock is the one fix still unconfirmed.** It cannot be
    reproduced here and the rehearsal did not isolate it: the phone was being
    handled throughout. What was confirmed is that a capture survives leaving
    the app and coming back. What is still untested is the screen staying lit
    on its own.
14. ~~**Waitlist follow-ups.**~~ All closed 2026-08-26.
    `WAITLIST_ADMIN_EMAILS` is set, `/admin/waitlist` makes the list readable,
    `oi@tumtum.cc` is confirmed working, and all five social accounts exist,
    are linked, and were checked in a browser.
15. **The end-of-night upload is 1.33 MB in a single request.** Measured, not
    changed. If it fails on festival cellular nothing is lost — the snapshot
    survives and the button can be pressed again — so chunking it was judged
    not worth a contract change four days out. Revisit if it actually fails.
16. **The deployed app never runs Alembic** — startup calls `create_all`,
    which only creates missing tables. Any migration that adds or alters a
    column silently does not happen in production; 007 is written and
    dormant. Decide after the festival: run migrations on deploy, or keep
    create_all and know its limits.
17. **The landing page has no sign-in link.** The only "Entrar" on it is the
    waitlist. Decide before the 25 September pilot how a person with an
    account gets in without typing an address.
18. **An event cannot cross midnight** — one date, two bare times, so
    "termina 03:00" cannot say it means the next day. A capture attaches by
    event id, so Saturday is unaffected; the model change waits.
19. **The Brazilian age/sex split for wearables is still unknown — and Super
    Panorama does not carry it.** Felipe opened the June 2026 edition on 27/08:
    it gives penetration (30,1%), the class cut and the function ranking, but
    **crosses wearables with nothing demographic** — its gender and age figures
    are about smartphones. The only route to the split is the raw crossing
    tables, on request from `fernando.paiva@mobiletime.com.br`. Felipe's to
    ask; it does not block Health Connect. Until then the only figure we have
    is global (Counterpoint, women ~35%), and it should not be guessed at.
20. **The iOS/Apple Watch path is real but uncosted.** Reading the Health app
    export through `/import` already works today; an iPhone app reading
    HealthKit is the exact mirror of the Health Connect plan and needs no watch
    app at all. What is not written down is the plan: stages, gates and an
    honest estimate, in the same shape as the other two, so the three can be
    compared before the order is fixed. Felipe was offered it 2026-08-27 and
    has not yet said yes. The Apple gates (US$ 99/year, a Mac or a macOS
    runner, TestFlight instead of a link) are calendar, not code.

---

## 2026-08-29 — Etapa 0, half answered: Samsung does write, all night

Felipe wore the Galaxy Fit3 overnight and read Health Connect in the morning.
The first of Etapa 0's two questions is answered with data instead of forum
reports.

**Samsung Health writes heart rate to Health Connect, continuously.** Nine
consecutive hourly records, 00:00 through 08:46, no gap:

| | |
|---|---|
| 00:00–00:59 | 57–76 bpm |
| 01:00–01:59 | 47–72 bpm |
| 01:41 | 60 bpm |
| 02:00–02:59 | 53–64 bpm |
| 03:00–03:59 | 51–70 bpm |
| 04:00–04:59 | 47–60 bpm |
| 05:00–05:59 | 50–63 bpm |
| 06:00–06:59 | 51–66 bpm |
| 07:00–07:59 | 53–64 bpm |
| 08:00–08:46 | 56–69 bpm |

The values are physiologically sensible (minimum at 04:00, rising on waking),
and the access log shows *"Gravação: Sinais vitais"* — writes, the direction
we need — at 00:09, 00:22, 00:31, 01:36, 01:42, 03:42, 07:53 and 08:46.

**The worst case is off the table.** The reports that Samsung only passes
exercise heart rate to Health Connect do not hold here: this was ordinary
sleep, no workout, and it all crossed. That was the single most consequential
unknown in `docs/health-connect-plan.md`.

**But the second question is invisible on that screen, and the reason is
structural.** Each row is a *record*, not a reading — and a Health Connect
heart-rate record is a **series**: start, end, and many samples inside. The
proof is in the row itself: `01:00–01:59 · 47–72 bpm` cannot come from one
reading, since a single sample has no minimum and maximum. The contrast row
`01:41–01:41 · 60 bpm` is a single-sample record; Health Connect knows the
difference and simply does not display it.

**So the cadence can only be measured by unpacking the series** — which is
precisely what `HealthConnectReader.readHeartRate` does, and what the
Conexão Saúde browser will not do. The measurement we designed to need no
code turns out to need exactly the screen we built: Etapa 0's question A is
answerable by hand, question B is not.

**Consequence for the setup steps, worth keeping:** the phone-side check
still earns its place — it proved the bridge is open and the night is
recorded, before any APK existed on that phone. It just cannot finish the
job. The 0.2 screen reads it on Sunday, against the same night, plus whatever
the Realness night adds.

---

## 2026-08-28 — J-Style broke their own MOQ, and doubled the NRE doing it

Arena's reply to Draft 4 arrived. **They gave us the thing that was refused
twice: a first batch of 10–50 units** of the customized raw-PPG V8. That is a
real exception to a 3,000-unit floor, and it cost her something internally.

**And the price of that concession is in the next line.** NRE went from
USD 15,000 to **USD 30,000**, and the rebate ladder is not mentioned at all.
They conceded on volume and recovered it — with interest — on engineering.

| | 24/08 counter | 28/08 offer |
|---|---|---|
| Batch | 3,000 (pilot refused) | **10–50** ✅ |
| Unit | USD 40–80 | USD 80 (top of range) |
| NRE | USD 15,000 | **USD 30,000** |
| Rebate | 20% ladder, 100% at 50k units | **absent** |

Total exposure fell from ~USD 255,000 to USD 34,000 — a genuinely different
conversation. Per device, though, validation costs **USD 680** at 50 units and
**USD 3,080** at 10.

**What the letter does not do is answer the question.** Draft 4 asked directly
whether the customized firmware removes the motion-conditioned processing —
the question this log has called the one that decides everything. The reply
proposes to develop it and agree criteria together. So USD 30,000 buys an
*attempt*, not an answer. Probably honest; still worth naming, because it
means **the payment structure matters more than the price**.

It also inverts Draft 4's argument. That letter framed the small batch as
*shared* de-risking. What came back has TumTum funding 100% of the firmware
development, with J-Style keeping the firmware and free to resell the
capability.

**Two things in the letter are genuinely valuable, and both are ours to use.**
Arena proposes agreeing acceptance criteria *before* development, and accepts
**our Polar H10 protocol as the objective test**. That hands us the lever: if
the protocol is objective enough for both teams to judge the result, it is
objective enough to gate payment. Milestone-staged NRE becomes a fair ask
precisely because Arena proposed the test.

**Decision: reply warmly, decide nothing.** Arena removed the deadline herself
— *"no need to rush… once you have your Phase 0 results."* Phase 0 validates
demand; custom hardware is Phase 1. Committing USD 30,000 of hardware
economics before knowing whether people want the product is exactly the
inversion the phase order exists to prevent. And the Health Connect path built
this week reaches 30,1% of Brazilian smartphone owners at zero hardware cost,
which removes any urgency from the band without closing the option.

**Draft 5 is written and unsent** (`docs/jstyle-email-draft.md`). It banks the
concession, accepts the *shape* of the proposal, and plants three structural
questions for after 25/09: staging the NRE against the acceptance test, what
happens if the firmware fails it, and whether the NRE can be credited against
a first production order. It deliberately does **not** counter on price —
the unit price is not the problem, and the NRE argument is far stronger with
pilot data behind it. Countering now would also signal we might pay
USD 30,000 before demand exists.

---

## 2026-08-28 — a confident number that described no device at all

Felipe bought a Galaxy Fit3 and a Xiaomi Smart Band 9 for the Etapa 0 test,
and the purchase exposed a defect before the hardware shipped.

**Health Connect is a shared store.** Paired to one phone, both bands write
heart rate into the same window — and the import screen was measuring their
union. The arithmetic is ugly: two bands each writing every 10 seconds,
neither dense enough alone, interleave into an apparent reading every 5
seconds. The screen would have announced *"Denso o bastante: dá para procurar
seus momentos"* for a device nobody was wearing.

**It is the project's own bug class, in its nastiest form yet.** Not a control
without feedback, not a stale display — a *confident number that describes
nothing real*, with nothing on screen to contradict it. Every previous
instance of this family announced itself eventually (a control that did
nothing, a page that said the wrong thing). This one would have looked like a
clean result and gone into the decision log as fact.

And it would have fired on the exact setup the measurement was about to use:
one person, two bands, one phone.

**The fix:** every reading now carries the package that wrote it. Cadence and
upload are computed on exactly one source, never a union. The report names its
source out loud; with more than one, the row says how many and switches
between them. Unknown packages keep their raw package name rather than a
guessed friendly label — the raw name is at least always true. The uploaded
session is stamped with the app that wrote it instead of "Health Connect",
which named the corridor rather than the device. A test pins the interleaving
arithmetic. CI green on 480dc13.

**It also made the experiment better.** Before, comparing two vendors meant
two nights or two phones. Now: both bands on one person, one wrist each, Polar
on the chest, one night. Same body, same heart, same window — so any
difference between Samsung and Xiaomi is the *vendor's*, not the night's, and
the strap is ground truth for both. Recorded in the Etapa 0 protocol.

**Generalisation worth keeping:** a shared data store makes provenance part of
the measurement. Any number computed across a store that several apps write to
must say whose data it describes, or it describes nobody's.

---

## 2026-08-27 — Huawei is the hole in "Health Connect reaches all of them"

Found while ranking devices for the Etapa 0 test. The 27/08 market entry said
Health Connect reaches the cheap bands Brazil buys, listing Huawei in the
volume tier — and for Huawei that is wrong: **Huawei Health does not write to
Health Connect at all.** Not a missing switch — since the 2019 sanctions
Huawei runs its own service stack (HMS), and no native bridge to Google's
health layer exists. The only route is a third-party sync app (Health Sync),
which is too much friction to ask of a fan.

Huawei Band is a top-3 seller in Brazil, so this is a real slice of the 30,1%
ceiling that the Health Connect path does not reach. It does not change the
ordering — the path still reaches Samsung, Xiaomi and Amazfit, which is most
of the volume — but the reach claim now carries its exception, and the pilot
onboarding should ask "qual relógio?" before promising anything.

---

## 2026-08-27 — Health Connect built to the screen in one evening, and what it cost

Felipe said go, and Etapas 1, 2 and 3 of `docs/health-connect-plan.md` are
code — pushed the same evening the plan was written. What remains is what no
session here can do: a person with a watch in hand (Etapa 4, and the Etapa 0
measurement the screen itself now performs).

**The zero-dependency rule ended, on purpose and with a successor.** The
Health Connect client requires AndroidX and its permission flow requires
`ComponentActivity`; there is no framework-only way to read another app's
data. The new rule, recorded in `build.gradle`: every runtime dependency must
be one Health Connect forces — nothing else. Three entered: `androidx.activity`,
`connect-client` 1.1.0, coroutines.

**The migration was smaller in code and bigger in toolchain than planned.**
Eight superclass declarations — every screen used `Activity` only as a
superclass, and the shared helpers take it as a parameter type, which a
`ComponentActivity` satisfies. But the published artifact demands
`compileSdk 36` and AGP 8.9.1+ (its docs said 35 — **the artifact's metadata
outranks the documentation**), which dragged AGP 8.11.1, Gradle 8.13 and
Kotlin 2.0.21 with it. Three CI rounds, all findable only in CI from here.
APK is now version 0.2.

**The screen is a corridor of honest gates**, and the cadence is measured,
never assumed — by median gap *and* 5-second-slot coverage, because either
alone flatters a sparse night (dense clusters around dead half-hours have a
lovely median; a 9-second metronome covers the night while starving the
detector's smoothing window). The verdict names what the data supports:
moments, or the curve — with the start-a-workout sentence offered right where
the sparse verdict lands. "Your watch wrote nothing" and "I could not ask"
never share a message. Eight CI-proven tests on the measurement.

**Etapa 0 got cheaper than the plan priced it.** The screen *is* the
measurement: no export, no file, no laptop — install 0.2, tap Trazer do
relógio, and the cadence report is the Etapa 0 reading. The two-stretch
protocol (normal wear vs. hand-started workout) still applies; the tool for
it now ships in the APK.

**The dead Google Fit chain went with it**, as section 6 ordered: the
frontend module that was never Health Connect, the backend `/sync` endpoint
and service nobody called, the client surface. While they existed under those
names they claimed half of this work was already done.

**Standing rule, standing:** the 0.1 APK on Felipe's phone is validated for
Realness on 29/08; 0.2 is not. **No update before the festival.**

---

## 2026-08-27 — Health Connect is the next step, and Etapa 0 just got sharper

Felipe asked whether the best-selling-band argument makes Health Connect the
natural next thing to build. It does — two independent lines point the same
way: it is the only path that reaches the cheap bands Brazil actually buys, and
it costs roughly half of Wear OS. **But the next step is still not code.**

Checking what the vendor apps actually do turned up something that changes
Etapa 0 from a density check into a two-question check:

> **Writing to Health Connect is the manufacturer's choice, and the list of
> what a vendor writes is not the list of what its device measures.**

- **Samsung Health** — the watch itself offers *measure continuously*, *every
  10 minutes*, or *manual only*. And there are user reports that **not all of
  it crosses into Health Connect: only exercise heart rate arrives reliably.**
  If that holds, *start a workout when the show starts* stops being a density
  mitigation and becomes a **functional requirement**. That is the single most
  consequential unknown in the plan right now.
- **Zepp / Amazfit** — writes to Health Connect, one-way (write only, never
  read), with an isolated report of heart rate not coming through.
- **Mi Fitness** (Xiaomi / Redmi) — writes steps, sleep, heart rate and
  workouts, chosen per metric at authorisation time.

None of those three lines is our measurement; they are third-party reports and
are recorded as the reason Etapa 0 exists, not as findings.

**Etapa 0 rewritten accordingly** in `docs/health-connect-plan.md`: two or
three people on **different brands**, each producing **two stretches on the
same device** — normal wear and a hand-started workout. Only that split can
separate *the watch did not measure it* from *the vendor did not pass it on*.
The gate now has a middle outcome: failing the normal stretch but passing the
workout stretch keeps the path alive and promotes the onboarding sentence into
the product.

**And it can be run for free at Realness on 29/08** — anyone in the group
wearing a band, one export, no code, on a real six-hour night.

---

## 2026-08-27 — "Apple Watch está fora" was a sentence that lied

Felipe read `docs/wear-os-plan.md` and asked, reasonably, whether Apple Watch
owners simply have no way into TumTum without carrying a second device. They
do. The document caused the misreading and has been corrected.

**What the sentence meant** was that an Apple Watch does not run Wear OS —
true, and irrelevant to whether TumTum reaches those people. **What it looked
like it meant** was that the platform is out of reach. Written inside a
document about one Android-specific path, with no scope marker, the narrow
claim read as the broad one.

This is the project's own recurring bug class — *a thing stating something
false about its own state* — found for the first time in a document rather
than in the app. The fix is the same fix: say which condition you are
describing.

**Apple Watch has three routes, and the first already works:**

1. **Today, zero code.** `parseHealthKitExport` in
   `frontend/lib/health/apple-health.ts` reads the Health app's XML export, and
   `/import` is live. Real friction to name: the export is a `.zip` of *all*
   health data, often hundreds of MB, and `/import` accepts `.xml` — the person
   unzips first.
2. **An iOS app reading HealthKit.** The exact mirror of the Health Connect
   plan, and the answer to Felipe's question: the watch already writes into the
   iPhone's Health store, so **no watchOS app is needed**. HealthKit is in two
   ways more generous than Health Connect — permission is entirely on-device
   (no OAuth, no quota) and there is no 30-day history limit.
3. **A watchOS app.** `HKWorkoutSession` — justified only by the same argument
   as Wear OS, and therefore last.

**Density behaves the same way, so the same free mitigation applies.** Apple
Watch samples roughly every 5 minutes outside a workout — ~72 points across six
hours, and the detector does not run — but continuously during a recorded
workout. *Start a workout when the show starts* is again the sentence that
decides it.

**The cost is Apple's gate, not the code.** US$ 99/year, a Mac to build (none
here; macOS runners on GitHub Actions are the way around it), and no APK link
at all — TestFlight, with internal testing capped at 100 people and external
testing gated behind Beta App Review, stricter for a health app. Order of
magnitude for a minimal iOS app (sign in, pick event, read the window, upload,
see the night on the site): comparable to Health Connect, ~2–3 weeks of code.
Full parity with the Android app is materially more. Recorded as open item 20;
the plan document is offered, not yet written.

**And Etapa 0 is free here too.** One Apple Watch owner exporting one night
through `/import` measures the real cadence — and if anyone at Realness on
29/08 wears one, that measurement costs nothing and can be taken against the
strap on the same person.

---

## 2026-08-27 — Super Panorama: three in ten wrists, and the one that matters

Felipe downloaded the **Super Panorama, junho 2026** (Mobile Time / Opinion
Box). It answers the market question and refuses the demographic one, so both
are recorded plainly.

**Penetration: 30,1%.** Of 4.138 Brazilians aged 16+ who own a smartphone,
30,1% also use a watch or band that talks to it (Gráfico 63). Three in ten
wrists — the ceiling on the Health Connect path before Apple Watch is
subtracted from it.

**And the audience we actually have skews above that.** Classes A and B are at
**37,6%**, against C at 29,6% and D/E at 27,5%. A ticketed festival crowd in
São Paulo is not the national average; the useful planning number for Realness
and for the pilot is nearer the top of that range than the middle.

**The function ranking is the finding that changes something** (Gráfico 64,
base 1.245 owners — what is *most important* to them day to day):

| | |
|---|---|
| Monitoramento de exercícios físicos | **30,2%** |
| Recebimento de notificações do celular | 29,8% |
| Monitoramento da quantidade de passos | 12,1% |
| **Monitoramento do batimento cardíaco** | **9,7%** |
| Calorias queimadas | 8,4% |
| Qualidade do sono | 8,4% |
| Outro | 1,4% |

**Exercise tracking is the number one thing Brazilians use a wearable for.**
That is exactly the gesture the free density mitigation asks for — *start a
workout when the show starts* — so the instruction is not asking people to
learn a new behaviour, it is asking them to do the thing they already do most.
The zero-cost mitigation in `docs/wear-os-plan.md` just got materially more
likely to work, which strengthens the ordering: Health Connect first, Wear OS
only if measurement demands it.

**Heart rate at 9,7% is a salience number, not a usage number.** The question
asked which single function matters most, not which ones the device performs —
almost every one of those bands reads heart rate continuously whatever its
owner ranks first. Read correctly it says: HR is not what people bought the
thing for. That cuts both ways. TumTum is not competing with an entrenched
habit, and it also cannot assume anyone knows their watch has been recording
their heart all along. The onboarding copy should tell them.

**The demographic cut is not in this report.** The gender and age splits in its
analysis (the 48,7%/35,2% on smartphone importance, the 16–29 cuts) are about
smartphones; wearables are crossed only with social class. The report itself
offers the raw crossing tables on request — `fernando.paiva@mobiletime.com.br`.
Open item 19 is updated rather than closed: what is known is now known, what is
missing is now known to be missing from this source specifically.

Source: `Super Panorama, junho 2026`, Mobile Time / Opinion Box, pages 40–41.

---

## 2026-08-27 — what Brazil actually wears, and what it does to the plan

Researched to put numbers under the capture-path choice. The market answer
turns out to be sharper than the demographic one.

**Volume in Brazil lives below US$ 150** — Xiaomi, Huawei, Zepp/Amazfit,
Positivo — and the best-selling wearable on Amazon Brasil in 2025 was the
Samsung **Galaxy Fit3**, a band rather than a watch. Apple and Samsung hold
the value share; they do not hold the wrists.

**None of those bands run Wear OS.** Xiaomi Smart Band, Amazfit, Galaxy Fit are
closed manufacturer systems. So the 3–5 week Wear OS investment would reach
Galaxy Watch 4+ and Pixel Watch and nothing that most of Brazil is actually
wearing. That is a much harder argument against it than "narrow reach" was
yesterday.

**Health Connect reaches all of them.** Mi Fitness, Zepp and Samsung Health all
write heart rate into Health Connect. It is the only path that touches the
Brazilian volume, which settles the ordering: Health Connect first, and Wear OS
only if a specific measurement demands it.

**The demographic cut could not be obtained.** The right source exists — Super
Panorama 2026 (Mobile Time / Opinion Box, 4,138 respondents, ±1.5 pp, covering
wearables with gender and age segmentation) — but `mobiletime.com.br` is
blocked by this environment's egress proxy and the figures do not surface in
search. The only demographic number found is global, not Brazilian:
Counterpoint has women at historically ~35% of smartwatch users. **Open item
19:** Felipe can open the Super Panorama and read the wearables section; until
then the age/sex split is unknown and should not be guessed at.

---

## 2026-08-27 — what a Wear OS app is actually for

Costed in `docs/wear-os-plan.md`, which supersedes the fase-2 estimate in
`docs/path-2-roadmap.md` (written before the phone app existed).

**It is not for "live".** Wear OS came up as the way to see the beat during the
show, but nobody looks at a watch mid-show and the Phase 0 loop does not need
it: live the event, then see the night. What a watch app actually buys is that
`ExerciseClient` guarantees ~1 Hz on every Wear OS 3+ device, regardless of any
setting its owner chose — which is precisely the risk that can kill the Health
Connect path. That is the reason to build it, and the only one.

**And there is a free mitigation first.** The same watch that samples every ten
minutes in normal use samples densely while an exercise session is running. One
sentence in onboarding — *start a workout when the show starts* — buys the
density for everyone who follows it. The Wear OS app is the version that works
for everyone who does not, and that is what several weeks would be purchasing.

**The reach is the uncomfortable number.** Wear OS 3+ means Galaxy Watch 4 or
newer and Pixel Watch. Older Galaxy is Tizen; Fitbit's own watches, Garmin and
Amazfit are not Wear OS; Apple Watch, the most common smartwatch of all, is
out. It is the narrowest audience of the three capture paths, for the highest
build cost.

**Distribution is the practical wall.** There is no "send them the APK" on Wear
OS: each watch needs developer options, wireless debugging and a PC running
adb — one in-person session per person. Fine for a concierge pilot of three to
five; it does not scale without the Play Store, which brings back the health
declaration form plus Wear review.

**Battery is a real gate, not a formality.** A continuous-heart-rate exercise
session is the most expensive mode a watch has. If a Galaxy Watch cannot cross
six hours, the app does not serve festivals — and that is only knowable by
measuring.

Estimate: 7–11 working sessions, 3–5 weeks of calendar after a watch is bought
(R$ 800–2,000, no emulator can stand in — a heart-rate sensor cannot be
emulated). Roughly double Health Connect, for the narrowest reach. It does not
fit comfortably before 25/09.

**Recommended order, each deciding the next:** the onboarding sentence, then
Health Connect stage 0 (a night, no code, measures the real density), then Wear
OS only if that measurement shows the density does not come for free. Building
it first would be paying weeks for insurance against an unquantified risk.

---

## 2026-08-27 — what Health Connect can and cannot be

Planned in `docs/health-connect-plan.md`, before writing any of it, because
two facts reframe the whole phase.

**Health Connect reads what a watch already recorded; it does not stream.**
Live heart rate from somebody's own watch is Health Services in a Wear OS app
— a different project, on the watch, weeks of it. For the Phase 0 question
(*is the delivery valuable?*) the after-the-event path is enough: wear your own
watch, open TumTum afterwards, see the night. That is what this phase builds.

**The risk that can kill it is measurable today, with no code.** The detector
assumes roughly a reading a second — 5 s smoothing, a 300 s baseline, a 5 s
minimum peak. But Samsung Health measures continuously *or every ten minutes*
depending on a setting its owner chose, and Fitbit gives 1–5 s only in training
mode. At one reading per ten minutes a six-hour festival is **36 points**: the
detector cannot run, and the curve it would draw is a pretty lie. So stage 0 is
two or three people exporting a real evening through the `/import` screen that
already parses Health Connect and already scores quality — a night's work, no
engineering, and it decides whether this phase promises *moments* or only *a
curve*.

**The infrastructure cost is real and invisible.** `connect-client` breaks the
APK's deliberate zero-dependency stance and its permission flow needs
ComponentActivity, so all seven screens migrate off plain `android.app.Activity`
before a single byte of health data is read.

**The Play Store is not in the way.** Health permissions need a developer
declaration form and review to publish, but a hand-installed APK reads Health
Connect with no approval at all. That is weeks that the pilot does not have to
spend.

Estimate: 4–6 working sessions, ~2 weeks of calendar with the device-test
loops, against ~4 weeks until 25/09. It fits if stage 0 passes.

**Debt found while planning:** `frontend/lib/health/google-health-connect.ts`
is not Health Connect at all — it is the Google Fit REST API, whose developer
signups closed on 2024-05-01 and which reaches end of life late in 2026. It
cannot be switched on even if we wanted it. Under that name it implies half of
Health Connect is done; it should be deleted with stage 1.

---

## 2026-08-27 — the native path finds both peaks, to the beat

The eight-minute two-effort rehearsal, on the day's final APK (`f73d87d`),
with Polar Flow recording in parallel as the reference. The whole native
pipeline — BLE capture → upload → automatic analysis → native night screen —
against a hand-noted protocol and an independent recording:

| | TumTum | Polar (reference) | Δ |
|---|---|---|---|
| Effort-1 peak | **140 bpm · 18:07** · 29 s | 140 bpm · 18:07:49 | 0 bpm, same minute |
| Effort-2 peak | **143 bpm · 18:10** · 35 s | 143 bpm · 18:10:35 | 0 bpm, same minute |
| Average / min / max | 100 / 78 / 143 | 100.5 / 78 / 143 | ≤ 0.5 bpm |
| False positives | **0** | — | — |

Exactly two moments found, ranked correctly by magnitude (the 143 first),
both matching Felipe's hand-written effort times and the Polar file to the
minute and to the beat. This closes the question the day opened with —
detection had never once run on a natively captured session — and re-confirms
the 2026-08-25 calibration on a different capture path.

**The Saturday arrangement is itself proven.** Both H10 BLE connections were
in use simultaneously — TumTum capturing, Polar Flow recording — which is
exactly the plan-A-plus-plan-B configuration of the festival, previously
argued from the datasheet and never exercised on purpose.

One caveat, deliberate: the rehearsal ran with "Sem evento", so peak-to-moment
matching and the card's event name were not exercised in this run. Both
depend only on the `event_id` link that the picker and the Eventos screen now
set. **On Saturday: choose Realness before connecting.**

---

## 2026-08-27 — the app said it was signed in, and it was not

Felipe opened the app to run the rehearsal. It showed the capture screen,
connected the H10, counted readings — and failed at the upload with **"Token
inválido ou expirado"**, with no way anywhere in the app to sign in again.

**The token lasts 24 hours and he had signed in the day before.** What turned
an expiry into a dead end is one line:

```kotlin
val gone = if (api.signedIn) GONE else VISIBLE
```

`signedIn` was `token != null`. **An expired token is still a stored token**,
so the app hid its password field, presented itself as authenticated, captured
happily, and discovered the truth only at the end. `logout()` existed in
`TumtumApi` and was called from nowhere. The only recovery was wiping the app's
data — which, on any night that mattered, would have taken the readings with
it, because the samples live in the service's memory and nowhere else.

Fourteenth of the family, and the first that could have cost a whole event.

**Two more of the same family fell out of it.** The failure message said
"toque de novo" while the button underneath read "Conectar sensor" and did in
fact start a *new* capture — the retry was never wired. And `CONNECTING` did
not count as capturing, so pressing the button mid-connect began a second
capture instead of ending the first, under a label that said "Encerrar e
enviar".

Now: signed in means a live token, checked against the expiry the JWT already
carries; a 401 on upload forgets the token and brings the login back **while
keeping every sample**; the button says "Enviar de novo" only when there is
something to resend, and resends it; and a session with under twelve hours
left says so *before* a strap goes on.

→ `AccessToken.kt` with tests, `MainActivity.render()`

---

## 2026-08-27 — the app had exactly one door, and it was the success branch

With the upload failing, the whole product became unreachable from inside its
own app. Not "hard to find" — unreachable. `ExperienceActivity` is
`exported="false"` with no launcher, and grep found **one** call site: the
success branch of the upload.

```kotlin
.onSuccess { sessionId -> ExperienceActivity.open(this, ...) }
```

So a failed send removed sessions, cards and every night from the app. Felipe's
report was "não consigo ver mais nada", and he was exactly right.

**A related finding on the site.** He went to `tumtum.cc` and landed on the
sales page, and concluded the site no longer had the app in it. Half true: the
`(app)` route group is still there and is still what the WebView loads — but
**the landing has no link to sign in.** The only "Entrar" on it is "Entrar na
lista", the waitlist. So the site *behaves* exactly as he described unless you
type the address. Worth deciding before the 25 September pilot, when three to
five people with accounts will open `tumtum.cc` and find no way in.

---

## 2026-08-27 — nothing ever asked for the peaks

`POST /api/health/sessions` does not run detection. `GET /api/experience/{id}`
only reads peaks already stored. The only thing that runs `detect_peaks` is
`POST /api/experience/{id}/analyze`, and the only callers were the web
`/import` and `/live` pages — **neither of which the Android app uses**.

So every natively captured night had an empty peaks table, and the experience
screen renders an empty peaks table as *"Nenhum momento se destacou aqui. Sua
batida seguiu no mesmo ritmo."* After six hours of a festival that is a lie,
and it would have been the first thing Felipe saw at 4 a.m.

Found by reading the call graph, not by running anything — the proxy cannot
reach production from the dev environment.

**The app now analyses at the end of every upload**, in a separate call so a
failed analysis cannot fail the upload: retrying 1.3 MB would file the same
night twice. And the native night screen tells the three silences apart —
capture too short for a 300-second baseline, nobody has looked yet, or looked
and found nothing. Only the third says a heart kept the same rhythm.

**The event was never attached either.** The app sent no `event_id`, and no
endpoint attaches one afterwards, so every capture was orphaned and its card
read "Evento" — the exact string the `/events/novo` page was built to stop.
There is now a picker before capture, and the choice survives the screen being
destroyed mid-capture.

---

## 2026-08-27 — every APK was signed by a different key

"App não instalado", after agreeing to install it. The project defines **no
`signingConfig`**, so Gradle falls back to `~/.android/debug.keystore` and
*generates it at random when the file is missing*. Every CI run is a fresh
machine. Every APK therefore carried a different signature, and Android
refuses an update whose signature does not match the installed app. The dialog
names none of this.

**For a hand-installed pilot this is worse than an annoyance.** It means the
only way to update is to uninstall first — and uninstalling kills the capture
service and every reading it is holding. That is precisely the instruction
nobody can be given after a six-hour show.

A debug keystore is now committed and wired into `build.gradle`. The
trade-off is written out there: it signs debug builds of an app installed by
hand, its password is the conventional one, and anyone with the repository
could sign an APK Android would accept as an update — which matters only to
somebody who can also get it onto the phone. A store release would need a real
key kept out of the repository.

**I told him not to uninstall, and then had to correct it.** With the old
random signature there was no other way; the fix only takes effect from the
next build onward.

---

## 2026-08-27 — a 500 with a name, and migrations that never run

Saving the festival's hours — 22:00 to 03:00 — returned "Erro interno do
servidor". `events.start_time` and `end_time` are **`TIME WITH TIME ZONE`**,
and asyncpg encodes that type as:

```
offset = obj.tzinfo.utcoffset(None)
```

Parsing `"22:00:00"` gives a naive `datetime.time`, whose `tzinfo` is `None`,
so the driver raises `AttributeError`. Creating the event had worked only
because its times were left blank and the columns never written. **Proved by
reading asyncpg's own encoder** after `pip install asyncpg` — there is no
Postgres and no Docker daemon in the dev environment, so it could not be
reproduced, and a guess was not good enough.

The same trap silently broke `/api/demo/seed`, which builds its events from
naive times: seeding has never worked against a real database.

**And the finding that outlives this bug: the deployed app does not run
Alembic.** `app/main.py` calls `Base.metadata.create_all` on startup, which
creates missing tables and **never alters an existing column**. Migrations 002,
003, 004 and 006 only create tables, so they worked by accident. 005 adds
columns, and 007 changes a type — neither would happen in production.

So the running fix is a validator that gives a bare time an offset, and
migration 007 (drop the timezone from both columns) is written and dormant.
The offset it stores carries no information and must not be read: a
time-of-day with an offset but no date cannot account for a daylight rule,
which is why Postgres discourages the type.

**Still open:** an event that crosses midnight cannot be represented at all —
one `date` and two bare times, so "termina 03:00" cannot say it is the next
day. Not a Saturday problem, since a capture attaches by event id.

---

## 2026-08-27 — the app stops being a shell

Felipe's call, made against a stated recommendation to defer it: **the site is
information and sales, the app is the experience.** The reasoning he gave is
sound — the two now have different jobs — and the version delivered is the
narrow one that fits before a festival.

Native: sign-in, event picker, capture, "Minhas noites", "Sua noite" (curve
and moments), the card and its share sheet. Still on the site, reachable in a
browser: creating and editing events, the Polar importer, profile, admin.

**Kept deliberately:** the WebView, now reachable from the bottom of the night
screen rather than merely present — a net nobody can reach is not a net. Same
argument that kept `/live` alive.

**Not done, and it was argued against:** rebuilding the card image natively.
It is drawn server-side to the brand manual; a fully native app would still
call that endpoint. The independence worth having is not leaving the app, not
redrawing what already works.

The chart is a Canvas polyline, no chart library — the APK still carries zero
runtime dependencies. Plain BPM over time, per the manual: no zones, no risk
colours, no normal ranges.

---

## 2026-08-26 — "perfil não encontrado" for a page that was not a profile

Felipe opened `tumtum.cc/esqueci-senha` before the page had shipped and got
**"Perfil não encontrado — Este usuário não existe ou o perfil é privado."**
The `[username]` route had caught the path and concluded that `esqueci-senha`
was a person who does not exist.

**A correction to my own first reading.** I said any wrong address on the site
would claim a person does not exist. That was too strong: Next gives real
routes priority over dynamic ones, so once `/esqueci-senha` existed it won.
What Felipe saw was correct behaviour for a page that genuinely was not there.

But the message still cost real time, because it was a **plausible** wrong
answer. It did not say "this page does not exist" — it made a confident claim
about a user nobody had asked about, and anyone reading it would go looking at
profiles. A wrong answer that sounds like an answer is worse than an error.

**The actual defect, and it is the rule again.** `catch { setError(true) }`
caught everything, so a network failure and a genuine 404 rendered the same
sentence. "Este usuário não existe" when the server could not be reached is the
app asserting something it never checked — the twelfth lesson, one route over.

Three outcomes now, in three sets of words:

| what happened | what it says |
|---|---|
| API answered 404 | "Nada por aqui" — the address belongs to nobody |
| API unreachable | "Não deu pra carregar… isso não quer dizer que a página não exista" |
| Unmatched path (2+ segments) | the new `app/not-found.tsx`, same voice |

There was **no `not-found.tsx` at all** before this — an unknown multi-segment
path fell through to Next's default page: unbranded, in English, with nowhere
to go. Every one of the four now offers a way back to the landing page, which
none of them did.

Driven in a browser, all four cases.

---

## 2026-08-26 — TumTum can send mail, and passwords can be recovered

Resend, on the subdomain `mail.tumtum.cc`. **Domain verified in six
minutes** — added 16:25, DNS verified 16:29, verified 16:31 — which is unusual
for GoDaddy and means the records went in correctly the first time.

**The subdomain was the whole point.** `oi@tumtum.cc` already receives, so
there are live MX records on the root domain; pointing a sending provider at
the root risks replacing them and killing an address nobody would think to
re-test. A subdomain isolates the sending stack from the mailbox entirely.

### What the reset flow decides

**The email never says whether the account exists.** Both outcomes return the
same sentence: *"Se esse e-mail tiver uma conta, o link acabou de sair."*
Answering "esse e-mail não está cadastrado" would turn the form into a free
tool for discovering who has an account, at any volume, for anyone.

**The database stores a hash, never the token.** What travels in the email is
not what sits in the table, so reading `password_reset_tokens` grants nobody a
reset — the same reason passwords are hashed. SHA-256 rather than bcrypt is
deliberate and would be wrong for a password: the input is 32 random bytes with
a 30-minute life, so there is no dictionary to run against it.

**Thirty minutes, one use.** Single use matters as much as expiry: without it a
link sitting in an inbox stays a working key for its whole lifetime, and
"I already reset it" would not close that window. Completing a reset also kills
every other outstanding link for that account — someone resetting because they
fear a break-in should not leave a spare key in a mailbox they no longer
control.

**A failed send is logged, loudly.** Because the person is told the same thing
either way, a send that fails would otherwise vanish completely and
"não recebi o e-mail" would be unanswerable. `EmailNotConfigured` raises rather
than returning quietly, for the same reason.

**The lookup is case-insensitive**, via `func.lower`. Addresses were stored
without normalisation (open item 11), so an account registered as `Felipe@`
could not otherwise be found by someone typing `felipe@` — the feature would
fail for exactly the people most likely to need it. Matching loosely here is a
patch over that; the column still needs fixing.

**A link with no token shows an explanation, not a form.** Some apps truncate
long URLs. A form that fails on submit would blame the person for what the link
did.

Success signs them in rather than bouncing to the login page: they just proved
control of the mailbox and chose a password, so asking them to type it again is
ceremony.

Twelve tests on the token rules, and the whole flow driven in a browser —
request, missing token, mismatched confirmation, success, token stored,
redirect.

**Replies reach a person.** `mail.tumtum.cc` only sends — a reply to it would
vanish. `reply_to` points at `oi@tumtum.cc`, which receives, so someone
answering "não fui eu que pedi isso" reaches a human instead of a black hole.
That is the difference between a security notice and a robot.

**Still needed before it works in production:** `RESEND_API_KEY` on Railway.
Everything else defaults correctly.

---

## 2026-08-26 — the waitlist learns who people are

The list collected an address and nothing else, which is enough to send a
message and not enough to write one. Felipe's point: *"precisamos de nome e
sobrenome das pessoas pra podermos nos comunicar com eles da melhor maneira."*
A mail that opens "Prezado usuário" is a mail from a company that does not know
you, and this product's whole voice is the opposite of that.

`first_name` and `last_name`, from the landing form through to the CSV. Still
nothing else — no phone, no birthday, no device. The page promises "a gente só
usa seu e-mail pra te avisar dos próximos eventos", and every column we do not
add is a promise we cannot break by accident later.

**Nullable, deliberately.** Entries collected before the form asked have no
name, and the alternative to nullable is inventing one. The admin list omits the
name line for those rather than rendering an empty heading, and a repeat
submission now *fills in* a name we are missing — someone who signed up before
the field existed and comes back with a name is telling us something we did not
know. It never overwrites a name already held.

**Names are tidied, not corrected.** `normalize_name` trims and collapses
internal whitespace and stops there. Capitalisation is not ours to fix: "de
Souza", "McDonald" and "van der Berg" are spelled the way their owners spell
them, and a title-casing helper would quietly rename people. Four tests hold
that line, because it is the kind of "improvement" that looks like a bug fix.

The three validation messages name the missing field one at a time rather than
saying "preencha tudo" — being told which field is missing is the difference
between fixing it and hunting for it.

Driven in a browser: each empty field produces its own message, and the
confirmation reads *"A gente te chama quando a TumTum chegar num evento perto
de você, Felipe."* The name is used one second after being given, which is the
argument for asking for it.

---

## 2026-08-26 — the admin account, and what the signup form was hiding

**The waitlist chain is closed and proven.** Felipe registered `felipe@tumtum.cc`,
pointed `WAITLIST_ADMIN_EMAILS` at it, and `/admin/waitlist` renders the list.
His personal account still gets the 403, which is what he wanted: **the
contact details of other people sit behind an account that is not his
personal identity.** That is a posture decision, not a configuration one, and
it is the right one — worth keeping as the platform grows rather than
collapsing back into "the founder's login can see everything".

Two things surfaced while checking that the account could even be created.

**1. Registration and login compare the address exactly as typed.** No
normalisation anywhere in `auth.py`, and the `users.email` column is a plain
`String`. So `Felipe@tumtum.cc` and `felipe@tumtum.cc` are two different
accounts, and an Android keyboard capitalises the first letter by default. The
same trap `services/waitlist.py` was written to avoid, still wide open one
table over. The admin gate itself is safe — it lowercases both sides — so the
failure mode is "cannot log in", not "cannot read the list".

**Not fixed, deliberately.** Lowercasing on the way in would orphan any
existing row whose stored address carries a capital, so the real fix is
normalisation plus a migration of what is already there. That is not a
three-days-before-the-festival change. Queued.

**2. There is no password reset, and no way to build one.** The backend has no
email capability at all — no SMTP, no provider, nothing in `requirements.txt`.
So "esqueci minha senha" cannot be built until an email provider is chosen and
its domain verified on `tumtum.cc`.

That absence changes what the signup form is. **A password typed with a typo
today is not a failed login — it is a permanently unreachable account**, since
nothing can recover it. So the reveal toggle and the confirmation field are not
polish: right now they are the entire recovery mechanism. Both shipped, on
signup and login, and driven in a browser: the toggle reveals and re-hides, it
does not submit the form (a bare `<button>` inside a form does — an eye icon
that signed you up would have been memorable), mismatched confirmations disable
the button, and matching ones release it.

---

## 2026-08-26 — the twelfth time, and the worst one yet

Felipe opened `/admin/waitlist` on his laptop and got a red box: *"Sua sessão
expirou. Entre na sua conta para continuar."* — an instruction with nothing to
act on. Told to sign in, on a page offering no way to sign in.

Pulling that thread found something much worse two screens away. **`/events`
and `/cards` had no error handling at all on load.** `loadEvents` used
`try/finally` with no `catch`, so a refused request left the list empty — and
an empty list renders as **"Nenhum evento encontrado"**. The app was making a
claim about the person's own data when it had simply failed to ask.

**Measured, not reasoned.** Both versions were built and driven in a browser
with every `/api/**` call forced to 401:

| under a 401 | before | after |
|---|---|---|
| `/events` | "Nenhum evento encontrado" **+ an offer to load demo events** | sign-in prompt, working button |
| `/cards` | "Você ainda não criou nenhum card" | sign-in prompt, working button |
| `/sessions` | red box, no way out | sign-in prompt, working button |
| `/admin/waitlist` | red box, no way out | sign-in prompt, working button |

The demo-seeding offer is the part that turns this from embarrassing into
dangerous, and it was not noticed until the before-state was actually rendered.
**Saturday's failure mode was sitting right there:** token expires during a
six-hour festival, Felipe opens Eventos, the app says his event does not exist
and invites him to populate the database with demo data. Over the real one.

`SignInRequired` also says *less* than the wording it replaces, deliberately. A
401 means the request carried no valid token, which is as often "this browser
never signed in" as "your session expired" — exactly what happened here, a
laptop after a phone. Asserting expiry in that case is the same defect one
level down: the app confidently wrong about its own state while sounding
precise.

**Twelve.** Every one found by a person using the thing, none by a test. What
is new this time is the shape: the first eleven were the app lying about a
control or a display. These two lied about *the user's data*, which is worse,
because a person has no way to know it is untrue. A stale timer looks wrong. An
empty list looks true.

The lesson worth carrying: **an empty state is a claim.** "Nenhum evento
encontrado" asserts something about the world, and any code path that can reach
it without having actually looked is a lie waiting for a bad afternoon. Every
list that can fail to load needs to tell "nothing there" apart from "I could
not ask".

---

## 2026-08-26 — the footer stops guessing

All five accounts are real, so all five are linked: Instagram, TikTok, X,
Facebook, LinkedIn. Three of them had been **dropped** rather than shipped
pointing at a platform's home page — the same defect as the form that did
nothing, and dropping them was the right call while their existence was
unknown. `oi@tumtum.cc` is confirmed to receive.

**The URLs were cleaned before use.** The links arrived carrying share tokens
from Felipe's own session — `?igsi=MXZoYmkxdnV6c2l4eQ==` on Instagram,
`?_r=1&_t=ZS-99DCJup2BCS` on TikTok. Those identify the device that generated
the share. They work, so pasting them would have looked fine and shipped a
personal identifier into a public page that gets crawled and archived. Stored
as canonical profile URLs instead.

Verified in a browser rather than by reading the source: all five render, and
their `href` values are the canonical ones with `rel="noopener noreferrer"`.

**The Facebook handle really is `tumtum.ccc`, with three c's** — confirmed by
Felipe: the two-c name was already taken. Recorded because it looks exactly
like a typo, and the next person to notice it will otherwise "fix" it into a
dead link.

---

## 2026-08-26 — the landing goes live, and Railway's deploy question finally has an answer

The whole public loop was exercised on the real deployment, by Felipe, from a
phone: the landing page renders on tumtum.cc, an address typed into the form
was accepted, and `/api/waitlist/count` went **0 → 1**. Page, form, API,
table and the Vercel↔Railway link, all confirmed together rather than
separately.

**Open item 8 is closed: Railway does redeploy on a push to `main`.** It had
been open for days marked "always implicit, never verified", because the
GitHub Actions deploy job has never worked and nobody had checked what was
standing in for it. The test was free once the endpoint existed —
`/api/waitlist/count` answered `Not Found` before the merge and `{"total":0}`
after it, with nobody touching the Railway dashboard in between. A new
endpoint is a better probe than a dashboard: it can only answer if the running
code is the merged code.

`WAITLIST_ADMIN_EMAILS` is set on the service. Worth remembering about
Railway's UI: a new variable is **staged**, not applied — it sits behind
"Apply 1 change / Deploy" until confirmed. Setting it and walking away leaves
it not set.

**And a gap of my own, now closed.** The read endpoint shipped without a way to
reach it. It needs an `Authorization` header, so the one person the list exists
for could not open it in a browser — a feature its only intended user cannot
use is not shipped, whatever the API says. `/admin/waitlist` is that page:
the list, a count, and a CSV export.

It is deliberately **not in the navigation.** Access is decided by the server
against `waitlist_admin_emails`, and the frontend has no way to know the
answer in advance, so a link in the bar would be a control that fails for
almost everyone who sees it — the house bug wearing a different hat. Felipe
reaches it by URL. If that becomes annoying, the fix is a flag on the profile
response and a conditional link, not an unconditional one.

The 403 says so in words rather than rendering an empty table, because an
empty table would claim "nobody signed up" when it means "you may not look" —
two very different facts that look identical.

CSV escaping moved to `lib/utils/csv.ts` with six tests. An unescaped comma in
a field does not fail loudly; it shifts one row's columns and surfaces days
later looking like a data problem.

---

## 2026-08-26 — the merge race, a fourth time

PR #37 was merged at `43d4db4`, two commits in. Four more had already been
written by then and landed after: the paragraph cut from the Arena letter, the
log entries, the retirement of the browser capture path, and the entire landing
page. Its description described all of it. None of it was in it.

Fixed the only way a merged PR can be: #37's description trimmed back to the
two commits it actually carries, with the correction stated at the top rather
than quietly, and **PR #38** opened for the rest. A merged pull request is
finished; it cannot absorb later work.

**The fourth time is the interesting one.** The rule — push everything first,
open the pull request last — has been in `CLAUDE.md` since the third. It was
written down, it was read, and it failed anyway, which means it is not a rule
problem. The failure needs two parties to arrive in the wrong order: the PR
gets opened while work continues, and the merge lands while more is still
coming. Writing "be careful" on one side of that does not synchronise it.

What actually removes it is not opening the PR until the work is finished and
pushed — not "mostly finished", *finished* — and, when a PR is already open and
more commits are coming, saying so in the description so the other side knows
not to merge yet. Both of those are mechanical. The instruction to remember was
the part that kept failing.

---

## 2026-08-26 — the site becomes the shop window, and the form that did nothing

Felipe's framing: *"o site pode ser apenas um instrumento de informação e
venda"*. Correct for the public half, and it does not cost the app anything —
because the split already existed. `(public)/` is what a stranger sees;
`(app)/` is what the Android WebView loads. The landing page could be replaced
wholesale without the capture path noticing, and now it has been.

The new landing came from a draft Felipe built in another session. Two things
were checked before anything else:

**The wordmark is the real one.** Compared byte for byte against
`frontend/components/brand/Wordmark.tsx`: same path, same `viewBox`, same
`fill-rule`, differing only in whitespace. The manual forbids a redrawn or
regenerated wordmark and this is the easiest rule in the project to break by
accident, so it is worth stating that it was verified rather than assumed.

**The waitlist form did nothing at all.** `<button type="button">` with no
handler anywhere in the file. A person typed their address, clicked, and the
page did not move — no request, no error, no confirmation. The lead was lost
*and* the person believed they had signed up.

That is the house bug — an interface asserting something false about its own
state — and a marketing page is the worst place for it, because unlike a
control inside the app, nobody ever comes back to discover it lied. It was
rebuilt with every outcome visible: sending locks the field and says so,
success names the state back, a repeat submission is warm rather than red
(they wanted to be on the list; they are), and a failure keeps what they typed
and says the server was at fault.

**Where the addresses go.** A `waitlist_entries` table and `POST /api/waitlist`,
public because no account exists at the moment someone asks. Email, an optional
`source`, and nothing else — the page promises "a gente só usa seu e-mail pra
te avisar dos próximos eventos", and a column we do not have is a promise we
cannot break by accident later.

Reading the list back is gated on `waitlist_admin_emails`, not on being signed
in. Being a user of the platform is not an access rule for other people's
contact details, and the setting is empty by default so the endpoint is closed
to everyone until someone deliberately opens it — the right posture for a table
that fills up long before anyone remembers it exists.

Normalisation lives in `services/waitlist.py` with five tests, because it is
the piece that decides whether two submissions are one person. Android's
keyboard capitalises the first letter by default, so `Felipe@` and `felipe@`
arrive from the same thumb. It lowercases and trims and deliberately stops
there: stripping dots or `+tags` would be us deciding two real addresses are
one human.

**Measured, not assumed.** Driven in a real browser at 360 px and 1280 px:
zero horizontal overflow at both — the failure that once put a whole nav
section off screen — zero console errors, and all six sections confirmed to
reach full opacity when scrolled to. The last one mattered: a reveal animation
that never fires leaves a marketing page blank, so `Reveal` starts visible and
only hides itself once it knows the observer is running. The three form
outcomes were driven end to end against stubbed responses.

**Still open:** the social links point at `instagram.com/tumtum.cc` and
`tiktok.com/@tumtum.cc`, which may not exist yet, and `oi@tumtum.cc` is
unconfirmed. Facebook, X and LinkedIn were dropped rather than shipped pointing
at nothing. The video slots are empty by design and say so on their face — the
page is built around footage that does not exist yet, and the labels are
visible because this page goes to people who might supply it.

---

## 2026-08-26 — the browser capture path is retired

Felipe's call, three days before the festival: now that the Android app
exists, the site is no longer a capture route. **"Ao vivo" is out of the
navigation.**

**The distinction that matters:** the site is not being dropped — it *is* the
app. Events, cards, sessions and profile all render in the WebView; only BLE
capture is native. What was retired is one screen, `/live`, and the Web
Bluetooth path behind it.

**It was never a working fallback, which is the real argument.** A Polar H10
accepts two simultaneous BLE connections. During an event both are already
spoken for — the app, and Polar's own app running in parallel as the reference
recording. A browser would be the third and would not connect. So "Ao vivo"
was not a spare route that we chose not to use; it was a route that could only
fail, sitting in the menu under the most confident label in the bar, waiting
for someone in a dark crowd to tap it looking for the capture screen.

*(The two-connection limit is the H10's documented behaviour and matches what
Felipe hit in practice when only Polar Beat would take the second slot. It has
not been measured by us. Worth confirming in the rehearsal, since the
festival plan depends on app and Polar Flow coexisting.)*

**What changed, and what deliberately did not.** The nav item is gone, and the
sessions empty state — which told people to "Conecte um sensor em Ao vivo" —
now points at the Android app instead. That second edit is the whole bug class
this project keeps finding: remove the destination, leave the sign, and the
app is lying about itself again.

The `/live` route itself was **kept**. Deleting a working screen three days
before the only six-hour test buys nothing and risks something; it is simply
no longer advertised. `ExperienceActivity` still intercepts it inside the
WebView and drops back to the native capture screen, which stays as the second
net. Revisit deleting it after the festival, when the cost of being wrong is
an ordinary week.

---

## 2026-08-26 — the launcher icon, and an hour lost to build identity

The Android launcher icon now shows the TUMTUM wordmark on black, confirmed on
Felipe's A17. The drawables were generated verbatim from
`frontend/components/brand/Wordmark.tsx`, so the phone icon and the web
wordmark are the same paths — the manual forbids redrawing the letters, and
generating from the master is the only way to be sure nobody did.

**The cost was not the fix, it was finding out the fix was already shipped.**
The icon "still" showed the Android robot after the change had been merged,
and the reason was that the installed APK was `tumtum-captura-4a12146.apk`,
built 2026-08-25 — a build from before the icon existed. Nothing was wrong
with the code; the wrong binary was on the phone.

**Rule this earns:** when a report says the app is missing something that was
merged, establish which build is installed *before* looking at the source. The
APK filename carries the commit — read it first. This is the same failure
shape as the bug class this project keeps finding, only inverted: not the app
lying about its state, but us reasoning about a version that was never
running.

---

## 2026-08-26 — Arena reopens the case and asks for the vision

**Draft 4 sent 2026-08-26**, in the cut-down form described in rule 2 below.
Awaiting her reply.

Hours after the decline was sent, Arena wrote back: *"Could you share with us
more vision about this project? I'm trying to reevaluate this case and figure
out if there's anything i can help with."*

**A refusal that reopens itself is not a refusal.** The 50–100 unit batch had
already been refused once, formally. "Reevaluate" and "anything I can help
with" are the words of someone who wants material to argue a smaller deal
internally — the ask is for something forwardable, not for reassurance.

So the reply (Draft 4, `docs/jstyle-email-draft.md`) is built to be forwarded,
and constrained by three rules this project has paid to learn:

1. **No invented numbers.** Every figure in it is measured and recorded here:
   26,999/27,000 readings overnight, 7% battery, 100% upload quality, zero
   false positives across 7.5 hours of sleep, two peaks found in a two-effort
   test. No market sizing, no unit projections, no revenue. A forecast that
   misses once costs more credibility than it ever bought.
2. ~~**Say plainly that demand is unvalidated.**~~ **Cut before sending, by
   Felipe, in two steps** — first the sentence "and I am not going to invent
   it", then the whole "What I cannot tell you yet" paragraph. The drafted
   argument was that conceding "nobody outside our tests has used this
   product" is unanswerable and explains the refusal without impugning their
   terms. Felipe's call was that a letter written to be forwarded inside
   J-Style should not hand the reader the sentence that kills it. Both
   readings are defensible; the founder's is the one that ships, and it is
   recorded here so the trade-off is visible if the reply lands badly.
   **What this costs:** the sent letter no longer explains *why* 3,000 units
   were declined. Draft 3 already did, so Arena is not left guessing — but if
   she asks again, the answer is the cut paragraph, and it should be given
   rather than improvised into a forecast.
3. **Reframe the small batch as *their* risk reduction.** This is the real
   move. The open question — does the customized Raw PPG firmware remove the
   motion-conditioned processing that clamped amplitude while stationary? —
   cannot be answered from a datasheet. If the answer turns out to be no at
   3,000 units, J-Style eats a failed acceptance, a refund fight and a dead
   partnership. At 50 units it is a Tuesday. The letter also offers a per-unit
   premium for a small run, which removes "this is a disguised discount
   request" as a reading.

The vision itself is stated once and concretely: the Phase 1 buyer is not a
consumer buying one band, it is an artist, a club or a festival putting bands
on many people at once — which is why a manufacturing partner matters here and
a consumer brand would not.

**Unchanged:** no NRE is paid, and no volume is committed, before the pilot
answers the Phase 0 question and before the validation protocol — stationary
condition eliminatory — is written into the contract as acceptance criteria.

---

## 2026-08-26 — Arena counters, and the calendar answers for us

Arena's reply to the pilot-batch request arrived. The actual ask — 50–100
units with raw-PPG firmware — was refused. The counter: MOQ cut from 5,000 to
3,000 (a real concession, and still 30–60× the requested pilot), NRE held at
US$ 15,000, softened by a cumulative-order rebate — 20% back at each of
10k/20k/30k/40k units, the remainder at 50k.

**The rebate refunds money exactly in the scenarios where it isn't needed.**
Order only the 3,000 and stop: nothing back. Reach 10,000: US$ 3k back.
At 50,000 units the NRE is fully refunded — and irrelevant. The risk stays
whole on our side; the structure is polished, the concession thin.

**Declined for now — on timing, not price.** Since this negotiation began,
the hypothesis was reframed (validate the *delivery*, with no custom
hardware — the standing Phase 0 premise) and the Android app proved overnight
capture on hardware people already own. The September pilot was explicitly
decoupled from the supplier decision on 08-17. There is no hardware decision
to make until the pilot answers the Phase 0 question; paying US$ 15k plus a
3,000-unit commitment now would fund firmware that has not yet proven it
fixes the stationary-clamp defect that failed both bands — the product's
core case, and the eliminatory line of the validation protocol.

**Kept for later:** the 50,000-unit rebate ladder reveals how much Arena
believes the relationship could be worth — leverage for a Phase 1
negotiation, alongside Veepoo and any other supplier competing by then. If
talks resume, firmware acceptance criteria (the validation protocol, with
the stationary condition eliminatory) go in the contract before any NRE is
paid. Reply draft: `docs/jstyle-email-draft.md`.

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

The morning closed the last gate: the ~27,000-point upload (≈1.6 MB) landed,
the backend scored the night **quality 100%** — no gaps in 7.5 hours — and
the display downsampling built for exactly this size drew 23:00 to 06:00
in-app without strain. Min 49, max 102, average 63: a night of sleep, read at
one beat per second by a three-day-old app. Still open: Health Connect.

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
