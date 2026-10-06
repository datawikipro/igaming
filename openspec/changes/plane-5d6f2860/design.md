# Architecture Design: #82: [marathonbet-by] Подключить Playwright volume mount и восстановить сбор котировок

## 1. Архитектура интеграции Marathonbet (BY)
Модуль `igaming-source-marathonbet-by` осуществляет сбор линий и котировок белорусского букмекера Marathonbet (`www.marathonbet.by`):
- `igaming-source-marathonbet-by-crawler`: сервис в роли `league-crawler`. Периодически обращается к API/SPA меню (`/su/react/event/menu/prematch`, `/su/react/event/menu/live`) для получения дерева категорий, лиг и матчей. При недоступности прямого HTTP-запроса выполняет fallback на `BrowserService` (Playwright Chromium).
- `igaming-source-marathonbet-by-loader`: сервис в роли `match-loader`. Загружает подробные рынки котировок по собранным матчам, сохраняет их в PostgreSQL (`igaming_marathonbet_by`) и транслирует обновления в Aggregator API.
- `igaming-source-marathonbet-by-db`: база данных PostgreSQL 15, содержащая таблицы `match_cache`, `league_cache`, `sport_cache`, `match_factor`.

## 2. Подключение Playwright и хранилища браузеров
Для предотвращения ошибки `Executable doesn't exist at /root/.cache/ms-playwright/chromium-1105/chrome-linux/chrome`:
- **Volume Mount**: подключение хостового каталога `/data/playwright-browsers` ноды `xeon-srv` в точку монтирования `/root/.cache/ms-playwright` через `hostPath` (type `Directory`).
- **Shared Memory & Temp Volumes**: монтирование `emptyDir` (medium `Memory`, sizeLimit `1Gi`) в `/dev/shm` и `/tmp` для предотвращения крашей браузерных процессов при рендеринге сложных страниц.
- **Environment Variables**:
  - `PLAYWRIGHT_BROWSERS_PATH=/root/.cache/ms-playwright` — явное указание каталога с предустановленными бинарниками Chromium, Firefox и WebKit.
  - `PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=true` — пропуск валидации системных зависимостей хоста для ускорения старта.

## 3. Пробы жизнеспособности (Actuator Probes) и класс приоритета
- В соответствии с Golden Rule 1 и спецификацией `verification-and-dod`, краулер оснащается пробами Kubernetes:
  - `startupProbe`: `/actuator/health/readiness` (port 3038, initialDelaySeconds 15, failureThreshold 30).
  - `readinessProbe`: `/actuator/health/readiness` (port 3038, initialDelaySeconds 10, failureThreshold 5).
  - `livenessProbe`: `/actuator/health/liveness` (port 3038, initialDelaySeconds 20, failureThreshold 5).
- Добавление `priorityClassName: prod-critical` для защиты подов краулера и лоадера от вытеснения (eviction) планировщиком Kubernetes.

## 4. Стратегия наполнения линии и соответствие критериям DoD
- После применения обновленной конфигурации Playwright `BrowserService` сможет успешно инициализировать инстанс Chromium и загрузить дерево событий Marathonbet.
- Запуск 5-минутного soak-теста (`schedule`) для контроля отсутствия необработанных исключений (`NullPointerException`, `CrashLoopBackOff`).
- Мониторинг наполнения таблицы `match_cache` в базе данных `igaming_marathonbet_by` до достижения порога Golden Rule 8 (`count(*) >= 500`).
