# Proposal: #11: [marathonbet] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `8b96a568-a393-43e6-83d8-4469fc05d81d`
Bookmaker: Marathonbet (`igaming-source-marathonbet`)

## Description
Комплексный ООП-рефакторинг и расширение мапперов для краулера Marathonbet (`igaming-source-marathonbet`):
1. **Киберспорт (Esports)**: Маппинг исходов матча и отдельных карт (1-5 карты), тоталов по картам/раундам/фрагам, а также фор (Handicap) по картам, раундам и убийствам (`MarathonEsportsResultMapper`, `MarathonEsportsTotalMapper`, `MarathonEsportsHandicapMapper`).
2. **Футбольная статистика (Статистика / Corners / Cards)**: Выделенный ООП-маппер `MarathonFootballStatsMapper` для статистических маркеров (угловые `StatType.CORNERS`, желтые карточки `StatType.YELLOW_CARDS`, фолы `StatType.FOULS`, офсайды `StatType.OFFSIDES`, удары в створ `StatType.SHOTS_ON_TARGET`) с поддержкой исходов 1X2, двойного шанса, тоталов и фор для всего матча и по таймам (`HALF_1`, `HALF_2`).
3. **Роспись исходов (Special & Extended Markets)**: Корректный парсинг и маппинг точного счета, тайм/матч (HT/FT), обе забьют (BTTS), команда забьет, чет/нечет без блокировки фильтрами подавления.
4. **Оптимизация фильтрации**: Устранение устаревших ограничений в `SUPPRESS_M_PATTERN`, блокировавших валидные маркеты (`IN_BOTH_HALVES`, `HALF_TIME_/_FULL_TIME`, `.*_MAP_RESULT`, `STAT_`, `ODD`, `EVEN`).
5. **E2E Пайплайн валидации и деплоя**: Jib-сборка OCI-образа, деплой тестового пода в K8s `igaming-dev`, 5-минутный soak-тест без единой ошибки, мердж в master, деплой в прод (`igaming-source`), 5-минутный прод-мониторинг и верификация наполнения линии (>= 500 матчей).
