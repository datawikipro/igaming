# Design: [smm-meta] ИИ-агент активности для Threads и Instagram (Firefox Persistent Context + Cache Warmup)

## Архитектура компонента `smm-bot-meta`

### 1. Назначение и изоляция
- Выделенный агент и K8s Deployment `smm-bot-meta` в неймспейсе `igaming-dev`.
- Поддержка двух Meta платформ: **Threads** (`threads.net`) и **Instagram** (`instagram.com`).
- Полное соблюдение Rule 9 (Firefox Persistent Context, запрет `new_context()`, прогрев кэша через нейтральные порталы) и Rule 10 (Фрибет = 80% гарантированных денег, партнерские воронки с UTM).

### 2. Конфигурация агента (`MetaAgentConfig`)
- `account_id`: идентификатор профиля (по умолчанию `meta_persona_main`).
- `redis_url`: URL подключения к Redis (`redis://igaming-redis:6379/0`).
- `session_key`: ключ сессии в Redis (`smm:session:meta`).
- `profile_key`: ключ архива браузерного профиля в Redis (`smm:profile:<account_id>`).
- `proxy_server`: кластерный US-прокси `http://100.83.113.50:3128` (маршрутизируется через `outline-us` 100.66.190.4 согласно Rule 6).
- `warmup_duration_seconds`: длительность предварительного прогрева кэша (по умолчанию 120 сек).
- `threads_base_url`: `https://www.threads.net`.
- `instagram_base_url`: `https://www.instagram.com`.
- `portal_api_url`: `http://igaming-portal:80`.
- `healthcheck_port`: порт встроенного HTTP-сервера (8080).

### 3. Персистентность сессии и профиля в Redis
- **Браузерный профиль** (`smm:profile:<account_id>`):
  - Каталог профиля Firefox (`cookies.sqlite`, `storage/default`, LocalStorage, IndexedDB, Cache).
  - Сжатие в tar.gz в памяти и сохранение в Redis при старте/завершении каждого цикла активности.
- **Состояние сессии** (`smm:session:meta`):
  ```json
  {
    "account_id": "meta_persona_main",
    "last_warmup_timestamp": 1727632800,
    "last_activity_timestamp": 1727633100,
    "total_posts_published": 14,
    "total_likes_given": 48,
    "threads_active": true,
    "instagram_active": true,
    "status": "HEALTHY",
    "updated_at": "2026-09-30T00:00:00Z"
  }
  ```

### 4. Обязательный прогрев кэша (Rule 9 Cache Warmup)
- Перед взаимодействием с доменами Meta агент вызывает `CacheWarmupManager`:
  - 2–3 минуты органического веб-серфинга по списку авторитетных порталов: BBC Sport, ESPN, Flashscore, Wikipedia Sports.
  - Человекоподобная кинематика: траектории перемещения мыши на основе кубических кривых Безье, микро-тремор, рандомизированные задержки чтения, интервальный скроллинг вниз и возвраты вверх.
  - Накопление HTTP-кэша, CDN-скриптов и куки в персистентном профиле перед обращением к антифрод-шлюзам Meta.

### 5. Человекоподобная активность на платформах Meta
- **Threads Activity Routine**:
  - Переход на `https://www.threads.net/` в персистентном контексте.
  - Плавный скроллинг ленты рекомендаций (Explore Feed) с паузами от 2 до 6 секунд на "чтение" постов.
  - Органические реакции (Like) с естественной вероятностью (15–25%).
  - Публикация тематических постов:
    - Математические разборы вилок и валуйных ставок (+EV).
    - Калькуляция конвертации фрибетов (80% Guaranteed Cash).
- **Instagram Activity Routine**:
  - Навигация по `https://www.instagram.com/`.
  - Просмотр ленты и Explore-раздела, скроллинг каруселей и фото.
  - Паузы и имитация чтения описаний.

### 6. Контент-политика и математика фрибетов (Rule 10)
- **Формула конвертации фрибета SNR (Stake Not Returned)**:
  $$\eta = \frac{(K_1 - 1)(K_2 - 1)}{K_2} \approx 0.80$$
  При $K_1 = 5.0, K_2 = 1.25$:
  $$\eta = \frac{(5.0 - 1)(1.25 - 1)}{1.25} = \frac{4.0 \times 0.25}{1.25} = 0.80 \ (80\%)$$
- **Текстовые шаблоны постов**:
  - Пример: *«⚡ Математика SmartBet.guru: как забрать 80% от любого фрибета гарантированно деньгами на карту?
    Фрибет 3 000 ₽ → 2 400 ₽ гарантированного кэша при любом исходе матча через арбитражное перекрытие (K1=5.00, K2=1.25).
    Подробный калькулятор и каталог фрибетов 52 БК: https://smartbet.guru/promos?utm_source=meta&utm_medium=threads&utm_campaign=freebet80
    ⚠️ Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно.»*
- **Обязательный дисклеймер**:
  Каждый пост строго валидируется на наличие фразы:
  *«Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно.»*

### 7. Мониторинг, Healthcheck & K8s
- HTTP-сервер на порту 8080:
  - `GET /healthz` -> HTTP 200 `{"status": "UP", "service": "smm-bot-meta"}`
  - `GET /actuator/health` -> HTTP 200 `{"status": "UP"}`
  - `GET /api/v1/meta/status` -> HTTP 200 JSON со статистикой сессии и метриками активности.
- K8s Deployment `smm-bot-meta` и Service `smm-bot-meta` в namespace `igaming-dev`.
- Строго K8s DNS Service Names: `igaming-redis.igaming-dev.svc.cluster.local`, `igaming-portal:80`.
