# Implementation Tasks

## 1. Persistence Layer: Redis Pipelining and Batch Upsert
- [x] 1.1 Implement `bulkUpsertOdds` in `RedisOddsStorageService` using `redisTemplate.executePipelined`
- [x] 1.2 Implement `bulkUpsertActualOdds` in `OddsActualRepository` / custom repository implementation using `JdbcTemplate.batchUpdate`
- [x] 1.3 Add deterministic sorting to odds records before database batch execution to prevent `40P01` deadlocks

## 2. Ingestion Service Refactoring
- [x] 2.1 Refactor `OddsUpsertManager.upsertActualOdds` to aggregate normalized odds into batches and call the bulk persistence operations
- [x] 2.2 Implement resilient fallback in `OddsUpsertManager` to guarantee zero quote loss if a batch encounters an unexpected error
- [x] 2.3 Optimize `MatchAggregatorService.processFastBatch` to leverage the new batch odds pipeline

## 3. Verification & Testing
- [x] 3.1 Verify compilation across `aggregator-domain` and `aggregator-ingestion`
- [x] 3.2 Execute unit tests for batch persistence and pipelining
- [x] 3.3 Validate OpenSpec change compliance via `npx --yes openspec validate --specs`
