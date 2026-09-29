#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
create_feedback_hub_plane_tasks.py

Decomposes and synchronizes the "Patron CRM & Unified Community Feedback Hub" tasks into Plane:
- Module: Patron CRM & Community Feedback Hub (SmartBet.guru)
- 6 Interlinked Tasks:
  1. [feedback-core] Подсистема хранения и модель данных (PostgreSQL JPA)
  2. [patron-ingest] Адаптеры интеграции платных платформ (Boosty, Patreon, VK Donut, TG VIP)
  3. [feedback-nlp] ИИ-классификатор, суммаризация обращений и генерация черновиков ответов
  4. [feedback-admin-ui] Единая админ-панель обратной связи в smartbet.guru (/admin/feedback)
  5. [plane-sync-loop] Двусторонняя синхронизация с Plane и авто-уведомление донатера о релизе фичи
  6. [patron-content] Пайплайн публикации эксклюзивного контента для платных подписчиков
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
    "name": "Patron CRM & Community Feedback Hub",
    "description": "Единый центр сбора обратной связи, пожеланий и баг-репортов с разделением платных донатеров (Boosty, Patreon, VK Donut, TG VIP) и бесплатных пользователей, AI-суммаризацией, единой админкой и сквозным циклом Plane -> Релиз -> Уведомление патрона."
}

TASKS_DEF = [
    # ── Task 1: Core Data Model & JPA ─────────────────────────────────────────
    {
        "key": "feedback_core",
        "title": "[feedback-core] Подсистема хранения и модель данных (PostgreSQL JPA): community_identity, patron_subscription, feedback_item",
        "priority": "urgent",
        "state": "AI разработка",
        "blocked_by": [],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Разработать реляционную модель данных и JPA-сущности в <code>igaming-portal</code> для сбора и агрегации обратной связи со всех каналов
с жестким разграничением прав, статусов донатеров (Boosty, Patreon, VK Donut, TG VIP, Site PRO) и бесплатных пользователей.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Таблица <code>community_identity</code>:</b> сквозной профиль пользователя (<code>user_id</code>, <code>telegram_id</code>, <code>boosty_user_id</code>, <code>patreon_id</code>, <code>vk_id</code>, <code>is_paid</code>, <code>vip_tier</code>, <code>monthly_spend</code>, <code>total_donated</code>, <code>badges</code>, <code>admin_notes</code>).</li>
  <li><b>Таблица <code>patron_subscription</code>:</b> история и текущие статусы подписок (платформа, tier, статус ACTIVE/EXPIRED, сумма, валюта, даты).</li>
  <li><b>Таблица <code>feedback_item</code>:</b> тикеты обратной связи (<code>source_platform</code>, <code>source_message_id</code>, <code>is_paid</code>, <code>patron_tier</code>, <code>donor_ltv</code>, <code>category</code> [FEATURE_REQUEST, BUG_REPORT, QUESTION, PRAISE, NOISE], <code>priority</code> [P1_VIP, P2_PAID, P3_FREE, P4_NOISE], <code>content</code>, <code>ai_summary</code>, <code>status</code>, <code>plane_issue_id</code>, <code>ai_draft_reply</code>).</li>
  <li><b>Liquibase / Flyway миграции:</b> версионированные SQL-скрипты с индексами по <code>is_paid</code>, <code>priority</code>, <code>status</code>, <code>created_at</code>.</li>
  <li><b>REST DTO & Repository:</b> Spring Data JPA репозитории с методами постраничной фильтрации для админки.</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Модуль <code>igaming-portal</code> успешно компилируется без ошибок.</li>
  <li>Миграции накатаны в PostgreSQL.</li>
  <li>Интеграционные тесты покрывают сохранение и связывание identity с тикетами.</li>
</ul>
"""
    },

    # ── Task 2: Patron Ingestion Adapters ──────────────────────────────────────
    {
        "key": "patron_ingest",
        "title": "[patron-ingest] Адаптеры интеграции платных платформ (Boosty, Patreon Webhooks, VK Donut Callback API, TG VIP Bot)",
        "priority": "high",
        "state": "AI разработка",
        "blocked_by": ["feedback_core"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Построить адаптеры и слушатели для автоматического приёма сообщений, комментариев и событий подписки со всех платных площадок:
<b>Boosty</b>, <b>Patreon</b>, <b>VK Donut / VK Premium</b> и <b>Telegram VIP</b>.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Patreon Webhook:</b> эндпоинт <code>/api/v1/webhooks/patreon</code> для обработки <code>members:pledge:create</code>, <code>members:pledge:update</code> и комментариев.</li>
  <li><b>VK Donut Callback API:</b> эндпоинт <code>/api/v1/webhooks/vk</code> для обработки <code>donut_subscription_create</code>, <code>donut_subscription_prolonged</code> и комментариев со стены группы с валидацией <code>donut.is_don</code>.</li>
  <li><b>Telegram VIP Listener (igaming-bot):</b> бот-слушатель в закрытом VIP-чате/канале, перехватывающий сообщения, проверяющий Telegram ID в <code>community_identity</code> и регистрирующий обращение с <code>is_paid=true</code>.</li>
  <li><b>Boosty Scraper / API adapter:</b> сбор комментариев донов под платными постами и личных сообщений в <code>smm-runner</code>.</li>
  <li><b>Автоматический пересчет LTV:</b> при каждом новом донате или продлении подписки поле <code>total_donated</code> в <code>community_identity</code> инкрементируется.</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Вебхуки валидируют сигнатуры и секреты.</li>
  <li>Тестовые события от Patreon, VK Donut и Telegram VIP создают тикеты с бейджем <code>is_paid=true</code> и соответствующим тарифом.</li>
</ul>
"""
    },

    # ── Task 3: NLP Categorization & AI Summary ───────────────────────────────
    {
        "key": "feedback_nlp",
        "title": "[feedback-nlp] ИИ-классификатор, суммаризация обращений и генерация черновиков ответов (LLM Gateway)",
        "priority": "high",
        "state": "AI разработка",
        "blocked_by": ["feedback_core"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Интегрировать конвейер автоматической обработки входящей обратной связи на базе LLM (через <code>llm-gateway</code>):
классификация категории, расчет тональности, генерация выжимки в 1 предложение и черновика персонального ответа.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Категоризация:</b> определение типа обращения:
    <ul>
      <li><code>FEATURE_REQUEST</code> — пожелание / запрос нового функционала.</li>
      <li><code>BUG_REPORT</code> — ошибка в линии, баг калькулятора, проблема с авторизацией.</li>
      <li><code>QUESTION</code> — вопрос по тарифам, стратегиям вилкования, настройке прокси.</li>
      <li><code>PRAISE</code> — благодарность / положительный отзыв (кандидат в промо-материалы).</li>
      <li><code>NOISE</code> — спам, нерелевантный флуд, токсичные выпады без сути.</li>
    </ul>
  </li>
  <li><b>Приоритет и SLA:</b>
    <ul>
      <li><code>P1_URGENT_PATRON</code> (SLA 15 мин): Платный VIP-патрон + Баг или Фича.</li>
      <li><code>P2_STANDARD_PATRON</code> (SLA 2 часа): Платный подписчик базового тарифа.</li>
      <li><code>P3_FREE_VALUABLE</code>: Бесплатный пользователь с конструктивной идеей.</li>
      <li><code>P4_FREE_NOISE</code>: Бесплатный спам/флуд (автоответ или архив).</li>
    </ul>
  </li>
  <li><b>Суммаризация (ai_summary):</b> сжатие длинного эмоционального текста в емкий тезис (до 100 символов).</li>
  <li><b>Генерация черновика (ai_draft_reply):</b> вежливый, технически выверенный ответ с обращением по имени, благодарностью за донат/подписку и указанием дальнейших шагов.</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Пакет юнит-тестов на 20 эталонных сообщениях показывает точность классификации > 90%.</li>
  <li>Время обработки одного обращения через LLM Gateway < 2 секунд.</li>
</ul>
"""
    },

    # ── Task 4: Feedback Admin UI ─────────────────────────────────────────────
    {
        "key": "feedback_admin_ui",
        "title": "[feedback-admin-ui] Единая админ-панель обратной связи в smartbet.guru (/admin/feedback)",
        "priority": "urgent",
        "state": "AI разработка",
        "blocked_by": ["feedback_core"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Разработать современный, реактивный интерфейс админ-панели <b>Feedback & Patron Desk</b> в приложении <code>smartbet.guru</code>
(Next.js 14, Tailwind CSS, TypeScript) по адресу <code>/[locale]/admin/feedback</code>.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Метрики в шапке дашборда:</b>
    <ul>
      <li>Текущий MRR от платных каналов (Boosty + Patreon + VK Donut + TG).</li>
      <li>Количество открытых VIP-тикетов (с таймером обратного отсчета SLA).</li>
      <li>Количество внедренных пожеланий донатеров.</li>
      <li>Размер общего бэклога идей.</li>
    </ul>
  </li>
  <li><b>Визуальная дифференциация карточек:</b>
    <ul>
      <li><b>Золотая плашка VIP-патрона:</b> градиентный бейдж <code>[💎 BOOSTY PRO — 2 500 ₽/мес | LTV: 17 500 ₽]</code>, ссылка на профиль, приоритет P1/P2.</li>
      <li><b>Серая плашка Free:</b> бейдж <code>[⚪ БЕСПЛАТНЫЙ: Threads @vasya]</code>.</li>
    </ul>
  </li>
  <li><b>Табы и фильтры:</b> «🔥 Только донатеры (P1/P2)», «💡 Пожелания (Фичи)», «🐛 Баги», «❓ Вопросы», «🌐 Бесплатные соцсети», «✅ Решенные».</li>
  <li><b>Действия в карточке:</b>
    <ul>
      <li>Кнопка <code>[🚀 В один клик в Plane]</code> — открытие предзаполненной модалки создания задачи.</li>
      <li>Кнопка <code>[⚡ Сгенерировать AI-ответ]</code> / <code>[💬 Ответить в Boosty/TG]</code>.</li>
      <li>Кнопка <code>[Перевести в архив / Решено]</code>.</li>
    </ul>
  </li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Компонент <code>FeedbackAdminDashboard.tsx</code> компилируется без ошибок TypeScript и Next.js.</li>
  <li>Поддерживается светлая и темная тема оформления.</li>
</ul>
"""
    },

    # ── Task 5: Plane Bi-directional Loop & Patron Notification ───────────────
    {
        "key": "plane_sync_loop",
        "title": "[plane-sync-loop] Двусторонняя синхронизация с Plane и авто-уведомление донатера о релизе фичи",
        "priority": "high",
        "state": "Todo",
        "blocked_by": ["feedback_admin_ui", "feedback_nlp"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Построить замкнутый цикл: обращение донатера Коли → создание задачи в Plane → реализация ИИ-воркерами на Xeon →
деплой в K8s dev/prod → <b>автоматическое уведомление Коли в его канал о том, что его фича готова</b>.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Создание Issue в Plane:</b> вызов REST API Plane из <code>igaming-portal</code> при нажатии кнопки в админке:
    <ul>
      <li>Название: <code>[feature/...] Краткое название (Запрос от VIP-патрона Коли)</code>.</li>
      <li>Метки: <code>vip-patron</code>, <code>community-request</code>.</li>
      <li>Связывание: сохранение <code>plane_issue_id</code> и <code>sequence_id</code> в тикете <code>feedback_item</code>.</li>
    </ul>
  </li>
  <li><b>Обратный Webhook из Plane:</b> перехват события перехода задачи в статус <code>Завершено</code> (Done).</li>
  <li><b>Автоматическая нотификация патрона:</b>
    <ul>
      <li>Отправка персонального сообщения через <code>igaming-bot</code> (если запрос из Telegram VIP).</li>
      <li>Публикация ответа на комментарий через <code>smm-runner</code> (если запрос из Boosty / Patreon).</li>
      <li>Текст: <i>«Коля, привет! Твое пожелание реализовано в релизе! Фича уже доступна на smartbet.guru. Огромное спасибо за поддержку!»</i></li>
    </ul>
  </li>
  <li><b>Автоматический перевод тикета в статус:</b> <code>RESOLVED</code>.</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Эмулированный прогон: создание задачи в Plane → смена статуса на Done → отправка вебхука и подтверждение доставки уведомления.</li>
</ul>
"""
    },

    # ── Task 6: Patron Exclusive Content Distribution ────────────────────────
    {
        "key": "patron_content",
        "title": "[patron-content] Пайплайн публикации эксклюзивного контента для платных подписчиков (Boosty, Patreon, VK Donut, TG VIP)",
        "priority": "medium",
        "state": "Todo",
        "blocked_by": ["patron_ingest"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Реализовать разделение контент-плана: бесплатные каналы получают базовые обучающие посты и тизеры,
а платные донатеры (Boosty, Patreon, VK Donut, Telegram VIP) получают эксклюзивную инсайдерскую аналитику и профитные связки.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Типы премиального контента:</b>
    <ul>
      <li><b>Жирные связки и коридоры:</b> разбор вилок с доходностью от 10% до 25% с детальным анализом лимитов и правил БК.</li>
      <li><b>Дайджест акций и фрибетов (80% кэша):</b> подборка свежих бонусов с пошаговыми инструкциями конвертации в реальные деньги.</li>
      <li><b>Мануалы по антифроду:</b> инструкции по прогреву браузерных профилей, настройке мобильных прокси и работе с дропами.</li>
    </ul>
  </li>
  <li><b>Форматирование под платформы:</b>
    <ul>
      <li>Boosty: форматированные статьи с закрытым доступом по уровням подписки.</li>
      <li>Telegram VIP: защищенные посты с запретом пересылки и скриншотов.</li>
      <li>VK Donut: эксклюзивные посты для донов группы.</li>
    </ul>
  </li>
  <li><b>Авто-публикация по расписанию:</b> интеграция с планировщиком в <code>igaming-bot</code> и <code>smm-runner</code>.</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Пайплайн генерирует и успешно доставляет тестовый премиальный пост в закрытую тестовую супергруппу Telegram VIP.</li>
</ul>
"""
    }
]


def run_remote_django(code, timeout=60):
    cmd = [
        "ssh", "-i", SSH_KEY,
        "-o", "BatchMode=yes",
        "-o", "ConnectTimeout=10",
        SSH_HOST,
        "kubectl exec -i -n plane deploy/plane-api -- python3 -"
    ]
    res = subprocess.run(cmd, input=code, capture_output=True, text=True, timeout=timeout, encoding="utf-8")
    return res.stdout, res.stderr


def sync_tasks():
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

p = Project.objects.filter(identifier='IGAMING').first()
user = User.objects.filter(email='aleksei.a.chernousov@gmail.com').first() or User.objects.first()

states = {{s.name: s for s in State.objects.filter(project=p)}}
ai_dev_state = states.get('AI разработка') or State.objects.filter(project=p, group='started').first()
todo_state = states.get('Todo') or states.get('Backlog') or State.objects.filter(project=p, group='unstarted').first()

raw_data = base64.b64decode('{b64_payload}').decode('utf-8')
data = json.loads(raw_data)

# 1. Create or get Module
mod_def = data['module']
mod, _ = Module.objects.get_or_create(
    project=p,
    name=mod_def['name'],
    defaults={{
        'description': mod_def['description'],
        'status': 'in-progress',
        'created_by': user,
        'updated_by': user
    }}
)

task_key_to_issue = {{}}
results = []

# 2. Create or update Issues
for t_def in data['tasks']:
    key = t_def['key']
    desired_state = ai_dev_state if t_def['state'] == 'AI разработка' else todo_state
    
    issue = Issue.objects.filter(project=p, name=t_def['title']).first()
    is_new = False
    if not issue:
        issue = Issue.objects.create(
            project=p,
            workspace=p.workspace,
            name=t_def['title'],
            description_html=t_def['html_desc'],
            state=desired_state,
            created_by=user,
            updated_by=user,
            priority=t_def['priority']
        )
        is_new = True
    else:
        issue.description_html = t_def['html_desc']
        issue.priority = t_def['priority']
        issue.save()

    task_key_to_issue[key] = issue

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
        "id": str(issue.id),
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

print("__PLANE_FEEDBACK__:" + json.dumps(output))
"""
    stdout, stderr = run_remote_django(django_code, timeout=120)
    for line in stdout.splitlines():
        if line.startswith("__PLANE_FEEDBACK__:"):
            return json.loads(line.replace("__PLANE_FEEDBACK__:", ""))
    print("Raw stdout:\n", stdout)
    if stderr:
        print("Raw stderr:\n", stderr)
    return {"status": "error", "output": stdout, "stderr": stderr}


def main():
    print("🚀 Synchronizing Patron CRM & Community Feedback Hub tasks into Plane...")
    res = sync_tasks()
    if res.get("status") == "success":
        print("\n✅ Successfully created Plane tasks!")
        print(f"📦 Module: {res['module']['name']}")
        print(f"🔗 Dependencies created: {res.get('new_dependencies_count', 0)}")
        print("\n=== TASKS SUMMARY ===")
        for t in res.get("tasks", []):
            status = "🆕 Created" if t.get("is_new") else "🔄 Updated"
            deps = f" [Blocked by: {', '.join(t['blocked_by'])}]" if t['blocked_by'] else " [NO DEPS -> IN WORK]"
            print(f"{status} #{t['sequence_id']:<4} | {t['state']:<14} | {t['title']}{deps}")
    else:
        print("❌ Error during task creation:", res)


if __name__ == "__main__":
    main()
