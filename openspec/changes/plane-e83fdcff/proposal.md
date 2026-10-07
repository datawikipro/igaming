# Proposal: [SMM OFFLINE] Сервис публикации постов smm-bot-telegram недоступен

## Context
- **Plane Task ID**: `e83fdcff-1771-4daf-b003-43b1af8dc71a`
- **Модуль**: `smm-agent` / `igaming-k8s` (`smm-bot-telegram`)
- **Соответствие Golden Rules**:
  - **Rule 1 (Definition of Done & 5-минутный таймер)**: Под задеплоен в namespace `igaming-dev` со статусом `Running 1/1`, Actuator пробы `/actuator/health/readiness` и `/actuator/health/liveness` отдают HTTP 200 `UP`, 5 минут бессбойной работы.
  - **Rule 2 (Запрет IP-адресов — Только K8s DNS Service Names)**: Использование только имен сервисов `igaming-redis`, `igaming-portal`, `igaming-aggregator-db`.
  - **Rule 6 (Сетевая маршрутизация краулеров и ботов)**: Исходящий трафик через кластерный HTTP-прокси `http://100.83.113.50:3128` с обходом ТСПУ/РКН.
  - **Rule 10 (Мониторинг фрибетов, 80% гарантированного кэша и ответственная игра)**: Публикация карточек фрибетов с расчетом 80% гарантированного кэша ($\eta \approx 0.80$), партнерскими ссылками и дисклеймерами ответственной игры.

## Description & Root Cause Analysis (RCA)

### 1. Описание инцидента
Сервис публикации мультиязычных спортивных сигналов и промо-акций в Telegram (`smm-bot-telegram`) был зафиксирован в состоянии офлайн / Unhealthy:
- В кластере Kubernetes периодически фиксировались события `Readiness probe failed: Get "http://10.244.3.100:8080/healthz": dial tcp 10.244.3.100:8080: connect: connection refused`.
- Под выпадал из эндпоинтов Service `smm-bot-telegram`, трафик на Webhook и внутренние API не проходил.
- В репозитории отсутствовал канонический декларативный манифест `igaming-k8s/smm-bot-telegram.yaml`.

### 2. Первопричины сбоя (Root Causes)
1. **Агрессивные параметры Readiness Probe**:
   - В исходной конфигурации пода было установлено `initialDelaySeconds: 3s`, `periodSeconds: 5s`, `failureThreshold: 3`.
   - Процесс Python при старте импортирует библиотеки, инициализирует структуру каналов и подключение к базам данных. В случае динамической попытки установки `psycopg2-binary` или задержек ввода-вывода процесс не успевал забиндить сокет 8080 за 3–13 секунд, что приводило к срыву пробы и переводу пода в Unready / CrashLoopBackOff.
2. **Блокирующий старт шедулера перед HTTP-сервером**:
   - В точке входа `main()` вызов `scheduler.start_scheduler_loop()` запускал рабочий цикл, который сразу же начинал выполнять синхронный широковещательный обход (`broadcast_cycle()`) с тяжелыми запросами к PostgreSQL и внешним Telegram API.
   - Если происходила сетевая задержка, запуск сервера `start_server()` задерживался, и K8s фиксировал отказ соединения.
3. **Отсутствие K8s-манифеста в системе контроля версий**:
   - Манифест `igaming-k8s/smm-bot-telegram.yaml` отсутствовал в Git (в отличие от других SMM-ботов: `smm-bot-meta.yaml`, `smm-bot-patreon.yaml`, `smm-bot-vk.yaml`, `smm-bot-reddit.yaml`).
   - Конфигурация существовала только в виде разового артефакта в кластере, что нарушает GitOps и принцип воспроизводимости инфраструктуры.
4. **Сетевые таймауты SSL при обращении к Telegram Bot API**:
   - Логи фиксировали предупреждения `<urlopen error _ssl.c:999: The handshake operation timed out>` при маршрутизации через прокси `http://100.83.113.50:3128`, требующие улучшенной обработки повторных попыток.
5. **Отсутствие автоматизированных тестов**:
   - В `smm-agent/tests/` отсутствовал тестовый модуль для Telegram-постера, проверяющий Actuator-эндпоинты, форматирование сигналов и устойчивость к сбоям.

### 3. Предлагаемое решение
1. **Подготовка и фиксация архитектуры** в `proposal.md`, `design.md` и дельта-спецификациях OpenSpec.
2. **Оптимизация кода `channel_poster_scheduler.py`**:
   - Неблокирующий запуск HTTP-сервера на порту 8080 до запуска циклов рассылки.
   - Начальная задержка перед первым широковещательным циклом, чтобы сервис успел пройти Readiness probe K8s.
   - Полноценная поддержка Actuator проб: `/actuator/health/readiness`, `/actuator/health/liveness`, `/actuator/health`, `/healthz`.
   - Безопасная инициализация `psycopg2` без зависаний.
3. **Создание манифеста `igaming-k8s/smm-bot-telegram.yaml`**:
   - ConfigMap `smm-bot-telegram-code` с актуальным кодом.
   - Deployment с безопасными таймингами (`initialDelaySeconds: 15s` для readiness, `30s` для liveness, `periodSeconds: 10s`).
   - Service `smm-bot-telegram` на порту 8080.
4. **Разработка набора тестов `smm-agent/tests/test_telegram_poster.py`**:
   - Проверка HTTP Actuator health/readiness/liveness.
   - Проверка математики 80% кэша фрибетов (Rule 10).
   - Проверка форматирования постов для всех 4 каналов (RU, EN, FR, ES) и дисклеймеров.
5. **Деплой и верификация DoD в K8s `igaming-dev`** с обязательным 5-минутным soak-тестом без ошибок.
