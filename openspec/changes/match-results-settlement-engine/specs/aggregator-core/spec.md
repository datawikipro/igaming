# Delta Specification: Independent Match Results & Settlement Engine

## ADDED REQUIREMENTS

### Requirement: Independent Multi-Source Match Result Ingestion
The system SHALL ingest final match scores and winner resolutions from independent third-party sport APIs without relying on bookmaker result feeds.

#### Scenario: Esports Dota 2 Result Capture via OpenDota
- **GIVEN** a completed Dota 2 match in `match_record` with `final_score1 IS NULL`
- **WHEN** the `OpenDotaResultProvider` polls the OpenDota match coordinator API
- **THEN** it resolves the series final score (e.g. `2:1`) and winner (`WIN1` or `WIN2`)
- **AND** updates `match_record.final_score1`, `match_record.final_score2`, `match_record.match_result` and `match_record.result_captured_at`.

---

### Requirement: Multi-Sport Automatic Settlement Resolution
The system SHALL support automatic score fetching across Football, Tennis, Basketball, Ice Hockey, and Esports using SofaScore and ESPN APIs.

#### Scenario: Football Match Final Whistle Capture
- **GIVEN** a football match that concluded in the last 15 minutes
- **WHEN** the `SofaScoreResultProvider` detects `status.type == 'finished'`
- **THEN** it extracts `homeScore` and `awayScore`
- **AND** calls `POST /api/matches/{id}/result` on `aggregator-api`
- **AND** updates associated surebets in `surebet_alert` to `EXPIRED` and `outcome_result = WON`.
