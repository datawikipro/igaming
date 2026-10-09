#!/usr/bin/env python3
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()

def run():
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
        
        # Click on English channel
        for a in page.locator(".chatlist a").all():
            if "English" in a.inner_text():
                a.click()
                break
        time.sleep(2)
        
        # Click header to open Channel Info
        page.mouse.click(480, 45)
        time.sleep(2)
        
        # Click the edit button in sidebar-header
        buttons = page.locator(".sidebar-header button.btn-icon, .sidebar-header .btn-icon")
        print(f"Found {buttons.count()} buttons in sidebar-header")
        # nth(0) is Close, nth(1) is Edit
        buttons.nth(1).click()
        time.sleep(3)
        page.screenshot(path=str(SCREEN_DIR / "tg_edit_clicked.png"))
        print("Clicked edit button!")
        
        # List all visible text elements in the opened view
        for item in page.locator(".sidebar-content .row, .sidebar-content .item, .sidebar-content div[role='button']").all():
            try:
                print("ITEM:", item.inner_text().replace('\n', ' -- '))
            except Exception:
                pass
                
        browser.close()

if __name__ == "__main__":
    run()
