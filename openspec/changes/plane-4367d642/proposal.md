# Proposal: [team-national-model] Модель данных и API: поддержка национальных сборных (team_type, is_national_team, country_code, flag_url)

## Context
- **Plane Task ID**: `4367d642-399b-4a87-baab-61ab55a09cf8`
- **Module**: `team-national-model`
- **Related Modules**: `igaming-dto`, `aggregator-domain`, `aggregator-api`, `igaming-portal`

## Summary
Реализация расширенной модели данных и API-контрактов для полноценной поддержки национальных сборных (National Teams) во всех слоях экосистемы SmartBet.guru:
1. **Перечисление `TeamType`**: типизация команд (`CLUB`, `NATIONAL_TEAM`, `INDIVIDUAL`, `PAIR`, `UNKNOWN`).
2. **DTO слой (`igaming-dto`)**: добавление атрибутов `team_type`, `is_national_team`, `country_code`, `flag_url` в ключевые DTO (`TeamDto`, `TeamProfileDto`, `MatchDto`, `MatchPreviewDto`, `SurebetAlertDto`), а также создание `NationalTeamStatsDto`.
3. **Domain слой (`aggregator-domain`)**: поддержка атрибутов в сущности `Team`, внедрение критериев фильтрации `TeamSpecifications.withFilters`, методов поиска и агрегатной аналитики по видам спорта и странам в `TeamRepository`.
4. **API слой (`aggregator-api`)**: поддержка расширенных фильтров в `/api/teams`, создание специализированных эндпоинтов `/api/teams/national` и `/api/teams/national/stats`.
5. **Gateway слой (`igaming-portal`)**: двусторонняя совместимость camelCase / snake_case и проксирование фильтров в Portal API.
