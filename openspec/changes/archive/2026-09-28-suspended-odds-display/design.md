## Context

See `proposal.md` for motivation. Currently, `MatchDetailController.java` executes:
```java
LocalDateTime freshnessCutoff = LocalDateTime.now().minusMinutes(isLive ? 2 : 30);
oddsActualRepository.findFlatOddsByMatchId(id, freshnessCutoff);
```
which filters out any odd whose last update was older than 2 minutes in live mode. On the frontend, `renderOddsCell` in `MatchDetailClient.tsx` receives no odd and renders a blank dash `<td>—</td>`.

## Goals / Non-Goals

**Goals:**
- Deliver all match odds to the frontend with an explicit boolean flag `isSuspended` indicating whether the market is open or frozen/withdrawn.
- In `MatchDetailClient.tsx`, render suspended odds as dimmed/grayed out cells with a lock icon, disabled quick-action betting, and a descriptive tooltip ("Приём ставок приостановлен букмекером").
- Preserve odds movement history modal exploration for suspended odds so users can inspect the price trajectory up to closure.
- Guarantee that suspended / stale odds are strictly excluded from surebet, +EV, and middle generation in `aggregator-surebet`.

**Non-Goals:**
- Allowing user betting or automated hedge execution on suspended odds.
- Retaining odds from completed matches for infinite duration outside normal retention policies.

## Decisions

### 1. Retention and Suspended Classification in SQL & Controller
- **Choice**: Instead of a hard `WHERE updated_at >= :freshnessCutoff` that discards records, `findFlatOddsByMatchId` will retrieve the latest price per outcome and compute `is_suspended` via:
  ```sql
  (o.updated_at < :freshnessCutoff) AS is_suspended
  ```
- **Rationale**: Keeps database query execution fast with existing index `idx_odds_actual_upsert_search`, avoiding N+1 queries while returning the last known closing line.
- **Alternatives Considered**: 
  - *Hard deletion on suspension*: Loses closing line data.
  - *Cron job marking rows inactive*: Adds write amplification on PostgreSQL under high-frequency tick rates.

### 2. DTO & API Contract (`FlatOddsProjection`)
- Add `Boolean getIsSuspended()` to `FlatOddsProjection` interface in `aggregator-domain`.
- Jackson serializes this directly as `"isSuspended": true | false`.
- Portal gateway proxies this to Next.js clients seamlessly.

### 3. Surebet Engine Isolation
- In `aggregator-surebet`, the in-memory evaluator filters candidates by `isSuspended == false` and strict live TTL (max 60s). Even if a suspended odd is retained in `odds_actual` for match-center visualization, the surebet matching engine ignores it completely.

### 4. UI Rendering in `MatchDetailClient.tsx`
- Class `.odds-suspended`:
  - `opacity: 0.45`
  - `cursor: pointer` (for opening odds history modal) but disables hedge button
  - Small lock icon 🔒 next to the price
  - Tooltip: `Приём ставок приостановлен букмекером (последняя котировка)` / `Betting suspended by bookmaker (closing odds)`

## Risks / Trade-offs

- **[Risk] High-volume live matches might retain obsolete alternate handicaps/totals**:
  - *Mitigation*: Limit the query to fetch only the latest row per `(bet_source_id, odds_type_id, param)` using the existing unique constraint/index.
- **[Risk] User confusion if they try to bet on a gray odd**:
  - *Mitigation*: Visually disable the hedge button and display explicit lock icon with explanatory tooltip.
