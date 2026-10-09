import sys
import json
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
    page.goto("https://web.telegram.org/k/#-3960368887")
    page.wait_for_timeout(3000)

    # 1. Click header
    page.locator(".chat-info").first.click()
    page.wait_for_timeout(2000)

    # 2. Click Edit pencil button
    page.locator("#column-right .sidebar-header button").nth(1).click()
    page.wait_for_timeout(2000)

    # 3. Dump all rows in #column-right
    rows = page.evaluate("""() => {
        const container = document.querySelector('#column-right');
        if (!container) return [];
        const elements = container.querySelectorAll('.row, .item, .menu-item, button, .sidebar-slider-item.active .sidebar-left-section');
        return Array.from(elements).map((el, i) => ({
            index: i,
            tag: el.tagName,
            className: el.className,
            text: el.innerText.trim().replace(/\\n/g, ' -- ')
        })).filter(x => x.text.length > 0);
    }""")

    with open("storage/tg_edit_rows.json", "w", encoding="utf-8") as f:
        json.dump(rows, f, indent=2, ensure_ascii=False)

    print(f"Dumped {len(rows)} rows to storage/tg_edit_rows.json:")
    for r in rows:
        print(f"  [{r['index']:02d}] <{r['tag']}> {r['text'][:70]}")

    page.screenshot(path="storage/tg_edit_screen_clean.png")
    browser.close()
