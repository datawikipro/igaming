#!/usr/bin/env python3
# -*- coding: utf-8 -*-
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

TASK_DEF = {
    "key": "video_shorts_generation",
    "title": "[video-shorts] Пайплайн автогенерации вертикальных видео (Shorts / Reels / TikTok) с разбором вилок и валуев",
    "priority": "low",
    "state": "Backlog",
    "blocked_by_key": "smm_meta_threads_ig",
    "blocked_by_title": "[smm-meta] ИИ-агент активности и публикации для Threads и Instagram (существующие аккаунты)",
    "html_desc": """
<h2>🎯 Цель задачи (Вторая очередь)</h2>
<p>
Построить автоматизированный пайплайн создания коротких вертикальных видеороликов (9:16)
для <b>YouTube Shorts</b>, <b>Instagram Reels</b> и <b>TikTok</b> для привлечения вирального бесплатного органического трафика.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Шаблонизатор контента:</b> генерация динамических видео из шаблона (Remotion / MoviePy / FFmpeg) на сервере Xeon.</li>
  <li><b>Структура ролика:</b>
    <ul>
      <li><i>Хук (0-3 сек):</i> «Букмекеры ошиблись на 15% в матче Реал — Барселона?!»</li>
      <li><i>Суть (3-20 сек):</i> визуализация вилочного калькулятора, коэффициентов и расчета гарантированной прибыли без риска.</li>
      <li><i>Призыв к действию (20-30 сек):</i> «Ссылка на бесплатный сканер SmartBet.guru в описании профиля».</li>
    </ul>
  </li>
  <li><b>Озвучка (TTS):</b> генерация голосового сопровождения через Edge-TTS или Kokoro TTS.</li>
  <li><b>Обязательный дисклеймер:</b> плашка об ответственной игре внизу видео.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Разработать скрипт рендеринга 30-секундных роликов на базе данных из <code>igaming-portal</code>.</li>
  <li>Встроить озвучку и динамические титры.</li>
  <li>Подключить автоматическую загрузку через браузерного агента в Reels и Shorts.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Скрипт рендерит готовый MP4 ролик за < 30 секунд на сервере Xeon.</li>
  <li>Видео полностью соответствует техническим требованиям 1080x1920 (9:16, 60fps).</li>
</ul>
"""
}

def add_video_task():
    payload = TASK_DEF
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
backlog_state = states.get('Backlog') or State.objects.filter(project=p, group='backlog').first()

raw_data = base64.b64decode('{b64_payload}').decode('utf-8')
data = json.loads(raw_data)

mod = Module.objects.filter(project=p, name__icontains='SMM Swarm').first()

issue, is_new = Issue.objects.get_or_create(
    project=p,
    name=data['title'],
    defaults={{
        'workspace': p.workspace,
        'description_html': data['html_desc'],
        'state': backlog_state,
        'created_by': user,
        'updated_by': user,
        'priority': data['priority']
    }}
)

if mod:
    ModuleIssue.objects.get_or_create(
        module=mod,
        issue=issue,
        project=p,
        workspace=p.workspace,
        defaults={{'created_by': user, 'updated_by': user}}
    )

blocking_issue = Issue.objects.filter(project=p, name=data['blocked_by_title']).first()
if blocking_issue:
    IssueBlocker.objects.get_or_create(
        project=p,
        workspace=p.workspace,
        block=issue,
        blocked_by=blocking_issue,
        defaults={{'created_by': user, 'updated_by': user}}
    )

print("__PLANE_VIDEO__:" + json.dumps({{'sequence_id': issue.sequence_id, 'title': issue.name, 'is_new': is_new}}))
"""
    cmd = [
        "ssh", "-i", SSH_KEY,
        "-o", "BatchMode=yes",
        "-o", "ConnectTimeout=10",
        SSH_HOST,
        "kubectl exec -i -n plane deploy/plane-api -- python3 -"
    ]
    res = subprocess.run(cmd, input=django_code, capture_output=True, text=True, timeout=60, encoding="utf-8")
    for line in res.stdout.splitlines():
        if line.startswith("__PLANE_VIDEO__:"):
            return json.loads(line.replace("__PLANE_VIDEO__:", ""))
    print("STDOUT:", res.stdout)
    if res.stderr:
        print("STDERR:", res.stderr)
    return {}

if __name__ == "__main__":
    res = add_video_task()
    print("Result:", res)
