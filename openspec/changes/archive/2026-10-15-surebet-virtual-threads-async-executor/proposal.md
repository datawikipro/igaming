# Change Proposal: Surebet Virtual Threads Async Executor

## Why
Currently:
1. **Missing Async Executor in aggregator-surebet**: `SurebetDetectorService` uses `@Async("surebetExecutor")` for `detectSurebetsForMatch` and `detectSurebetsForOutright`. However, no bean named `surebetExecutor` was defined in the `aggregator-surebet` module context. As a result, Spring falls back to a default `SimpleAsyncTaskExecutor` creating unmanaged platform threads on every invocation.
2. **Obsolete Heavy ThreadPool in aggregator-ingestion**: `aggregator-ingestion` declared an unused `ThreadPoolTaskExecutor` (16-32 threads, 1000 capacity queue) named `surebetExecutor` left behind from when ingestion and surebet detection lived in the same monolithic application.
3. **High-Throughput Arbitrage Evaluation**: Arbitrage calculation tasks are frequent and lightweight. Java 21 Virtual Threads provide optimal non-blocking concurrency without queuing contention or thread pool starvation.

## What Changes
1. **Configure Dedicated Virtual Thread Executor in `aggregator-surebet`**:
   - Create `AsyncConfig` in `aggregator-surebet` providing `@Bean("surebetExecutor")` backed by Java 21 virtual threads via `SimpleAsyncTaskExecutor.setVirtualThreads(true)`.
2. **Clean Up `aggregator-ingestion` Async Configuration**:
   - Replace the legacy `ThreadPoolTaskExecutor` in `aggregator-ingestion` with a modern virtual-threads-based `taskExecutor`.

## Impact
- **Specs Impact**: Modifies requirement in `aggregator-core` specification.
- **Performance Impact**: Zero queue starvation and instant asynchronous dispatch for surebet detection tasks on Java 21 virtual threads.
