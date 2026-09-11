# Proposal: Maximize Bookmaker Match and Odds Coverage

## Why

While core high-volume bookmakers (Leon, Fonbet, Pari, Betcity, Winline) aggregate over 460,000 odds across 8,500+ matches, several critical high-tier and mid-tier bookmakers (Betboom, Pinnacle, Olimpbet, Marathonbet, Zenit, Bwin) either suffer from container startup crashes due to legacy Jib entrypoint overrides (`exec java -jar app.jar`), encounter proxy rotation bottlenecks, or only ingest a fraction of their prematch line. Resolving these issues will maximize match ingestion, expand cross-bookmaker event matching, and significantly boost surebet (+EV) generation.

## What Changes

1. **K8s Container Entrypoint & Probe Harmonization**:
   - Strip invalid `command: ["/bin/sh", "-c"]` with `exec java -jar app.jar` across all bookmaker manifests (`betboom.ru.yaml`, `olimpbet.ru.yaml`, `olimpbet.kz.yaml`, `pinnacle.yaml`, `bwin.yaml`, `digitain.yaml`, `marathonbet.*.yaml`, `zenit.win.yaml`, etc.), enabling standard Jib classpaths.
   - Align health probes to `/actuator/health/readiness` and `/actuator/health/liveness`.

2. **Betboom & Pinnacle Line Activation**:
   - Restore Betboom full-tree crawler and match loader execution.
   - Verify and activate Pinnacle Guest API crawler and ingestion pipeline for baseline sharp odds.

3. **Olimpbet & Marathonbet Proxy & Feed Resiliency**:
   - Stabilize loader proxy pool rotation to prevent 403 / 429 rate-limiting.
   - Scale line collectors to extract comprehensive prematch categories and live feeds.

4. **Zenit Prematch Tree Expansion**:
   - Expand sport category tree ingestion and prematch printer endpoint fetching.

5. **Multi-Bookmaker Overlap and Surebet Pipeline Verification**:
   - Verify aggregate match volume growth, multi-bookmaker match overlaps, and active surebet generation.
   - Enforce 5-minute zero-error runtime verification in K8s.

## Capabilities

### Modified Capabilities
- `crawler-engine`: Expand prematch tree extraction, resilient proxy rotation, and Jib entrypoint compatibility for Betboom, Pinnacle, Olimpbet, Marathonbet, Zenit, and Bwin.
- `k8s-infrastructure`: Standardize pod definitions, remove legacy command overrides, eliminate blocking init containers, and configure proper actuator probes.

## Impact

- Affected modules: `igaming-k8s/*.yaml`, `igaming-source-betboom`, `igaming-source-pinnacle`, `igaming-source-olimpbet`, `igaming-source-marathonbet`, `igaming-source-zenit`, `igaming-source-bwin`.
- Infrastructure: `igaming-source` namespace in Kubernetes.
- Business value: Increased match overlap from 18+ active bookmakers, unlocking higher quantity and quality of arbitrage and value betting opportunities.
