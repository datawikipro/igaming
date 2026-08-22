# Proposal: Crawler Feed & Market Depth Optimization

## 1. Summary
Optimize data collection and market depth across all bookmaker crawlers (especially BetB2B/1xBet family, Betcity, Marathonbet, Winline, and Tennisi) to eliminate artificial feed bottlenecks, expand market coverage (Totals, Handicaps, Props) by 10x-20x, increase league processing throughput, and ensure 100% accurate direct deep-linking across all sources.

## 2. Motivation
- Currently, Leon, Fonbet, and Pari collect over 118,000 to 144,000 odds each across 1,500-2,100 matches using high-efficiency bulk feeds.
- In contrast, 1xBet / BetB2B (60-230 matches, ~2k odds), Betcity (385 matches, 3.1k odds), and Marathonbet (22 matches, 176 odds) suffer from artificial limitations:
  1. BetB2B family was rewriting feeds to Get1x2_VZip, which only returns top showcase matches with 1X2 outcomes, omitting the broader catalog and deep markets.
  2. Betcity prematch endpoint was pointing to d/off/events?rev=6 instead of requesting full event rosters (add=dep_event&template=1).
  3. Marathonbet scheduler was bottlenecked to 3 leagues per 5 seconds (processPendingLeagues(3)), causing starvation across 700+ leagues.
  4. BetB2B direct event links contained dummy hardcoded slugs (/line/sport/league/game-).

## 3. Scope & Changes
- BetB2B Family (1xBet, FanSport, Melbet, 22bet, BetAndYou, SpinBetter):
  - Fix URL generation to dynamically resolve sport and championship IDs.
  - Enable multi-market ingestion (Totals, Handicaps, BTTS, Double Chance).
- Betcity Source:
  - Update prematch feed URL to include add=dep_event&template=1 to capture full market depth.
- Marathonbet Source:
  - Scale league batch processing from 3 to 25 leagues per cycle to process all 700+ leagues rapidly.
- Frontend / Aggregator Sanitation:
  - Add bookmakerUrls.ts sanitizer to guarantee resilient direct deep-linking on the web UI.

## 4. Verification
- openspec validate --specs must pass cleanly.
- Live URL tests must return HTTP 200 for all bookmakers.
- Real-time odds count for Betcity, 1xBet, and Marathonbet will increase significantly.
