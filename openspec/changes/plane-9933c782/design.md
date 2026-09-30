# Design: [plane-sync-loop] Двусторонняя синхронизация с Plane и авто-уведомление донатера о релизе фичи

## Architecture Overview

```mermaid
sequenceDiagram
    autonumber
    actor Patron as Донатер (Коля)
    participant Channel as Канал (TG VIP / Boosty / Patreon / VK)
    participant Desk as Portal Admin UI (/admin/feedback)
    participant Portal as igaming-portal
    participant Plane as Plane REST API & Board
    participant Workers as AI Workers (Xeon Swarm)
    participant Webhook as Plane Webhook Receiver
    participant Notifier as PatronNotificationService
    participant Bot as igaming-bot / SMM Bots

    Patron->>Channel: Пожелание / Запрос фичи
    Channel->>Portal: Ingest фидбека (FeedbackItem, VIP P1/P2)
    Desk->>Portal: POST /api/v1/feedback/{id}/create-plane-issue
    Portal->>Plane: POST /api/v1/workspaces/.../issues (labels: vip-patron)
    Plane-->>Portal: Issue #70 created (id, sequence_id)
    Portal->>Portal: Связывание тикета: plane_issue_id, plane_sequence_id, статус IN_PROGRESS
    Plane->>Workers: Задача в статусе "AI разработка"
    Workers->>Plane: Выполнение, тесты, деплой -> Статус "Завершено"
    Plane->>Webhook: POST /api/v1/feedback/webhook/plane (state: "Завершено")
    Webhook->>Portal: Поиск тикета по plane_issue_id
    Portal->>Portal: Смена статуса на RESOLVED
    Portal->>Notifier: notifyPatronFeatureReleased(item)
    alt Источник: Telegram VIP
        Notifier->>Bot: sendHtml(chatId, "Коля, привет! Твое пожелание реализовано в релизе!...")
        Bot-->>Patron: Личное сообщение в Telegram
    else Источник: Boosty / Patreon / VK Donut
        Notifier->>Bot: dispatchSmmReply(platform, author, message)
    end
```

## Component Architecture

1. **`PlaneClient` & `PlaneProperties` (`igaming-portal`)**:
   - `plane.api.url`: K8s DNS `http://plane-proxy.plane.svc.cluster.local:80`
   - `plane.api.token`: Токен авторизации Plane REST API
   - `plane.workspace.slug`: `dataplatform`
   - `plane.project.id`: `2df124d7-25b0-4145-a63c-aafcb0fe0041`
   - Методы:
     - `createIssue(title, descriptionHtml, priority, labels, stateId)`
     - `getIssue(issueId)`
     - `updateIssueState(issueId, stateId)`

2. **`PlaneSyncService` (`igaming-portal`)**:
   - `createPlaneIssueForFeedback(UUID feedbackItemId, CreatePlaneIssueRequest req)`
     - Формирует шаблонное имя: `[feature/{category}] {Summary} (Запрос от VIP-патрона {Author})`
     - Проставляет теги `vip-patron`, `community-request`
     - Сохраняет `plane_issue_id`, `plane_sequence_id`, `plane_issue_url`
     - Переводит тикет в `IN_PROGRESS`
   - `handlePlaneWebhook(PlaneWebhookPayload payload)`
     - Определяет событие завершения задачи (`Завершено` / `Done`)
     - Находит `FeedbackItem`
     - Переводит в `RESOLVED`
     - Запускает `PatronNotificationService`

3. **`PatronNotificationService` (`igaming-portal`)**:
   - `notifyPatronFeatureReleased(FeedbackItem item)`
   - Формирует персонализированный текст:
     `«{AuthorName}, привет! Твое пожелание реализовано в релизе! Фича уже доступна на smartbet.guru. Огромное спасибо за поддержку!»`
   - В зависимости от `sourcePlatform`:
     - `TELEGRAM_VIP` / `TELEGRAM` $\rightarrow$ отправка через `igaming-bot` или прямой Telegram Bot API по `telegramId`.
     - `BOOSTY` / `PATREON` / `VK_DONUT` $\rightarrow$ отправка через SMM-интеграцию или адаптеры платформ.
   - Фиксирует отправку в `adminReply` и `repliedAt`.

4. **REST Controllers**:
   - `PortalFeedbackController`:
     - `POST /api/v1/feedback/{id}/plane-issue`
     - `POST /api/v1/feedback/webhook/plane`
     - `POST /api/v1/community/webhook/plane`
