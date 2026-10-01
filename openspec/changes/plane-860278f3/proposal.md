# Proposal: [wplay] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `860278f3-3300-4f68-97bb-2bf41f77e0a5`
Bookmaker: Wplay Colombia (`igaming-source-wplay`, лицензированный колумбийский оператор на платформе Playtech)

## Description
Комплексный ООП-рефакторинг слоя нормализации и маппинга котировок модуля `igaming-source-wplay`:
1. Переход от монолитного парсера к расширяемой компонентной архитектуре обработчиков рынков (`WplayMarketHandler`) с внедрением контекста (`WplayMarketContext`) и поддержкой видов спорта (`SportType`).
2. Создание унифицированных DTO-моделей (`WplayEventDto`, `WplayMarketDto`, `WplayOutcomeDto`) для стандартизации обработки рынков и исходов.
3. Расширение росписи исходов:
   - Основные исходы (1X2, Moneyline, таймы/периоды) в `WplayMatchResultHandler`;
   - Двойной шанс (1X, 12, X2 матча и таймов) в `WplayDoubleChanceHandler`;
   - Обе забьют (BTTS Yes/No) в `WplayBttsHandler`;
   - Ничья нет ставки (DNB / Empate no acción с Handicap 0.0) в `WplayDrawNoBetHandler`;
   - Тоталы (Over/Under матча, таймов и индивидуальные тоталы) в `WplayTotalHandler`;
   - Форы (европейские и азиатские форы) в `WplayHandicapHandler`;
   - Точный счет (Correct score / Marcador correcto) в `WplayCorrectScoreHandler`;
   - Тайм/Матч (Half Time / Full Time / Descanso/Final) в `WplayPeriodHandler`.
4. Поддержка статистических маркеров (исходы 1X2, тоталы, форы):
   - `WplayStatsCornersHandler` — угловые с `StatType.CORNERS`;
   - `WplayStatsCardsHandler` — карточки и желтые карточки с `StatType.YELLOW_CARDS`.
5. Поддержка киберспорта (`WplayEsportsHandler`, CS2, Dota 2, League of Legends, Valorant):
   - Победители карт (`BetScope.MAP_1` .. `BetScope.MAP_7`);
   - Тоталы и форы карт (`StatType.MAPS`);
   - Тоталы и форы раундов (`StatType.ROUNDS`);
   - Убийства (`StatType.KILLS`) и First Blood (`StatType.FIRST_BLOOD`).
6. Интеграция цепочки обработчиков в `WplayOddsMapper` с сохранением обратной совместимости с HTML-парсером Wplay.
7. Разработка всестороннего набора модульных тестов и валидация сборки.
