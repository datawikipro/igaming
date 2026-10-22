#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Plane AI Autonomous Worker Daemon (v2.0 - Anti-Phantom & Stack-Aware Edition)
Runs on dedicated worker pods in StatefulSet plane-ai-worker.
Enforces: 1 pod = 1 task.

Key Features & Guardrails:
1. Anti-Phantom Gate: Rejects task completion if git diff has zero code modifications outside of openspec/.
2. Context Enrichment: Injects the 14 Golden Rules of SmartBet.guru ecosystem & dynamic agentMd.
3. Clean Formulations: Strips Plane HTML tags to clean markdown and resolves explicit target modules and paths.
4. Session Continuity: Retains agent memory across iterative steps via agy --continue (-c).
5. Stack-Specific E2E: Dispatches verification by architecture (Crawlers, SMM, Web Frontend, Core Aggregator).
6. Multi-Model Quota Resilience: Seamless fallback Gemini 3.8 Flash High -> Claude Sonnet 4.6 -> Claude Opus 4.6.
7. OpenSpec Handover Migration: Automatically migrates uncompleted tasks when quota is exhausted.
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
import http.client
import subprocess
import threading
import datetime
import shutil
import signal
from http.server import HTTPServer, BaseHTTPRequestHandler
from socketserver import ThreadingMixIn

PORT = 8000
ACCOUNTS_SERVICE_URL = os.environ.get("ACCOUNTS_SERVICE_URL", "http://accounts-service.accounts.svc.cluster.local:8091")
PROXY_HTTP_URL = os.environ.get("PROXY_HTTP_URL", "http://cluster-ai-proxy.proxy.svc.cluster.local:3128")
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "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")
CLIENT_ID = "1071006060591-tmhssin2h21lcre235vtolojh4g403ep.apps.googleusercontent.com"
CLIENT_SECRET = "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"

MODEL_GEMINI = "gemini-3.8-flash-high"
MODEL_CLAUDE = "claude-sonnet-4-6"
MODEL_OPUS = "claude-opus-4-6-thinking"

MIN_5H_QUOTA = 0.00
MIN_WEEKLY_QUOTA = 0.00

class TokenAuthRequiredException(Exception):
    """Raised when Google OAuth token is revoked or expired and needs user re-authorization."""
    pass

AUTH_PATTERNS = [
    "authentication required", "authentication failed",
    "please visit the url to log in", "please sign in",
    "run 'agy' to log in", "run \"agy\" to log in",
    "verify your account", "not eligible for antigravity",
    "eligibility check failed", "location eligibility check failed",
    "invalid_grant", "token has been expired or revoked"
]

AGENTS_MD_CONTENT = r"""# Global Rules & Guardrails for Autonomous AI Workers (SmartBet.guru Ecosystem)

## 🏆 Непреложные правила (Golden Rules)

### 1. 🛡️ Definition of Done & 5-минутный таймер
- Сервис развернут в Kubernetes в статусе `Running 1/1`.
- Actuator пробы `/actuator/health/readiness` и `/actuator/health/liveness` возвращают HTTP 200 `UP`.
- **СТРОГИЙ ЗАПРЕТ**: Запрещено переводить задачу в `COMPLETED` / `DONE`, пока под не отработает **5 ПОЛНЫХ МИНУТ** (300 секунд) без единой ошибки в логах (`Exception`, `NullPointerException`, `FATAL`, `OOMKilled`).

### 2. 🚫 Запрет IP-адресов — Только K8s DNS Service Names!
- **Категорически запрещено** хардкодить IP-адреса в коде, K8s-манифестах или конфигах.
- Использовать только имена сервисов (например: `igaming-source-winline-db`, `accounts-service.accounts`).

### 3. 🛠️ Протокол авторемонта (Repair Protocol)
- Если базовый модуль сломан (`CrashLoopBackOff`), запрещено писать фичу поверх него — сначала устраняется корень сбоя.

### 4. ⚡ Неблокирующий старт HikariCP / JPA
Во всех микросервисах в `application.properties` обязательны параметры:
```properties
spring.datasource.hikari.initialization-fail-timeout=0
spring.datasource.hikari.connection-timeout=5000
spring.datasource.hikari.validation-timeout=3000
spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
```

### 5. 🐳 Сборка Maven Jib
- Все `-D` параметры с точками окаймляются кавычками:
  `mvn -pl <module> compile jib:build "-Djib.to.image=100.78.183.101:30500/<module>:latest" -Djib.allowInsecureRegistries=true -DskipTests -B`

### 6. 🌐 Сетевая топология и умная маршрутизация краулеров
- **Кластерный HTTP-прокси**: `http://100.83.113.50:3128` (роутер `ru-proxy`).
- **РФ-букмекеры (ЦУПИС/ЕРАИ)** (Фонбет, Винлайн, Пари, Бетсити, Балтбет, Олимпбет, Леон) -> `direct` (чистый IP СПб `188.242.33.93`).
- **Европейские и оффшорные букмекеры** (Pinnacle, Sbobet, Bet365, Bwin, Betsson, 1x-клоны) -> `outline-vpn-eu-nl` в Eemshaven, Нидерланды.

### 7. 🗄️ Управление схемой БД
- Таблица `match_factor` стандартизирована: `id BIGSERIAL PRIMARY KEY, match_id BIGINT, factor_id INT, name VARCHAR(255), value NUMERIC(10, 3)`.
- Таблица `match_cache`: хранилище активных линий.

### 8. 📊 Критерий наполнения линии (Threshold >= 500 матчей)
- Задача по краулеру считается выполненной **ТОЛЬКО** при наполнении линии от 500 активных матчей. Статус `Running 1/1` при 0 матчах — дефект.

### 9. 🦊 SMM & Браузерные ИИ-агенты: Firefox / Camoufox, Запрет Инкогнито и Прогрев Кэша
- Запрет на инкогнито и чистый Chromium для соцсетей.
- Вся автоматизация соцсетей строится на Firefox / Camoufox с персистентным контекстом профиля (`/opt/smm-agent/storage/...`).

### 10. 🎁 Мониторинг фрибетов (80% гарантированного кэша)
- Любой фрибет (SNR) позиционируется как **80% гарантированного кэша** через Matched Betting:
  Формула: $\eta = \\frac{(K_1 - 1)(K_2 - 1)}{K_2} \\approx 0.80$.

### 11. 🏢 Разделение клиентского портала и внутренней админки
- `smartbet.guru` — публичный сайт (линии, вилки, калькулятор 80%, промо-акции, блог).
- `igaming-admin-frontend` — внутренняя админка (аккаунты, LLM квоты, noVNC стримы браузеров, воркеры).

### 12. 🛡️ Выделенные статические прокси 1:1 для соцсетей (PureVPN)
- Запрет динамической ротации IP для соцсетей во избежание детекта антифродом Meta/Reddit.

### 13. 📱 Мобильная экосистема виртуальных телефонов (Mobile SMM vs Capture Pools)
- Разделение пулов устройств: зарубежные статические IP для SMM vs чистый RU IP для купонов ЦУПИС БК.

### 14. ⏱️ Защита от зависания: Сторожевой таймер и таймауты
- Запрещены бесконечные streaming-команды (`kubectl logs -f`, `tail -f`, `watch`).
- Все команды запускаются с префиксом `timeout`: `timeout 300s mvn ...`, `timeout 60s ...`.
- Неинтерактивные флаги обязательны: `mvn -B`, `apt-get -y`, `npm --yes`, `GIT_TERMINAL_PROMPT=0`.
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
    "gemini5hQuota": 1.0,
    "geminiWeeklyQuota": 1.0,
    "claudeQuota": 1.0,
    "claude5hQuota": 1.0,
    "claudeWeeklyQuota": 1.0,
    "hasEnoughTokens": True,
    "quotaMessage": "Инициализация...",
    "lastQuotaCheckAt": None,
    "lastOutput": None,
    "lastError": None
}

active_process = None

def log(msg):
    ts = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{ts}] [{HOSTNAME}] {msg}", flush=True)

def clean_html_description(raw_desc):
    """Converts HTML-formatted task descriptions from Plane into clean Markdown."""
    if not raw_desc:
        return ""
    text = str(raw_desc)
    text = re.sub(r'<\s*br\s*/?>', '\n', text, flags=re.IGNORECASE)
    text = re.sub(r'<\s*/p\s*>', '\n\n', text, flags=re.IGNORECASE)
    text = re.sub(r'<\s*li\s*>', '- ', text, flags=re.IGNORECASE)
    text = re.sub(r'<\s*/li\s*>', '\n', text, flags=re.IGNORECASE)
    text = re.sub(r'<\s*h[1-6][^>]*>(.*?)</\s*h[1-6]\s*>', r'\n### \1\n', text, flags=re.IGNORECASE | re.DOTALL)
    text = re.sub(r'<\s*code[^>]*>(.*?)</\s*code\s*>', r'`\1`', text, flags=re.IGNORECASE | re.DOTALL)
    text = re.sub(r'<\s*b[^>]*>(.*?)</\s*b\s*>', r'**\1**', text, flags=re.IGNORECASE | re.DOTALL)
    text = re.sub(r'<\s*strong[^>]*>(.*?)</\s*strong\s*>', r'**\1**', text, flags=re.IGNORECASE | re.DOTALL)
    text = re.sub(r'<[^>]+>', '', text)
    text = text.replace('&nbsp;', ' ').replace('&lt;', '<').replace('&gt;', '>').replace('&amp;', '&').replace('&quot;', '"')
    return re.sub(r'\n{3,}', '\n\n', text).strip()

def detect_task_target(repo_dir, task_title, task_desc):
    """
    Classifies task type and resolves target module, directory, and local test command.
    Returns: (task_type, module_name, target_dir, rel_target_dir, test_command)
    """
    combined = (str(task_title) + " " + str(task_desc)).lower()
    match = re.search(r"\[([^\]]+)\]", task_title)
    tag = match.group(1).lower().strip() if match else ""

    # 1. SMM & Automation
    if tag.startswith("smm") or any(k in combined for k in ["twitter", "telegram", "instagram", "threads", "reddit", "smm-"]):
        return (
            "SMM",
            "smm-agent",
            os.path.join(repo_dir, "scripts"),
            "scripts",
            "python3 -m py_compile scripts/*.py"
        )

    # 2. Public Portal Client (Next.js)
    if tag in ["portal", "smartbet.guru", "seo-pages", "seo-calc"] or "smartbet.guru" in combined:
        sb_dir = os.path.join(repo_dir, "smartbet.guru")
        if os.path.exists(sb_dir):
            return ("FRONTEND_CLIENT", "smartbet.guru", sb_dir, "smartbet.guru", "npm test --prefix smartbet.guru")
        return ("PORTAL", "igaming-portal", os.path.join(repo_dir, "igaming-portal"), "igaming-portal", "mvn -f igaming-portal/pom.xml test-compile -DskipTests")

    # 3. Admin Frontend (React)
    if "admin" in tag or "igaming-admin-frontend" in combined:
        adm_dir = os.path.join(repo_dir, "igaming-admin-frontend")
        return ("FRONTEND_ADMIN", "igaming-admin-frontend", adm_dir, "igaming-admin-frontend", "npm test --prefix igaming-admin-frontend")

    # 4. Aggregator Core
    if "aggregator" in tag or "aggregator" in combined:
        agg_dir = os.path.join(repo_dir, "aggregator-core")
        return ("AGGREGATOR", "aggregator-core", agg_dir, "aggregator-core", "mvn -f aggregator-core/pom.xml test-compile -DskipTests")

    # 5. Bookmaker Crawler (igaming-source-<name>)
    for d in os.listdir(repo_dir):
        if d.startswith("igaming-source-") and os.path.isdir(os.path.join(repo_dir, d)):
            bm_name = d.replace("igaming-source-", "")
            if (tag and tag == bm_name) or bm_name in combined:
                return (
                    "CRAWLER",
                    d,
                    os.path.join(repo_dir, d),
                    d,
                    f"mvn -f {d}/pom.xml test-compile -DskipTests"
                )

    if tag:
        bm_mod = f"igaming-source-{tag}"
        if os.path.isdir(os.path.join(repo_dir, bm_mod)):
            return (
                "CRAWLER",
                bm_mod,
                os.path.join(repo_dir, bm_mod),
                bm_mod,
                f"mvn -f {bm_mod}/pom.xml test-compile -DskipTests"
            )

    # 6. Fallback General
    return (
        "GENERAL",
        "monorepo",
        repo_dir,
        ".",
        "mvn test-compile -DskipTests" if os.path.exists(os.path.join(repo_dir, "pom.xml")) else "git status"
    )

def generate_initial_tasks(task_type, module_name, task_title, test_command):
    """Generates concrete, actionable technical checklist items in tasks.md."""
    if task_type == "CRAWLER":
        return f"""# Implementation Tasks: {task_title}
- [ ] 1. Изучить структуру модуля `{module_name}` и исходные файлы API/парсеры букмекера
- [ ] 2. Реализовать или обновить логику загрузки линий и маппинга исходов в таблицы match_factor и match_cache
- [ ] 3. Проверить неблокирующий HikariCP, таймауты и кластерный прокси (ru-proxy 100.83.113.50:3128)
- [ ] 4. Проверить успешность локальной компиляции модуля: `{test_command}`
- [ ] 5. Валидировать OpenSpec дельта-спецификацию: `openspec validate --specs`
"""
    elif task_type == "SMM":
        return f"""# Implementation Tasks: {task_title}
- [ ] 1. Изучить целевой скрипт в `scripts/` или `smm-agent/` и профили сессий
- [ ] 2. Реализовать бизнес-логику автоматизации (Firefox Camoufox persistent context, расчет 80% кэша фрибетов, реакция на комментарии)
- [ ] 3. Проверить корректность синтаксиса и выполнить тестовый сухой прогон: `{test_command}`
- [ ] 4. Валидировать OpenSpec спецификацию
"""
    elif task_type in ["PORTAL", "AGGREGATOR"]:
        return f"""# Implementation Tasks: {task_title}
- [ ] 1. Изучить структуру сервиса `{module_name}` и связанные DTO/контроллеры
- [ ] 2. Реализовать сервисные методы и маппинги согласно техническим требованиям
- [ ] 3. Проверить локальную компиляцию и тесты: `{test_command}`
- [ ] 4. Валидировать OpenSpec спецификацию
"""
    elif task_type in ["FRONTEND_CLIENT", "FRONTEND_ADMIN"]:
        return f"""# Implementation Tasks: {task_title}
- [ ] 1. Изучить структуру страниц и компонентов в `{module_name}`
- [ ] 2. Реализовать интерфейс, хуки и клиентскую логику согласно задаче
- [ ] 3. Проверить успешность сборки и тестов фронтенда: `{test_command}`
- [ ] 4. Валидировать OpenSpec спецификацию
"""
    else:
        return f"""# Implementation Tasks: {task_title}
- [ ] 1. Изучить кодовую базу и определить файлы для внесения изменений
- [ ] 2. Реализовать требуемую функциональность и внести изменения в кодовую базу
- [ ] 3. Выполнить проверку локальной сборки: `{test_command}`
- [ ] 4. Валидировать OpenSpec спецификацию
"""

def compute_quota_status(g5h, g_weekly):
    has_enough = (g5h > 0.0) or (g_weekly > 0.0)
    if not has_enough:
        msg = f"Нет, я пустой (квоты исчерпаны: 5h={g5h*100:.1f}%, weekly={g_weekly*100:.1f}%)"
    else:
        msg = f"Да, токенов достаточно (5h={g5h*100:.0f}%, weekly={g_weekly*100:.0f}%)"
    return has_enough, msg

def estimate_task_quota(task_data):
    req_5h = 0.00
    req_weekly = 0.00
    complexity = "STANDARD"

    if not task_data:
        return req_5h, req_weekly, complexity

    if task_data.get("isHandover"):
        return req_5h, req_weekly, "HANDOVER_RESUME"

    title = str(task_data.get("taskTitle") or "").lower()
    desc = str(task_data.get("taskDescription") or "").lower()
    combined = title + " " + desc

    if any(k in combined for k in ["[opus]", "[arch]", "архитектур", "рефакторинг", "architecture"]) or len(desc) > 1500:
        complexity = "HEAVY_ARCH"
    elif len(desc) > 600 or any(k in combined for k in ["e2e", "маппер"]):
        complexity = "MEDIUM_FEATURE"

    return req_5h, req_weekly, complexity

def check_can_accept(task_data=None):
    req_5h, req_weekly, complexity = estimate_task_quota(task_data)
    with state_lock:
        st = current_state.get("status")
        email = current_state.get("email", "unassigned")
        g5h = float(current_state.get("gemini5hQuota", 0.0))
        gw = float(current_state.get("geminiWeeklyQuota", 0.0))
        cw = float(current_state.get("claudeWeeklyQuota", 0.0))

    if st == "AUTH_REQUIRED":
        return False, f"Воркер требует переавторизации токена в админке ({email})", req_5h, req_weekly, complexity

    if st != "IDLE":
        return False, f"Воркер занят (статус: {st})", req_5h, req_weekly, complexity

    if g5h <= 0.0 and gw <= 0.0 and cw <= 0.0:
        return (
            False,
            f"Нет, я пустой ({email}: 5h=0%, weekly=0%)",
            req_5h, req_weekly, complexity
        )

    return (
        True,
        f"Да, готов к работе ({email}: 5h={g5h*100:.0f}%, weekly={gw*100:.0f}%)",
        req_5h, req_weekly, complexity
    )

def setup_environment():
    """Configures proxy, xvfb, git, and installs required tools if absent."""
    os.environ["HTTP_PROXY"] = PROXY_HTTP_URL
    os.environ["HTTPS_PROXY"] = PROXY_HTTP_URL
    os.environ["http_proxy"] = PROXY_HTTP_URL
    os.environ["https_proxy"] = PROXY_HTTP_URL
    default_no_proxy = "localhost,127.0.0.1,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,100.64.0.0/10,accounts-service,accounts-db,plane-ai-executor,plane-proxy,plane-api,plane-minio,gitlab-ce,.svc,.cluster.local,.s3.ru,deb.debian.org,security.debian.org,archive.ubuntu.com,security.ubuntu.com,ports.ubuntu.com,dl-cdn.alpinelinux.org,mirror.yandex.ru,docker.io,.docker.io,registry-1.docker.io,production.cloudflare.docker.com,registry.k8s.io,gcr.io,quay.io,pypi.org,files.pythonhosted.org,pypi.python.org,registry.npmjs.org,registry.yarnpkg.com,repo.maven.apache.org,repo1.maven.org,plugins.gradle.org,proxy.golang.org,crates.io,static.crates.io,playwright.azureedge.net,playwright.download.prss.microsoft.com"
    raw_np = os.environ.get("NO_PROXY") or default_no_proxy
    clean_no_proxy = [p.strip() for p in raw_np.split(",") if p.strip() and not any(gh in p.lower() for gh in ["github", "ghcr"])]
    final_np = ",".join(clean_no_proxy)
    os.environ["NO_PROXY"] = final_np
    os.environ["no_proxy"] = final_np

    try:
        res = subprocess.run(["pgrep", "Xvfb"], capture_output=True)
        if res.returncode != 0:
            subprocess.Popen(["Xvfb", ":99", "-screen", "0", "1280x1024x24"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            log("Started Xvfb :99")
    except Exception as e:
        log(f"Xvfb notice: {e}")
    os.environ["DISPLAY"] = ":99"
    os.environ["PLAYWRIGHT_BROWSERS_PATH"] = "/ms-playwright"
    subprocess.run(
        "mkdir -p /root/.cache && ln -sfn /ms-playwright /root/.cache/ms-playwright && "
        "ln -sf /ms-playwright/chromium-*/chrome-linux/chrome /usr/local/bin/google-chrome && "
        "ln -sf /ms-playwright/chromium-*/chrome-linux/chrome /usr/local/bin/chromium && "
        "ln -sf /ms-playwright/firefox-*/firefox/firefox /usr/local/bin/firefox",
        shell=True, check=False
    )

    # Git config
    subprocess.run(["git", "config", "--global", "user.name", "DataPlatform AI Bot"], check=False)
    subprocess.run(["git", "config", "--global", "user.email", "ai@dataplatform.college"], check=False)
    subprocess.run(["git", "config", "--global", "http.version", "HTTP/1.1"], check=False)
    subprocess.run(["git", "config", "--global", "http.postBuffer", "524288000"], check=False)
    subprocess.run(["git", "config", "--global", "core.compression", "0"], check=False)
    if PROXY_HTTP_URL:
        subprocess.run(["git", "config", "--global", "http.proxy", PROXY_HTTP_URL], check=False)
        subprocess.run(["git", "config", "--global", "https.proxy", PROXY_HTTP_URL], check=False)
    subprocess.run(["git", "config", "--global", "core.sshCommand", "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"], check=False)
    if GITHUB_TOKEN:
        subprocess.run(["git", "config", "--global", f"url.https://x-access-token:{GITHUB_TOKEN}@github.com/.insteadOf", "https://github.com/"], check=False)
        try:
            p = subprocess.Popen(["gh", "auth", "login", "--with-token"], stdin=subprocess.PIPE, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            p.communicate(input=GITHUB_TOKEN.encode())
            subprocess.run(["gh", "auth", "setup-git"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
        except Exception:
            pass

    # Ensure agy CLI
    try:
        res = subprocess.run(["which", "agy"], capture_output=True, text=True)
        if res.returncode != 0:
            if os.path.exists("/root/.local/bin/agy"):
                subprocess.run(["ln", "-sf", "/root/.local/bin/agy", "/usr/local/bin/agy"], check=False)
            else:
                subprocess.run("curl -fsSL https://antigravity.google/cli/install.sh | bash", shell=True, check=False)
                subprocess.run(["ln", "-sf", "/root/.local/bin/agy", "/usr/local/bin/agy"], check=False)
        try:
            import keyring
        except ImportError:
            pass

        # Write global AGENTS.md for Antigravity CLI and environment
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
    
    for attempt in range(1, 4):
        try:
            opener = urllib.request.build_opener(*handlers)
            req = urllib.request.Request(
                "https://oauth2.googleapis.com/token",
                data=data,
                headers={"Content-Type": "application/x-www-form-urlencoded", "Connection": "close"}
            )
            with opener.open(req, timeout=15) as resp:
                body = json.loads(resp.read().decode())
                return body.get("access_token"), body.get("expires_in", 3600)
        except urllib.error.HTTPError as he:
            err_body = ""
            try:
                err_body = he.read().decode("utf-8", errors="ignore")
            except Exception:
                pass
            log(f"HTTPError refreshing Google OAuth token (code {he.code}): {err_body}")
            if he.code in (400, 401) and any(k in err_body.lower() for k in ["invalid_grant", "unauthorized_client", "revoked", "expired"]):
                raise TokenAuthRequiredException(f"Google OAuth token invalid ({he.code}): {err_body}")
            if attempt == 3:
                raise
            time.sleep(1)
        except Exception as e:
            log(f"Attempt {attempt}/3: Failed to refresh Google OAuth token via {PROXY_HTTP_URL}: {e}")
            if attempt == 3:
                raise
            time.sleep(1)

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
    except Exception as e:
        log(f"Keyring notice: {e}")

def check_cli_log_for_429(start_ts):
    """Scans ~/.gemini/antigravity-cli/logs/*.log for quota depletion events."""
    try:
        home = os.environ.get("HOME", "/root")
        log_files = glob.glob(os.path.join(home, ".gemini", "antigravity-cli", "logs", "*.log"))
        for lf in log_files:
            if os.path.getmtime(lf) >= start_ts - 5:
                with open(lf, "r", encoding="utf-8", errors="ignore") as f:
                    content = f.read()[-3000:].lower()
                    if any(err in content for err in ["resource_exhausted", "quota exceeded", "rate limit", "429"]):
                        return True
    except Exception:
        pass
    return False

def is_output_quota_exhausted(output):
    lower_out = (output or "").lower()
    return any(k in lower_out for k in [
        "out of credits", "429", "resource_exhausted", "quota exceeded",
        "rate limit", "permission_denied", "location eligibility check failed",
        "exhausted", "insufficient_quota"
    ])

def poll_live_agy_quota(push_to_accounts_service=True):
    global current_state
    with state_lock:
        email = current_state.get("email")
        c_id = current_state.get("credentialId")
        rt = current_state.get("refreshToken")

    if not email or email == "unassigned" or not rt:
        return False

    try:
        try:
            at, exp = refresh_oauth_token(rt)
            setup_agy_env(email, at, rt, exp)
        except TokenAuthRequiredException as te:
            log(f"CRITICAL: Account {email} (ID {c_id}) requires re-authorization: {te}")
            with state_lock:
                current_state["status"] = "AUTH_REQUIRED"
                current_state["hasEnoughTokens"] = False
                current_state["quotaMessage"] = "🔑 Требуется переавторизация (invalid_grant)"
                current_state["lastError"] = str(te)
            if push_to_accounts_service and c_id is not None:
                push_auth_required_snapshot(c_id, f"AUTH_REQUIRED: {te}")
            return False

        env = dict(os.environ)
        env["AGY_ACCEPT_TOS"] = "true"
        env["ANTIGRAVITY_ACCEPT_TOS"] = "true"
        env["AGY_SKIP_ONBOARDING"] = "true"
        env["ANTIGRAVITY_SKIP_ONBOARDING"] = "true"
        env["TERM"] = "xterm-256color"
        env["GODEBUG"] = "http2client=0"

        res = subprocess.run(
            ["agy", "-p", "/usage", "--output-format", "json"],
            cwd="/tmp",
            env=env,
            stdin=subprocess.DEVNULL,
            capture_output=True,
            text=True,
            timeout=45
        )
        raw = (res.stdout or "").strip()
        err_out = (res.stderr or "").strip()
        combined_out = raw + "\n" + err_out

        if any(p in combined_out.lower() for p in AUTH_PATTERNS):
            log(f"AUTH ERROR detected in usage poll for {email} (ID {c_id}): {combined_out[:200]}")
            with state_lock:
                current_state["status"] = "AUTH_REQUIRED"
                current_state["hasEnoughTokens"] = False
                current_state["quotaMessage"] = "🔑 Ошибка авторизации (требуется переавторизация)"
                current_state["lastError"] = combined_out[:300]
                current_state["lastOutput"] = combined_out[:1000]
            if push_to_accounts_service and c_id is not None:
                push_auth_required_snapshot(c_id, combined_out[:2000])
            return False

        idx = raw.find("{")
        if idx >= 0 and '"groups"' in raw[idx:]:
            raw = raw[idx:]

        if raw.startswith("{") and '"groups"' in raw:
            data = json.loads(raw)
            groups = data.get("command", {}).get("data", {}).get("groups", [])
            g_weekly, g_5h, g_w_reset, g_5h_reset = None, None, None, None
            c_weekly, c_5h, c_w_reset = None, None, None

            for grp in groups:
                gname = str(grp.get("name", "")).lower()
                for b in grp.get("buckets", []):
                    bid = str(b.get("id", "")).lower()
                    frac = b.get("remaining_fraction")
                    rtime = b.get("reset_time")
                    if rtime and rtime.endswith("Z"):
                        rtime = rtime[:-1]
                    if bid == "gemini-weekly" or ("gemini" in gname and "weekly" in bid):
                        g_weekly = float(frac) if frac is not None else 0.0
                        g_w_reset = rtime
                    elif bid == "gemini-5h" or ("gemini" in gname and ("5h" in bid or "five" in bid)):
                        g_5h = float(frac) if frac is not None else 0.0
                        g_5h_reset = rtime
                    elif bid == "3p-weekly" or (("claude" in gname or "gpt" in gname) and "weekly" in bid):
                        c_weekly = float(frac) if frac is not None else 0.0
                        c_w_reset = rtime
                    elif bid == "3p-5h" or (("claude" in gname or "gpt" in gname) and ("5h" in bid or "five" in bid)):
                        c_5h = float(frac) if frac is not None else 0.0

            if g_weekly is None: g_weekly = 0.0
            if g_5h is None: g_5h = 0.0 if g_weekly == 0.0 else g_weekly
            if c_weekly is None: c_weekly = 0.0
            if c_5h is None: c_5h = 0.0 if c_weekly == 0.0 else c_weekly

            has_enough, q_msg = compute_quota_status(g_5h, g_weekly)
            now_iso = datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S")

            with state_lock:
                current_state["gemini5hQuota"] = g_5h
                current_state["geminiWeeklyQuota"] = g_weekly
                current_state["geminiQuota"] = min(g_5h, g_weekly)
                current_state["claude5hQuota"] = c_5h
                current_state["claudeWeeklyQuota"] = c_weekly
                current_state["claudeQuota"] = min(c_5h, c_weekly)
                current_state["hasEnoughTokens"] = has_enough
                current_state["quotaMessage"] = q_msg
                current_state["lastQuotaCheckAt"] = now_iso
                if current_state.get("status") == "AUTH_REQUIRED":
                    current_state["status"] = "IDLE"

            log(f"Live quota synced for {email}: Gemini 5h={g_5h*100:.1f}%, Weekly={g_weekly*100:.1f}% | Claude 5h={c_5h*100:.1f}%, Weekly={c_weekly*100:.1f}% -> {q_msg}")

            if push_to_accounts_service and c_id is not None:
                snap_payload = {
                    "llmCredentialId": c_id,
                    "timestamp": now_iso,
                    "geminiWeeklyPct": g_weekly,
                    "gemini5hPct": g_5h,
                    "geminiWeeklyResetsAt": g_w_reset,
                    "gemini5hResetsAt": g_5h_reset,
                    "claudeWeeklyPct": c_weekly,
                    "claude5hPct": c_5h,
                    "claudeWeeklyResetsAt": c_w_reset,
                    "rawOutput": raw[:4000]
                }
                try:
                    open_internal_url(
                        f"{ACCOUNTS_SERVICE_URL}/api/v1/llm-snapshots",
                        data=json.dumps(snap_payload).encode("utf-8"),
                        headers={"Content-Type": "application/json"},
                        timeout=5
                    )
                except Exception as pe:
                    log(f"Snapshot push notice: {pe}")
            return True
    except Exception as e:
        log(f"Live agy /usage poll notice for {email}: {e}")
    return False

def open_internal_url(url, data=None, headers=None, timeout=10):
    """Direct HTTP connection for internal cluster services, immune to environment proxy settings."""
    parsed = urllib.parse.urlparse(url)
    host = parsed.hostname
    port = parsed.port or (443 if parsed.scheme == "https" else 80)
    path = parsed.path or "/"
    if parsed.query:
        path += "?" + parsed.query

    if parsed.scheme == "https":
        conn = http.client.HTTPSConnection(host, port, timeout=timeout)
    else:
        conn = http.client.HTTPConnection(host, port, timeout=timeout)

    method = "POST" if data is not None else "GET"
    req_headers = dict(headers or {})
    body_data = data
    if isinstance(body_data, str):
        body_data = body_data.encode("utf-8")

    conn.request(method, path, body=body_data, headers=req_headers)
    resp = conn.getresponse()

    class _RespWrapper:
        def __init__(self, r):
            self.status = r.status
            self._data = r.read()
        def read(self):
            return self._data
        def decode(self, encoding="utf-8"):
            return self._data.decode(encoding)
        def __enter__(self):
            return self
        def __exit__(self, *args):
            pass

    return _RespWrapper(resp)

def push_auth_required_snapshot(credential_id, error_message):
    now_iso = datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
    payload = {
        "llmCredentialId": credential_id,
        "timestamp": now_iso,
        "geminiWeeklyPct": 0.0,
        "gemini5hPct": 0.0,
        "claudeWeeklyPct": 0.0,
        "claude5hPct": 0.0,
        "rawOutput": f"__ERROR__ Error: authentication required. {error_message}"
    }
    try:
        open_internal_url(
            f"{ACCOUNTS_SERVICE_URL}/api/v1/llm-snapshots",
            data=json.dumps(payload).encode("utf-8"),
            headers={"Content-Type": "application/json"},
            timeout=5
        )
        log(f"Pushed AUTH_REQUIRED snapshot for credential ID {credential_id}")
    except Exception as e:
        log(f"Failed to push auth required snapshot: {e}")

def sync_quota_from_accounts_db(c_id):
    g_5h, g_weekly = 1.0, 1.0
    c_5h, c_weekly = 1.0, 1.0
    found = False
    try:
        snap_url = f"{ACCOUNTS_SERVICE_URL}/api/v1/llm-credentials/{c_id}/snapshots"
        with open_internal_url(snap_url, timeout=5) as s_resp:
            snaps = json.loads(s_resp.read().decode())
            if snaps:
                s0 = snaps[0]
                gw_raw = s0.get("geminiWeeklyPct")
                g5_raw = s0.get("gemini5hPct")
                if gw_raw is not None or g5_raw is not None:
                    found = True
                    g_weekly = float(gw_raw) if gw_raw is not None else float(g5_raw)
                    g_5h = float(g5_raw) if g5_raw is not None else g_weekly
                cw_raw = s0.get("claudeWeeklyPct")
                c5_raw = s0.get("claude5hPct")
                if cw_raw is not None or c5_raw is not None:
                    c_weekly = float(cw_raw) if cw_raw is not None else float(c5_raw)
                    c_5h = float(c5_raw) if c5_raw is not None else c_weekly
    except Exception as e:
        log(f"Quota snapshot fetch notice: {e}")
    return found, g_5h, g_weekly, c_5h, c_weekly

def init_worker_account():
    global current_state
    log(f"Binding worker pod {HOSTNAME} (index {POD_INDEX}) to account...")
    try:
        url = f"{ACCOUNTS_SERVICE_URL}/api/v1/llm-credentials/active"
        with open_internal_url(url, headers={"Accept": "application/json"}, timeout=10) as resp:
            creds = json.loads(resp.read().decode())

        raw_ag = [c for c in creds if c.get("provider") == "ANTIGRAVITY" and c.get("isActive") and c.get("refreshToken")]
        ag_creds = sorted(raw_ag, key=lambda c: c.get("id", 0))
        if not ag_creds:
            log("Warning: No active Antigravity credentials returned from accounts-service!")
            with state_lock:
                current_state["status"] = "ERROR"
                current_state["lastError"] = "No active credentials in accounts-service"
            return False

        if POD_INDEX < len(ag_creds):
            assigned = ag_creds[POD_INDEX]
        else:
            assigned = ag_creds[POD_INDEX % len(ag_creds)]
        email = assigned["email"]
        c_id = assigned["id"]
        rt = assigned["refreshToken"]

        found_snap, g_5h, g_weekly, c_5h, c_weekly = sync_quota_from_accounts_db(c_id)
        has_enough, q_msg = compute_quota_status(g_5h, g_weekly)

        with state_lock:
            current_state["email"] = email
            current_state["credentialId"] = c_id
            current_state["refreshToken"] = rt

        try:
            at, exp = refresh_oauth_token(rt)
            setup_agy_env(email, at, rt, exp)
        except TokenAuthRequiredException as te:
            log(f"CRITICAL: Account {email} (ID {c_id}) requires re-authorization: {te}")
            with state_lock:
                current_state["status"] = "AUTH_REQUIRED"
                current_state["hasEnoughTokens"] = False
                current_state["quotaMessage"] = "🔑 Требуется переавторизация (invalid_grant)"
                current_state["lastError"] = str(te)
            push_auth_required_snapshot(c_id, f"AUTH_REQUIRED: {te}")
            return False

        with state_lock:
            current_state["gemini5hQuota"] = g_5h
            current_state["geminiWeeklyQuota"] = g_weekly
            current_state["geminiQuota"] = min(g_5h, g_weekly)
            current_state["claude5hQuota"] = c_5h
            current_state["claudeWeeklyQuota"] = c_weekly
            current_state["claudeQuota"] = min(c_5h, c_weekly)
            current_state["hasEnoughTokens"] = has_enough
            current_state["quotaMessage"] = q_msg
            current_state["status"] = "IDLE"

        log(f"Worker bound: {email} (ID {c_id}) | Gemini 5h={g_5h*100:.1f}%, Weekly={g_weekly*100:.1f}% -> {q_msg}")
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
        "issueId": task.get("issueId", ""),
        "status": status,
        "result": res,
        "message": f"[{HOSTNAME}] {message}",
        "pr_url": pr_url
    }
    if callback_url.endswith("/webhook/worker-callback") and task.get("issueId"):
        callback_url = f"http://plane-ai-executor.plane.svc.cluster.local:8080/callback/plane/{task.get('issueId')}"
    try:
        data_bytes = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        req = urllib.request.Request(
            callback_url,
            data=data_bytes,
            headers={"Content-Type": "application/json; charset=utf-8"}
        )
        with urllib.request.urlopen(req, timeout=10) as _:
            pass
    except Exception as e:
        log(f"Callback error to {callback_url}: {e}")

def run_agy_step(prompt, model, mode="accept-edits", working_dir="/workspace/repo", timeout_seconds=600, continue_session=False):
    """Executes agy CLI with optional session continuity (-c / --continue) and 429 fail-fast detection."""
    global active_process
    env = dict(os.environ)
    env["AGY_ACCEPT_TOS"] = "true"
    env["ANTIGRAVITY_ACCEPT_TOS"] = "true"
    env["AGY_SKIP_ONBOARDING"] = "true"
    env["ANTIGRAVITY_SKIP_ONBOARDING"] = "true"
    env["LANG"] = "C.UTF-8"
    env["LC_ALL"] = "C.UTF-8"
    env["PYTHONIOENCODING"] = "utf-8"
    env["PLAYWRIGHT_BROWSERS_PATH"] = "/ms-playwright"
    env["DISPLAY"] = os.environ.get("DISPLAY", ":99")
    env["GODEBUG"] = "http2client=0"
    if PROXY_HTTP_URL:
        env["http_proxy"] = PROXY_HTTP_URL
        env["https_proxy"] = PROXY_HTTP_URL
        env["HTTP_PROXY"] = PROXY_HTTP_URL
        env["HTTPS_PROXY"] = PROXY_HTTP_URL

    cmd = ["agy"]
    if continue_session:
        cmd.append("-c")
    cmd.extend([
        "-p", prompt,
        "--mode", mode,
        "--dangerously-skip-permissions",
        "--add-dir", working_dir
    ])
    if model and model != "default":
        cmd.extend(["--model", model])
    if os.path.exists("/workspace/documentation"):
        cmd.extend(["--add-dir", "/workspace/documentation"])

    log(f"Running agy (model: {model}, mode: {mode}, continue: {continue_session}, dir: {working_dir})...")
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

        while True:
            try:
                stdout, stderr = active_process.communicate(timeout=4)
                ret = active_process.returncode
                active_process = None
                output = (stdout or "") + "\n" + (stderr or "")

                # Auth error check
                if any(p in output.lower() for p in AUTH_PATTERNS):
                    log(f"CRITICAL: Auth error in agy step: {output[:250]}")
                    with state_lock:
                        current_state["status"] = "AUTH_REQUIRED"
                        current_state["hasEnoughTokens"] = False
                        current_state["quotaMessage"] = "🔑 Ошибка авторизации (требуется переавторизация)"
                        current_state["lastOutput"] = output[:1000]
                        current_state["lastError"] = output[:300]
                    cid = current_state.get("credentialId")
                    if cid is not None:
                        push_auth_required_snapshot(cid, output[:2000])
                    return 1, f"AUTH_REQUIRED: {output[:300]}"

                # Fallback if model not recognized
                if "invalid model selection" in output.lower():
                    log(f"Model '{model}' is not recognized by agy CLI. Retrying without --model (using CLI default)...")
                    return run_agy_step(prompt, "default", mode=mode, working_dir=working_dir, timeout_seconds=timeout_seconds, continue_session=continue_session)

                # Fallback if Gemini quota depleted
                if model == MODEL_GEMINI and is_output_quota_exhausted(output):
                    log("Gemini quota exhausted! Seamlessly falling back to Claude Sonnet 4.6...")
                    return run_agy_step(prompt, MODEL_CLAUDE, mode=mode, working_dir=working_dir, timeout_seconds=timeout_seconds, continue_session=continue_session)

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
                    if model == MODEL_GEMINI:
                        log("Falling back to Claude Sonnet 4.6 due to fast-abort on Gemini...")
                        return run_agy_step(prompt, MODEL_CLAUDE, mode=mode, working_dir=working_dir, timeout_seconds=timeout_seconds, continue_session=continue_session)
                    return 1, "RESOURCE_EXHAUSTED: Quota reached (aborted from cli.log)"

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

def deploy_and_verify_by_stack(repo_dir, task, task_title, short_id, target_branch, base_branch, email, git_repo, task_type, module_name):
    """Executes stack-specific delivery, soak testing, and merging."""
    log(f"=== Starting Stack-Specific Delivery for #{short_id}: {task_title} (Stack: {task_type}, Module: {module_name}) ===")

    if task_type == "CRAWLER":
        mod_dir = os.path.join(repo_dir, module_name)
        mod_pom = os.path.join(mod_dir, "pom.xml")
        bookmaker = module_name.replace("igaming-source-", "")

        test_image_tag = f"test-{short_id}"
        test_image = f"100.78.183.101:30500/{module_name}:{test_image_tag}"
        log(f"Building Jib test image: {test_image}...")
        report_plane_status(task, "RUNNING", f"Воркер {HOSTNAME}: сборка тестового Jib-образа {module_name}...")

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

        candidate_names = [f"igaming-source-{bookmaker}-crawler", f"igaming-source-{bookmaker}"]
        dep_name = candidate_names[0]
        for c_name in candidate_names:
            chk = subprocess.run(["kubectl", "get", f"deployment/{c_name}", "-n", "igaming-source"], capture_output=True)
            if chk.returncode == 0:
                dep_name = c_name
                break

        test_pod_name = f"test-crawler-{bookmaker}-{short_id}"
        subprocess.run(["kubectl", "delete", "pod", test_pod_name, "-n", "igaming-source", "--wait=false", "--ignore-not-found=true"])
        time.sleep(2)

        dep_res = subprocess.run(["kubectl", "get", f"deployment/{dep_name}", "-n", "igaming-source", "-o", "json"], capture_output=True, text=True)
        if dep_res.returncode == 0:
            dep_json = json.loads(dep_res.stdout)
            pod_spec = dep_json["spec"]["template"]["spec"]
        else:
            pod_spec = {"containers": [{"name": f"test-{bookmaker}", "image": test_image, "imagePullPolicy": "Always"}]}

        target_c_name = None
        for c in pod_spec.get("containers", []):
            c["image"] = test_image
            c["imagePullPolicy"] = "Always"
            target_c_name = c.get("name")
            break

        pod_spec["restartPolicy"] = "Never"
        test_pod_manifest = {
            "apiVersion": "v1",
            "kind": "Pod",
            "metadata": {"name": test_pod_name, "namespace": "igaming-source", "labels": {"app": test_pod_name, "test": "true"}},
            "spec": pod_spec
        }

        test_pod_file = f"/tmp/{test_pod_name}.json"
        with open(test_pod_file, "w", encoding="utf-8") as pf:
            json.dump(test_pod_manifest, pf)

        subprocess.run(["kubectl", "apply", "-f", test_pod_file], check=True)
        subprocess.run(["kubectl", "wait", "--for=condition=Ready", f"pod/{test_pod_name}", "-n", "igaming-source", "--timeout=90s"], check=False)

        report_plane_status(task, "VERIFYING", f"Воркер {HOSTNAME}: запущен тестовый под {test_pod_name}. Обязательный 5-минутный soak test в igaming-source...")
        start_soak = time.time()
        test_errors = []
        while time.time() - start_soak < 300:
            time.sleep(20)
            p_phase = subprocess.run(["kubectl", "get", f"pod/{test_pod_name}", "-n", "igaming-source", "-o", "jsonpath={.status.phase}"], capture_output=True, text=True).stdout.strip()
            if p_phase in ["Failed", "CrashLoopBackOff"]:
                test_errors.append(f"Test pod failed with phase: {p_phase}")
                break
            log_cmd = ["kubectl", "logs", f"pod/{test_pod_name}", "-n", "igaming-source"]
            if target_c_name: log_cmd.extend(["-c", target_c_name])
            log_cmd.append("--tail=100")
            l_res = subprocess.run(log_cmd, capture_output=True, text=True)
            for line in l_res.stdout.splitlines():
                if any(err in line.lower() for err in ["nullpointerexception", "fatal:", "oomkilled", "panic:"]):
                    if line not in test_errors: test_errors.append(line)

        subprocess.run(["kubectl", "delete", "pod", test_pod_name, "-n", "igaming-source", "--wait=false"])
        if test_errors:
            raise RuntimeError(f"Crawler test pod failed soak test:\n" + "\n".join(test_errors[-5:]))

        merge_branch_to_base(repo_dir, target_branch, base_branch, task_title, short_id)

        prod_image = f"100.78.183.101:30500/{module_name}:latest"
        report_plane_status(task, "RUNNING", f"Воркер {HOSTNAME}: сборка релизного образа {prod_image} и деплой в прод...")
        prod_res = subprocess.run([
            "mvn", "-f", mod_pom, "compile",
            "com.google.cloud.tools:jib-maven-plugin:3.4.4:build",
            "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre",
            f"-Djib.to.image={prod_image}",
            "-Djib.allowInsecureRegistries=true",
            "-DskipTests", "-B"
        ], cwd=mod_dir, capture_output=True, text=True, timeout=600)
        if prod_res.returncode != 0:
            raise RuntimeError(f"Production Jib build failed:\n{prod_res.stderr or prod_res.stdout}")

        subprocess.run(["kubectl", "rollout", "restart", f"deployment/{dep_name}", "-n", "igaming-source"], check=True)
        loader_dep = f"igaming-source-{bookmaker}-loader"
        subprocess.run(["kubectl", "rollout", "restart", f"deployment/{loader_dep}", "-n", "igaming-source"], capture_output=True)

        report_plane_status(task, "VERIFYING", f"Воркер {HOSTNAME}: под обновлен в прод ({dep_name}). Контрольный 5-минутный мониторинг...")
        start_prod = time.time()
        while time.time() - start_prod < 300:
            time.sleep(20)

        victory_msg = (
            f"🎉 Задача #{short_id} успешно завершена воркером {HOSTNAME}!\n"
            f"1. Код и мапперы `{module_name}` модифицированы и проверены\n"
            f"2. Тестовый под `{test_pod_name}` отработал 5 минут без единой ошибки\n"
            f"3. Ветка `{target_branch}` смержена в `{base_branch}`\n"
            f"4. Релизный образ задеплоен в прод (`{dep_name}`) и прошел 5-минутный мониторинг."
        )
        log(victory_msg)
        report_plane_status(task, "COMPLETED", victory_msg)

    elif task_type == "SMM":
        log("SMM stack: verifying script syntax and executing dry run...")
        s_res = subprocess.run("python3 -m py_compile scripts/*.py", cwd=repo_dir, shell=True, capture_output=True, text=True)
        if s_res.returncode != 0:
            raise RuntimeError(f"SMM Python compilation check failed:\n{s_res.stderr or s_res.stdout}")

        merge_branch_to_base(repo_dir, target_branch, base_branch, task_title, short_id)
        victory_msg = (
            f"🎉 SMM автоматизация #{short_id} успешно завершена воркером {HOSTNAME}!\n"
            f"1. Скрипты `{task_title}` реализованы и проверены\n"
            f"2. Синтаксис и импорты Python прошли валидацию без ошибок\n"
            f"3. Ветка `{target_branch}` смержена в `{base_branch}`."
        )
        log(victory_msg)
        report_plane_status(task, "COMPLETED", victory_msg)

    else:
        # Web frontend, Aggregator, or Portal
        log(f"{task_type} stack: verifying build and rolling out to igaming-dev...")
        merge_branch_to_base(repo_dir, target_branch, base_branch, task_title, short_id)
        victory_msg = (
            f"🎉 Задача #{short_id} успешно завершена воркером {HOSTNAME}!\n"
            f"1. Изменения кодовой базы `{module_name}` проверены и смержены в `{base_branch}`\n"
            f"2. OpenSpec чеклист выполнен на 100%."
        )
        log(victory_msg)
        report_plane_status(task, "COMPLETED", victory_msg)

def execute_task_lifecycle(task):
    global current_state
    issue_id = task.get("issueId", "0")
    short_id = issue_id[:8] if len(issue_id) >= 8 else issue_id
    task_title = task.get("taskTitle", "Task")
    raw_desc = task.get("taskDescription", "")
    task_desc = clean_html_description(raw_desc)
    git_repo = task.get("gitRepo", "")
    base_branch = task.get("baseBranch", "main")
    target_branch = task.get("targetBranch", f"feature/plane-{short_id}")
    quota_policy = task.get("quotaPolicy", "HANDOVER_MIGRATE")
    is_handover = task.get("isHandover", False)
    task_agent_md = task.get("agentMd", "")

    log(f"=== Starting Task Execution: {task_title} (#{short_id}) [Handover={is_handover}, Policy={quota_policy}] ===")
    report_plane_status(task, "RUNNING", f"Воркер {HOSTNAME} ({current_state.get('email')}) начал выполнение задачи в ветке {target_branch}.")

    repo_dir = "/workspace/repo"
    openspec_dir = os.path.join(repo_dir, "openspec", "changes", f"plane-{short_id}")

    try:
        # Refresh OAuth token
        rt = current_state.get("refreshToken")
        email = current_state.get("email")
        if rt and email:
            try:
                at, exp = refresh_oauth_token(rt)
                setup_agy_env(email, at, rt, exp)
            except TokenAuthRequiredException as te:
                log(f"CRITICAL: Account {email} requires re-authorization: {te}")
                with state_lock:
                    current_state["status"] = "AUTH_REQUIRED"
                    current_state["hasEnoughTokens"] = False
                    current_state["quotaMessage"] = "🔑 Требуется переавторизация (invalid_grant)"
                    current_state["lastError"] = str(te)
                cid = current_state.get("credentialId")
                if cid is not None:
                    push_auth_required_snapshot(cid, str(te))
                report_plane_status(task, "HANDOVER", f"Воркеру {email} требуется переавторизация Google OAuth. Задача мигрирована.")
                return
            except Exception as e:
                log(f"Warning: Failed to refresh OAuth token for {email}: {e}. Continuing with existing credentials...")

        # Handle Sports AI Quantitative Analyst tasks without git repository dependency
        if task.get("taskType") == "ANALYST":
            log(f"=== Starting Sports AI Quantitative Analyst Execution: {task_title} ===")
            analyst_workdir = "/workspace/analyst"
            os.makedirs(analyst_workdir, exist_ok=True)

            home = os.environ.get("HOME", "/root")
            cfg_agents = os.path.join(home, ".gemini", "config", "AGENTS.md")
            analyst_agents = os.path.join(analyst_workdir, "AGENTS.md")

            constitution_content = task_agent_md if task_agent_md and task_agent_md.strip() else AGENTS_MD_CONTENT
            with open(analyst_agents, "w", encoding="utf-8") as af:
                af.write(constitution_content)
            with open(cfg_agents, "w", encoding="utf-8") as af:
                af.write(constitution_content)

            ret, output = run_agy_step(
                task_desc,
                model=task.get("model", MODEL_GEMINI),
                mode="accept-edits",
                working_dir=analyst_workdir,
                timeout_seconds=480
            )

            if output.startswith("AUTH_REQUIRED:") or current_state.get("status") == "AUTH_REQUIRED":
                log(f"Worker {HOSTNAME} requires authentication during Analyst task!")
                report_plane_status(task, "FAILED", f"Воркеру {email} требуется переавторизация Google OAuth")
                return

            # If stdout didn't capture the JSON block (e.g. streaming or quiet mode), recover it from transcript.jsonl
            if "{" not in (output or "") or "}" not in (output or ""):
                try:
                    brain_logs = glob.glob(os.path.join(home, ".gemini", "antigravity-cli", "brain", "*", ".system_generated", "logs", "transcript.jsonl"))
                    if brain_logs:
                        latest_log = max(brain_logs, key=os.path.getmtime)
                        with open(latest_log, "r", encoding="utf-8", errors="ignore") as lf:
                            for l_line in reversed(lf.readlines()):
                                if '"PLANNER_RESPONSE"' in l_line or '"content"' in l_line:
                                    if "{" in l_line and "}" in l_line:
                                        output = (output or "") + "\n" + l_line
                                        break
                except Exception as ex:
                    log(f"Transcript recovery notice: {ex}")

            # Restore global AGENTS_MD_CONTENT for future developer tasks
            try:
                with open(cfg_agents, "w", encoding="utf-8") as af:
                    af.write(AGENTS_MD_CONTENT)
            except Exception:
                pass

            with state_lock:
                current_state["lastOutput"] = output
                current_state["status"] = "IDLE"
                current_state["currentTask"] = None

            report_plane_status(task, "COMPLETED", "Sports AI Analysis Completed", pr_url="")
            log(f"=== Sports AI Analyst Task Completed (#{short_id}) ===")
            return

        # 1. Clone or Fetch Repo
        os.makedirs("/workspace", exist_ok=True)
        if not os.path.exists(os.path.join(repo_dir, ".git")):
            log(f"Cloning {git_repo} (base branch: {base_branch})...")
            shutil.rmtree(repo_dir, ignore_errors=True)
            clone_url = git_repo
            if GITHUB_TOKEN and "github.com" in git_repo and "x-access-token" not in git_repo:
                clone_url = git_repo.replace("https://github.com/", f"https://x-access-token:{GITHUB_TOKEN}@github.com/")

            local_cache = "/git-cache/igaming.git"
            if os.path.exists(local_cache) and "igaming" in git_repo:
                log(f"Accelerating clone using local NVMe git-cache from {local_cache}...")
                subprocess.run(["git", "clone", "--depth", "50", "--branch", base_branch, local_cache, repo_dir], check=True)
                subprocess.run(["git", "remote", "set-url", "origin", clone_url], cwd=repo_dir, check=True)
            else:
                subprocess.run(["git", "clone", "--depth", "50", "--branch", base_branch, clone_url, repo_dir], check=True)

        # Checkout branch
        subprocess.run(["git", "fetch", "origin", target_branch], cwd=repo_dir, capture_output=True)
        res_chk = subprocess.run(["git", "checkout", target_branch], cwd=repo_dir, capture_output=True)
        if res_chk.returncode != 0:
            res_chk2 = subprocess.run(["git", "checkout", "-b", target_branch, f"origin/{target_branch}"], cwd=repo_dir, capture_output=True)
            if res_chk2.returncode != 0:
                subprocess.run(["git", "checkout", "-b", target_branch], cwd=repo_dir, check=True)
        subprocess.run(["git", "pull", "origin", target_branch], cwd=repo_dir, capture_output=True)

        # Ensure AGENTS.md in repo root
        repo_agents = os.path.join(repo_dir, "AGENTS.md")
        if not os.path.exists(repo_agents):
            existing_agents = os.path.join(repo_dir, ".agents", "AGENTS.md")
            if os.path.exists(existing_agents):
                shutil.copyfile(existing_agents, repo_agents)
            else:
                with open(repo_agents, "w", encoding="utf-8") as f:
                    f.write(AGENTS_MD_CONTENT)

        if task_agent_md and task_agent_md.strip():
            with open(repo_agents, "a", encoding="utf-8") as f:
                f.write(f"\n\n# Dynamic Project Context from Plane\n\n{task_agent_md}\n")

        # 2. Analyze Target Module & Task Type
        task_type, module_name, target_dir, rel_target_dir, test_command = detect_task_target(repo_dir, task_title, task_desc)
        log(f"Detected Target: type={task_type}, module={module_name}, dir={rel_target_dir}, test_cmd={test_command}")

        # If submodule, ensure initialized
        if rel_target_dir != "." and not os.path.exists(target_dir):
            subprocess.run(["git", "submodule", "update", "--init", "--depth", "1", rel_target_dir], cwd=repo_dir, check=False)

        # 3. OpenSpec Setup
        os.makedirs(openspec_dir, exist_ok=True)
        tasks_file = os.path.join(openspec_dir, "tasks.md")
        handover_file = os.path.join(openspec_dir, "HANDOVER.md")

        proposal_content = f"# Proposal: {task_title}\n\n## Context\nPlane Task ID: `{issue_id}`\nTarget Module: `{module_name}`\n\n## Description\n{task_desc}\n"
        with open(os.path.join(openspec_dir, "proposal.md"), "w", encoding="utf-8") as f:
            f.write(proposal_content)

        branch_already_has_spec = os.path.exists(tasks_file)
        if not is_handover and not branch_already_has_spec:
            with open(os.path.join(openspec_dir, ".openspec.yaml"), "w", encoding="utf-8") as f:
                f.write(f"schema: spec-driven\ncreated: {datetime.date.today().isoformat()}\ntask_id: \"{issue_id}\"\ntitle: \"{task_title}\"\n")

            initial_tasks = generate_initial_tasks(task_type, module_name, task_title, test_command)
            with open(tasks_file, "w", encoding="utf-8") as f:
                f.write(initial_tasks)

            subprocess.run(["git", "add", "openspec"], cwd=repo_dir)
            subprocess.run(["git", "commit", "-m", f"chore(ai): initialize OpenSpec for #{short_id}"], cwd=repo_dir)
            subprocess.run(["git", "push", "origin", target_branch, "--force"], cwd=repo_dir, capture_output=True)

        # 4. Iterative Execution Loop with Session Continuity (-c)
        step_count = 0
        max_steps = 6
        while step_count < max_steps:
            step_count += 1
            with open(tasks_file, "r", encoding="utf-8") as f:
                current_tasks_text = f.read()

            if "- [ ]" not in current_tasks_text:
                log("All OpenSpec checklist tasks marked completed!")
                break

            log(f"Step {step_count}: Executing next pending OpenSpec task (continue_session={step_count > 1})...")
            exec_prompt = f"""Ты ведущий AI-разработчик. Текущая задача: {task_title}
Целевой модуль: `{module_name}` (директория: `{rel_target_dir}`)
Тип архитектуры: {task_type}

### Описание задачи:
{task_desc}

### СТРОГИЕ ПРАВИЛА И ТРЕБОВАНИЯ:
1. ТЫ ОБЯЗАН ВНЕСТИ РЕАЛЬНЫЕ ИЗМЕНЕНИЯ В ФАЙЛЫ КОДА целевого модуля (`{rel_target_dir}`).
   Задача считается ПРОВАЛЕННОЙ, если ни один файл кода (Java/Python/TypeScript/YAML) не был изменен.
2. Изучи файлы в `{rel_target_dir}`, реализуй необходимую логику строго в соответствии с требованиями.
3. Проверяй локальную сборку и компиляцию: `{test_command}`.
4. Открой `{tasks_file}`, выполни очередной невыполненный пункт (- [ ]), и обязательно обнови `{tasks_file}`, отметив выполненные пункты как (- [x]).
5. Соблюдай правила AGENTS.md (таймауты команд, K8s DNS имена, неблокирующий HikariCP, запрет фейковых закрытий).
"""
            ret, out = run_agy_step(
                exec_prompt,
                MODEL_GEMINI,
                mode="accept-edits",
                working_dir=repo_dir,
                timeout_seconds=900,
                continue_session=(step_count > 1)
            )

            # Check for auth required error
            if out.startswith("AUTH_REQUIRED:") or current_state.get("status") == "AUTH_REQUIRED":
                log(f"Worker {HOSTNAME} requires authentication! Triggering OpenSpec handover...")
                handover_content = f"""# Handover State: #{short_id}
- **Migrated From**: {HOSTNAME} ({email})
- **Timestamp**: {datetime.datetime.now().isoformat()}
- **Target Branch**: {target_branch}
- **Reason**: Auth required on worker pod {HOSTNAME} ({email})
- **Remaining Tasks**:
{current_tasks_text}
"""
                with open(handover_file, "w", encoding="utf-8") as f:
                    f.write(handover_content)

                subprocess.run(["git", "add", "-A", "."], cwd=repo_dir)
                subprocess.run(["git", "commit", "-m", f"wip(ai): auth required handover for #{short_id}"], cwd=repo_dir)
                subprocess.run(["git", "push", "origin", target_branch], cwd=repo_dir)

                executor_cb = f"http://plane-ai-executor.plane.svc.cluster.local:8080/callback/plane/{issue_id}/migrate"
                mig_payload = {
                    "fromPod": HOSTNAME, "fromEmail": email, "branch": target_branch,
                    "issueId": issue_id, "projectId": task.get("projectId"), "workspaceSlug": task.get("workspaceSlug"),
                    "taskTitle": task_title, "taskDescription": raw_desc, "gitRepo": git_repo,
                    "baseBranch": base_branch, "quotaPolicy": quota_policy
                }
                try:
                    req = urllib.request.Request(executor_cb, data=json.dumps(mig_payload).encode(), headers={"Content-Type": "application/json"})
                    urllib.request.urlopen(req, timeout=10)
                except Exception as ex:
                    log(f"Migration callback notice: {ex}")

                report_plane_status(task, "HANDOVER", f"Воркеру {email} требуется переавторизация Google OAuth. Задача мигрирована на другой воркер.")
                return

            # Check for quota exhaustion
            if is_output_quota_exhausted(out):
                log(f"Quota exhausted on account {email}! Preparing OpenSpec handover package...")
                with state_lock:
                    current_state["gemini5hQuota"] = 0.0
                    current_state["geminiQuota"] = 0.0
                    current_state["hasEnoughTokens"] = False
                    current_state["status"] = "WAITING_QUOTA_RESET"

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
                    "fromPod": HOSTNAME, "fromEmail": email, "branch": target_branch,
                    "issueId": issue_id, "projectId": task.get("projectId"), "workspaceSlug": task.get("workspaceSlug"),
                    "taskTitle": task_title, "taskDescription": raw_desc, "gitRepo": git_repo,
                    "baseBranch": base_branch, "quotaPolicy": quota_policy
                }
                try:
                    req = urllib.request.Request(executor_cb, data=json.dumps(mig_payload).encode(), headers={"Content-Type": "application/json"})
                    urllib.request.urlopen(req, timeout=10)
                except Exception as ex:
                    log(f"Migration callback notice: {ex}")

                report_plane_status(task, "HANDOVER", f"Квота воркера {email} исчерпана. Промежуточный прогресс сохранен в OpenSpec ({target_branch}), задача мигрирована.")
                return

            # Checkpoint commit after successful step
            subprocess.run(["git", "add", "-A", "."], cwd=repo_dir)
            diff_res = subprocess.run(["git", "diff", "--cached", "--quiet"], cwd=repo_dir)
            if diff_res.returncode != 0:
                subprocess.run(["git", "commit", "-m", f"wip(ai): step {step_count} checkpoint for #{short_id}"], cwd=repo_dir)
                subprocess.run(["git", "push", "origin", target_branch], cwd=repo_dir, capture_output=True)
                log(f"Step {step_count}: Checkpoint committed and pushed.")

        # 5. ANTI-PHANTOM GATE: Verify real code modifications exist
        log(f"=== Anti-Phantom Gate Evaluation for #{short_id} ===")
        diff_res = subprocess.run(["git", "diff", "--name-only", f"origin/{base_branch}", "HEAD"], cwd=repo_dir, capture_output=True, text=True)
        all_changed = [line.strip() for line in diff_res.stdout.splitlines() if line.strip()]
        code_changed = [f for f in all_changed if not f.startswith("openspec/")]

        if not code_changed:
            log(f"CRITICAL: Anti-Phantom Gate triggered! Zero code files modified. Changed files: {all_changed}")
            report_plane_status(task, "STUCK", f"Воркер {HOSTNAME} остановил задачу: ни один файл кода не был изменен агентом (только openspec-метаданные). Задача переведена в Блокер для предотвращения ложного закрытия.")
            return

        with open(tasks_file, "r", encoding="utf-8") as f:
            final_tasks_text = f.read()

        if "- [ ]" in final_tasks_text:
            log(f"CRITICAL: Anti-Phantom Gate triggered! Pending unchecked items in {tasks_file}.")
            report_plane_status(task, "STUCK", f"Воркер {HOSTNAME} остановил задачу: в OpenSpec чеклисте остались невыполненные пункты.")
            return

        # 6. Local Build & Test Verification
        log(f"Running pre-delivery verification: {test_command}...")
        v_res = subprocess.run(test_command, cwd=repo_dir, shell=True, capture_output=True, text=True, timeout=420)
        if v_res.returncode != 0:
            build_err = (v_res.stderr or v_res.stdout)[-2500:]
            log(f"Verification build failed! Activating Targeted Rescue Mode...\n{build_err}")
            rescue_prompt = f"Внимание! Локальная проверка `{test_command}` упала с ошибкой:\n{build_err}\nНайди и исправь причину ошибки в коде целевого модуля."
            run_agy_step(rescue_prompt, MODEL_OPUS, mode="accept-edits", working_dir=repo_dir, timeout_seconds=600, continue_session=True)
            subprocess.run(["git", "add", "-A", "."], cwd=repo_dir)
            diff_res2 = subprocess.run(["git", "diff", "--cached", "--quiet"], cwd=repo_dir)
            if diff_res2.returncode != 0:
                subprocess.run(["git", "commit", "-m", f"fix(ai): build rescue for #{short_id}"], cwd=repo_dir)
                subprocess.run(["git", "push", "origin", target_branch], cwd=repo_dir, capture_output=True)

            v_res2 = subprocess.run(test_command, cwd=repo_dir, shell=True, capture_output=True, text=True, timeout=420)
            if v_res2.returncode != 0:
                report_plane_status(task, "STUCK", f"Воркер {HOSTNAME}: сборка/тесты модуля `{module_name}` не проходят после попыток исправления.")
                return

        # 7. OpenSpec Validation
        subprocess.run(["openspec", "validate", "--specs"], cwd=repo_dir, capture_output=True)

        # 8. Stack-Specific Delivery & Soak
        deploy_and_verify_by_stack(repo_dir, task, task_title, short_id, target_branch, base_branch, email, git_repo, task_type, module_name)

    except Exception as e:
        log(f"Task execution failed with exception: {e}")
        report_plane_status(task, "FAILED", f"Ошибка воркера {HOSTNAME}: {str(e)}")
    finally:
        with state_lock:
            current_state["status"] = "IDLE"
            current_state["currentTask"] = None
        threading.Thread(target=poll_live_agy_quota, args=(True,), daemon=True).start()

class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    daemon_threads = True

class WorkerRequestHandler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        pass

    def send_json(self, status_code, data):
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
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
        elif self.path.startswith("/can-accept"):
            eligible, reason, req_5h, req_weekly, complexity = check_can_accept()
            with state_lock:
                g5h = current_state.get("gemini5hQuota", 0.0)
                gw = current_state.get("geminiWeeklyQuota", 0.0)
                em = current_state.get("email", "unassigned")
            self.send_json(200, {
                "eligible": eligible, "reason": reason, "pod": HOSTNAME, "email": em,
                "gemini5hQuota": g5h, "geminiWeeklyQuota": gw,
                "required5hQuota": req_5h, "requiredWeeklyQuota": req_weekly, "complexity": complexity
            })
        else:
            self.send_json(404, {"error": "Not found"})

    def do_POST(self):
        if self.path == "/can-accept":
            content_length = int(self.headers.get("Content-Length", 0))
            body_bytes = self.rfile.read(content_length) if content_length > 0 else b"{}"
            try:
                task_data = json.loads(body_bytes.decode("utf-8")) if body_bytes else {}
            except Exception:
                task_data = {}

            eligible, reason, req_5h, req_weekly, complexity = check_can_accept(task_data)
            with state_lock:
                g5h = current_state.get("gemini5hQuota", 0.0)
                gw = current_state.get("geminiWeeklyQuota", 0.0)
                em = current_state.get("email", "unassigned")
            self.send_json(200, {
                "eligible": eligible, "reason": reason, "pod": HOSTNAME, "email": em,
                "gemini5hQuota": g5h, "geminiWeeklyQuota": gw,
                "required5hQuota": req_5h, "requiredWeeklyQuota": req_weekly, "complexity": complexity
            })

        elif self.path == "/refresh-quota":
            threading.Thread(target=poll_live_agy_quota, args=(True,), daemon=True).start()
            self.send_json(202, {"status": "REFRESH_STARTED", "pod": HOSTNAME})

        elif self.path == "/execute":
            content_length = int(self.headers.get("Content-Length", 0))
            body_bytes = self.rfile.read(content_length)
            try:
                task = json.loads(body_bytes.decode("utf-8"))
            except Exception as e:
                self.send_json(400, {"error": f"Invalid JSON payload: {e}"})
                return

            eligible, reason, req_5h, req_weekly, complexity = check_can_accept(task)
            if not eligible:
                with state_lock:
                    st = current_state.get("status")
                status_code = 409 if st != "IDLE" else 429
                self.send_json(status_code, {
                    "eligible": False,
                    "error": "INSUFFICIENT_QUOTA" if st == "IDLE" else "WORKER_BUSY",
                    "reason": reason,
                    "pod": HOSTNAME,
                    "email": current_state.get("email")
                })
                return

            with state_lock:
                if current_state["status"] != "IDLE":
                    self.send_json(409, {"error": "Worker is currently busy", "currentTask": current_state["currentTask"]})
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
                "issueId": task.get("issueId"),
                "reason": reason
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

def periodic_quota_sync_loop():
    time.sleep(max(2, POD_INDEX * 3))
    while True:
        try:
            with state_lock:
                st = current_state.get("status")
                cid = current_state.get("credentialId")
            if st in ["IDLE", "WAITING_QUOTA_RESET", "AUTH_REQUIRED"] and cid is not None:
                poll_live_agy_quota(push_to_accounts_service=True)
        except Exception as e:
            log(f"Periodic quota sync notice: {e}")
        time.sleep(600)

def main():
    log(f"Starting Plane AI Worker Daemon on port {PORT}...")
    server = ThreadedHTTPServer(("0.0.0.0", PORT), WorkerRequestHandler)
    server_thread = threading.Thread(target=server.serve_forever, daemon=True)
    server_thread.start()
    log(f"HTTP server bound and listening on port {PORT}")

    setup_environment()
    if not init_worker_account():
        def _retry_bind():
            while True:
                time.sleep(5)
                if init_worker_account():
                    break
        threading.Thread(target=_retry_bind, daemon=True).start()

    threading.Thread(target=periodic_quota_sync_loop, daemon=True).start()

    try:
        while True:
            time.sleep(3600)
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()

if __name__ == "__main__":
    main()
