# Proposal: #27: [bwin] ООП-рефакторинг мапперов: Киберспорт, Статистика (Угловые/ЖК) и роспись исходов

## Context
Plane Task ID: `cbf8988b-ab42-4f5f-b80e-2eaa022c0671`

## Description
Комплексный ООП-рефакторинг архитектуры маппинга котировок для источника `bwin` (Entain API).
Переход от монолитного метода маппинга к модульной архитектуре на базе паттерна Strategy (`BwinMarketHandler`):
- Обработчики киберспорта: CS2, Dota 2, LoL, Valorant (победа на карте, тоталы карт/раундов, форы, First Blood, Kills).
- Обработчики статистики: Угловые (Corners) и Желтые карточки (Yellow Cards) — тоталы матча и команд, форы, исходы 1X2, таймы.
- Обработчики росписи исходов: 1X2/Moneyline, Totals, Handicaps, Double Chance, Draw No Bet, Both Teams to Score (BTTS), Correct Score.
- Настройка неблокирующего старта HikariCP согласно правилу #4 AGENTS.md, добавление Actuator health probes и Jib сборщика.
- Комплексное модульное тестирование и стресс-тестирование под нагрузкой.
