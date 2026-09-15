## ADDED Requirements

### Requirement: Playtech Sportsbook Engine Ingestion
The crawler engine SHALL support ingestion of betting odds from bookmakers powered by Playtech Sportsbook (such as Caliente Mexico, Wplay Colombia, Codere) through the `web_nr` REST and WebSocket protocol.

#### Scenario: Ingesting Playtech sport event trees
- **WHEN** a Playtech-based crawler requests event listings from `web_nr`
- **THEN** events, match factors, handicap spreads, and totals are extracted and normalized without browser DOM overhead.

---

### Requirement: Kaizen Gaming Betano API Ingestion
The crawler engine SHALL support structured extraction of sports events and markets from Kaizen Gaming platforms (Betano Brazil, Portugal, Romania) via their front-facing JSON API.

#### Scenario: Ingesting Betano match odds
- **WHEN** the Betano crawler polls soccer or basketball fixtures
- **THEN** match markets, moneyline, over/under, and Asian lines are normalized into `match_cache` and `match_factor`.
