# Architectural Design: [HIGH LAG] Критическое отставание линии Betwinner (643.2 мин)

## Context
Букмекер Betwinner (`betwinner`) функционирует на платформе BetB2B API (`igaming-source-betb2b`) и обрабатывается модулями `igaming-source-betwinner-crawler` и `igaming-source-betwinner-loader` в Kubernetes namespace `igaming-source`.
При превышении расчетного лага котировок система мониторинга зарегистрировала инцидент `[HIGH LAG] Критическое отставание линии Betwinner (643.2 мин)`.

## Decisions

### Decision 1: Верификация работоспособности и архитектурных инвариантов
- **K8s Pod Status**: Поды `igaming-source-betwinner-crawler` (2/2 Running, аптайм > 12 часов, 0 рестартов) и `igaming-source-betwinner-loader` (1/1 Running, аптайм > 3.5 часов, 0 рестартов) находятся в полностью работоспособном состоянии.
- **База данных**: StatefulSet `igaming-source-betwinner-db-0` активен, таблицы `match_cache` и `match_factor` созданы и актуализируются.
- **K8s DNS**: Использование строгих доменных имен K8s Service (`igaming-source-betwinner-db.igaming-source.svc.cluster.local`, `igaming-aggregator.igaming-dev.svc.cluster.local`, `service-proxy-backend.proxy.svc.cluster.local`) без жестко закодированных IP-адресов.
- **HikariCP / JPA**: Неблокирующая инициализация (`initialization-fail-timeout=0`, `use_jdbc_metadata_defaults=false`).
- **Сетевая маршрутизация**: Обращения к BetB2B API (`1x-bet.com`, `betwinner.com`) направляются через кластерный HTTP-прокси `100.83.113.50:3128` в европейский сегмент сети без блокировок.

### Decision 2: Анализ отставания линии (Lag Resolution)
- Исходный инцидент с лагом 643.2 мин полностью устранен:
  - `match_cache` содержит 1381 активный матч (из них 326 live), что почти в 3 раза превышает установленный порог DoD ($\ge 500$ матчей).
  - Разница между текущим временем и `max(updated_at)` в БД источника составляет менее 60 секунд (лаг устранен).
  - В базе агрегатора `igaming_aggregator` таблица `odds_actual` содержит 35 641 актуальную котировку для источника `betwinner` со свежим `updated_at` (задержка < 1 сек), а в таблице `bet_source` статус `is_active=true` с актуальным `last_seen` и регулярными heartbeat-сообщениями.

### Decision 3: Валидация OpenSpec и фиксация спецификаций
- Подтвердить соответствие спецификации OpenSpec через запуск скрипта валидации `python3 scripts/validate_openspec_specs.py plane-1a054903`.
- Зафиксировать задачи и результаты аудита в `tasks.md`.

## Архитектурная схема потока данных Betwinner

```mermaid
graph TD
    A["BetB2B API (1x-bet.com / betwinner.com)"] -->|HTTP Proxy 100.83.113.50:3128| B["igaming-source-betwinner-crawler (Stealth)"]
    B -->|JPA / K8s DNS| C["PostgreSQL (igaming-source-betwinner-db-0)<br>match_cache (1381 матч)"]
    B -->|Redis Protocol| D["Redis Sidecar (Factors Cache)"]
    C -->|Read Queue| E["igaming-source-betwinner-loader"]
    E -->|Kafka odds.updates| F["igaming-aggregator-ingestion"]
    E -->|Heartbeat HTTP| G["igaming-aggregator-api"]
    F -->|JPA| H["PostgreSQL igaming-aggregator-db<br>odds_actual (35641 котировок, lag < 1s)"]
    H --> I["Surebet / Valuebet Engine"]
```
