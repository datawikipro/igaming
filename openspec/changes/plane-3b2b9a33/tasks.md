# Implementation Tasks: [STALE] Букмекер Бетсити перестал присылать данные (лаг 1250.6 мин)
- [x] 1. Аудит состояния подов и восстановление сбора данных Бетсити
  - [x] 1.1 Комплексный аудит подов igaming-source-betcity-crawler (2/2 Running, аптайм > 24ч) и igaming-source-betcity-loader (2/2 Running, аптайм > 24ч)
  - [x] 1.2 Проверка Actuator health-проб (/actuator/health, /actuator/health/readiness, /actuator/health/liveness HTTP 200 UP на порту 3041)
  - [x] 1.3 Проверка соответствия Golden Rules: запрет IP (Golden Rule 2), неблокирующий старт HikariCP (Golden Rule 4), Direct-маршрутизация РФ-букмекера (Golden Rule 6)
  - [x] 1.4 Верификация ликвидации лага: в БД источника lag < 1s, в БД агрегатора igaming_aggregator lag < 1s, статус bet_source is_active=true
- [ ] 2. Мониторинг стабильности сбора линии и 5-минутный soak-тест
- [ ] 3. Валидация OpenSpec и фиксация спецификаций
