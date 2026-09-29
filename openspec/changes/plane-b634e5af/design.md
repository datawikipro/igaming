# Design: [pod-patreon] Выделенный pod и агент Patreon

## Архитектура компонента `smm-bot-patreon`

### 1. Назначение и изоляция
- Выделенный Kubernetes Deployment `smm-bot-patreon` в неймспейсе `igaming-dev`.
- Полная изоляция учетной записи создателя Patreon от других SMM-ботов и краулеров.
- Автономный жизненный цикл, независимый мониторинг и раздельное управление секретами.

### 2. Персистентность сессии в Redis (`smm:session:patreon`)
- Ключ Redis: `smm:session:patreon`.
- Структура данных (JSON/Hash):
  ```json
  {
    "account_id": "patreon_creator_smartbet",
    "access_token": "...",
    "refresh_token": "...",
    "token_expires_at": 1727632800,
    "creator_id": "smartbet_patreon_id",
    "campaign_id": "smartbet_campaign_id",
    "webhook_secret": "...",
    "tiers": {
      "tier_pro_25": {"name": "Pro Arbitrageur", "amount_cents": 2500, "currency": "USD"},
      "tier_vip_100": {"name": "VIP Syndicate", "amount_cents": 10000, "currency": "USD"}
    },
    "last_sync_timestamp": 1727625600
  }
  ```
- При старте под считывает существующую сессию из Redis. При обновлении токенов или конфигурации состояние немедленно сохраняется обратно.

### 3. Сетевая маршрутизация (Rule 6)
- Patreon блокирует или ограничивает доступ с российских датацентровых и домашних IP.
- Все исходящие HTTP/HTTPS-запросы к `https://www.patreon.com` направляются через кластерный HTTP-прокси `http://100.83.113.50:3128`.
- Роутер `sing-box` на узле `ru-proxy` автоматически маршрутизирует трафик американских платформ через шлюз `outline-us` (`100.66.190.4`).
- Настройка через `HTTP_PROXY`, `HTTPS_PROXY` и параметры HTTP-клиентов.

### 4. Приём и валидация Webhook API v2
- Эндпоинт: `POST /webhooks/patreon`.
- Проверка безопасности:
  - Вычисление HMAC-SHA256 от сырого тела запроса (`raw_body`) с секретным ключом `webhook_secret`.
  - Сравнение с заголовком `X-Patreon-Signature` методом константного времени (`hmac.compare_digest`).
  - При несовпадении — немедленный возврат HTTP 403 Forbidden.
- Поддерживаемые события (`X-Patreon-Event`):
  - `members:pledge:create` — оформление новой платной подписки.
  - `members:pledge:update` — апгрейд/даунгрейд тарифа или смена статуса.
  - `members:pledge:delete` — отмена подписки.
  - `posts:comments:create` — комментарий патрона под постом.

### 5. Интеграция с Patron CRM и Feedback Desk
- Преобразование входящих событий и комментариев в структуру `feedback_item`:
  ```json
  {
    "source_platform": "PATREON",
    "source_message_id": "comment_12345",
    "patron_id": "patron_9876",
    "patron_name": "John Doe",
    "email": "john.doe@example.com",
    "is_paid": true,
    "currency": "USD",
    "patron_tier": "VIP Syndicate",
    "pledge_amount_cents": 10000,
    "donor_ltv": 30000,
    "priority": "P1_URGENT_PATRON",
    "category": "FEATURE_REQUEST",
    "content": "Please add Pinnacle vs BetMGM live odds alerts",
    "status": "NEW",
    "created_at": "2026-09-29T17:00:00Z"
  }
  ```
- Очередь тикетов: сохранение в Redis список `feedback:queue:patron` и отправка через REST API `igaming-portal` (`/api/v1/feedback/item`).

### 6. Публикация закрытого премиум-контента (Member Desk)
- API эндпоинт `/api/v1/patreon/posts` и программный метод `publish_premium_post`.
- Поддержка разграничения по уровням (gated tiers):
  - Тьер $25/mo: базовые вилки >10%, обзоры фрибетов (80% кэша).
  - Тьер $100/mo: эксклюзивные коридоры, мгновенные сигналы, персональные консультации.
- Ответ на комментарии: `reply_to_comment` для персонального уведомления патрона о реализации его фичи (замыкание цикла обратной связи).

### 7. Healthcheck & Kubernetes Deployment
- Встроенный сервер на базе Python `aiohttp` / `http.server` на порту 8080.
- Эндпоинты:
  - `GET /healthz` -> HTTP 200 OK `{"status": "UP", "service": "smm-bot-patreon"}`.
  - `GET /actuator/health` -> HTTP 200 OK `{"status": "UP"}`.
- Пробы k8s: `livenessProbe` и `readinessProbe` на порт 8080 path `/healthz`.
- Ресурсы: requests `100m / 256Mi`, limits `500m / 512Mi`.
