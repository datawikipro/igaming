# 📖 Руководство по проекту SmartBet.guru / igaming

> Этот документ — **главный справочник** по проекту для разработчиков, DevOps и AI-ассистентов.
> Обновляй его при каждом значимом изменении архитектуры или инфраструктуры.

---

## 🗂️ Оглавление

1. [Что такое этот проект](#1-что-такое-этот-проект)
2. [Репозитории и рабочие директории](#2-репозитории-и-рабочие-директории)
3. [Архитектура системы](#3-архитектура-системы)
4. [Микросервисы и модули](#4-микросервисы-и-модули)
5. [Kubernetes-инфраструктура](#5-kubernetes-инфраструктура)
6. [Правила расстановки нод (Node Scheduling)](#6-правила-расстановки-нод-node-scheduling)
7. [Букмекеры и источники данных](#7-букмекеры-и-источники-данных)
8. [CI/CD и деплой](#8-cicd-и-деплой)
9. [Локальная разработка](#9-локальная-разработка)
10. [Частые проблемы и их решение](#10-частые-проблемы-и-их-решение)

---

## 1. Что такое этот проект

**SmartBet.guru** — спортивно-аналитическая SaaS-платформа для поиска **спортивных арбитражных ситуаций (вилок)**, **ставок с перевесом (+EV)** и **коридоров** в реальном времени.

**Бизнес-модель**: подписка (Free / Premium) + реферальная программа + продажа API-потока данных.

**Целевая аудитория**: профессиональные беттеры, арбитражники, разработчики ботов для ставок.

---

## 2. Репозитории и рабочие директории

Проект физически разделён на **3 директории** на локальной машине:

| Директория | Назначение | Что там лежит |
|---|---|---|
| `C:\Users\chernousov_a\IdeaProjects\igaming` | **Основной монорепо** | Все бэкенд-сервисы (Java/Spring), K8s YAML-манифесты, скрипты DevOps |
| `C:\Users\chernousov_a\WebstormProjects\igaming` | **Фронтенд** | Next.js приложение `smartbet.guru` |
| `C:\Users\chernousov_a\Documents\igaming` | **Рабочая директория** | Скрипты, kubectl-команды, временные файлы |

> ⚠️ **Важно**: kubectl-команды выполнять из `Documents\igaming`. Бэкенд-код редактировать в `IdeaProjects\igaming`. Фронтенд в `WebstormProjects\igaming`.

### Ключевые поддиректории в `IdeaProjects\igaming`:

```
igaming/
├── igaming-k8s/          # K8s YAML-манифесты для всех источников (букмекеров)
├── igaming-aggregator/   # Главный агрегатор (Spring Boot, Java)
├── igaming-portal/       # API-шлюз + бизнес-логика (Spring Boot, Java)
├── igaming-bot/          # Telegram-бот (Spring Boot, Java)
├── igaming-source-*/     # Краулеры/лоадеры для каждого букмекера
├── igaming-capture-*/    # Сборщики live-результатов (SofaScore, LiveResult)
├── igaming-auth-microservice/  # Авторизация (Spring Security + JWT)
├── service-proxy-backend/      # Прокси-бэкенд для VPN/ротации IP
└── igaming-admin-backend/      # Бэкенд панели администратора
```

---

## 3. Архитектура системы

```
┌─────────────────────────────────────────────────────────────┐
│                    ПОЛЬЗОВАТЕЛИ                              │
│   Next.js (smartbet.guru)  │  Chrome Extension  │  TG Bot   │
└──────────────┬──────────────────────────┬────────────────────┘
               │ REST / WebSocket         │
               ▼                          ▼
┌──────────────────────────┐   ┌──────────────────────────────┐
│    smartbet-portal       │   │      igaming-bot             │
│  (API Gateway + Auth +   │   │   (Telegram + фильтры        │
│   Tier Filter + кэш)     │   │    по подписке)              │
└──────────────┬───────────┘   └──────────────────────────────┘
               │
               ▼
┌──────────────────────────┐
│   igaming-aggregator     │◄──── Kafka ──────────────────────┐
│  (матчинг вилок в RAM,   │                                  │
│   хранение в PostgreSQL) │                                  │
└──────────────────────────┘                                  │
                                                              │
┌───────────────────────────────────────────────────┐         │
│              igaming-source-* (краулеры)          │─────────┘
│  1xbet │ Fonbet │ Winline │ Pinnacle │ Betcity...  │
│  каждый: [crawler] + [loader x2] + [postgres-db]  │
└───────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────┐
│  service-proxy-backend   │  (ротация IP, VPN-пулы)
└──────────────────────────┘
```

### Поток данных (упрощённо):

1. **Краулер** (`igaming-source-*-crawler`) — один инстанс, держит подключение к API/WS букмекера, получает обновления котировок, пишет в локальную Postgres-БД.
2. **Лоадер** (`igaming-source-*-loader`) — 2+ инстанса, читает из БД, нормализует данные, отправляет в **Kafka**.
3. **Агрегатор** (`igaming-aggregator`) — слушает Kafka, матчит события разных БК в памяти, находит вилки, сохраняет в свою Postgres.
4. **Портал** (`smartbet-portal`) — проксирует запросы фронтенда к агрегатору, применяет фильтры по тарифу (Free/Premium), кэширует ответы.
5. **Фронтенд** (`smartbet.guru`) — Next.js, подключается к порталу по REST/WebSocket, отображает вилки в реальном времени.

---

## 4. Микросервисы и модули

### 4.1 Ключевые бэкенд-сервисы

| Сервис | Стек | Namespace K8s | Описание |
|---|---|---|---|
| `igaming-aggregator` | Spring Boot, Kafka, JPA | `igaming-dev` | Матчинг вилок, центральная БД |
| `igaming-portal` | Spring Boot, JWT | `igaming-dev` | API-шлюз, тарифы, кэш |
| `igaming-bot` | Spring Boot, TelegramBots | `igaming-dev` | Telegram-бот уведомлений |
| `igaming-auth-microservice` | Spring Security | `igaming-dev` | Авторизация / JWT |
| `igaming-redis` | Redis | `igaming-dev` | Кэш коэффициентов (sidecar в подах лоадеров) |
| `service-proxy-backend` | Node.js | `service-proxy` | VPN-прокси для краулеров |
| `smartbet-mobile-app` | (фронт) | `igaming-dev` | Мобильная версия |

### 4.2 Источники данных (igaming-source-*)

Каждый источник = **3 компонента**:

```
igaming-source-{bookmaker}/
├── crawler   → 1 реплика  → nodeSelector: standard (preferred)
├── loader    → 2 реплики  → nodeSelector: standard (preferred), spot допускается
└── db (PostgreSQL StatefulSet) → nodeSelector: master (обязательно)
```

**Список букмекеров**: 1xbet, 888starz, baltbet, betandyou, betboom, betcity (com + ru), betm, bettery, fansport, fon.bet, fonbet.by, fonbet.kz, leon, ligastavok, linebet, marathonbet (by + com + ru), megapari, melbet, olimpbet (kz + ru), pari, pinnacle, sportbet, tennisi, winline, zenit.

### 4.3 Live-capture сервисы

| Сервис | Назначение |
|---|---|
| `igaming-capture-liveresult` | Сбор live-результатов матчей |
| `igaming-capture-sofascore` | Интеграция с SofaScore API |

---

## 5. Kubernetes-инфраструктура

### Кластер

- **Оркестратор**: k3s v1.35+
- **Master**: `master-vm` — CentOS Stream 10, статичный сервер
- **Worker-ноды**: `std-gcp-*` — GCP VM, CentOS Stream 9, динамические

### Ноды кластера (актуально на 2026-06)

| Нода | Роль | `node-type` | OS | Описание |
|---|---|---|---|---|
| `master-vm` | control-plane | `master` | CentOS 10 | Постоянный сервер, хранит БД |
| `std-gcp-1f150cfc` | worker | `standard` | CentOS 9 | GCP, europe-north2 |
| `std-gcp-2d21a892` | worker | `standard` | CentOS 9 | GCP, europe-north1 |
| `std-gcp-cb56b22e` | worker | `standard` | CentOS 9 | GCP, europe-north1 |
| `std-gcp-d903cb72` | worker | `standard` | CentOS 9 | GCP, europe-north2 |
| `std-gcp-ef0f10d5` | worker | `standard` | CentOS 9 | GCP, europe-north1 |

> **Spot-ноды** (`node-type=spot`) — временные, GCP может их удалять. Используются только для масштабирования под нагрузкой. Сейчас отсутствуют.

### Namespaces

| Namespace | Что там |
|---|---|
| `igaming-dev` | Все игровые сервисы, краулеры, агрегатор |
| `service-proxy` | Прокси-инфраструктура |
| `s3-dev` | Airflow (оркестрация задач) |
| `kube-system` | Системные компоненты k3s |

---

## 6. Правила расстановки нод (Node Scheduling)

> ⚠️ **Это критически важный раздел.** Неправильный `nodeSelector` = Pending поды.

### Правило 1: `-db` StatefulSets → `master`

Все PostgreSQL-базы данных краулеров ДОЛЖНЫ запускаться на `master-vm`.
Причина: PVC (PersistentVolumeClaim) привязан к локальному диску мастера.

```yaml
# В StatefulSet для БД:
spec:
  template:
    spec:
      nodeSelector:
        node-type: master
```

### Правило 2: `-crawler` и `-loader` (первый инстанс) → `standard`

Базовые рабочие поды запускаются на стабильных standard-нодах. Spot допускается как запасной вариант.

```yaml
# В Deployment для crawler/loader:
spec:
  template:
    spec:
      affinity:
        nodeAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 10
            preference:
              matchExpressions:
              - key: node-type
                operator: In
                values:
                - standard
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: node-type
                operator: In
                values:
                - spot
                - standard   # ← допускаются обе, предпочтительна standard
```

### Правило 3: Дополнительные реплики (масштабирование) → `spot`

Когда нужно больше лоадеров под нагрузкой — деплоятся на spot-ноды (дешевле).

### Итоговая таблица

| Тип пода | nodeSelector / affinity | Может на master? | Может на standard? | Может на spot? |
|---|---|---|---|---|
| `*-db` (PostgreSQL) | `required: master` | ✅ Да | ❌ Нет | ❌ Нет |
| `*-crawler` | `preferred: standard, allowed: standard+spot` | ❌ Нет | ✅ Да (приоритет) | ✅ Запасной |
| `*-loader` (реплика 1) | `preferred: standard, allowed: standard+spot` | ❌ Нет | ✅ Да (приоритет) | ✅ Запасной |
| `*-loader` (доп. реплики) | `preferred: spot` | ❌ Нет | ✅ Запасной | ✅ Да (приоритет) |
| `igaming-bot`, `igaming-redis`, сервисы | `allowed: standard+spot` | ❌ Нет | ✅ Да | ✅ Да |
| `igaming-aggregator`, `igaming-portal` | `required: standard` | ❌ Нет | ✅ Да | ❌ Нет |

---

## 7. Букмекеры и источники данных

### Активные источники

Манифесты в `igaming-k8s/*.yaml`. Каждый файл содержит: Service + Deployment (crawler) + Service + Deployment (loader) + Service + StatefulSet (db).

| Файл | Букмекер | Статус |
|---|---|---|
| `1xbet.yaml` | 1xBet | ✅ |
| `pinnacle.yaml` | Pinnacle | ⚠️ БД Pending (нет spot-ноды) |
| `marathonbet.by.yaml` | MarathonBet BY | ⚠️ БД Pending |
| `marathonbet.com.yaml` | MarathonBet COM | ⚠️ БД Pending |
| `betcity.com.yaml` | BetCity COM | ⚠️ БД Pending |
| ... | остальные | ✅ |

### Структура YAML-манифеста источника

```yaml
# 1. Service для crawler
# 2. Deployment crawler (1 реплика)
# 3. Service для loader
# 4. Deployment loader (2 реплики)
# 5. Service для db
# 6. StatefulSet db (postgres:15-alpine)
```

### 7.3 Режимы работы браузера (Stealth Profiles)

Для экономии ресурсов кластера и эффективного обхода защит от ботов (Cloudflare, Qrator и др.) в проекте действует правило выбора профиля браузера для краулеров.

#### ⚙️ Три режима работы браузера:
1. **`BASIC` (Легкий режим)**:
   - *Запуск*: Headless (без GUI), без stealth-аргументов.
   - *По умолчанию*: Прописан как default-значение в [BrowserService.java](file:///c:/Users/chernousov_a/IdeaProjects/igaming/igaming-source-core/src/main/java/pro/datawiki/igaming/source/core/browser/BrowserService.java).
   - *Где используется*: Для всех простых источников, не использующих сложную защиту или парсящих данные через чистые HTTP API (например, `baltbet`, `leon`, `pinnacle`, `sbobet`).
2. **`HEADLESS_STEALTH` (Средний режим)**:
   - *Запуск*: Headless с подключением stealth-плагинов/аргументов Playwright.
   - *Где используется*: Для источников, требующих маскировки, но не блокирующих обычные headless-запросы (например, `winline`, `marathonbet`, `betcity`, `olimpbet`, `zenit`, `fon-bet-ru`).
3. **`XVFB_HEADED` (Максимальный режим)**:
   - *Запуск*: Headed (полноценное окно браузера с рендерингом), запущенное внутри виртуального фреймбуфера `Xvfb` (дисплей `:99`), который стартует через `entrypoint.sh` внутри контейнера.
   - *Где используется*: Для сайтов с жесткой защитой («совсем плохо»), блокирующих headless-браузеры (семейство `Fonbet` на зарубежных зеркалах — `pari`, `bettery`, `fonbet.by`, `fonbet.kz`; семейство `Betb2b/1xbet`; `ligastavok`; `tennisi`; `sportbet`).

#### ⚠️ Правила ресурсосбережения (Важно для AI-ассистентов):
* **Использовать минимально возможный режим**:
  * Если можно использовать `BASIC` — используем его.
  * Если заблокировано — повышаем до `HEADLESS_STEALTH`.
  * Если совсем жесткая защита — только тогда переходим на `XVFB_HEADED`.
* **Запрет глобального включения**: Нельзя глобально включать `HEADLESS_STEALTH` или `XVFB_HEADED` для всех краулеров, так как это перегрузит CPU/RAM на нодах кластера.
* **Исключения (только BASIC)**: Краулеры `baltbet`, `leon`, `pinnacle` и `sbobet` **должны всегда оставаться на дефолтном `BASIC`** (без переопределения переменной в K8s-манифестах).

#### 🛠️ Настройка через манифесты:
Режим конфигурируется через переменную окружения `APP_BROWSER_STEALTH_PROFILE` в блоке `env` контейнера `-crawler` в K8s-манифесте:
```yaml
        env:
        - name: APP_BROWSER_STEALTH_PROFILE
          value: XVFB_HEADED  # или HEADLESS_STEALTH
```
Если переменная отсутствует, по умолчанию применяется легкий режим `BASIC`.

---

## 8. CI/CD и деплой

### ⚠️ Основные правила сборки и деплоя (Для AI-ассистентов):
1. **GitHub Actions НЕ используются для деплоя** (несмотря на наличие файлов рабочих процессов в `.github/workflows`).
2. **Все сборки и деплой выполняются локально через PowerShell-скрипты**:
   - `.\restart-ci.ps1` — для сборки всех модулей.
   - `.\restart-ci.ps1 -Only {имя_модуля}` — для сборки и перезапуска конкретного модуля (например, `tennisi` или `fon-bet-ru`).
3. **Строгая последовательность запуска (КРИТИЧЕСКИ ВАЖНО)**:
   - Сборка на удаленном сервере сборочной машины (`100.86.137.112`) использует единую общую рабочую директорию (`build/igaming`), где перед началом каждой сборки выполняется сброс локальной ветки (`git reset --hard FETCH_HEAD`).
   - Из-за этого **параллельный запуск нескольких процессов `restart-ci.ps1` категорически запрещен**, так как они будут сбрасывать и затирать файлы друг друга. Все сборки должны запускаться **строго по очереди**.

### Образы Docker

Все образы публикуются в **GitHub Container Registry**:
```
ghcr.io/datawikipro/igaming-source-{bookmaker}:latest
ghcr.io/datawikipro/igaming-aggregator:latest
ghcr.io/datawikipro/igaming-portal:latest
ghcr.io/datawikipro/smartbet.guru:latest
```

Секрет для pull: `regcred` (во всех namespace).

### Деплой нового источника (букмекера)

```powershell
# 1. Применить манифест
kubectl apply -f igaming-k8s/{bookmaker}.yaml

# 2. Проверить поды
kubectl get pods -n igaming-dev | Select-String "{bookmaker}"

# 3. Если БД Pending — проверить nodeSelector
kubectl describe pod {pod-name} -n igaming-dev | Select-String "Node-Selectors|Events"
```

### Обновление образа (rolling update)

```powershell
kubectl rollout restart deployment/{deployment-name} -n igaming-dev
```

---

## 9. Локальная разработка

### Стек

| Компонент | Версия | Установка |
|---|---|---|
| Java | 21 | IntelliJ IDEA |
| Maven | 3.9+ | IntelliJ IDEA |
| Node.js | 20+ | npm |
| kubectl | latest | chocolatey / winget |
| k3s | v1.35+ | на master-vm |

### Переменные окружения (бэкенд)

Каждый `igaming-source-*` использует:
```
SPRING_PROFILES_ACTIVE=league-crawler | match-loader
SPRING_DATASOURCE_URL=jdbc:postgresql://igaming-source-{bm}-db:5432/igaming_{bm}
SPRING_DATA_REDIS_HOST=localhost
APP_AGGREGATOR_URL=http://igaming-aggregator
APP_PROXY_BACKEND_URL=http://service-proxy-backend.service-proxy.svc.cluster.local
JAVA_OPTS=-XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0 -XX:+UseG1GC
```

### Фронтенд (Next.js)

```powershell
# Директория: C:\Users\chernousov_a\WebstormProjects\igaming\smartbet.guru
npm run dev   # localhost:3000
npm run build # production build
```

---

## 10. Частые проблемы и их решение

### ❌ Поды в статусе Pending: "didn't match Pod's node affinity/selector"

**Причина**: Не хватает нод с нужным `node-type` лейблом.

**Диагностика**:
```powershell
# Посмотреть все Pending поды
kubectl get pods --all-namespaces --field-selector=status.phase=Pending

# Узнать причину конкретного пода
kubectl describe pod {pod-name} -n igaming-dev | Select-String "Node-Selectors|Events" -Context 2

# Посмотреть лейблы нод
kubectl get nodes --show-labels
```

**Решение для -db подов** (нет ноды с `node-type=master`):
```powershell
# Применить nodeSelector прямо в StatefulSet
kubectl patch statefulset {ss-name} -n igaming-dev --type='json' `
  -p='[{"op":"add","path":"/spec/template/spec/nodeSelector","value":{"node-type":"master"}}]'
```

**Решение для crawler/loader подов** (нет spot-нод, а YAML требует их):
- Убедиться что в `required` есть `standard` (не только `spot`)
- Или навесить лейбл `spot` на standard-ноды временно:
```powershell
kubectl label node std-gcp-1f150cfc node-type=spot --overwrite
```

---

### ❌ CrashLoopBackOff у краулера/лоадера

**Диагностика**:
```powershell
kubectl logs {pod-name} -n igaming-dev --previous
kubectl describe pod {pod-name} -n igaming-dev
```

**Частые причины**:
- БД недоступна (StatefulSet не запустился)
- Не хватает памяти (OOMKilled) → увеличить `resources.limits.memory`
- Букмекер сменил API → смотреть логи на `Connection refused` / `403`

---

### ❌ Нода master-vm перегружена (CPU 100%)

Симптом: все поды на мастере медленные, kubectl тормозит.

```powershell
# Посмотреть что жрёт ресурсы
kubectl top pods -n igaming-dev --sort-by=cpu

# Перенести тяжёлые поды на worker-ноды
# Убедиться что на master-vm только -db StatefulSets
kubectl get pods -n igaming-dev -o wide | Select-String "master-vm"
```

---

### ❌ Spot-ноды исчезли (GCP вытеснил)

Все поды с `node-type=spot` переходят в Pending.

**Быстрое решение**: временно навесить `spot` лейбл на standard-ноды:
```powershell
kubectl label nodes std-gcp-1f150cfc std-gcp-2d21a892 std-gcp-cb56b22e `
  std-gcp-d903cb72 std-gcp-ef0f10d5 node-type=spot --overwrite
```

**Правильное решение**: поднять новые spot-ноды через скрипт `add_stable_nodes.ps1` или GCP Console.

---

## 📋 Полезные команды kubectl

```powershell
# Все поды с их нодами
kubectl get pods -n igaming-dev -o wide

# Все Pending поды
kubectl get pods --all-namespaces --field-selector=status.phase=Pending

# Перезапустить все деплойменты в неймспейсе
kubectl rollout restart deployment -n igaming-dev

# Смотреть логи в реальном времени
kubectl logs -f {pod-name} -n igaming-dev

# Ресурсы нод
kubectl top nodes

# Ресурсы подов
kubectl top pods -n igaming-dev --sort-by=memory

# Применить всё из igaming-k8s
kubectl apply -f igaming-k8s/

# Удалить и пересоздать под (StatefulSet нельзя delete просто так)
kubectl delete pod {pod-name} -n igaming-dev  # StatefulSet сам пересоздаст
```

---

*Последнее обновление: 2026-06-02. Обновляй этот документ при изменении инфраструктуры.*
