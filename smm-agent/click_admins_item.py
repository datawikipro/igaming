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
        
        # Open Info
        page.mouse.click(480, 45)
        time.sleep(2)
        
        # Click pencil at (1228, 44)
        page.mouse.click(1228, 44)
        time.sleep(2)
        
        # Scroll down in edit panel
        page.mouse.move(1000, 700)
        page.mouse.wheel(0, 400)
        time.sleep(2)
        
        # Click Administrators at (1000, 548)
        print("Clicking Administrators at (1000, 548)...")
        page.mouse.click(1000, 548)
        time.sleep(3)
        page.screenshot(path=str(SCREEN_DIR / "tg_admins_view_opened.png"))
        
        print("Listing elements in opened panel:")
        for el in page.locator(".sidebar-content div, .sidebar-content button, .sidebar-content a, .sidebar-content span").all():
            try:
                t = el.inner_text().strip()
                if t and 2 < len(t) < 35:
                    print("  Text:", t)
            except Exception:
                pass
                
        browser.close()

if __name__ == "__main__":
    run()
