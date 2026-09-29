#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
update_smm_with_firefox_and_affiliates.py

Updates Plane issues #54, #55, #56 with Firefox / Persistent Profile / Cache Warmup requirements.
Creates issue #60 ([promo-radar]) and #61 ([affiliate-hub]).
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

TASK_UPDATES = {
    54: {
        "name": "[smm-runner] Базовый OCI-образ браузерного ИИ-агента (Firefox/Camoufox + Persistent Profile + Cache Warmup + US Proxy)",
        "description_html": """
<h2>🎯 Цель задачи</h2>
<p>
Собрать специализированный OCI-образ и раннер для автономных ИИ-СММ агентов на базе <b>Firefox / Camoufox (Gecko engine)</b>
с постоянным профилем (Persistent Context), обязательным прогревом кэша (Cache Warmup) и маршрутизацией трафика через IP США.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Браузерный движок: Firefox / Camoufox вместо Chromium:</b>
    Meta (Threads, Instagram), Reddit и Cloudflare активно детектируют CDP (Chrome DevTools Protocol).
    Использование Playwright Firefox / Camoufox устраняет маркеры автоматизации CDP на уровне ядра.</li>
  <li><b>СТРОЖАЙШИЙ ЗАПРЕТ на инкогнито:</b>
    Запрещено использовать <code>browser.new_context()</code> без истории.
    Использовать исключительно <code>firefox.launch_persistent_context(user_data_dir=...)</code>.</li>
  <li><b>Персистентность профилей в Redis:</b>
    Профиль браузера (файлы <code>cookies.sqlite</code>, <code>storage/default</code> IndexedDB, кэш, LocalStorage)
    архивируется и синхронизируется в Redis (<code>smm:profile:&lt;account_id&gt;</code>). При рестарте пода агент просыпается в уже «прогретом» окружении.</li>
  <li><b>Модуль прогрева кэша (Browser Cache Warmup):</b>
    Перед входом в соцсеть или выполнением действий агент серфит 2-3 минуты по нейтральным новостным и спортивным ресурсам,
    генерируя реальный HTTP-кэш, куки и историю с человекоподобными кривыми движения мыши (Безье) и случайными задержками ввода.</li>
  <li><b>Сетевая маршрутизация:</b> трафик по умолчанию направляется через прокси США (<code>100.66.190.4</code> / <code>ru-proxy</code>).</li>
  <li><b>Виртуальный дисплей:</b> запуск в Xvfb с разрешением 1920x1080x24.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Подготовить Dockerfile с установкой Python 3.11+, Playwright Firefox, Camoufox, Xvfb, tini.</li>
  <li>Реализовать модуль <code>browser_manager.py</code> с поддержкой <code>launch_persistent_context</code> и выгрузкой/загрузкой архива профиля в Redis.</li>
  <li>Реализовать модуль <code>cache_warmup.py</code> (предварительный серфинг по спортивным сайтам).</li>
  <li>Собрать OCI-образ <code>100.78.183.101:30500/smm-agent-base:latest</code> и протестировать на детект ботов.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Образ собран и доступен в кластерном реестре.</li>
  <li>Браузер запускается в режиме Firefox Persistent Context, проходит проверки Browserleaks на 100% без флагов автоматизации.</li>
  <li>Кэш и куки успешно персистятся в Redis.</li>
</ul>
""",
        "description_stripped": """[smm-runner] Базовый OCI-образ браузерного ИИ-агента (Firefox/Camoufox + Persistent Profile + Cache Warmup + US Proxy).
Стек: Python 3.11+, Playwright Firefox, Camoufox (Gecko engine), Xvfb.
Требования:
- Строгий запрет инкогнито и чистого Chromium из-за детекции CDP антифродом Meta/Threads.
- Использование firefox.launch_persistent_context с постоянной директорией userDataDir.
- Персистирование архива профиля (куки, IndexedDB, кэш) в Redis smm:profile:<account_id>.
- Обязательный модуль прогрева кэша (Cache Warmup) на 2-3 минуты серфинга по спортивным порталам перед целевыми действиями.
- Движения мыши по кривым Безье, естественные задержки ввода.
- Маршрутизация через IP США."""
    },
    55: {
        "name": "[auto-reg] Модуль персон, регистрация в БК и партнерских программах (US IP + 2FA Gateway)",
        "description_html": """
<h2>🎯 Цель задачи</h2>
<p>
Автоматизировать генерацию цифровых персон, регистрацию аккаунтов на целевых площадках (Reddit, форумы, соцсети),
а также регистрацию в букмекерских конторах (52 БК) и партнерских сетях с использованием 2FA Inbox Gateway.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Генератор персон:</b> генерация реалистичных профилей (имя, никнейм, спортивные предпочтения, отпечаток Firefox).</li>
  <li><b>Интеграция с 2FA Gateway:</b> автоматический опрос <code>auth-inbox-gateway:8000</code> для получения кодов из почты (IMAP) и TOTP-токенов.</li>
  <li><b>Регистрация в БК и партнерках:</b> модуль автоматического заполнения форм регистрации у букмекеров и партнерских сетей для получения реферальных ссылок и промокодов.</li>
  <li><b>Персистентные профили:</b> сохранение созданных сессий в Redis в формате Firefox persistent storage.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Реализовать генератор персон <code>persona_factory.py</code>.</li>
  <li>Создать клиенты регистрации для целевых сервисов в <code>scripts/auto_register/</code>.</li>
  <li>Интегрировать получение OTP-кодов через <code>auth-inbox-gateway</code>.</li>
  <li>Сохранять готовые профили в Redis.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Скрипт автономно регистрирует аккаунт, подтверждает 2FA и сохраняет прогретый профиль в Redis.</li>
</ul>
""",
        "description_stripped": """[auto-reg] Модуль персон, регистрация в БК и партнерских программах (US IP + 2FA Gateway).
Генерация персон, регистрация аккаунтов на форумах, Reddit, в БК и партнерских сетях.
Интеграция с auth-inbox-gateway для автоматического получения кодов подтверждения из почты и TOTP.
Сохранение сессий в Redis."""
    },
    56: {
        "name": "[smm-meta] ИИ-агент активности для Threads и Instagram (Firefox Persistent Context + Cache Warmup)",
        "description_html": """
<h2>🎯 Цель задачи</h2>
<p>
Развернуть автономного ИИ-агента для регулярной публикации контента и взаимодействия в <b>Threads</b> и <b>Instagram</b>
с использованием <b>Firefox Persistent Context</b>, предварительным прогревом кэша и защитой от антифрод-банов Meta.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Движок:</b> исключительно Firefox / Camoufox с <code>launch_persistent_context</code>. Никакого инкогнито!</li>
  <li><b>Прогрев кэша перед постингом:</b> серфинг по ленте рекомендаций 2-3 минуты, чтение постов, лайки с задержками Безье.</li>
  <li><b>Контентная фабрика:</b> интеграция с API <code>igaming-portal</code> и <code>igaming-aggregator</code> (топовые вилки &gt;5%, валуи, сравнение линий БК).</li>
  <li><b>Монетизация и конверсия:</b> автоматическая вставка реферальных трекинг-ссылок и промокодов SmartBet.guru.</li>
  <li><b>Обязательный дисклеймер:</b> отказ от ответственности и предупреждение об азартных играх.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Реализовать сценарий входа и постинга для Threads и Instagram в <code>meta_poster.py</code> через Firefox.</li>
  <li>Внедрить модуль прогрева кэша перед постингом.</li>
  <li>Подключить генерацию текстов через LLM с актуальными вилками и реферальными ссылками.</li>
  <li>Провести тестовую публикацию в аккаунтах (smartbet.guru, fr.smartbet.guru, es.smartbet.guru).</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Агент успешно авторизуется без проверочных блокировок Meta.</li>
  <li>Опубликован тестовый пост с разбором арбитража и ссылкой на калькулятор SmartBet.guru.</li>
</ul>
""",
        "description_stripped": """[smm-meta] ИИ-агент активности для Threads и Instagram (Firefox Persistent Context + Cache Warmup).
Использование только Firefox с постоянным профилем.
Прогрев кэша перед постингом (2-3 минуты серфинга).
Публикация аналитики вилок, валуев и партнерских ссылок с обязательным дисклеймером."""
    }
}

NEW_TASKS = [
    {
        "key": "promo_radar",
        "title": "[promo-radar] Автоматический краулер и мониторинг акций, фрибетов и бонусов 52 БК",
        "priority": "high",
        "state": "Todo",
        "blocked_by": [54],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Создать автономный краулер промо-разделов и бонусных лендингов всех <b>52 подключенных букмекеров</b>
для автоматического сбора фрибетов (бездепозитные бонусы, фрибеты за регистрацию, страховки ставок, кэшбэк).
Данные служат топливом для вирусного SMM-контента и конверсионных страниц SmartBet.guru.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Краулер акций:</b> периодический парсинг промо-разделов (Winline, Fonbet, Betcity, Pinnacle, 1xbet и др.) с извлечением:
    <ul>
      <li>Название акции и размер фрибета (например, <i>«3000₽ за регистрацию без депозита»</i>).</li>
      <li>Тип бонуса (FREEBET, DEPOSIT_MATCH, CASHBACK, ACCA_INSURANCE).</li>
      <li>Условия отыгрыша (вейджер, минимальный коэффициент, срок действия).</li>
      <li>Партнерская ссылка / промокод для получения бонуса.</li>
    </ul>
  </li>
  <li><b>База данных:</b> таблица <code>bookmaker_promos</code> в БД <code>igaming_portal</code>.</li>
  <li><b>API:</b> эндпоинты <code>GET /api/v1/promos</code> и <code>GET /api/v1/promos/daily-digest</code>.</li>
  <li><b>Фронтенд & SMM синергия:</b>
    <ul>
      <li>Трансляция акций на страницу <code>smartbet.guru/promos</code>.</li>
      <li>Автоматическая генерация ежедневного дайджеста фрибетов для Telegram, Threads, Instagram и Reddit.</li>
    </ul>
  </li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Создать модуль <code>igaming-promo-radar</code> или расширение краулеров в <code>igaming-source-*</code>.</li>
  <li>Спроектировать таблицу <code>bookmaker_promos</code> в PostgreSQL.</li>
  <li>Реализовать краулинг промо-страниц топ-15 БК первой волны.</li>
  <li>Добавить эндпоинт в <code>igaming-portal</code>.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>В базе сохранено не менее 30 актуальных акций и фрибетов от проверенных БК.</li>
  <li>Эндпоинт <code>/api/v1/promos</code> отдает структурированный список бонусов.</li>
  <li>Дайджест фрибетов готов к публикации в соцсетях.</li>
</ul>
""",
        "description_stripped": """[promo-radar] Автоматический краулер и мониторинг акций, фрибетов и бонусов 52 БК.
Сбор акций, фрибетов за регистрацию и бездепозитных бонусов 52 подключенных букмекеров.
Сохранение в bookmaker_promos в igaming_portal.
API /api/v1/promos.
Генерация дайджеста фрибетов для постов в Telegram, Threads, Instagram и Reddit."""
    },
    {
        "key": "affiliate_hub",
        "title": "[affiliate-hub] Регистрация в партнерских программах БК, генерация трекинг-ссылок и интеграция с SMM",
        "priority": "high",
        "state": "Todo",
        "blocked_by": [55, 60],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Автоматизировать управление партнерскими ссылками (Affiliate / Referral Hub), интеграцию с букмекерскими партнерскими программами (CPA / RevShare),
генерацию трекинг-ссылок с UTM-метками и бесшовную подстановку реферальных ссылок в контент соцсетей и сайта SmartBet.guru.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Хранилище реферальных программ:</b> таблица <code>affiliate_links</code> (букмекер, партнерская сеть, трекинг-ссылка, промокод, модель оплаты CPA/RevShare, UTM-шаблоны).</li>
  <li><b>Редирект-шлюз (Link Cloaker):</b> короткие URL вида <code>smartbet.guru/go/{bookmaker}?utm_source=telegram&utm_campaign=arb123</code> с фиксацией кликов и прозрачным 302-редиректом.</li>
  <li><b>Инъекция в SMM-контент:</b> автоматическая подстановка реферальных ссылок в:
    <ul>
      <li>Сигналы вилок (кнопки БК плеча 1 и плеча 2).</li>
      <li>Дайджесты фрибетов от <code>promo-radar</code>.</li>
      <li>Вилочный калькулятор (кнопка <i>«Проставить в [БК]»</i>).</li>
      <li>Профиль Instagram / Threads (Linktree / Bio-ссылка).</li>
    </ul>
  </li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Создать таблицу <code>affiliate_links</code> и редирект-роут в <code>smartbet-guru</code> (Next.js 14) <code>/go/[bookmaker]/route.ts</code>.</li>
  <li>Заполнить базовые партнерские ссылки и промокоды для БК.</li>
  <li>Интегрировать автоматическую генерацию ссылок в Telegram-бота и SMM-пайплайн.</li>
  <li>Проверить сквозной трекинг кликов.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Редирект <code>smartbet.guru/go/{bookmaker}</code> корректно перенаправляет пользователя по партнерской ссылке с сохранением UTM.</li>
  <li>Посты вилок и акций содержат кликабельные реферальные ссылки.</li>
</ul>
""",
        "description_stripped": """[affiliate-hub] Регистрация в партнерских программах БК, генерация трекинг-ссылок и интеграция с SMM.
Хранилище партнерских ссылок и промокодов 52 БК.
Редирект-шлюз smartbet.guru/go/[bookmaker] с UTM-трекингом.
Автоматическая инъекция партнерских ссылок в посты вилок, дайджесты фрибетов и калькулятор."""
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

def main():
    payload = {
        "updates": TASK_UPDATES,
        "new_tasks": NEW_TASKS
    }
    b64_payload = base64.b64encode(json.dumps(payload, ensure_ascii=False).encode("utf-8")).decode("ascii")

    django_code = f"""
import os, sys, json, base64, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue, User, Module, ModuleIssue, IssueBlocker

p = Project.objects.filter(identifier='IGAMING').first() or Project.objects.first()
user = User.objects.filter(email='aleksei.a.chernousov@gmail.com').first() or User.objects.first()

states = {{s.name: s for s in State.objects.filter(project=p)}}
todo_state = states.get('Todo') or states.get('Ожидание блокеров') or State.objects.filter(project=p, group='unstarted').first()

raw_data = base64.b64decode('{b64_payload}').decode('utf-8')
data = json.loads(raw_data)

# 1. Update existing issues (#54, #55, #56)
updated = []
for seq_id, upd in data['updates'].items():
    seq_id = int(seq_id)
    issue = Issue.objects.filter(project=p, sequence_id=seq_id).first()
    if issue:
        issue.name = upd['name']
        issue.description_html = upd['description_html']
        issue.description_stripped = upd['description_stripped']
        issue.save()
        updated.append(seq_id)

# 2. Get module
mod = Module.objects.filter(project=p, name__icontains='SMM').first()

# 3. Create new issues (#60, #61)
created_issues = []
max_seq = Issue.objects.filter(project=p).order_by('-sequence_id').values_list('sequence_id', flat=True).first() or 59

for n_def in data['new_tasks']:
    existing = Issue.objects.filter(project=p, name__icontains=n_def['key']).first()
    if not existing:
        max_seq += 1
        issue = Issue.objects.create(
            project=p,
            workspace=p.workspace,
            name=n_def['title'],
            sequence_id=max_seq,
            description_html=n_def['html_desc'],
            description_stripped=n_def['description_stripped'],
            priority=n_def['priority'],
            state=todo_state,
            created_by=user,
            updated_by=user
        )
        if mod:
            ModuleIssue.objects.get_or_create(
                project=p,
                workspace=p.workspace,
                module=mod,
                issue=issue
            )
        # Link blockers
        for b_seq in n_def['blocked_by']:
            b_issue = Issue.objects.filter(project=p, sequence_id=b_seq).first()
            if b_issue:
                IssueBlocker.objects.get_or_create(
                    project=p,
                    workspace=p.workspace,
                    block=issue,
                    blocked_by=b_issue,
                    defaults={{'created_by': user, 'updated_by': user}}
                )
        created_issues.append((issue.sequence_id, issue.name))
    else:
        existing.name = n_def['title']
        existing.description_html = n_def['html_desc']
        existing.description_stripped = n_def['description_stripped']
        existing.save()
        created_issues.append((existing.sequence_id, existing.name))

out = {{
    "status": "success",
    "updated": updated,
    "new_tasks": created_issues
}}
print("__PLANE_RES__:" + json.dumps(out))
"""

    print("🚀 Executing Plane task update and expansion via Django...")
    stdout, stderr = run_remote_django(django_code, timeout=60)
    for line in stdout.splitlines():
        if line.startswith("__PLANE_RES__:"):
            res = json.loads(line.replace("__PLANE_RES__:", ""))
            print("✅ Plane updated successfully!")
            print(f"Updated issues: {res.get('updated')}")
            print(f"Created/Syncd issues: {res.get('new_tasks')}")
            return
    print("Stdout:\n", stdout)
    if stderr:
        print("Stderr:\n", stderr)

if __name__ == "__main__":
    main()
