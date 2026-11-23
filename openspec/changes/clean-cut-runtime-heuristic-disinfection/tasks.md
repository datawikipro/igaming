# Tasks: Clean-Cut Runtime Heuristic Disinfection

- [x] Purge runtime heuristics in `MdmTeamResolutionService.java`
  - [x] Remove `SimilarityUtils.areTeamsSimilar` and substring matching (`contains`)
  - [x] Replace `contains("/")` with canonical doubles discipline check
  - [x] Remove gender string guessing `toSlug(teamName).contains("women")`
- [x] Purge runtime heuristics in `MdmTournamentResolutionService.java`
  - [x] Remove `SimilarityUtils.areTournamentsSimilar` and Jaro-Winkler
  - [x] Remove `contains("cup")` / `contains("кубок")` heuristic branches
- [x] Purge runtime heuristics in `MdmMatchResolutionService.java`
  - [x] Remove `tryResolveByTemporalMatchAnchor` and runtime fuzzy match injection
- [x] Clean `aggregator-normalizer`
  - [x] Remove `CyberMatchDetector` silent discard in `TeamNormalizationHandler.java`
- [x] Clean `igaming-dto` and `MatchIntegrityValidator.java`
  - [x] Integrate canonical sport ID checks (45, 65) for doubles racket sports
- [x] Build & Unit Test Verification
  - [x] Run `mvn test` across affected modules (35 tests in mdm-service, 231 tests in dto, all passing)
- [x] Deploy & 5-minute Soak Test
  - [x] Rebuild and deploy `igaming-mdm-service` to Xeon cluster
  - [x] Verify 5 minutes of clean logs with 0 errors / 0 exceptions
