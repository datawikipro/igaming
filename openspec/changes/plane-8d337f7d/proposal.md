# Proposal: [888sport] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `8d337f7d`
Module: `igaming-source-888sport` (Kambi provider integration)

## Description
Проведение ООП-рефакторинга модуля маппинга коэффициентов 888sport (провайдер Kambi). Замена монолитного процедурного маппинга на расширяемую цепочку обработчиков `Sport888MarketHandler`, реализующую паттерн Chain of Responsibility / Strategy для обработки всех типов рынков:
- Основные исходы (1X2, Moneyline, исходы по таймам/периодам) через `Sport888MatchResultHandler`.
- Роспись исходов (Totals, Handicaps, Double Chance, Draw No Bet, BTTS, Correct Score).
- Статистические маркеты (Угловые и Желтые карточки / Cards) с поддержкой тоталов, фор и 1X2 через `Sport888StatsHandler`.
- Киберспортивные маркеты (CS2, Dota2, LoL и др.) с поддержкой победителей карт, тоталов карт, раундов, форы по раундам, тоталов убийств и First Blood через `Sport888EsportsHandler`.

## Impact & Capabilities
- **Модульность и расширяемость**: Добавление новых рынков не требует изменения основного маппера — достаточно реализовать интерфейс `Sport888MarketHandler` с соответствующей аннотацией `@Order`.
- **Поддержка статистики**: Парсинг угловых и карточек в стандартизированные JPA-структуры `match_factor` с указанием `StatType.CORNERS` и `StatType.YELLOW_CARDS`.
- **Поддержка киберспорта**: Автоматическое распознавание киберспортивных дисциплин и нормализация маркеров `StatType.MAPS`, `StatType.ROUNDS`, `StatType.KILLS`, `BinaryMarketBet.MarketType.FIRST_BLOOD`.
- **Отказоустойчивость**: Сохранение обратной совместимости через fallback на `BetTypeResolverService` и аудит неизвестных маркетов в `UnmappedBetService`.
