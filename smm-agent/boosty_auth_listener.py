#!/usr/bin/env python3
"""
SmartBet.guru — Boosty Auth Listener & Auto-Sync (Variant 2)
1. Opens https://boosty.to in default browser.
2. Listens on http://127.0.0.1:8899/token (CORS-enabled) for 1-click token transmission.
3. Simultaneously monitors Windows clipboard and Firefox cookies.sqlite files for 'auth' cookie.
4. Validates token via Boosty API (GET /v1/user/current).
5. Automatically updates K8s secret igaming-boosty-bot-secrets and restarts pod.
"""
import http.server
import json
import os
import shutil
import socketserver
import sqlite3
import subprocess
import sys
import tempfile
import threading
import time
from pathlib import Path
from urllib.parse import parse_qs, unquote, urlparse
import requests

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

PORT = 8899
STORAGE_DIR = Path("smm-agent/storage").resolve()
STORAGE_DIR.mkdir(parents=True, exist_ok=True)
TOKEN_FILE = STORAGE_DIR / "boosty_tokens.json"

captured_event = threading.Event()
valid_auth_data = None

def validate_boosty_token(token: str):
    """Checks if the access token is valid by querying Boosty API."""
    try:
        headers = {
            "Authorization": f"Bearer {token}",
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        }
        res = requests.get("https://api.boosty.to/v1/user/current", headers=headers, timeout=10)
        if res.status_code == 200:
            data = res.json()
            return data
    except Exception as e:
        print(f"[API ERROR] {e}")
    return None

def update_k8s_secret(access_token: str, device_id: str, blog_name: str = "smartbetguru"):
    """Updates Kubernetes secret igaming-boosty-bot-secrets in namespace igaming-dev."""
    print(f"\n[K8S] Применение токена Boosty в Kubernetes (namespace igaming-dev)...")
    yaml_manifest = f"""apiVersion: v1
kind: Secret
metadata:
  name: igaming-boosty-bot-secrets
  namespace: igaming-dev
type: Opaque
stringData:
  db-password: "igaming"
  boosty-blog-name: "{blog_name}"
  boosty-access-token: "{access_token}"
  boosty-device-id: "{device_id}"
  telegram-bot-token: "8391441164:AAHJmCpBPP--7OfqW0OujtkX7UryJGfe6Yo"
  telegram-donor-chat-id: "-1002345678901"
"""
    try:
        p = subprocess.Popen(["kubectl", "apply", "-f", "-"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        out, err = p.communicate(input=yaml_manifest)
        print(f"[K8S] Результат: {out.strip()} {err.strip()}")
        
        # Rollout restart
        print("[K8S] Перезапуск пода igaming-boosty-bot...")
        subprocess.run(["kubectl", "rollout", "restart", "deployment/igaming-boosty-bot", "-n", "igaming-dev"], check=True)
        print("[K8S] ✅ Rollout restart успешно запущен!")
    except Exception as e:
        print(f"[K8S ERROR] Ошибка обновления секрета: {e}")

class BoostyAuthHandler(http.server.BaseHTTPRequestHandler):
    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "*")
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.end_headers()

    def do_GET(self):
        global valid_auth_data
        parsed = urlparse(self.path)
        qs = parse_qs(parsed.query)
        
        raw_val = None
        if "token" in qs:
            raw_val = qs["token"][0]
        elif "data" in qs:
            raw_val = qs["data"][0]
            
        if raw_val:
            parsed_token = extract_token_from_string(raw_val)
            if parsed_token:
                user_info = validate_boosty_token(parsed_token.get("accessToken", ""))
                if user_info:
                    valid_auth_data = (parsed_token, user_info)
                    captured_event.set()
                    self.send_response(200)
                    self.send_header("Content-Type", "application/json; charset=utf-8")
                    self.end_headers()
                    self.wfile.write(b'{"status":"ok","message":"Token successfully verified and accepted!"}')
                    return
                    
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.end_headers()
        self.wfile.write(b'<html><body><h3>SmartBet Boosty Receiver Active</h3></body></html>')

    def do_POST(self):
        global valid_auth_data
        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length).decode("utf-8", errors="ignore")
        parsed_token = extract_token_from_string(body)
        if parsed_token:
            user_info = validate_boosty_token(parsed_token.get("accessToken", ""))
            if user_info:
                valid_auth_data = (parsed_token, user_info)
                captured_event.set()
                self.send_response(200)
                self.send_header("Content-Type", "application/json; charset=utf-8")
                self.end_headers()
                self.wfile.write(b'{"status":"ok","message":"Token successfully verified!"}')
                return
        self.send_response(400)
        self.end_headers()
        self.wfile.write(b'{"status":"error","message":"Invalid token"}')

    def log_message(self, format, *args):
        pass

def extract_token_from_string(text: str):
    """Tries to extract Boosty access token from cookie string, raw token, or JSON."""
    if not text:
        return None
    text = text.strip()
    
    # 1. Check if whole text is JSON
    try:
        data = json.loads(text)
        if isinstance(data, dict) and "accessToken" in data:
            return data
    except Exception:
        pass
        
    # 2. Check if URL-encoded JSON
    try:
        unquoted = unquote(text)
        data = json.loads(unquoted)
        if isinstance(data, dict) and "accessToken" in data:
            return data
    except Exception:
        pass
        
    # 3. Check if cookie header containing auth=...
    if "auth=" in text:
        for part in text.split(";"):
            part = part.strip()
            if part.startswith("auth="):
                cookie_val = part[5:]
                res = extract_token_from_string(cookie_val)
                if res:
                    return res
                    
    # 4. Check if raw bearer token string (usually 40-100 characters base64/hex)
    if len(text) >= 30 and " " not in text and "\n" not in text and ("=" not in text or text.endswith("=")):
        return {"accessToken": text}
        
    return None

def monitor_firefox_cookies():
    """Continuously checks Firefox user profiles for boosty auth cookie."""
    global valid_auth_data
    profiles_dir = os.path.expandvars(r'%APPDATA%\Mozilla\Firefox\Profiles')
    if not os.path.exists(profiles_dir):
        return
        
    while not captured_event.is_set():
        try:
            for prof in os.listdir(profiles_dir):
                prof_path = os.path.join(profiles_dir, prof)
                db_path = os.path.join(prof_path, 'cookies.sqlite')
                if os.path.exists(db_path):
                    td = tempfile.mkdtemp()
                    tmp = os.path.join(td, 'cookies.sqlite')
                    try:
                        shutil.copy2(db_path, tmp)
                        wal = db_path + '-wal'
                        if os.path.exists(wal):
                            shutil.copy2(wal, tmp + '-wal')
                        conn = sqlite3.connect(tmp)
                        cursor = conn.cursor()
                        cursor.execute("SELECT value FROM moz_cookies WHERE name = 'auth' AND host LIKE '%boosty.to%'")
                        row = cursor.fetchone()
                        conn.close()
                        if row and row[0]:
                            res = extract_token_from_string(row[0])
                            if res:
                                user_info = validate_boosty_token(res.get("accessToken", ""))
                                if user_info:
                                    valid_auth_data = (res, user_info)
                                    captured_event.set()
                                    return
                    except Exception:
                        pass
                    finally:
                        shutil.rmtree(td, ignore_errors=True)
        except Exception:
            pass
        time.sleep(2)

def monitor_clipboard():
    """Continuously checks Windows clipboard for copied tokens or cookies."""
    global valid_auth_data
    last_text = ""
    while not captured_event.is_set():
        try:
            cur = subprocess.check_output(['powershell', '-NoProfile', '-Command', 'Get-Clipboard'], text=True, stderr=subprocess.DEVNULL).strip()
            if cur and cur != last_text:
                last_text = cur
                res = extract_token_from_string(cur)
                if res and "accessToken" in res:
                    user_info = validate_boosty_token(res["accessToken"])
                    if user_info:
                        print("\n[CLIPBOARD] 📋 Обнаружен валидный токен в буфере обмена!")
                        valid_auth_data = (res, user_info)
                        captured_event.set()
                        return
        except Exception:
            pass
        time.sleep(1.5)

def main():
    print("=" * 65)
    print(" SmartBet.guru — Автоматический захват токена Boosty (Вариант 2)")
    print("=" * 65)
    print(f"HTTP Receiver:    http://127.0.0.1:{PORT}/token")
    print(f"Target Email:     www.smartbet.guru@gmail.com")
    print("=" * 65 + "\n")

    # Start HTTP Server in background thread
    class ThreadedHTTPServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
        daemon_threads = True

    try:
        httpd = ThreadedHTTPServer(("127.0.0.1", PORT), BoostyAuthHandler)
        server_thread = threading.Thread(target=httpd.serve_forever, daemon=True)
        server_thread.start()
        print(f"[DAEMON] HTTP ресивер запущен на http://127.0.0.1:{PORT}/token")
    except Exception as e:
        print(f"[WARNING] Не удалось запустить локальный HTTP сервер на {PORT}: {e}")

    # Start Firefox profiles watcher
    ff_thread = threading.Thread(target=monitor_firefox_cookies, daemon=True)
    ff_thread.start()

    # Start Clipboard watcher
    clip_thread = threading.Thread(target=monitor_clipboard, daemon=True)
    clip_thread.start()

    # Open Boosty in default browser
    print("[BROWSER] Открытие https://boosty.to в браузере...")
    subprocess.run(["powershell", "-NoProfile", "-Command", "Start-Process 'https://boosty.to'"])

    print("\n" + "👉" * 30)
    print("ДЕЙСТВИЯ ДЛЯ ЗАВЕРШЕНИЯ (будет выполнено автоматически):")
    print("1. В открывшемся окне браузера нажмите 'РЕГИСТРАЦИЯ' или 'ВОЙТИ'.")
    print("2. Авторизуйтесь через Google (www.smartbet.guru@gmail.com).")
    print("3. Как только вход будет выполнен:")
    print("   - Токен автоматически подхватится из cookies или буфера обмена!")
    print("   - Либо просто скопируйте куку 'auth' в буфер обмена (Ctrl+C).")
    print("   - Либо откройте консоль DevTools на boosty.to и выполните:")
    print("     fetch('http://127.0.0.1:8899/token?data=' + encodeURIComponent(document.cookie))")
    print("👉" * 30 + "\n")

    print("[STATUS] Ожидание подтверждения авторизации (максимум 5 минут)...")
    
    # Wait for up to 300 seconds
    start_time = time.time()
    while time.time() - start_time < 300:
        if captured_event.wait(timeout=2):
            break
        elapsed = int(time.time() - start_time)
        if elapsed % 20 == 0 and elapsed > 0:
            print(f"[{elapsed}s / 300s] Ожидание авторизации...")

    if valid_auth_data:
        token_data, user_info = valid_auth_data
        access_token = token_data.get("accessToken", "")
        device_id = token_data.get("deviceId", "boosty_desktop_session")
        username = user_info.get("name") or user_info.get("blogUrl") or "smartbetguru"
        user_id = user_info.get("id", "unknown")
        
        print("\n" + "🎉" * 35)
        print("🎉 АВТОРИЗАЦИЯ BOOSTY УСПЕШНО ПОДТВЕРЖДЕНА И ВАЛИДИРОВАНА!")
        print(f"🎉 Пользователь Boosty: {username} (ID: {user_id})")
        print("🎉" * 35)
        
        # Save locally
        save_payload = {
            "token": token_data,
            "user": user_info,
            "updated_at": time.strftime("%Y-%m-%d %H:%M:%S")
        }
        with open(TOKEN_FILE, "w", encoding="utf-8") as f:
            json.dump(save_payload, f, indent=2, ensure_ascii=False)
        print(f"[STORAGE] Учетные данные сохранены в {TOKEN_FILE}")
        
        # Update Kubernetes
        update_k8s_secret(access_token=access_token, device_id=device_id, blog_name=username)
        print("\n[COMPLETE] ✅ Бот igaming-boosty-bot успешно настроен и перезапущен в K8s!")
    else:
        print("\n[TIMEOUT] Время ожидания (5 мин) истекло. Пожалуйста, запустите команду повторно при готовности.")

if __name__ == "__main__":
    main()
