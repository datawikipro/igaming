# Tasks: Bookmakers Real-Time Scores and Danger Indicators Pipeline

- [x] 1. Enrich DTO contracts in `igaming-dto` <!-- id: 1 -->
  - [x] 1.1 Add indicator fields (`isSuspended`, `dangerStatus`, `hasBroadcast`, `broadcastUrl`, `corners1/2`, `cards`) to `CanonicalLiveScoreDto` <!-- id: 1.1 -->
  - [x] 1.2 Add corresponding indicator fields to `OddsUpdateRequest` <!-- id: 1.2 -->
  - [x] 1.3 Compile and install `igaming-dto` (`mvn clean install -DskipTests`) <!-- id: 1.3 -->

- [x] 2. Core Bookmaker Engine in `igaming-source-core` <!-- id: 2 -->
  - [x] 2.1 Add `sports.canonical.scores` topic publisher to `AggregatorClient.java` <!-- id: 2.1 -->
  - [x] 2.2 Wire automatic `CanonicalLiveScoreDto` emission upon receiving live updates with score, suspension, or broadcast <!-- id: 2.2 -->
  - [x] 2.3 Compile and verify `igaming-source-core` <!-- id: 2.3 -->

- [x] 3. Tier-1 Bookmaker Parsers & Mappers <!-- id: 3 -->
  - [x] 3.1 Fonbet (`igaming-source-fon-bet-ru`): Extract `score1/2`, `timer`, `eventBlocks` suspension status, and TV stream flags <!-- id: 3.1 -->
  - [x] 3.2 Winline (`igaming-source-winline`): Extract `score1/2`, `timer`, market locked flags, and video stream metadata <!-- id: 3.2 -->
  - [x] 3.3 Betcity (`igaming-source-betcity`): Extract `sc_ev` score, time, and market suspension state <!-- id: 3.3 -->

- [x] 4. Downstream MDM & AI-Analyst Integration <!-- id: 4 -->
  - [x] 4.1 Update `MdmLiveScoreConsensusService` to handle bookmaker scores and set ephemeral `danger:match:<id>` Redis flag <!-- id: 4.1 -->
  - [x] 4.2 Verify `ReactiveSettlementKafkaConsumer` in `igaming-ai-analyst` receives and handles bookmaker score updates <!-- id: 4.2 -->

- [x] 5. Compilation, Container Builds & Deployment <!-- id: 5 -->
  - [x] 5.1 Rebuild Jib images for modified modules (`igaming-source-fon-bet-ru`, `igaming-source-winline`, `igaming-mdm-service`) <!-- id: 5.1 -->
  - [x] 5.2 Deploy to Kubernetes (`xeon-srv`) and verify error-free running pods <!-- id: 5.2 -->
  - [x] 5.3 Verify live score and danger events arriving in `sports.canonical.scores` <!-- id: 5.3 -->
