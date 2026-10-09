import json
import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()

with sync_playwright() as p:
    b = p.chromium.launch_persistent_context(str(STORAGE_DIR), headless=True, viewport={"width": 1280, "height": 900})
    page = b.pages[0] if b.pages else b.new_page()
    
    print("Navigating to startchannel URL...")
    # In web.telegram.org/k, startchannel URL is https://web.telegram.org/k/#?startchannel=smartbet_guru_bot or https://web.telegram.org/k/#@smartbet_guru_bot
    page.goto("https://web.telegram.org/k/#?startchannel=smartbet_guru_bot")
    time.sleep(5)
    page.screenshot(path="smm-agent/storage/tg_startchannel.png")
    
    # Check dialogs/popups
    popups = page.evaluate("""() => {
        return Array.from(document.querySelectorAll('.popup, .modal-dialog, .chatlist-chat')).map(el => ({
            text: el.innerText.replace(/\\n/g, ' '),
            className: el.className
        }));
    }""")
    print("Popups found:\n", json.dumps(popups[:10], indent=2, ensure_ascii=False))
    b.close()
