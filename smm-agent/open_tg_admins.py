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
        
        # Click pencil at (1228, 44)
        page.mouse.click(1228, 44)
        time.sleep(2)
        
        # Scroll down in the edit panel
        print("Scrolling down in edit panel...")
        page.mouse.move(1000, 700)
        page.mouse.wheel(0, 400)
        time.sleep(2)
        page.screenshot(path=str(SCREEN_DIR / "tg_edit_scrolled.png"))
        
        # Find Administrators item
        admin_item = page.locator("text='Administrators', text='Admins'").first
        if admin_item.is_visible():
            print("Found Administrators item! Clicking it...")
            admin_item.click()
            time.sleep(3)
            page.screenshot(path=str(SCREEN_DIR / "tg_admins_view.png"))
        else:
            print("Administrators text not found directly, listing all items:")
            for row in page.locator(".sidebar-content div, .sidebar-content a, .sidebar-content button").all():
                try:
                    t = row.inner_text().strip()
                    if t and len(t) < 40:
                        print("  Row:", t)
                except Exception:
                    pass
                    
        browser.close()

if __name__ == "__main__":
    run()
