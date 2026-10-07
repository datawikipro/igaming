# Implementation Tasks: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен
- [x] 1. Implement [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен
  - [x] 1.1 Верификация статуса пода smm-bot-telegram (1/1 Running, аптайм > 4 ч, 0 рестартов) в Kubernetes namespace igaming-dev
  - [x] 1.2 Проверка Actuator health-проб (/healthz, /actuator/health, /actuator/health/readiness, /actuator/health/liveness — HTTP 200 UP)
  - [x] 1.3 Нагрузочное тестирование под параллельной нагрузкой (130 запросов, concurrency 10, 100% success rate, 0 ошибок)
  - [x] 1.4 Верификация сетевой связности с Redis (feedback:queue:telegram) и Telegram Bot API через кластерный HTTP-прокси (100.83.113.50:3128)
- [x] 2. Мониторинг стабильности и 5-минутный soak-контроль работы сервиса публикаций
- [x] 3. Валидация OpenSpec и фиксация спецификаций
