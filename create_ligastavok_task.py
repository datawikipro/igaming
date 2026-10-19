import subprocess
import base64

remote_script = """
import os, sys, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue, User

p = Project.objects.filter(identifier='IGAMING').first() or Project.objects.first()
print(f"Project: {p.name} ({p.identifier})")

states = State.objects.filter(project=p)
print("Available states:", [(s.name, s.group) for s in states])

blocked_state = State.objects.filter(project=p, name__icontains='Блок').first()
if not blocked_state:
    blocked_state = State.objects.filter(project=p, name__icontains='Backlog').first()
if not blocked_state:
    blocked_state = State.objects.filter(project=p, group='backlog').first()
if not blocked_state:
    blocked_state = State.objects.filter(project=p, group='unstarted').first()

print(f"Target state: {blocked_state.name if blocked_state else None}")

user = User.objects.first()

title = '[ligastavok] Блокировка Qrator Anti-Bot по белому IP (Tag: QAB) — требуется чистый RU прокси'
desc = '''<h3>Описание проблемы</h3>
<p>Краулер <b>igaming-source-ligastavok</b> имеет 0 активных матчей в <code>match_cache</code>. При обращении к ресурсу <code>https://www.ligastavok.ru/live</code> периметр защиты <b>Qrator</b> возвращает блокировку доступа:</p>
<blockquote>
<p><b>Доступ заблокирован системой защиты</b><br>
User IP: 188.242.33.93<br>
Host: www.ligastavok.ru<br>
Tag: <b>QAB (Qrator Anti-Bot)</b></p>
</blockquote>

<h3>Контекст инфраструктуры</h3>
<ul>
  <li>Внешний адрес узла <code>xeon-srv</code> (СПб) — белый статический IP <code>188.242.33.93</code>, на котором размещены публичные сервисы платформы, поэтому сброс/смена IP через перезагрузку роутера невозможна.</li>
  <li>Зарубежные прокси (DE, NL, US, HK) не подходят из-за жесткого гео-ограничения Лиги Ставок (разрешен только трафик из РФ).</li>
  <li>Мобильный SIM-шлюз Xiaomi 12T Pro временно не находится дома.</li>
</ul>

<h3>Что требуется для решения</h3>
<ol>
  <li>Подключить чистый российский резидентный или мобильный 4G/LTE прокси (не заблокированный в базе Qrator).</li>
  <li>Зарегистрировать прокси в <code>service-proxy-backend</code> в пуле региона <code>ru</code>.</li>
  <li>Переключить конфигурацию <code>igaming-source-ligastavok</code> на использование этого прокси (<code>APP_VPN_ENABLED=true</code>, <code>APP_PROXY_DIRECT=false</code>).</li>
  <li>Достичь критерия приемки: <b>&ge; 500 активных матчей</b> в <code>match_cache</code>.</li>
</ol>'''

existing = Issue.objects.filter(project=p, name__icontains='ligastavok').first()
if existing:
    existing.name = title
    existing.description_html = desc
    if blocked_state:
        existing.state = blocked_state
    existing.priority = 'high'
    existing.save()
    print(f"🔄 Updated existing Issue #{existing.sequence_id}: '{existing.name}' -> State: '{existing.state.name}'")
else:
    issue = Issue.objects.create(
        project=p,
        workspace=p.workspace,
        name=title,
        description_html=desc,
        state=blocked_state,
        created_by=user,
        updated_by=user,
        priority='high'
    )
    print(f"✨ Created Issue #{issue.sequence_id}: '{issue.name}' -> State: '{blocked_state.name if blocked_state else None}'")
"""

b64 = base64.b64encode(remote_script.strip().encode('utf-8')).decode('ascii')
cmd = f'echo {b64} | base64 -d > /tmp/manage_plane_ligastavok.py && kubectl cp /tmp/manage_plane_ligastavok.py plane/$(kubectl get pods -n plane -l app=plane-api -o jsonpath="{{.items[0].metadata.name}}"):/tmp/manage_plane_ligastavok.py && kubectl exec -n plane deploy/plane-api -- python3 /tmp/manage_plane_ligastavok.py'

ssh_cmd = ["ssh", "-n", "-o", "StrictHostKeyChecking=no", "root@100.78.183.101", cmd]
p = subprocess.run(ssh_cmd, capture_output=True, text=True)
print("STDOUT:", p.stdout)
if p.stderr:
    print("STDERR:", p.stderr)
