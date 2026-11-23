# Design: Clean-Cut Runtime Heuristic Disinfection

## Architecture Overview
The core philosophy is **Strict Deterministic ID Resolution** and **Fail-Fast Anomaly Logging**:
1. Runtime ingestion matches incoming bookmaker events strictly by canonical ID (`team_id = ?`, `id = ?`) or through pre-compiled, LLM-verified mapping dictionaries (`mdm_team_mapping`, `mdm_tournament_mapping`, `mdm_sport_mapping`).
2. When a raw string has no established mapping, the runtime service MUST NOT attempt fuzzy substring search, Jaro-Winkler string similarity, or regex stripping. It immediately emits an anomaly event to `mdm.anomaly.errors` and skips mutant creation.
3. Sub-sports and disciplines (e.g. `TENNIS_DOUBLES`, `BASKETBALL_3X3`, `FOOTBALL_WOMEN`) are first-class citizens identified by their distinct `sport_id` / slug, completely replacing structural string checks like `contains("/")` or `contains("women")`.

## Component Level Changes

### 1. `MdmTeamResolutionService` & `MdmTournamentResolutionService`
- Remove:
  - `SimilarityUtils.areTeamsSimilar(...)` and `SimilarityUtils.areTournamentsSimilar(...)`.
  - `SimilarityUtils.jaroWinklerSimilarity(...)`.
  - `contains("/")` doubles branching. Instead, check `sport.getSlug().contains("doubles")` or sport ID matching `TENNIS_DOUBLES`.
  - `contains("women")`, `contains("zhen")`, and substring guessing.
  - Substring `contains` matching (`candClean.contains(rawClean)`).

### 2. `aggregator-normalizer`
- In `TeamNormalizationHandler`:
  - Eliminate `CyberMatchDetector.isCyberMatch(...)` silent rejection.
  - Forward events to MDM with raw metadata intact.

### 3. Verification Strategy
- Maven test-compilation on all altered modules:
  - `igaming-dto`
  - `igaming-mdm-service`
  - `aggregator-normalizer`
- Deploy to K8s and verify 5-minute soak test on Xeon cluster.
