# Change Proposal: Normalizer Virtual Threads and Parallel Worker

## Why
Currently in `aggregator-normalizer`:
1. **Sequential Odds Type Processing**: In `NormalizationWorker.processOddsTypePendingRequests()`, a batch of 200 pending odds type normalization requests is processed in a sequential `for` loop on a single scheduler thread. If an item requires remote LLM normalization, the entire batch blocks, creating a severe backlog of unmapped odds types.
2. **Platform Thread Pool Limitation**: In `NormalizationWorker.init()`, the worker allocates a fixed pool of 30 OS platform threads (`Executors.newFixedThreadPool(parallelThreads)`). Since LLM Gateway requests are I/O-bound HTTP calls, 30 platform threads can easily be exhausted under sudden traffic surges, wasting OS memory and causing thread context-switching overhead.

## What Changes
1. **Java 21 Virtual Threads for NormalizationWorker**:
   - Replace `Executors.newFixedThreadPool(parallelThreads)` with `Executors.newVirtualThreadPerTaskExecutor()`.
   - Remove thread-pool sizing constraints while drastically reducing JVM heap consumption.
2. **Parallelize `processOddsTypePendingRequests`**:
   - Process odds type batches concurrently using `CompletableFuture.runAsync(..., executorService)` so fast-track in-memory matches resolve immediately without waiting on LLM calls.

## Impact
- **Specs Impact**: Modifies requirement in `aggregator-core` specification.
- **Performance Impact**: High concurrency for odds type resolution, eliminating scheduler stalling during LLM Gateway network latency.
