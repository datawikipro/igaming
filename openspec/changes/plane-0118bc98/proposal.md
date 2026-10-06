# Proposal: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
Plane Task ID: `0118bc98`

## Problem Statement
В системе мониторинга зафиксирован инцидент о недоступности сервиса публикации постов `smm-bot-telegram` в Kubernetes namespace `igaming-dev`.
Необходимо провести комплексную диагностику, верифицировать статус подов и логов, выполнить контролируемый перезапуск сервиса, проверить сетевую доступность Actuator и healthcheck-проб (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`), подтвердить сетевую связность с Redis (`igaming-redis`) и Telegram Bot API через кластерный HTTP-прокси (`100.83.113.50:3128`), провести нагрузочное тестирование и выдержать обязательный 5-минутный период soak-мониторинга в соответствии с Golden Rule 1 (AGENTS.md).

## Proposed Changes
1. **Диагностика и перезапуск сервиса**:
   - Проверка текущего состояния пода `smm-bot-telegram` в namespace `igaming-dev`.
   - Контролируемый перезапуск деплоймента (`kubectl rollout restart deployment smm-bot-telegram`).
   - Проверка успешного завершения роллаута и перехода пода в статус `Running 1/1` с 0 рестартов.
2. **Верификация проб и сетевой связности**:
   - Проверка доступности HTTP-эндпоинтов `/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` (HTTP 200 UP).
   - Проверка статуса флота каналов `/api/v1/telegram/status` и тестовой публикации `/api/v1/telegram/post`.
   - Подтверждение связности с `igaming-redis` и кластерным прокси `100.83.113.50:3128`.
3. **Нагрузочное тестирование и тесты**:
   - Запуск нагрузочного стресс-теста `scripts/test_smm_telegram_load.py` (130 запросов, concurrency до 10, 100% success rate, 0 ошибок).
   - Запуск набора модульных тестов `smm-agent/tests/test_telegram_poster.py` (10/10 тестов OK).
4. **5-минутный Soak-мониторинг**:
   - Непрерывный мониторинг пода в течение 5+ минут после перезапуска без единой ошибки и рестарта.
5. **OpenSpec валидация**:
   - Валидация канонических спецификаций и активного предложения через `scripts/validate_openspec_specs.py`.
