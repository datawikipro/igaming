# Crawler Engine Specification

## Purpose
Provides an extensible framework for discovering sports leagues, scraping live and prematch betting odds across bookmakers, and streaming normalized odds into the aggregation pipeline.

## Requirements

### Requirement: Dual Execution Roles
Each bookmaker source module must support distinct runtime profiles: `league-crawler` (discovering events, scraping odds, pushing to Kafka) and `match-loader` (reading `match_cache` from PostgreSQL with optimistic/skip-locked concurrency, enriching detailed event data).

#### Scenario: Crawler role execution
- **WHEN** the application starts with profile `league-crawler` (`app.role=league-crawler`)
- **THEN** the bookmaker crawler actively discovers leagues and scrapes betting odds, pushing updates to Kafka topic `odds.updates`.

#### Scenario: Loader role execution
- **WHEN** the application starts with profile `match-loader` (`app.role=match-loader`)
- **THEN** the `GenericMatchLoadScheduler` invokes `AbstractBaseBookmakerService.loadMatchCards()` using `SELECT FOR UPDATE SKIP LOCKED` on `match_cache` for concurrent processing across replicas.

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
All source bookmaker modules MUST configure HikariCP with non-blocking timeouts and define Kubernetes `startupProbe` to prevent deployment deadlocks during database cold starts and schema migrations.

**Critical constraint**: `spring.datasource.hikari.initialization-fail-timeout` MUST NOT be set to `0` in `application.properties` when it is also overridden via Kubernetes env var (`SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT`). HikariCP seals its configuration after pool startup — a duplicate property application throws `IllegalStateException: The configuration of the pool is sealed once started`, causing `CrashLoopBackOff`.

**Correct pattern**:
- In `application.properties`: omit `initialization-fail-timeout` entirely, or set `spring.datasource.hikari.initialization-fail-timeout=-1`
- In Kubernetes env var (if needed to override): `SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT: "-1"` (value `-1` = wait indefinitely, never fail on init)
- `connection-timeout` stays at `5000` ms
- `validation-timeout` stays at `3000` ms

#### Scenario: Source pod cold start
- **WHEN** a bookmaker source pod starts before its backing PostgreSQL database is fully ready
- **THEN** HikariCP defers pool establishment without crashing the Spring context, and the Kubernetes `startupProbe` permits up to 300 seconds for initial database connectivity and schema migrations.

#### Scenario: HikariCP sealed pool crash (anti-pattern)
- **WHEN** `initialization-fail-timeout=0` is present in both `application.properties` and as a Kubernetes env var override
- **THEN** the application MUST NOT be deployed in this state — it will crash on every startup with `IllegalStateException`. The env var value must be `-1` to override safely.

---

### Requirement: Resilient HTTP Socket and Connection Timeouts
All bookmaker source modules MUST configure Apache HttpClient 5 request configurations with explicit connect, response (socket read), and connection lease timeouts to prevent crawler worker thread exhaustion during external network stalls.

#### Scenario: External API socket read timeout
- **WHEN** an external bookmaker API endpoint, proxy, or upstream challenge hangs without transmitting data
- **THEN** Apache HttpClient 5 SHALL terminate the connection after a maximum 20-second response timeout, preventing thread starvation.

#### Scenario: Internal service non-proxy timeout
- **WHEN** internal Kubernetes service communication occurs via `internalRestTemplate`
- **THEN** strict 5-second connect and 10-second response timeouts SHALL be enforced without proxy intervention.

#### Scenario: Connection pool lease timeout
- **WHEN** threads request a pooled connection from `PoolingHttpClientConnectionManager`
- **THEN** a connection lease timeout of 5 seconds SHALL be enforced, preventing indefinite pool contention.

---

### Requirement: SofaScore Dual-Storage Raw Ingestion and Streaming
The SofaScore capture module (`igaming-capture-sofascore`) MUST persist 100% of raw external event payloads (fixture details, lineups, H2H historical encounters, streaks) to a dedicated PostgreSQL raw store (`sofascore_raw_events`) AND stream them to Kafka topic `sports.raw.sofascore` to enable zero-loss auditing and asynchronous downstream enrichment.

#### Scenario: Raw event dual-storage on fixture enrichment
- **WHEN** SofaScore fixtures or match cards are scraped or enriched
- **THEN** raw JSON payloads are written to `sofascore_raw_events` with unique constraint `(sport, event_id)` and simultaneously published to Kafka `sports.raw.sofascore`.

#### Scenario: Non-blocking graceful degradation on Kafka disconnect
- **WHEN** Kafka brokers are temporarily unavailable or disabled (`app.kafka.enabled=false`)
- **THEN** raw events continue persisting to PostgreSQL without interrupting scheduled scrapers or throwing unhandled exceptions.

---

### Requirement: Cross-Language Lineups and Event Enrichment Matcher
The SofaScore and LiveResult capture modules MUST support cross-language phonetic transliteration and multi-stage fallback matching to reliably resolve Russian/Cyrillic bookmaker match names against Latin provider APIs.

#### Scenario: Cross-language team name resolution
- **WHEN** a match with Cyrillic team names (e.g. "Динамо Махачкала" vs "Акрон") requires lineup enrichment from an English-language provider (e.g. SofaScore "Dynamo Makhachkala" vs "Akron")
- **THEN** `SimilarityUtils.areTeamsSimilar()` performs a two-stage evaluation: exact/Jaro-Winkler comparison directly, followed by phonetic Cyrillic-to-Latin transliteration with Jaro-Winkler threshold >= 0.85 and substring containment.

#### Scenario: Search query fallback with transliteration
- **WHEN** direct schedule lookup fails or provider API search with raw Cyrillic characters returns empty results
- **THEN** `searchByTeamName()` automatically executes fallback searches with phonetically transliterated queries across both home and away teams.

#### Scenario: Lineups enrichment window for live matches
- **WHEN** matches have already kicked off or are within the 3-hour post-kickoff window
- **THEN** `findMatchesNeedingEnrichment()` includes them in the polling window (`now - 3 hours` to `now + 48 hours`) and filters out already confirmed lineups (`is_lineup_confirmed = true`), ensuring live matches retain official starting XI and substitutions.

---

### Requirement: Unified In-Memory Architecture for Bookmaker Pods
Bookmaker sources MAY operate in a unified single-pod runtime (`app.role=unified`) combining Discovery (Crawler), Enrichment (Loader), and Reactive Sentry (Refresher) inside one JVM backed by an In-Memory H2 database (`jdbc:h2:mem:...;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`).

#### Scenario: Unified pod execution
- **WHEN** the application starts with `app.role=unified`
- **THEN** `MatchFetchScheduler` (Crawler), `GenericMatchLoadScheduler` (Loader), and `GenericOddsRefreshScheduler` (Refresher) all execute concurrently within the single container.

#### Scenario: Sub-millisecond in-memory cache operations
- **WHEN** the Loader selects pending match cards or the Refresher queries match metadata by external ID
- **THEN** operations are executed against in-memory H2 without TCP socket latency, disk I/O, or cross-pod database connection contention.

#### Scenario: MDC role-based logging isolation
- **WHEN** multiple concurrent roles log messages to standard output
- **THEN** SLF4J MDC tags every log event with `[CRAWLER]`, `[LOADER]`, or `[REFRESHER]`, enabling granular filtering (`kubectl logs ... | grep "[LOADER]"`) without separate containers.




