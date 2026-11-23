# Proposal: Clean-Cut Runtime Heuristic Disinfection

## Problem & Motivation
In accordance with Rule 1 (Zero Outlier Discard & No Crutches Policy), Rule 1.2 (Strict ID-Only & LLM-Verified Mapping Mandate), and Rule 1.4 (Clean-Cut Policy for Non-SLA Projects), the codebase contains legacy string-based heuristics that compromise data integrity and system performance:
1. **Fuzzy String Matching in Runtime**: `MdmTeamResolutionService` and `MdmTournamentResolutionService` execute runtime Jaro-Winkler calculations (`jaroWinklerSimilarity >= 0.75`), substring search (`candClean.contains(rawClean)`), and phonetic checks, causing false-positive mappings (e.g., mixing male/female teams, confusing youth squads, creating mutant match entities).
2. **Slash `/` Doubles Heuristic**: Doubles tennis was heuristically detected by `teamName.contains("/")`, breaking when regular clubs or sponsors contain slashes. Doubles must be strictly resolved by canonical sport/discipline ID (`TENNIS_DOUBLES`).
3. **Silent Match Dropping (Cyber/Amateur)**: `CyberMatchDetector` actively regex-matches and silently drops events in `TeamNormalizationHandler` and `MatchIntegrityValidator`, masking dirty crawler data instead of exposing it to testers and correcting source mappings.
4. **Regex Suffix/Gender Stripping**: Widespread `replaceAll` regexes strip brackets, age categories (`U19`, `U21`), and gender tokens (`(W)`, `(ж)`), losing vital semantic classification before mapping occurs.
5. **Runtime SQL String Matching**: `CanonicalDictionarySyncService` performs 29 queries using `LIKE`/`ILIKE` on entity names.

## Proposed Changes
1. **Purge Fuzzy Fallbacks in MDM**:
   - Delete all fallback calls to `SimilarityUtils.areTeamsSimilar(...)`, `SimilarityUtils.areTournamentsSimilar(...)`, `jaroWinklerSimilarity(...)`, and substring guessing in `MdmTeamResolutionService` and `MdmTournamentResolutionService`.
   - Replace slash-based `isDoubles` checking with strict canonical sport/discipline verification (`TENNIS_DOUBLES`).
   - If an entity is not found via canonical ID or pre-mapped blueprint dictionary, immediately throw/log `[MDM-FAILFAST]` and send to anomaly queue.
2. **Remove Silent Discarding in Aggregator**:
   - Remove `CyberMatchDetector` silent-drop logic from `TeamNormalizationHandler`.
   - Stop hiding crawler defects; pipe events through without silent drops.
3. **Clean DTOs & Normalizers**:
   - Refactor `SimilarityUtils`, `TeamSignatureNormalizer`, and `CyberMatchDetector` to prevent runtime string tampering.
4. **Verification**:
   - Compile all affected modules.
   - Deploy `igaming-mdm-service` and `aggregator-normalizer` on Xeon dev cluster.
   - Run 5-minute soak test checking zero exceptions and clean logs.
