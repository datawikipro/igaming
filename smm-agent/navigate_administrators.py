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

    # Click pencil icon
    page.mouse.click(1228, 44)
    time.sleep(3)

    # Scroll edit container down to see bottom items
    page.evaluate("""() => {
        const scroller = document.querySelector('.edit-peer-container .sidebar-content, .sidebar-content');
        if (scroller) scroller.scrollTop = 400;
    }""")
    time.sleep(2)
    page.screenshot(path="smm-agent/storage/tg_edit_scrolled_rows.png")

    # Find rows in the edit panel
    rows = page.evaluate("""() => {
        const items = Array.from(document.querySelectorAll('.edit-peer-container .row-clickable, .edit-peer-container .item, .sidebar-content .row-clickable'));
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
    print("Found rows:\n", json.dumps(rows, indent=2, ensure_ascii=False))
    browser.close()
