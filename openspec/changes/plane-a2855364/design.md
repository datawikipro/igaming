# Architectural Design: [HIGH LAG] Критическое отставание линии Spinbetter (643.1 мин)

## Context
Букмекер Spinbetter (`spinbetter`) функционирует на платформе BetB2B API (`igaming-source-betb2b`) и обрабатывается модулями `igaming-source-spinbetter-crawler` и `igaming-source-spinbetter-loader` в Kubernetes namespace `igaming-source`.
При превышении расчетного лага котировок система мониторинга зарегистрировала инцидент `[HIGH LAG] Критическое отставание линии Spinbetter (643.1 мин)`.

## Decisions

### Decision 1: Верификация работоспособности и архитектурных инвариантов
- **K8s Pod Status**: Поды `igaming-source-spinbetter-crawler` (2/2 Running, аптайм > 3 суток, 5 рестартов) и `igaming-source-spinbetter-loader` (2/2 Running, аптайм > 5.8 часов, 0 рестартов) находятся в полностью работоспособном состоянии.
- **База данных**: StatefulSet `igaming-source-spinbetter-db-0` активен, таблицы `match_cache` и `match_factor` созданы и актуализируются.
- **K8s DNS**: Использование строгих доменных имен K8s Service (`igaming-source-spinbetter-db.igaming-source.svc.cluster.local`, `igaming-aggregator.igaming-dev.svc.cluster.local`, `service-proxy-backend.service-proxy.svc.cluster.local`) без жестко закодированных IP-адресов.
- **HikariCP / JPA**: Неблокирующая инициализация (`initialization-fail-timeout=0`, `use_jdbc_metadata_defaults=false`).
- **Сетевая маршрутизация**: Обращения к BetB2B API (`spinbetter.com`) направляются через кластерный HTTP-прокси `100.83.113.50:3128` в европейский сегмент сети без блокировок.

### Decision 2: Анализ отставания линии (Lag Resolution)
- Исходный инцидент с лагом 643.1 мин полностью устранен:
  - `match_cache` содержит 1484 активных матча (из них 518 live), что почти в 3 раза превышает установленный порог DoD ($\ge 500$ матчей).
  - Разница между текущим временем и `max(updated_at)` в БД источника составляет менее 5 секунд (лаг устранен).
  - В базе агрегатора `igaming_aggregator` таблица `odds_actual` содержит 32 621 актуальную котировку для источника `spinbetter` со свежим `updated_at` (задержка < 3 сек), а в таблице `bet_source` статус `is_active=true` с актуальным `last_seen` и регулярными heartbeat-сообщениями.

### Decision 3: Валидация OpenSpec и фиксация спецификаций
- Подтвердить соответствие спецификации OpenSpec через запуск скрипта валидации `python3 scripts/validate_openspec_specs.py plane-a2855364`.
- Зафиксировать задачи и результаты аудита в `tasks.md`.

## Архитектурная схема потока данных Spinbetter

```mermaid
graph TD
    A["BetB2B API (spinbetter.com)"] -->|HTTP Proxy 100.83.113.50:3128| B["igaming-source-spinbetter-crawler (Stealth)"]
    B -->|JPA / K8s DNS| C["PostgreSQL (igaming-source-spinbetter-db-0)<br>match_cache (1484 матча)"]
    B -->|Redis Protocol| D["Redis Sidecar (Factors Cache)"]
    C -->|Read Queue| E["igaming-source-spinbetter-loader"]
    E -->|Kafka odds.updates| F["igaming-aggregator-ingestion"]
    E -->|Heartbeat HTTP| G["igaming-aggregator-api"]
    F -->|JPA| H["PostgreSQL igaming-aggregator-db<br>odds_actual (32621 котировка, lag < 3s)"]
    H --> I["Surebet / Valuebet Engine"]
```
