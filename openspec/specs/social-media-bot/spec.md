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

---

### Requirement: Isolated Per-Container Dedicated Proxy Routing (Multi-Agent Independent Egress)
Each social media bot container MUST explicitly declare its assigned proxy service endpoint via environment variables (`HTTP_PROXY`, `HTTPS_PROXY`, `US_PROXY`) resolving to Kubernetes ClusterIP DNS (`*.proxy.svc.cluster.local:3128`), without host-level centralized routing intercepts. Multiple agents operating on the same social network MUST be deployable as independent instances, each bound to an isolated exit network (e.g. DE, US, FI, or Direct domestic).

#### Scenario: Running multiple independent agents for the same platform
- **WHEN** deploying multiple agents targeting the same social platform (e.g. Reddit or Threads)
- **THEN** each agent pod is configured with a distinct dedicated proxy endpoint (`http://us-proxy.proxy.svc.cluster.local:3128`, `http://de-proxy.proxy.svc.cluster.local:3128`, or `http://fi-proxy.proxy.svc.cluster.local:3128`), ensuring distinct external IP addresses and complete network isolation.

#### Scenario: Domestic bot direct egress
- **WHEN** deploying Russian domestic social bots (e.g. VK)
- **THEN** proxies are disabled (`NO_PROXY=*`), routing outgoing traffic directly through the domestic residential IP.

---

### Requirement: Organic Social Graph Building and Strict Persona Locale Enforcement
Social media agents operating across Threads, Instagram, and Twitter (X) MUST actively construct a validated organic social graph based on a 3-tier hierarchy (Tier 1: Anchor authorities, Tier 2: Niche creators, Tier 3: Engaged sports commenters) to prevent the "lonely poplar" anti-fraud footprint. All peer discovery, browsing, and following operations MUST enforce strict language and regional locale isolation (`locale`: `fr`, `en`, `es`, `ru`).

#### Scenario: Tier 1 Anchor following with human kinematics
- **WHEN** an agent initiates a social browsing cycle on Threads, Instagram, or Twitter
- **THEN** it checks Redis state for unfollowed Tier 1 anchor handles matching its configured locale, navigates to their profiles, performs natural reading pauses and feed scrolling, and follows them via cubic Bezier mouse curves while respecting daily safety caps (5-15 actions/day).

#### Scenario: Non-RU persona anti-cyrillic enforcement
- **WHEN** an agent with a non-Russian locale (`fr`, `en`, `es`) evaluates a potential peer candidate from search results or thread comments
- **THEN** any profile containing Cyrillic characters (`[\u0400-\u04FF]`) in its username, display name, bio, or recent posts MUST be immediately rejected prior to page visit or follow action.

#### Scenario: Redis state persistence and daily rate limits
- **WHEN** an agent executes a follow action
- **THEN** it records the username in `smm:graph:<platform>:<account_id>:<locale>:followed` and increments the daily counter in `smm:graph:<platform>:<account_id>:<locale>:daily:<YYYY-MM-DD>`, halting further follow operations once the daily quota is reached.

---

### Requirement: Physical SIM Gateway and Automated 2FA OTP Interception (SmsForwarder & Mobile Gateway)
The social media bot ecosystem MUST support automated interception of 2FA and OTP verification codes from Meta (Instagram, Threads) and messaging channels directed to physical phone numbers. Since Meta prioritizes WhatsApp delivery for Instagram security codes and restricts OTPs exclusively to the primary device hosting the physical SIM (`+79137671550`), an automated gateway daemon (`pppscn/SmsForwarder` on Xiaomi 12T Pro and `tg-inbox.service` on `xeon-srv:8088`) forwards incoming SMS and WhatsApp push notifications to the server backend.

#### Scenario: Intercepting Meta 2FA security code via Webhook and WhatsApp
- **WHEN** an AI agent triggers a login or verification challenge on Instagram or Threads
- **THEN** the agent requests verification via WhatsApp or SMS, the physical device automatically forwards incoming WhatsApp notification or SMS to `POST http://188.242.33.93:8088/api/forwarder/webhook` (proxied by `ru-proxy` to `xeon-srv:8088`), and the agent retrieves the parsed verification code from `GET /api/inbox/latest-code`.

#### Scenario: Direct ADB fallback on USB connection
- **WHEN** the primary device is connected via USB (`516bbeca`)
- **THEN** OTP extraction can execute directly via ADB without network dependencies using SMS content query or notification dumpsys.

---

### Requirement: SMM Persona Lifecycle, Virtual Android Phone Ecosystem (Redroid), and Session Keeper
The social media automation framework MUST manage autonomous personas (`en.smartbet.guru`, `fr.smartbet.guru`, `es.smartbet.guru`, `ru.smartbet.guru`) executing inside dedicated virtual Android phone containers (Redroid) with persistent credentials, humanized circadian activity cycles, and automated session renewal to prevent token and cookie expiration.

#### Scenario: Virtual Android phone execution and native mobile apps
- **WHEN** an SMM persona initiates platform interactions
- **THEN** it executes actions within a dedicated Redroid virtual phone environment using official native mobile applications (`com.instagram.android`, `com.instagram.barcelona`, `com.twitter.android`, `org.mozilla.firefox`) controlled via ADB input events and accessibility/CDP protocols.

#### Scenario: Immutable persona credentials and vault storage
- **WHEN** managing authentication for social media personas
- **THEN** credentials (username, email, password, phone `+79137671550`, 2FA method, backup codes) are securely stored and versioned in the persona vault (`smm:persona:<persona_id>:vault`), strictly prohibiting transient or undocumented placeholder credentials.

#### Scenario: Circadian rhythm and persona daily lifecycle
- **WHEN** operating throughout the day
- **THEN** the persona adheres to realistic local time schedules: morning feed warmup and story viewing (08:00-10:00), daytime analytical posting with 80% freebet cash math and disclaimer (11:00-16:00), evening social graph networking and Bezier following (17:00-21:00), and complete inactivity during night hours (22:00-07:00).

#### Scenario: Automated Session Keeper and anti-expiration renewal
- **WHEN** maintaining long-running social accounts
- **THEN** a background Session Keeper job runs every 12-24 hours, performing lightweight auth validation, saving fresh session cookies/tokens to Redis (`smm:profile:<persona_id>`), and triggering automated WhatsApp/SMS 2FA re-authentication upon encountering security checkpoints.

---

### Requirement: Enterprise Continuous Documentation and Spec-First Synchronization (DocOps)
All AI agents (interactive assistants in Antigravity, subagents, and autonomous Plane AI workers on Xeon) MUST synchronously update technical documentation, operational rules (`AGENTS.md`), and OpenSpec definitions alongside any code or infrastructure modifications, enforcing zero documentation drift.

#### Scenario: Synchronous documentation update on infrastructure change
- **WHEN** an agent modifies infrastructure ports, proxy configurations, SMM gateways, bookmaker services, or database schemas
- **THEN** the agent updates corresponding documentation in `AGENTS.md` (replicated across repositories) and feature delta specifications before marking the task complete.

#### Scenario: Definition of Done documentation validation gate
- **WHEN** an agent prepares to complete a task or merge a pull request
- **THEN** the task MUST NOT be closed in Plane unless all modifications are documented and `openspec validate --specs` executes with zero validation failures.

---

### Requirement: Transparency Narrative and Market Limit Objection Handling (Anti-Ban Meta SMM)
The SMM framework MUST proactively address reader skepticism regarding why an arbitrage or quantitative system shares market edges publicly instead of trading them privately, framing the explanation around platform account limits and anti-fraud restrictions while maintaining zero gambling/betting trigger words on Meta platforms (Threads, Instagram).

#### Scenario: Public objection handling and transparency narrative
- **WHEN** publishing educational or positioning content in public channels (Threads, Instagram, X)
- **THEN** the post articulates the market liquidity reality: institutional scaling is capped by bookmaker stake limits and account restrictions, so the platform uses only a tiny fraction of detected discrepancies, while regular users benefit from extracting risk-free bonuses and value from pricing divergences.

#### Scenario: Meta Anti-Ban trigger-word compliance
- **WHEN** generating posts for Threads and Instagram
- **THEN** the content validator enforces a strict ban on trigger terms ("ставки", "букмекер", "вилка", "коэффициент", "пассивный доход", "odds", "betting", "surebet") and routes users to Telegram/Discord via bio link for full data.




