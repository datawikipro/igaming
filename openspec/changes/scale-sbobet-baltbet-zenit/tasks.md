## 1. SBOBET Optimization
- [x] 1.1 Add Early Market `?od=all` and expand `SPORT_URL_SEGMENTS` in `SbobetApiClient.java`
- [x] 1.2 Expand `TARGET_SPORTS` in `MatchService.java` to cover 17 sports

## 2. Baltbet Optimization
- [x] 2.1 Implement `extractEvents` in `BaltbetEventDiscoverer.java`
- [x] 2.2 Implement 25-worker parallel batch enrichment and `teamNameCache` in `AbstractBaltbetFamilyService.java`

## 3. Zenit Optimization
- [x] 3.1 Optimize `ZenitApiClient.java` for resilient HTTP GET and non-blocking session warming
- [x] 3.2 Reduce prematch and live polling intervals in `zenit.win.yaml`

## 4. Build, Deploy & Verify
- [ ] 4.1 Compile and build Jib container images for Sbobet, Baltbet, and Zenit
- [ ] 4.2 Apply K8s manifests and rollout restart deployments
- [ ] 4.3 Verify match scaling in `odds_actual` in aggregator database
- [ ] 4.4 Run 5-minute DoD validation
