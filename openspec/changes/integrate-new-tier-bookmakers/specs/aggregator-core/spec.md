## ADDED Requirements

### Requirement: Betting Exchange Back/Lay Arbitrage Matching
The aggregator core SHALL evaluate arbitrage opportunities involving betting exchange Back and Lay quotes, factoring in exchange commission rates to calculate guaranteed net returns.

#### Scenario: Cross-market Back vs Lay surebet
- **WHEN** a bookmaker offers Back odds \( O_{back} \) and an exchange offers Lay odds \( L_{lay} \) such that net return after commission \( c \) is positive
- **THEN** a `Surebet` entity is created with exchange liability distribution and net yield calculation.

### Requirement: Regional Taxonomies for New Tier Sportsbooks
The aggregator SHALL map team names, competitor aliases, and league structures from new European, CIS, and Greek markets into canonical entities without creating duplicate fixtures.

#### Scenario: Multi-language entity resolution
- **WHEN** an event is ingested from Stoiximan in Greek script or Favbet in Ukrainian
- **THEN** the entity resolver standardizes the match signature against international Romanized ground truth before surebet evaluation.
