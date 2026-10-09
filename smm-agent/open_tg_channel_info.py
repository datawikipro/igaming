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
        
        print("1. Opening Telegram Web...")
        page.goto("https://web.telegram.org/k/", timeout=45000, wait_until="domcontentloaded")
        time.sleep(5)
        
        # Click on English channel
        print("2. Clicking SmartBet.guru English...")
        for a in page.locator(".chatlist a").all():
            if "English" in a.inner_text():
                a.click()
                break
        time.sleep(3)
        
        # Clear draft
        input_box = page.locator(".input-message-input").first
        if input_box.is_visible():
            input_box.click()
            page.keyboard.press("Control+A")
            page.keyboard.press("Backspace")
            time.sleep(1)
            
        # Click header at (480, 45)
        print("3. Clicking header at (480, 45)...")
        page.mouse.click(480, 45)
        time.sleep(3)
        page.screenshot(path=str(SCREEN_DIR / "tg_eng_info_opened.png"))
        print("Done!")
        browser.close()

if __name__ == "__main__":
    run()
