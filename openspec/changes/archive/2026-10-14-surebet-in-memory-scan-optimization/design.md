## Context

See proposal.md - Why.
In `aggregator-surebet`, `SurebetDetectorService.fullScan()` evaluates multi-bookmaker matches for arbitrage opportunities. Currently:
1. For every candidate `matchId`, `matchRepository.findMetadataById(matchId)` is invoked inside an individual `transactionTemplate.execute(...)` transaction, triggering hundreds of redundant database round-trips per scan cycle.
2. Parallel execution is constrained to a fixed 8-thread pool (`Executors.newFixedThreadPool(8)`).

## Goals / Non-Goals

**Goals:**
- Add `findMetadataByIdIn(Collection<Long> ids)` to `MatchRepository` in `aggregator-domain`.
- Refactor `fullScan()` in `SurebetDetectorService` to pre-fetch all metadata in bulk into an in-memory lookup map before parallel evaluation.
- Upgrade `scanExecutor` to Java 21 Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`).
- Keep rule evaluation completely in-memory, invoking database operations only when alerts must be persisted or purged.

**Non-Goals:**
- Modifying the mathematical return or stake calculation in `SurebetRuleEvaluator`.
- Modifying Redis storage formats in `RedisOddsStorageService`.

## Decisions

- **Chunked Bulk Retrieval**: Query metadata in chunks up to 1000 IDs to ensure PostgreSQL IN-clause efficiency, aggregating results into a `Map<Long, MatchMetadataProjection>`.
- **Java 21 Virtual Thread Scan Executor**: Replace the fixed 8-thread executor with `Executors.newVirtualThreadPerTaskExecutor()`, enabling instantaneous scheduling of all match evaluation tasks concurrently.
- **Targeted Transaction Boundaries**: Remove the encompassing transaction wrapper around `getOddsForMatch` and `evaluateRules`; only write operations in `alertManager` need transactional boundaries.

## Risks / Trade-offs

- **[Risk] High Memory Footprint during large scans**:
  - **Mitigation**: `MatchMetadataProjection` is a lightweight read-only interface projection containing only 6 fields (`id`, `team1Name`, `team2Name`, `sportName`, `startTime`, `isLive`), requiring less than a few megabytes for thousands of matches.
