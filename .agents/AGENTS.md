# Конституция AI-Ассистентов — Проект SmartBet.guru (iGaming)

Этот документ определяет правила разработки для экосистемы **SmartBet.guru / iGaming** (бэкенд, фронтенд, краулеры, SMM).

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
> 6. **Repair Protocol**: сначала починка базового модуля на `fix/*`, затем разработка фичи.

---

## 🎯 Специфичные Правила Проекта iGaming (Domain Rules)

### 1. 🎯 Запрет на Отбрасывание Аномалий (Zero Outlier Discard Policy)
- **ФУНДАМЕНТАЛЬНЫЙ ПРИНЦИП ПРОЕКТА**:
  - **Проблемы мы НЕ отбрасываем, а ПОКАЗЫВАЕМ тестировщикам!**
  - Тестировщики и операторы видят проблемы и аномальные вилки (20%+, 50%+, нереалистичные коэффициенты), рапортуют о них, мы находим первопричину в краулере/маппере конкретного букмекера и исправляем парсинг.
- **СТРОЖАЙШИЙ ЗАПРЕТ**:
  - **Категорически запрещено** внедрять в `aggregator-surebet`, `aggregator-core`, `portal-gateway` алгоритмы слепого отбрасывания котировок или вилок ("drop outlier", "drop consensus outlier", сайлент-фильтрация высокого профита).
  - Единственно верное решение — найти дефект парсера и исправить краулер/маппер непосредственно у источника (`igaming-source-*`).

### 2. 📊 Критерий Наполнения Линии (Threshold >= 500 матчей)
- Задача по любому букмекеру считается выполненной **ТОЛЬКО** при наполнении линии от **500 активных матчей** (`SELECT count(*) FROM match_cache >= 500`). Статус `Running 1/1` при 0 матчей — незавершённый дефект.

### 3. ⚡ Неблокирующий старт HikariCP / JPA и Сборка Jib
- В `application.properties` обязательны:
  ```properties
  spring.datasource.hikari.initialization-fail-timeout=0
  spring.datasource.hikari.connection-timeout=5000
  spring.datasource.hikari.validation-timeout=3000
  spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
  ```
- В PowerShell все параметры с точками берутся в двойные кавычки: `mvn.cmd -pl <module> jib:build "-Djib.to.image=..." -DskipTests`.

### 4. 📱 SMM-Автоматизация, Персоны и SIM-Шлюз
- **Языковая изоляция**: французская персона (`fr.smartbet.guru`) находится под жесткой защитой от кириллицы (Anti-Cyrillic Guard).
- **Валютная и региональная изоляция каналов**:
  - В российский канал (`ru`, `@smartbetguru`) отправлять **ТОЛЬКО** сигналы букмекеров с лицензией РФ в **RUB** (`₽`) или проверенных международных букмекеров.
  - **СТРОЖАЙШИЙ ЗАПРЕТ**: публикация в РФ-канал белорусских (`fon-bet-by`, `marathonbet-by`, `betera` в BYN/Br) или казахстанских (`fon-bet-kz`, `olimpbet-kz` в KZT/₸) букмекеров, а также смешивание разных валют или приравнивание BYN к ₽.
  - Названия букмекеров всегда нормализуются в опрятный вид без служебных дефисов слагов (`fon-bet-ru` $\rightarrow$ «Фонбет», `fon-bet-by` $\rightarrow$ «Фонбет (BY)», `fon-bet-kz` $\rightarrow$ «Фонбет (KZ)»).
- **Контент и математика фрибетов**:
  - Фрибет **всегда** ставится на плечо с **высоким** коэффициентом ($k \ge 3.0$), а перекрытие деньгами (hedge) — на плечо с низким коэффициентом ($k \approx 1.15-1.40$).
  - Обязательный расчет **80% гарантированного кэша фрибета** и строгий дисклеймер против лудомании.
- **Циркадный ритм**: утренний прогрев $\rightarrow$ дневной пост $\rightarrow$ вечерний нетворкинг по кривой Безье $\rightarrow$ ночной сон (22:00–07:00).
- **SIM-шлюз Xiaomi 12T Pro** (`100.114.62.28`, `+79137671550`): прием 2FA кодов Meta/WhatsApp через вебхук `auth-inbox-gateway`.
- **Session Keeper**: поддержание сессий раз в 12–24 часа для защиты от 14–30 дневного экспайра cookies.

---

## 🧭 Живые Спецификации Возможностей iGaming:
- [`crawler-engine`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/crawler-engine/spec.md) — краулеры 52 букмекеров.
- [`aggregator-core`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/aggregator-core/spec.md) — расчет вилок / +EV / коридоров.
- [`portal-gateway`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/portal-gateway/spec.md) — API Gateway, JWT, тарифы.
- [`frontend-architecture`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/frontend-architecture/spec.md) — Next.js 14/15, Mobile App, Chrome Extension.
- [`social-media-bot`](file:///c:/Users/chernousov_a/IdeaProjects/igaming-openspec/openspec/specs/social-media-bot/spec.md) — SMM-агенты, Redroid, 1:1 прокси.
