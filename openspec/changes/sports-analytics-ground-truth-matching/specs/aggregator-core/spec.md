## ADDED Requirements

### Requirement: Ground Truth Sports Analytics Fixture Synchronization
The aggregator engine MUST periodically synchronize upcoming event schedules (fixtures for 24 to 72 hours ahead) and canonical team identities from external sports analytics providers (SofaScore, FlashScore, ESPN, LiveResult, OpenDota).

#### Scenario: Preloading upcoming sports fixtures
- **WHEN** the scheduled fixture synchronization worker runs
- **THEN** reference fixtures with canonical team IDs, localized names, tournament information, and official start timestamps are stored in PostgreSQL and indexed in the in-memory cache.

#### Scenario: Provider failure resilience
- **WHEN** an external sports analytics provider endpoint times out or returns HTTP 429/5xx
- **THEN** the synchronization engine logs the error, preserves existing cached fixtures without deletion, and executes exponential backoff.

### Requirement: Ground Truth Reference Match Resolution
The normalization and unification pipeline MUST match incoming bookmaker odds events against preloaded Ground Truth fixtures using sport-polymorphic signatures and start time windows before falling back to temporary teams or LLM normalization queues.

#### Scenario: Successful reference fixture matching
- **WHEN** an odds update arrives from a bookmaker and its normalized sport signatures match a Ground Truth reference fixture within a 3-hour start time window
- **THEN** the bookmaker event is immediately mapped to the canonical match entity, and permanent TeamAlias records are stored in the database and cache.

#### Scenario: Fallback to LLM for unlisted minor events
- **WHEN** an incoming bookmaker event cannot be matched against any reference fixture or existing aliases
- **THEN** a temporary team/match is instantiated and an asynchronous normalization request is enqueued for LLM resolution.

### Requirement: Auto-Enrichment and Metadata Ingestion
The ingestion engine MUST enrich canonical teams and matches with official logos, localized names, country, and tournament hierarchy from sports analytics providers.

#### Scenario: Populating canonical team metadata
- **WHEN** a reference team is ingested or updated from an analytics provider
- **THEN** the canonical Team record is populated with logo_url, 
ame_english, 
ame_local, and country attributes.
