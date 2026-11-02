# Design: Unified Kafka Event Bus for Canonical Match Scores and Lineups

## Architecture Overview

```
 [SofaScore Crawler]         [HLTV / OpenDota Crawler]
          │                              │
          │ CanonicalLiveScoreDto        │ CanonicalLiveScoreDto
          ▼                              ▼
 ────────────────────────────────────────────────────────
       Topic: sports.canonical.scores
 ────────────────────────────────────────────────────────
          │                              │
          ▼                              ▼
 [igaming-mdm-service]          [igaming-ai-analyst]
  - Monotonic Clock Consensus    - Reactive Settlement (<50ms)
  - Redis In-Memory State        - PnL & Tactical Forensics
  - 1-min Batch Flusher to DB    - ai_learning_memory update


 [SofaScore Lineups]         [HLTV Roster & Veto / OpenDota Draft]
          │                              │
          │ CanonicalMatchLineupDto      │ CanonicalMatchLineupDto
          ▼                              ▼
 ────────────────────────────────────────────────────────
       Topic: sports.canonical.lineups
 ────────────────────────────────────────────────────────
          │                              │
          ▼                              ▼
 [igaming-mdm-service / DB]     [igaming-ai-analyst]
  - Match Dossier Roster Store   - Gatekeeper Re-evaluation
  - Player MDM Linkage           - Formation & Depth Analysis
```

## Data Contracts

### 1. `CanonicalLiveScoreDto` (igaming-dto)
- `matchId` (Long): Canonical MDM match identifier (`mdm_match.id`).
- `sourceCode` (String): Source identifier (`sofascore`, `hltv`, `opendota`, etc.).
- `externalEventId` (String): Source external identifier.
- `sport` (String): Sport slug or name.
- `status` (MatchStatus): `SCHEDULED`, `LIVE`, `FINISHED`, `CANCELLED`, `POSTPONED`, `INTERRUPTED`.
- `score1` / `score2` (Integer): Primary total score (goals, sets, maps, points).
- `regularScore1` / `regularScore2` (Integer): Regulation time score.
- `overtimeScore1` / `overtimeScore2` (Integer): Overtime score.
- `penaltiesScore1` / `penaltiesScore2` (Integer): Shootout penalties.
- `elapsedSeconds` (Integer): Monotonic elapsed seconds.
- `displayTimer` (String): Human-readable display string (`45'`, `78:12`, `FT`).
- `periods` (List<PeriodScoreDto>): Period-by-period score breakdown.
- `timestamp` (Long): Epoch milliseconds.

### 2. `CanonicalMatchLineupDto` (igaming-dto)
- `matchId` (Long): Canonical MDM match ID.
- `sourceOrigin` (String): `sofascore`, `hltv`, `opendota`, `vlr`.
- `externalEventId` (String): Source match/event ID.
- `sport` (String): Sport slug.
- `confirmed` (boolean): Flag indicating official lineup confirmation.
- `lineups` (MatchLineupsDto): Starters, bench, missing players, substitutions, coaches.
- `vetoActions` (List<VetoActionDto>): Esports CS2/Valorant map ban/pick actions.
- `heroDrafts` (List<HeroDraftDto>): Esports Dota 2/LoL hero ban/pick actions.
- `timestamp` (Long): Epoch milliseconds.

## Producer Implementation
- **`igaming-capture-sofascore`**:
  - `SofaRawEventService` and `SofaTaskProcessor`: Ingest match ticks, convert to `CanonicalLiveScoreDto`, and emit to `sports.canonical.scores`.
  - `SofaScoreEnrichmentProvider` and `SofaOnDemandService`: When lineups or missing players are parsed, convert to `CanonicalMatchLineupDto` and emit to `sports.canonical.lineups`.
- **`igaming-capture-esports`**:
  - `EsportsRawEventService`:
    - On CS2 HLTV ingest: emit series/map score as `CanonicalLiveScoreDto` to `sports.canonical.scores`, and emit rosters + veto actions as `CanonicalMatchLineupDto` to `sports.canonical.lineups`.
    - On Dota 2 OpenDota ingest: emit final match score as `CanonicalLiveScoreDto` to `sports.canonical.scores`, and emit hero drafts + player statistics as `CanonicalMatchLineupDto` to `sports.canonical.lineups`.

## Consumer Implementation
- **`igaming-mdm-service`**:
  - `CanonicalScoreKafkaConsumer`:
    - Listens on `sports.canonical.scores`.
    - Invokes `MdmLiveScoreConsensusService.ingestLiveScore(dto)`.
    - Benefits: Decouples crawlers from MDM HTTP endpoints, absorbs traffic spikes, Redis in-memory speed, resilient delivery with consumer group offset tracking.
- **`igaming-ai-analyst`**:
  - Add `spring-kafka` dependency in `pom.xml`.
  - `ReactiveSettlementKafkaConsumer`:
    - Listens on `sports.canonical.scores`.
    - When `dto.isFinished()`, looks up pending recommendations for `dto.getMatchId()`.
    - Calls `matchSettlementService.settleSingleRecommendation(rec)` immediately.
    - Achieves sub-second bet settlement upon match conclusion.
  - `ReactiveLineupKafkaConsumer`:
    - Listens on `sports.canonical.lineups`.
    - Ingests confirmed lineups into the match dossier and updates pre-match confidence / tactical depth metrics.
