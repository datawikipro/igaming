## 1. Backend Data Model & Query Support

- [x] 1.1 Update `FlatOddsProjection` in `aggregator-domain` to include `Boolean getIsSuspended()`.
- [x] 1.2 Update query in `OddsActualRepository` to project `(o.updated_at < :freshnessCutoff) AS is_suspended` while retaining the latest prices per outcome.
- [x] 1.3 Update `MatchDetailController.java` in `aggregator-api` to return suspended odds with `isSuspended: true` instead of dropping them from `/api/matches/{id}/odds`.
- [x] 1.4 Verify that `aggregator-surebet` strictly ignores any odds where `isSuspended == true` or staleness cutoff is exceeded.

## 2. Frontend Interface & Styling

- [x] 2.1 Update frontend types/interfaces in `smartbet.guru` to recognize `isSuspended?: boolean` on odds objects.
- [x] 2.2 Add styling in `matches-detail.css` for `.odds-clickable-cell.odds-suspended` (dimmed opacity 0.45, muted text, disabled quick-hedge button, dashed border).
- [x] 2.3 Update `renderOddsCell` in `MatchDetailClient.tsx` to display the lock icon 🔒, disabled hedge button, and explanatory tooltip ("Приём ставок приостановлен букмекером").
- [x] 2.4 Verify that clicking a suspended odd cell still triggers `OddsHistoryModal` to allow inspection of line movements.

## 3. Verification & Deployment

- [x] 3.1 Run tests and verify compilation of `aggregator-domain`, `aggregator-api`, and `aggregator-surebet`.
- [x] 3.2 Verify Next.js build in `smartbet.guru`.
- [x] 3.3 Validate OpenSpec specifications with `openspec validate --specs`.
