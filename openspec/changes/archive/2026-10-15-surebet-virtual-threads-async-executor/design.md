# Design: Surebet Virtual Threads Async Executor

## Architecture Decisions

### 1. Dedicated `AsyncConfig` in `aggregator-surebet`
- Declare an `AsyncConfig` with `@EnableAsync` in `pro.datawiki.igaming.aggregator.surebet.config`.
- Configure `@Bean(name = "surebetExecutor")` using Spring's `SimpleAsyncTaskExecutor` with `setVirtualThreads(true)` and prefix `"surebet-vt-"`.
- This ensures any `@Async("surebetExecutor")` calls run on Java 21 Virtual Threads without blocking OS platform threads and without artificial queue capacity bounds.

### 2. Streamlining `AsyncConfig` in `aggregator-ingestion`
- In `aggregator-ingestion`, remove the legacy `ThreadPoolTaskExecutor(16, 32, 1000)` named `surebetExecutor`.
- Replace it with a clean virtual-thread `taskExecutor` (`SimpleAsyncTaskExecutor` with `setVirtualThreads(true)`).
