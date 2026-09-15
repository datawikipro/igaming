## Why

Латинская Америка (LatAm) — наиболее быстрорастущий рынок спортивного беттинга в мире (легализация Бразилии с оборотом $20–30 млрд, Мексика, Колумбия), а европейские биржи ставок (Betdaq, Matchbook) предоставляют фундаментальную ликвидность с минимальной маржой.
Интеграция ведущих букмекеров LatAm (Betano, Caliente, Wplay, BetPlay) и Европы (Unibet, Tipico, Betdaq) в SmartBet.guru открывает глобальный кросс-континентальный арбитраж и активирует многоканальную монетизацию: CPA-партнерки за депозиты ($30–$150/игрок), продажу B2B Odds API и инструмент Matched Betting.

## What Changes

- **Маршрутизация через ru-proxy (outline-fi)**: Интеграция 22 международных доменов LatAm, Европы, Африки и крипто-букмекеров через шлюз в Нидерландах с TLS Client Hello префиксом.
- **Подключение флагманов Латинской Америки**:
  - `igaming-source-betano` — адаптер к крупнейшему букмекеру Бразилии (Kaizen Gaming).
  - `igaming-source-caliente` и `igaming-source-wplay` — единый модуль для платформы Playtech Sportsbook (Мексика и Колумбия).
  - `igaming-source-betplay` и `igaming-source-rushbet` — подключение колумбийских операторов через Kambi Offering API.
- **Подключение европейских лидеров и бирж**:
  - `igaming-source-unibet` — общеевропейский Kambi-флагман.
  - `igaming-source-tipico` — лидер рынка Германии и Австрии.
  - `igaming-source-betdaq` — британско-ирландская биржа ставок (стакан Back/Lay).
- **Подключение африканского и крипто-сегмента**:
  - `igaming-source-sportybet` — крупнейший оператор Нигерии и Кении.
  - `igaming-source-bcgame` — крипто-букмекер со скоростными котировками.
- **Монетизационный инструментарий**:
  - Внедрение SubID/CPA реферальных ссылок в интерфейс арбитражных вилок.
  - Проектирование B2B Data Feeds API в `igaming-portal`.

## Capabilities

### Modified Capabilities
- `crawler-engine`: Поддержка платформы Playtech (web_nr API), Kambi LatAm каналов, Kaizen Gaming API и биржевого стакана Betdaq.
- `portal-gateway`: Поддержка B2B Odds API ключей и генерации реферальных партнерских ссылок с отслеживанием переходов.

## Impact

- **Новые сервисы**: `igaming-source-betano`, `igaming-source-caliente`, `igaming-source-unibet`, `igaming-source-betdaq`, `igaming-source-sportybet`.
- **Базы данных**: Выделение PostgreSQL StatefulSets с лимитами 512Mi.
- **Агрегатор**: Приток более 10,000 активных матчей по южноамериканскому футболу (Brasileirao, Copa Libertadores, Liga MX) и европейским чемпионатам.
