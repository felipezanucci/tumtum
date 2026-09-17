# Pilot event options — what replaces 2026-09-25

**Created:** 2026-09-17, after Felipe said the Tasha & Tracie date of 25/09
probably will not happen.
**Purpose:** choose the event (or events) that carry the concierge pilot, and
answer the question Felipe asked directly — *can the test run at a football
match?*

This document is the research. The decision and its reasoning live in
`docs/decision-log.md`.

---

## 1. What an event has to provide

Every requirement below is already recorded somewhere in this project. None of
them is new; what is new is applying them to a calendar.

| Requirement | Where it comes from |
|---|---|
| **The moments need a chest strap.** The watch path gives the curve of the night, not the moments: the 20 Realness moments lasted 8–22 s (median 13) and a Galaxy Fit3 samples once per ~32 s at best | Etapa 0, closed 2026-08-30 |
| **A timeline, or the moments have no names.** A peak matches a timeline entry only within **±60 s** (`event_correlator.py`) | Open item 22 |
| **The event must not cross midnight.** One date plus two bare times; "termina 03:00" cannot say it means the next day | Open item 18 |
| **2 h+ of real event timing**, 3–5 people, one carrying the reference strap | `path-2-roadmap.md`, Phase 5 gate |
| **The end-of-night upload is 1.33 MB in one request.** A congested venue network can refuse it; nothing is lost, the button can be pressed again later | Open item 15 |
| **An engaged fan base.** The pilot's real research question is not which card is prettiest but **which one someone actually sends to somebody** | Open item 25 |

The first line is the one that decides logistics: **the number of people with
real *moments* equals the number of chest straps, not the number of
participants.** Everyone else contributes the curve of their night. Today the
log knows of one strap.

---

## 2. The timeline problem, measured

This is the part that separates a concert from a match, so it is worth being
precise rather than impressionistic.

**Setlist.fm does not publish per-song timestamps — only song order.**
`parse_setlist_to_timeline()` therefore *estimates*: it starts at the event
start time and adds a flat 4 minutes per song. The error accumulates. One long
intro, one speech between songs, one guest appearance, and by the tenth song
the estimate is ten or more minutes away from the truth. Against a ±60 s
matching window, matching fails from roughly the third song onwards.

**API-Football publishes the minute of every goal and card.**
`parse_fixture_to_timeline()` converts that minute to a clock time as
`kickoff + elapsed minutes` — which ignores the ~15-minute half-time interval
and first-half stoppage time. **Every second-half event therefore lands 15–20
minutes before it really happened**, fifteen times outside the ±60 s window.
The first half is roughly right; the second half is wrong for every entry.

**Neither service is reachable from the app.** `setlist_service.py` and
`football_service.py` are imported by no route and no test — they are dead code
today. What exists and works is `POST /api/events/{id}/timeline`,
authenticated, one entry at a time.

So, in practice:

| | Concert | Football match |
|---|---|---|
| Entries needed for a full timeline | ~20–25 songs | 6–10 (kickoff, goals, cards, half-time, final whistle) |
| Are the timestamps objective? | No — nobody publishes them | Yes — the minute of each goal is published |
| Coverage of the moments that actually move a heart | **Complete** — every moment of a show is a song | **Partial** — the biggest spikes are often a missed penalty, a near-miss, a brawl, and no feed reports those |
| Cheapest correct method today | Someone marks song starts live, or a video with a visible clock | Type 6–10 entries from the match report, applying the half-time offset by hand |

**Football gives the cheaper and more objective timeline; a concert gives the
more complete one.** Both need a human, because the code that was supposed to
remove the human is wrong in one case and dead in both.

---

## 3. Concert candidates in São Paulo

Dates below come from public listings on 2026-09-17 and **every one of them has
to be confirmed on the ticket page before anything is organised** — round
splits, added dates and venue changes are routine. Sources at the end.

| Date | Event | Venue | Fan base | Ends before midnight? | Notes |
|---|---|---|---|---|---|
| **28, 30, 31/10** | **BTS — World Tour Arirang** | MorumBIS | The most organised fan base in the world, and a sharing culture that *is* the viral engine this product wants | Gates 16h, show 20h → yes | Sold out since April, price R$ 340–1.250. Only works through people who already hold tickets. Three nights = three chances. Fixed tour setlist, timings posted by fans in real time |
| **12 and 13/11** | Hayley Williams (Paramore) | Espaço Unimed | Sold out fast, both nights; a singalong crowd that films everything | Yes | The best mid-size option: dense emotion, known repertoire, indoor, ends before midnight |
| 08/11 | TAEMIN | Vibra São Paulo | K-pop; organised, high-sharing | Yes | |
| 29/11 | KARD | Audio | K-pop | Likely no (Audio starts late) | |
| 28/11 | BABYMETAL | Espaço Unimed | Intense, but a moshing crowd is the worst case for motion artefacts | Yes | |
| 23 and 25/11 | New Order | Espaço Unimed | One date sold out, a second was added — engaged, but an older crowd that posts less | Yes | |
| 10/10 | ZAYN | Nubank Parque (ex-Allianz) | Young, ex-1D, high sharing | Yes | Stadium |
| 13/10 | Robbie Williams | Nubank Parque | Broad, less social | Yes | |
| 30/10 | Jorja Smith | Espaço Unimed | | Yes | |
| 17/10 | Pitty | Espaço Unimed | Loyal, Brazilian, sings every word | Yes | |
| 10/10 | ZIG Festival | Pavilhão + Audio | The Realness crowd — queer, dancing, six hours | **No** | Same midnight problem as Realness |
| 05 and 06/12 | Primavera Sound | Autódromo de Interlagos | Huge, mixed | **No** | Two 12-hour days: battery and the midnight limit both bite |
| **06/11** | **Tasha & Tracie — Serena e Venus** | Arena Club, **Santos** | *The same show and the same fan base as the date being missed* | No (22h start) | 80 km from São Paulo. If the 25/09 choice was about a relationship rather than the venue, this is the same test six weeks later |

---

## 4. Football candidates in São Paulo

| Date | Match | Stadium | Why it is interesting | Access |
|---|---|---|---|---|
| **20/09, 16h** | Corinthians × Fluminense (28ª) | Neo Química Arena | **This Sunday.** Daylight, ends ~18h, no midnight problem at all | R$ 90 / 45, **online only via Fiel Torcedor, and the purchase requires facial biometrics already registered** — that is the blocker at three days' notice |
| **10/10, 21h** | São Paulo × Vitória (30ª) | MorumBIS | Ends ~23h, before midnight. Tickets are the easiest of the three big clubs | Open sale, spfcticket.net |
| **11/10, 17h30** | **Palmeiras × Corinthians** (30ª) | Nubank Parque | A derby: the highest arousal available in a league round | Hard — members first, visiting sector tiny |
| 17–19/10 | Corinthians at home (31ª, opponent to confirm) | Neo Química Arena | Comfortable date, ends before midnight | Fiel Torcedor |
| **20–22/10** | **Palmeiras × Fluminense — Libertadores semifinal, second leg** | Nubank Parque | The single most emotional sporting event available in São Paulo this year: a continental semifinal decided at home | Near-impossible; members and a queue |
| 28/10 | São Paulo × Corinthians (33ª) | Neo Química Arena | Another derby | Hard, and it collides with BTS at MorumBIS |

Round splits are confirmed by the CBF only a few weeks ahead and the sources
disagreed on two of these; confirm each one before committing.

---

## 5. Football versus concert — the honest evaluation

### What a match buys

1. **An objective, published timeline.** The minute of a goal is a fact, and
   45,000 people agree on it. Nothing in a concert has that property.
2. **A synchronised collective peak.** A goal spikes every heart in the stadium
   within the same two seconds. That is the cleanest possible demonstration
   that the product measures what it claims — and it is the only way to test
   card 04, *A galera*, which needs a valid collective sample.
3. **A story every Brazilian already understands.** "No gol do fulano aos 37'"
   needs no explanation; a song title needs the listener to know the artist.
   For a card meant to travel, that matters.
4. **It ends before midnight**, at 16h, 19h or 21h30 kick-offs — so open item
   18 never comes up.
5. **Tickets exist.** A league round is buyable at R$ 45–90 three days out. A
   sold-out concert is not.

### What a match costs

1. **It can be 0–0.** Then the timeline is kick-off, half-time and the final
   whistle, and the night's peaks have no names — exactly the Realness
   failure, repeated by choice.
2. **The strongest moments are not in any feed.** The missed penalty, the
   near-miss, the shove at the touchline. Someone has to note the clock time
   of anything that made the stand roar, or half the story is lost.
3. **It is short.** ~2 h including the walk in — it meets the Phase 5 gate,
   with no margin.
4. **Standing, jumping, arms up.** The chest strap does not care; anyone
   contributing only a watch curve will produce motion artefacts.
5. **Stadium cellular is the worst network in the city.** The 1.33 MB upload
   will probably fail at the whistle; it must be retried on the way home. That
   is survivable by design, but it has to be said out loud to five people
   before they leave.
6. **Entry friction.** Fiel Torcedor and facial biometrics at Corinthians;
   member priority at Palmeiras. Nobody joins the pilot on the day.

### What a concert buys that a match does not

The pilot's actual question is **which card gets sent to someone**, and that is
a question about a fan base, not about a sport. A K-pop or Paramore crowd is
the single most card-hungry audience available: they already film, already
post, already compare with each other. A football crowd shares a *result*; a
music crowd shares *their own night*, which is what TumTum sells.

### Verdict

**They are not the same test, and the right answer is to run both.**

- The **match** is the technical validation: objective timestamps, a
  synchronised peak, cheap tickets, an early date, no midnight problem. It
  answers *does the correlation hold in public, on more than one body*.
- The **concert** is the product validation: an engaged fan base and a night
  that belongs to the person. It answers *does anyone send the card*.

Running the match first is also the cheaper order: it is sooner, the tickets
are buyable, and everything it teaches about straps, uploads and timelines
makes the concert test better.

---

## 6. Recommendation

1. **Technical test — São Paulo × Vitória, 10/10, 21h, MorumBIS.** Three weeks
   out, buyable tickets, ends before midnight, a full stadium. If Felipe and
   two or three others already hold Fiel Torcedor registration with biometrics,
   **Corinthians × Fluminense this Sunday, 20/09, 16h** is faster and cheaper
   still — and it is *earlier* than the date being missed.
   The maximum-emotion version, if tickets can be found, is **Palmeiras ×
   Fluminense, Libertadores semifinal, 20–22/10, Nubank Parque**.
2. **Product test — BTS at MorumBIS, 28/30/31 October**, if three people who
   already hold tickets can be found. Nothing else in the calendar combines
   that scale of feeling with that appetite for posting. If not: **Hayley
   Williams, 12 or 13/11, Espaço Unimed**, or **Tasha & Tracie in Santos,
   06/11**, which is the same show that is being missed.

---

## 7. What has to be decided before any date is booked

1. **How many chest straps.** One strap means one person with moments and the
   rest with curves — and it means card 04, *A galera*, cannot be tested at
   all. A goal spiking three instrumented chests at the same second is the
   strongest demo this product can produce, and it needs three straps. Any
   strap that broadcasts the standard BLE Heart Rate Service (0x180D) works
   with the app as built; that is a purchase decision, not an engineering one.
2. **Who writes the timeline, and when.** For a match: 6–10 entries typed
   after the final whistle, with the half-time offset applied by hand. For a
   concert: someone marking song starts live. Neither is automated today, and
   `parse_fixture_to_timeline()` is wrong in the second half.
3. **Whether the event ends before midnight** (open item 18), which rules out
   Audio, ZIG and Primavera as they stand.
4. **Consent.** Health data, five people, in writing, before the day — the
   brand goes quiet and careful on exactly this screen.

---

## Sources

Public listings consulted on 2026-09-17. All dates need confirming on the
ticket page before use.

- [BTS World Tour Arirang — MorumBIS, ticket page](https://www.ticketmaster.com.br/event/bts-world-tour-arirang)
- [BTS em São Paulo 2026 — datas e ingressos](https://www.desbravasp.com.br/blog/datas/bts-sao-paulo-2026-shows-ingressos-datas)
- [Espaço Unimed — agenda de shows](https://www.espacounimed.com.br/agenda-de-shows/)
- [Vibra São Paulo — agenda](https://www.ticket360.com.br/locais/vibra-sao-paulo)
- [Audio — agenda](https://www.ticket360.com.br/locais/audio)
- [Allianz / Nubank Parque — agenda](https://allianzparque.com.br/category/agenda/)
- [Primavera Sound São Paulo 2026 — line-up e datas](https://portalpopline.com.br/primavera-sound-sao-paulo-2026-line-up-por-dia-e-valores-ingressos/)
- [Tasha & Tracie — Serena e Venus, São Paulo 25/09](https://www.ticket360.com.br/evento/33412/ingressos-para-tasha-e-tracie-apresentam-serena-e-venus-a-experiencia)
- [Tasha & Tracie — agenda da turnê](https://www.songkick.com/artists/10184385-tasha-and-tracie)
- [Ingressos: Corinthians x Fluminense, 20/09](https://www.corinthians.com.br/noticias/ingressos-corinthians-x-fluminense-20-09-brasileirao-2026)
- [CBF detalha rodadas 27 a 30 do Brasileirão](https://www.cbf.com.br/futebol-brasileiro/noticias/campeonato-brasileiro-serie-a/a/cbf-detalha-rodadas-27-a-30-do-brasileirao-betano)
- [Semifinais da Libertadores 2026 — duelos e programação](https://www.olympics.com/pt/noticias/copa-libertadores-2026-semifinais-duelos-programacao)
- [Palmeiras x Fluminense — datas-base e mandos da semifinal](https://nossopalestra.com.br/noticias/libertadores-da-america/palmeiras-x-fluminense-veja-datas-base-mandos-e-caminho-ate-final-da-libertadores)
- [São Paulo FC — próximos jogos e ingressos](https://www.spfcticket.net/proximos-jogos/)
