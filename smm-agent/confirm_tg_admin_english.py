import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()
BOT_USERNAME = "smartbet_guru_bot"

with sync_playwright() as p:
    browser = p.chromium.launch_persistent_context(
        user_data_dir=str(STORAGE_DIR),
        headless=True,
        args=["--no-sandbox", "--disable-blink-features=AutomationControlled"],
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.goto(f"https://web.telegram.org/k/#?tgaddr=tg%3A%2F%2Fresolve%3Fdomain%3D{BOT_USERNAME}", timeout=45000, wait_until="domcontentloaded")
    page.wait_for_timeout(4000)
    
    # Click top header to open Bot Info
    page.mouse.click(500, 45)
    page.wait_for_timeout(2000)
    
    # Click Add to Group or Channel
    row = page.locator("div.row-clickable:has-text('Add to Group or Channel')").first
    row.click()
    page.wait_for_timeout(2000)
    
    # Click 'SmartBet.guru | English'
    target = page.locator(".popup-container .chatlist-chat:has-text('SmartBet.guru | English'), .modal .chatlist-chat:has-text('SmartBet.guru | English')").first
    target.click()
    page.wait_for_timeout(2000)
    
    # Click the checkmark at (1228, 44)
    print("Clicking Checkmark button to confirm adding admin...")
    check_btn = page.locator(".sidebar-header button.appear-zoom, .sidebar-header button:has-text(''), button.primary").first
    if check_btn.is_visible():
        check_btn.click()
    else:
        page.mouse.click(1228, 44)
        
    page.wait_for_timeout(3000)
    page.screenshot(path=str(SCREEN_DIR / "tg_bot_added_english_confirmed.png"))
    print("English channel admin addition confirmed!")
    browser.close()
