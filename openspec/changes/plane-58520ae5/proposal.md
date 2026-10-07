# Proposal: [HIGH LAG] Критическое отставание линии Fonbet (KZ) (672.4 мин)

## Context
Plane Task ID: `58520ae5-7a0e-4a53-ba22-e2542c17d02b`

## Problem Statement
В системе мониторинга зафиксирован критический инцидент с отставанием линии казахстанского букмекера Fonbet (KZ) (`fon-bet-kz`) с расчетным лагом 672.4 мин.
Fonbet (KZ) функционирует на единой платформе Fonbet Family (`igaming-source-fon-bet-ru` образ с параметрами `APP_BOOKMAKER_NAME=fon-bet-kz`, `APP_TARGET_HOST=fonbet.kz`) и обслуживается сервисами `igaming-source-fon-bet-kz-crawler`, `igaming-source-fon-bet-kz-loader` и базой данных `igaming-source-fon-bet-kz-db-0` в Kubernetes namespace `igaming-source`.
Необходимо провести комплексный аудит источника `igaming-source-fon-bet-kz`, верифицировать работоспособность краулера, лоадера и базы данных в кластере Kubernetes, проверить Actuator health-пробы, сетевую связность, соблюдение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`), проанализировать статус передачи котировок в ядро `igaming-aggregator` и подтвердить устранение лага котировок в соответствии с Definition of Done.

## Proposed Changes
1. **Верификация источника данных Fonbet (KZ)**:
   - Проверка статуса подов `igaming-source-fon-bet-kz-crawler`, `igaming-source-fon-bet-kz-loader` и базы данных `igaming-source-fon-bet-kz-db-0` в Kubernetes namespace `igaming-source`.
   - Проверка Actuator health-проб (`/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` — HTTP 200 `UP`).
   - Проверка соответствия Golden Rules: неблокирующий старт HikariCP (`initialization-fail-timeout=0`), использование K8s DNS Service Names (`igaming-source-fon-bet-kz-db.igaming-source.svc.cluster.local`, `igaming-aggregator`) вместо прямых IP, `ddl-auto=update`, `synchronous_commit=off`, кеширование факторов в Redis sidecar (`APP_PERSISTENCE_USE_REDIS_FACTORS=true`).
   - Подтверждение критерия наполнения линии ($\ge 500$ активных матчей в `match_cache`, факт: ~4000 активных матчей).
   - Проверка актуальности данных в БД источника (лаг < 1 сек) и доставки котировок в `igaming-aggregator` (`odds_actual`: > 180 000 котировок, лаг ~10 сек, heartbeat `bet_source` активен).
2. **Мониторинг стабильности сбора линии и 5-минутный soak-тест**:
   - 5-минутный soak-контроль работы подов без сбоев и фатальных ошибок (0 NPE, 0 OOMKilled, 0 IllegalStateException, 0 CrashLoopBackOff, аптайм лоадера > 10 мин).
   - Верификация непрерывного потока котировок в Kafka топик `odds.updates` и стабильности heartbeat в агрегатор.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`).
   - Валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
