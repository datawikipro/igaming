#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Bulk creates one Plane task per missing bookmaker in a single SQL transaction.
"""
import os, sys, json, subprocess, uuid
from datetime import datetime

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"
PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"
WORKSPACE_ID = "a1369905-4689-487b-8b9c-e54555fe5b90"
CREATOR_ID = "14c687a7-0f86-4249-9e4f-e5b9ff898241"
DB_USER = "plane"
DB_NAME = "plane"
DB_PASSWORD = "plane_db_secure_password_2026"
STATE_BACKLOG = "ed9f5e45-27fa-4e42-bcb0-5866b2e06a02"  # Backlog

MISSING_BOOKMAKERS = {
    "10bet": "10Bet",
    "888sport": "888Sport",
    "apuestatotal": "ApuestaTotal",
    "bcgame": "BC.Game",
    "bet365": "Bet365",
    "bet7k": "Bet7K",
    "bet9ja": "Bet9ja",
    "betbra": "Betbra",
    "betcity": "Бетсити",
    "betdaq": "Betdaq",
    "betesporte": "BetEsporte",
    "betfair": "Betfair",
    "betman": "BetMan",
    "betmgm": "BetMGM",
    "betnacional": "BetNacional",
    "betsson": "Betsson",
    "betvictor": "BetVictor",
    "betway": "Betway",
    "bookmaker": "Bookmaker",
    "bovada": "Bovada",
    "bwin": "Bwin",
    "caesars": "Caesars",
    "caliente": "Caliente",
    "cloudbet": "CloudBet",
    "crickex": "CricketX",
    "dafabet": "Dafabet",
    "digitain": "Digitain",
    "draftkings": "DraftKings",
    "esportesdasorte": "EsportesDaSorte",
    "estave": "Estave",
    "expekt": "Expekt",
    "favbet": "FavBet",
    "fon-bet-ru": "Fonbet",
    "fortuna": "Fortuna",
    "iddaa": "Iddaa",
    "interwetten": "Interwetten",
    "leon": "Леон",
    "ligastavok": "ЛигаСтавок",
    "matchbook": "Matchbook",
    "mozzartbet": "Mozzartbet",
    "nike": "Nike",
    "norsktipping": "NorskTipping",
    "novibet": "Novibet",
    "novig": "Novig",
    "olimpbet": "Олимпбет",
    "paf": "PAF",
    "parimatch": "Париматч",
    "pinnacle": "Pinnacle",
    "polymarket": "Polymarket",
    "sbobet": "SBOBET",
    "sportbet": "SportBet",
    "stoiximan": "Stoiximan",
    "sts": "STS",
    "superbet": "Superbet",
    "swisslos": "Swisslos",
    "tab": "TAB",
    "tennisi": "Тенниси",
    "tipsport": "TipSport",
    "tonybet": "TonyBet",
    "vaidebet": "VaideBet",
    "vbet": "VBet",
    "veikkaus": "Veikkaus",
    "winamax": "Winamax",
    "winline": "Winline",
}

def ssh_psql(sql, timeout=30):
    cmd = [
        "ssh", "-i", SSH_KEY,
        "-o", "BatchMode=yes", "-o", "ConnectTimeout=10",
        "-o", "StrictHostKeyChecking=no",
        SSH_HOST,
        f"PGPASSWORD={DB_PASSWORD} kubectl exec -i -n plane deployment/plane-db-primary -- psql -U {DB_USER} -d {DB_NAME} -t -A"
    ]
    return subprocess.run(
        cmd, input=sql, capture_output=True, text=True,
        timeout=timeout, encoding="utf-8"
    )

def main():
    r = ssh_psql(f"SELECT COALESCE(MAX(sequence_id), 0) + 1 FROM issues WHERE project_id='{PROJECT_ID}';")
    if r.returncode != 0:
        print("Error getting sequence_id:", r.stderr)
        return
    next_seq = int(r.stdout.strip())
    print(f"Starting next sequence_id: {next_seq}")

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S+00")
    
    sql_parts = ["BEGIN;"]
    desc_json = json.dumps({"type": "doc", "content": [{"type": "paragraph", "content": [{"type": "text", "text": "Краулер и лоадер линии: наполнение >= 500 матчей"}]}]}).replace("'", "''")

    for i, (slug, display) in enumerate(MISSING_BOOKMAKERS.items()):
        seq = next_seq + i
        issue_id = str(uuid.uuid4())
        name = f"[{slug}] Ingestion and match line population (target >= 500 matches)".replace("'", "''")
        desc_html = (
            f"<p>Запустить crawler и loader для букмекера <strong>{display}</strong> (igaming-source-{slug}).</p>"
            f"<ul>"
            f"<li>Статус пода в K8s: Running 1/1 или 2/2</li>"
            f"<li>Actuator /actuator/health returns UP</li>"
            f"<li>Критерий наполнения линии: &ge; 500 активных матчей в match_cache</li>"
            f"<li>Odds streaming в igaming-aggregator (таблица odds_actual)</li>"
            f"<li>5-минутный тест стабильности без аварийных рестартов</li>"
            f"</ul>"
        ).replace("'", "''")
        sort_order = 50000.0 + i * 100.0

        sql_parts.append(f"""
INSERT INTO issues (
    id, created_at, updated_at,
    name, description_html, description_json,
    project_id, workspace_id, state_id,
    priority, sequence_id,
    created_by_id, is_draft, sort_order
) VALUES (
    '{issue_id}', '{now}', '{now}',
    '{name}', '{desc_html}', '{desc_json}',
    '{PROJECT_ID}', '{WORKSPACE_ID}', '{STATE_BACKLOG}',
    'medium', {seq},
    '{CREATOR_ID}', false, {sort_order}
);
""")

    sql_parts.append("COMMIT;")
    full_sql = "\n".join(sql_parts)

    print(f"Executing batch transaction for {len(MISSING_BOOKMAKERS)} bookmaker tasks...")
    res = ssh_psql(full_sql, timeout=30)
    print("Return code:", res.returncode)
    if res.stdout:
        print("STDOUT:\n", res.stdout.strip())
    if res.stderr:
        print("STDERR:\n", res.stderr.strip())

    if res.returncode == 0:
        print(f"Successfully created {len(MISSING_BOOKMAKERS)} tasks in Plane!")

if __name__ == "__main__":
    main()
