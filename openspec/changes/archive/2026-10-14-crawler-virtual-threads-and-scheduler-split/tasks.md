## 1. Refactor AbstractBaseBookmakerService

- [x] 1.1 Remove `matchCacheRepository.resetStuckPendingMatches()` from `loadMatchCards()` transactional block.
- [x] 1.2 Add dedicated `@Scheduled(fixedDelay = 60_000, initialDelay = 10_000)` maintenance method in `AbstractBaseBookmakerService` to reclaim stuck pending matches.
- [x] 1.3 Replace `Executors.newCachedThreadPool()` with `Executors.newVirtualThreadPerTaskExecutor()` in `loadMatchCards()` using try-with-resources.

## 2. Verification & Testing

- [x] 2.1 Validate OpenSpec change compliance (`openspec validate crawler-virtual-threads-and-scheduler-split`).
- [x] 2.2 Verify compilation of `igaming-source-core` and downstream crawlers (`mvn.cmd -DskipTests compile -pl igaming-source-core,igaming-source-betano,igaming-source-pinnacle`).
- [x] 2.3 Run existing unit tests in `igaming-source-core`.
