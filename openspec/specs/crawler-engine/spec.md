# Crawler Engine Specification

## Purpose
Provides an extensible framework for discovering sports leagues, scraping live and prematch betting odds across bookmakers, and streaming normalized odds into the aggregation pipeline.

## Requirements

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

### Requirement: Stealth Browser Camoufox Engine and Interactive noVNC Fallback
Crawlers operating against heavily protected sites or social media platforms must employ Firefox / Camoufox Gecko engine with persistent profile storage in Redis and fallback to an interactive noVNC console in `igaming-admin-frontend`.

#### Scenario: Anti-bot bypass with warm cache
- **WHEN** a stealth browser pod initializes
- **THEN** it restores cookie and LocalStorage state from Redis, performs human-like warm-up navigation, and routes egress through designated residential or regional proxy hops.

#### Scenario: Automated and manual captcha resolution
- **WHEN** Cloudflare Turnstile, reCAPTCHA, or puzzle slider challenges are encountered
- **THEN** the crawler attempts automated bypass via CapSolver API or OpenCV template matching, and alerts the operator via the `/browsers` noVNC console when manual intervention is required.

---

### Requirement: SPA Redux State Dump Extraction
For single-page application (SPA) sportsbooks utilizing React and Redux state stores (such as BetBoom), crawlers MUST extract odds and event payloads by evaluating JavaScript directly inside the page context (`window.__INITIAL_STATE__` or application store dispatchers) rather than traversing fragile DOM elements.

#### Scenario: BetBoom Redux store extraction
- **WHEN** the BetBoom browser client navigates to live or prematch betting schedules
- **THEN** it executes the store extraction script in page context, retrieves the structured events payload, and deserializes odds into canonical market DTOs without DOM scraping.

---

### Requirement: BetB2B Clone Network Parameterization and Ingestion
Crawlers targeting the 1x / BetB2B family of sportsbooks (such as Linebet, Betwinner, 22bet, 1xbit) MUST utilize a unified base engine parameterized with sportsbook-specific domain configurations, API routes, and isolated Kubernetes deployments.

#### Scenario: Ingestion from BetB2B clone endpoints
- **WHEN** a clone instance (e.g. `igaming-source-linebet` or `igaming-source-betwinner`) runs
- **THEN** it connects to its assigned external host using standardized BetB2B payload deserializers, applies clone-specific partner IDs, and streams normalized odds to Kafka.

---

### Requirement: Non-Blocking Database Pool Initialization and Startup Probes
All source bookmaker modules MUST configure HikariCP with non-blocking timeouts (`initialization-fail-timeout=0`, `connection-timeout=5000`) and define Kubernetes `startupProbe` to prevent deployment deadlocks during database cold starts and schema migrations.

#### Scenario: Source pod cold start
- **WHEN** a bookmaker source pod starts before its backing PostgreSQL database is fully ready
- **THEN** HikariCP defers pool establishment without crashing the Spring context, and the Kubernetes `startupProbe` permits up to 300 seconds for initial database connectivity and schema migrations.

