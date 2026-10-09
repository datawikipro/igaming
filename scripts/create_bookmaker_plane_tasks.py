#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Creates one Plane task per bookmaker that doesn't have one yet,
using the same dag_autonomous_orchestrator infrastructure.
"""
import os, sys, json, subprocess, uuid

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"

PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"
DB_USER = "plane"
DB_NAME = "plane"
DB_PASSWORD = "plane_db_secure_password_2026"

# State UUID for "Backlog"  (from dag_autonomous_orchestrator.py)
STATE_BACKLOG = "ed9f5e45-27fa-4e42-bcb0-5866b2e06a02"
# Workspace slug
WORKSPACE_SLUG = "dataplatform"

def run_sql(sql, timeout=20):
    escaped = sql.replace('"', '\\"')
    cmd = [
        "ssh", "-i", SSH_KEY,
        "-o", "BatchMode=yes", "-o", "ConnectTimeout=10",
        SSH_HOST,
        f"PGPASSWORD={DB_PASSWORD} kubectl exec -i -n plane deployment/plane-db-primary -- "
        f"psql -U {DB_USER} -d {DB_NAME} -t -A -F '|||' -c \"{escaped}\""
    ]
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout, encoding="utf-8")
    if res.returncode != 0:
        raise RuntimeError(f"SQL error: {res.stderr}")
    return [l.strip() for l in res.stdout.strip().splitlines() if l.strip()]


def get_existing_issues():
    sql = (
        "SELECT i.id, i.sequence_id, i.name "
        f"FROM issues i WHERE i.project_id='{PROJECT_ID}' ORDER BY i.sequence_id;"
    )
    rows = run_sql(sql)
    issues = []
    for row in rows:
        parts = row.split("|||")
        if len(parts) >= 3:
            issues.append({"id": parts[0], "seq": parts[1], "name": parts[2]})
    return issues


def get_workspace_id():
    sql = "SELECT id FROM workspaces LIMIT 1;"
    rows = run_sql(sql)
    return rows[0].strip() if rows else None


def get_creator_user_id():
    sql = "SELECT id FROM users LIMIT 1;"
    rows = run_sql(sql)
    return rows[0].strip() if rows else None


def create_issue(name: str, description: str) -> str:
    issue_id = str(uuid.uuid4())
    now = "NOW()"
    escaped_name = name.replace("'", "''")
    escaped_desc = description.replace("'", "''")
    sql = f"""
INSERT INTO issues (id, created_at, updated_at, name, description_html, project_id, state_id, priority, sequence_id)
VALUES (
    '{issue_id}', {now}, {now},
    '{escaped_name}',
    '<p>{escaped_desc}</p>',
    '{PROJECT_ID}',
    '{STATE_BACKLOG}',
    'medium',
    (SELECT COALESCE(MAX(sequence_id), 0) + 1 FROM issues WHERE project_id='{PROJECT_ID}')
)
RETURNING id, sequence_id;
"""
    rows = run_sql(sql)
    if rows:
        parts = rows[0].split("|||")
        return parts[1] if len(parts) > 1 else "?"
    return "?"


# All bookmakers that need to be monitored (from igaming-source-* modules)
ALL_BOOKMAKERS = [
    "10bet", "888sport", "apuestatotal", "atg", "baltbet", "bcgame",
    "bet365", "bet7k", "bet9ja", "betano", "betb2b", "betboom", "betbra",
    "betcity", "betdaq", "betesporte", "betfair", "betman", "betmgm",
    "betnacional", "betrivers", "betsson", "betvictor", "betway",
    "bookmaker", "bovada", "bwin", "caesars", "caliente", "cloudbet",
    "crickex", "dafabet", "digitain", "draftkings", "esportesdasorte",
    "estave", "expekt", "fanduel", "favbet", "fon-bet-ru", "fortuna",
    "iddaa", "interwetten", "leon", "ligastavok", "marathonbet",
    "matchbook", "mozzartbet", "nike", "norsktipping", "novibet",
    "novig", "olimpbet", "paf", "parimatch", "pinnacle", "polymarket",
    "sbobet", "smarkets", "sportbet", "stake", "stoiximan", "sts",
    "superbet", "supersport", "swisslos", "tab", "tennisi", "tipico",
    "tipsport", "tonybet", "unibet", "vaidebet", "vbet", "veikkaus",
    "winamax", "winline", "winner", "wplay", "zenit",
    # already completed (888starz, melbet-com, betboom via prev sessions):
    "888starz", "melbet-com",
]


if __name__ == "__main__":
    print("Fetching existing Plane issues...")
    try:
        existing = get_existing_issues()
    except Exception as e:
        print(f"ERROR getting issues: {e}")
        sys.exit(1)

    print(f"Found {len(existing)} existing issues")
    with open("scratch/plane_existing_issues.json", "w", encoding="utf-8") as f:
        json.dump(existing, f, indent=2, ensure_ascii=False)

    # Extract bookmaker names from existing issues
    existing_names_lower = {i["name"].lower() for i in existing}

    # Print all existing for reference
    for i in existing:
        print(f"  #{i['seq']}: {i['name']}")
