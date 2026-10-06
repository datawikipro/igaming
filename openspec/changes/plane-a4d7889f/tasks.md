# Implementation Tasks: [SUPER-ARB] Аномальная вилка 32.9% с участием BetAndYou
- [x] 1. Исследование источника данных и верификация линии BetAndYou
  - [x] 1.1 Верификация статуса подов igaming-source-betandyou-crawler, igaming-source-betandyou-loader и базы данных igaming-source-betandyou-db в K8s (Running 2/2, 1/1, 0 рестартов)
  - [x] 1.2 Проверка Actuator health-проб (readiness/liveness HTTP 200 UP на порту 3053) и неблокирующего старта HikariCP
  - [x] 1.3 Проверка наполнения линии match_cache (порог >= 500 матчей: факт 1446 матчей, 1117 обновлены за последние 5 минут)
  - [x] 1.4 Проверка актуальности данных в БД источника и доставки котировок в aggregator (28008 котировок в odds_actual, 2568 за последние 5 мин, bet_source is_active=true, lag < 1 мин)
- [ ] 2. Анализ аномальных арбитражей (EXTREME_SUREBET) и валидация маппинга исходов
- [ ] 3. Валидация OpenSpec и фиксация отчета задачи
