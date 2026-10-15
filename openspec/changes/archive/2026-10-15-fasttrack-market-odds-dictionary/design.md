# Design: FastTrack In-Memory Market and Odds Dictionary Optimization

## Architecture & Data Flow

```
                      Raw Odds Ingestion (CommonNormalizationService.findOddsType)
                                               │
                                 ┌─────────────┴─────────────┐
                                 │ In-memory Cache / Alias?  │
                                 └─────────────┬─────────────┘
                                        YES    │    NO
                             ┌─────────────────┘    │
                             │                      ▼
                             │       ┌───────────────────────────────┐
                             │       │ FastTrackDictionary /         │
                             │       │ BetTypeRegistry.resolve()     │
                             │       └──────────────┬────────────────┘
                             │             MATCH    │    NO MATCH
                             │       ┌──────────────┘    │
                             ▼       ▼                   ▼
                      Return Cached / Canonical     Create Temp OddsType &
                      OddsType (Sub-millisecond)   Queue Background LLM Normalization
```

## Implementation Details

1. **FastTrackDictionary.java**:
   - `SPORT_LOOKUP`: Static `Map<String, SportType>` initialized with lower-cased keys for $O(1)$ lookup.
   - `EXACT_ODDS_MAPPING`: Static `Map<String, String>` initialized with upper-cased keys for instant $O(1)$ lookups.
   - Static pre-compiled `Pattern` instances:
     - `LEON_HANDICAP_PATTERN` (`^[12X] \\(.+\\)$`)
     - `BETB2B_TM_PATTERN` (`^TM\\(.+\\)$`)
     - `BETB2B_TL_PATTERN` (`^TL\\(.+\\)$`)
     - `BETB2B_HM1_PATTERN` (`^HM1\\(.+\\)$`)
     - `BETB2B_HL2_PATTERN` (`^HL2\\(.+\\)$`)
   - Outcome codes normalized to canonical `BetType` codes:
     - `WIN1`, `WIN2`, `DRAW`
     - `DC_1X`, `DC_12`, `DC_X2`
     - `BTTS_YES`, `BTTS_NO`
     - `TOTAL_OVER`, `TOTAL_UNDER`
     - `HANDICAP_1`, `HANDICAP_2`

2. **CommonNormalizationService.java**:
   - Inject `FastTrackDictionary` into constructor.
   - In `findOddsType`:
     - If alias is absent from cache and DB, call `fastTrackDictionary.resolveOddsAlias(rawName)`.
     - When resolved to a valid `BetType` or standard code, resolve/create `Market` and `OddsType` synchronously, persist alias via `oddsTypeAliasRepository.upsertAlias`, and put into `oddsTypeByNameCache`.
     - Avoid creating temporary `TEMP_...` records or queueing LLM requests when fast-track succeeds.

3. **BetTypeRegistry.java**:
   - Extend `resolve(String rawName)` with double chance (`1X`, `12`, `X2`, `DC1X`, etc.) and BTTS (`BTTS_YES`, `BTTS_NO`, `GG`, `NG`, etc.).
   - Support `DOUBLE_CHANCE_1X`, `DOUBLE_CHANCE_12`, `DOUBLE_CHANCE_X2` in `parseDynamic` as aliases to `DC_1X`, `DC_12`, `DC_X2`.
