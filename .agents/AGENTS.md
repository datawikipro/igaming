# Правила для AI-ассистентов — Проект igaming (бэкенд-монорепо)

Этот документ содержит обязательные правила и контекст для AI-ассистентов, работающих с проектом **SmartBet.guru**.

---

## 📌 Общий контекст проекта

**SmartBet.guru** — спортивно-аналитическая SaaS-платформа для поиска **вилок (Surebets)**, **ставок с перевесом (+EV / ValueBets)** и **коридоров (Middles)** в реальном времени.

- **Бизнес-модель**: подписка Free / Premium + реферальная программа + API-поток.
- **Основная документация**: `/docs/GUIDE.md` — читать ОБЯЗАТЕЛЬНО перед любыми изменениями.
- **Архитектура**: `/docs/архитектура.md`

---

## 🗂️ Структура рабочих директорий

| Директория (macOS) | Назначение |
|---|---|
| `/Users/aleksei.chernousov/IdeaProjects/igaming` | **Этот репозиторий** — бэкенд-монорепо (Java/Spring + K8s + скрипты) |
| `/Users/aleksei.chernousov/WebstormProjects/igaming` | Фронтенд (Next.js `smartbet.guru`, Chrome Extension, Admin UI) |
| `/Users/aleksei.chernousov/Documents/igaming` | Рабочие скрипты, kubectl-команды, сессии |

---

## ⚙️ Ключевые компоненты (этот репозиторий)

| Модуль | Стек | Описание |
|---|---|---|
| `aggregator-api` | Spring Boot | REST API агрегатора (матчи, коэффициенты) |
| `aggregator-domain` | Java | Доменные сущности (Match, Odds, Surebet) |
| `aggregator-ingestion` | Spring Kafka | Приём данных от краулеров через Kafka |
| `aggregator-normalizer` | Spring Boot | Нормализация названий команд/событий |
| `aggregator-surebet` | Spring Boot | Детектор вилок в памяти + запись в PostgreSQL |
| `aggregator-odds-sync` | Spring Boot | Синхронизация коэффициентов между БК |
| `aggregator-enrichment` | Spring Boot | Обогащение данных (статистика, лиги) |
| `igaming-portal` | Spring Boot + JWT | API-шлюз: авторизация, тарифы, кэш, прокси |
| `igaming-bot` | Spring Boot + TelegramBots | Telegram-бот: сигналы вилок по подпискам |
| `igaming-auth-microservice` | Spring Security | JWT-авторизация (auth-base) |
| `igaming-source-core` | Java + Playwright | Базовая библиотека краулеров |
| `igaming-source-*` | Java/Node.js/Python | Краулеры/лоадеры каждого букмекера |
| `igaming-capture-liveresult` | Java | Сбор live-результатов матчей |
| `igaming-capture-sofascore` | Java | Интеграция SofaScore API |
| `service-proxy-backend` | Node.js | VPN/прокси-пул для краулеров |
| `igaming-k8s` | YAML | K8s-манифесты для всех сервисов |
| `igaming-llm-admin` / `igaming-llm-worker` | Python | LLM-инфраструктура |
| `igaming-analytics-service` | Java | Аналитический сервис |
| `igaming-infra-operator` | Go/Java | Kubernetes-оператор |

---

## 🔧 Правила разработки

### CI/CD, Фиче-ветки и Деплой AI-агентами
- **Деплой выполняется автоматически через GitHub Actions** на self-hosted раннере `actions-runner-igaming`.
- **Автономный рабочий цикл AI-агента (Робота)**:
  1. **Создание ветки**: Агент начинает работу над задачей Plane с создания ветки: `python scripts/plane_robot_runner.py start-branch <TASK_KEY>` (создается `task/<task_key>`).
  2. **Разработка & Пробы**: Агент вносит код, настраивает Spring Actuator Liveness/Readiness пробы и тесты.
  3. **Сборка образа `{name}:{task-id}`**: При push в любую ветку GitHub Actions автоматически собирает Docker-образ с тегом `ghcr.io/datawikipro/{module}:{task-id}`.
  4. **Проверка в K8s**: Агент проверяет превью-под в Kubernetes (`kubectl rollout status` и `/actuator/health/liveness`).
  5. **Авто-мердж & Продакшн CI/CD**: Если под 100% здоров, агент запускает `python scripts/plane_robot_runner.py finish-merge <TASK_KEY>`. Изменения мерджатся в `master`, пушатся в `origin`, где CI/CD обновляет образ `:latest` и делает `rollout restart` деплоймента, а задача в Plane переводится в `Done`.

### Kubernetes — правила расстановки нод

> ⚠️ Неправильный `nodeSelector` = Pending-поды. Всегда проверять!

| Тип пода | nodeSelector |
|---|---|
| `*-db` (PostgreSQL StatefulSet) | `required: node-type=master` |
| `*-crawler`, `*-loader` | `preferred: standard`, `allowed: standard+spot` |
| Доп. реплики лоадеров | `preferred: spot` |
| `igaming-aggregator`, `igaming-portal` | `required: standard` |
| Остальные сервисы | `allowed: standard+spot` |

### Docker-образы
Публикуются в GitHub Container Registry:
```
ghcr.io/datawikipro/igaming-source-{bookmaker}:latest
ghcr.io/datawikipro/igaming-aggregator:latest
ghcr.io/datawikipro/igaming-portal:latest
```
Секрет для pull: `regcred`.

---

## 🌐 Браузерные профили краулеров (Stealth Profiles)

Краулеры `igaming-source-*` используют три режима. **Использовать минимально возможный!**

| Режим | Переменная `APP_BROWSER_STEALTH_PROFILE` | Когда использовать |
|---|---|---|
| `BASIC` (default) | *(не задавать)* | Простые API: `baltbet`, `leon`, `pinnacle`, `sbobet` |
| `HEADLESS_STEALTH` | `HEADLESS_STEALTH` | Средняя защита: `winline`, `marathonbet`, `betcity`, `olimpbet`, `zenit`, `fon-bet-ru` |
| `XVFB_HEADED` | `XVFB_HEADED` | Жёсткая защита: семейство Fonbet (зарубежные), семейство BetB2B/1xbet, `ligastavok`, `tennisi`, `sportbet` |

⚠️ **ЗАПРЕТ**: нельзя глобально включать `HEADLESS_STEALTH`/`XVFB_HEADED` для всех краулеров — это перегрузит кластер.

---

## 📋 Активные букмекеры (igaming-source-*)

1xbet, 888sport, baltbet, betb2b, betboom, betcity, betfair, betmgm, betsson, bwin, caesars, dafabet, digitain, draftkings, fanduel, fon-bet-ru, leon, ligastavok, marathonbet, olimpbet, pinnacle, sbobet, sportbet, tennisi, unibet, winline, zenit

---

## 🚫 Автоматизация социальных сетей

При автоматизации (Threads, Instagram и т.п.) **НЕ пытаться войти в аккаунт** — сессия уже настроена.

- **Chrome Profile**: `/Users/aleksei.chernousov/Documents/igaming/sessions/chrome`
- **Credentials**: `/Users/aleksei.chernousov/Documents/igaming/passwords.txt`

Запуск Chrome:
```bash
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
  --remote-debugging-port=9222 \
  --headless=new \
  --user-data-dir="/Users/aleksei.chernousov/Documents/igaming/sessions/chrome" \
  --disable-gpu
```
Прочитать WebSocket URL из `http://127.0.0.1:9222/json/version` и записать в `/Users/aleksei.chernousov/Library/Application Support/Google/Chrome/DevToolsActivePort` перед вызовом browser-субагента.

---

## ⚡ Частые проблемы

- **Поды Pending**: проверь `nodeSelector` / `affinity` — всегда есть `standard` в `required` или `allowed`.
- **CrashLoopBackOff**: смотри `kubectl logs {pod} -n igaming-dev --previous`.
- **Spot-ноды удалены GCP**: временно навесить `node-type=spot` на standard-ноды.
- **OOMKilled**: увеличить `resources.limits.memory` в К8s-манифесте.

---

## 🏗️ Архитектура краулеров `igaming-source-*` — ООП и паттерны

> ⚠️ **ЧИТАТЬ ОБЯЗАТЕЛЬНО** перед написанием нового краулера или лоадера. Всё уже есть в `igaming-source-core` — не изобретай велосипед.

### Двухрежимная модель: crawler vs loader

Каждый `igaming-source-*` модуль работает в **одном из двух режимов** через `SPRING_PROFILES_ACTIVE`:

| Профиль | Env var | Назначение |
|---|---|---|
| `league-crawler` | `app.role=league-crawler` | Обходит сайт, собирает коэффициенты, пушит в агрегатор через Kafka |
| `match-loader`   | `app.role=match-loader`   | Читает `match_cache` из Postgres, обогащает данные по матчам (детали) |

В K8s для каждого букмекера обычно два Deployment: `-crawler` и `-loader`.

---

### 🧱 Базовые абстрактные классы (НЕ переписывай, используй)

#### `AbstractBaseBookmakerService`
**Пакет**: `pro.datawiki.igaming.source.core.engine`

Базовый класс для **лоадеров**. Наследуй его в `{Bookmaker}LoaderService` или `{Bookmaker}DummyLoaderService`.

```
AbstractBaseBookmakerService
  ├── loadMatchCards(int batchSize)     ← уже реализован: SELECT FOR UPDATE SKIP LOCKED из match_cache,
  │                                        параллельная обработка через CachedThreadPool,
  │                                        авто-пропуск устаревших матчей (>5 часов после старта)
  └── abstract loadSingleMatchCard(MatchCache cache)  ← реализуй в наследнике
```

**Что уже делает из коробки**:
- `SELECT FOR UPDATE SKIP LOCKED` — распределённая блокировка между репликами лоадера
- Параллельная обработка батча (ExecutorService)
- Авто-пропуск стейл-матчей (prematch закончился > 5 ч назад)
- Метрики производительности (`recordSkipped()`, `reportPerformance()`)
- `@Value("${app.bookmaker.name}")` — имя букмекера из properties
- `@Value("${app.bookmaker.regions:RU}")` — регионы букмекера

**Пример минимального лоадера** (если сайт не требует загрузки деталей по матчу):
```java
@Service
public class MyBookmakerDummyLoaderService extends AbstractBaseBookmakerService {
    @Override
    public String getBookmakerFamily() { return "mybookmaker"; }

    @Override
    protected boolean loadSingleMatchCard(MatchCache cache) {
        // Нет детального API → просто помечаем PROCESSED
        matchCacheRepository.updateStatus(cache.getId(), MatchCache.Status.PROCESSED, LocalDateTime.now());
        return true;
    }
}
```

---

#### `GenericMatchLoadScheduler`
**Пакет**: `pro.datawiki.igaming.source.core.scheduler`

**Не нужно писать scheduler для лоадера** — он уже есть в core и активируется автоматически при `app.role=match-loader`:

```java
@ConditionalOnProperty(name = "app.role", havingValue = "match-loader")
public class GenericMatchLoadScheduler {
    // Каждые ${app.match-loader.poll-delay-ms:1000} ms
    // вызывает bookmakerService.loadMatchCards(30)
}
```

Spring автоматически инжектит твой `AbstractBaseBookmakerService` наследник.

---

#### `AbstractBetTypeMapper`
**Пакет**: `pro.datawiki.igaming.source.core.mapper`

Базовый класс для маппинга рыночных исходов в типизированный `BetType`. Уже содержит:

| Метод | Что маппит |
|---|---|
| `map1X2Record(o, scope, statType)` | П1 / X / П2, HOME/DRAW/AWAY, на разных языках |
| `map1X2DCRecord(o, scope, statType)` | Двойной шанс: 1X, 12, X2 |
| `mapHandicapRecord(o, scope, statType, isAsian, param)` | Фора (азиатская/европейская) |
| `mapTotalRecord(o, scope, subject, statType, isAsian, param)` | ТБ/ТМ, Over/Under, Больше/Меньше |
| `mapCorrectScoreRecord(label, scope)` | Точный счёт (парсит "2:1", "1-0") |
| `mapOddEvenRecord(o, scope, statType)` | Чёт/Нечет |
| `withParam(betType, param)` | Заменяет param у TotalBet/HandicapBet |

**Используй в своих `{Bookmaker}OddsMapper`**:
```java
public class MyOddsMapper extends AbstractBetTypeMapper {
    public BetType map(String marketName, String outcome, Double param) {
        BetType bt = map1X2Record(outcome, BetScope.FULL_MATCH, StatType.MATCH);
        if (bt != null) return bt;
        return mapTotalRecord(outcome, BetScope.FULL_MATCH, BetSubject.MATCH, StatType.MATCH, true, param);
    }
}
```

> ⚠️ **Исключение**: Использование `AbstractBetTypeMapper` необязательно для семейств букмекеров (таких как Kambi или BetB2B), которые маппят комплексные/групповые ответы API на более высоком уровне (bulk mapping) без поштучного разбора отдельных исходов.

---

#### `AbstractApiErrorTracker`
**Пакет**: `pro.datawiki.igaming.source.core.service`

Трекер ошибок API — просто наследуй и добавь `@Scheduled`:
```java
@Component
public class MyBookmakerApiErrorTracker extends AbstractApiErrorTracker {
    public MyBookmakerApiErrorTracker(MatchCacheRepository repo) { setMatchCacheRepository(repo); }

    @Override protected String getSourceName() { return "MyBookmaker"; }

    @Scheduled(fixedRateString = "60000")
    @Override public void reportErrors() { super.reportErrors(); }
}
```
Методы: `recordAttempt()`, `recordError(reason)` — вызывай в ApiClient.

---

### 📦 Ключевые сервисы core (не переписывать)

| Сервис | Что делает |
|---|---|
| `MatchPersistenceService` | `saveOrUpdateMatchMetadata(MatchCache, json)` — upsert в Postgres с SHA-256 хешем, Caffeine L1 кешем, авто-расчётом `potentialEndTime` через `SportRegistry` |
| `AggregatorClient` | `pushOddsUpdate(request)` → Kafka `odds.updates`. Встроенная SHA-256 дедупликация (Caffeine 100K записей, TTL 12ч). `reportUnchangedOdds()` → батчевый флуш раз в 5с |
| `SportNormalizationService` | `normalize(sportName)` → `SportType` enum. `normalizeAndNotify()` — логирует неизвестный спорт |
| `BetTypeResolverService` | Fallback-резолвер через ML/правила если `AbstractBetTypeMapper` не справился |
| `UnmappedBetService` | `saveAndNotify(bookmaker, sport, runnerName, marketName, eventId)` — сохраняет неизвестные маркеты в БД |
| `VpnManagerService` | Direct-first → Proxy pool → `System.exit(1)`. Использует `BlockDetector` интерфейс |
| `RedisFactorService` | Хранит `MatchFactor` в Redis вместо Postgres (быстрее, меньше нагрузка на БД) |

---

### 🗄️ Таблицы Postgres (основные)

| Таблица | Entity | Назначение |
|---|---|---|
| `match_cache` | `MatchCache` | Все найденные матчи. Статусы: `NEW → PENDING → PROCESSED / FAILED` |
| `league_cache` | `LeagueCache` | Найденные лиги/турниры. `externalId` = `"{bookmaker}-{id}"`. Статусы: `NEW → PROCESSED / FAILED` |
| `sport_cache` | `SportCache` | Виды спорта (id + name) |
| `unmapped_bets` | `UnmappedBet` | Рыночные исходы без маппинга (для мониторинга) |

#### `LeagueCache` — правило именования `externalId`
```
fanduel-14098       ← FanDuel NFL (eventGroupId=14098)
dafabet-soccer      ← Dafabet, спорт-ключ "soccer"
pinnacle-29         ← Pinnacle sportId=29
```
Краулер пишет лиги в `league_cache`, лоадер читает через `SELECT FOR UPDATE SKIP LOCKED`.

---

### 🔄 Паттерн: Discovery → Scraping (без хардкода лиг)

**Правильная архитектура для новых краулеров**:

```
@PostConstruct / @Scheduled(6h)
  DiscoveryService.discoverLeagues()
    → navigate to bookmaker main page
    → intercept sports/navigation API response
    → upsert into league_cache (bookmaker prefix в externalId)
    → fallback: hardcoded defaults

@Scheduled(каждые N минут)
  ScraperScheduler
    → leagueCacheRepository.findAndLockPendingLeagues(pageable)
    → filter by externalId prefix (напр. "fanduel-")
    → apiClient.processLeague(id, sport, league, url)
    → leagueCacheRepository.updateStatus(PROCESSED / FAILED)
```

**НЕ делай** статический массив `String[][] LEAGUES = {...}` в scheduler — это хардкод.

---

### 🌐 Перехват трафика (BrowserService)

```java
// Перехват первого ответа URL с условием
String json = browserService.navigateAndInterceptResponse(
    pageUrl,               // куда навигировать
    url -> url.contains("event-groups") && url.contains(groupId),  // фильтр
    20000                  // таймаут ms
);

// Перехват WebSocket фреймов (socket.io)
page.onWebSocket(ws -> {
    ws.onFrameReceived(frame -> {
        if (frame.text().startsWith("42")) { /* socket.io data */ }
    });
});

// Получить существующий контекст (не создаёт новый браузер)
BrowserContext ctx = browserService.getContext("default");
```

---

### 🏷️ `BetScope`, `BetSubject`, `StatType` — что передавать

| Параметр | Типичные значения |
|---|---|
| `BetScope` | `FULL_MATCH`, `FIRST_HALF`, `SECOND_HALF`, `FIRST_SET`, `FIRST_QUARTER` |
| `BetSubject` | `MATCH`, `TEAM1`, `TEAM2`, `PLAYER` |
| `StatType` | `MATCH` (голы/очки), `CORNERS`, `CARDS`, `SHOTS` |

---

### 📋 Чеклист при создании нового краулера `igaming-source-{bookmaker}`

- [ ] `{Bookmaker}Application` → `@SpringBootApplication(scanBasePackages = {..., "pro.datawiki.igaming.source.core"})`
- [ ] `{Bookmaker}DummyLoaderService extends AbstractBaseBookmakerService` — даже если детального API нет
- [ ] `{Bookmaker}OddsMapper extends AbstractBetTypeMapper` — маппинг рынков
- [ ] `{Bookmaker}ApiErrorTracker extends AbstractApiErrorTracker`
- [ ] `{Bookmaker}LeagueDiscoveryService` — перехват навигации, запись в `league_cache`
- [ ] `{Bookmaker}ScraperScheduler` — читает `league_cache`, не хардкодит лиги
- [ ] `application.properties` с `app.bookmaker.name`, `app.bookmaker.regions`, `app.role`
- [ ] `application-league-crawler.properties`: `app.role=league-crawler`
- [ ] `application-match-loader.properties`: `app.role=match-loader`
- [ ] `Dockerfile` (копировать из соседнего модуля)
- [ ] K8s manifest в `igaming-k8s/{bookmaker}.yaml`: `-crawler` Deployment + `-loader` Deployment + `-db` StatefulSet + Services
- [ ] `nodeSelector` по правилам из секции «Kubernetes — правила расстановки нод»

---

## 📢 Социальные Сети и Контент-План

**Правила автоматизации SMM (igaming-bot)**:
Бот публикует контент 5 раз в день по расписанию (модуль `SocialMediaContentScheduler`):
- **09:00**: Пост на тему из блога (берется из базы `content_plan_10_days.json`).
- **12:00**: Публикация актуальной вилки из агрегатора (прибыль до 6%).
- **15:00**: Пост о новых фичах платформы, технологиях и преимуществах для пользователей. Мы описываем сложные технические детали (K8s, Playwright, WebSocket) простым языком, фокусируясь на пользовательском опыте.
- **18:00**: Публикация актуальной вилки (прибыль до 6%).
- **21:00**: Публикация жирной вилки (прибыль до 10%).

*Все тексты лежат в `igaming-bot/src/main/resources/content_plan_10_days.json`.*
*Постинг происходит через Playwright в Instagram и X (Twitter). Обход 2FA-проверок реализован через IMAP-доступ к `www.smartbet.guru@gmail.com`.*

## ⚠️ Строгие Правила Генерации Контента
1. **Тарифные лимиты**:
   - **Гость (анонимный)** и **Free (авторизован)** видят вилки **до 5.0%**. Не пишите "до 1.0%" в рекламе!
   - **Premium**: Без лимитов (до 20%+). Реклама должна гласить "Хотите видеть вилки БЕЗ ЛИМИТОВ (до 20%+)".
2. **Обязательный дисклеймер**: Во все посты для соцсетей, статьи блога и любые рекламные тексты **всегда** добавляйте дисклеймер: "Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно."
