# Aggregator Core Specification

## ADDED Requirements

### Requirement: Virtual Threads Async Dispatching
The arbitrage engine MUST execute asynchronous event evaluation tasks (`@Async("surebetExecutor")`) on Java 21 virtual threads to guarantee non-blocking task execution and prevent thread pool starvation.

#### Scenario: Asynchronous surebet detection execution
- **WHEN** `SurebetDetectorService.detectSurebetsForMatch` is invoked asynchronously
- **THEN** it SHALL be executed on a dedicated Java 21 virtual thread managed by `surebetExecutor`.
