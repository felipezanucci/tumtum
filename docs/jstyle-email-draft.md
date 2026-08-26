# Draft emails to J-Style (Arena Lee)

## 1. Test results — sent 2026-08-24

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


---

## 2. Reply to the customization offer — sent 2026-08-25

**Her answer, in short:** the standard V8 SDK does not support continuous raw
streaming; that requires dedicated firmware customization. Terms offered:
**NRE USD 15,000, MOQ 5,000 units** — covering SDK/API licensing, protocol
documentation, firmware support, and integration assistance. She proposes
defining sampling rate, packet format, streaming duration and data handling
with their engineering team before finalising scope, and notes our 2-second
observation reflects the standard firmware rather than a hardware limit.

**Why this reply says no for now:** at roughly USD 40–80 per unit, a 5,000-unit
MOQ is USD 200,000–400,000 of inventory plus the NRE — committed *before* the
Phase 0 hypothesis is validated. Firmware customization also runs 2–4 months,
past the pilot. The two questions below are the ones worth asking before
parking this. Sent 2026-08-25.

---

**Sent as follows.** Felipe tightened the draft below: the two questions were
kept as written, the "if neither is possible" paragraph was dropped, and the
ask was restated once at the end as a single combined proposal — pilot batch
*and* NRE credit together, rather than two separate doors. Dropping the
graceful exit is deliberate: it leaves the offer on the table instead of
handing over a way to decline it.

**Subject:** Re: V8 validation results — next steps

Hi Arena,

Thank you for the clear answer. Confirming that continuous raw PPG requires
dedicated firmware — rather than an SDK command we had simply missed — closes
the technical question for us. You have now been direct with us twice, and it
has saved us real time both times.

We agree with your reading of the results: the sensor itself performed well,
with a mean absolute error under 2 bpm against our Polar H10 reference in
steady state. The constraint is firmware, not hardware.

On the commercial terms, let me be equally direct about where we are.

Tumtum is pre-launch. We run our first pilot events this quarter, using
participants' existing watches as the heart-rate source, specifically so that
we can validate demand before committing to hardware. A 5,000-unit MOQ is far
beyond what this stage can absorb. That is not a comment on your pricing: it is
that the commitment would come before the validation that would justify it.

I would rather find a path, so two questions:

1. **Is a pilot batch possible?** In the range of 50–100 units carrying the
   customized firmware, at a correspondingly higher unit price. Putting a
   working continuous-raw-PPG device on real users at a real event is exactly
   what would justify a volume order afterwards — and it would let your
   engineering team validate the customization in the field before a production
   run.

2. **Can the NRE be credited against a future volume order?** If we fund the
   USD 15,000 now and the pilot succeeds, having that amount applied to the
   first production order would make the decision considerably easier to
   defend.

Would it be possible for us to move forward on this basis — starting with a
smaller pilot batch of 50–100 units with the customized firmware, and with the
USD 15,000 NRE credited toward a future production order if the pilot is
successful?

Please let me know if this could be workable on your side.

Best regards,
Felipe

---

### What we are waiting on

An answer to the combined proposal. Three outcomes and what each means:

| Her answer | What we do |
|---|---|
| Pilot batch **and** NRE credit accepted | Define the technical scope with their engineering team; the V8 re-enters the hardware plan for Phase 1, behind the 25/09 pilot, not in front of it |
| Pilot batch only, no credit | Still workable — the batch is the expensive part to walk away from, not the NRE |
| Neither | J-Style stays parked, not closed. Veepoo remains the alternative; we come back when demand is validated and volume is defensible |

Nothing in the 2026-09-25 pilot depends on this answer. Path 2 carries it.

---

### The draft this replaced

Kept for the record — the version that offered a graceful exit.

**Subject:** Re: V8 validation results — next steps


Hi Arena,

Thank you for the clear answer. Confirming that continuous raw PPG requires
dedicated firmware — rather than an SDK command we had simply missed — closes
the technical question for us. You have now been direct with us twice, and it
has saved us real time both times.

We agree with your reading of the results: the sensor itself performed well,
with a mean absolute error under 2 bpm against our Polar H10 reference in
steady state. The constraint is firmware, not hardware.

On the commercial terms, let me be equally direct about where we are.

Tumtum is pre-launch. We run our first pilot events this quarter, using
participants' existing watches as the heart-rate source, specifically so that
we can validate demand before committing to hardware. A 5,000-unit MOQ is far
beyond what this stage can absorb. That is not a comment on your pricing: it is
that the commitment would come before the validation that would justify it.

I would rather find a path than close the conversation, so two questions:

1. **Is a pilot batch possible?** In the range of 50–100 units carrying the
   customized firmware, at a correspondingly higher unit price. Putting a
   working continuous-raw-PPG device on real users at a real event is exactly
   what would justify a volume order afterwards — and it would let your
   engineering team validate the customization in the field before a production
   run.

2. **Can the NRE be credited against a future volume order?** If we fund the
   USD 15,000 now and the pilot succeeds, having that amount applied to the
   first production order would make the decision considerably easier to
   defend.

If either is workable, I am ready to define the technical scope with your
engineering team — sampling rate, packet format, session duration and the
data-handling requirements you mentioned. We already have a validation protocol
and an instrumented Android app that could serve as the acceptance test for the
customization, and I would be glad to share both.

If neither is possible at this stage, I understand completely. In that case I
would like to keep the door open. When we have validated demand and are ready
to commit to volume, J-Style is the supplier we would come back to first — you
have been straight with us at every step, and that counts for a great deal in a
decision like this one.

Best regards,
Felipe Zanucci
Tumtum

---

## Draft 3 — reply to Arena's 3,000-MOQ / rebate counter (2026-08-26)

> Context: Arena refused the 50–100 unit pilot batch, countered with MOQ
> 3,000 and an NRE rebate ladder starting at 10,000 cumulative units. The
> decision log records why this is declined on timing, not price. Goals of
> this reply: acknowledge the genuine movement, decline without closing the
> door, anchor the timeline to our pilot, and plant the acceptance-criteria
> flag for any future contract.

Dear Arena team,

Thank you for the detailed reply, and for taking our situation seriously.
Reducing the MOQ from 5,000 to 3,000 units is a genuine step, and the
cumulative-order rebate tells us you see long-term potential in this
project. We see it too, and we value the effort you have put into finding
a structure that could work for both sides.

After careful consideration, we have decided to put the customized Raw PPG
project on hold for now — a question of timing rather than of your terms.
Our September pilot runs on existing consumer devices precisely so that we
can validate real market demand before committing to custom hardware. A
3,000-unit commitment with the full NRE upfront is a production-scale
decision, and making it before the pilot gives us demand data would not be
responsible on our side — nor, we believe, good for the partnership.

Two questions, so that we can move quickly when the time comes:

1. Could you let us know how long the 3,000-unit MOQ and the rebate
   structure remain valid?
2. When we move forward, firmware acceptance criteria will matter to us as
   much as commercial terms. Our validation protocol tests amplitude
   response with the wearer stationary — the condition where the current
   firmware limits rises. Could you confirm whether the customized Raw PPG
   firmware removes the motion-conditioned processing entirely?

We expect pilot results in October and will come back to you with volume
planning if the data supports it. We appreciate the relationship we have
built so far and hope to continue it at the right moment.

Best regards,
Felipe Zanucci
Founder, TumTum
