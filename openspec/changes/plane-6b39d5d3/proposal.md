# Proposal: [pod-vk-donut] Выделенный pod и агент VK Donut: изолированная учетка сообщества, Callback API и ответы донам

## Context
Plane Task ID: `6b39d5d3-ec3d-4d02-b88a-4b256a992a72`
Module: Patron CRM & Community Feedback Hub
Target Pod: `smm-bot-vk`
Target Namespace: `igaming-dev`

## Description
Развернуть отдельный изолированный pod в Kubernetes (`smm-bot-vk`) с выделенным токеном доступа сообщества ВКонтакте.
Под управляет клубом донов VK Donut, обрабатывает события оплаты и взаимодействует с платящими подписчиками.

### Архитектурные требования:
1. **K8s Deployment:** `smm-bot-vk` в namespace `igaming-dev` с healthcheck HTTP `/healthz` на порту 8080.
2. **Изолированная сессия в Redis:** `smm:session:vk` (Group Access Token, Confirmation Secret, Secret Key, активные доны, дедупликация и LTV).
3. **Сетевая маршрутизация:** прямой домашний IP СПб (`ru-proxy:direct`) для минимального пинга к серверам VK.
4. **Функционал агента:**
   - Приём событий VK Callback API: `donut_subscription_create`, `donut_subscription_prolonged`, `donut_subscription_price_changed`, `donut_subscription_cancelled`, `donut_subscription_expired`.
   - Мониторинг комментариев на стене сообщества с проверкой флага `donut.is_don = 1`.
   - Маркировка обращений донов как `is_paid=true` с фиксацией тарифа VK Donut в `community_identity:vk:<user_id>`.
   - Публикация эксклюзивных постов для донов сообщества (`donut_paid_duration`).
   - Автоматические ответы донам в комментариях и сообщениях сообщества.
