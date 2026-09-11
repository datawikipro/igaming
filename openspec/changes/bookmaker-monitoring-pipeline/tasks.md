## 1. Database & Ingestion Setup

- [x] 1.1 Apply `uq_odds_actual_upsert` unique index on `odds_actual` in `igaming-aggregator-db`
- [x] 1.2 Verify `igaming-aggregator-ingestion` connects to Kafka `odds.updates` and processes odds stream without SQL errors

## 2. Bookmaker Source Activation

- [x] 2.1 Scale and initialize primary bookmaker crawlers & loaders (`winline`, `fon-bet-ru`, `leon`, `marathonbet`, `olimpbet`)
- [x] 2.2 Scale additional supporting bookmakers (`baltbet`, `betcity`, `betboom`, `zenit`, `pinnacle`, `1xbet`, `pari`, `ligastavok`, `tennisi`, `melbet`, `bet365`, `sbobet`)
- [x] 2.3 Verify heartbeats and active odds streaming into `odds_actual`

## 3. Surebet Scanner & Pipeline Verification

- [x] 3.1 Verify `igaming-aggregator-surebet` scheduled scanner execution loop
- [x] 3.2 Verify `igaming-portal` gateway `/api/v1/bookmakers` and matches endpoints

## 4. Continuous Subagent Monitoring

- [x] 4.1 Launch subagent process for Kafka offset and ingestion rate monitoring
- [x] 4.2 Launch subagent process for multi-bookmaker match overlap and surebet tracking
