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
    page.goto("https://web.telegram.org/k/#-3960368887")
    page.wait_for_timeout(3000)

    # 1. Click header
    page.locator(".chat-info").first.click()
    page.wait_for_timeout(2000)

    # 2. Click pencil
    page.mouse.click(1228, 44)
    page.wait_for_timeout(2000)

    # 3. Get exact center of the row in browser
    coords = page.evaluate("""() => {
        const rows = Array.from(document.querySelectorAll('.edit-peer-container .row-clickable'));
        for (const r of rows) {
            if (r.innerText && r.innerText.includes('Channel Type')) {
                const rect = r.getBoundingClientRect();
                return {
                    found: true,
                    x: Math.round(rect.x + rect.width / 2),
                    y: Math.round(rect.y + rect.height / 2),
                    rect: { x: rect.x, y: rect.y, w: rect.width, h: rect.height }
                };
            }
        }
        return { found: false, totalRows: rows.length };
    }""")
    print("Exact row coords:", coords)

    if coords.get('found'):
        x, y = coords['x'], coords['y']
        print(f"Clicking at ({x}, {y})...")
        page.mouse.move(x, y)
        page.wait_for_timeout(200)
        page.mouse.down()
        page.wait_for_timeout(100)
        page.mouse.up()
        page.wait_for_timeout(2000)

    page.screenshot(path="storage/tg_type_clicked_pointer.png")
    print("Screenshot saved to storage/tg_type_clicked_pointer.png")
    browser.close()
