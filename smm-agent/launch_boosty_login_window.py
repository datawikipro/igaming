#!/usr/bin/env python3
"""
SmartBet.guru — Headed Firefox Boosty Login & Cookie Watcher (Variant 2)
Launches genuine Mozilla Firefox without automation flags on the user's desktop,
watches cookies.sqlite for the 'auth' cookie, updates K8s secrets and restarts the bot.
"""
import json
import os
import shutil
import sqlite3
import subprocess
import sys
import time
from pathlib import Path
from urllib.parse import unquote

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

FIREFOX_BIN = Path(r"C:\Users\chernousov_a\AppData\Local\ms-playwright\firefox-1509\firefox\firefox.exe")
PROFILE_DIR = Path(r"C:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\storage\boosty_firefox_user").resolve()
STORAGE_DIR = Path(r"C:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\storage").resolve()
COOKIES_DB = PROFILE_DIR / "cookies.sqlite"

BLOG_NAME = "smartbetguru"
EMAIL = "www.smartbet.guru@gmail.com"
PASSWORD = "Racoiaws12"

def update_k8s_secret(access_token: str, device_id: str, blog_name: str = BLOG_NAME):
    """Updates Kubernetes secret igaming-boosty-bot-secrets in namespace igaming-dev."""
    print("\n[K8S] Updating secret igaming-boosty-bot-secrets in namespace igaming-dev...")
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
        print(f"[K8S] Secret applied: {out.strip()} {err.strip()}")
        
        # Rollout restart
        print("[K8S] Restarting deployment igaming-boosty-bot...")
        subprocess.run(["kubectl", "rollout", "restart", "deployment/igaming-boosty-bot", "-n", "igaming-dev"], check=True)
        print("[K8S] Deployment restart triggered!")
    except Exception as e:
        print(f"[K8S ERROR] Failed to update K8s: {e}")

def check_auth_cookie():
    """Reads cookies.sqlite safely and returns parsed auth payload if found."""
    if not COOKIES_DB.exists():
        return None
    try:
        # Connect in read-only mode to prevent database locking
        uri = f"file:{COOKIES_DB.as_posix()}?mode=ro"
        conn = sqlite3.connect(uri, uri=True, timeout=1.0)
        cursor = conn.cursor()
        cursor.execute("SELECT value FROM moz_cookies WHERE name = 'auth' AND host LIKE '%boosty.to%'")
        row = cursor.fetchone()
        conn.close()
        
        if row and row[0]:
            val = row[0]
            decoded = unquote(val)
            try:
                data = json.loads(decoded)
                if "accessToken" in data:
                    return data
            except Exception:
                if len(val) > 20:
                    return {"accessToken": val}
    except Exception:
        # Database might be locked by Firefox during write; retry on next tick
        pass
    return None

def main():
    print("=" * 65)
    print(" SmartBet.guru — Interactive Boosty Login Window (Variant 2)")
    print("=" * 65)
    print(f"Firefox Binary: {FIREFOX_BIN}")
    print(f"Profile Dir:    {PROFILE_DIR}")
    print(f"Target Email:   {EMAIL}")
    print("=" * 65)
    
    if not FIREFOX_BIN.exists():
        print(f"[ERROR] Firefox binary not found at {FIREFOX_BIN}")
        sys.exit(1)
        
    PROFILE_DIR.mkdir(parents=True, exist_ok=True)
    
    # 1. Check if auth cookie already exists
    existing = check_auth_cookie()
    if existing and "accessToken" in existing:
        print(f"[FOUND] Existing Boosty session found in profile!")
        print(f"Token preview: {existing['accessToken'][:25]}...")
        update_k8s_secret(existing["accessToken"], existing.get("deviceId", "boosty_user_session"))
        return

    # 2. Launch Firefox without any automation flags
    print("\n[LAUNCH] Starting visible Firefox window on your desktop...")
    print(f"Opening https://boosty.to ...")
    
    cmd = [
        str(FIREFOX_BIN),
        "-no-remote",
        "-profile", str(PROFILE_DIR),
        "https://boosty.to"
    ]
    
    ff_proc = subprocess.Popen(cmd)
    print(f"[PROCESS] Firefox PID: {ff_proc.pid}")
    print("\n" + "👉" * 30)
    print("ПОЖАЛУЙСТА, ПЕРЕЙДИТЕ В ОТКРЫВШЕЕСЯ ОКНО FIREFOX НА ЭКРАНЕ:")
    print("1. Нажмите кнопку 'РЕГИСТРАЦИЯ' или 'SIGN UP' (вверху справа).")
    print("2. Выберите вход через Google (иконка G).")
    print(f"3. Введите почту: {EMAIL}")
    print(f"   Пароль:        {PASSWORD}")
    print("4. После входа токен будет АВТОМАТИЧЕСКИ перехвачен, и окно закроется!")
    print("👉" * 30 + "\n")
    
    print("[MONITOR] Ожидание успешной авторизации (мониторинг cookies.sqlite)...")
    start_time = time.time()
    max_wait = 300  # 5 minutes
    
    auth_data = None
    while time.time() - start_time < max_wait:
        # Check if user closed Firefox manually
        if ff_proc.poll() is not None:
            print("[INFO] Окно браузера было закрыто пользователем. Проверка кук...")
            auth_data = check_auth_cookie()
            break
            
        auth_data = check_auth_cookie()
        if auth_data and "accessToken" in auth_data:
            print("\n" + "🎉" * 30)
            print("🎉 УСПЕШНАЯ АВТОРИЗАЦИЯ НА BOOSTY! ТОКЕНЫ ПЕРЕХВАЧЕНЫ!")
            print("🎉" * 30)
            break
            
        time.sleep(1.5)
        elapsed = int(time.time() - start_time)
        if elapsed % 15 == 0 and elapsed > 0:
            print(f"[{elapsed}s / {max_wait}s] Ожидание входа в Boosty...")
            
    if auth_data and "accessToken" in auth_data:
        access_token = auth_data["accessToken"]
        refresh_token = auth_data.get("refreshToken", "")
        device_id = auth_data.get("deviceId", "boosty_desktop_session")
        
        print(f"\n[INFO] Access Token: {access_token[:20]}...{access_token[-10:]}")
        print(f"[INFO] Device ID:    {device_id}")
        
        # Save to local file
        out_file = STORAGE_DIR / "boosty_tokens.json"
        with open(out_file, "w", encoding="utf-8") as f:
            json.dump(auth_data, f, indent=2)
        print(f"[STORAGE] Токены сохранены в {out_file}")
        
        # Update Kubernetes
        update_k8s_secret(access_token, device_id)
        
        # Close Firefox gently
        try:
            if ff_proc.poll() is None:
                ff_proc.terminate()
        except Exception:
            pass
        print("\n[COMPLETE] Boosty аккаунт успешно подключен к инфраструктуре SmartBet.guru!")
    else:
        print("\n[TIMEOUT / CANCELLED] Авторизация не завершена за отведенное время.")
        try:
            if ff_proc.poll() is None:
                ff_proc.terminate()
        except Exception:
            pass

if __name__ == "__main__":
    main()
