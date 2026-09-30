# Proposal: [auto-reg] Модуль персон, регистрация в БК и партнерских программах (US IP + 2FA Gateway)

## Context
- **Plane Task ID:** `94e1e364-59c5-4095-9b05-d59dc48778fd`
- **Issue Reference:** Plane Issue #55
- **Golden Rules:**
  - Rule #2 (Запрет IP-адресов — Только K8s DNS Service Names)
  - Rule #6 (Сетевая топология и умная маршрутизация: US Proxy `http://100.83.113.50:3128` -> `100.66.190.4`)
  - Rule #9 (Firefox / Camoufox, Запрет Инкогнито, Persistent Profile в Redis `smm:profile:<id>`, прогрев кэша)
  - Rule #10 (Мониторинг фрибетов, акций БК и автоматизация партнерских программ — Affiliate & Promo Radar)

## Description
Автоматизировать процесс генерации цифровых персон, регистрации учетных записей на целевых площадках (букмекерские конторы из 52 БК, партнерские сети CPA/RevShare, спортивные форумы и Reddit) с использованием постоянного контекста Firefox (Playwright/Camoufox), сетевой маршрутизации через US IP proxy и двухфакторного подтверждения через `auth-inbox-gateway`.

### Архитектурные требования:
1. **Расширенный генератор цифровых персон (`PersonaFactory`):**
   - **Персоны вебмастеров (Affiliate):** ФИО, профиль трафика, сайт (SmartBet.guru), ежемесячный охват, способы выплат (USDT TRC20/ERC20, Capitalist), корпоративные данные.
   - **Персоны игроков (Bettor / Bookmaker):** реалистичные профили игроков под юрисдикции (US / RU / EU), паспортные данные/SSN (маскированные), дата рождения (21-35 лет), адреса, почтовые индексы, номера телефонов.
   - **Персоны для соцсетей и форумов (Social / Forum):** никнеймы, спортивные интересы (АПЛ, НБА, киберспорт, вилки), предпочтения, аватары.
   - **Цифровой отпечаток браузера (Browser Fingerprint):** User-Agent Firefox, экранное разрешение, WebGL Vendor/Renderer, аппаратные характеристики (hardwareConcurrency, deviceMemory), таймзона и локаль.

2. **Интеграция с 2FA Gateway (`auth-inbox-gateway:8000` & OTP/TOTP Engine):**
   - Автоматический опрос `http://auth-inbox-gateway:8000/api/v1/inbox/code` для получения входящих одноразовых кодов из IMAP ящиков с настраиваемыми regex-шаблонами.
   - Поддержка алгоритма TOTP (RFC 6238) для генерации 6-значных токенов двухфакторной аутентификации.
   - Поддержка fallback mock gateway для автономного тестирования в изолированных средах.

3. **Движок авторегистрации (`AutoRegistrationEngine`):**
   - Клиенты регистрации для букмекерских платформ (Winline, Fonbet, Pari, Betcity, Pinnacle, 1xBet, Stake, DraftKings).
   - Клиенты регистрации в партнерских сетях (Uffiliates, 1xPartners, Fonbet Affiliates, Betcity Affiliates, Pinnacle Affiliates).
   - Клиенты регистрации на тематических форумах и Reddit.
   - Поддержка человекоподобного ввода (Cubic Bezier траектории мыши, эмуляция набора текста с задержками и опечатками).
   - Маршрутизация через US Proxy (`http://100.83.113.50:3128`) для защиты от локальных блокировок и антифрода.

4. **Персистентность сессий и профилей в Redis:**
   - Сохранение созданного профиля Firefox в Redis по ключу `smm:profile:<persona_id>` (tar.gz с cookies.sqlite, storage/default, IndexedDB, LocalStorage).
   - Сохранение учетных данных созданных аккаунтов (логин, пароль, 2FA секрет, партнерский ID, промокод, реферальная ссылка) в структурированном виде в Redis `smm:account:<service>:<persona_id>`.
   - Синхронизация полученных партнерских офферов с микросервисом `igaming-affiliate-service`.

5. **Kubernetes манифесты и интеграция:**
   - K8s Job / Deployment `igaming-k8s/smm-auto-reg.yaml` для запуска задач авторегистрации в namespace `igaming-dev`.
   - Интеграция с сервисами `igaming-redis`, `auth-inbox-gateway`, `igaming-affiliate-service`.
