# Конституция AI-Ассистентов — Проект SmartBet.guru (iGaming)

Этот документ определяет правила разработки для экосистемы **SmartBet.guru / iGaming** (бэкенд, фронтенд, краулеры, SMM, квантовый анализ).

---

## 🏛️ Базовая Конституция (Наследование)

> 🛡️ **Этот проект наследует все универсальные правила экосистемы из базовой конституции**:
> 📄 [**`docs/infrastructure/AGENTS_CORE.md`**](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/docs/infrastructure/AGENTS_CORE.md)
>
> **Обязательно к соблюдению**:
> 1. **Doc-as-Code Mandate**: запрет закрытия задач без обновления спецификаций OpenSpec и документации.
> 2. **Git Pre-Flight Check & Multi-Device Safety**: защита работы на 2 ноутбуках и пуле ИИ-воркеров (`feature/plane-*`), запрет `git reset --hard` и слепых `git add .`.
> 3. **Definition of Done**: под в K8s `Running 1/1`, 5 минут без ошибок, сторожевой таймер `schedule`.
> 4. **Единая Инфраструктура**: строжайший запрет старых серверов, все узлы берутся из [**`INFRASTRUCTURE.md`**](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/docs/infrastructure/INFRASTRUCTURE.md).
> 5. **K8s DNS**: только имена сервисов, запрет хардкода IP.
> 6. **Repair Protocol & Zero-Delay Hotfix**: сначала починка базового модуля на `fix/*`, затем разработка фичи. При починке ошибок — немедленный деплой без ожидания подтверждения, устранение сбоя для пользователей в первую очередь и обязательная сквозная проверка открытия.
> 7. **Защита Кодировки UTF-8 в PowerShell**: запрет пайпов `| kubectl apply -f -`, деплой через SFTP или чистые UTF-8 файлы без BOM.

---

## 🎯 Специфичные Правила Проекта iGaming (Domain Rules)

### 1. 🎯 Запрет на Отбрасывание Аномалий и Сокрытие Ошибок (Zero Outlier Discard & No Crutches Policy)
- **ФУНДАМЕНТАЛЬНЫЙ ПРИНЦИП ПРОЕКТА**:
  - **Проблемы мы НЕ отбрасываем и НЕ прячем костылями, а ПОКАЗЫВАЕМ тестировщикам и исправляем в первоисточнике!**
  - Если скрыть проблему костылями на фронтенде или в API-шлюзе, мы **замаскируем баг** и лишим команду возможности обнаружить и устранить корень проблемы. Скрыв аномалию, мы никогда не найдем баг.
  - Тестировщики и операторы видят проблемы, аномальные вилки (20%+, 50%+, нереалистичные коэффициенты), дубли или некорректно смаппленные матчи, рапортуют о них, мы находим первопричину в краулере/маппере конкретного букмекера или нормализаторе и исправляем у источника.
- **СТРОЖАЙШИЙ ЗАПРЕТ НА КОСТЫЛИ И СКРЫТИЕ**:
  - **Категорически запрещено** внедрять на фронтенд, в `portal-gateway`, `aggregator-surebet` или `aggregator-core` «костыли» сокрытия данных (например, скрывать мусорные матчи через клиентские регулярные выражения типа `isCyberOrAmateurMatch`, срезать аномалии на UI или внедрять сайлент-фильтрацию "drop outlier" / "drop consensus outlier").
  - Единственно верное решение — устранить дефект в нормализаторе/краулере у источника (`igaming-source-*`, `TeamSignatureNormalizer`), а в базе данных полностью вычистить невалидные сущности. Данные на UI должны отображаться честно без искусственного маскирования дефектов бэкенда.

### 2. 📊 Критерий Наполнения Линии (Threshold >= 500 матчей)
- Задача по любому букмекеру считается выполненной **ТОЛЬКО** при наполнении линии от **500 активных матчей** (`SELECT count(*) FROM match_cache >= 500`). Статус `Running 1/1` при 0 матчей — незавершённый дефект.

### 3. ⚡ Неблокирующий старт HikariCP / JPA и Сборка Jib
- **Правило конфигурации пула БД**:
  - `spring.datasource.hikari.initialization-fail-timeout` **нельзя дублировать со значением 0** при наличии Kubernetes env var override (во избежание `IllegalStateException: The configuration of the pool is sealed once started`).
  - Рекомендуемый шаблон в `application.properties`:
    ```properties
    # spring.datasource.hikari.initialization-fail-timeout=-1 (или опустить, -1 = вечное ожидание БД)
    spring.datasource.hikari.connection-timeout=5000
    spring.datasource.hikari.validation-timeout=3000
    spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
    ```
- В PowerShell все параметры с точками берутся в двойные кавычки: `mvn.cmd -pl <module> jib:build "-Djib.to.image=..." -DskipTests`.

### 4. 🧠 AI-Аналитик и Квантовый Анализ Матчей (`igaming-ai-analyst`)
- **Конституция аналитика**: [`.agents/AGENTS_ANALYST.md`](file:///c:/Users/chernousov_a/IdeaProjects/igaming/.agents/AGENTS_ANALYST.md).
- **Строго 1 институциональная рекомендация на матч**: нулевая внутренняя каннибализация (`Zero Portfolio Cannibalization`).
- **Микро-профилирование игроков (Bottom-Up Player Archetypes)**:
  - Оценка тактического рисунка из индивидуальных качеств 11 игроков старта и опций ротации, а не из абстрактного исторического бренда клуба.
- **⛔ Правило Джокера и Вето на ТМ (Bench Chaos Veto)**:
  - **СТРОЖАЙШЕЕ ТАБУ**: если в заявке/на скамейке команды есть взрывной нападающий / дриблер (`⚡ [ДЖОКЕР АТАКИ]` или рейтинг $\ge 7.0$), **КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО** давать рекомендацию на жесткий `TOTAL_UNDER_2.0` или `TOTAL_UNDER_2.5`! Выход свежего джокера против уставших ЦЗ ломает низ.
  - **Обязательное решение**: перенаправлять выбор на азиатскую фору (`AH 1 (0)` / `Ф(+1.0)`), двойной шанс (`1X` / `X2`) или смягченный подушечный тотал `TOTAL_UNDER_3.5`.
- **Сценарное дерево и теневые ставки (Scenario Tree & Risk Hierarchy)**:
  - Каждая рекомендация содержит `core_hypothesis`, `identified_risk`, `recommended_bet` и 2 теневых сценария (`shadow_picks`): ветка `DRAW_DEFENSE` (страховка 1:1) и ветка `CUSHION_TOTAL` (страховка при раскрытии игры до 2:1).
- **Персистентная память обучения (`ai_learning_memory`)**:
  - `MatchSettlementService` автоматически рассчитывает результаты завершенных матчей и записывает тактические уроки для калибровки будущих предсказаний.

### 5. 🏷️ Нормализация Сигнатур Команд и Транслитерация (Signature Normalization)
- **Изоляция полов и возрастов**:
  - Женские команды (`(W)`, `women`, `женщины`, `femmes`, `damas`, `frauen`) строго нормализуются в каноническую форму `«Команда (W)»` и никогда не матчатся с мужскими.
  - Молодёжные команды (`U19`, `U21`, `U23`, `молодёжные`, `юноши`) сохраняют возрастную категорию и изолируются от основных составов.
- **Мультиязычная транслитерация и контекст (`SimilarityUtils`, `TeamSignatureNormalizer`)**:
  - Сопоставление с SofaScore и внешними источниками выполняет фонетическую транслитерацию, удаление диакритики и проверку категорий турнира.

### 6. 📺 Broadcast Hub и Видеотрансляции
- Каталог видеопотоков (`ScoreBat`, открытые стримы) агрегирует метаданные в `broadcasts_json` (`has_broadcast = true`).
- **Zero Server Bandwidth**: доставка видео исключительно через клиентские iframe/embed на стороне браузера без проксирования видеопотока серверами платформы.

### 7. 📱 SMM-Автоматизация, Персоны и SIM-Шлюз
- **Языковая изоляция**: французская персона (`fr.smartbet.guru`) находится под жесткой защитой от кириллицы (Anti-Cyrillic Guard).
- **Валютная и региональная изоляция каналов**:
  - В российский канал (`ru`, `@smartbetguru`) отправлять **ТОЛЬКО** сигналы букмекеров с лицензией РФ в **RUB** (`₽`) или проверенных международных букмекеров.
  - **СТРОЖАЙШИЙ ЗАПРЕТ**: публикация в РФ-канал белорусских (`fon-bet-by`, `marathonbet-by`, `betera` в BYN/Br) или казахстанских (`fon-bet-kz`, `olimpbet-kz` в KZT/₸) букмекеров, а также смешивание разных валют или приравнивание BYN к ₽.
  - Названия букмекеров всегда нормализуются в опрятный вид без служебных дефисов слагов (`fon-bet-ru` $\rightarrow$ «Фонбет», `fon-bet-by` $\rightarrow$ «Фонбет (BY)», `fon-bet-kz` $\rightarrow$ «Фонбет (KZ)»).
- **Контент и математика фрибетов**:
  - Фрибет **всегда** ставится на плечо с **высоким** коэффициентом ($k \ge 3.0$), а перекрытие деньгами (hedge) — на плечо с низким коэффициентом ($k \approx 1.15-1.40$).
  - Обязательный расчет **80% гарантированного кэша фрибета** и строгий дисклеймер против лудомании.
- **Прозрачность и обработка возражений по лимитам**:
  - Объяснение аудитории, почему мы делимся сигналами публично: институциональное масштабирование ограничено букмекерскими лимитами ставок и антифродом.
- **Meta Anti-Ban (Threads / Instagram)**:
  - Строгий запрет триггер-слов ("ставки", "букмекер", "вилка", "коэффициент", "пассивный доход", "odds", "betting", "surebet") в публичных постах; перевод за аналитикой через ссылку в bio.
- **Циркадный ритм**: утренний прогрев $\rightarrow$ дневной пост $\rightarrow$ вечерний нетворкинг по кривой Безье $\rightarrow$ ночной сон (22:00–07:00).
- **SIM-шлюз Xiaomi 12T Pro** (`100.114.62.28`, `+79137671550`): прием 2FA кодов Meta/WhatsApp через вебхук `auth-inbox-gateway`.
- **Session Keeper**: поддержание сессий раз в 12–24 часа для защиты от 14–30 дневного экспайра cookies.

### 8. 🚨 Немедленный Деплой Hotfix и Принцип Работоспособности для Пользователя (Zero-Delay Hotfix & End-to-End Verification)
- **Мгновенный деплой без ожидания подтверждения**:
  - Если пользователь попросил починить ошибку (баг, неработающая страница, упавший сервис, 404/500, сломанная кнопка или маршрут) — **КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО ждать подтверждения на деплой** (*«Хотите задеплоить?»*, *«Деплоим?»*, *«Жду аппрува»*).
  - Ассистент обязан **деплоить исправление сразу же и автономно**.
- **Принцип «Пользователи не должны видеть ошибку» (User-First Mitigation)**:
  - **Первоочередная цель — пользователи не должны видеть ошибку.** Сначала восстанавливается доступность и работоспособность ресурса в боевом/целевом окружении.
  - Если в коде есть детали, требующие уточнения, архитектурные вопросы, пограничные случаи или предложения по дальнейшей доработке:
    - Сначала выкатывается работающий фикс, устраняющий сбой для пользователей.
    - Затем в ответе ассистент явно описывает детали: *«Ошибка устранена и проверена на окружении. При этом есть моменты, требующие вашего уточнения и последующей доработки: ...»*.
    - **Категорически запрещено откладывать деплой или блокировать устранение сбоя** ради выяснения второстепенных деталей.
- **Обязательная сквозная проверка доступности (End-to-End Verification)**:
  - Деплой считается завершённым **ТОЛЬКО** после того, как агент лично проверил, что страница или эндпоинт реально открывается и функционирует (HTTP 200, рендеринг без падений через curl / fetch / Playwright / консоль).
  - *Пример*: если пользователь сказал *«не открывается страница А»*, результат работы — страница А гарантированно открывается и доступна пользователям, а не просто "код закоммичен".

### 9. 🏛️ Доменный Фундамент: Обязательная опора на `pro.datawiki.igaming.dto.sport.Sport`
- **ФУНДАМЕНТАЛЬНЫЙ ПРИНЦИП ВСЕЙ ЭКОСИСТЕМЫ**:
  - Интерфейс `pro.datawiki.igaming.dto.sport.Sport` и его полиморфная иерархия (`FootballSport`, `TennisSport`, `EsportsSport`, `Cs2Sport`, `BasketballSport`, `IceHockeySport`, `TableTennisSport`, `CombatSport` и др.) являются **ЕДИНОЙ СВЯЩЕННОЙ ОСНОВОЙ** для всех операций с видами спорта.
  - **СТРОЖАЙШИЙ ЗАПРЕТ**:
    - **Категорически запрещено** писать внешние процедурные `switch (sport)` / `if ("FOOTBALL".equals(sport))` или дублировать логику спортивных правил в сервисах, контроллерах, краулерах или квантовом аналитике.
  - **ОБЯЗАТЕЛЬНО К СОБЛЮДЕНИЮ**:
    - Вся спортивная специфика обязана инкапсулироваться непосредственно в интерфейсе `Sport` и переопределяться в специализированных классах:
      1. **Векторные пространства и Qdrant**: имя спортивной коллекции (`getVectorCollectionName()`), размерность вектора (`getVectorDimension()`), имена признаков (`getVectorFeatureNames()`), стилистические эталоны (`getStyleAnchors()`), тип спорта (`isTeamSport()`).
      2. **Рыночный охват и расчет ставок**: допустимость рынков (`isMarketSupported()`), полиморфный расчет исходов (`evaluateSettlement()`).
      3. **Лингвистика и тактика**: единицы счета (`getScoreUnit()`), фразы стадионов/площадок (`getVenuePhrase()`), тактические протоколы (`getTacticalRulesPrompt()`), глубокий анализ драфтов/вето (`buildTacticalDeepDive()`).
    - Любой новый вид спорта или фича начинается с расширения контракта `Sport` и регистрации в `Sports.java`.

### 10. 🌐 Прокси-Инфраструктура и Обход ТСПУ/РКН (Egress Proxy Mesh & DPI Chaining)
- **ФУНДАМЕНТАЛЬНАЯ АРХИТЕКТУРА ТРАФИКА**:
  $$\text{Pod (Crawler / Service)} \longrightarrow \text{K8s Service (:3128)} \longrightarrow \text{Tinyproxy} \longrightarrow \text{OpenVPN (tun0)} \xrightarrow[\text{100.111.55.54:3128}]{\text{HTTP CONNECT via de-proxy-1}} \text{PureVPN Egress Node}$$
- **Причина цепочки через `de-proxy-1` (Frankfurt, AEZA, `100.111.55.54:3128`)**:
  - Серверы PureVPN напрямую из РФ блокируются ТСПУ/РКН (TCP RST на порт 80, TLS handshake timeout на 443).
  - OpenVPN на ноде `xeon-srv` обязан туннелироваться через промежуточный европейский HTTP CONNECT прокси в `client.ovpn`:
    `http-proxy 100.111.55.54 3128`
- **Защита маршрутизации в `up.sh` при `redirect-gateway def1`**:
  - При активации `tun0` маршрутизатор подменяет default gateway. Скрипт `up.sh` обязан явно сохранять маршруты через `eth0`:
    - Pod network: `10.244.0.0/16`
    - ClusterIP: `10.96.0.0/12`
    - Tailscale mesh: `100.64.0.0/10`
    - Локальные сети: `172.16.0.0/12`, `192.168.0.0/16`
  - Дополнительно настраиваются: `iptables -t nat -A POSTROUTING -o tun0 -j MASQUERADE` и `TCPMSS --clamp-mss-to-pmtu`.
- **Стек контейнеров пода (3/3)**:
  1. `openvpn` (`dperson/openvpn-client:latest`) — подъем защищенного туннеля через `de-proxy-1`.
  2. `tinyproxy` (`vimagick/tinyproxy:latest`) — локальный HTTP-прокси на порту 3128.
  3. `curl-tester` (`curlimages/curl:latest`) — отладка и health check внутри пода (`sleep infinity`).
- **Глобальная сетка из 8 серверов (PureVPN Mesh по всему миру)**:
  1. 🇳🇱 Нидерланды: `http://purevpn-nl.proxy.svc.cluster.local:3128` (`149.40.59.74`, Amsterdam)
  2. 🇺🇸 США: `http://purevpn-us-ca.proxy.svc.cluster.local:3128` (`135.136.12.38`, Los Angeles)
  3. 🇧🇷 Бразилия: `http://purevpn-br.proxy.svc.cluster.local:3128` (`185.222.213.75`, São Paulo)
  4. 🇸🇬 Сингапур: `http://purevpn-sg.proxy.svc.cluster.local:3128` (`104.36.48.132`, Singapore)
  5. 🇬🇧 Великобритания: `http://purevpn-uk.proxy.svc.cluster.local:3128` (`91.90.121.151`, Manchester)
  6. 🇯🇵 Япония: `http://purevpn-jp.proxy.svc.cluster.local:3128` (`46.231.167.68`, Tokyo)
  7. 🇦🇺 Австралия: `http://purevpn-au.proxy.svc.cluster.local:3128` (`217.79.116.211`, Sydney)
  8. 🇿🇦 ЮАР: `http://purevpn-za.proxy.svc.cluster.local:3128` (`213.139.10.101`, Johannesburg)
- **Привязка к ноде**: все прокси строго привязаны к `xeon-srv` через `nodeAffinity`.

---

## 🧭 Живые Спецификации Возможностей iGaming:
- [`crawler-engine`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/crawler-engine/spec.md) — краулеры 52 букмекеров.
- [`aggregator-core`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/aggregator-core/spec.md) — расчет вилок / +EV / коридоров, нормализация сигнатур.
- [`ai-analyst`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/ai-analyst/spec.md) — квантовый AI-аналитик, AGY-воркеры, вето скамейки, сценарные деревья.
- [`broadcast-hub`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/broadcast-hub/spec.md) — каталог стримов, ScoreBat, клиентский embed.
- [`hypothesis-engine`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/hypothesis-engine/spec.md) — виртуальный бэктестинг и лаборатория гипотез.
- [`freebet-activator`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/freebet-activator/spec.md) — монетизация бонусов и фрибетов (80% кэша).
- [`portal-gateway`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/portal-gateway/spec.md) — API Gateway, JWT, тарифы.
- [`frontend-architecture`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/frontend-architecture/spec.md) — Next.js 14/15, Mobile App, калькулятор экспрессов и систем.
- [`social-media-bot`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/social-media-bot/spec.md) — SMM-агенты, Redroid, 1:1 прокси, Anti-Ban.
