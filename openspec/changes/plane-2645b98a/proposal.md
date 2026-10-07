# Proposal: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
Plane Task ID: `2645b98a`

## Problem Statement
В системе мониторинга зафиксирован алерт о недоступности сервиса публикации постов `smm-bot-telegram` в Kubernetes namespace `igaming-dev`.
Необходимо провести комплексную верификацию сервиса, проверить состояние подов и логов, подтвердить сетевую доступность Actuator и healthcheck-проб (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`), проверить сетевую связность с Redis (`igaming-redis`) и Telegram Bot API через кластерный HTTP-прокси (`100.83.113.50:3128`), провести полный цикл нагрузочного тестирования (Actuator probes, Telegram Status API, Webhook & AI Prompter Ingestion) и подтвердить соблюдение критериев Definition of Done (Golden Rule 1, аптайм > 5 минут без единой ошибки и рестарта).

## Proposed Changes
1. **Диагностика и верификация сервиса в Kubernetes**:
   - Проверка текущего состояния пода `smm-bot-telegram` в namespace `igaming-dev` (`Running 1/1`, 0 рестартов, аптайм > 170 минут).
   - Инспекция логов пода на отсутствие необработанных исключений и сбоев.
2. **Верификация проб и сетевой связности**:
   - Проверка доступности HTTP-эндпоинтов `/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` (HTTP 200 UP).
   - Проверка статуса флота региональных каналов `/api/v1/telegram/status` и тестовой публикации `/api/v1/telegram/post`.
   - Проверка сетевой связности с `igaming-redis` (очередь `feedback:queue:telegram`) и кластерным прокси `100.83.113.50:3128`.
3. **Нагрузочное тестирование и модульные тесты**:
   - Запуск нагрузочного стресс-теста `scripts/test_smm_telegram_load.py` (130 запросов, concurrency до 10, 100% success rate, 0 ошибок).
   - Запуск набора модульных тестов `smm-agent/tests/test_telegram_poster.py` (10/10 тестов OK).
4. **Soak-мониторинг и соблюдение Golden Rules**:
   - Подтверждение непрерывной стабильной работы сервиса без рестартов и сбоев (>170 мин при требовании Golden Rule 1 от 5 минут).
5. **OpenSpec валидация**:
   - Валидация канонических спецификаций и активного предложения через `scripts/validate_openspec_specs.py`.
