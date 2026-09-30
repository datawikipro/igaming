# Spec Delta: portal-gateway

## ADDED Requirements

### Requirement: Public National Team and Participant Directory
The portal gateway SHALL expose REST API endpoints allowing web clients and consumer applications to filter teams by national status, participant type, and country code.

#### Scenario: Filtering teams by national team flag
- **WHEN** a client calls `/api/v1/teams` with `is_national_team=true` or calls `/api/v1/teams/national`
- **THEN** the portal forwards the query to the aggregator service and returns the matching national teams with complete metadata.

#### Scenario: Filtering teams by type and country
- **WHEN** a client calls `/api/v1/teams` with `team_type` and `country_code`
- **THEN** the portal returns the filtered list of teams corresponding to the requested criteria.
