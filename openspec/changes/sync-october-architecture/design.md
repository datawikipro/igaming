## Context

See `proposal.md` for motivation. Over early October 2026, multiple architectural components were built and deployed in production on the bare-metal Xeon host, including mobile capture pools, PureVPN static node allocation, BetB2B crawler scaling, BetBoom Redux store dumps, and Plane DAG orchestration. This design document establishes the architectural blueprints and contracts governing these components.

## Goals / Non-Goals

**Goals:**
- Provide clear architectural documentation and formal contracts for proxy leasing, mobile phone pools, crawler execution models, and DAG orchestration.
- Maintain full parity between operational rules (`AGENTS.md`) and living OpenSpec specifications.
- Ensure all delta specifications pass `openspec validate --specs` cleanly.

**Non-Goals:**
- Modifying currently stable production services or altering existing database schemas.
- Refactoring public portal client API contracts.

## Decisions

### 1. Direct Redux-Store State Dump vs DOM Traversal
- **Decision**: In SPAs with complex nested layouts and dynamic hydration (e.g. BetBoom), crawlers execute a JavaScript extraction script within the Playwright/Chromium context to dump state directly from `window.__INITIAL_STATE__` / store dispatchers.
- **Rationale**: DOM parsing is CPU-intensive, brittle across UI redesigns, and prone to race conditions during React virtual DOM reconciliation. Memory state dump provides instantaneous, complete JSON snapshots.
- **Alternatives considered**: MutationObserver DOM scrapers (rejected due to high CPU load and missed rapid odds fluctuations).

### 2. 1:1 Static Proxy Mapping with PureVPN 8/10 Quota
- **Decision**: Each social media profile and sensitive bookmaker scraper is assigned a dedicated static proxy via Kubernetes DNS (`http://purevpn-<loc>.proxy:3128`). Each PureVPN account leases at most 8 nodes (80% of the 10-device limit).
- **Rationale**: Anti-fraud systems in Meta and sensitive sportsbooks flag frequent IP hopping. Reserving 2 slots prevents hard account lockouts caused by overlapping reconnects or orphaned sessions.
- **Alternatives considered**: Dynamic rotating residential proxies (rejected due to immediate fraud bans on social platforms).

### 3. Isolated Mobile Device Pools (SMM vs Capture RU vs Capture Global)
- **Decision**: Physically isolate Android virtual phone containers into three distinct pools:
  1. *SMM Pool*: Operates over US/NL/UK static proxies; zero betting apps installed.
  2. *RU Capture Pool*: Operates over direct domestic ISP IP (`188.242.33.93`) for TsUPIS books.
  3. *Global Capture Pool*: Operates over country-specific proxies (NL/UK/BR) for offshore books.
- **Rationale**: Betting apps on US SMM profiles trigger risk scores in Meta/TikTok. Operating RU sportsbooks over foreign proxies triggers regional access blocks.
- **Alternatives considered**: Single multipurpose virtual phone pool (rejected due to severe cross-contamination risks).

### 4. DAG Autonomous Task Orchestrator vs Linear Polling
- **Decision**: Implement `dag_autonomous_orchestrator.py` consuming `dependency_graph.yaml` to orchestrate multi-worker AI pipelines (`plane-ai-worker-0..17`).
- **Rationale**: Complex multi-module features have interdependencies (e.g. core mappers before crawlers, aggregators before UI). The DAG orchestrator unblocks child tasks with sub-millisecond dispatching upon parent completion.
- **Alternatives considered**: Manual operator status triage in Plane UI (rejected due to latency and human dependency).

## Risks / Trade-offs

- **[PureVPN node reconnection delay]** → `service-proxy-backend` maintains a 2-node buffer and performs automated readiness probes before leasing.
- **[Bookmaker Redux store key changes]** → Deserializers implement defensive field lookups with fallback alerts in telemetry.
- **[Mobile APK signature verification issues]** → Aurora Store and direct ADB package installations automate signature acceptance and runtime permission granting (`-g`).
