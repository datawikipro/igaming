# Proposal: [team-national-model] Модель данных и API: поддержка национальных сборных (team_type, is_national_team, country_code, flag_url)

## Context
Plane Task ID: `4367d642-399b-4a87-baab-61ab55a09cf8`
Sequence ID: 77
Module: `team-national-model`

## Problem & Motivation
В текущей модели данных платформы все команды и участники матчей хранились в единой сущности `team` без явной классификации клуб/национальная сборная, без привязки к двухбуквенным ISO кодам стран и без URL флагов. Это ограничивало возможности:
1. Селективной фильтрации национальных сборных на карточках матчей и в сканере вилок.
2. Отображения флагов стран вместо стандартных клубных логотипов (`TeamAvatar`).
3. Формирования аналитических досье сборных и турнирной статистики международных чемпионатов (ЧМ, Евро, Кубок Африки, Лига Наций и др.).

## Proposed Architecture & Changes
1. **Слой DTO (`igaming-dto`)**:
   - `TeamType` enum: `CLUB`, `NATIONAL_TEAM`, `INDIVIDUAL` с `@JsonCreator` `fromString`.
   - `NationalTeamStatsDto`: сводная статистика сборных (`total_count`, `by_sport`, `distinct_countries_count`).
   - Расширение сущностей `TeamDto`, `TeamProfileDto`, `MatchDto`, `SurebetAlertDto` полями `team_type`, `is_national_team`, `country_code`, `flag_url`.

2. **Слой доменной модели (`aggregator-domain`)**:
   - `TeamType` enum: `CLUB`, `NATIONAL_TEAM`.
   - Расширение сущности `Team` полями:
     - `team_type VARCHAR(50)` (по умолчанию `CLUB`)
     - `is_national_team BOOLEAN` (по умолчанию `false`)
     - `country_code VARCHAR(10)`
     - `flag_url VARCHAR(512)`
   - `TeamSpecifications`: гибкая фильтрация через JPA Criteria API по имени (русский, английский, локальный), типу команды, признаку национальной сборной, коду страны и виду спорта.
   - `TeamRepository`: методы поиска и агрегации статистики по сборным.

3. **Слой REST API (`aggregator-api`)**:
   - `TeamQueryController`:
     - `GET /api/teams`: поиск и фильтрация с параметрами `team_type`, `is_national_team`, `country_code`, `sport`.
     - `GET /api/teams/national`: выделенный эндпоинт получения только национальных сборных.
     - `GET /api/teams/national/stats`: агрегированные метрики по сборным платформам.
     - `GET /api/teams/{id}`, `/upcoming`, `/past`.
