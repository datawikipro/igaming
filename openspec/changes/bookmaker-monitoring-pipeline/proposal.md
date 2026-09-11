## Why

To support real-time arbitrage (surebet) and valuebet detection on SmartBet.guru, the system requires active multi-bookmaker crawlers/loaders continuously streaming odds into Kafka, an ingestion engine persisting odds into PostgreSQL without constraint conflicts, and automated scanners detecting positive mathematical expectation (+EV) and surebets.

## What Changes

- **Live Bookmaker Activation**: Scale and run key bookmaker crawler & loader microservices (`winline`, `fon-bet-ru`, `leon`, `marathonbet`, `olimpbet`, `betcity`, `baltbet`, `zenit`, `pinnacle`) in namespace `igaming-source`.
- **Database Schema Fix for Upserts**: Establish unique index `uq_odds_actual_upsert` on `odds_actual(match_id, bet_source_id, odds_type_id, COALESCE(param, -10000.0), COALESCE(outright_selection_id, -1))` in `igaming-aggregator-db` to enable conflict-free native upserts from Kafka ingestion workers.
- **Aggregator Pipeline Activation**: Run `igaming-aggregator-ingestion` (Kafka consumer) and `igaming-aggregator-surebet` (arbitrage/valuebet detector) in namespace `igaming-master`.
- **Autonomous Monitoring & Health Verification**: Launch dedicated monitoring processes to track Kafka lag, loader heartbeats, and surebet detection rates.

## Capabilities

### Modified Capabilities
- `crawler-engine`: Support concurrent multi-source bookmaker ingestion with automatic health checks and staggered CPU throttling.
- `aggregator-core`: High-throughput Kafka `odds.updates` consumption with conflict-free PostgreSQL upsert and automated periodic surebet/valuebet scans.

## Impact

- **Microservices**: `igaming-aggregator-ingestion`, `igaming-aggregator-surebet`, `igaming-source-*` (crawlers and loaders).
- **Databases**: `igaming-aggregator-db` (`odds_actual`, `match_record`, `surebet_alert`, `valuebet_alert`).
- **Messaging**: Kafka broker topic `odds.updates`.
