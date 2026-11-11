# Архитектура Cloudflare, Zero Trust и Cloudflare Tunnel

Данный документ содержит полную спецификацию и инвентаризацию внешнего сетевого контура проекта **SmartBet.guru**, включая конфигурацию Cloudflare DNS, Cloudflare Tunnel, Zero Trust Access и правила обхода региональных блокировок.

---

## 1. Общие сведения об аккаунте и доменах

- **Учётная запись Cloudflare**: `Aleksei.a.chernousov@gmail.com's Account`
- **Account ID**: `dc8a8e560373f155bb5e842f44778793`
- **Основной рабочий домен**: `smartbet.guru` (Статус: `Active`, план `Free`)
- **Смежные домены в профиле**:
  - `careerlab.top`
  - `carrerlab.space`
  - `dataplatform.college`

---

## 2. Архитектура Cloudflare Tunnel

Все внешние входящие HTTP/HTTPS запросы к микросервисам Kubernetes и инфраструктурным панелям маршрутизируются через **Cloudflare Tunnel**, исключая необходимость проброса небезопасных NodePort-портов напрямую в публичный интернет.

### 2.1. Активный боевой туннель
- **Имя туннеля**: `smartbet.guru`
- **Tunnel ID**: `9a924382-f385-4393-ae9f-94005ec0a251`
- **Статус**: 🟢 **Healthy**
- **Реплики**: 2 активных процесса `cloudflared` (версия `2026.9.1`)
- **Количество активных маршрутов (Public Hostnames)**: **36 приложений**

### 2.2. Неактивные / устаревшие туннели
- **`smartbet-tunnel`** (`3ba6e3e1-4798-499b-8717-c853cb29a3d6`): Статус `Down` (0 реплик, локальный черновой файл в репозитории `cloudflared-config.yml`).
- **`smartbet-guru-tunnel`** (`a54bf2c9-56a6-4c4a-af9a-10d5d64d5cc1`): Статус `Inactive` (0 реплик).

---

## 3. Полный реестр маршрутов туннеля (37 Public Hostnames)

В соответствии с правилом **Golden Rule #2 (Только K8s DNS Service Names)**, туннель пересылает трафик на внутренние канонические FQDN сервисов кластера:

### А. Экосистема SmartBet.guru (iGaming & LLM)

| Публичный URL | Сервис назначения (K8s DNS / Внутренний IP) | Назначение |
|---|---|---|
| **`https://smartbet.guru`** | `https://188.242.33.93:443` | Основной фронтенд / портал (включая AI-Аналитик на `/analyst` и `/ru/analyst` со статусом Alpha) |
| **`https://www.smartbet.guru`** | `https://188.242.33.93:443` | Зеркало www |
| **`https://app.smartbet.guru`** | `http://smartbet-mobile-app.igaming-dev.svc.cluster.local.:80` | Мобильное PWA / Web-приложение |
| **`https://llm.smartbet.guru`** | `http://llm-frontend.llm.svc.cluster.local.:80` | **LLM Control Center** (админка моделей и ключей) |
| **`https://crawler.smartbet.guru`** | `http://igaming-crawler-frontend.igaming-dev.svc.cluster.local.:80` | Панель мониторинга краулеров БК |
| **`https://proxy.smartbet.guru`** | `http://proxy-frontend.proxy.svc.cluster.local.:80` | Управление VPN/прокси-пулами |
| **`https://igaming-admin.smartbet.guru`** | `http://igaming-admin-frontend.accounts.svc.cluster.local.:80` | Главная панель администрирования |
| **`https://plane.smartbet.guru`** | `http://plane-web.plane.svc.cluster.local.:80` | Таск-трекер Plane (задачи iGaming) |
| **`https://superset.smartbet.guru`** | `http://100.75.13.86:30888` | BI-дашборды Apache Superset |
| **`https://home-routing.smartbet.guru`** | `http://10.96.106.220:8089` | Маршрутизатор домашней сети / шлюз |
| **`https://airflow.smartbet.guru`** | `http://airflow-api-server.airflow.svc.cluster.local.:8080` | Оркестратор ETL/DAG-пайплайнов |
| **`https://openmetadata.smartbet.guru`** | `http://openmetadata-server.openmetadata-temp.svc.cluster.local.:8585` | Каталог метаданных и Data Lineage |

### Б. Инфраструктура Data Platform (`dataplatform.college`)

| Публичный URL | Сервис назначения (K8s DNS) | Назначение |
|---|---|---|
| `orion.dataplatform.college` | `http://data-catalog-ui.data-catalog.svc.cluster.local.:3000` | UI каталога данных |
| `catalog.orion.dataplatform.college` | `http://data-catalog-ui.data-catalog.svc.cluster.local.:3000` | Зеркало каталога данных |
| `trino.orion.dataplatform.college` | `http://trino.trino.svc.cluster.local.:8080` | SQL-движок Trino |
| `spark.orion.dataplatform.college` | `http://spark-master.spark.svc.cluster.local.:8080` | Apache Spark Master UI |
| `spark-history.orion.dataplatform.college` | `http://spark-history-server.spark.svc.cluster.local.:18080` | Spark History Server |
| `minio.orion.dataplatform.college` | `http://minio.s3.svc.cluster.local.:9001` | MinIO S3 Console UI |
| `s3.orion.dataplatform.college` | `http://minio.s3.svc.cluster.local.:9000` | S3 API endpoint |
| `superset.orion.dataplatform.college` | `http://100.75.13.86:30888` | Superset BI |
| `airflow.orion.dataplatform.college` | `http://airflow-api-server.airflow.svc.cluster.local.:8080` | Airflow API Server |
| `openmetadata.orion.dataplatform.college` | `http://openmetadata-server.openmetadata.svc.cluster.local.:8585` | OpenMetadata Server |
| `grafana.orion.dataplatform.college` | `http://grafana.monitoring.svc.cluster.local.:3000` | Grafana Мониторинг |
| `plane.infra.dataplatform.college` | `http://plane-web.plane.svc.cluster.local.:80` | Plane Task Tracker |
| `plane.dataplatform.college` | `http://plane-web.plane.svc.cluster.local.:80` | Plane Task Tracker |
| `gitlab.infra.dataplatform.college` | `http://gitlab-ce.gitlab.svc.cluster.local.:30080` | GitLab CE |
| `gitlab.dataplatform.college` | `http://gitlab-ce.gitlab.svc.cluster.local.:30080` | GitLab CE |
| `docker.infra.dataplatform.college` | `http://docker-registry-ui.container-registry.svc.cluster.local.:80` | Docker Registry Web UI |
| `docker.dataplatform.college` | `http://docker-registry-ui.container-registry.svc.cluster.local.:80` | Docker Registry Web UI |
| `registry.infra.dataplatform.college` | `http://docker-registry.container-registry.svc.cluster.local.:5000` | Docker OCI Registry v2 |
| `registry.dataplatform.college` | `http://docker-registry.container-registry.svc.cluster.local.:5000` | Docker OCI Registry v2 |
| `minio.infra.dataplatform.college` | `http://minio.s3.svc.cluster.local.:9001` | MinIO Console |
| `minio.dataplatform.college` | `http://minio.s3.svc.cluster.local.:9001` | MinIO Console |
| `s3.infra.dataplatform.college` | `http://minio.s3.svc.cluster.local.:9000` | MinIO S3 API |
| `s3.dataplatform.college` | `http://minio.s3.svc.cluster.local.:9000` | MinIO S3 API |
| `grafana.infra.dataplatform.college` | `http://grafana.monitoring.svc.cluster.local.:3000` | Grafana |
| `grafana.dataplatform.college` | `http://grafana.monitoring.svc.cluster.local.:3000` | Grafana |

---

## 4. Cloudflare Zero Trust & Access Policies

- **Командный домен (Team Domain)**: `shiny-queen-d7ad.cloudflareaccess.com`
- **Защищённые приложения (14 штук)**:
  - `llm.smartbet.guru` (Политика: `Admin Policy`)
  - `crawler.smartbet.guru` (Политика: `Admin Policy`)
  - `proxy.smartbet.guru` (Политика: `Admin Policy`)
  - `igaming-admin.smartbet.guru` (Политики: `Admin Policy`, `Slack Webhooks Bypass`)
  - `plane.smartbet.guru` (Политика: `Allow User Email`)
  - `airflow.smartbet.guru` (Политика: `Allow Aleksei`)
  - 8 сервисов под доменом `*.dataplatform.college` (Политика: `Allow Owner`)

### 4.1. Переиспользуемые политики безопасности (Reusable Policies)
- **`Admin Policy`** (`9655316a-2de3-470a-afb4-fe5d1c644344`): Action `Allow`. Доступ только авторизованным администраторам.
- **`Allow Aleksei`** (`01e34ae4-3297-402e-be5d-79b6903d3b09`): Action `Allow`. Персональный доступ владельца.
- **`Allow User Email`** (`d7e2be18-ba12-4a7f-9350-f76fa11c38f8`): Action `Allow` по белому списку корпоративных email.
- **`Slack Webhooks Bypass`** (`07669d4b-d5bf-42a8-92b5-b87da6ec732d`): Action `Bypass` для внешних интеграций вебхуков Slack без прохождения интерактивного Zero Trust логина.

### 4.2. Интеграция с бэкенд-сервисами в коде
Сервисы (например, [`igaming-affiliate-service`](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-affiliate-service/src/main/java/pro/datawiki/igaming/affiliate/security/CloudflareAccessInterceptor.java)) валидируют входящий трафик от Zero Trust через HTTP-заголовки:
- `Cf-Access-Authenticated-User-Email`
- `Cf-Access-Jwt-Assertion`
- Для служебных запросов предусмотрен заголовок `X-Admin-Secret` (`sb_admin_cf_secret_2026`).

---

## 5. Маршрутизация трафика РФ и обход замедлений РКН (RU Bypass)

Нода `xeon-srv` физически расположена в РФ (Санкт-Петербург, `188.242.33.93`). 
В связи с тем, что ТСПУ/РКН периодически замедляет или блокирует пулы Anycast IP Cloudflare внутри России, настроена специальная двухконтурная маршрутизация:

1. **WAF Redirect Rule (Cloudflare Edge)**:
   - **Название правила**: `Redirect RU visitors to ru.smartbet.guru for bypass of RKN throttling`
   - **Условие (Expression)**:
     ```
     ip.geoip.country eq "RU" and (http.host eq "smartbet.guru" or http.host eq "www.smartbet.guru")
     ```
   - **Действие**: HTTP 302 редирект на:
     ```
     concat("https://ru.smartbet.guru", http.request.uri.path)
     ```
2. **Прямые DNS-записи (Unproxied / DNS Only)**:
   - `ru.smartbet.guru` $\rightarrow$ `188.242.33.93` (A-запись без Cloudflare Proxy)
   - `ru-proxy.smartbet.guru` $\rightarrow$ `188.242.33.93` (A-запись без Cloudflare Proxy)
   - `direct-admin.smartbet.guru` $\rightarrow$ `188.242.33.93` (A-запись без Cloudflare Proxy)
   - `direct-voice.smartbet.guru` $\rightarrow$ `188.242.33.93` (A-запись без Cloudflare Proxy)
3. **Результат**:
   - Пользователи из РФ попадают напрямую на домашний статический IP в Санкт-Петербурге без замедлений и промежуточных блокировок.
   - Пользователи из остальных стран мира обслуживаются через защищённый Cloudflare Edge Anycast CDN/WAF.

---

## 6. Настройки SSL/TLS

- **SSL/TLS Encryption Mode**: **Full** (трафик от Cloudflare к origin шифруется).
- **Always Use HTTPS**: **Enabled** (принудительный редирект HTTP $\rightarrow$ HTTPS).
- **TLS 1.3**: **Enabled**.
- **Encrypted Client Hello (ECH)**: **Disabled (off)** (отключено на уровне зоны для предотвращения сброса TLS-хэндшейка мобильными операторами РФ при проверке DPI/ТСПУ).
- **Minimum TLS Version**: **1.0** (для максимальной совместимости мобильных клиентов).
- **Edge Certificates**: Universal SSL сертификаты на `smartbet.guru` и `*.smartbet.guru`.

---

## 7. Защита от спам-ботов и скрейперов (Bot & Scraping Shield)

В связи с выявлением паразитной активности (360+ бот-сессий из Китая: Beijing, Qingdao, Chengdu), внедрена многоуровневая защита:

1. **Edge Middleware Shield (`src/middleware.ts`)**:
   - Геолокация: запросы с `cf-ipcountry: CN` мгновенно блокируются с кодом `403 Forbidden`.
   - Сигнатуры User-Agent: фильтрация ботов `Bytespider`, `Baiduspider`, `YisouSpider`, `Sogou`, `PetalBot`, `AspiegelBot`.
   - Защита отрабатывает за **<1 мс** на границе приложения без обращения к API, базе данных и рендерингу SSR.

---

## 8. SEO и изоляция инфраструктурных сервисов

1. **Изоляция Apache Superset (`superset.smartbet.guru`)**:
   - Служебный поддомен полностью исключен из индекса поисковых систем Google Search Console через постоянный URL Removal.
2. **Host-Aware Canonical Resolution (`src/lib/seo.ts`)**:
   - Канонические URL формируются строго относительно текущего хоста запроса (`smartbet.guru` vs `ru.smartbet.guru` vs `sportvibe.today`), что ликвидировало проблему Canonical Mismatch (11 206 страниц в GSC).
3. **Кэширование Sitemap.xml**:
   - Реализовано двухуровневое кэширование (in-memory + Cloudflare Edge CDN `s-maxage=86400`), статус в Search Console подтвержден как **🟢 Успешно**.
