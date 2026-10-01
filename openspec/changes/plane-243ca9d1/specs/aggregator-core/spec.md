# Spec Delta: aggregator-core

## ADDED Requirements

### Requirement: Automated National Team Detection and Classification
The aggregator system SHALL automatically identify national sports teams from raw bookmaker names, extract age restrictions and gender attributes, and link them to standardized ISO-3166 country codes and vector flag assets.

#### Scenario: Detecting standard national team
- **WHEN** a team name such as "Сборная Испании" or "Spain" is analyzed
- **THEN** the detector identifies the team as a national team with country ISO code "ESP", flag URL "/assets/flags/es.svg", and canonical display name "Испания".

#### Scenario: Detecting age-restricted and gender-specific squads
- **WHEN** a team name such as "Германия (до 21 года)" or "Germany U21" is analyzed
- **THEN** the detector identifies the age category "U21", links to country ISO code "DEU", and produces the canonical display name "Германия U21".

#### Scenario: Deduplicating and backfilling historical national teams
- **WHEN** the team backfill process is executed
- **THEN** duplicate variants of the same national team are merged into a canonical team entity, old names are saved as team aliases, and national team attributes are persisted.
