## 1. BetB2B Multi-Sport Line Expansion

- [x] 1.1 Update `AbstractBetB2BFamilyApiClient.java` to fetch `LineFeed/GetSports_Zip` and extract all active sport IDs
- [x] 1.2 Implement multi-sport batching in `fetchLine()` passing combined `sports=...` parameters or querying major sports
- [x] 1.3 Verify parsing and normalization of expanded BetB2B multi-sport payload in `XbetFamilyEventDiscoverer`

## 2. Marathonbet Category Optimization

- [x] 2.1 Refactor `MarathonbetApiClient` to utilize bulk category requests or direct HTTP JSON endpoints
- [x] 2.2 Optimize `MarathonDiscoveryService` to avoid per-league browser tab opening and reduce scan time to under 120s

## 3. Zenit Session & Timeline Handling

- [x] 3.1 Strengthen `ZenitApiClient` pre-flight imprint cookie acquisition and retry mechanism
- [x] 3.2 Verify full prematch timeline and tournament discovery parameters in Zenit line requests

## 4. Build, Deployment & Verification

- [x] 4.1 Compile updated modules with Maven Jib (`igaming-source-betb2b`, `igaming-source-marathonbet`, `igaming-source-zenit`)
- [x] 4.2 Deploy updated images to Kubernetes namespace `igaming-source`
- [x] 4.3 Verify match counts across BetB2B (>= 1,000–3,000+), Marathonbet (>= 500+), and Zenit (>= 500+) in `odds_actual`
- [x] 4.4 Run 5-minute error-free validation in cluster
