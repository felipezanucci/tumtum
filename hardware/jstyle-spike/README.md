# JStyle V8 raw-PPG spike

Goal: decide the JStyle purchase by testing whether Tumtum can derive accurate,
low-latency heart rate from the V8 bracelet's **raw PPG stream**, bypassing the
firmware HR algorithm whose motion gating blocks our stationary-peak use case
(validated against a Polar H10 in Jun–Aug 2026; confirmed by JStyle on 2026-08-20).

Full step-by-step guide (PT-BR): see the "Guia do Spike PPG" artifact linked in
the project notes.

## Contents

| File | Purpose |
|------|---------|
| `PPGActivity.java` | Drop-in replacement for `app/src/main/java/com/jstyle/test2025/activity/PPGActivity.java` in the vendor's `v8test` Android Studio project (inside `V8 SDK/v8_sdk_android.zip`). Adds CSV logging of every raw PPG sample (wall-clock timestamp + packetID), logging of the firmware's own HR values, and auto re-arm of the measurement session for multi-hour runs. |
| `analyze_ppg.py` | Derives 1 Hz heart rate from the recorded raw PPG, compares it against a Polar H10 reference and the firmware HR, and reports the decision metrics: peak delay, peak amplitude ratio, MAE, sample rate, packet loss, re-arm count. |

## Quick run

1. Unzip the vendor SDK, open `v8test/` in Android Studio, replace `PPGActivity.java`
   with the copy in this folder, run the app on an Android phone (8.0+).
2. Connect the V8, open the PPG screen, press **Start** — files land in
   `Android/data/com.jstyle.test2025/files/` (`ppg_raw_*.csv`, `events_*.csv`).
3. Record the Polar H10 in parallel (e.g. Polar Sensor Logger on a second phone).
4. Analyze:

```bash
pip install numpy scipy pandas matplotlib
python3 analyze_ppg.py --ppg ppg_raw_XXXX.csv --events events_XXXX.csv --polar polar_hr.csv
```

`analyze_ppg.py` was smoke-tested against synthetic PPG with a known HR peak
(recovers 100 Hz sample rate, MAE 0.7 bpm, peak delay 2 s, amplitude ratio 0.99).

## Decision thresholds (from the Jun–Aug validation protocol)

- Stationary HR peak delay vs Polar H10: **≤ 5 s** (firmware today: 23–33 s)
- Peak amplitude captured: **≥ 85%** (firmware today: 46–64%)
- Packet loss over a session: **< 5%**
- Soak: raw stream survives **4 h** (with auto re-arms) without manual intervention

Green on all four → proceed with JStyle and negotiate the integration package.
Red on any → escalate to the vendor with the numbers, and advance the Veepoo track.
