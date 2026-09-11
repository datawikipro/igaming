## 1. Zenit Prematch Line Fix

- [x] 1.1 Analyze Zenit prematch endpoint parameters and failure response
- [x] 1.2 Update ZenitApiClient prematch fetch logic and cookie refresh
- [x] 1.3 Verify Zenit prematch match cache populates 500+ matches

## 2. Pinnacle & Betboom Pipeline Hardening

- [x] 2.1 Update aggregator DNS URLs in Pinnacle and Betboom K8s manifests
- [x] 2.2 Re-deploy Pinnacle and Betboom and verify line ingestion into `odds_actual`

## 3. Proxy & Error Resilience Across Loaders

- [x] 3.1 Verify Marathonbet, 1xBet, Olimpbet loader throughput
- [x] 3.2 Ensure non-fatal recovery when proxy rotation limits are reached

## 4. Multi-Bookmaker Arbitrage Validation & DoD

- [x] 4.1 Verify multi-bookmaker matches in `match_record` reach 50+
- [x] 4.2 Validate real-time surebets & valuebets delivery to `smartbet.guru`
- [x] 4.3 Verify 5-minute continuous error-free operation in K8s
