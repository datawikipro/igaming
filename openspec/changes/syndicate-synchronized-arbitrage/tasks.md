## 1. Database Schema & Dual-Ledger Entities

- [x] 1.1 Create Flyway migration for `syndicate_groups`, `syndicate_members`, and `member_bookmaker_accounts` tables in PostgreSQL
- [x] 1.2 Create JPA entities and repositories for `SyndicateGroup`, `SyndicateMember`, and `MemberBookmakerAccount`
- [x] 1.3 Create Flyway migration for `syndicate_ledger_entries` and `syndicate_rebalance_tickets`
- [x] 1.4 Implement `DualLedgerService` with conservative initial profit booking ($\min(P_1, P_2)$) and post-settlement actual profit reconciliation

## 2. Probability Scoring & Smart Skew Engine

- [x] 2.1 Define `ProbabilityScoreService` interface and `OutcomeProbability` DTOs in `igaming-aggregator`
- [x] 2.2 Implement `MarketConsensusProbabilityProvider` calculating de-margined sharp benchmark probabilities
- [x] 2.3 Implement `SmartSkewRoutingEngine` to assign high-probability favorite legs to deficit accounts and underdog legs to surplus accounts

## 3. Targeted Matching & Stake Calculation

- [x] 3.1 Implement currency-specific stake rounding utility (`StakeRoundingUtil`) with configurable steps (e.g. 100 RUB, 5 USD)
- [x] 3.2 Implement FX Cross-Rate Matrix provider and enforce $> 6.0\%$ minimum yield gate on multi-currency pairs
- [x] 3.3 Implement `SyndicateMatcherService` evaluating live surebets against active syndicate member combinations with fair load balancing

## 4. Two-Phase Handshake & WebSocket Coordinator

- [x] 4.1 Implement `SyndicateHandshakeCoordinator` managing 4-second TTL proposal state in Redis
- [x] 4.2 Configure STOMP WebSocket endpoints `/topic/syndicate/{groupId}` and `/app/syndicate/ack` in `igaming-portal`
- [x] 4.3 Implement drift calculation engine ($\delta = (B_{real} - B_{math}) / B_{math}$) and automated 20% threshold alert generation

## 5. Chrome Extension Synchronized Execution

- [x] 5.1 Add WebSocket/runtime listener for `PREPARE_COUPON` and `EXECUTE_SYNC` commands in `chrome-extension`
- [x] 5.2 Implement bookmaker coupon autofill with rounded stakes and slippage check (`minAcceptableOdds`)
- [x] 5.3 Implement sub-second bet submission trigger and emergency abort reporting on odds drop

## 6. UI & Dashboard (Web & Mobile)

- [x] 6.1 Implement Syndicate Room Dashboard with live member status, real vs. mathematical balance cards, and drift meters
- [x] 6.2 Implement 4-second dual confirmation popup with circular countdown timer and haptic/audio alert
- [x] 6.3 Implement Rebalance Settlement Ticket modal for Master and operators with transfer details and confirmation buttons

## 7. Verification & DoD

- [x] 7.1 Write unit tests for stake rounding, lower-bound profit calculations, and $>6\%$ FX yield gating
- [x] 7.2 Write integration tests for two-phase dual-ACK coordinator with timeout and abort scenarios
- [x] 7.3 Validate OpenSpec specifications via `openspec validate --specs`

