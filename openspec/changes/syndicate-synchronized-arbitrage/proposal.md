## Why

Single-operator arbitrage betting faces severe operational constraints: bookmaker account linking via IP/fingerprint correlations, fast stake limits reductions, and the physical latency of single-handedly clicking counter-legs in volatile live markets.

This change introduces collaborative syndicate betting with N operators and 1 Master treasury leader. By distributing counter-legs across independent physical users and devices, executing bets through a millisecond dual-confirmation handshake via browser extensions, and maintaining mathematical equilibrium via Smart Skew probabilistic self-balancing and a dual-ledger accounting system, the syndicate operates at scale while keeping all members within a profitable, risk-balanced "Green Zone".

## What Changes

- **N-Participant Syndicate Management**: Support arbitrary group size (1 Master / Bankroll Leader + N Operators) with dynamic presence tracking, per-bookmaker credentials, and active balance caps.
- **Configurable Stake Rounding & Granularity**: Round calculated stakes to natural steps per currency (e.g., multiples of 100 RUB, 5 USD, 10 BYN) for anti-bot camouflage.
- **Dual-Ledger Accounting (Conservative Estimated vs. Actual Settled Profit)**: Record theoretical profit initially using the conservative lower-bound outcome ($\min(P_1, P_2)$) due to rounding skew, resolving to exact actual profit upon event completion.
- **Cross-Border Multi-Currency Arbitrage (>6.0% Threshold)**: Maintain real-time FX cross-rate matrices for international syndicates (e.g. RU - BY - USA), automatically enforcing a strict $> 6.0\%$ minimum yield gate for cross-currency pairs to absorb FX conversion spreads and volatility.
- **Automated 20% Drift Detection & Settlement Alerts**: Detect when $|(B_{real} - B_{math}) / B_{math}| \ge 20\%$ and generate automated P2P transfer tickets between the Master and operators.
- **Smart Skew & Probabilistic Leg Routing**: Pluggable `ProbabilityScoreService` (defaulting to sharp market consensus, ready for dedicated AI scoring microservice) that routes high-probability favorite legs (e.g. odds 1.20, ~83% winrate) to accounts in deficit and underdog counter-legs to accounts with surplus, ensuring continuous natural convergence into the "Green Zone" ($\Delta \approx 0\%$).
- **Two-Phase Dual-Confirmation Handshake**: Fast WebSocket coordinator that broadcasts targeted arbitrage proposals, requires simultaneous ACKs within a 4-second deadline, and triggers synchronized browser extension placement (`EXECUTE_SYNC`).
- **Synchronized Browser Automation (Chrome Extension / Desktop / Mobile)**: Sub-second coupon autofill and execution with slippage tolerance and automatic emergency abort if odds drop below breakeven.

## Capabilities

### New Capabilities
- `syndicate-arbitrage`: N-participant syndicate group management, dual-ledger accounting, 20% drift settlement workflow, smart skew probabilistic routing, and two-phase dual-ACK execution coordinator.

### Modified Capabilities
- `aggregator-core`: Add group-targeted surebet matching, fair load balancing across eligible operator pairs, and smart skew probability-based leg assignment.
- `portal-gateway`: Expose syndicate REST APIs, real-time STOMP/WebSocket room channels (`/topic/syndicate/{groupId}`), and dual-ledger settlement endpoints.

## Impact

- **Database**: New PostgreSQL tables (`syndicate_groups`, `syndicate_members`, `member_bookmaker_accounts`, `syndicate_ledger_entries`, `syndicate_rebalance_tickets`, `syndicate_execution_orders`).
- **Backend Services**:
  - `igaming-aggregator`: `SyndicateMatcherService`, `SmartSkewRoutingEngine`, `ProbabilityScoreService` (market consensus & sharp benchmark provider).
  - `igaming-portal`: `SyndicateHandshakeCoordinator`, `DualLedgerService`, `SyndicateRebalancerService`, WebSocket STOMP controllers.
- **Frontend & Chrome Extension**: Group dashboard UI, live balance matrix, emergency 4-second confirmation modal, and extension `EXECUTE_SYNC` handlers with anti-fingerprint protection.
