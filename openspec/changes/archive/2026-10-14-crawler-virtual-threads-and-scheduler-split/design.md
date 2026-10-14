## Context

See proposal.md - Why.
In `igaming-source-core`, `AbstractBaseBookmakerService` provides the base orchestration logic for match-loader instances across 80+ sportsbooks. The current `loadMatchCards` implementation contains two major scalability bottlenecks:
1. Redundant database UPDATE statements on every polling tick (`resetStuckPendingMatches`).
2. Heavyweight OS thread creation via `Executors.newCachedThreadPool()` on every batch.

## Goals / Non-Goals

**Goals:**
- Migrate match enrichment concurrency to Java 21 Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`).
- Utilize try-with-resources for executor lifecycle management.
- Remove `resetStuckPendingMatches` from the transactional hot path of `loadMatchCards`.
- Provide a decoupled periodic maintenance task running once every 60 seconds to reclaim stuck pending matches.
- Maintain complete binary and source compatibility for all 80 crawler modules inheriting from `AbstractBaseBookmakerService`.

**Non-Goals:**
- Modifying individual bookmaker scraping parsers or DTO mappings.
- Changing `matchCacheRepository` query semantics.

## Decisions

- **Java 21 Virtual Thread Per Task Executor with Try-with-Resources**:
  `Executors.newVirtualThreadPerTaskExecutor()` enables thousands of concurrent HTTP/REST scraping requests without platform thread pool exhaustion. In Java 21, `ExecutorService` implements `AutoCloseable`, guaranteeing that `close()` awaits submitted task completion cleanly.
- **Dedicated Scheduled Reclamation Loop**:
  Move `matchCacheRepository.resetStuckPendingMatches(...)` to a dedicated method `@Scheduled(fixedDelay = 60_000, initialDelay = 10_000) public void resetStuckMatchesPeriodically()` executed in its own isolated transaction template.

## Risks / Trade-offs

- **[Risk] Pinning of Carrier Threads**: If a crawler uses `synchronized` blocks around blocking network I/O, carrier thread pinning could occur.
  - **Mitigation**: The bookmaker API clients in our ecosystem use Spring `RestTemplate`, `WebClient`, or Apache HttpClient, which do not pin virtual threads.
