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
    page.goto("https://web.telegram.org/k/#-4371643544")
    time.sleep(4)
    page.locator(".chat-info").first.click()
    time.sleep(2)
    page.mouse.click(1228, 44) # Pencil
    time.sleep(3)

    # Scroll and click Administrators
    page.evaluate("""() => {
        const rows = Array.from(document.querySelectorAll('.sidebar-right .row-clickable, .sidebar-right .item, .sidebar-right .row'));
        const adminRow = rows.find(r => r.innerText && r.innerText.includes('Administrators'));
        if (adminRow) {
            adminRow.scrollIntoView({ block: 'center' });
            adminRow.click();
        }
    }""")
    time.sleep(3)
    
    print("Clicking Add New Admins via JS...")
    page.evaluate("() => document.querySelector('button[aria-label=\"Add New Admins\"]').click()")
    time.sleep(3)
    
    print("Clicking and typing into search input...")
    # Click directly on the input in the popup
    inp = page.locator(".popup input.selector-search-input, .popup input[type='search'], .popup input").first
    inp.click()
    time.sleep(1)
    page.keyboard.type("smartbet_guru_bot", delay=100)
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/tg_search_typed_bot.png")
    
    # Also press Enter or wait
    time.sleep(2)
    page.keyboard.press("Enter")
    time.sleep(2)
    page.screenshot(path="smm-agent/storage/tg_search_typed_bot_enter.png")
    
    # Check elements in popup
    results = page.evaluate("""() => {
        return Array.from(document.querySelectorAll('.popup .chatlist-chat, .popup .row-clickable, .popup .selector-item, .popup .selector-user-item, .popup .item')).map(el => ({
            text: el.innerText.replace(/\\n/g, ' '),
            className: el.className
        }));
    }""")
    print("Popup search results:\n", json.dumps(results, indent=2, ensure_ascii=False))
    b.close()
