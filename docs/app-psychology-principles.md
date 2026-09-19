# What makes an app feel great — the principles, the benchmark, and where TumTum stands

**Asked for by Felipe, 2026-09-19**, after watching Wyatt Feaster's *"The
psychology trick that makes any app feel 10x better"* (YouTube `MVOpq3WRnbo`,
~6 min, ~84k views): *"faça uma busca completa em todos os princípios que
tornam apps incríveis… e cheque top apps como benchmark."*

**What this document is.** A sourced catalogue of the behavioural principles
behind apps that feel good (Part 2), a benchmark of how the best consumer apps
actually use them, with published numbers where they exist (Part 3), an audit
of TumTum's own loop screen by screen against that catalogue (Part 4), and a
ranked list of what to borrow, what to refuse, and how to measure it in the
pilot (Parts 5–7). It is a research document; it decides nothing by itself.

**What it is not.** Nothing here overrides the brand manual (v0.4) or
`docs/design-brief.md`. Where a mechanic that works for Duolingo would make
TumTum look like healthtech, or joke at the user, or state something false
about its own state, the answer is already no — Part 6 lists those refusals so
nobody has to re-argue them.

**Sourcing.** The video itself could not be watched from this environment
(youtube.com and every transcript mirror are blocked by the network policy; no
transcript or write-up of it is indexed anywhere). Its three named
principles — goal gradient, labor illusion, choice overload — come from the
YouTube summary Felipe sent. Every principle and benchmark below carries its
origin paper or primary source; numbers marked *reported* come from secondary
coverage and should be checked against the primary before being quoted in
public. Where a study failed to replicate, that is written next to it,
because a principle that does not survive replication is not a rule to build
on.

---

## 1. The three from the video, in TumTum terms

The video's spine is the standard trio of behavioural UX: **show progress,
show work, show fewer options.** Each is real, each has a number behind it,
and each already has a place in TumTum — one by accident, one missing, one
done right.

| Principle | The study | What TumTum does today | The gap |
|---|---|---|---|
| **Goal gradient** — effort rises as the goal nears; a head start makes people finish (Hull 1932; Kivetz, Urminsky & Zheng 2006: a 12-stamp card with 2 stamps pre-filled was completed in 12.7 days vs 15.6 for a 10-stamp card) | Kivetz 2006, *JMR* | The gallery counts NOITES / MOMENTOS / SEU RECORDE from zero. The capture shows "TOCANDO HÁ" and a sample count. The locked night says "abre às 10h". | The one progress bar a fan has — *install → account → permission → watch → first event → first reveal* — is invisible, and the first reveal is days or weeks away. Nothing tells the person how close the first night is. |
| **Labor illusion** — people value a result more when they see the work, and prefer a slower result with visible effort to an instant one (Buell & Norton 2011; Uber's step-by-step matching copy cut post-request cancellations 11%) | Buell & Norton 2011, *Management Science*; Uber 2018 | The reveal draws the curve left to right in 1.2 s, then fades the peaks in. The server's real work — upload, detection, matching to the timeline — is a one-line grey caption under the moments. | The pipeline genuinely does three things. Showing them ("lendo sua noite… procurando os momentos… casando com o que tocava") is honest operational transparency, not a fake delay, and the research says it raises the value of what comes out. |
| **Choice overload** — beyond a handful, options reduce both choosing and satisfaction, *when* the options are similar and the person is unsure (Iyengar & Lepper 2000: 24 jams, 3% bought; 6 jams, 30%; Chernev 2015 meta-analysis: real but conditional) | Iyengar & Lepper 2000, *JPSP* | Four skins with visual previews; three moments shown of twenty; one CTA on the reveal; a single reaction (SENTI TB) in the feed; the brief's rule "show only the formats this moment can produce". | Already right where the fan is. Where overload actually lives is Configurações — operator toggles beside fan settings, with a string that tells the fan to ignore half the screen. |

---

## 2. The catalogue

Forty-odd principles, grouped by what they act on. Each block: what it is,
where it comes from and the number if there is one, one app that does it
mechanically, when it backfires, and the TumTum line. Sources are the
primary paper where one exists, then the best secondary write-up.

### 2.1 Motivation and progress

**Goal gradient effect.** Effort accelerates near a goal. Hull 1932 (rats ran
faster nearer the food); Kivetz, Urminsky & Zheng, *JMR* 2006 (the coffee
card: 12 stamps with 2 given completed 20% faster than 10 blank; effort
collapses right after the reward — the *post-reward reset*). Apps: LinkedIn's
profile-strength meter; iFood's "faltam R$ 12 para frete grátis". Backfires
when the progress is invented (a bar at 30% for no reason) or the goal was
never the person's. *TumTum:* the only honest goal is the fan's own — the
next show, the first reveal, the season count. Make that visible; never
invent a bar. Sources: [Kivetz 2006](https://journals.sagepub.com/doi/10.1509/jmkr.43.1.39) · [NN/g](https://www.nngroup.com/articles/goal-gradient/).

**Endowed progress effect.** A head start raises completion. Nunes & Drèze,
*JCR* 2006 (car-wash cards: 8 blank stamps → 19% completed; 10 with 2
pre-punched → 34%, and faster; stronger when the endowment has a reason).
Apps: Duolingo's bar partly filled after the first tap; TurboTax "15% done"
after a name. Backfires when transparently unearned. *TumTum:* the onboarding
already has an earned first step (the account, "leva menos que uma música");
a step list with that step ticked is honest endowed progress. Source: [Nunes & Drèze 2006](https://www.researchgate.net/publication/23547282_The_Endowed_Progress_Effect_How_Artificial_Advancement_Increases_Effort).

**Zeigarnik and Ovsiankina effects (open loops).** Unfinished tasks stay
active; interrupted tasks pull to be resumed. Zeigarnik 1927; Ovsiankina 1928.
*Replication note:* a 2025 meta-analysis (Nature *HSSC*) finds no reliable
memory advantage (Zeigarnik "lacks universal validity") but a robust
resumption tendency, 67% weighted. Apps: Netflix "Continue watching"; "3/5
collected". Backfires as unread dots and manufactured cliffhangers (named in
the European Parliament's Nov 2025 addictive-design resolution). *TumTum:*
the reveal lock *is* an open loop the fan opened themselves — a night
recorded and not yet seen. That is the legitimate kind. Sources: [2025 meta-analysis](https://www.nature.com/articles/s41599-025-05000-w) · [Ovsiankina](https://en.wikipedia.org/wiki/Ovsiankina_effect).

**Commitment and consistency; streaks.** People act to stay consistent with
a commitment (Cialdini 1984); a streak turns consistency into a losable
asset. Duolingo reports Day-7 retention +14% from streak features
(*reported*); the SEC 10-K for FY2025 puts ~43M DAU on a 7-day+ streak and
~15M on 365+. Snapchat streaks are now litigated in six US states;
California banned addictive features for under-16s on 10 Sept 2026.
*TumTum:* **no streak.** Six events a year is not a daily habit and a
streak would lie about the product. The commitment mechanic that fits is the
marked event — "Palmeiras x Corinthians, sábado" — a promise the fan made to
themselves. Sources: [Duolingo blog](https://blog.duolingo.com/how-streaks-keep-duolingo-learners-committed-to-their-language-goals/) · [Smashing, streak design 2026](https://www.smashingmagazine.com/2026/02/designing-streak-system-ux-psychology/) · [Pennsylvania v. Snap, Aug 2026](https://www.techrepublic.com/article/news-pennsylvania-snapchat-lawsuit-teen-safety/).

**Loss aversion.** Losses weigh about twice gains (Kahneman & Tversky 1979,
λ ≈ 2; replicated across 19 countries, Ruggeri 2020). Apps: "your streak
will be lost in 2 hours"; "close your rings — 4 hours left". This is the
engine of most dark patterns (countdowns, "only 2 left", confirmshaming) and
the FTC (2022) and DSA Art. 25 name false urgency explicitly. *TumTum:* the
one true loss is a night not captured — the show will not happen again. Say
that once, before the event, calmly; never manufacture it. Sources: [Prospect theory](https://www.econometricsociety.org/publications/econometrica/1979/03/01/prospect-theory-analysis-decision-under-risk) · [FTC dark patterns](https://www.ftc.gov/reports/bringing-dark-patterns-light).

**Sunk cost.** Investment already made keeps people investing (Arkes & Blumer
1985: full-price theatre subscribers attended more plays). Apps: lifetime
XP, cumulative kilometres, "your collection". Backfires as the roach-motel
cancel flow. *TumTum:* the gallery is stored value that grows with every
night; keep it exportable (it already is — the ZIP) so it never becomes a
hostage. Source: [Arkes & Blumer 1985](https://www.sciencedirect.com/science/article/abs/pii/0749597885900494).

**Fresh start effect.** Temporal landmarks (new year, new season, a
birthday) spike aspirational behaviour (Dai, Milkman & Riis, *Management
Science* 2014). Apps: monthly challenges resetting on the 1st; Wrapped as a
year-end landmark. Backfires when a reset punishes someone who was ahead.
*TumTum:* the football season, the festival calendar and the year-end
"retrospectiva" are natural landmarks; a *season* count is honest where a
streak is not. Source: [Dai 2014](https://pubsonline.informs.org/doi/10.1287/mnsc.2014.1901).

**Self-determination theory.** Motivation lasts when autonomy, competence
and relatedness are met (Deci & Ryan 1985/2000); tangible rewards on a task
people already like *undermine* intrinsic motivation (128-study
meta-analysis, Deci, Koestner & Ryan 1999). Apps: Apple Watch lets you set
your own Move goal; Strava clubs. Backfires when points and leagues are
bolted onto a loved activity. *TumTum:* the fan already loves the show.
Points for attending would crowd that out. The test for any feature: would
it still feel good with the reward removed? Source: [Ryan & Deci 2000](https://selfdeterminationtheory.org/SDT/documents/2000_RyanDeci_SDT.pdf).

**Flow.** Absorption when challenge and skill balance and feedback is
immediate (Csikszentmihalyi 1975/1990). Engineered without a goal it becomes
the "machine zone" (Schüll 2012) — infinite scroll, autoplay, the targets of
California SB 976. *TumTum:* the fan's flow is the show itself; the app's
job during it is to disappear ("A gente só olha depois. Aproveita o show").

**Fogg behaviour model (B = MAP).** A behaviour happens when motivation,
ability and a prompt meet (Fogg 2009). Prompts fired at people with no
motivation are nagging. *TumTum:* motivation is highest in the hours after
the show; ability is one tap; the prompt is the one notification the product
is allowed — "sua noite abriu". Source: [Fogg model](https://thedecisionlab.com/reference-guide/psychology/fogg-behavior-model).

**Hook model.** Trigger → action → variable reward → investment (Eyal 2014).
Its critics (Chou; the US social-media-addiction MDL, 3,000+ cases) describe
the same loop as the defect. *TumTum:* the loop is event-paced, not
daily; the investment step is real (the night, the card, the gallery) and
the trigger is the calendar, not a push.

**Variable reward.** Unpredictable rewards produce the steadiest behaviour
(Ferster & Skinner 1957; dopamine fires to *unpredicted* reward, Schultz
1997). Pull-to-refresh is a lever pull. Loot boxes are banned for minors in
Brazil's ECA Digital (Lei 15.211/2025). *TumTum:* the variable reward is
built into the product honestly — nobody knows what their night will show
until they see it. Do not add a second one on top.

**Curiosity gap.** Curiosity is the discomfort of a gap between what you
know and what you want to know; it peaks at moderate gaps (Loewenstein
1994). Apps: Wrapped's "your 2025 is ready" envelope; Stories' unviewed
ring. Backfires as clickbait and blurred paywalls. *TumTum:* the locked
night is a curiosity gap of exactly the right size — the fan knows they were
there and knows the number exists. The copy "Sem espiar — é parte da
mágica" is the right register. Source: [Loewenstein 1994](https://www.researchgate.net/publication/232440476_The_Psychology_of_Curiosity_A_Review_and_Reinterpretation).

**IKEA effect.** People value what they built, if they finished it (Norton,
Mochon & Ariely 2012: builders paid 63% more for their own assembly; the
effect vanishes when the task is not completed). Apps: Bitmoji; playlists.
*TumTum:* choosing the skin and, later, naming the moment or adding a photo
are small completable acts of authorship that make the card *theirs* — and
a card that is theirs gets posted. Source: [Norton 2012](https://www.hbs.edu/ris/Publication%20Files/11-091.pdf).

**Endowment effect.** Owning raises value (Kahneman, Knetsch & Thaler 1990:
mug owners demanded ~2× what buyers offered). *TumTum:* "Sua galeria" and
"Suas noites" are the right words; ownership is real because the data is
exportable and deletable.

**Effort heuristic.** Quality is judged by the effort believed to have gone
in (Kruger et al. 2004; mixed replication, Ziano 2023). It is the basis of
the labor illusion. *TumTum:* "lemos 21.960 batidas para achar seus 20
momentos" is a true sentence that raises the value of the reveal.

### 2.2 Perception and memory

**Peak–end rule.** An experience is remembered by its most intense moment
and its ending, not its average or length (Kahneman, Fredrickson, Schreiber
& Redelmeier 1993 — the cold-water study; Redelmeier & Kahneman 1996 —
colonoscopy duration correlated 0.03 with remembered pain). Apps: Duolingo's
end-of-lesson burst; Mailchimp's high-five after Send; Uber's trip-end
screen. Backfires when the last screen is a paywall or a rating nag.
*TumTum:* **the product is literally a peak-end machine** — it finds the
peak and hands it back. Its own ending, though, is Android's share sheet.
The last screen a fan sees after posting should be TumTum's, not the
system's. Sources: [1993 paper](https://en.wikipedia.org/wiki/Peak%E2%80%93end_rule) · [colonoscopy study](https://www.amherst.edu/system/files/media/0678/colonoscopy%202.pdf).

**Duration neglect.** Length barely affects evaluation afterwards
(Fredrickson & Kahneman 1993). Wrapped compresses a year into ~90 seconds
with one peak. *TumTum:* a six-hour night becomes one number and one
moment. That compression is the product, not a loss.

**Labor illusion.** Shown work raises perceived value; people prefer a
slower result with visible effort to an instant identical one (Buell &
Norton 2011, five experiments in travel and dating search; mediated by
perceived effort and reciprocity; ceiling around a minute; fails when the
outcome is poor). Apps: Kayak's airline logos ticking past; TurboTax's
deliberate "checking every deduction" pause; Duolingo's "building your
course". Backfires as *benevolent deception* once the fake delay is noticed.
*TumTum:* never fake a delay. Show the three real steps of the pipeline
while they run. Sources: [Buell & Norton 2011](https://pubsonline.informs.org/doi/10.1287/mnsc.1110.1376) · [growth.design case](https://growth.design/case-studies/labor-perception-bias).

**Operational transparency.** Windows into the process raise satisfaction
and trust (Buell, *HBR* 2019; Domino's Pizza Tracker 2008; Uber's
behavioural-science write-up: idleness aversion, transparency and goal
gradient during the wait, −11% cancellations). *TumTum:* the app already
does this where it matters most — "buraco de dado aparece como buraco",
"X MIN SEM DADO", seven honest sync states. Extend it to the analysis.
Sources: [Buell 2019](https://www.hbs.edu/faculty/Pages/item.aspx?num=55804) · [Uber](https://www.uber.com/us/en/blog/applied-behavioral-science-at-scale/).

**Doherty threshold and response time.** Under ~400 ms feels fluent
(Doherty & Thadani 1982); 0.1 s instantaneous, 1 s keeps the flow, 10 s
loses attention (Nielsen 1993); Google's 400 ms delay cut searches 0.74%
(Brutlag 2009). *TumTum:* the card must render instantly; the analysis may
take a visible few seconds. Two different rules for two different screens.
Source: [Speed matters](https://research.google/blog/speed-matters/).

**Aesthetic–usability effect.** Attractive interfaces are perceived as
easier and forgiven more (Kurosu & Kashimura 1995, 26 ATM layouts, 252
participants; Tractinsky 2000 replication). Backfires by hiding usability
failures in tests. *TumTum:* the reason the design pass matters for the
reveal and the card; and the reason a pretty card over a wrong number
borrows trust it has not earned. Source: [NN/g](https://www.nngroup.com/articles/aesthetic-usability-effect/).

**Von Restorff (isolation) effect.** The different item is the remembered
one (von Restorff 1933). *TumTum:* one pink number on black; the MAIOR
badge; one CTA. Already right. Highlight everything and nothing stands out.

**Serial position effect.** First and last are remembered; the middle
sags (Ebbinghaus; Murdock 1962). Wrapped opens with minutes listened and ends
with the share card. *TumTum:* number first, share last — the reveal is
already ordered this way.

**Picture superiority effect.** Pictures are remembered better than words
(Paivio 1971; Shepard 1967). The "10% vs 65%" figure has no study behind it —
do not cite it. *TumTum:* the card is the memory object; Strava's finding
that activities with photos get 3.1× the kudos argues for card 02 and for
letting a fan drop their own photo on card 01.

**Mere exposure.** Repetition breeds liking, peaking around 10–20 exposures
(Zajonc 1968; Bornstein 1989). *TumTum:* the fixed silhouette of Mutante Pop
is mere exposure by design — one shape, many skins.

**Nostalgia and the reminiscence bump.** Memories from ages ~10–30 dominate;
revisiting them boosts mood and connection (Rubin 1986; Sedikides 2006→).
Facebook's "On This Day" reached 60M daily visitors within a year. Backfires
as *algorithmic cruelty* (Facebook's 2014 Year in Review around a
bereavement). *TumTum:* "um ano atrás, seu coração foi a 142 no Allianz"
is a strong, cheap re-engagement — with a way to hide a night.

**Self-reference effect.** Information about the self is encoded deepest
(Rogers, Kuiper & Kirker 1977; 129-study meta-analysis, Symons & Johnson
1997). *TumTum:* the voice rule — "Seu coração foi a 187", never
"Detectamos um pico" — is this principle written into the brand.

**Spotlight effect.** People overestimate how much others notice them
(Gilovich 2000: ~46% estimated vs ~23% actual). *TumTum:* the fear of
posting a "weird" card is larger than the audience's attention. Close
Friends is where the first card goes; say so.

**Halo effect.** One salient trait colours the rest (Thorndike 1920).
*TumTum:* the App Store screenshots and the first screen buy trust for the
number; that trust is only deserved if the number is right.

### 2.3 Decision and load

**Choice overload.** Real but conditional: strongest when options are
complex, similar, and the person is unsure (Iyengar & Lepper 2000; Chernev
2015, N = 7,202). Backfires the other way as patronising or as hidden
options. *TumTum:* four skins with previews; only producible formats; three
moments of twenty. Curate, do not conceal — the export and the delete stay
reachable. Sources: [Chernev 2015](https://chernev.com/wp-content/uploads/2017/02/ChoiceOverload_JCP_2015.pdf) · [on the jam study](https://www.jasoncollins.blog/posts/not-the-jam-study-again).

**Hick's law.** Decision time grows with log₂ of the options (Hick 1952).
Applies to simple choices, not deliberation. *TumTum:* one control on the
capture screen; one CTA on the reveal.

**Miller's law and chunking.** ~7 ± 2 chunks, closer to 4 (Miller 1956;
Cowan 2001). "Never more than seven menu items" is a misreading. *TumTum:*
the moment list shows three; the onboarding is three pages.

**Cognitive load.** Cut extraneous load; manage intrinsic load (Sweller
1988). *TumTum:* labels on the chart, not in a legend; one question per
screen in the permission corridor.

**Tesler's law.** Complexity is conserved; someone absorbs it. *TumTum:*
aligning a watch's timestamps to a setlist so the fan sees one curve is
absorbed complexity — and absorbed complexity that guesses wrong needs an
override (the operator marks, the manual timeline).

**Progressive disclosure.** Show what is needed now (Nielsen 2006).
Backfires when essentials (delete, export, cancel) are buried. *TumTum:*
operator functions belong behind a disclosure; deletion does not.

**Default effect.** The pre-selected option wins (Johnson & Goldstein 2003:
organ donation 4–27% opt-in vs 86–99% opt-out). Pre-ticked consent is
unlawful (*Planet49* 2019; LGPD). *TumTum:* private by default, nothing
shared without a tap — already the rule ("Compartilhar é sempre ativo").

**Anchoring.** A first number pulls later estimates (Tversky & Kahneman
1974: 25% vs 45% from a rigged wheel). *TumTum:* the first number a fan sees
anchors what "a lot" means. Lead with the peak, not the average; and never
compare bodies.

**Framing.** The same fact as gain or loss changes choices (Tversky &
Kahneman 1981: 72% vs 78% on the Asian-disease problem). Confirmshaming is
framing weaponised. *TumTum:* "Você tava lá" is a gain frame; "você vai
perder" is a loss frame. Use the first.

**Decoy effect.** A dominated option shifts choice (Huber, Payne & Puto
1982; Ariely's *Economist* example 16/0/84 → 68/32). Weak with real products
(Frederick 2014). *TumTum:* irrelevant until there is a paid tier; note it
for then.

**Recognition over recall.** Show; do not make people remember (Nielsen
heuristic 6). *TumTum:* when a night has no timeline, show the setlist and
let the fan *recognise* the song at the peak instead of typing it.

**Jakob's law.** People expect your app to work like the ones they use
(Nielsen 2000). Snapchat's 2018 redesign lost ~3M daily users. *TumTum:*
Stories tap-through for a multi-scene reveal; the system share sheet;
bottom tabs. Novelty in the surface, not in the mechanics.

**Fitts's law and the thumb zone.** Big, close targets are fast (Fitts 1954;
49% one-handed thumb grip, Hoober 2013; 48 dp minimum). *TumTum:* the 18/09
rehearsal fixes (the 44 dp back arrow, the share button pinned at the
bottom) were Fitts's law learned in the field.

**Occam's razor / minimalism.** Fewest elements that do the job (Nielsen
heuristic 8). Backfires as hidden navigation (discoverability halved, NN/g
2016) and as empty screens that lie by omission. *TumTum:* the card shows
the number, the moment and the event and nothing else.

### 2.4 Social

**Social proof.** When unsure, people do what similar others do (Cialdini
1984; Goldstein, Cialdini & Griskevicius 2008: "75% of guests reuse their
towels" lifted reuse from ~35% to ~44%, and "75% of guests *in this room*" to
~49%). Fabricated social proof is now explicitly illegal (FTC fake-reviews
rule 2024; DSA Art. 25; CDC Art. 37). *TumTum:* "A galera" is social proof
rendered as a chart, and the app's rule is already the ethical one — nothing
aggregated reaches a fan until the cohort is real and large enough, and the
footnote says exactly what is measured. Source: [Goldstein 2008](https://academic.oup.com/jcr/article/35/3/472/1856257).

**Reciprocity.** A favour received creates an obligation to return it (Regan
1971: a Coke bought doubled raffle-ticket sales). It is also the mediator
Buell & Norton found behind the labor illusion. *TumTum:* give the complete
first card before asking for anything — a photo, a follow, a tribe.

**Scarcity and FOMO.** Limited things seem more valuable (Worchel 1975:
cookies from a jar of 2 beat a jar of 10); FOMO is predicted by low need
satisfaction (Przybylski 2013). Manufactured scarcity is the most enforced
dark pattern (UK CMA 2019 v. Booking.com; DSA; CDC Art. 39). *TumTum:* the
real scarcity is the whole product — the show happened once. Never add a
fake one.

**Authority.** People defer to credible sources (Milgram 1963: 65%).
*TumTum:* the only authority signal that fits is the *artist's* or the
*club's* participation (card 05), never a medical one.

**Unity.** "We" persuades more than "you and I" (Cialdini 2016; Tajfel &
Turner 1979). *TumTum:* the tribes chosen at sign-up, and a card that
compares a fan's heart with the artist's, are unity made literal. Rival-club
dynamics are the edge.

**Identity signalling and self-presentation.** People share what says who
they are; music is the archetypal signalling domain (Berger & Heath 2007 —
people diverge in identity-relevant domains, not in dish soap); high-arousal
emotion drives sharing (Berger & Milkman 2012); optimal distinctiveness
(Brewer 1991) explains Wrapped — everyone posts, but *mine* is unique; a
screenshot is a costly signal because it is hard to fake. Backfires when the
card looks like an advert for the platform rather than a statement about the
person — social currency must belong to the sharer. *TumTum:* this is the
mechanism behind card 01, and the reason the design-brief QA line "a fan
would post this without feeling they are posting an advert" is the test
that matters most. Sources: [Berger & Heath 2007](https://academic.oup.com/jcr/article-abstract/34/2/121/1793110) · [Berger & Milkman 2012](https://journals.sagepub.com/doi/10.1509/jmr.10.0353) · [UVA on Wrapped](https://news.virginia.edu/content/why-spotify-wrapped-turns-your-music-habits-social-event).

**Bandwagon.** Visible adoption breeds adoption (Leibenstein 1950); the snob
effect is its mirror. *TumTum:* "3 amigos capturaram hoje" only when true.

**Parasocial relationships, fandom and proof of presence.** Fans form
one-sided intimacy with performers (Horton & Wohl 1956) and transcendence in
crowds (Durkheim's collective effervescence); possessions become part of the
self (Belk 1988); experiences are more central to identity, more talked
about and less comparison-prone than things (Van Boven & Gilovich 2003;
Carter & Gilovich 2012). A ticket stub is a retrieval cue that brings the
night back. And the two cautions that matter for a product used *during* a
show: photographing impairs memory of the thing photographed (Henkel 2014),
and taking photos *to share* lowers enjoyment of the experience itself
(Barasch 2018). *TumTum:* **a TumTum card is a receipt of feeling** — proof
of presence no photo gives, produced with the phone in the pocket, which is
exactly the costly signal fans have been trying to make with phones held up.
That sentence is the product. Sources: [Carter & Gilovich 2012](https://www.academia.edu/17738617/I_am_what_I_do_not_what_I_have_the_differential_centrality_of_experiential_and_material_purchases_to_the_self) · [Henkel 2014](https://www.researchgate.net/publication/259207719_Point-and-Shoot_Memories_The_Influence_of_Taking_Photos_on_Memory_for_a_Museum_Tour) · [the ticket stub](https://musictech.com/features/opinion-analysis/the-lost-art-of-the-ticket-stub-and-its-futuristic-revival/).

### 2.5 Feedback and delight

**Feedback loops.** Every action gets an immediate, informative response
(Norman 1988 — the gulf of evaluation). *TumTum:* the decision log's bug
class — *the app stating something false about its own state*, found
fourteen times in one day on 18/09 — is a feedback-loop failure, and it is
the one that only shows up in a real person's hands. Two more are in Part 4.

**Microinteractions.** Trigger, rules, feedback, loops (Saffer 2013).
Feedback proportional to the importance of the action; respect
reduced-motion. *TumTum:* the 1.2 s curve draw is a microinteraction that
earns its time; a share button that animates would not.

**Optimistic UI.** Show the result before the server confirms; roll back
visibly on failure (Mishunov 2016: only where failure is rare and
reversible). *TumTum:* fine for a reaction; wrong for an upload. The app is
already pessimistic where it should be — "Nada foi apagado" on a failed
delete is the model.

**Skeleton screens vs spinners.** Evidence conflicts (Viget 2017 found
skeletons *worst*; ECCE 2018 the opposite). For a 5–30 s analysis, a
narrated progress beats both. *TumTum:* the analysis screen (Part 5.2).

**Empty states.** Must tell "nothing here" from "I could not ask" — "an
empty state is a claim" (this project's own rule, 2026-08-26). *TumTum:*
"Nenhum show ainda. Seu coração tá de folga." is honest and in voice;
`event_pick_offline` distinguishes the failed request. The gallery's empty
state is a promise ("A primeira é a próxima captura"), not a value — Part
5.1 is about that.

**"Aha moment", time-to-value, activation.** The first moment a person
experiences the core value; the "magic numbers" (7 friends in 10 days,
2,000 messages) are correlations, not causes (Mixpanel's caveat). *TumTum:*
the aha is the first curve with one moment named. **Everything before it is
cost, and today it is days or weeks away** — the largest gap between TumTum
and every app in Part 3.

**Kano model.** Must-bes, performance features, delighters that decay into
must-bes (Kano 1984; pull-to-refresh was a delighter in 2008). *TumTum:* the
share sheet with the image attached is a must-be; the skin picker is a
delighter today and will be a must-be by next year.

**Norman's three levels.** Visceral, behavioural, reflective (Norman 2004).
*TumTum:* pink-on-black number (visceral); the gap drawn as a gap
(behavioural honesty); "eu tava lá e meu coração prova" (reflective). The
card works on all three or it does not work.

**Walter's hierarchy; voice and tone.** Functional → reliable → usable →
pleasurable; humour switches off when the person is in trouble (Walter
2011; Mailchimp's voice-and-tone guide). *TumTum:* the manual already
encodes this — fun everywhere except consent, privacy and failure.

**Pratfall effect.** A competent actor becomes more likeable after a small
blunder (Aronson 1966). *TumTum:* "79 MIN SEM DADO" said plainly is a
pratfall the brand can afford; "ops, algo deu errado 🙈" is not.

**Surprise.** Unexpected reward produces the larger response (Schultz 1997).
Keep surprises for delight, never for consequences. *TumTum:* the night
itself is the surprise; a surprise skin is a small second one.

**Celebration moments.** Proportionate, rare, tied to a goal the person set,
collectible, shareable (Apple's rings). The reference failure: Robinhood's
confetti, removed 31 March 2021, banned permanently in the 2024 $7.5M
settlement — celebrate outcomes the person values, never actions that
benefit the platform. Duolingo's "You made Duo sad" lines won open rates by
5–8% (*reported*) and became the resented owl. *TumTum:* celebrate the
memory ("você tava lá"), never the magnitude (187 is not a score).

### 2.6 Ethics

**Brignull's deceptive-patterns taxonomy.** Sixteen types at
deceptive.design: comparison prevention, confirmshaming, disguised ads, fake
scarcity, fake social proof, fake urgency, forced action, hard to cancel,
hidden costs, hidden subscription, nagging, obstruction, preselection,
sneaking, trick wording, visual interference. **Every principle above has a
dark twin** — goal gradient → fake progress; loss aversion → fake urgency;
social proof → fake counts; defaults → preselection; progressive disclosure
→ obstruction; Fitts → visual interference. Mathur et al. 2019 found 1,818
instances across ~11k shopping sites. Source: [deceptive.design](https://www.deceptive.design/types).

**The regulatory picture, 2025–2026.** EU: DSA Art. 25 in force since Feb
2024; a Digital Fairness Act proposal due Q4 2026 targeting dark patterns
and addictive design; the European Parliament's 26 Nov 2025 resolution
(483–92–86) asking for a minimum age of 16 and, for minors, bans on infinite
scroll, autoplay, reward loops and loot boxes. UK: DMCC Act bans drip
pricing and fake reviews from April 2025. US: California SB 976 (addictive
feeds, upheld Sept 2025) and the 10 Sept 2026 under-16 law; New York SAFE
for Kids; Minnesota, Vermont, Nebraska, Virginia codes; Utah and
Pennsylvania suing Snap over streaks. Australia: under-16 ban since 10 Dec
2025. **Brazil:** no dark-pattern statute; CDC Arts. 6, 37 and 39 and LGPD
Arts. 5, 8 and 11 apply — **heart rate is sensitive data under Art. 11 and
needs specific, highlighted consent**; ANPD became a regulatory agency in
Sept 2025; **ECA Digital (Lei 15.211/2025), in force 17 March 2026**, applies
to any product "likely to be accessed" by under-18s, bans loot boxes,
restricts profiling of minors and requires privacy by default. *TumTum:*
the app is 18+ on Play; the consent screen is already the quiet one; keep
it that way, and read the ECA text before any reward mechanic. Sources:
[EP resolution](https://www.europarl.europa.eu/news/en/press-room/20251013IPR30892/new-eu-measures-needed-to-make-online-services-safer-for-minors) · [ECA Digital](https://www.demarest.com.br/en/digital-statute-for-children-and-adolescents-law-no-15211-2025/) · [dark patterns and LGPD consent](https://www.conjur.com.br/2026-mai-21/dark-patterns-e-o-paradoxo-do-consentimento-na-lgpd/).

**Calm technology.** Technology should need the least attention and inform
from the periphery (Weiser & Brown 1995; Case 2015); the humane-tech
movement (Harris 2013→; Center for Humane Technology 2018) is the
counter-current to everything in §1.11–1.12. *TumTum:* an app that captures
during the show and asks for nothing until the night is ready is calm by
construction — and the real retention risk of a calm, once-per-event
product is being forgotten, which the event itself answers, not a push.

---

## 3. The benchmark — what the best apps actually do

Thirty-odd apps, chosen for four reasons: they turn personal data into
something people post (Wrapped, Strava, Duolingo), they sell proof of
presence (Ticketmaster, sócio-torcedor, Letterboxd), they live in TumTum's
own fan space (Weverse, Sofascore, Cartola, Setlist.fm), or they are the
apps a São Paulo fan already has on the phone (iFood, Nubank, TikTok). For
each: the mechanic as it appears on screen, the principle, and the published
number if there is one. A sourcing caveat applies to all of it: most primary
sites were unreachable from this environment, so the numbers come from
search-surfaced excerpts and secondary coverage, each cited; anything
resting on a single weak source is marked *[weak]*.

### 3.1 Shareable moments and recaps

**Spotify Wrapped.** A once-a-year, full-screen vertical story: minutes
listened, top artists, top songs, then a summary card, with a share button on
every scene pre-sized for Stories, TikTok, X and WhatsApp. The card needs no
caption. Launched 2015 as "Year in Music" (~5M users); the decisive change was
2016, when sharing to social was added. Engaged users 90M (2020) → 227M (2023)
→ 2025: **200M in ~24 hours (+19%), 300M+ in total, shared 500M+ times
(+41%)**; December 2020's edition lifted app downloads 21% in its first week.
2025 added the first *multiplayer* mode (Wrapped Party, up to nine friends
comparing live), Listening Age and six themed Clubs — after years of solo
cards the next lever was comparison with friends. Annabell & Rasmussen (2025,
*New Media & Society*) call it an "algorithmic event": everyone experiences
it the same week, which is what floods the timeline. *Principles:* identity
signalling, synchronised scarcity, social proof, comparison. *TumTum read:*
the format to copy is scene-per-stat with a share on every scene, and the
2025 pivot says comparison ("78% em sincronia com o Chris Martin") is where
this goes. Sources: [MBW](https://www.musicbusinessworldwide.com/spotify-wrapped-campaign-hit-200m-engaged-users-in-24-hours-a-19-yoy-increase/) · [Variety](https://variety.com/2025/music/news/spotify-wrapped-breaks-own-record-250-million-engagements-1236603493/) · [Sensor Tower 2020](https://sensortower.com/blog/spotify-wrapped-is-on-a-roll) · [SAGE 2025](https://journals.sagepub.com/doi/10.1177/14614448251391301) · [history](https://www.slashgear.com/1731101/spotify-wrapped-history-untold-story-creator-no-credit/).

**Strava.** Kudos (a one-tap thumbs-up), segments with leaderboards, PRs and
trophies per activity, a share image (map + stats) for Stories, Flyover
(shareable to Stories since Feb 2025), Athlete Intelligence, and Year in Sport
(12th edition 2025, now subscribers-only). Numbers: **14 billion kudos given
in 2025 (+20%)**; **activities with photos get 3.1× the kudos**; a field study
of 329 club runners (*Social Networks* 2022) found receiving kudos made them
run more and more often. *Principles:* cheap reciprocity, competition with
self before others, a photo turns data into a story. *TumTum read:* the 3.1×
argues for card 02 and for a fan's own photo on card 01; a one-tap reaction
on a friend's moment is the cheapest social loop there is — SENTI TB is
already that. Sources: [Strava 2025 report](https://press.strava.com/articles/strava-releases-12th-annual-year-in-sport-trend-report-2025) · [kudos study](https://www.sciencedirect.com/science/article/pii/S0378873322000909) · [Feb 2025 features](https://www.wareable.com/news/strava-athlete-intelligence-full-launch-flyover-sharing-progress-comparison).

**Duolingo.** Streak with freezes and repair, leagues, hearts (replaced by
Energy in 2025, received as a cash grab), the owl's notifications, a Year in
Review with a share step, and an onboarding that runs a lesson *before* the
account so people sign up to keep progress they already earned. Numbers: over
600 streak experiments in four years; changing a button from "continue" to
**"commit to my goal" won +10,000 DAU**; multiple streak freezes were "a major
DAU driver"; the rule is more forgiveness early, less as the streak grows;
**FY2025 10-K: ~43M DAU with a 7-day+ streak, ~15M with 365+**; Q2 2026 DAU
58.7M, with a June 2026 Streak Revival campaign credited for acceleration.
Notification volume could not be raised without CEO approval. The guilt-owl
became a meme and then a complaint. *Principles:* loss aversion softened by
forgiveness, commitment copy, endowed progress before signup. *TumTum read:*
not the streak — the *forgiveness design* (a failed capture must never read
as "you lost your night") and the commitment copy on the pre-event screen.
The owl is the warning: funny about the brand, never at the user. Sources:
[Lenny's — Shuttleworth](https://www.lennysnewsletter.com/p/behind-the-product-duolingo-streaks) · [10-K FY2025](https://www.sec.gov/Archives/edgar/data/1562088/000162828026012494/duol-20251231.htm) · [Q2 2026](https://investors.duolingo.com/static-files/3c8277ee-bc94-4f5d-9b77-0db3e46f88b8) · [Mazal on Lenny's](https://www.lennysnewsletter.com/p/how-duolingo-reignited-user-growth).

**Apple Fitness rings.** Three rings that close daily; awards that are also
iMessage stickers; 7-day friend competitions (watchOS 5, 2018); Global Close
Your Rings Day (24 April) with a limited-edition award and #CloseYourRings.
*Principles:* goal gradient (the ring visibly closes), scarcity of limited
awards, a badge that is itself a share object. *TumTum read:* the badge-as-
sticker idea — the card that is also the collectible — and the brand's
explicit NEVER on progress rings. Source: [Apple, April 2025](https://www.apple.com/newsroom/2025/04/get-active-with-apple-watch/).

**Wordle.** One puzzle a day for everyone; after solving, Share copies a
**spoiler-free emoji grid** — compact, instantly recognisable, shows effort
without the answer, invites comparison. 90 players on 1 Nov 2021, 2M+ daily by
January 2022, bought by the NYT that month; **5.3B plays in 2024**, ~10M
daily. *Principles:* synchronous scarcity makes every share comparable; the
grid is socially safe to post — no spoiler, no boast; each grid is a peer
recommendation. *TumTum read:* **the closest analogue to the grid is a tiny,
abstract, instantly recognisable glyph of the night** — the curve silhouette
with one marked peak — that reads as TumTum in a feed before the viewer
reads a number. Mutante Pop's "fixed silhouette, mutating skin" is the same
mechanism. Sources: [history](https://puzzlecottage.com/wordle-history) · [NYT growth](https://techcrunch.com/2022/05/04/wordle-new-york-times-user-growth) · [2024 plays](https://www.newsline.com/expert-time/Wordles-Persistent-Popularity-Underpins-NYTs-Digital-Engagement-Strategy-34-2895).

**Letterboxd, Untappd, Concert Archives, Gigvault — the diary market.**
Letterboxd: a diary (film, date, rating), stats, a Year in Review needing ≥10
films; 11.4M members end-2023 → 30M+ mid-2026, 898M films logged in 2025
(+28%). Untappd: ~1B check-ins, badges up to 40,000 check-ins, "Recappd"
2025 — and a 2026 ethics paper (arXiv 2601.04841) showing its quantity and
ABV badges gamify excess. Concert Archives: a personal concert history with
setlists, most-seen artists, annual recaps, concert-anniversary reminders.
Gigvault logs a show in 30 s and runs a "Concert Wrapped" that updates after
every show. *Principles:* the diary is a permanent record whose value grows
the longer it is kept; the recap is the harvest. *TumTum read:* every one of
these holds the *fact* of attendance and none holds the *feeling*. That gap
is the product. Sources: [Letterboxd numbers](https://expandedramblings.com/index.php/letterboxd-statistics-facts/) · [Untappd ethics](https://arxiv.org/abs/2601.04841) · [concert-diary apps](https://gigvault.app/blog/best-apps-to-track-concerts).

**Pokémon Go.** Real-world collection tied to place; Community Day (three
hours a month, exclusive shiny odds); ticketed GO Fest (Chicago 2026: 103,000+
tickets, a North American record). *Principles:* scarcity windows, place-bound
collection, co-presence. *TumTum read:* the event is already the scarcity
window; the app does not need to invent one. Source: [Scopely 2026](https://www.scopely.com/en/news/ten-years-in-pokemon-gos-annual-pokemon-go-fest-event-series-broke-attendance-and-engagement-records-across-three-continents).

**BeReal.** One random notification a day, a two-minute window, a
front-and-back photo; you cannot see friends' posts until you post. Peak
73.5M MAU (Aug 2022) → 16M (Mar 2025); downloads −60% in 2024 and −50% again
in 2025; sold to Voodoo for ~€500M. *Lesson:* time scarcity alone produces a
spike, not a habit — nothing accumulated, so nothing pulled people back.
*TumTum read:* the gallery is what accumulates; the reveal lock's scarcity
needs it. Source: [Business of Apps](https://www.businessofapps.com/data/bereal-statistics/).

**Instagram.** Stories (2 Aug 2016, to "alleviate the pressure to post your
best stuff"); Close Friends (Nov 2018); the hidden-like-counts test (Canada
April 2019, Brazil July 2019, global Nov 2019) that in May 2021 became a
per-user option because counts were "beneficial for some and annoying to
others"; Notes (Dec 2022). *Principles:* lowering the bar to post raises
posting; social proof is something users *want*, not only suffer. *TumTum
read:* the card lands in a medium built for low-stakes 24-hour posting, and
Close Friends is where "meu coração foi a 187" goes first. Design for the
Story, not the grid. Sources: [Stories launch](https://www.csmonitor.com/Technology/2016/0802/Instagram-takes-a-page-from-Snapchat-with-Stories-launch) · [hidden likes](https://techcrunch.com/2019/11/14/instagram-private-like-counts/) · [2021 reversal](https://www.cnn.com/2021/05/26/tech/facebook-instagram-hiding-likes/index.html).

**TikTok and Kwai.** The first video plays before any sign-up; the guiding
question is "how much can we let people do without an account?". Brazil:
91–98M TikTok users, 78% open it daily, 90+ minutes a day; Kwai 60M+ monthly
in Brazil, strongest in the Nordeste. *Principle:* value before commitment.
*TumTum read:* the hardest benchmark for TumTum, whose first value needs an
event (Part 5.1). Sources: [Appcues on TikTok onboarding](https://goodux.appcues.com/blog/tiktok-user-onboarding) · [Opinion Box](https://blog.opinionbox.com/pesquisa-tiktok-no-brasil/).

**Snapchat streaks, Tinder.** Streaks: a flame and a count on a friendship,
an hourglass before it dies, paid restores at $0.99 (2023); now sued in six US
states and the target of California's 2026 under-16 law. Tinder: a co-founder
has said the swipe was modelled on Skinner's variable-ratio schedules. *TumTum
read:* the two mechanics the brand refuses on principle (Part 6). Sources:
[Pennsylvania suit](https://www.techrepublic.com/article/news-pennsylvania-snapchat-lawsuit-teen-safety/) · [California law](https://www.washingtonpost.com/nation/2026/09/10/newsom-signs-bill-banning-addictive-features-social-media-protect-teens/) · [Tinder](https://lithub.com/swipe-right-for-loneliness-on-the-gamification-of-dating-apps/).

### 3.2 Labor illusion, feedback and speed

**Uber.** The car moving on the map, a live ETA, and step-by-step copy during
matching. Uber's own behavioural-science team names idleness aversion,
operational transparency and goal gradient as the three levers of the wait;
exposing each matching step in Express Pool **cut post-request cancellations
11%** in an A/B test. **iFood** does the same for a Brazilian audience
(stage notifications, courier on a map, a recomputed ETA) — and runs a
**Retrospectiva** every year since 2019; the 2025 edition is 15 Stories-format
screens. *TumTum read:* the audience already knows both the tracker and the
Stories-format recap; neither needs explaining. Sources: [Uber](https://www.uber.com/us/en/blog/applied-behavioral-science-at-scale/) · [iFood 2025](https://www.em.com.br/degusta/2025/12/7305745-ifood-lanca-retrospectiva-2025-com-capivara-vira-lata-e-novidades.html).

**Kayak, TurboTax, Duolingo's loading screen.** Buell & Norton's five
experiments: people preferred the site that made them wait *with* airline
names ticking past over the same results instantly. TurboTax runs a
5–10-second "checking every deduction" animation that could finish at once.
*TumTum read:* the analysis is real work; show it. Never pad it. Sources:
[Buell & Norton](https://pubsonline.informs.org/doi/10.1287/mnsc.1110.1376) · [TurboTax](https://www.appcues.com/blog/how-turbotax-makes-a-dreadful-user-experience-a-delightful-one).

**Mailchimp.** Freddie's sweating finger over the big Send button, then a
high-five once sent: empathy at the point of highest stakes, reward *after*
the irreversible act. *TumTum read:* the moment a fan taps Compartilhar is
their anxious Send; the screen after it should be the high-five. Source:
[Creative Review](https://www.creativereview.co.uk/mailchimp-on-playfulness/).

**Superhuman, Linear, Shazam.** Superhuman's 100 ms rule; Linear's local-first
UI that updates before the server confirms; Shazam's one button and a
few seconds' wait (1B+ installs). *TumTum read:* the capture screen is
already Shazam-shaped; the card must be Superhuman-fast; the analysis may be
Kayak-slow. Sources: [First Round](https://review.firstround.com/superhuman-onboarding-playbook/) · [Linear](https://dev.to/0xgosu/why-linear-feels-fast-local-data-small-updates-and-product-discipline-1m53).

**Robinhood.** Confetti after every trade, removed 31 March 2021 after
Massachusetts named gamification in its complaint; the 2024 $7.5M settlement
bans it permanently. *TumTum read:* celebration is fine when the celebrated
thing is a memory; it is not fine when it is a number that could be read as
health. Source: [CNBC](https://www.cnbc.com/2021/03/31/robinhood-gets-rid-of-confetti-feature-amid-scrutiny-over-gamification.html).

### 3.3 Brazil

**Nubank.** Tone as the differentiator (anti-bank purple, plain language,
published design principles); 131M customers end-2025. NuCoin launched as a
tradeable token (Mar 2023), trading cut Jan 2024, relaunched 2025 as a simple
non-tradeable loyalty game — the local precedent that *simple in-app games*
survive and *tradeable* gamification confuses. No shareable in-app
retrospectiva exists. *TumTum read:* the reference for a Brazilian brand that
is loved for its voice; nothing to copy mechanically. Sources: [Building Nubank](https://building.nubank.com/design-principles-at-nubank/) · [NuCoin 2025](https://br.cointelegraph.com/news/nubank-relaunches-nucoin-loyalty-program-2025).

**Cartola FC.** Fantasy football since 2005: 100 cartoletas, a mercado that
closes at kick-off, "mitar", private leagues, captain scores double; second-
largest fantasy game in the world by 2021, 1.7M daily users and 4.6M teams in
2023. *Principle:* the match becomes personally consequential — every goal
moves *your* score. *TumTum read:* the proof that Brazilian fans build a
weekly habit around their own stake in a match. TumTum's stake is the fan's
own body; "mitou" is the goal that sent the heart to 160. Sources: [engagement](https://tudosobreincentivos.com.br/engajamento-de-usuarios/) · [2023 numbers](https://cartolafcmix.com/arquivo-mix/cartola-fc-tem-sucesso-de-engajamento-em-2023-e-tera-novidades-em-2024/).

**Sócio-torcedor programmes.** Fiel Torcedor scores points at purchase *and
again at stadium check-in*; Nação's "Rating" from tenure and attendance breaks
queue ties; Avanti sets pre-sale stage by tier. Corinthians' fan token sold
850,000 units in two hours. *Principle:* status by attendance — being there is
converted into points and priority. *TumTum read:* "proof I was there"
already has a currency in São Paulo, and these programmes are the natural
distribution partners. Sources: [Fiel Torcedor points](https://www.corinthians.com.br/noticias/fiel-torcedor-apresenta-novidades-em-pontuacoes-e-cashback) · [Nação](https://www.lance.com.br/lancepedia/socio-torcedor-flamengo-nacao.html).

**Ticketing.** Sympla keeps past events in a Tickets tab; Ingresso.com added an
in-app annual retrospective in 2025 *[weak]*; Ticketmaster's Digital
Collectibles — an NFT stub minted on entry, "proving event attendance" —
passed 14.5M mints across 4,000 events. *TumTum read:* the ticketing app owns
the record of attendance and does nothing with the emotion of the night.
Source: [Ticketmaster](https://business.ticketmaster.com/digital-collectibles-go-global-with-the-aflw/).

### 3.4 The fan space

**Weverse and Bubble.** Weverse: artist communities, paid memberships, DMs
that arrive as if to you, decorated digital fan letters (4.88M in 2025), 12M
MAU, BTS community past 30M with +300% new followers the month of the June
2025 reunion. Bubble: a one-to-many chat rendered as one-to-one, where the
subscription day-count unlocks longer replies — tenure as visible devotion.
*TumTum read:* the artist side of card 05 ("Na mesma vibe") sits in this
parasocial economy; a performer's heart rate is a Bubble-grade intimacy
object, and the BTS pilot candidate (28–31/10) is inside the most organised
fandom on earth. Sources: [Weverse 2025](https://en.weverse.co/news/?bmode=view&idx=165565023) · [NN/g on Bubble](https://www.nngroup.com/articles/kpop-private-messaging/).

**Sofascore, FotMob, OneFootball.** Goal pushes (~30 s behind broadcast),
live timelines, momentum graphs; Sofascore 3M users in Brazil, its second
market. *TumTum read:* the momentum graph is the closest existing visual to
TumTum's curve — but it is the *team's* momentum. TumTum's is the fan's. Their
timelines are the correlator's input for a match. Source: [Sofascore Brazil](https://maquinadoesporte.com.br/futebol/sofascore-ve-copa-do-mundo-de-clubes-como-oportunidade-para-expandir-representatividade-no-brasil/).

**Setlist.fm, Bandsintown.** Setlist.fm: 10.5M setlists, attendance marks,
personal stats, no photos, no recap. Bandsintown: 100M registered fans, 15M
concert reminders set in 2024, integrated into Spotify since Feb 2024. *TumTum
read:* the pre-event reminder and the setlist are both already products;
TumTum's job is the layer neither has.

**Coldplay's Xylobands.** LED wristbands lit in sync across the crowd since
2012. *Principle:* the crowd as one display — belonging made visible. The
closest live analogue to card 04.

### 3.5 Heart rate at events — what already exists

- **The Bielefeld football-fever study** (*Scientific Reports* 2026; preprint
  Sept 2025): 229 Arminia Bielefeld fans wore smartwatches for ~12 weeks
  around the 24 May 2025 DFB-Pokal final. Mean heart rate **94 bpm in the
  stadium vs 79 on TV vs 74 at a public viewing**; after the first goal the
  stadium gap widened to **+35.8%, mean peak 108 bpm**; stress ~42% above
  regular days. A follow-on open study is recruiting smartwatch owners for the
  2026 World Cup. These are the reference magnitudes for the detector and for
  copy. Sources: [phys.org](https://phys.org/news/2026-02-smartwatch-stadium-atmosphere-spikes-heart.html) · [arXiv 2509.09569](https://arxiv.org/abs/2509.09569) · [PMC](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC12877198/).
- **The Minneapolis Miracle, 14 Jan 2018:** after the Vikings' last-second
  touchdown, Apple Watches told fans they had "a heart rate above 120 BPM
  while inactive", and the screenshots went viral. **This is TumTum's card,
  produced by accident** — proof that fans already post this when a device
  hands it to them. Source: [9to5Mac](https://9to5mac.com/2018/01/15/apple-watch-vikings-saints-fans-heart-rate-warning/).
- **F2/F1 broadcast biometrics** (July 2023): live driver heart rate on a
  120–200 scale; F1 said it must be "careful" because drivers may not want
  rivals to see elevation — the caution for card 05. **WHOOP Live** on PGA
  Tour broadcasts is opt-in (Justin Thomas: 114 average, 154 peak). Sources:
  [RaceFans](https://www.racefans.net/2023/07/06/pretty-cool-new-heart-rate-graphic-trialled-during-f2-broadcast-may-come-to-f1/) · [PGA Tour](https://www.pgatour.com/news/2021/04/14/whoop-live-justin-thomas-rory-mcilroy-heart-rate-fitness-charity-program.html).
- Not found: any "Oura at Coachella" programme, any broadcaster's fan-heart-
  rate experiment, any documented viral concert heart-rate trend. Coverage
  of matches is now academically solid; coverage of concerts is thin — which
  is also a gap nobody has filled.

### 3.6 What repeats across the winners

1. **Personal data turned into an identity artefact** — Wrapped (500M+
   shares), Year in Sport, Letterboxd, Untappd, Duolingo, iFood, Ingresso.com.
2. **The card explains itself** — no caption needed; Wordle's grid; Strava's
   photos (3.1× kudos).
3. **Synchronised scarcity** — one week a year, one puzzle a day, three hours
   a month; BeReal shows scarcity without accumulation does not retain.
4. **Proof of presence as a collection** — badges, diaries, digital stubs
   (14.5M mints), fan letters (4.88M), stadium check-ins that score.
5. **Loss aversion with forgiveness** — the streak works because of the
   freeze; the unforgiving version is what the lawsuits name.
6. **Cheap reciprocal reactions** — 14B kudos; "kudos make you run".
7. **Comparison with a small group, not the world** — Wrapped Party, Apple's
   7-day competitions, Cartola leagues.
8. **Show the work** — Uber −11% cancellations; Kayak; Duolingo's loader.
9. **Value before signup; one control** — TikTok, Duolingo, Shazam.
10. **Speed reads as quality** — for the card and the feed, not the analysis.
11. **Status tied to real attendance** — Fiel Torcedor, Nação, GO Fest.
12. **Celebrate carefully; the brand is the joke, never the user** —
    Mailchimp's high-five lands after the anxious act; Robinhood's confetti
    was banned; the owl became a liability.

Of these, the ones with published causal numbers are 1 (Wrapped's shares and
the download lift), 2 (Strava's photos), 5 (Duolingo's copy and freezes), 6
(the kudos field study), 8 (Uber's A/B) and the Instagram hidden-likes
reversal (social proof retained by user demand). The rest are well described
and unmeasured.
---

## 4. TumTum against the catalogue, screen by screen

The loop as it exists in `cc.tumtum.app` on the Play internal track
(`app-b131`, 18/09), read against Parts 2 and 3. Seven stages: before the
event, during, the wait, the reveal, the card, the collection, the social
layer. For each: what exists, which principles it already applies (most of
them by instinct, which is worth knowing so they are not designed away),
and what is missing.

### 4.1 Before the event — anticipation and commitment

**Exists.** An event chosen or created before the capture (Etapa 3); the
Live tab's empty state "Nenhum show ainda. Seu coração tá de folga." with
"Conectar meu relógio" and "Criar um evento"; a feed banner "%s · hoje à
noite — Você e mais N amigos confirmaram" that runs on the fake repository.

**Applies.** Commitment (marking the event is a promise the fan makes to
themselves — Duolingo's "commit to my goal" copy is the same lever); the
fresh-start effect (an event is a natural threshold); social proof (friends
confirmed — true in the design, false in the build).

**Missing.** Nothing happens between install and the event. No countdown, no
"bota o relógio" the hour before, no ritual. The app's first job arrives
days or weeks after install, which is the time-to-value problem of §5.1.

### 4.2 During — the capture

**Exists.** "A gente só olha depois. Aproveita o show."; with a chest sensor
the number is hidden behind a long press, because seeing the number changes
the number; a sample counter, the sensor battery, the connection state,
seven notification states; operator-only marks (GOL / MÚSICA / MOMENTO)
behind a settings toggle; Encerrar a noite pinned so the night always has
an exit (second rehearsal, 18/09).

**Applies.** Calm technology, fully; operational transparency (the counter,
the states); Shazam's one-control shape; Barasch 2018 taken seriously — the
phone stays in the pocket so the show is experienced, not recorded.

**Missing.** Nothing. This screen is right, and the brand should defend it
against every future request to "show something during the show".

### 4.3 The wait — end of night to reveal

**Exists.** Upload and analysis with a one-line grey caption in seven honest
states (enviando · momentos do servidor · calculados neste aparelho · sem
conta · sem internet · sessão expirada · o servidor respondeu %s) and "Enviar
de novo"; the reveal lock — off by default, an operator setting — that opens
a night at 10:00 the next morning, with the locked screen "Sua noite está
sendo revelada. A curva abre aqui às 10h. A gente te avisa — vale a espera."
and the list badge LACRADA / "Sem espiar — é parte da mágica".

**Applies.** Operational transparency in the sync states; the curiosity gap
and the open loop in the lock — by accident, the strongest anticipation
device in the product, the same shape as Wordle's daily gate and Wrapped's
"your 2025 is ready"; peak–end, since the wait sets up the peak.

**Missing and wrong.**
- **"A gente te avisa" is a claim with no code behind it.** Only the capture
  service posts notifications; nothing is scheduled for `revealAt`. With the
  lock on, the app promises a notification it will never send — the
  fourteenth-plus instance of the log's bug class, in the one sentence
  whose whole job is to make the wait bearable.
- The server's real work is invisible. Upload, detection and matching happen
  in that order and the fan sees a grey line. §5.2.
- The lock competes with the Stories window: the card people post from the
  Uber at 2 am is not the card they post at 10:00 with coffee. Which one
  gets sent is unknown, and `one-app-plan.md` already lists the lock as a
  product call to make before the first event.

### 4.4 The reveal

**Exists.** The curve draws left to right in 1.2 s, then the peaks fade in
over 350 ms; the hero number in pink ("116"), "bpm às 01h24"; the headline
"EU TAVA TRANQUILO. AÍ VEIO ISSO." on every night; three moments of the
server's twenty, the biggest badged MAIOR; the gap drawn as a gap and named
("79 MIN SEM DADO"); the sync line; the export (operator); one CTA,
"Escolher como compartilhar", pinned at the bottom since the third
rehearsal.

**Applies.** The labor illusion in its honest form (the drawing *is* the data
arriving — no delay is faked); Von Restorff (one pink number, one badge);
Hick's law (three moments, one CTA); serial position (number first, share
last); operational transparency (the gap); self-reference in the voice.

**Missing.**
- **The cause.** Without a timeline row the moment reads "116 bpm às 01h24"
  with no story (open item 22), and the story is what the self-reference
  effect encodes and what the card needs. The operator marks fix this for a
  match; nothing fixes it for a fan at a concert. §5.6.
- **The headline never changes**, so the second reveal cannot surprise; the
  31/08 card copy is generated from the night's numbers — the reveal's is
  not.
- **No sense of where this night stands.** The gallery knows it is the third
  night and a record; the reveal, where the feeling is, does not say so.
- No celebration — deliberately, and rightly, for the number. A
  celebration of the *memory* ("você tava lá") is a different thing and is
  absent too.

### 4.5 The card — choose and share

**Exists.** "Um momento. Qual pele?" with four skins (A ASSINATURA · A NOITE ·
O GRITO · O LIMPO) as live previews; the card screen with "Postar no feed"
and "Compartilhar" (a 1080×1920 PNG into the system share sheet, "Gerando o
card…" while it renders); after posting, "No feed. A galera já pode sentir
também."

**Applies.** Choice architecture (four options, previews not labels — the
brief's rule of showing only producible formats is Iyengar applied); the
IKEA effect (choosing the skin is a small, completable act of authorship,
which is why a chosen card gets posted and an assigned one does not);
identity signalling (the skin is a costume: which me tonight).

**Missing and wrong.**
- **"Postar no feed" posts into a repository that exists only on this
  phone** (`FakeSocialRepository`), and the confirmation says other people
  can now feel it. Nobody can. On the Play track this is a false claim in
  a fan's hands; until the feed is real, the button should not be there.
- **The last screen is Android's.** After the share sheet the fan comes back
  to the same card screen. The peak–end rule says the product's ending is
  the system share sheet — Mailchimp's high-five is missing. §5.3.
- **The card does not yet pass the Wordle test.** Cover the wordmark and it
  is a black field with a number (item 24, second half). The silhouette is
  the mechanism; it is still the base one. §5.5.
- No photo. Strava's 3.1× says a picture turns a data post into a story.

### 4.6 The collection — gallery, nights, profile

**Exists.** "Sua galeria · Tudo que você sentiu desde agosto"; NOITES ·
MOMENTOS · SEU RECORDE; 9:14 covers in the chosen skin, white "SEM CARD
AINDA" covers for nights without one (since 18/09, so a night is never
hidden); the public profile with NOITES · AMIGOS · RECORDE and published
nights only; the ZIP export.

**Applies.** Collection and endowment (the ticket-stub drawer — Letterboxd's
diary, Concert Archives, Ticketmaster's stubs); stored value that grows
(Eyal's investment step, honestly, because it is exportable); self-reference
("tudo que você sentiu"); personal record (Strava's PR).

**Tension.** "SEU RECORDE" — the highest bpm — frames a higher number as
better. Within one body it is defensible; the brief's QA line ("no
suggestion that raw BPM means the same thing across different bodies") and
the healthtech NEVER both pull against it. Keep it personal, never
comparative, and consider "SEU MAIS ALTO" over "RECORDE".

**Missing.** The covers show the number; the stub is the *event* — the name,
the date, the place are what a person recognises in a drawer. The empty
state is a promise ("A primeira é a próxima captura"), not a value.

### 4.7 The social layer

**Exists (on the fake repository, hidden from the Play listing).** A friends'
feed of cards with one reaction, SENTI TB; an event feed with "NINGUÉM TAVA
TRANQUILO" and "X% bateram o próprio pico durante a mesma música"; the
"A galera" screen (card 04) with the footnote "Só entra na conta quem
escolheu compartilhar. Nenhum dado individual aparece — só o agregado, e só
quando o grupo é grande o bastante."

**Applies.** Social proof stated exactly (that footnote is the ethical
version of every "12 people are viewing this"); the single reaction is
Strava's kudos and sidesteps the like-count anxiety Instagram tested its way
out of; unity through the tribes.

**Right call already made.** Nothing invented reaches a fan's hand (18/09):
the crowd screen stays off until a real cohort exists. Keep it.

### 4.8 Where choice overload actually lives

- **Configurações** (414 lines): the reveal lock, the operator marks, the
  participant code and the export sit beside the fan's own settings, with a
  string that says "Só para quem opera o teste. Um fã não precisa mexer
  aqui." A settings screen that tells the fan to ignore half of itself is
  overload by admission. One "Operador" disclosure fixes it.
- **The permission corridor** (Health Connect → battery exemption →
  Bluetooth → notifications): Tesler's law — the complexity cannot be
  removed, only sequenced and explained, which is what the 18/09 fixes to
  the battery sheet did.

### 4.9 Two more of the log's bug class, found by this audit

1. `locked_body` — "A gente te avisa" — with no scheduled notification (§4.3).
2. `card_posted` — "No feed. A galera já pode sentir também." — on a post to
   an on-phone fake repository (§4.5).

Neither breaks anything, neither shows in a test, both surface in a real
person's hands. Recorded as open items in the decision log.

---

## 5. What to borrow — ranked

Ranked by how much of the loop each unlocks against what it costs, with the
principle it rests on and what the pilot can measure. Costs are working
sessions, the unit the rest of the log uses. None of these is decided here.

### 5.1 The first night must not be weeks away — *aha moment, endowed progress, TikTok's rule*

Every app in Part 3 delivers its first value in minutes; TumTum's needs a
ticket, a watch and an evening, and then possibly a wait until 10:00. That
is the largest structural gap in this document, and it is not a design
problem.

Two honest ways to close it:
- **The retroactive night.** `WatchSourcesScreen` in setup mode already reads
  the last 24 hours from Health Connect without an event. The Etapa 0
  verdict says the watch path delivers *the curve of the night* (1/min in
  background, 1 per ~32 s in a workout) even if the moments need the strap.
  So: "Teve show ou jogo esta semana? Traz do relógio." — pick the day and
  the window, build a night from the watch's own history, label its moments
  as the phone's top-N until the server has a timeline. Onboarding ends
  with a curve instead of a promise. Cost: 1–2 sessions, mostly the window
  picker and the honest labelling.
- **A demo night, labelled.** Where the watch has nothing, show a night that
  says "exemplo" on every surface and never sits next to real data — the
  /events seeding lesson. Cost: ½ session.

Measure: share of installs that see a curve within ten minutes.

### 5.2 Show the work — *labor illusion, operational transparency*

The pipeline does three real things after Encerrar a noite. Replace the grey
caption with a narrated progress while they happen: "Enviando 21.960
batidas…", "Procurando seus momentos…", "Casando com o que tocava…". True,
under 30 s, never padded; the failure states stay exactly as they are. Buell
& Norton's ceiling (about a minute) and Uber's −11% are the bounds. Cost:
½ session — the states already exist. Measure: none in a five-person pilot;
ask.

### 5.3 The loop ends on a TumTum screen — *peak–end, Mailchimp*

After the share sheet returns: a screen that says what just happened and
where the night now lives — the card as the cover, "3 noites · 41
momentos", "Sua galeria". The high-five after the anxious Send. Remove
"Postar no feed" until the feed is real (§4.5). Cost: ½ session.

### 5.4 Decide the reveal lock, and build the notification it promises — *curiosity gap, open loop*

The lock is the product's strongest anticipation device and an experiment
protocol at the same time. Decide which: if it stays for fans, build the one
notification the product is allowed ("Sua noite abriu.") and a countdown on
the locked screen; if it is only for the blind-card protocol, keep it behind
the operator toggle and fix the copy. The research cuts both ways — the wait
raises value (Loewenstein; Wrapped's envelope) and Stories are posted the
same night (Instagram) — and BeReal says scarcity without accumulation does
not retain, so the lock only earns its keep once the gallery does. Cost: ½
session either way. Measure: with 3–5 people, ask each one when they wanted
to see it.

### 5.5 The card passes the Wordle test — *identity signalling, mere exposure, the two-second test*

A silhouette recognisable at thumbnail size before the viewer reads a
number: the curve, one marked peak, the number, the event. Mutante Pop
already says the silhouette is fixed and the skin mutates — that *is* the
Wordle grid and the Wrapped card. This is item 24's second half and the
design pass the brief asks for; it is not an engineering task. Measure:
sends per night, and which format (item 25).

### 5.6 Let the fan name the moment — *recognition over recall, self-reference, IKEA*

When a night has no timeline, offer the setlist (Setlist.fm has the order,
never the times — item 28) or a free field at the peak: "O que tava
rolando?". Recognising a song from a list is easy; typing from memory is
not; a moment the fan named is theirs, and a card that is theirs gets
posted. This also closes item 22 for fans rather than operators. Cost: 1
session on the phone, the timeline endpoint already exists.

### 5.7 The calendar is the trigger — *Fogg, commitment, fresh start*

The marked event on the Live tab with its date and a countdown; one
facilitator prompt an hour before ("Hoje: Palmeiras x Corinthians. Bota o
relógio."). Not a guilt notification; the prompt Fogg's model says works
when motivation is already high. Cost: 1 session (a scheduled notification
is the same code §5.4 needs).

### 5.8 Forgiveness design — *Duolingo's real lesson*

A failed capture, a gap, a night with no data must never read as "you lost
your night". Mostly right already ("Não gravamos nenhuma batida" is
factual). Add the next step to every failure ("Na próxima, confere o
relógio antes do primeiro acorde") and keep the event as a night in the
list, so the drawer still holds the stub. Cost: ¼ session.

### 5.9 A season, not a streak — *fresh start, endowed progress*

"5 noites em 2026" on the gallery and the reveal; "SEU MAIS ALTO" instead of
"SEU RECORDE". The honest cousin of the streak for a product used six times
a year. Cost: ¼ session.

### 5.10 Keep the reaction single and the audience small — *kudos, spotlight, Wrapped Party*

When the feed becomes real: SENTI TB stays the only reaction; counts are
shown among friends, never as a global ranking; "A galera" stays an
aggregate. The 2025 Wrapped pivot says comparison with a small group is the
next lever after the solo card — that is card 05, and it waits for an
artist.

### 5.11 A photo on the card — *picture superiority, Strava's 3.1×*

Let a fan drop their own photo behind card 01, and build card 02 when
licensed media exists. Cost: 1 session in the renderer. Measure: sends per
card type.

### 5.12 Retrospectiva TumTum — *Wrapped, iFood, fresh start*

At year end, a Stories-format recap of the person's nights. Needs more than
one night per person, so it is December's question, not October's. The
audience already knows the format from iFood and Spotify; it needs no
explanation.

### 5.13 Operator functions behind one disclosure — *progressive disclosure, choice overload*

One "Operador" section in Configurações, collapsed by default. Cost: ¼
session.

---

## 6. What not to borrow — the refusals

Each with the principle it would exploit and the reason it is refused, so
the argument does not have to be had twice.

| Mechanic | Principle | Why not, here |
|---|---|---|
| **Daily streaks** | loss aversion, commitment | Six events a year is not a daily habit; a streak would lie about what the product is. Season counts instead (§5.9). |
| **Leagues, leaderboards, rankings on bpm** | social comparison | Bodies differ; "no suggestion that raw BPM means the same thing across different bodies" is a QA line in the brief. The crowd stays an aggregate. |
| **Scores** — readiness, recovery, a "sync %" before it is validated | authority, performance framing | Healthtech in one number. The manual's territory line. Card 05's sync score is explicitly open in the brief (§14) and stays open. |
| **Guilt notifications** | loss aversion, nagging | "Funny about itself, never about the user." The owl is the reference failure, and Duolingo itself capped notification volume at CEO approval. |
| **Manufactured scarcity or urgency** | scarcity, FOMO | Real scarcity is the whole product; a fake one is the most enforced dark pattern in three jurisdictions. |
| **Confetti for a high number** | celebration, variable reward | 187 is not an achievement. Robinhood's confetti is banned by settlement; a heart is more sensitive than a trade. Celebrate the memory, not the magnitude. |
| **Progress rings, ECG motion, "zones"** | goal gradient, aesthetics | Brand NEVERs 2, 3 and 6. |
| **Value-blurred paywalls** ("see who was in sync with you — upgrade") | curiosity gap | Reads as manipulation; the brief's tiebreaker is fun, not trust, but never at the person's expense. |
| **Invented social proof** — counts, "friends confirmed", a feed of fake people | social proof, bandwagon | Illegal in the EU/US, abusive under CDC Art. 37, and the 18/09 rule already: nothing invented reaches a fan's hand. |
| **Pre-ticked sharing or consent** | default effect | LGPD Art. 8 and 11; "Compartilhar é sempre ativo" is the rule and the promise. |
| **Loot boxes, surprise mechanics tied to re-opening** | variable reward | ECA Digital bans them for minors; the night is already the surprise. |

---

## 7. What the pilot can measure

Three to five people cannot A/B anything. What they can do is count, and be
asked. For each mechanic above, the honest signal:

| Question | Signal | Mechanic |
|---|---|---|
| Did the first value arrive? | Minutes from install to the first curve on screen | §5.1 |
| Did the night become a card? | Nights with a card chosen ÷ nights captured | activation |
| Did the card leave the phone? | **Sends per night, and to whom** — the share sheet fires an intent; whether it was sent is a question to ask, not a number to log | §5.5, item 25 |
| Which card? | Skin and format chosen per send | item 25 |
| When did they want to see it? | Reveal opened within an hour of the end ÷ opened after 10:00; and the question "quando você quis ver?" | §5.4 |
| Did they come back? | Gallery opened in the seven days after the event | collection |
| Did the story exist? | Moments with a name ÷ moments shown | §5.6 |
| Did the app lie? | Every sentence on screen that a person read as false — the log's running count | feedback loops |

The one number that decides the product is the third: **whether a stranger
sends the card to somebody.** Everything else in this document is in service
of it.

---

## 8. Sources

Primary papers and the best secondary write-ups are linked inline in Parts
2 and 3. The two research files this document condenses were compiled on
2026-09-19 from web search only — the network policy of the environment
blocked full-page fetches of most primary sites (journals, growth.design,
lawsofux.com, nngroup.com, the Spotify and Strava newsrooms, YouTube) — so
every number was cross-checked across at least two search-surfaced sources
where possible and flagged *reported* or *[weak]* where not. Before any of
these numbers appears in public copy or a deck, read the primary.

Index sites worth keeping open: [growth.design/psychology](https://growth.design/psychology)
(a catalogue of ~100 principles with app case studies), [lawsofux.com](https://lawsofux.com),
[deceptive.design](https://www.deceptive.design/types), [coglode.com](https://www.coglode.com).
