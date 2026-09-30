# Proposal: [sbobet] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `c6868efa-ef3e-4b70-8024-d59dfe47c2e3`
Module: `igaming-source-sbobet` (SBOBET bookmaker integration)

## Description
Проведение полного ООП-рефакторинга модуля маппинга коэффициентов SBOBET (`igaming-source-sbobet`).
Замена процедурных фрагментов и расширение цепочки обработчиков `SbobetMarketHandler` с использованием паттерна Chain of Responsibility / Strategy для комплексной поддержки всех категорий спортивных рынков:
- **Роспись исходов**:
  - `SbobetDoubleChanceHandler`: Двойной шанс (1X, 12, X2) для матча и 1-го тайма.
  - `SbobetDrawNoBetHandler`: Ничья исключена (WIN1_2WAY, WIN2_2WAY).
  - `SbobetBttsHandler`: Обе забьют (Both Teams To Score - YES / NO).
  - `SbobetCorrectScoreHandler`: Точный счет матча и тайма (1-0, 2-1, ..., Any Other Score).
- **Статистические маркеты**:
  - `SbobetStatsHandler`: Угловые (`StatType.CORNERS`) и желтые карточки (`StatType.YELLOW_CARDS` / `StatType.CARDS`), включая исходы 1X2, тоталы и форы как на весь матч, так и по таймам.
- **Киберспортивные маркеты**:
  - `SbobetEsportsHandler`: Поддержка киберспорта (CS2, Dota 2, LoL, Valorant и др.) с нормализацией победителей карт (`PERIOD_1..N`), тоталов и фор по картам (`StatType.MAPS`), раундов (`StatType.ROUNDS`), тоталов убийств (`StatType.KILLS`) и First Blood (`BinaryMarketBet.MarketType.FIRST_BLOOD`).
- **Основные маркеты** (сохранение и упорядочивание через `@Order`):
  - `SbobetTotalHandler`: Тоталы матча и таймов.
  - `SbobetHandicapHandler`: Азиатские и европейские форы матча и таймов.
  - `SbobetMoneylineHandler`: 1X2 и Moneyline матча и таймов.

## Impact & Capabilities
- **Модульность и расширяемость**: Добавление новых типов маркетов SBOBET инкапсулируется в отдельные обработчики без изменения ядра маппера.
- **Поддержка статистики**: Парсинг угловых и карточек в стандартизированные JPA-структуры `match_factor` с указанием `StatType.CORNERS` и `StatType.YELLOW_CARDS`.
- **Поддержка киберспорта**: Автоматическое распознавание киберспортивных дисциплин и нормализация маркеров `StatType.MAPS`, `StatType.ROUNDS`, `StatType.KILLS`, `BinaryMarketBet.MarketType.FIRST_BLOOD`.
- **Высокая точность сопоставления**: Корректное определение `BetScope` (FULL_MATCH, HALF_1, HALF_2, PERIOD_1..5) на основе структуры ключей и метаданных SBOBET.
