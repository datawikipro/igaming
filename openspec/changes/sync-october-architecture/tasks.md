## 1. Crawler Engine Synchronization

- [x] 1.1 Verify Redux-store dump extraction in BetBoom browser client (`redux-store-extract.js`)
- [x] 1.2 Verify BetB2B clone configuration parameterization (`linebet`, `betwinner`, `22bet`, `1xbit`)
- [x] 1.3 Verify HikariPoolDefaultConfig and startupProbe implementations across source modules

## 2. Infrastructure & Proxy Pool Synchronization

- [x] 2.1 Verify 1:1 static proxy mappings and PureVPN 8/10 quota rules in `service-proxy-backend`
- [x] 2.2 Verify regional TSPU bypass via Finnish Shadowsocks tunnel and `ru-proxy` routing
- [x] 2.3 Verify autonomous Plane DAG orchestrator (`dag_autonomous_orchestrator.py`)

## 3. Social Media & Mobile Ecosystem Synchronization

- [x] 3.1 Verify mobile device pool isolation (SMM vs Capture RU vs Capture Global)
- [x] 3.2 Verify automated native coupon capture and responsible gambling disclaimers
- [x] 3.3 Verify automated video Shorts/Reels pipeline and Boosty OAuth token listener

## 4. OpenSpec Living Specs Merge

- [x] 4.1 Sync delta requirements into main `openspec/specs/crawler-engine/spec.md`
- [x] 4.2 Sync delta requirements into main `openspec/specs/k8s-infrastructure/spec.md`
- [x] 4.3 Sync delta requirements into main `openspec/specs/social-media-bot/spec.md`
- [x] 4.4 Validate all main specs via `openspec validate --specs`
