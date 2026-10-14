# Technical Design: Batch Odds Upsert and Redis Pipeline

## Context
See `proposal.md` for background and motivation.
Currently, `OddsUpsertManager.upsertActualOdds` in `aggregator-domain` loops over every item in `validOdds` and executes:
- An individual Spring Data JPA `@Modifying` native query `oddsActualRepository.upsertActualOdd(...)`.
- An individual Redis command `redisOddsStorageService.upsertOdd(...)`.
- A check against `durableOddsHistoryService.recordOddChangeAsync(...)`.

This causes substantial I/O overhead and database deadlocks when concurrent bookmaker workers update the same match.

## Goals / Non-Goals

**Goals:**
- Implement `JdbcTemplate`-backed batch upserting in `OddsActualRepository` (`bulkUpsertActualOdds`).
- Implement Redis command pipelining in `RedisOddsStorageService` (`bulkUpsertOdds`) via `executePipelined`.
- Refactor `OddsUpsertManager.upsertActualOdds` to aggregate normalized odds into batches and execute bulk operations.
- Ensure deterministic sorting of items in the batch to completely prevent PostgreSQL `40P01` deadlocks.
- Provide a robust fallback to individual upserts if a batch fails, guaranteeing zero quote loss.

**Non-Goals:**
- Schema/DDL changes on the `odds_actual` table.
- Changes to public REST APIs or DTO contracts.
- Altering the rules or thresholds in `SurebetRuleEvaluator`.

## Decisions

### Decision 1: Spring `NamedParameterJdbcTemplate` / `JdbcTemplate` for Batch Upsert
- **Choice**: Use `JdbcTemplate.batchUpdate` with `INSERT INTO odds_actual (...) VALUES (...) ON CONFLICT (match_id, odds_type_id, param) DO UPDATE SET ...`.
- **Rationale**: Prepared statement batching is standard, type-safe, protects against SQL injection, and natively leverages PostgreSQL JDBC batch rewriting (`reWriteBatchedInserts=true`).
- **Alternative considered**: Generating raw dynamic SQL strings with comma-separated tuples. Rejected due to vulnerability to syntax errors, query size limits, and lack of parameter escaping.

### Decision 2: Redis Command Pipelining via `redisTemplate.executePipelined`
- **Choice**: Group all `hMSet` (or `hSet`) and `sAdd` operations for a match's odds into a single Redis pipeline execution.
- **Rationale**: Pipelining sends all commands in a single TCP network burst and reads all responses together, reducing Redis round-trip latency by 95% for matches with hundreds of outcomes.
- **Alternative considered**: Redis Transactions (`MULTI`/`EXEC`). Rejected because transactional atomicity is not strictly needed for caching and adds unnecessary rollback overhead.

### Decision 3: Deterministic Record Ordering for Deadlock Elimination
- **Choice**: Sort the batch records deterministically by `(odds_type_id, param)` prior to execution.
- **Rationale**: PostgreSQL row-level locks on `ON CONFLICT DO UPDATE` are acquired in the order rows are processed. When concurrent transactions acquire locks in the identical order, deadlock `40P01` is mathematically impossible.

### Decision 4: Safe Fallback Protocol (Zero Outlier Discard)
- **Choice**: Wrap the batch persistence in a try-catch block. If an unexpected `DataAccessException` occurs, log a warning and fall back to the existing sequential loop.
- **Rationale**: Protects data ingestion continuity and guarantees compliance with the Zero Outlier Discard Policy.

## Risks / Trade-offs
- **[Memory usage during batch construction]** → Batches are scoped per-match (typically 100-800 elements), keeping heap allocation negligible (<100KB per event).
- **[PostgreSQL Connection Pool Sizing]** → Reducing transaction duration from ~400ms to ~15ms will significantly free up HikariCP pool capacity.
