# Proposal: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
Plane Task ID: `bc707877-5c7f-4a4c-aa86-92c7d40f58f2`

## Problem Statement
<<<<<<< HEAD
В системе мониторинга зафиксирован инцидент с недоступностью сервиса публикации постов `smm-bot-telegram` в Kubernetes namespace `igaming-dev`.
Необходимо провести диагностику и комплексную верификацию сервиса: проверить статус пода и отсутствие рестартов, проверить сетевую доступность Actuator и healthcheck-проб (/healthz, /actuator/health, /actuator/health/readiness, /actuator/health/liveness), провести нагрузочное тестирование и подтвердить корректность интеграции с Redis (`igaming-redis`) и Telegram Bot API через кластерный HTTP-прокси `100.83.113.50:3128`.

## Proposed Changes
1. **Верификация работоспособности сервиса и нагрузочное тестирование**:
   - Проверка статуса пода `smm-bot-telegram` в namespace `igaming-dev` (`1/1 Running`, аптайм > 130 мин, 0 рестартов).
   - Проверка Actuator health-проб (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` -> HTTP 200 UP).
   - Проверка K8s Service `smm-bot-telegram` (порт 8080/TCP) и зависимостей (Redis, Bot API).
   - Стресс-тестирование под параллельной нагрузкой (130 запросов, concurrency 10, 100% success rate, 0 ошибок) через `scripts/test_smm_telegram_load.py`.
2. **Мониторинг стабильности и 5-минутный soak-контроль**:
   - 5-минутный soak-контроль работы сервиса публикаций без ошибок (0 NPE, 0 Crash, 0 OOMKilled).
   - Проверка наполнения очереди обращений Patron CRM `feedback:queue:telegram` в Redis.
3. **OpenSpec валидация и стандартизация**:
   - Формирование полного комплекта спецификаций OpenSpec (`.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`).
   - Валидация спецификаций скриптом `scripts/validate_openspec_specs.py`.
=======
В системе мониторинга зафиксирован инцидент о недоступности сервиса публикации постов `smm-bot-telegram` в Kubernetes namespace `igaming-dev`.
Необходимо провести комплексную верификацию сервиса, проверить статус пода и сетевую доступность Actuator healthcheck-проб, протестировать обработку запросов и интеграцию с Redis (`igaming-redis`) и Telegram Bot API под нагрузкой, а также убедиться в соблюдении архитектурных правил проекта (AGENTS.md, K8s DNS Service Names, проксирование через кластерный роутер `100.83.113.50:3128`).

## Proposed Changes
1. **Верификация работоспособности сервиса**:
   - Проверка статуса пода `smm-bot-telegram` в namespace `igaming-dev` (`1/1 Running`, 0 рестартов, аптайм > 50 мин).
   - Проверка Actuator health-проб (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` -> HTTP 200 UP).
   - Проверка конфигурации K8s Service `smm-bot-telegram` (порт 8080/TCP) и зависимостей (Redis, Bot API).
2. **Нагрузочное тестирование (Stress & Load Testing)**:
   - Проведение стресс-тестирования Actuator проб, Status API (`/api/v1/telegram/status`) и Webhook/AI-Prompter (`/api/v1/telegram/webhook`) с параллельностью 10 потоков и 130 запросами.
   - Проверка доставки сигналов с расчетом 80% гарантированного кэша фрибетов (Matched Betting) через `POST /api/v1/telegram/post`.
   - Проверка наполнения очереди обращений Patron CRM `feedback:queue:telegram` в Redis.
3. **OpenSpec валидация**:
   - Фиксация спецификаций и прохождение валидации `validate_openspec_specs.py`.
>>>>>>> feature/plane-6bbf15ee
