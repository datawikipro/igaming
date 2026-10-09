#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
create_national_teams_plane_tasks.py

Creates structured Plane tasks for:
1. [team-national-model] Aggregator/Core: Data model & API for national teams (team_type, is_national_team, country_code, flag_url)
2. [team-national-detect] Enrichment/Normalizer: Auto-detection of national teams, ISO country matching, and DB backfill
3. [frontend-team-flags] Frontend: TeamAvatar component with SVG country flags for national teams across all match cards
4. [country-analytics-seo] Analytics/SEO: National team dossiers, country stats, and automated match preview articles
5. [player-faces-tennis] Data/UI (Deferred/Low): Player portraits & headshots for tennis and individual sports
"""

import json
import os
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

MODULE_NAME = "Aggregator Core & Arbitrage Engine"
FRONTEND_MODULE_NAME = "Frontend & UI/UX (SmartBet.guru)"
GROWTH_MODULE_NAME = "AI Growth & SMM Swarm (SmartBet.guru)"

TASKS_DEF = [
    # ── Task 1: Backend Data Model ──────────────────────────────────────────
    {
        "key": "team_national_model",
        "title": "[team-national-model] Модель данных и API: поддержка национальных сборных (team_type, is_national_team, country_code, flag_url)",
        "priority": "urgent",
        "state": "Todo",
        "module_name": MODULE_NAME,
        "blocked_by": [],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Расширить реляционную модель данных команд в <code>aggregator-domain</code>, <code>igaming-dto</code> и шлюзе <code>igaming-portal</code>
для явного разделения участников на <b>Клубы (CLUB)</b>, <b>Национальные сборные (NATIONAL_TEAM)</b> и <b>Спортсменов-одиночников (INDIVIDUAL)</b>.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Расширение сущности <code>Team</code>:</b>
    <ul>
      <li><code>TeamType teamType</code> (enum: <code>CLUB</code>, <code>NATIONAL_TEAM</code>, <code>INDIVIDUAL</code>, default: <code>CLUB</code>).</li>
      <li><code>Boolean isNationalTeam</code> (индексированный флаг для быстрой фильтрации).</li>
      <li><code>String countryCode</code> (двухбуквенный ISO 3166-1 alpha-2 код страны: ZM, TG, FR, DE, BR, US и др.).</li>
      <li><code>String flagUrl</code> (ссылка на векторный SVG или высококачественный PNG флага).</li>
      <li>Связь с таблицей <code>Country</code> (через <code>country_id</code>).</li>
    </ul>
  </li>
  <li><b>DTO & Serialization (<code>igaming-dto</code>):</b>
    Добавление полей <code>is_national_team</code>, <code>team_type</code>, <code>country_code</code>, <code>flag_url</code> в <code>TeamDto</code>, <code>MatchDto</code>, <code>TeamProfileDto</code>.
  </li>
  <li><b>REST API (<code>aggregator-api</code> & <code>igaming-portal</code>):</b>
    Проброс метаданных сборных в эндпоинтах <code>/api/v1/matches/{id}</code>, <code>/api/v1/matches</code>, <code>/api/v1/teams/{id}</code> и выдаче арбитражных ситуаций (вилок/коридоров).
  </li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Модули <code>igaming-dto</code>, <code>aggregator-domain</code>, <code>aggregator-api</code>, <code>igaming-portal</code> компилируются без ошибок.</li>
  <li>Поля успешно сериализуются в JSON-ответах для матчей вида Замбия — Того.</li>
</ul>
"""
    },

    # ── Task 2: Auto-Detection & Backfill ─────────────────────────────────────
    {
        "key": "team_national_detect",
        "title": "[team-national-detect] Автоматическое распознавание национальных сборных, связывание с ISO-кодами стран и бекфилл БД",
        "priority": "high",
        "state": "Todo",
        "module_name": MODULE_NAME,
        "blocked_by": ["team_national_model"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Реализовать интеллектуальный механизм классификации команд как национальных сборных на этапе нормализации и обогащения,
а также провести разовый бекфилл по существующей базе команд в PostgreSQL.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Контекстное распознавание по турнирам (Лигам):</b>
    Турниры категорий <i>International / National Teams</i> (Кубок Африканских Наций, ЧМ, Лига Наций УЕФА, Кубок Америки, Евро, квалификации и товарищеские матчи сборных) автоматически помечают участников как <code>NATIONAL_TEAM</code>.
  </li>
  <li><b>Словарный маппинг в <code>TeamNormalizationHandler</code>:</b>
    Сверка названий ("Замбия" / "Zambia" -> ZM, "Того" / "Togo" -> TG, "Кот-д'Ивуар" / "Ivory Coast" -> CI) со справочником ISO 3166-1 (включая футбольные ассоциации Англии, Шотландии, Уэльса, Сев. Ирландии).
  </li>
  <li><b>LLM Prompt Enhancement:</b>
    В системный промпт нормализации команд добавить возврат полей <code>is_national_team: true/false</code> и <code>country_iso: "XX"</code>.
  </li>
  <li><b>Backfill Migration Job:</b>
    SQL/Java утилита для единовременной разметки существующих ~50k команд в таблице <code>team</code> с установкой <code>is_national_team=true</code> и соответствующего <code>country_id</code>.
  </li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Новые матчи сборных из краулеров автоматически сохраняются с <code>is_national_team = true</code>.</li>
  <li>Бекфилл успешно размечает существующие сборные (Замбия, Того, Франция и т.д.) в базе <code>igaming</code>.</li>
</ul>
"""
    },

    # ── Task 3: Frontend TeamAvatar Component ─────────────────────────────────
    {
        "key": "frontend_team_flags",
        "title": "[frontend-team-flags] Компонент TeamAvatar с поддержкой SVG-флагов для сборных на карточках матчей и вилок",
        "priority": "high",
        "state": "Todo",
        "module_name": FRONTEND_MODULE_NAME,
        "blocked_by": ["team_national_model"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Устранить показ безликих буквенных аватарок (буква «З» для Замбии, «Т» для Того) на фронтенде <code>smartbet.guru</code>
путем внедрения единого адаптивного компонента <code>TeamAvatar</code> с поддержкой флагов государств.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Новый компонент <code>src/components/common/TeamAvatar.tsx</code>:</b>
    <ul>
      <li><b>Уровень 1:</b> Официальный логотип клуба или эмблема федерации (<code>team.logo_url</code>), если он доступен.</li>
      <li><b>Уровень 2 (Флаг страны):</b> Если команда является сборной (<code>is_national_team</code>) или передан <code>country_code</code> — отрисовка четкого SVG-флага через локальный векторный сет или FlagCDN (<code>https://flagcdn.com/w80/{code}.png</code> / SVG) с аккуратным скруглением, рамкой и тенью.</li>
      <li><b>Уровень 3:</b> Силуэт атлета для индивидуальных видов спорта (теннис, бокс, ММА).</li>
      <li><b>Уровень 4 (Fallback):</b> Буквенный Orbitron-плейсхолдер только при полном отсутствии метаданных.</li>
    </ul>
  </li>
  <li><b>Интеграция по всему фронтенду:</b>
    <ul>
      <li>Заголовок карточки вилки <code>ArbCardHeader.tsx</code>.</li>
      <li>Модалка прямого перехода <code>DirectMatchCardsModal.tsx</code>.</li>
      <li>Страница матча <code>MatchDetailClient.tsx</code> (замена текущего <code>match-team-logo-placeholder</code>).</li>
      <li>Карусель топовых вилок <code>TopArbsCarousel.tsx</code>.</li>
      <li>Страница профиля команды <code>TeamDetailClient.tsx</code>.</li>
    </ul>
  </li>
  <li><b>Эстетика:</b> Стилизация под киберпанк/неон тему SmartBet (соотношение сторон 4:3 или круглая миниатюра с тонким бордером <code>var(--border-subtle)</code>).</li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>На карточке матча «Замбия — Того» отображаются флаги Замбии 🇿🇲 и Того 🇹🇬 вместо букв «З» и «Т».</li>
  <li>Клубные матчи (Реал — Барселона) сохраняют отображение клубных логотипов.</li>
</ul>
"""
    },

    # ── Task 4: Analytics, Country Hubs & SEO Content ────────────────────────
    {
        "key": "country_analytics_seo",
        "title": "[country-analytics-seo] Досье национальных сборных: профили стран, турнирная статистика и генерация аналитических статей",
        "priority": "medium",
        "state": "Backlog",
        "module_name": GROWTH_MODULE_NAME,
        "blocked_by": ["frontend_team_flags", "team_national_detect"],
        "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Использовать атрибуты национальных сборных для создания глубоких аналитических хабов по странам и автоматической генерации экспертных SEO-статей и SMM-постов к матчам сборных (Кубок Африки, ЧМ, Евро).
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Страницы досье сборных (<code>/[locale]/teams/[slug]</code> или <code>/[locale]/countries/[iso]</code>):</b>
    Флаг страны, гимн, конфедерация (CAF, UEFA, CONMEBOL, CONCACAF, AFC, OFC), текущий рейтинг FIFA/FIBA, список соревнований, исторический H2H против соперников по региону.
  </li>
  <li><b>Статистика ставок на сборные:</b>
    Агрегация специфических статистических трендов национальных сборных: низовые/верховые матчи в квалификациях, средние тоталы угловых и ЖК в турнирах сборных в сравнении с клубным футболом.
  </li>
  <li><b>Автоматический контент-генератор к международным паузам (International Breaks):</b>
    Интеграция с ИИ-пайплайном публикаций (Telegram, Threads, блог) для генерации превью матчей сборных с фокусом на математические вилки и перекосы линий букмекеров.
  </li>
</ul>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Страница сборной содержит обогащенное досье с флагом и характеристиками конфедерации.</li>
  <li>ИИ-генератор статей формирует качественные превью к международным матчам.</li>
</ul>
"""
    },

    # ── Task 5: Player Faces for Tennis (Deferred / Low Priority) ─────────────
    {
        "key": "player_faces_tennis",
        "title": "[player-faces-tennis] [Теннис/Единоборства] Сбор портретов спортсменов (Player Face Avatars) и отображение в карточках одиночных матчей",
        "priority": "low",
        "state": "Backlog",
        "module_name": FRONTEND_MODULE_NAME,
        "blocked_by": [],
        "html_desc": """
<h2>🎯 Цель задачи (Отложенная / Low Priority)</h2>
<p>
Для одиночных видов спорта (теннис ATP/WTA, бокс, MMA UFC) внедрить сбор и отображение реальных портретов/лиц спортсменов
вместо абстрактных иконок или буквенных инициалов.
</p>

<h3>🔍 Архитектурные требования</h3>
<ul>
  <li><b>Сбор портретов (Player Headshots):</b>
    Интеграция со свободными источниками фотографий спортсменов (Wikidata, Wikimedia Commons API, TheSportsDB Player API).
  </li>
  <li><b>Кэширование и CDN:</b>
    Сжатие и оптимизация аватарок в WebP с хранением на MinIO/S3 или локальном кластерном CDN.
  </li>
  <li><b>Специализированный UI для тенниса:</b>
    Карточка матча отображает лица теннисистов (например, Джокович vs Алькарас), номер посева в сетке турнира и флаг страны гражданства.
  </li>
</ul>

<h3>🛡️ Примечание</h3>
<p><i>Задача отложена в беклог с низким приоритетом по запросу бизнеса. Реализация запланирована после завершения функционала по сборным и флагам стран.</i></p>
"""
    }
]


def run_remote_django(code: str, timeout: int = 40):
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
        "tasks": TASKS_DEF
    }
    json_data = json.dumps(payload, ensure_ascii=False)

    django_code = """
import os, sys, json, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()

from plane.db.models import Project, State, Issue, User, Module, ModuleIssue, IssueBlocker

data = json.loads(sys.stdin.read())

p = Project.objects.filter(identifier='IGAMING').first()
if not p:
    p = Project.objects.first()

user = User.objects.first()

states_cache = {s.name.lower(): s for s in State.objects.filter(project=p)}
modules_cache = {m.name.lower(): m for m in Module.objects.filter(project=p)}

def get_state(name):
    if not name:
        return State.objects.filter(project=p, group='unstarted').first()
    key = name.lower()
    if key in states_cache:
        return states_cache[key]
    for s_name, s in states_cache.items():
        if key in s_name or s_name in key:
            return s
    return State.objects.filter(project=p, group='unstarted').first()

def get_module(mod_name):
    if not mod_name:
        return None
    key = mod_name.lower()
    if key in modules_cache:
        return modules_cache[key]
    for m_name, m in modules_cache.items():
        if key in m_name or m_name in key:
            return m
    return None

results = []
task_key_to_issue = {}

# 1. Create or update issues
for t_def in data['tasks']:
    key = t_def['key']
    title = t_def['title']
    target_state = get_state(t_def.get('state', 'Todo'))
    priority = t_def.get('priority', 'medium')
    module = get_module(t_def.get('module_name'))

    issue = Issue.objects.filter(project=p, name=title).first()
    if not issue:
        short_key = f"[{key}]"
        issue = Issue.objects.filter(project=p, name__istartswith=short_key).first()

    is_new = False
    if not issue:
        issue = Issue.objects.create(
            project=p,
            workspace=p.workspace,
            name=title,
            description_html=t_def['html_desc'],
            state=target_state,
            priority=priority,
            created_by=user,
            updated_by=user
        )
        is_new = True
    else:
        issue.description_html = t_def['html_desc']
        issue.priority = priority
        if target_state:
            issue.state = target_state
        issue.save()

    task_key_to_issue[key] = issue

    if module:
        ModuleIssue.objects.get_or_create(
            module=module,
            issue=issue,
            project=p,
            workspace=p.workspace,
            defaults={'created_by': user, 'updated_by': user}
        )

    results.append({
        "key": key,
        "sequence_id": issue.sequence_id,
        "id": str(issue.id),
        "title": issue.name,
        "is_new": is_new,
        "state": issue.state.name if issue.state else None,
        "priority": issue.priority,
        "module": module.name if module else None,
        "blocked_by": t_def.get('blocked_by', [])
    })

# 2. Setup IssueBlocker dependencies
dependencies_count = 0
for t_def in data['tasks']:
    dependent_key = t_def['key']
    dependent_issue = task_key_to_issue.get(dependent_key)
    for blocking_key in t_def.get('blocked_by', []):
        blocking_issue = task_key_to_issue.get(blocking_key)
        if dependent_issue and blocking_issue:
            blocker, b_created = IssueBlocker.objects.get_or_create(
                project=p,
                workspace=p.workspace,
                block=dependent_issue,
                blocked_by=blocking_issue,
                defaults={
                    'created_by': user,
                    'updated_by': user
                }
            )
            if b_created:
                dependencies_count += 1

output = {
    "status": "success",
    "tasks": results,
    "new_dependencies_count": dependencies_count
}

print("__PLANE_TASKS_SYNC__:" + json.dumps(output))
"""
    # Notice: we can run django script with data piped into stdin or encoded
    import base64
    b64_script = base64.b64encode(django_code.strip().encode('utf-8')).decode('ascii')
    b64_data = base64.b64encode(json_data.encode('utf-8')).decode('ascii')

    remote_runner = f"""
import subprocess, base64
script = base64.b64decode('{b64_script}').decode('utf-8')
data = base64.b64decode('{b64_data}').decode('utf-8')
p = subprocess.run(['kubectl', 'exec', '-i', '-n', 'plane', 'deploy/plane-api', '--', 'python3', '-'], input=script + '\\n#DATA_START\\n', text=True, capture_output=True)
"""
    # Even simpler: run ssh with python script on local subprocess passing data into stdin of remote python
    cmd = [
        "ssh", "-i", SSH_KEY,
        "-o", "BatchMode=yes",
        "-o", "ConnectTimeout=10",
        SSH_HOST,
        f"echo {b64_data} | base64 -d | kubectl exec -i -n plane deploy/plane-api -- python3 -c \"$(echo {b64_script} | base64 -d)\""
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=60, encoding="utf-8")
    stdout, stderr = res.stdout, res.stderr
    for line in stdout.splitlines():
        if line.startswith("__PLANE_TASKS_SYNC__:"):
            return json.loads(line.replace("__PLANE_TASKS_SYNC__:", ""))
    return {"status": "error", "output": stdout, "stderr": stderr}


def main():
    print("🚀 Synchronizing National Teams & Country Flags tasks into Plane...")
    res = sync_tasks()
    if res.get("status") == "success":
        print("\n✅ Successfully created Plane tasks!")
        print(f"🔗 Dependencies created: {res.get('new_dependencies_count', 0)}")
        print("\n=== TASKS SUMMARY ===")
        for t in res.get("tasks", []):
            status = "🆕 Created" if t.get("is_new") else "🔄 Updated"
            deps = f" [Blocked by: {', '.join(t['blocked_by'])}]" if t['blocked_by'] else " [NO DEPS -> READY]"
            print(f"{status} #{t['sequence_id']:<4} | {t['priority']:<7} | {t['state']:<10} | {t['module'] or 'General':<35} | {t['title']}{deps}")
    else:
        print("❌ Error during task creation:", res)


if __name__ == "__main__":
    main()
