## ADDED Requirements

### Requirement: Full Prematch Category Tree Ingestion
Bookmaker crawlers for bookmakers offering structured navigation trees (e.g. Zenit, Betboom, Olimpbet, Marathonbet) SHALL traverse and ingest all active sport category categories and prematch events in addition to live feeds.

#### Scenario: Zenit prematch category ingestion
- **WHEN** the Zenit crawler executes its scheduled prematch discovery
- **THEN** it iterates through all non-empty sport disciplines and requests event lists via the prematch printer endpoint, populating `match_cache` with prematch events.

#### Scenario: Betboom full sport tree extraction
- **WHEN** the Betboom crawler polls the sports tree endpoint (`/api/site_api/v1/sporthub/tree/get_tree`)
- **THEN** it parses all active tournament and match nodes across both live and prematch trees and queues match cards for loader enrichment.

### Requirement: Resilient Loader Proxy Rotation
Bookmaker loaders utilizing outbound HTTP proxies SHALL handle rate-limiting (HTTP 403 / 429) and proxy connection drops gracefully with exponential backoff and proxy key rotation without crashing the container.

#### Scenario: Rate limit encounter during line loading
- **WHEN** an outbound request receives an HTTP 403 or 429 status code
- **THEN** the loader rotates to the next healthy proxy in the pool and backs off execution for that specific bookmaker endpoint before retrying.
