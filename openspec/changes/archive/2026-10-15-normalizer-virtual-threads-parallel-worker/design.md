# Design: Normalizer Virtual Threads and Parallel Worker

## Architecture Decisions

### 1. Java 21 `Executors.newVirtualThreadPerTaskExecutor()`
- Replace:
  ```java
  this.executorService = Executors.newFixedThreadPool(parallelThreads);
  ```
  with:
  ```java
  this.executorService = Executors.newVirtualThreadPerTaskExecutor();
  ```
- LLM calls take hundreds of milliseconds of network waiting. Virtual threads yield when waiting on HTTP I/O, allowing tens of thousands of concurrent tasks with almost zero OS memory footprint.

### 2. Parallelizing `processOddsTypePendingRequests()`
- Replace the sequential `for (NormalizationRequest req : oddsTypePending)` loop with:
  ```java
  List<CompletableFuture<Void>> futures = oddsTypePending.stream()
          .map(req -> CompletableFuture.runAsync(() -> {
              ...
          }, executorService))
          .collect(Collectors.toList());
  CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
  ```
- This unblocks in-memory dictionary hits instantly while long-running LLM calls run in parallel virtual threads.
