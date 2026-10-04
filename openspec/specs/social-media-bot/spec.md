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

### Requirement: Multilingual Telegram Regional Network Management
The SMM automation suite must manage regional Telegram broadcast channels for Russian (RU), English (EN), French (FR), and Spanish (ES) audiences, configure public usernames, grant administrator rights to `@smartbet_guru_bot`, and link discussion groups for audience feedback.

#### Scenario: Channel administration and public username assignment
- **WHEN** configuring regional Telegram channels via `telegram_web_manager.py`
- **THEN** English (`-3960368887`), France (`-4371643544`), and España (`-4346736376`) channels have `@smartbet_guru_bot` promoted as administrator with post and management privileges, and public usernames (`@SmartBetGuruEN`, `@SmartBetGuruFR`, `@SmartBetGuruES`) are verified.

#### Scenario: Discussion group creation and comment stream monitoring
- **WHEN** channel publications receive comments in linked discussion groups
- **THEN** the bot captures incoming comments, parses context from the parent post, routes inquiries to Patron CRM queue, and provides AI-prompter assistance.

---

### Requirement: Multilingual Native Card Publishing and Site Action Buttons
The `ChannelPosterScheduler` must format arbitrage and promo signals in the regional channel's native language, including 80% freebet guaranteed cash math and interactive inline buttons with affiliate tracking.

#### Scenario: Localized signal broadcasting with 80% freebet calculation
- **WHEN** an arbitrage or freebet opportunity is published to a regional channel
- **THEN** the message includes localized team and market details, calculated 80% guaranteed cash conversion, mandatory responsible gambling disclaimer in the target language, and inline buttons linking to the SmartBet calculator and bookmakers.
