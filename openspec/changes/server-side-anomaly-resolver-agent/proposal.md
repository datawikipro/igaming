# Proposal: Server-Side Anomaly-Resolver Agent & Conflict Quarantine Pipeline

## Why

In high-volume multi-bookmaker ingestion, synthetic cross-matches (such as SBOBET or Baltbet fantasy duels like "Celtics vs Lakers" in League 9661) can ingest into MDM *before* official real-world fixtures (e.g. "Knicks vs Lakers" in NBA) arrive.
If the collision detection logic blindly and unilaterally rejects any second match for a team within the same sport time-window, a catastrophic inversion occurs:
**The fake/fantasy match is accepted and bet on, while the real official fixture is rejected and discarded!**

To adhere strictly to the **Zero Outlier Discard & No Crutches Policy (Domain Rule 1)**:
1. When a team schedule collision occurs, neither match must be unilaterally dropped. Both must be quarantined against betting (`SCHEDULE_CONFLICT_PENDING_RESOLUTION`) and the conflict event written to the anomaly error queue (Kafka topic `mdm.anomaly.errors`).
2. An autonomous server-side agent / robot (`igaming-anomaly-resolver-agent`) running in Kubernetes on `xeon-srv` must consume the anomaly queue, cross-reference external ground-truth sources (SofaScore On-Demand, official fixtures, web), determine the legitimate match, cancel the synthetic/fantasy match, and unblock & re-evaluate the genuine match via REST API.

## What Changes

1. **Quarantine Instead of Silent Discard**:
   - In `igaming-ai-analyst` (`AiAnalystOrchestrator`), when a team schedule collision is detected, do NOT mark `result_status = 'CANCELLED'`.
   - Set stage to `SCHEDULE_CONFLICT_PENDING_RESOLUTION` (`conviction_tier = 'PASS'`, `result_status = 'PENDING_RESOLUTION'`) on both candidate matches.
   - Publish a structured `SCHEDULE_COLLISION` event to `mdm.anomaly.errors` via `POST /api/v1/mdm/report-unmapped`.

2. **MDM & Analyst Conflict Resolution APIs**:
   - In `igaming-mdm-service`:
     - `POST /api/v1/mdm/matches/{id}/cancel`: marks match as `CANCELLED` with audit notes.
     - `POST /api/v1/mdm/matches/{id}/promote-ground-truth`: clears conflict flag, sets source to `SOFASCORE` or verified canonical, links SofaScore ID.
   - In `igaming-ai-analyst`:
     - `POST /api/v1/ai-analyst/recommendations/{id}/resolve-conflict`: accepts `action` (`CANCEL` or `PROMOTE`) and triggers immediate ML re-evaluation on promotion.

3. **Autonomous Server-Side Robot (`igaming-anomaly-resolver-agent`)**:
   - New dedicated Spring Boot service in K8s (`igaming-enrichment` or `igaming-analytics` namespace).
   - `@KafkaListener` consuming `mdm.anomaly.errors`.
   - Automated Ground-Truth Resolver:
     - Queries SofaScore On-Demand API (`/api/v1/sofascore/on-demand/match/...`) and `sofascore_raw_events`.
     - Analyzes tournament legitimacy (detects placeholder synthetic leagues `League \d+`, `Fantasy League`, etc. vs canonical tournaments).
     - Resolves the collision: executes MDM & Analyst REST API calls to cancel the synthetic duel and promote/evaluate the true fixture.
   - Exposes `/actuator/health` and `/api/v1/agent/status` for monitoring and audits.

## Capabilities

### New Capabilities
- `anomaly-resolver-agent`: Autonomous server-side service consuming MDM error queues, performing ground-truth verification against SofaScore and the internet, and remediating anomalies via backend REST APIs.

### Modified Capabilities
- `ai-analyst`: Non-destructive schedule conflict quarantine; emits `SCHEDULE_COLLISION` anomaly; exposes `/resolve-conflict` API.
- `mdm-core`: Exposes match cancellation and ground-truth promotion endpoints for external/agent remediation.

## Impact

- Zero legitimate matches discarded due to arrival order race conditions.
- Synthetic and fantasy league cross-matches are automatically identified and cancelled at runtime.
- Seamless end-to-end integration across MDM, AI Analyst, Kafka, and SofaScore.
