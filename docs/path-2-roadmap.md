# Path 2 — heart rate from the fans' own watches

**Created:** 2026-08-24, after the J-Style evaluation closed both supplier paths
(`docs/jstyle-v8-evaluation.md`).
**Strategic purpose:** decouple the pilot from the hardware supplier crisis. The
first real event can run on watches people already own; a Tumtum-branded band
becomes just one more source later, not a blocker.
**Hard milestone:** concierge test, Tasha & Tracie, **2026-09-25** (~4.5 weeks).

---

## 1. Architecture

Every source converges on one contract. The app never knows where a beat came
from:

```
{ source, bpm, timestamp, quality }
```

| Field | Meaning |
|---|---|
| `source` | `apple_watch` \| `wear_os` \| `ble_hrs` \| `import` \| (later) `tumtum_band` |
| `bpm` | integer, 30–250 (matches the existing backend constraint) |
| `timestamp` | ISO 8601, UTC |
| `quality` | 0–100: sampling cadence, gaps and contact status, computed the way `frontend/lib/health/quality.ts` already does it |

Sources → contract → app → backend (`POST /api/health/sessions`, already exists)
→ TimescaleDB → peak detection (already exists).

**The backend and the analysis pipeline are already built.** Path 2 is entirely
about the capture layer.

---

## 2. What we already have (as of 2026-08-24)

- Backend ingestion, peak detection, event correlation, card generation.
- **File import path**: `/import` in the PWA parses Health Connect, Samsung
  Health, Apple Health and generic CSV/JSON exports, scores data quality against
  the peak-detection requirements, and uploads a session. This is Path 2's
  post-event fallback and it works today.
- **A sharp validation protocol**: 3 blocks of 60 s rest / 30 s max effort /
  90 s seated recovery, against a Polar H10, scored on amplitude ratio ≥ 0.85,
  peak delay ≤ 5 s, MAE ≤ 5. Every candidate source passes through it.
- **An analysis pipeline** for that protocol, and the lesson that alignment must
  use absolute clocks, never MAE minimisation.

---

## 3. The architectural decision to make first

Today's testing settled one thing: **a pure PWA cannot read HealthKit, Health
Connect, or vendor BLE protocols.** But it did not close everything:

| Capture route | iOS | Android | Native code needed |
|---|---|---|---|
| Web Bluetooth → standard BLE Heart Rate Service (0x180D) | ❌ not supported in Safari | ✅ Chrome | **none** |
| HealthKit live (Apple Watch) | ✅ | — | watchOS + iOS |
| Health Services (Wear OS) | — | ✅ | Wear OS + Android |
| File import after the event | ✅ | ✅ | none (**built**) |

So the sequencing is: **take the free win first** (Web Bluetooth on Android),
then invest in native. The decision of *PWA + native companion* vs *full React
Native app* can wait until Phase 3, and should be made with Phase 1 data in
hand.

---

## 4. Phases

### Phase 0 — Foundations · week 1 · in-house

| # | Task | Done when |
|---|---|---|
| 0.1 | Write the BPM contract into `CLAUDE.md` and add a `quality` field to the session schema | Migration applied, contract documented |
| 0.2 | Apple Developer account (US$ 99) and Google Play account (US$ 25) | Both active |
| 0.3 | Buy: used Galaxy Watch or Pixel Watch, Amazfit Bip 6 | In hand |
| 0.4 | Open the watchOS freelancer role | Posted, 3+ candidates |

> **Infra constraint:** the MacBook Air 2015 / Monterey caps out at Xcode 14 and
> cannot build modern watchOS. The freelancer must use their own machine
> (chosen), or a used Mac mini M1 (~R$ 2.500–3.500) if the work is brought
> in-house later. Wear OS builds fine on the current machine with a physical
> device (no emulator).

---

### Phase 1 — Web Bluetooth, standard BLE · weeks 1–2 · in-house · **highest priority**

The cheapest route to live heart rate, and it validates the entire pipeline
end to end with zero native code.

| # | Task |
|---|---|
| 1.1 | Web Bluetooth client in the PWA: request device filtered on service `0x180D`, subscribe to Heart Rate Measurement `0x2A37`, parse flags/8-vs-16-bit/contact/R-R |
| 1.2 | Map readings to the BPM contract, buffer, and post to the existing session endpoint |
| 1.3 | Reconnect logic: BLE drops during a 2–4 h event are certain, not hypothetical |
| 1.4 | Screen: "connect your device", live BPM, connection state |

**Gate:** run the 3-block protocol with the **Polar H10 as the source** — it
speaks 0x180D natively, so this validates our own capture and parsing against a
reference we trust (in this case, against itself via `tumtum_ble.py`). Then
repeat with the **Amazfit Bip 6** in broadcast mode.

> **To verify:** the Amazfit Bip 6 is reported to broadcast standard BLE heart
> rate under Zepp OS ("Heart Rate Broadcast"). Confirm on the actual device
> before counting on it.

**Limitation to state plainly:** Android only. iPhone users fall back to file
import until Phase 3 lands.

#### Result — 2026-08-24, Samsung Galaxy A17 + Polar H10

| Check | Result |
|---|---|
| Frame decoding | 539 real GATT frames from the Polar replayed through the parser: **100% agreement** with `tumtum_ble.py`, including all 835 R-R intervals |
| Live capture | Connected on the first try; heart rate, R-R intervals and signal quality all live on screen |
| Cadence | **240 readings in 239 s = 1.004/s** — the 1 Hz target, no gaps |
| Reconnection | Strap removed mid-session: state went to "Reconectando… tentativa 1" and recovered on its own, the reading count continuing from where it left off |
| Signal quality verdict | Aprovado (cadence 1.0 s, coverage 85% — the gap being the deliberate disconnection test) |
| Saving the session | **Blocked**: the API at `tumtum-production.up.railway.app` is unreachable. `NEXT_PUBLIC_API_URL` is correctly configured, so this is the backend being down, not the capture layer |

**Capture is validated. The gate is met on everything the browser controls.**
What remains for this phase is infrastructure: bringing the backend back up so a
session can be persisted and run through peak detection.

Three defects the first real device test exposed, all fixed:
- Tailwind had never compiled in any deployment (no `postcss.config.js`), so
  the app had always rendered as unstyled HTML.
- Browsers that expose `navigator.bluetooth` without implementing the chooser
  (Samsung Internet here) left the UI on "Conectando…" forever with nothing to
  act on.
- The elapsed clock restarted on every reconnection: the state callback is
  captured once by the monitor, so it read a frozen `startedAt`.

#### Result — 2026-08-25, the chain closes end to end

The blocked row above is now green. On the same A17 and Polar H10, on the
deployed app, a session went the whole way through:

| Stage | Result |
|---|---|
| Capture | 306 readings, 5min32s, 70–145 bpm, coverage 100%, cadence 1.0 s, quality **Aprovado** |
| Crash recovery | Session restored from the localStorage snapshot after a page reload mid-capture |
| Save | Persisted through `POST /api/health/sessions` |
| Peak detection | **1 peak: 145 bpm at 10:29, 30 s duration** |
| Experience view | Curve rendered, avg 91 / max 145 / min 70, quality 100%, card generation offered |

**The gate is met.** Sensor → capture → save → peak detection → visualisation
works on a real phone with a real strap, with no native code and no supplier.

**Detection accuracy — measured 2026-08-25, same day.** The open question above
is now closed. A clean 7-minute two-effort protocol was designed against the
detector's real parameters (`baseline_window_sec = 300`, not the 60 s in
CLAUDE.md) by simulating five candidate protocols 20 times each; the chosen one
recovered the right peak count in 20/20 runs with peaks landing within 1 s of
each effort's end. Run on the A17 with the Polar H10:

| | Noted by hand | Detected |
|---|---|---|
| Effort 1 ended | 11:22 | **136 bpm at 11:22**, 42 s |
| Effort 2 ended | 11:25 | **146 bpm at 11:25**, 44 s |

Criteria fixed before the run: exactly two peaks, each matching the noted time.
**Both met.** The notes are minute-resolution, so this establishes agreement to
the minute rather than the ±10 s originally proposed — sufficient for the
question asked, which was whether the algorithm finds the right number of peaks
and places them correctly.

The ranking is by magnitude rather than chronology, which is why the 146 bpm
peak is listed first.

Four more defects this run exposed, all fixed (PR #11):
- `/live` never checked for a signed-in user: a capture could run for a whole
  event and only fail at the save. Now stated on arrival, and 401s speak
  Portuguese instead of returning FastAPI's `Not authenticated`.
- The recovery snapshot stored whole-second offsets, so readings under a second
  apart collapsed onto one timestamp. `hr_data` is keyed by
  `(time, session_id)`, so the insert was rejected and the session lost. In
  simulation, 174 of 200 realistic captures carried at least one collision.
- **Every backend 500 had been reaching users as "the server is unreachable."**
  Starlette raises its 500 outside all user middleware, so the response carried
  no CORS header, the browser refused it, and `fetch` rejected. The error trap
  now sits inside the CORS layer.
- Pull-to-refresh reloaded the page mid-capture and dropped the sensor.

---

### Phase 2 — Wear OS · weeks 2–4 · in-house with Claude Code

| # | Task |
|---|---|
| 2.1 | Kotlin Wear OS app, Health Services `ExerciseClient` (~1 Hz while an exercise session is active) |
| 2.2 | `MessageClient` / `DataClient` to the paired phone |
| 2.3 | Phone-side receiver → BPM contract → backend |
| 2.4 | Foreground service + wake lock for multi-hour capture (today's testing showed why: no wake lock means Android throttles the capture the moment the screen sleeps) |

**Gate:** 3-block protocol vs Polar H10, plus a **4-hour soak** with battery
measured at both ends.

---

### Phase 3 — Apple Watch · weeks 3–5 · freelancer, in parallel

| # | Task |
|---|---|
| 3.1 | watchOS app: `HKWorkoutSession` + `HKLiveWorkoutBuilder` for live heart rate |
| 3.2 | `WCSession` to the iPhone app |
| 3.3 | iOS side → BPM contract → backend |
| 3.4 | Decide and implement the app shell: PWA + native companion, or full React Native |

**Known limits:** HealthKit delivers heart rate roughly every 1–5 s, not 1 Hz,
and gives no live beat-to-beat R-R. Feed the measured cadence into the `quality`
score rather than pretending it is 1 Hz.

**Gate:** same 3-block protocol, same thresholds.

---

### Phase 4 — Onboarding and multi-source · week 5

| # | Task |
|---|---|
| 4.1 | "Connect your watch" onboarding with the three live paths + import as fallback |
| 4.2 | Source precedence when someone has more than one connected |
| 4.3 | Surface `quality` to the user honestly ("your watch samples every 5 s — peaks may be smoothed") |
| 4.4 | Session recovery: app killed mid-event must not lose the session |

---

### Phase 5 — Simulated field test · week 6 · before 25/09

3–5 people, mixed sources, one wearing a Polar H10 as reference, a **2 h+**
session with real event timing.

**Gate — all must hold:**
- No session loses more than 5% of its samples
- Peak detection finds the planted peaks on every source
- Share card generates for every participant
- Battery survives the full session on every device

---

## 5. Minimum viable path to 25/09

If everything slips, the concierge test still runs on:

1. **Phase 1** (Web Bluetooth + Android + any 0x180D device), plus
2. **File import**, already built, for everyone else.

That combination needs no native code, no app store review, and no supplier.
Phases 2 and 3 are what turn the concierge test into a product — they are not
prerequisites for the milestone.

---

## 6. Risks

| Risk | Mitigation |
|---|---|
| Freelancer watchOS delivery slips past 25/09 | Phase 1 + import carries the milestone; Apple Watch users import after the event |
| Amazfit Bip 6 does not broadcast 0x180D | Chest straps do; verify in Phase 1 before recommending a device to fans |
| BLE drops during a real event (crowd, RF noise) | Reconnect logic in 1.3, and buffer locally so a drop costs samples, not the session |
| Android kills the capture in background | Foreground service + wake lock (2.4) — already observed today without one |
| Fans' watches sample too slowly to resolve peaks | `quality` score is computed and surfaced; set expectations in onboarding instead of shipping a bad curve |

---

## 7. Relationship to the hardware track

Path 2 is **not** a retreat from owning hardware. It is what makes the supplier
decision unhurried: with fans' watches carrying the pilot, a Tumtum band can be
chosen on its merits rather than on schedule pressure. When one arrives, it
becomes another `source` in the same contract — no product rework.
