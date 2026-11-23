# Proposal: Clean-Cut Ecosystem Disinfection

## Problem & Motivation
Following the successful cleanup of `igaming-mdm-service` under `clean-cut-runtime-heuristic-disinfection`, a comprehensive audit revealed that legacy technical debt, procedural heuristics, and hardcoded fallbacks remain embedded in several key subsystems across the ecosystem:

1. **Destructive Background Cron SQL (`aggregator-normalizer`)**:
   - `CanonicalDictionarySyncService` runs every 30 minutes executing 29 raw SQL scripts with `regexp_replace` and `ILIKE`.
   - It forcibly reclassifies teams containing a slash `/` as `TENNIS_DOUBLES`, reclassifies teams with country tags in parentheses as `CYBER_FOOTBALL`, and executes `unifyOpponentAnchorTeams` merging teams based on Levenshtein $\ge 0.65$ without LLM confirmation.
   - This cyclically pollutes PostgreSQL and destroys canonical MDM invariants.

2. **Critical Odds Market Corruption & Fake Surebets (`aggregator-domain`)**:
   - `CommonNormalizationService` maps runner names containing team names (`isSameTeam(rawName, ...)`) to match winners `WIN1` and `WIN2`.
   - Secondary statistical markets (e.g. corners, cards) matching a team's name are misclassified as match outcome odds, generating phantom 30–50%+ surebets.
   - `isSameTeam` uses loose `n1.contains(n2) || n2.contains(n1)` substring matching.

3. **Runtime Fuzzy Matching in Settlement & Analysis (`igaming-ai-analyst`, `igaming-anomaly-resolver-agent`)**:
   - `MatchSettlementService` uses `SimilarityUtils.areTeamsSimilar` during settlement to link esports match IDs in runtime, leading to miscalculations of ROI/PnL.
   - `MatchPreFlightGatekeeper` detects schedule collisions via team string names instead of canonical IDs.
   - `EmpiricalAnomalyAuditorService` uses `(hMatch || aMatch)` in `evaluateFastHeuristic`, cancelling and treating distinct matches as duplicates if only one team matches.
   - Hardcoded sport IDs `sportId == 43 || ... || 49` instead of enum methods.

4. **Client-Side Defect Masking (`smartbet.guru`)**:
   - `syntheticFilter.ts` intercepts matches on 4 screens in the frontend, suppressing matches containing *"хозяева"*, *"гости"*, and *"home vs away"* instead of resolving or filtering them cleanly at the crawler/API layer.

## Proposed Changes
1. **Track 1: Aggregator Normalizer & Cron Disinfection**:
   - Disable/remove destructive SQL Step 5 (slash tennis doubles, bracket cyber football, prefix regex stripping) in `CanonicalDictionarySyncService`.
   - Disable Step 6.6 (`unifyOpponentAnchorTeams` fuzzy merging). Entity unification belongs strictly to MDM with LLM verification.
   - Remove cyber/football collapsing in `MatchDeduplicationAndCleanupService`.

2. **Track 2: Odds Market Normalization Disinfection**:
   - Remove substring runner-to-team matching (`isSameTeam` -> `WIN1`/`WIN2`) in `CommonNormalizationService.java`.
   - Eliminate `contains` from `isSameTeam(n1, n2)`; require exact normalized identity.

3. **Track 3: AI-Analyst, Settlement & Anomaly Resolver Disinfection**:
   - Purge `SimilarityUtils.areTeamsSimilar` from `MatchSettlementService` esports score resolution; require canonical IDs.
   - Update `MatchPreFlightGatekeeper` collision check to use canonical `team1Id`/`team2Id`.
   - Fix `EmpiricalAnomalyAuditorService` to require strict dual-team match `(hMatch && aMatch)`.
   - Replace hardcoded sport IDs with `Sport` enum methods.
   - Clean remaining `.contains("/")` in `TeamSignatureNormalizer`.

4. **Track 4: Frontend Masking Elimination**:
   - Remove `syntheticFilter.ts` and stop client-side masking in `page.tsx`, `MatchesClient.tsx`, `HomeClient.tsx`, and `ArbsTeaserWidget.tsx`.

5. **Verification**:
   - Unit test compilation across all modules.
   - Jib build and deployment of affected containers to Xeon dev cluster.
   - 5-minute soak verification with 0 exceptions in logs.
