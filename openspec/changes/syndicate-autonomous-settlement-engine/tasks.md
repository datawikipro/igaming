# Implementation Tasks: Syndicate Autonomous Simulation & Settlement Engine

## 1. 15 Virtual Accounts & State Seeding
- [ ] 1.1 Define 15 virtual accounts in `src/lib/syndicateSimulationData.ts` (3 Standard, 5 PRO, 7 Ultimate) with bookmaker balances and roles. <!-- id: 1.1 -->
- [ ] 1.2 Implement room state stores supporting persistent local storage and live in-memory updates for the 15 accounts. <!-- id: 1.2 -->

## 2. Hourly Cron Betting Engine & Multi-Factor Match Matcher
- [ ] 2.1 Build `SyndicateBettingEngine` service to select live arbs every 1 hour based on yield, shortest time to start, and balance capacity. <!-- id: 2.1 -->
- [ ] 2.2 Calculate Deep Smart-Skew stakes for each room tier (Standard 6.6K ₽, PRO 25K ₽, Ultimate 100K ₽). <!-- id: 2.2 -->
- [ ] 2.3 Store placed virtual bets in `SyndicateBetHistoryStore`. <!-- id: 2.3 -->

## 3. Visual Proof & Odds Highlight Component
- [ ] 3.1 Create `OddsHighlightProofCard.tsx` component that renders a realistic bookmaker slip with neon bounding box on the target odds and direct match link. <!-- id: 3.1 -->
- [ ] 3.2 Add modal / viewer for inspection of odds screenshots and stake details. <!-- id: 3.2 -->

## 4. Match Result Settlement & Dual-Ledger Bookkeeping
- [ ] 4.1 Implement `MatchSettlementService` to evaluate match outcomes and determine winning/losing legs. <!-- id: 4.1 -->
- [ ] 4.2 Execute Real Ledger adjustments ($B_{real}$) and Math Ledger equal profit distribution ($M_{balance}$). <!-- id: 4.2 -->
- [ ] 4.3 Implement automatic balance drift tracker and P2P rebalancing ticket generator ($\Delta \ge 20\%$). <!-- id: 4.3 -->

## 5. UI Integration & Live Demonstration
- [ ] 5.1 Add "История ставок и симуляций (Почасовой цикл)" widget with visual proof cards to `/syndicate/room`. <!-- id: 5.1 -->
- [ ] 5.2 Add manual "Сымитировать часовой раунд ставок" and "Рассчитать результат матча (Settlement)" buttons for instant testing in UI. <!-- id: 5.2 -->
- [ ] 5.3 Deploy to Kubernetes and verify 5-minute clean operation. <!-- id: 5.3 -->
