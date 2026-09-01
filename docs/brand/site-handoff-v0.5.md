# Handoff: TumTum Website (tumtum.cc) — v0.5 redesign (desktop + mobile)

## Overview
Full redesign of TumTum's public landing page (tumtum.cc) applying the v0.4 brand palette (TumTum Pink as a large surface) and adding three new product sections: the Event Feed, The Crowd stats, and the Gallery of Feelings. Bilingual PT/EN with a language switcher in the nav. This update adds the complete MOBILE design (≤480px) and a copy change on the waitlist CTA.

## About the Design Files
The files in this bundle are **design references built in HTML** — prototypes showing intended look and behavior, NOT production code to copy directly. The task is to **recreate these designs in TumTum's existing website codebase** (the site live at tumtum.cc), using its established patterns and libraries. `TumTum-Site.dc.html` (desktop) and `TumTum-Site-Mobile.dc.html` (mobile) use a proprietary design runtime (<x-dc>, <sc-if> tags, {{ holes }}) — read it as a markup + inline-style spec, not as final HTML.

## Fidelity
**High-fidelity.** Colors, typography, spacing and copy are final and should be recreated faithfully. Desktop measurements are ~1440px. **Mobile is now fully designed** (reference at 390px, max-width 480px): implement ONE responsive page whose layout matches TumTum-Site.dc.html at ≥1024px and TumTum-Site-Mobile.dc.html at ≤480px; between the two, interpolate sensibly (stack the hero first, then the ticker, keep the card carousel from tablet down or wrap the 4 cards 2×2).

## Mobile design (TumTum-Site-Mobile.dc.html) — key differences from desktop
- **Nav (sticky, z-10):** wordmark 96px + "Entrar na lista" pill + a text "MENU" button (12px/700, letter-spacing .1em — the brand uses text labels, not invented icons). No inline links, no inline PT/EN.
- **Menu panel:** opens under the nav (sticky top:69px, black background, 24px side padding). Compact: links 13px/600 white, 8px vertical padding, no dividers (Como funciona, Os cards, O feed, Galeria, Entrar in #8A8A8A); below, two small language buttons "PT" / "EN" (10px, active = #FF6F91 bg + black text, inactive = #2E2E2E border + #8A8A8A). Any link click closes the menu. Language choice also closes it.
- **Hero:** stacked — H1 52px (PT) / 50px (EN), sub 17px, both CTAs full-width (primary black, secondary 2px black border), then the 9:16 moment card centered below (280×498, video top 238px, number 112px).
- **Social-proof strip:** the 3 blocks become full-width horizontal rows (number 48px left, caption + meta right), stacked black → yellow → white.
- **How it works:** the 3 steps stack vertically (same #0F0F0F blocks, 2px gap; numbers 36px, titles 18px, body 14px).
- **The cards:** horizontal swipe carousel — overflow-x auto, scroll-snap-type x mandatory, cards fixed 236×412 with scroll-snap-align start, 12px gap, 24px side padding, hidden scrollbar; intro copy ends with a "Arrasta pro lado →" / "Swipe →" hint in #8A8A8A.
- **Feed:** stacked; the two app screenshots sit side by side (each ~50%, max 200px, radius 16, second offset 32px down); stats below a #2E2E2E hairline (numbers 24px).
- **Gallery:** copy first, then the 2×2 poster grid full-width (1fr 1fr, gap 18/14px, same rotations, offsets +20/-8/+12px, numbers 44px, shadows 0 20px 40px).
- **Waitlist:** left-aligned, H2 40px, input and button stacked full-width.
- **Footer:** stacked column — wordmark, social links row (wrap), privacy note, then a final hairline row with copyright + sign-in.
- **Section padding mobile:** 72px vertical, 24px side gutters (56-64px on dark utility sections).

## Languages (new requirement)
- Two complete content versions: PT (default) and EN.
- PT/EN switcher in the nav: discreet text-only control "PT / EN" (12px). Active language: #000, weight 700, underline (3px offset); inactive: #B4B4B4, weight 500; both darken to #000 on hover; "/" divider in #E6E6E6.
- Choice persists (prototype: localStorage key `tt-site-lang`; in production prefer an /en route or cookie + hreflang for SEO).
- All copy for both languages lives in the reference file, side by side in two <sc-if> blocks. Treat it as the canonical string source.

## Screens / Views (page order)
1. **Nav** — white, 1px #E6E6E6 bottom border, 22px 64px padding. Black wordmark 132px left. Links (14px/600): How it works, The cards, The feed, Gallery, Sign in (gray #8A8A8A — this is the sign-in link the current site lacks), "Join the list" CTA (#FF6F91 pill, #000 text, hover #EFFF00), PT/EN switcher.
2. **Hero** — full #FF6F91 background, 88px 64px 96px padding (no eyebrow chip). H1 clamp(54px, 6vw, 88px)/700 (each line must never wrap — nowrap), -0.035em tracking, 0.94 line-height, BLACK: "YOU FELT IT. / NOW YOU HAVE / PROOF." Sub 20px rgba(0,0,0,.75). CTAs: primary black "Quero no meu próximo evento" / "I want this at my next event" (hover #EFFF00/black), secondary 2px black border (hover inverts). Right: 9:16 card (310×551, #000 background, shadow 0 40px 80px rgba(0,0,0,.28)) with event video in the top 265px (production: <video> muted loop playsinline), yellow event chip, white plate below with "I WAS FINE. / THEN THIS.", 126px black "187", 74px black wordmark.
3. **Social-proof strip** — 3 equal flex columns: black (#FF6F91 number "176"), yellow #EFFF00 (black "191"), white (black "183"). Numbers 64px/700, -0.04em, tabular. Caption 15px/600 + meta 12.5px.
4. **What TumTum does** — white, centered, max 860px. Eyebrow 12px/600 .16em #8A8A8A. H2 52px/700 with an #EFFF00 <span> highlight.
5. **How it works (#como)** — black, 3 steps as #0F0F0F columns with a 2px gap. Numbers 44px #FF6F91, titles 20px white, body 15px #8A8A8A.
6. **The cards (#cards)** — white, centered. Four equal-width cards in one row that shrink together (flex:1 1 0; min 170px, max 248px; height 412px; overflow hidden on all four; 14px gap; row max-width 1040px):
   - "Just the moment" (white, #E6E6E6 border): event GIF/video fills the top 226px with the format label as a white-on-rgba(0,0,0,.6) chip over it; below on white: copy 13px/700 on two fixed lines ("AQUI ACABOU / MEU PSICOLÓGICO." · "THIS IS WHERE / I LOST IT."), number 72px/700 black, meta 11px.
   - "My night" (black): copy 15px white, number 72px #FF6F91, CONTINUOUS SVG sparkline (single path, stroke #FF6F91 8/1000 viewBox, round caps) with the yellow peak dot inset from the edge so it renders whole (r15 at x≈978 of 1000).
   - "The crowd" (pink #FF6F91): crowd GIF/video top 226px with label chip; below on pink: copy, then YOU 187 (34px black) vs THE CROWD 172 (34px rgba(0,0,0,.45)), caption.
   - "Same vibe · coming soon" (yellow #EFFF00): all-black type, number 72px.
   Disclaimer under the row: 12.5px #8A8A8A.
7. **The event feed (#feed)** — black. Two app screenshots (264px wide, radius 22, #2E2E2E border, second offset 56px down). Copy right, max 520px; FELT IT TOO highlighted in #EFFF00; two stats with 26px #FF6F91 numbers ("8,734 people…", "64% hit their own peak during the same song — every body has its own number, never a ranking"). BRAND RULE: community stats always state exactly what they measure; never a BPM ranking across people.
8. **Gallery of feelings (#galeria)** — #FF6F91 background. Copy left (52px black H2; 14/312/187 stats). 2×2 grid of 196px poster-minis, 9:14 aspect, gap 22/18px, each slightly rotated (-2° / +1.6° / +1.2° / -1.6°) with vertical offsets (+30 / -14 / +18px) and shadow 0 28px 56px rgba(0,0,0,.28):
   - Lolla: event GIF background + bottom scrim gradient (transparent 34% → rgba(0,0,0,.88) 84%), time chip #EFFF00/black top-left, number 56px #FF6F91, label 10px white.
   - Palmeiras: crowd GIF + scrim, chip #FF6F91/black "94:12", number 56px white.
   - Coldplay: red-crowd GIF + scrim, chip #EFFF00 "21H03", number 56px white.
   - Realness: solid #EFFF00, chip black with #EFFF00 text "01H24", number 56px black.
   Numbers are pink ONLY over the dark scrim; on light surfaces they are black. Different skin per night = the Mutante Pop concept.
9. **Waitlist (#lista)** — black, centered. H2: "Quero isso no meu próximo evento." / "I want this at my next event." 340px input (#0F0F0F, #2E2E2E border, radius 12) + #FF6F91 button. Privacy note 12.5px #6F6F6F. The form must post to the existing email-only waitlist.
10. **Footer** — black, #1E1E1E top border. White wordmark 110px, social links 13px #8A8A8A (hover #FF6F91): Instagram, TikTok, X, LinkedIn, oi@tumtum.cc. Privacy line 12px #4A4A4A max 420px. Final row: "TumTum · São Paulo · 2026" + "Sign in to your account" link.

## Interactions & Behavior
- Smooth anchors to #como, #cards, #feed, #galeria, #lista.
- Hovers exactly as specified (solid color swaps; suggest 120–200ms ease-out).
- Language switcher swaps ALL text content and persists the choice.
- Hero video: autoplay muted loop playsinline; fallback = static frame.
- No scroll animations; the brand forbids any pulse/heartbeat/ECG motion.

## State Management
- `lang: 'pt' | 'en'` (persisted).
- Waitlist form: submitting + success/error states (error copy in brand voice, never blaming the user).

## Design Tokens
- Colors: #000000, #FFFFFF, TumTum Pink #FF6F91, Toxic Yellow #EFFF00; neutrals #0F0F0F, #1E1E1E, #2E2E2E, #4A4A4A, #6F6F6F, #8A8A8A, #B4B4B4, #E6E6E6.
- HARD RULES: never white text on pink (2.65:1) or on yellow (1.11:1); on pink/yellow everything is black; a number is never pink outside a black background; the wordmark is only ever the black or white SVG asset, never typed text.
- Type: Instrument Sans (Google Fonts, weights 400/500/600/700). Tabular figures on every number (font-variant-numeric: tabular-nums).
- Scale used: 88/64/60/52/44/40/34/26/20/17/15/14/13/12.5/12/11/10px. Tracking: heroes -0.035em, H2 -0.03em, numbers -0.04 to -0.055em, eyebrows +0.16em.
- Radius: 0 on content surfaces/cards; 12px on buttons and inputs; 999px on pills (nav CTA, switcher). Spacing: multiples of 4; 64px side gutters; 100–120px section padding.
- Shadow: hero card only (0 40px 80px rgba(0,0,0,.28)).

## Assets (in this bundle's assets/ folder)
- tumtum-wordmark-black.svg / tumtum-wordmark-white.svg — the ONLY approved logo forms. Never recreate with a font. (Note: the current vector is a polygonal trace; request a curve export from the brand designer before large-format use.)
- event-clip.gif — hero card + Lolla gallery mini.
- torcida.gif — "The crowd" card + Palmeiras gallery mini (already trimmed: last 2s removed).
- cold-play.gif — "Just the moment" card + Coldplay gallery mini.
- The reference file appends ?v=2 to gif URLs for cache-busting during design review — drop that in production, and convert ALL THREE gifs to muted looping MP4/WebM (<video autoplay muted loop playsinline>): the gifs total ~20MB, far too heavy to ship.
- shots/09-feed.png, shots/10-feed-evento.png — app mockup screenshots used in the feed section (replace with updated captures as the app evolves).

## Files
- TumTum-Site.dc.html — the complete desktop design reference (PT + EN).
- TumTum-Site-Mobile.dc.html — the complete mobile design reference (PT + EN), including the MENU panel behavior.
- assets/ — logos, clip and screenshots.

## Going live with Claude Code
1. Download this zip and drop the folder into the current website repo.
2. In the repo terminal run `claude` and ask: "Read design_handoff_site_tumtum/README.md and recreate the landing page in our stack as ONE responsive page (desktop + mobile references included), replacing the current home. Keep the existing waitlist and routing; add the /en route and the language switcher."
3. Review the diff, run locally, ship through your normal deploy flow.
