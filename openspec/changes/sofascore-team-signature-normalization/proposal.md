# Proposal: SofaScore Team Signature Normalization (Women & Youth Disambiguation)

## Why
When teams are ingested from bookmakers, team names pass through `ClubTeamSignatureParser` which extracts `Gender.FEMALE`, `AgeCategory (U17..U23, YOUTH)`, and `TeamTier (RESERVE)`. This creates distinct canonical entities in the database such as `«Челси (ж)»` or `«Спартак U19»`.
However, `igaming-capture-sofascore` took raw team strings from SofaScore (`"Chelsea FC Women"` or just `"Chelsea"` when the tournament is `"Women's Super League"`).
The existing `SimilarityUtils` stripped all punctuation and appended suffixes into un-transliterable tokens (`"челсиж"` vs `"chelseafcwomen"`), causing Jaro-Winkler similarity to drop below threshold (0.76 < 0.85). Consequently, SofaScore events for women's and youth leagues were never matched, resulting in 0 H2H, 0 lineups, 0 DNA history, and empty team cards on the frontend.

## What Changes
1. **`TeamSignatureNormalizer` in `igaming-dto`**:
   - Universal, zero-dependency parser for `Gender`, `AgeCategory`, `TeamTier`, and core club name.
   - Extracts gender and age both from team names (e.g. `(ж)`, `(w)`, `Women`, `Fem`, `U19`, `до 19`) and from tournament/category context (e.g. `Women's Super League`, `Frauen-Bundesliga`, `UEFA Youth League`, `Category: Women`).
   - Produces canonical display names matching `ClubTeamSignature.toCanonicalDisplayName()`.
2. **Context-Aware `SimilarityUtils`**:
   - Supports `areTeamsSimilar(name1, name2, context1, context2)`.
   - Strictly enforces gender and age category parity: women's teams never match men's teams, and youth teams never match senior teams.
   - Compares cleaned core names with diacritic normalization, phonetic transliteration, substring containment, and Jaro-Winkler.
3. **Capture & Ingestion Integration in `igaming-capture-sofascore`**:
   - `SofaScoreFixtureProvider`, `SofaScoreResultProvider`, `SofaScoreEnrichmentProvider`, and `SofaCrawlerService` pass tournament and category context to `TeamSignatureNormalizer` and `SimilarityUtils`.
   - Reference fixtures and raw events are stored with canonicalized team names.
