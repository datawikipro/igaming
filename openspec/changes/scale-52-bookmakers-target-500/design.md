## Context

The SmartBet.guru betting arbitrage cluster aggregates 52 distinct bookmakers running in Kubernetes (`igaming-source` namespace).
Hardware and topology constraints:
- **`xeon-srv`**: On-premise master & primary compute host located in St. Petersburg (128 GB RAM, multi-core Xeon). Subject to strict night-time quiet hours (22:00–10:00 MSK), during which hardware reboots are strictly forbidden.
- **`ru-proxy` (`100.83.113.50`)**: Cluster network gateway on residential domestic IP (`188.242.33.93`) running `sing-box` smart routing.
- **`outline-vpn-eu-nl` (`100.79.1.73`)**: Netherlands datacenter egress proxy for offshore and European sportsbooks.
- **GCP Nodes**: Cloud worker instances in `us-central1-a` for supplementary workloads.

See `proposal.md` for motivation and background.

---

## Goals / Non-Goals

**Goals:**
- Maintain $\ge 500$ active matches in `match_cache` across each of the 52 bookmaker databases.
- Ensure all 52 PostgreSQL StatefulSets operate stably with 512Mi memory limits and complete DDL schemas without OOM restarts.
- Systematize the status of all 52 bookmakers into a clear review matrix (Done vs Remaining).
- Standardize proxy egress routing based on bookmaker regulatory jurisdiction.
- Paced Kubernetes deployment management (200ms interval) to guarantee zero `etcd` transaction latency starvation.

**Non-Goals:**
- Hardware reboot or power cycle of `xeon-srv` between 22:00 and 10:00 MSK.
- Truncation or suppression of high-yield or outlier odds (Golden Rule #9: No Silent Drop).
- Altering core matching mathematical formulas inside `igaming-aggregator`.

---

## Decisions

### Decision 1: Database Memory Scaling (512Mi vs 96Mi)
- **Choice**: Upgrade all 52 PostgreSQL StatefulSet manifests from `limits.memory: 96Mi` to `limits.memory: 512Mi` (`requests.memory: 128Mi`, `limits.cpu: 500m`, `requests.cpu: 50m`).
- **Rationale**: When bookmakers accumulate over 4,000 matches with 40,000+ factor records, PostgreSQL RSS naturally reaches 140–210 MB. At 96Mi, the Linux kernel OOM killer terminates PostgreSQL (`Exit Code: 137`), corrupting active connections and wiping ephemeral volumes.
- **Capacity**: 52 databases * 512Mi = 26.6 GB maximum theoretical allocation, safely fitting within the 128 GB RAM available on `xeon-srv` (<22% memory utilization).

### Decision 2: Centralized Schema Healing & Auto-Provisioning
- **Choice**: Execute schema verification and missing table creation (`fix_db_schemas.py`) across all bookmaker databases centrally through `igaming-aggregator-db-0`.
- **Rationale**: If a bookmaker database restarts or is recreated, running crawler/loader pods do not rerun their `initContainer db-schema-check` because init containers only execute on pod initialization. Centralized schema validation ensures tables `match_cache`, `league_cache`, `sport_cache`, `match_factor`, `unmapped_bet`, `unmapped_sport`, and `mapping_conflict` are always present.

### Decision 3: Paced Deployment Rollouts (200ms Interval)
- **Choice**: Deploy rolling restarts via a paced script (`restart_all_deployments.py`) introducing a 200ms sleep between consecutive deployment triggers.
- **Rationale**: Issuing simultaneous rollout restarts across 101 deployments floods `etcd` with raft log write transactions, exceeding lease renewal thresholds and causing `kube-apiserver` connection drops. Pacing smooths control-plane load completely.

### Decision 4: Split-Horizon Proxy Routing via `sing-box`
- **Choice**: 
  - Direct route (`188.242.33.93`): All Russian TSUPIS bookmakers (Winline, Fonbet, Pari, Betcity, Baltbet, Olimpbet, Tennisi, Leon).
  - EU Proxy route (`100.79.1.73:443`): European & offshore bookmakers (Pinnacle, Sbobet, Bet365, Bwin, Betsson, Betsafe, Nordicbet, MrGreen, 888starz, Betwinner, 1x-clones).
- **Rationale**: Russian bookmakers actively block European datacenter/cloud IPs (403 Forbidden / Cloudflare challenge), while offshore bookmakers block Russian domestic IPs or are blocked by Roskomnadzor DPI.

---

## 52-Bookmaker Status & Review Matrix

### Category A: Fully Completed & Verified Healthy ($\ge 500$ Matches)
| Bookmaker | Verified Match Count | Architecture / Notes |
|---|---|---|
| `betm` | 4,185 | Direct API, high throughput, stable |
| `betcity` | 3,705 | Direct API, full prematch tree |
| `bettery` | 2,103 | Fonbet engine clone, healthy |
| `fon-bet-by` | 1,950 | Fonbet Belarus feed, healthy |
| `fon-bet-kz` | 1,474 | Fonbet Kazakhstan feed, healthy |
| `fon-bet-ru` | 917 | Primary Russian line, healthy |
| `tennisi` | 6,027 | Full line extractor, stable |
| `baltbet` | 3,791 | High-volume prematch and live |
| `pari` | 4,245 | Scaled up after memory increase to 512Mi |
| `winline` | 3,282 | Playwright crawler + API loader, healthy |
| `olimpbet` | 2,003 | Direct API, healthy |
| `melbet` | 1,497 | BetB2B line, multi-sport |
| `spinbetter` | 1,231 | BetB2B line, multi-sport |
| `fansport` | 1,212 | BetB2B line, multi-sport |
| `melbet-com` | 1,207 | BetB2B line, multi-sport |
| `888starz` | 1,228 | BetB2B line, multi-sport |
| `betwinner` | 1,202 | BetB2B line, multi-sport |
| `megapari` | 1,193 | BetB2B line, multi-sport |
| `linebet` | 1,188 | BetB2B line, multi-sport |
| `betlabel` | 1,168 | BetB2B line, multi-sport |
| `betandyou` | 1,156 | BetB2B line, multi-sport |
| `1xbet` | 1,140 | BetB2B primary line |
| `1xbit` | 1,151 | Database fixed to `igaming_1xbit` |

### Category B: In Progress / Partially Crawling (<500 Matches)
| Bookmaker | Current Match Count | Immediate Next Step |
|---|---|---|
| `marathonbet` | 180 (585 leagues) | League parser running, climbing to ~1,500 |
| `marathonbet-com` | 2 leagues | Needs category iteration loop restart |
| `marathonbet-by` | 2 leagues | Needs category iteration loop restart |
| `zenit` | 0 | Pre-flight `imprintHash` warm-up stabilization |
| `olimpbet-kz` | 0 | Schema applied, needs rolling restart verification |
| `leon` | 0 | Schema applied, crawler restart verification |

### Category C: Blocked / Requires Container or Configuration Patch
| Bookmaker | Root Cause | Planned Fix |
|---|---|---|
| `betboom` | Missing Chromium binary in Jib container | Jib build using `igaming-source-base:latest` as base image |
| `ligastavok` | Qrator 403 on direct URL navigation | Stealth cookies warm-up and API direct endpoint |
| `sunbet` | Invalid base URL in K8s manifest | Update URL to `https://www.sunbet.co.za` and route via EU proxy |
| `pinnacle` | Offshore API auth / proxy | Verify Guest API ingestion through `100.79.1.73` |
| `sbobet` | Asian feed retry timeouts | Route via `100.79.1.73` with keep-alive session |
| `bet365` | Geo-blocking / TLS fingerprinting | Route via EU NL proxy with TLS Client Hello masking |
| `bwin` | European line 403 | Route via `100.79.1.73` |
| `betsson` / `betsafe` / `nordicbet` | Scandinavian platform cluster | Route via EU NL proxy, verify common API client |
| `mrgreen` | European feed geo-block | Route via EU NL proxy |
| `dafabet` | Asian line session token | Verify proxy and cookie rotation |
| `digitain` | Token auth header parsing | Fix authorization headers in crawler |
| `rushbet` / `betplay` | Latin America / European platforms | Verify database schema and proxy |
| `betmgm` / `betmgm-firefox` | US geolocation restriction | Route via `outline-vpn-us` (`100.66.190.4`) |
| `fanduel` / `caesars` | US geolocation restriction | Route via `outline-vpn-us` (`100.66.190.4`) |

---

## Risks / Trade-offs

- **[Risk: Physical Server Down during Night Hours]** $\rightarrow$ **Mitigation**: Strictly respect Golden Rule #10. Do not power cycle or reboot `xeon-srv` between 22:00 and 10:00 MSK. Perform full architectural review and preparation now; execute cluster verification once day window begins.
- **[Risk: High Load on Reboot]** $\rightarrow$ **Mitigation**: Deploy `restart_all_deployments.py` with 200ms pacing so containers initialize progressively without CPU spikes or power trips.
