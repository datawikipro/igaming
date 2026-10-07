# Architectural Design: #1097: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
Plane Task ID: `06e51210-4e6b-4f16-8c58-bd2341f14089`

Модуль SMM Telegram (`smm-bot-telegram`) развернут в Kubernetes namespace `igaming-dev` и отвечает за автоматизированную мультиязычную публикацию сигналов арбитража, +EV исходов и акций с расчетом 80% гарантированного кэша (Rule 10), а также за обработку комментариев и обратной связи пользователей в каналах вещания экосистемы SmartBet.guru:
- RU: `@SmartBetGuru` (ID: `-1002244889900`)
- EN: `@SmartBetGuruEN` (ID: `-1003960368887`)
- FR: `@SmartBetGuruFR` (ID: `-1004371643544`)
- ES: `@SmartBetGuruES` (ID: `-1004346736376`)

В результате инцидента зафиксирован алерт `[SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен`.

## Architecture & Invariants

### 1. Сервис smm-bot-telegram в Kubernetes
- **Namespace**: `igaming-dev`
- **Pod**: `smm-bot-telegram` (ReplicaSet `smm-bot-telegram-79cf4995d8`, 1/1 Running)
- **Порт HTTP API**: 8080 (ClusterIP сервис `smm-bot-telegram:8080`)
- **DNS зависимости**:
  - `redis://igaming-redis.igaming-dev.svc.cluster.local:6379/0` (строго K8s DNS Service Name, Golden Rule 2)
  - Кластерный прокси: `http://100.83.113.50:3128` для исходящих запросов к Telegram Bot API (`https://api.telegram.org`)
  - `NO_PROXY`: `localhost,127.0.0.1,10.0.0.0/8,igaming-redis,igaming-portal,.svc.cluster.local`

### 2. Поддерживаемые API Endpoints
- `GET /healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`: Actuator health probes (UP, 200 OK)
- `GET /api/v1/telegram/status`: статус флота региональных каналов (ru, en, fr, es)
- `POST /api/v1/telegram/post`: ручная или регламентная публикация арбитражных сигналов с формулой 80% кэша с фрибета
- `POST /api/v1/telegram/webhook`: приём входящих сообщений/комментариев из чатов, генерация AI ответа и постановка задачи в Patron CRM очередь Redis (`feedback:queue:telegram`)

## Data Flow Diagram

```mermaid
graph TD
    A["igaming-aggregator / Redis"] -->|Surebet & Freebet Signals| B["smm-bot-telegram (ChannelPosterScheduler)"]
    B -->|Calculate 80% Cash SNR| C["LocalizedCardFormatter (RU, EN, FR, ES)"]
    C -->|Cluster Proxy 100.83.113.50:3128| D["Telegram Bot API"]
    D -->|Post to Channels| E["Telegram Channels (@SmartBetGuru*)"]
    E -->|User Comments| F["Telegram Discussion Groups"]
    F -->|Webhook /api/v1/telegram/webhook| B
    B -->|AI Prompter Categorization| G["Patron CRM Redis Queue (feedback:queue:telegram)"]
```
