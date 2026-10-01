# Proposal: [team-national-detect] Автоматическое распознавание национальных сборных, связывание с ISO-кодами стран и бекфилл БД

## Context
Plane Task ID: `243ca9d1-f4a5-4a33-9c9c-43656b22da1e`
Feature Branch: `feature/plane-243ca9d1`

## Description
В агрегаторе накоплено множество команд с сырыми букмекерскими названиями национальных сборных (например, "Сборная Испании", "Spain", "Германия (до 21 года)", "France Women", "Россия U19"), у которых не установлен флаг `is_national_team = true`, не указан `team_type = NATIONAL_TEAM`, отсутствуют ISO-3166 коды стран и ссылки на векторные флаги SVG.

Данное предложение реализует:
1. Реестр стран и ISO-кодов `CountryIsoRegistry` с поддержкой Alpha-2/Alpha-3 кодов, мультиязычных названий (RU/EN), синонимов и ссылок на векторные флаги SVG (`/assets/flags/{code}.svg`).
2. Модели `NationalTeamDetectionResult` и `NationalTeamBackfillReport`.
3. Эвристический детектор `NationalTeamDetector` для автоматической классификации сборных, выделения возрастных категорий (U23, U21, U20, U19, U18, U17, OLYMPIC, YOUTH) и пола (MALE, FEMALE), с фильтрацией клубных маркеров (ФК, FC, БК и др.).
4. Сервис пакетной обработки `NationalTeamBackfillService` с поддержкой сухого прогона (`dryRun`), фильтрации по виду спорта и лимитирования выборки.
5. Интеграцию в `HistoricalTeamBackfillService` для сквозного слияния вариантов названий сборных в канонические записи.
6. REST API эндпоинты в `DataManagementController` (`/teams/backfill-national-teams` и `/teams/detect-national`).
