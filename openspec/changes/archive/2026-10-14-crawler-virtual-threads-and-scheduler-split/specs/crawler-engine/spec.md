## MODIFIED Requirements

### Requirement: Dual Execution Roles
Each bookmaker source module MUST support distinct runtime profiles: `league-crawler` (discovering events, scraping odds, pushing to Kafka) and `match-loader` (reading `match_cache` from PostgreSQL with optimistic/skip-locked concurrency, enriching detailed event data using Java 21 Virtual Threads and decoupled periodic maintenance).

#### Scenario: Crawler role execution
- **WHEN** the application starts with profile `league-crawler` (`app.role=league-crawler`)
- **THEN** the bookmaker crawler actively discovers leagues and scrapes betting odds, pushing updates to Kafka topic `odds.updates`.

#### Scenario: Loader role execution
- **WHEN** the application starts with profile `match-loader` (`app.role=match-loader`)
- **THEN** `GenericMatchLoadScheduler` invokes `AbstractBaseBookmakerService.loadMatchCards()` using `SELECT FOR UPDATE SKIP LOCKED` on `match_cache` and dispatches parallel match card enrichment across Java 21 Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`).

#### Scenario: Decoupled stuck match reclamation
- **WHEN** matches remain in pending state past their lease timeout
- **THEN** a dedicated background scheduled task periodically resets stuck pending records to new without executing redundant update statements inside the hot match loading polling loop.
