# Design: [team-national-model] Модель данных и API: поддержка национальных сборных (team_type, is_national_team, country_code, flag_url)

## Context
See proposal.md - Context & Description.
В рамках расширения модели данных участников спортивных событий потребовалось выделить национальные сборные и типизировать команды (`CLUB`, `NATIONAL_TEAM`, `INDIVIDUAL`, `ESPORTS`), а также добавить связку со страной (код ISO-3166) и URL флага для визуального отображения на портале.

## Goals / Non-Goals

**Goals:**
- Добавить перечисление `TeamType` (`CLUB`, `NATIONAL_TEAM`, `INDIVIDUAL`, `ESPORTS`) в `igaming-dto`.
- Расширить `TeamDto` и `TeamProfileDto` полями `team_type`, `is_national_team`, `country_code`, `flag_url`.
- Расширить JPA-сущность `Team` в `aggregator-domain` полями `team_type`, `is_national_team`, `country_code`, `flag_url`, индексом `idx_team_national` и вспомогательным методом `markAsNationalTeam(countryCode, flagUrl)`.
- Добавить в `TeamRepository` методы выборки сборных (`findByIsNationalTeam`, `findByTeamType`, `findByCountryCode`).
- Расширить `TeamQueryController` и `DataManagementController` в `aggregator-api` параметрами фильтрации и специализированным эндпоинтом `/teams/national`.
- Пробросить параметры фильтрации в `PortalTeamController` в `igaming-portal`.

**Non-Goals:**
- Алгоритмы эвристического автоопределения сборных по наименованию команд (выделены в отдельную задачу `team-national-detect`).
- Модификация парсеров внешних букмекеров (краулеры передают сырые строки или базовые сущности).

## Decisions

- **Decision 1: Перечисление TeamType в igaming-dto**:
  Перечисление содержит значения `CLUB`, `NATIONAL_TEAM`, `INDIVIDUAL`, `ESPORTS`. В JPA-сущности `Team` сохраняется как строка `@Enumerated(EnumType.STRING)` с длиной 32 символа.
- **Decision 2: Булев флаг is_national_team и метод markAsNationalTeam**:
  Флаг `is_national_team` денормализован для быстрого построения индексов и прямых запросов. Метод `markAsNationalTeam(countryCode, flagUrl)` атомарно переводит `is_national_team` в `true`, устанавливает `team_type = TeamType.NATIONAL_TEAM` и заполняет атрибуты страны.
- **Decision 3: Обратная совместимость REST API**:
  Параметры `is_national_team`, `team_type`, `country_code` в эндпоинтах `/teams` и `/api/v1/teams` являются опциональными (`required = false`), что исключает нарушение контракта для существующих потребителей API.
- **Decision 4: Выделенный эндпоинт /teams/national**:
  Предоставляет прямой доступ к списку национальных сборных без необходимости передачи query-параметров.

## Risks / Trade-offs

- **[Risk] Null-значения для исторических записей команд в БД** → Mitigation: поле `is_national_team` имеет значение по умолчанию `false`, мапперы и DTO поддерживают nullable-значения `team_type`, `country_code`, `flag_url`.
- **[Risk] Разнородные форматы ISO кодов стран (alpha-2 vs alpha-3)** → Mitigation: поле `country_code` допускает строки до 10 символов, нормализация кодов стран согласуется со словарем `Country`.
