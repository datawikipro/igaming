# Proposal: #40: [smarkets] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
- Plane Task ID: `51b736fd-84c4-4203-a18e-14cbfb294613`
- Sequence ID: 40
- Module: `igaming-source-smarkets` (Smarkets European Betting Exchange P2P Orderbook Source Integration Service)

## Description
Проведение комплексного ООП-рефакторинга модуля маппинга котировок биржи ставок Smarkets (`igaming-source-smarkets`).
Переход от монолитного сопоставления исходов в `SmarketsOddsMapper` к компонентной масштабируемой архитектуре на основе паттерна Chain of Responsibility / Strategy (`SmarketsMarketHandler`) с разделением по типам рынков:

1. **Базовые рынки и основная роспись**:
   - `SmarketsMatchResultHandler`: 1X2, Moneyline, 3-Way и 2-Way победители матча.
   - `SmarketsDoubleChanceHandler`: Двойной шанс (1X, 12, X2).
   - `SmarketsDrawNoBetHandler`: Ничья нет ставок (Draw No Bet / Handicap 0.0).
   - `SmarketsTotalHandler`: Тоталы голов/очков (Over/Under) с извлечением числового параметра из названия контракта, рынка или `market_type.param`.
   - `SmarketsHandicapHandler`: Азиатские и европейские гандикапы/форы.
   - `SmarketsBttsHandler`: Обе забьют (Both Teams to Score - YES / NO).
   - `SmarketsCorrectScoreHandler`: Точный счет матча (Correct Score).

2. **Статистические маркеты**:
   - `SmarketsCornersHandler`: Угловые (`StatType.CORNERS`) — тоталы матча и команд, форы по угловым, исходы 1X2.
   - `SmarketsCardsHandler`: Желтые и красные карточки (`StatType.YELLOW_CARDS`, `StatType.CARDS`) — тоталы, форы, удаление/красная карточка (Да/Нет).

3. **Киберспортивные дисциплины**:
   - `SmarketsEsportsHandler`: CS2, Dota 2, League of Legends, Valorant — победа на карте (`BetScope.MAP_1..MAP_7`), тоталы/форы карт (`StatType.MAPS`), раунды (`StatType.ROUNDS`), убийства (`StatType.KILLS`), First Blood (`FIRST_BLOOD`).

4. **Периоды и таймы**:
   - `SmarketsPeriodHandler`: 1X2, тоталы и форы 1-го и 2-го таймов, периодов и четвертей (`BetScope.HALF_1`, `HALF_2`, `PERIOD_1..PERIOD_4`).
   - `SmarketsHalfTimeFullTimeHandler`: Рынки Тайм/Матч (HT/FT).

## Impact & Capabilities
- **Модульность и расширяемость**: Изоляция каждого семейства рынков в отдельном Spring-компоненте со строгим контрактом `SmarketsMarketHandler`.
- **Строгая типизация**: Полная интеграция с канонической иерархией `pro.datawiki.igaming.dto.market.*` (`BetScope`, `BetSubject`, `StatType`).
- **Соблюдение регламентов**: Неблокирующий старт HikariCP, Actuator probes, кластерный HTTP-прокси, сохранение устойчивости парсинга котировок из стакана P2P заявок Smarkets.
