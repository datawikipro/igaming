# Proposal: FastTrack In-Memory Market and Odds Dictionary Optimization

## Problem
In the odds ingestion pipeline (`CommonNormalizationService.findOddsType`), raw odds that arrive without an existing alias in `odds_type_alias` fall through to temporary database records creation and background LLM normalization queueing. Although `FastTrackDictionary` was created to quickly map common sport and odds aliases, it was only invoked downstream by `NormalizationWorker` and `LlmNormalizerService`, rather than directly in the synchronous ingestion path of `CommonNormalizationService.findOddsType`.
Furthermore, `FastTrackDictionary` utilized linear $O(N)$ searches over static maps on every invocation and had limited coverage of ubiquitous betting outcomes (such as Italian/Balkan GG/NG, explicit Double Chance variations, Both Teams To Score variations, and per-call regex compilation overhead).

## Solution
1. **Synchronous FastTrack Resolution in Ingestion**: Integrate `FastTrackDictionary.resolveOddsAlias` into `CommonNormalizationService.findOddsType` before generating temporary records, automatically creating canonical aliases and returning cached `OddsType` in sub-millisecond memory lookups.
2. **O(1) Map Lookups & Pre-compiled Regex**: Refactor `FastTrackDictionary` to use pre-lowercased/pre-uppercased static hash maps for $O(1)$ lookups, and pre-compile regular expressions (`Pattern`) to eliminate per-call regex compilation overhead.
3. **Comprehensive Odds Vocabulary**: Expand `FastTrackDictionary` with standard outcomes across European, CIS, and international sportsbooks:
   - 1X2 outcomes: "1", "2", "X", "Win1", "Win2", "Home", "Away", "П1", "П2", "Ничья", "Tie"
   - Double Chance: "1X", "12", "X2", "1Х", "Х2", "1-X", "1-2", "X-2", "1 OR X", "1 OR 2", "X OR 2", "DC_1X", "DC_12", "DC_X2"
   - Both Teams To Score (BTTS): "BTTS_YES", "BTTS_NO", "GG", "NG", "Обе забьют - Да", "Обе забьют - Нет", "ОЗ - Да", "ОЗ - Нет", "ОЗ (Да)", "ОЗ (Нет)"
   - Totals & Handicaps: "ТБ", "ТМ", "Total Over", "Total Under", "Ф1", "Ф2", "Handicap 1", "Handicap 2", "H1", "H2", "AH 1", "AH 2"
4. **Enhanced BetTypeRegistry Resolution**: Support double chance and BTTS resolution in `BetTypeRegistry.resolve()`.
5. **Unit Tests**: Implement comprehensive test suites in `FastTrackDictionaryTest.java` verifying all fast-track paths.
