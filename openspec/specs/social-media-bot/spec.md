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
