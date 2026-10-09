#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
create_plane_social_swarm_tasks.py

Creates 7 specialized tasks in Plane for autonomous AI Workers to execute in parallel:
1. [smm-twitter] Twitter/X (@smartbetguru) — Android/Web Automation, Signal Posting & Comment Monitor
2. [smm-telegram] Telegram Multi-Channel Hub (EN, FR, ES, RU) — Bot Admin, Public Usernames & Discussion Comments
3. [smm-instagram] Instagram Swarm (FR/ES/EN) — Ephemeral Phone Lifecycle, Coupon Cards & Comment SLA
4. [smm-threads] Meta Threads Swarm — Camoufox Persistent Context, Warmup & Thread Posting
5. [smm-reddit] Reddit Crowd Marketing — r/sportsbook & r/matchedbetting with Dedicated US Proxy
6. [smm-identity] Regional Identity Factory — Catch-all Domains, SMS-Activate Gateway & Regional Google Profiles
7. [smm-multimedia] YouTube Shorts & TikTok Video Pipeline — Account Setup & Automated 9:16 Video Publishing
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
    "name": "Social Media Swarm & Multi-Platform Growth (SmartBet.guru)",
    "description": "Автономная мультиплатформенная экосистема привлечения аудитории: Twitter/X, Telegram (мультиязычные каналы), Instagram, Threads, Reddit, фабрика региональных Gmail/Catch-all аккаунтов и видео-конвейер YouTube Shorts / TikTok."
}

TASKS_DEF = [
    {
        "key": "smm_twitter",
        "title": "[smm-twitter] Автоматизация Twitter/X (@smartbetguru): авторизация, постинг вилок и мониторинг комментариев",
        "priority": "urgent",
        "state": "Todo",
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Обеспечить полную автономность аккаунта <b>Twitter / X (@smartbetguru)</b>: сквозную авторизацию, регулярный постинг арбитражных сигналов и фрибетов с расчетом 80% гарантированного кэша, а также цикл мониторинга и ответов на комментарии пользователей.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Сетевая маршрутизация:</b> трафик к доменам <code>x.com</code>, <code>twitter.com</code>, <code>t.co</code>, <code>twimg.com</code> направляется через американский шлюз <code>outline-us</code> на кластерном роутере <code>ru-proxy</code> (обход ТСПУ/РКН).</li>
  <li><b>Исполнение:</b> Android-эмулятор (порт 5555 / noVNC 6080) с приложением Twitter/Twitter Lite либо автономный headless Chromium с персистентным профилем сессии в <code>/opt/smm-agent/storage/twitter_profile</code>.</li>
  <li><b>Формула фрибета:</b> каждый твит с фрибетом публикуется с обязательным расчетом 80% гарантированной прибыли (SNR Matched Betting): <i>«£100 Freebet → £82 Guaranteed Cash»</i>.</li>
  <li><b>Мониторинг комментариев:</b> после публикации твита запускается таймер ожидания (15 минут). При появлении реплаев агент генерирует математически выверенный ответ через LLM.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Проверить и зафиксировать сессию входа в Twitter через <code>twitter_post.py</code> на xeon-srv или через Android Chrome / Twitter App.</li>
  <li>Реализовать функцию <code>check_and_reply_comments(tweet_id)</code> с обращением к LLM.</li>
  <li>Интегрировать постинг в общий планировщик <code>seo_robot.py</code> с интервалом раз в 4 часа.</li>
</ol>
"""
    },
    {
        "key": "smm_telegram",
        "title": "[smm-telegram] Мультиязычная сеть Telegram (RU, EN, FR, ES): бот-админ, публичные @username и чтение комментариев",
        "priority": "urgent",
        "state": "Todo",
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Завершить настройку 3 новых региональных каналов Telegram (SmartBet.guru | English, France, España), выдать права боту <b>@smartbet_guru_bot</b>, установить публичные ссылки (@username) и подключить привязанные группы для чтения и ответов на комментарии.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>ID каналов:</b> English (<code>-3960368887</code>), France (<code>-4371643544</code>), España (<code>-4346736376</code>). Вход выполнен с сессии владельца <code>+79137671550</code>.</li>
  <li><b>Назначение бота в админы:</b> через существующую сессию Playwright Telegram Web назначить <code>@smartbet_guru_bot</code> администратором с правами публикации сообщений.</li>
  <li><b>Публичные юзернеймы:</b> установить каналы как публичные с именами <code>@SmartBetGuruEN</code>, <code>@SmartBetGuruFR</code>, <code>@SmartBetGuruES</code>.</li>
  <li><b>Комментарии:</b> создать и привязать группы обсуждения (Discussion Groups) к каждому каналу. Агент слушает входящие сообщения в группах и отвечает через ИИ-суфлера.</li>
</ul>

<h3>📋 Пошаговый чеклист реализации</h3>
<ol>
  <li>Автоматизировать добавление @smartbet_guru_bot в администраторы через скрипт <code>smm-agent/telegram_web_manager.py</code>.</li>
  <li>Включить Bot API постинг в ChannelPosterScheduler с отправкой нативных карточек и кнопок перехода на сайт.</li>
  <li>Подключить вебхук обработки комментариев в бота.</li>
</ol>
"""
    },
    {
        "key": "smm_instagram",
        "title": "[smm-instagram] Instagram Swarm (fr.smartbet.guru + регионы): эфемерный жизненный цикл телефона и 15-мин SLA комментариев",
        "priority": "high",
        "state": "Todo",
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Автоматизировать постинг в Instagram (начиная с <code>fr.smartbet.guru</code>) по строгой архитектуре: 1 управляющий сервис запускает телефон в Podman, открывает Instagram, публикует визуальный купон, ждет 15 минут для ответов на комментарии и выключает телефон.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Эфемерный телефон:</b> контейнер <code>budtmo/docker-android</code> поднимается по требованию, предотвращая детекцию фоновой активности и утечки памяти.</li>
  <li><b>Генерация карточек купонов:</b> библиотека PIL / нативный скриншот купона БК с расчетом гарантированного кэша 80% (на французском/английском языке).</li>
  <li><b>Мониторинг комментариев:</b> цикл ожидания ровно 15 минут после публикации. Извлечение текста новых комментариев через UIAutomator/ADB dump, генерация ответов через LLM и ввод текста.</li>
  <li><b>Выключение:</b> гарантированный <code>podman stop</code> после завершения цикла.</li>
</ul>
"""
    },
    {
        "key": "smm_threads",
        "title": "[smm-threads] Meta Threads: браузерный агент Camoufox (Gecko), прогрев кэша и публикация вилочных тредов",
        "priority": "high",
        "state": "Todo",
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Развернуть автономного браузерного ИИ-агента для публикации аналитических тредов в Meta Threads через движок Camoufox с соблюдением Правила 9 (Gecko Persistent Context, запрет чистого Chromium/Инкогнито).
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Движок:</b> <code>Camoufox / Firefox</code> с персистентным профилем в Redis / Volume.</li>
  <li><b>Прокси:</b> выделенный статический европейский IP (PureVPN NL: <code>100.97.93.196:3128</code>).</li>
  <li><b>Cache Warmup:</b> перед каждой публикацией обязательный 2-минутный серфинг ленты Threads для эмуляции живого пользователя.</li>
  <li><b>Формат постов:</b> треды из 2-3 сообщений (карточка матча -> перекрытие фрибета -> ссылка на калькулятор smartbet.guru).</li>
</ul>
"""
    },
    {
        "key": "smm_reddit",
        "title": "[smm-reddit] Reddit Crowd Marketing: прогрев кармы, сабреддиты r/sportsbook и r/matchedbetting",
        "priority": "high",
        "state": "Todo",
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Организовать крауд-маркетинг в англоязычных сабреддитах Reddit (r/sportsbook, r/Arbing, r/matchedbetting, r/valuebets) для привлечения органического зарубежного трафика на SmartBet.guru.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Прокси:</b> строго выделенный статический IP Лос-Анджелес, США (<code>purevpn-us-ca.proxy:3128</code>) по Правилу 12.</li>
  <li><b>Режим прогрева (Warm-up):</b> поддержание естественного поведения, набор кармы на нейтральных сабреддитах до перехода к целевым обсуждениям.</li>
  <li><b>Экспертные ответы:</b> математические разборы маржи букмекеров, формулы перекрытия фрибетов (80% кэша), органичные ответы на посты с вопросами о сканерах.</li>
</ul>
"""
    },
    {
        "key": "smm_identity",
        "title": "[smm-identity] Мультирегиональная фабрика аккаунтов: Catch-all домены, SMS-шлюз и Google/Gmail профили",
        "priority": "urgent",
        "state": "Todo",
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Создать инфраструктуру подготовки региональных учетных записей (США, Франция, Испания, Бразилия, Великобритания) для соцсетей, преодолевая ограничения SMS-верификации и антифрода.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Catch-all Email Gateway:</b> прием почты на собственный домен (например, <code>*@smartbet.guru</code>) с автоматическим парсингом верификационных ссылок и OTP-кодов через IMAP/Regex без риска блокировок почтовика.</li>
  <li><b>SMS Verification Gateway:</b> интеграция с API виртуальных номеров (SMS-Activate, Vak-SMS) для аренды чистых мобильных номеров целевых регионов (US, FR, ES, UK) под регистрацию Google/Gmail и WhatsApp/Telegram.</li>
  <li><b>Региональные профили Google:</b> подготовка готовых связок (IP + Профиль браузера/телефона + Учетная запись) для дальнейшей авторизации в YouTube, TikTok и Meta.</li>
</ul>
"""
    },
    {
        "key": "smm_multimedia",
        "title": "[smm-multimedia] Видео-конвейер YouTube Shorts & TikTok: генерация 9:16 видео, озвучка и автопубликация",
        "priority": "urgent",
        "state": "Todo",
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Подготовить инфраструктуру каналов и автоматической публикации вирусных вертикальных видео (9:16) для YouTube Shorts, TikTok и Instagram Reels с использованием больших нейросетевых моделей видео- и аудио-генерации.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Сборка динамических купонов:</b> скрипт захвата реальных мобильных коэффициентов (из Capture Phones) с динамической анимацией расчета выигрыша фрибета (80% кэша).</li>
  <li><b>Генерация аудио:</b> нативная мультиязычная озвучка инсайдов и вилок голосом (ElevenLabs / Edge-TTS) на английском, французском, испанском и русском языках.</li>
  <li><b>Интеграция с видео-моделями:</b> подключение API генерации фона и персонажей (Sora, Kling, Veo, Minimax) при активации доступа к большим моделям.</li>
  <li><b>Публикация:</b> автозагрузка видеофайлов в YouTube Shorts и TikTok через мобильный эмулятор или официальный Content API.</li>
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

def sync_tasks() -> dict:
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
from plane.db.models import Project, State, Issue, User, Module, ModuleIssue

p = Project.objects.filter(identifier='IGAMING').first() or Project.objects.first()
user = User.objects.filter(email='aleksei.a.chernousov@gmail.com').first() or User.objects.first()

states = {{s.name: s for s in State.objects.filter(project=p)}}
todo_state = states.get('Todo') or states.get('Backlog') or State.objects.filter(project=p, group='unstarted').first()

raw_data = base64.b64decode('{b64_payload}').decode('utf-8')
data = json.loads(raw_data)

# 1. Create or get Module
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

results = []

# 2. Create tasks
for t_def in data['tasks']:
    key = t_def['key']
    title = t_def['title']
    prio = t_def['priority']
    html_desc = t_def['html_desc']

    issue = Issue.objects.filter(project=p, name=title).first()
    is_new = False

    if not issue:
        issue = Issue.objects.create(
            project=p,
            workspace=p.workspace,
            name=title,
            description_html=html_desc,
            state=todo_state,
            created_by=user,
            updated_by=user,
            priority=prio
        )
        is_new = True
    else:
        issue.description_html = html_desc
        issue.priority = prio
        issue.save()

    # Link to Module
    ModuleIssue.objects.get_or_create(
        module=mod,
        issue=issue,
        project=p,
        workspace=p.workspace,
        defaults={{
            'created_by': user,
            'updated_by': user
        }}
    )

    results.append({{
        "key": key,
        "sequence_id": issue.sequence_id,
        "title": issue.name,
        "is_new": is_new,
        "state": issue.state.name if issue.state else None,
        "priority": issue.priority
    }})

output = {{
    "status": "success",
    "module": {{
        "id": str(mod.id),
        "name": mod.name
    }},
    "tasks": results
}}

print("__PLANE_SWARM__:" + json.dumps(output))
"""
    stdout, stderr = run_remote_django(django_code, timeout=120)
    for line in stdout.splitlines():
        if line.startswith("__PLANE_SWARM__:"):
            return json.loads(line.replace("__PLANE_SWARM__:", ""))
    print("Raw stdout:\n", stdout)
    if stderr:
        print("Raw stderr:\n", stderr)
    return {"status": "error", "output": stdout, "stderr": stderr}

def main():
    print("🚀 Synchronizing 7 Social Media Swarm tasks into Plane...")
    res = sync_tasks()
    if res.get("status") == "success":
        print("\n✅ Successfully updated Plane tasks!")
        print(f"📦 Module: {res['module']['name']}")
        print("\n=== CREATED TASKS ===")
        for t in res.get("tasks", []):
            status = "🆕 Created" if t.get("is_new") else "🔄 Updated"
            print(f"{status} #{t['sequence_id']:<4} | {t['state']:<12} | {t['priority']:<8} | {t['title']}")
    else:
        print("❌ Error during task creation:", res)

if __name__ == "__main__":
    main()
