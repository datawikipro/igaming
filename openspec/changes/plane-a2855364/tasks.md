# Implementation Tasks: [HIGH LAG] Критическое отставание линии Spinbetter (643.1 мин)
- [x] 1. Implement [HIGH LAG] Критическое отставание линии Spinbetter (643.1 мин)
  - [x] 1.1 Верификация статуса подов igaming-source-spinbetter-crawler, igaming-source-spinbetter-loader и igaming-source-spinbetter-db-0 в K8s
  - [x] 1.2 Проверка Actuator health-проб (readiness/liveness HTTP 200 UP) и неблокирующего старта HikariCP
  - [x] 1.3 Проверка наполнения линии match_cache (порог >= 500 матчей: факт 1484 матча, из них 518 live)
  - [x] 1.4 Проверка ликвидации отставания в БД источника (lag < 5s) и доставки котировок в агрегатор (32 621 котировка в odds_actual, lag < 3s, heartbeat UP)
  - [x] 1.5 Проведение 5-минутного soak-теста стабильности без сбоев (аптайм лоадера > 5.8 часов, 0 рестартов)
  - [x] 1.6 Валидация OpenSpec артефактов и спецификаций
