#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
create_smm_growth_plane_tasks.py

Creates the Epic Module and full Task Tree for AI Growth & SMM Swarm in Plane:
- Infra & Redis (No deps -> In Progress)
- Auth & 2FA Inbox Gateway (No deps -> In Progress)
- Programmatic SEO Landing Pages (No deps -> In Progress)
- Interactive Surebet Calculator Lead-Magnet (No deps -> In Progress)
- Stealth Agent Runner (Blocked by Infra & Auth -> Todo)
- Auto-Registration & Persona Generator (Blocked by Runner & Auth -> Todo)
- Threads & Instagram SMM Agent (Blocked by Runner & Auth -> Todo)
- Telegram Channel Revitalization (Blocked by Infra -> Todo)
- Reddit & Crowd Marketing Agent (Blocked by Runner & AutoReg -> Todo)

Links blockers via IssueBlocker.
"""

import base64
import json
import os
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

MODULE_DEF = {
    "name": "AI Growth & SMM Swarm (SmartBet.guru)",
    "description": "Автономная система привлечения целевого трафика: ферма ИИ-СММ агентов в K8s (Threads, Instagram, Telegram, Reddit, форумы), 2FA-шлюз авторизации (TOTP/IMAP), инфраструктура Redis, программное SEO и крауд-маркетинг."
}

TASKS_DEF = [
    # ── Stream 1: Infra & Auth (Independent -> In Progress) ────────────────────────
    {
        "key": "infra_redis",
        "title": "[infra-redis] Развертывание Redis и схемы персистентности сессий в K8s namespace igaming-dev",
        "priority": "urgent",
        "state": "In Progress",
        "blocked_by": [],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Развернуть централизованный инстанс <b>Redis</b> в Kubernetes namespace <code>igaming-dev</code>
для обеспечения очередей заданий ИИ-СММ агентов, кэширования сессий браузеров, межпроцессного взаимодействия и rate-лимитеров.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>StatefulSet Redis:</b> легковесный образ <code>redis:7-alpine</code> с монтированием PVC для сохранения данных при рестартах подов.</li>
  <li><b>K8s Service:</b> Headless/ClusterIP сервис с каноническим DNS именем: <code>igaming-redis.igaming-dev.svc.cluster.local:6379</code> (строгий запрет IP-адресов).</li>
  <li><b>Схема ключей:</b>
    <ul>
      <li><code>account:{id}:meta</code> — логин, зашифрованный пароль, TOTP-секрет, статус (ACTIVE, BANNED, WARMUP).</li>
      <li><code>account:{id}:cookies</code> — JSON с дампом cookies и LocalStorage для быстрого восстановления сессии без повторной авторизации.</li>
      <li><code>queue:smm:{platform}</code> — очередь целевых постов и комментариев для исполнения агентами.</li>
    </ul>
  </li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Создать K8s манифест <code>igaming-k8s/apps/infra/redis.yaml</code> (StatefulSet, PVC 5Gi, Service 6379).</li>
  <li>Применить манифест в namespace <code>igaming-dev</code>: <code>kubectl apply -f redis.yaml</code>.</li>
  <li>Проверить сетевую доступность и выполнение команды <code>PING</code> внутри кластера.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Под <code>igaming-redis-0</code> находится в статусе <code>Running 1/1</code>.</li>
  <li>Отклик на команду <code>redis-cli ping</code> возвращает <code>PONG</code>.</li>
  <li>DNS имя <code>igaming-redis.igaming-dev.svc.cluster.local</code> успешно резолвится из других подов.</li>
</ul>
"""
    },
    {
        "key": "auth_2fa_gateway",
        "title": "[auth-2fa] Разработка микросервиса Auth & 2FA Inbox Gateway (TOTP генератор + IMAP сборщик кодов)",
        "priority": "urgent",
        "state": "In Progress",
        "blocked_by": [],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Создать универсальный микросервис <b>Auth & 2FA Inbox Gateway</b> для полной автономности агентов при авторизации.
Сервис устраняет потребность в ручном вводе кодов двухфакторной аутентификации из Authenticator-приложений и входящих Email-писем.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Генерация TOTP (Authenticator App):</b> поддержка алгоритма RFC 6238 через <code>pyotp</code>. По запросу <code>GET /api/v1/2fa/totp?secret=...</code> возвращает актуальный 6-значный код.</li>
  <li><b>Email IMAP сборщик кодов:</b> подключение по IMAP (SSL 993) к почтовым ящикам аккаунтов (Catch-all домен или Gmail/Outlook App Passwords).</li>
  <li><b>Regex парсинг кодов:</b> автоматическое извлечение верификационных кодов из писем от Instagram, Threads, Reddit, Twitter, Google.</li>
  <li><b>REST API:</b>
    <ul>
      <li><code>GET /api/v1/2fa/totp?account_id={id}</code> — моментальный возврат TOTP.</li>
      <li><code>GET /api/v1/2fa/email-code?email={email}&service={service}&timeout_sec=60</code> — лонг-поллинг ожидания письма и выдача кода.</li>
    </ul>
  </li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Реализовать сервис на Python (FastAPI) в <code>igaming-auth-gateway/</code>.</li>
  <li>Добавить модуль <code>totp_service.py</code> для генерации TOTP.</li>
  <li>Добавить асинхронный IMAP-клиент <code>imap_inbox_service.py</code> с regex-парсерами паттернов кодов.</li>
  <li>Собрать OCI-образ на сервере Xeon: <code>100.78.183.101:30500/auth-inbox-gateway:latest</code>.</li>
  <li>Развернуть Deployment и Service в K8s namespace <code>igaming-dev</code>.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Эндпоинт <code>/actuator/health</code> или <code>/healthz</code> возвращает HTTP 200 OK.</li>
  <li>Тестовый запрос с TOTP-seed генерирует валидный временный код, совпадающий со стандартным Google Authenticator.</li>
  <li>Под стабильно работает в K8s без рестартов.</li>
</ul>
"""
    },

    # ── Stream 2: Programmatic SEO & Lead Magnet (Independent -> In Progress) ──────
    {
        "key": "seo_landing_pages",
        "title": "[seo-pages] Программатик-SEO лендинги для 52 БК, сравнений и лиг (Next.js 14 App Router)",
        "priority": "high",
        "state": "In Progress",
        "blocked_by": [],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Обеспечить лавинообразный органический поисковый трафик на <b>SmartBet.guru</b> за счёт генерации
сотен качественных посадочных страниц (Programmatic SEO) на базе подключенных 52 букмекеров и спортивных событий.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Динамические маршруты сравнения БК:</b> <code>/compare/[bookmaker1]-vs-[bookmaker2]</code> (например, <i>Winline против Фонбет</i>, <i>Pinnacle vs Bet365</i>) со сравнением маржи, коэффициентов и лимитов.</li>
  <li><b>Обзоры букмекеров:</b> <code>/bookmakers/[slug]</code> — детальные страницы с реальной статистикой средней маржи по футболу, теннису, баскетболу и киберспорту.</li>
  <li><b>JSON-LD микроразметка Schema.org:</b>
    <ul>
      <li><code>SportsEvent</code> — для страниц актуальных матчей с коэффициентами.</li>
      <li><code>FAQPage</code> — часто задаваемые вопросы для расширенных сниппетов Google/Яндекс.</li>
      <li><code>SoftwareApplication</code> / <code>AggregateRating</code> — для доверия поисковиков.</li>
    </ul>
  </li>
  <li><b>SSR / Incremental Static Regeneration (ISR):</b> моментальная отдача готового HTML поисковым краулерам (Googlebot, YandexBot) с высокими оценками Core Web Vitals.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>В репозитории веб-интерфейса создать страницы в <code>src/app/[locale]/compare/[pair]/page.tsx</code>.</li>
  <li>Создать страницы обзоров в <code>src/app/[locale]/bookmakers/[slug]/page.tsx</code>.</li>
  <li>Реализовать генерацию <code>generateMetadata()</code> с уникальными title, description и OpenGraph.</li>
  <li>Добавить генератор <code>sitemap.ts</code> для автоматического включения всех сгенерированных URL.</li>
  <li>Проверить сборку через <code>npm run build</code> на сервере Xeon.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Страницы рендерятся на сервере без клиентских ошибок гидратации.</li>
  <li>Валидатор Google Rich Results подтверждает корректность разметки Schema.org.</li>
  <li>Sitemap содержит актуальные ссылки на сравнения и страницы БК.</li>
</ul>
"""
    },
    {
        "key": "seo_surebet_calculator",
        "title": "[seo-calc] Общедоступный вилочный калькулятор и конвертер коэффициентов как SEO лид-магнит",
        "priority": "high",
        "state": "In Progress",
        "blocked_by": [],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Создать высококонверсионный бесплатный инструмент (Лид-магнит) — <b>интерактивный калькулятор вилок и валуев</b>
на странице <code>/tools/calculator</code>, привлекающий горячий поисковый трафик игроков.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Математическое ядро:</b> расчет распределения ставок для 2-исходных и 3-исходных вилок с учетом комиссии и валют.</li>
  <li><b>Deep-linking через URL:</b> синхронизация введенных данных с адресной строкой (<code>?odds1=1.95&odds2=2.10&stake=10000</code>) для возможности расшаривания готовых расчетов агентами в соцсетях.</li>
  <li><b>Конверсионный триггер:</b> блок <i>«Найти живые вилки с доходностью от 5% прямо сейчас»</i> с кнопкой перехода в основной сканер SmartBet.guru.</li>
  <li><b>Микроразметка:</b> Schema.org <code>WebApplication</code>.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Создать страницу <code>src/app/[locale]/tools/calculator/page.tsx</code>.</li>
  <li>Реализовать компонент расчета с ползунками и адаптивной версткой под мобильные телефоны.</li>
  <li>Добавить поддержку RU и EN переводов через <code>next-intl</code>.</li>
  <li>Встроить OpenGraph генератор превью-карточек для соцсетей.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Калькулятор мгновенно пересчитывает доходность и суммы ставок на клиенте.</li>
  <li>Поддерживается копирование ссылки с предустановленными коэффициентами.</li>
  <li>Сборка <code>npm run build</code> проходит без ошибок.</li>
</ul>
"""
    },

    # ── Stream 3: Stealth Browser Engine (Blocked by Infra & Auth) ────────────────
    {
        "key": "smm_stealth_runner",
        "title": "[smm-runner] Базовый OCI-образ браузерного ИИ-агента (Playwright Stealth + Xvfb + US Proxy routing)",
        "priority": "urgent",
        "state": "Todo",
        "blocked_by": ["infra_redis", "auth_2fa_gateway"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Собрать специализированный OCI-образ и раннер для автономных ИИ-СММ агентов с полной антидетект-обвязкой,
эмуляцией реального пользователя и маршрутизацией трафика через IP США.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Стек:</b> Python 3.11+, Playwright, <code>playwright-stealth</code>, Xvfb виртуальный дисплей.</li>
  <li><b>Маршрутизация:</b> трафик по умолчанию направляется через прокси США (<code>outline-us</code> / <code>ru-proxy</code> 100.66.190.4).</li>
  <li><b>Антидетект:</b> маскировка navigator.webdriver, WebGL отпечатков, Canvas, плагинов и аудио-контекста.</li>
  <li><b>Persistent Sessions:</b> монтирование K8s PersistentVolume (PVC) под <code>userDataDir</code> для сохранения авторизованных сессий и cookies.</li>
  <li><b>Защита от зомби-процессов:</b> использование <code>tini</code> как PID 1 (Golden Rule 11).</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Создать <code>Dockerfile</code> базового агента с установкой браузеров Playwright и Xvfb.</li>
  <li>Реализовать Python модуль <code>stealth_browser.py</code> с подключением прокси и загрузкой сохраненных cookies.</li>
  <li>Собрать образ на сервере Xeon: <code>100.78.183.101:30500/smm-agent-base:latest</code>.</li>
  <li>Провести верификацию на тестовой странице детекта ботов (Browserleaks).</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Образ собран и доступен в локальном реестре.</li>
  <li>Браузер успешно запускается в K8s поде, внешний IP определяется как США.</li>
  <li>Тесты антидетекта показывают 100% результат обычного браузера без флагов автоматизации.</li>
</ul>
"""
    },
    {
        "key": "auto_account_generator",
        "title": "[auto-reg] Модуль генерации персон и авторегистрации аккаунтов (US IP + 2FA Gateway)",
        "priority": "high",
        "state": "Todo",
        "blocked_by": ["smm_stealth_runner", "auth_2fa_gateway"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Автоматизировать процесс создания профилей и регистрации аккаунтов для агентов на целевых площадках (Reddit, форумы, почты)
с привязкой к IP США и автоматическим получением кодов подтверждения.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Генератор персон:</b> генерация реалистичных профилей (имя, никнейм, дата рождения, интересы, стиль письма).</li>
  <li><b>Интеграция с Auth Gateway:</b> автоматический запрос и обработка кодов подтверждения из почты и сохранение TOTP-секрета.</li>
  <li><b>Сохранение сессий:</b> сохранение готового авторизованного профиля в Redis и PVC для последующего использования раннерами.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Реализовать сценарии регистрации для целевых сервисов в <code>scripts/auto_register/</code>.</li>
  <li>Внедрить интеграцию с <code>auth-inbox-gateway</code> для перехвата подтверждающих ссылок и OTP кодов.</li>
  <li>Протестировать сквозной флоу регистрации тестового аккаунта.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Скрипт автономно регистрирует аккаунт, подтверждает 2FA и сохраняет валидную сессию в Redis.</li>
</ul>
"""
    },

    # ── Stream 4: Social Channels (Threads, IG, Telegram) ─────────────────────────
    {
        "key": "smm_meta_threads_ig",
        "title": "[smm-meta] ИИ-агент активности и публикации для Threads и Instagram (существующие аккаунты)",
        "priority": "urgent",
        "state": "Todo",
        "blocked_by": ["smm_stealth_runner", "auth_2fa_gateway"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Развернуть автономного ИИ-агента для регулярной публикации контента и взаимодействия в <b>Threads</b> и <b>Instagram</b>
с использованием уже имеющихся аккаунтов.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Авторизация:</b> вход в существующие аккаунты через браузер с использованием сохраненных сессий и 2FA Gateway.</li>
  <li><b>Контентная фабрика:</b> интеграция с API <code>igaming-portal</code> — получение свежих вилок (>5%), разбор валуйных коэффициентов дня, математических парадоксов букмекеров.</li>
  <li><b>Форматы контента:</b>
    <ul>
      <li><b>Threads:</b> текстовые экспертные треды с разбором стратегий арбитража, сравнением маржи и ссылками на калькулятор.</li>
      <li><b>Instagram:</b> генерация карточек с коэффициентами, сторис с прогнозами математического перевеса (+EV).</li>
    </ul>
  </li>
  <li><b>Обязательный дисклеймер:</b> включение предупреждения об ответственной игре (согласно OpenSpec <code>social-media-bot</code>).</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Подготовить сценарий входа и постинга для Threads и Instagram через Playwright Stealth.</li>
  <li>Подключить LLM-промпт спортивного аналитика для генерации текстов на английском и русском языках.</li>
  <li>Развернуть агентский под в K8s с расписанием (CronJob / Worker).</li>
  <li>Провести тестовую публикацию.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Агент успешно авторизуется в аккаунтах без триггера блокировок.</li>
  <li>Тестовый тред в Threads и пост в Instagram опубликованы с корректным форматированием и обязательным дисклеймером.</li>
</ul>
"""
    },
    {
        "key": "smm_telegram_channel",
        "title": "[smm-tg] Ревитализация и автоматизация Telegram-канала SmartBet.guru",
        "priority": "high",
        "state": "Todo",
        "blocked_by": ["infra_redis"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Оживить официальный Telegram-канал проекта <b>SmartBet.guru</b> за счёт регулярной автоматической публикации
высокодоходных арбитражных ситуаций, аналитики и интерактивных калькуляторов.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Сервис:</b> расширение существующего сервиса <code>igaming-bot</code>.</li>
  <li><b>Режимы публикаций:</b>
    <ul>
      <li><b>Утро:</b> дайджест главных спортивных событий дня и разбор маржи букмекеров.</li>
      <li><b>День:</b> сигналы о живых вилках (Surebets Free до 5% и анонсы Premium 10%+) с прямыми ссылками на событие.</li>
      <li><b>Вечер:</b> итоги дня, расчет реального профита по вилкам.</li>
    </ul>
  </li>
  <li><b>Интерактивные кнопки:</b> inline-кнопки <i>«Открыть в калькуляторе»</i> и <i>«Перейти к вилке на SmartBet.guru»</i>.</li>
  <li><b>Дисклеймер:</b> обязательное указание отказа от ответственности и предупреждения о рисках.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Настроить права бота в целевом Telegram-канале.</li>
  <li>Реализовать шаблоны форматирования постов с кнопками в <code>igaming-bot</code>.</li>
  <li>Настроить расписание отправки через Spring Scheduler.</li>
  <li>Запустить и протестировать публикацию.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>В канал успешно отправлен тестовый пост с кнопками перехода на сайт.</li>
  <li>Посты публикуются строго по расписанию без сбоев.</li>
</ul>
"""
    },

    # ── Stream 5: Crowd Marketing & Reddit (Blocked by Runner & AutoReg) ───────────
    {
        "key": "crowd_reddit_forums",
        "title": "[crowd-reddit] ИИ-агент крауд-маркетинга для Reddit (r/sportsbook, r/Arbing) с режимом прогрева",
        "priority": "medium",
        "state": "Todo",
        "blocked_by": ["smm_stealth_runner", "auto_account_generator"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Создать и запустить автономного агента для экспертного крауд-маркетинга на <b>Reddit</b> (сабреддиты <code>r/sportsbook</code>, <code>r/Arbing</code>, <code>r/arbitragebetting</code>)
и профильных англоязычных форумах.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Протокол прогрева (Warm-up Phase):</b> первые 14 дней агент только читает треды, ставит апвоуты и оставляет нейтральные экспертные комментарии без ссылок для накопления кармы.</li>
  <li><b>Мониторинг ключевых тем:</b> отслеживание постов с вопросами о поиске вилок, дропах коэффициентов, выборе сканера.</li>
  <li><b>Нативный постинг:</b> развернутые экспертные ответы с математическими выкладками и нативным упоминанием SmartBet.guru как бесплатного инструмента со сканером до 5%.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Реализовать логику мониторинга сабреддитов через браузерного агента Playwright Stealth.</li>
  <li>Настроить оценку релевантности тредов через LLM.</li>
  <li>Запустить режим прогрева с лимитом 2-3 активности в сутки.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Агент успешно находит релевантные треды и оставляет контекстные комментарии.</li>
  <li>Аккаунт не попадает под shadowban Reddit.</li>
</ul>
"""
    }
]

def run_remote_django(script_code: str, timeout: int = 120) -> tuple[str, str]:
    cmd = [
        "ssh", "-i", SSH_KEY,
        "-o", "BatchMode=yes",
        "-o", "ConnectTimeout=10",
        SSH_HOST,
        "kubectl exec -i -n plane deploy/plane-api -- python3 -"
    ]
    res = subprocess.run(cmd, input=script_code, capture_output=True, text=True, timeout=timeout, encoding="utf-8")
    return res.stdout, res.stderr

def sync_smm_tasks() -> dict:
    payload = {
        "module": MODULE_DEF,
        "tasks": TASKS_DEF
    }
    b64_payload = base64.b64encode(json.dumps(payload).encode("utf-8")).decode("ascii")

    django_code = f"""
import os, sys, json, base64, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue, User, Module, ModuleIssue, IssueBlocker

p = Project.objects.filter(identifier='IGAMING').first() or Project.objects.first()
user = User.objects.filter(email='aleksei.a.chernousov@gmail.com').first() or User.objects.first()

states = {{s.name: s for s in State.objects.filter(project=p)}}
todo_state = states.get('Todo') or State.objects.filter(project=p, group='unstarted').first()
in_progress_state = states.get('In Progress') or states.get('AI разработка') or State.objects.filter(project=p, group='started').first()

raw_data = base64.b64decode('{b64_payload}').decode('utf-8')
data = json.loads(raw_data)

# 1. Create or get Epic Module
m_def = data['module']
mod, created = Module.objects.get_or_create(
    project=p,
    name=m_def['name'],
    defaults={{
        'workspace': p.workspace,
        'description': m_def['description'],
        'created_by': user,
        'updated_by': user,
        'status': 'in-progress'
    }}
)

task_key_to_issue = {{}}
results = []

# 2. Create or update tasks
for t_def in data['tasks']:
    key = t_def['key']
    title = t_def['title']
    prio = t_def['priority']
    req_state = t_def['state']
    html_desc = t_def['html_desc']

    target_state = in_progress_state if req_state == 'In Progress' else todo_state

    issue = Issue.objects.filter(project=p, name=title).first()
    is_new = False

    if not issue:
        issue = Issue.objects.create(
            project=p,
            workspace=p.workspace,
            name=title,
            description_html=html_desc,
            state=target_state,
            created_by=user,
            updated_by=user,
            priority=prio
        )
        is_new = True
    else:
        # If task already in progress or completed, don't revert to unstarted
        if issue.state.group not in ['completed', 'cancelled']:
            issue.state = target_state
        issue.name = title
        issue.description_html = html_desc
        issue.priority = prio
        issue.save()

    task_key_to_issue[key] = issue

    # Link to Module
    ModuleIssue.objects.get_or_create(
        module=mod,
        issue=issue,
        project=p,
        workspace=p.workspace,
        defaults={{'created_by': user, 'updated_by': user}}
    )

    results.append({{
        "key": key,
        "sequence_id": issue.sequence_id,
        "title": issue.name,
        "is_new": is_new,
        "state": issue.state.name if issue.state else None,
        "priority": issue.priority,
        "blocked_by": t_def['blocked_by']
    }})

# 3. Create IssueBlocker dependencies
dependencies_count = 0
for t_def in data['tasks']:
    dependent_key = t_def['key']
    dependent_issue = task_key_to_issue.get(dependent_key)
    for blocking_key in t_def['blocked_by']:
        blocking_issue = task_key_to_issue.get(blocking_key)
        if dependent_issue and blocking_issue:
            blocker, b_created = IssueBlocker.objects.get_or_create(
                project=p,
                workspace=p.workspace,
                block=dependent_issue,
                blocked_by=blocking_issue,
                defaults={{
                    'created_by': user,
                    'updated_by': user
                }}
            )
            if b_created:
                dependencies_count += 1

output = {{
    "status": "success",
    "module": {{
        "id": str(mod.id),
        "name": mod.name
    }},
    "tasks": results,
    "new_dependencies_count": dependencies_count
}}

print("__PLANE_SMM__:" + json.dumps(output))
"""
    stdout, stderr = run_remote_django(django_code, timeout=120)
    for line in stdout.splitlines():
        if line.startswith("__PLANE_SMM__:"):
            return json.loads(line.replace("__PLANE_SMM__:", ""))
    print("Raw stdout:\n", stdout)
    if stderr:
        print("Raw stderr:\n", stderr)
    return {"status": "error", "output": stdout, "stderr": stderr}

def main():
    print("🚀 Synchronizing AI Growth & SMM Swarm tasks into Plane...")
    res = sync_smm_tasks()
    if res.get("status") == "success":
        print("\n✅ Successfully updated Plane tasks!")
        print(f"📦 Module: {res['module']['name']}")
        print(f"🔗 Dependencies created: {res.get('new_dependencies_count', 0)}")
        print("\n=== TASKS SUMMARY ===")
        for t in res.get("tasks", []):
            status = "🆕 Created" if t.get("is_new") else "🔄 Updated"
            deps = f" [Blocked by: {', '.join(t['blocked_by'])}]" if t['blocked_by'] else " [NO DEPS -> IN WORK]"
            print(f"{status} #{t['sequence_id']:<4} | {t['state']:<12} | {t['title']}{deps}")
    else:
        print("❌ Error during task creation:", res)

if __name__ == "__main__":
    main()
