# Implementation Tasks: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен
- [x] 1. Implement [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен
  - [x] 1.1 Верификация статуса пода smm-bot-telegram (1/1 Running, аптайм > 4 ч, 0 рестартов) в Kubernetes namespace igaming-dev
  - [x] 1.2 Проверка Actuator health-проб (/healthz, /actuator/health, /actuator/health/readiness, /actuator/health/liveness — HTTP 200 UP)
  - [x] 1.3 Нагрузочное тестирование без вызова сторонних сервисов (130 запросов, concurrency 10, 100% success rate, 0 ошибок)
  - [x] 1.4 Модульное тестирование математики фрибета (80% cash), мультиязычных шаблонов и очереди Patron CRM (10 unit тестов OK)
  - [x] 1.5 Верификация связности с Redis (feedback:queue:telegram) и K8s DNS Service Name smm-bot-telegram
