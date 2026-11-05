# 🗺️ Полная Карта Межсервисного Взаимодействия — SmartBet.guru

> **Статус**: Актуализировано по реальному состоянию K8s-кластера (`xeon-srv`).  
> **Единый источник истины** по всем протоколам взаимодействия: **REST API, Kafka Event Stream, PostgreSQL, Redis, ClickHouse, Qdrant**.

---

## 🏛️ 1. Архитектурная Топология Кластера (Mermaid)

```mermaid
flowchart TD
    subgraph Sources["🌐 Слой Сбора (igaming-source)"]
        SRC_CRAWLER["40+ Букмекеров (Crawlers & Loaders)"]
    end

    subgraph Master["⚙️ Мастер-инфраструктура (igaming-master)"]
        KAFKA["Kafka 3.7 (:9092, :9093)<br/>⚠️ Внимание: StatefulSet без PVC!"]
        PORTAL_PG[("PostgreSQL: portal-postgres (:5432)<br/>БД пользователей и тарифов")]
    end

    subgraph Enrichment["⚡ Обогащение и MDM (igaming-enrichment)"]
        MDM["MDM Service (:8095)<br/>Канонические сущности & Live-счета"]
        INGEST["Aggregator Ingestion (:8080)<br/>Приём котировок в PostgreSQL"]
        SUREBET["Aggregator Surebet (:8080)<br/>Детектор вилок и валуев"]
        NORMALIZER["Aggregator Normalizer (:8080)<br/>LLM-нормализатор названий"]
        ENRICH["Aggregator Enrichment (:8085)<br/>Обогащение метаданными"]
        RESOLVER["Anomaly Resolver Agent (:8098)<br/>Авто-резолвер нераспознанных матчей"]
        SOFA_BRIDGE["SofaScore Bridge (:8090)<br/>FastAPI скрапер SofaScore"]
        SOFA_DEMAND["SofaScore On-Demand (:3041)"]
        ENRICH_DB[("PostgreSQL: igaming-aggregator-db (:5432)<br/>Схемы: aggregator, mdm, sofascore, analytics")]
        ENRICH_REDIS[("Redis: igaming-redis (:6379)")]
    end

    subgraph Analytics["🧠 Квантовая Аналитика & ML (igaming-analytics)"]
        AI_ANALYST["AI Analyst Core (:8080)<br/>NodePort :30895"]
        ML_INF["ML Tactical Inference (:8085)<br/>FastAPI / MultiTaskTacticalNet"]
        ORBIT["AI Orbit Controller<br/>Скриптовый менеджер орбит"]
        VISION["Match Vision (:8080)<br/>NodePort :30896"]
        DDS_LOADER["DDS Loader CronJob<br/>Parquet -> ClickHouse"]
        CH[("ClickHouse DDS (:8123 HTTP, :9000 Native)")]
        QDRANT[("Qdrant Vector DB (:6333 REST, :6334 gRPC)")]
        ANALYTICS_SRV["Analytics Service (:3043 / :80)"]
    end

    subgraph API["🚀 API и Пользовательский Шлюз (igaming-api)"]
        AGG_API["Aggregator API (:80, :8080)<br/>Шлюз котировок и арбитража"]
        PORTAL["igaming-portal (:8081 / :80)<br/>NodePort :32081 / JWT Gateway"]
        B2B["B2B API (:8080)"]
        SMARTBET_WEB["smartbet.guru (Next.js 14 :3000)<br/>NodePort :32000"]
        SPORTVIBE_WEB["sportvibe.today (Next.js 14 :3000)<br/>NodePort :32001"]
    end

    subgraph External["🌍 Внешние / Смежные сервисы"]
        PLANE_WORKERS["Plane AI Workers 0..19 (:8000)<br/>plane.svc.cluster.local"]
        CARRIER_AUTH["Carrier Auth (:8000)<br/>carrierlab.svc.cluster.local"]
        LLM_GW["LLM Gateway (:8001)<br/>llm.svc.cluster.local"]
    end

    %% Потоки данных: Источники -> Инфраструктура
    SRC_CRAWLER -->|REST: Blueprint & Live-score :8095| MDM
    SRC_CRAWLER -->|REST: Heartbeat & Unchanged :80| AGG_API
    SRC_CRAWLER -->|Kafka: canonical.odds.updates| KAFKA
    SRC_CRAWLER -->|Kafka: sports.canonical.scores| KAFKA
    SRC_CRAWLER -->|Kafka: mdm.anomaly.errors| KAFKA
    SRC_CRAWLER -.->|Kafka: odds.updates (LEGACY FALLBACK - SLEEPING!)| KAFKA

    %% Потоки данных: Kafka -> Enrichment
    KAFKA -->|Kafka Consumer: canonical.odds.updates| INGEST
    KAFKA -->|Kafka Consumer: mdm.anomaly.errors| RESOLVER
    RESOLVER -->|REST: Резолвинг аномалий :8095| MDM
    RESOLVER -->|REST: Он-деманд парсинг :3041| SOFA_DEMAND
    SOFA_DEMAND -->|REST: Скрапинг :8090| SOFA_BRIDGE

    %% Потоки данных: Enrichment <-> БД
    INGEST -->|JDBC :5432| ENRICH_DB
    INGEST -->|RESP :6379| ENRICH_REDIS
    SUREBET -->|JDBC :5432 / findDirtyMatchIdsSince| ENRICH_DB
    SUREBET -->|RESP :6379| ENRICH_REDIS
    MDM -->|JDBC :5432| ENRICH_DB
    MDM -->|RESP :6379| ENRICH_REDIS

    %% Потоки данных: Analytics
    AI_ANALYST -->|REST: /predict :8085| ML_INF
    AI_ANALYST -->|REST: /api/v1/mdm/matches :8095| MDM
    AI_ANALYST -->|REST: LLM Аудит :8000| PLANE_WORKERS
    AI_ANALYST -->|HTTP :8123| CH
    AI_ANALYST -->|REST :6333| QDRANT
    AI_ANALYST -->|JDBC :5432| ENRICH_DB
    VISION -->|REST :8000| PLANE_WORKERS
    VISION -->|JDBC :5432| ENRICH_DB
    DDS_LOADER -->|HTTP :8123| CH
    ORBIT -->|JDBC :5432| ENRICH_DB
    ORBIT -->|REST :8080| AI_ANALYST

    %% Потоки данных: API & Frontend
    AGG_API -->|JDBC :5432| ENRICH_DB
    AGG_API -->|RESP :6379| ENRICH_REDIS
    PORTAL -->|REST :80/api| AGG_API
    PORTAL -->|REST :8095| MDM
    PORTAL -->|REST :8001| LLM_GW
    PORTAL -->|JDBC :5432| PORTAL_PG
    B2B -->|REST :8080| AGG_API
    SMARTBET_WEB -->|REST :80| PORTAL
    SMARTBET_WEB -->|REST :8000| CARRIER_AUTH
    SPORTVIBE_WEB -->|REST :80| PORTAL
    SPORTVIBE_WEB -->|REST :8000| CARRIER_AUTH
```

---

## 📋 2. Сводная Матрица Межсервисного Взаимодействия Подов

| Под / Сервис | Namespace | Входящий порт | Входящий протокол | Исходящие взаимодействия | Протокол | Целевой хост / K8s DNS | Целевой порт |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`kafka-0`** | `igaming-master` | `9092` (broker), `9093` (KRaft) | Kafka Binary Protocol | — | — | — | — |
| **`portal-postgres-0`**| `igaming-master` | `5432` | PostgreSQL Wire | — | — | — | — |
| **`igaming-mdm-service`** | `igaming-enrichment` | `8095` | HTTP REST / Actuator | PostgreSQL<br/>Redis<br/>Kafka | JDBC<br/>RESP<br/>Kafka | `igaming-aggregator-db:5432`<br/>`igaming-redis:6379`<br/>`kafka.igaming-master.svc:9092` | 5432<br/>6379<br/>9092 |
| **`igaming-aggregator-ingestion`** | `igaming-enrichment` | `8080` (actuator) | HTTP | Kafka<br/>PostgreSQL<br/>Redis | Kafka<br/>JDBC<br/>RESP | `kafka:9092`<br/>`igaming-aggregator-db:5432`<br/>`igaming-redis:6379` | 9092<br/>5432<br/>6379 |
| **`igaming-aggregator-surebet`** | `igaming-enrichment` | `8080` | HTTP Actuator | PostgreSQL<br/>Redis | JDBC<br/>RESP | `igaming-aggregator-db:5432`<br/>`igaming-redis:6379` | 5432<br/>6379 |
| **`igaming-aggregator-enrichment`** | `igaming-enrichment` | `8085` | HTTP Actuator | Kafka<br/>PostgreSQL<br/>Redis | Kafka<br/>JDBC<br/>RESP | `kafka:9092`<br/>`igaming-aggregator-db:5432`<br/>`igaming-redis:6379` | 9092<br/>5432<br/>6379 |
| **`igaming-anomaly-resolver-agent`** | `igaming-enrichment` | `8098` | HTTP Actuator | Kafka<br/>MDM Service<br/>AI Analyst<br/>SofaScore On-Demand | Kafka<br/>REST<br/>REST<br/>REST | `kafka.igaming-master.svc:9092`<br/>`igaming-mdm-service:8095`<br/>`igaming-ai-analyst.igaming-analytics.svc:8080`<br/>`igaming-capture-sofascore-on-demand:3041` | 9092<br/>8095<br/>8080<br/>3041 |
| **`sofascore-bridge`** | `igaming-enrichment` | `8090` | HTTP REST (FastAPI) | SofaScore Web/API | HTTPS | Внешние адреса SofaScore | 443 |
| **`igaming-capture-sofascore-crawler`** | `igaming-enrichment` | `3041` | HTTP Actuator | SofaScore Bridge<br/>Aggregator API<br/>PostgreSQL<br/>Redis | REST<br/>REST<br/>JDBC<br/>RESP | `sofascore-bridge:8090`<br/>`igaming-aggregator:8080`<br/>`igaming-aggregator-db:5432`<br/>`igaming-redis:6379` | 8090<br/>8080<br/>5432<br/>6379 |
| **`igaming-aggregator-api`** | `igaming-api` | `80`, `8080` | HTTP REST | PostgreSQL<br/>Redis | JDBC<br/>RESP | `igaming-aggregator-db.igaming-enrichment.svc:5432`<br/>`igaming-redis.igaming-enrichment.svc:6379` | 5432<br/>6379 |
| **`igaming-portal`** | `igaming-api` | `8081` (service :80) | HTTP REST & WS STOMP | PostgreSQL<br/>Redis (sidecar)<br/>Aggregator API<br/>MDM Service<br/>LLM Gateway | JDBC<br/>RESP<br/>REST<br/>REST<br/>REST | `portal-postgres:5432`<br/>`localhost:6379`<br/>`igaming-aggregator:80`<br/>`igaming-mdm-service:8095`<br/>`llm-gateway-microservice:8001` | 5432<br/>6379<br/>80<br/>8095<br/>8001 |
| **`igaming-b2b-api`** | `igaming-api` | `8080` | HTTP REST | Aggregator API<br/>Redis | REST<br/>RESP | `igaming-aggregator:8080`<br/>`igaming-redis:6379` | 8080<br/>6379 |
| **`smartbet-guru`** | `igaming-api` | `3000` (service :80) | HTTP (Node.js Next.js) | Portal Gateway<br/>Carrier Auth | REST<br/>REST | `igaming-portal:80`<br/>`carrier-auth:8000` | 80<br/>8000 |
| **`sportvibe-today`** | `igaming-api` | `3000` (service :80) | HTTP (Node.js Next.js) | Portal Gateway<br/>Carrier Auth | REST<br/>REST | `igaming-portal:80`<br/>`carrier-auth:8000` | 80<br/>8000 |
| **`igaming-ai-analyst`** | `igaming-analytics` | `8080` (NodePort :30895) | HTTP REST | ML Inference<br/>MDM Service<br/>Plane AI Workers<br/>ClickHouse<br/>Qdrant<br/>PostgreSQL | REST<br/>REST<br/>REST<br/>HTTP<br/>REST<br/>JDBC | `igaming-ml-inference:8085`<br/>`igaming-mdm-service.igaming-enrichment.svc:8095`<br/>`plane-ai-worker-%d.plane-ai-worker.plane.svc:8000`<br/>`clickhouse:8123`<br/>`qdrant:6333`<br/>`igaming-aggregator-db.igaming-dev.svc:5432` | 8085<br/>8095<br/>8000<br/>8123<br/>6333<br/>5432 |
| **`igaming-ml-inference`** | `igaming-analytics` | `8085` | HTTP REST (FastAPI) | Local PyTorch Models | In-memory | — | — |
| **`igaming-match-vision`** | `igaming-analytics` | `8080` (NodePort :30896) | HTTP REST | Plane AI Workers<br/>PostgreSQL | REST<br/>JDBC | `plane-ai-worker-%d.plane-ai-worker.plane.svc:8000`<br/>`igaming-aggregator-db.igaming-enrichment.svc:5432` | 8000<br/>5432 |
| **`igaming-ai-orbit-controller`** | `igaming-analytics` | — (cron worker) | — | AI Analyst<br/>PostgreSQL | REST<br/>JDBC | `igaming-ai-analyst.igaming-dev.svc:8080`<br/>`igaming-aggregator-db.igaming-dev.svc:5432` | 8080<br/>5432 |
| **`igaming-dds-loader-microbatch`** | `igaming-analytics` | — (CronJob */2) | — | ClickHouse | HTTP | `clickhouse.igaming-analytics.svc:8123` | 8123 |
| **`igaming-source-*` (40+ краулеров)** | `igaming-source` | Зависит от краулера | HTTP Actuator | MDM Service<br/>Aggregator API<br/>Kafka<br/>Proxy Mesh | REST<br/>REST<br/>Kafka<br/>HTTP Proxy | `igaming-mdm-service:8095`<br/>`igaming-aggregator:80`<br/>`kafka:9092`<br/>`purevpn-*.proxy:3128` | 8095<br/>80<br/>9092<br/>3128 |

---

## 📬 3. Каталог Kafka Топиков и Потоков Событий

| Топик Kafka | Producer (Кто пишет) | Consumer (Кто читает) | Payload / DTO | Назначение & Особенности |
| :--- | :--- | :--- | :--- | :--- |
| **`canonical.odds.updates`** | `igaming-source-*` (`AggregatorClient.pushCanonicalOddsUpdate`) | `igaming-aggregator-ingestion` (`CanonicalOddsKafkaListener`) | `CanonicalMatchEventDto` | **Основной боевой поток котировок** (MDM Stage 1). Concurrency = 8, O(1) hash grouping по MatchKey, прямой upsert в `odds_actual`. |
| **`odds.updates`** | `igaming-source-*` (Fallback при нераспознанных MDM сущностях), `aggregator-api` | `igaming-aggregator-ingestion` (`OddsUpdateKafkaListener`) | `OddsUpdateRequest` | ⚠️ **ВНИМАНИЕ**: В поде `igaming-aggregator-ingestion` флаг `APP_KAFKA_LISTENER_AUTO_STARTUP` = `false`! Топик **НЕ вычитывается** автоматически! |
| **`sports.canonical.scores`** | `igaming-source-*` (`AggregatorClient.emitLiveScoreIfPresent`) | MDM / Live Dashboard | `CanonicalLiveScoreDto` | Телеметрия счетов в реальном времени, опасных моментов (DangerStatus), приостановок (Suspended) и трансляций. |
| **`mdm.anomaly.errors`** | `igaming-source-*` (`MdmIngestionClient.reportAnomaly`) | `igaming-anomaly-resolver-agent` | `MdmAnomalyDto` | Ошибки маппинга сущностей (новые команды/лиги, не сопоставленные с SofaScore). Агент подхватывает и вызывает SofaScore On-Demand. |
| **`igaming.sofascore.raw-events`** | `igaming-capture-sofascore` | `igaming-aggregator-enrichment` | `SofaRawEventDto` | Сырые события SofaScore для обогащения метаданных матчей. |
| **`llm-normalization-tasks`** | `igaming-aggregator-normalizer` | `llm-poller` / `llm-gateway` | `NormalizationTaskDto` | Задачи на распознавание названий через Gemini / DeepSeek. |
| **`llm-normalization-results`** | `llm-gateway` | `igaming-aggregator-normalizer` | `NormalizationResultDto` | Ответы от LLM с каноническими именами для записи в `team_alias`. |
| **`odds.outrights`** | `igaming-source-*` | `igaming-aggregator-ingestion` | `OddsUpdateRequest` | Долгосрочные ставки (победители турниров). |

---

## 🔎 4. Детальный Анализ Сбоев Связи: Почему Поды Перестают Общаться?

### ⚠️ Причина 1: Падения Kafka без сохранения состояния (Отсутствие PVC в `kafka-0`)
* **Факт**: StatefulSet `kafka` в namespace `igaming-master` развернут **без PersistentVolumeClaim** (только projected service account token). Данные хранятся в эфемерном слое контейнера.
* **Что происходит**: При рестарте контейнера (из-за лимита RAM 1Gi без `-Xmx` или нод-эвикшена):
  1. Полностью стираются все созданные топики, партиции и метаданные.
  2. Все Kafka клиенты получают `Node disconnected` и таймаут 30 секунд.
  3. После авто-создания топиков их UUID меняется (`associated topicId changed`), вызывая в клиентах сброс эпохи и ребалансировку consumer-групп.
  4. Все коммиты смещений (offsets) теряются, клиенты зависают или падают с `UnknownMemberId`.

### ⚠️ Причина 2: Ловушка невычитываемого топика (`odds.updates` vs `canonical.odds.updates`)
* **Факт**: В `igaming-aggregator-ingestion` переменная `APP_KAFKA_LISTENER_AUTO_STARTUP: "false"`.
* **Что происходит**: Если краулер (например, Pinnacle, Winline) не смог смаппить матч через MDM (вернулась ошибка или новая команда), код `AggregatorClient` отправляет событие в старый топик `odds.updates`. Но консьюмер на этот топик **отключен**.
* **Симптом**: В логах краулера написано `Sent odds update to Kafka topic odds.updates`, но в базу `odds_actual` ничего не попадает. Создается иллюзия, что "краулер перестал передавать данные".

### ⚠️ Причина 3: Цепочки CNAME в Kubernetes DNS (ExternalName Sprawl)
* **Факт**: В кластере развернуты множественные перекрёстные CNAME-алиасы:
  - `igaming-source` $\rightarrow$ `igaming-aggregator.igaming-dev` $\rightarrow$ `igaming-aggregator-api.igaming-dev` $\rightarrow$ `igaming-aggregator-api.igaming-api`.
  - В `igaming-ai-orbit-controller` (в namespace `igaming-analytics`) прописан `ANALYST_HOST: igaming-ai-analyst.igaming-dev.svc.cluster.local`, который редиректит обратно в `igaming-analytics`!
* **Что происходит**: CoreDNS при многократном CNAME-резолвинге в условиях всплесков трафика или кратковременной деградации node-local-dns может возвращать `NXDOMAIN` или падать по таймауту. В Java-приложениях это вызывает зависание HTTP пулов RestTemplate/HttpClient.

### ⚠️ Причина 4: Ошибочные порты и адреса в старой документации (`API_REFERENCE.md`)
* В `API_REFERENCE.md` было указано:
  - `MDM Service`: `igaming-mdm-service.igaming-dev.svc.cluster.local:8080`.
  - В реальности: порт **`8095`**, namespace `igaming-enrichment`. При обращении на `:8080` запрос висит до таймаута (exit code 28).
  - `Portal Gateway`: `smartbet-portal.igaming-dev.svc.cluster.local:80`.
  - В реальности: сервис называется **`igaming-portal`** в `igaming-api`.

### ⚠️ Причина 5: Зомби-поды в namespace `igaming-dev`
* В `igaming-dev` продолжают работать 10 устаревших подов (`igaming-aggregator-api`, `igaming-analytics-service`, `igaming-capture-sofascore-*`), которые не получают внешний сетевой трафик (так как сервисы в `igaming-dev` переведены на ExternalName), но потребляют ресурсы сервера и могут конкурировать за подключения к БД `igaming-aggregator-db`.

### ⚠️ Причина 6: Ошибка Playwright Chromium в `igaming-source-winline-crawler`
* Краулер Winline циклически падает с ошибкой:
  `Executable doesn't exist at /root/.cache/ms-playwright/chromium-1124/chrome-linux/chrome`.
  В Docker-образе не предустановлен бинарник браузера Chromium, из-за чего Winline не может запуститься и парсить live-линию.
