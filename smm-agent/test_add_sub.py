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

    # Scroll and click Subscribers
    page.evaluate("""() => {
        const rows = Array.from(document.querySelectorAll('.sidebar-right .row-clickable, .sidebar-right .item, .sidebar-right .row'));
        const subRow = rows.find(r => r.innerText && r.innerText.includes('Subscribers'));
        if (subRow) {
            subRow.scrollIntoView({ block: 'center' });
            subRow.click();
        }
    }""")
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/tg_inside_subscribers.png")
    
    print("Clicking Add Subscriber button...")
    page.evaluate("() => { const b = document.querySelector('button[aria-label=\"Add Subscribers\"], button[aria-label=\"Add Members\"], .btn-circle.is-visible'); if (b) b.click(); }")
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/tg_add_subscriber_popup.png")
    
    print("Searching for SmartBet in subscribers...")
    page.evaluate("""() => {
        const inp = document.querySelector('.popup input.selector-search-input, .popup input');
        if (inp) {
            inp.focus();
            inp.value = 'SmartBet';
            inp.dispatchEvent(new Event('input', { bubbles: true }));
        }
    }""")
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/tg_subscriber_search_results.png")
    
    results = page.evaluate("""() => {
        const items = Array.from(document.querySelectorAll('.popup .chatlist-chat, .popup .row-clickable, .popup .selector-item, .popup .item'));
        return items.map(el => ({
            text: el.innerText.replace(/\\n/g, ' '),
            className: el.className
        }));
    }""")
    print("Popup items in subscribers search:\n", json.dumps(results, indent=2, ensure_ascii=False))
    b.close()
