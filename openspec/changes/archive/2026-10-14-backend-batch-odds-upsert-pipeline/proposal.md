# Proposal: Batch Odds Upsert and Redis Pipeline Architecture

## Motivation
In the current aggregation pipeline (`aggregator-domain`), `OddsUpsertManager.upsertActualOdds()` iterates over all incoming odds items for a match sequentially:
```java
for (OddItem itemDto : validOdds) {
    // 1. Individual Redis HSET / network round-trip per odd
    redisOddsStorageService.upsertOdd(...);
    // 2. Individual SQL INSERT ... ON CONFLICT DO UPDATE per odd!
    oddsActualRepository.upsertActualOdd(...);
}
```
A single soccer, basketball, or tennis match frequently carries between 200 and 800 distinct betting outcomes. Under load with 30-50 active bookmaker crawlers streaming odds updates simultaneously, this architectural anti-pattern causes:
1. Massive N+1 transaction overhead (hundreds of thousands of individual SQL statements executed per minute).
2. Database lock contention and PostgreSQL deadlocks (error code `40P01`), which previously required thread-sleep retries in `MatchAggregatorService`.
3. High Redis command latency and thread pool contention due to lack of command pipelining.

## Proposed Changes
1. **Repository Batch Upsert**: Introduce batch upsert capability in `OddsActualRepository` / `JdbcTemplate` to persist all normalized odds of a match in a single batch operation (`executeBatch` or multi-row `INSERT INTO odds_actual (...) VALUES (...), (...), ... ON CONFLICT (...) DO UPDATE`).
2. **Redis Pipeline Execution**: Introduce `bulkUpsertOdds(Long matchId, List<RedisOddEntry> entries)` in `RedisOddsStorageService` using `redisTemplate.executePipelined(...)` to perform all hash and index updates in 1 round-trip per match.
3. **Refactored `OddsUpsertManager`**: Pre-resolve normalization IDs (market and oddsType) and dispatch bulk collections to Redis and PostgreSQL batches, reducing processing time per match by 90-95%.
4. **Eliminate Deadlocks**: Maintain deterministic sorting of odds records before batch insertion, eliminating the root cause of `40P01` deadlocks.

## Capabilities & Specs
- Modifies capability: `specs/aggregator-core/spec.md` with requirement for high-throughput batch odds persistence and Redis pipelining.

## Risks & Mitigations
- **Data Integrity**: Batch persistence must exactly match the semantics of the existing single-row upsert, ensuring timestamps, source IDs, external IDs, and URLs are accurately updated.
- **Partial Failure Handling**: If a batch insertion encounters a syntax or constraint violation, an automatic fallback to safe individual upserting ensures no valid quotes are dropped, in full adherence to the Zero Outlier Discard Policy.
