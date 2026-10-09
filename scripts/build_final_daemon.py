import re
import subprocess

with open("scripts/current_daemon.py", "r", encoding="utf-8") as f:
    code = f.read()

# 1. current_state quotas
state_target = '"geminiQuota": 1.0,\n    "claudeQuota": 1.0,'
state_replacement = '"geminiQuota": 1.0,\n    "gemini5hQuota": 1.0,\n    "geminiWeeklyQuota": 1.0,\n    "claudeQuota": 1.0,'
if state_target in code:
    code = code.replace(state_target, state_replacement)

# 2. Strict 1:1 binding
init_account_old = """def init_worker_account():
    \"\"\"Maps this pod to its designated Antigravity account (POD_INDEX % len(creds)).\"\"\"
    global current_state
    log(f"Binding worker pod {HOSTNAME} (index {POD_INDEX}) to account...")
    try:
        url = f"{ACCOUNTS_SERVICE_URL}/api/v1/llm-credentials/active"
        req = urllib.request.Request(url, headers={"Accept": "application/json"})
        with urllib.request.urlopen(req, timeout=10) as resp:
            creds = json.loads(resp.read().decode())
        
        raw_ag = [c for c in creds if c.get("provider") == "ANTIGRAVITY" and c.get("isActive") and c.get("refreshToken")]
        
        # Score credentials by available quota (prefer weekly > 10% and 5h > 20%)
        scored_creds = []
        for c in raw_ag:
            c_id = c.get("id")
            g_score = 0.0
            c_score = 1.0
            try:
                s_url = f"{ACCOUNTS_SERVICE_URL}/api/v1/llm-credentials/{c_id}/snapshots"
                with urllib.request.urlopen(s_url, timeout=3) as s_resp:
                    sn = json.loads(s_resp.read().decode())
                    if sn:
                        w_pct = sn[0].get("geminiWeeklyPct")
                        h_pct = sn[0].get("gemini5hPct")
                        if w_pct is not None:
                            g_score += float(w_pct) * 2.0
                        if h_pct is not None:
                            g_score += float(h_pct)
                        c_5h = sn[0].get("claude5hPct")
                        if c_5h is not None:
                            c_score = float(c_5h)
            except Exception:
                pass
            scored_creds.append((g_score, c_score, c))
        
        # Sort descending by quota score so active pods get accounts with real available quota
        scored_creds.sort(key=lambda x: (x[0], x[1]), reverse=True)
        ag_creds = [item[2] for item in scored_creds]
        if not ag_creds:
            log("Warning: No active Antigravity credentials returned from accounts-service!")
            with state_lock:
                current_state["status"] = "ERROR"
                current_state["lastError"] = "No active credentials in accounts-service"
            return False

        # Deterministic assignment
        if POD_INDEX < len(ag_creds):
            assigned = ag_creds[POD_INDEX]
        else:
            assigned = ag_creds[POD_INDEX % len(ag_creds)]
        email = assigned["email"]
        c_id = assigned["id"]
        rt = assigned["refreshToken"]

        # Fetch quota snapshot
        gemini_q = 1.0
        claude_q = 1.0
        try:
            snap_url = f"{ACCOUNTS_SERVICE_URL}/api/v1/llm-credentials/{c_id}/snapshots"
            with urllib.request.urlopen(snap_url, timeout=5) as s_resp:
                snaps = json.loads(s_resp.read().decode())
                if snaps:
                    g_pct = snaps[0].get("gemini5hPct")
                    if g_pct is None:
                        g_pct = snaps[0].get("geminiWeeklyPct")
                    if g_pct is not None:
                        gemini_q = float(g_pct)
                    c_pct = snaps[0].get("claude5hPct")
                    if c_pct is None:
                        c_pct = snaps[0].get("claudeWeeklyPct")
                    if c_pct is not None:
                        claude_q = float(c_pct)
        except Exception as e:
            log(f"Quota snapshot fetch notice: {e}")

        # Perform initial token refresh & agy setup
        at, exp = refresh_oauth_token(rt)
        setup_agy_env(email, at, rt, exp)

        with state_lock:
            current_state["email"] = email
            current_state["credentialId"] = c_id
            current_state["refreshToken"] = rt
            current_state["geminiQuota"] = float(gemini_q)
            current_state["claudeQuota"] = float(claude_q)
            current_state["status"] = "IDLE"

        log(f"Worker bound successfully: {email} (ID {c_id}) | Gemini Quota: {gemini_q*100:.0f}%, Claude: {claude_q*100:.0f}%")
        return True
    except Exception as e:
        log(f"Error binding worker account: {e}")
        with state_lock:
            current_state["status"] = "ERROR"
            current_state["lastError"] = str(e)
        return False"""

init_account_new = """def init_worker_account():
    \"\"\"Strictly maps worker pod N (0..19) 1:1 to dedicated Antigravity Account N (sorted by stable DB ID).\"\"\"
    global current_state
    log(f"Binding worker pod {HOSTNAME} (index {POD_INDEX}) strictly 1:1 to dedicated account...")
    try:
        url = f"{ACCOUNTS_SERVICE_URL}/api/v1/llm-credentials/active"
        req = urllib.request.Request(url, headers={"Accept": "application/json"})
        with urllib.request.urlopen(req, timeout=10) as resp:
            creds = json.loads(resp.read().decode())
        
        raw_ag = [c for c in creds if c.get("provider") == "ANTIGRAVITY" and c.get("isActive") and c.get("refreshToken")]
        if not raw_ag:
            log("Warning: No active Antigravity credentials returned from accounts-service!")
            with state_lock:
                current_state["status"] = "ERROR"
                current_state["lastError"] = "No active credentials in accounts-service"
            return False

        # STRICT 1:1 STATIC BINDING: Sort deterministically by stable DB id
        raw_ag.sort(key=lambda x: x["id"])

        # Pod N strictly takes Account N (no dynamic quota sorting, no round-robin between accounts)
        assigned = raw_ag[POD_INDEX % len(raw_ag)]
        email = assigned["email"]
        c_id = assigned["id"]
        rt = assigned["refreshToken"]

        # Fetch latest quota snapshot for this specific assigned account
        gemini_5h = 1.0
        gemini_w = 1.0
        claude_q = 1.0
        try:
            snap_url = f"{ACCOUNTS_SERVICE_URL}/api/v1/llm-credentials/{c_id}/snapshots"
            with urllib.request.urlopen(snap_url, timeout=5) as s_resp:
                snaps = json.loads(s_resp.read().decode())
                if snaps:
                    g_5h = snaps[0].get("gemini5hPct")
                    if g_5h is not None:
                        gemini_5h = float(g_5h)
                    g_w = snaps[0].get("geminiWeeklyPct")
                    if g_w is not None:
                        gemini_w = float(g_w)
                    c_pct = snaps[0].get("claude5hPct")
                    if c_pct is not None:
                        claude_q = float(c_pct)
        except Exception as e:
            log(f"Quota snapshot fetch notice: {e}")

        # Perform initial token refresh & agy setup
        at, exp = refresh_oauth_token(rt)
        setup_agy_env(email, at, rt, exp)

        with state_lock:
            current_state["email"] = email
            current_state["credentialId"] = c_id
            current_state["refreshToken"] = rt
            current_state["geminiQuota"] = min(gemini_5h, gemini_w)
            current_state["gemini5hQuota"] = gemini_5h
            current_state["geminiWeeklyQuota"] = gemini_w
            current_state["claudeQuota"] = float(claude_q)
            current_state["status"] = "IDLE"
            current_state["lastError"] = None

        log(f"Worker {HOSTNAME} successfully bound 1:1 to {email} (ID {c_id}) | 5h: {gemini_5h*100:.0f}%, Weekly: {gemini_w*100:.0f}%, Claude: {claude_q*100:.0f}%")
        return True
    except Exception as e:
        log(f"Error binding worker account: {e}")
        with state_lock:
            current_state["status"] = "ERROR"
            current_state["lastError"] = str(e)
        return False"""

code = code.replace(init_account_old, init_account_new)

# 3. Add /can-accept endpoint
handler_old = """    def do_GET(self):
        if self.path == "/health":
            self.send_json(200, {"status": "ok", "pod": HOSTNAME, "state": current_state.get("status")})
        elif self.path == "/status":
            with state_lock:
                snapshot = dict(current_state)
            self.send_json(200, snapshot)
        else:
            self.send_json(404, {"error": "Not found"})"""

handler_new = """    def do_GET(self):
        if self.path == "/health":
            self.send_json(200, {"status": "ok", "pod": HOSTNAME, "state": current_state.get("status")})
        elif self.path == "/status":
            with state_lock:
                snapshot = dict(current_state)
            self.send_json(200, snapshot)
        elif self.path == "/can-accept":
            with state_lock:
                st = current_state.get("status")
                g5h = current_state.get("gemini5hQuota", 1.0)
                gw = current_state.get("geminiWeeklyQuota", 1.0)
                email = current_state.get("email")
            is_idle = (st == "IDLE")
            has_tokens = (g5h >= 0.10 and gw >= 0.05)
            eligible = is_idle and has_tokens
            reason = "Готов к выполнению" if eligible else ("Занят другой задачей" if not is_idle else f"Низкая квота ({email}): 5h={g5h*100:.0f}%, week={gw*100:.0f}%")
            self.send_json(200, {
                "eligible": eligible,
                "reason": reason,
                "pod": HOSTNAME,
                "email": email,
                "status": st,
                "gemini5hQuota": g5h,
                "geminiWeeklyQuota": gw
            })
        else:
            self.send_json(404, {"error": "Not found"})"""

code = code.replace(handler_old, handler_new)

# 4. Remove github domains from default_no_proxy and set git proxy
code = code.replace(
    ',github.com,.github.com,githubusercontent.com,.githubusercontent.com,ghcr.io',
    ''
)

setup_env_git_patch = """    # Git config
    subprocess.run(["git", "config", "--global", "http.proxy", PROXY_HTTP_URL], check=False)
    subprocess.run(["git", "config", "--global", "https.proxy", PROXY_HTTP_URL], check=False)
    subprocess.run(["git", "config", "--global", "user.name", "DataPlatform AI Bot"], check=False)"""

code = code.replace("""    # Git config
    subprocess.run(["git", "config", "--global", "user.name", "DataPlatform AI Bot"], check=False)""", setup_env_git_patch)

# 5. Clean git state before clone/checkout in execute_task_lifecycle
clone_fix = """        # 1. Clone or Fetch Repo
        os.makedirs("/workspace", exist_ok=True)
        if os.path.exists(os.path.join(repo_dir, ".git")):
            # Abort any stale rebase or merge
            subprocess.run(["git", "rebase", "--abort"], cwd=repo_dir, capture_output=True)
            subprocess.run(["git", "merge", "--abort"], cwd=repo_dir, capture_output=True)
        else:
            log(f"Cloning {git_repo} (base branch: {base_branch})...")
            shutil.rmtree(repo_dir, ignore_errors=True)
            clone_url = git_repo
            if GITHUB_TOKEN and "github.com" in git_repo and "x-access-token" not in git_repo:
                clone_url = git_repo.replace("https://github.com/", f"https://x-access-token:{GITHUB_TOKEN}@github.com/")
            clone_env = dict(os.environ)
            clone_env["GIT_TERMINAL_PROMPT"] = "0"
            clone_env["http_proxy"] = PROXY_HTTP_URL
            clone_env["https_proxy"] = PROXY_HTTP_URL
            subprocess.run(["git", "clone", "--depth", "50", "--branch", base_branch, clone_url, repo_dir], env=clone_env, check=True)
        
        if not os.path.exists(os.path.join(repo_dir, ".git")):
            raise RuntimeError(f"Repository {repo_dir} does not exist after clone!")"""

code = code.replace("""        # 1. Clone or Fetch Repo
        os.makedirs("/workspace", exist_ok=True)
        if not os.path.exists(os.path.join(repo_dir, ".git")):
            log(f"Cloning {git_repo} (base branch: {base_branch})...")
            shutil.rmtree(repo_dir, ignore_errors=True)
            clone_url = git_repo
            if GITHUB_TOKEN and "github.com" in git_repo and "x-access-token" not in git_repo:
                clone_url = git_repo.replace("https://github.com/", f"https://x-access-token:{GITHUB_TOKEN}@github.com/")
            subprocess.run(["git", "clone", "--depth", "50", "--branch", base_branch, clone_url, repo_dir], check=True)""", clone_fix)

with open("scripts/final_daemon.py", "w", encoding="utf-8") as f:
    f.write(code)

print("Saved scripts/final_daemon.py, length:", len(code))
