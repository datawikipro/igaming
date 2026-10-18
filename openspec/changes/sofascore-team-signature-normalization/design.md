# Design: SofaScore Team Signature Normalization

## Architecture
```
                  ┌──────────────────────────────────────────────────┐
                  │              Bookmaker Odds Stream               │
                  │              e.g. "Челси (жен)"                  │
                  └─────────────────────────┬────────────────────────┘
                                            │
                                            ▼
                  ┌──────────────────────────────────────────────────┐
                  │           ClubTeamSignatureParser                │
                  │   -> Core: "челси", Gender: FEMALE, Age: MAIN    │
                  │   -> Canonical: "Челси (ж)"                      │
                  └─────────────────────────┬────────────────────────┘
                                            │
                                            ▼
┌───────────────────────────────────────────┴───────────────────────────────────────────┐
│                          SimilarityUtils.areTeamsSimilar                              │
│   1. Parse Signatures (Team Name + Tournament Context)                                │
│   2. Strict Guard: GenderParity && AgeParity && TierParity                            │
│   3. Clean Core Name Matching (Transliteration + JaroWinkler >= 0.85)                 │
└───────────────────────────────────────────▲───────────────────────────────────────────┘
                                            │
                  ┌─────────────────────────┴────────────────────────┐
                  │           TeamSignatureNormalizer                │
                  │   -> Core: "chelsea", Gender: FEMALE, Age: MAIN  │
                  │   -> Canonical: "Chelsea (ж)"                    │
                  └─────────────────────────▲────────────────────────┘
                                            │
                  ┌─────────────────────────┴────────────────────────┐
                  │             SofaScore Raw JSON Event             │
                  │   homeTeam: "Chelsea FC Women" (or "Barcelona")  │
                  │   tournament: "Women's Super League"             │
                  └──────────────────────────────────────────────────┘
```

## Decisions
1. **Zero-Dependency Signature Normalizer in `igaming-dto`**:
   - Class `pro.datawiki.igaming.dto.signature.TeamSignatureNormalizer`.
   - Evaluates:
     - Gender: `FEMALE`, `MALE`
     - AgeCategory: `MAIN`, `U17`..`U23`, `YOUTH`
     - TeamTier: `MAIN`, `RESERVE_2`, `RESERVE_3`, `YOUTH`
   - Strips noise prefixes (`FC`, `WFC`, `FK`, `Club`, etc.) and gender/age markers.
   - Extracts gender and age from tournament and category if absent in the team name.
2. **Strict Guarded Matching in `SimilarityUtils`**:
   - If `gender1 != gender2`, return `false` immediately.
   - If `age1 != age2`, return `false` immediately.
   - If `tier1 != tier2`, return `false` immediately.
   - Core names stripped of gender/age tokens are compared with cross-language phonetic transliteration and Jaro-Winkler.
3. **Canonicalization at Ingestion**:
   - `SofaScoreFixtureProvider` sets `team1Name` and `team2Name` in `ReferenceFixtureDto` to canonical display names.
   - `SofaRawEventDto` stores canonical names so downstream services (`TeamDnaEngineService`, `sofascore_raw_events`) match the canonical `Team` records in PostgreSQL.
