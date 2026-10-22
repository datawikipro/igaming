import sys
import requests

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
import os
TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "")
PROXIES = {
    "http": "http://100.83.113.50:3128",
    "https": "http://100.83.113.50:3128"
}

channels = ["@SmartBetGuruEN", "@SmartBetGuruFR", "@SmartBetGuruES"]

print("Checking Telegram Bot API getChatAdministrators...")
for ch in channels:
    url = f"https://api.telegram.org/bot{TOKEN}/getChatAdministrators?chat_id={ch}"
    try:
        r = requests.get(url, proxies=PROXIES, timeout=15)
        data = r.json()
        if data.get("ok"):
            admins = [f"@{a['user'].get('username')} [{a['status']}]" for a in data["result"]]
            print(f"[OK] {ch} -> SUCCESS! Admins ({len(admins)}): {', '.join(admins)}")
        else:
            print(f"[ERROR] {ch} -> ERROR: {data.get('description')}")
    except Exception as e:
        print(f"[EXCEPTION] {ch} -> EXCEPTION: {e}")
