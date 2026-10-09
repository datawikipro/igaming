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

### Requirement: Isolated Mobile Virtual Phone Ecosystem and Native Coupon Capture
The automation framework MUST maintain strict isolation between the Social Media phone pool (operating solely through US/NL/UK static proxies) and the Capture pools. The Capture pool MUST be separated into RU Capture (running on direct home ISP IP for TsUPIS bookmakers) and Global Capture (running via regional country proxies). Applications MUST be provisioned directly via ADB (`adb install -r -g`) or Aurora Store.

#### Scenario: Mobile coupon capture for marketing publication
- **WHEN** an arbitrage or matched betting opportunity is scheduled for publication
- **THEN** the capture worker automatedly launches the bookmaker mobile app, selects the betting market, takes a native coupon screenshot, and embeds it into the post payload alongside the 80% freebet cash calculation and responsible gambling disclaimer.

#### Scenario: Device pool isolation enforcement
- **WHEN** a virtual phone device is assigned to the Social Media pool
- **THEN** bookmaker betting applications are strictly excluded from installation on that device to prevent platform anti-fraud flags.

---

### Requirement: Automated Multi-Platform Video Shorts and Reels Generation
The social media system MUST support automated rendering and publishing of vertical short-form videos (9:16 aspect ratio) for YouTube Shorts, Instagram Reels, and TikTok featuring live surebets, line anomalies, and freebet conversions.

#### Scenario: Video shorts automated compilation
- **WHEN** the video compilation pipeline triggers with an arbitrage alert
- **THEN** it generates synchronized audio commentary, renders dynamic motion graphics with match odds and coupon cards, outputs an MP4 vertical video, and schedules it across connected social channels.

---

### Requirement: Persistent Boosty OAuth Authentication and Community Broadcasting
The publishing system MUST manage persistent authentication state and access tokens for Boosty, allowing automated broadcast of premium predictions, arbitrage digests, and development devlog updates to paid supporters.

#### Scenario: Boosty token renewal and posting
- **WHEN** a scheduled post for Boosty subscribers is prepared
- **THEN** the publisher verifies active session tokens, refreshes them via the background auth listener if required, and dispatches the formatted publication via the Boosty API.

