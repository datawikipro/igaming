# Proposal: [team-national-model] Модель данных и API: поддержка национальных сборных (team_type, is_national_team, country_code, flag_url)

## Context
Plane Task ID: `4367d642-399b-4a87-baab-61ab55a09cf8`
Feature Branch: `feature/plane-4367d642`

## Description
Расширение модели данных команд и REST API для полноценной поддержки национальных сборных (`is_national_team`), типизации участников (`team_type`), ассоциации с кодами стран ISO-3166 (`country_code`) и векторными/растровыми флагами (`flag_url`).

### Проблема и цели
1. В текущей модели `Team` и DTO отсутствовало явное разграничение между клубными командами и национальными сборными (Россия, Испания, Бразилия, Франция и т.д.).
2. Для корректной работы компонентов отображения (`TeamAvatar`, карточки вилок, матчей) и последующего алгоритма автодетекта сборных (`team-national-detect`) необходимы стандартизированные поля:
   - `team_type`: тип команды (`CLUB`, `NATIONAL_TEAM`, `INDIVIDUAL`, `ESPORTS`);
   - `is_national_team`: булев флаг для мгновенной фильтрации сборных;
   - `country_code`: 2- или 3-буквенный ISO-код страны (например, "ESP", "FRA", "RUS", "BRA");
   - `flag_url`: URL векторного флага (SVG) для визуализации в интерфейсе.
3. REST API (`aggregator-api`, `igaming-portal`) должен поддерживать фильтрацию команд по статусу сборной, типу команды и коду страны, а также предоставлять специализированные эндпоинты для получения национальных сборных.

### Затрагиваемые компоненты
- `igaming-dto`: добавление перечисления `TeamType`, расширение `TeamDto` и `TeamProfileDto`.
- `aggregator-domain`: расширение JPA-сущности `Team` полями `team_type`, `is_national_team`, `country_code`, `flag_url`, расширение `TeamRepository` методами фильтрации.
- `aggregator-api`: расширение `TeamQueryController` и `DataManagementController` с фильтрацией и эндпоинтом `/teams/national`.
- `igaming-portal`: проброс параметров фильтрации в `PortalTeamController`.
