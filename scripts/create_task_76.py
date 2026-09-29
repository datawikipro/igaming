#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
create_task_76.py

Creates task #76 in Plane:
[stealth-novnc] Интерактивная noVNC-консоль браузеров в веб-админке и Fallback Captcha Solvers (CapSolver + noVNC Web UI)
"""

import json
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "xeon-local"
SSH_CONFIG = r"C:\Users\chernousov_a\.ssh\config"
PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"
WORKSPACE_SLUG = "dataplatform"
STATE_AI_DEV = "1d4476f7-1d60-4b35-8211-439d318bf5d7"     # AI разработка

DJANGO_SCRIPT = """
import os, sys, json, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()

from plane.db.models import Project, State, Issue, User, Module, ModuleIssue

p = Project.objects.filter(identifier='IGAMING').first() or Project.objects.first()
user = User.objects.filter(email='aleksei.a.chernousov@gmail.com').first() or User.objects.first()
state_ai = State.objects.filter(project=p, name='AI разработка').first()
if not state_ai:
    state_ai = State.objects.filter(project=p, group='started').first()

mod = Module.objects.filter(project=p, name__icontains='SMM').first()
if not mod:
    mod = Module.objects.filter(project=p).first()

title = "[stealth-novnc] Интерактивная noVNC-консоль браузеров в веб-админке и Fallback Captcha Solvers (CapSolver + noVNC Web UI)"
desc = '''
<h2>🎯 Цель задачи</h2>
<p>
Разработать и внедрить гибридную систему решения капч и удалённого контроля браузерных агентов <b>noVNC Web Console + Fallback Solvers</b>:
автоматическое распознавание капч через API (CapSolver / 2Captcha) и компьютерное зрение (OpenCV), а при необходимости —
интерактивное управление браузером агента прямо через веб-интерфейс <code>smartbet.guru/admin/browsers</code> (без AnyDesk).
</p>

<h3>🔍 Архитектурные уровни защиты (3-Tier Defense)</h3>
<ul>
  <li><b>Tier 1 — Превентивный обход (Camoufox Stealth):</b>
    Использование модифицированного Gecko-движка Camoufox с персистентными профилями (cookies, indexedDB, cache storage),
    генерацией валидных Canvas/WebGL/Audio/Font отпечатков, человекоподобными кривыми мыши Безье и рандомизированными паузами.
  </li>
  <li><b>Tier 2 — Автоматическое решение (Automated Solvers):</b>
    <ul>
      <li>Интеграция с <code>CapSolver</code> / <code>2Captcha</code> API: Cloudflare Turnstile, reCAPTCHA v2/v3, hCaptcha, GeeTest.</li>
      <li>OpenCV модуль для пазлов (Slider Puzzles): поиск выреза шаблона (template matching) и эмуляция перетаскивания ползунка с реалистичным профилем ускорения/замедления.</li>
    </ul>
  </li>
  <li><b>Tier 3 — Интерактивный noVNC Fallback (Zero-Install Web UI):</b>
    <ul>
      <li>Внутри OCI-контейнера браузерного агента запускается виртуальный X-сервер <code>Xvfb :99</code>, <code>x11vnc -display :99 -rfbport 5900</code> и <code>websockify --web /usr/share/novnc 6080 localhost:5900</code>.</li>
      <li>Порт 6080 проксируется через K8s Ingress / Traefik по защищённому пути <code>/admin/browsers/{service_name}/vnc</code>.</li>
      <li>В веб-админке <code>smartbet.guru/admin/browsers</code> интегрирован noVNC-клиент (HTML5 Canvas + WebSockets): оператор может в 1 клик подключиться к любому браузерному поду, кликнуть капчу руками или ввести 2FA-код.</li>
      <li>Telegram Alerting: при обнаружении нерешённой капчи бот мгновенно отправляет скриншот и direct-link в Telegram с кнопкой <i>«Решить в веб-консоли»</i>.</li>
    </ul>
  </li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Обновить базовый образ <code>smm-runner</code>: установить <code>xvfb</code>, <code>x11vnc</code>, <code>websockify</code>, <code>novnc</code>.</li>
  <li>Разработать Python-модуль <code>captcha_solver.py</code>: клиент CapSolver API + OpenCV шаблонный матчинг слайдеров + триггер алерта оператору.</li>
  <li>Разработать веб-интерфейс админки <code>/admin/browsers</code> в <code>smartbet.guru</code> (список активных сессий агентов, превью скриншотов, полноэкранная noVNC веб-консоль).</li>
  <li>Настроить K8s Ingress/Service маршрутизацию WebSocket трафика noVNC.</li>
  <li>Провести верификацию в <code>igaming-dev</code> с эмуляцией капчи.</li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Образ <code>smm-runner</code> собирается и запускает Xvfb, websockify и camoufox без ошибок.</li>
  <li>Страница <code>/admin/browsers</code> в Next.js отображает статус подов и позволяет взаимодействовать с браузером через WebSocket/noVNC.</li>
  <li>Автоматический модуль CapSolver/OpenCV отрабатывает с фолбеком на веб-консоль при сбоях.</li>
  <li>5-минутный soak test в K8s без единой ошибки.</li>
</ul>
'''

iss, created = Issue.objects.get_or_create(
    project=p,
    name=title,
    defaults={
        'workspace': p.workspace,
        'description_html': desc,
        'state': state_ai,
        'created_by': user,
        'updated_by': user,
        'priority': 'high'
    }
)

if not created:
    iss.description_html = desc
    iss.state = state_ai
    iss.priority = 'high'
    iss.save()

if mod:
    ModuleIssue.objects.get_or_create(
        module=mod,
        issue=iss,
        project=p,
        workspace=p.workspace,
        defaults={'created_by': user, 'updated_by': user}
    )

print(json.dumps({
    "sequence_id": iss.sequence_id,
    "id": str(iss.id),
    "name": iss.name,
    "created": created,
    "state": iss.state.name,
    "priority": iss.priority
}))
"""

def main():
    print("Creating Task #76 in Plane...")
    cmd = [
        "ssh", "-F", SSH_CONFIG,
        "-o", "BatchMode=yes",
        "-o", "ConnectTimeout=10",
        SSH_HOST,
        "kubectl exec -i -n plane deployment/plane-api -- python3 -"
    ]
    res = subprocess.run(cmd, input=DJANGO_SCRIPT, capture_output=True, text=True, timeout=60, encoding="utf-8")
    print("STDOUT:", res.stdout.strip())
    if res.stderr:
        print("STDERR:", res.stderr.strip())

if __name__ == "__main__":
    main()
