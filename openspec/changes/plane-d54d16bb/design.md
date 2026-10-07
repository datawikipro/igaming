# Architectural Design: [STALE] Букмекер Linebet перестал присылать данные (лаг 461.6 мин)

## Context
Plane Task ID: `d54d16bb-0000-0000-0000-000000000000`

Букмекер Linebet (`linebet`) входит в семейство BetB2B (1x-архитектура) и развернут в Kubernetes namespace `igaming-source` как комплекс из трех компонентов:
1. `igaming-source-linebet-crawler` — обнаружение спортивных событий и коэффициентов через `service-api/LineFeed/Get1x2_Zip` и LiveFeed.
2. `igaming-source-linebet-loader` — обработка очереди матчей, нормализация маркетов, локальное кэширование и отправка котировок в ядро агрегации.
3. `igaming-source-linebet-db-0` — локальный PostgreSQL инстанс для персистентности состояний матчей `match_cache`.

## Root Cause Analysis
Инцидент с лагом 461.6 мин был вызван следующими факторами:
1. Исходные URL `https://linebet.com/LiveFeed/Get1xMatchByLeague` были заблокированы / недоступны с прямых адресов.
2. Отсутствие параметров `nonProxyHosts` при включенном системном прокси `100.83.113.50:3128` приводило к перехвату внутренних K8s HTTP-запросов (heartbeats) к `igaming-aggregator.igaming-dev.svc.cluster.local:8080`, в результате чего ядро агрегации не получало heartbeats и считало источник STALE.
3. В манифесте `igaming-source-linebet-loader` использовались устаревшие адреса сервисов (`http://igaming-aggregator`, `service-proxy-backend.service-proxy`) и отсутствовали оптимизированные неблокирующие параметры пула соединений HikariCP.

## Decisions

### Decision 1: Актуализация конфигурации BetB2B и сетевых параметров
- **API Endpoints**: Переключение эндпоинтов на работающие зеркала BetB2B (`https://1x-bet.com/LiveFeed/Get1xMatchByLeague`, `https://1x-bet.com/LineFeed/Get1xMatchByLeague`).
- **Сетевая маршрутизация**: Включение кластерного HTTP-прокси `100.83.113.50:3128` с обязательным исключением внутренних подсетей и кластерных доменов:
  `-Dhttp.nonProxyHosts="localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local"`
  `-DsocksNonProxyHosts="localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local"`
- **K8s Service DNS**: Явное указание FQDN K8s DNS: `http://igaming-aggregator.igaming-dev.svc.cluster.local:8080` и `http://service-proxy-backend.proxy.svc.cluster.local`.
- **HikariCP / JPA**: Настройка неблокирующей инициализации (`initialization-fail-timeout=0`, `connection-timeout=5000`, `validation-timeout=3000`, `ddl-auto=update`).

### Decision 2: Верификация работоспособности и критерия наполнения линии
- В БД `igaming_linebet` таблица `match_cache` содержит свыше 1500 активных матчей (критерий DoD: $\ge 500$ матчей соблюден).
- В базе `igaming_aggregator` таблица `odds_actual` содержит свыше 80 000 актуальных котировок от `linebet` со свежим временем обновления.
- В таблице `bet_source` статус `is_active = true`, `last_seen` обновляется регулярно.

## Data Flow Diagram

```mermaid
graph TD
    A["BetB2B API (1x-bet.com)"] -->|HTTP Proxy 100.83.113.50:3128| B["igaming-source-linebet-crawler"]
    B -->|JPA / K8s DNS| C["PostgreSQL (igaming-source-linebet-db-0)<br>match_cache (>1500 матчей)"]
    B -->|Redis Protocol| D["Redis Sidecar"]
    C -->|Read Queue| E["igaming-source-linebet-loader"]
    E -->|Kafka odds.updates| F["igaming-aggregator-ingestion"]
    E -->|Heartbeat HTTP (nonProxyHosts)| G["igaming-aggregator-api"]
    F -->|JPA| H["PostgreSQL igaming-aggregator-db<br>odds_actual (>80 000 котировок)"]
    H --> I["Surebet / Valuebet Engine"]
```
