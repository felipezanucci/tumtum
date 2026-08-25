# JStyle V8 — Hardware evaluation report

**Device under test:** JCVital V8 (J-Style / Joint Corp), screenless ECG smart band
**Reference:** Polar H10 (ECG chest strap)
**Host:** Samsung Galaxy A17 (Android)
**Bench sessions:** 2026-06 → 2026-08-17 (BLE/GATT + screen capture), **2026-08-24 (SDK, this report)**
**Instrumentation:** `hardware/jstyle-spike/` (instrumented vendor demo, CI-built APK) and
`tumtum_ble.py` (Polar reference logger, `tumtum-hardware` repo)

> Written in English because it is the working language of the supplier
> correspondence and may be attached to it.

---

## 1. Executive summary

The V8 **fails all three acceptance criteria of Tumtum's validation protocol**,
measured against an ECG reference, across three independent blocks:

| Criterion | Limit | Measured | |
|---|---|---|---|
| Peak amplitude ratio | ≥ 0.85 | **0.34** (0.21–0.56) | ❌ |
| Peak delay | ≤ 5 s | **+42 s to +54 s**, or never reached | ❌ |
| MAE | ≤ 5 bpm | **17.5** (16.1–19.1) | ❌ |

Two independent paths to usable heart rate were tested and both are closed:

- **Path A — processed BPM from firmware/SDK.** The SDK delivers the same
  motion-gated value as the vendor app. During 30 s of maximum exertion the
  device reported a flat 78–82 bpm while the reference climbed to 128.
- **Path B — raw PPG.** Not reproducible. Zero packets across three independent
  command configurations and 32 measurement commands.

This confirms in our own data what J-Style stated in writing on 2026-08-20: the
motion gating is in firmware and cannot be disabled. **The V8 as delivered
cannot serve Tumtum's core use case** — an emotional heart-rate peak while the
body is still.

---

## 2. Background

Tumtum's product case is a heart-rate peak with **no body movement**: a chorus at
a concert, a goal watched from the stands. The validation protocol therefore
makes the **stationary condition eliminatory**.

Prior sessions established:

- **GATT inspection (Phase 0):** neither the V8 nor the 2208A exposes the
  standard Heart Rate Service (0x180D) — only proprietary services
  (`fff0`/`190e`). With the V8 as DUT, `tumtum_ble.py log` connects but receives
  zero packets.
- **Phase 1 (2026-08-17):** V8 failed the 3-block protocol against the Polar
  H10. Diagnosis: **accelerometer-conditioned motion gating**. Stationary, the
  firmware treats a fast rise as artefact and clamps the ramp (amplitude ratio
  0.46–0.64, peak delay 23–33 s). In motion it releases tracking (0.87, 2–8 s).
  MAE < 2 in steady state: the sensor is good, the problem is firmware.
- **Vendor response (Arena Lee, 2026-08-20):** motion filter confirmed, **not
  disableable**, no fast-response mode. Offered: streaming via SDK at ~1 s.
- **SDK analysis (2026-08-20):** the V8 Android SDK exposes raw PPG streaming
  (`setECGRealtimeDuringHRVEnabled(true)` during an HRV measurement), which
  would bypass the firmware algorithm entirely — the basis of Path B.

---

## 3. Method

An instrumented replacement for the vendor demo's PPG screen logs every raw PPG
sample and every BLE callback to CSV, with wall-clock timestamps. Eight runs
were executed on 2026-08-24, each one narrowing a hypothesis; the APK is built
by CI from `hardware/jstyle-spike/v8test/`.

**Protocol (run 8, the decisive one):** three blocks of 60 s rest / 30 s maximum
effort / 90 s **seated** recovery, Polar H10 recorded in parallel on a Mac, V8
recorded continuously on the A17 under a single Start covering all three blocks.

**Alignment:** the two series were aligned by **absolute wall clock**.
MAE-minimising cross-correlation was deliberately rejected: with a device whose
defect *is* a systematic lag, that method absorbs the lag into the fitted offset
and hides it (it would have reported a spurious +19 s to +25 s "clock offset").
The reported figures are therefore conservative.

---

## 4. Results

### 4.1 Path A — heart rate from firmware/SDK

| Block | Baseline | Polar peak | V8 peak | Delay | Amplitude ratio | MAE |
|---|---|---|---|---|---|---|
| 1 | 80.9 | **129** | 108 | **+42 s** | **0.56** | **19.1** |
| 2 | 78.1 | **136** | 92 | n/a* | **0.24** | **17.3** |
| 3 | 87.7 | **138** | 97 | n/a* | **0.21** | **16.1** |
| **Mean** | | | | | **0.34** | **17.5** |

\* In blocks 2 and 3 peak-to-peak delay is undefined: the device never tracked
the rise at all, so the timing of its maximum is noise rather than a response.
A robust substitute — **time to reach half of the true excursion**:

| Block | Reference crosses | V8 crosses | Delay | Max under-read |
|---|---|---|---|---|
| 1 | 18:34:50 | 18:35:44 | **+54 s** | −47 bpm |
| 2 | 18:40:07 | **never** | — | −45 bpm |
| 3 | 18:45:21 | **never** | — | −47 bpm |

**Observed behaviour (block 1, representative).** Through the 30 s of maximum
effort the reference rises 81 → 128 bpm; the V8 goes 78 → 82. By the time the
reference is well into recovery (89 bpm) the V8 finally climbs to 107 — now
**over-reading by +18 to +27 bpm**. Blind during the peak, and wrong in the
opposite direction afterwards.

Two supporting observations from earlier runs the same day:

- With the subject **at rest and the true rate stable at ~77 bpm**, two session
  re-acquisitions started at 73 and 67 bpm and ramped at **0.50 and 0.59 bpm/s**
  to the true value — matching the 0.51 bpm/s stationary ramp measured against
  the Polar H10 in July. The SDK value carries the same firmware rate limit as
  the app.
- In one run the device produced a **spurious 35 bpm drop in 20 s** (95 → 60)
  during recovery, then re-climbed. This is not slowness but instability, and
  was not seen in the July screen-capture data.

### 4.2 Path B — raw PPG

Raw PPG streamed **once**, for exactly 2.03 s: 10 packets, 800 samples,
≈394 samples/s, a clean optical waveform, beginning 59 ms after a re-arm — then
47 further re-arms produced nothing. A controlled A/B/C probe, run after a
physical device reset, tried to reproduce it:

| Configuration | Commands | PPG packets |
|---|---|---|
| A — exact recipe of the successful run, on a virgin state; the raw-PPG flag never cleared | 15 | **0** |
| B — the vendor demo's own literal duration value | 15 | **0** |
| C — clean stop, then a single start left completely undisturbed for 75 s | 1 | **0** |

**Zero packets across 32 measurement commands and three independent
configurations.** The 2 s burst is not reproducible with any command sequence
constructible from the delivered SDK.

### 4.3 Protocol findings (useful to the supplier)

Established empirically and reusable regardless of the outcome:

- The `RealTimeStep` 1 Hz stream is **stable**: 241 callbacks over 4 minutes,
  1.004 s median interval, zero gaps. It carries `heartRate` **only while a
  measurement session is active**; otherwise it reports 0 and repeats a stale
  step snapshot.
- The duration parameter of `SetDeviceMeasurementWithType` is in **seconds**: a
  60 s request yielded 9 s of warm-up plus exactly 51 s of 1 Hz heart rate.
- **Sensor warm-up is 8–9 s** after a measurement command.
- A measurement **start is a no-op while the firmware believes a session is
  active**; recovery requires an explicit stop first.
- One raw-PPG packet mixed ~33 header/metadata bytes in among the samples: the
  SDK parser does not cleanly separate payload from frame metadata.
- After ~32 measurement commands the device stopped responding even to
  `AutoHeartRate` (19 s with no data against an 8–9 s warm-up), suggesting
  firmware degradation under repeated commands. Short window — indicative, not
  proven.

---

## 5. Verdict

| Path | Status |
|---|---|
| **A — processed BPM (firmware/SDK)** | **Failed** all three criteria, against ECG reference, in three blocks |
| **B — raw PPG** | **Not demonstrable** with the delivered SDK |

The V8 as delivered is **not viable** for Tumtum. The 2208A, tested previously,
was the less bad of the two in the stationary condition (0.60–0.82 amplitude
ratio, 10–17 s delay) but fails the same criteria and shares the same firmware
family.

---

## 6. Open questions for J-Style

1. Is there a documented command sequence that sustains the **raw PPG stream
   continuously**? Ours delivers ~2 s per session at ≈394 samples/s and then
   goes silent, and will not restart without an explicit stop.
2. What is the **nominal PPG sampling rate** and the exact frame layout? One
   packet interleaved header bytes with samples.
3. Is there a **maximum measurement session duration**, and can a session be
   sustained or auto-re-armed for 4 hours?
4. Does the **US$ 15k integration package** unlock continuous raw PPG and live
   R-R streaming, with protocol documentation and a written acceptance test? The
   `openRRInterval` / `realtimeRRIntervalData` constants exist in the parser of
   every SDK variant, but no method sends an activation command.

---

## 6b. Vendor's answer (2026-08-25)

J-Style confirmed our findings in writing: the **standard V8 SDK does not
support continuous raw-data streaming**, and the ~2 s burst we measured reflects
the standard firmware rather than a hardware limit. Continuous raw PPG requires
**dedicated firmware customization**, offered as:

| Term | |
|---|---|
| NRE | **USD 15,000** — SDK/API licensing, protocol documentation, firmware support, integration assistance |
| **MOQ** | **5,000 units** |

They propose defining sampling rate, packet format, streaming duration and
data-handling requirements with their engineering team before finalising scope.

**Assessment.** The decisive number is the MOQ, not the NRE. At roughly
USD 40–80 per unit that is **USD 200,000–400,000 of inventory**, committed
before Phase 0 has validated that people want to see and share their heart rate
at events. Firmware customization also runs 2–4 months, past the pilot, and the
continuous-streaming firmware does not exist yet — so technical risk survives
the payment until an acceptance test proves otherwise.

**Position taken:** park, do not close. Two questions asked before archiving —
whether a 50–100 unit pilot batch with the customized firmware is possible at a
higher unit price, and whether the NRE can be credited against a future volume
order. Draft in `docs/jstyle-email-draft.md`.

## 7. Implications

- The **hardware supplier decision is unblocked**: J-Style is out unless the
  answers to §6 change the picture materially, in writing and with an acceptance
  test.
- The **Veepoo** track (contacted 2026-08-18) becomes the primary alternative if
  it confirms native raw PPG / R-R streaming.
- The **Amazfit Bip 6** remains the leading off-the-shelf candidate, and is
  attractive precisely because it needs no proprietary SDK — Zepp OS is reported
  to broadcast standard BLE heart rate (to be verified with the same protocol).
- **None of this blocks the pilot.** Path 2 — heart rate from fans' own watches
  — is independent of the supplier decision. See `docs/path-2-roadmap.md`.

---

## Appendix — run log, 2026-08-24

| # | Time | Build | Purpose | Outcome |
|---|---|---|---|---|
| 1–3 | 14:59–15:03 | v1 | First raw-PPG attempt | 2 packets then silence; then zero |
| 4 | 16:32 | v4 | Seconds-based duration, HR fallback | **2.03 s of raw PPG** (10 packets, 800 samples, ≈394/s), then 47 ignored re-arms |
| 5 | 17:00 | v5 | Toggle stop→start re-arm | Zero PPG (watchdog too aggressive vs warm-up); 51 s of 1 Hz HR; ramp signature 0.50/0.59 bpm/s |
| 6 | 17:18 | v6 | Warm-up aware timings; effort test | Flat through 30 s of effort; peak +94 s late; spurious −35 bpm drop |
| 7 | 17:57 | v7 | A/B/C raw-PPG probe | **Zero packets, all three configurations** |
| 8 | 18:33 | v8 | **Polar-referenced 3-block protocol** | **All three criteria failed** |

Raw CSVs and the analysis of each run are attached to the session record. The
instrumented app source is in `hardware/jstyle-spike/`; the prebuilt APK is
published to the `spike-apk` release by CI on every push.
