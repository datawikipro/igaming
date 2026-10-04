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

### Requirement: Twitter/X Automation (@smartbetguru)
The `smm-agent` service must automate Twitter/X account `@smartbetguru` using a persistent stealth
Firefox profile, posting arbitrage and freebet signals with affiliate links, and monitoring
@mentions for patron feedback escalation.

#### Scenario: Persistent session authentication (Rule 9 — No Incognito)
- **WHEN** the `twitter_agent` starts
- **THEN** it launches Firefox via `launch_persistent_context()` using the stored profile
  (`smm:profile:twitter:<account_id>` in Redis), performs a mandatory 2–3 min neutral-site
  cache warmup (BBC Sport, ESPN, Flashscore), and authenticates via persistent cookies or the
  login flow — without ever using `new_context()` (incognito is strictly forbidden).

#### Scenario: Dedicated static US-CA proxy 1:1 mapping (Rules 6 & 12)
- **WHEN** any Twitter/X browser session is launched
- **THEN** all outbound traffic is routed exclusively through `purevpn-us-ca.proxy:3128`
  (K8s DNS service name, Los Angeles, CA), using a 1:1 static account-to-node mapping
  — IP rotation is categorically prohibited.

#### Scenario: Surebet signal posting with affiliate links (Rule 10)
- **WHEN** a surebet job of type `"surebet"` arrives on the Redis queue `smm:queue:twitter`
- **THEN** the agent composes a tweet (≤280 chars) containing: bookmaker names, odds, profit %,
  smartbet.guru UTM affiliate links, and the short responsible gambling disclaimer
  "⚠️ Играйте ответственно." — and publishes it with a minimum 10-minute inter-post interval.

#### Scenario: Freebet post with 80% guaranteed cash (Rule 10)
- **WHEN** a freebet job of type `"freebet"` arrives on the Redis queue `smm:queue:twitter`
- **THEN** the agent computes the SNR conversion: `eta = ((K1-1)*(K2-1))/K2 ≈ 0.80`, states the
  guaranteed cash amount, includes the freebet calculator URL, and posts ≤280 chars with
  disclaimer. Max 6 posts per hour across all tweet types.

#### Scenario: @Mention monitoring and patron CRM escalation
- **WHEN** the monitor loop runs every 180 seconds
- **THEN** new @mentions are captured from the Notifications/Mentions tab, deduplicated via
  Redis set `smm:seen:twitter:comments`, and enqueued to `feedback:queue:twitter` for
  downstream Patron CRM triage with 15-minute SLA (P1/P2 for paying patrons).

#### Scenario: Kubernetes health probes
- **WHEN** Kubernetes probes the pod at `/actuator/health/liveness` or `/actuator/health/readiness`
- **THEN** the agent responds HTTP 200 `{"status": "UP"}` on liveness always, and readiness
  only after successful Twitter authentication and warmup completion.
