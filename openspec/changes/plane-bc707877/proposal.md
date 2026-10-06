# Proposal: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
- **Plane Task ID**: `bc707877-5c7f-4a4c-aa86-92c7d40f58f2`
- **Severity**: High / SMM Offline
- **Component**: `smm-bot-telegram` (Kubernetes namespace `igaming-dev`)

## Problem Description
Контейнер `smm-bot-telegram` в namespace `igaming-dev` не отвечал на healthcheck запросы в рабочее время (09:00 - 23:00 MSK). Вследствие этого K8s liveness/readiness пробы завершались ошибкой, приводя к недоступности сервиса публикации контента, сигналов и обучающих материалов в региональные Telegram-каналы SmartBet.guru (`@smartbet_guru_bot`, каналы EN, FR, ES, RU).

## Proposed Solution
1. **Диагностика и восстановление сервиса**:
   - Восстановить HTTP-сервер на порту 8080 с корректными эндпоинтами проверки работоспособности: `/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`.
   - Обеспечить надежное соединение с Redis (`redis://igaming-redis.igaming-dev.svc.cluster.local:6379/0`) с поддержкой автономного fallback-режима.
   - Настроить сетевую маршрутизацию с использованием K8s DNS и кластерного прокси (`http://100.83.113.50:3128`).
2. **Артефакты и кодовая база**:
   - Синхронизировать исполняемые скрипты агента публикации и планировщика (`channel_poster_scheduler.py`, `telegram_web_manager.py`) в модуль `smm-agent/`.
   - Сохранить полный декларативный Kubernetes-манифест `igaming-k8s/smm-bot-telegram.yaml` (ConfigMap с кодом, Service, Deployment с affinity и ресурсами).
3. **Верификация и соблюдение DoD**:
   - Убедиться, что под находится в статусе `Running 1/1`.
   - Проверить успешный ответ HTTP 200 `UP` от actuator/healthcheck эндпоинтов.
   - Выдержать обязательный 5-минутный период бессбойной работы (Soak Test Window).
