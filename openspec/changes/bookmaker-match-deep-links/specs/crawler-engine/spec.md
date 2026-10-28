## ADDED Requirements

### Requirement: Openable Match Event URL Generation
All crawlers (`igaming-source-*`) and data capture modules (`igaming-capture-sofascore`) SHALL construct and populate valid, openable direct match URLs (`eventUrl`) for every discovered event, and persist them in the crawler local `match_cache.event_url` and upstream `OddsUpdateRequest` / `CanonicalMatchEventDto`.

#### Scenario: Crawler extracts match and populates openable URL
- **WHEN** a crawler parses pre-match or live fixture details from a bookmaker feed or ground-truth provider (SofaScore)
- **THEN** it generates an openable HTTPS URL targeting that specific match
- **AND** it saves the URL in `match_cache.event_url`
- **AND** it includes the `eventUrl` in the `OddsUpdateRequest` and `CanonicalMatchEventDto` emitted to Kafka
