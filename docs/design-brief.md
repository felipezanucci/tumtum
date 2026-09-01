# TumTum — design brief for Claude Design

**Paste this whole file before asking for any TumTum design work.** It is the
working context: what the brand is, what it must never become, the palette and
type, and the actual screens that need designing — with real data to build
them from.

Source of truth: **TUMTUM Manual de Marca, MVP v0.4 (2026-08-31)**. Where this
file is thinner than the manual, the manual wins. Where they conflict, the
manual wins. Hierarchy: (1) the founder's most recent explicit decision,
(2) the manual's LOCKED rules, (3) the original brief, (4) WORKING
recommendations, (5) old moodboards and old AI generations — which are not
sources of truth at all.

**v0.4 changed the palette.** Acid Lime `#C6FF00` is gone. Any earlier TumTum
artwork, screenshot, or export you may be shown is historical: rebuild it in
the new palette rather than matching it.

---

## 1. What TumTum is — and the one mistake that ruins everything

TumTum captures how a person reacted during a **concert or a football match**,
connects that reaction to what was happening at that exact instant, and turns
the result into something they want to post.

> **The promise:** TumTum turns what you felt into a story you can see, prove
> and share.
>
> **The second the brand exists for:** *"Eu sabia que aquela hora foi diferente
> — e agora eu tenho a prova."*

The category is **emotional memory + culture + data**. It is measured with a
heart rate, and that is the trap:

> ### TumTum is NOT healthtech, fitness, wellness, sleep, recovery, or medical software.
> This is the single failure mode that destroys the brand, and it creeps in
> through small decisions — a chart that looks clinical, a word like "zone", a
> reassuring tone, a progress ring. If a design could sit inside a fitness app
> without anyone noticing, it is wrong.

**Audience:** 18–34, São Paulo metro area, 6+ events a year, already owns a
smartwatch, posts Stories at shows, keeps the ticket stub. They do not want a
health insight. They want a story.

Three tribes, in priority order: **1) Fandom** (Swifties, coldplayers, K-pop,
sertanejo), **2) Torcida** (Palmeiras, Corinthians, São Paulo, Flamengo),
**3) Festivaleiros** (Lollapalooza, Rock in Rio, raves).

**Explicitly not the audience:** anyone using Whoop/Oura/Garmin/Strava to
optimise their body, anyone wanting a medical reading, anyone expecting live
intervention during the event.

**Design tiebreaker:** forced to choose between looking *trustworthy* and
looking *fun*, choose **fun** — except on health-data permission, privacy and
consent screens, where the brand goes quiet and careful.

**Emotional temperature:** night, sweat, crowd, noise, tired, dark, packed.
Forty thousand people around you and a result that is unmistakably yours.

---

## 2. Hard NEVERs

Read these before anything else. They are LOCKED.

1. **Never redraw, imitate, stretch, slant, compress or regenerate the TUMTUM
   logo.** It is a locked vector asset. See §5 — this constrains you directly.
2. **Never use ECG, heartbeat or waveform as decoration** — not as identity,
   pattern, divider, background, loader, or generic motion. (A real BPM-over-
   time line is allowed under strict conditions; see §8.)
3. **Never make TumTum look like healthtech, fitness or wellness.**
4. **Never set a flat coloured wordmark.** The logo is black, white, or an
   approved skin. Pink and Yellow wordmarks are forbidden.
5. **Never put white text on Pink or Yellow** — 2.65:1 and 1.11:1.
6. **Never add heart zones, risk colours, "normal ranges", recovery scores or
   any diagnostic framing.**
7. **Never drop a club crest, artist logo or tour identity inside the
   wordmark.** Interpret the culture; do not dress the logo as someone else's
   brand.
8. **No mascot.** Deferred for the MVP. Do not invent one.

---

## 3. Colour — two neutrals and a proprietary pop duo

| Token | Hex | Role |
|---|---|---|
| Black | `#000000` | Primary canvas and text |
| White | `#FFFFFF` | Neutral / inverse / breathing room |
| **TumTum Pink** | **`#FF6F91`** | **Primary accent — the chromatic signature** |
| Toxic Yellow | `#EFFF00` | Secondary accent — energy, labels, CTAs, markers |

- Black and white are the structural neutrals.
- **TumTum Pink is the lead colour** and may occupy **large surfaces** — full
  backgrounds, colour fields, big areas. This is a change in kind from the
  previous palette, where the accent was a small acid highlight.
- Toxic Yellow works best on **labels, CTAs, highlights and data markers** —
  smaller, sharper, a burst rather than a field.
- Working balance in accent use: roughly **70% Pink / 30% Yellow**. A balance,
  not a layout quota.
- **On Pink or Yellow surfaces, set the logo and type in black.**
- Mutations may introduce other colours and materials; this palette stays the
  institutional anchor.

### Contrast — measured, not estimated

| Pair | Ratio | Verdict |
|---|---|---|
| White on Black | 21.0:1 | Excellent |
| Black on Toxic Yellow | 18.97:1 | Excellent — preferred |
| Toxic Yellow on Black | 18.97:1 | Excellent |
| Black on TumTum Pink | 7.93:1 | Excellent — preferred for logo and text |
| **TumTum Pink on Black** | **7.93:1** | **Passes AA everywhere, AAA for large text** |
| White on TumTum Pink | 2.65:1 | **Do not use for functional text** — fails even large-text AA |
| White on Toxic Yellow | 1.11:1 | **Never** |
| Pink on Toxic Yellow | 2.39:1 | **Never as a text/background pair** |

> **A consequence worth designing around.** The old accent sat at 17.7:1 on
> black; Pink sits at 7.93:1. Still comfortably legible, but roughly half as
> loud. Pink emphasis on a black canvas no longer *screams* on its own — it
> needs **scale** to carry the same weight. Push size and weight rather than
> reaching for a brighter tint, and use Toxic Yellow when something genuinely
> needs to cut through.

### Default digital behaviour

Pink is the main chromatic signature and can take large surfaces. Black is the
main contrast. Toxic Yellow arrives as label, CTA, marker and burst of energy.
White is the neutral and the breathing room.

---

## 4. Typography

Two families. **Do not add a third.**

| Role | Face | Rule |
|---|---|---|
| Logo only | **Chosmos** | The TUMTUM wordmark, uppercase. Never in UI, body or marketing. |
| Hero numbers | Instrument Sans **Bold 700** | Big numeric moments (`187`). Impact through scale and weight, never a third display face. |
| Headlines | Instrument Sans **Semibold 600 / Bold 700** | Share card copy, landing, OOH. |
| UI labels | Instrument Sans **Medium 500 / Semibold 600** | Buttons, navigation, labels, metadata. |
| Body | Instrument Sans **Regular 400** | Long text, help, legal, support content. |
| Tabular data | Instrument Sans, **tabular figures** | Timelines, comparisons, aligned metrics. |

Instrument Sans is SIL OFL 1.1 and available on Google Fonts. It is precise
enough for product and data but has enough personality to keep TumTum from
looking like generic SaaS.

---

## 5. The logo — a locked asset, and a legal constraint on you specifically

The wordmark reads **TUMTUM**, uppercase, set in Chosmos (working master:
Chosmos Regular). The earlier "wide first TUM / narrow second TUM" experiment
was **explicitly abandoned** — there is no cadence between the two halves.

**Approved institutional versions:** black wordmark, white wordmark, or an
approved skin/texture. **Nothing else.**

> ### This constrains you directly
> **Do not render the word "TUMTUM" in a substitute typeface and present it as
> the logo.** Chosmos may not be uploaded to or imitated by an AI tool — the
> Typozon EULA (v3.4) carries an explicit AI/machine-learning restriction, so
> this is a legal boundary, not a style preference.
>
> In mockups, place the **official PNG/SVG asset** (the repo has
> `backend/app/assets/brand/tumtum-wordmark-white.png`), or leave a clearly
> marked placeholder rectangle at the right proportions. A lookalike rendered
> from another font is worse than no logo at all, because it gets mistaken for
> approved artwork later.

**In shareable content the number is the hero. TumTum signs; it never
dominates.** The logo is small and is never the main information.

Still open (do not invent and call it a rule): final kerning, clear-space
metric, minimum size, app icon/favicon, any reduced monogram.

---

## 6. Voice & copy

> If the line would fit in a WhatsApp group at two in the morning, it probably
> fits TumTum.

| Rule | How it sounds |
|---|---|
| Feeling, not health | *batida, momento, arrepio, sentiu*. No clinical physiology. |
| Short as a shout | Nobody reads a paragraph in a crowd or scrolling Stories. |
| **The person is the subject** | *"Seu coração foi a 187."* — never *"Detectamos um pico de 187."* |
| Brazilian Portuguese | *"a gente", "tá", "tava"* where natural. No caricature. |
| Zero medical advice | No diagnosis, reassurance, alarm, or normal/abnormal framing. |
| Humour | Exaggeration, self-awareness, complicity. **Never at the user's expense.** |

**Prefer:** batida, momento, noite, galera, junto, sentiu, arrepio, vibe.
**Use carefully:** coração, BPM — neutral units, never wrapped in a clinical
reading.
**Avoid:** *frequência cardíaca* as marketing language, resposta fisiológica,
zona, recuperação, performance, diagnóstico.

### Approved copy bank — use these verbatim in mockups

**Personal moment**
- `EU TAVA TRANQUILO. / AÍ VEIO ISSO.` ← the MVP hero line
- `Foi aqui que eu perdi a compostura.`
- `Até aqui eu tava me comportando.`
- `Esse foi o exato momento do surto.`
- `Eu achei que tava de boa. O dado discorda.`
- `Nada discreto da minha parte.`
- `Aqui acabou meu psicológico.`
- `Não respondo pelo que aconteceu depois.`
- `Meu corpo entregou tudo.`
- `Provas de que eu senti demais.`

**Community**
- `NINGUÉM TAVA TRANQUILO. / APARENTEMENTE FOI COLETIVO.`
- `A galera inteira perdeu a compostura.`
- `A torcida também sentiu.`

**Artist / player comparison**
- `NA MESMA VIBE. / LITERALMENTE.`
- `Vocês sentiram essa juntos.`
- `Você sentiu mais que quem tava no palco.`

**Empty and error states**
- `Nenhum show ainda. Seu coração tá de folga.`
- `Não achamos batida nesse horário. O relógio tava no pulso?`

**Never sound like this**
- ~~"Detectamos um pico de frequência cardíaca de 187 BPM."~~
- ~~"Você atingiu uma zona cardíaca intensa."~~
- ~~"Seu resultado está acima do normal."~~
- ~~"Monitore sua recuperação após o evento."~~
- ~~"Parabéns por alcançar sua meta cardíaca."~~

---

## 7. Visual territory: Mutante Pop

**The shape stays. The skin changes.** One recognisable structure that survives
thousands of appearances. The MTV reference is conceptual, not nostalgic: a
stable brand that accepts radical changes of colour, texture and material.

> **NEVER default to "1980s MTV".** No automatic Memphis, retro pastiche,
> cyan-magenta nostalgia, or direct MTV imitation.

Ten skins in the MVP kit — **Base/Solid, Zebra, Neon, Cromado, Inflável,
Glitter, Iridescente, Pelúcia, Plástico, Borracha**. Mutation is **surface
treatment, never redesign**: same letters, same silhouette, same proportions.

Each event or night can be a visually different TumTum and still be
recognisable. The mutation should feel cultural, collectible and social — not
a generic filter pack.

---

## 8. Data visualisation — the sharpest line in the manual

> The heart-rate **data** may appear. The heart-rate **aesthetic** may never
> become the brand.

**Allowed:** a simple BPM-over-time line, when it is the user's **real data**
and the chart is **product information** — specifically in "Minha noite" and
in community/comparison contexts where the line adds evidence.

**Rules for the chart:**
- Simple BPM over time. **Never an ECG trace.**
- Mark time clearly and highlight the relevant moment.
- **TumTum Pink** for the main emphasis on dark surfaces; **Toxic Yellow** for
  highlight points and labels. White/grey may carry secondary series.
- No heart zones, risk colours, normal ranges, recovery scores, diagnosis.
- The chart answers ***"when did this happen?"*** — never *"what does this mean
  clinically?"*

**A rule the product already enforces, and the design must respect:** when a
capture has a gap, **the line breaks**. It is never interpolated across
minutes nobody measured. If you draw a night with missing data, draw the hole.

---

## 9. The share cards — five narratives, one moment

All five are born from the **same moment object** (event, timestamp, user
value, context, media, timeline, community and artist data where they exist).
These are not five disconnected products.

| # | Name | Internal | Available when | Hero copy |
|---|---|---|---|---|
| 01 | **Só o momento** | MINIMAL | **Always** — the universal default | `EU TAVA TRANQUILO. / AÍ VEIO ISSO.` |
| 02 | **Ver o momento** | MOMENT | Licensed event photo/video matched to the moment | same |
| 03 | **Minha noite** | TIMELINE | The time series is good enough | same |
| 04 | **A galera** | COMMUNITY | A statistically and privately valid cohort exists | `NINGUÉM TAVA TRANQUILO. / APARENTEMENTE FOI COLETIVO.` |
| 05 | **Na mesma vibe** | SYNC | Explicit artist/athlete participation + validated sync | `NA MESMA VIBE. / LITERALMENTE.` |

**Format:** designed for **9:16 first** (1080×1920 Story). Feed (1080×1080) and
link preview (1200×630) are separate layouts, not the portrait one squeezed.

**Story safe areas:** the top ~250px and bottom ~340px sit under the host app's
profile header and reply bar. Anything placed there is simply not seen.

**The choice screen:** after the reveal, show **visual previews** — "Escolha
como compartilhar". The preview matters more than the label. **Do not show
locked formats**; show only the 2–5 versions this moment can actually produce,
ordered by rarity and emotional value.

### The two-second test

Someone scrolling must understand **number + moment** almost immediately.
Motion may reward attention after that, but must never delay comprehension.

---

## 10. Product & UI rules

| Area | Rule |
|---|---|
| UI face | Instrument Sans |
| Default surfaces | Black or white. High contrast, simple, **mobile-first**. |
| Primary accent | TumTum Pink — selected states, key numbers, main emphasis |
| Secondary accent | Toxic Yellow — secondary emphasis, burst, highlight |
| Hero data | Instrument Sans Bold at large scale. **Number first, context after.** |
| Permissions / privacy | **Personality turned down.** Clear, calm, respectful. No joke about consent. |
| Empty / error | Short, human, lightly self-mocking. Never blame the person, never imply a medical problem. |

> **There is no complete component library yet, and that is deliberate.** Do
> not invent a radius scale, spacing system or permanent iconography and call
> it a brand rule. Those are product design decisions still to be made — make
> them consciously and label them as proposals, not as brand law.

---

## 11. The screens that need design

These exist in code today. Most were built for function, not looks — the
capture path is proven at a real six-hour festival, and the interface around it
has never had a design pass.

### The core loop (highest value)

| Screen | What it does | State |
|---|---|---|
| **A noite** (`/experience`, Android `NightActivity`) | The reveal. HR curve for the whole night + the detected moments, drawn on a canvas. **This is the emotional payoff of the entire product.** | Works, never designed |
| **O card** (`/cards`, Android `CardActivity`) | Preview of the generated share card + the system share sheet | Works. Card 01 recently reworked: leads with the night's highest peak, copy generated from the night's own numbers, curve as evidence. **Its surface is still undesigned** — cover the wordmark and it is a black field with a coloured number. |
| **Escolha como compartilhar** | The five-format preview picker described in §9 | **Does not exist yet** |
| **Suas sessões** (`/sessions`) | List of captured nights | Works, plain |
| **Captura ao vivo** (`/live`, Android `MainActivity`) | The capture screen itself — running, connected, elapsed | Works, plain |

### Getting in

| Screen | Notes |
|---|---|
| **Landing** (`/`) | Public, live on **tumtum.cc**. Information + sales + a working waitlist (email only). |
| **Login / signup / esqueci-senha / redefinir-senha** | Exist. Note: **the landing page has no sign-in link** — an open product problem. |
| **Onboarding** | Exists, minimal |
| **Trazer do meu relógio** (Android `WatchImportActivity`) | Health Connect import: a corridor of permission gates, then a per-source density measurement |
| **Permissão de dados de saúde** (Android `PrivacyRationaleActivity`) | **The quiet screen.** This is where the brand's personality is deliberately turned down. |

### Around the loop

`/events` (list, new, detail, edit) · `/profile` · `/import` (Polar CSV) ·
`/[username]` public profile · `/cards/[id]` public card page ·
`/admin/waitlist`

---

## 12. Real data — use this, never lorem

Build every mockup from the **Realness Festival capture, 2026-08-29**. It is a
real six-hour night from a real person, and using it keeps the layouts honest
about how much room actual values need.

```
Event      Realness Festival 2026 — São Paulo, drag festival
Date       29/08/2026
Window     21:11 → 03:17  (6h06)
Average    78 bpm
Maximum    116 bpm at 01h24
Minimum    58 bpm
Moments    20 detected, each lasting 8–22 seconds (median 13)
Quality    78%  (a 79-minute gap at the start — the strap was connected
                 briefly at home, then again at the venue)
User       Felipe Zanucci
```

**Moments have no names yet.** There are no timeline rows for this event, so a
moment currently reads `116 bpm às 01h24` with no story attached. Designing the
named case (`durante "Vogue"`) is worth doing — but the **unnamed case must
also look finished**, because today that is the only case that exists.

Other realistic numbers if you need variety: a peak of `187` (the manual's own
example), `142`, `98`. Event names: `Coldplay — Allianz Parque`,
`Palmeiras x Corinthians`, `Lollapalooza — Dia 2`.

---

## 13. QA checklist — run before delivering

- [ ] A fan would post this without feeling they are posting an advert.
- [ ] A major artist, club or festival could appear beside it without TumTum looking amateur.
- [ ] Five different nights could look obviously different and obviously TumTum.
- [ ] Nobody would mistake this for fitness, sleep, wellness or medical software.
- [ ] It reads as fun and lightly self-mocking, not solemn.
- [ ] The logo is the approved asset — not an AI approximation or a font lookalike.
- [ ] The number/data is the hero of the share card.
- [ ] No decorative ECG or waveform got in.
- [ ] Consent and privacy screens are calmer than the shareable surfaces.
- [ ] It works on a small screen and communicates fast.
- [ ] Any community statistic states exactly what it measures.
- [ ] No suggestion that raw BPM means the same thing across different bodies.

---

## 14. Open — do not invent a permanent answer

App icon and favicon · reduced logo / monogram · final logo vector, kerning,
clear space and minimum size · motion timings and easing · the community metric
methodology · the "Na mesma vibe" sync score · final UX naming of the five
cards · the component library (radius, spacing, icons).

Proposals are welcome and useful. Proposals presented as settled brand rules
are not — this project's recurring failure is a document or a screen stating
something false with confidence.
