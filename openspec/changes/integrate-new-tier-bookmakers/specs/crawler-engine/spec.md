## ADDED Requirements

### Requirement: B2B Platform Multi-Tenant Engines
The crawler engine SHALL support unified B2B platform adapters (Kambi, Altenar, Digitain) allowing multiple bookmaker brand storefronts to share common DTO structures, connection pools, and bet type mappers while maintaining isolated tenant configurations and databases.

#### Scenario: Altenar platform fan-out
- **WHEN** the Altenar B2B engine initiates connection to `sb-feed.altenar.com`
- **THEN** it streams match odds for configured LatAm and EU brands (Bet7k, BetNacional, EsportesDaSorte, Vaidebet, Novibet) without requiring independent browser instances or residential proxies.

#### Scenario: Kambi channel multi-tenancy
- **WHEN** Kambi engine executes scheduled event discovery
- **THEN** it queries distinct CDN endpoints (`/offering/v2018/{channel}/`) for TonyBet, Stoiximan, Expekt, Paf, and ATG using appropriate regional market and language headers.

### Requirement: Crypto and Web3 Decentralized Feed Extraction
The system SHALL ingest betting lines from cryptocurrency sportsbooks and Web3 prediction/exchange markets using direct GraphQL, REST, and smart contract orderbook feeds without bot detection hurdles.

#### Scenario: Stake and BC.Game high-throughput ingestion
- **WHEN** `igaming-source-stake` or `igaming-source-bcgame` poll match fixtures
- **THEN** odds updates are received via non-blocking HTTP WebClients through the standard datacenter cluster gateway.

#### Scenario: Web3 prediction market orderbook
- **WHEN** `igaming-source-polymarket` or `igaming-source-sxbet` poll liquidity pools
- **THEN** binary and categorical market probabilities are decoded and normalized for cross-bookmaker arbitrage.

### Requirement: Residential Proxy Gated Execution (Blocker Protocol)
The system SHALL strictly quarantine bookmaker modules requiring residential proxy egress into a blocked state until explicit developer authorization and proxy credentials are provided.

#### Scenario: Execution block on protected US and UK books
- **WHEN** a task targeting WilliamHill, FanDuel, DraftKings, Ladbrokes, PaddyPower, or Caesars is queued
- **THEN** the workflow halts with a blocked dependency status requiring explicit developer provision of residential proxy gateway endpoints (`HTTP_RESIDENTIAL_PROXY_URL`).
