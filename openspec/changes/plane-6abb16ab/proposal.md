# Proposal: [SUPER-ARB] Аномальная вилка 10.1% с участием Winline

## Context
Plane Task ID: `6abb16ab-0000-0000-0000-000000000000`

## Problem Statement
В системе мониторинга агрегатора зафиксирован инцидент `[SUPER-ARB] Аномальная вилка 10.1% с участием Winline`.
Требуется проверить корректность работы валидации арбитражных ситуаций в микросервисе `igaming-aggregator-surebet`:
1. Убедиться, что семейство букмекера Winline и все его региональные домены (`winline`, `winline-ru`, `winline-by`, `winline-kz`, `winline.ru`, `winline.by`) зарегистрированы в правиле синдикатов-клонов (`CloneSyndicateRule`), исключая возникновение фиктивных вилок между клонами одного букмекера.
2. Подтвердить, что реальные арбитражные ситуации высокой доходности (10.1%) между Winline и независимыми букмекерами (например, Pinnacle) корректно регистрируются в соответствии с политикой No Yield Cap.
3. Провести верификацию в кластере Kubernetes (`igaming-dev`), подтвердить работоспособность Actuator health-проб и выдержать обязательный 5-минутный тест стабильности (Golden Rule #1 в `AGENTS.md`).

## Proposed Changes
1. **Регистрация семейства Winline в `CloneSyndicateRule`**:
   - Добавление маппинга Winline и его региональных зеркал/алиасов в `BOOKMAKER_FAMILIES`.
   - Добавление префиксной фильтрации `winline` в методе `resolveFamily()`.
2. **Модульное тестирование**:
   - Тест на принятие валидной вилки 10.1% между Winline и независимым букмекером (Pinnacle) в рамках No Yield Cap.
   - Тест на отклонение клоновой связки `winline` + `winline-by`.
3. **Деплой и верификация в Kubernetes**:
   - Развертывание `igaming-aggregator-surebet` в namespace `igaming-dev`.
   - Проверка Actuator эндпоинтов `/actuator/health/readiness` и `/actuator/health/liveness` (HTTP 200 `UP`).
   - 5-минутный мониторинг стабильности работы пода (Soak Window).
4. **Валидация OpenSpec**:
   - Прохождение валидации через `validate_openspec_specs.py`.
