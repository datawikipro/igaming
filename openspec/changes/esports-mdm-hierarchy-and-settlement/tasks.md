# Tasks: Esports Canonical MDM Hierarchy & Autonomous Settlement

- [x] Create Flyway migration `V22__add_mdm_season_and_stage_hierarchy.sql` (mdm_season, mdm_season_stage, mdm_match.stage_id, mdm_match.best_of).
- [x] Create Flyway migration `V23__reclassify_esports_disciplines.sql` (reclassify 15,204 matches from sport_id=6 into dedicated 33-42 disciplines).
- [x] Update `igaming-dto` with `bestOf` mathematical formulas, `TournamentBracketFormat` polymorphic types, and market validators.
- [x] Deploy updated `igaming-mdm-service` to Kubernetes cluster (`1/1 Running`).
- [x] Implement score submission endpoint in `MdmLiveScoreConsensusService` and `MdmIngestionClient.submitLiveScore()`.
- [x] Enhance `igaming-capture-esports` to propagate scores into MDM and persist into `esports_raw_events` (`1/1 Running`).
- [x] Fix `@EventListener` and `ddl-auto=none` in `aggregator-enrichment` (`1/1 Running`).
- [x] Import foreign table `esports_raw_events` into `igaming_analytics` via `postgres_fdw`.
- [x] Integrate esports score fallback resolution in `MatchSettlementService.java` for CS2 (HLTV) and Dota 2 (OpenDota).
- [x] Verify live automatic settlement on active esports recommendations (Match 1387659 settled 0:2 LOST).
- [x] 5-minute error-free stability audit across all enrichment and analytics pods.
