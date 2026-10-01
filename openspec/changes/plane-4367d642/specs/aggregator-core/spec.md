## ADDED Requirements

### Requirement: National Teams Data Model and API Support
The aggregator core and domain model MUST classify sports entities as clubs or national teams, supporting ISO country codes, flag URLs, and dedicated query endpoints.

#### Scenario: Querying national teams
- **WHEN** a client sends a GET request to `/api/teams/national` with optional parameters `country_code`, `sport`, `search`, `page`, `size`
- **THEN** only teams with `team_type = NATIONAL_TEAM` and `is_national_team = true` are returned along with country code and flag URL.

#### Scenario: National teams statistical aggregation
- **WHEN** a client sends a GET request to `/api/teams/national/stats`
- **THEN** the system aggregates and returns `totalCount` of national teams, count `bySport`, and `distinctCountriesCount`.

#### Scenario: Generic team filtering by national team properties
- **WHEN** a client requests `/api/teams` with `team_type`, `is_national_team`, or `country_code` filters
- **THEN** the result set is filtered accordingly, supporting both snake_case and camelCase query parameters.
