## 1. Physical Server Availability & Cluster Prerequisite (Morning Window)

- [ ] 1.1 Verify physical server `xeon-srv` power on after 10:00 MSK (Golden Rule #10 acoustic limit)
- [ ] 1.2 Verify Ethernet carrier signal on router `ru-proxy` port `enp3s0` (`/sys/class/net/enp3s0/carrier` == 1)
- [ ] 1.3 Verify local network ping response from `xeon-srv` (`172.16.0.29`)
- [ ] 1.4 Verify Tailscale tunnel connection to `xeon-srv` (`100.78.183.101:6443`)
- [ ] 1.5 Verify Kubernetes node readiness (`kubectl get nodes`) for `xeon-srv`

## 2. Cluster Baseline Infrastructure & Schema Verification (Completed Milestones)

- [x] 2.1 Upgrade all 52 PostgreSQL StatefulSets from 96Mi to 512Mi memory limits in `igaming-k8s/*.yaml`
- [x] 2.2 Standardize non-blocking HikariCP and `synchronous_commit = off` across database configs
- [x] 2.3 Run centralized DDL schema auto-provisioning (`fix_db_schemas.py`) across all 52 databases via `igaming-aggregator-db-0`
- [x] 2.4 Fix database name mapping for `1xbit` (`igaming_1xbit`)
- [x] 2.5 Fix database name mapping for `betmgm-firefox` (`igaming_betmgm`)
- [x] 2.6 Implement paced rolling restart automation (`restart_all_deployments.py`) with 200ms delay to prevent `etcd` starvation

## 3. Russian Licensed Bookmakers (TSUPIS) Verification (Target >= 500 Matches)

- [x] 3.1 **BetM** (`igaming-source-betm`): verify line accumulation reaches $\ge 500$ matches (Achieved: 4,185 matches)
- [x] 3.2 **Betcity RU** (`igaming-source-betcity`): verify line accumulation reaches $\ge 500$ matches (Achieved: 3,705–5,005 matches)
- [x] 3.3 **Bettery** (`igaming-source-bettery`): verify line accumulation reaches $\ge 500$ matches (Achieved: 2,103 matches)
- [x] 3.4 **Fonbet RU** (`igaming-source-fon-bet-ru`): verify line accumulation reaches $\ge 500$ matches (Achieved: 917 matches)
- [x] 3.5 **Pari RU** (`igaming-source-pari`): verify line accumulation reaches $\ge 500$ matches (Achieved: 4,245 matches)
- [x] 3.6 **Winline RU** (`igaming-source-winline`): verify line accumulation reaches $\ge 500$ matches (Achieved: 3,282 matches)
- [x] 3.7 **Baltbet** (`igaming-source-baltbet`): verify line accumulation reaches $\ge 500$ matches (Achieved: 3,791 matches)
- [x] 3.8 **Tennisi RU** (`igaming-source-tennisi`): verify line accumulation reaches $\ge 500$ matches (Achieved: 6,027 matches)
- [x] 3.9 **Olimpbet RU** (`igaming-source-olimpbet`): verify line accumulation reaches $\ge 500$ matches (Achieved: 2,003 matches)
- [ ] 3.10 **Leon RU** (`igaming-source-leon`): verify crawler restart with schema applied and confirm `match_cache >= 500`
- [ ] 3.11 **Zenit** (`igaming-source-zenit`): stabilize `imprintHash` session cookies and query full prematch tree until `match_cache >= 500`
- [ ] 3.12 **Betboom** (`igaming-source-betboom`): rebuild container with Jib on `ghcr.io/datawikipro/igaming-source-base:latest` (Chrome included), deploy, and verify `match_cache >= 500`
- [ ] 3.13 **Liga Stavok** (`igaming-source-ligastavok`): configure stealth browser profile to bypass Qrator 403, intercept line API responses, and verify `match_cache >= 500`

## 4. CIS Regional Bookmakers (KZ / BY) Verification (Target >= 500 Matches)

- [x] 4.1 **Fonbet Belarus** (`igaming-source-fon-bet-by`): verify line accumulation reaches $\ge 500$ matches (Achieved: 1,950 matches)
- [x] 4.2 **Fonbet Kazakhstan** (`igaming-source-fon-bet-kz`): verify line accumulation reaches $\ge 500$ matches (Achieved: 1,474 matches)
- [ ] 4.3 **Olimpbet Kazakhstan** (`igaming-source-olimpbet-kz`): verify post-schema restart and confirm `match_cache >= 500`
- [ ] 4.4 **Marathonbet Belarus** (`igaming-source-marathonbet-by`): execute category iteration and verify `match_cache >= 500`

## 5. Marathonbet Line Expansion Across Portals (Target >= 500 Matches)

- [ ] 5.1 **Marathonbet RU** (`igaming-source-marathonbet`): complete category batch parser across 585 leagues until `match_cache >= 500` (Currently: 180 matches, 585 leagues)
- [ ] 5.2 **Marathonbet COM** (`igaming-source-marathonbet-com`): route via EU NL proxy `100.79.1.73`, iterate category feed, and verify `match_cache >= 500`

## 6. BetB2B / 1x Family Clones Verification (Target >= 500 Matches Each)

- [x] 6.1 **Melbet** (`igaming-source-melbet`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,497 matches)
- [x] 6.2 **Spinbetter** (`igaming-source-spinbetter`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,231 matches)
- [x] 6.3 **888starz** (`igaming-source-888starz`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,228 matches)
- [x] 6.4 **Fansport** (`igaming-source-fansport`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,212 matches)
- [x] 6.5 **Melbet COM** (`igaming-source-melbet-com`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,207 matches)
- [x] 6.6 **Betwinner** (`igaming-source-betwinner`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,202 matches)
- [x] 6.7 **Megapari** (`igaming-source-megapari`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,193 matches)
- [x] 6.8 **Linebet** (`igaming-source-linebet`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,188 matches)
- [x] 6.9 **Betlabel** (`igaming-source-betlabel`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,168 matches)
- [x] 6.10 **Betandyou** (`igaming-source-betandyou`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,156 matches)
- [x] 6.11 **1xBet** (`igaming-source-1xbet`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,140 matches)
- [x] 6.12 **1xBit** (`igaming-source-1xbit`): verify multi-sport line accumulation reaches $\ge 500$ matches (Achieved: 1,151 matches)
- [ ] 6.13 **22Bet** (`igaming-source-22bet`): verify crawler restart with schema applied and confirm `match_cache >= 500`

## 7. European & Offshore Bookmakers Activation via EU NL Proxy (Target >= 500 Matches Each)

- [ ] 7.1 **Sunbet** (`igaming-source-sunbet`): update URL in manifest to `https://www.sunbet.co.za`, route through EU NL proxy `100.79.1.73`, and verify `match_cache >= 500`
- [ ] 7.2 **Pinnacle** (`igaming-source-pinnacle`): verify Guest API line collection via EU NL proxy and confirm `match_cache >= 500`
- [ ] 7.3 **Sbobet** (`igaming-source-sbobet`): verify session retry logic and connection via EU NL proxy until `match_cache >= 500`
- [ ] 7.4 **Bet365** (`igaming-source-bet365`): configure TLS Client Hello prefix masking through EU NL proxy and verify `match_cache >= 500`
- [ ] 7.5 **Bwin** (`igaming-source-bwin`): route through EU NL proxy, verify European line ingestion until `match_cache >= 500`
- [ ] 7.6 **Betsson** (`igaming-source-betsson`): route through EU NL proxy and verify `match_cache >= 500`
- [ ] 7.7 **Betsafe** (`igaming-source-betsafe`): route through EU NL proxy and verify `match_cache >= 500`
- [ ] 7.8 **Nordicbet** (`igaming-source-nordicbet`): route through EU NL proxy and verify `match_cache >= 500`
- [ ] 7.9 **MrGreen** (`igaming-source-mrgreen`): route through EU NL proxy and verify `match_cache >= 500`
- [ ] 7.10 **Dafabet** (`igaming-source-dafabet`): route through EU NL proxy, verify Asian line session tokens, and confirm `match_cache >= 500`
- [ ] 7.11 **Digitain** (`igaming-source-digitain`): fix authorization header token parser, route through proxy, and verify `match_cache >= 500`
- [ ] 7.12 **Rushbet** (`igaming-source-rushbet`): verify Latin America line extraction and confirm `match_cache >= 500`
- [ ] 7.13 **Betplay** (`igaming-source-betplay`): verify line collection through EU NL proxy and confirm `match_cache >= 500`
- [ ] 7.14 **888sport** (`igaming-source-888sport`): verify line collection through EU NL proxy and confirm `match_cache >= 500`
- [ ] 7.15 **Leovegas** (`igaming-source-leovegas`): verify line collection through EU NL proxy and confirm `match_cache >= 500`
- [ ] 7.16 **Sportbet** (`igaming-source-sportbet`): verify line collection through EU NL proxy and confirm `match_cache >= 500`
- [ ] 7.17 **Betcity COM** (`igaming-source-betcity-com`): route international mirror through EU NL proxy and verify `match_cache >= 500`

## 8. Americas Bookmakers Activation via US Proxy (Target >= 500 Matches Each)

- [ ] 8.1 **BetMGM** (`igaming-source-betmgm`): route via US exit node `outline-vpn-us` (`100.66.190.4`) and verify `match_cache >= 500`
- [ ] 8.2 **BetMGM Firefox** (`igaming-source-betmgm-firefox`): route via US exit node and verify `match_cache >= 500`
- [ ] 8.3 **FanDuel** (`igaming-source-fanduel`): route via US exit node and verify `match_cache >= 500`
- [ ] 8.4 **Caesars** (`igaming-source-caesars`): route via US exit node and verify `match_cache >= 500`

## 9. Cluster-Wide Line Verification & Definition of Done

- [ ] 9.1 Execute `python scratch/fast_cluster_audit.py` and verify all 52 bookmakers show status `HEALTHY (>=500)`
- [ ] 9.2 Confirm total line depth across cluster exceeds 800,000 active odds in `odds_actual`
- [ ] 9.3 Confirm cross-bookmaker arbitrage generation produces active opportunities in `surebet_alert`
- [ ] 9.4 Execute 5-minute continuous error-free pod status verification in Kubernetes via `schedule`
- [ ] 9.5 Run `openspec validate --specs` across the repository and commit verified artifacts
