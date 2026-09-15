## Context

Экосистема SmartBet.guru включает 61 активный инстанс букмекеров. Полномасштабный анализ базы BetBurger (166 активных букмекеров) показал, что 85%+ недостающих операторов НЕ требуют резидентских прокси и функционируют на 5 ключевых B2B-движках либо открытых REST/GraphQL протоколах.
Настоящий дизайн описывает архитектуру масштабного расширения линии до 130+ источников и устанавливает строгий протокол изоляции задач, требующих резидентских прокси.

## Goals / Non-Goals

**Goals:**
- Реализовать B2B-движки для пакетного подключения:
  - Altenar Engine (`igaming-source-altenar-base`) $\rightarrow$ 12 брендов
  - Kambi Multi-Tenant Engine $\rightarrow$ 10 брендов
  - Digitain Platform Router $\rightarrow$ 6 брендов
  - Crypto / Web3 Engine $\rightarrow$ 8 брендов
  - Eastern Europe & CIS REST Feeds $\rightarrow$ 10 брендов
- Установить формальный блокирующий барьер `[BLOCKER: RESIDENTIAL_PROXIES]` для контор США и Великобритании, требующих жилых IP-сетей.

**Non-Goals:**
- Самостоятельная закупка или интеграция неподтвержденных платных residential-прокси без согласования с разработчиком/владельцем проекта.

## Decisions

### 1. Архитектура пакетного подключения через B2B-движки (Platform Leverage)
Вместо написания отдельных независимых парсеров с нуля для каждого бренда, разработка группируется вокруг ядер B2B-платформ:
- **Altenar Engine**: Базовый модуль `igaming-source-altenar-base` подключается к `sb-feed.altenar.com`. Каждый бренд (Bet7k, BetNacional, EsportesDaSorte, Vaidebet, Novibet) конфигурируется как легковесный Spring Boot сервис с указанием `altenar.integration.id` и `altenar.token`.
- **Kambi Engine**: Модуль `igaming-source-core-kambi` принимает `channel` и `market`. Стоимость создания нового Kambi-источника снижается до 1 часа.
- **Digitain Engine**: Расширение модуля `igaming-source-digitain` на внешние витрины (Vbet, Totogaming, Iforbet).

### 2. Крипто-сегмент и Web3
- Stake, BC.Game, Cloudbet, Shuffle не используют блокировок по ASN хостингов. Запросы направляются через стандартный шлюз `ru-proxy` (`100.83.113.50:3128`).
- Polymarket и SX.bet используют прямые открытые Web3 RPC / GraphQL API без каких-либо прокси.

### 3. Протокол блокировки контор под Residential Proxies
- Для 12 защищенных операторов (William Hill, Ladbrokes, PaddyPower, Skybet, Betfred, Boylesports, FanDuel, DraftKings, Caesars, ESPN BET, 1win, Interwetten) в плане заводится **выделенная блокирующая задача**:
  `[BLOCKER] Подключение и настройка пула Residential Proxies в кластере`.
- **Правило для AI-агентов**: Запрещено начинать реализацию или деплой любых задач из Трека 8, пока блокирующая задача не закрыта разработчиком. Если агент доходит до этого трека, он **обязан остановиться и запросить у разработчика учетные данные / endpoint резидентских прокси**.

## Risks / Trade-offs

- **[Риск: Частота запросов Altenar WebSocket]** → *Митигация*: Использовать единый WebSocket connection на бренд с подпиской на дифференциальные дельты котировок (`odds_delta`).
- **[Риск: Нехватка ресурсов на worker нодах]** → *Митигация*: Новые легковесные Kambi/Altenar инстансы требуют всего 128-256Mi RAM на JVM за счет отсутствия Chromium.

## Migration Plan & Phase Gates

1. **Фаза 1 (Открыта)**: Kambi Clones (TonyBet, Stoiximan, Expekt, Paf, ATG) + CIS REST (Favbet, Parimatch, Vbet).
2. **Фаза 2 (Открыта)**: Altenar Engine Base + подключение 10+ латиноамериканских и европейских брендов.
3. **Фаза 3 (Открыта)**: Крипто-букмекеры (Stake, BC.Game, Cloudbet, Web3) + Биржи ставок (Betfair, Betdaq).
4. **Фаза 4 (Открыта)**: Восточная Европа (Superbet, STS, Fortuna, Tipsport, Mozzartbet).
5. **Фаза 5 (ЗАБЛОКИРОВАНА)**: Ожидание подтверждения разработчиком готовности Residential Proxies $\rightarrow$ Разблокировка UK & US гигантов.
