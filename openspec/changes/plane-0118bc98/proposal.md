# Proposal: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
<<<<<<< HEAD
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
=======
Plane Task ID: `0118bc98-e785-4ee1-9cd2-d0b8339ea879`
Sequence ID: `#1079`

## Problem Statement
В системе мониторинга зафиксирован инцидент о недоступности сервиса публикации постов `smm-bot-telegram` в Kubernetes namespace `igaming-dev`.
Необходимо провести комплексную верификацию сервиса, проверить статус пода и сетевую доступность Actuator healthcheck-проб, протестировать обработку запросов и интеграцию с Redis (`igaming-redis`) и Telegram Bot API под нагрузкой, а также убедиться в соблюдении архитектурных правил проекта (AGENTS.md, K8s DNS Service Names, проксирование через кластерный роутер `100.83.113.50:3128`).

## Proposed Changes
1. **Верификация работоспособности сервиса**:
   - Проверка статуса пода `smm-bot-telegram` в namespace `igaming-dev` (`1/1 Running`, 0 рестартов, аптайм > 60 мин).
   - Проверка Actuator health-проб (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` -> HTTP 200 UP).
   - Проверка конфигурации K8s Service `smm-bot-telegram` (порт 8080/TCP) и зависимостей (Redis, Bot API).
2. **Нагрузочное тестирование (Stress & Load Testing)**:
   - Проведение стресс-тестирования Actuator проб, Status API (`/api/v1/telegram/status`) и Webhook/AI-Prompter (`/api/v1/telegram/webhook`) с параллельностью 10 потоков и 130 запросами.
   - Проверка доставки сигналов с расчетом 80% гарантированного кэша фрибетов (Matched Betting) через `POST /api/v1/telegram/post`.
   - Проверка наполнения очереди обращений Patron CRM `feedback:queue:telegram` в Redis.
3. **OpenSpec валидация**:
   - Фиксация спецификаций и прохождение валидации `validate_openspec_specs.py`.
>>>>>>> feature/plane-6bbf15ee
