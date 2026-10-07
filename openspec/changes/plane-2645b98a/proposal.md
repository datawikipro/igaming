# Proposal: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
Plane Task ID: `2645b98a`
Feature Branch: `feature/plane-2645b98a`

## Problem Statement
В системе мониторинга зафиксирован алерт о недоступности сервиса публикации постов `smm-bot-telegram` в Kubernetes namespace `igaming-dev`.
Необходимо провести комплексную верификацию сервиса, проверить статус пода и сетевую доступность Actuator healthcheck-проб, протестировать обработку запросов и интеграцию с Redis (`igaming-redis`) и Telegram Bot API под параллельной нагрузкой без вызовов внешних сторонних сервисов, а также убедиться в соблюдении архитектурных правил проекта (AGENTS.md, K8s DNS Service Names, проксирование через кластерный роутер `100.83.113.50:3128`, 5-минутный soak-контроль).

## Proposed Changes
1. **Верификация работоспособности сервиса**:
   - Проверка статуса пода `smm-bot-telegram` в namespace `igaming-dev` (`1/1 Running`, 0 рестартов, аптайм > 4 ч).
   - Проверка Actuator health-проб (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` -> HTTP 200 UP).
   - Проверка конфигурации K8s Service `smm-bot-telegram` (порт 8080/TCP) и зависимостей (Redis `igaming-redis`, Bot API).
2. **Тестирование без вызова сторонних сервисов (Stress & Load Testing & Mock/Unit)**:
   - Проведение стресс-тестирования Actuator проб, Status API (`/api/v1/telegram/status`) и Webhook/AI-Prompter (`/api/v1/telegram/webhook`) с параллельностью 10 потоков и 130 запросами (100% success rate, 0 ошибок).
   - Прогон unit-тестов математики фрибета (80% guaranteed cash SNR), мультиязычных шаблонов (RU, EN, FR, ES) и очередей Patron CRM без обращения к внешним API.
   - Проверка доставки сигналов с расчетом 80% гарантированного кэша фрибетов (Matched Betting) через `POST /api/v1/telegram/post` в fallback/резильентном режиме.
3. **OpenSpec валидация**:
   - Фиксация спецификаций и прохождение валидации `validate_openspec_specs.py`.
