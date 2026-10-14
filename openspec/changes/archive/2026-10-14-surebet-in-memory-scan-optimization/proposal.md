## Why

In `aggregator-surebet`, `SurebetDetectorService.fullScan()` currently executes an N+1 query pattern: for every candidate match ID (up to hundreds of matches per scan cycle), it opens a separate database transaction and invokes `matchRepository.findMetadataById(matchId)`. This causes hundreds of database round-trips and transaction start/commit overhead on every scan. Furthermore, task concurrency is capped at a fixed 8-thread pool (`Executors.newFixedThreadPool(8)`).

Batching metadata retrieval (`findMetadataByIdIn`) and processing scan tasks with Java 21 Virtual Threads converts the scan loop into an in-memory evaluation pipeline, reducing database load by over 90% and speeding up arbitrage detection cycle latency.

## What Changes

- **Batch Metadata Retrieval in `MatchRepository`**: Add `findMetadataByIdIn(Collection<Long> ids)` to `MatchRepository` in `aggregator-domain`, pre-fetching all candidate match metadata in a single indexed SQL query.
- **In-Memory Scan Pipeline in `SurebetDetectorService`**: Refactor `fullScan()` to bulk-fetch metadata maps for all active candidate match IDs upfront. Remove per-match metadata DB queries and individual transactions from the hot evaluation loop.
- **Java 21 Virtual Threads in Scan Executor**: Modernize `scanExecutor` to leverage Java 21 Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`), allowing non-blocking parallel evaluation of all active events simultaneously.

## Capabilities

### Modified Capabilities
- `aggregator-core`: Update `In-Memory Surebet Detection` requirement to mandate batch metadata pre-fetching and Virtual Thread concurrency to prevent N+1 database overhead.

## Impact

- `aggregator-domain`: `MatchRepository` adds `findMetadataByIdIn`.
- `aggregator-surebet`: `SurebetDetectorService` refactored to eliminate N+1 queries.
