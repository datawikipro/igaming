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
    browser = p.chromium.launch_persistent_context(
        user_data_dir=str(STORAGE_DIR),
        headless=True,
        args=["--disable-blink-features=AutomationControlled", "--no-sandbox"],
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.goto("https://web.telegram.org/k/#-4371643544")
    time.sleep(4)

    # Click header
    page.locator(".chat-info").first.click()
    time.sleep(2)

    # Click pencil icon at x=958, y=48
    print("Clicking pencil icon at (958, 48)...")
    page.mouse.click(958, 48)
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/tg_after_pencil_958.png")

    # Find rows in the right sidebar
    rows = page.evaluate("""() => {
        const items = Array.from(document.querySelectorAll('.sidebar-right .row-clickable, .sidebar-right .item, .sidebar-right .row'));
        return items.map(el => {
            const rect = el.getBoundingClientRect();
            return {
                text: el.innerText.replace(/\\n/g, ' '),
                className: el.className,
                x: Math.round(rect.x + rect.width / 2),
                y: Math.round(rect.y + rect.height / 2),
                visible: rect.width > 0 && rect.height > 0
            };
        });
    }""")
    print("Found rows after pencil click:\n", json.dumps(rows, indent=2, ensure_ascii=False))
    browser.close()
