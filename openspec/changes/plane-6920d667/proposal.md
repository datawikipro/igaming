# Proposal: [capture-advanced-stats] Продвинутые метрики для хоккея и баскетбола (Corsi/Fenwick/Pace/ORtg)

## Context
Plane Task ID: `6920d667-e3b1-496f-94bd-e15389e669a9`
Target Module: `monorepo`

## Description
**Контекст:** Расширение спортивных контрактов `IceHockeySport` и `BasketballSport` институциональными метриками.

**Что нужно сделать:**

- Для хоккея (КХЛ/НХЛ): сбор Corsi, Fenwick, High Danger Scoring Chances (HDCF), GSAx вратарей.
- Для баскетбола (Евролига/НБА): сбор Pace, Offensive/Defensive Rating, True Shooting % (TS%) и глубина ротации скамейки.
- Публикация в Kafka топик `sports.canonical.advanced-metrics`.
