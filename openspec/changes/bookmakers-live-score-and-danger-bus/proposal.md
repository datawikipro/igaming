# Proposal: Streaming Real-Time Scores and Danger Indicators from Bookmaker Feeds to Kafka

## 1. Problem Statement & Motivation
Currently, bookmaker crawlers (`igaming-source-*`) scrape betting lines every 15–30 seconds and send odds via Kafka topic `odds.updates` / `canonical.odds.updates`. Although bookmaker feeds contain real-time scores, timers, and critical market state indicators (e.g., market lock/suspension during penalties, VAR reviews, dangerous attacks, and live video stream URLs), this information is largely discarded or confined within odds payloads.

Meanwhile, official score aggregators like SofaScore experience a 20–45 second delay due to satellite broadcast latencies and human moderation. In contrast, top bookmakers (Fonbet, Winline, Betcity, Pinnacle, Bet365) ingest real-time data feeds directly from pitch-side stadium scouts (Sportradar, Genius Sports, RunningBall) with sub-second to 2-second latencies.

By enabling bookmaker crawlers to directly stream normalized live scores, match clocks, market suspensions (`isSuspended`), and live video stream indicators to the unified Kafka score bus (`sports.canonical.scores`), the platform achieves:
1. **Sub-second Goal & Score Detection**: Detecting goals and score transitions 15–30 seconds before SofaScore.
2. **Leading Danger Indicator (Early Warning)**: When a bookmaker locks/suspends markets (`isSuspended = true`), it acts as an immediate early warning of high-impact events (penalty awarded, VAR review, red card check, dangerous attack).
3. **Live Broadcast Discovery**: Capturing direct HLS video stream references (`.m3u8` / player URLs) for client-side embedding on SmartBet.guru (*Zero Server Bandwidth*).

## 2. Scope & Capabilities
1. **`igaming-dto`**:
   - Enrich `CanonicalLiveScoreDto` with indicators: `isSuspended`, `dangerStatus`, `hasBroadcast`, `broadcastUrl`, `corners1`, `corners2`, `yellowCards1`, `yellowCards2`, `redCards1`, `redCards2`.
   - Enrich `OddsUpdateRequest` with corresponding indicator fields.
2. **`igaming-source-core`**:
   - Add Kafka producer for `sports.canonical.scores` in `AggregatorClient`.
   - When a bookmaker crawler pushes a live match with score or suspension state, emit a `CanonicalLiveScoreDto` to `sports.canonical.scores`.
   - Provide standard indicator extraction hooks in `AbstractBaseBookmakerService` and `AbstractMatchMapper`.
3. **Tier-1 Bookmaker Mappers**:
   - `Fonbet`: Parse `eventMiscs` (score, timer, comment), `eventBlocks` (market suspension / blocked odds), and TV stream flags.
   - `Winline`: Parse `score1`, `score2`, `timer`, locked market indicators, and broadcast stream metadata.
   - `Betcity`: Parse `sc_ev` (event score & period breakdown) and market suspension state.
4. **`igaming-mdm-service`**:
   - In `MdmLiveScoreConsensusService`, consume bookmaker live scores with low latency to update Redis consensus `live:match:<id>`.
   - Set ephemeral danger state in Redis (`danger:match:<id>`) when `isSuspended == true`.
5. **`igaming-ai-analyst`**:
   - Consume bookmaker score and suspension events for instant reaction, hedging alerts, and settlement.

## 3. User Value & Impact
- Faster settlement: recommendations settle in real time directly from bookmaker pitch feeds.
- Live alerts: instant notification when odds suspend right before a goal is scored.
- Zero extra external API cost: uses feeds already being parsed for betting odds.
