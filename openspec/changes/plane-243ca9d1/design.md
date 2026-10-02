# Design: #78 [team-national-detect] Автоматическое распознавание национальных сборных

## Цель
Автоматически определять, являются ли команды матча национальными сборными, определять ISO-3166-1 alpha-2 код страны и сохранять эти данные в `match_cache` (каждый краулер) и `match_record` (агрегатор).

## Архитектура решения

```
MatchPersistenceService.saveOrUpdateMatchMetadata()
       │
       ▼
NationalTeamDetector.detect(team1, team2, leagueName)
       │
       ├─ Приоритет 1: is_national_league(leagueName)
       │    └── World Cup, Nations League, EURO, Copa America, ...
       │         → ОБЕ команды = сборные
       │
       ├─ Приоритет 2: detectCountryIso(teamName)
       │    └── HashMap 250+ вариантов (EN + RU)
       │         + anti-pattern: IS NOT club (FC, United, City, ...)
       │
       └── NationalTeamResult(isNational: bool, countryIso: String?)
                │
                ▼
         MatchCache { isNationalHome, isNationalAway, homeCountryIso, awayCountryIso }
```

## Новые поля в MatchCache / match_cache

| Поле Java            | Колонка БД          | Тип SQL        | Описание                              |
|---------------------|---------------------|----------------|---------------------------------------|
| `isNationalHome`    | `is_national_home`  | BOOLEAN        | Домашняя команда — сборная страны?    |
| `isNationalAway`    | `is_national_away`  | BOOLEAN        | Гостевая команда — сборная страны?    |
| `homeCountryIso`    | `home_country_iso`  | VARCHAR(10)    | ISO-3166-1 alpha-2 код (напр. RU, DE) |
| `awayCountryIso`    | `away_country_iso`  | VARCHAR(10)    | ISO-3166-1 alpha-2 код                |

Особые значения для британских федераций: `GB-ENG`, `GB-SCT`, `GB-WLS`, `GB-NIR`

## Алгоритм распознавания

### Шаг 1 — Лига (высокая точность)
Если `leagueName` содержит ключевые слова международных турниров:
- `World Cup`, `Nations League`, `EURO`, `Copa America`, `African Cup`, `Asian Cup`, `Gold Cup`, `Confederations Cup`, `Olympic Football`, `Чемпионат мира`, `Лига наций`, `Кубок Африки`, `Кубок Азии`, etc.

→ Обе команды помечаются как `isNational = true`

### Шаг 2 — Имя команды
Для каждой команды:
1. Нормализация: `trim().toLowerCase()`
2. Поиск в HashMap (250+ EN + RU названий стран)
3. Anti-pattern check: если имя содержит `FC`, `United`, `City`, `Dynamo`, `Spartak`, `CSKA`, etc. → `isNational = false`

### Стратегия по умолчанию
- Нет совпадения → `isNational = false`, `countryIso = null`

## Бекфилл БД

SQL-скрипты в `/workspace/repo/scripts/`:
- `national_team_backfill.sql` — для каждой БД краулера (таблица `match_cache`)
- `national_team_backfill_aggregator.sql` — для агрегатора (таблица `match_record`)

3-проходная стратегия:
1. **Pass 1**: UPDATE по league_name (высокая точность)
2. **Pass 2**: UPDATE по team name + ISO-маппинг
3. **Pass 3**: SET FALSE для всего оставшегося NULL

## Затронутые компоненты

| Компонент               | Изменение                                              |
|------------------------|--------------------------------------------------------|
| `igaming-source-core`  | + NationalTeamDetector, NationalTeamResult, поля MatchCache, MatchPersistenceService |
| `match_cache` (все БД) | + 4 новые колонки + бекфилл                           |
| `match_record` (agg)   | + 4 новые колонки + бекфилл                           |

## Место хранения ISO-кода

ISO-код сохраняется на уровне `match_cache` (краулер) и доступен в `match_record` агрегатора. В будущем — доступен через API портала для фильтрации матчей сборных.
