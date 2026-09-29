#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Auto-orchestrator for SMM Growth Swarm
Checks worker status, detects completed tasks, unblocks and dispatches next wave tasks.
"""
import json
import subprocess
import sys
import time

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

SSH_HOST = "root@100.78.183.101"
SSH_KEY = r"C:\Users\chernousov_a\.ssh\id_ed25519"
PROJECT_ID = "2df124d7-25b0-4145-a63c-aafcb0fe0041"
WORKSPACE_SLUG = "dataplatform"

STATE_DONE = "e5f607b9-2f87-46c3-b4d0-243b3ca4a8c3"       # Завершено
STATE_AI_DEV = "1d4476f7-1d60-4b35-8211-439d318bf5d7"     # AI разработка

TASK_MAP = {
    54: ("a36e7819-1749-4359-8289-c80a0220264c", "[smm-runner] Базовый OCI-образ браузерного ИИ-агента (Firefox/Camoufox + Persistent Profile + Cache Warmup)"),
    55: ("94e1e364-59c5-4095-9b05-d59dc48778fd", "[auto-reg] Модуль генерации персон, регистрация в БК и партнерках"),
    56: ("99c57d7f-2e08-4fcb-8984-a4c8594811f8", "[smm-meta] ИИ-агент активности для Threads и Instagram (Firefox + Warmup)"),
    57: ("61c5500d-110e-4f3b-ab4e-34ea8b2c6d9a", "[smm-tg] Ревитализация Telegram-канала"),
    58: ("7f025fda-e873-4e83-a563-53135c651702", "[crowd-reddit] ИИ-агент крауд-маркетинга для Reddit"),
    59: ("b1cbfcef-863a-4be2-b550-2c87ece37263", "[video-shorts] Пайплайн автогенерации вертикальных видео"),
    60: ("ea619a45-a556-432f-b0eb-aefc73557011", "[promo-radar] Автоматический краулер и мониторинг акций, фрибетов и бонусов 52 БК"),
    61: ("1ea33341-9562-4263-b80a-1521968c8170", "[affiliate-hub] Регистрация в партнерских программах БК, генерация трекинг-ссылок и интеграция с SMM"),
    62: ("e0aeb955-8752-4e20-ac75-6ddda1fc8d9c", "[freebet-calc] Калькулятор конвертации фрибетов в 80% гарантированных денег (/tools/freebet-calculator) + гайд и посты")
}


def run_ssh(remote_cmd, input_text=None, timeout=40):
    cmd = [
        "ssh", "-i", SSH_KEY,
        "-o", "BatchMode=yes",
        "-o", "ConnectTimeout=10",
        SSH_HOST,
        remote_cmd
    ]
    return subprocess.run(cmd, input=input_text, capture_output=True, text=True, timeout=timeout, encoding="utf-8")


def check_git_master():
    subprocess.run(["git", "fetch", "origin"], capture_output=True, text=True)
    res = subprocess.run(["git", "log", "origin/master", "-n", "30", "--oneline"], capture_output=True, text=True)
    return res.stdout


def get_plane_issues_state():
    sql = (
        "SELECT sequence_id, state_id "
        "FROM issues "
        f"WHERE project_id='{PROJECT_ID}' AND sequence_id >= 50 "
        "ORDER BY sequence_id;"
    )
    res = run_ssh("kubectl exec -i -n plane deployment/plane-db-primary -- psql -U plane -d plane -t -A -F ','", input_text=sql)
    issue_states = {}
    for line in res.stdout.strip().splitlines():
        parts = line.strip().split(",")
        if len(parts) >= 2:
            try:
                seq = int(parts[0])
                state_id = parts[1]
                issue_states[seq] = state_id
            except ValueError:
                pass
    return issue_states


def set_plane_issue_state(seq, new_state_id):
    sql = f"UPDATE issues SET state_id='{new_state_id}', updated_at=NOW() WHERE project_id='{PROJECT_ID}' AND sequence_id={seq};"
    run_ssh("kubectl exec -i -n plane deployment/plane-db-primary -- psql -U plane -d plane", input_text=sql)


def trigger_plane_webhook(seq, iss_id):
    py_code = f"""
import json, urllib.request
payload = {{
    "event": "issue_activity",
    "action": "updated",
    "data": {{
        "field": "state",
        "new_value": "AI разработка",
        "issue": "{iss_id}",
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
with urllib.request.urlopen(req, timeout=15) as r:
    print(f"Triggered #{seq}: {{r.status}}")
"""
    res = run_ssh("python3 -", input_text=py_code)
    print(res.stdout.strip())


def main():
    print(f"=== Auto-Orchestrator Run at {time.strftime('%Y-%m-%d %H:%M:%S')} ===")
    master_log = check_git_master()
    states = get_plane_issues_state()
    print("Current Plane States:", states)

    # 1. Check completion in master commits
    for seq in [54, 57, 55, 56, 58, 59, 60, 61, 62]:
        iss_id, title = TASK_MAP[seq]
        short_id = iss_id[:8]
        if f"#{short_id}" in master_log:
            if states.get(seq) != STATE_DONE:
                print(f"Task #{seq} ({short_id}) detected in master! Marking as Завершено.")
                set_plane_issue_state(seq, STATE_DONE)
                states[seq] = STATE_DONE

    # 2. Check wave unblocking
    # Wave 2: Ensure #54 (runner), #57 (tg), #60 (promo-radar), #61 (affiliate-hub), #62 (freebet-calc) are active in AI разработка
    for w2_seq in [54, 57, 60, 61, 62]:
        if states.get(w2_seq) != STATE_DONE and states.get(w2_seq) != STATE_AI_DEV:
            print(f"Ensuring wave 2 task #{w2_seq} ({TASK_MAP[w2_seq][1]}) is in AI разработка...")
            set_plane_issue_state(w2_seq, STATE_AI_DEV)
            trigger_plane_webhook(w2_seq, TASK_MAP[w2_seq][0])
            states[w2_seq] = STATE_AI_DEV

    # If #54 is done, unblock #55 (auto-reg) and #56 (smm-meta)
    if states.get(54) == STATE_DONE:
        for next_seq in [55, 56]:
            if states.get(next_seq) not in (STATE_AI_DEV, STATE_DONE):
                print(f"Unblocking task #{next_seq} ({TASK_MAP[next_seq][1]})...")
                set_plane_issue_state(next_seq, STATE_AI_DEV)
                trigger_plane_webhook(next_seq, TASK_MAP[next_seq][0])
                states[next_seq] = STATE_AI_DEV

    # If #55 & #56 are done, unblock #58 (crowd-reddit)
    if states.get(55) == STATE_DONE and states.get(56) == STATE_DONE:
        if states.get(58) not in (STATE_AI_DEV, STATE_DONE):
            print("Unblocking task #58 (crowd-reddit)...")
            set_plane_issue_state(58, STATE_AI_DEV)
            trigger_plane_webhook(58, TASK_MAP[58][0])
            states[58] = STATE_AI_DEV

    # If #56 is done, unblock #59 (video-shorts)
    if states.get(56) == STATE_DONE:
        if states.get(59) not in (STATE_AI_DEV, STATE_DONE):
            print("Unblocking task #59 (video-shorts)...")
            set_plane_issue_state(59, STATE_AI_DEV)
            trigger_plane_webhook(59, TASK_MAP[59][0])
            states[59] = STATE_AI_DEV

    print("=== Cycle Summary ===")
    for seq in sorted(TASK_MAP.keys()):
        iss_id, title = TASK_MAP[seq]
        st = states.get(seq, "unknown")
        status_name = "Завершено" if st == STATE_DONE else ("AI разработка" if st == STATE_AI_DEV else "Ожидание блокеров")
        print(f"#{seq:2d} | {status_name:18s} | {title}")


if __name__ == "__main__":
    main()
