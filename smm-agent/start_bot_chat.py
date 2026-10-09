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
    
    print("Navigating to bot chat...")
    page.goto("https://web.telegram.org/k/#@smartbet_guru_bot")
    time.sleep(5)
    page.screenshot(path="smm-agent/storage/tg_bot_chat.png")
    
    # Check if there is a START button
    btn = page.locator("button:has-text('START'), button:has-text('Start'), .chat-input-control-button").first
    if btn.is_visible():
        print("Found START button, clicking...")
        btn.click()
        time.sleep(3)
    else:
        print("No START button, sending /start...")
        page.keyboard.type("/start")
        page.keyboard.press("Enter")
        time.sleep(2)
        
    page.screenshot(path="smm-agent/storage/tg_bot_chat_after_start.png")
    b.close()
