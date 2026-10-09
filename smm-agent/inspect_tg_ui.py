#!/usr/bin/env python3
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()

def inspect_channel_ui():
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
        
        # Click on SmartBet.guru | English
        print("2. Clicking SmartBet.guru | English chat in sidebar...")
        chat = page.locator(".chatlist a:has-text('SmartBet.guru | English')").first
        chat.click()
        time.sleep(3)
        page.screenshot(path=str(SCREEN_DIR / "tg_eng_chat_opened.png"))
        
        # Clear draft message if any
        input_box = page.locator(".input-message-input").first
        if input_box.is_visible():
            input_box.click()
            page.keyboard.press("Control+A")
            page.keyboard.press("Backspace")
            time.sleep(1)
            
        # Click the channel title header to open Channel Info panel
        print("3. Clicking channel header...")
        header = page.locator(".chat-info, .top-profile, .peer-title").first
        header.click()
        time.sleep(3)
        page.screenshot(path=str(SCREEN_DIR / "tg_eng_info_panel.png"))
        
        # Dump visible text in info panel
        print("Visible buttons/elements in info panel:")
        for el in page.locator(".sidebar-header button, .profile-avatars-info, .chat-info, .btn-icon, .profile-content").all():
            try:
                txt = el.inner_text().replace('\n', ' ')
                aria = el.get_attribute("aria-label") or ""
                cls = el.get_attribute("class") or ""
                print(f"  El: [{cls}] aria='{aria}' text='{txt[:60]}'")
            except Exception:
                pass
                
        browser.close()

if __name__ == "__main__":
    inspect_channel_ui()
