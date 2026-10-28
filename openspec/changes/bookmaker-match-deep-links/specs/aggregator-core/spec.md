## ADDED Requirements

### Requirement: Canonical Match Bookmaker URLs Persistence
The aggregator core and MDM service SHALL persist all known bookmaker and source match deep links in `mdm_match.bookmaker_urls` and `match_record.bookmaker_urls` as a JSON map of `{bookmakerCode: eventUrl}`.

#### Scenario: Aggregator ingests match event with eventUrl
- **WHEN** the aggregator or MDM processes a match update from a bookmaker crawler or SofaScore
- **THEN** it records the bookmaker code and `eventUrl` into the match's `bookmaker_urls` map
- **AND** it preserves previously recorded URLs for other bookmakers for that canonical match
- **AND** it includes `bookmakerUrls` in `CanonicalMatchHeaderDto` and Redis match cache
