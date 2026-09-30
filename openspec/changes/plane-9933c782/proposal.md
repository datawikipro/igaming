# Proposal: [plane-sync-loop] Двусторонняя синхронизация с Plane и авто-уведомление донатера о релизе фичи

## Context
Plane Task ID: `9933c782-591c-457a-8fc4-086ee040bc37`
Module: Patron CRM & Community Feedback Hub (`igaming-portal`, `igaming-bot`)

## Description
Построение замкнутого цикла обработки обратной связи:
обращение донатера (Коли) $\rightarrow$ создание задачи в Plane из админки в один клик $\rightarrow$ реализация ИИ-воркерами на ноде Xeon $\rightarrow$ деплой в K8s dev/prod $\rightarrow$ **автоматическое уведомление донатера в его персональный канал о готовности и релизе фичи**.

## Архитектурные требования

1. **Создание Issue в Plane из `igaming-portal`**:
   - Вызов REST API Plane (`http://plane-proxy.plane.svc.cluster.local:80/api/v1/workspaces/{workspace}/projects/{projectId}/issues/`).
   - Формирование названия: `[feature/...] Краткое название (Запрос от VIP-патрона {Имя})`.
   - Метки: `vip-patron`, `community-request`.
   - Сохранение связки: `plane_issue_id`, `plane_sequence_id` и `plane_issue_url` в сущности `FeedbackItem`.
   - Перевод тикета в статус `IN_PROGRESS`.

2. **Обратный Webhook из Plane (`POST /api/v1/feedback/webhook/plane`)**:
   - Перехват событий перехода задачи в статус `Завершено` (`Done`, state ID `e5f607b9-2f87-46c3-b4d0-243b3ca4a8c3` / `b1239dd6-2d97-432a-bf6f-8f5fa90d75ae`).
   - Поиск связанного тикета `FeedbackItem` по `plane_issue_id` или `plane_sequence_id`.
   - Автоматический перевод тикета в статус `RESOLVED` с фиксацией времени `replied_at`.

3. **Автоматическая нотификация патрона (`PatronNotificationService`)**:
   - Для запросов из **Telegram VIP**: персональная отправка сообщения через `igaming-bot` или прямой Telegram Bot API по `telegram_id` донатера.
   - Для запросов из **Boosty / Patreon / VK Donut**: отправка нотификации через соответствующие адаптеры / SMM-ботов.
   - Текст уведомления:
     *«{Имя}, привет! Твое пожелание реализовано в релизе! Фича уже доступна на smartbet.guru. Огромное спасибо за поддержку!»*
   - Сохранение отправленного текста в `admin_reply` тикета.

4. **Тестирование и верификация (Definition of Done)**:
   - Эмулированный прогон: создание обращения $\rightarrow$ создание задачи в Plane $\rightarrow$ симуляция вебхука перехода в `Завершено` $\rightarrow$ проверка перевода в `RESOLVED` и успешной доставки уведомления.
