# Proposal: Unified Kafka Event Bus for Canonical Match Scores and Lineups

## Executive Summary
This proposal transitions the platform's real-time match scoring and team roster/lineup ingestion from a high-latency polling and point-to-point HTTP architecture to a resilient, unified Event-Driven Architecture (EDA) backed by Apache Kafka. Using canonical domain DTOs (`CanonicalLiveScoreDto` and `CanonicalMatchLineupDto`), data crawlers (`igaming-capture-sofascore`, `igaming-capture-esports`) continuously publish real-time match scores, rosters, and tactical draft/veto data to dedicated streaming topics (`sports.canonical.scores` and `sports.canonical.lineups`). Downstream consumers (`igaming-mdm-service`, `igaming-ai-analyst`, `aggregator-enrichment`) react asynchronously and instantaneously, driving settlement latency from minutes down to milliseconds and ensuring zero data loss during service restarts.

## Problem Statement
1. **Polling & Cron Latency**:
   The AI Analyst's `MatchSettlementService` relied on a 2-minute periodic cron loop (`@Scheduled(cron = "0 */2 * * * *")`) scanning PostgreSQL tables (`findByResultStatus("PENDING")`) to detect match results. If a match finished right after a cron cycle, settlement was delayed up to 120 seconds, creating race conditions with settlement timeout guards.
2. **Brittle Point-to-Point HTTP Coupling**:
   Crawlers (`igaming-capture-sofascore`, `igaming-capture-esports`) made synchronous HTTP REST calls (`RestTemplate.postForObject`) directly to MDM (`/api/v1/mdm/matches/{id}/live-score`) and aggregator endpoints. Any temporary network spike, GC pause, or pod rolling update caused dropped score updates and crawler task failures.
3. **Absence of a Canonical Lineup Event Stream**:
   While SofaScore and HLTV/OpenDota capture starting lineups, benches, injuries, map vetoes, and hero drafts, there was no unified Kafka event bus for rosters. Tactical models and the Gatekeeper could not react in real time when official lineups were confirmed 40–60 minutes before kickoff.

## Proposed Solution
1. **Canonical DTO Expansion**:
   - `pro.datawiki.igaming.dto.live.CanonicalLiveScoreDto`: Enrich with `externalEventId` and `sport` for universal routing across providers.
   - `pro.datawiki.igaming.dto.live.CanonicalMatchLineupDto`: Establish a canonical, strongly-typed lineup event incorporating `matchId`, `sourceOrigin`, `sport`, `confirmed`, `lineups` (`MatchLineupsDto`), `vetoActions` (CS2/Valorant map vetoes), and `heroDrafts` (Dota 2/LoL hero drafts).
2. **Standardized Kafka Streaming Topics**:
   - `sports.canonical.scores`: Carries `CanonicalLiveScoreDto` for live clock progression, score updates, and terminal match statuses.
   - `sports.canonical.lineups`: Carries `CanonicalMatchLineupDto` for starting rosters, benches, substitutions, injuries, and tactical drafts.
3. **Producers**:
   - `igaming-capture-sofascore`: Broadcasts scores to `sports.canonical.scores` and confirmed lineups to `sports.canonical.lineups`.
   - `igaming-capture-esports`: Broadcasts HLTV/OpenDota series scores to `sports.canonical.scores` and map vetoes/rosters/drafts to `sports.canonical.lineups`.
4. **Consumers**:
   - `igaming-mdm-service`: Consumes `sports.canonical.scores` via `CanonicalScoreKafkaConsumer` and feeds into `MdmLiveScoreConsensusService` for monotonic clock and Redis cache-aside consensus.
   - `igaming-ai-analyst`: Consumes `sports.canonical.scores` via `ReactiveSettlementKafkaConsumer` to immediately settle pending recommendations when a match status is terminal; consumes `sports.canonical.lineups` via `ReactiveLineupKafkaConsumer` to update match dossiers with confirmed lineups.
