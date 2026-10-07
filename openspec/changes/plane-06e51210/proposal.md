# Proposal: #1097: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
Plane Task ID: `06e51210-4e6b-4f16-8c58-bd2341f14089`

## Problem Statement
В системе мониторинга зафиксирован инцидент о недоступности сервиса публикации постов `smm-bot-telegram` в Kubernetes namespace `igaming-dev`.
Необходимо провести комплексную верификацию сервиса:
- проверить статус пода в Kubernetes (`1/1 Running`, 0 перезапусков);
- проверить сетевую доступность Actuator и healthcheck-проб (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`);
- протестировать обработку запросов и интеграцию с Redis (`igaming-redis`), очередью обращений Patron CRM `feedback:queue:telegram` и Telegram Bot API под параллельной нагрузкой;
- подтвердить соблюдение правил архитектуры и стандартов проекта (AGENTS.md, K8s DNS Service Names, проксирование через кластерный роутер `100.83.113.50:3128`, расчет 80% гарантированного кэша с фрибета по правилу 10);
- провести 5-минутный soak-контроль стабильности сервиса без ошибок в логах.

## Proposed Changes
1. **Верификация работоспособности сервиса**:
   - Проверка статуса пода `smm-bot-telegram` в namespace `igaming-dev` (`1/1 Running`, 0 рестартов, аптайм > 7 часов).
   - Проверка Actuator health-проб (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` -> HTTP 200 UP).
   - Проверка конфигурации K8s Service `smm-bot-telegram` (порт 8080/TCP) и сетевых зависимостей (Redis, Bot API).
   - Выполнение стресс-тестирования Actuator проб, Status API (`/api/v1/telegram/status`) и Webhook/AI-Prompter (`/api/v1/telegram/webhook`) с параллельностью 10 потоков и 130 запросами (100% success rate, 0 ошибок).
   - Проверка unit-тестов `smm-agent/tests/test_telegram_poster.py` (10/10 тестов успешно).
2. **Мониторинг стабильности и 5-минутный soak-контроль**:
   - Непрерывный мониторинг пода без ошибок в логах (`stdout`/`stderr`) в течение 5+ минут (Golden Rule 1 Soak Window).
   - Проверка публикации тестового сигнала с расчетом 80% кэша с фрибета через `POST /api/v1/telegram/post`.
3. **OpenSpec валидация**:
   - Фиксация спецификаций и успешное прохождение валидации `python3 scripts/validate_openspec_specs.py`.
