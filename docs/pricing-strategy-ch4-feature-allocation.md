# TumTum Pricing Strategy — Chapter 4: Feature Allocation Matrix

---

## 4.1 Complete Feature Allocation Table

| # | Feature | TumTum (Free) | TumTum Pro | Microtx (non-sub) | Justification |
|---|---------|:------------:|:----------:|:-----------------:|---------------|
| **CORE — Data & Visualization** | | | | | |
| 1 | Connect wearable (Apple Watch, Fitbit, Garmin, Galaxy Watch) | Unlimited | Unlimited | — | Table stakes. Paywalling device connection = zero adoption. |
| 2 | Record HR during events | Unlimited | Unlimited | — | Core data collection must be free. Users contribute data. |
| 3 | View HR curve synced to event timeline | Full | Full | — | The "aha moment." Gating this = users never understand the value. |
| 4 | See detected peaks | Top 3 per event | All peaks (unlimited) | — | Free shows enough for the "wow." Pro reveals the full picture for data lovers. |
| 5 | Event timeline (setlist / match events) | Full | Full | — | Context is essential. HR curve without timeline is meaningless. |
| 6 | Session stats (avg/max/min BPM, duration) | Full | Full | — | Basic stats reinforce engagement. No reason to gate. |
| **SHARE CARDS** | | | | | |
| 7 | Basic share card (1 per event) | 1 card, basic template, with watermark | Unlimited cards, all templates, no watermark | R$ 4,90 per premium card | Free card powers virality (watermark = brand exposure). Premium cards drive upgrades. |
| 8 | Premium card templates | Not available | All templates included | R$ 4,90 per card (includes template) | Visual quality is the #1 differentiator. Users see premium templates and want them. |
| 9 | Animated / video cards | Not available | Included | R$ 6,90 per animated card | Animated cards get 3-5x social engagement. High perceived value. |
| 10 | Card customization (choose peak, colors, layout) | Not available | Full customization | Not available | Customization is a Pro power-user feature. Keeps the upgrade path clear. |
| 11 | HD export (1080p+) | Standard quality (720p) | Full HD / 4K | Included in premium card purchase | HD matters for Instagram/TikTok quality. Subtle but noticeable difference. |
| 12 | Watermark removal | Watermark present | No watermark | Included in any microtx card | The #1 single conversion trigger based on Canva/Strava patterns. |
| **SOCIAL & COMPARISON** | | | | | |
| 13 | Share to social media (IG, TikTok, X, WhatsApp) | Available (for free cards) | Available (for all cards) | Available (for purchased cards) | Sharing mechanism must always work. It's the viral engine. |
| 14 | Friend comparison (side-by-side HR at same event) | Not available | Included | Not available | Competitive feature that drives group upgrades. "Both of you need Pro to compare." |
| 15 | Event leaderboard (highest peak at event) | View only (see your rank) | Full leaderboard + your position | — | Free users can see they're #47 of 200. Pro users see the full ranking + friends. |
| 16 | Artist/athlete HR comparison (Phase 1) | Not available | Included | R$ 7,90 per event unlock | THE killer premium feature. Exclusive, shareable, emotionally powerful. |
| 17 | Sync percentage with artist/athlete | Not available | Included | Included with comparison unlock | Part of the comparison package. The number everyone will share. |
| **HISTORY & COLLECTIONS** | | | | | |
| 18 | Event history | Last 5 events | Unlimited history | — | 5 events is enough for new users. Over time, losing history creates upgrade pressure. |
| 19 | Card gallery (saved cards) | Last 10 cards | Unlimited gallery | — | Same logic. Recent cards are free. Full archive is Pro. |
| 20 | Peak highlights reel (auto-generated video) | Not available | Included (monthly + yearly) | — | Premium content generation feature. Great for year-end viral moments. |
| 21 | Export data (CSV/JSON of your HR data) | Not available | Included | — | Power user feature. Respects data ownership for paying customers. |
| **EXPERIENCE** | | | | | |
| 22 | Priority card generation (skip queue) | Standard queue | Priority processing | — | During high-traffic moments (post-match), free users wait 2-5 min. Pro: instant. |
| 23 | Push notifications (event reminders, card ready) | Basic | Advanced (peak alerts, friend activity) | — | Free gets functional notifications. Pro gets social/engagement notifications. |
| 24 | Onboarding & wearable setup | Full support | Full support | — | Everyone gets great onboarding. Bad onboarding = uninstall. |

---

## 4.2 Feature Allocation by Segment

### Soccer Fans — What Matters Most

```
FREE tier value for soccer fans:
├── ✅ Record HR during match (90 min + extra time)
├── ✅ See HR curve with goals, cards, halftime marked
├── ✅ Top 3 peaks (usually = top 3 emotional moments of the match)
├── ✅ 1 basic card per match (e.g., "My heart hit 165 BPM at the derby")
└── ✅ Share on WhatsApp/Instagram

PRO upgrade triggers for soccer fans:
├── 🔥 Friend comparison ("Who felt the goal more?")
├── 🔥 Season history (every match of the Brasileirao)
├── 🔥 All peaks (see EVERY emotional moment, not just top 3)
├── 🔥 Premium cards (club-colored templates, animated goal moments)
├── 🔥 Event leaderboard ("I was the #3 most excited person at Neo Quimica Arena")
└── 🔥 Player comparison — Phase 1 ("85% in sync with the goalkeeper during penalties")
```

### Concert Fans — What Matters Most

```
FREE tier value for concert fans:
├── ✅ Record HR during show (2-3 hours)
├── ✅ See HR curve with setlist songs marked
├── ✅ Top 3 peaks (your most emotional songs)
├── ✅ 1 basic card per show ("My heart peaked at 172 BPM during Evidencias")
└── ✅ Share on Instagram Stories/TikTok

PRO upgrade triggers for concert fans:
├── 🔥 Animated cards (HR curve pulsing to the beat — TikTok gold)
├── 🔥 Premium templates (artist-themed, festival-branded)
├── 🔥 No watermark (clean aesthetic for Instagram grid)
├── 🔥 Multiple cards per show (one per favorite song)
├── 🔥 Artist comparison — Phase 1 ("78% in sync with Anitta")
└── 🔥 Highlights reel (auto-generated video of your top moments)
```

---

## 4.3 Feature Gating Rules (Engineering Implementation Guide)

### Gating Logic

```python
# Simplified feature gating logic for backend

class FeatureGate:
    FREE_LIMITS = {
        "peaks_per_event": 3,
        "cards_per_event": 1,
        "card_templates": ["basic"],
        "event_history": 5,        # last 5 events
        "card_gallery": 10,        # last 10 cards
        "card_quality": "720p",
        "watermark": True,
        "friend_comparison": False,
        "artist_comparison": False,
        "animated_cards": False,
        "card_customization": False,
        "highlights_reel": False,
        "data_export": False,
        "priority_generation": False,
        "full_leaderboard": False,
    }
    
    PRO_LIMITS = {
        "peaks_per_event": None,   # unlimited
        "cards_per_event": None,   # unlimited
        "card_templates": ["all"],
        "event_history": None,     # unlimited
        "card_gallery": None,      # unlimited
        "card_quality": "4k",
        "watermark": False,
        "friend_comparison": True,
        "artist_comparison": True,
        "animated_cards": True,
        "card_customization": True,
        "highlights_reel": True,
        "data_export": True,
        "priority_generation": True,
        "full_leaderboard": True,
    }
```

### Microtransaction Unlock Logic

```python
# When a free user tries to access a Pro feature, check if they
# have a one-time purchase (microtransaction) for that specific item.

class MicrotxUnlock:
    PURCHASABLE = {
        "premium_card": {
            "price_brl": 4.90,
            "scope": "single_card",      # unlocks one premium card
            "includes": ["premium_template", "no_watermark", "hd_export"],
        },
        "animated_card": {
            "price_brl": 6.90,
            "scope": "single_card",
            "includes": ["animation", "premium_template", "no_watermark", "hd_export"],
        },
        "card_bundle_3": {
            "price_brl": 9.90,
            "scope": "3_cards",
            "includes": ["premium_template", "no_watermark", "hd_export"],
        },
        "artist_comparison_event": {
            "price_brl": 7.90,
            "scope": "single_event",     # unlocks comparison for one event
            "includes": ["artist_hr_comparison", "sync_percentage"],
        },
    }
```

---

## 4.4 Anti-Patterns to Avoid

| Anti-Pattern | Why It's Bad | TumTum's Approach |
|-------------|-------------|-------------------|
| Gating the HR curve itself | Users contributed their biometric data. Hiding it behind a paywall feels exploitative and likely violates trust. | HR curve is always free. Full data visibility. |
| Limiting events per month on free tier | Creates artificial scarcity on the data collection side. Users stop wearing the device = less data = less engagement. | Unlimited event recording on all tiers. |
| Requiring Pro to share at all | Sharing IS the growth engine. Blocking it = killing virality for short-term revenue. | Free users can always share (with watermark). |
| Hard paywall on all card creation | If free users can't create ANY card, they never experience the value. | 1 free basic card per event. Always. |
| Too many microtransaction options | Decision fatigue. "Do I want the R$ 4,90 or R$ 6,90 or R$ 9,90?" | Max 4 microtx options. Clear differentiation. |
| Feature-gating push notifications | Notifications drive retention. Blocking them blocks re-engagement. | Basic notifications are free. |

---

## 4.5 Upsell Touchpoints in the User Journey

| Moment | What User Sees | Upsell CTA |
|--------|---------------|------------|
| After peak detection (3 peaks shown) | "Voce teve 8 picos nesse evento. Veja todos com TumTum Pro." | Soft banner below peak list |
| Card generation (basic card preview) | Premium card preview blurred next to basic card. "Desbloqueie este card por R$ 4,90 ou assine o Pro." | Side-by-side comparison |
| Trying to remove watermark | "Cards sem marca d'agua com TumTum Pro ou por R$ 4,90 avulso." | Modal with two options |
| Friend attended same event | "Voce e @amigo estiveram no mesmo show! Compare seus batimentos com TumTum Pro." | Push notification + in-app card |
| Event history (6th event) | "Seu historico completo esta no TumTum Pro. Voce tem 12 eventos salvos." | Banner on history page |
| Artist posts their HR (Phase 1) | "Anitta compartilhou os batimentos dela no show de ontem! Veja sua sincronia com TumTum Pro." | Push notification + locked comparison card |

---

*Next: Chapter 5 — Artist/Athlete Revenue Share Model*
