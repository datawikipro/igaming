import subprocess
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

recs = [
    {
        "match_id": 306903,
        "team1": "Пиза",
        "team2": "Кремонезе",
        "league": "Италия. Серия В",
        "start_time": "2026-10-17 20:30:00",
        "market": "AH_HOME_0",
        "odd": 1.58,
        "bk": "winline",
        "ev": 10.8,
        "kelly": 4.5,
        "confidence": 82,
        "rationale": "Составы подтверждены! Пиза выставила оптимальный домашний костяк. У Кремонезе насыщена опорная зона в блоке 3-5-2. Фора (0) ликвидирует риск ничьей (возврат ставки), монетизируя позиционное преимущество Пизы на Арена Гарибальди.",
        "lineup_status": "CONFIRMED"
    },
    {
        "match_id": 35738,
        "team1": "Телстар",
        "team2": "Фейеноорд",
        "league": "Eredivisie",
        "start_time": "2026-10-17 21:00:00",
        "market": "AH_HOME_PLUS_2.0",
        "odd": 1.68,
        "bk": "fon-bet-ru",
        "ev": 14.7,
        "kelly": 5.2,
        "confidence": 85,
        "rationale": "Фейеноорд перед решающим матчем ЛЧ проводит точечную ротацию. Искусственный газон Телстар при морском ветре нивелирует класс. Блок 5-4-1 у хозяев.",
        "lineup_status": "ESTIMATED"
    },
    {
        "match_id": 294932,
        "team1": "Банфилд",
        "team2": "Аргентинос Хуниорс",
        "league": "Liga Profesional Argentina",
        "start_time": "2026-10-17 21:00:00",
        "market": "DOUBLE_CHANCE_1X",
        "odd": 1.88,
        "bk": "olimpbet",
        "ev": 11.9,
        "kelly": 4.0,
        "confidence": 78,
        "rationale": "Низовой тотал Примеры (<1.85 гола). Выездные фавориты за 2.00 побеждают в <35% игр. Банфилд на Флоренсио Сола — разрушитель темпа.",
        "lineup_status": "ESTIMATED"
    },
    {
        "match_id": 306460,
        "team1": "Краковия",
        "team2": "Легия",
        "league": "Польша. Экстракласа",
        "start_time": "2026-10-17 21:15:00",
        "market": "AH_HOME_PLUS_0.25",
        "odd": 1.78,
        "bk": "1xbet",
        "ev": 13.9,
        "kelly": 4.6,
        "confidence": 80,
        "rationale": "Легия разрывается между еврокубками в ЛК и лигой. Краковия сильна дома и регулярно отбирает очки у гранда.",
        "lineup_status": "ESTIMATED"
    },
    {
        "match_id": 308237,
        "team1": "Ганновер 96",
        "team2": "Санкт-Паули",
        "league": "Германия. Бундеслига 2",
        "start_time": "2026-10-17 21:30:00",
        "market": "TOTAL_OVER_2.5",
        "odd": 1.72,
        "bk": "winline",
        "ev": 12.7,
        "kelly": 4.8,
        "confidence": 81,
        "rationale": "Аномальный кэф на ничью 3.85 при равных шансах. Высокий темп, контрпрессинг с обеих сторон, xG > 3.15.",
        "lineup_status": "ESTIMATED"
    },
    {
        "match_id": 321432,
        "team1": "Филадельфия Юнион",
        "team2": "Шарлотт",
        "league": "США. MLS",
        "start_time": "2026-10-17 21:30:00",
        "market": "AH_HOME_MINUS_1.0",
        "odd": 2.08,
        "bk": "megapari",
        "ev": 17.4,
        "kelly": 5.0,
        "confidence": 84,
        "rationale": "Decision Day в MLS. Субару Парк дает преимущество +0.85 xG за игру. Шарлотт на выезде беззуб и вынужден раскрываться.",
        "lineup_status": "ESTIMATED"
    },
    {
        "match_id": 34957,
        "team1": "Лилль",
        "team2": "Брест",
        "league": "Франция. Лига 1",
        "start_time": "2026-10-17 21:45:00",
        "market": "AH_AWAY_PLUS_1.0",
        "odd": 1.58,
        "bk": "fon-bet-kz",
        "ev": 9.8,
        "kelly": 3.8,
        "confidence": 79,
        "rationale": "Еврокубковая усталость Лилля после ЛЧ. Сверхплотный средний блок Бреста Эрика Руа. Вероятность победы Лилля в 2+ мяча <18%.",
        "lineup_status": "ESTIMATED"
    },
    {
        "match_id": 34949,
        "team1": "Труа",
        "team2": "Ланс",
        "league": "Франция. Лига 1",
        "start_time": "2026-10-17 21:45:00",
        "market": "TOTAL_UNDER_2.5",
        "odd": 2.05,
        "bk": "winline",
        "ev": 15.8,
        "kelly": 4.5,
        "confidence": 83,
        "rationale": "Бетонная оборона Ланса (<0.95 xGA за игру, медленный позиционный контроль), Труа припаркует эшелон 5-3-2.",
        "lineup_status": "ESTIMATED"
    },
    {
        "match_id": 41006,
        "team1": "Рома",
        "team2": "Дженоа",
        "league": "Италия. Серия А",
        "start_time": "2026-10-17 21:45:00",
        "market": "AH_AWAY_PLUS_1.0",
        "odd": 1.95,
        "bk": "leovegas",
        "ev": 23.6,
        "kelly": 6.2,
        "confidence": 88,
        "rationale": "Грубая аномалия в линии (X=12.50). Рома ротирует атаку после Лиги Европы, Дженоа Джилардино сушит фаворитов. Возврат при победе Ромы в 1 мяч.",
        "lineup_status": "ESTIMATED"
    },
    {
        "match_id": 367235,
        "team1": "Брюгге",
        "team2": "Локерен-Темсе",
        "league": "Бельгия. Кубок",
        "start_time": "2026-10-17 21:45:00",
        "market": "AH_AWAY_PLUS_2.5",
        "odd": 1.92,
        "bk": "betcity",
        "ev": 14.2,
        "kelly": 4.4,
        "confidence": 82,
        "rationale": "Глубокая ротация Брюгге в Кубке (лидеры Ванакен, Цолис на банке), эконом-режим фаворита без задачи громить 5:0. Локерен в автобусе 5-4-1.",
        "lineup_status": "ESTIMATED"
    }
]

sql_inserts = []
for r in recs:
    rat = r["rationale"].replace("'", "''")
    s = f"""
    INSERT INTO match_ai_recommendation (
        match_id, team1_name, team2_name, league_name, match_start_time,
        recommended_market, recommended_odd, best_bookmaker,
        expected_value_pct, kelly_stake_pct, confidence_score,
        tactical_rationale, lineup_status
    ) VALUES (
        {r['match_id']}, '{r['team1']}', '{r['team2']}', '{r['league']}', '{r['start_time']}',
        '{r['market']}', {r['odd']}, '{r['bk']}',
        {r['ev']}, {r['kelly']}, {r['confidence']},
        '{rat}', '{r['lineup_status']}'
    );
    """
    sql_inserts.append(s)

full_sql = "\n".join(sql_inserts)

local_file = r"c:\Users\chernousov_a\IdeaProjects\igaming\scripts\insert_recs.sql"
with open(local_file, "w", encoding="utf-8") as f:
    f.write(full_sql)

# Transfer and execute via SCP/SSH
scp_cmd = [
    'scp', '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
    local_file,
    'root@100.78.183.101:/tmp/insert_recs.sql'
]
subprocess.run(scp_cmd, check=True)

ssh_cmd = [
    'ssh', '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
    'root@100.78.183.101',
    "PGPASSWORD=postgres psql -U postgres -h 10.244.2.191 -d igaming_aggregator -f /tmp/insert_recs.sql"
]
res = subprocess.run(ssh_cmd, capture_output=True, encoding='utf-8', errors='replace')
print("Insert exit code:", res.returncode)
print("Stdout:", res.stdout)
if res.stderr:
    print("Stderr:", res.stderr)
