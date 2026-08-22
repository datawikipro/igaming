# Design: Crawler Feed & Market Depth Optimization

## Architecture & Feed Pipeline

### 1. BetB2B (1xBet, Melbet, FanSport, 22bet, SpinBetter)
- Problem: Get1x2_VZip returns only the top 100-200 showcase events with 1X2 odds.
- Solution: Dynamic URL builder + deep link resolution with normalizeSportSlug() and league ID extraction from XbetFamilyGame.
- Feed optimization: Parse sub-market blocks and dispatch full odds arrays to Aggregator.

### 2. Betcity Prematch Roster
- Problem: app.betcity.prematch-url was https://ad.betcity.ru/d/off/events?rev=6, which returns event headers without market odds.
- Solution: Update prematch-url default to https://ad.betcity.ru/d/off/bets?rev=8&add=dep_event&template=1 (matching live behavior), which delivers full sub-markets for all upcoming games.

### 3. Marathonbet League Throughput
- Problem: Processing 3 leagues every 5 seconds means ~36 leagues/minute, taking >20 minutes to scan 700+ leagues.
- Solution: Increase batch size in processPendingLeagues(25) and tune retry delays to complete full catalog sweeps in < 2 minutes.

### 4. Direct Link Sanitizer
- Frontend sanitizer bookmakerUrls.ts dynamically sanitizes legacy/cached URLs to ensure 100% direct event navigation.
