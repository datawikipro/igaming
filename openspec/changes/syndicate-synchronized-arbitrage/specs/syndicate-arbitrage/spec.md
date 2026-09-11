## Purpose

Enables multi-user syndicate arbitrage betting (1 Master + N Operators) with currency-specific stake rounding, conservative-to-actual dual-ledger accounting, cross-border multi-currency FX conversion (>6.0% yield gate), automated 20% drift rebalancing, smart skew probabilistic favorite routing, and two-phase sub-second dual confirmation via browser extensions.

## ADDED Requirements

### Requirement: N-Participant Syndicate and Role Hierarchy
The system SHALL allow users to create and manage syndicate rooms of arbitrary size with role differentiation (`MASTER` as treasury leader and `OPERATOR` as betting runners), tracking live device presence and per-bookmaker account credentials.

#### Scenario: Syndicate room creation and operator onboarding
- **WHEN** a Master user creates a syndicate room with base currency settings and invites operators
- **THEN** invited operators join the room, register their available bookmakers and currencies, and enter `ACTIVE_SYNC` status.

---

### Requirement: Stake Rounding and Granularity Control
The calculation engine SHALL round all computed stake amounts to configurable currency-specific granularity steps (e.g. multiples of 100 RUB, 5 USD, 10 BYN) to emulate human betting behavior and avoid bookmaker fraud detection.

#### Scenario: Rounding stakes to currency step
- **WHEN** an optimal theoretical stake is calculated as 4,372.40 RUB for Leg 1 and 1,821.10 RUB for Leg 2
- **THEN** the system rounds the stakes to 4,400 RUB and 1,800 RUB respectively according to the 100 RUB step configuration.

---

### Requirement: Conservative Initial vs. Actual Settled Profit Accounting
The ledger SHALL record expected mathematical profit using the worst-case lower-bound outcome ($\min(P_{outcome1}, P_{outcome2})$) immediately after placement, and adjust to exact actual realized profit upon verified event settlement.

#### Scenario: Pre-settlement conservative profit booking
- **WHEN** stake rounding causes Outcome 1 to yield +1,800 RUB and Outcome 2 to yield +1,200 RUB
- **THEN** the ledger initially attributes +600 RUB ($\frac{1}{2} \times 1,200\text{ RUB}$) mathematical profit share to each participating operator.

#### Scenario: Post-match actual profit settlement
- **WHEN** the match concludes with Outcome 1 winning (producing +1,800 RUB net profit)
- **THEN** the ledger recalculates each operator's mathematical share to +900 RUB ($\frac{1}{2} \times 1,800\text{ RUB}$) and updates their fair balance.

---

### Requirement: Cross-Border Multi-Currency Arbitrage (>6.0% Yield Gate)
The system SHALL support cross-border syndicate pairs across different national currencies (e.g., RUB, BYN, USD, EUR) using an active FX cross-rates matrix, enforcing a strict minimum yield threshold of $> 6.0\%$ for all cross-currency pairs to compensate for currency conversion spread and volatility risk.

#### Scenario: Cross-currency pair with yield above 6.0%
- **WHEN** an arbitrage opportunity between a Russian operator (RUB) and Belarusian/US operator (BYN/USD) yields 7.2% based on current FX cross-rates
- **THEN** the opportunity is approved and broadcast to the syndicate operators.

#### Scenario: Cross-currency pair with yield below 6.0%
- **WHEN** a cross-currency arbitrage opportunity yields 4.5% ($< 6.0\%$)
- **THEN** the system filters out the opportunity for cross-border execution to prevent FX spread erosion.

---

### Requirement: 20% Drift Detection and Settlement Workflow
The system SHALL calculate the drift $\delta = \frac{B_{real} - B_{math}}{B_{math}}$ for every participant and trigger P2P settlement action tickets whenever $|\delta| \ge 20\%$.

#### Scenario: Surplus rebalance alert (> +20%)
- **WHEN** an operator's real balance exceeds their mathematical share by $\ge 20\%$ due to consecutive winning legs
- **THEN** a `SURPLUS_SETTLEMENT_TICKET` is dispatched to the Master and operator to transfer the excess funds to the Master.

#### Scenario: Deficit replenishment alert (< -20%)
- **WHEN** an operator's real balance drops below $80\%$ of their mathematical balance due to losing legs
- **THEN** a `DEFICIT_REPLENISHMENT_TICKET` is dispatched to the Master to top up the operator's operational bankroll.

---

### Requirement: Smart Skew Routing for Deficit Recovery
The syndicate matching engine SHALL route high-probability favorite legs (identified via market consensus or external scoring service) to operator accounts currently in balance deficit on the respective bookmaker to naturally restore their balance toward the green zone without physical transfers.

#### Scenario: Routing heavy favorite to deficit account
- **WHEN** Operator 1 has a $-15\%$ deficit on Winline and a surebet leg on Winline has odds 1.20 ($P \approx 83\%$)
- **THEN** the dispatcher routes the Winline favorite leg to Operator 1 and the counter-leg (underdog) to an operator with positive balance surplus on the opposing bookmaker.

---

### Requirement: Two-Phase Dual Confirmation Handshake
The system SHALL require simultaneous confirmation from both participating operators within a 4-second window before authorizing automated bet placement.

#### Scenario: Dual confirmation within timeout
- **WHEN** both operators tap "Confirm" before the 4-second timer expires
- **THEN** the server commits the order and broadcasts `EXECUTE_SYNC` to their browser extensions.

#### Scenario: Timeout or single rejection
- **WHEN** either operator rejects or fails to respond within 4 seconds
- **THEN** the proposal is aborted immediately and no bets are submitted.

---

### Requirement: Syndicate Tier Limits and Early-Bird Feed Priority
The platform SHALL segment Syndicate Rooms into `STANDARD`, `PRO`, and `ULTIMATE` tiers, enforcing participant/bookmaker limits and providing exclusive Early-Bird priority feeds for higher tiers.

#### Scenario: Standard room limits
- **WHEN** a user creates a `STANDARD` room (1,990 RUB/mo)
- **THEN** the room supports up to 3 participants, up to 6 bookmakers per player, Deep Smart-Skew auto-balancing, and standard single-currency operations.

#### Scenario: PRO room early-bird priority and multi-currency
- **WHEN** a high-yield surebet (>5.0%) is detected
- **THEN** the opportunity is broadcast exclusively to `PRO` (up to 20 devices, unlim bookmakers, multi-currency) and `ULTIMATE` rooms for 3-5 seconds before propagating to the standard feed.

---

### Requirement: Drop Burn and All-In Capital Sweep Mode
The PRO matching engine SHALL support `DROP_BURN` mode on designated disposable accounts, enabling 100% full-balance all-in asymmetric stake routing to rapidly liquidate expiring drop balances into secure primary accounts.

#### Scenario: All-In asymmetric burn execution
- **WHEN** an operator marks an account with 5,000 RUB as `DROP_BURN` and pairs it with a primary account holding 20,000 RUB
- **THEN** the dispatcher routes 100% of the drop balance (5,000 RUB) to an underdog leg (odds ~5.60) and 20,000 RUB on the primary account to the favorite leg (odds ~1.22), guaranteeing total profit (+3,400 RUB on primary or +3,000 RUB on drop) and draining the disposable account on favorite win.

---

### Requirement: Open Mesh Pool with Escrow Collateral
The platform SHALL operate an open-access communal liquidity pool where participants allocate partial bankroll with a 20% Escrow security deposit and zero monthly subscription fee, taking a 1.0% fee on Net Mathematical Profit at clearing.

#### Scenario: Zero-risk open pool participation
- **WHEN** a user joins the Open Mesh Pool with 10,000 RUB allocated bankroll and deposits 2,000 RUB (20%) in Escrow
- **THEN** their accounts participate in communal mesh arbitrage with drift strictly capped at $\Delta_{max} = 2,000\text{ RUB}$, paying only 1% fee on realized net math profit.

---

### Requirement: Smart Freebet Conversion Optimizer (SNR Matched Betting)
The system SHALL provide an automated Freebet Optimization engine that queues user-registered Stake-Not-Returned (SNR) promotional bonuses, dynamically scanning live and prematch markets to identify optimal counter-arb pairs in the $K = 4.00$–$5.50$ range to extract maximum guaranteed cash conversion (75%–84% net cash return).

#### Scenario: Queuing and optimal pairing of freebet
- **WHEN** a participant registers a 5,000 RUB freebet on a supported bookmaker
- **THEN** the optimizer queues the bonus in `SEARCHING_OPTIMAL_PAIR` status, continuously evaluating market pairs until finding a counter-stake yielding $\ge 78.0\%$ guaranteed cash conversion, notifying the user to authorize placement.



