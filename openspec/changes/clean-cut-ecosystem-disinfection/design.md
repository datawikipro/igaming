# Design: Clean-Cut Ecosystem Disinfection

## Architecture & Principles
1. **Zero Outlier Discard & No Crutches Policy (Rule 1)**:
   - Data defects must be addressed at the source (crawlers and MDM mapping), never masked by client-side filters (`syntheticFilter.ts`) or procedural hacks.
2. **Strict ID-Only & LLM-Verified Mapping Mandate (Rule 1.2)**:
   - Runtime code must never perform string similarity (`SimilarityUtils.areTeamsSimilar`, Levenshtein, Jaccard) or substring guessing (`contains`, `startsWith`).
   - Entity merging and dictionary updates must occur strictly in offline MDM pipelines with LLM verification.
3. **Clean-Cut Policy for Non-SLA Projects (Rule 1.4)**:
   - Legacy bridges and dangerous cron routines must be severed cleanly rather than patched with conditional branches.
4. **Zero-Diff OCP Mandate (Rule 8)**:
   - Sport classification and market parsing must operate through domain enums and polymorphic strategies rather than hardcoded `sportId == 43 || ...` or procedurally generated outcomes.

## Technical Specifications by Module

### 1. `aggregator-normalizer`
- **`CanonicalDictionarySyncService.java`**:
  - In `executeSync()` / `runDataRefinementSteps()`, remove or completely disable Step 5:
    - Remove `UPDATE team SET sport_id = 'TENNIS_DOUBLES' WHERE ... ~* '/'`
    - Remove `UPDATE team SET sport_id = 'CYBER_FOOTBALL' WHERE ... ~* '\\([a-zA-Z0-9_-]+\\)$'`
    - Remove `UPDATE team SET default_display_name = regexp_replace(...)`
  - In `runDataRefinementSteps()`, disable Step 6.6 (`unifyOpponentAnchorTeams`).
- **`MatchDeduplicationAndCleanupService.java`**:
  - Remove cyber-football / football grouping heuristics.

### 2. `aggregator-domain`
- **`CommonNormalizationService.java`**:
  - In `normalizeRunnerOutcome`:
    - Remove fallback branches checking `isSameTeam(rawName, match.getTeam1()) -> OutcomeType.WIN1` and `isSameTeam(rawName, match.getTeam2()) -> OutcomeType.WIN2`.
    - If a runner cannot be safely resolved to a standard 1X2 / OverUnder / Handicap outcome, it must retain its raw market descriptor or fail fast rather than mutating into match winner.
  - In `isSameTeam`:
    - Change implementation to exact equality after normalization: `n1.equals(n2)`. Remove `n1.contains(n2) || n2.contains(n1)`.

### 3. `igaming-ai-analyst` & `igaming-anomaly-resolver-agent`
- **`MatchSettlementService.java`**:
  - In `resolveEsportsScore()` and `extractEsportsScoreFromRow()`:
    - Eliminate runtime `SimilarityUtils.areTeamsSimilar` fallback.
    - Resolve esports scores strictly by matching canonical `team1Id` and `team2Id` or source `matchId`.
- **`MatchPreFlightGatekeeper.java`**:
  - In `detectScheduleCollisions`:
    - Compare `rec.team1Id().equals(existing.team1Id())` instead of string matching.
- **`EmpiricalAnomalyAuditorService.java`**:
  - In `evaluateFastHeuristic`:
    - Change `if (hMatch || aMatch)` to `if (hMatch && aMatch)`.
  - In sport checks:
    - Replace `(sportId == 43 || sportId == 44 ...)` with `Sport.isStatisticalProxy(sportId)` or enum lookup.
- **`TeamSignatureNormalizer.java`**:
  - Remove `rawName.contains("/")` checking.

### 4. `smartbet.guru` (Frontend)
- **`syntheticFilter.ts`**:
  - Remove file and replace references in `page.tsx`, `MatchesClient.tsx`, `HomeClient.tsx`, and `ArbsTeaserWidget.tsx` with direct match array passing.
