# Proposal: [accounts] Исправить health check эндпоинты в EndpointHealthWatcherService (workload-manager)

## Context
Plane Task ID: `681e576c-611d-4c5a-8027-39a2d7f141b3`
Target Module: `monorepo`

## Description
**Проблема:** Сервис `accounts/workload-manager` каждые 15-30 минут принудительно перезапускает полностью здоровые сервисы `airflow-api-server`, `data-catalog-backend`, `llm-admin` из-за 30 неудачных health-проверок.

**Причина:** В `EndpointHealthWatcherService.java` зашиты устаревшие урлы проверок:

- Airflow: в Airflow 3 урл изменен с `/health` на `/api/v2/monitor/health`.
- LLM Admin: Spring Actuator доступен по `/actuator/health`, а не в корне `/`.
- Data Catalog: актуализировать путь проверки здоровья.
**Что сделать:**

- В модуле `accounts-service` в `EndpointHealthWatcherService.java` (или application.properties) обновить пути healthcheck для `airflow-api` и `llm-admin`.
- Убедиться, что компиляция проходит успешно.
