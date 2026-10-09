import subprocess
import json
import base64
import sys

sys.stdout.reconfigure(encoding='utf-8')

PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"
WORKSPACE_SLUG = "dataplatform"

TASKS = [
    # --- BetB2B Clones ---
    {
        "bk": "1xbet",
        "title": "[1xbet] Обновить образ на latest, настроить роутинг через прокси и восстановить стриминг котировок",
        "desc": "Букмекер на платформе BetB2B (партнер 1). Поток котировок прервался. Задачи: 1) Обновить crawler и loader deployments на образ 100.78.183.101:30500/igaming-source-betb2b:latest; 2) Настроить прокси APP_PROXY_HOST=100.83.113.50, APP_PROXY_PORT=3128, APP_VPN_FORCE=true; 3) Проверить эндпоинт Get1x2_Zip?partner=1; 4) Сбросить статус match_cache в NEW. DoD: под в статусе Running 2/2, HTTP 200 на /actuator/health, в базе >= 500 матчей, непрерывный стрим в odds_actual."
    },
    {
        "bk": "fansport",
        "title": "[fansport] Обновить образ на latest, настроить роутинг через прокси и восстановить стриминг котировок",
        "desc": "Букмекер на платформе BetB2B (партнер 101). Задачи: 1) Переключить образ на 100.78.183.101:30500/igaming-source-betb2b:latest; 2) Задать переменные кластерного прокси 100.83.113.50:3128; 3) Сбросить match_cache в статус NEW; 4) Проверить Kafka Producer с enable.idempotence=false. DoD: >= 500 матчей в match_cache, активный стрим в odds_actual в igaming-dev."
    },
    {
        "bk": "linebet",
        "title": "[linebet] Обновить образ на latest, подключить прокси и запустить стрим котировок",
        "desc": "Букмекер на платформе BetB2B (партнер 237). Задачи: 1) Обновить deployments на образ igaming-source-betb2b:latest; 2) Настроить прокси 100.83.113.50:3128; 3) Убедиться в корректности парсинга коэффициентов и сбросить кэш в NEW. DoD: Running 2/2, >= 500 матчей в базе, наличие свежих котировок в odds_actual."
    },
    {
        "bk": "betwinner",
        "title": "[betwinner] Развернуть crawler и loader на базе betb2b:latest и запустить сбор линии",
        "desc": "Букмекер на платформе BetB2B (партнер 114). Задачи: 1) Проверить наличие deployment/db в namespace igaming-source; 2) Обновить образ на igaming-source-betb2b:latest с прокси 100.83.113.50:3128; 3) Запустить сбор prematch и live линий. DoD: >= 500 матчей в match_cache, Actuator UP, свежие котировки в odds_actual."
    },
    {
        "bk": "betlabel",
        "title": "[betlabel] Настроить crawler/loader BetB2B и запустить сбор линии",
        "desc": "Букмекер на платформе BetB2B (партнер 332). Задачи: 1) Развернуть/обновить deployments на образ 100.78.183.101:30500/igaming-source-betb2b:latest; 2) Подключить кластерный прокси 100.83.113.50:3128; 3) Проверить сбор линии. DoD: >= 500 матчей, Actuator UP, котировки в odds_actual."
    },
    {
        "bk": "helabet",
        "title": "[helabet] Обновить образ на latest, проверить partner ID и запустить стрим",
        "desc": "Букмекер на платформе BetB2B (партнер 312). Задачи: 1) Обновить deployment на igaming-source-betb2b:latest; 2) Задать APP_PROXY_HOST=100.83.113.50, APP_PROXY_PORT=3128; 3) Сбросить кэш в NEW. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "jvspin",
        "title": "[jvspin] Обновить образ на latest и восстановить сбор котировок",
        "desc": "Букмекер на платформе BetB2B. Задачи: 1) Обновить deployments на igaming-source-betb2b:latest; 2) Подключить кластерный прокси 100.83.113.50:3128; 3) Наполнить match_cache. DoD: >= 500 матчей, Actuator UP, стрим в odds_actual."
    },
    {
        "bk": "paripesa",
        "title": "[paripesa] Обновить образ BetB2B latest и запустить стриминг котировок",
        "desc": "Букмекер на платформе BetB2B. Задачи: 1) Обновить deployments на igaming-source-betb2b:latest; 2) Настроить прокси 100.83.113.50:3128; 3) Запустить краулер и лоадер. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "sapphirebet",
        "title": "[sapphirebet] Обновить образ на betb2b:latest и запустить сбор линии",
        "desc": "Букмекер на платформе BetB2B. Задачи: 1) Обновить deployments на igaming-source-betb2b:latest; 2) Настроить прокси 100.83.113.50:3128; 3) Сбросить match_cache в статус NEW. DoD: >= 500 матчей, Actuator UP, стрим в odds_actual."
    },

    # --- Российские легальные букмекеры (ЦУПИС / ЕРАИ) ---
    {
        "bk": "betboom",
        "title": "[betboom] Восстановить WebSocket соединение и стриминг котировок (target >= 500 matches)",
        "desc": "Российский легальный букмекер BetBoom. Стрим котировок прервался. Задачи: 1) Проверить логи краулера и лоадера; 2) Проверить доступность WebSocket/REST эндпоинтов через direct роутинг (РФ IP СПб); 3) Устранить дефекты парсинга исходов; 4) Обеспечить >= 500 матчей. DoD: Running 2/2, Actuator UP, >= 500 матчей в match_cache, непрерывный поток в odds_actual."
    },
    {
        "bk": "leon",
        "title": "[leon] Обновить API эндпоинты betline и восстановить парсинг котировок",
        "desc": "Российский легальный букмекер Leon. Эндпоинты /api-2/betline отдают 400 INVALID_CODE. Задачи: 1) Исследовать актуальные вызовы современного фронтенда leon.ru; 2) Обновить LeonApiClient на актуальный API эндпоинт; 3) Проверить прямое подключение без прокси (direct СПб IP); 4) Наполнить match_cache >= 500 матчей. DoD: Running 2/2, Actuator UP, >= 500 матчей, стрим в odds_actual."
    },
    {
        "bk": "ligastavok",
        "title": "[ligastavok] Завершить интеграцию Playwright и наполнение линии (target >= 500 matches)",
        "desc": "Российский легальный букмекер Лига Ставок. Задачи: 1) Запустить сбор линии через Playwright и WebSocket перехватчик; 2) Обеспечить обход защиты фронтенда через headless stealth; 3) Наполнить match_cache >= 500 матчей; 4) Наладить отправку в агрегатор. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "tennisi",
        "title": "[tennisi] Проверить парсер котировок, структуру match_factor и восстановить стрим",
        "desc": "Российский легальный букмекер Tennisi. Задачи: 1) Проверить схему таблицы match_factor и соответствие JPA; 2) Проверить сбор prematch/live линий; 3) Наполнить match_cache >= 500 матчей; 4) Возобновить отправку в Kafka. DoD: Running 2/2, Actuator UP, >= 500 матчей, свежие котировки в odds_actual."
    },

    # --- Букмекеры СНГ (Казахстан, Беларусь, Армения) ---
    {
        "bk": "fon-bet-kz",
        "title": "[fon-bet-kz] Исправить имя БД PostgreSQL и восстановить инжест (target >= 500 matches)",
        "desc": "Казахстанский филиал Fonbet. В k8s deployment и application.properties указана БД igaming_fon_bet_kz, а в PostgreSQL создана igaming_fonbet_kz. Задачи: 1) Исправить имя БД в deployment; 2) Перезапустить сбор линии; 3) Наполнить match_cache >= 500 матчей. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "olimpbet-kz",
        "title": "[olimpbet-kz] Восстановить сбор котировок и наполнение линии (target >= 500 matches)",
        "desc": "Казахстанский Olimpbet. Задачи: 1) Проверить краулер и лоадер; 2) Проверить доступность API olimpbet.kz; 3) Наполнить match_cache >= 500 матчей; 4) Наладить отправку котировок. DoD: Running 2/2, Actuator UP, >= 500 матчей, котировки в odds_actual."
    },
    {
        "bk": "marathonbet-by",
        "title": "[marathonbet-by] Подключить Playwright volume mount и восстановить сбор котировок",
        "desc": "Белорусский Marathonbet. В логах ошибка 'Executable doesn't exist at /root/.cache/ms-playwright/chromium'. Задачи: 1) Добавить volume mount /data/playwright-browsers в deployment igaming-source-marathonbet-by; 2) Пересобрать образ на базе 100.78.183.101:30500/playwright-java:21; 3) Наполнить match_cache >= 500 матчей. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "mostbet",
        "title": "[mostbet] Настроить обход защиты через EU-NL прокси и восстановить стрим линии",
        "desc": "Международный букмекер Mostbet. Задачи: 1) Настроить роутинг через outline-vpn-eu-nl (100.83.113.50:3128); 2) Проверить API эндпоинты линии; 3) Наполнить match_cache >= 500 матчей; 4) Наладить передачу в агрегатор. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "vivarobet",
        "title": "[vivarobet] Восстановить Swarm API / WebSocket сбор на платформе BetConstruct",
        "desc": "Армянский букмекер Vivarobet (BetConstruct). Задачи: 1) Проверить сессию Swarm API / WebSocket; 2) Проверить маппинг исходов; 3) Наполнить match_cache >= 500 матчей; 4) Обеспечить стриминг в Kafka. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "totogaming",
        "title": "[totogaming] Восстановить REST API сбор линии и стриминг котировок",
        "desc": "Армянский букмекер TotoGaming. Задачи: 1) Проверить доступность API и формат ответов; 2) Наполнить match_cache >= 500 матчей; 3) Проверить отправку котировок в агрегатор. DoD: Running 2/2, Actuator UP, >= 500 матчей, свежие котировки в odds_actual."
    },

    # --- Крупнейшие международные букмекеры ---
    {
        "bk": "pinnacle",
        "title": "[pinnacle] Маршрутизация через PureVPN DE и обход Cloudflare 403",
        "desc": "Эталонный мировой букмекер Pinnacle. Прямые запросы к pinnacle.com блокируются Cloudflare (403). Задачи: 1) Настроить роутинг через выделенный немецкий прокси k8s-purevpn-de (или outline-vpn-eu-nl) в sing-box / VpnManagerService; 2) Проверить стабильный сбор линии; 3) Наполнить match_cache >= 500 матчей. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "betano",
        "title": "[betano] Отремонтировать отправку котировок в агрегатор (Odds Ingestion & Aggregator Link)",
        "desc": "Международный букмекер Betano (Kaizen Gaming). Кэш событий заполнен (1000+ матчей в match_cache), но котировки не поступают в aggregator (odds_actual = 0). Задачи: 1) Проверить MatchService, AggregatorClient, URL агрегатора и формат батч-запроса котировок; 2) Устранить блокировку отправки; 3) Наполнить match_cache. DoD: появление betano в odds_actual с >= 500 активных матчей и непрерывный стрим."
    },
    {
        "bk": "sbobet",
        "title": "[sbobet] Восстановить парсер секций после снятия таймаута",
        "desc": "Азиатский букмекер Sbobet. В BrowserExecutor снят 25-секундный детективный таймаут. Задачи: 1) Проверить перехват $P.onUpdate('od', ...) и DOM-структуру секций; 2) Настроить азиатский прокси purevpn-sg; 3) Наполнить match_cache >= 500 матчей. DoD: Running 2/2, Actuator UP, >= 500 матчей, непрерывный поток котировок в агрегатор."
    },
    {
        "bk": "bet365",
        "title": "[bet365] Настроить браузерный агент Camoufox (Gecko), сессионные токены и WebSocket перехват",
        "desc": "Мировой лидер беттинга Bet365. Задачи: 1) Настроить браузерную автоматизацию на Camoufox (Gecko) с постоянным профилем; 2) Маршрутизировать трафик строго через статический британский прокси purevpn-uk; 3) Реализовать перехват WebSocket потока коэффициентов; 4) Наполнить match_cache >= 500 матчей. DoD: Running 2/2, Actuator UP, >= 500 матчей, стрим в odds_actual."
    },
    {
        "bk": "marathonbet-com",
        "title": "[marathonbet-com] Маршрутизация через EU-NL прокси и восстановление сбора линии",
        "desc": "Международный Marathonbet. Задачи: 1) Настроить роутинг через outline-vpn-eu-nl (100.83.113.50:3128); 2) Проверить парсер live и prematch линий; 3) Наполнить match_cache >= 500 матчей; 4) Обеспечить передачу в агрегатор. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "dafabet",
        "title": "[dafabet] Настроить роутинг через азиатские прокси и запустить сбор линии",
        "desc": "Азиатский букмекер Dafabet. Задачи: 1) Настроить роутинг через purevpn-sg (Сингапур); 2) Проверить API эндпоинты линии; 3) Наполнить match_cache >= 500 матчей; 4) Наладить стриминг в агрегатор. DoD: Running 2/2, Actuator UP, >= 500 матчей, котировки в odds_actual."
    },
    {
        "bk": "cloudbet",
        "title": "[cloudbet] Восстановить сбор линии через GraphQL API (target >= 500 matches)",
        "desc": "Крипто-букмекер Cloudbet. Задачи: 1) Проверить GraphQL схему и API ключи/токены; 2) Наполнить match_cache >= 500 матчей; 3) Проверить маппинг исходов и передачу в Kafka. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "stake",
        "title": "[stake] Обход Cloudflare stealth и восстановление GraphQL API инжеста",
        "desc": "Крипто-букмекер Stake. Задачи: 1) Обеспечить обход Cloudflare через Camoufox/прокси; 2) Восстановить перехват GraphQL котировок; 3) Наполнить match_cache >= 500 матчей; 4) Стриминг в Kafka. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "betway",
        "title": "[betway] Обновить авторизационные токены SBTech и восстановить сбор котировок",
        "desc": "Европейский букмекер Betway (SBTech). Задачи: 1) Настроить роутинг через purevpn-uk/purevpn-de; 2) Восстановить генерацию сессионных токенов; 3) Наполнить match_cache >= 500 матчей; 4) Обеспечить стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "betsson",
        "title": "[betsson] Восстановить загрузчик линий Betsson Group через EU прокси",
        "desc": "Скандинавская группа Betsson/Nordicbet/Betsafe. Задачи: 1) Настроить проксирование через purevpn-nl/de; 2) Проверить API парсер линии; 3) Наполнить match_cache >= 500 матчей; 4) Возобновить отправку в Kafka. DoD: Running 2/2, Actuator UP, >= 500 матчей, котировки в odds_actual."
    },
    {
        "bk": "bwin",
        "title": "[bwin] Настроить стелс-сессию Entain и восстановить сбор линии",
        "desc": "Европейский букмекер Bwin (Entain). Задачи: 1) Роутинг через outline-vpn-eu-nl; 2) Восстановление стелс-сессии и парсинга цифровой подписи; 3) Наполнение match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "draftkings",
        "title": "[draftkings] Настроить роутинг через US West прокси и парсинг американских рынков",
        "desc": "Американский букмекер DraftKings. Задачи: 1) Роутинг строго через purevpn-us-ca (Лос-Анджелес); 2) Проверить парсинг рынков NFL, NBA, MLB, NHL; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в Kafka. DoD: Running 2/2, Actuator UP, >= 500 матчей, свежие котировки в odds_actual."
    },
    {
        "bk": "betfair",
        "title": "[betfair] Восстановить API-NG Stream подключение и маппинг биржевых коэффициентов",
        "desc": "Британская биржа и спортсбук Betfair. Задачи: 1) Проверить API-NG сертификаты/токены; 2) Настроить роутинг через purevpn-uk; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "10bet",
        "title": "[10bet] Восстановить сбор линии на платформе Altenar/SBTech",
        "desc": "Европейский букмекер 10bet. Задачи: 1) Проверить API эндпоинты платформы Altenar; 2) Настроить прокси purevpn-de; 3) Наполнить match_cache >= 500 матчей; 4) Наладить стрим в агрегатор. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "stoiximan",
        "title": "[stoiximan] Восстановить стриминг котировок Kaizen Gaming через EU прокси",
        "desc": "Букмекер Stoiximan (Kaizen Gaming). Задачи: 1) Роутинг через европейский прокси; 2) Проверить парсер API ответов; 3) Наполнить match_cache >= 500 матчей; 4) Наладить отправку в агрегатор. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "superbet",
        "title": "[superbet] Восстановить сбор линии через REST API (target >= 500 matches)",
        "desc": "Европейский букмекер Superbet. Задачи: 1) Проверить доступность REST API и сессионные заголовки; 2) Наполнить match_cache >= 500 матчей; 3) Обеспечить отправку котировок в агрегатор. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "fortuna",
        "title": "[fortuna] Восстановить JSON API сбор линии и наполнить match_cache",
        "desc": "Центральноевропейский букмекер Fortuna (Чехия/Польша/Словакия). Задачи: 1) Проверить JSON API эндпоинты; 2) Наполнить match_cache >= 500 матчей; 3) Проверить отправку в Kafka. DoD: Running 2/2, Actuator UP, >= 500 матчей, котировки в odds_actual."
    },
    {
        "bk": "interwetten",
        "title": "[interwetten] Восстановить XML/JSON feed парсер и стриминг котировок",
        "desc": "Австрийский букмекер Interwetten. Задачи: 1) Проверить доступность фида и роутинг через purevpn-de; 2) Наполнить match_cache >= 500 матчей; 3) Стриминг в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "mozzartbet",
        "title": "[mozzartbet] Восстановить REST API парсер котировок (target >= 500 matches)",
        "desc": "Сербский букмекер Mozzartbet. Задачи: 1) Проверить REST API эндпоинты; 2) Наполнить match_cache >= 500 матчей; 3) Наладить отправку в агрегатор. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "tipsport",
        "title": "[tipsport] Восстановить REST API сбор линии через EU прокси",
        "desc": "Чешский/словацкий букмекер Tipsport. Задачи: 1) Проверить роутинг через европейский прокси; 2) Наполнить match_cache >= 500 матчей; 3) Стриминг в Kafka. DoD: Running 2/2, Actuator UP, >= 500 матчей, котировки в odds_actual."
    },
    {
        "bk": "tipico",
        "title": "[tipico] Обход Cloudflare stealth через PureVPN DE и восстановление сбора линии",
        "desc": "Немецкий букмекер Tipico. Задачи: 1) Роутинг через purevpn-de; 2) Стелс-обход защиты Cloudflare; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },

    # --- Букмекеры Латинской Америки (Бразилия, Колумбия) ---
    {
        "bk": "wplay",
        "title": "[wplay] Восстановить сбор линии на платформе SBTech через LatAm прокси",
        "desc": "Колумбийский регулируемый букмекер Wplay (SBTech). Задачи: 1) Настроить роутинг через LatAm прокси (purevpn-br); 2) Восстановить авторизационные токены; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "rushbet",
        "title": "[rushbet] Восстановить Kambi API перехватчик через LatAm прокси",
        "desc": "Колумбийский букмекер Rushbet (Kambi). Задачи: 1) Проверить Kambi API эндпоинты; 2) Настроить роутинг через purevpn-br; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "rivalo",
        "title": "[rivalo] Восстановить REST API сбор линии и стриминг котировок",
        "desc": "Латиноамериканский букмекер Rivalo. Задачи: 1) Проверить REST API эндпоинты; 2) Наполнить match_cache >= 500 матчей; 3) Наладить отправку в агрегатор. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "betesporte",
        "title": "[betesporte] Восстановить сбор линии бразильского букмекера через PureVPN BR",
        "desc": "Бразильский букмекер Betesporte. Задачи: 1) Роутинг через purevpn-br (Сан-Паулу); 2) Проверить API эндпоинты; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "betnacional",
        "title": "[betnacional] Восстановить сбор линии через бразильский прокси purevpn-br",
        "desc": "Крупный бразильский букмекер Betnacional. Задачи: 1) Настроить роутинг через purevpn-br; 2) Проверить API парсер линии; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, свежие котировки в odds_actual."
    },
    {
        "bk": "bet7k",
        "title": "[bet7k] Восстановить сбор линии через бразильский прокси purevpn-br",
        "desc": "Бразильский букмекер Bet7k. Задачи: 1) Роутинг через purevpn-br; 2) Проверить парсинг коэффициентов; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "vaidebet",
        "title": "[vaidebet] Восстановить JSON API сбор линии и стриминг котировок",
        "desc": "Бразильский букмекер Vaidebet. Задачи: 1) Роутинг через purevpn-br; 2) Проверить JSON API ответы; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },

    # --- Букмекеры Южной Европы и биржи ---
    {
        "bk": "novibet",
        "title": "[novibet] Восстановить REST API сбор линии через европейский прокси",
        "desc": "Греческий/международный букмекер Novibet. Задачи: 1) Роутинг через purevpn-nl/de; 2) Проверить REST API линии; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, свежие котировки в odds_actual."
    },
    {
        "bk": "parimatch",
        "title": "[parimatch] Восстановить перехватчик WebSocket / REST для международной версии",
        "desc": "Международная версия Parimatch. Задачи: 1) Проверить WebSocket / REST эндпоинты; 2) Настроить роутинг через EU-NL; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "vbet",
        "title": "[vbet] Восстановить WebSocket сбор линии на платформе BetConstruct",
        "desc": "Международный букмекер Vbet (BetConstruct). Задачи: 1) Проверить WebSocket соединение и Swarm протокол; 2) Наполнить match_cache >= 500 матчей; 3) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, свежие котировки в odds_actual."
    },
    {
        "bk": "winamax",
        "title": "[winamax] Восстановить REST API сбор котировок через европейский прокси",
        "desc": "Французский регулируемый букмекер Winamax. Задачи: 1) Роутинг через purevpn-de/nl; 2) Проверить REST API эндпоинты линии; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, поток в odds_actual."
    },
    {
        "bk": "matchbook",
        "title": "[matchbook] Восстановить REST API парсер рынков и коэффициентов биржи ставок",
        "desc": "Британская биржа ставок Matchbook. Задачи: 1) Роутинг через purevpn-uk; 2) Проверить REST API эндпоинты биржевых рынков; 3) Наполнить match_cache >= 500 матчей; 4) Стрим в odds_actual. DoD: Running 2/2, Actuator UP, >= 500 матчей, свежие котировки в odds_actual."
    }
]

tasks_json = json.dumps(TASKS, ensure_ascii=False)

remote_django = f"""
import os, sys, json, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue, User, Module, ModuleIssue

p = Project.objects.filter(identifier='IGAMING').first()
ai_state = State.objects.filter(project=p, name='AI разработка').first()
if not ai_state:
    ai_state = State.objects.filter(project=p, group='started').first()

crawler_module = Module.objects.filter(project=p, name__icontains='Crawler').first()
user = User.objects.first()

created_issues = []
tasks = json.loads('''{tasks_json}''')

for t in tasks:
    title = t['title']
    desc = t['desc']
    bk = t['bk']
    
    # Check if issue already exists by tag
    iss = Issue.objects.filter(project=p, name__icontains=f'[{{bk}}]').first()
    if not iss:
        iss = Issue.objects.create(
            project=p,
            workspace=p.workspace,
            name=title,
            description_html=f'<p>{{desc}}</p>',
            state=ai_state,
            created_by=user,
            updated_by=user,
            priority='urgent'
        )
        print(f'✨ Created Issue #{{iss.sequence_id}}: "{{iss.name}}" (ID: {{iss.id}})')
    else:
        iss.name = title
        iss.description_html = f'<p>{{desc}}</p>'
        iss.state = ai_state
        iss.priority = 'urgent'
        iss.save()
        print(f'🔄 Updated Issue #{{iss.sequence_id}}: "{{iss.name}}" (ID: {{iss.id}})')
        
    if crawler_module:
        ModuleIssue.objects.get_or_create(module=crawler_module, issue=iss, project=p)
        
    created_issues.append({{"seq_id": iss.sequence_id, "id": str(iss.id), "bk": bk, "title": title}})

print("CREATED_JSON:" + json.dumps(created_issues, ensure_ascii=False))
"""

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "kubectl exec -i -n plane deploy/plane-api -- python3 -"
]

res = subprocess.run(cmd, input=remote_django, capture_output=True, text=True, timeout=120, encoding='utf-8')
print("STDOUT:\n", res.stdout)
if res.stderr:
    print("STDERR:\n", res.stderr)

# Parse output
issues = []
for line in res.stdout.splitlines():
    if line.startswith("CREATED_JSON:"):
        issues = json.loads(line[len("CREATED_JSON:"):])

print(f"\nSuccessfully processed {len(issues)} Plane issues for remaining bookmakers!")
with open("scratch/remaining_bookmakers_plane_issues.json", "w", encoding="utf-8") as f:
    json.dump(issues, f, ensure_ascii=False, indent=2)
