# Design: smm-bot-telegram Service Architecture & Recovery

## Architecture Overview

`smm-bot-telegram` — автономный агент публикации контента и управления флотом каналов Telegram проекта SmartBet.guru.
Сервис развернут в кластере Kubernetes в namespace `igaming-dev` и выполняет:
1. Регулярную публикацию спортивных аналитических постов, вилочных сигналов (+EV / surebets) и расчетов 80% кэша с фрибетов по региональным каналам:
   - RU (`@smartbet_guru_ru`)
   - EN (`@smartbet_guru_en`)
   - ES (`@smartbet_guru_es`)
   - FR (`@smartbet_guru_fr`)
2. Взаимодействие с Telegram Bot API (`@smartbet_guru_bot`) и Telegram Web Persistent Sessions.
3. Хранение истории публикаций и сессий в Redis (`smm:session:telegram:*`, `smm:events:telegram:*`).

```
+-------------------------------------------------------------+
|                     smm-bot-telegram                        |
|                                                             |
|  +---------------------------+  +------------------------+  |
|  | ChannelPosterScheduler    |  | TelegramWebManager     |  |
|  | - Publication timetable   |  | - Session persistence  |  |
|  | - 80% freebet templates   |  | - Channel audit        |  |
|  +-------------+-------------+  +-----------+------------+  |
|                |                            |               |
|  +-------------v----------------------------v------------+  |
|  | Embedded HTTPServer (:8080)                           |  |
|  | - /healthz, /actuator/health                          |  |
|  | - /actuator/health/readiness, /liveness               |  |
|  | - /api/v1/telegram/status, /webhook                   |  |
|  +-------------------------------------------------------+  |
+-------------------------------------------------------------+
               |                               |
       K8s DNS Service                  HTTP Proxy (Rule 6)
       igaming-redis:6379               100.83.113.50:3128
```

## Healthcheck & Probes Design

Для устранения дефекта недоступности внедрен встроенный многопоточный HTTP-сервер:
- Порт: `8080`
- Поддерживаемые эндпоинты:
  - `/healthz` — K8s readiness/liveness probe (HTTP 200 `{"status": "UP", "service": "smm-bot-telegram", ...}`)
  - `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` — Actuator-совместимые пробы для стандартов экосистемы
  - `/api/v1/telegram/status` — операционный статус каналов и очереди постов
- Неблокирующий запуск в фоновом демоне (`threading.Thread`) параллельно с планировщиком публикаций.

## Network & K8s Configuration
- Сервисное имя K8s DNS: `igaming-redis.igaming-dev.svc.cluster.local` (строгий запрет хардкода IP-адресов по Правилу 2).
- Выделенный прокси: `http://100.83.113.50:3128` с обходом локальной сети через `NO_PROXY`.
- Ресурсные лимиты: requests `cpu: 100m, memory: 128Mi`, limits `cpu: 500m, memory: 512Mi`.
- Node Affinity: приоритет размещения на `xeon-srv` (`standard` / `spot`).
