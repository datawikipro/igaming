#!/usr/bin/env python3
import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()
BOT_USERNAME = "smartbet_guru_bot"

def test_add_admin_flow():
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(STORAGE_DIR),
            headless=True,
            args=["--no-sandbox", "--disable-blink-features=AutomationControlled"],
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()
        page.goto("https://web.telegram.org/k/", timeout=45000, wait_until="domcontentloaded")
        time.sleep(5)
        
        # 1. Select English channel
        print("1. Selecting English channel...")
        for a in page.locator(".chatlist a").all():
            if "English" in a.inner_text():
                a.click()
                break
        time.sleep(2)
        
        # 2. Click header (x=480, y=45)
        print("2. Opening Channel Info...")
        page.mouse.click(480, 45)
        time.sleep(2)
        
        # 3. Click pencil at (1228, 44)
        print("3. Opening Edit...")
        page.mouse.click(1228, 44)
        time.sleep(2)
        
        # 4. Scroll down in edit panel
        print("4. Scrolling to Administrators...")
        page.mouse.move(1000, 700)
        page.mouse.wheel(0, 400)
        time.sleep(2)
        
        # 5. Click Administrators (x=1000, y=548)
        print("5. Clicking Administrators...")
        page.mouse.click(1000, 548)
        time.sleep(3)
        
        # 6. Click Add Admin button at (1216, 836)
        print("6. Clicking Add Admin (+) button at (1216, 836)...")
        page.mouse.click(1216, 836)
        time.sleep(3)
        page.screenshot(path=str(SCREEN_DIR / "tg_step1_user_picker.png"))
        
        # 7. Type bot username into search
        print(f"7. Typing @{BOT_USERNAME}...")
        # Check active element or search input
        search_inp = page.locator("input[placeholder*='Search'], .input-search-input").last
        if search_inp.is_visible():
            search_inp.fill(f"@{BOT_USERNAME}")
        else:
            page.keyboard.type(f"@{BOT_USERNAME}")
        time.sleep(3)
        page.screenshot(path=str(SCREEN_DIR / "tg_step2_search_results.png"))
        
        # 8. Inspect search results
        print("Search results:")
        for res in page.locator(".selector-user, .chatlist-chat, .search-group .chatlist-chat, .selector").all():
            try:
                print("  Result:", res.inner_text().replace('\n', ' -- '))
            except Exception:
                pass
                
        browser.close()

if __name__ == "__main__":
    test_add_admin_flow()
