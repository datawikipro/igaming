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

    # 3. Use Playwright locator to click Channel Type
    print("Clicking Channel Type via locator...")
    loc = page.locator(".edit-peer-container .row-clickable").filter(has_text="Channel Type")
    print("Found count:", loc.count())
    if loc.count() > 0:
        loc.first.click()
        print("Clicked loc.first!")
    
    page.wait_for_timeout(2500)
    page.screenshot(path="storage/tg_channel_type_LOCATOR.png")
    print("Screenshot saved to storage/tg_channel_type_LOCATOR.png")

    browser.close()
