# TumTum — building with this library

TumTum is a Brazilian product for **concerts and football matches**: it captures
how someone reacted during an event and turns it into something they post. The
category is emotional memory and culture, measured with a heart rate.

> **It is not healthtech, fitness, wellness, sleep, recovery, or medical
> software.** That is the one failure mode that ruins the brand, and it creeps
> in through small choices — a clinical-looking chart, a word like "zone", a
> progress ring, a reassuring tone. If a screen could sit inside a fitness app
> unnoticed, it is wrong.

**UI text is Brazilian Portuguese.** Short, spoken, the person as the subject —
*"Seu coração foi a 187"*, never *"Detectamos um pico de 187"*. No medical
advice, no diagnosis, no normal/abnormal framing.

## Setup: no provider, but the ground is black

There is no theme provider and no context wrapper — components are standalone
and style themselves. What they assume is the **canvas**: `styles.css` sets
`body { background-color: #000; color: #fff }`, and every component is drawn
for that ground. Render one on a white page and low-contrast text disappears.

Put your own layout on a black surface:

```jsx
<div className="min-h-screen bg-tumtum-black text-tumtum-white">
  {/* screen content */}
</div>
```

Type is **Instrument Sans**, expected as `--font-instrument-sans`; it falls back
to `system-ui` when the host does not provide it.

## The styling idiom: Tailwind with a small named palette

Style with **Tailwind utility classes**. Never invent hex values — every colour
in this system has a token, and there are only these:

| Token | Where it goes |
|---|---|
| `tumtum-black` | The canvas, and text on the accents |
| `tumtum-white` | Text and inverse surfaces |
| `tumtum-pink` | **Primary accent** — key numbers, selected states, main emphasis |
| `tumtum-yellow` | Secondary accent — labels, CTAs, markers, highlights |
| `tumtum-surface` | Raised panel (white at 5% over black) |
| `tumtum-border` | Hairline separation (white at 14%) |
| `tumtum-muted` | Supporting text (white at 60%) |
| `tumtum-faint` | Captions, legal lines (white at 34%) |

They combine with the usual prefixes: `bg-`, `text-`, `border-`, `from-`, `to-`.

**Two contrast rules that are not negotiable.** Never white on `tumtum-pink`
(2.65:1) or on `tumtum-yellow` (1.11:1) — both accents take **black** text.
`tumtum-pink` on black is 7.93:1, which passes, but it is only half as loud as
the accent it replaced: reach for **scale** when something needs to shout, never
a brighter tint.

Weights are named for their role, so use these rather than `font-bold`:
`font-hero` (700, big numbers), `font-headline` (600), `font-label` (500, UI),
`font-body` (400). Aligned figures get `tabular-nums`.

## Where the truth lives

- **`_ds/<folder>/styles.css`** and its `@import` closure — the compiled
  stylesheet, the full utility vocabulary.
- **`components/<group>/<Name>/<Name>.d.ts`** — the real prop contract.
- **`components/<group>/<Name>/<Name>.prompt.md`** — per-component usage.

Groups: `general` (Button, Badge, Card, Input, PasswordInput, Modal, Avatar,
Loading, SignInRequired, ErrorBoundary), `hr` (HRCurve, PeakMarker,
TimelineBar), `cards` (SoloCard, ComparisonCard), `layout` (Nav, Footer),
`marketing` (LandingNav, WaitlistForm, VideoSlot, Reveal), `brand` (Wordmark),
`events` (EventForm).

## The data rule, which is a design rule here

A heart-rate line may appear **only when it is the user's real data and the
chart is product information** — `HRCurve` exists for that. Simple BPM over
time, `tumtum-pink` for the line, `tumtum-yellow` for the marked moment.

**Never** use ECG, heartbeat or waveform shapes as decoration — not as a
divider, background, loader, or motion signature. **Never** add heart zones,
risk colours, normal ranges or recovery scores. A gap in a capture is drawn as
a gap; the line breaks rather than crossing minutes nobody measured.

## The logo is a locked asset

`Wordmark` renders the approved TUMTUM wordmark. **Do not typeset the word
"TUMTUM" in any other font** — the logo face is licensed with an explicit
AI-use restriction, so a lookalike is a legal problem, not just an off-brand
one. Use `<Wordmark />` or leave a marked placeholder. It is black, white, or
an approved skin — never a flat coloured wordmark.

In shareable content **the number is the hero; the logo signs and stays small.**

## One idiomatic screen

```jsx
<div className="min-h-screen bg-tumtum-black px-6 py-10 text-tumtum-white">
  <p className="font-label text-sm text-tumtum-muted">Realness Festival 2026</p>
  <p className="mt-2 font-hero text-7xl tabular-nums text-tumtum-pink">
    116 <span className="font-label text-2xl text-tumtum-yellow">bpm</span>
  </p>
  <div className="mt-8 rounded-2xl border border-tumtum-border bg-tumtum-surface p-5">
    <Badge variant="accent">20 momentos</Badge>
    <p className="mt-3 font-body text-tumtum-muted">
      Sua noite foi 78. Às 01h24, não foi.
    </p>
  </div>
  <Button variant="primary" size="lg" className="mt-8 w-full">
    Compartilhar
  </Button>
</div>
```
