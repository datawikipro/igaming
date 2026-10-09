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

    # Scroll the edit container down
    print("Scrolling sidebar right down...")
    page.evaluate("""() => {
        const scrollers = document.querySelectorAll('.sidebar-right .scrollable, .sidebar-right .sidebar-content, .sidebar-right');
        scrollers.forEach(s => s.scrollTop += 600);
    }""")
    time.sleep(2)
    page.screenshot(path="smm-agent/storage/tg_edit_scrolled_to_bottom.png")
    
    rows = page.evaluate("""() => {
        return Array.from(document.querySelectorAll('.sidebar-right .row-clickable, .sidebar-right .item')).map(r => ({
            text: r.innerText.replace(/\\n/g, ' '),
            rect: r.getBoundingClientRect()
        }));
    }""")
    print("Rows visible after scroll:\n", json.dumps(rows, indent=2, ensure_ascii=False))
    b.close()
