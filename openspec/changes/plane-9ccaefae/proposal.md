# Proposal: [betano] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `9ccaefae-07b6-4d4f-ac61-0cda839f445e`
Bookmaker: Betano (`igaming-source-betano`, флагман Kaizen Gaming в Бразилии, Португалии, Румынии и др.)

## Description
Комплексный ООП-рефакторинг мапперов котировок Betano, добавление объектно-ориентированной архитектуры обработчиков на базе `AbstractBetTypeMapper`, реализация специализированных обработчиков рынков:
- Роспись основных исходов (Moneyline, 1X2, 2-Way, 3-Way, Double Chance, Draw No Bet, Both Teams to Score, тоталы, форы, точный счёт, HalfTime/FullTime);
- Статистика футбола и игровых видов спорта (угловые, жёлтые карточки, суммарные карточки, фолы, офсайды, удары в створ) для всего матча и по таймам;
- Киберспорт (победители карт, форы и тоталы карт/раундов, тоталы убийств, первая кровь);
- Моделирование данных DTO (`BetanoEventDto`, `BetanoMarketDto`, `BetanoOutcomeDto`, `BetanoResponseDto`) с поддержкой API Betano/Kaizen;
- Настройка `pom.xml` (Spring Boot 3.4.1, Actuator, Jib OCI-сборка) и `application.properties` (неблокирующий HikariCP, Actuator probes, HTTP proxy routing, PostgreSQL tmpfs);
- Комплексное покрытие модульными тестами в `BetanoOddsMapperTest` и верификация по стандартам Definition of Done.
