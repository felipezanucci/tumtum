# Draft email to J-Style (Arena Lee) — 2026-08-24

Context and numbers: `docs/jstyle-v8-evaluation.md`.
Review before sending; adjust the closing paragraph to how hard you want to push.

---

**Subject:** V8 validation results — raw PPG streaming question before we decide

Hi Arena,

Thank you again for your answer on August 20th. Confirming in writing that the
motion filter cannot be disabled saved us weeks, and that kind of straight
answer is rare from a supplier — it counts in your favour.

Following your suggestion that another technical approach might be more
appropriate, we spent this week testing the one path that would work for us:
reading the **raw PPG signal** from the V8 through your Android SDK and running
our own beat detection on it, bypassing the firmware algorithm entirely. We
built an instrumented version of your demo app and ran it on a Galaxy A17, with
a Polar H10 chest strap as ECG reference.

I want to share what we measured, because two of the findings are about your
protocol rather than about our use case, and may be useful to you either way.

**1. The processed heart rate from the SDK is the same filtered value as the app.**

We ran our validation protocol — three blocks of 60 s rest, 30 s maximum
effort, 90 s seated recovery — against the Polar H10:

| Criterion | Our requirement | V8 measured |
|---|---|---|
| Peak amplitude captured | ≥ 85% | 34% (21–56%) |
| Peak delay | ≤ 5 s | +42 s to +54 s, or peak never reached |
| Mean absolute error | ≤ 5 bpm | 17.5 bpm |

Through the 30 seconds of maximum effort the V8 reported a flat 78–82 bpm while
the reference climbed to 128. In two of the three blocks it never reached even
half of the real heart-rate excursion, under-reading by up to 47 bpm. This is
consistent with what you described, so it is not a surprise — but it does close
that path for us.

**2. We could not sustain the raw PPG stream.**

This is the part where we need your help. Using
`setECGRealtimeDuringHRVEnabled(true)` during an HRV measurement, we received
raw PPG exactly once: 10 packets, 800 samples, about 394 samples/s, a clean
optical waveform — and then it stopped after 2.03 seconds. The following 47
re-arm commands produced nothing.

We then ran a controlled test with three different command configurations,
after a physical device reset, including one that reproduced the successful
sequence exactly and never cleared the raw-PPG flag. Across 32 measurement
commands and all three configurations we received **zero packets**.

**3. Two protocol details you may want to check.**

- One raw PPG packet contained roughly 33 header/metadata bytes interleaved
  among the samples — the SDK parser does not appear to separate frame metadata
  from payload cleanly.
- After around 32 measurement commands the device stopped responding even to
  `AutoHeartRate` and needed a physical reset. Our observation window was short,
  so treat this as a lead rather than a conclusion.

**What we need to know to decide.**

1. Is there a documented command sequence that keeps the raw PPG stream running
   **continuously**, rather than for ~2 seconds per session?
2. What is the nominal PPG sampling rate, and what is the exact frame layout?
3. Is there a maximum measurement session duration, and can a session be
   sustained or automatically re-armed for **4 hours** (the length of a concert
   or a match)?
4. The constants `openRRInterval` and `realtimeRRIntervalData` are declared in
   the parser of every SDK variant we received, but no method sends an
   activation command. **Is this what the US$ 15,000 integration package
   unlocks?** If so, we would need the exact scope in writing: continuous raw
   PPG and/or live R-R streaming, a 4-hour session, protocol documentation, and
   an acceptance test we can run ourselves.

To be transparent about where we stand: our product depends on detecting a
heart-rate rise in someone who is standing still at a concert. If continuous raw
PPG is available and documented, the V8 sensor itself looked good in our steady
state measurements and we would like to continue with you. If it is not
available, we will close the technical evaluation and continue with other
options, and I would rather know that quickly than keep both of us busy.

Happy to share our full test data and the instrumented app if your engineering
team wants to reproduce any of this.

Best regards,
Felipe Zanucci
Tumtum
