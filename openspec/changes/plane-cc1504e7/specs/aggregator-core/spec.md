# Aggregator Core Specification Delta (plane-cc1504e7)

## Purpose
Specifies syndicate clone filtering and temporal discrepancy validation rules in the aggregator arbitrage evaluation pipeline to eliminate false super-arbitrage alerts involving BetB2B family bookmakers (such as FanSport).

## ADDED Requirements

### Requirement: Syndicate Clone Pair Arbitrage Suppression
The arbitrage engine (`SurebetValidator`) must reject candidate surebet pairings formed between bookmakers belonging to the same underlying odds syndicates to prevent phantom or unexecutable cross-clone arbitrage opportunities.

#### Scenario: BetB2B syndicate clone rejection
- **WHEN** an apparent arbitrage opportunity is detected between bookmakers sharing the BetB2B odds engine (`fansport`, `1xbet`, `melbet`, `megapari`, `linebet`, `betandyou`, `888starz`, `spinbetter`)
- **THEN** `CloneSyndicateRule` rejects the surebet, marks it as invalid, and prevents publication or alerting of phantom arbitrage.

---

### Requirement: Temporal Live and Prematch Separation
The surebet evaluation pipeline must prevent cross-matching between Live and Prematch market states when event progression or timing creates artificial discrepancy margins.

#### Scenario: Live vs Prematch arbitrage prevention
- **WHEN** a candidate surebet contains one leg in Live state and another in Prematch state without confirmed identical game-clock synchronization
- **THEN** `LivePrematchSeparationRule` invalidates the candidate pair and prevents anomalous alert propagation.
