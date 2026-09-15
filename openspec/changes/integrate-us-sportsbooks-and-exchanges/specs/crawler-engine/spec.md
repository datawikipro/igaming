## ADDED Requirements

### Requirement: US Sportsbook and Exchange Routing Egress
Crawlers targeting United States regulated sportsbooks, betting exchanges, and US-facing offshore bookmakers SHALL route all HTTP and WebSocket connections through designated US network gateways (`outline-us` or dedicated residential egress) to avoid territorial geoblocking and domestic ISP filtering.

#### Scenario: Routing US bookmaker requests through cluster proxy
- **WHEN** a US bookmaker crawler or loader executes HTTP requests
- **THEN** requests pass through `http://100.83.113.50:3128` and egress from a valid United States IPv4 address without IP disclosure.

---

### Requirement: P2P Betting Exchange Orderbook Ingestion
Crawlers for peer-to-peer betting exchanges (such as Sporttrade, Novig, ProphetX) SHALL ingest bid/ask market depth (Orderbook Back and Lay prices) and convert them to continuous decimal odds streams.

#### Scenario: Ingesting two-sided exchange markets
- **WHEN** an exchange market update with best bid and best ask is received
- **THEN** the crawler normalizes the best available Back (buy) and Lay (sell) prices and pushes them into `odds.updates` with market liquidity volumes.

---

### Requirement: Player Props and Micro-Market Extraction
Crawlers supporting American sports (NFL, NBA, MLB, NHL) SHALL support extraction of player proposition markets (points, rebounds, assists, rushing yards, touchdowns) and micro-betting in-play intervals.

#### Scenario: Extracting player prop markets
- **WHEN** a bookmaker provides individual player proposition lines
- **THEN** the crawler extracts player identifiers, stat categories (e.g. `POINTS`, `RUSHING_YARDS`), and threshold parameters into normalized `BetType` objects.
