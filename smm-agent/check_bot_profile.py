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
    
    # Click chat header
    page.locator(".chat-info").first.click()
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/tg_bot_profile.png")
    
    # Check elements in sidebar-right
    items = page.evaluate("""() => {
        return Array.from(document.querySelectorAll('.sidebar-right button, .sidebar-right .row-clickable, .sidebar-right .item')).map(el => ({
            text: el.innerText.replace(/\\n/g, ' '),
            aria: el.getAttribute('aria-label'),
            className: el.className
        }));
    }""")
    print("Bot profile items:\n", json.dumps(items, indent=2, ensure_ascii=False))
    
    # Click 3 dots menu if available
    menu_btn = page.locator(".sidebar-right .btn-menu-toggle, .sidebar-right .btn-icon").all()
    print("Found menu buttons:", len(menu_btn))
    for i, btn in enumerate(menu_btn):
        print(f"Btn {i}: aria={btn.get_attribute('aria-label')}, text={btn.inner_text()}")
        if btn.is_visible() and ("more" in (btn.get_attribute("aria-label") or "").lower() or not btn.inner_text()):
            btn.click()
            time.sleep(1)
            break
            
    page.screenshot(path="smm-agent/storage/tg_bot_profile_menu.png")
    menu_items = page.evaluate("""() => {
        return Array.from(document.querySelectorAll('.btn-menu-item, .menu-item')).map(el => el.innerText.replace(/\\n/g, ' '));
    }""")
    print("Menu items:\n", menu_items)
    b.close()
