# Design: [feedback-nlp] ИИ-классификатор, суммаризация обращений и генерация черновиков ответов (LLM Gateway)

## Architecture & Integration

### 1. Архитектура подсистемы NLP и обратной связи в `igaming-portal`
- **Доменные сущности (JPA)**:
  - `FeedbackCategory`: `FEATURE_REQUEST`, `BUG_REPORT`, `QUESTION`, `PRAISE`, `NOISE`.
  - `FeedbackPriority`: `P1_URGENT_PATRON`, `P2_STANDARD_PATRON`, `P3_FREE_VALUABLE`, `P4_FREE_NOISE`.
  - `FeedbackSentiment`: `POSITIVE`, `NEUTRAL`, `NEGATIVE`.
  - `FeedbackStatus`: `NEW`, `IN_PROGRESS`, `IN_PLANE`, `RESOLVED`, `ARCHIVED`.
  - `FeedbackSourcePlatform`: `BOOSTY`, `PATREON`, `VK_DONUT`, `TELEGRAM_VIP`, `SITE`, `THREADS`, `INSTAGRAM`, `REDDIT`, `OTHER`.
  - `CommunityIdentity`: профиль донатера/пользователя (user_id, telegram_id, boosty_user_id, patreon_id, vk_id, is_paid, vip_tier, monthly_spend, total_donated, badges, admin_notes).
  - `PatronSubscription`: история и статусы подписок (community_identity_id, platform, tier, status, amount, currency, dates).
  - `FeedbackItem`: тикет обратной связи со всеми атрибутами классификации, суммаризации и черновика ответа.

### 2. Сервис NLP классификации (`FeedbackNlpService`)
- Взаимодействие с `llm-gateway` через REST API (`POST /api/v1/llm/generate` или `POST /api/v1/igaming/generate`).
- Специализированный системный промпт с Few-Shot примерами для строгого JSON-вывода.
- Калькулятор приоритета:
  - `isPaid && (category == BUG_REPORT || category == FEATURE_REQUEST)` $\rightarrow$ `P1_URGENT_PATRON`
  - `isPaid` $\rightarrow$ `P2_STANDARD_PATRON`
  - `category == NOISE` $\rightarrow$ `P4_FREE_NOISE`
  - Иначе $\rightarrow$ `P3_FREE_VALUABLE`
- Надежный эвристический fallback-парсер с проверкой ключевых паттернов и сентимента для гарантированного SLA < 2 секунд даже при сетевых сбоях или тайм-аутах LLM.

### 3. REST API (`FeedbackController`)
- `POST /api/v1/feedback/analyze`: анализ произвольного текста обращения.
- `POST /api/v1/feedback/submit`: создание тикета с мгновенным NLP-анализом и сохранением в БД.
- `GET /api/v1/feedback`: получение списка тикетов с пагинацией и фильтрами по статусу, приоритету, категории и статусу донатера.
- `GET /api/v1/feedback/{id}`: детальная информация по тикету.
- `POST /api/v1/feedback/{id}/reanalyze`: повторный запуск NLP-анализа.

### 4. Верификация
- Пакет юнит-тестов `FeedbackNlpServiceTest` на 20 эталонных сообщениях проверяет точность классификации > 90% и время обработки < 2 секунд.
