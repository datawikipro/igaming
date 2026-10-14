## Why

In `igaming-source-core`, `AbstractBaseBookmakerService.loadMatchCards()` suffers from two major scalability issues under high event load:
1. Every invocation of `loadMatchCards()` (running every 5s across 80 crawlers) calls `matchCacheRepository.resetStuckPendingMatches(LocalDateTime.now(), LocalDateTime.now().minusMinutes(3))` inside the database transaction, producing dozens of redundant table updates per second and database contention.
2. Parallel processing of match cards instantiates and destroys an OS platform thread pool (`Executors.newCachedThreadPool()`) on every single batch, introducing substantial thread allocation overhead and kernel thread exhaustion risks during traffic spikes.

Migrating to Java 21 Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`) and decoupling stuck match reclamation into a dedicated periodic scheduler eliminates database write contention on the hot path and maximizes concurrent I/O throughput.

## What Changes

- **Virtual Threads for Parallel Match Enrichment**: In `AbstractBaseBookmakerService.loadMatchCards()`, replace `Executors.newCachedThreadPool()` with Java 21 `Executors.newVirtualThreadPerTaskExecutor()`, utilizing try-with-resources for deterministic lifecycle management.
- **Dedicated Stuck Match Reset Scheduler**: Remove `matchCacheRepository.resetStuckPendingMatches()` from the inline transaction of `loadMatchCards()`. Introduce a decoupled `@Scheduled(fixedDelay = 60_000, initialDelay = 10_000)` reclamation loop that runs once every 60 seconds.
- **Improved Timeout and Stale Filtering**: Ensure completed futures and timeout errors in match card loads are cleanly handled without leaving orphaned locks.

## Capabilities

### Modified Capabilities
- `crawler-engine`: Update `Dual Execution Roles` requirement to mandate Java 21 Virtual Threads for match loader parallelism and decoupled periodic reclamation of stuck pending matches.

## Impact

- `igaming-source-core`: `AbstractBaseBookmakerService` updated.
- All 80 crawler microservices extending `AbstractBaseBookmakerService` immediately inherit high-throughput virtual threads and reduced database contention without code changes in individual scrapers.
