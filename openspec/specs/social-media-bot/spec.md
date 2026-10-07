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

### Requirement: Instagram Swarm — Ephemeral Phone Lifecycle (Multi-Region)
The `smm-instagram-swarm` service must operate a fleet of regional Instagram agents (fr, es, de, br, uk), each using an ephemeral Android-emulated phone profile that is created fresh for every session and destroyed upon completion.

#### Scenario: Ephemeral phone profile creation and destruction
- **WHEN** an Instagram Swarm cycle starts for any region
- **THEN** a temporary Firefox browser profile is created in a system temp directory emulating an Android phone (random UA, viewport, fingerprint), used for the session, and permanently deleted (shutil.rmtree) after the session ends — profile data is never persisted to Redis.

#### Scenario: Regional 1:1 proxy mapping
- **WHEN** launching an Instagram Swarm agent for a specific region
- **THEN** the agent uses a static dedicated PureVPN proxy (Rule 12): fr→purevpn-nl, es/de→purevpn-de, br→purevpn-br, uk→purevpn-uk, accessed via K8s DNS service names only.

#### Scenario: Cache Warmup before Instagram interaction
- **WHEN** any Instagram Swarm cycle begins
- **THEN** the ephemeral browser performs 150 seconds of neutral sports/news warmup (Rule 9) before navigating to instagram.com.

#### Scenario: Localized freebet post publication
- **WHEN** the post interval timer fires for a region
- **THEN** a localized post in the region's language (fr/es/de/pt/en) is published containing freebet math (80% guaranteed cash), UTM-tagged affiliate link to the regional subdomain (fr.smartbet.guru, etc.), and the mandatory responsible gambling disclaimer.

---

### Requirement: Instagram Comment 15-min SLA Monitor
The `smm-instagram-sla-monitor` service must poll Instagram comments across all regional swarm posts every 90 seconds, enforce 15-minute SLA for patron responses, and escalate breaches.

#### Scenario: Comment SLA ticket registration
- **WHEN** a new comment is detected under any Instagram swarm post
- **THEN** a SLA ticket is registered with a 15-minute countdown in Redis (key: `smm:sla:comment:<id>`), priority P1 (patron with question), P2 (patron), or P3 (general public).

#### Scenario: Patron SLA breach alert
- **WHEN** a P1/P2 patron comment exceeds 15 minutes without a reply
- **THEN** a Telegram alert is sent via igaming-bot API (`/api/v1/alert/sla`) and the ticket is auto-escalated to Plane as `[feature/feedback]` issue with `urgent` priority.

#### Scenario: Auto-reply generation
- **WHEN** a comment is processed by the SLA monitor
- **THEN** a localized auto-reply in the commenter's region language is generated with UTM link to freebet calculator and mandatory responsible gambling disclaimer, and POSTed back via igaming-portal PatronCRM API.

#### Scenario: Manual reply via admin dashboard
- **WHEN** an operator sends `POST /api/v1/sla/tickets/reply` to the SLA monitor
- **THEN** the corresponding ticket is marked as replied and removed from the open SLA watchlist.

---

### Requirement: Patreon International Page — Tiers, Content Templates & Posting Schedule

The `smm-bot-patreon` service must maintain an English-language Patreon presence for international (USD) subscribers, with two defined membership tiers, five standardized post templates, a fixed weekly content schedule, and mandatory responsible gambling disclaimers on every publication.

#### Tier Definitions

| Tier | Name | Price | `min_tier_cents` | Target Audience |
|------|------|-------|-----------------|-----------------|
| T1 | **Pro Arbitrageur** | $25/mo | `2500` | Value bettors with $500–$2,000 bankroll |
| T2 | **VIP Syndicate** | $100/mo | `10000` | Professional arb players with $5,000+ bankroll |

**Pro Arbitrageur ($25/mo) benefits:**
- Surebet signals ≥ 2% margin (daily digest, 20–40 opportunities/day)
- English-language corridor alerts and freebet hedge radar
- Freebet Calculator access + monthly bookmaker account strategy guide
- 24-hour Patreon support SLA

**VIP Syndicate ($100/mo) benefits** — everything in Pro, plus:
- Unlimited yield surebets (10–20%+ ROI, no 5% cap)
- Real-time Telegram VIP channel (seconds latency, not hourly batches)
- Pinnacle + Betfair Exchange exclusive signals
- Soft bookmaker rotation & account longevity guide
- Monthly 30-min async Q&A with quant team
- Private VIP Discord server
- 15-minute SLA during market hours (Mon–Sun, 09:00–23:00 UTC+3)

#### Scenario: Publishing a surebet signal post to Patreon
- **WHEN** a new arbitrage opportunity is detected with margin ≥ 2% on international bookmakers
- **THEN** `PatreonTemplateEngine.render_surebet_signal()` generates an English post with full stake calculator table, bookmaker deep-links via `smartbet.guru/go/<bookmaker>`, guaranteed profit range, and the standard EN disclaimer, posted at `min_tier_cents=2500`.

#### Scenario: Publishing a freebet 80% cash post to Patreon
- **WHEN** a new SNR freebet bonus is detected on international bookmakers
- **THEN** `PatreonTemplateEngine.render_freebet_80_cash()` generates an English post stating the guaranteed cash amount using formula `η = (K₁ - 1)(K₂ - 1) / K₂ ≈ 0.80` with full calculation table, a link to `smartbet.guru/tools/freebet-calculator`, and affiliate tracking URL.
- **The post MUST include:** freebet nominal, K₁ odds (4.5–6.0), K₂ odds (1.20–1.28), guaranteed cash in USD, and the standard EN disclaimer.

#### Scenario: Publishing a corridor alert to Patreon
- **WHEN** a risk-free bracket (corridor) is detected with meaningful corridor width ≥ 0.5 on international bookmakers
- **THEN** `PatreonTemplateEngine.render_corridor_alert()` generates an English post with Over/Under lines, optimal stakes, corridor win profit, and max-loss scenario.

#### Scenario: Publishing weekly performance digest
- **WHEN** the weekly cron job fires every Friday
- **THEN** `PatreonTemplateEngine.render_weekly_digest()` generates a summary post with signal volume (surebets, corridors, freebets), average margin, top 5 opportunities table, and estimated community ROI.

#### Scenario: New patron onboarding guide
- **WHEN** a new Pro Arbitrageur or VIP Syndicate member joins
- **THEN** `PatreonTemplateEngine.render_onboarding_guide(tier)` provides a 5-step setup guide: account opening priority list (Pinnacle P1, Betfair P1, 1xBet P2, Bet365 P2), bankroll allocation table, first arb bet walkthrough, and freebet extraction steps.

#### Content Publishing Schedule

| Day | Template | Min Tier |
|-----|----------|----------|
| Mon–Fri | `surebet_signal` (top daily opportunity) | Pro ($25) |
| Mon–Fri | `corridor_alert` (top daily bracket) | Pro ($25) |
| Tuesday | `freebet_80_cash` (weekly promo radar) | Pro ($25) |
| Wednesday | `account_strategy` / `onboarding_guide` | Pro ($25) |
| Friday | `weekly_digest` | Pro ($25) |
| Daily (real-time) | `surebet_signal` stream (all opportunities) | VIP ($100) |

#### Scenario: Mandatory English disclaimer on Patreon posts
- **WHEN** publishing any post to Patreon (any template, any tier)
- **THEN** every post MUST append the standard EN disclaimer:
  > "⚠️ Sports betting carries financial risk. SmartBet.guru provides algorithmic arbitrage signals and mathematical analysis tools — not gambling advice. Never bet money you cannot afford to lose. Play responsibly."

#### Implementation Artifacts
- Page content & tier descriptions: `smm-agent/patreon_page_content.md`
- Python template engine: `smm-agent/patreon_content_templates.py`
  - `PatreonTemplateEngine.render_surebet_signal(signal)` → dict with title/content/teaser/min_tier_cents
  - `PatreonTemplateEngine.render_freebet_80_cash(signal)` → dict with guaranteed_cash, conversion_rate
  - `PatreonTemplateEngine.render_corridor_alert(signal)` → dict with corridor_win, max_loss
  - `PatreonTemplateEngine.render_weekly_digest(...)` → weekly summary dict
  - `PatreonTemplateEngine.render_onboarding_guide(tier)` → onboarding post dict
- Publishing integration: `smm-agent/patreon_agent.py` → `PatreonMemberDesk.publish_premium_post(title, content, min_tier_cents)`
- K8s deployment: `igaming-k8s/smm-bot-patreon.yaml` (namespace `igaming-dev`, port 8080)

