#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import base64
import json
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

TASK_TITLE = "[video-shorts] Пайплайн автогенерации вертикальных видео (Shorts / Reels / TikTok) с разбором вилок и валуев"
UPDATED_DESC = """
<h2>🎯 Цель задачи (Вторая очередь)</h2>
<p>
Построить автоматизированный пайплайн создания коротких вертикальных видеороликов (9:16)
для <b>YouTube Shorts</b>, <b>Instagram Reels</b> и <b>TikTok</b> с разбором реальных арбитражных ситуаций и привлечения вирального трафика.
</p>

<h3>⚠️ Аппаратное ограничение сервера Xeon</h3>
<p>
Сервер <code>xeon-srv</code> не имеет дискретной видеокарты (GPU) и не предназначен для тяжелого локального видеомонтажа или нейросетевого рендеринга.
<b>Решение:</b> генерация видео строится исключительно через внешние облачные ИИ-сервисы и API (Kling / Runway / HeyGen / InVideo / D-ID) с готовым отдачей MP4 по вебхуку, без локальной нагрузки на процессор сервера.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Генерация сценария и хука:</b> LLM на базе реальной найденной вилки (>8%) из <code>igaming-portal</code>.</li>
  <li><b>Облачный рендеринг:</b> вызов внешнего AI Video API для синтеза спикера/аватара и наложения инфографики.</li>
  <li><b>Автопостинг:</b> загрузка готового ролика через браузерных агентов в Instagram Reels и YouTube Shorts.</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Пайплайн генерирует ролик через внешний API без задействования CPU Xeon в рендеринге.</li>
  <li>Файл видео доступен по прямой ссылке для публикации агентом.</li>
</ul>
"""

django_code = f"""
import os, sys, json, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, Issue

p = Project.objects.filter(identifier='IGAMING').first()
issue = Issue.objects.filter(project=p, name='{TASK_TITLE}').first()
if issue:
    issue.description_html = '''{UPDATED_DESC}'''
    issue.save()
    print('UPDATED_SUCCESS')
else:
    print('NOT_FOUND')
"""

cmd = [
    "ssh", "-i", SSH_KEY,
    "-o", "BatchMode=yes",
    "-o", "ConnectTimeout=10",
    SSH_HOST,
    "kubectl exec -i -n plane deploy/plane-api -- python3 -"
]
res = subprocess.run(cmd, input=django_code, capture_output=True, text=True, timeout=30, encoding="utf-8")
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
