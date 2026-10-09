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
    page.mouse.click(1228, 44)
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/tg_edit_real_pencil.png")
    
    rows = page.evaluate("""() => {
        return Array.from(document.querySelectorAll('.sidebar-right .row-clickable, .sidebar-right .item, .sidebar-right .row')).map(r => {
            const rect = r.getBoundingClientRect();
            return {
                text: r.innerText.replace(/\\n/g, ' '),
                className: r.className,
                x: Math.round(rect.x + rect.width/2),
                y: Math.round(rect.y + rect.height/2),
                w: Math.round(rect.width),
                h: Math.round(rect.height)
            };
        }).filter(r => r.w > 0 && r.h > 0 && r.x > 500);
    }""")
    with open("smm-agent/storage/tg_edit_rows.json", "w", encoding="utf-8") as f:
        json.dump(rows, f, indent=2, ensure_ascii=False)
    print("Saved edit rows to smm-agent/storage/tg_edit_rows.json")
    b.close()
