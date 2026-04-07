# TumTum Pricing Strategy — Chapter 1: Executive Summary & Pricing Model Analysis

> Document version: 1.0 | Date: April 2026 | Market: Brazil
> Segments: Sports (Soccer) & Entertainment (Concerts)

---

## 1.1 Executive Summary

TumTum transforms live event emotions into shareable content by capturing heart rate data during concerts and soccer matches. This document defines the optimal pricing strategy for the Brazilian market, balancing simplicity, virality, and revenue growth.

**Core recommendation: Freemium + Hybrid Monetization**

- A generous free tier that powers the viral loop (share cards on social media)
- A single premium subscription tier at **R$ 14,90/month** (or R$ 119,90/year)
- Microtransactions for non-subscribers (R$ 4,90 per premium card)
- Revenue share with artists/athletes at **75/25 split** (artist gets 75%)
- B2B data insights as a future revenue stream

**Why this model wins for TumTum:**

| Criteria | Recommendation | Rationale |
|----------|---------------|-----------|
| Simplicity | 2 tiers (Free + Pro) | No decision fatigue. Brazilian consumers abandon complex pricing. |
| Virality | Free cards with watermark | Every shared card = free marketing. Paywall kills virality. |
| Revenue capture | Subscription + microtx | Captures both recurring and impulse-buy revenue. |
| Scalability | Usage grows with events attended | More events = more cards = more value = higher willingness to upgrade. |

---

## 1.2 Pricing Model Analysis

### Models Evaluated

We evaluated five pricing models against TumTum's specific context: a Brazilian consumer app used periodically (event-driven, not daily), where virality via social sharing is the primary growth engine.

#### Model 1: Pure Subscription (Monthly/Annual)

| Pros | Cons |
|------|------|
| Predictable recurring revenue | Hard to justify for periodic use (1-4 events/month) |
| Simple to understand | High churn risk between event seasons |
| Industry standard | Subscription fatigue in Brazil (avg consumer has 2.4 paid subs) |

**Verdict:** Partially adopted — subscription is one revenue pillar, but not the only one.

#### Model 2: Per-Event / Per-Card (Pure Transactional)

| Pros | Cons |
|------|------|
| Pay-for-what-you-use, feels fair | Unpredictable revenue |
| Low commitment barrier | No recurring revenue base |
| Works well for infrequent users | Power users feel punished |

**Verdict:** Partially adopted — microtransactions complement subscriptions for casual users.

#### Model 3: Freemium with Feature Gating

| Pros | Cons |
|------|------|
| Massive top-of-funnel | Must carefully choose what to gate |
| Viral loop stays intact on free tier | Risk of "good enough" free tier killing conversions |
| Industry-proven in Brazil (Cartola FC, Spotify) | Requires discipline in feature allocation |

**Verdict:** Adopted as the core model. Free tier must be useful but leave clear upgrade triggers.

#### Model 4: Tiered Subscription (3+ tiers)

| Pros | Cons |
|------|------|
| Captures different willingness-to-pay segments | Adds complexity |
| Standard SaaS playbook | Overkill for a consumer app in Phase 0 |
| Allows upsell path | Brazilian consumers don't comparison-shop 3 tiers |

**Verdict:** Rejected for Phase 0. A single premium tier is simpler. Can add tiers later when user segments are validated with real data.

#### Model 5: Ad-Supported Free Tier

| Pros | Cons |
|------|------|
| Monetizes non-paying users | Brazilian mobile CPMs are low (R$ 3-8 CPM) |
| Familiar model | Ads destroy the premium, emotional brand feel |
| Can generate meaningful revenue at scale | Intrusive during intimate "relive your moment" experience |

**Verdict:** Rejected. TumTum's brand is "premium, emotional, nocturnal." Ads break this completely. Revenue-per-user from ads would be negligible vs. subscriptions.

---

## 1.3 Recommended Model: Freemium + Hybrid Monetization

```
                    ┌─────────────────────────────┐
                    │        FREE TIER             │
                    │   (Viral engine — 90-95%     │
                    │    of users stay here)       │
                    └──────────┬──────────────────┘
                               │
                 ┌─────────────┼─────────────────┐
                 │             │                  │
                 v             v                  v
          ┌────────────┐ ┌──────────┐    ┌──────────────┐
          │ TUMTUM PRO │ │ MICRO TX │    │  B2B DATA    │
          │ R$14,90/mo │ │ R$4,90/  │    │  (Future)    │
          │ 5-8% conv  │ │  card    │    │  Event       │
          │            │ │ 10-15%   │    │  promoters   │
          │            │ │ of free  │    │              │
          └────────────┘ └──────────┘    └──────────────┘
```

**Why this hybrid works for TumTum specifically:**

1. **Event-driven usage pattern.** Unlike Spotify (daily use), TumTum is used around events. A pure subscription feels like paying for nothing between events. The microtransaction option captures value from occasional users who attend 1-2 events per quarter.

2. **Virality depends on free sharing.** The share card is TumTum's #1 growth mechanism. If you paywall card generation entirely, growth dies. Free users MUST be able to create and share cards.

3. **Brazilian market realities.** With minimum wage at R$ 1.518/month, a R$ 14,90 subscription = ~1% of minimum wage. This is the ceiling for a non-daily-use app. Microtransactions at R$ 4,90 sit in the impulse-buy zone.

4. **Value scales with usage.** Users who attend more events generate more cards, hit free limits more often, and have stronger motivation to subscribe. The model self-selects: power users subscribe, casual users buy individual cards or stay free.

---

## 1.4 Value Capture at Scale

As TumTum's user base grows, revenue scales through multiple levers:

| Scale Phase | Users | Primary Revenue | Secondary Revenue |
|------------|-------|----------------|-------------------|
| **Launch** (0-50K) | Early adopters | Microtransactions | — |
| **Growth** (50K-500K) | Mainstream fans | Subscriptions (5-8% conversion) | Microtransactions |
| **Scale** (500K-2M) | Mass market | Subscriptions + micro tx | B2B data insights, sponsored cards |
| **Dominance** (2M+) | Platform | All consumer revenue | Artist/athlete partnerships, branded experiences |

**Key insight:** The subscription model captures MORE value as customers scale because:
- More events attended per user = more cards = more reasons to subscribe
- Network effects: friends comparing heart rates creates social pressure to upgrade
- Seasonal patterns (Brasileirao season, summer festival season) create natural subscription triggers
- Artist comparison feature (Phase 1) adds exclusive value that justifies premium pricing

---

*Next: Chapter 2 — Customer Value Analysis & Willingness to Pay*
