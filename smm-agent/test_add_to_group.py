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
    page.goto("https://web.telegram.org/k/#@smartbet_guru_bot")
    time.sleep(4)
    page.locator(".chat-info").first.click()
    time.sleep(2)
    
    print("Clicking Add to Group...")
    page.locator(".sidebar-right .row:has-text('Add to Group')").click()
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/tg_add_to_group_dialog.png")
    
    chats = page.evaluate("""() => {
        return Array.from(document.querySelectorAll('.popup .chatlist-chat, .modal-dialog .chatlist-chat, .selector-item')).map(el => el.innerText.replace(/\\n/g, ' '));
    }""")
    print("Chats shown in Add to Group:\n", chats)
    b.close()
