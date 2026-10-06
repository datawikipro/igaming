# Proposal: #951: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
Plane Task ID: `ea3f45c1-41c7-4b05-93b2-39c54a8669bf`

## Description
Восстановление доступности и верификация работоспособности сервиса публикации постов smm-bot-telegram в Kubernetes namespace `igaming-dev`.
Проведение комплексной диагностики и проверки подов `smm-bot-telegram`, сетевого взаимодействия с Telegram Bot API через кластерный HTTP-прокси `http://100.83.113.50:3128`, проверка связи с Redis (`igaming-redis`), работоспособности HTTP healthcheck эндпоинтов (`/healthz`, `/actuator/health`), синхронизация K8s манифеста и проведение модульных и интеграционных проверок.
