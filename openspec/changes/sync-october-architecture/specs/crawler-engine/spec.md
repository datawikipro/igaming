## ADDED Requirements

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
