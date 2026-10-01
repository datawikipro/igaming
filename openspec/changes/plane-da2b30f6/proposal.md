# Proposal: #37: [bcgame] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
- Plane Task ID: `da2b30f6-ee19-4328-b6cb-d515ddb2a9ad`
- Sequence ID: 37
- Module: `igaming-source-bcgame` (BC.Game crypto sportsbook integration)

## Description
Проведение комплексного ООП-рефакторинга модуля маппинга коэффициентов BC.Game (`igaming-source-bcgame`).
Переход от устаревшего процедурного сопоставления исходов к гибкой компонентной архитектуре на основе паттерна Chain of Responsibility / Strategy (`BcgameMarketHandler`) с полной поддержкой всех ключевых категорий рынков:
1. **Расширенная роспись исходов**:
   - `BcgameResultMarketHandler`: 1X2, Moneyline, Двойной шанс (1X, 12, X2), Ничья исключена (Draw No Bet) для матча и таймов.
   - `BcgameBttsHandler`: Обе забьют (Both Teams to Score - YES / NO) для матча и периодов.
   - `BcgameCorrectScoreHandler`: Точный счет матча и таймов.
2. **Маркеты спортивной статистики**:
   - `BcgameStatsMarketHandler`: Угловые (`StatType.CORNERS`), желтые карточки (`StatType.YELLOW_CARDS`), карточки (`StatType.CARDS`), фолы (`StatType.FOULS`), офсайды (`StatType.OFFSIDES`), удары в створ (`StatType.SHOTS_ON_TARGET`). Поддержка исходов 1X2, тоталов и фор как на весь матч, так и по таймам/периодам.
3. **Киберспортивные маркеты**:
   - `BcgameEsportsMarketHandler`: Полноценная поддержка киберспортивных дисциплин (CS2, Dota 2, League of Legends, Valorant, Rainbow Six) с нормализацией победителей карт (`BetScope.MAP_1..MAP_7`), тоталов/фор карт (`StatType.MAPS`), раундов (`StatType.ROUNDS`), тоталов убийств (`StatType.KILLS`), First Blood (`BinaryMarketBet.MarketType.FIRST_BLOOD`), разрушения башен (`TOWERS`) и Рошана (`ROSHAN`).
4. **Базовые маркеты**:
   - `BcgameTotalMarketHandler`: Общие и индивидуальные тоталы команд для матча и таймов.
   - `BcgameHandicapMarketHandler`: Азиатские и европейские форы матча и периодов.

## Impact & Capabilities
- **Модульность**: Изолированные обработчики позволяют независимо расширять логику сопоставления без риска регрессии.
- **Стандартизация**: Котировки приводятся к канонической модели `igaming-dto` с точным определением `BetScope` и `BetSubject`.
- **Готовность к масштабированию**: Корректная обработка структуры данных BC.Game / Betby для надежного наполнения линии (`match_cache >= 500`).
