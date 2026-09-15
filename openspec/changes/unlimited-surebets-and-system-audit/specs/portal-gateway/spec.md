## MODIFIED Requirements

### Requirement: Subscription Tier Enforcement
The portal must deliver comprehensive, unrestricted arbitrage, value bet, and middle bet feeds across all user tiers, eliminating legacy artificial profit caps in alignment with the updated business model.

#### Scenario: Anonymous and Free user full access
- **WHEN** an unauthenticated guest or authenticated Free user requests surebets via `/api/v1/arbs` or `/api/v1/arbs/top`
- **THEN** all active surebets are returned with exact calculated profit percentages without being filtered, truncated, or masked by any profit percentage ceilings.

#### Scenario: Unfiltered ValueBets and Middles
- **WHEN** any user requests value bets via `/api/v1/valuebets` or middle bets via `/api/v1/middles`
- **THEN** the full result set is delivered without applying legacy EV (<= 0.05) or middle profit (<= 0.60) filtering thresholds.
