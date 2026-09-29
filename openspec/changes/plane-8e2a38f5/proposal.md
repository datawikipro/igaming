# Proposal: [feedback-core] Подсистема хранения и модель данных (PostgreSQL JPA): community_identity, patron_subscription, feedback_item

## Context
Plane Task ID: `8e2a38f5-5918-4121-b819-a1e1fceb685f`
Service: `igaming-portal`
Database: PostgreSQL (`smartbet_portal` / `igaming_portal`)

## Description
Разработка реляционной модели данных и JPA-сущностей в сервисе `igaming-portal` для централизованного сбора и обработки обратной связи со всех каналов и платформ монетизации (Boosty, Patreon, VK Donut, Telegram VIP, Site PRO) и бесплатных пользователей.

### Архитектурные требования:
1. **Сквозной профиль донатера (`CommunityIdentity`)**:
   - `id`: первичный идентификатор (IDENTITY/BIGINT).
   - `user_id`: опциональный ID пользователя веб-портала.
   - `telegram_id`: Telegram ID (BIGINT).
   - `boosty_user_id`: ID/username в Boosty.
   - `patreon_id`: ID пользователя Patreon.
   - `vk_id`: ID во ВКонтакте (BIGINT).
   - `display_name`, `email`.
   - `is_paid`: флаг активного платного статуса.
   - `vip_tier`: уровень подписки (FREE, STANDARD_PATRON, VIP_PATRON, SITE_PRO, WHALE).
   - `monthly_spend`: месячные траты на подписки.
   - `total_donated`: суммарный объем донатов (LTV) для расчета приоритета SLA.
   - `badges`: бейджи пользователя (JSON или comma-separated).
   - `admin_notes`: внутренние заметки саппорта/администратора.
   - `created_at`, `updated_at`.

2. **Подписки и донаты (`PatronSubscription`)**:
   - `id`: первичный идентификатор.
   - `identity`: связь с `CommunityIdentity`.
   - `platform`: платформа (`BOOSTY`, `PATREON`, `VK_DONUT`, `TELEGRAM_VIP`, `SITE_PRO`).
   - `tier`: наименование тарифа.
   - `status`: статус (`ACTIVE`, `EXPIRED`, `CANCELLED`, `PENDING`).
   - `amount`, `currency`: сумма и валюта платежа.
   - `external_sub_id`: идентификатор подписки во внешней системе.
   - `start_date`, `end_date`: период действия подписки.
   - `created_at`, `updated_at`.

3. **Тикеты обратной связи (`FeedbackItem`)**:
   - `id`: первичный идентификатор (UUID).
   - `identity`: связь с `CommunityIdentity` (nullable для анонимных обращений).
   - `source_platform`: источник (`BOOSTY`, `PATREON`, `VK_DONUT`, `TELEGRAM_VIP`, `SITE_FEEDBACK`, `DISCORD`, `EMAIL`).
   - `source_message_id`: внешний ID сообщения/комментария.
   - `author_name`: отображаемое имя автора.
   - `is_paid`: флаг платного донатера (индексирован).
   - `patron_tier`: уровень тарифа автора на момент создания тикета.
   - `donor_ltv`: LTV автора на момент создания.
   - `category`: категория (`FEATURE_REQUEST`, `BUG_REPORT`, `QUESTION`, `PRAISE`, `NOISE`).
   - `priority`: приоритет обработки (`P1_VIP`, `P2_PAID`, `P3_FREE`, `P4_NOISE`).
   - `content`: текст обращения.
   - `ai_summary`: сжатая выжимка сути обращения (до 500 символов).
   - `status`: статус (`NEW`, `IN_REVIEW`, `IN_PROGRESS`, `RESOLVED`, `REJECTED`, `ARCHIVED`).
   - `plane_issue_id`: идентификатор задачи в Plane.
   - `plane_issue_url`: URL задачи в Plane.
   - `ai_draft_reply`: черновик ответа, сгенерированный ИИ.
   - `admin_reply`: финальный ответ администратора.
   - `replied_at`: дата и время ответа.
   - `created_at`, `updated_at`.

4. **Spring Data JPA Репозитории и Спецификации**:
   - `CommunityIdentityRepository` с методами поиска по внешним ID (`telegram_id`, `boosty_user_id`, `patreon_id`, `vk_id`, `user_id`).
   - `PatronSubscriptionRepository` для выборки активных подписок.
   - `FeedbackItemRepository` с динамической фильтрацией через JPA Specification и пагинацией.

5. **Сервисный уровень и REST API**:
   - `CommunityFeedbackService`: логика регистрации/привязки аккаунтов, учета подписок и обновления LTV, автоматического определения приоритета тикета, создания и фильтрации тикетов.
   - `PortalFeedbackController`: эндпоинты `/api/v1/feedback/**` и `/api/v1/community/**`.

6. **DDL и Миграция**:
   - SQL DDL скрипт `schema-feedback.sql` с индексами по ключевым полям (`is_paid`, `priority`, `status`, `created_at`).
