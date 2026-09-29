#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
add_freebet_calc_and_posts_task.py

1. Updates #56, #58, #60 in Plane with the 80% Guaranteed Freebet Cash Conversion formula & marketing angle.
2. Creates #62: [freebet-calc] Отдельный калькулятор конвертации фрибетов в 80% гарантированных денег (/tools/freebet-calculator), промо-гайд на сайте и серия постов в соцсетях.
"""

import base64
import json
import subprocess
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

FREEBET_TASK = {
    "key": "freebet_calc",
    "title": "[freebet-calc] Отдельный калькулятор конвертации фрибетов в 80% гарантированных денег (/tools/freebet-calculator), гайд на сайте и анонс-посты",
    "priority": "urgent",
    "html_desc": """
<h2>🎯 Цель задачи</h2>
<p>
Создать на сайте <b>SmartBet.guru</b> отдельный интерактивный <b>Калькулятор конвертации фрибетов (Matched Betting / SNR Calculator)</b>
на роуте <code>/[locale]/tools/freebet-calculator</code>, доказывающий математически и позволяющий в 1 клик превратить любой фрибет букмекера в <b>~80% гарантированных живых денег</b> независимо от исхода матча.
Также разместить заметный анонс на главной странице, обучающий гайд в блоге и опубликовать серию вирусных постов о формуле 80% кэша.
</p>

<h3>🧮 Математическое ядро (Формула 80% гарантированного кэша)</h3>
<ul>
  <li>Для сгораемого фрибета (SNR — Stake Not Returned) номиналом <code>F</code> на высоком коэффициенте <code>K1</code> (оптимально 4.50–6.00) и перекрывающего плеча <code>K2</code> (1.20–1.28):
    <ul>
      <li><b>Сумма перекрытия (хедж-ставка в БК №2):</b> <code>S2 = F * (K1 - 1) / K2</code></li>
      <li><b>Гарантированная чистая прибыль при ЛЮБОМ исходе:</b> <code>Profit = S2 * (K2 - 1) = F * (K1 - 1) * (K2 - 1) / K2</code></li>
      <li><b>Коэффициент конвертации (Retention Rate):</b> <code>eta = ((K1 - 1) * (K2 - 1) / K2) * 100%</code> (при K1=5.0 и K2=1.25 дает ровно <b>80.0%</b> гарантированных денег: фрибет 3 000 ₽ = 2 400 ₽ чистыми на карту!).</li>
    </ul>
  </li>
</ul>

<h3>🔍 Архитектурные и продуктовые требования</h3>
<ol>
  <li><b>Страница <code>src/app/[locale]/tools/freebet-calculator/page.tsx</code>:</b>
    <ul>
      <li>Поля ввода: Номинал фрибета (быстрые пресеты: 1 000, 3 000, 5 000, 10 000 ₽ / $50, $100), режим <code>SNR (Сгораемый фрибет)</code> / <code>SR (Несгораемый)</code>, Коэффициент под фрибет <code>K1</code>, Коэффициент перекрытия <code>K2</code>, Комиссия биржи/БК.</li>
      <li>Визуальная карточка результата: крупный бейдж <b>«Гарантированная выплата: 2 400 ₽ (80.0% от фрибета)»</b>, точная сумма хедж-ставки <code>S2</code>, таблица сценариев (Исход 1 победил vs Исход 2 победил — одинаковый чистый профит!).</li>
      <li><b>Deep-linking через URL:</b> поддержка параметров <code>?freebet=3000&k1=5.0&k2=1.25&bookie=winline</code> для расшаривания готовых расчетов в Telegram/Threads/Reddit.</li>
      <li><b>Таблица актуальных фрибетов БК с пересчетом в 80% кэша:</b> блок «Забери фрибет и выведи 80% чистыми» (например: Winline 3 000 ₽ -> 2 400 ₽ на карту, Fonbet 2 000 ₽ -> 1 600 ₽ на карту, Пари 5 000 ₽ -> 4 000 ₽ на карту) с кнопками «Забрать фрибет» (через редирект <code>/go/[bookmaker]</code>) и «Рассчитать в калькуляторе».</li>
    </ul>
  </li>
  <li><b>Продвижение на сайте (Дать знать пользователям):</b>
    <ul>
      <li>Добавить яркую ссылку/бейдж <b>«🎁 Калькулятор фрибетов (80% в кэш)»</b> в навигацию / хедер и промо-баннер на главной странице и в обычном калькуляторе <code>/tools/calculator</code>.</li>
      <li>Добавить подробный SEO-гайд / статью в блог: <i>«Как гарантированно вывести 80% от любого фрибета букмекера: математика перекрытия без риска»</i>.</li>
      <li>Локализация: полная поддержка <code>messages/ru.json</code> и <code>messages/en.json</code>, Schema.org <code>SoftwareApplication</code> и <code>FAQPage</code>.</li>
    </ul>
  </li>
  <li><b>Генерация и публикация анонс-постов для соцсетей:</b>
    <ul>
      <li>Подготовить и запустить генератор постов (Telegram, Threads, Instagram, Reddit) с разбором конкретных примеров: как новички теряют 100% фрибета на экспрессах, а профи через калькулятор фрибетов SmartBet.guru забирают 80% живыми деньгами за 2 минуты.</li>
    </ul>
  </li>
</ol>

<h3>🛡️ Definition of Done</h3>
<ul>
  <li>Страница <code>/[locale]/tools/freebet-calculator</code> работает, проходит <code>npm run build</code> и задеплоена в <code>smartbet-guru</code> (namespace <code>igaming-dev</code>).</li>
  <li>На главной странице и в меню есть заметный переход на калькулятор фрибетов.</li>
  <li>Опубликован анонс-пост в Telegram-канале и подготовлены шаблоны для Threads/Reddit с глубокими ссылками на <code>/tools/freebet-calculator?freebet=3000&k1=5.0&k2=1.25</code>.</li>
  <li>Под <code>smartbet-guru</code> отработал 5 полных минут без ошибок.</li>
</ul>
""",
    "description_stripped": """[freebet-calc] Отдельный калькулятор конвертации фрибетов в 80% гарантированных денег (/tools/freebet-calculator), гайд на сайте и анонс-посты.
1. Создать страницу src/app/[locale]/tools/freebet-calculator/page.tsx (расчет SNR/SR фрибета: S2 = F*(K1-1)/K2, Profit = F*(K1-1)*(K2-1)/K2 ~ 80% гарантированного кэша).
2. Добавить таблицу фрибетов БК с пересчетом в 80% гарантированных денег на карту и партнерскими ссылками /go/[bookmaker].
3. Добавить промо-баннер и ссылку на калькулятор фрибетов на главную страницу, в хедер и в /tools/calculator, а также статью-гайд.
4. Добавить переводы в messages/ru.json и messages/en.json, собрать и задеплоить smartbet-guru в igaming-dev."""
}


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


def main():
    b64_payload = base64.b64encode(json.dumps(FREEBET_TASK, ensure_ascii=False).encode("utf-8")).decode("ascii")

    django_code = f"""
import os, sys, json, base64, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'plane.settings.production')
sys.path.insert(0, '/code')
django.setup()
from plane.db.models import Project, State, Issue, User, Module, ModuleIssue

p = Project.objects.filter(identifier='IGAMING').first() or Project.objects.first()
user = User.objects.filter(email='aleksei.a.chernousov@gmail.com').first() or User.objects.first()
states = {{s.name: s for s in State.objects.filter(project=p)}}
ai_dev_state = states.get('AI разработка') or State.objects.filter(project=p, group='started').first()

t_def = json.loads(base64.b64decode('{b64_payload}').decode('utf-8'))

# Append 80% freebet rule to #56, #58, #60
addon_html = "<hr><h4>💰 Правило 80% гарантированного кэша с каждого фрибета</h4><p>В каждом посте об акциях и на витрине промо обязательно оперировать математическим фактом: <b>любой фрибет через калькулятор перекрытия SmartBet.guru (/tools/freebet-calculator) — это 80% гарантированных живых денег на карту при любом исходе матча</b> (например, фрибет 3 000 ₽ = 2 400 ₽ чистыми деньгами по формуле <code>F * (K1 - 1) * (K2 - 1) / K2</code>).</p>"
addon_text = "\\nВАЖНО: Каждый фрибет = 80% гарантированных живых денег через вилочное перекрытие (/tools/freebet-calculator). Обязательно указывать сумму гарантированного вывода (например, фрибет 3000₽ -> 2400₽ чистыми на карту)!"

for seq in [56, 58, 60, 61]:
    iss = Issue.objects.filter(project=p, sequence_id=seq).first()
    if iss and "80% гарантированного кэша" not in (iss.description_html or ""):
        iss.description_html = (iss.description_html or "") + addon_html
        iss.description_stripped = (iss.description_stripped or "") + addon_text
        iss.save()

existing = Issue.objects.filter(project=p, name__icontains='[freebet-calc]').first()
if not existing:
    max_seq = Issue.objects.filter(project=p).order_by('-sequence_id').values_list('sequence_id', flat=True).first() or 61
    issue = Issue.objects.create(
        project=p,
        workspace=p.workspace,
        name=t_def['title'],
        sequence_id=max_seq + 1,
        description_html=t_def['html_desc'],
        description_stripped=t_def['description_stripped'],
        priority=t_def['priority'],
        state=ai_dev_state,
        created_by=user,
        updated_by=user
    )
    mod = Module.objects.filter(project=p, name__icontains='SMM').first()
    if mod:
        ModuleIssue.objects.get_or_create(project=p, workspace=p.workspace, module=mod, issue=issue)
else:
    existing.name = t_def['title']
    existing.description_html = t_def['html_desc']
    existing.description_stripped = t_def['description_stripped']
    existing.state = ai_dev_state
    existing.save()
    issue = existing

print("__FREEBET_TASK__:" + json.dumps({{"seq": issue.sequence_id, "id": str(issue.id), "name": issue.name}}))
"""
    stdout, stderr = run_remote_django(django_code, timeout=60)
    for line in stdout.splitlines():
        if line.startswith("__FREEBET_TASK__:"):
            res = json.loads(line.replace("__FREEBET_TASK__:", ""))
            print(f"✅ Created/Updated #{res['seq']} ({res['id']}): {res['name']}")
            return
    print("Stdout:", stdout)
    print("Stderr:", stderr)


if __name__ == "__main__":
    main()
