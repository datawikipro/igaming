## ADDED Requirements

### Requirement: Syndicate Targeted Matching and Cross-Currency Filtering
The aggregator engine SHALL evaluate detected surebets against active syndicate group portfolios, apply currency-specific stake rounding, enforce the $> 6.0\%$ minimum yield gate on cross-currency pairs, and apply Smart Skew favorite leg allocation.

#### Scenario: Cross-currency surebet evaluation with stake rounding
- **WHEN** a candidate surebet between RUB and USD accounts is evaluated
- **THEN** the aggregator validates that margin $> 6.0\%$, applies roundings of 100 RUB and 5 USD to respective legs, and routes the higher probability leg to the operator with the larger relative deficit.
