## Why

Интеграция американского сегмента спортивного беттинга (US Sportsbooks, P2P Exchanges, DFS Sweepstakes) открывает фундаментальную возможность построения «Золотого треугольника арбитража» (США — Европа — Азия) для экосистемы SmartBet.guru. 
Американские биржи ставок с околонулевой маржой (Sporttrade, Novig, ProphetX) и открытый REST API гиганта Bovada дают непрерывный поток высоколиквидных вилок от 3% до 15%+ и валуйных сигналов (+EV) против инертных линий рекреационных букмекеров (Bovada, DraftKings, BetRivers, а также РФ-букмекеров).

## What Changes

- **Американский сетевой egress через ru-proxy**: Централизованная маршрутизация 30+ американских платформ через US Gateway (`outline-us` в Айове, США, `34.9.20.96`) с последующим подключением US Residential Egress для чувствительных к Cloudflare IP Intelligence сервисов.
- **Подключение первой волны легких загрузчиков (профиль `BASIC`)**:
  - Создание микросервиса `igaming-source-bovada` на базе открытого REST API Bovada (4.78 МБ котировок за запрос, 0 МБ накладных расходов на Chromium).
  - Создание загрузчиков для Kambi-сегмента США (`igaming-source-betrivers`, `igaming-source-ballybet`).
  - Создание загрузчиков для шарпов Лас-Вегаса (`igaming-source-circasports`, `igaming-source-bookmaker`).
- **Подключение американских бирж ставок (Orderbook Ingestion)**:
  - Создание модулей сбора стаканов заявок (Orderbook / P2P Back & Lay) для `Sporttrade`, `ProphetX` и `Novig`.
- **Подключение второй волны (профиль `HEADLESS_STEALTH`)**:
  - Создание Playwright-краулеров для `FanDuel`, `BetOnline`, `Caesars` и `ESPN BET`.
- **Поддержка микро-рынков и Player Props (DFS Pick'em)**:
  - Интеграция мини-маркетов (показатели игроков: тоталы очков, передач, ярдов) и микро-беттинга (in-play fast markets).
- **Парсинг глобальных азиатских брокеров (OrbitX / Betfair White Label, BetInAsia)**:
  - Единая точка входа для сравнения американских линий с мировым эталоном Betfair/Pinnacle.

## Capabilities

### Modified Capabilities
- `crawler-engine`: Добавление американских профилей сбора данных (REST API через US-шлюз, Playwright Stealth для Cloudflare Turnstile, Orderbook WebSocket/REST для спортивных бирж США).
- `aggregator-core`: Нормализация и матчинг американских типов линий (Moneyline, Point Spread с гандикапами, Player Props) и арбитраж биржевого стакана против классических контор.

## Impact

- **Новые сервисы**: `igaming-source-bovada`, `igaming-source-sporttrade`, `igaming-source-betrivers-us`, `igaming-source-novig`, `igaming-source-circasports`, `igaming-source-bookmaker`.
- **Инфраструктура**: Обновление правил `sing-box` на `ru-proxy`, выделение StatefulSet баз данных PostgreSQL с лимитами 512Mi для каждого источника.
- **Агрегатор**: Увеличение пула активных матчей в памяти на 5,000+ событий из US NFL, NBA, MLB, NHL, NCAA.
