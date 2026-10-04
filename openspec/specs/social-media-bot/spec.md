# Social Media and Bot Specification

## Purpose
Automates Telegram subscription alerts, scheduled educational/marketing social posts, responsible gambling compliance disclaimers, multi-tier patron feedback aggregation (Patron CRM), and 80% freebet monetization funnels for SmartBet.guru.

## Requirements

### Requirement: Telegram Alert Distribution
The `igaming-bot` service must dispatch real-time arbitrage notifications to Telegram subscribers filtered according to their configured sport, bookmaker, and profit margin preferences.

#### Scenario: Real-time surebet notification
- **WHEN** a new arbitrage opportunity matching the subscriber's filter criteria is identified
- **THEN** a formatted Telegram alert containing bookmakers, direct event links, odds, and stake calculator is sent.

---

### Requirement: Content Publishing and Disclaimers
Social media automation schedulers must follow the established daily publication timetable and include mandatory responsible gambling disclaimers on every marketing post.

#### Scenario: Marketing and blog post publication
- **WHEN** the scheduled content job fires
- **THEN** content is posted according to schedule with the mandatory disclaimer: "Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно."

#### Scenario: Stated yield limits in promotional materials
- **WHEN** describing Free vs Premium tiers in marketing copy
- **THEN** Free is accurately described as up to 5.0% yield, and Premium is described as unlimited (20%+).

---

### Requirement: Multi-Tier Patron CRM and Feedback Aggregation
The social media ecosystem must capture incoming user comments, suggestions, and support requests across all channels, clearly differentiating between paying patrons (Boosty, Patreon, VK Donut, Telegram VIP) and free public commentators.

#### Scenario: Paid patron prioritization and SLA tracking
- **WHEN** an inquiry is received from a paying patron (Boosty PRO, Patreon VIP, VK Donut, TG VIP)
- **THEN** the ticket is assigned P1/P2 priority with an enforced 15-minute SLA countdown and highlighted in the operator dashboard.

#### Scenario: AI-assisted triage and Plane issue escalation
- **WHEN** an operator reviews incoming community feedback
- **THEN** an AI-generated concise summary and personalized response draft are provided, with one-click escalation to Plane issues (`[feature/feedback]`).

---

### Requirement: Matched Betting Freebet 80% Guaranteed Cash Positioning
All promotional and marketing publications featuring bookmaker welcome bonuses, registration freebets, and promotions must position freebets as 80% guaranteed cash via matched betting on high odds.

#### Scenario: Freebet promotion announcement
- **WHEN** broadcasting promo offers or freebet alerts in Telegram, Threads, Instagram, or Reddit
- **THEN** the publication states the guaranteed cash conversion (e.g., "Фрибет 3 000 ₽ → 2 400 ₽ гарантированного кэша при любом исходе через вилку") with links to the SmartBet freebet calculator and affiliate tracking URLs.

---

### Requirement: Boosty Public Page & Subscription Tiers
The SmartBet.guru Boosty page (`boosty.to/smartbetguru`) must be configured with a clear three-tier subscription structure, a content publication schedule, and freebet promotion posts with 80% cash conversion calculations.

#### Scenario: Subscription tier structure
- **WHEN** a visitor views the Boosty page
- **THEN** three clearly defined subscription tiers are presented:
  - **Free** (0 ₽) — public posts, surebets up to 5.0% yield, educational content.
  - **Базовый** (299 ₽/month) — daily signal digest, surebets up to 10% yield, priority Telegram alerts.
  - **Premium** (2 500 ₽/month) — unlimited surebets (20%+), corridors, +EV signals, personal freebet calculator, direct operator chat, Boosty-exclusive posts.

#### Scenario: Content publication schedule
- **WHEN** the scheduled content job fires
- **THEN** content is published according to the following timetable (MSK):
  - Monday 10:00 — FREE weekly arbitrage digest (public, ≤5% yield, CTA to Premium).
  - Daily 09:00 — BASIC daily signal digest (subscribers ≥299 ₽, ≤10% yield).
  - Sunday 20:00 — PREMIUM weekly performance report (subscribers ≥2 500 ₽, unlimited yield).
  - On-demand — PREMIUM surebet/corridor/+EV signals posted in real-time when high-value opportunities are detected.
- **AND** every post includes the mandatory disclaimer: "Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно."

#### Scenario: Freebet promo post on Boosty
- **WHEN** a bookmaker freebet promotion is published on Boosty
- **THEN** the post includes:
  - Freebet nominal amount and bookmaker name.
  - Guaranteed cash calculation: η = (K₁-1)(K₂-1)/K₂ ≈ 0.80 (e.g., "Фрибет 3 000 ₽ → 2 400 ₽ гарантированного кэша").
  - Link to `https://smartbet.guru/tools/freebet-calculator`.
  - Affiliate tracking URL with UTM parameters.

#### Scenario: Premium exclusive post access control
- **WHEN** a Premium post is published
- **THEN** the Boosty `price` field is set to 2 500 (RUB) so only Premium subscribers can read the full content, with a public teaser visible to free users.

#### Scenario: Admin REST API for tier info and publishing
- **WHEN** the operator calls `GET /admin/boosty/tiers`
- **THEN** the response lists all three tiers with `priceRub`, `maxYield`, `benefits`, and `boostyUrl`.
- **WHEN** the operator calls `POST /admin/boosty/publish/freebet` with `{ bookmaker, freebet_amount_rub, affiliate_url }`
- **THEN** the freebet promo post is published to Boosty and the response includes `guaranteed_cash_rub` (= freebet × 0.80).

