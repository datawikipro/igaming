## Context

See [proposal.md](file:///C:/Users/chernousov_a/IdeaProjects/igaming/openspec/changes/syndicate-synchronized-arbitrage/proposal.md) for motivation and functional overview.

The existing SmartBet.guru ecosystem comprises `igaming-aggregator` (Spring Boot + Kafka + in-memory matching), `igaming-portal` (REST/STOMP gateway + PostgreSQL), and `chrome-extension` (autofill + anti-fingerprint). This design integrates multi-user syndicate groups, dual-ledger accounting, probabilistic self-balancing, currency stake rounding, cross-border FX yield thresholds, and coordinated dual execution.

## Goals / Non-Goals

**Goals:**
- Provide real-time syndicate matching with sub-second latency across $N$ operator combinations.
- Implement strict dual-ledger accounting with conservative initial profit booking and post-settlement reconciliation.
- Automatically steer syndicate balances to the $\pm 10\%$ Green Zone using Smart Skew favorite leg allocation.
- Enforce currency-specific stake roundings (e.g. multiples of 100 RUB, 5 USD) and $> 6.0\%$ minimum yield for cross-currency pairs.
- Coordinate 2-phase WebSocket handshakes ($T_{ack} \le 4000\text{ ms}$) triggering synchronized Chrome Extension actions.

**Non-Goals:**
- Automated custody or on-chain withdrawal of user funds from external bookmakers (handled P2P by Master/Operators).
- Deep ML sport prediction model training inside this change (uses `ProbabilityScoreService` with market consensus baseline; dedicated AI microservice will attach later).

## Decisions

### 1. Dual-Ledger Entity Model & Precision
- **Decision**: Store all balances and shares using integer cents/kopecks (`BIGINT` or `NUMERIC(18, 4)`) to avoid IEEE-754 floating-point drift.
- **Entities**:
  - `SyndicateGroup`: `id`, `name`, `master_user_id`, `base_currency`, `status`.
  - `SyndicateMember`: `group_id`, `user_id`, `role`, `status` (`ONLINE`, `READY`, `OFFLINE`).
  - `MemberBookmakerAccount`: `member_id`, `bookmaker_id`, `currency`, `real_balance`, `stake_step` (e.g. 100).
  - `SyndicateLedgerEntry`: `group_id`, `trade_id`, `member_id`, `delta_real`, `delta_math_estimated`, `delta_math_actual`, `settled_at`.
  - `SyndicateRebalanceTicket`: `group_id`, `from_user_id`, `to_user_id`, `amount`, `currency`, `status` (`PENDING`, `CONFIRMED`, `CANCELLED`).
- **Alternative considered**: Single aggregated balance field. Rejected because separating real cash on bookmakers from fair theoretical entitlement is the foundation of group trust and rebalancing.

### 2. Smart Skew & Pluggable Probability Score Architecture
- **Decision**: Introduce interface `ProbabilityScoreService` with method:
  `OutcomeProbability scoreOutcome(SportEvent event, OutcomeType outcome, MarketOdds odds);`
- **Default Implementation**: `MarketConsensusProbabilityProvider` calculating de-margined true probabilities using sharp benchmark bookmakers (e.g., Pinnacle/Fair Exchange ratios).
- **Extensibility**: Future AI/ML score microservice connects via gRPC or Spring Cloud Feign client implementing the same interface.

### 3. Stake Rounding & Conservative Lower-Bound Settlement
- **Decision**: Stake calculation engine computes theoretical stakes, applies `Math.round(stake / step) * step`, and evaluates asymmetric returns:
  $$P_{min} = \min(Return_1 - (S_1 + S_2), Return_2 - (S_1 + S_2))$$
  $P_{min}$ is booked upon trade execution. Upon match result ingestion, the ledger updates to $P_{actual}$.

### 4. Cross-Border Multi-Currency Arbitrage
- **Decision**: Dynamic FX matrix table in Redis/PostgreSQL (`FX_RATES:RUB_BYN`, `FX_RATES:RUB_USD`).
- If currency pair is heterogeneous ($Curr_1 \neq Curr_2$), the matcher enforces $Margin \ge 6.0\%$; otherwise standard margin thresholds ($> 1.0\%$) apply.

### 5. Two-Phase Handshake via Redis / In-Memory State
- **Decision**: State of in-flight proposal stored in Redis with 4-second TTL:
  `syndicate:proposal:{proposalId}` containing `{ status: "PENDING_DUAL_ACK", acks: Set<UserId> }`.
- When `acks.size() == 2`, dispatch STOMP topic message `EXECUTE_SYNC` with target timestamp and abort threshold.

## Risks / Trade-offs

- **[Risk] Bookmaker odds drift during 4-second handshake window** → **Mitigation**: Extension checks current DOM odds against `minAcceptableOdds` immediately before clicking. If odds dropped below breakeven, aborts and notifies server with `ODDS_SLIPPED`.
- **[Risk] One leg accepted by bookmaker, second leg rejected (One-sided exposure)** → **Mitigation**: Dual-sync protocol executes both requests within $\le 50\text{ ms}$. If a reject occurs, extension immediately raises an `EMERGENCY_UNHEDGED_EXPOSURE` WebSocket event to allow rapid manual or automated hedge.
- **[Risk] Operator fails to settle physical P2P rebalance transfer** → **Mitigation**: Master dashboard shows audit history; operator account is frozen from further syndicate participation if drift exceeds safety cap (35%) without confirmed settlement.
