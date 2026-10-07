# Design: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Архитектура компонента `smm-bot-telegram`

### 1. Назначение и изоляция
- Выделенный микросервис и K8s Deployment `smm-bot-telegram` в неймспейсе `igaming-dev`.
- Управление мультиязычной сетью каналов Telegram (RU, EN, FR, ES) и зеркалирование сигналов в Discord Webhook.
- Обеспечение непрерывной доступности, обработка вебхуков обратной связи и интеграция с Patron CRM / Redis.
- Полное соблюдение **Rule 1** (DoD, Actuator пробы, 5-минутный soak), **Rule 2** (DNS Service Names), **Rule 6** (Сетевая маршрутизация через кластерный HTTP-прокси `http://100.83.113.50:3128`), **Rule 10** (80% Guaranteed Cash для фрибетов, партнерские ссылки и дисклеймеры).

### 2. Спецификация HTTP эндпоинтов и Actuator проб
Сервер работает на порту 8080:
- `GET /healthz` — базовый k8s liveness/readiness probe:
  ```json
  {
    "status": "UP",
    "service": "smm-bot-telegram",
    "checks": {
      "telegram_bot": "UP",
      "channels": 4,
      "redis": "UP"
    },
    "timestamp": "2026-10-07T17:30:00Z"
  }
  ```
- `GET /actuator/health` — Spring Boot Actuator совместимый эндпоинт (HTTP 200 `UP`).
- `GET /actuator/health/readiness` — K8s readiness probe (HTTP 200 `UP`).
- `GET /actuator/health/liveness` — K8s liveness probe (HTTP 200 `UP`).
- `GET /api/v1/telegram/status` — текущее состояние каналов, счетчик опубликованных постов и конфигурация.
- `POST /api/v1/telegram/webhook` / `POST /webhook` — приём апдейтов от Telegram и комментариев из групп обсуждений для ИИ-суфлера.
- `POST /api/v1/telegram/post` — ручной / внешний триггер публикации сигнала в каналы.

### 3. Резильентный неблокирующий старт (Non-blocking Startup Sequence)
Для предотвращения падений `Readiness probe failed: connection refused`:
1. **Немедленный запуск HTTP-сервера**:
   - HTTP-сервер стартует сразу после базовой конфигурации.
   - Потоки `serve_forever` начинают принимать входящие соединения без ожидания сетевых вызовов к внешним API Telegram или базам данных.
2. **Отложенный первый запуск шедулера**:
   - Фоновый поток `start_scheduler_loop()` выдерживает начальную паузу перед первым вызовом `broadcast_cycle()`.
   - Это гарантирует, что K8s успеет успешно опросить `/actuator/health/readiness` и перевести под в статус `Ready 1/1` до выполнения тяжелых внешних операций.
3. **Безопасная обработка зависимостей**:
   - Исключение блокирующих вызовов `pip install` в runtime.
   - Корректная деградация (graceful degradation): при недоступности PostgreSQL используются кэшированные/статические генераторы сигналов без падения процесса.

### 4. Сетевая топология и устойчивость Telegram API (Rule 6)
- Исходящие запросы к `https://api.telegram.org` и `https://discord.com` осуществляются через прокси:
  - `HTTP_PROXY=http://100.83.113.50:3128`
  - `HTTPS_PROXY=http://100.83.113.50:3128`
- Внутренние сервисы исключены через `NO_PROXY`:
  - `NO_PROXY=localhost,127.0.0.1,10.0.0.0/8,igaming-redis,igaming-portal,.svc.cluster.local`
- Механизм повторных попыток (Retries):
  - При возникновении сетевых таймаутов (`The handshake operation timed out`) запросы повторяются с экспоненциальной задержкой.
  - В случае временной недоступности внешней сети формируется защищенный лог без фатального завершения процесса.

### 5. Декларативный K8s Манифест (`igaming-k8s/smm-bot-telegram.yaml`)
Манифест включает:
1. `ConfigMap` `smm-bot-telegram-code`:
   - `channel_poster_scheduler.py`
   - `telegram_web_manager.py`
2. `Deployment` `smm-bot-telegram`:
   - Namespace: `igaming-dev`
   - Реплики: 1
   - NodeAffinity: предпочтение нод со статусом `standard` / `spot`
   - Ресурсы: limits `cpu: 500m`, `memory: 512Mi`; requests `cpu: 100m`, `memory: 128Mi`
   - Пробы:
     - `readinessProbe`: path `/healthz`, port 8080, `initialDelaySeconds: 15`, `periodSeconds: 5`, `timeoutSeconds: 2`, `failureThreshold: 3`
     - `livenessProbe`: path `/healthz`, port 8080, `initialDelaySeconds: 30`, `periodSeconds: 10`, `timeoutSeconds: 3`, `failureThreshold: 3`
3. `Service` `smm-bot-telegram`:
   - ClusterIP
   - Port: 8080 -> TargetPort: 8080

### 6. Стратегия тестирования (`smm-agent/tests/test_telegram_poster.py`)
Набор юнит- и интеграционных тестов:
1. `test_actuator_health_readiness_liveness()`: проверка HTTP 200 и схемы JSON для `/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`.
2. `test_freebet_80_percent_guaranteed_cash()`: проверка математической конвертации $\eta = \frac{(K_1 - 1)(K_2 - 1)}{K_2} \approx 0.80$ и корректности карточки фрибета по Rule 10.
3. `test_multilingual_card_formatter()`: проверка генерации постов на RU, EN, FR, ES с обязательными дисклеймерами и партнерскими ссылками с UTM.
4. `test_resilient_network_retries()`: проверка устойчивости к таймаутам сети и корректного логирования.
