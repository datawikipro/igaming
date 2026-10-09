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
    print("Clicking pencil at (1228, 44)...")
    page.mouse.click(1228, 44)
    page.wait_for_timeout(2000)

    # 3. Scroll down 300px
    page.evaluate("""() => {
        const editContainer = document.querySelector('.edit-peer-container') || document.querySelector('.sidebar-slider-item:nth-child(2)');
        if (editContainer) {
            editContainer.scrollTop = 300;
        }
    }""")
    page.wait_for_timeout(1000)

    # 4. Click Channel Type row at (845, 637)
    print("Clicking Channel Type at (845, 637)...")
    page.mouse.click(845, 637)
    page.wait_for_timeout(2000)

    page.screenshot(path="storage/tg_type_opened_confirmed.png")
    print("Screenshot saved to storage/tg_type_opened_confirmed.png")

    browser.close()
