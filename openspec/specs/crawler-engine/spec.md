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
