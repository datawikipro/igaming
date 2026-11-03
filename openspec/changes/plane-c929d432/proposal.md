# Proposal: [source-exchange] Интеграция биржевой ликвидности и объемов ставок (Betfair Exchange / Smart Money)

## Context
Plane Task ID: `c929d432-8772-41ef-b767-1f05cd375871`
Target Module: `igaming-source-betfair`

## Description
**Контекст:** Детекция реальных институциональных прогрузов (Smart Money) на основе объема денег в стаканах бирж ставок.

**Что нужно сделать:**

- Создать модуль краулера `igaming-source-betfair-exchange` для сбора Traded Volume по исходам.
- Вычислять аномальные скачки объемов ликвидности.
- Публиковать алерты движения умных денег в Kafka топик `odds.smart-money`.
