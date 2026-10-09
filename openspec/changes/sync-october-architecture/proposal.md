## Why

Over the first week of October 2026, substantial architectural implementations, bug fixes, and operational standards were deployed across the SmartBet.guru ecosystem without corresponding updates to the OpenSpec specifications. These implementations encompass dedicated 1:1 static proxy routing (Rule 12), the mobile virtual phone ecosystem and native coupon capture (Rule 13), BetB2B bookmaker clone network orchestration, SPA Redux state extraction for BetBoom, autonomous DAG task dependency dispatching in Plane, and an automated video Shorts/Reels generator. This change reconciles the OpenSpec specifications with the production codebase to prevent architecture drift.

## What Changes

- **Crawler Engine**:
  - Formalize SPA Redux-store state extraction evaluating browser memory directly (`window.__INITIAL_STATE__`) for dynamic web clients (such as BetBoom).
  - Formalize BetB2B / 1x clone network scaling and standardized multi-clone parameterization (Linebet, Betwinner, 22bet, 1xbit).
  - Enforce non-blocking database initialization (`HikariPoolDefaultConfig`, `DatabaseMigrationRunner`) with Kubernetes `startupProbe`.
- **Kubernetes & Proxy Infrastructure**:
  - Codify Rule 12: Dedicated 1:1 static proxy assignment for social media and anti-bot bookmakers via K8s Service DNS.
  - Codify PureVPN multi-account quota constraints (8 nodes leased per account, reserving 2 buffer slots).
  - Formalize regional TSPU/RKN censorship bypass routing (Shadowsocks tunnel in Finland for OpenVPN endpoints; Kambi CDN routing).
  - Codify the autonomous DAG dependency orchestrator (`dag_autonomous_orchestrator.py`) managing task execution across AI developer workers (`plane-ai-worker-0..17`).
- **Social Media, Mobile & Content Ecosystem**:
  - Codify Rule 13: Strict isolation between SMM virtual phones (US/NL/UK static IP) and Capture pools (RU direct home IP vs Global proxies).
  - Formalize automated native mobile coupon card capture with high CTR and mandatory responsible gaming disclaimers.
  - Formalize automated video Shorts/Reels multi-platform rendering pipeline (`video_shorts_pipeline.py`).
  - Formalize persistent Boosty OAuth authentication and token rotation.

## Capabilities

### New Capabilities
*(None - existing capability specifications are expanded to cover new components).*

### Modified Capabilities
- `crawler-engine`: Added BetB2B clone network scaling, SPA Redux-store state extraction, and non-blocking database initialization.
- `k8s-infrastructure`: Added dedicated 1:1 static proxies, PureVPN quota leasing, TSPU bypass routing, startup probes, and autonomous DAG dependency orchestration.
- `social-media-bot`: Added mobile virtual phone ecosystem, native coupon capture, automated video Shorts pipeline, and Boosty OAuth integration.

## Impact

- Specifications updated in `openspec/specs/crawler-engine/`, `openspec/specs/k8s-infrastructure/`, and `openspec/specs/social-media-bot/`.
- Codebase consistency guaranteed across `igaming-source-*`, `service-proxy-backend`, `smm-agent`, `scripts/`, and `.agents/AGENTS.md`.
