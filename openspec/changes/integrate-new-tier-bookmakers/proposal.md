## Why

Экосистема SmartBet.guru в настоящее время агрегирует 61 букмекера в Kubernetes. Сравнение с крупнейшим мировым сервисом арбитража BetBurger (166 активных букмекеров и 600+ клонов) показывает, что большинство недостающих букмекеров работают на унифицированных B2B-платформах (Altenar, Kambi, Digitain, BetB2B) либо являются крипто-букмекерами без геоблокировок.
Настоящее изменение описывает полный генеральный план масштабирования до 130+ активных букмекеров, разбитый на открытые треки (доступные немедленно через кластерный EU-шлюз) и трек с явной блокирующей задачей ожидания Residential Proxies для контор США и Великобритании.

## What Changes

- **Трек 1: Kambi Ecosystem (+10 букмекеров)**:
  - Мульти-арендное масштабирование Kambi CDN (`eu-offering.kambicdn.org`): TonyBet, Stoiximan, ExpektDK, Paf, ATG, Casumo, LeoVegas EU.
- **Трек 2: Altenar B2B Engine (+12 букмекеров LatAm & EU)**:
  - Единый адаптер к платформе Altenar (`sb-feed.altenar.com`): Bet7k, BetNacional, EsportesDaSorte, Vaidebet, LuvaBet, BetBra, BetEsporte, Apuestatotal, Olimpo, Novibet.
- **Трек 3: Digitain Platform (+6 букмекеров)**:
  - Активация существующего модуля `igaming-source-digitain` для витрин Vbet, Totogaming, Iforbet, Winbet, Betpoint.
- **Трек 4: Crypto & Web3 Sportsbooks (+8 букмекеров)**:
  - Прямой сбор котировок без датацентр-блоков: Stake (GraphQL), BC.Game (REST), Cloudbet (API), Shuffle, Gamdom, FortuneJack, Polymarket (Web3 Polygon), SX.bet.
- **Трек 5: Восточная Европа и СНГ (+10 букмекеров)**:
  - Superbet, STS, Fortuna, Tipsport, Sazka, Synottip, Mozzartbet, Meridianbet, Favbet, Parimatch.
- **Трек 6: Мировые биржи ставок (+4 биржи)**:
  - Betfair Exchange (Back/Lay APING REST), Betdaq, Smarkets, Matchbook.
- **Трек 7: Независимые европейские операторы (+8 букмекеров)**:
  - Betway, 10bet, Betvictor, Tipico, Betano, Codere, Caliente, Wplay.
- **Трек 8 [ЗАБЛОКИРОВАН]: US & UK Heavyweights под Residential Proxies (+12 букмекеров)**:
  - *Блокирующая задача*: Настройка пула US/UK Residential Egress в `ru-proxy`.
  - *Заблокированные источники*: William Hill, Ladbrokes, PaddyPower, Skybet, Betfred, Boylesports, FanDuel, DraftKings, Caesars, ESPN BET, 1win, Interwetten.

## Capabilities

### Modified Capabilities

- `crawler-engine`: Внедрение модулей Altenar B2B, Kambi Multi-Tenant, Digitain Multi-Tenant, Web3 Orderbook, а также поддержка профиля `RESIDENTIAL_BLOCKED` с гейтом подтверждения от разработчика.
- `aggregator-core`: Расширение мультиязычных словарей нормализации (Бразилия, Греция, Чехия, Польша, Балканы) и Back/Lay арбитражного стакана.

## Impact

- **Масштаб**: Рост базы источников с 61 до 125+ активных инстансов без резидентских прокси и до 140+ при снятии блокировки Residential.
- **Инфраструктура**: Использование свободных мощностей SSD-ноды `k8s-worker-2`, экономный расход RAM за счет переиспользования единых B2B-парсеров (Altenar, Kambi, Digitain).
