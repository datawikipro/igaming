# Proposal: #39: [paf] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `3f1ab1a4-81d9-454f-bc67-0e19f0da66ec`
Module: `igaming-source-paf` (Paf / Play Among Friends Kambi Integration)

## Description
Интеграция международного лицензированного букмекера Paf (Play Among Friends — скандинавский оператор Аландских островов / Финляндии / Швеции / Эстонии / Испании, функционирующий на платформе Kambi) в экосистему SmartBet.guru с реализацией объектно-ориентированного (ООП) маппинга рынков, росписи исходов, киберспортивных дисциплин и расширенной спортивной статистики (угловые, желтые карточки):

1. **Архитектура модуля `igaming-source-paf`**:
   - Создание микросервиса на Spring Boot 3.4 и Java 21 на базе абстракций и DTO `igaming-source-core` и `igaming-dto`.
   - Интеграция с Kambi Offering API (`https://eu-offering-api.kambicdn.com/offering/v2018` / `https://eu.offering-api.kambicdn.com/offering/v2018`) через `PafApiClient`.
   - Маршрутизация запросов через кластерный HTTP-прокси (`http://100.83.113.50:3128`) согласно правилу #6 AGENTS.md для европейских и оффшорных букмекеров.
   - Эмуляция клиентских браузерных заголовков Paf (`Origin: https://www.paf.com`, `Referer: https://www.paf.com/`).

2. **ООП-маппинг исходов (`PafOddsMapper`)**:
   - Наследование от базового класса `AbstractKambiOddsMapper`.
   - Поддержка основных рынков: 1X2 (Match Result / Moneyline), тоталы (Over/Under), форы (Handicap), обе забьют (BTTS), двойной шанс (Double Chance), ничья нет ставок (Draw No Bet), точный счет (Correct Score).
   - Расширение на статистические рынки: угловые (Corners), желтые карточки (Cards) — тоталы, форы, 1X2, разбивка по таймам.
   - Поддержка киберспорта (CS2, Dota 2, League of Legends, Valorant): победитель матча/карты, тотал карт/раундов/убийств, форы, First Blood.
   - Формирование типизированных исходов `OddItem` и `BetType` в `OddsUpdateRequest`.

3. **Сбор и синхронизация линии (`PafDiscoveryService`, `MatchService`)**:
   - Периодический опрос линий 16 видов спорта (футбол, баскетбол, теннис, хоккей, киберспорт, волейбол, гандбол, настольный теннис и др.).
   - Хеширование локального состояния для предотвращения избыточных апдейтов в БД и брокер.
   - Передача котировок в ядро `igaming-aggregator` и трансляция heartbeat-сигналов.

4. **Отказоустойчивость и K8s-инфраструктура**:
   - Неблокирующий старт HikariCP (`initialization-fail-timeout=0`, `connection-timeout=5000`, `validation-timeout=3000`).
   - Actuator-пробы liveness и readiness (`/actuator/health/liveness`, `/actuator/health/readiness`).
   - Выделенная база PostgreSQL в tmpfs memory с `synchronous_commit = off`.
   - Соблюдение Definition of Done: сборка OCI через Maven Jib, прохождение тестов, 5-минутный мониторинг без сбоев и наполнение линии >= 500 матчей.
