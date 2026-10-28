# Tasks: Server-Side Anomaly-Resolver Agent & Conflict Quarantine Pipeline

## 1. AI Analyst Quarantine & Anomaly Emission

- [x] 1.1 In `AiAnalystOrchestrator.java`, replace unilateral match cancellation on team collision with quarantine (`SCHEDULE_CONFLICT_PENDING_RESOLUTION`) for both candidate matches
- [x] 1.2 In `AiAnalystOrchestrator.java`, publish `SCHEDULE_COLLISION` anomaly event to `mdm.anomaly.errors` via `AnomalyReportingService` or MDM REST endpoint
- [x] 1.3 Add `POST /api/v1/ai-analyst/recommendations/{id}/resolve-conflict` endpoint in `AiAnalystController.java` to support remote remediation (`action=CANCEL|PROMOTE`)
- [x] 1.4 Update UI (`index.html`) to display `⚠️ CONFLICT PENDING RESOLUTION` badge and status

## 2. MDM Service Match Quarantine Endpoints

- [x] 2.1 Add `POST /api/v1/mdm/matches/{id}/cancel` in `MdmMatchController.java`
- [x] 2.2 Add `POST /api/v1/mdm/matches/{id}/promote-ground-truth` in `MdmMatchController.java`
- [x] 2.3 Verify and test MDM endpoints

## 3. Server-Side Anomaly Resolver Microservice (`igaming-anomaly-resolver-agent`)

- [x] 3.1 Create Maven module `igaming-anomaly-resolver-agent` with `pom.xml`, `application.properties`, and main application class
- [x] 3.2 Implement `@KafkaListener` consuming topic `mdm.anomaly.errors` for `SCHEDULE_COLLISION` events
- [x] 3.3 Implement Ground-Truth Resolver Service evaluating SofaScore fixtures, tournament legitimacy, and bookmaker breadth
- [x] 3.4 Implement Remediation Client calling MDM and AI Analyst APIs to cancel the fake match and unblock/evaluate the genuine match
- [x] 3.5 Expose health and management REST endpoints (`/actuator/health`, `/api/v1/agent/status`, `/api/v1/agent/resolve-collision`)

## 4. Build, Deployment, and Verification

- [x] 4.1 Build Jib container images for `igaming-ai-analyst`, `igaming-mdm-service`, and `igaming-anomaly-resolver-agent`
- [x] 4.2 Create Kubernetes deployment manifest `igaming-anomaly-resolver-agent.yaml` and apply to cluster
- [x] 4.3 Verify all pods reach `Running 1/1` and observe 5-minute error-free watchdog
- [x] 4.4 Run end-to-end integration test with a simulated schedule collision and verify autonomous resolution
