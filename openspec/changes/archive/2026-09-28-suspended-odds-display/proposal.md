## Why

When a bookmaker stops offering odds for a particular market or outcome (e.g. late in a soccer match at 5:0; explicit market suspension; or an outcome omitted from live feeds), the aggregator previously purged or filtered out the odd completely via freshness cutoffs (`WHERE updated_at >= cutoff`), causing the frontend match center to display an empty dash `—`. 

This degrades user experience by obscuring historical closing line prices and creating ambiguity over whether the market was ever offered or lost due to a crawler failure. Furthermore, displaying the last known closing odds in a muted "suspended/gray" state provides critical match context, while strictly prohibiting these inactive odds from polluting the arbitrage matching engine with phantom surebets.

## What Changes

- **Backend Suspended Odds Lifecycle & Projection**:
  - Add `isSuspended` (and/or `status`: `ACTIVE` vs `SUSPENDED`) to flat odds projections and queries (`FlatOddsProjection`, `OddsActual`).
  - Update `MatchDetailController.java` to return suspended/stale odds rather than dropping them, retaining the last observed price with `isSuspended: true` throughout the match lifecycle.
  - Retain strict exclusion of suspended / stale odds from surebet, value bet, and middle detection in `aggregator-surebet`.
- **Frontend Match Center UI Rendering**:
  - Update `MatchDetailClient.tsx` to render suspended odds in a distinct dimmed gray state with reduced opacity, lock icon 🔒, disabled quick-betting, and informative tooltip indicating market suspension by the bookmaker.
  - Preserve odds movement history modal triggers on click for suspended cells to allow inspection of closing line line-movement.

## Capabilities

### Modified Capabilities
- `aggregator-core`: Add specification requirements for odds suspension detection (explicit vs staleness TTL) and the strict exclusion of suspended odds from surebet generation.
- `portal-gateway`: Add specification requirements for returning suspended odds metadata (`isSuspended: true`) in `/api/v1/matches/{id}/odds` and `/api/matches/{id}/odds`.

## Impact

- **Affected Services**:
  - `aggregator-domain`: `FlatOddsProjection`, `OddsActual` / query logic.
  - `aggregator-api`: `MatchDetailController.java` (`/api/matches/{id}/odds`).
  - `aggregator-surebet`: `SurebetEvaluator` ensures suspended odds are skipped in calculation.
  - `smartbet.guru` (Frontend): `MatchDetailClient.tsx`, `matches-detail.css`.
- **Breaking Changes**: None. Backwards-compatible addition of `isSuspended` field to existing REST payload.
