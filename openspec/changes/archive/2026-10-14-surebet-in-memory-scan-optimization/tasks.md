## 1. Batch Metadata Query in Aggregator Domain

- [x] 1.1 Add `findMetadataByIdIn(@Param("ids") Collection<Long> ids)` to `MatchRepository` in `aggregator-domain`.

## 2. In-Memory Surebet Scan Refactor

- [x] 2.1 Replace fixed 8-thread `scanExecutor` with Java 21 `Executors.newVirtualThreadPerTaskExecutor()` in `SurebetDetectorService`.
- [x] 2.2 Refactor `fullScan()` to bulk pre-fetch metadata for all candidate `matchIds` into a `Map<Long, MatchMetadataProjection>`.
- [x] 2.3 Eliminate per-match `findMetadataById` calls and remove unnecessary outer transactions from in-memory evaluation.

## 3. Verification & Testing

- [x] 3.1 Validate OpenSpec change compliance (`openspec validate surebet-in-memory-scan-optimization`).
- [x] 3.2 Verify compilation and unit tests in `aggregator-domain` and `aggregator-surebet` (`mvn.cmd test -pl aggregator-domain,aggregator-surebet`).
