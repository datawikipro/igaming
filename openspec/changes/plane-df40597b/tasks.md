# Implementation Tasks: [SUPER-ARB] Аномальная вилка 10.5% с участием Stoiximan
- [x] 1. Верификация источника данных Stoiximan и работоспособности сервиса
  - [x] 1.1 Контроль статуса подов в K8s namespace igaming-source (igaming-source-stoiximan Running 1/1, igaming-source-stoiximan-db-0 Running 1/1, 0 сбоев, аптайм 18h)
  - [x] 1.2 Проверка Actuator health-проб (/actuator/health/readiness и /actuator/health/liveness -> HTTP 200 UP)
  - [x] 1.3 Выполнение критерия наполнения линии (Golden Rule 8: порог >= 500 матчей, факт: 1196 матчей в match_cache, 960 обновлены за последние 10 мин)
  - [x] 1.4 Верификация доставки котировок в igaming-aggregator (12 461 котировок в odds_actual, 1795 обновлены за последние 5 мин, bet_source активен с лагом < 10 сек)
  - [x] 1.5 Контроль стандартов AGENTS.md (неблокирующий HikariCP initialization-fail-timeout=0, K8s DNS Service Names, synchronous_commit=off, ddl-auto=update)
- [ ] 2. Анализ аномального арбитража и валидация маппинга исходов
- [ ] 3. OpenSpec валидация и завершение задачи
