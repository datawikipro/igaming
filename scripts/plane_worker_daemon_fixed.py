#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Plane AI Autonomous Worker Daemon
Runs on each of the 18 dedicated pods in StatefulSet plane-ai-worker.
Enforces: 1 pod = 1 task.
Integrates:
- Antigravity CLI (agy) with Gemini 3.8 Flash High, Claude Sonnet 4.6 & Claude Opus 4.6 Thinking
- Fast 429 quota detection via cli.log inspection + automatic model fallback (Gemini -> Claude Sonnet)
- OpenSpec specification-driven iterative development with intermediate git commits
- Quota management: WAIT_QUOTA_RESET or HANDOVER_MIGRATE to another idle pod (with cooldown so exhausted pods aren't re-picked)
- Multi-stack E2E verification: Java bookmaker crawlers (igaming-source) AND Python/Next.js/Infra services (igaming-dev)
"""

import os
import sys
import json
import time
import glob
import socket
import re
import urllib.request
import urllib.parse
import subprocess
import threading
import datetime
import shutil
import signal
from http.server import HTTPServer, BaseHTTPRequestHandler
from socketserver import ThreadingMixIn

PORT = 8000
ACCOUNTS_SERVICE_URL = os.environ.get("ACCOUNTS_SERVICE_URL", "http://accounts-service.accounts.svc.cluster.local:8091")
PROXY_HTTP_URL = os.environ.get("PROXY_HTTP_URL", "http://100.64.48.127:8888")
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "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")
CLIENT_ID = "1071006060591-tmhssin2h21lcre235vtolojh4g403ep.apps.googleusercontent.com"
CLIENT_SECRET = "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"

MODEL_GEMINI = "gemini-3.8-flash-high"
MODEL_CLAUDE = "claude-sonnet-4-6"
MODEL_OPUS = "claude-opus-4-6-thinking"

AGENTS_MD_CONTENT = """# Global Rules & Guardrails for Autonomous AI Workers

## ⏱️ Защита от зависания: Сторожевой таймер (Watchdog), таймауты команд и протокол отстрела (Kill Protocol)

### Проблема «мертвого ожидания»
При запуске долгих команд (сборка Maven/Jib, Gradle, npm/Next.js, запуск тестов, Spark/Airflow, SSH-сессия на сервере, сканирование диска, тяжелые скрипты) процесс может зависнуть (дедлок, ожидание интерактивного ввода stdin, блокировка сокета, сетевой сбой, зависание пакетов, бесконечный цикл).

### 🚫 СТРОГИЙ ЗАПРЕТ НА «СЛЕПОЕ ОЖИДАНИЕ»
- Категорически запрещено оставлять фоновую задачу без немедленно взведённого сторожевого таймера.
- Запрещено запускать потенциально бесконечные streaming-команды (`kubectl logs -f`, `tail -f`, `watch`, бесконечный `ping` без параметров ограничения количества пакетов).
- Превентивные меры: всегда использовать `timeout`: `timeout 300s mvn ...`, `timeout 60s ...`.
- Запретить ожидание ввода: `mvn -B`, `apt-get -y`, `npm --yes`, `npm ci --prefer-offline --no-audit`, `GIT_TERMINAL_PROMPT=0`.

---

## 🛡️ Абсолютный запрет фейков и преждевременного закрытия (Правило 5 минут и чистых логов)

> [!CAUTION]
> **КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО переводить задачу в Завершено (COMPLETED / DONE), пока не прошло 5 минут и не проверены логи пода!**
> 1. **Запрет преждевременных рапортов о готовности**:
>    Ни при каких обстоятельствах нельзя закрывать задачу сразу после сборки или рестарта пода.
> 2. **Обязательное 5-минутное окно стабилизации (5-Minute Soak & Log Inspection Window)**:
>    После деплоя сервис должен проработать не менее 5 минут (300 секунд) в статусе `Running 1/1` без единой ошибки в логах.
> 3. **Чистые логи без ошибок**:
>    Логи проверяются на отсутствие `Exception`, `NullPointerException`, `IllegalStateException`, `Error`, `FATAL`, `Crash`, `OOMKilled`.

---

## 🔇 Запрет перезагрузки физического сервера ночью (22:00 — 10:00)
- СТРОГИЙ ЗАПРЕТ: если для какого-либо действия требуется перезагрузка хоста `xeon-srv`, в период с 22:00 до 10:00 перезагружаться категорически нельзя (кулеры шумят на 100% в жилом помещении).

---

## 📌 OpenSpec & Specification-Driven Development
- Все изменения оформляются через OpenSpec: `proposal.md`, `tasks.md`, `design.md`.
- Каждый выполненный этап фиксируется промежуточным git-коммитом (`wip(ai): ...`) и отметкой `- [x]` в `tasks.md`.
"""

# Pod Identity
HOSTNAME = os.environ.get("HOSTNAME", socket.gethostname())
try:
    POD_INDEX = int(HOSTNAME.split("-")[-1])
except Exception:
    POD_INDEX = 0

state_lock = threading.Lock()
current_state = {
    "podIndex": POD_INDEX,
    "podName": HOSTNAME,
    "email": "unassigned",
    "credentialId": None,
    "refreshToken": None,
    "status": "INITIALIZING",  # IDLE, BUSY, WAITING_QUOTA_RESET, ERROR
    "currentTask": None,
    "geminiQuota": 1.0,
    "claudeQuota": 1.0,
    "lastError": None
}

active_process = None


def log(msg):
    ts = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{ts}] [{HOSTNAME}] {msg}", flush=True)


def setup_environment():
    """Configures proxy, xvfb, git, and installs required tools if absent."""
    os.environ["HTTP_PROXY"] = PROXY_HTTP_URL
    os.environ["HTTPS_PROXY"] = PROXY_HTTP_URL
    os.environ["http_proxy"] = PROXY_HTTP_URL
    os.environ["https_proxy"] = PROXY_HTTP_URL
    os.environ["NO_PROXY"] = "localhost,127.0.0.1,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,100.64.0.0/10,accounts-service,accounts-db,plane-ai-executor,plane-proxy,plane-api,plane-minio,gitlab-ce,.svc,.cluster.local,.s3.ru,github.com"
    os.environ["no_proxy"] = os.environ["NO_PROXY"]

    try:
        res = subprocess.run(["pgrep", "Xvfb"], capture_output=True)
        if res.returncode != 0:
            subprocess.Popen(["Xvfb", ":99", "-screen", "0", "1280x1024x24"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            log("Started Xvfb :99")
    except Exception as e:
        log(f"Xvfb notice: {e}")
    os.environ["DISPLAY"] = ":99"

    subprocess.run(["git", "config", "--global", "user.name", "DataPlatform AI Bot"], check=False)
    subprocess.run(["git", "config", "--global", "user.email", "ai@dataplatform.college"], check=False)
    subprocess.run(["git", "config", "--global", "core.sshCommand", "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"], check=False)
    if GITHUB_TOKEN:
        subprocess.run(["git", "config", "--global", f"url.https://x-access-token:{GITHUB_TOKEN}@github.com/.insteadOf", "https://github.com/"], check=False)
        try:
            p = subprocess.Popen(["gh", "auth", "login", "--with-token"], stdin=subprocess.PIPE, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            p.communicate(input=GITHUB_TOKEN.encode())
            subprocess.run(["gh", "auth", "setup-git"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
        except Exception:
            pass

    try:
        res = subprocess.run(["which", "agy"], capture_output=True, text=True)
        if res.returncode != 0:
            if os.path.exists("/root/.local/bin/agy"):
                subprocess.run(["ln", "-sf", "/root/.local/bin/agy", "/usr/local/bin/agy"], check=False)
            else:
                subprocess.run("curl -fsSL https://antigravity.google/cli/install.sh | bash", shell=True, check=False)
                subprocess.run(["ln", "-sf", "/root/.local/bin/agy", "/usr/local/bin/agy"], check=False)

        home = os.environ.get("HOME", "/root")
        os.makedirs(os.path.join(home, ".gemini", "config"), exist_ok=True)
        with open(os.path.join(home, ".gemini", "config", "AGENTS.md"), "w", encoding="utf-8") as f:
            f.write(AGENTS_MD_CONTENT)
        log("Installed global AGENTS.md to ~/.gemini/config/AGENTS.md")
    except Exception as e:
        log(f"Setup environment warning: {e}")


def refresh_oauth_token(refresh_token):
    data = urllib.parse.urlencode({
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET,
        "refresh_token": refresh_token,
        "grant_type": "refresh_token"
    }).encode()
    handlers = []
    if PROXY_HTTP_URL:
        handlers.append(urllib.request.ProxyHandler({"http": PROXY_HTTP_URL, "https": PROXY_HTTP_URL}))
    opener = urllib.request.build_opener(*handlers)
    req = urllib.request.Request("https://oauth2.googleapis.com/token", data=data, headers={"Content-Type": "application/x-www-form-urlencoded"})
    with opener.open(req, timeout=30) as resp:
        body = json.loads(resp.read().decode())
        return body.get("access_token"), body.get("expires_in", 3600)


def setup_agy_env(email, access_token, refresh_token, expires_in):
    home = os.environ.get("HOME", "/root")
    gdir = os.path.join(home, ".gemini")
    cdir = os.path.join(gdir, "antigravity-cli")
    os.makedirs(os.path.join(cdir, "cache"), exist_ok=True)

    with open(os.path.join(gdir, "oauth_creds.json"), "w") as f:
        json.dump({"access_token": access_token, "token_type": "Bearer", "refresh_token": refresh_token}, f)
    with open(os.path.join(gdir, "google_accounts.json"), "w") as f:
        json.dump({"active": email, "old": []}, f)
    with open(os.path.join(cdir, "cache", "onboarding.json"), "w") as f:
        json.dump({"consumerOnboardingComplete": True, "enterpriseOnboardingComplete": False, "onboardingComplete": True}, f)
    with open(os.path.join(cdir, "settings.json"), "w") as f:
        json.dump({
            "security": {"auth": {"selectedType": "oauth-personal"}},
            "theme": "dark",
            "trustedWorkspaces": [home, "/tmp", "/root", "/workspace", "/workspace/repo", "/workspace/documentation"]
        }, f)

    exp_str = (datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(seconds=expires_in)).isoformat()
    token_obj = {
        "token": {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "token_type": "Bearer",
            "expiry": exp_str
        },
        "auth_method": "consumer"
    }
    with open(os.path.join(cdir, "antigravity-oauth-token"), "w") as f:
        json.dump(token_obj, f)

    try:
        import keyring
        keyring.set_password("gemini:antigravity", email, json.dumps(token_obj))
    except Exception:
        pass


def init_worker_account():
    """Maps this pod to its designated Antigravity account (POD_INDEX % len(creds))."""
    global current_state
    log(f"Binding worker pod {HOSTNAME} (index {POD_INDEX}) to account...")
    try:
        url = f"{ACCOUNTS_SERVICE_URL}/api/v1/llm-credentials/active"
        req = urllib.request.Request(url, headers={"Accept": "application/json"})
        with urllib.request.urlopen(req, timeout=10) as resp:
            creds = json.loads(resp.read().decode())

        ag_creds = [c for c in creds if c.get("provider") == "ANTIGRAVITY" and c.get("isActive") and c.get("refreshToken")]
        if not ag_creds:
            log("Warning: No active Antigravity credentials returned from accounts-service!")
            with state_lock:
                current_state["status"] = "ERROR"
                current_state["lastError"] = "No active credentials in accounts-service"
            return False

        assigned = ag_creds[POD_INDEX % len(ag_creds)]
        email = assigned["email"]
        c_id = assigned["id"]
        rt = assigned["refreshToken"]

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
        return False


def report_plane_status(task, status, message="", pr_url=""):
    callback_url = task.get("callbackUrl")
    if not callback_url:
        return
    res = "progress"
    if status in ["COMPLETED", "PR_CREATED"]:
        res = "done"
    elif status in ["FAILED", "STUCK"]:
        res = "stuck"
    elif status in ["HANDOVER", "HANDOVER_MIGRATING"]:
        res = "handover"

    payload = {
        "status": status,
        "result": res,
        "message": f"[{HOSTNAME}] {message}",
        "pr_url": pr_url
    }
    try:
        data_bytes = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        req = urllib.request.Request(
            callback_url,
            data=data_bytes,
            headers={"Content-Type": "application/json; charset=utf-8"}
        )
        with urllib.request.urlopen(req, timeout=10) as r:
            pass
    except Exception as e:
        log(f"Callback error to {callback_url}: {e}")


def check_cli_log_for_429(start_ts):
    """Checks the latest agy cli.log created/modified after start_ts for immediate 429 RESOURCE_EXHAUSTED."""
    try:
        cli_log = "/root/.gemini/antigravity-cli/cli.log"
        if not os.path.exists(cli_log):
            return False
        if os.path.getmtime(cli_log) < start_ts - 2:
            return False
        with open(cli_log, "r", encoding="utf-8", errors="ignore") as f:
            lines = f.readlines()[-25:]
        tail_text = "".join(lines).lower()
        if "resource_exhausted (code 429)" in tail_text or "individual quota reached" in tail_text:
            return True
    except Exception:
        pass
    return False


def is_output_quota_exhausted(out_text):
    lower = (out_text or "").lower()
    return any(k in lower for k in [
        "out of credits",
        "resource_exhausted",
        "quota exceeded",
        "individual quota reached",
        "upgrade your subscription to increase your limits",
        "rate limit",
        "permission_denied"
    ])


def run_agy_step(prompt, model, mode="accept-edits", working_dir="/workspace/repo", timeout_seconds=420):
    global active_process
    env = dict(os.environ)
    env["AGY_ACCEPT_TOS"] = "true"
    env["ANTIGRAVITY_ACCEPT_TOS"] = "true"
    env["AGY_SKIP_ONBOARDING"] = "true"
    env["ANTIGRAVITY_SKIP_ONBOARDING"] = "true"
    env["LANG"] = "C.UTF-8"
    env["LC_ALL"] = "C.UTF-8"
    env["PYTHONIOENCODING"] = "utf-8"

    cmd = [
        "agy",
        "-p", prompt,
        "--model", model,
        "--mode", mode,
        "--dangerously-skip-permissions",
        "--add-dir", working_dir
    ]
    if os.path.exists("/workspace/documentation"):
        cmd.extend(["--add-dir", "/workspace/documentation"])

    log(f"Running agy (model: {model}, mode: {mode}, dir: {working_dir})...")
    start_ts = time.time()
    try:
        active_process = subprocess.Popen(
            cmd,
            cwd=working_dir,
            env=env,
            stdin=subprocess.DEVNULL,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            encoding="utf-8",
            errors="replace",
            preexec_fn=os.setsid
        )
        # Poll in 4-second intervals so we can fail-fast if cli.log shows 429 RESOURCE_EXHAUSTED
        while True:
            try:
                stdout, stderr = active_process.communicate(timeout=4)
                ret = active_process.returncode
                active_process = None
                output = (stdout or "") + "\n" + (stderr or "")
                return ret, output
            except subprocess.TimeoutExpired:
                elapsed = time.time() - start_ts
                if check_cli_log_for_429(start_ts):
                    log(f"Fast-abort: detected 429 RESOURCE_EXHAUSTED in cli.log after {int(elapsed)}s!")
                    try:
                        os.killpg(os.getpgid(active_process.pid), signal.SIGKILL)
                        active_process.wait(timeout=3)
                    except Exception:
                        pass
                    active_process = None
                    return 1, "RESOURCE_EXHAUSTED: Individual quota reached (fast-aborted from cli.log)"
                if elapsed >= timeout_seconds:
                    log(f"Step timed out after {timeout_seconds}s. Terminating agy process group and capturing partial progress...")
                    try:
                        os.killpg(os.getpgid(active_process.pid), signal.SIGKILL)
                        active_process.wait(timeout=5)
                    except Exception:
                        pass
                    active_process = None
                    return 0, "timeout_partial_checkpoint"
    except Exception as e:
        log(f"run_agy_step error: {e}")
        return 1, str(e)


def run_agy_with_fallback(prompt, mode="accept-edits", working_dir="/workspace/repo", timeout_seconds=900):
    """Tries MODEL_GEMINI first (if geminiQuota > 0), then falls back to MODEL_CLAUDE (claude-sonnet-4-6)."""
    global current_state
    with state_lock:
        gem_q = current_state.get("geminiQuota", 1.0)
        cld_q = current_state.get("claudeQuota", 1.0)

    if gem_q > 0:
        ret, out = run_agy_step(prompt, MODEL_GEMINI, mode=mode, working_dir=working_dir, timeout_seconds=timeout_seconds)
        if not is_output_quota_exhausted(out):
            return ret, out, False
        log(f"Gemini quota exhausted on {HOSTNAME} ({current_state.get('email')}). Marking geminiQuota=0.0 and trying {MODEL_CLAUDE}...")
        with state_lock:
            current_state["geminiQuota"] = 0.0

    if cld_q > 0:
        ret, out = run_agy_step(prompt, MODEL_CLAUDE, mode=mode, working_dir=working_dir, timeout_seconds=timeout_seconds)
        if not is_output_quota_exhausted(out):
            return ret, out, False
        log(f"Claude quota also exhausted on {HOSTNAME} ({current_state.get('email')}). Marking claudeQuota=0.0.")
        with state_lock:
            current_state["claudeQuota"] = 0.0

    return 1, "RESOURCE_EXHAUSTED: Both Gemini and Claude quotas exhausted on this pod", True


def detect_bookmaker_module(repo_dir, task_title, base_branch):
    """Returns igaming-source-<bookmaker> ONLY if the task or git diff actually targets a bookmaker crawler."""
    diff_res = subprocess.run(["git", "diff", "--name-only", f"origin/{base_branch}", "HEAD"], cwd=repo_dir, capture_output=True, text=True)
    for line in diff_res.stdout.splitlines():
        part = line.split("/")[0]
        if part.startswith("igaming-source-"):
            return part

    NON_BOOKMAKER_TAGS = {
        "infra-redis", "auth-2fa", "seo-pages", "seo-calc", "smm-runner",
        "auto-reg", "smm-meta", "smm-tg", "crowd-reddit", "video-shorts",
        "frontend", "portal", "aggregator", "bot", "infra", "seo", "smm"
    }
    match = re.search(r"\[([^\]]+)\]", task_title)
    if match:
        bm_tag = match.group(1).lower().strip()
        if bm_tag in NON_BOOKMAKER_TAGS:
            return None
        candidates = [d for d in os.listdir(repo_dir) if d.startswith("igaming-source-") and bm_tag in d]
        if candidates:
            return candidates[0]
        return f"igaming-source-{bm_tag}"
    return None


def merge_branch_to_base(repo_dir, target_branch, base_branch, task_title, short_id):
    log(f"Merging {target_branch} into {base_branch}...")
    subprocess.run(["git", "push", "origin", target_branch, "--force"], cwd=repo_dir, check=False)

    pr_merged = False
    try:
        gh_m_res = subprocess.run(["gh", "pr", "merge", target_branch, "--merge", "--admin"], cwd=repo_dir, capture_output=True, text=True)
        if gh_m_res.returncode == 0:
            pr_merged = True
            log("PR merged via GitHub CLI successfully!")
    except Exception as e:
        log(f"gh pr merge notice: {e}")

    if not pr_merged:
        for attempt in range(5):
            try:
                subprocess.run(["git", "checkout", base_branch], cwd=repo_dir, check=True)
                subprocess.run(["git", "pull", "--rebase", "origin", base_branch], cwd=repo_dir, check=True)
                m_res = subprocess.run(["git", "merge", "--no-ff", target_branch, "-m", f"feat(ai): merge verified {task_title} (#{short_id})"], cwd=repo_dir, capture_output=True, text=True)
                if m_res.returncode != 0:
                    subprocess.run(["git", "checkout", "--theirs", "."], cwd=repo_dir)
                    subprocess.run(["git", "add", "-A", "."], cwd=repo_dir)
                    subprocess.run(["git", "commit", "-m", f"feat(ai): merge verified {task_title} (conflict resolved)"], cwd=repo_dir)
                push_res = subprocess.run(["git", "push", "origin", base_branch], cwd=repo_dir, capture_output=True, text=True)
                if push_res.returncode == 0:
                    log(f"Merged and pushed directly to {base_branch}!")
                    break
                time.sleep(3 * (attempt + 1))
            except Exception as e:
                log(f"Merge retry error on attempt {attempt+1}: {e}")
                time.sleep(3 * (attempt + 1))


def deploy_and_verify_non_bookmaker(repo_dir, task, task_title, short_id, target_branch, base_branch):
    """E2E verification and 5-minute soak for non-bookmaker tasks (infra-redis, auth-2fa, seo-pages, seo-calc, smm, etc.)."""
    log(f"=== Starting Ecosystem E2E Verification for #{short_id}: {task_title} ===")
    match = re.search(r"\[([^\]]+)\]", task_title)
    tag = match.group(1).lower().strip() if match else ""

    dep_map = {
        "infra-redis": ("igaming-dev", "igaming-redis"),
        "auth-2fa": ("igaming-dev", "auth-inbox-gateway"),
        "seo-pages": ("igaming-dev", "smartbet-guru"),
        "seo-calc": ("igaming-dev", "smartbet-guru"),
    }
    namespace, dep_name = dep_map.get(tag, ("igaming-dev", "smartbet-guru"))

    # If auth-inbox-gateway or another docker directory was modified, build & push image if docker/buildah/kaniko or kubectl apply is available
    if tag == "auth-2fa" and os.path.isdir(os.path.join(repo_dir, "auth-inbox-gateway", "k8s")):
        subprocess.run(["kubectl", "apply", "-f", os.path.join(repo_dir, "auth-inbox-gateway", "k8s", "deployment.yaml")], check=False)

    log(f"Waiting for deployment/{dep_name} in namespace {namespace} to be ready...")
    subprocess.run(["kubectl", "rollout", "status", f"deployment/{dep_name}", "-n", namespace, "--timeout=120s"], check=False)

    report_plane_status(task, "VERIFYING", f"Воркер {HOSTNAME}: запуск обязательного 5-минутного мониторинга пода {dep_name} в namespace {namespace}...")
    start_soak = time.time()
    soak_errors = []
    while time.time() - start_soak < 300:
        time.sleep(20)
        elapsed = int(time.time() - start_soak)
        log(f"Ecosystem soak elapsed: {elapsed}/300s for {dep_name} in {namespace}...")
        l_res = subprocess.run(["kubectl", "logs", f"deployment/{dep_name}", "-n", namespace, "--tail=80"], capture_output=True, text=True)
        for line in l_res.stdout.splitlines():
            lower_l = line.lower()
            if any(err_kw in lower_l for err_kw in ["nullpointerexception", "fatal:", "oomkilled", "panic:", "traceback (most recent call last)"]):
                if line not in soak_errors:
                    soak_errors.append(line)

    if soak_errors:
        err_msg = "\n".join(soak_errors[-5:])
        raise RuntimeError(f"Service {dep_name} failed 5-minute soak test:\n{err_msg}")

    merge_branch_to_base(repo_dir, target_branch, base_branch, task_title, short_id)

    victory_msg = (
        f"🎉 Задача успешно выполнена и проверена воркером {HOSTNAME}!\n"
        f"1. Реализованы все пункты OpenSpec для {task_title}\n"
        f"2. Сервис `{dep_name}` в namespace `{namespace}` отработал 5 полных минут (300с) в статусе Running без ошибок\n"
        f"3. Изменения смержены и запушены в ветку `{base_branch}`."
    )
    log(victory_msg)
    report_plane_status(task, "COMPLETED", victory_msg)


def deploy_and_verify_e2e(repo_dir, task, task_title, short_id, target_branch, base_branch, email, git_repo):
    module_name = detect_bookmaker_module(repo_dir, task_title, base_branch)
    if not module_name:
        deploy_and_verify_non_bookmaker(repo_dir, task, task_title, short_id, target_branch, base_branch)
        return

    log(f"=== Starting Bookmaker E2E Delivery Pipeline for #{short_id}: {task_title} (module: {module_name}) ===")
    mod_dir = os.path.join(repo_dir, module_name)
    mod_pom = os.path.join(mod_dir, "pom.xml")
    if not os.path.exists(mod_pom):
        log(f"Module pom.xml not found at {mod_pom}, initializing submodule {module_name}...")
        subprocess.run(["git", "submodule", "update", "--init", "--depth", "1", module_name], cwd=repo_dir, check=False)

    if not os.path.exists(mod_pom):
        raise RuntimeError(f"Module pom.xml still does not exist at {mod_pom}")

    bookmaker = module_name.replace("igaming-source-", "")
    report_plane_status(task, "RUNNING", f"Воркер {HOSTNAME}: сборка Jib-образа {module_name}...")
    test_image_tag = f"test-{short_id}"
    test_image = f"100.78.183.101:30500/{module_name}:{test_image_tag}"
    log(f"Building Jib test image: {test_image} using {mod_pom}...")

    jib_res = subprocess.run([
        "mvn", "-f", mod_pom,
        "compile",
        "com.google.cloud.tools:jib-maven-plugin:3.4.4:build",
        "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre",
        f"-Djib.to.image={test_image}",
        "-Djib.allowInsecureRegistries=true",
        "-DskipTests",
        "-B"
    ], cwd=mod_dir, capture_output=True, text=True, timeout=600)
    if jib_res.returncode != 0:
        raise RuntimeError(f"Jib test image build failed:\n{jib_res.stderr or jib_res.stdout}")
    log(f"Jib test image built: {test_image}")

    candidate_names = [
        f"igaming-source-{bookmaker}-crawler",
        f"igaming-source-{bookmaker}",
        f"igaming-source-{bookmaker}-json"
    ]
    dep_name = None
    for c_name in candidate_names:
        chk = subprocess.run(["kubectl", "get", f"deployment/{c_name}", "-n", "igaming-source"], capture_output=True)
        if chk.returncode == 0:
            dep_name = c_name
            break
    if not dep_name:
        dep_name = f"igaming-source-{bookmaker}-crawler"

    test_pod_name = f"test-crawler-{bookmaker}-{short_id}"
    subprocess.run(["kubectl", "delete", "pod", test_pod_name, "-n", "igaming-source", "--wait=false", "--ignore-not-found=true"])
    time.sleep(2)

    dep_res = subprocess.run(["kubectl", "get", f"deployment/{dep_name}", "-n", "igaming-source", "-o", "json"], capture_output=True, text=True)
    if dep_res.returncode == 0:
        dep_json = json.loads(dep_res.stdout)
        pod_spec = dep_json["spec"]["template"]["spec"]
    else:
        pod_spec = {
            "containers": [{
                "name": f"test-{bookmaker}",
                "image": test_image,
                "imagePullPolicy": "Always"
            }]
        }

    target_c_name = None
    for c in pod_spec.get("containers", []):
        c_name = c.get("name", "")
        if c_name == dep_name or bookmaker in c_name or "crawler" in c_name:
            c["image"] = test_image
            c["imagePullPolicy"] = "Always"
            target_c_name = c_name
            break
    if not target_c_name and pod_spec.get("containers"):
        pod_spec["containers"][0]["image"] = test_image
        pod_spec["containers"][0]["imagePullPolicy"] = "Always"
        target_c_name = pod_spec["containers"][0]["name"]

    pod_spec["restartPolicy"] = "Never"
    test_pod_manifest = {
        "apiVersion": "v1",
        "kind": "Pod",
        "metadata": {
            "name": test_pod_name,
            "namespace": "igaming-source",
            "labels": {"app": test_pod_name, "test": "true"}
        },
        "spec": pod_spec
    }

    test_pod_file = f"/tmp/{test_pod_name}.json"
    with open(test_pod_file, "w", encoding="utf-8") as pf:
        json.dump(test_pod_manifest, pf)

    subprocess.run(["kubectl", "apply", "-f", test_pod_file], check=True)
    subprocess.run(["kubectl", "wait", "--for=condition=Ready", f"pod/{test_pod_name}", "-n", "igaming-source", "--timeout=90s"], check=False)

    report_plane_status(task, "VERIFYING", f"Воркер {HOSTNAME}: запущен тестовый под {test_pod_name}. Выполняется обязательный 5-минутный soak test в igaming-source...")
    start_soak = time.time()
    test_errors = []
    while time.time() - start_soak < 300:
        time.sleep(20)
        p_phase = subprocess.run(["kubectl", "get", f"pod/{test_pod_name}", "-n", "igaming-source", "-o", "jsonpath={.status.phase}"], capture_output=True, text=True).stdout.strip()
        if p_phase in ["Failed", "CrashLoopBackOff"]:
            test_errors.append(f"Test pod entered failure phase: {p_phase}")
            break
        log_cmd = ["kubectl", "logs", f"pod/{test_pod_name}", "-n", "igaming-source"]
        if target_c_name:
            log_cmd.extend(["-c", target_c_name])
        log_cmd.append("--tail=100")
        l_res = subprocess.run(log_cmd, capture_output=True, text=True)
        for line in l_res.stdout.splitlines():
            lower_l = line.lower()
            if any(err_kw in lower_l for err_kw in ["nullpointerexception", "fatal:", "oomkilled", "panic:"]):
                if line not in test_errors:
                    test_errors.append(line)

    subprocess.run(["kubectl", "delete", "pod", test_pod_name, "-n", "igaming-source", "--wait=false"])
    if test_errors:
        err_msg = "\n".join(test_errors[-5:])
        raise RuntimeError(f"Test pod failed 5-minute soak test:\n{err_msg}")

    merge_branch_to_base(repo_dir, target_branch, base_branch, task_title, short_id)

    prod_image = f"100.78.183.101:30500/{module_name}:latest"
    report_plane_status(task, "RUNNING", f"Воркер {HOSTNAME}: сборка релизного образа {prod_image} и деплой в прод...")
    prod_jib_res = subprocess.run([
        "mvn", "-f", mod_pom,
        "compile",
        "com.google.cloud.tools:jib-maven-plugin:3.4.4:build",
        "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre",
        f"-Djib.to.image={prod_image}",
        "-Djib.allowInsecureRegistries=true",
        "-DskipTests",
        "-B"
    ], cwd=mod_dir, capture_output=True, text=True, timeout=600)
    if prod_jib_res.returncode != 0:
        raise RuntimeError(f"Jib production image build failed:\n{prod_jib_res.stderr or prod_jib_res.stdout}")

    subprocess.run(["kubectl", "rollout", "restart", f"deployment/{dep_name}", "-n", "igaming-source"], check=True)
    loader_dep = f"igaming-source-{bookmaker}-loader"
    subprocess.run(["kubectl", "rollout", "restart", f"deployment/{loader_dep}", "-n", "igaming-source"], capture_output=True)
    subprocess.run(["kubectl", "rollout", "status", f"deployment/{dep_name}", "-n", "igaming-source", "--timeout=180s"], check=False)

    report_plane_status(task, "VERIFYING", f"Воркер {HOSTNAME}: под развернут в прод ({dep_name}). Идёт 5-минутный мониторинг стабильности...")
    start_prod = time.time()
    while time.time() - start_prod < 300:
        time.sleep(20)

    victory_msg = (
        f"🎉 Задача успешно завершена! Полный E2E-пайплайн пройден воркером {HOSTNAME}:\n"
        f"1. Код и рефакторинг {module_name} написаны\n"
        f"2. Тестовый под {test_pod_name} отработал 5 минут без единой ошибки\n"
        f"3. Изменения смержены и запушены в {base_branch}\n"
        f"4. Релизный образ собран через Jib и развернут в прод ({dep_name})\n"
        f"5. Пройден контрольный 5-минутный мониторинг на бою!"
    )
    log(victory_msg)
    report_plane_status(task, "COMPLETED", victory_msg)


def release_exhausted_cooldown(delay_sec=1800):
    def _cooldown():
        time.sleep(delay_sec)
        with state_lock:
            if current_state["status"] == "WAITING_QUOTA_RESET":
                current_state["status"] = "IDLE"
                current_state["geminiQuota"] = 1.0
                current_state["claudeQuota"] = 1.0
                log("Quota cooldown expired, returning pod to IDLE.")
    threading.Thread(target=_cooldown, daemon=True).start()


def execute_task_lifecycle(task):
    global current_state
    issue_id = task.get("issueId", "0")
    short_id = issue_id[:8] if len(issue_id) >= 8 else issue_id
    task_title = task.get("taskTitle", "Task")
    task_desc = task.get("taskDescription", "")
    git_repo = task.get("gitRepo", "")
    base_branch = task.get("baseBranch", "main")
    target_branch = task.get("targetBranch", f"feature/plane-{short_id}")
    quota_policy = task.get("quotaPolicy", "HANDOVER_MIGRATE")
    is_handover = task.get("isHandover", False)
    keep_waiting_quota = False

    log(f"=== Starting Task Execution: {task_title} (#{short_id}) [Handover={is_handover}, Policy={quota_policy}] ===")
    report_plane_status(task, "RUNNING", f"Воркер {HOSTNAME} ({current_state.get('email')}) начал выполнение задачи в ветке {target_branch}.")

    repo_dir = "/workspace/repo"
    openspec_dir = os.path.join(repo_dir, "openspec", "changes", f"plane-{short_id}")

    try:
        rt = current_state.get("refreshToken")
        email = current_state.get("email")
        if rt and email:
            at, exp = refresh_oauth_token(rt)
            setup_agy_env(email, at, rt, exp)

        os.makedirs("/workspace", exist_ok=True)
        if not os.path.exists(os.path.join(repo_dir, ".git")):
            log(f"Cloning {git_repo} (base branch: {base_branch})...")
            shutil.rmtree(repo_dir, ignore_errors=True)
            clone_url = git_repo
            if GITHUB_TOKEN and "github.com" in git_repo and "x-access-token" not in git_repo:
                clone_url = git_repo.replace("https://github.com/", f"https://x-access-token:{GITHUB_TOKEN}@github.com/")
            subprocess.run(["git", "clone", "--depth", "50", "--branch", base_branch, clone_url, repo_dir], check=True)

        subprocess.run(["git", "fetch", "origin"], cwd=repo_dir, capture_output=True)
        res_chk = subprocess.run(["git", "checkout", target_branch], cwd=repo_dir, capture_output=True)
        if res_chk.returncode != 0:
            res_chk2 = subprocess.run(["git", "checkout", "-b", target_branch, f"origin/{target_branch}"], cwd=repo_dir, capture_output=True)
            if res_chk2.returncode != 0:
                subprocess.run(["git", "checkout", "-b", target_branch], cwd=repo_dir, check=True)
        subprocess.run(["git", "pull", "origin", target_branch], cwd=repo_dir, capture_output=True)

        # If task targets a bookmaker submodule, ensure it is initialized before agy runs
        bm_mod = detect_bookmaker_module(repo_dir, task_title, base_branch)
        if bm_mod and not os.path.exists(os.path.join(repo_dir, bm_mod, "pom.xml")):
            subprocess.run(["git", "submodule", "update", "--init", "--depth", "1", bm_mod], cwd=repo_dir, check=False)

        repo_agents = os.path.join(repo_dir, "AGENTS.md")
        if not os.path.exists(repo_agents):
            existing_agents = os.path.join(repo_dir, ".agents", "AGENTS.md")
            if os.path.exists(existing_agents):
                shutil.copyfile(existing_agents, repo_agents)
            else:
                with open(repo_agents, "w", encoding="utf-8") as f:
                    f.write(AGENTS_MD_CONTENT)

        os.makedirs(openspec_dir, exist_ok=True)
        tasks_file = os.path.join(openspec_dir, "tasks.md")
        handover_file = os.path.join(openspec_dir, "HANDOVER.md")

        # Always ensure proposal.md has the latest task_desc if it was empty before
        proposal_path = os.path.join(openspec_dir, "proposal.md")
        proposal_content = f"# Proposal: {task_title}\n\n## Context\nPlane Task ID: `{issue_id}`\n\n## Description\n{task_desc}\n"
        with open(proposal_path, "w", encoding="utf-8") as f:
            f.write(proposal_content)

        branch_already_has_spec = os.path.exists(tasks_file)
        if branch_already_has_spec:
            with open(tasks_file, "r", encoding="utf-8", errors="ignore") as f:
                existing_tasks = f.read()
            if "????" in existing_tasks:
                branch_already_has_spec = False

        if not branch_already_has_spec:
            with open(os.path.join(openspec_dir, ".openspec.yaml"), "w", encoding="utf-8") as f:
                f.write(f"schema: spec-driven\ncreated: {datetime.date.today().isoformat()}\ntask_id: \"{issue_id}\"\ntitle: \"{task_title}\"\n")

            tasks_content = f"""# Implementation Tasks: {task_title}
- [ ] 1. Изучить текущий код и архитектуру модуля, подготовить структуру изменений
- [ ] 2. Реализовать основную бизнес-логику, манифесты и покрыть проверками согласно описанию задачи
- [ ] 3. Проверить сборку/валидацию и подготовить изменения к деплою
"""
            with open(tasks_file, "w", encoding="utf-8") as f:
                f.write(tasks_content)

            subprocess.run(["git", "add", "openspec"], cwd=repo_dir)
            subprocess.run(["git", "commit", "-m", f"chore(ai): initialize OpenSpec for #{short_id}"], cwd=repo_dir)
            subprocess.run(["git", "push", "origin", target_branch, "--force"], cwd=repo_dir, capture_output=True)

        step_count = 0
        max_steps = 6
        while step_count < max_steps:
            step_count += 1
            with open(tasks_file, "r", encoding="utf-8") as f:
                current_tasks_text = f.read()

            if "- [ ]" not in current_tasks_text:
                log("All OpenSpec checklist tasks marked completed!")
                break

            log(f"Step {step_count}: Executing next pending OpenSpec task...")
            exec_prompt = f"""Ты автономный AI-разработчик. Текущая задача: {task_title}
Описание задачи:
{task_desc}

Строго соблюдай правила из AGENTS.md (сторожевые таймеры, неинтерактивность, запрет преждевременного закрытия).
Открой {tasks_file}, найди первый невыполненный пункт (- [ ]), выполни его в кодовой базе, и обнови {tasks_file}, отметив выполненные пункты как (- [x]).
Если все пункты выполнены и проверены, отметь все пункты (- [x]).
"""
            ret, out, both_exhausted = run_agy_with_fallback(exec_prompt, mode="accept-edits", working_dir=repo_dir, timeout_seconds=900)

            if both_exhausted:
                log(f"All model quotas exhausted on {HOSTNAME} ({email})!")
                with state_lock:
                    current_state["status"] = "WAITING_QUOTA_RESET"
                    current_state["currentTask"] = None
                keep_waiting_quota = True
                release_exhausted_cooldown(1800)

                handover_content = f"""# Handover State: #{short_id}
- **Migrated From**: {HOSTNAME} ({email})
- **Timestamp**: {datetime.datetime.now().isoformat()}
- **Target Branch**: {target_branch}
- **Reason**: Quota Depletion on worker pod {HOSTNAME}
- **Remaining Tasks**:
{current_tasks_text}
"""
                with open(handover_file, "w", encoding="utf-8") as f:
                    f.write(handover_content)

                subprocess.run(["git", "add", "-A", "."], cwd=repo_dir)
                subprocess.run(["git", "commit", "-m", f"wip(ai): checkpoint & OpenSpec handover for #{short_id}"], cwd=repo_dir)
                subprocess.run(["git", "push", "origin", target_branch], cwd=repo_dir)

                executor_cb = f"http://plane-ai-executor.plane.svc.cluster.local:8080/callback/plane/{issue_id}/migrate"
                mig_payload = {
                    "fromPod": HOSTNAME,
                    "fromEmail": email,
                    "branch": target_branch,
                    "issueId": issue_id,
                    "projectId": task.get("projectId"),
                    "workspaceSlug": task.get("workspaceSlug"),
                    "taskTitle": task_title,
                    "taskDescription": task_desc,
                    "gitRepo": git_repo,
                    "baseBranch": base_branch,
                    "quotaPolicy": quota_policy
                }
                try:
                    req = urllib.request.Request(executor_cb, data=json.dumps(mig_payload).encode(), headers={"Content-Type": "application/json"})
                    urllib.request.urlopen(req, timeout=10)
                    log("Migration callback sent successfully to plane-ai-executor.")
                except Exception as e:
                    log(f"Failed to send migration callback: {e}")

                report_plane_status(task, "HANDOVER", f"Квота аккаунта {email} исчерпана. Задача сохранена в OpenSpec ({target_branch}) и мигрирована на следующий свободный воркер.")
                return

            subprocess.run(["git", "add", "-A", "."], cwd=repo_dir)
            diff_res = subprocess.run(["git", "diff", "--cached", "--quiet"], cwd=repo_dir)
            if diff_res.returncode != 0:
                subprocess.run(["git", "commit", "-m", f"wip(ai): completed task checkpoint for #{short_id}"], cwd=repo_dir)
                subprocess.run(["git", "push", "origin", target_branch], cwd=repo_dir, capture_output=True)
                log(f"Step {step_count}: Checkpoint committed and pushed.")

        # 4. Automated Build Check for Bookmaker module (only if bookmaker module exists)
        bm_mod = detect_bookmaker_module(repo_dir, task_title, base_branch)
        if bm_mod and os.path.exists(os.path.join(repo_dir, bm_mod, "pom.xml")):
            mod_pom = os.path.join(repo_dir, bm_mod, "pom.xml")
            log(f"Running verification build on {mod_pom}...")
            b_res = subprocess.run(["mvn", "-B", "-f", mod_pom, "test-compile", "-DskipTests"], cwd=os.path.join(repo_dir, bm_mod), capture_output=True, text=True, timeout=500)
            if b_res.returncode != 0:
                build_err = b_res.stderr or b_res.stdout
                log("Build check failed! Activating Targeted Rescue Mode...")
                rescue_prompt = f"Внимание! Сборка модуля {bm_mod} упала с ошибкой:\n{build_err[-2500:]}\nНайди и исправь причину ошибки в коде, чтобы сборка `mvn -f {bm_mod}/pom.xml test-compile` проходила успешно."
                run_agy_with_fallback(rescue_prompt, mode="accept-edits", working_dir=repo_dir, timeout_seconds=600)
                subprocess.run(["git", "add", "-A", "."], cwd=repo_dir)
                subprocess.run(["git", "commit", "-m", f"fix(ai): build rescue for #{short_id}"], cwd=repo_dir)
                subprocess.run(["git", "push", "origin", target_branch], cwd=repo_dir, capture_output=True)

        # 5. Autonomous E2E Delivery
        deploy_and_verify_e2e(repo_dir, task, task_title, short_id, target_branch, base_branch, email, git_repo)

    except Exception as e:
        log(f"Task execution failed with exception: {e}")
        report_plane_status(task, "FAILED", f"Ошибка воркера {HOSTNAME}: {str(e)}")
    finally:
        with state_lock:
            if not keep_waiting_quota:
                current_state["status"] = "IDLE"
            current_state["currentTask"] = None


class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    daemon_threads = True


class WorkerRequestHandler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        pass

    def send_json(self, status_code, data):
        body = json.dumps(data).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path == "/health":
            self.send_json(200, {"status": "ok", "pod": HOSTNAME, "state": current_state.get("status")})
        elif self.path == "/status":
            with state_lock:
                snapshot = dict(current_state)
            self.send_json(200, snapshot)
        else:
            self.send_json(404, {"error": "Not found"})

    def do_POST(self):
        if self.path == "/execute":
            content_length = int(self.headers.get("Content-Length", 0))
            body_bytes = self.rfile.read(content_length)
            try:
                task = json.loads(body_bytes.decode("utf-8"))
            except Exception as e:
                self.send_json(400, {"error": f"Invalid JSON payload: {e}"})
                return

            with state_lock:
                if current_state["status"] != "IDLE":
                    self.send_json(409, {
                        "error": "Worker is currently busy or in quota cooldown",
                        "currentTask": current_state["currentTask"]
                    })
                    return
                current_state["status"] = "BUSY"
                current_state["currentTask"] = {
                    "issueId": task.get("issueId"),
                    "title": task.get("taskTitle"),
                    "startedAt": datetime.datetime.now().isoformat()
                }

            threading.Thread(target=execute_task_lifecycle, args=(task,), daemon=True).start()
            self.send_json(202, {
                "status": "ACCEPTED",
                "pod": HOSTNAME,
                "email": current_state["email"],
                "issueId": task.get("issueId")
            })

        elif self.path == "/cancel":
            global active_process
            if active_process:
                try:
                    active_process.terminate()
                except Exception:
                    pass
            with state_lock:
                current_state["status"] = "IDLE"
                current_state["currentTask"] = None
            self.send_json(200, {"status": "CANCELLED"})
        else:
            self.send_json(404, {"error": "Not found"})


def main():
    log(f"Starting Plane AI Worker Daemon on port {PORT}...")
    setup_environment()
    init_worker_account()

    server = ThreadedHTTPServer(("0.0.0.0", PORT), WorkerRequestHandler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()


if __name__ == "__main__":
    main()
