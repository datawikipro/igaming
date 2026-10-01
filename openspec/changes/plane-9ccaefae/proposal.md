# Proposal: [betano] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `9ccaefae-07b6-4d4f-ac61-0cda839f445e`
Module: `igaming-source-betano` (Betano / Kaizen Gaming Kambi Integration)

## Description
Интеграция международного букмекера Betano (бренд группы Kaizen Gaming, сестринский по отношению к Stoiximan, оперирующий на платформе Kambi) в экосистему SmartBet.guru с реализацией ООП-маппинга рынков, росписи исходов, киберспортивных дисциплин и расширенной статистики (угловые, желтые карточки):

1. **Архитектура модуля `igaming-source-betano`**:
   - Построение микросервиса на Spring Boot 3.4 и Java 21 на базе абстракций `igaming-source-core`.
   - Интеграция с Kambi Offering API (`https://eu.offering-api.kambicdn.com/offering/v2018`) через `BetanoApiClient`.
   - Маршрутизация запросов через выделенный кластерный HTTP-прокси (`http://100.83.113.50:3128` / `proxy-us.service-proxy.svc.cluster.local:31292`) согласно правилу #6 AGENTS.md для оффшорных и европейских букмекеров.
   - Эмуляция заголовков клиентских сессий Betano (`Origin: https://www.betano.com`, `Referer: https://www.betano.com/`).

2. **ООП-маппинг исходов (`BetanoOddsMapper`)**:
   - Наследование от базового класса `AbstractKambiOddsMapper`.
   - Поддержка основных рынков: денежная линия (1X2), тоталы (Over/Under), форы (Handicap), обе забьют (BTTS), двойной шанс (Double Chance), ничья нет ставок (Draw No Bet).
   - Расширение на статистические рынки: угловые (Corners), желтые карточки (Cards).
   - Поддержка киберспорта (Esports: CS2/CS:GO, Dota 2, League of Legends, Valorant).
   - Корректная трансляция сущностей в DTO `OddsUpdateRequest` с типизированными исходами `OddItem` и `BetType`.

3. **Сбор и синхронизация линии (`BetanoDiscoveryService`, `MatchService`)**:
   - Периодический опрос линий 16 видов спорта (футбол, баскетбол, теннис, хоккей, киберспорт, волейбол, гандбол, настольный теннис и др.).
   - Хеширование локального состояния для предотвращения избыточных апдейтов в БД и брокер.
   - Передача котировок в ядро `igaming-aggregator` и трансляция heartbeat-сигналов.

4. **Отказоустойчивость и K8s-инфраструктура**:
   - Неблокирующий старт HikariCP (`initialization-fail-timeout=0`, `connection-timeout=5000`, `validation-timeout=3000`).
   - Actuator-пробы liveness и readiness (`/actuator/health/liveness`, `/actuator/health/readiness`).
   - Выделенная база PostgreSQL в tmpfs memory с `synchronous_commit = off`.
   - Соблюдение Definition of Done: сборка OCI через Maven Jib, прохождение тестов, 5-минутный мониторинг без сбоев и наполнение линии >= 500 матчей.
