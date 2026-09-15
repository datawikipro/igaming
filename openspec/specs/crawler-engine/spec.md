# Crawler Engine Specification

## Purpose
Provides an extensible framework for discovering sports leagues, scraping live and prematch betting odds across bookmakers, and streaming normalized odds into the aggregation pipeline.

## Requirements

### Requirement: Triple Execution Roles
Each bookmaker source module must support distinct runtime profiles: `league-crawler` (discovering events, scraping odds, pushing to Kafka), `match-loader` (reading `match_cache` from PostgreSQL with optimistic/skip-locked concurrency, enriching detailed event data), and `odds-refresher` (reactive single-event refresher consuming targeted requests from Redis queue with sub-millisecond latency and 0% idle CPU).

#### Scenario: Crawler role execution
- **WHEN** the application starts with profile `league-crawler` (`app.role=league-crawler`)
- **THEN** the bookmaker crawler actively discovers leagues and scrapes betting odds, pushing updates to Kafka topic `odds.updates`.

#### Scenario: Loader role execution
- **WHEN** the application starts with profile `match-loader` (`app.role=match-loader`)
- **THEN** the `GenericMatchLoadScheduler` invokes `AbstractBaseBookmakerService.loadMatchCards()` using `SELECT FOR UPDATE SKIP LOCKED` on `match_cache` for concurrent processing across replicas.

#### Scenario: Refresher role execution
- **WHEN** the application starts with profile `odds-refresher` (`app.role=odds-refresher`)
- **THEN** the `GenericOddsRefreshScheduler` listens to Redis queue `odds:refresh:{bookmaker}` via blocking pop (`BRPOP`) and reactively invokes `AbstractBaseBookmakerService.refreshMatchByExternalId()` for instantaneous single-event quote verification.

---

### Requirement: Stealth Profile Support
Crawlers must select the lowest required browser stealth level among `BASIC`, `HEADLESS_STEALTH`, and `XVFB_HEADED` to prevent bot detection while conserving cluster memory and compute resources.

#### Scenario: Basic API scraping
- **WHEN** a bookmaker provides accessible HTTP/JSON APIs (such as Baltbet, Leon, Pinnacle, Sbobet)
- **THEN** the crawler operates in default `BASIC` mode without spinning up stealth browser heads.

#### Scenario: Hardened anti-bot protection
- **WHEN** a bookmaker enforces advanced fingerprinting or Cloudflare protection (such as Fonbet, BetB2B, 1xbet, LigaStavok, Tennisi)
- **THEN** the crawler configures `APP_BROWSER_STEALTH_PROFILE=XVFB_HEADED` or `HEADLESS_STEALTH` as required for that specific source.

---

### Requirement: Standardized Market and Bet Type Mapping
Bookmaker odds mappers must extend `AbstractBetTypeMapper` to map bookmaker market outcomes to standardized `BetType` definitions across scopes, subjects, and stat types.

#### Scenario: 1X2 market outcome mapping
- **WHEN** a bookmaker outcome string representing Home, Draw, or Away is received
- **THEN** `map1X2Record()` converts the outcome into a normalized `BetType` for the appropriate `BetScope` and `StatType`.

#### Scenario: Totals and Handicap mapping with parameters
- **WHEN** total over/under or handicap spreads are received
- **THEN** `mapTotalRecord()` or `mapHandicapRecord()` attaches the numerical parameter and Asian/European handicap flags.

---

### Requirement: Dynamic League Discovery
Crawlers must discover available leagues dynamically via navigation responses or API inspection and store them in `league_cache` with external IDs prefixed by `{bookmaker}-`.

#### Scenario: Automated league discovery
- **WHEN** the discovery job runs periodically
- **THEN** it navigates to the bookmaker navigation endpoints, extracts available leagues, and upserts them into `league_cache` without static hardcoded league arrays.

---

### Requirement: Odds Integrity Validation (No Silent Drop)
Crawlers and match loaders must validate mathematical and structural consistency of odds via `OddsIntegrityValidator` before streaming updates to Kafka, adhering strictly to the No Silent Drop principle.

#### Scenario: Detecting internal negative margin or inverted lines
- **WHEN** paired outcomes in the same event yield an internal surebet (\( \frac{1}{O_1} + \frac{1}{O_2} < 0.98 \)) or inverted total progressions
- **THEN** an `ERROR` is logged, an `OddsAnomalyDto` is transmitted to `OddsAnomalyService`, and the odds are NOT discarded.

---

### Requirement: Raw Payload Retention on Anomaly
Crawlers must retain raw bookmaker response payloads in memory during processing and attach the complete raw payload to anomaly reports upon error detection for regression testing and rapid debugging.

#### Scenario: Attaching raw payload to anomaly report
- **WHEN** an anomaly or duplicate mapping collision is detected during match parsing
- **THEN** the raw bookmaker payload is included in `raw_payload` of the report and persisted in `odds_anomaly`, while normal error-free events immediately release the raw payload from memory.

---

### Requirement: Duplicate Coefficient Collision Detection
When multiple distinct factor IDs or outcomes resolve to identical `semanticKey` values with different odds, the collision must be registered centrally.

#### Scenario: Reporting mapper collision
- **WHEN** `AbstractOddsProcessor.isDuplicate()` detects a collision on a semantic key with differing odds values
- **THEN** a `CRITICAL DUPLICATE COEFFICIENT ERROR` is logged, and a `DUPLICATE_COEFFICIENT_COLLISION` anomaly report is dispatched with the raw payload.

### Requirement: In-Memory PostgreSQL for Source Operational Caching
Every bookmaker database in `igaming-source` MUST mount storage on `emptyDir: { medium: Memory }` with a 1Gi size limit and physical disk write operations disabled via server arguments.

#### Scenario: In-memory database initialization
- **WHEN** the source PostgreSQL StatefulSet starts in Kubernetes
- **THEN** PostgreSQL mounts storage on `emptyDir` RAM disk (`tmpfs`) with `fsync=off` and `synchronous_commit=off`, producing zero physical disk write I/O.

### Requirement: Java-Driven Schema Auto-Migration
Every crawler and loader application MUST execute `DatabaseMigrationRunner` on startup before Hibernate EntityManager initialization to ensure schema consistency.

#### Scenario: Pre-Hibernate database schema initialization
- **WHEN** a bookmaker source crawler or loader initializes its Spring application context
- **THEN** `DatabaseMigrationRunner` executes high-precedence idempotent DDL ensuring `match_cache`, `match_factor`, `league_cache`, and all columns exist before Hibernate metadata validation.

### Requirement: Headless Browser Process Lifecycle & Zombie Reaping
All pod specifications executing browser automation MUST enable `shareProcessNamespace: true` so orphaned browser processes are reaped by the pod pause container.

#### Scenario: Browser automation zombie process containment
- **WHEN** a browser-based crawler terminates or crashes Chrome subprocesses
- **THEN** PID namespace sharing allows the pod pause container (PID 1) to harvest defunct processes, preventing process table exhaustion.
