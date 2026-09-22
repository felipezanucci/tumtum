# Decision log

Running record of what was decided, why, and what is still open. Details live in
the linked documents — this file is the index and the reasoning, not a diary.

---

## Where things stand — 2026-09-21

| Track | Status |
|---|---|
| **Hardware supplier** | J-Style **broke their own MOQ.** Arena's 2026-08-28 reply offers **10–50 units** of the customized raw-PPG V8 at USD 80/unit — the pilot batch Draft 4 argued for — with **NRE USD 30,000** (double the previous 15k, and the rebate ladder gone). She accepts our Polar protocol as the objective acceptance test, proposes agreeing criteria before development, and says explicitly there is no need to rush until Phase 0 results. **Draft 5 written, not sent:** bank the concession, decide nothing, plant three structural questions for after 25/09. Still no NRE and no volume before the pilot. *(History: pilot batch refused; MOQ 5,000 → 3,000; NRE 15k with a rebate ladder paying back only from 10,000 units — declined on timing. Arena then asked for "more vision"; Draft 4 went out 2026-08-26.)* |
| **Android app (native)** | **22/09, b149 in Felipe's hands: Bloco B passed six of six, and nine findings came back.** The two football anchors show their own clock on the button, a repeated anchor is refused, a show never shows APITO — the match clock works in a hand. Everything else the morning found became one batch: **the card is now burned into the person's own video** and leaves through the system share sheet, so it posts to Instagram, X, TikTok, Snap, WhatsApp or the gallery alike; marks reach the server without waiting for a night; a failed event registration is said on the screen the operator is actually looking at; and every `<select>` on the site was white-on-white and unreadable. *(earlier 22/09:)* **four items in one PR (#77)** — the match clock with two operator anchors, the guess list under an unnamed moment, the web admin that replaces the operator sheet, and video on the card through Instagram's Story editor; none yet in a hand. **22/09, b145: the loop is closed end to end, in a real hand.** teste7 — 240 readings captured on the Polar, uploaded, the two sync steps visible under the curve, `Momentos encontrados pelo servidor`, and the peak at 22h35 named **GOL** by a mark tapped during the capture. Item 41 answered: the night does reach the server. Eight of eight findings from 21/09 confirmed fixed; the Compose wheels roll. *(earlier 22/09:)* b142 in Felipe's hands the same night — three of the eight fixes confirmed on the phone (the operator door, the gallery photo, the battery row), the Android date/time wheels came up **blank on One UI** and were rebuilt in Compose, and the past-night flow was **cut at his word**; b143 on its way. *(21/09:)* b140 met Felipe's hands. Blocos 1–6 of the test list run; no crash, eight findings — two product rules, three "the app said nothing about its own state" bugs, one layout that overflowed on the one screen used in the dark, one photo the gallery dropped, and a night whose upload nobody could see happen. All eight built the same session (entry of 21/09) and waiting for the next build; Bloco 8 (the reveal lock, before 10h) still to run. **Two apps existed; on 18/09 the designed one, `cc.tumtum.app`, became *the* app — `docs/one-app-plan.md` brings the proven pipeline into it in five stages, and Etapas 0–3 were built, merged and *proved on a phone* the same day.** Four rehearsals with the Polar on 18/09 (`app-b111` → `app-b120`): capture at 1 Hz with no gap, upload, the server's detector, a moment **named GOL** by a mark tapped during the capture, the card, the share sheet. Fourteen defects of the "app unclear about its own state" class found and fixed across the four; one backend 500 that had waited since the timeline endpoint was written. **Next: Etapa 4, Play.** The rest of this row describes `cc.tumtum.capture`, now the reference: proven at a real six-hour event, 29/08. The Realness capture ran 21:11→03:17 with the strap, uploaded, analysed, and opened as a night with **20 moments**. Not a WebView shell: Sign-in that knows its own token's expiry, an event chosen before capturing, a retry that retries, a native night (curve + moments, drawn on a Canvas) and a native card with the system share sheet. Capture itself is untouched: 26,999/27,000 readings overnight, screen off, 7% battery, upload at quality 100%. Every build is now signed with a committed key, so the app updates in place instead of demanding an uninstall. **Health Connect is built to the screen (v0.2, Etapas 1–3)** — what remains is a watch in a hand: the device test, and the density measurement the screen itself now performs. **0.1 stays on Felipe's phone until after the festival.** |
| **Path 2 — fans' own watches** | **Etapa 0 closed, 30/08.** Samsung writes heart rate to Health Connect all night, no gap — but at **1/min in background and 1 per ~32 s inside a workout**, and the two live in *different records*. The decisive number came from the strap: the twenty moments it found last 8–22 s (median 13), so **every one of them is shorter than the interval between two Fit3 readings**. The watch path delivers *the curve of the night*; the moments need the strap. Cross-validated the same night: strap 116 bpm and Fit3 115 bpm, both at 01:24. **Untested: Xiaomi Mi Band 9** (bought, one night away) and Apple Watch. **Qualified 17/09:** that verdict is about a concert. A goal lasts minutes, and in simulation a watch at 1 per 32 s recovers 4 goals in 5 — at 1 per minute, 1 in 5. Opening the Mi Band answers both this and item 23 for nothing. |
| **Detection** | **Validated against a second device in the field.** 20 moments at Realness, durations 8–22 s; the night's max agreed with an independent optical sensor to within 1 bpm, at the same minute. The quality score, which read a flat 100% over a 79-minute hole, now measures **continuity** — the share of 5-second slots holding a reading — and puts Realness at **78**. Old sessions are restated the next time their night is analysed. **Rebuilt 17/09.** Simulation showed the 300 s rolling *mean* could not see an emotion longer than ~90 s — a goal celebration dropped, a favourite song sung for four minutes invisible, only the 8–22 s spikes inside them reported, which is exactly the Realness signature. Now a rolling **median** over 1200 s with an IQR spread, hysteresis and a 10 bpm minimum rise; peaks carry the bounds of their region; the correlator names a moment by its **cause** (latest entry between region start and peak) instead of the nearest entry to the peak. Match 5/5, show 3/3 songs + 3/3 spikes, Realness-shaped night 20/20, zero on a quiet night, 0.05 s for six hours. 19 tests. **In production since 18/09 (#49).** Confirmation on real data is one tap: "Procurar meus momentos" on the Realness night — item 29. |
| **Backend** | Live on Railway and **carrying the quality fix and the new card since 01/09**. Deploys from `main` via Railway's own git integration. |
| **Frontend** | **Live on tumtum.cc, desktop and mobile**, merged 01/09 (#45 then #46). Ten sections from the Claude Design handoff, bilingual — `/` in PT and `/en` in English, one layout, `hreflang` alternates. The v0.5 handoff's mobile design shipped too: the four cards are a snap-scrolling swipe carousel, the nav is a text MENU panel, the proof strip is full-width rows, the gallery leads with copy. **One responsive page**, verified at 360/390/480/768/1024/1440 with no horizontal overflow at any width. The handoff's ~25 MB of GIFs ship as 1.4 MB of MP4. **18/09:** `/privacidade` and `/en/privacy` live, linked from the footer — the URL the Play Console asks for. |
| **Brand** | **Manual v0.4 (31/08) is adopted and shipped.** TumTum Pink `#FF6F91` replaced Acid Lime everywhere — 70 usages, three codebases, live since 01/09. `docs/design-brief.md` is the self-contained handoff for design tools. Mutation skins still parked. |
| **Share loop** | Card 01 built to the manual, at Story size and inside the safe areas, generated from a real capture, and sharing opens the system sheet **with the image attached** — the plumbing is done. **The card itself is not.** Felipe's verdict on the Realness card, 30/08: it does not create any desire to post. It leads with a number nobody is impressed by (92, because ranking is by magnitude, not bpm), carries a headline that is identical on every card ever made, and has no evidence of the night on it. **Half fixed 31/08:** the card leads with the highest peak (116, not 92), the copy is generated from the night's own numbers, and the curve is on it as evidence — the gap in a capture is drawn as a gap. **The surface is still the base one**, and which card people actually post is now an open research question for the pilot. |
| **Polar as fallback** | **Working end to end.** A real Polar Flow export imports; the average it computes matches the one Polar wrote into the file. Beat → Flow sync is manual — pull down and hold. **This is now the only fallback** — the browser capture path was retired 2026-08-26. |
| **Pilot** | **The 25/09 date is probably lost.** Felipe said on 17/09 he most likely cannot run the test at the Tasha & Tracie show. The calendar was searched and shortlisted in `docs/pilot-event-options.md`: a **football match** as the technical test (objective timestamps, a peak synchronised across every chest in the stadium, tickets that actually exist, and a kick-off that ends before midnight) and a **concert with an engaged fan base** as the product test (which card someone actually sends). Still **decoupled from the supplier decision**. The binding constraint is not the calendar: with one chest strap only one person has moments, and card 04 cannot be tested at all. |

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
   strap. The product side is closed; what remains is organising. **Reopened
   2026-09-17:** the 25/09 date is probably lost, and
   `docs/pilot-event-options.md` holds the shortlist that replaces it — a match
   first (São Paulo × Vitória 10/10, or Corinthians × Fluminense 20/09 if the
   Fiel Torcedor biometrics are already registered), a concert second (BTS
   28–31/10 through people who already hold tickets, Hayley Williams 12–13/11,
   or Tasha & Tracie in Santos 06/11). **How many straps is the decision that
   actually sizes the pilot**, not which date.
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
17. ~~**The landing page has no sign-in link.**~~ Closed 2026-09-01 by the
    site redesign: "Entrar" in the nav and "Entrar na sua conta" in the
    footer, both to `/login`.
18. **An event cannot cross midnight** — one date, two bare times, so
    "termina 03:00" cannot say it means the next day. A capture attaches by
    event id, so Saturday is unaffected; the model change waits.
21. ~~**`data_quality_score` cannot see a hole.**~~ Fixed 2026-08-30. The
    score counts 5-second slots now, so a gap empties its slots and shows.
    Realness reads 78 instead of 100. **What is left is one deploy and one
    tap:** the row in the database still carries the old 100 until that night
    is analysed again, and the app itself is the thing that does it —
    "Procurar meus momentos" restates the score from the stored readings.
22. **The Realness moments have no names.** No timeline rows for the event, so
    `116 bpm às 01:24` has no story attached — and the story is the card.
    Felipe's memory of the night is the only source.
26. ~~**The palette changed and the code has not.**~~ Done 2026-08-31, forced
    by the design-system sync: syncing components whose accent is a deleted
    colour would have seeded every screen designed in Claude Design with lime.
    70 usages across 34 files, all three codebases. Every `bg-tumtum-lime`
    already carried `text-tumtum-black`, so contrast survived the swap intact.
27. ~~**Mobile is adapted, not designed.**~~ Closed 2026-09-01: the v0.5
    handoff arrived with the mobile design and all four questions answered —
    the cards became a swipe carousel, the gallery went full-width with its
    copy first, the feed's screenshots stayed side by side at half width, and
    the nav got a text MENU panel. Implemented as one responsive page.
    *(Original:)* **Mobile was adapted, not designed.** The Claude Design handoff specified
    desktop only (~1440px), so everything below `md` is engineering: where the
    desktop composes, the site stacks. It is correct — verified at 390×844,
    nothing overflows — and it is not a design. Felipe is commissioning a
    mobile handoff. The four places the adaptation is dumbest, and which only
    design can answer: **the four share cards** (stacked into a long scroll;
    a snapping horizontal swipe is the obvious alternative), **the 2×2
    gallery** of rotated posters (two 196px columns do not breathe in 390px),
    **the feed's two phone screenshots** (showing a phone inside a phone
    needs another composition), and **the nav**, whose section links simply
    disappear below `md`, leaving wordmark + CTA + PT/EN.
23. **Xiaomi Mi Band 9 untested.** Bought, unopened as far as the log knows.
    One night answers whether the 1/min ceiling is Samsung's or Health
    Connect's — the last open question of Etapa 0.
24. **The card does not make anyone want to post it.** Raised 2026-08-30,
    **half closed 2026-08-31**: the card now leads with the highest peak
    instead of the highest-ranked one, says something built from the night's
    own numbers instead of a constant, and carries the curve as evidence.
    **Still open is the surface** — cover the wordmark and it is still a black
    field with a lime number. Mutante Pop is the declared territory and the
    skins are still parked; that is the remaining half.
25. **Which card people actually want to post is a research question, not a
    design opinion.** Felipe's call, 2026-08-31, choosing layout D: *"esse é um
    ponto que vale pesquisarmos com o público da TumTum."* He is right, and it
    is worth writing down why. Four layouts were argued from first principles —
    what the number means, what the copy can prove, what the manual permits —
    and every one of those arguments is about **whether the card is honest and
    well made**, which is not the same question as **whether a stranger posts
    it**. Nobody here can answer the second from a screen. What makes it
    answerable: the four renders already exist, so the material for a
    side-by-side is built. The cheapest real signal is the 25/09 pilot — 3–5
    people who will each have a night of their own — and the honest measure is
    not "which is prettiest" but **which one someone actually sends to
    somebody**. Do not let the taste of the people who built it stand in for
    that.
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
28. **The timeline code cannot produce a usable timeline.** Found 2026-09-17.
    *(22/09: the naming half of this is now item 42 and
    `docs/naming-moments-plan.md`, which costs both parsers honestly and
    says why the setlist one has a ceiling no fix can raise.)*
    *(Sidestepped for the pilot on 18/09, Etapa 3: marks tapped during the
    capture become timeline entries with wall-clock instants, and the
    fourth rehearsal proved it end to end — a GOL tapped at 14h51 named the
    moment the server found. The dead code stays dead.)*
    `parse_fixture_to_timeline()` adds the match minute to the kick-off and
    ignores the ~15-minute half-time interval, so every second-half event is
    15–20 minutes early — fifteen times outside the correlator's ±60 s window.
    `parse_setlist_to_timeline()` estimates 4 minutes per song, which drifts
    past the window by roughly the third song, because Setlist.fm publishes
    order and never times. **Neither service is imported by any route or
    test.** Until one of them is fixed and wired, every pilot timeline is typed
    by hand through `POST /api/events/{id}/timeline` — 6–10 entries for a
    match, ~20–25 for a show.
29. ~~**The peak detector cannot see an emotion longer than ~90 s — which
    rules out a song.**~~ Found by simulation 2026-09-17 and **fixed the same
    day**: rolling median + IQR spread, 1200 s window, hysteresis, a 10 bpm
    minimum rise, and a causal matching rule in the correlator (entry of
    2026-09-17, "rebuilt around a median"). 19 tests. **What stays open is the
    confirmation on real data:** the next re-analysis of the Realness night.
    If moments appear that last minutes, the 8–22 s durations of 30/08 were
    the instrument's ceiling and the watch verdict of that day is re-read via
    item 30; if not, the night was spikes and the fix cost nothing.
31. **From 30/09 the APK will not install on an untouched Brazilian phone**
    unless TumTum is a verified developer with the package and signing
    certificate registered (entry of 2026-09-17). Register as a person this
    week (ID, US$ 25); make a release key out of the repo and a release
    build; register `cc.tumtum.capture` + that certificate; keep ADB and the
    24-hour advanced flow written down per phone as the fallback. The Play
    Store is *not* required. The installed build today is
    `cc.tumtum.capture.debug`, signed with the committed debug key — which
    is what would otherwise get registered.
    **A public Play listing by 10/10 is not realistic** (12 testers × 14
    days for a new personal account, the Health apps declaration review,
    the foreground-service declaration); **the internal testing track may
    be** — a Play link, automatic updates, verification handled — pending
    one check: whether the Health declaration is enforced on that track.
    **Account type, decided 17/09 at the sign-up screen:** personal now.
    A personal Play account can be upgraded to an organisation in place
    later (a new organisation payments profile, verified with CNPJ + D-U-N-S,
    then linked — apps, package names and users stay; the reverse is not
    possible). Organisation now would need a D-U-N-S number, up to 30 days,
    and miss both 30/09 and 10/10. Costs of personal until the upgrade: the
    12 testers × 14 days rule before production, and payments/taxes in
    Felipe's name. Upgrade before monetising.
32. ~~**Account deletion is a promise kept by hand.**~~ Built 2026-09-18:
    `DELETE /api/users/me` removes shares, cards, readings, peaks, sessions,
    wearable connections and reset tokens before the user, in that order
    (tested); "Apagar minha conta" in the app calls it first and wipes the
    phone only when the server confirmed, saying so when it did not. The
    privacy page now names the in-app path first, the e-mail second. The
    Play data-safety form can answer "yes, in the app".
33. **Two Android apps existed; the pilot's app is `cc.tumtum.app`.**
    Decided 2026-09-18. `docs/one-app-plan.md` is the merge: Etapa 0 one
    repository, 1 a real account, 2 the night uploads and the server's
    moments come back, 3 the event has a name, 4 Play. `cc.tumtum.capture`
    is the reference until each piece is ported, then retired. **Etapas
    0–3 done and proved on Felipe's phone, 18/09:** the fourth rehearsal
    came back with a moment named *GOL* by the server, 169 bpm, 129 s.
    **Next: Etapa 4, Play** — the account is verified; what is left is
    "Create app" as `cc.tumtum.app`, the `.aab`, the internal track, and a
    minified build tested on a phone.
34. **A public feed with no block and no report will not pass an open Play
    release.** Raised 2026-09-18 while answering the content-rating
    questionnaire. The app declares user-generated content shared publicly
    (feed, public profile, published nights) and answers *no* to "can users
    block others" and *no* to "can users report users or content", which is
    true today. **The internal testing track, with a closed list of
    e-mails, does not enforce it**; a production release to the open store
    normally does. Before the store listing goes public, either build a
    report and a block (the smaller version: report a night, block a
    profile, both landing somewhere a human reads) or keep the feed private
    to the pilot. Not a pilot blocker; a launch blocker.
35. **Deleting one night, keeping the account, does not exist.** Found
    2026-09-18 while answering the Play data-safety question "can users
    request deletion of some data without deleting the account". There is
    no delete on the reveal screen, no `deleteNight` in the repository and
    no `DELETE /api/health/sessions/{id}` — only account-wide deletion
    (item 32). **The privacy page had been promising it since 17/09**
    ("também dá para apagar uma sessão específica"), which is the same
    defect class this log keeps counting, this time in a legal document:
    the copy is corrected to say it is all or nothing, and the Play form
    answers *no*. Worth building before the pilot: a fan who captures a
    night by accident, or at the wrong event, currently has to delete
    their whole account to be rid of it.
30. **The Mi Band 9 is now the cheapest experiment in the project.** It answers
    open item 23 *and* whether a wrist device can carry a football moment: at
    one reading per 32 s the simulation recovers 4 goals in 5, at one per
    minute only 1 in 5, and 1/min is the band's documented best continuous
    setting. One night of wearing it measures which it actually writes.
36. ~~**"A gente te avisa" — and nothing does.**~~ Closed 20/09 (lote 3): the
    reminder exists (AlarmManager, set again on boot and on every start), the
    locked screen promises it only when notifications are enabled, and the
    lock itself stays an operator setting. *(Copy corrected 19/09, lote 1:
    the locked night now says only "A curva abre aqui às 10h. Vale a espera."
    The decision — fan feature with a real notification, or protocol only —
    is still open.)* Found 2026-09-19 by the
    psychology audit (`docs/app-psychology-principles.md`, §4.3). With the
    reveal lock on, the locked night says *"A curva abre aqui às 10h. A gente
    te avisa — vale a espera."*, and no code schedules a notification for
    `revealAt`; only the capture service ever posts one. The log's bug class,
    in the one sentence whose job is to make the wait bearable. Either build
    the notification (the one push the product is allowed) or cut the
    sentence — and decide first whether the lock is a fan feature or only the
    blind-card protocol (`one-app-plan.md`, *Later*).
37. ~~**"Postar no feed" posts to nobody.**~~ Closed 19/09 (lote 1): the button
    is gone and the chosen skin is saved when the card is shared instead.
    *(Original:)* Same audit, §4.5. The button on the
    card screen writes into `FakeSocialRepository`, which lives on this phone
    and is populated with invented people, and the confirmation reads *"No
    feed. A galera já pode sentir também."* On the Play internal track that
    is a false claim in a tester's hands. Hide the button until the feed is
    the server's.
38. **The ranked backlog of behavioural mechanics is written, not decided.**
    *(22/09: the §5.1 answer built on 20/09 — a night brought from the
    watch's history — was cut at Felipe's word; "the first value is days
    or weeks from install" is fully open again.)*
    `docs/app-psychology-principles.md` §5 lists thirteen, costed in
    sessions, with the principle behind each; §6 lists eleven the brand
    refuses so the argument is not had twice. The largest one is not a
    design task: **the first value is days or weeks from install**, and every
    benchmark app delivers it in minutes (§5.1 — a retroactive night from
    the watch's own history, or a demo night labelled as one). Felipe's
    to pick from; nothing there is on the pilot's critical path.
39. ~~**A fan can type an event, and must not be able to.**~~ **Built the
    same day** (second entry of 21/09): AO VIVO lists TumTum's events for
    the fan to activate, the typed sheet is the operator's and its times are
    the Android wheels, `ServerEvent` reads the hours. On a phone: not yet.
    *(Original:)* Two product
    rules from Felipe's test of b140 on 21/09 (entry of that day). (a)
    **Events are TumTum's**: the fan never creates one — the events where
    TumTum works are registered by us, appear on the fan's screen, and the
    fan *activates* one or not. (b) **A time is never typed**: every time
    field is the platform's picker. What that changes in the app: "Marcar
    o próximo" and "Trazer uma noite que já passou" become a pick from the
    server's list (the server already sends `date`, `start_time`,
    `end_time`; the app reads only `date`); the typed sheet — name, venue,
    kind, date, times — moves behind the OPERADOR door with pickers in
    place of its text fields; `EventTimes` becomes operator-only. **Until
    this is built, Bloco 7's typed-date checks are not worth running.**
    Costed at one session.
40. ~~**A web admin for events, on tumtum.cc.**~~ **Built 22/09 (#77).**
    `/admin/eventos` lists every event and `/admin/eventos/{id}` holds its
    timeline: the entries told apart as exact or estimated, each removable,
    a match from API-Football or a setlist from Setlist.fm attached in two
    taps, a mark added by hand with a picked time. The server decides who
    operates (`ADMIN_EMAILS`, and the waitlist's admins count) and answers
    `is_admin` on the account; create/update event, the demo seed and the
    timeline sources are refused to anyone else with a 403 the site puts
    into words. The fan's `/events` lost "Novo evento" and the demo seed
    button. *(Original ask, 21/09:)* *"um
    site web dentro do domínio da TumTum com login de administrador"* where
    TumTum registers the events that then appear in every fan's app.
    Evaluated the same day and agreed: web wins over the phone because the
    operator registers from a desk, with a keyboard, looking at a setlist —
    and almost all of it exists. The server already has create/update event
    and the timeline endpoints (`backend/app/api/events.py`), the site
    already has an admin area (`frontend/app/(app)/admin/waitlist`), and the
    app now reads the times. What is missing is **one page**: a form (name,
    venue, city, kind, date, start, end — pickers, never typed) and a list to
    edit, gated like `/admin/waitlist` is. Until it exists the operator's
    sheet on the phone is the admin, and it sends the same fields. Costed at
    half a session. Not on the pilot's critical path while the operator and
    the founder are the same person.
41. ~~**Did teste3 reach the server?**~~ **Answered 22/09 on b145**: yes,
    the pipeline works. teste7 uploaded 240 readings, showed both steps under
    the curve, came back `Momentos encontrados pelo servidor`, and the peak at
    22h35 carried the name **GOL** from the mark tapped during the capture.
    teste3's silence on b140 was the invisibility bug, not a failed upload.
    *(Original:)* Unknown, and it matters: the GOL tapped
    at 18h20 did not name the moment at 18h20·35 s. The upload's state was
    invisible on b140 (entry of 21/09, item 6), so the night may have failed
    to go up, or gone up and been analysed without the mark matching. The
    next build says which on the first line under the curve; until then
    **Felipe can scroll down on the teste3 night in b140 and read the line
    under the moments** — "Momentos encontrados pelo servidor" means the
    correlator is the question, anything else names the failure. The
    backend's correlator tests cover a goal just before a region; nothing
    covers a mark *inside* a region that is the peak's own cause. If the
    night did go up, that is the test to write next.
42. **A moment's name cannot depend on anyone remembering to tap.**
    Felipe, 22/09: *"a pessoa que está no estádio assistindo o jogo não vai
    lembrar de clicar gol."* Right, and narrower than it sounds — the
    detector already finds moments with no marks at all (its `timeline`
    parameter is unused), and the fan already never sees a mark button
    (operator switch, off by default). What marks buy is the **name**.
    Analysed in **`docs/naming-moments-plan.md`**: football is solvable
    (fix the half-time, anchor on two operator taps — kick-off and the
    second half — and every goal names itself for everyone, ~1 session);
    a concert is not solvable from the setlist at all, because Setlist.fm
    publishes order and never times, so any estimate has a ceiling of a
    few minutes and a few minutes is a wrong name. **Amended the same day:
    the audio-recognition path is dead for live music** — a fingerprint
    matches one specific *recording*, and a band playing live is not that
    recording, so the technique is by construction inapplicable to live
    performance (a positive Shazam match at a concert is evidence of
    backing tracks, not of recognition working). It survives only for DJ
    sets. Price, since it was asked and is not the obstacle: AudD
    US$0.005/request, ACRCloud ~US$0.0045, ~180 requests for a 3 h show,
    **under US$1 per show.** So the order is now: **1)** football —
    half-time arithmetic + two operator anchor taps, the only exact
    timeline available to us, ~1 session; **2)** the **guess list** from
    setlist + Spotify durations, which with (a) gone is *the* concert
    answer rather than a stopgap, ~1 session; **3)** nothing, which is
    fine — card 01 needs no name. Supersedes the naming half of item 28.
    **1) and 2) built 22/09 (#77):** the match clock with two operator
    anchors (`football_service.py`, `MatchClock`; APITO and 2º TEMPO on the
    capture screen of a sports event; `kickoff`/`second_half` entry types)
    and the guess list (`setlist_guess.py`; `candidate_labels` on every
    unnamed peak; "TAVA ROLANDO UMA DESSAS?" chips under the moment). The
    rule that ties them: **a measured time asserts, a derived time offers**
    — an entry with `estimated: true` or `anchored: false` never reaches the
    correlator. Waiting on a real match to be tested. **Amended the same
    day by item 44:** the guess list stands, but *Setlist.fm cannot feed
    it* — its API terms forbid the local datastore the feature writes into.
    The order has to come from the operator typing it, or from MusicBrainz.
43. **Can the card carry a video, not just a photo?** Felipe asked on
    22/09 to check before building. **Yes, and it is the most expensive
    thing on the list.** The picker is one word (`ImageAndVideo` instead
    of `ImageOnly`); the render is not — compositing the card over a video
    means MediaExtractor → MediaCodec decode → an OpenGL surface → MediaCodec
    encode → MediaMuxer, ~400–600 lines of EGL and timestamp work that ships
    with Android (no dependency) and behaves differently per chipset, which
    is exactly the code that cannot be verified from an environment with no
    SDK. **A much cheaper 80% exists:** Instagram's `ADD_TO_STORY` intent
    takes a video as the background and a transparent PNG as the sticker
    layer — the person's video with our card over it, in the place this
    product actually gets posted, for about half a session. Recommendation:
    the Instagram route now if video matters before the pilot, the full
    encoder after it, never both at once. **Instagram route built 22/09
    (#77)** — `export/InstagramStory.kt`: the picker takes video when
    Instagram is on the phone, the card renders as a transparent sticker
    (scrim kept), the video is copied into our cache and both go to
    `ADD_TO_STORY`. Two things only the phone can answer: how large
    Instagram draws a full-frame sticker, and whether it insists on the
    Facebook App ID (`res/values/instagram.xml`, empty until registered).
    **Both phone questions answered 22/09, and the scope widened.** Felipe
    put a video through the Instagram route: it worked *without* the
    Facebook App ID, and Instagram drew the sticker **smaller than the
    screen** — the flat scrim showed as a visible black box behind the card.
    His ask then changed the item: *"pros usuários poderem subir video em
    qualquer rede social. seja instagram, x, snap, tiktok, etc."* **Built
    the same day, and the 400–600 lines never had to be written.** The
    insight is that it is **one file, not N integrations**: every one of
    those apps, plus the gallery, takes an MP4 from the system share sheet,
    so nothing social-network-specific is needed at all. `androidx.media3`
    Transformer does the decode → overlay → encode → mux pipeline that was
    costed as hand-written EGL (`export/VideoCard.kt`, 175 lines including
    its reasoning). API 23 needed, this app is minSdk 28. Choices: 9:16
    scale-to-fill with centre crop, 30 s from the start with the screen
    saying it was trimmed, and **the encoder's own progress figure** rather
    than a bar on a timer — Media3 answers "not started" and "unavailable"
    as well as a number, and a bar sitting at 0% because nobody asked is the
    same lie as a spinner. The scrim became a gradient anchored on the
    *measured* top of the type block, plus a text shadow as a second net;
    Felipe asked whether the mask could go entirely and it cannot — his own
    test video had a white t-shirt directly behind the white meta line.
44. **Setlist.fm cannot be the setlist source, and it is not about money.**
    Researched 22/09 when Felipe asked whether the two API keys are his to
    sign up for (they are — neither can be provisioned programmatically).
    Three separate walls, any one of which is fatal:
    **(a)** The API is non-commercial only, and commercial is defined by
    *purpose*, not revenue: *"If the primary purpose of your application is
    to derive revenue, it is considered commercial."* Pre-revenue is not a
    defence. **(b)** setlist.fm has been a **Live Nation / Ticketmaster**
    property since March 2012, so a commercial licence is a Live Nation
    contract rather than a self-serve upgrade — and the requests go
    unanswered: a developer emailed `help@setlist.fm` three times with no
    acknowledgement, and a forum moderator confirmed the address was right
    while saying *"it usually takes ages to get a reply, and I honestly
    don't know why."* **(c) The one that bites regardless of money:** the
    **API** terms (`setlist.fm/help/api-terms`, a different document from
    the general terms) forbid a persistent local datastore — short-lived
    caching only, direct server calls, immediate distribution to end users
    — plus a mandatory followable attribution link per setlist
    (`json_Setlist.url`). **`attach_setlist` writes every song into our
    `event_timeline` and keeps it**, which is exactly what that clause
    forbids, on a free key and on a paid one alike. So #77's setlist source
    is built against a source we may not use in production. One thing the
    research *settled* in our favour: `json_Song` in setlist.fm's own
    OpenAPI spec has exactly five fields — cover, info, name, tape, with —
    so "order only, never times" is **proven**, not inferred, and the guess
    list's whole premise holds. **The route that survives is the operator
    typing the setlist**: zero cost, zero terms risk, guaranteed coverage,
    and `setlist_guess.py` already does not care where the order came from.
    Second-best is **MusicBrainz**, the only free, commercial-use-permitted
    source that structurally holds setlists — `setlist` is a *core column*
    of the replicated `event` table (CC0), not a CC-BY-NC-SA annotation —
    but its coverage is probably too thin for São Paulo and could not be
    sampled from here. Ruled out: Songkick (closed to applicants, from
    USD 500/month, tour dates not setlists), Bandsintown (self-serve but
    artist-scoped, no setlists), Last.fm (no setlists), Spotify (does not
    expose setlists through the Web API — the suspicion was right),
    scraping (same breach with the rate protection removed).
45. **API-Football is fine, cheap, and already publishes the anchors we
    built taps for.** Same research, 22/09. Signing up is five minutes:
    `dashboard.api-football.com/register`, no credit card, the free plan
    active on clicking the confirmation link, key at Account → My Access.
    **Take the direct route, never RapidAPI** — RapidAPI is the same data
    behind `api-football-v1.p.rapidapi.com` with `x-rapidapi-key`, and the
    keys are not interchangeable with the `v3.football.api-sports.io` +
    `x-apisports-key` pair our code uses. Free is 100 requests/day and
    **10/minute** (not 30). **Whether a free key can read the *current*
    season is genuinely disputed in the sources** — one verification pass
    found two September-2026 GitHub reports quoting a live API error
    *"free plan serves seasons 2022 to 2024 only"*, another found the
    opposite framing (free covers current, paid unlocks deeper history).
    Every vendor page is egress-blocked from this environment, so **one
    request with a real key settles it and nothing else will.** It matters
    less than it looks: the live route needs polling (480 requests at 15 s
    or 120 at 60 s for a two-hour match) which blows 100/day either way, so
    a real pilot is **Pro at USD 19/month** regardless. Two findings worth
    code: **(i)** the fixture object carries `periods.first` and
    `periods.second` — UNIX timestamps for each half's start — and
    `football_service.py` reads neither (verified by grep); whether they are
    the *real* whistles or the schedule restated is undetermined and rests
    on a single source, so they belong **below** the operator's taps and
    **above** the schedule guess, never asserted as measured. **(ii)** A
    documented second route to the real whistle: poll `/fixtures?live=all`
    and watch `status.short` cross `NS`→`1H` and `HT`→`2H`, with `BT`
    (break time) sitting between the last two. Terms: API-SPORTS grants **no
    competition rights of its own** and pushes the licence burden onto us,
    disclaims ownership of crests and logos (so a club crest on a share card
    is our risk, which widens the manual's existing ban), prohibits reselling
    the data, and **encourages caching** — so storing match events in our own
    tables is fine, unlike setlist.fm. Brazilian alternative for the record:
    **API Futebol** (self-serve, commercial use permitted, Brasileirão +
    Copa do Brasil + Libertadores + estaduais, `/ao-vivo` with the current
    minute) at **R$ 99/month per championship** — not cheaper, but it covers
    the estaduais and it is local.
46. **Nothing in items 44 and 45 is legal clearance.** Every primary page —
    `setlist.fm`, `api-sports.io`, `api-football.com`, `football-data.org`,
    `musicbrainz.org`, `fotmob.com` — is blocked by this environment's
    egress proxy (403 on CONNECT, reproduced twice). Every quotation above
    reached us as a search-engine extraction of a page nobody here could
    open. Of 33 checked claims on the alternatives pass, 18 survived
    adversarial verification, 11 came back UNCERTAIN and **4 were refuted**,
    one of them backwards in a way that would have changed the plan. Before
    any money or any promise rests on this: **a human opens
    `setlist.fm/help/api-terms` and `api-sports.io`'s terms and reads them.**
47. ~~**Who may write a timeline entry.**~~ **Closed 22/09 — and it was a
    data leak, not only a correctness bug.** Felipe asked whether this was
    about the community feed; it was not, and checking in order to answer him
    found that the hole was **worse than the item described**. The assistant
    had told him the fan's naming was "annotation on their own night". It was
    not: `nameMoment` → `addMark` → `NightSync.pushMarks` →
    `POST /events/{id}/timeline`, so what a fan typed went onto the **shared**
    event timeline. Two consequences, neither of them malice — just two uses
    sent down one pipe. A fan typing *"golaço kkkk"* on their own 22h41 could
    put those words on a stranger's card, since the correlator reads that
    table for everyone at the event; and what they meant as a private note was
    readable by the operator and by anyone else there.
    **The fix cost nothing.** The label was already kept on the phone, and
    `NightSync` already carries a local label across a re-analysis when the
    server has no name for that moment — so the server call added only the
    unwanted half. It is gone, the endpoint takes `require_admin`, and the
    re-upload that used to follow a naming went with it (there is nothing
    left to tell the server). `tests/test_operator_only_routes.py` reads the
    router rather than a docstring, and was checked by reverting the guard and
    watching it go red.
    **One trap on the way:** locking the endpoint means a non-operator account
    gets a 403 there, and `upload` would have turned that into a FAILED night
    — the platform's permissions costing a fan their own readings. `upload`
    now survives a **refusal** and nothing else: swallowing every failure
    would let a network blip upload a night linked to no event *for good*,
    since `serverSessionId` is stored and never recreated.
52. **The OPERADOR toggle is a UI gate, not a permission.** Noticed 22/09
    closing item 47. The capture screen's mark buttons are behind
    `user.operatorMarks`, which is a **local DataStore flag** anyone can turn
    on in Configurações — the app never asks the server who it is talking to,
    even though `/api/auth/me` has answered `is_admin` since #77. Harmless
    today, because the server refuses the write and the night survives it
    (item 47), but the app can end up showing a counted mark that will never
    leave the phone — the "app stating something false about its own state"
    class, again. The fix is to read `is_admin` into the session and gate the
    toggle on it. Not urgent while the operator and the founder are the same
    person.
53. **The community feed: decided per event, 22/09 — and the screens already
    exist, on invented data.** Felipe's call: *"apenas as pessoas que
    estiveram no evento podem interagir. Isso deve criar um senso de
    comunidade maior."* Agreed, and it is the stronger product: it is card 04
    (*A galera*), which the brand manual already specifies, the closed group
    makes moderation tractable, and "a galera que estava neste jogo" is a
    reason to open the app that "everyone on TumTum" is not.
    **Correction to what this item said an hour earlier.** It claimed nothing
    was built — no endpoint, no table, no page. The first two are true; the
    third is not. `FeedScreen`, `EventFeedScreen` and `CrowdScreen` have been
    in the app since the design mockups, behind `SocialRepository`, and the
    implementation wired in is `FakeSocialRepository`. So **the design work is
    done** and what is missing is the backend and the gate — which makes this
    much cheaper than it was costed at, and much more urgent than it looked
    (item 54).
    The sub-question the decision opens, and it is the one with teeth:
    **what proves someone was there?** The honest answer already in the
    database is an `hr_sessions` row linked to that `event_id` — evidence, no
    new data, and it ties posting to having a night to post. Its edge is a
    person whose strap died: they were there and cannot take part.
    Still true: item 34 is the price of entry, and block and report ship
    **with** the feed rather than after it. Still open: a card carries the
    person's BPM, so posting publishes health data — that needs consent per
    post, not a blanket setting, and it needs to be undoable, which ties it to
    item 35 (deleting one night does not exist).
54. ~~**The app's first screen shows invented people.**~~ **Deleted 22/09.**
    `Routes.Feed` was the start destination and rendered
    `FakeSocialRepository`: a banner claiming *"Hoje: Taylor Swift · 3 amigos
    confirmados"*, moments by **Mariana Alves (194 bpm)** and **Rodrigo Costa
    (176 bpm)** — people who do not exist with heart rates nobody measured —
    and an event feed claiming **8,734 people shared**. On the Play internal
    testing track since 18/09. The worst instance of the class this log keeps
    counting, because it was not a stale control or a wrong message but
    fabricated people and fabricated measurements, in a product whose promise
    is that the number is really yours. The repository, `CrowdScreen` and its
    3,412 invented people are gone, and what replaced them is item 53's real
    feed — the honest fix and the feature were the same work.
55. ~~**Block and report still do not exist, and the feed is live.**~~
    **Built 22/09** — see the entry "report and block exist". The Play
    content questionnaire's two answers change to *yes*. Item 34
    said they ship *with* a public feed. The feed shipped on 22/09 and they
    did not, so this is the gap, recorded rather than left implicit. What
    makes it survivable for now: the feed is **per event and closed** — only
    people with a measured night at that event can read it, post to it or
    react — the only reaction is positive with no scale, there are no public
    profiles and no handles, and every post carries a take-down for its
    author. That is a crowd-sized moderation problem, not a platform-sized
    one, and the pilot's crowd is people Felipe knows. **Before the store
    listing goes public it is still a blocker**: report a post, block a
    person, both landing where a human reads them.
57. **Every APK link this session was the wrong one, and the right one already
    existed.** Felipe, 22/09: *"eu quero o link que inicie o download direto...
    sem clicar em nada. sempre me mande o link do apk assim."* A workflow
    artifact can never be that — GitHub requires a signed-in session and always
    wraps it in a `.zip` — so every link sent this session really meant "log
    in, download a zip, unzip it, find the apk".
    **The direct links were already there.** `build-app.yml` has published a
    GitHub Release on every `main` build since b136, and this repository is
    public, so `releases/download/app-bN/tumtum-1.0-bN.apk` has always been a
    plain URL that starts the download with no account at all. Releases exist
    for b136, b140, b142, b145, b147, b149, b153, b156, b159, b161 — one for
    **every build Felipe merged**. The assistant never opened the Releases page
    and offered only the artifact route, so he accepted the bad path because it
    was the only one on the table.
    The 404 he hit was `app-b160`: a *branch* build, and the release step was
    gated `if: github.ref == 'refs/heads/main'`. Fixed by publishing on every
    build, branch builds as prereleases, so an APK can be tested before the
    merge. **That fix is real but smaller than it looked** — what was missing
    was not the mechanism but reading what the repository already did.
    Verified rather than assumed: `app-b161` returns HTTP 200, 38 MB,
    `application/vnd.android.package-archive` with no credentials, and the
    branch build published `app-b162` as a prerelease on its own.
    Recorded in CLAUDE.md as standing: **never send an
    `actions/runs/.../artifacts/...` URL again.**
56. **Nobody has ever seen the feed with more than one person in it.** The
    privacy floor in `services/crowd` is four measured nights, and a pilot
    with three to five straps sits exactly on it. So the two states most
    likely to appear in Felipe's hand are *"ainda somos poucos aqui"* and an
    empty feed — both honest, both untested against a real crowd. The
    collective moment (card 04) cannot be judged until an event has four
    nights uploaded, which makes **one match with four straps** the cheapest
    experiment that answers whether any of this is worth anything.

---

## 2026-09-22 — report and block exist, and deleting an account that had posted did not work

**#36 / item 55, decided by Felipe:** *"a gente não vai ter como fugir dessa."*
The feed shipped this morning without report or block, and the log said so:
a crowd-sized moderation problem, but a blocker before the store listing goes
public. Built in the smaller shape item 34 described — report a post, block a
person, both landing where a human reads them:

- **Both are made from a post.** The feed names nobody any other way — no
  handles, no profiles, no ids on the wire — so the server resolves the post
  to its author. Only somebody who can see a post (`require_attendance`) can
  act on it.
- **A report is one of three reasons**, *ofensivo*, *parece falso*, *outra
  coisa*, and **no free text**: a report box is the one place strangers could
  otherwise write to each other. One per person per post.
- **It lands in `/admin/denuncias`**, where an operator keeps the post or
  takes it down, and in an e-mail to every operator when e-mail is configured.
  **Three distinct reports hide the post** until somebody decides — a crowd
  acts faster than an inbox — and "Manter" brings it back. The author still
  sees their own post; a post does not vanish on the person who made it.
- **A block works both ways.** Neither sees the other's posts, so a block
  cannot be used to watch somebody who can no longer see you. It is undone in
  Configurações, where the list shows names and nothing else.
- The screens are the careful kind the manual asks for on safety: plain
  words, no jokes, each choice saying what it will do before it does it.

**The Play questionnaire now changes.** On 18/09 it was answered *no* to
"can users block others" and *no* to "can users report users or content",
which was true. Both are now *yes*.

### The bug this found

Writing the deletion rows for the two new tables showed that the feed's own
two tables — `event_posts`, `event_post_reactions` — had never been added to
`account_deletion.py`. `event_posts` points at the user **and** at the night,
with no ON DELETE clause, so **deleting an account that had ever posted was
refused by the database**: the privacy page's promise, broken for exactly the
people who used the feature, since this morning. Found before anybody tried.

The deletion list was a tuple somebody had to remember to update, and the
test checked the tuple against itself. The new test reads **every foreign
key in the schema** and fails if any table that points at a person, a night,
a card or a post is missing from the list. Refresh tokens, reports and blocks
are in it; so is whatever gets written next.

---

## 2026-09-22 — colour on the first screen, one Pink button on the last, and the card Felipe picked

Wave 4 of the evening's findings.

**The feed home had no colour (#30).** Felipe: *"tá muito cinza, preta e
branca… mais colorida, mais viva."* The manual's digital default is a black
canvas with Pink as the main emphasis and Toxic Yellow as the second
explosion; the screen had a white page, grey rows, and one yellow row when
something was live. It is now black; the headline's second line is Pink at 34
sp; the night that is on now is a **full Pink block** with black type; every
other event is a dark card whose door is a Toxic Yellow pill. Pink on black is
half as loud as the old lime, so the emphasis comes from surface and scale,
as the manual says it must.

**The Stories button is gone (#40).** Felipe: *"a gente pode tirar esse botão
de postar no Instagram Stories."* The card is burned into the video and
leaves through the system share sheet, which reaches every network alike; a
button naming one of them put it above the rest. With it went the
`<queries>` entry that let the app ask whether Instagram was installed on the
phone — a question it no longer needs to ask about anybody.

**The done screen had two Pink buttons and a card drawn over its own text
(#41).** The card preview was a fixed 214 dp wide inside a box that shrank as
the done state stacked title, stats and five buttons below — so the 380 dp
card overflowed and drew its curve across "8 noites em 2026 · 10 momentos".
The preview now takes its width from the height it is given. And the screen
has **one primary act at a time**: *Mostrar pra galera do rolê* when the night
can go to its rolê; only *Pode mostrar* / *Agora não* while asking; *Ver a
galeria* once posted or when posting is impossible. "Compartilhar de novo" is
a line of text.

**Card A2 (#42), Felipe's pick from three mockups.** The acid chip that sat
alone at the top of the card is gone; the event now sits in an acid box **at
the foot**, "bpm às 22h12" beside it, the wordmark to the right — everything
about *where* on one line under the evidence. A long name ("SÃO PAULO ×
VITÓRIA") takes its own row rather than being cut to four letters; the rule
lives in `CardFoot`, shared by the renderer and the preview so they cannot
disagree. On the yellow and white skins the box turns black, since acid on
either would vanish. **The curve is thicker (8 → 11 px) with a dark outline**,
and the peak marker is ringed: over a bright frame the old line disappeared,
and the curve is the card's evidence.

---

## 2026-09-22 — a session lasts as long as the phone is used

**#34, decided by Felipe:** *"refresh token de verdade, e a pessoa só reentra
quando desinstala ou sai — conserta a causa."* Until now the only credential
was a 24-hour access token with nothing to renew it, so every account was
signed out once a day — while the app went on showing the person's name and
avatar. Felipe read "Entra na sua conta" on a screen that plainly knew who he
was, and could not tell whether to sign in again or whether something was
broken. Both claims were the app's; they contradicted each other.

What replaced it:

| | |
|---|---|
| access token | **1 hour** — short enough that a revoked session stops soon |
| refresh token | **90 days from last use**, opaque, stored only as a hash |
| every renewal | **rotates** the refresh token |
| a spent token presented again | a copy exists → **the whole family is revoked** |
| "Sair" | revokes the family on the server, not just the phone |
| password reset | revokes every device's family |

**The part that took thought: stadium cellular.** Rotation plus "reuse means
theft" has a failure mode exactly where this product lives. The server
rotates, the answer is lost on a saturated network, the phone asks again with
the token it still holds — and a strict reading signs the fan out mid-match.
So for **two minutes** after a rotation, and only while the token it produced
has never been used, the old token may ask again: the unused child is revoked
and a fresh one issued. Once the child has been used, whoever holds the parent
holds a copy by definition. `parent_id` and `revoke_reason` exist for that
rule and nothing else. A logout or a reset gets no grace.

Two self-inflicted versions of the same failure are closed on the clients:
the app renews under a mutex and the web under a browser-wide lock
(`navigator.locks`), so two requests — or two tabs — renewing at once cannot
spend the same token twice and revoke their own session. The app renews a
minute before expiry rather than after a 401, so no request leaves with a
token that dies in flight.

`Session.isLive` now means *usable without a password*: a live access token
**or** a refresh token to renew it. Judged by the hour-long token alone, every
screen would have announced "sessão expirada" sixty minutes after sign-in.
When the server refuses the chain, the app drops the refresh token, and the
session then reads as expired — which is at last true, and is said with
"Entrar de novo", which returns to where the person was (#35).

**What the merge costs, said before it happens:** any client that does not
read `refresh_token` — the APK in Felipe's hand today, and the retired
`android-capture` app — now holds a one-hour session instead of a 24-hour
one, until it is replaced. Installing the build from the same merge ends it.
The web admin renews on its own from the same merge.

Migration `010_refresh_tokens`; the deployed app creates the table itself at
startup (open item 16). Tests: `backend/tests/test_refresh_tokens.py`.

---

## 2026-09-22 — the app stops describing itself falsely, eight times over

Wave 2 of the evening's test findings, all one family: **the app stating
something about its own state that is not true.**

- **The button that faded (#45).** A disabled button was drawn at 40% alpha.
  TumTum Pink at 40% over white is about `#FFD4DE` — on a phone, in real light,
  no button at all. Felipe had raised it before ("tem que ficar rosa o tempo
  inteiro"). The fill is now always the fill; "not yet" became a sentence
  under the button naming what is missing. Settings' name field had used the
  fade as its *only* confirmation that a save happened; it now says "Salvo."
- **The error that was a cancellation (#46).** `catch (e: Exception)` caught
  `CancellationException`, so every reload that a newer one superseded
  painted "Não deu pra carregar o rolê" for a second. Proved by the
  screenshot: the crowd chip vanished in the same frame, as it had to.
- **SENTI TB (#47).** The server answered with the post's new count; the app
  dropped the body at the wire, reduced the rest to a Boolean, and ignored the
  Boolean. The answer is now the count, applied in place. Third time in one
  evening that the reason for a failure had been thrown away — with the
  football search (#43) and the cancellation above.
- **The invitation with no door (#48).** "Pode ser você" now comes with
  "Mostrar a minha" when this phone has a night at the event, and stops
  inviting when it cannot offer the act.
- **The fan is never asked (#49).** Felipe: *"pode tirar, não vai haver mais
  essa possibilidade do usuário digitar."* "Toca pra dizer o que tava rolando"
  was the last door left after the guess chips. A moment arrives named by the
  timeline, or stays nameless.
- **The header that forgot (#32)** and **the name said twice (#31).**

---

## 2026-09-22 — the API had written the answer and the code threw it away

Felipe searched the admin for a Palmeiras match on 20/09 and read **"Nenhum
jogo com esses dados."** The match existed: Grêmio × Palmeiras, 11h.

What API-Football had actually answered was:

```json
{"errors": {"season": "The Season field is required."}, "results": 0, "response": []}
```

**With HTTP 200.** Every call in `football_service.py` ended
`if response.status_code != 200: return []`, and `grep errors` over the file
returned nothing. So a wrong key, an uncovered season, a spent quota, an
illegal parameter combination and a genuine zero all came out as the same
empty list and the same sentence on screen. The nineteenth instance of the
defect this project keeps counting, now in an operator tool: **an empty state
is a claim.**

### What it cost to find

An evening, and it should have cost five seconds. The diagnosis was in the
response body the whole time. Ruling things out by hand took three rounds at
Felipe's Terminal, because this environment cannot reach `api-sports.io` and
the key lives only on Railway:

| ruled out | by |
|---|---|
| plan doesn't cover 2026 | dashboard: **Pro**, active |
| quota spent | `requests: {current: 2, limit_day: 7500}` |
| bad key | `/status` → HTTP 200 with the account on it |
| UTC date rollover | kick-off 14:00Z on the 20th — same calendar day |
| wrong team id | `teams?id=121` → `Palmeiras, PAL, Brazil, 1914` |

Then `&season=2026` returned the fixture, `results: 1`, id 1492384.

**A second lesson, free:** the first diagnostic command I gave Felipe was
`curl -s`, whose `-s` suppresses the error message. Three blocks came back
blank and told us nothing. `curl -s` *is* `if status != 200: return []` — I
wrote the very bug I was hunting, into the tool I was hunting it with. The
habit is not rare and not anyone's carelessness; it is what "handle the error"
degrades into when nobody is watching.

### Fixed

- **The season goes out.** `search_fixtures()` already had a `season`
  parameter and already built `params["season"]` — `events.py` simply never
  passed one. The calendar year is right for Brazil, whose championships run
  January to December; European leagues label 2026/27 as season 2026, which
  the API will now say out loud if it ever matters.
- **One door to the API.** `_get()` is the only place that calls it, and
  nothing below it returns an empty result to mean a failure: a non-200 and a
  200 carrying `errors` both raise `FootballApiError`, which carries the API's
  own sentence to the operator's screen.
- **A team nobody has heard of no longer returns the whole day.** A typo used
  to fall through to `date` alone — 1151 fixtures.
- **A failed search clears the previous answer**, so the page cannot print
  "Nenhum jogo com esses dados" beside an error saying no search was made.

Ten tests, `backend/tests/test_football_api_errors.py`, built around the exact
payload that hid.

---

## 2026-09-22 — a measured time belongs to the slot, not to the song

**A reversal.** `_merge_started` keyed a setlist row's `started_at` on
`(position, title)`, reasoning that a song which moved should not carry its
old stamp onto a new slot. The reasoning was about the wrong thing.

Felipe corrected one word in a song's title during a test. The row's time
vanished, **COMEÇOU lit up again** as if the song had not started, and the
only recovery the screen offered was to tap it — writing *now* into a song
that began an hour earlier. His question was the whole argument: *"Como é que
eu vou marcar de novo? Se eu estiver fazendo isso pelo show mesmo."*

**A false measurement is worse than a lost one**, and the old key had made the
false one the easy path.

What the operator taps is a **slot**. "The third thing started at 21h44" is a
fact about the show's third thing, whatever it turns out to be called. So the
key is the position. Correcting a spelling, fixing the order after the fact,
or extending the tail all keep the record intact; a position nobody tapped
still comes back `None`, and a paste can never raise the number of measured
rows. The edit screen now says so where the operator is about to edit.

The case this does not handle: inserting a song *before* rows already
measured shifts them onto the wrong titles. In practice an operator editing
mid-show inserts at the current position, i.e. after the measured rows, so it
degenerates to editing the tail. Written down here rather than defended in
code.

---

## 2026-09-22 — the operator kept being dropped into the fan's site

Felipe clicked *Editar* on an event inside `/admin/eventos/{id}`, landed
somewhere that did not look like the admin, **read it as having been signed
out**, and typed `/admin/eventos` back in by hand.

He had not been signed out. Two operator screens were living in the fan's part
of the site and pushing back into it on save:

| was | is |
|---|---|
| `/events/novo` → pushes to `/events` | `/admin/eventos/novo` → pushes to the new event |
| `/events/[id]/editar` → pushes to `/events/{id}` | `/admin/eventos/[id]/editar` → back to the event |

Both also spoke in the fan's voice ("a noite que **você** vai capturar") about
an act the fan is never allowed to perform. **Events are TumTum's** (21/09),
and where a screen lives is part of saying so — `/events/novo` was a door to
creating an event sitting inside the fan's own list.

### And the date was still typed, in the wrong order

`<input type="date">` renders in the **browser's** locale, not the product's,
so Felipe got month-first and a box to type into. Two standing rules meet
there: a time is never typed, and every date or time field is a picker,
operator screens included. `DateField` is now three selects — **dia, mês,
ano** — beside the `TimeField` that already worked this way, months named
(`set`, not `09`) so they are read rather than counted. February knows how
many days it has; picking a month clamps the day instead of wiping it; a
value from another year is kept rather than silently shown as `--`. The stored
value stays ISO, because that is what the API reads — only the reading order
changed.

---

## 2026-09-22 — the link that was always there

Felipe asked for an APK link that downloads by itself. The answer took three
exchanges and the last one was the one that mattered: **the link he wanted had
existed for every build he ever merged, and nobody had looked.**

`build-app.yml` publishes a GitHub Release on each `main` build, and has since
b136. The repository is public. So
`releases/download/app-bN/tumtum-1.0-bN.apk` has always been a plain URL that
starts a download with no account — b136, b140, b142, b145, b147, b149, b153,
b156, b159, b161, one for every merge.

What was sent instead, all session, was the workflow artifact: a URL that
requires a signed-in GitHub session and always serves a `.zip`. "Here is the
APK" really meant "log in, download a zip, unzip it, find the apk". He took
that route without complaint because it was the only one offered.

His 404 was `app-b160` — a *branch* build, and the release step was gated on
`main`, so branch builds never published one. That gate is now gone and branch
builds publish prereleases, which is genuinely useful: an APK can be tested
before the merge rather than after. But it is a smaller fix than it first
looked, and calling it "the fix" would have hidden the real failure.

### The failure is not the gate

It is that a capability sitting in the repository's own workflow file went
unread for a month while its absence was worked around every single day. The
artifact route was never chosen over the release route — the release route was
never seen. Both were in `build-app.yml`, eleven lines apart.

The same shape as the `FakeSocialRepository` finding earlier today: the thing
that mattered was already in the codebase, and what was missing was reading
it. Twice in one day is a pattern worth naming — **before building a mechanism,
check whether the repository already has one.**

### Verified, not assumed

`app-b161` → HTTP 200, 38,069,756 bytes,
`application/vnd.android.package-archive`, no credentials sent. The branch
build published `app-b162` as a prerelease on its own, confirming the change
works. `app-b160` → 404, which is exactly why he saw one.

b161 is `main` with #83 merged, so it is the build carrying the real feed.

---

## 2026-09-22 — the feed is built, and what it cost was deleting the fake one

Felipe: *"Execute!"* — so the per-event feed of item 53 is built, and the
invented people of item 54 are gone. They were one piece of work, which is
the part worth remembering.

### The gate is evidence, not a claim

**What proves somebody was at an event is an `hr_sessions` row carrying that
`event_id`.** It needs no new table, it cannot be asserted by a client, and
it ties the right to take part to having a night to take part with — which is
also what makes the feed worth reading. `require_attendance` is the only
thing that opens the feed, and it answers **403** rather than an empty list,
because "you were not there" and "nobody posted" are different sentences.

Its edge is real and stated in the code: somebody whose strap died was there
and cannot join. The alternative — trusting the app's word that a person
activated an event — is not a gate at all.

### Four kinds of nothing

`SocialRepository` keeps them apart: empty, refused, signed out, failed —
and `Failed` carries whether it was the network. An empty list would collapse
all four into "nothing here", which is the claim this project has now been
caught making twelve times. Every one of them is its own sentence on screen.

### The privacy floor is the interesting part of card 04

A collective figure over a small crowd **is not collective**: with two nights
uploaded, "64% bateram o próprio pico" says exactly what each of those two
people did, and "o pico da galera foi às 22h41" points at whoever was there.
So `services/crowd` publishes nothing below four measured nights — and
returns the count that refused, so the screen says *"ainda somos poucos
aqui"* instead of drawing a zero.

One more rule that matters: **one peak per person, their biggest.** Somebody
with nine peaks would otherwise outvote three people who had one each, and
the question being asked is how many *people* rose at once.

### Consent is a moment, not a setting

Posting is not sharing. The share sheet sends a picture the person controls
to people they chose; a post puts their heart rate, at a named minute, in
front of strangers who happened to be at the same event. So the ask is at the
moment of posting, the sentence says what will be visible and to whom, and
the take-down is **deliberately not behind the attendance gate** — somebody
must be able to withdraw what they published even after their night is gone.
A consent whose undo can expire is not consent.

### What was deleted

`FakeSocialRepository`, `CrowdScreen` and its 3,412 invented people, the
public profiles of strangers, and every string that carried an invented
number. There are no handles and no cross-event browsing: the server sends a
display name and initials and nothing that identifies a person anywhere else,
which is the right amount of identity for a room of people who were already
in the same room.

### What is not done, and is now visible rather than implied

**Item 55:** block and report. Item 34 said they ship with a public feed;
the feed shipped and they did not. The per-event closure makes it a
crowd-sized problem rather than a platform-sized one — closed membership, one
positive reaction with no scale, no profiles, a take-down on every post — but
it is still a blocker before the store listing goes public.

**Item 56:** nobody has ever seen this with more than one person in it. The
floor is four measured nights and the pilot has three to five straps, so the
two states most likely to appear in a real hand are *"ainda somos poucos"*
and an empty feed. **One match with four straps** is the cheapest experiment
that says whether any of it is worth anything.

Backend 150 tests and ruff clean; the Android half compiles in CI only.

---

## 2026-09-22 — the feed is per event, and the one we already ship is made of invented people

Felipe decided the shape: **per event.** *"Assim apenas as pessoas que
estiveram no evento podem interagir. Isso deve criar um senso de comunidade
maior."*

It is the right call, and not only for the community reason. It is card 04 of
the brand manual, which has been specified since v0.4 and never had a sample
to exist. A closed group makes moderation tractable in a way an open feed does
not. And it gives someone a reason to open the app the day after a match that
"everyone on TumTum" never would.

### What checking the codebase found instead

An hour earlier this log said the feed was not built: no endpoint, no table,
no page. **The third was wrong**, and the way it was wrong matters.

`FeedScreen`, `EventFeedScreen` and `CrowdScreen` have been in the app since
the design mockups, behind a `SocialRepository` interface whose only
implementation is `FakeSocialRepository`. `Routes.Feed` is the **start
destination** and the first tab of the bottom bar. So the first thing the app
shows, today, on the Play internal testing track, is:

- a banner: *"Hoje: Taylor Swift · 3 amigos confirmados"*
- a moment by **Mariana Alves**, 194 bpm at 23h47, *"aqui acabou meu
  psicológico"*
- a moment by **Rodrigo Costa**, 176 bpm aos 89 do segundo tempo

None of these people exist. None of those hearts were measured. One tap on the
banner opens an event feed claiming **8,734 people shared** and that **64%
bateram o próprio pico** at a Taylor Swift night at the Morumbi.

This is the defect class the log keeps counting — the app stating something
false about itself — and this is the worst instance recorded. Everything
before it was a stale control, a message describing the wrong condition, an
empty state claiming "nothing here". This is **fabricated people carrying
fabricated heart rates**, on the first screen, in a product whose entire
promise is that the number is really yours.

Two things limit it. `postOwnMoment` is never called, so a real card never
mixes into the invented list — the two do not touch. And `CrowdScreen` is
unreachable, which was deliberate: the entry to "sua noite × a galera" was
kept out precisely because the screen behind it shows 3,412 invented people.
Somebody drew that line once and did not carry it to the tab next door.

It has almost certainly been read as placeholder by the only person who has
opened it. It does not survive one tester who is not the founder.

### What this changes about the estimate

The design is **done** — three screens, the domain models, the repository
interface, the seam already cut in the right place. What is missing is the
backend behind `SocialRepository` and the gate that decides who may post. That
is a great deal less than building a feed from nothing, and it means the
honest fix and the real feature are the same piece of work rather than two.

### The sub-question the decision opens

**What proves somebody was at the event?** The answer already in the database
is an `hr_sessions` row carrying that `event_id`: evidence rather than a
claim, no new data, and it ties the right to post to having a night to post
about. Its edge is the person whose strap died — there, and unable to take
part.

### What is still unresolved, and is not technical

A card carries the person's BPM. Posting it **publishes health data** to
strangers who happen to have been at the same match. That needs consent at the
moment of posting rather than a setting agreed once, and it needs to be
undoable — which runs straight into item 35, where deleting a single night
still does not exist.

And the honest risk in the decision itself: **per event is stronger at scale
and weaker at pilot size.** With three to five people carrying straps, a
per-event feed has three to five posts, and a feed that looks abandoned says
something worse than no feed. The same data at that size is not a feed at all
— it is card 04, one line: *"você e mais três estavam lá; seu pico foi o
maior."* Same query, same table, different surface. That is what the pilot can
actually fill.

---

## 2026-09-22 — item 47 was a leak, and the question that found it was about something else

Felipe asked whether item 47 was about the TumTum feed — the place where
people would share cards and the community engages. It was not: the **event
timeline** is the factual script of an event ("Gol de pênalti aos 39", "Yellow
às 22h12"), invisible to fans, read by the correlator to name moments. The
feed is a different thing and **does not exist yet** (item 53).

Checking the code in order to answer him found that the item understated its
own problem, and that the assistant had told him something false the message
before: that a fan naming their own moment was annotation on their own night.

It was not. `nameMoment` called `addMark`, and `NightSync.pushMarks` sends
every unsynced mark to `POST /events/{id}/timeline`. **What a fan typed went
onto the shared timeline of the event.** So a fan at Corinthians × Palmeiras
typing *"golaço kkkk vai corinthians"* on their 22h41 wrote a label that the
correlator could put on a stranger's card — and the note they thought was
private was visible to the operator and to everyone else there.

Neither half is malice. Two different uses had been sent down one pipe, and
nobody had looked at the pipe since.

### The fix cost nothing, which is why it should have been found sooner

The fan's label was **already** saved on the phone by `setMomentLabel`, and
`NightSync` **already** preserves a local label across a re-analysis when the
server has no name of its own for that moment. Everything the person sees
worked without the server call. What the call added was only the part nobody
wanted.

So: the `addMark` is gone, the endpoint takes `require_admin` like the rest of
the operator surface, and the re-upload that used to follow a naming went too
— there is nothing left to tell the server.

### The trap under the fix

Locking the endpoint means a non-operator account is refused there with a 403,
and `upload` pushes the event and its marks **before** the readings. Left
alone, TumTum's own permissions would have turned into a FAILED night for a
fan — their readings lost to a rule about who may write our timeline.

The first version swallowed every failure there, which was worse and wrong in
a quieter way: a night that uploads while its event could not be created is
linked to no event **permanently**, because `serverSessionId` is stored and
the branch never runs again. A network blip would have cost that night its
names forever, where failing the upload costs only a retry.

It now survives a **refusal** and nothing else. 403 is a statement about who
this account is, which no retry changes; everything else is a failure a retry
can fix, and stays one.

### The guard is a test that reads the router

`tests/test_operator_only_routes.py` walks the FastAPI routes and asserts the
dependency names, so the guard cannot be lost to a refactor without a red
build. It was checked the only way a test is worth anything — by reverting
`require_admin` and watching it go red, then restoring it.

It also pins the asymmetry that makes this correct rather than merely locked:
browsing events and **reading** a timeline stay open; writing does not.

### Two items opened

**52:** the OPERADOR toggle is a local DataStore flag, not a permission. The
app has never asked the server who it is talking to, although `/api/auth/me`
has answered `is_admin` since #77. Harmless now that the server refuses and
the night survives, but it can show a counted mark that will never leave the
phone — the same old class.

**53:** the feed, with the product question that precedes any code: everybody,
or per event? Card 04 already specifies the second, and it is the stronger one.

Backend 135 tests and ruff clean.

---

## 2026-09-22 — the card arrives ready, or it says nothing

Felipe made the rule absolute, and it is the one that reorganises everything
else: *"tanto pro futebol quanto pro show, a gente tem que dar pronto pro
usuário. [...] A gente não pode depender de nenhuma ação do usuário em nenhum
dos dois cenários."* The card must say *"esse pico foi por causa de um pênalti
aos 39 do primeiro tempo"* or *"esse pico foi porque tocou Yellow às 22h12"*,
and nothing may be asked of the fan to get there.

One distinction makes the rest tractable: **"no user action" is not "no TumTum
action".** The fan is the user. A person on TumTum's payroll is an operating
cost, which the rule permits — and for a concert it is the only thing that
works at all.

### The guess list is gone

The chips under an unnamed moment — *"tava rolando uma dessas?"* — were built
on 22/09 that morning and removed the same evening at Felipe's word: *"a gente
não pode contar com o usuário para ele ter que lembrar que aquele batimento
foi de uma música determinada, pode eliminar."*

They were the honest half-answer to a problem we had not solved, and the rule
says the honest half-answer is not wanted: either the app knows or it is
quiet. `candidate_labels` is out of the schema, the API, the client and the
Reveal screen. What survives is the rule underneath them, moved into
`event_correlator.is_tentative`: **a derived time reaches nothing.** An
unanchored match minute can be ten minutes out, and a name ten minutes out is
worse than no name, because a card that lies costs trust an unlabelled one
does not.

The fan's own free-text naming stays. That is annotation on their own night,
not a dependency the product rests on.

### Football: the API had the answer and we were not reading it

The API gives *what* and *which match minute*; it never gave *what time that
minute was*, which is why the two operator taps exist. Item 45 found
`periods.first` and `periods.second` in the fixture and could not settle
whether they are real whistles or the schedule restated — every vendor page
is egress-blocked from here, and it still is: `v3.football.api-sports.io`
returns `connect_rejected`, reproduced again today.

**So the code settles it instead, per match.** A period that differs from the
scheduled kick-off *cannot be the schedule restated* — something measured it.
A period equal to the schedule is indistinguishable from a feed that measured
nothing, and the safe reading of a tie is the pessimistic one. Both halves are
judged on one verdict, because what is being judged is the feed, not a number:
once `periods.first` is shown to be real for a fixture, `periods.second` from
the same payload is real too.

Every entry now carries `clock_source` — `tap`, `api_periods` or `schedule` —
so **the question answers itself over real matches** instead of waiting on a
document nobody here can open. If the periods turn out to be real, the two
operator taps at a match become optional and football needs nobody in the
stadium. Ten tests, including the tie, the sub-minute clock skew, a garbled
payload, and a feed that lends its second period only once it has proved the
first.

Priority is unchanged where it matters: the operator's tap still outranks the
API, because they were standing there.

### Shows: paste the order, then one button

No API exists and none is coming. So `event_setlist` holds the operator's
script: the order pasted in beforehand — a tour plays close to the same set
every night, so last night's is a good draft — and, during the show, one
**COMEÇOU** button that stamps the next song with the instant it really
started and writes a `song_start` entry on the event's timeline. Twenty taps
across two hours, no typing, nothing to decide while the music plays.

Each tap is a measured time, so the drift never accumulates: missing one costs
only that song, the next re-anchors, and the gap is visible on the screen
rather than silently guessed at. A correction pasted mid-show keeps the times
of songs still at the same position with the same title — moving a song drops
its stamp, because an old measurement is not evidence about a new slot.

Two things the tests pin that a careless version gets wrong: pasted numbering
is stripped, but **a dash tight against the title is not numbering** — strip
it blind and Logic's "1-800-273-8255" is filed as "800-273-8255"; and a song
that is only a number survives.

### What is not settled

Item 51: this screen is on the web, and a packed venue may have no signal.
The app would queue taps offline and already has the machinery. If the first
real show loses taps, that is the port to make.

Item 50 is the strategic one: **football scales and shows do not.** One API
call covers every match in the country; every show costs a body.

Backend 131 tests and ruff clean; frontend tsc, lint and 55 tests.

---

## 2026-09-22 — b149 in a real hand: the card burned into the video, and nine findings

Felipe tested b149 and the site through the morning. **Bloco B passed six of
six** — the first test of the match clock in a hand. The two anchors appear
first on a sports capture, a tapped one carries its own clock on the button,
a second tap of the same anchor is refused with *"é uma vez por jogo"*, a
repeated GOL is not refused (it is a second goal), and a show never shows
APITO. The clock built the night before survives contact.

Everything else here is what the morning found.

### Video, anywhere — and it was never N integrations

Felipe, after putting a video through the Instagram route: *"acho que
precisamos começar a solucionar esse problema de vez... pros usuários poderem
subir video em qualquer rede social. seja instagram, x, snap, tiktok, etc."*

Item 43 had costed the general case at 400–600 lines of MediaExtractor,
MediaCodec and EGL, and called it the most expensive thing on the board. Both
halves of that were wrong, for the same reason: **it is one file, not N
integrations.** Instagram, X, TikTok, Snapchat, WhatsApp, Telegram and "save
to the gallery" all accept the same thing — one MP4 through the system share
sheet. Burn the card into the file and every one of them is covered, with no
social-network SDK anywhere. And `androidx.media3` Transformer already *is*
the decode → overlay → encode → mux pipeline, so the EGL was never ours to
write. `export/VideoCard.kt` is 175 lines, most of them the reasoning.

Three choices, each reversible and each written down where the code is:
9:16 scaled to fill and centre-cropped (a Story crops, it does not letterbox);
30 s from the start, **with the screen saying it was trimmed** rather than
quietly shortening; and the encoder's own progress figure, polled — Media3
answers "not started" and "unavailable" as well as a number, and a bar sitting
at 0% because nobody asked is the same lie as a spinner that means nothing.

`ADD_TO_STORY` stays as the Instagram shortcut, where the card remains a
sticker the person can drag. Two of its unknowns are now answered: it works
**without** the Facebook App ID, and Instagram draws a full-frame sticker
smaller than the screen.

### The mask could not go, and his own video is why

Felipe asked whether the black mask behind the card could be removed
entirely, leaving only the peak and the number. It cannot — the frame he sent
has a white t-shirt sitting directly behind the white meta line. What it
could stop being is a *box*: the flat 60% scrim became a gradient anchored on
the **measured** top of the type block rather than a guessed fraction of the
height, so the video runs clean through the top half and behind the acid
chip and only darkens under the words. A text shadow is the second net.

### Marks are the event's truth, not one person's capture

jogo1's four marks — APITO, 2º TEMPO and two GOL — never reached the server.
Cause: `pushMarks` only ever ran **inside a night's upload**, and that capture
had no readings, so no night was ever created, so nothing carried them up.

That is worse than a lost test. If the operator's strap drops or their battery
dies, every fan at that match loses the two taps that turn API-Football's
minutes into real times — which is exactly the situation the taps exist for.
Marks now go up the moment they are tapped (`NightSync.pushMarksLater`), and
a failure leaves them unsynced for `retryPending` to find through
`eventsWithUnsynced`, with no night involved.

### Three more of the same family — the app knowing and not saying

The class the log has now recorded sixteen times.

- **jogo1 and show1 never registered on the server, and the app knew.** It
  wrote the warning to the AO VIVO tab, which "Começa agora" leaves in the
  same instant. The warning is on the capture screen now — where the eye
  already is. Same shape as 21/09's upload evidence below the fold.
- **Every `<select>` on the site was white text on the OS's white popup.**
  Only the selected option was readable, so the popup looked like a tall
  empty box. Felipe read the entry-type dropdown as *"only Momento exists"* —
  and his teste6 mark went in as the wrong type because of it. A dropdown
  that shows one of six options is not a cosmetic bug; it is the control
  lying about what it offers. Six selects, four files, one rule in
  `globals.css` (`color-scheme: dark`, which is what makes the OS draw the
  popup dark rather than us painting over it).
- **A jogo spoke like a show.** ROLANDO HÁ, "aproveita o jogo", and plurals
  so "1 marcados" counts in its own language — plus one word per kind
  everywhere, since the app said Esporte in one place and JOGO in another.

### And the small ones

The already-registered events in the operator sheet collapse behind a counted
row (Felipe: *"tá muito feio eles aparecendo tudo"*). The media button is
**"Foto ou vídeo atrás"** — it had still said "Tirar foto" while accepting
video — with *trocar* and *tirar* side by side instead of two steps to swap.

### What is not proven

The video export has never run on hardware (item 48). CI compiles it; no
device has executed it. It is built to fail loudly — `burn` returns null and
the photo and Instagram routes stay standing — so the worst case is the
behaviour b149 already had.

CI also answered a version question the environment could not: **media3
1.11.0 is compiled with Kotlin 2.2 and this project is on 2.0.21**, so
`kspDebugKotlin` refused to read its metadata. Pinned to **1.3.1**, which
predates the Kotlin 2.0 boundary entirely and already carries every API used
here. The constraint is written beside the version in `libs.versions.toml`
so nobody bumps it blind. Raising the project's Kotlin instead would drag the
Compose plugin, KSP and Room, and cannot be checked from here.

Backend 114 tests and ruff clean; frontend tsc, lint and 55 tests.

## 2026-09-22 — the two API keys: one is five minutes, the other is a Live Nation contract

Felipe asked whether he has to sign up for the football and setlist APIs by
hand. He does — neither can be provisioned programmatically. The useful part
of the answer is what the signing up runs into, and it is asymmetric enough
that it changes what #77 built. Detail in items **44**, **45** and **46**;
the short version:

**API-Football: yes, do it, take the direct route.** Five minutes at
`dashboard.api-football.com/register`, no credit card, free plan live on the
confirmation click. **Never RapidAPI** — same data, different host and
header, keys not interchangeable, and our code is wired for the direct pair.
Budget **USD 19/month** for the pilot whatever the free tier turns out to
allow, because catching the real whistle means polling and polling blows
100 requests/day on its own.

**Setlist.fm: no, and not for the reason anyone expected.** The
non-commercial restriction is real and is defined by *purpose* rather than
revenue, so being pre-revenue is not a defence; setlist.fm is a Live Nation
property, so the commercial licence is a contract nobody answers requests
about. But the clause that actually settles it is one neither I nor the
first research pass had looked for: **the API terms forbid a persistent
local datastore.** Short-lived caching, direct server calls, immediate
distribution. `attach_setlist`, merged hours earlier in #77, writes every
song title into `event_timeline` and keeps it — which is the forbidden
thing, on a free key and a paid one alike. **Paying would not fix it.**

So #77 shipped a setlist source pointed at a source we may not use. The
guess list itself is untouched and its premise got *stronger*: setlist.fm's
own OpenAPI spec defines `json_Song` with exactly five fields — cover, info,
name, tape, with — so "order only, never times" is now proven rather than
inferred. What has to change is where the order comes from: **the operator
types it**, which costs nothing, risks nothing and is already what
`setlist_guess.py` expects. MusicBrainz is the honourable second (setlist is
a core CC0 column, not a non-commercial annotation) and is probably too thin
for São Paulo.

**And a thing we built that may not have been necessary.** API-Football's
fixture object already carries `periods.first` and `periods.second` — UNIX
timestamps for each half's start — and `football_service.py` reads neither.
Whether they are the real whistles or the schedule restated is undetermined
and rests on one source, so they cannot replace the operator's two taps;
they can sit *between* the taps and the schedule guess, which is strictly
better than what ships today. There is also a documented live route:
`/fixtures?live=all`, watching `status.short` cross NS→1H and HT→2H (with
BT in between).

**The method note, because it earned its place.** Four research agents, each
re-checked by an adversarial verifier told to refute rather than agree. On
the alternatives pass alone: 18 of 33 claims confirmed, 11 uncertain, **4
refuted** — and one of the refutations was *backwards*, the first pass having
named API-Football's season limit as the pilot's main risk when the
restriction may run the other way. Without the second pass that would have
gone into this log as fact. Every vendor page is egress-blocked here, so all
of it is search-engine extraction of pages nobody could open: **item 46 says
plainly that none of this is legal clearance and a human has to read the two
terms pages.** The process lesson from earlier today repeats itself — the
first pass researched the price before the viability, and this one researched
the licence before the storage clause that mattered more.

## 2026-09-22 — four items in one batch: the match clock, the guess list, the web admin, video on the card

Felipe: *"pode executar todos os itens da lista acima."* Items 40, 42 (both
halves) and 43 in one PR (#77), backend first because it is the part that
can be tested here, Android last because it cannot.

**The match clock (item 42, football).** `parse_fixture_to_timeline` had
added the API's minute to the *scheduled* kick-off and stopped, so every
second-half event landed ~15 minutes early and a kick-off that slipped by
five moved the whole match. It is now two clocks, one per half, each anchored
on an operator tap — **APITO** and **2º TEMPO**, the two instants everyone in
a stadium knows exactly, once each per match, stored as `kickoff` and
`second_half` entries and read back by `anchors_from_timeline`. With no tap
the clock falls back to the schedule and an assumed 15-minute interval and
**says so** in every entry's metadata (`anchored: false`,
`assumed_half_time_min`, `uncertainty_sec`). The capture screen of a sports
event shows the two anchors first, in acid outline until tapped, and a tapped
one carries its own clock on the button; a second tap minutes later stores
nothing and the line says "é uma vez por jogo". 13 tests.

**The guess list (item 42, concerts).** Setlist.fm has order and never
times, so a setlist's timestamps are estimates that drift a minute a song.
The rule that makes this safe: **a measured time asserts, a derived time
offers.** `analyze_session` splits the timeline — entries with
`estimated: true` (setlist) or `anchored: false` (unanchored match) never
reach the correlator; instead `setlist_guess.candidate_labels` puts two or
three of them on every unnamed peak, ranked by distance from the region's
start, window widened only a minute before (a show never runs early) and the
whole drift after. The app shows them as chips under the moment — "TAVA
ROLANDO UMA DESSAS?" — and a tap names it the same way typing does. Room
v7 keeps them. 8 tests.

**The web admin (item 40).** It turned out the site already had
`/events/novo` and `/events/[id]/editar` — open to any signed-in account,
and the demo seed to nobody signed in at all, so the list every fan picks
from could be written to by anyone. Now: `ADMIN_EMAILS` on the server
(the waitlist's admins count, so Railway needs no new variable if Felipe's
is already there), `require_admin` on create/update event, the seed and
the timeline sources, `is_admin` on `/api/auth/me`, and two pages —
`/admin/eventos` and `/admin/eventos/{id}` with the timeline (exact and
estimated told apart, each entry removable), a fixture search on
API-Football and a setlist search on Setlist.fm that build the timeline
and can be re-run after the anchors land (rows are replaced by `source`,
nothing else touched), and a hand-added mark with a picked time. The fan's
`/events` lost "Novo evento" and the seed button. 5 tests.

**Video on the card (item 43).** The cheap 80%: `InstagramStory.kt`. When
Instagram is on the phone the picker takes video; the preview is the first
frame; the card renders as a **sticker** — no background, scrim kept — and
goes with a cache copy of the video to `com.instagram.share.ADD_TO_STORY`,
where the person sees the video playing under the card and can still move
it. Without Instagram the option is photo only and a line says why. Two
answers only a phone can give: how large Instagram draws a 1080×1920
sticker, and whether it refuses an intent with no Facebook App ID
(`res/values/instagram.xml`, empty until registered).

**What none of this touched:** the detector. It already finds moments with
no marks; everything here is about the name.

**Verification:** backend 114 tests and ruff clean; frontend `tsc`, lint
(pre-existing `<img>` warnings only) and 55 tests; Android compiles only in
CI, as always, and the anchors, the chips and the Instagram intent are
tested on the phone or not at all.

## 2026-09-22 — the audio-recognition path is retracted: a fingerprint matches a recording, not a song

Felipe asked what the paid API of path (a) costs. The price is trivial and the
answer is that **path (a) does not work**, which the document should have said
before recommending it.

`docs/naming-moments-plan.md`, merged in #75 hours earlier, called audio
recognition on one device per event *"a resposta definitiva para shows"* and
*"a única opção que nomeia um show inteiro com precisão sem ninguém tocar em
nada."* The architecture of that idea is still sound — one TumTum device
listening, publishing a timeline server-side for everyone at that event, no
fan's microphone, no new permission, no change to the store's privacy
declaration. **The recognition is what does not happen.**

An acoustic fingerprint identifies **one specific recording** — the studio
master. It is engineered to survive noise, compression and a bad microphone,
and explicitly *not* to survive a different tempo, a different key, a
stretched intro or a crowd singing over the top: *acoustic fingerprinting is
not robust against considerable musical changes, which is why it is not
applicable in live music use.* The same band playing the same song live is, to
the algorithm, a different recording, and there is nothing in the database to
match it against. The corollary doubles as the test: **when Shazam succeeds at
a concert, what it detected was playback** — a backing track, or the PA
playlist between songs. Identifying *which song this is* from an arbitrary
live performance is cover/version identification, an open research problem,
not an API integration.

What survives: **DJ sets**, where the recording is literally what is playing.
Heavy-backing-track pop recognises the tracks that run as bases and silently
misses the ones actually played, which is worse than nothing, because a wrong
name on a card is worse than no name.

**The price, for the record**, since it was the question: AudD is
US$0.005/request pay-as-you-go (300 free, down to ~US$0.002 at 500k volume,
US$45/month for continuous stream monitoring); ACRCloud sells yearly packages
at roughly ¥320 per 10,000 requests (~US$0.0045). Listening 10 s a minute over
a three-hour show is ~180 requests — **under US$1 per show**, a few hundred
dollars for a thousand shows. Cheap and useless for the case that matters.

The re-ranking: **football first** (half-time + two operator anchor taps — the
only exact timeline we can get, and `docs/pilot-event-options.md` already
picks a match as the pilot's technical test), **the guess list second** and now
as *the* concert answer rather than a placeholder, audio recognition third and
only for DJ sets.

The process lesson is the embarrassing part and goes in the log because of it:
**the cost was researched before the viability.** "How much does it cost" is
only a question after "does it work", and here it came first because the
answer looked obvious. The document is corrected rather than deleted — the
retraction is in §4(a) with the reasoning, so nobody re-derives it in three
months. House rule honoured: #75 is merged, so this correction is a new PR,
never an edit to that one.

## 2026-09-22 — the loop closed on a phone, and the two questions it raised

b145 in Felipe's hand, minutes after the merge. **teste7: 240 readings on the
Polar, uploaded, both sync steps drawn under the curve, `Momentos encontrados
pelo servidor`, and the peak at 22h35 named GOL by the mark tapped during the
capture.** Every one of the eight findings of 21/09 is now confirmed fixed on
a real phone, the Compose wheels roll, the event registered for 20h00 came
back in the list badged MARCADO, and **item 41 is answered**: the night does
reach the server, and teste3's silence on b140 was the invisibility bug rather
than a failed upload. This is the first time the whole chain — capture,
upload, detector, a moment with a name, a card — has been seen working by the
person it was built for, on one screen, in one sitting.

Two things came out of it, and neither is a defect.

**The photo option was invisible.** *"Esse escrito branco… tem que ficar mais
evidente."* On the black card it was white meta type between the card and a
pink CTA, which reads as a caption for the card rather than a control. It is
now a button in Toxic Yellow (a new `OutlineAcid` style: 18.97:1 on black, the
loudest thing the palette has on a dark surface) that does not take the filled
surface belonging to the primary action.

**And the big one: a moment's name cannot depend on anyone remembering to
tap.** Felipe is right, and the problem is both smaller and harder than it
reads. Smaller: the detector never needed the marks — `detect_peaks()` does
not look at the timeline, the parameter is literally marked unused, and the
peak at 22h35 was found before any name was attached; and the fan already
never sees a mark button, because that is an operator switch that ships off.
Harder: what the marks buy is the **name**, and the only source of names that
works today is a person present and paying attention. That serves a pilot of
five and nothing beyond it.

`docs/naming-moments-plan.md` is the analysis (open item 42). Its two findings
worth carrying here: **football is solvable** — the half-time bug is
arithmetic, and two operator taps (kick-off, second half) collapse a
fifteen-minute error to seconds, after which every goal names itself for
everyone at the match — and **a concert is not solvable from the setlist,
ever**, because Setlist.fm publishes order and never times. That is a property
of the source, not of our parser: even with real durations from Spotify,
banter and intros push a twenty-song set 20–30 minutes past the sum, so by the
third song the estimate is outside the correlator's window and by the tenth
the card would simply lie. The honest cheap answer is to **offer a guess
rather than make a claim** — "tava tocando uma dessas?" with the two or three
songs whose window covers that minute — which is the same
recognition-over-recall principle (§5.6) that already justified letting the
person name a moment. The real answer is **audio recognition on one device per
event**, publishing the timeline server-side: no fan's microphone, no new
permission, a paid API and a decision to make.

Also asked and answered without building: **video on the card is possible and
is the most expensive item on the board** (item 43). The picker is one word;
the render is a MediaCodec/OpenGL/MediaMuxer pipeline that ships with Android
and varies by chipset — unverifiable from here. Instagram's `ADD_TO_STORY`
takes a video background plus a transparent sticker, which buys most of it for
half a session, in the place the product is actually posted.

**What it settles:** item 41, and the shape of the naming problem.
**What is open:** items 42 and 43, and Bloco 8 (the reveal lock, before 10h),
still the only test from the 21/09 list never run.

---

## 2026-09-22 — b142 in a real hand: the wheels were blank, and the past night is cut

Felipe installed b142 within the hour. What he confirmed on the phone:
the OPERADOR door with its control beside the label and the hint under it
(item 1); the gallery tile drawing his photo behind the black card
(item 8); the three status rows on AO VIVO, the battery one included
(item 3); the operator's shortcuts lit by the new switch. Three of eight,
seen. Two things came back.

**The wheels were blank.** *"A janelinha para rodar dia, mês, ano aparece
em branco… tanto as datas quanto as horas."* The photo shows the dialog
open with the selection dividers drawn and not one digit. b142 inflated
Android's own `DatePicker`/`TimePicker` in spinner mode, betting on
"the platform's picker" being the safest thing to ship without an SDK.
On One UI it is the opposite: Samsung replaces those widgets, and the
plain `Theme.Material.Light` this app declares does not carry whatever
their version reads its text colour from. **Rebuilt in Compose**: a
`LazyColumn` five rows tall, the middle row framed, snapping to the row
under the frame when the scroll settles — the snap written by hand
(`animateScrollToItem` on `isScrollInProgress` turning false) instead of
`rememberSnapFlingBehavior`, whose experimental status on this BOM nobody
here can check. Day rolls 1..N for the chosen month, month rolls jan–dez,
year rolls this year and two ahead; hour 0–23, minute 0–59. No OEM in the
path, and it can look like TumTum. The two XML layouts went with it.
Lesson for the file: *"the platform's own widget" is not the safe choice
on a phone whose maker replaces the platform's widgets.*

**"Trazer uma noite que já passou" is cut — for everyone.** Felipe asked
whether the third operator shortcut also showed to fans, and did not wait
for the answer: *"na verdade, elimina geral. Nem para mim como
administrador, nem para ninguém. Essa sessão ela é irrelevante. Pode
excluir essa parte do aplicativo de uma vez por todas."* Gone: the
shortcut, the sheet's Past mode, the fan list's JÁ ROLARAM section and its
TRAZER badge, `EventTimes.past` and NOT_PAST, `createPastEvent`. The
fan's list is now only what has not ended, a live one first; an event that
ends leaves it. **What this costs, written down so it is not relearned:**
lote 5 (20/09) built that flow as the answer to §5.1 of the psychology
research — *the first value must not be weeks away* — by bringing a night
the watch already held. That answer is gone, and item 38's largest entry
("the first value is days or weeks from install") is open again. The
product side is Felipe's call and is made; the research question stands.

**One thing added, not asked for, and why.** With the Past mode gone
nothing on the phone rolled an *end* time, so the upcoming sheet now rolls
COMEÇO and FIM. Not tidiness: the end is the window a fan's AGORA badge
lives in and the window a capture is measured against, and with no end an
event is taken to last five hours (`ServerEvents.NIGHT_LENGTH`) — so a
two-hour match would read AGORA for three hours after the whistle, which
is the same false claim about the app's own state as everything else
fixed this week. An end at or before the start is the next day (22h→02h
is one roll); more than sixteen hours is a slip of the wheel.

**Still open from b142:** the two tests that matter were not reached
because the wheels blocked the first — whether the night's server state
shows under the curve and the GOL names its moment (item 41), and whether
a registered event comes back in the list as MARCADO. b143 carries the
wheels; those two are its test.

---

## 2026-09-21 — b140 in a real hand: eight findings, one session, and the night nobody saw go up

Felipe ran the test list on b140 the same evening the two product rules
were recorded — Teste 1 (Bloco 9), then Blocos 3, 4, 5 and 6 in one run
with the Polar, photographing each screen. Nothing crashed. Eight things
came back, and he asked for all of them to be built at once, after the
tests, not one by one. This entry is the eight, what each turned out to be,
and what was built. Everything here is verified by CI's build only: no
SDK in this environment, and none of it has been on a phone.

**What the test found, and what it actually was.**

1. *OPERADOR → MOSTRAR truncated with its own hint* — "Um fã não precisa
   MOSTRAR mexer aqui". The control shared a line with a two-line hint.
   The three rows of that section (the door, the two switches) now put the
   control beside the label and the hint under both.
2. *The capture screen did not fit* — the big number fell under MARCAR O
   MOMENTO, "bpm agora" was clipped, and the badges hid behind a scroll.
   The 18/09 fix had split the screen into a scrolling top and a fixed
   bottom, which only moved the overflow. This is the one screen used
   inside a show, in the dark; it now has no scroll at all — three weighted
   gaps, and a short phone gets smaller numbers instead of an overflow.
3. *"The battery gate did not appear. Nothing appeared."* — **not a bug**:
   the exemption had been granted on an earlier build and Android keeps it
   per app, so the gate is skipped, correctly and silently. The problem is
   the silence: a skipped step nobody announced reads as a step the app
   forgot. AO VIVO now has a third status row, next to the watch and the
   sensor, that says whether the exemption is in place.
4. *Felipe had to create "teste2" by hand to run the test at all* — the
   rule from the morning, met in practice. Built; item 5 below.
5. *A web admin for events* — Felipe's ask, evaluated, agreed, and left as
   open item 40: one page on top of endpoints and an admin area that exist.
6. *"Encerrar a noite" did nothing: no "Enviando", no "Procurando", no
   "Momentos encontrados pelo servidor"; and the GOL tapped at 18h20 did
   not name the moment at 18h20·35 s.* The two steps were drawn inside the
   peaks column, which fades in only after the curve's 1.2 s animation, and
   a 130-reading upload finishes before that; the result line then sat
   below the fold, under the moments. Felipe saw the phone's moments, a
   nameless peak, and read "the night never went up" — which may even be
   true. **Whether teste3 reached the server is still unknown** (item 41).
   What is built: the server's state is the first line under the curve —
   the steps while they run, held 1.8 s after they finish with both marked
   OK, then one sentence (the server's moments in white, the phone's and
   why in grey, a failure in rose with "Enviar de novo"). `NightSync` lights
   SENDING from the first byte, so a night in flight is never shown with no
   step on.
7. *A second GOL inside ten seconds got no answer.* The code had the answer
   — "GOL já marcado às 18h20. Um toque basta." — and two things hid it:
   the line changed only its words, and a `StateFlow` swallows an equal
   value, so a third tap changed nothing at all. Now every tap is a new
   value (`MarkFeedback.tick`), the button lights **acid** for a repeat and
   **rose** for a mark stored, the phone buzzes with a different pattern
   for each (CONFIRM/REJECT on API 30+), and the feedback is set *before*
   the row is written so a fast second tap finds the first instead of
   becoming a second mark.
8. *The gallery showed the skin, not the photo.* The photo lived only in
   the card screen's memory — "for this card, this share" (lote 4). The
   night now keeps the photo behind its last shared black card
   (`nights.photoPath`, Room v6, `CardPhotoStore` in filesDir at card
   size); the tile draws it under the card's own 60% scrim, and the card
   screen restores it so "Compartilhar de novo" shows what was sent. Gone
   with the account.

One more, found in the photos and not reported: *"Toca pra dizer o que tava
rolando" was there* (the 18:21 screenshot shows it) and Felipe read it as a
caption. It is acid now — the colour of a thing to touch on this screen.

**The events, as built (item 4).** The rule was *the fan never creates an
event; a time is never typed.* AO VIVO for a fan is now: the PRÓXIMO card
if one is marked, the three status rows, and **EVENTOS DA TUMTUM** — the
ones to come (a live one first, badge AGORA) and the ones already over —
each a row with date · hour, name, venue, and what a tap does: MARCAR
(becomes the PRÓXIMO, reminder an hour before, the notification permission
asked for), AGORA (starts the capture through the same battery gate), TRAZER
(the watch is asked over the event's own window, then the usual chooser).
The empty state is a claim: "Nenhum evento da TumTum por enquanto" and "Não
consegui buscar … Tentar de novo" are different sentences. `ServerEvent`
reads `start_time`/`end_time` at last — the digits on the phone's clock,
the column's offset ignored as the backend schema says, an end before the
start is the next day, and an event with no time is not offered (nothing
to count down to, no window to ask a watch over). An event with a start and
no end is taken to last five hours: a query window, never a reading, and
the operator's sheet always sends an end.

The typed sheet did not disappear: it is how TumTum registers from the
phone until item 40 exists. It is the operator's, reached by three
shortcuts at the foot of AO VIVO that a new switch — *Cadastrar eventos
pelo celular*, behind Configurações → OPERADOR — turns on; it left
Settings. Its DATA, HORA/COMEÇO and FIM are **the Android wheels**
(`android:datePickerMode="spinner"`, `timePickerMode="spinner"`, inflated
from two layouts into a dialog — no dependency, and the literal thing
Felipe described: *"aquele campo onde a pessoa rola as horas"*; Material
3's clock dial is a tap on a clock face, not a wheel). What it registers
goes **to the server first**, with start and end, so every fan's list has
it, then to this phone; a failure is said on the tab and the event still
works locally. `EventTimes` lost its parsing and kept its meaning — a next
event in the past, a past night not yet over, midnight, sixteen hours —
with seven tests; `ServerEventsTest` grew five for the times and the fan's
split. `NightSync`'s twin created at upload now carries the night's times
too, so nights from before today show up timed.

**What it settles:** items 39 and (the app side of) 4; the shape of AO
VIVO for a fan — a list to choose from, never a form to fill. **What it
costs:** one session; a Room migration (v6); one more operator switch to
turn on once. **What is open:** item 40 (the web admin), item 41 (did
teste3 go up), Bloco 8 of the test list, and the next build in Felipe's
hands — every one of the eight is a claim until then.

---

## 2026-09-21 — two product rules from the first test of b140: events are TumTum's, and a time is never typed

Felipe installed b140 (lotes 1–5, the first time any of them met a phone)
and ran Blocos 1 and 2 of the test list — the event sheet in its three
modes, and the reminder an hour before. What came back was not a defect
but two rules, both stated as standing:

- **The fan never creates an event.** *"Na experiência final do usuário, o
  usuário nunca vai ter a possibilidade de adicionar ele mesmo um evento."*
  The events where TumTum can be used are **pre-registered by TumTum** and
  appear on the fan's screen for the fan to **activate or not**. Nothing
  else. A name field, a venue field, a kind chooser, a date and an hour are
  all things a fan should never see.
- **A time is never typed.** *"Sempre que tiver campo de hora, a gente tem
  que aparecer aquele campo onde a pessoa rola as horas para colocar. Nunca
  digitar."* Every time field in the product is the platform's picker; a
  free-text `HH:mm` is forbidden, including behind the operator door.

**Why the first rule is bigger than it looks.** Lote 3 built "Marcar o
próximo" as a form (§5.7 of the psychology research said *the calendar is
the trigger*) and lote 5 built "Trazer uma noite que já passou" as another
form (§5.1, the first value in minutes). Both were designed around a
person typing what the server should already know. The server *does*
know: `GET /api/events` returns `date`, `start_time` and `end_time` for
every event (`EventResponse` in `backend/app/schemas/event.py`), and the
app's `ServerEvent` reads only `date` and throws the times away — which is
exactly why the sheet then asks the person for them. Under the rule, both
flows collapse into one gesture: **a list of TumTum's events, one tap to
activate.** The countdown, the reminder an hour before, and the Health
Connect window for a past night all come from the event the server sent.
The fan types nothing. The empty state is a claim like any other: "no
TumTum event near you yet" must be told apart from "could not ask".

**What moves, and where.** The typed sheet does not disappear — it is how
TumTum registers an event from the phone in the first place — but it
becomes an **operator surface** behind Configurações → OPERADOR, next to
the marks and the reveal lock, and its date and time fields become
pickers (Material 3's `DatePicker` and `TimePicker` are in the BOM this
build already uses, `2024.12.01`; no new dependency). `EventTimes` and its
eight tests stay, now guarding the operator's input only —
`event_bad_time` ("Formato: 20/09/2026 e 21:30") is the one string that
stops making sense under a picker and has to change or go. The three modes
of the sheet (`Now`, `Upcoming`, `Past`) stay as the operator's three
questions.

**What it settles:** open item 39, and the shape of the AO VIVO tab for a
fan — a list to choose from, never a form to fill. **What it costs:** one
session, and the typed-date validations of Bloco 7 (dd/MM, midnight,
16 h) are no longer worth Felipe's time on b140; the rest of the list
(Blocos 3–6, 8, 9) stands.

---

## 2026-09-20 — lotes 3, 4 and 5: reminders, the next event, the person names the moment, a photo, and a night from the watch's history

Felipe could only test the next day and asked for everything at once:
*"Você consegue fazer todas as alterações até lá?"* Three commits on one
branch, one PR, verified by CI's build only — no SDK here, and none of it has
been on a phone. Two assumptions made without waiting: the reveal lock stays
an operator setting (off by default), and the notification it promises is
built for when it is on.

**Lote 3 — the two reminders the product allows itself (§5.4, §5.7).**
AlarmManager and a `ReminderReceiver`, no new dependency and no exact-alarm
permission: "Sua noite abriu" when a locked night unlocks, "Hoje: <evento>.
Bota o relógio." an hour before the event the person marked. Alarms die on
reboot and on update, so they are set again from what the phone knows in
`BootReceiver` and `TumTumApp.onCreate`. Tapping one opens the night or the
AO VIVO tab (`MainActivity` extras → `TumTumRoot`). **The next event now
lives in prefs**, one at a time: a card on AO VIVO with a countdown (EM 3
DIAS · AMANHÃ · EM 2H), *Começar agora* (the operator's own path: battery
gate, capture service), *Desmarcar*, and a line that says whether Android
will let the reminder arrive — with the permission asked for when it will
not. This is the first time a fan, not the operator, starts a capture from
the Live tab; the operator's *Marcar evento — operador* is unchanged.

**Lote 4 — the person names the moment (§5.6), and a photo (§5.11).** On the
reveal, a moment without a cause reads *"Toca pra dizer o que tava rolando"*;
the tap opens one field. The label is saved on the phone at once and offered
to the event's timeline as a `highlight` mark, so the server names the
moment by it on the next analysis — and a typed label outlives the server's
answer when the server has none within a minute of it. The sync's SENDING
step now shows only when readings actually go up. On the card, the black
skin (*A NOITE*) accepts a photo from the picker behind a 60% scrim: white
text and the pink number stay legible, the photo lives for that share only.
Other skins do not take a photo; black on a photo is not a pair the manual
allows.

**Lote 5 — "Trazer uma noite que já passou" (§5.1).** On AO VIVO, when a
watch is connected: name, date, start and end; the event is created already
closed (so the active-event flow never sees it); Health Connect is asked
over exactly that window; the usual source chooser and reveal follow, with
the phone's top-N moments until the server answers. `EventTimes` holds the
typed-date rules as a pure object with eight tests: *dd/MM/yyyy*, *HH:mm* or
*HHhMM*, an end before the start crossed midnight, a night still running is
not the past, more than 16 hours is a typo. The demo night for a phone with
no watch history was not built.

**What a phone has to answer tomorrow, in order of risk:** the sheet's
three modes render and validate; a marked event survives a restart and its
reminder fires (the reminder is inexact — minutes, not seconds); *Começar
agora* with a paired sensor goes through the battery gate; a past night
comes back from Health Connect with the right window; naming a moment shows
at once and survives the re-analysis; the photo card renders and shares.

---

## 2026-09-20 — lote 2: the work is shown, and the loop ends on a TumTum screen

The second batch from the psychology audit (`docs/app-psychology-principles.md`
§5.2 and §5.3), one session as estimated, verified by CI's build only.

- **The analysis shows its two real steps.** `NightSync` now exposes a phase
  per in-flight night — SENDING while the readings, the event and the marks
  go up, ANALYSING while the server looks for the moments and names them —
  and the reveal draws them as two rows with the active one lit: *"Enviando
  21.960 batidas"* then *"Procurando seus momentos e o que tocava em cada
  um"*, the finished one marked OK. Buell & Norton's finding is that shown
  work raises the value of the result; the rule here is stricter than
  theirs: **never a step that did not run, never a padded second.** The
  server does detection and matching in one call, so they are one step on
  screen, not two. The seven failure states are untouched.
- **After the share sheet, a TumTum screen.** The chooser is launched for a
  result; when it comes back the card screen says *"Ficou na sua galeria."*
  with *"3 noites em 2026 · 41 momentos"*, a pink *Ver a galeria* and an
  outline *Compartilhar de novo*. The peak–end rule: the last thing seen is
  ours, not Android's. The copy claims only what is true — the card exists
  and the night carries its skin. **Whether it was sent, nobody here knows**,
  and the screen does not pretend to; a cancelled sheet lands on the same
  screen, which is still true.

Not touched: capture, upload, detection. Cost: one session.

---

## 2026-09-19 — lote 1: the five cheapest changes from the psychology audit

Felipe asked how long the audit's backlog would take; the answer was ~6–7
sessions of code gated by phone loops, and he said start with lote 1 — the
copy-and-layout changes that touch nothing near the capture pipeline. Built
in one pass, verified by CI's APK build only (no SDK in this environment):
**Felipe's ten minutes on the phone are the real check.**

- **"Postar no feed" is gone** (item 37). It wrote into the on-phone fake
  repository and confirmed "A galera já pode sentir também", which nobody
  could. Side effect handled: that button was also what saved the chosen
  skin onto the night (the gallery cover), so `publish(nightId, skin)` now
  runs when the card is shared. Compartilhar became the pink primary button.
- **The locked night no longer promises a notification** (item 36, copy
  half): "A curva abre aqui às 10h. Vale a espera." The decision about the
  lock — fan feature with a real notification, or protocol only — stays
  open; the sentence just stops claiming code that does not exist.
- **Configurações has an OPERADOR door.** The operator marks, the reveal
  lock, the participant code and "Marcar evento — operador" sit behind one
  collapsed row (MOSTRAR / OCULTAR), closed on every visit. The fan's own
  settings — reading permission, profile, sensor, account, delete — stay
  where they were. Nothing essential moved behind the door.
- **A season, not a streak:** the gallery counts "NOITES EM 2026" (nights of
  the current year; older ones stay in the grid, just not in the number),
  and "SEU RECORDE" / "RECORDE" became "SEU MAIS ALTO" / "MAIS ALTO" — a
  higher bpm is not a score, and the brief's line about bodies applies.
- **One failure now says the next step:** "Não achamos batida nessa
  janela. Na próxima, confere se o relógio estava gravando antes de
  começar." The bigger half of §5.8 — keeping a night with no data as a
  night in the list — was not done; today that path clears the event and
  returns to the feed, and changing it is a repository change, not copy.

Cost: one session, as estimated. Not done from lote 1 as pitched: nothing
else. What this does **not** touch: BLE, the capture service, upload,
detection, the reveal's data path.

---

## 2026-09-19 — what makes an app feel great: the catalogue, the benchmark, and where TumTum stands

Felipe sent Wyatt Feaster's *"The psychology trick that makes any app feel
10x better"* (goal gradient, labor illusion, choice overload) and asked for
the complete search: every principle that makes apps feel great, and the top
apps as the benchmark. The result is **`docs/app-psychology-principles.md`**
— forty-odd principles with their origin papers and numbers, thirty apps
with what they do on screen and what they published, TumTum's own loop
audited screen by screen against both, and a ranked list of what to borrow,
what to refuse, and what a five-person pilot can actually measure.

What it settles, in one paragraph each:

- **The three from the video already have places in the product.** Choice
  overload is handled right where the fan is (four skins with previews,
  three moments of twenty, one CTA) and wrong only in Configurações. The
  labor illusion exists in its honest form — the curve drawing in 1.2 s is
  the data arriving — and is missing where it would count most: the
  server's three real steps after Encerrar a noite are a grey caption. The
  goal gradient has no bar to act on, because the fan's only progress bar —
  install to first reveal — is invisible and weeks long.
- **The biggest gap is time-to-value, and it is structural.** TikTok plays a
  video before asking anything; Duolingo runs a lesson before the account;
  Shazam is one button. TumTum's first value needs a ticket, a watch and an
  evening. The honest fix is a retroactive night from the watch's history
  (the setup screen already reads 24 h from Health Connect) or a demo night
  labelled as one — §5.1.
- **The card's mechanism is the Wordle grid and the Wrapped card:** a
  silhouette recognisable at thumbnail size before the number is read.
  Mutante Pop's "fixed silhouette, mutating skin" is exactly that; the
  surface is still the base one (item 24, second half). Wrapped 2025 was
  shared 500M+ times; Strava's activities with photos get 3.1× the kudos;
  the 2025 Wrapped pivot to comparing with friends is where card 05 sits.
- **What the brand refuses is now a table** (§6): streaks, bpm leaderboards,
  scores, guilt notifications, manufactured scarcity, confetti for a number,
  invented social proof, pre-ticked consent, loot-box mechanics — each with
  the principle it would exploit and the reason. Snapchat's streaks are
  being sued in six US states; Robinhood's confetti is banned by settlement;
  Brazil's ECA Digital (in force 17/03/2026) bans loot boxes and LGPD Art. 11
  makes heart rate sensitive data.
- **The reference magnitudes exist now.** Bielefeld's 229-fan study
  (*Scientific Reports* 2026): 94 bpm mean in the stadium vs 79 on TV, a
  stadium mean peak of 108 after the first goal, +36% over TV. And the
  organic precedent for the card: the 2018 Minneapolis Miracle, when Apple
  Watches told fans their heart was above 120 while "inactive" and the
  screenshots went viral. Fans already post this when a device hands it to
  them.
- **Two more of the bug class,** found by reading the strings against the
  code: the locked night promises a notification nothing sends, and
  "Postar no feed" confirms a post nobody can see. Items 36 and 37.

What it cost to learn: the video itself could not be watched from this
environment (youtube.com and every transcript mirror are blocked, and no
write-up of it is indexed), and most primary sites were blocked too, so the
two research passes ran on search excerpts and secondary coverage, cross-
checked and flagged where thin. Every number in the document should be read
from its primary before it goes into public copy. Nothing was decided; the
document is Felipe's to pick from, and item 38 says so.

---

## 2026-09-18 — Etapa 4: the first release is on the Play internal testing track

At 18:39 the Play Console showed `cc.tumtum.app` with one release on the
internal testing track, *"Disponível para testadores internos"*, built from
`app-b131` (target API 36). The whole afternoon's path, in order: developer
account verified → app created → login details for the reviewer (an account
created at tumtum.cc/signup, because the phone could not create a second
one) → content rating (*Todos os outros tipos*, user content shared, no
block/report yet) → target audience 18+ → data safety (name, e-mail, user
id, health info; deletion in-app, URL `/apagar-conta`) → health declaration
(*Atividade e condicionamento físicos*, the only category that covers a
heart-rate read) → store category *Entretenimento* → listing text in the
brand's voice → icon and feature graphic rendered from the official vector →
four 1080×1920 screenshots drawn from the app's own code and the real
exported session → `.aab` upload, refused at API 35, accepted at 36.

Two things the day settled for the future:

- **The listing shows only what the app really does.** Feed and crowd
  screens were asked for and refused: they run on the fake repository, and a
  screenshot of invented people is the same lie as the number the app used
  to show. They return after the pilot produces real data.
- **Tablet, Chromebook and XR stay empty.** The app is a phone in a pocket
  at a show; declaring large-screen support would invite reviews on a layout
  the app does not have.

**What is left:** the testers list and its opt-in link (Felipe's next
click); the temporary store name *"cc.tumtum.app (unreviewed)"* stays until
every dashboard task is done and the app goes through review; R8 on and
tested on a phone; `cc.tumtum.app` in the developer-verification portal for
30/09.

---

## 2026-09-18 — the Play Console refused the bundle: API 36 or nothing

The first `.aab` upload was accepted, read correctly (version 129, minSdk 28)
and then rejected at review with one error: *"o nível desejado da API do app
é 35. No entanto, esse nível precisa ser de pelo menos 36"*. Google's target
requirement moved to Android 16.

`compileSdk` was already 36, so only `targetSdk` had to move, and the
behaviour changes Android 16 brings cost nothing here:

- the large-screen resizability rule is a no-op, because the manifest locks
  no orientation;
- edge-to-edge is already on (`enableEdgeToEdge`) and every screen pads for
  the system bars;
- the capture service already declares `foregroundServiceType="connectedDevice"`
  with the matching permission, which is the API 34+ shape.

Three warnings came with it and none blocks: no testers assigned yet (the
next step), no deobfuscation file (R8 is off until it is tested on a phone,
item on Etapa 4), and no native debug symbols (there is no native code of
ours). Install size 8.96 MB.

---

## 2026-09-18 — a deletion URL for Play, and a promise the privacy page could not keep

Two findings from the Play Console data-safety form, plus the merge rule
broken a sixth time.

- **The form wants a public account-deletion URL** and spells out what the
  page must do: name the app or the developer, spell out the steps, and say
  which data is deleted, which is kept, and for how long. `/privacidade`
  covers deletion in two sentences and says nothing about retention. So
  **`tumtum.cc/apagar-conta`** (and `/en/delete-account`), rendered by the
  same quiet component, answering the three bullets in order and closing
  with the sentence the form is really after: nothing kept for an extra
  period, no backup window, no suspended account. True only because of
  `DELETE /api/users/me` (item 32, the same day).
- **The privacy page promised a per-session delete the app never had.** The
  form's next question asks whether *some* data can be deleted without
  deleting the account. Checking before answering: no button on the reveal,
  no repository method, no endpoint. The answer is no — but `/privacidade`
  had said since 17/09, in both languages, "também dá para apagar uma
  sessão específica … sem apagar a conta". Never true. The defect class
  this project keeps finding, this time in the document the Play listing
  points at. Corrected to what is true, and recorded as open item 35.
- **The merge rule, broken a sixth time.** PR #63 was opened after the
  first commit and the deletion page was pushed to it afterwards; Felipe
  merged at the earlier commit, so the page was never in `main` while its
  URL was already pasted into the Play Console. The rule has been in
  CLAUDE.md since the fifth time and the fix is the same each time:
  **do not open the PR until the work is finished and pushed.** What is new
  is the cost — not a lost commit, but a live form pointing at a 404.

---

## 2026-09-18 — a phone could not have a second account

The Play Console asks for a reviewer login, so Felipe signed out to create
one, and could not: the login screen had only the login form, and
Configurações after a sign-out offered only *Entrar*. The local profile
(name, @, photo) stayed on screen, which read as "still logged in". Two
findings, one product decision.

- **Sign-out keeps the local profile and the nights.** That was the
  design (a token dies, a night does not), but the copy said *"Esta
  conta ainda não entrou no servidor"*, which is false after a sign-out.
  It now says there is no server session on this phone and offers both
  doors: *Entrar* and *Criar outra conta neste aparelho*. The login
  screen has *Não tem conta? Criar uma*.
- **A second account replaces the first on the phone, and the nights go
  with the first.** Nights have no owner column, so a new account on a
  phone that already holds nights would show it someone else's. Creating
  a second account therefore wipes the phone's nights, and the create
  screen says so before the tap, naming the @ it replaces. The old
  account's uploaded nights stay on the server; they do not come back to
  the phone, because nothing downloads a night yet.
- **For the Play form today:** the reviewer account can be created at
  tumtum.cc/signup, which registers against the same server, without
  touching the phone at all.

---

## 2026-09-18 — Configurações was reachable from nowhere, and the way back was invisible

From Felipe's check of `app-b123`. The three fixes of the previous entry
held (the night shows, the avatar opens the profile, the wordmark goes to
the feed); two more surfaced.

- **Configurações had no door.** The *Você* tab restores whatever screen
  was on top of it, and after one visit to the gallery that screen is the
  gallery — which had no back arrow and no link to settings. The only
  entrances were on the nights list underneath, so *Apagar minha conta*,
  the operator toggle and the sensor pairing were unreachable in normal
  use. Now the gallery header carries the same two badges as the nights
  list (SUAS NOITES, CONFIGURAÇÕES), and the person's own profile has a
  *Configurações* button beside *Editar perfil*.
- **The back arrow was a 14 sp grey glyph with no touch padding**, drawn
  ten different ways in ten screens. One `BackArrow` component now: 24 sp,
  semibold, in the surface's text colour, inside a 44 dp target. Same
  arrow on every screen that has one.

---

## 2026-09-18 — the gallery hid a night, and two things on the header did nothing

Three more from Felipe's hands, on the *Você* tab after the fourth rehearsal.

- **Ensaio 3 was not in the gallery.** The gallery listed only nights with
  a card chosen (published), so a night just captured, analysed and named
  was nowhere until the person shared it — while the header counted
  "2 noites" against a phone holding three. The gallery now shows every
  night; one without a card is a white cover with *SEM CARD AINDA*, and
  the skin arrives when the card is chosen. The public profile still shows
  published nights only. Empty-state copy no longer talks about
  publishing.
- **The avatar did nothing on two of the four tabs.** Feed and Você opened
  the profile; Galeria and Ao vivo did not. All four do now.
- **The wordmark did nothing anywhere.** It now goes to the feed from every
  tab, which is what a logo in a header is for.

Same class as the other fifteen: controls with no answer, a list making
a claim the data contradicts.

---

## 2026-09-18 — item 32: the privacy page's promise is now code

`tumtum.cc/privacidade` has promised since 17/09 that deleting the account
deletes the account, the readings, the moments and the cards. There was no
endpoint; the "Apagar minha conta" button in the app wiped the phone and
left the server untouched, which is the exact shape of defect this log
keeps counting — the app announcing a state that is not true.

- **`DELETE /api/users/me`.** One service, `account_deletion.py`, deletes
  in foreign-key order: shares, cards, readings, peaks, sessions, wearable
  connections, password-reset tokens, then the user. Three of those keys
  carry no `ON DELETE`, so a bulk delete in the wrong order is refused by
  Postgres rather than cascaded; the order is pinned by a test. Events and
  their timelines are shared and stay.
- **The app asks the server first and the phone second.** If the server
  did not delete — offline, expired token, an error — the dialog says so
  and nothing local is touched. Only a confirmed deletion (or a 404, the
  account already gone) wipes the phone and returns to onboarding.
- **The privacy page now names the in-app path first**, PT and EN, and
  keeps the e-mail path as the alternative with its 7-day promise.

**What it settles:** the Play data-safety form's deletion question, and a
promise that until today depended on Felipe deleting rows by hand.

---

## 2026-09-18 — Etapa 3's gate passed: a moment named GOL, on the phone

After #58 went live Felipe reopened the Ensaio 3 night and tapped *Enviar
de novo*. The mark went up, the session went up, the server's detector ran
against the event's timeline, and the reveal came back with one moment:
**169 bpm · GOL · às 14h51 · 129 s**, labelled *Momentos encontrados pelo
servidor*, with the share button fixed at the bottom of the screen.

That is the gate `docs/one-app-plan.md` set for Etapa 3, and it closes the
loop the pilot needs: a tap in the dark on the operator's phone names the
moment on everyone's night attached to that event. Etapas 0 to 3 are done
and proved on a phone, all on one day, across five pull requests (#54 to
#58) and four rehearsals.

**What remains before a pilot**, in order: Etapa 4 (Play internal track,
Felipe's Block 3 with the verified account), the R8-minified build tested
on a phone, `cc.tumtum.app` registered in the developer verification for
30/09, and item 32 (account deletion from the app). Nothing in the capture,
upload, detection or card path is waiting on code.

---

## 2026-09-18 — fourth rehearsal: the first GOL ever marked found a 500 that had been waiting since Etapa 3

Felipe ran the rehearsal on `app-b117` with the operator toggle on, marked
a GOL, and the night came back *Não subiu: o servidor respondeu server:500*.
Four findings, all fixed.

- **The 500 was the timeline endpoint, and it had never worked.** The
  response schema for a timeline entry has a field called `metadata`,
  read from the ORM row. On a SQLAlchemy model `metadata` is the table
  registry, not the JSON column (which is why the column is named
  `metadata_`), so validation got a `MetaData` object, failed, and the
  server answered 500. Nobody had posted a timeline entry before today:
  Ensaio 2 had no marks, the Realness night had no marks, and the
  endpoint had no API test. The sync pushes marks **before** the session,
  so one GOL was enough to stop the whole upload. Fixed with a
  `validation_alias` on the schema and a test that validates a real
  `EventTimeline` row. `GET /api/events/{id}` with a timeline would have
  failed the same way. **What it settles:** the cause of every 500 on a
  marked night; the GOL label on a moment is still unverified, one
  rehearsal away.
- **The live number is on the screen.** Third time Felipe asked; the §10
  argument (seeing the number changes the number) is recorded and
  overruled by the founder for the pilot. The capture shows the strap's
  current bpm in Rose, always. Reverting is the block that this entry's
  commit removed.
- **"Escolher como compartilhar" was below the fold.** The reveal is now a
  scrolling block over a fixed pink button, the same shape as the capture
  screen after its own fix this morning.
- **"Sua noite × a galera" is hidden.** The screen behind it renders the
  mockup's 3,412 people and a 71% sync from a fake repository; Felipe
  asked whether it was invented, and it is. Card 04 needs a real
  collective sample; until then a fan must not be able to reach a number
  the app made up.

**Cost:** one rehearsal, and the lesson that an endpoint with no test and
no caller is an endpoint that does not work. Item 28's "timeline dead
code" was deader than the log said.

---

## 2026-09-18 — third rehearsal: the pipeline is right, the screens around it were not

Felipe ran the rehearsal a third time on `app-b115`: one minute still, two
minutes of effort, three minutes of rest, with the Polar. The exported
session is the first clean end-to-end proof of the designed app: 363
readings at 1 Hz with no gap, 703 R-R intervals, contact 100%, peak 161 bpm
at 14h11m17, and the detector reported **one** moment, 93 s long — the
stretch above 150 — where the old mean-based detector would have reported
spikes or nothing. The card was generated and the share sheet opened with
the image attached. Six observations came back, all about what surrounds
the pipeline, and all were fixed in one pass:

1. **"Começa agora" was not pink.** The button faded to 40% while the name
   was blank, and a faded pink on white reads as nothing. It is always
   pink now; a tap with no name says *Dá um nome pro evento primeiro*.
2. **The three dots instead of a live number.** Felipe read it as a bug. It
   is the §10 rule — seeing your own number changes the number — and it
   stays, but the number now appears on a **single tap** for eight
   seconds, not only on a long press, and the line says *Toca para ver*.
   Flipping to always-on is one line if the pilot argues for it.
3. **The 30-minute margin leaked into every surface.** The reveal said
   *30 MIN SEM DADO* starting at 13h38, the sources screen said *10% da
   noite coberta*, and `session.json` carried both. The margin exists for
   the watch path, whose recording may start before the event is marked.
   **With the strap, the night is the capture:** when the live sensor has
   data the window is the event's own start and end, no margin. The
   watch-only window keeps it.
4. **"Trazer do meu relógio" asked for a choice that did not exist.** The
   source screen is for two or more sources. Now: exactly one source with
   data saves the night and goes straight to the reveal; two or more show
   the screen; none shows an honest *Não gravamos nenhuma batida* with a
   way back, instead of a title promising sources found. This is also
   where the morning's *"Não achamos batida nessa janela"* came from. The
   end-of-night save lives in one place (`saveEndedNight`) for both paths.
5. **The operator toggle was invisible.** Felipe ran the whole rehearsal
   without the GOL · MÚSICA · MOMENTO buttons because the switch shipped
   off and was buried. It is now the first row of Configurações →
   Experimento, and the section says who it is for.
6. **The participant field took the event's name.** `session.json` carried
   `participantId: "Ensaio 2"`. Relabelled *Código no experimento*, with
   *Não é o nome do evento* in the hint.

**What the rehearsal settles:** capture, upload, detection, naming
infrastructure, card and share all work on the designed app with a real
strap. What it does not settle is the GOL label on a moment — the buttons
were hidden, so no mark was made. That is the next rehearsal's one check.

---

## 2026-09-18 — second rehearsal: a night with no way out, and 31 goals from one thumb

Felipe ran a ten-minute capture with the Polar on `app-b111` (641
samples, sensor 100%, the pipeline itself fine) and could not end it. Two
defects, one product decision.

- **"Encerrar a noite" was off the screen.** The button exists; Etapa 3's
  row of marks, added above it in a column that does not scroll, pushed it
  below the navigation bar on his phone. The system back button looked dead
  too, by design: the AO VIVO tab sends an active night straight back to the
  capture. So the app showed a running night with no exit, for ten minutes,
  through a kill and a relaunch. Defect fourteen of the class — the app
  hiding its own state — and the first that trapped someone. Fixed by
  layout, not by copy: the screen is now two blocks, the top one scrolls and
  the bottom one (marks, hint, Encerrar) is fixed and always visible,
  whatever the phone's height.
- **The taps gave nothing back.** GOL · MÚSICA · MOMENTO changed only a grey
  counter at the corner, so he pressed until something visibly happened: 31
  marks in a ten-minute rehearsal. Now the tapped button lights Rose for a
  second, a line under the row says *GOL marcado às 13h37* with **Desfazer**
  beside it (undo removes the mark while it is still local; once the server
  has it, it stays and the line keeps saying so), and a second tap of the
  same kind within ten seconds stores nothing and says *já marcado às 13h37,
  um toque basta*.
- **Why would a fan do this at all? They will not.** The question was the
  right one. The marks exist because the timeline integrations are dead
  code (item 28) and a match needs its goals timestamped by somebody; that
  somebody is **the person running the test, once, for everyone** — the
  marks go to the event's shared timeline and name the moments of every
  night attached to it. So the row is now behind **Configurações →
  Experimento → "Marcar momentos na captura"**, off by default. A fan's
  capture screen has the stopwatch, the dots and Encerrar, nothing to
  operate. Felipe turns the toggle on his own phone.

**Cost:** a ten-minute rehearsal, and 31 `goal` entries on the server's
timeline of the rehearsal event, which is a rehearsal event and can stay.

---

## 2026-09-18 — first rehearsal on `app-b111`: the permission sheet told nobody what to press

Felipe ran the Block 1 rehearsal and sent four screenshots. Two are
defects, both of the class this project keeps finding — the app being
unclear or false about its own state — and neither showed in a test.

- **The battery sheet did not say what to do.** It opened with "Seu Android
  quer matar a captura", two black-and-white buttons, and no word about
  which one to press or what the Android dialog behind it would ask. On a
  permission screen the brand goes quiet and careful; this one was loud and
  vague. Rewritten: the title is *Uma permissão antes de começar*, the body
  names the permission (run without battery optimisation, nothing else), and
  three numbered steps say exactly what happens — **tap the pink button,
  Tirar da otimização; Android asks "Parar a otimização do uso da bateria?",
  tap OK; you come back and the capture starts by itself.** The button that
  must be pressed is now **Rose**, the only pink thing on the sheet, and the
  step names it. Two behaviours changed with the copy: coming back from the
  Android dialog the sheet **checks by itself** and moves on when the
  exemption is there, so the third step is true; and the verify button,
  which used to do nothing when the exemption was missing, now says so
  (*Ainda não está liberado*). A control with no feedback was defect
  thirteen of that class.
- **The event picker showed the word `null` as a venue.** "Realness Festival
  2026 · null". Android's `org.json` renders a JSON null as the text "null"
  in `optString`; the reference `org.json` the unit tests run on returns the
  fallback, so the test asserting a null venue passed and the phone
  disagreed. Fixed by checking `isNull` before reading; the test now covers
  missing, null and blank, with the divergence written down in the code so
  the next `optString` does not repeat it.
- **Open question:** the first screenshot shows *"Não achamos batida nessa
  janela"* on the watch-sources screen. That line is the honest empty state
  for a Health Connect read with no samples; which step produced it is not
  known from the picture and is asked below rather than guessed.

**Cost:** one rehearsal to find both; nothing in a test could have. Both
are on the branch that follows #54.

---

## 2026-09-18 — Etapa 3 built: the event has a name

The last piece the pilot needs from the app before Play: a night attached
to an event the server knows, and a way to say *when the goal was* without
paper.

- **The event comes from the server's list, or goes to it.** The sheet that
  marks an event (Configurações → Experimento, the operator's act) now asks
  `GET /api/events` once when it opens and offers the events nearest to
  today — tonight first, whatever the server's order — so picking one
  attaches the night to **the timeline the pilot shares**: the thing that
  names a goal for everyone in the stadium, instead of five private events
  with the same name. Typing still works; the **kind** (show · jogo ·
  festival) is chosen with it, because the server's schema demands one. A
  typed event is created on the server at the first sync
  (`POST /api/events`), not at the tap — the sheet works offline. "Could not
  ask" is told apart from "nothing there".
- **Marking a moment is three taps in the dark.** GOL · MÚSICA · MOMENTO on
  the capture screen, each storing a mark with the clock of the tap in a
  new `marks` table; the only feedback is the count, and the count is the
  truth (what is stored). On every sync — before the analysis, every time —
  the unsynced marks become timeline entries
  (`POST /api/events/{id}/timeline`, `goal` · `song_start` · `highlight`),
  the session goes up with `event_id`, and the detector's moments come back
  **named by the causal rule of 17/09**. A mark tapped late still lands
  before the next analysis.
- Room 4 → 5: events carry their server twin and kind; marks get a table.
  `ServerEvents` is pure and tested (fields, tonight-first ordering, the
  cap). One build failed on two missing imports, fixed in the next commit —
  the cost of writing Compose without a compiler in reach.

**What this closes:** open item 28's practical half. The timeline
integrations are still dead code, but a match no longer needs them: the
kick-off, the goals and the whistle are taps, with wall-clock instants, on
the same timeline for everyone who picked the event.

**Gate:** CI green (run 10). Felipe's half, the one the plan names: a
rehearsal with one GOL marked comes back on the reveal as a moment whose
line reads *GOL*.

**Next: Etapa 4** — Play: the `.aab` already builds on every push; what is
left is the account's verification, "Create app" as `cc.tumtum.app`, the
internal testing track, and a minified build tested on a phone before R8
goes back on.

---

## 2026-09-18 — Etapa 2 built: the night goes up, and the server's moments come back

The stage that turns the designed app from a local diary into the product:
the readings reach the server, the detector rebuilt on 17/09 runs on them,
and its moments come back to the reveal, named where the event has a
timeline.

**The shape, and the two rules inside it.** The phone stays the source of
truth for the readings: a night is saved in Room first and only then
offered to the server (`POST /api/health/sessions`, then
`POST /api/experience/{id}/analyze`); the server's moments replace the
phone's top-N in one transaction, and the night records **where it stands**
— PENDING, SENT, ANALYSED, FAILED with the reason as a key — and **which
moments it holds** (LOCAL or SERVER). Those two columns are what let the
reveal say one true sentence instead of showing one thing while claiming
another. Failure costs a retry, never the night: on every app start, on
opening the night, and by a button on the reveal. Nights saved before today
start PENDING and upload on the next start — the backup that did not exist
when the gallery came up empty this morning.

**What the reveal says**, exactly one of: *Enviando a noite pro servidor…* ·
*Momentos encontrados pelo servidor* · *Momentos calculados neste aparelho*
plus why the night has not gone up (no account yet; session expired — go to
Settings; no internet — it stays here; the server's own answer with its
code). A moment with a cause — the song, the goal — shows it above its time.
The line is honest by construction: it is computed from the two columns
and the in-flight set, not from what a screen remembers.

**Decisions taken in the port, stated:** readings outside 30–250 bpm are
dropped before upload rather than sent, because one rejected value would
fail the whole upload with a validation error; readings sharing an instant
keep the first (the server does the same); `isPeak` among server moments is
the highest bpm, consistent with the 31/08 rule that the card leads with the
highest peak; the token's 24-hour life is checked *before* the upload, so an
expired session is a named state on the reveal and never a mystery at the
end of a night. Room 3 → 4 with a migration; the payload builder and the
answer parser are pure and tested.

**Gate:** CI green — unit tests (now 12 on the API side), `assembleDebug`,
the `.aab`. Felipe's half: a two-minute rehearsal capture, close the event,
open the night — the line should go *Enviando…* → *Momentos encontrados pelo
servidor*, and the peak on the reveal should match the session on
tumtum.cc.

**Next: Etapa 3** — the event has a name: chosen from the server's list,
and a "marcar momento" button that posts the clock time so the correlator
can name a goal.

---

## 2026-09-18 — Etapa 1 built: the designed app has a real account

The first byte `cc.tumtum.app` ever sends to a server. What changed, and
the two judgement calls inside it:

- **The client is the capture app's, ported** (`data/api/TumtumApi.kt`,
  `AccessToken.kt`): HttpURLConnection and org.json, suspend functions on
  IO, a token that knows its own expiry — the 27/08 lesson, a dead token
  found at the end of a night with no way back to a password field. The
  session lives in `UserPrefs` beside the account (`UserState.session`), so
  every screen sees signed-in and signed-out through the same state flow.
- **"Criar conta" creates the account on the server first** (`POST
  /api/auth/register`) and keeps it locally second. **"Entrar"** is e-mail
  and password against `/api/auth/login`, the name filled from
  `/api/auth/me`. The @, the tribes and the participant id stay on the
  phone: the server has no @ — the site's public profile is by name —
  and inventing one server-side is a decision for later, not a port.
- **Every failure says what it is:** wrong credentials (401), an e-mail
  that already has an account (409, the server's own sentence), no
  internet, or the server's sentence with its code. The old note on the
  login screen — *"Sem servidor ainda: a conta vive neste aparelho"* — is
  gone; it would now be the app stating something false about its own
  state.
- **Settings shows the server's side of the account** in three states —
  never signed in, signed in as *e-mail*, or expired — with sign-in and
  sign-out. The token lasts 24 h (`ACCESS_TOKEN_EXPIRE_MINUTES`), so
  "expired" will be the normal state of a phone opened a day later; the
  night upload (Etapa 2) has to ask for the password before it tries, not
  after, and this state is where it will look.
- INTERNET permission added — the app had none, being local; org.json as
  a test dependency, since the stub on the unit-test classpath throws; five
  tests on the token and the session.

**Gate:** CI green — unit tests, `assembleDebug`, and the release `.aab`.
The half that is Felipe's: sign in on the phone with the tumtum.cc account,
kill the app, open it, see Settings still say *Conectada como …*; then a
wrong password, and see it say so.

**Next: Etapa 2** — the night uploads and the server's moments come back.

---

## 2026-09-18 — the merge rule, broken a fifth time, by the assistant

PR #50 was opened as "log only" and merged by Felipe at 00:54. The
assistant kept pushing to the same branch afterwards — the two-apps entry,
the one-app plan, Etapa 0, its log — and kept **editing the description of a
merged PR** and sending its link as "the merge", while `main` had none of
it. Felipe asked whether he had to do anything for Etapa 1 and was sent the
link of a PR that was already closed. The four earlier occurrences are in
this log; CLAUDE.md records the rule in bold; this is the fifth, and the
first by the assistant knowing the rule word for word.

**What it cost:** an hour of Felipe believing Etapa 0 was in `main`, and a
question — "eu preciso instalar o app novamente?" — that only made sense
because the state described to him was false. This is the project's
signature bug class, in the assistant instead of the app: **stating
something false about its own state.**

**What is done about it:** the four commits are rebased onto `main` and go
out as a new PR. And a mechanical guard, since knowing the rule is not what
fixes it: **before sending any merge link, read the PR's state from GitHub
and quote it** — `merged: false`, head SHA equal to the branch's — instead of
remembering it.

---

## 2026-09-18 — Etapa 0 done: one repository, the designed app is `android/`

Felipe: *"vai."* The `app` tree (commit `23b10b8`, 02/09, no common
ancestor with `main`) is in `main` as `android/` in one commit, with the
source commit named so the branch's 19-commit history stays reachable on
origin; the capture app is `android-capture/`, the reference until each
piece is ported. The CLAUDE.md structure block and `android/BUILDING.md` say
so.

**One workflow for the app** (`build-app.yml`): a debug APK on every push,
published as a GitHub Release from `main` so builds keep installing over
each other, and the release `.aab` signed with the upload key of 17/09 when
the four secrets exist — otherwise the job skips itself with a notice.
`versionCode = 100 + run number`: a new workflow file restarts the run
number at 1, and Android refuses a downgrade, so the offset keeps every
build above whatever the old workflow put on Felipe's phone. The old
workflow's `ci-status-b*` / `ci-log-b*` branch signalling — built for an
environment with no GitHub API — is gone; those branches on origin are
its residue and can be deleted.

**Two judgement calls, stated:** R8 and shrinking are **off** in the
release block (it had never been built; Room, Compose and a foreground
service are what an untested minified build breaks silently — on again
after a minified build has captured a night on a phone, Etapa 4); and the
capture app's workflow lost its release job, since the key belongs to the
real app now.

**Gate:** `Build TumTum app` run 1 green on the branch — unit tests and
`assembleDebug` of `cc.tumtum.app` from `android/`; `Build capture APK
(reference)` green from `android-capture/`. The second half of the gate,
"installs over the build Felipe has", is his to confirm with the next
Release after the merge.

**Addendum, 01:49:** Felipe set the four `TUMTUM_UPLOAD_*` secrets and the
workflow was dispatched by hand on the branch: the `release` job decoded
the key, built `:app:bundleRelease :app:assembleRelease`, and uploaded
`tumtum-app-aab` (18.3 MB: the `.aab` for the Play Console and a
release-signed APK). The release signing config and the secrets are proven
before any merge; the first `.aab` of `cc.tumtum.app` exists.

**Next: Etapa 1**, a real account — `TumtumApi.kt` ported, "Criar conta"
and sign-in against `/api/auth`.

---

## 2026-09-18 — one app: the designed one absorbs the proven pipeline

Felipe, on learning there were two: *"a gente precisa centralizar tudo em um
app só… vamos considerar esse app que já tem toda experiência desenhada e
layout aprovado."* **Decided: `cc.tumtum.app` is the app.** The plan of the
merge — five stages, each with a gate a person checks on a phone, 4–5
sessions, ~2 weeks against 10/10 — is `docs/one-app-plan.md`.

What the decision keeps and what it moves, in one line each: the detector
rebuilt on 17/09 stays central (it is the server, and Etapa 2 is what makes
the designed app use it); the upload key is reused; the release build and
the `.aab` job move to the new tree; the Play Console account is the same
and **"Create app" must name `cc.tumtum.app`**, which cannot change
afterwards; the privacy page stays true; app 0.2.1 belongs to the app being
retired.

One thing the survey found that the plan has to say plainly: the designed
app's `NightAnalyzer.moments()` is **the eight highest samples at least ten
minutes apart** — a top-N, not a detector. It cannot see a rise against a
baseline, which is the whole of yesterday's work. Until Etapa 2 the phone
and the server will name different moments for the same night, and the
screen has to say which it is showing.

---

## 2026-09-18 — there are two Android apps, and this log knew about one of them

Felipe sent a screenshot after the merge: *"no app não aparece mais nenhum
card e nenhuma noite… não tem nada"* — a screen titled **Sua galeria**, 0
noites, 0 momentos, tabs FEED · AO VIVO · VOCÊ. None of that copy exists in
`android/` or in the site. It lives on branch **`app`**: package
**`cc.tumtum.app`**, 19 commits between 01/09 and 02/09, "primeira
implementação do handoff" of the Claude Design app handoff — Kotlin +
Compose, Room, its own BLE capture, its own Health Connect reader, its own
moment finder (`NightAnalyzer`, a local-peak rule, not the backend's
algorithm), a reveal lock that opens a night only at 10:00 the next morning
("protocolo do dia 25"), and — the fact that decides tonight's question —
**no backend at all**: "sem backend (social em repositório fake trocável)".
Not one HTTP call in it. Its CI publishes a GitHub Release `app-b<N>` per
build, signed with a committed debug keystore.

**This log never recorded it.** Every entry since 01/09 says "the Android
app" and means `android/` — `cc.tumtum.capture`, the native capture app
proven at Realness, the one that uploads to Railway and whose moments come
from `detect_peaks()`. The two were built in different sessions from
different briefs and have never been named side by side. That is a hole in
the memory this file exists to be, and it is now closed.

### What the screenshot means

The merge of #49 changed the backend, the site and `cc.tumtum.capture`.
**It cannot have emptied `cc.tumtum.app`**: that app keeps its nights in a
local Room database (`tumtum.db`) on the phone and has never sent a byte to
any server. Whatever emptied it happened on the phone — a reinstall or a
cleared storage takes the database with it, and nothing on the server can
bring it back because nothing was ever there. The gallery counts
*published* nights and sums moments over *all* nights; both at zero means
the local database holds no analysed night at all.

### The question this opens — item 33

Everything done this week targets `cc.tumtum.capture`: the detector rebuild
(backend), the release key and the `.aab` job (its `build.gradle`), the
Play Console plan (its package name), the privacy page's description of
what the app does. If the pilot's app is `cc.tumtum.app`, most of that
points at the wrong package — and that app's own moment finder has not been
examined at all. **Which app carries the pilot is Felipe's decision and is
not written anywhere.** The honest summary of the fork: `capture` is the
proven pipeline (six-hour capture, upload, the detector, cards with the
share sheet, a backend that can hold five people's nights); `app` is the
designed experience (feed, galeria, peles, the reveal protocol, live BLE
with a foreground service) with no server behind it. Merging them is a
project, not a task.

---

## 2026-09-18 — PR #49 merged: the detector, the release build and the privacy page are in production

Felipe merged #49 (`fd13e57`), nine commits from one day's thread. What the
merge put live, and what each one still needs to be *confirmed* — because a
merge is a deploy, not a verification, and this session cannot reach
tumtum.cc or Railway through its network policy to check (every probe
answered 403 from the proxy, not from the site):

| What went live | How it is confirmed |
|---|---|
| **The rebuilt detector** (median baseline, hysteresis, minimum rise, causal naming) on Railway | **Felipe's tap:** open the Realness night in the app and press "Procurar meus momentos". If moments appear that last minutes rather than seconds, the 30/08 durations were the instrument's ceiling — item 29 closes and the watch verdict is re-read via item 30. If nothing new appears, the night was spikes and the fix cost nothing. Either answer is a result. |
| **`tumtum.cc/privacidade` and `/en/privacy`**, linked from the footer | Open both once in a phone browser. Vercel's own build was green; the page has not been seen rendered on a device. |
| **The release build type and the CI release job** | Nothing runs until the four `TUMTUM_UPLOAD_*` secrets exist in the repository. The next push touching `android/` after they do should produce the `tumtum-captura-aab` artefact; that artefact is the confirmation. |
| **App 0.2.1** (the one-minute floor on the empty state) | Optional install; it changes one screen. |
| `docs/pilot-event-options.md`, the simulation script, open items 28–32 | In the repository. |

Nothing else changed in production: the timeline integrations are still
dead code (item 28), deletion is still by hand (item 32), and the phone
still needs nothing for the detector to count.

---

## 2026-09-17 — the Play account exists; the release key, the release build and the privacy page follow

Felipe created the Play Console account the same evening — personal, as
decided — and the identity check is with Google. Two "ação necessária" items
remain on his side of the console (confirm access to an Android phone via
the Play Console app; verify the phone number, which waits on identity).
The pieces on this side, done tonight:

- **An upload key, out of the repository.** Generated here, handed to Felipe
  as files to keep in a password manager, never committed. With Play App
  Signing the definitive key is Google's, so this one is recoverable by
  support request if lost — which is the reason it was acceptable to
  generate it in a session and hand it over rather than insist on a
  ceremony. SHA-256
  `F2:08:8E:B9:EA:8B:39:5E:9B:F0:C3:75:80:92:6A:87:87:1E:C6:54:D2:2F:49:41:A1:46:07:5B:AF:6A:7F:25`.
  It reaches CI as four repository secrets (`TUMTUM_UPLOAD_KEYSTORE_BASE64`,
  `_KEYSTORE_PASSWORD`, `_KEY_ALIAS`, `_KEY_PASSWORD`) — **Felipe's to set**;
  until they exist the release job skips itself with a notice and the debug
  build is untouched.
- **A release build type**: `cc.tumtum.capture`, no `.debug` suffix, no
  shrinking (a WebView plus BLE plus Health Connect is what R8 breaks
  silently, and nothing here is large). A release build attempted without
  the key fails at once naming the four variables, rather than producing an
  unsigned bundle. The workflow builds the `.aab` for the console and a
  release-signed APK for direct installs by a verified developer, as the
  `tumtum-captura-aab` artefact.
- **`tumtum.cc/privacidade` and `/en/privacy`.** The console asks for a
  privacy-policy URL and the site had none. Written in the one register the
  manual reserves for this screen — quiet, careful, short — and describing
  what the product does *today*: e-mail and name, the readings you capture
  or import, the event, the moments, the cards, which network a share went
  to; one Health Connect permission, heart rate only, only the event
  window; no sale, no ads, nothing published without the button; servers
  we rent; an error monitor that receives errors and not heartbeats.
  Linked from the site footer in both languages.

**One promise the page makes that the code does not yet keep on its own:**
deletion. There is no account-deletion endpoint — `users.py` has get, patch
and public profile — so the page promises deletion *by e-mail to
oi@tumtum.cc, confirmed within 7 days*, which is a manual process Felipe
runs. That is honest today and is open item 32; the Play data-safety form
will ask the same question.

---

## 2026-09-17 — from 30/09 an APK from an unverified developer will not install in Brazil

Felipe remembered being told that from October the APK could no longer be
installed by hand and asked whether that is so and what the process is.
Checked against Google's own pages today: **it is so, with a nuance that
changes the plan.** Android developer verification reaches Brazil (with
Indonesia, Singapore and Thailand) on **30 September 2026**: on a certified
device, an app whose developer is not verified and whose package is not
registered will not install by the normal route. **It does not require the
Play Store.** What it requires is a *verified developer* and a *registered
package name and signing certificate* — in the Android Developer Console
(distribution outside Play only) or in the Play Console (either). The APK can
keep travelling as a link.

Two escape hatches remain for an unregistered app, both per phone: **ADB**
(exempt outright — no wait, no limit, but a laptop and a cable at every
install) and the **advanced flow** (developer options → "apps de
desenvolvedores não verificados", confirm you are not being coerced, restart,
**wait 24 hours**, authenticate). And a **limited-distribution tier**: free, no
government ID, **up to 20 devices** — sized for a pilot of five.

### What the repository holds that the registration will ask for

- The installed app is **`cc.tumtum.capture.debug`** — the debug build type
  carries the suffix, and "the pilot installs this by hand, so a debug build
  is the product" (build.gradle). The package registered must be that string,
  not `cc.tumtum.capture`.
- It is signed with **the committed debug keystore** (`android/debug.keystore`,
  password `android`, alias `androiddebugkey`), fingerprint SHA-256
  `5F:9F:78:F7:AE:B3:6E:2E:03:D1:E4:4F:84:2C:5D:98:62:98:1F:85:87:BD:25:AF:38:03:6B:30:57:60:78:1A`.
  Registering that certificate works mechanically and binds TumTum's verified
  identity to a key anyone with the repository can sign with. The build file
  already says the release "needs a real release key kept out of the
  repository". Verification is the moment that stops being optional.

### The process, in order

1. **Register this week, as a person.** Government ID, US$ 25 once. An
   organisation account needs a D-U-N-S number and takes weeks; a person can
   be verified in days, and the account can change later. If a Play Console
   account is going to exist anyway, register there: it covers apps
   distributed outside Play too, and Play verification carries over.
2. **Make a release key and a release build.** Keystore out of the repo (a
   GitHub secret, the workflow signs), `release` build type without the
   suffix, package `cc.tumtum.capture`. One uninstall and reinstall on
   Felipe's phone, since a new key cannot update the old install. One
   session of work.
3. **Register the package and the certificate.** Whichever pair is chosen —
   release is the honest one — is the pair every tester's phone will check.
4. **Keep both hatches written down for the day.** If verification has not
   landed by the first event (10/10 at the earliest), ADB at a meetup with a
   laptop, or the advanced flow started at least 24 hours before — per
   phone, and a frightening screen at exactly the moment the brand goes
   quiet and careful about health data.

None of this touches the **health-permissions Play review** of Etapa 5 in
`health-connect-plan.md`: verification is identity, not policy review, and a
hand-installed APK still reads Health Connect without one.

### "Is the Play Store itself feasible by then?" — a listing, no; a test track, yes

Felipe asked. Checked the same evening. **A public listing by 10/10 is not
realistic**: a personal Play Console account created after November 2023
must run a **closed test with 12 testers opted in for 14 continuous days**
before it may even apply for production access (organisation accounts are
exempt, and need a D-U-N-S number that takes weeks); the **Health apps
declaration** is mandatory for anything on closed, open or production
tracks, with a review measured in weeks not days (Etapa 5 of the Health
Connect plan already says so); and the app also carries a Bluetooth
foreground service, which is its own declaration. Three reviews in thirteen
days, on a brand-new account, is a bet, not a plan.

**A test track is a different question.** The **internal testing** track —
up to 100 testers by e-mail list, a Play link, installs and updates through
the Play Store — needs the account, one upload and an opt-in, and is not
gated by the 14-day rule. Apps distributed through Play are registered for
developer verification automatically, so the 30/09 wall disappears with it.
The one thing to verify on the console itself before relying on it: whether
the Health Connect declaration is enforced on the internal track (Google's
own page lists closed, open and production). If it is, the pilot's testers
install by hand with a verified developer as in the process above; if it
is not, internal testing is the cleanest route there is — a link, no cable,
automatic updates, and a first step onto the Play Store taken early rather
than late. Either way the account is the first move, and it is the same
account.

---

## 2026-09-17 — the detector is rebuilt around a median, and a moment is named by its cause

Felipe: *how do we fix the algorithm so it reads these moments — the songs a
fan loves most and the most emotional moments of a match — and identifies
them correctly?* Two changes, shipped together with 19 tests, the
specification in CLAUDE.md rewritten to match, and 84 backend tests green.

### Detection: the median is the fix, the twenty minutes follow from it

The failure was never the width of the window; it was that a **mean contains
the event it is the reference for.** Widening from 60 s to 300 s bought a
13-second spike and lost everything longer than ~90 s. Widening again to
1800 s would buy a song and start to lose an encore, and it was already slow
— 9 s for a six-hour night, O(n × window). So the baseline is now a rolling
**median** with an IQR spread, kept as a sorted window that each sample
enters and leaves once by binary search: a six-hour night analyses in
~0.05 s, and the reference does not move until an elevation fills half the
window. With a 1200 s window that is a nine-minute moment before anything is
lost.

Two smaller pieces made it clean rather than merely correct:

- **Hysteresis.** A region opens at z > 2 and closes at z ≤ 1. Without it a
  four-minute song fragmented into a dozen slivers wherever the noise dipped
  for a second — the 1200 s row of this morning's table reported 16 moments
  for 6 events.
- **A minimum rise of 10 bpm** to open a region, 5 to keep one open. A robust
  spread on a quiet, slowly drifting hour is a couple of bpm, so the first
  median prototype scored a 4 bpm wobble at z > 2 and reported seven moments
  on a night with nothing in it. A moment is a rise a person would feel.

| Scenario | Before (mean, 300 s) | After (median, 1200 s) |
|---|---|---|
| Match, strap 1 Hz — 3 goals, near-miss, saved penalty | 3/5 | **5/5**, five reported |
| Match, watch at 1 per 32 s | 0/5 | **4/5** |
| Match, watch at 1 per 60 s | 0/5 | 3/5 |
| Show — 3 favourite songs sung for 3'50" + 3 spikes of 15 s | 0/3 songs | **3/3 songs, 3/3 spikes, six reported** |
| Realness-shaped night — twenty 8–22 s moments | 20/20 | **20/20** |
| Quiet, drifting 3 h (three seeds) | 0 | **0**, at every window up to 3600 s |
| Longest single elevation seen | ~90 s | **~540 s** |
| Six-hour night, wall time | 1.7 s | **0.05 s** |

Each peak now carries `start_time` and `end_time` — the whole song, not the
second it peaked — and merged regions keep the union of their bounds. Not
stored yet; the card's curve could shade the whole moment with it.

### Naming: the cause precedes the moment

The second half of the question was *identifying them correctly*, and the
correlator was wrong for the same reason the detector was: it assumed a
moment is a point. It matched the nearest entry within ±60 s **of the peak**.
A favourite song peaks wherever the heart was highest — often three minutes
in — and at that second the song's own entry is out of reach while the next
song's may be inside it. The rule is now causal: **the latest entry between
(region start − 60 s) and (peak + 15 s)**, with the old nearest-within-±60 s
as the fallback for a spike that nothing precedes. A stored peak, which has
only a timestamp and a duration, is treated as a region ending at the peak,
so nights already in the database are named on the same rule when
re-analysed.

### What this does to what is already stored

Nothing until a night is analysed again — the same mechanism as the quality
score: "Procurar meus momentos" restates a session from its stored readings.
**Realness is the confirmation.** The next time that night is opened, if
moments appear that were never reported and last minutes rather than
seconds, the 30/08 durations were the instrument's ceiling, and the watch
verdict of that day gets re-read in the light of item 30: long moments are
the ones a slow watch *can* see. If nothing new appears, the night really was
made of spikes, and the fix cost nothing.

### What the phone needs, and what it does not

Felipe asked whether the app on his Android has to change for any of this to
count. **No — the detection runs on the backend.** The app captures, uploads,
and then calls `POST /api/experience/{id}/analyze`; the moments it draws are
whatever Railway returns. The merge is the update. "Procurar meus momentos"
on the Realness night, after the merge, is the confirmation described above,
from the phone he already has.

What the app *did* hold was a claim about the old detector: a capture under
five minutes hid the button and said a moment "precisa dos cinco minutos em
volta para comparar" — `BASELINE_WINDOW_SECONDS = 300` copied from the spec.
The new detector needs no such minimum: measured against it, a 15 s spike
shows in a two-minute capture and a three-minute rise needs about eight. So
that screen would have refused a capture the detector could read, which is
this project's signature bug class — the app stating something false about
its own state. Fixed in the same PR: the floor is one minute, the copy names
no number, and the build is 0.2.1 (versionCode 3) so it installs over 0.2.
**Optional to install**: it changes only that one empty state. The web
import screen (`frontend/lib/health/quality.ts`) still describes a 60 s
baseline in its comments and calls a one-per-minute cadence "insufficient"
— true for a spike, no longer true for a song or a goal; that verdict waits
on the Mi Band measurement (item 30) rather than on a guess.

**Departure from this morning's own rule.** Twice today this entry's
predecessors said "deliberately not changed: a deployed detector, a simulated
finding, confirm on Realness first." Felipe then asked for the fix directly,
which is the word the precedent waited on; the confirmation still happens, on
the first re-analysis, and the change is reversible by one merge. The
simulation was the argument for changing; the tests are the reason it is
safe to.

---

## 2026-09-17 — "a song lasts four minutes — is that not the same case?" It is, and worse

Felipe, reading the goal finding: at a show the thing to measure is the peak on
a specific song, and a song lasts 3, 4, 5 minutes. Is that not the same case as
the goal? **Yes.** Simulated the same evening
(`scripts/simulate_moment_detection.py`, scenario 2): a 1h52 show with three
songs of sustained euphoria (~3'50") and three 15-second spikes inside other
songs, strap at 1 Hz.

| Baseline | Favourite songs | 15 s spikes | Moments reported |
|---|---|---|---|
| **300 s (today)** | **0/3** | 3/3 | 3 |
| 900 s | 0/3 | 3/3 | 3 |
| 1200 s | 3/3 | 3/3 | 16 |
| **1800 s** | **3/3** | **3/3** | **6** |

### The Realness durations may belong to the instrument

At today's setting the detector cannot see a song. What it reports is the
short spikes, with durations of 8–22 s — **which is exactly what Realness
reported: twenty moments, all 8–22 s, none longer.** The 30/08 entry took
those durations as a property of the night and built the watch verdict on
them ("every moment is shorter than the interval between two Fit3 readings").
The simulation says they may instead be the ceiling of what a 300 s window can
report. It does not say which; it says the question exists, and it is
answerable on data we already hold: **re-run the Realness night at 1800 s and
see whether moments appear that were never reported.** If they do, the watch
verdict of 30/08 needs re-reading as well — not reversed, re-read: long
moments are the ones a slow watch *can* see.

### The rule, and what it encodes

A sweep of single elevations at +32 bpm: **the baseline window must be about
five times the length of the emotion.** 300 s sees up to ~90 s; a 4-minute song
needs ~1200 s, a 5-minute one ~1500 s. False positives on a slowly drifting
quiet capture begin at 2700 s. Working range **1200–1800 s**, one number for
both a match and a show.

The window is not a tuning constant; it is the definition of a moment. At
300 s a moment is a spike against the last five minutes. At 1800 s it is a
song against the last half hour. The card says *"seu coração em [música]"*;
the window has to be the song's size for the sentence to be true. Whether the
short spikes are *also* worth reporting — the drop, the guest walking on — is
a product question, not settled here.

**Still not changed**, for the same reason as this morning, now with a
sharper test: the Realness re-run is the confirmation, and it costs one
command against data that exists.

---

## 2026-09-17 — the detector loses a goal, and a watch can see one

Felipe asked three things: the fan bases behind the shortlisted shows, whether
buying a cheap Xiaomi band and handing it to a tester would do, and how the
match on the pitch gets married to the reading on the wrist. The contacts and
the alignment protocol are in `docs/pilot-event-options.md`, sections 9 and 10.
The watch question produced a defect, and it is the one worth recording.

### A goal is the wrong length for our own baseline

No capture we own is a match, so this is a **simulation** —
`scripts/simulate_moment_detection.py`, a 2h15 match at 1 Hz with three goals, a
near-miss and a saved penalty, decimated to the Health Connect cadences and run
through the real `detect_peaks()`. It found this before it answered anything:

| Elevation lasting | Detected, 300 s baseline |
|---|---|
| 23 · 38 · 68 · 98 · 143 s | yes |
| **188 · 278 · 368 s** | **no** |

**Any elevation longer than about half the baseline window disappears**, for
exactly the reason CLAUDE.md gives for widening the window from 60 s to 300 s
in the first place: a peak that sits inside its own reference window raises the
mean it is measured against. The 300 s window solved that for a 13-second
concert moment and **reintroduces it one timescale up**, where a goal lives. At
1 Hz with today's setting the simulation found the near-miss and the saved
penalty and **lost two of the three goals** — the biggest moments of the match
are the ones it drops.

At 600–900 s the same match reads 5/5, and widening costs nothing where we have
evidence: a concert-shaped night of twenty 8–22 s moments reads 20/20 at both
300 s and 900 s, and a quiet three hours reports zero peaks at every setting.

**Not changed.** This is a deployed detector and a simulated finding, and the
precedent is the quality score, which waited on Felipe's word. The honest
confirmation is cheap and exists: re-run the Realness night at 900 s and check
the twenty moments survive on real data.

### The first time a wrist device could resolve the moments

| Baseline | Strap, 1 Hz | Watch, 1 per 32 s | Watch, 1 per 60 s |
|---|---|---|---|
| 300 s (today) | 3/5 | 0/5 | 0/5 |
| **900 s** | **5/5** | **4/5** | 1/5 |

Etapa 0 closed with the watch delivering the curve and never the moments. That
verdict holds for a concert and is arithmetic. **It does not transfer to
football**, because a goal lasts minutes: at one reading per 32 s the
simulation recovers four goals in five. At one per minute — which is the Xiaomi
Smart Band 9's best continuous setting — it recovers one in five.

So the answer to *should I buy a Xiaomi and hand it over* is: **not as a source
for the pilot, and there is nothing to buy.** Felipe already owns an unopened
Mi Band 9 (open item 23). Opening it, wearing it a night and measuring what it
writes into Health Connect inside a workout closes the last open question of
Etapa 0 and answers this one at no cost. The cadence, not the price, is the
whole question.

### The alignment protocol, in one line

Absolute clocks, marked live, cross-checked against the published minute of the
goal — **never `kickoff + elapsed`**, which is the bug in
`parse_fixture_to_timeline()` recorded this morning, and never a broadcast
clock, whose delay is as large as the matching window. The piece worth building
is a **"marcar momento" button** that posts the current UTC time to
`/api/events/{id}/timeline`: 6–12 taps turn a match into a testable event, and
the endpoint already exists.

---

## 2026-09-17 — the pilot loses its date, and the timeline code turns out to be the real constraint

Felipe: he probably cannot run the test at the Tasha & Tracie show on 25/09.
He asked for upcoming São Paulo shows with engaged fan bases, and whether the
test could run at a football match instead. The calendar research and the
full shortlist are in **`docs/pilot-event-options.md`**; what belongs here is
what the search found underneath the calendar.

### The question was about events and the answer is about code

Both integrations that were supposed to produce an event timeline are unusable
as they stand, and **neither is imported by any route or any test** — they are
dead code:

- **Setlist.fm publishes song order, never times.** `parse_setlist_to_timeline()`
  therefore estimates, at a flat 4 minutes per song from the start time. The
  error accumulates: one long intro or one speech and the tenth song is ten
  minutes out. The correlator matches within **±60 s**, so matching fails from
  about the third song.
- **`parse_fixture_to_timeline()` computes `kickoff + elapsed minutes`**, which
  ignores the ~15-minute half-time interval and first-half stoppage. **Every
  second-half goal lands 15–20 minutes before it happened** — fifteen times
  outside the matching window. The first half is roughly right, which is the
  dangerous kind of wrong.

What works today is `POST /api/events/{id}/timeline`, authenticated, one entry
at a time. So whichever event is chosen, **a human writes the timeline.** That
reframes the choice: a concert needs ~20–25 hand-marked song starts, a match
needs 6–10 entries typed from the match report.

### Football, evaluated honestly

It buys four things a concert cannot: **objective timestamps** (the minute of a
goal is a published fact), **a synchronised collective peak** — every heart in
the stadium spikes within the same two seconds, which is the only way to test
card 04, *A galera* — **a story every Brazilian already understands**, and
**kick-off times that end before midnight**, so open item 18 never comes up.
Tickets also exist: a league round is buyable at R$ 45–90 three days out, where
the concerts with the fan bases worth testing sold out months ago.

It costs: **it can be 0–0**, and then the moments have no names, which is the
Realness failure repeated by choice; **the biggest spikes are not in any feed**
(the missed penalty, the near-miss), so someone still notes clock times by
hand; it is **only ~2 h**, meeting the Phase 5 gate with no margin; and
**stadium cellular is the worst network in the city**, so the 1.33 MB upload
(item 15) should be expected to fail at the whistle and be retried on the way
home.

**They are not the same test.** The match answers *does the correlation hold in
public, on more than one body*. The concert answers *does anyone send the
card*, which is the pilot's actual research question (item 25) and is a
question about a fan base, not a sport. The recommendation is to run both, the
match first because it is sooner, buyable, and everything it teaches makes the
concert test better.

### What the search shortlisted

- **Technical test: São Paulo × Vitória, 10/10, 21h, MorumBIS** — buyable
  tickets, ends before midnight, full stadium. **Corinthians × Fluminense this
  Sunday, 20/09, 16h** is faster and *earlier than the date being missed*, but
  the ticket is online-only through Fiel Torcedor **and requires facial
  biometrics already registered** — nobody joins that one on the day. Maximum
  emotion, if tickets can be found: **Palmeiras × Fluminense, Libertadores
  semifinal second leg, 20–22/10, Nubank Parque**.
- **Product test: BTS at MorumBIS, 28, 30 and 31/10** — three nights, and the
  most organised fan base in the world, whose sharing culture is precisely the
  engine the card needs. Sold out since April, so it only works through people
  who already hold tickets. Otherwise **Hayley Williams, 12–13/11, Espaço
  Unimed** (both nights sold out fast, indoor, ends before midnight), or
  **Tasha & Tracie in Santos, 06/11** — the same show being missed, six weeks
  later, 80 km away.
- Ruled out on the midnight rule as they stand: ZIG Festival (10/10), Audio
  late shows, Primavera Sound (05–06/12, two 12-hour days).

### The constraint that is not the calendar

Choosing a date does not fix the thing that limits the pilot. **The moments
need a chest strap** — Etapa 0 settled that on 30/08 — so the number of people
with real moments equals the number of straps, not the number of participants.
The log knows of one. With one strap the collective peak at a goal cannot be
measured at all, and card 04 cannot be tested. Three straps that broadcast the
standard BLE Heart Rate Service (0x180D) would change that, and the app already
speaks that protocol. It is a purchase decision, not an engineering one.

---

## 2026-09-01 — the site is redesigned from the Claude Design handoff, and the deploy is one merge away

Felipe designed a full replacement for tumtum.cc in Claude Design and brought
the export. **The handoff was excellent** — a README that names itself a spec,
final copy in both languages, hard rules restated (never white on the accents,
a number is pink only on a dark ground, the wordmark only ever from the SVG),
and honest notes about its own limits: measurements are desktop-only, and its
~25 MB of GIFs "far too heavy to ship".

Rebuilt in the site's own stack, per the README's instruction, not copied:

- **One layout, two languages.** `SiteLanding` renders from a `SiteCopy`
  object; `/` passes PT and the new `/en` passes EN, with hreflang alternates.
  The type demands both languages, so a section cannot exist in one and
  silently miss the other. The prototype's localStorage switcher became routes
  — the handoff itself recommends that for production.
- **The GIFs became 8-second muted MP4 loops: 25 MB → 1.4 MB**, plus poster
  frames for the moment before load. Largest slot in the design is 310px, so
  620px-wide video covers retina.
- **The waitlist form kept its honesty and dropped its questions.** The new
  design has one email field; the names were optional in the API all along,
  and the page beneath the form promises "a gente só usa seu e-mail". Every
  outcome is still visible — sending locks and says so, a repeat submission is
  warm rather than red, a failure keeps what was typed and blames the server.
- **The landing finally has a sign-in link** — two, nav and footer. Open item
  17 closes: the design solved it, the rebuild shipped it.
- `LandingNav`, `VideoSlot`, `Reveal`, `WaitlistForm` deleted with the old
  page — nothing else imported them. `Reveal` goes unmourned: the new design
  specifies no scroll animation at all, in line with the motion rules.
- Verified by rendering, not only by building: production build served
  locally, screenshotted at 1440 and 390 wide, both routes. Mobile was not
  designed in the handoff and follows the site's existing stacking.

**Not yet live.** The site deploys from `main`; this is pushed to the branch,
where Vercel builds a preview per branch. Going live is merging PR #45 —
deliberately left as Felipe's act, since it also carries eleven days of other
work: the palette, the quality score, the card with the curve, Health Connect.

### Open item 17 — closed by design, literally

The item read "decide before the pilot how a person with an account gets in
without typing an address." The decision arrived from the design side: the
redesign simply included the link, twice. Some open items are closed by
engineering; this one was closed by someone drawing what was missing.

---

## 2026-08-31 — the palette migration, forced by a sync that would have spread the old one

Item 26 was left open with the timing as Felipe's call. Starting a design-system
sync to Claude Design answered it: the sync uploads the real compiled
components, and **the design agent then builds every screen out of them**. Push
a library whose primary accent is a colour the manual deleted, and every screen
designed afterwards is lime — the code and the designs both need redoing.

So the migration ran first. **70 usages across 34 files**, in all three
codebases: `tailwind.config.ts` and 12 components plus the pages that use them,
`colors.xml` and `HRCurveView` on Android, `card_generator.py` on the backend.

### Mostly a rename, and one reason that is not luck

**Every `bg-tumtum-lime` in the codebase already carried
`text-tumtum-black`.** Not one white-on-accent pairing existed, so the swap
preserved contrast without a single judgement call. That is not luck: white on
Acid Lime was 1.19:1 and already forbidden by v0.1, so the rule that protected
the old palette is what made the new one land clean. Black on Pink is 7.93:1.

The literal colours were the part that needed care rather than `sed` — the
Tailwind token, the `PINK` constant in `HRCurve.tsx`, two SVG attributes on the
landing page, and the RGB tuple in the card generator.

### The change makes one thing visibly better

Rendering the card in Pink showed something the argument had missed. **Acid
Lime and Toxic Yellow were nearly the same hue**, so the yellow moment-marker
sat on a lime curve and all but vanished — the very element whose job is to
say *this is the moment*. Against Pink it reads immediately. The palette
change bought a legibility fix nobody asked for.

Against that, the loss is real and already recorded: Pink is 7.93:1 on black
where Lime was 17.7:1. The hero number is large enough that AAA still holds,
but anything small and Pink is now carrying less weight than it did.

Verified: 65 backend tests, 55 frontend tests, ruff clean, lint unchanged.

---

## 2026-09-01 — the mobile handoff, and a breakpoint that was in the wrong place

The v0.5 handoff answered all four questions open item 27 had named, and the
answers were better than the adaptation they replace: the four cards become a
**snap-scrolling swipe carousel** with an explicit "Arrasta pro lado" hint,
the proof strip becomes full-width rows with the number on the left, the
gallery leads with its copy and takes the grid full width, and the nav trades
its vanished links for a **text MENU panel** — text rather than an invented
icon, which the handoff states as a brand rule.

Implemented as **one responsive page**, which the handoff explicitly asks for
over two.

### The correction the measurements forced

The first pass used Tailwind's `md` (768px) as the switch between the two
designs, which is the reflex. It was wrong, and measurably: **at 768px the
page overflowed by 64px**, because the hero had already become two columns
while there was not room for both. Moving the structural switches to `lg`
fixed most of it and left **23px**, from the proof strip — its wrapper turned
into a row at `md` while its children only became rows at `lg`, so each block
was a row inside a row, forcing its own width.

The handoff had said it plainly: **desktop ≥1024, mobile ≤480**. The lesson is
not "use lg": it is that a design's own stated breakpoints are part of the
spec, and substituting a framework default for them is a silent
reinterpretation.

Verified at six widths — 360, 390, 480, 768, 1024, 1440 — `scrollWidth` equals
the viewport at every one.

### Worth noting about the copy

The handoff's one copy change was **"próximo show" → "próximo evento"** in both
CTAs — the same change Felipe had asked for an hour earlier, arrived at
independently. TumTum is concerts *and* football, and the CTA was the last
place still naming only half of it.

---

## 2026-09-01 — PR #45 merged: everything that was true only on a branch is now true in production

Thirty commits, five days, and until this morning **none of it existed for
anybody but us**. `tumtum.cc` still served the old page, the app still
generated lime cards, and the Realness night still claimed a quality it did
not have. The merge is the moment the work stopped being a description of
itself.

What crossed into production, and what each one had been waiting on:

| Change | Was blocked on |
|---|---|
| The whole tumtum.cc redesign, PT + `/en` | the merge alone — built, verified, never live |
| TumTum Pink replacing Acid Lime, all three codebases | the merge; every card generated before today is off-brand |
| The quality score counting continuity | a deploy, since 31/08 |
| The card leading with the highest peak, carrying the curve | the same deploy |
| Health Connect, "Trazer do meu relógio" | an APK build, now running |

### The one thing that does not fix itself

The Realness session **still holds `data_quality_score = 100` in the
database**. The new code recomputes on analysis rather than migrating, so the
number corrects when that night is opened and "Procurar meus momentos" is
tapped — not before. This is deliberate and was designed that way, but it
means the fix is not finished by the deploy. **Nobody should read 100 on that
night and conclude the fix failed.**

### What the mobile detour cost, and bought

The site went out with three real mobile defects that only a measured browser
found: the feed's two 220px screenshots summed to 452px on a 390px screen and
put the whole page into horizontal scroll; the nav CTA wrapped onto two lines;
the nav overflowed by 12px. All three are fixed and verified at 390×844
(`scrollWidth` equals the viewport).

The lesson is narrower than "test on mobile": **resizing a desktop browser
would have shown the wrap and hidden the overflow**, because the page scrolls
sideways rather than visibly breaking. It took reading `document.scrollWidth`
against `innerWidth` to see it at all.

**Mobile is correct, not designed.** The handoff specified desktop only
(~1440px); everything below `md` is engineering adaptation — where the desktop
composes, this stacks. Felipe is commissioning a real mobile handoff, and the
four places the adaptation is dumbest are named in open item 27.

### A trap this session walked into twice

Two long debugging detours had the same root: **a stale process serving a
stale build.** A zombie `next-server` survived `pkill`, kept port 3111, and
served CSS from two builds earlier — which looked exactly like "Tailwind is
not generating arbitrary values", a completely wrong diagnosis that cost
several rounds. Then `pkill -9 -f "next|node"` matched its own command line
and killed the shell running it.

Both are the project's own bug class wearing work clothes: **the tool stating
something false about its own state.** The rule that would have caught it
first: when a rendered page disagrees with the source, verify *which build is
being served* before questioning the build system.

---

## 2026-08-31 — manual v0.4: the accent is TumTum Pink, and the code is now out of date

Felipe brought manual **v0.4 (31 Aug 2026)** and said the palette changes
again. **Acid Lime `#C6FF00` is gone. TumTum Pink `#FF6F91` is the primary
accent.** Toxic Yellow `#EFFF00` stays as the secondary. Black and white are
unchanged.

Written up as `docs/design-brief.md` — a self-contained handoff he can paste
into Claude Design, since he intends to design the screens there. `CLAUDE.md`
was corrected in the same pass: it stated the lime palette as fact, and it is
the file every future session reads first. **A stale instruction file is this
project's bug class with the widest blast radius** — it does not mislead one
screen, it misleads everything built after it.

### The change is not a swap of one hex for another

Two things behave differently and both affect layout:

**Pink can hold large surfaces.** The old accent was an acid highlight that
only worked in small doses. Pink may be a full background, a colour field, a
whole panel. That widens what a layout can do rather than just recolouring it.

**Pink is roughly half as loud on black.** Measured: **7.93:1** against Acid
Lime's 17.7:1. It still passes AA everywhere and AAA at large sizes, so nothing
is inaccessible — but emphasis that used to come free from the colour now has
to come from **scale**. The correct response is bigger, not brighter; reaching
for a lighter tint would leave the palette.

White on Pink is **2.65:1** and fails even large-text AA, so the "never white on
an accent" rule survives intact, with a new number.

### A constraint that lands squarely on the design tool

The Typozon EULA (v3.4) carries an **explicit AI/machine-learning restriction**
on Chosmos. So the rule "never let a generator redraw the logo" is not only
brand hygiene here — handing Chosmos to an AI tool, or having one imitate it,
is a legal question. The brief says it plainly and offers the two acceptable
moves: place the official asset, or leave a marked placeholder. A lookalike
rendered from a substitute font is worse than no logo, because it gets mistaken
for approved artwork later.

### What is now inconsistent, deliberately

**The product still ships lime.** `TUMTUM_LIME` in `card_generator.py`, the
Android drawing code, and `frontend/tailwind.config.ts` are all the old
palette, so every card generated today is off-brand — including the ones
rendered this morning. The change is mechanical, a handful of constants, but it
is visible in a shipped app, so it is Felipe's call whether it lands now or
with the screen design. Recorded as open item 26 rather than done quietly.

The manual also notes something worth keeping in view: any TumTum artwork or
screenshot from before 31/08 is **historical, not a variant**. It should be
rebuilt in the new palette, never matched.

---

## 2026-08-31 — the card says what the night says: highest peak, generated copy, and the curve as evidence

Four layouts were rendered from the Realness numbers and compared side by
side. Felipe chose **D — the number plus the curve** — and added the sentence
that matters more than the choice: *"esse é um ponto que vale pesquisarmos com
o público da TumTum."* That is open item 25, and it is the right instinct.
Everything below is an argument about whether the card is **honest and well
made**. Whether a stranger posts it is a different question and nobody in this
repo can answer it from a screen.

### The comparison earned its cost

Rendering all four was not ceremony. **Variant C was the control** — the
shipped layout with the right number and honest copy — and it came back still
empty. That killed a hypothesis of mine that fixing the number and the copy
would be enough on its own, and it killed it with an image rather than an
argument. Without C, D would have looked like over-building.

### Three changes, and the reason each one is not cosmetic

**The peak.** `cards.py` ordered by `rank`, which is the detector's magnitude
— z-score × duration. That favours a long statistically unusual rise over a
brief spike, which is right for *finding* moments and wrong for choosing the
subject of a card. At Realness rank 1 was **92 bpm on a night that reached
116**: the card led with the smaller number. Now ordered by `bpm`.

**The copy.** `MOMENT_COPY = ("EU TAVA TRANQUILO.", "AÍ VEIO ISSO.")` is gone,
replaced by `moment_copy()`, which builds two lines from the night's own
figures. The constant failed twice at once: identical on everybody's card, so
it said nothing about anybody; and *"eu tava tranquilo"* asserted **a calm
baseline nobody measured** — on a night that ran high it was simply false, in
the largest text on a public object.

The reference is the **session average**, not the detector's local baseline,
because the local baseline is not stored on a peak and a card must never reach
for a number it does not have. Every branch of the function states something
the card's own figures establish, and the fallbacks claim less rather than
inventing more: no average, or no rise above it, and the copy drops to
*"SEU CORAÇÃO, ÀS 01H24."* Four tests exist purely to prove it never dresses a
flat moment as a climb.

**The curve.** Card 01 deliberately carried no chart, and `cards.py` held a
comment explaining that the readings were not loaded for exactly that reason.
That is now reversed, and the reversal is the interesting part: the old
decision was right while the card was a number on a black field, and wrong
once the card had to give a stranger a reason to believe it. The manual
permits this precisely — the person's own data, as product information.

### The part that came from this morning

The curve resamples onto a **uniform time grid**, not onto the readings.
Spacing the readings evenly would have squeezed the Realness gap down to
nothing and drawn a continuous line across 79 minutes nobody measured — **the
same lie the quality score was telling until this morning**. An empty slot
stays empty, `segments()` hands the runs over already split, and the card
draws a night with a hole in it as a night with a hole in it. Rendered against
the real shape, the first fifth of the frame is bare, and that is correct.

The card also **stores the curve it drew** in its metadata. `get_card_image`
regenerates from metadata once the Redis TTL lapses, so without this the image
served a week later would quietly differ from the one that was shared. A card
is a snapshot of a moment, not a live view of a session that may since have
been re-analysed.

### Left undone, deliberately

The surface. Cover the wordmark and it is still a black field with a lime
number — no skin, no texture, nothing a person learns to recognise while
scrolling. That was the third of the four problems and it is untouched,
because the first two decide what the design has to accommodate. It is the
remaining half of item 24.

---

## 2026-08-30 — the card came out, and it does not make anyone want to post it

Felipe generated the first card from the Realness night and gave the verdict
the project needed most: **"está muito simples e não gera esse desejo e
necessidade de postar."**

That is not a complaint about polish. **The share card is the viral engine.**
Everything upstream of it — the capture, the detection, the watch path, the
quality score fixed this same morning — exists to produce an object somebody
wants to put on their Story. If the object does not travel, Phase 0 answers
its own question with a no, and it answers it regardless of how good the
plumbing underneath is. So this is the top of the list, not the bottom.

Four things are wrong with the card. Only two of them are visual.

### 1. The hero number is 92, and 92 is not a number

The card picked **92 bpm às 02h05**. The night's maximum was **116 às 01h24**.
`cards.py` orders peaks by `rank` and takes the first, and `rank` comes from
the detector's **magnitude — z-score × duration**. That ranking is right for
*finding moments*: it favours a long, sustained, statistically unusual rise
over a brief spike. It is wrong for *choosing the hero of a card*, where the
only job is to be striking.

But swapping it for `max(bpm)` only trades 92 for 116, and 116 is not
striking either. **The deeper problem is that an absolute BPM means nothing
without the person's own baseline.** Ninety-two reads as a flight of stairs.
The dramatic fact of that night is not 92 or 116 — it is that Felipe sat
still for six hours at **78 average** and something pulled him to 116 anyway.
The card shows the destination and hides the journey, which is the entire
story. Whatever the design becomes, it has to carry the **jump**, not the
value.

### 2. The headline is a constant, and it may be a lie

```python
MOMENT_COPY = ("EU TAVA TRANQUILO.", "AÍ VEIO ISSO.")
```

Every card TumTum has ever generated says this, and every card it generates
tomorrow will too. A thing that travels has to be *different each time* —
part of why anyone posts is that the object says something specific about
them, and a fixed line says the same thing about everybody.

Worse, it is a **claim about a state nobody measured**. "Eu tava tranquilo"
asserts a calm baseline before the moment. If the baseline was already
elevated the card is simply asserting something false, confidently, in the
largest text on it. That is this project's bug class — the app stating
something untrue about its own state — now the **fourteenth** instance and
the first one on a public object. The line has real data sitting right next
to it (the baseline the detector already computes for every peak) and uses
none of it.

### 3. Cover the wordmark and nothing says TumTum

A black field, a lime number, centred type. It is clean, it is on-manual, and
it is anonymous. **Mutante Pop is the declared territory** — one recognisable
structure surviving thousands of surfaces — and the skins have been parked
since 25/08. The card is the one place in the product where the surface is
supposed to be doing the work, and it is the one place still showing the
base. Nothing here is a shape a person learns to recognise while scrolling.

### 4. There is no evidence on it

Card 01 is "Só o momento" by design and deliberately carries no chart; the
time series belongs to card 03, "Minha noite". That decision is defensible
and it is also what leaves this frame empty. The manual explicitly permits
the real heart-rate line as *product information* — and the curve is the
proof that something happened, the thing that separates this from a poster
anyone could type. Realness has a curve worth showing: six hours, twenty
moments, a visible climb.

Related and unresolved: **the moment has no name** (open item 22). There are
no timeline rows for Realness, so `02h05` is a timestamp where a story should
be. "Durante *Vogue*" is a card. "02h05" is a receipt.

### What tomorrow starts from

Not a restyle. The order is: decide what number the card leads with (the
jump, almost certainly), give the headline something real to say, then make
the surface unmistakable. Design last, because two of the four problems are
about what the card *knows*, not how it looks.

---

## 2026-08-30 — "Qualidade 100%" over a 79-minute hole: the score measured the wrong thing

The twelfth instance of this project's oldest bug class was a document. The
thirteenth is a number, and it is the most confident one the app has shown.

The night screen for Realness read **Qualidade 100%**. The capture ran
21:11 → 03:17, and the strap was only on for the last 287 of those 366
minutes — Felipe connected it briefly at home, then again at the venue. A
**79-minute hole**, a fifth of the window, and the score was perfect.

Nothing was broken. The formula was:

```python
expected_points = duration_minutes * 12   # ~1 reading per 5 seconds
coverage = min(len(bpm_values) / expected_points, 1.0)
```

It measures **volume**, and the cap is what makes volume a lie. The strap
delivers **one reading per second** — twelve times the assumed rate — so the
ratio only falls below 1.0 once **more than 11 readings in 12 are gone**. A
capture can lose eighty percent of a night and still be announced as
complete. The rate was assumed in 2026, the strap arrived later, and nothing
connected the two.

### The tell was on the same screen

The curve drew the hole as one long straight diagonal, because that is what
two readings 79 minutes apart look like when you join them. **The chart was
honest and the number was not**, side by side, in the same view. That is the
whole bug class in one screenshot: the drawing came from the data, the number
came from an assumption about the data.

It surfaced only because Felipe volunteered, unprompted, that 21:11 was a
brief test at home. Nothing in the app said so. No test failed. Nothing
would ever have failed — the formula was correct against its own premise.

### The fix is a change of question

`backend/app/services/data_quality.py` counts **5-second slots**: how many
of the window's slots hold at least one reading. 5 seconds because that is
the detector's own smoothing window — the finest resolution any answer
downstream depends on — and it is the same measure `Cadence` already
performs on the phone before an upload is offered.

One measure closes both failures. A hole empties its slots, so it shows.
A watch writing once a minute fills one slot in twelve and scores **8**,
which is the truth about what it can and cannot find. Volume alone buys
nothing: a thousand readings crammed into one minute of an hour describe one
minute. Realness now reads **78**, and the missing 22 is the hole.

Nine tests, the first of them the Realness capture at its real numbers, so
the case that exposed this is now the case that guards it.

### Restating the past

`analyze_session` recomputes the score from the stored rows on every
analysis. Old sessions are not migrated and not left stranded either: the
night corrects itself the next time someone opens it. **The Realness row
still says 100 until then** — one deploy, then "Procurar meus momentos" on
that night.

### What it costs to learn

Both numbers came from the same readings and only one of them was checked
against reality. **A derived number needs a case where it must be wrong,
and the test has to be written from a real capture, not from the formula.**
Nine tests written against that formula would all have passed.

---

## 2026-08-30 — the Realness night: six hours, twenty moments, and the number that settles the watch question

The festival capture ran. **21:11 → 03:17, six hours and six minutes, twenty
moments detected, Média 78 · Máx 116 · Mín 58.** The strap held, the upload
went through, the night opened, and the card button is sitting there. The
capture path is no longer "proven in a rehearsal" — it is proven at a real
six-hour event.

### The cross-validation nobody planned

The Fit3 was on the wrist the whole night with a workout running (22:28–03:17,
4h48). Two independent technologies, one body, one night:

| | Strap (chest, ECG-grade) | Fit3 (wrist, optical) |
|---|---|---|
| Maximum of the night | **116 bpm** | **115 bpm** |
| Time of that peak | **01:24** | **01:24** |

One bpm apart, same minute. That validates both instruments at once, and
settles that the 01:24 peak was real rather than an artefact.

### The finding that closes the watch question

The twenty moments the detector found have durations:

```
22 21 19 19 17 17 16 14 13 13 13 13 12 12 12 11 11 10 10 8   (seconds)
```

Median **13 s**, longest **22 s**. And the Fit3, in its best case — workout
running — samples once every **32 s**.

> **All twenty moments are shorter than the interval between two Fit3
> readings.**

| | Samples falling inside one moment |
|---|---|
| Strap | 8 to 22 |
| Fit3 with a workout | **0.25 to 0.69** |
| Fit3 without | 0.13 to 0.37 |

That is not an argument, it is arithmetic on measured data: a 13-second moment
has nowhere to exist in a series sampled every 32 seconds. **The watch path
delivers the curve of the night; the moments are too small for it.** The
morning's verdict was right; this is the reason it is right.

### Correcting the correction

The record is worth keeping honest. This morning I read only the hourly
background records, found 1/min, and concluded the workout changed nothing.
At midday the workout record showed ~32 s and peaks to 115, so I corrected to
"between the curve and the moments". Tonight the moment durations settle it
properly, back at the original verdict by a better road. **The first
conclusion was right for the wrong reason, which is not the same as being
right.**

### A defect Felipe found by asking a good question

He mentioned the session starts at 21:11 because he tested the strap briefly
at home, then connected for real at the venue around 22:30 — so the night
should contain a **79-minute hole**. The app reported **Qualidade 100%**.

The scoring formula assumes a full night is one reading every 5 s:

```
expected = duration_minutes × 12
coverage = min(actual / expected, 1.0)
```

The strap delivers one reading per *second* — twelve times the assumed rate —
so coverage saturates:

| | |
|---|---|
| 6h06 → "expected" | 4,392 points |
| Strap at 1 Hz | 21,960 points |
| With the 79-minute hole | ~17,220 points |
| Score, either way | **100%** |

**The strap can lose 80% of its readings and the app still says "Qualidade
100%".** This project's signature bug class again, with an extra twist worth
keeping: **the chart is honest and the number is not** — the curve draws the
gap as one long straight diagonal at the start, exactly where the data is
missing, while the score beside it claims perfection.

The fix is to measure *continuity* (how many minutes of the window contain a
reading) rather than *volume*. Not yet applied: it touches a deployed backend
and would restate the score on existing sessions, so it waits on Felipe's
word.

### Sitting made it the hard case, and it passed

Felipe spent most of the night seated. That is not a weakness in the test —
it is the strongest version of it. TumTum exists to capture emotion in a body
that is **not moving**, and stationary response is precisely the condition
where the J-Style V8 failed: its firmware clamps amplitude when the
accelerometer reads still. A seated night that still yields twenty moments
between 84 and 116 bpm, against a resting 58, is the core hypothesis holding
in the adverse condition rather than the flattering one. Dancing would have
made the peaks bigger and the detection easier.

The Fit3's agreement is worth the same reading: an optical wrist sensor on a
seated person is a hard case for optical, and it still landed within 1 bpm of
the chest strap.

### What the night did not produce

**The moments have no names.** The Realness has no timeline in the database,
so `116 bpm às 01:24` is a number without a story — and the card's whole point
is the story. Felipe's own memory of the night is, for now, the only source
for that timeline.

---

## 2026-08-29 — the cadence is one per minute, and that is the middle outcome

Tapping into a Health Connect record opens **Detalhes da entrada**, and the
samples are there after all. From the 03:00–03:59 record, sleeping, no
workout:

```
03:00 55  03:09 62  03:18 59  03:27 61
03:01 59  03:10 61  03:19 57  03:28 58
03:02 57  03:11 61  03:20 58  03:29 58
03:03 59  03:12 62  03:21 60  03:30 59
03:04 59  03:13 63  03:22 59  03:31 59
03:05 60  03:14 62  03:23 60  03:32 60
03:06 61  03:15 64  03:24 60
03:07 60  03:16 53  03:25 61
03:08 61  03:17 53  03:26 61
```

**One reading per minute, exactly, not a minute missed.** 33 consecutive
samples across 32 minutes.

| | |
|---|---|
| Measured cadence | **1 / 60 s** |
| Etapa 0 gate | 1 / 5 s |
| Feared worst case | 1 / 10 min |
| `Cadence.denseEnough` | **false** — median gap 60,000 ms, 5-second slot coverage 8.6% |

Ten times better than the nightmare, twelve times short of the detector.
Exactly the middle outcome `docs/health-connect-plan.md` anticipated, which
means the plan's branch applies rather than a redesign.

**What 1/min delivers, honestly split.** Six hours is 360 points — a true,
continuous, good-looking curve of the night, no pretence needed. What it does
not deliver is *moments*: a chill through a chorus lasts 30–60 seconds, so at
one sample a minute that peak yields **one sample, or none**, depending where
the minute falls. And the detector's own arithmetic says the same: its
5-second smoothing window would hold 0.08 of a point and its 300-second
baseline 5 points — no reliable standard deviation lives in five numbers. It
would run, and what it found would be noise wearing the name of a moment.

> **Corrected the same day — see the 30/08 entry.** Samsung writes *two kinds
> of record* for one night, and this reads only one of them. The hourly
> background records are 1/min; the **workout record** is one every ~32 s and
> carries peaks the hourly ones never saw (115 bpm against their 97). The
> conclusion below — that the workout changes nothing — was drawn from half
> the data. The final answer is in the 30/08 entry, and it agrees with this
> one's *verdict* for a different and better reason.

**So the workout stretch is now the decisive measurement of the project.**
The single remaining question is whether a hand-started workout lifts the Fit3
off 1/min. Yes and the onboarding sentence is promoted from advice to
requirement, and the path delivers moments. No and Health Connect delivers
*the curve*, not *the moments* — which changes the card's promise, not the
schedule, precisely as the plan said in §2.

Felipe runs it tonight at Realness: start a workout on the Fit3 when the show
starts, leave it running. The strap remains the night's real capture.

**Worth correcting from this morning:** the record view does expose its
samples, one tap deeper than I looked. The earlier note that only our app
could unpack them was wrong about the browser, right about everything else —
and question B got answered by hand after all.

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
