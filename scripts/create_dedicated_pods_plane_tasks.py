#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
create_dedicated_pods_plane_tasks.py

Creates 4 dedicated Plane tasks for isolated Pod deployments:
- [pod-boosty] Выделенный pod и агент Boosty (изолированная учетка, сессия, чистый IP РФ)
- [pod-patreon] Выделенный pod и агент Patreon (изолированная учетка, US proxy, Webhooks)
- [pod-vk-donut] Выделенный pod и агент VK Donut (изолированная учетка сообщества, Callback API)
- [pod-tg-vip] Выделенный pod и бот закрытого VIP-сообщества Telegram (@SmartBetVipBot)
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

MODULE_NAME = "Patron CRM & Community Feedback Hub"

DEDICATED_POD_TASKS = [
    {
        "key": "pod_boosty",
        "title": "[pod-boosty] Выделенный pod и агент Boosty: изолированная учетка, сессия и сбор комментов донов",
        "priority": "high",
        "state": "AI разработка",
        "blocked_by": ["feedback_core"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Развернуть <b>отдельный изолированный pod</b> в Kubernetes (<code>smm-bot-boosty</code>) с выделенной учетной записью автора/модератора Boosty.
Никакого разделения сессий с другими сервисами: автономный жизненный цикл, независимый перезапуск и собственный сетевой маршрут.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>K8s Deployment & Config:</b> <code>smm-bot-boosty</code> в namespace <code>igaming-dev</code>.</li>
  <li><b>Изолированная сессия в Redis:</b> хранилище сессии <code>smm:session:boosty</code> (токены авторизации, cookies, LocalStorage).</li>
  <li><b>Сетевая маршрутизация:</b> прямой домашний IP СПб (<code>188.242.33.93</code> через <code>ru-proxy:direct</code>) без датацентровых прокси во избежание фрода Boosty.</li>
  <li><b>Функционал агента:</b>
    <ul>
      <li>Периодический мониторинг комментариев под платными постами и закрытыми ветками обсуждений.</li>
      <li>Проверка уровня подписки автора комментария и отправка тикета в <code>igaming-portal</code> с <code>is_paid=true</code>.</li>
      <li>Публикация эксклюзивных постов по уровням подписки.</li>
      <li>Публикация ответов в треды донов по сигналу из админки или авто-уведомлению о готовности фичи.</li>
    </ul>
  </li>
  <li><b>Healthcheck:</b> Actuator/HTTP health endpoint <code>/healthz</code> на порту 8080.</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Под <code>smm-bot-boosty</code> работает в статусе <code>Running 1/1</code> в <code>igaming-dev</code>.</li>
  <li>5-минутный soak test без единой ошибки и рестарта.</li>
</ul>
"""
    },
    {
        "key": "pod_patreon",
        "title": "[pod-patreon] Выделенный pod и агент Patreon: изолированная учетка, US Proxy, Webhooks & Member Desk",
        "priority": "high",
        "state": "AI разработка",
        "blocked_by": ["feedback_core"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Развернуть <b>отдельный изолированный pod</b> в Kubernetes (<code>smm-bot-patreon</code>) с выделенной учетной записью создателя Patreon.
Под обеспечивает приём вебхуков, мониторинг долларовых подписок и публикацию закрытых постов.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>K8s Deployment:</b> <code>smm-bot-patreon</code> в namespace <code>igaming-dev</code>.</li>
  <li><b>Изолированная сессия в Redis:</b> <code>smm:session:patreon</code> (Patreon OAuth2 Refresh/Access tokens, webhook secrets).</li>
  <li><b>Сетевая маршрутизация:</b> трафик через американский шлюз <code>outline-us</code> (<code>100.66.190.4</code>) во избежание региональных блокировок.</li>
  <li><b>Функционал агента:</b>
    <ul>
      <li>Слушатель Webhook APIv2 (<code>members:pledge:create</code>, <code>members:pledge:update</code>, <code>posts:comments:create</code>).</li>
      <li>Валидация входящих подписей вебхуков HMAC-SHA256.</li>
      <li>Регистрация обращений патронов в <code>feedback_item</code> с фиксацией валюты USD и уровня поддержки.</li>
      <li>Публикация англоязычных премиум-постов для зарубежных вилочников ($25/mo, $100/mo).</li>
    </ul>
  </li>
  <li><b>Healthcheck:</b> HTTP <code>/healthz</code> на порту 8080.</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Под задеплоен в <code>igaming-dev</code> в статусе <code>Running 1/1</code>.</li>
  <li>5 минут стабильной работы в k8s.</li>
</ul>
"""
    },
    {
        "key": "pod_vk_donut",
        "title": "[pod-vk-donut] Выделенный pod и агент VK Donut: изолированная учетка сообщества, Callback API и ответы донам",
        "priority": "high",
        "state": "AI разработка",
        "blocked_by": ["feedback_core"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Развернуть <b>отдельный изолированный pod</b> в Kubernetes (<code>smm-bot-vk</code>) с выделенным токеном доступа сообщества ВКонтакте.
Под управляет клубом донов VK Donut, обрабатывает события оплаты и взаимодействует с платящими подписчиками.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>K8s Deployment:</b> <code>smm-bot-vk</code> в namespace <code>igaming-dev</code>.</li>
  <li><b>Изолированная сессия в Redis:</b> <code>smm:session:vk</code> (Group Access Token, Confirmation Secret, Secret Key).</li>
  <li><b>Сетевая маршрутизация:</b> прямой домашний IP СПб (<code>ru-proxy:direct</code>) для минимального пинга к серверам VK.</li>
  <li><b>Функционал агента:</b>
    <ul>
      <li>Приём событий VK Callback API: <code>donut_subscription_create</code>, <code>donut_subscription_prolonged</code>, <code>donut_subscription_price_changed</code>.</li>
      <li>Мониторинг комментариев на стене сообщества с проверкой флага <code>donut.is_don = 1</code>.</li>
      <li>Маркировка обращений донов как <code>is_paid=true</code> с фиксацией тарифа VK Donut.</li>
      <li>Публикация эксклюзивных постов для донов сообщества.</li>
    </ul>
  </li>
  <li><b>Healthcheck:</b> HTTP <code>/healthz</code> на порту 8080.</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Под <code>smm-bot-vk</code> запущен и стабильно работает 5 минут в <code>igaming-dev</code>.</li>
</ul>
"""
    },
    {
        "key": "pod_tg_vip",
        "title": "[pod-tg-vip] Выделенный pod и бот закрытого VIP-сообщества Telegram (@SmartBetVipBot)",
        "priority": "high",
        "state": "AI разработка",
        "blocked_by": ["feedback_core"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Развернуть <b>отдельный изолированный pod</b> в Kubernetes (<code>igaming-bot-vip</code>) с выделенным Telegram-ботом (<code>@SmartBetVipBot</code>),
полностью изолированным от публичного вещательного канала. Бот администрирует закрытую супергруппу донатеров.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>K8s Deployment:</b> <code>igaming-bot-vip</code> в namespace <code>igaming-dev</code>.</li>
  <li><b>Изолированный бот-токен:</b> отдельный токен от BotFather в K8s Secret <code>telegram-vip-bot-secret</code>.</li>
  <li><b>Сессия и контекст в Redis:</b> <code>smm:session:tg_vip</code>.</li>
  <li><b>Функционал агента:</b>
    <ul>
      <li>Приветствие новых VIP-пользователей при входе в закрытый чат, верификация статуса подписки.</li>
      <li>Перехват всех сообщений участников супергруппы, автоматическое определение автора и создание тикета обратной связи в <code>feedback_item</code> с наивысшим приоритетом <code>P1_URGENT_PATRON</code>.</li>
      <li>Мгновенная доставка сигналов по вилкам с доходностью >10% и эксклюзивных коридоров.</li>
      <li>Персональные ответы участникам и отправка нотификаций о готовности запрошенных ими фич.</li>
    </ul>
  </li>
  <li><b>Healthcheck:</b> Spring Boot Actuator <code>/actuator/health/liveness</code> и <code>/actuator/health/readiness</code>.</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Под <code>igaming-bot-vip</code> запущен в <code>igaming-dev</code> в статусе <code>Running 1/1</code>.</li>
  <li>5 минут soak test без сбоев соединения с Telegram Bot API.</li>
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


def sync_dedicated_pods():
    payload = {
        "module_name": MODULE_NAME,
        "tasks": DEDICATED_POD_TASKS
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

raw_data = base64.b64decode('{b64_payload}').decode('utf-8')
data = json.loads(raw_data)

mod = Module.objects.filter(project=p, name__icontains=data['module_name']).first()
if not mod:
    mod, _ = Module.objects.get_or_create(
        project=p,
        name=data['module_name'],
        defaults={{'created_by': user, 'updated_by': user}}
    )

feedback_core_issue = Issue.objects.filter(project=p, name__icontains='[feedback-core]').first()

results = []
for t_def in data['tasks']:
    issue = Issue.objects.filter(project=p, name=t_def['title']).first()
    is_new = False
    if not issue:
        issue = Issue.objects.create(
            project=p,
            workspace=p.workspace,
            name=t_def['title'],
            description_html=t_def['html_desc'],
            state=ai_dev_state,
            created_by=user,
            updated_by=user,
            priority=t_def['priority']
        )
        is_new = True
    else:
        issue.description_html = t_def['html_desc']
        issue.priority = t_def['priority']
        issue.state = ai_dev_state
        issue.save()

    ModuleIssue.objects.get_or_create(
        module=mod,
        issue=issue,
        project=p,
        workspace=p.workspace,
        defaults={{'created_by': user, 'updated_by': user}}
    )

    if feedback_core_issue:
        IssueBlocker.objects.get_or_create(
            project=p,
            workspace=p.workspace,
            block=issue,
            blocked_by=feedback_core_issue,
            defaults={{'created_by': user, 'updated_by': user}}
        )

    results.append({{
        "key": t_def['key'],
        "sequence_id": issue.sequence_id,
        "id": str(issue.id),
        "title": issue.name,
        "is_new": is_new,
        "state": issue.state.name if issue.state else None,
        "priority": issue.priority
    }})

output = {{
    "status": "success",
    "tasks": results
}}

print("__PLANE_PODS__:" + json.dumps(output))
"""
    stdout, stderr = run_remote_django(django_code, timeout=120)
    for line in stdout.splitlines():
        if line.startswith("__PLANE_PODS__:"):
            return json.loads(line.replace("__PLANE_PODS__:", ""))
    print("Raw stdout:\n", stdout)
    if stderr:
        print("Raw stderr:\n", stderr)
    return {"status": "error", "output": stdout, "stderr": stderr}


def main():
    print("🚀 Synchronizing Dedicated Pod tasks into Plane...")
    res = sync_dedicated_pods()
    if res.get("status") == "success":
        print("\n✅ Successfully created Dedicated Pod Plane tasks!")
        for t in res.get("tasks", []):
            print(f"{t['sequence_id']}: (\"{t['id']}\", \"{t['title']}\"),")


    else:
        print("❌ Error during task creation:", res)


if __name__ == "__main__":
    main()
