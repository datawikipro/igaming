# Tasks: Unified Kafka Event Bus for Canonical Match Scores and Lineups

- [x] 1. Expand canonical DTOs in `igaming-dto` <!-- id: 1 -->
  - [x] 1.1 Enrich `Sport` interface with `getLineupKafkaTopic()` and register canonical sport mappings in `Sports.java` <!-- id: 1.1 -->
  - [x] 1.2 Implement polymorphic `CanonicalLineupEvent` interface and dedicated sport-specific lineup DTOs: `FootballMatchLineupDto`, `Cs2MatchLineupDto`, `Dota2MatchLineupDto`, `BasketballMatchLineupDto`, `HockeyMatchLineupDto` <!-- id: 1.2 -->
  - [x] 1.3 Compile and install `igaming-dto` (`mvn clean install -DskipTests`) <!-- id: 1.3 -->

- [x] 2. Implement Kafka Producers <!-- id: 2 -->
  - [x] 2.1 Establish canonical score topic `sports.canonical.scores` and polymorphic lineup topics `sports.<sport>.lineups` <!-- id: 2.1 -->
  - [x] 2.2 Wire score and football lineup publishing in `igaming-capture-sofascore` (`SofaTaskProcessor`, `SofaReconciliationService`) <!-- id: 2.2 -->
  - [x] 2.3 Wire score, roster, map vetos, and hero drafts publishing in `igaming-capture-esports` (`EsportsRawEventService`) <!-- id: 2.3 -->

- [x] 3. Implement Kafka Consumers <!-- id: 3 -->
  - [x] 3.1 Implement `CanonicalScoreKafkaConsumer` in `igaming-mdm-service` with Redis consensus cache updating <!-- id: 3.1 -->
  - [x] 3.2 Add `spring-kafka` and configure consumer properties in `igaming-ai-analyst` <!-- id: 3.2 -->
  - [x] 3.3 Implement `ReactiveSettlementKafkaConsumer` in `igaming-ai-analyst` for sub-second recommendation settlement <!-- id: 3.3 -->
  - [x] 3.4 Implement `ReactiveLineupKafkaConsumer` in `igaming-ai-analyst` with dynamic regex pattern `^sports\..*\.lineups$` <!-- id: 3.4 -->

- [x] 4. Compilation, Jib Container Builds & Deployment <!-- id: 4 -->
  - [x] 4.1 Compile and verify test suites across all affected modules <!-- id: 4.1 -->
  - [x] 4.2 Build and push Jib images for `igaming-mdm-service`, `igaming-ai-analyst`, `igaming-capture-esports`, `igaming-capture-sofascore` <!-- id: 4.2 -->
  - [x] 4.3 Deploy / restart pods in K8s and verify error-free logs on `xeon-srv` <!-- id: 4.3 -->
  - [x] 4.4 Verify live event flow through Kafka topics and consumer partition assignments <!-- id: 4.4 -->
