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

### CI/CD и деплой
- **GitHub Actions НЕ используется** для деплоя (несмотря на наличие `.github/workflows`).
- **Деплой выполняется локально** через PowerShell-скрипты:
  - `.\restart-ci.ps1` — сборка всех модулей
  - `.\restart-ci.ps1 -Only {имя_модуля}` — сборка конкретного модуля (например `tennisi`, `fon-bet-ru`)
- **КРИТИЧЕСКИ ВАЖНО**: Сборки выполняются **строго по очереди** на удалённой машине `100.89.122.84`. Параллельный запуск `restart-ci.ps1` **ЗАПРЕЩЁН** — скрипты перезаписывают файлы друг друга (`git reset --hard`).

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

Вся автоматизация социальных сетей (Instagram, Threads) реализована в виде скриптов на **Node.js** с использованием библиотеки **Playwright** для управления браузером.

- **Папка со скриптами (скил автоматизации)**: `C:\Users\chernousov_a\.gemini\config\skills\instagram_automation\scripts\`
  * `instagram_control.js` — управление сессиями, авторизация и постинг в Instagram.
  * `threads_login_all.js` / `threads_clean_login_and_post.js` — прямая авторизация аккаунтов в Threads (с обходом 2FA).
  * `post_welcome.js` / `threads_post_welcome_via_switch.js` — публикация приветственного контента.
- **Chrome Profile**: `C:\Users\chernousov_a\Documents\igaming\sessions\chrome` (на Windows Server)
- **Credentials**: `C:\Users\chernousov_a\Documents\igaming\passwords.txt`

Запуск Chrome на сервере (Windows):
```powershell
& "C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222 --headless=new --user-data-dir="C:\Users\chernousov_a\Documents\igaming\sessions\chrome" --disable-gpu
```

### 🧠 Важные правила и уроки автоматизации (Meta / Instagram / Threads)

#### 1. Управление CDP-соединением и фоновым процессом
* **Не блокировать порты:** Headless Chrome постоянно запущен на сервере на порту 9222. Попытка параллельно запустить Playwright `launchPersistentContext` с той же папкой профиля вызовет ошибку блокировки (`Lock file can not be created!`). Подключайтесь по CDP: `chromium.connectOverCDP('http://127.0.0.1:9222')`.
* **Безопасное отключение:** При завершении скрипта CDP **категорически запрещено** вызывать `browser.close()`, так как это полностью завершит процесс системного Chrome на сервере. Вместо этого используйте `browser.disconnect()`.

#### 2. Защита Meta от автоматизации и OIDC-редиректы
* **Блокировка недоверенных событий:** Системы безопасности Meta (OIDC OAuth на Threads) блокируют программные редиректы авторизации, если клик по кнопке быстрого входа «Продолжить с Instagram» был вызван через JS-событие (`dispatchEvent('click')`). Браузер помечает событие как `isTrusted: false`, и сервер возвращает ошибку: *«Не все странники теряются, но этой страницы здесь уже нет»*.
* **Только физические клики:** Для входа через связку Instagram -> Threads используйте исключительно аппаратную симуляцию клика Playwright `await button.click({ force: true })`.
* **Время ожидания:** Threads обрабатывает OIDC-редиректы медленно. После клика давайте браузеру не менее 15–25 секунд на обработку OAuth и обновление URL перед выполнением скриншотов или проверок.
* **Предпочтительный прямой вход:** Для надежности входа в Threads без ошибок редиректа рекомендуется использовать прямую форму авторизации (клик на ссылку *«Войти по имени пользователя»* -> ввод логина/пароля -> автоматическое чтение 2FA-кода).

#### 3. Управление сессиями и куками при мультиаккаунтинге
* **Очистка перед каждым входом:** Из-за особенностей Threads, сессионные куки и локальное хранилище разных аккаунтов на домене `threads.com` могут конфликтовать.
* **Изолированный старт:** Перед авторизацией каждого аккаунта в Threads скрипт должен очистить куки домена `threads.com` (`context.clearCookies({ domain: 'threads.com' })`), а также очистить `localStorage` и `sessionStorage` для Threads. При этом **куки `instagram.com` очищать нельзя**, чтобы не сбросить активные сессии в Instagram.

#### 4. Обработка 2FA-кодов
* **Типы кодов:** Meta может запрашивать как 6-значные СМС-коды, так и 8-значные резервные коды (backup codes). Валидация ввода в скриптах должна поддерживать оба формата: `/^\d{6}$|^\d{8}$/`.
* **Автоматическое чтение:** 2FA-коды поступают на Gmail `www.smartbet.guru@gmail.com` и автоматически вычитываются скриптом `fetch_2fa.py` через IMAP.

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
