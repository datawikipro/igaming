# Design: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
<<<<<<< HEAD
Plane Task ID: `0118bc98`
=======
Plane Task ID: `0118bc98-e785-4ee1-9cd2-d0b8339ea879`
Sequence ID: `#1079`
>>>>>>> feature/plane-6bbf15ee

## Architecture & Invariants

### 1. Сервис smm-bot-telegram в Kubernetes
- **Namespace**: `igaming-dev`
<<<<<<< HEAD
- **Pod**: `smm-bot-telegram` (1/1 Running, 0 рестартов)
- **Service**: ClusterIP `smm-bot-telegram:8080`
- **DNS зависимости**:
  - `redis://igaming-redis.igaming-dev.svc.cluster.local:6379/0` (строго K8s DNS Service Name, запрет hardcoded IP согласно Golden Rule 2)
=======
- **Pod**: `smm-bot-telegram` (ReplicaSet `smm-bot-telegram-79cf4995d8`, 1/1 Running)
- **Порт HTTP API**: 8080 (ClusterIP сервис `smm-bot-telegram:8080`)
- **DNS зависимости**:
  - `redis://igaming-redis.igaming-dev.svc.cluster.local:6379/0` (строго K8s DNS Service Name)
>>>>>>> feature/plane-6bbf15ee
  - Кластерный прокси: `http://100.83.113.50:3128` для исходящих запросов к Telegram Bot API (`https://api.telegram.org`)
  - `NO_PROXY`: `localhost,127.0.0.1,10.0.0.0/8,igaming-redis,igaming-portal,.svc.cluster.local`

### 2. Поддерживаемые API Endpoints
- `GET /healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`: Actuator health probes (UP, 200 OK)
- `GET /api/v1/telegram/status`: статус флота региональных каналов (ru, en, fr, es)
<<<<<<< HEAD
- `POST /api/v1/telegram/post`: ручная или регламентная публикация арбитражных сигналов с формулой 80% кэша с фрибета (Golden Rule 10)
=======
- `POST /api/v1/telegram/post`: ручная или регламентная публикация арбитражных сигналов с формулой 80% кэша с фрибета
>>>>>>> feature/plane-6bbf15ee
- `POST /api/v1/telegram/webhook`: приём входящих сообщений/комментариев из чатов, генерация AI ответа и постановка задачи в Patron CRM очередь Redis (`feedback:queue:telegram`)

```mermaid
graph TD
<<<<<<< HEAD
    A["Kubernetes Ingress / Clients"] -->|HTTP :8080| B["smm-bot-telegram (Python BaseHTTP)"]
=======
    A["Kubernetes Ingress / Client"] -->|HTTP :8080| B["smm-bot-telegram (Python/BaseHTTP)"]
>>>>>>> feature/plane-6bbf15ee
    B -->|Health Probes| C["/actuator/health (UP 200)"]
    B -->|Telegram Bot API via 100.83.113.50:3128| D["api.telegram.org"]
    D --> E["Regional Channels (@SmartBetGuru, @SmartBetGuruEN, @SmartBetGuruFR, @SmartBetGuruES)"]
    B -->|LPUSH feedback:queue:telegram| F["igaming-redis:6379 (Patron CRM Queue)"]
    F --> G["Patron CRM & Feedback Desk (Admin Frontend /accounts)"]
```
