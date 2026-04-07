# TumTum Pricing Strategy — Chapter 6: Financial Projections & Unit Economics

---

## 6.1 Unit Economics Summary

### Revenue Per User Per Month (RPUPM) by Segment

| User Type | % of Users | Monthly Revenue | Calculation |
|-----------|:----------:|:---------------:|-------------|
| Free (no purchase) | 75-80% | R$ 0 | Viral value only (sharing cards = free marketing) |
| Free + microtx (occasional buyer) | 12-15% | R$ 4,90-7,90 | ~1 premium card or comparison per month |
| Pro Monthly | 3-5% | R$ 14,90 | Full subscription |
| Pro Annual | 3-5% | R$ 9,99 (effective) | R$ 119,90/year billed annually |

### Blended ARPU (Average Revenue Per User)

```
Scenario: 100,000 Monthly Active Users (MAU)

User breakdown:
├── 78,000 free users (78%)              = R$ 0
├── 12,000 occasional buyers (12%)       = R$ 5,90 avg x 12,000 = R$ 70,800
├── 4,000 Pro Monthly (4%)               = R$ 14,90 x 4,000 = R$ 59,600
├── 6,000 Pro Annual (6%)                = R$ 9,99 x 6,000 = R$ 59,940
│
├── TOTAL Monthly Revenue:                 R$ 190,340
├── Blended ARPU (all users):              R$ 1,90/month
├── Blended ARPU (paying users only):      R$ 8,65/month
└── Paid conversion rate:                  22% (10% subscription + 12% microtx)
```

### Customer Lifetime Value (LTV)

| User Type | Monthly Revenue | Avg Lifetime | LTV | LTV After Costs |
|-----------|:--------------:|:------------:|:---:|:---------------:|
| Free | R$ 0 | 8 months | R$ 0 | R$ 0 (but drives virality) |
| Microtx buyer | R$ 5,90 | 6 months | R$ 35,40 | R$ 28,32 (80% margin) |
| Pro Monthly | R$ 14,90 | 5 months (high churn) | R$ 74,50 | R$ 59,60 (80% margin) |
| Pro Annual | R$ 9,99 | 14 months (low churn) | R$ 139,86 | R$ 111,89 (80% margin) |

**Key insight:** Annual subscribers have 2x the LTV of monthly subscribers despite paying 33% less per month. This is because annual billing dramatically reduces churn (no monthly "should I cancel?" decision point).

---

## 6.2 Revenue Projections — 24 Month Forecast

### Assumptions

| Variable | Value | Basis |
|----------|-------|-------|
| Launch date | Q3 2026 | After Phase 0 MVP validation |
| Initial users (Month 1) | 5,000 | Beta launch with 2-3 event partnerships |
| Monthly user growth rate | 25-40% (early), 15-20% (mature) | Viral coefficient from card sharing |
| Pro subscription conversion | 8% of MAU (at maturity) | Benchmark: Strava ~10%, health apps avg ~5% |
| Microtx conversion | 12% of free users (per month) | Benchmark: gaming apps 5-15%, FutebolCard ~10% |
| Monthly churn (Pro Monthly) | 12% | Health/fitness app average |
| Annual churn (Pro Annual) | 25% yearly (~2.3% monthly) | Better than industry avg due to event seasons |
| Annual:Monthly subscriber ratio | 60:40 | Aggressive annual push with 33% discount |

### Month-by-Month Projection (First 12 Months)

| Month | MAU | Pro Subs | Microtx Buyers | Sub Revenue (R$) | Microtx Revenue (R$) | Total Revenue (R$) |
|:-----:|:---:|:--------:|:--------------:|:----------------:|:-------------------:|:------------------:|
| 1 | 5,000 | 150 | 350 | 1,836 | 2,065 | 3,901 |
| 2 | 7,000 | 280 | 500 | 3,430 | 2,950 | 6,380 |
| 3 | 10,000 | 500 | 750 | 6,126 | 4,425 | 10,551 |
| 4 | 15,000 | 900 | 1,200 | 11,027 | 7,080 | 18,107 |
| 5 | 22,000 | 1,500 | 1,800 | 18,378 | 10,620 | 28,998 |
| 6 | 32,000 | 2,400 | 2,700 | 29,405 | 15,930 | 45,335 |
| 7 | 45,000 | 3,600 | 3,900 | 44,107 | 23,010 | 67,117 |
| 8 | 60,000 | 4,800 | 5,400 | 58,810 | 31,860 | 90,670 |
| 9 | 80,000 | 6,400 | 7,200 | 78,413 | 42,480 | 120,893 |
| 10 | 100,000 | 8,000 | 9,000 | 98,016 | 53,100 | 151,116 |
| 11 | 120,000 | 9,600 | 10,800 | 117,619 | 63,720 | 181,339 |
| 12 | 150,000 | 12,000 | 13,500 | 147,024 | 79,650 | 226,674 |

**Year 1 Total Revenue: ~R$ 950,000 (~R$ 1M)**

### Year 2 Projection (Months 13-24)

With artist comparison launching in Year 2 and growing brand awareness:

| Metric | Month 13 | Month 18 | Month 24 |
|--------|:--------:|:--------:|:--------:|
| MAU | 180,000 | 350,000 | 600,000 |
| Pro Subscribers | 14,400 | 31,500 | 54,000 |
| Microtx Revenue/mo | R$ 95,580 | R$ 206,500 | R$ 354,000 |
| Sub Revenue/mo | R$ 176,429 | R$ 385,939 | R$ 661,608 |
| Artist Comparison Rev/mo | R$ 15,000 | R$ 85,000 | R$ 220,000 |
| **Total Revenue/mo** | **R$ 287,009** | **R$ 677,439** | **R$ 1,235,608** |

**Year 2 Total Revenue: ~R$ 8.5M**

**Combined 24-month revenue: ~R$ 9.5M**

---

## 6.3 Cost Structure & Margins

### Variable Costs (Per User Per Month)

| Cost | Free User | Pro Subscriber | Microtx Buyer |
|------|:---------:|:--------------:|:-------------:|
| Infrastructure (servers, DB, CDN) | R$ 0,08 | R$ 0,15 | R$ 0,10 |
| Card generation (compute) | R$ 0,05 | R$ 0,25 | R$ 0,10 |
| Payment processing (3-5%) | R$ 0 | R$ 0,60 | R$ 0,25 |
| Wearable API costs | R$ 0,02 | R$ 0,02 | R$ 0,02 |
| External APIs (Setlist.fm, API-Football) | R$ 0,01 | R$ 0,01 | R$ 0,01 |
| **Total variable cost** | **R$ 0,16** | **R$ 1,03** | **R$ 0,48** |
| **Revenue** | **R$ 0** | **R$ 12,25 avg** | **R$ 5,90 avg** |
| **Gross margin** | **N/A** | **~92%** | **~92%** |

### Fixed Costs (Monthly, at Scale)

| Cost | Month 6 | Month 12 | Month 24 |
|------|:-------:|:--------:|:--------:|
| Engineering team (3-5 devs) | R$ 60,000 | R$ 80,000 | R$ 150,000 |
| Cloud infrastructure | R$ 5,000 | R$ 15,000 | R$ 45,000 |
| Marketing & user acquisition | R$ 10,000 | R$ 30,000 | R$ 80,000 |
| Artist/athlete partnerships (advances) | R$ 0 | R$ 15,000 | R$ 50,000 |
| Artist revenue share payments | R$ 0 | R$ 5,000 | R$ 165,000 |
| Operations & support | R$ 5,000 | R$ 10,000 | R$ 25,000 |
| **Total fixed costs** | **R$ 80,000** | **R$ 155,000** | **R$ 515,000** |

### Path to Profitability

| Milestone | When | MAU | Monthly Revenue | Monthly Costs | Net |
|-----------|:----:|:---:|:--------------:|:-------------:|:---:|
| Break-even (contribution margin) | Month 4-5 | 15-22K | R$ 18-29K | R$ 15-25K | ~R$ 0 |
| Operating break-even | Month 10-12 | 100-150K | R$ 150-227K | R$ 130-155K | R$ 20-72K |
| Strong profitability | Month 18-24 | 350-600K | R$ 677K-1.2M | R$ 350-515K | R$ 327-720K |

---

## 6.4 Revenue Mix Evolution

```
Year 1 Revenue Mix:               Year 2 Revenue Mix:
┌────────────────────────┐        ┌────────────────────────┐
│                        │        │                        │
│   Subscriptions  62%   │        │   Subscriptions  53%   │
│   ████████████████     │        │   █████████████        │
│                        │        │                        │
│   Microtx        38%   │        │   Microtx        29%   │
│   █████████            │        │   ███████              │
│                        │        │                        │
│   Artist Rev      0%   │        │   Artist Rev     18%   │
│                        │        │   ████                 │
│                        │        │                        │
└────────────────────────┘        └────────────────────────┘
```

**The shift toward artist revenue in Year 2 is strategic:** it represents a new revenue stream that doesn't cannibalize existing revenue but adds incremental value. The 18% contribution from artist comparisons in Year 2 validates the feature and sets up Year 3 where it could reach 25-30% of revenue.

---

## 6.5 Key Metrics to Track

### North Star Metrics

| Metric | Target (Month 6) | Target (Month 12) | Target (Month 24) |
|--------|:-----------------:|:------------------:|:------------------:|
| MAU | 32,000 | 150,000 | 600,000 |
| Paid conversion rate (sub + microtx) | 16% | 22% | 25% |
| Pro subscription rate | 6% | 8% | 9% |
| Monthly churn (Pro Monthly) | <15% | <12% | <10% |
| Annual churn (Pro Annual) | <30% | <25% | <20% |
| Blended ARPU (all users) | R$ 1,42 | R$ 1,90 | R$ 2,06 |
| Cards shared per user per event | 1.2 | 1.5 | 1.8 |
| Viral coefficient (new users per share) | 0.15 | 0.20 | 0.25 |

### Pricing-Specific Metrics to Monitor

| Metric | What It Tells You | Action If Off-Target |
|--------|-------------------|---------------------|
| Free-to-Pro conversion rate | Is Pro valuable enough? | If <5% after 6 months: add features or lower price. |
| Microtx purchase frequency | Are one-time purchases attractive? | If <8% of free users buy: test R$ 2,90 price point. |
| Annual vs. Monthly mix | Are users committing long-term? | If <40% annual: increase annual discount or add annual-only features. |
| Time-to-first-purchase | How fast do users see value? | If >30 days: improve onboarding and upgrade prompts. |
| Feature usage by tier | What do Pro users actually use? | Reallocate underused Pro features to free. Invest in popular ones. |
| Artist comparison take rate | Is the killer feature converting? | If <10% of users at artist events buy: lower price or improve UX. |
| Churn reason survey | Why do people cancel? | Adjust pricing, features, or communication accordingly. |

---

## 6.6 Pricing Experiments Roadmap

### Phase 0 — Launch (Months 1-3)
- **Launch with R$ 14,90/month and R$ 119,90/year.**
- **No microtransactions yet.** Keep it simple. Free + Pro only.
- **Track:** conversion rate, feature usage, churn reasons, qualitative feedback.
- **Goal:** Validate that 5-8% of users will pay for Pro.

### Phase 1 — Microtransactions (Months 4-6)
- **Add per-card purchases** (R$ 4,90 premium, R$ 6,90 animated).
- **Test pricing:** A/B test R$ 3,90 vs. R$ 4,90 vs. R$ 5,90 for premium cards.
- **Track:** microtx conversion rate, cannibalization of Pro subs (are people buying cards instead of subscribing?).
- **Goal:** Microtx should ADD revenue without reducing subscription conversion.

### Phase 2 — Artist Comparison (Months 7-12)
- **Launch artist comparison feature** with first partners.
- **Price at R$ 7,90/event** for non-subscribers (included in Pro).
- **Track:** take rate, artist revenue payouts, impact on Pro conversion.
- **Goal:** Artist comparison should increase Pro conversion by 20-30%.

### Phase 3 — Seasonal Pass (Month 9+)
- **Launch Brasileirao Season Pass** (R$ 79,90) at the start of the next season.
- **Test:** Does it cannibalize annual subs or capture a new segment?
- **Track:** Season pass adoption, retention through season, renewal rate.
- **Goal:** Capture soccer-only users who won't commit to annual.

### Phase 4 — Price Optimization (Months 12-18)
- **Run Van Westendorp pricing survey** with 1,000+ users.
- **A/B test Pro pricing:** R$ 12,90 vs. R$ 14,90 vs. R$ 17,90.
- **Test bundle pricing:** Pro + artist comparison at premium tier.
- **Goal:** Find the price that maximizes revenue (not just conversion).

---

## 6.7 Risk Factors & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|:----------:|:------:|-----------|
| Subscription fatigue (users cancel after 2-3 months) | High | Medium | Push annual plans aggressively. Create seasonal value (Brasileirao, festival season). |
| Microtx cannibalizes subscriptions | Medium | Medium | Monitor closely. If happening, limit microtx to 2 per month for free users. |
| Artists demand higher revenue share | Medium | Low | Grandfathered contracts protect margins. New contracts negotiated at market rate. |
| Competitor copies the concept | Low (short-term) | High (long-term) | Speed to market + exclusive artist partnerships = moat. First-mover advantage in Brazil. |
| Low wearable penetration in Brazil | Medium | High | Apple Watch penetration is growing. Focus on Classes A/B initially who have wearables. Phase 1 Tumtum band solves this. |
| Payment processing issues (Pix, boleto) | Low | Medium | Use local processor (Stripe BR, Pagar.me, or Mercado Pago) with full Pix/boleto support. |

---

## 6.8 Summary: The TumTum Pricing Blueprint

```
┌─────────────────────────────────────────────────────────────────┐
│                    TUMTUM PRICING BLUEPRINT                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  MODEL:     Freemium + Hybrid (Subscription + Microtransactions) │
│                                                                   │
│  TIERS:     2 (TumTum Free + TumTum Pro)                        │
│                                                                   │
│  PRICING:   Pro Monthly:  R$ 14,90/month                        │
│             Pro Annual:   R$ 119,90/year (R$ 9,99/month)        │
│             Season Pass:  R$ 79,90/season (soccer)              │
│             Premium Card: R$ 4,90 (microtx)                     │
│             Animated Card: R$ 6,90 (microtx)                    │
│             Artist Comparison: R$ 7,90/event (microtx)          │
│                                                                   │
│  ARTIST     75% to artist / 25% to TumTum                      │
│  SPLIT:     (on comparison-attributable revenue only)            │
│                                                                   │
│  TARGETS:   Month 12: 150K MAU, R$ 227K/month revenue           │
│             Month 24: 600K MAU, R$ 1.2M/month revenue           │
│                                                                   │
│  PAYMENT:   Pix, Credit Card (installments), Boleto             │
│                                                                   │
│  PRINCIPLE: Free tier powers virality. Pro tier captures value.  │
│             Keep it simple. Two choices. No friction.            │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

*End of TumTum Pricing Strategy Document*
*Chapters: 1 (Executive Summary) | 2 (Customer Value) | 3 (Tier Structure) | 4 (Feature Allocation) | 5 (Revenue Share) | 6 (Financial Projections)*
