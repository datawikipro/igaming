# Crawler Engine Specification Delta (plane-1a76ef56)

## Purpose
Specifies 3-way market suppression, handicap polarity sign preservation, and statistical market isolation for the Winline bookmaker engine within the crawler and loader ingestion subsystem.

## ADDED Requirements

### Requirement: Winline 3-Way European Market Suppression
The Winline handicap and total mapping subsystem (`WinlineFootballHandicapMapper`, `WinlineCommonHandicapMapper`, `WinlineFootballTotalMapper`, `WinlineCommonTotalMapper`) must strictly reject 3-way European handicap and 3-way total markets to eliminate phantom arbitrage (super-arbs) caused by unaligned settlement conditions.

#### Scenario: Winline 3-way European handicap suppression
- **WHEN** an odds payload from Winline contains European 3-way handicap markets or draw outcomes within handicap markets
- **THEN** the mapper suppresses the outcomes by returning `null`, preventing publication of phantom surebets.

#### Scenario: Winline 3-way total suppression
- **WHEN** an odds payload from Winline contains 3-way total markets or exact total count outcomes ("Ровно", "Точно")
- **THEN** the mapper suppresses the outcomes, avoiding confusion with standard 2-way over/under totals.

---

### Requirement: Winline Handicap Sign Inversion and Parameter Precedence
The Winline handicap mapping subsystem must ensure correct handicap polarity between opposing sides and prioritize explicit outcome label parameters.

#### Scenario: Opposite sign assignment for opposing team
- **WHEN** a handicap parameter is extracted from a general market title (e.g. "Фора (-1.5)")
- **THEN** Team 1 receives `-1.5` and Team 2 receives the inverted handicap `+1.5`.

#### Scenario: Outcome label parameter precedence
- **WHEN** an outcome label explicitly includes a signed handicap (e.g. "Ф1(-1.5)", "Ф2(+1.5)")
- **THEN** this explicit value overrides any feed-level or fallback parameters.
