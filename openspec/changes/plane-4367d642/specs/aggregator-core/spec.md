# Spec Delta: aggregator-core

## ADDED Requirements

### Requirement: National Team Identification and Classification
The aggregator system SHALL classify sports teams and participants into distinct categories (`CLUB`, `NATIONAL_TEAM`, `INDIVIDUAL`, `ESPORTS`) and provide query capabilities with ISO-3166 country association and flag assets.

#### Scenario: Querying national teams
- **WHEN** a client requests teams filtered by `is_national_team=true` or calls `/teams/national`
- **THEN** the aggregator returns teams marked as national teams with their associated ISO country code and flag asset URL.

#### Scenario: Querying teams by participant type and country
- **WHEN** a client queries teams specifying `team_type=CLUB` and a valid `country_code`
- **THEN** the system filters and returns matching team entities.
