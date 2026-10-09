import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

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

    # 3. Scroll .edit-peer-container down by 300px
    page.evaluate("""() => {
        const editContainer = document.querySelector('.edit-peer-container') || document.querySelector('.sidebar-slider-item:nth-child(2)');
        if (editContainer) {
            editContainer.scrollTop = 300;
        }
    }""")
    page.wait_for_timeout(1000)

    page.screenshot(path="storage/tg_edit_scrolled_down.png")
    print("Screenshot saved to storage/tg_edit_scrolled_down.png")
    browser.close()
