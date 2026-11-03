# Proposal: [aggregator-normalizer] Добавить нормализацию спорта BALONCESTO -> BASKETBALL и поддержку GOLF

## Context
Plane Task ID: `52f85e98-5ec8-4f22-9da1-c12c7b11069b`
Target Module: `aggregator-core`

## Description
**Проблема:** Латиноамериканские букмекеры (BetPlay, Rushbet) передают название спорта на испанском: `BALONCESTO`. Нормализатор сообщает `Sport BALONCESTO not found` и отбрасывает матчи. Также Betsafe передает `GOLF` и `PADEL`.

**Что сделать:**

- В нормализаторе спортов (`SportNormalizer`, `TeamSignatureNormalizer` или маппинге синонимов) добавить алиас `BALONCESTO -> BASKETBALL`.
- Зарегистрировать поддержку спорта `GOLF` в перечислении/интерфейсе `Sport` и `Sports.java`.
- Убедиться в компиляции модуля нормализатора.
