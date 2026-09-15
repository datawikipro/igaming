## Why

The SmartBet.guru ecosystem aggregates sports betting events across 52 bookmakers to detect real-time arbitrage (+EV surebets and middles). While top bookmakers (Leon, Betcity, Betm, Bettery, Fonbet RU/KZ/BY, Pari, Winline) aggregate over 460,000 odds across thousands of matches, smaller or offshore platforms frequently stagnate at 0 to dozens of matches due to:
1. Low default PostgreSQL memory limits (`limits.memory: 96Mi`), causing OOM kills when factor volumes grow past 4,000 matches.
2. Uninitialized database schemas (`match_cache`, `match_factor`, `league_cache`, `sport_cache`) after pod restarts.
3. Geo-blocking / anti-bot challenges (Qrator, Cloudflare, Akamai) requiring explicit routing through cluster proxies (`ru-proxy` for Russian TSUPIS vs EU Netherlands Shadowsocks proxy for offshore books).
4. etcd latency saturation during mass un-paced Kubernetes rollouts.

This change establishes the comprehensive audit, remediation, and operational baseline required to guarantee that all 52 bookmakers maintain at least 500 active matches in their respective databases (`SELECT count(*) FROM match_cache >= 500`).

## What Changes

- **Database Memory & Stability Scale-up**: Upgrade all 52 PostgreSQL StatefulSets from 96Mi to 512Mi RAM (`limits: {cpu: 500m, memory: 512Mi}`, `requests: {cpu: 50m, memory: 128Mi}`) with non-blocking HikariCP parameters and `synchronous_commit = off`.
- **Automated Schema Provisioning & Healing**: Deploy standardized DDL across all 52 database instances covering `match_cache`, `league_cache`, `sport_cache`, `match_factor`, `unmapped_bet`, `unmapped_sport`, and `mapping_conflict`.
- **Crawler Anti-Bot & Network Routing Alignment**: 
  - РФ-букмекеры (Winline, Fonbet, Pari, Betcity, Baltbet, Olimpbet, Tennisi, Leon) route via direct home ISP IP СПб (`188.242.33.93`) without VPN flagging.
  - Европейские и оффшорные букмекеры (Pinnacle, Sbobet, Bet365, Bwin, Betsson, Betsafe, Nordicbet, MrGreen, 888starz, 1x-clones) route via EU NL proxy (`34.158.66.184:443`, Tailscale `100.79.1.73`).
  - Betboom Playwright browser container upgrade to `ghcr.io/datawikipro/igaming-source-base:latest`.
  - Ligastavok Qrator bypass and response payload interception.
- **Paced Kubernetes Deployment Management**: Implement paced rolling restarts (200ms delay per deployment) to eliminate etcd transaction latency spikes on Xeon hardware.
- **Cluster Line Audit & Definition of Done ($\ge 500$)**: Institutionalize `fast_cluster_audit.py` to continuously verify `SELECT count(*) FROM match_cache >= 500` across all 52 bookmaker databases with the mandatory 5-minute clean run schedule.

## Capabilities

### Modified Capabilities
- `crawler-engine`: Enforce minimum match volume threshold ($\ge 500$ active matches in `match_cache`) across all 52 supported bookmakers, resilient multi-sport batching, and target proxy routing.
- `k8s-infrastructure`: Standardize 512Mi memory limits on all bookmaker PostgreSQL StatefulSets, enforce K8s DNS naming only, and implement paced deployment rollout automation.
- `verification-and-dod`: Establish line filling threshold ($\ge 500$ matches) as an explicit gate in Definition of Done alongside the 5-minute error-free pod status and strict physical server night-time maintenance windows (22:00–10:00).

## Impact

- **Affected Modules**: All 52 bookmaker services in `igaming-source-*`, `igaming-k8s/*.yaml`, aggregator core event matcher.
- **Kubernetes Infrastructure**: `igaming-source` namespace, 52 PostgreSQL StatefulSets, 101 crawler and loader deployments.
- **Arbitrage & Performance**: Total line depth expands towards ~150,000+ active events, dramatically multiplying cross-bookmaker arbitrage and +EV opportunities.
