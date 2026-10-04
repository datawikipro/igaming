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

### Requirement: Automated 9:16 Video Shorts Generation Pipeline (YouTube Shorts, Instagram Reels, TikTok)
The `smm-video-shorts` service (module: `smm-agent/video_shorts_pipeline.py`) must autonomously generate, render, and prepare multi-platform vertical video content (9:16 aspect ratio) based on real-time arbitrage opportunities from `igaming-portal`.

#### Constraint: No Local GPU / CPU-Intensive Rendering
The Xeon node (`xeon-srv`) has no GPU. All video synthesis is delegated to external cloud AI APIs (HeyGen, D-ID, InVideo, RunwayML). The service triggers generation via API and receives finished MP4 via webhook — zero CPU rendering overhead on the host.

#### Scenario: Viral Shorts script generation from surebet
- **WHEN** a high-yield arbitrage opportunity (profit ≥ 8%) is detected from `igaming-portal`
- **THEN** `ShortsScriptGenerator` produces a complete 9:16 video script with:
  - Viral hook text from template library (e.g., "Букмекеры в бешенстве: математическая ошибка!")
  - Exact stake math (`calculate_stakes()` with guaranteed profit in RUB)
  - CTA directing to `smartbet.guru` with affiliate UTM tracking links for both bookmakers
  - Mandatory disclaimer: "Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно."
  - Estimated duration: 25–59 seconds (YouTube Shorts / TikTok limit)

#### Scenario: Cloud AI video synthesis via external provider
- **WHEN** a script is ready for rendering
- **THEN** the pipeline submits to a `BaseCloudVideoProvider` (HeyGen, D-ID, or Mock) and:
  - Sets `dimension: {width: 1080, height: 1920}` and `aspect_ratio: 9:16`
  - Uses Russian TTS voice (`ru-RU-DmitryNeural` or equivalent)
  - Receives completed MP4 URL via webhook callback at `/api/v1/shorts/webhook`
  - Falls back to `MockCloudVideoProvider` if API key is absent

#### Scenario: Multi-platform publication bundle preparation
- **WHEN** video render is complete
- **THEN** `ShortsAutoposter.prepare_publication_bundle()` creates per-platform packages:
  - **YouTube Shorts**: title ≤ 100 chars, tags, `category_id: "17"` (Sports), `privacy_status: public`
  - **Instagram Reels**: caption with hook, profit info, bookmaker names, top 8 hashtags, mandatory disclaimer, link-in-bio pointer
  - **TikTok**: text ≤ 100 chars with 5 trending hashtags, public visibility
  - Bundle JSON persisted to Redis (`smm:shorts:bundle:<job_id>`, TTL 7 days) and queued in `smm:shorts:queue`

#### Scenario: K8s health and API server
- **WHEN** `smm-video-shorts` pod starts
- **THEN** HTTP server on port 8080 exposes:
  - `GET /healthz` → `{"status": "UP"}` (liveness/readiness probes)
  - `POST /api/v1/shorts/generate` → triggers full generation cycle
  - `GET /api/v1/shorts/latest` → returns last generated video bundle
  - `POST /api/v1/shorts/webhook` → accepts cloud render callbacks
