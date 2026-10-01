# Proposal: [team-national-model] Модель данных и API: поддержка национальных сборных (team_type, is_national_team, country_code, flag_url)

## Context
Plane Task ID: `4367d642-399b-4a87-baab-61ab55a09cf8`
Module: `team-national-model` (aggregator-domain, igaming-dto, aggregator-api)

## Description
Реализация поддержки национальных сборных команд в единой модели данных SmartBet.guru и REST API:
1. **Модель данных и типизация участников**:
   - Поддержка классификатора `team_type` (`TeamType`: `CLUB`, `NATIONAL_TEAM`);
   - Флаг национальной сборной `is_national_team` (`Boolean`);
   - Двухбуквенный ISO-код страны `country_code` (ISO 3166-1 alpha-2, e.g. `RU`, `FR`, `BR`, `DE`);
   - Ссылка на SVG/PNG-флаг `flag_url` (CDN `flagcdn.com` или локальный каталог статики);
   - Автоматическая синхронизация флага `is_national_team = true` с `team_type = NATIONAL_TEAM`.
2. **Общие DTO (`igaming-dto`)**:
   - Внедрение `TeamType` enum в `pro.datawiki.igaming.dto`;
   - Расширение `TeamDto` и `TeamProfileDto` полями `team_type`, `is_national_team`, `country_code`, `flag_url` с аннотациями `@JsonProperty` и `@JsonAlias` для обратной совместимости camelCase и snake_case;
   - Вспомогательные методы и конструкторы для безопасной работы с клиентами API.
3. **Хранилище сущностей (`aggregator-domain`)**:
   - Jackson-аннотации `@JsonProperty` и `@JsonAlias` на сущности `Team` для корректной сериализации/десериализации;
   - Индексация и методы поиска в `TeamRepository` по `isNationalTeam`, `teamType`, `countryCode`;
   - Вспомогательные методы подсчета и постраничной выборки сборных.
4. **REST API шлюз (`aggregator-api`)**:
   - Расширение контроллера `TeamQueryController` (`/api/teams`) поддержкой фильтрации по параметрам:
     - `team_type` (`TeamType`: `CLUB`, `NATIONAL_TEAM`);
     - `is_national_team` (`Boolean`: `true`/`false`);
     - `country_code` (`String`: код страны, e.g. `FR`);
   - Добавление специализированного эндпоинта `/api/teams/national` для получения только сборных команд с фильтрацией по стране и поисковому запросу;
   - Добавление эндпоинта статистики `/api/teams/national/stats` (количество сборных по видам спорта и странам).
5. **Верификация и тесты**:
   - Модульные тесты на DTO сериализацию/десериализацию;
   - Модульные тесты на методы репозитория и контроллера;
   - Инсталляция обновленных артефактов в локальный репозиторий Maven.
