# Proposal: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
<<<<<<< HEAD
Plane Task ID: `38e86374-1aa0-4e91-bfd5-8d1ff5f32ec0`

## Description

=======
Plane Task ID: `38e86374-e812-4eb2-a892-95f2a1b948c2`
Title: `[SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен`

## Description
Восстановление доступности и верификация стабильной работы сервиса публикации постов smm-bot-telegram в Kubernetes namespace `igaming-dev`.
Проведение комплексной диагностики подов `smm-bot-telegram`, сетевого взаимодействия с Telegram Bot API через кластерный HTTP-прокси `http://100.83.113.50:3128`, проверка связи с Redis (`igaming-redis`), работоспособности HTTP healthcheck и Actuator эндпоинтов (`/healthz`, `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`), выполнение нагрузочного тестирования и проведение 5-минутного soak-тестирования в соответствии с Golden Rule 1 (Definition of Done) и AGENTS.md.
>>>>>>> feature/plane-38e86374
