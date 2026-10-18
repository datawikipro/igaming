# Tasks: SofaScore Team Signature Normalization

- [x] 1. Create `TeamSignatureNormalizer` in `igaming-dto` with gender, age, and tier parsing from team names and tournament context <!-- id: 1.1 -->
- [x] 2. Update `SimilarityUtils` in `igaming-dto` and `igaming-capture-sofascore` to enforce gender/age parity and compare clean core names <!-- id: 1.2 -->
- [x] 3. Write comprehensive unit tests in `igaming-dto` covering women's teams, youth teams, and tournament context <!-- id: 1.3 -->
- [x] 4. Integrate tournament-aware matching and canonicalization into `SofaScoreFixtureProvider`, `SofaScoreResultProvider`, `SofaScoreEnrichmentProvider`, and `SofaCrawlerService` <!-- id: 1.4 -->
- [x] 5. Run full test suites across `igaming-dto` and `igaming-capture-sofascore` <!-- id: 1.5 -->
- [x] 6. Build container image via Jib and deploy to Kubernetes cluster <!-- id: 1.6 -->
