<<<<<<< HEAD
# Proposal: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
Plane Task ID: `8f4b939f-411a-46c0-9b70-741b26c028df`

## Problem Statement
В системе мониторинга зафиксирован алерт о недоступности сервиса публикации постов `smm-bot-telegram` в Kubernetes namespace `igaming-dev`.
Необходимо провести комплексную верификацию сервиса, проверить сетевую доступность Actuator и healthcheck-проб, протестировать обработку запросов и интеграцию с Redis (`igaming-redis`) и Telegram Bot API под нагрузкой, а также убедиться в соблюдении архитектурных правил проекта (AGENTS.md, K8s DNS Service Names, проксирование через кластерный роутер `100.83.113.50:3128`).

## Proposed Changes
1. **Верификация работоспособности сервиса**:
   - Проверка статуса пода `smm-bot-telegram` в namespace `igaming-dev` (`1/1 Running`, 0 рестартов).
   - Проверка Actuator health-проб (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` -> HTTP 200 UP).
   - Проверка конфигурации K8s Service `smm-bot-telegram` (порт 8080/TCP) и зависимостей (Redis, Bot API).
2. **Нагрузочное тестирование (Stress & Load Testing)**:
   - Проведение стресс-тестирования Actuator проб, Status API (`/api/v1/telegram/status`) и Webhook/AI-Prompter (`/api/v1/telegram/webhook`) с параллельностью 10 потоков и 130 запросами.
   - Проверка доставки сигналов с расчетом 80% гарантированного кэша фрибетов (Matched Betting) через `POST /api/v1/telegram/post`.
   - Проверка наполнения очереди обращений Patron CRM `feedback:queue:telegram` в Redis.
3. **OpenSpec валидация**:
   - Фиксация спецификаций и прохождение валидации `validate_openspec_specs.py`.
=======
# Proposal: #998: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
Plane Task ID: `8f4b939f-9269-4bcf-af18-c1f34bc2816d`

## Description

>>>>>>> feature/plane-8f4b939f
