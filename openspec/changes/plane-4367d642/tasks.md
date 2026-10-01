# Implementation Tasks: [team-national-model] Модель данных и API: поддержка национальных сборных (team_type, is_national_team, country_code, flag_url)

- [x] 1. Изучить текущий модуль и подготовить структуру классов
  - [x] 1.1 Анализ текущих модулей `igaming-dto`, `aggregator-domain`, `aggregator-api` и проверка структуры полей
  - [x] 1.2 Создание перечисления `TeamType` (`CLUB`, `NATIONAL_TEAM`) в `igaming-dto` и согласование с `aggregator-domain`
  - [x] 1.3 Расширение моделей DTO в `igaming-dto` (`TeamDto`, `TeamProfileDto`) полями `team_type`, `is_national_team`, `country_code`, `flag_url` с аннотациями Jackson `@JsonProperty` и `@JsonAlias`
  - [x] 1.4 Доработка JPA-сущности `Team` в `aggregator-domain`: аннотации `@JsonProperty` и `@JsonAlias` для сериализации/десериализации, синхронизация сборной и типа
  - [x] 1.5 Добавление методов выборки в `TeamRepository` (`findByTeamType`, `findByIsNationalTeam`, `findByCountryCodeIgnoreCase`, `countByTeamType`, `countByIsNationalTeam`)
  - [x] 1.6 Верификация компиляции структуры классов в `igaming-dto`, `aggregator-domain` и `aggregator-api`
- [ ] 2. Реализовать основную бизнес-логику и маппинги данных
  - [ ] 2.1 Реализация фильтрации по `team_type`, `is_national_team`, `country_code` в `TeamQueryController` (`GET /api/teams`)
  - [ ] 2.2 Реализация специализированного REST эндпоинта `GET /api/teams/national` в `aggregator-api`
  - [ ] 2.3 Реализация эндпоинта статистики сборных `GET /api/teams/national/stats`
  - [ ] 2.4 Добавление модульных тестов на сериализацию/десериализацию DTO и работу контроллера
- [ ] 3. Валидировать OpenSpec и проверить сборку/тесты
  - [ ] 3.1 Сборка и прогон unit-тестов модулей `igaming-dto`, `aggregator-domain`, `aggregator-api`
  - [ ] 3.2 Установка обновленных артефактов в локальный Maven-репозиторий (`mvn install`)
  - [ ] 3.3 Валидация спецификаций OpenSpec (`openspec validate --specs` или локальная проверка)
  - [ ] 3.4 Оформление итоговых отчетов и коммитов
