import subprocess
import json
import base64
import sys

sys.stdout.reconfigure(encoding='utf-8')

PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"
WORKSPACE_SLUG = "dataplatform"

TASKS = [
    {
        "bk": "betano",
        "title": "[betano] Отремонтировать отправку котировок в агрегатор (Odds Ingestion & Aggregator Link)",
        "desc": "Кэш событий заполнен (1000+ матчей в match_cache), но котировки не поступают в aggregator (odds_actual = 0). Проверить MatchService, AggregatorClient, URL агрегатора и формат батч-запроса котировок. Критерий выполнения (DoD): появление betano в odds_actual с >= 500 активных матчей и непрерывный стрим."
    },
    {
        "bk": "ligastavok",
        "title": "[ligastavok] Завершить интеграцию Playwright и наполнение линии (target >= 500 matches)",
        "desc": "Эндпоинты обновлены на /live и /sports, баг инициализации VPN в VpnManagerService устранен. Запустить сбор линии через Playwright и WebSocket перехватчик, наполнить match_cache >= 500 матчей и наладить отправку в агрегатор. Критерий выполнения: >= 500 матчей в match_cache и поток в odds_actual."
    },
    {
        "bk": "marathonbet-by",
        "title": "[marathonbet-by] Подключить Playwright volume mount и восстановить сбор котировок",
        "desc": "В логах ошибка 'Executable doesn't exist at /root/.cache/ms-playwright/chromium'. Добавить монтирование /data/playwright-browsers в deployment igaming-source-marathonbet-by, пересобрать образ на базе 100.78.183.101:30500/playwright-java:21 и наполнить match_cache >= 500 матчей."
    },
    {
        "bk": "fon-bet-by",
        "title": "[fon-bet-by] Исправить имя БД PostgreSQL и восстановить инжест (target >= 500 matches)",
        "desc": "В k8s deployment и application.properties указана БД igaming_fon_bet_by, тогда как в PostgreSQL база называется igaming_fonbet_by. Исправить имя БД, проверить сбор линии и наполнить match_cache >= 500 матчей."
    },
    {
        "bk": "fon-bet-kz",
        "title": "[fon-bet-kz] Исправить имя БД PostgreSQL и восстановить инжест (target >= 500 matches)",
        "desc": "В k8s deployment и application.properties указана БД igaming_fon_bet_kz, а в PostgreSQL создана igaming_fonbet_kz. Исправить имя БД, перезапустить сбор и наполнить match_cache >= 500 матчей."
    },
    {
        "bk": "pinnacle",
        "title": "[pinnacle] Маршрутизация через PureVPN DE и обход Cloudflare 403",
        "desc": "Прямые запросы к pinnacle.com блокируются Cloudflare (403). Настроить роутинг через выделенный немецкий прокси k8s-purevpn-de (или outline-vpn-eu-nl) в sing-box / VpnManagerService, проверить стабильный сбор линии и наполнить match_cache >= 500 матчей."
    },
    {
        "bk": "leon",
        "title": "[leon] Обновить API эндпоинты betline и восстановить парсинг котировок",
        "desc": "Запросы к старым эндпоинтам /api-2/betline возвращают 400 INVALID_CODE. Проанализировать актуальные сетевые вызовы современного фронтенда leon.ru, обновить LeonApiClient на рабочий эндпоинт, наполнить match_cache >= 500 матчей."
    },
    {
        "bk": "sbobet",
        "title": "[sbobet] Восстановить парсер секций после снятия таймаута",
        "desc": "В BrowserExecutor снят 25-секундный детективный таймаут. Проверить перехват $P.onUpdate('od', ...) и DOM-структуру секций, обеспечить непрерывный поток котировок в агрегатор."
    },
    {
        "bk": "1xbit",
        "title": "[1xbit] Обновить зеркало BetB2B и запустить сбор линии",
        "desc": "Букмекер на платформе BetB2B. Обновить домен зеркала и API эндпоинты в application.properties, обеспечить обход геоблокировок и наполнить match_cache >= 500 матчей."
    },
    {
        "bk": "melbet-com",
        "title": "[melbet-com] Обновить зеркало BetB2B и запустить сбор линии",
        "desc": "Международная версия Melbet на платформе BetB2B. Обновить активные зеркала API, проверить парсинг коэффициентов и наполнить match_cache >= 500 матчей."
    }
]

tasks_json = json.dumps(TASKS, ensure_ascii=False)

remote_django = f"""
import os, sys, json, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue, User, Module, ModuleIssue

p = Project.objects.filter(identifier='IGAMING').first()
ai_state = State.objects.filter(project=p, name='AI разработка').first()
crawler_module = Module.objects.filter(project=p, name__icontains='Crawler').first()
user = User.objects.first()

created_issues = []
tasks = json.loads('''{tasks_json}''')

for t in tasks:
    title = t['title']
    desc = t['desc']
    bk = t['bk']
    
    iss = Issue.objects.filter(project=p, name__icontains=f'[{{bk}}]').first()
    if not iss:
        iss = Issue.objects.create(
            project=p,
            workspace=p.workspace,
            name=title,
            description_html=f'<p>{{desc}}</p>',
            state=ai_state,
            created_by=user,
            updated_by=user,
            priority='urgent'
        )
        print(f'✨ Created Issue #{{iss.sequence_id}}: "{{iss.name}}" (ID: {{iss.id}})')
    else:
        iss.name = title
        iss.description_html = f'<p>{{desc}}</p>'
        iss.state = ai_state
        iss.save()
        print(f'🔄 Updated Issue #{{iss.sequence_id}}: "{{iss.name}}" (ID: {{iss.id}})')
        
    if crawler_module:
        ModuleIssue.objects.get_or_create(module=crawler_module, issue=iss, project=p)
        
    created_issues.append((iss.sequence_id, str(iss.id)))

print("CREATED_JSON:" + json.dumps(created_issues))
"""

b64 = base64.b64encode(remote_django.strip().encode('utf-8')).decode('ascii')
cmd = f"echo {b64} | base64 -d > /tmp/create_plane_tasks.py && kubectl cp /tmp/create_plane_tasks.py plane/$(kubectl get pods -n plane -l app=plane-api -o jsonpath='{{.items[0].metadata.name}}'):/tmp/create_plane_tasks.py && kubectl exec -n plane deploy/plane-api -- python3 /tmp/create_plane_tasks.py"

ssh_cmd = ["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", cmd]
res = subprocess.run(ssh_cmd, capture_output=True, text=True, timeout=60, encoding='utf-8')
print("STDOUT:\n", res.stdout)
if res.stderr:
    print("STDERR:\n", res.stderr)

# Parse output and trigger dispatch
issues = []
for line in res.stdout.splitlines():
    if line.startswith("CREATED_JSON:"):
        issues = json.loads(line[len("CREATED_JSON:"):])

if issues:
    print(f"\nDispatching {len(issues)} issues to plane-ai-executor webhook...", flush=True)
    dispatch_code = f"""
import json, urllib.request

issues = {issues}
for seq_id, iss_id in issues:
    payload = {{
        "event": "issue_activity",
        "action": "updated",
        "data": {{
            "field": "state",
            "new_value": "AI разработка",
            "issue": iss_id,
            "project": "{PROJECT_ID}",
            "workspace": "{WORKSPACE_SLUG}"
        }}
    }}
    req = urllib.request.Request(
        "http://127.0.0.1:30885/webhook/plane",
        data=json.dumps(payload).encode('utf-8'),
        headers={{'Content-Type': 'application/json'}},
        method='POST'
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as r:
            print(f"Dispatched #{{seq_id}} ({{iss_id}}): HTTP {{r.status}} -> {{r.read().decode('utf-8')}}")
    except Exception as e:
        print(f"Dispatch error #{{seq_id}}: {{e}}")
"""
    cmd_disp = ["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", "python3 -"]
    res_disp = subprocess.run(cmd_disp, input=dispatch_code, capture_output=True, text=True, timeout=60, encoding='utf-8')
    print("DISPATCH OUTPUT:\n", res_disp.stdout)
    if res_disp.stderr:
        print("DISPATCH STDERR:\n", res_disp.stderr)
