# Implementation Tasks: #951: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен
- [x] 1. Верификация статуса пода и работоспособности сервиса smm-bot-telegram
  - [x] 1.1 Проверка статуса подов smm-bot-telegram в K8s namespace igaming-dev (Running 1/1, 0 рестартов, аптайм > 45 мин)
  - [x] 1.2 Проверка проб /healthz и Actuator health (/actuator/health HTTP 200 UP)
  - [x] 1.3 Проверка сетевой связности с Redis (igaming-redis) и Telegram Bot API через кластерный HTTP-прокси
  - [x] 1.4 Верификация тестов smm-agent/tests/test_telegram_poster.py (10/10 тестов успешно) и синхронизация манифеста igaming-k8s/smm-bot-telegram.yaml
- [x] 2. Мониторинг стабильности публикации постов и 5-минутный soak-тест
  - [x] 2.1 Непрерывный мониторинг пода в течение 5+ минут (Golden Rule 1 Soak Window: аптайм 60+ мин, 0 рестартов, отсутствие ошибок/исключений в stdout/stderr)
  - [x] 2.2 Проверка эндпоинта /api/v1/telegram/status и успешная публикация тестового сигнала через /api/v1/telegram/post (msg_id: 12)
  - [x] 2.3 Прохождение всех unit-тестов smm-agent/tests/test_telegram_poster.py (10/10 тестов OK)
- [x] 3. Валидация OpenSpec и фиксация спецификаций
  - [x] 3.1 Валидация канонических спецификаций и активного предложения через scripts/validate_openspec_specs.py (12/12 спецификаций и артефакты plane-ea3f45c1 валидны)
  - [x] 3.2 Устранение артефактов слияния в openspec/specs/social-media-bot/spec.md и подтверждение целостности всех спецификаций
  - [x] 3.3 Проверка соответствия Definition of Done (Golden Rule 1) и стандартам экосистемы AGENTS.md

