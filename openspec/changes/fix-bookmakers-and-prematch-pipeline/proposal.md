# Change: Fix Bookmaker Crawlers, Zenit Prematch Ingestion, and Full Pipeline Stabilization

## Why

Currently, there is a significant disproportion in bookmaker line volumes (e.g. Winline at 16k+ odds vs Zenit at ~400 odds). Zenit only collects live events due to a failing prematch printer endpoint, while other high-value sources (Pinnacle, Betboom, 1xBet, Marathonbet) require configuration, timeout resilience, and proxy stabilization to stream consistent prematch and live odds across all sports.

## What Changes

1. **Zenit Prematch & Live Recovery**:
   - Update Zenit prematch query parameters and fallback to direct endpoint parsing.
   - Adjust anti-bot cookie capture (`imprint` generation) and timeout resiliency.
2. **Pinnacle & Betboom Source Stabilization**:
   - Ensure DNS configuration (`igaming-aggregator.igaming-master.svc.cluster.local`) and credentials are active.
   - Streamline API extraction for Pinnacle Guest API to restore the primary sharp benchmark for +EV betting.
3. **Loader & Proxy Resiliency (Marathonbet, 1xBet, Olimpbet)**:
   - Handle proxy rotation exhaustion gracefully with exponential backoff and retry instead of fatal pod exits.
   - Streamline database polling across all 25+ bookmakers.
4. **End-to-End Verification & DoD**:
   - Verify that match volumes across bookmakers normalize into multi-bookmaker overlaps (target 50+ overlaps).
   - Ensure continuous generation of active surebets and valuebets served on `smartbet.guru`.
