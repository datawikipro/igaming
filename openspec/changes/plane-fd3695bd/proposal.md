# Proposal: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
Plane Task ID: `fd3695bd`

## Problem Statement
В системе мониторинга зафиксирован алерт о недоступности сервиса публикации постов `smm-bot-telegram` в Kubernetes namespace `igaming-dev`.
Необходимо провести комплексную верификацию сервиса, проверить сетевую доступность Actuator и healthcheck-проб, протестировать обработку запросов и интеграцию с Redis (`igaming-redis`) и Telegram Bot API под нагрузкой, а также убедиться в соблюдении архитектурных правил проекта (AGENTS.md, K8s DNS Service Names, проксирование через кластерный роутер `100.83.113.50:3128`).

## Proposed Changes
1. **Верификация работоспособности сервиса**:
   - Проверка статуса пода `smm-bot-telegram` в namespace `igaming-dev` (`1/1 Running`, 0 рестартов, стабильный аптайм).
   - Проверка Actuator health-проб (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` -> HTTP 200 UP).
   - Проверка конфигурации K8s Service `smm-bot-telegram` (порт 8080/TCP) и зависимостей (Redis, Bot API) по K8s DNS Service Names.
2. **Нагрузочное тестирование (Stress & Load Testing)**:
   - Проведение стресс-тестирования Actuator проб, Status API (`/api/v1/telegram/status`) и Webhook/AI-Prompter (`/api/v1/telegram/webhook`) с параллельностью до 10 потоков и 130 запросами (100% success rate, 0 ошибок).
   - Проверка публикации арбитражных сигналов с формулой 80% гарантированного кэша фрибетов (Matched Betting) через `POST /api/v1/telegram/post`.
   - Проверка интеграции с Patron CRM и наполнения очереди `feedback:queue:telegram` в Redis.
3. **OpenSpec валидация**:
   - Фиксация артефактов изменения и успешное прохождение валидации `validate_openspec_specs.py`.
