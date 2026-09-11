# Delta Specification: Syndicate Autonomous Simulation & Settlement Engine

## ADDED REQUIREMENTS

### Requirement: Autonomous 15-Account Syndicate Simulation Structure
The system SHALL maintain a state machine of 15 virtual accounts divided across three distinct syndicate rooms:
1. **Standard Room**: Exactly 3 virtual accounts (`std-master`, `std-op-1`, `std-op-2`), single currency (RUB), up to 6 connected bookmakers per account.
2. **PRO Room**: Exactly 5 virtual accounts (`pro-master`, `pro-op-1`, `pro-op-2`, `pro-drop-1`, `pro-drop-2`), multicurrency support (RUB/BYN), Early-Bird signal delivery (3–5s advance), and active `🔥 All-In Burn` capability on drop accounts.
3. **Ultimate Room**: Exactly 7 virtual accounts (`ult-master`, `ult-op-1`..`ult-op-6`), cross-border high-liquidity arbitrage, and automated P2P clearing queue.

#### Scenario: 15-Account Initial Setup
- **GIVEN** the simulation engine is initialized
- **THEN** 15 accounts are seeded with distinct realistic bookmaker balances (totaling 200,000 ₽ for Standard, 420,000 ₽ for PRO, and 1,450,000 ₽ for Ultimate)
- **AND** all member mathematical balances $M_{balance}$ equal their initial deposits.

---

### Requirement: Hourly Periodic Betting Cycle with Multi-Factor Match Selection
The simulation engine SHALL execute a synchronous syndicate bet cycle every 1 hour (60 minutes) using real sporting events and live odds from `aggregator-core`.
The selection algorithm SHALL rank candidates using a multi-factor score:
$$\text{Score} = w_{yield} \cdot Y(A) - w_{time} \cdot \ln(1 + T_{start}) + w_{bal} \cdot B_{fit}$$
favoring LIVE fixtures and events starting within the next 15–60 minutes that match current account balances.

#### Scenario: Hourly Bet Selection
- **GIVEN** the hourly cron interval triggers
- **WHEN** multiple qualified arbitrage opportunities exist
- **THEN** the system prioritizes LIVE fixtures and matches with the shortest time to start that fit available room balances
- **AND** calculates stakes using Deep Smart-Skew ($75\%–80\%$ on favorite leg, $20\%–25\%$ on long leg)
- **AND** places virtual bets across the respective syndicate room member accounts.

---

### Requirement: Visual Proof Generation with Odds Highlight
For every virtual bet leg placed, the system SHALL generate and persist a visual proof card featuring:
1. Bookmaker brand logo and direct sanitized event URL.
2. Match header (Sport, League, Event name, Live/Prematch status, Score).
3. Visual highlight bounding box on the selected outcome and target odds.
4. Calculation breakdown (Stake, Odds, Potential Return).

#### Scenario: Visual Proof Verification
- **GIVEN** a placed bet on Winline @ 1.25
- **WHEN** viewing the bet details in the Syndicate UI
- **THEN** the outcome `П1` and odds `1.25` are highlighted with a high-contrast bounding box.

---

### Requirement: Match Settlement & Double-Entry Math Accounting
Upon match conclusion, the settlement service SHALL:
1. Poll and verify official final match results.
2. Determine winning vs losing legs.
3. Apply double-entry bookkeeping:
   - Update Real Bookmaker balances ($B_{real} = B_{real} + NetWin$ on winner, $B_{real} = B_{real} - Stake$ on loser).
   - Update Math Ledger balances ($M_{balance} = M_{balance} + P_{net}/N$ for all $N$ group members).
4. Calculate balance drift $\Delta = (B_{real} - M_{balance}) / M_{balance}$.
5. Automatically issue a `SyndicateRebalanceTicket` when $|\Delta| \ge 20\%$.

#### Scenario: Settlement after Favorite Victory
- **GIVEN** a 5,400 ₽ bet on Winline @ 1.25 and a 1,200 ₽ bet on Betcity @ 5.40 (Total Stake: 6,600 ₽)
- **WHEN** the match ends with Winline winning (Gross Payout: 6,750 ₽)
- **THEN** Winline balance increases by $+1,350\text{ ₽}$
- **AND** Betcity balance decreases by $-1,200\text{ ₽}$
- **AND** Group Net Profit of $+150\text{ ₽}$ is credited equally ($+50\text{ ₽}$ each to 3 Standard members).
