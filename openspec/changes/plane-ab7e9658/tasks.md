# Implementation Tasks: [STALE] Букмекер FanSport перестал присылать данные (лаг 17.3 мин)
- [x] 1. Верификация источника данных и работоспособности сервиса FanSport
  - [x] 1.1 Проверка статуса подов igaming-source-fansport-crawler, igaming-source-fansport-loader и базы данных igaming-source-fansport-db-0 в K8s
  - [x] 1.2 Проверка Actuator health-проб (readiness/liveness HTTP 200 UP) и неблокирующего старта HikariCP
  - [x] 1.3 Проверка наполнения линии match_cache (порог >= 500 матчей: факт 1925 матчей, из них 850 live)
  - [x] 1.4 Проверка ликвидации лага в БД источника (lag < 1s) и доставки котировок в агрегатор (85120+ котировок в odds_actual, lag < 1s)
- [ ] 2. Мониторинг стабильности сбора линии и 5-минутный soak-тест
- [ ] 3. Валидация OpenSpec и фиксация спецификаций
