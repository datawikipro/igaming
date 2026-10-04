# Social Media and Bot Specification

## Purpose
Automates Telegram subscription alerts, scheduled educational/marketing social posts, responsible gambling compliance disclaimers, multi-tier patron feedback aggregation (Patron CRM), and 80% freebet monetization funnels for SmartBet.guru.

---

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

<<<<<<< HEAD
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
=======
### Requirement: Мультирегиональная фабрика аккаунтов (Multi-Regional Account Factory)
The `smm-agent` ecosystem must support automated creation of authentic-looking social media and bookmaker accounts across multiple jurisdictions (RU, US, EU) using catch-all domain emails, virtual SMS numbers, and Google/Gmail profile generation.

#### Scenario: Catch-all domain email registration
- **WHEN** a new persona is created for registration on a social platform or bookmaker
- **THEN** a unique catch-all email address on a registered domain is assigned (e.g., `<alias>@smartbet.guru` or a configured secondary catch-all domain), and incoming OTP verification emails are polled via `auth-inbox-gateway` within 60 seconds.

#### Scenario: Virtual SMS number acquisition
- **WHEN** a registration flow requires SMS verification
- **THEN** `auth-inbox-gateway` rents a virtual number from the available provider pool (OnlineSim → SmsActivate → GrizzlySMS fallback chain), waits up to 120 seconds for the OTP code, then releases the number.

#### Scenario: Multi-provider SMS fallback
- **WHEN** the primary SMS provider has no balance or is unavailable
- **THEN** the system automatically falls back to the next configured provider in the priority chain without manual intervention.

#### Scenario: Google/Gmail profile creation
- **WHEN** a `SocialPersona` or `BettorPersona` requires a Gmail address
- **THEN** a dedicated warm browser profile (Firefox/Camoufox persistent context) with the assigned static proxy is used to create and verify the Gmail account, with all session cookies and localStorage persisted to Redis under key `smm:profile:<persona_id>`.

#### Scenario: Auth-inbox-gateway health check
- **WHEN** `smm-agent` starts up
- **THEN** `AuthInboxGatewayClient.is_gateway_healthy()` returns `True` within 5 seconds, or the agent logs a warning and continues with mock fallback mode.

---

### Requirement: Stealth Browser Profiles & Static Proxy Binding
Each social media account persona must be bound 1:1 to a static dedicated proxy node to prevent IP reputation flags from Meta, Reddit, and bookmaker anti-fraud systems.

#### Scenario: Persistent profile static proxy binding
- **WHEN** a persona's browser session is initialized
- **THEN** the session uses the persona's assigned proxy (e.g., `http://purevpn-nl.proxy:3128` for EU/Meta accounts) and the warm Firefox/Camoufox persistent context is restored from Redis.

#### Scenario: Browser cache warmup before target actions
- **WHEN** a browser session is restored for an account action (publish, comment, register)
- **THEN** a 2–3 minute warmup phase with organic browsing (news sites, sports portals, social feed) is executed using Bezier curve mouse trajectories before any target action is performed.
>>>>>>> b6ce4ce (wip(ai): [plane-4a454c05] update OpenSpec social-media-bot with multi-regional account factory requirements)
