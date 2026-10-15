# Tasks: Normalizer Virtual Threads and Parallel Worker

- [x] 1. Switch executorService in NormalizationWorker.init() to Java 21 Virtual Threads <!-- id: 1-virtual-threads-executor -->
- [x] 2. Refactor processOddsTypePendingRequests to process batches in parallel with CompletableFuture <!-- id: 2-parallelize-odds-type-processing -->
- [x] 3. Run automated tests and compilation across aggregator-normalizer <!-- id: 3-run-tests -->
