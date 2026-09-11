## 1. Clean Up K8s Manifests & Fix Jib Entrypoints

- [x] 1.1 Remove `exec java -jar app.jar` command overrides from `igaming-k8s/betboom.ru.yaml`, `igaming-k8s/olimpbet.ru.yaml`, `igaming-k8s/olimpbet.kz.yaml`, `igaming-k8s/pinnacle.yaml`, `igaming-k8s/bwin.yaml`, `igaming-k8s/digitain.yaml`, `igaming-k8s/marathonbet.*.yaml`, `igaming-k8s/zenit.win.yaml`, etc.
- [x] 1.2 Align startup/liveness/readiness probes to `/actuator/health/readiness` and `/actuator/health/liveness` across modified manifests.
- [x] 1.3 Apply updated manifests into Kubernetes namespace `igaming-source`.

## 2. Activate High-Value Sources (Betboom, Pinnacle, Olimpbet)

- [x] 2.1 Verify `igaming-source-betboom-crawler` and `igaming-source-betboom-loader` start in `Running 2/2` status and ingest live/prematch events.
- [x] 2.2 Re-apply `igaming-k8s/pinnacle.yaml` and verify Pinnacle Guest API line ingestion into `odds_actual`.
- [x] 2.3 Re-apply `igaming-k8s/olimpbet.ru.yaml` and `igaming-k8s/olimpbet.kz.yaml`, verifying database readiness and loader execution.

## 3. Scale and Stabilize Marathonbet & Zenit Ingestion

- [x] 3.1 Re-apply `igaming-k8s/marathonbet.ru.yaml`, `marathonbet.com.yaml`, and `marathonbet.by.yaml` with proxy pool failover.
- [x] 3.2 Verify `igaming-source-zenit-crawler` prematch extraction populates matches in `match_cache`.

## 4. End-to-End Verification & Definition of Done

- [x] 4.1 Verify total odds in `odds_actual` and match records in `match_record` scale up.
- [x] 4.2 Validate cross-bookmaker overlaps and active surebets in `surebet_alert`.
- [x] 4.3 Execute 5-minute continuous runtime verification in K8s without restarts.
