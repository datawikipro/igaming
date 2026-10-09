#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import base64
import json
import subprocess
import sys

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

TASKS = [
    {
        "title": "[community-boosty] Создание и оформление публичной страницы Boosty (SmartBet.guru), настройка уровней подписки и виджетов",
        "priority": "high",
        "state": "Todo",
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Зарегистрировать и оформить официальную страницу проекта на <b>Boosty</b> для монетизации русскоязычной аудитории (доны, VIP-сигналы арбитража, закрытый чат).
</p>
<h3>🔍 Шаги реализации</h3>
<ul>
  <li>Создать страницу <code>boosty.to/smartbetguru</code> (аватар, баннер, описание сервиса и калькулятора 80% фрибетов).</li>
  <li>Настроить уровни подписки:
    <ul>
      <li><b>Free Tier</b> — базовые посты и дайджесты бонусов;</li>
      <li><b>PRO Подписчик (500 ₽/мес)</b> — моментальные сигналы вилок до 5%, доступ к расширенному калькулятору;</li>
      <li><b>VIP Арбитраж (2 500 ₽/мес)</b> — коридоры, +EV сигналы, приоритетный доступ к боту и закрытый TG чат.</li>
    </ul>
  </li>
  <li>Подключить вебхук / API интеграцию с <code>igaming-boosty-bot</code> для автоматической выдачи инвайтов в закрытый VIP Telegram-канал.</li>
</ul>
"""
    },
    {
        "title": "[community-patreon] Создание и оформление страницы Patreon (SmartBet.guru) для зарубежной аудитории",
        "priority": "high",
        "state": "Todo",
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Создать и настроить страницу <b>Patreon</b> (<code>patreon.com/smartbetguru</code>) под международную англоязычную, французскую и испанскую аудиторию.
</p>
<h3>🔍 Шаги реализации</h3>
<ul>
  <li>Оформить профиль (логотип SmartBet.guru, калькулятор Matched Betting, дисклеймер).</li>
  <li>Установить международные тиры:
    <ul>
      <li><b>Supporter ($5/mo)</b> — поддержка разработки, ранний доступ к статьям;</li>
      <li><b>Pro Scanner ($19/mo)</b> — доступ к премиум-линии сканера без задержек;</li>
      <li><b>Syndicate VIP ($49/mo)</b> — персональные алерты по Pinnacle/Bet365/Sbobet, закрытый Discord/TG.</li>
    </ul>
  </li>
  <li>Связать с модулем <code>smm-agent/patreon_agent.py</code> для автоматического приема вебхуков о подписках.</li>
</ul>
"""
    },
    {
        "title": "[community-vk] Создание официального сообщества ВКонтакте (SmartBet.guru), настройка VK Donut и автопостинга",
        "priority": "high",
        "state": "Todo",
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Запустить официальную публичную страницу <b>ВКонтакте (vk.com/smartbetguru)</b>, подключить VK Donut для монетизации и настроить автопостинг контента через API.
</p>
<h3>🔍 Шаги реализации</h3>
<ul>
  <li>Создать паблик ВКонтакте «SmartBet.guru — Умные ставки, вилки и фрибеты».</li>
  <li>Оформить меню сообщества (ссылки на сайт, калькулятор 80% фрибетов, отзывы).</li>
  <li>Включить <b>VK Donut</b> (подписка от 300 ₽/мес) с эксклюзивными статьями и разборами БК.</li>
  <li>Настроить Callback API для приема комментариев и вопросов в личные сообщения группы с автоответами ИИ.</li>
</ul>
"""
    }
]

def main():
    b64 = base64.b64encode(json.dumps(TASKS).encode('utf-8')).decode('ascii')
    django_code = f"""
import os, sys, json, base64, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue, User, Module, ModuleIssue

p = Project.objects.filter(identifier='IGAMING').first() or Project.objects.first()
user = User.objects.filter(email='aleksei.a.chernousov@gmail.com').first() or User.objects.first()
states = {{s.name: s for s in State.objects.filter(project=p)}}
todo_state = states.get('Todo') or State.objects.filter(project=p, group='unstarted').first()

tasks = json.loads(base64.b64decode('{b64}').decode('utf-8'))
results = []
for t in tasks:
    iss, created = Issue.objects.get_or_create(
        project=p,
        name=t['title'],
        defaults={{
            'workspace': p.workspace,
            'description_html': t['html_desc'],
            'priority': t['priority'],
            'state': todo_state,
            'created_by': user,
            'updated_by': user
        }}
    )
    results.append({{'id': iss.sequence_id, 'title': iss.name, 'created': created}})
print('__COMMUNITY_TASKS__:' + json.dumps(results))
"""
    cmd = [
        "ssh", "-i", SSH_KEY, "-o", "BatchMode=yes", "-o", "ConnectTimeout=10",
        SSH_HOST, "kubectl exec -i -n plane deploy/plane-api -- python3 -"
    ]
    res = subprocess.run(cmd, input=django_code, capture_output=True, text=True, timeout=60, encoding="utf-8")
    for line in res.stdout.splitlines():
        if line.startswith("__COMMUNITY_TASKS__:"):
            data = json.loads(line.replace("__COMMUNITY_TASKS__:", ""))
            for item in data:
                status = "🆕 Created" if item['created'] else "🔄 Existed"
                print(f"{status} #{item['id']}: {item['title']}")
            return
    print(res.stdout, res.stderr)

if __name__ == "__main__":
    main()
