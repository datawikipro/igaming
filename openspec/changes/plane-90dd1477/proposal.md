# Proposal: [capture-tennis-deep] Углубленный point-by-point сбор теннисной статистики (Tennis Abstract)

## Context
Plane Task ID: `90dd1477-1904-4ce1-a89a-5bcadf725f71`
Target Module: `monorepo`

## Description
**Контекст:** Для полиморфного класса `TennisSport` требуется статистика подачи и приема очков по покрытиям.

**Что нужно сделать:**

- Реализовать сбор % выигрыша очков на 1-й и 2-й подаче по покрытиям (хард, грунт, трава, индор) из Tennis Abstract / Tennis Explorer.
- Собирать метрики реализации брейк-поинтов и статистику тай-брейков.
- Публиковать в Kafka топик `sports.tennis.point-by-point` с MDM маппингом игроков.
