# Tariff and Yield Limits Specification

## Purpose
Defines user access tiers, arbitrage profit threshold filtering, and upgrade prompts across the SmartBet.guru frontend UI.

## Requirements

### Requirement: Tier-Based Margin Display
The UI must enforce arbitrage display limits: anonymous visitors and authenticated Free users see arbitrage opportunities up to 5.0% profit, while Premium users unlock uncapped returns and live filters.

#### Scenario: Displaying surebets for Free tier
- **WHEN** a Free or unauthenticated user browses `/arbs`
- **THEN** surebets with >5.0% profit are obscured or accompanied by a Premium subscription unlock callout.

#### Scenario: Displaying surebets for Premium tier
- **WHEN** an authenticated Premium user browses `/arbs`
- **THEN** all surebets (including high-profit >5% and Live filters) are displayed in full real-time detail.
