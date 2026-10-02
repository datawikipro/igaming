# Handover State: #ed9da028
- **Migrated From**: plane-ai-worker-2 (developer.usa.test4@gmail.com)
- **Timestamp**: 2026-10-02T01:52:27.127420
- **Target Branch**: feature/plane-ed9da028
- **Reason**: Gemini Quota Depletion on worker pod plane-ai-worker-2
- **Remaining Tasks**:
# Implementation Tasks: [apuestatotal] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов
- [x] 1. Изучить текущий модуль и подготовить структуру классов
- [x] 2. Реализовать основную бизнес-логику и маппинги данных
  - [x] 2.1 Реализовать обработчики росписи исходов (ApuestatotalMatchResultHandler, ApuestatotalDoubleChanceHandler, ApuestatotalBttsHandler, ApuestatotalTotalHandler, ApuestatotalHandicapHandler)
  - [x] 2.2 Реализовать статистические мапперы: ApuestatotalStatsCornersHandler (угловые) и ApuestatotalStatsCardsHandler (ЖК)
    - [x] 2.2.1 Реализовать ApuestatotalStatsCornersHandler (угловые 1X2, тоталы Over/Under, форы с StatType.CORNERS)
    - [x] 2.2.2 Реализовать ApuestatotalStatsCardsHandler (ЖК/карточки 1X2, тоталы Over/Under, форы с StatType.YELLOW_CARDS)
  - [x] 2.3 Реализовать киберспортивный маппер ApuestatotalEsportsHandler (CS2/Dota2/LoL/Valorant: карты, раунды, форы, тоталы)
    - [x] 2.3.1 Реализовать победителей карт (BetScope.MAP_1..MAP_5, StatType.MATCH)
    - [x] 2.3.2 Реализовать тоталы и форы по картам (StatType.MAPS) и раундам (StatType.ROUNDS)
  - [x] 2.4 Интегрировать хэндлеры в ApuestatotalOddsMapper с наследованием AbstractBetTypeMapper и поддержкой SportType
    - [x] 2.4.1 Реализовать внедрение и сортировку хэндлеров по @Order с fallback на дефолтный список
    - [x] 2.4.2 Обеспечить маппинг в mapToOddsUpdateRequest и mapStakeGroup с контекстом SportType и MatchCache
  - [x] 2.5 Разработать комплексные unit-тесты в ApuestatotalOddsMapperTest (основные исходы, статистика, киберспорт, edge cases)
    - [x] 2.5.1 Тесты основных рынков (1X2, Moneyline, таймы/периоды, тоталы, форы)
    - [x] 2.5.2 Тесты росписи и статистики (Double Chance, BTTS, Corners, Yellow Cards)
    - [x] 2.5.3 Тесты киберспорта с SportType (CS2, Dota2, LoL, Valorant: победители карт, тоталы и форы карт/раундов)
    - [x] 2.5.4 Тесты граничных случаев (null/empty payload, неподдерживаемые маркеты, некорректные коэффициенты)
- [ ] 3. Валидировать OpenSpec и проверить сборку/тесты
  - [x] 3.1 Валидация спецификаций openspec validate --specs (проверено: 12 спецификаций openspec/specs, skip_specs: true в .openspec.yaml)
  - [ ] 3.2 Запуск полной сборки Maven и проверка всех unit-тестов
    - [x] 3.2.1 Компиляция модуля igaming-source-apuestatotal и зависимостей (clean compile и test-compile: 28 классов + 6 тестовых успешно скомпилированы)
    - [ ] 3.2.2 Выполнение 97 unit-тестов мапперов
      - [x] 3.2.2.1 Выполнение тестов росписи исходов (ApuestatotalMatchResultAndDoubleChanceHandlerTest, ApuestatotalBttsHandlerTest, ApuestatotalTotalAndHandicapHandlerTest: 29 тестов успешно выполнены)
      - [ ] 3.2.2.2 Выполнение тестов статистики и киберспорта (43 теста)
        - [ ] 3.2.2.2.1 Выполнение тестов статистики угловых и ЖК (ApuestatotalStatsCornersAndCardsHandlerTest: 25 тестов)
        - [ ] 3.2.2.2.2 Выполнение тестов киберспортивных рынков (ApuestatotalEsportsHandlerTest: 18 тестов)
      - [ ] 3.2.2.3 Выполнение комплексного набора ApuestatotalOddsMapperTest (25 тестов)


## Instructions for incoming worker:
1. Pull branch `feature/plane-ed9da028`.
2. Read `/workspace/repo/openspec/changes/plane-ed9da028/tasks.md`.
3. Continue from the next unchecked item (`- [ ]`).
4. Validate changes and submit final PR.
