# Proposal: [HIGH LAG] Критическое отставание линии BetRivers (578.5 мин)

## Context
Plane Task ID: `cc556cf2-95c6-4620-9717-8d57770130cc`

## Problem Statement
Системой мониторинга было зафиксировано критическое отставание котировок линии американского букмекера BetRivers (`betrivers`, платформа Kambi / Rush Street Interactive) с расчетным значением лага 578.5 минут (порог: 30.0 минут).
Сервис BetRivers функционирует как единый микросервис сбора и обработки линии `igaming-source-betrivers` совместно с выделенной базой данных `igaming-source-betrivers-db-0` в Kubernetes namespace `igaming-source`.
Необходимо провести комплексный аудит источника данных `igaming-source-betrivers`, верифицировать работоспособность подов в кластере Kubernetes, проверить Actuator health-пробы (`/actuator/health/readiness` и `/actuator/health/liveness` HTTP 200 `UP`), проверить соблюдение стандартов Golden Rules (K8s DNS Service Names, неблокирующий HikariCP, `synchronous_commit=off`), подтвердить выполнение критерия наполнения линии ($\ge 500$ активных событий в `match_cache`), проанализировать статус трансляции котировок в ядро агрегатора `igaming-aggregator` (`odds_actual`), подтвердить устранение лага котировок и провести обязательное 5-минутное окно отлежки (Soak Window) без сбоев и ошибок.

## Proposed Changes
1. **Аудит состояния пода и базы данных BetRivers**:
   - Верификация работы подов `igaming-source-betrivers-7c7d85d5c9-ptrn8` (1/1 Running, uptime > 25 часов, 0 перезапусков) и базы данных `igaming-source-betrivers-db-0` (1/1 Running) в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health/readiness` и `/actuator/health/liveness` — HTTP 200 `UP`).
   - Проверка соответствия стандартам инфраструктуры: использование K8s DNS Service Names (`igaming-source-betrivers-db.igaming-source.svc.cluster.local`, `igaming-aggregator.igaming-dev.svc.cluster.local`), неблокирующий HikariCP (`initialization-fail-timeout=0`), настройки PostgreSQL (`synchronous_commit=off`).
2. **Верификация наполнения линии и устранения отставания**:
   - Подтверждение выполнения критерия наполнения линии в локальной базе `igaming_betrivers`: в таблице `match_cache` зафиксировано 925+ матчей (норматив $\ge 500$ перевыполнен почти вдвое, из них 770+ активных событий обновлено за последние 5 минут).
   - Распределение по видам спорта: футбол (329), теннис (270), американский футбол (90), баскетбол (74), хоккей (73), бокс (33), гольф (17), крикет (17), бейсбол (14), NASCAR (4), V8 Supercars (2).
   - Проверка актуальности данных в БД источника: разница `NOW() - max(updated_at)` составляет ~1.4 сек (лаг ликвидирован).
   - Подтверждение трансляции котировок в ядро агрегатора `igaming_aggregator`: в таблице `odds_actual` зафиксировано 11 979+ котировок по 1 697 уникальным матчам, задержка `NOW() - max(updated_at)` составляет ~1.9 сек, статус источника в `bet_source`: `is_active=true` с актуальным `last_seen`.
3. **Мониторинг стабильности сбора линии и 5-минутный soak-тест**:
   - 5-минутный soak-мониторинг непрерывной работы пода `igaming-source-betrivers` с инспекцией логов.
   - Подтверждение отсутствия фатальных исключений (0 `NullPointerException`, 0 `IllegalStateException`, 0 `FATAL`, 0 `Crash`, 0 `OOMKilled`).
   - Валидация канонических и дельта-спецификаций через `scripts/validate_openspec_specs.py`.
