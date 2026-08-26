# Decision log

Running record of what was decided, why, and what is still open. Details live in
the linked documents — this file is the index and the reasoning, not a diary.

---

## Where things stand — 2026-08-25

| Track | Status |
|---|---|
| **Hardware supplier** | J-Style **parked, not closed — and they reopened it themselves.** Their counter (pilot batch refused; MOQ 5,000 → 3,000; NRE US$ 15k with a rebate ladder paying back only from 10,000 units) was declined on timing. Arena then asked for "more vision", and **Draft 4 was sent 2026-08-26** — it argues the small batch as *their* risk reduction on the unanswered firmware question. **Ball in their court.** No NRE and no volume before the pilot. |
| **Android app (hybrid)** | **The full cycle is proven at real scale.** 26,999/27,000 readings overnight, screen off, 7% battery; the ~27k-point upload landed at quality 100% and the whole night rendered in-app. Launcher icon confirmed on the A17 as the TUMTUM wordmark on black. What remains is Health Connect. |
| **Path 2 — fans' own watches** | **Phase 1 validated in the field, and rehearsed on the phone.** A 25-minute capture recorded 1,504 readings in 1,504 seconds — one per second, nothing lost. Reconnection that never gives up, R-R intervals. |
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
11. **The screen wake lock is the one fix still unconfirmed.** It cannot be
    reproduced here and the rehearsal did not isolate it: the phone was being
    handled throughout. What was confirmed is that a capture survives leaving
    the app and coming back. What is still untested is the screen staying lit
    on its own.
12. ~~**Waitlist follow-ups.**~~ All closed 2026-08-26.
    `WAITLIST_ADMIN_EMAILS` is set, `/admin/waitlist` makes the list readable,
    `oi@tumtum.cc` is confirmed working, and all five social accounts exist and
    are linked. The one thing worth a second look: the Facebook handle is
    `tumtum.ccc`, with three c's, unlike every other account — given by Felipe
    and used as given, but it is the kind of detail that is either deliberate
    (the two-c name was taken) or a typo nobody would notice.
13. **The end-of-night upload is 1.33 MB in a single request.** Measured, not
    changed. If it fails on festival cellular nothing is lost — the snapshot
    survives and the button can be pressed again — so chunking it was judged
    not worth a contract change four days out. Revisit if it actually fails.

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

**Left as given, but noted:** the Facebook handle is `tumtum.ccc`, three c's,
unlike every other account. Either the two-c name was taken or it is a typo,
and only Felipe can say which.

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
