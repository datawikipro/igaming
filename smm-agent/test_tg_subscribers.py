import time
from pathlib import Path
from playwright.sync_api import sync_playwright

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
    page.goto("https://web.telegram.org/k/", timeout=45000, wait_until="domcontentloaded")
    page.wait_for_timeout(4000)
    
    # 1. Select English channel
    for a in page.locator(".chatlist a").all():
        if "English" in a.inner_text():
            a.click()
            break
    page.wait_for_timeout(2000)
    
    # Open Channel Info
    page.mouse.click(500, 45)
    page.wait_for_timeout(2000)
    
    # Open Edit
    page.mouse.click(1228, 43)
    page.wait_for_timeout(2000)
    
    page.mouse.move(1000, 500)
    page.mouse.wheel(0, 400)
    page.wait_for_timeout(2000)
    
    # Click Subscribers row (it was below Administrators, around y=615)
    print("Clicking Subscribers row at (1000, 615)...")
    page.mouse.click(1000, 615)
    page.wait_for_timeout(2000)
    page.screenshot(path=str(SCREEN_DIR / "tg_subscribers_opened.png"))
    
    # In Subscribers screen, click Add Subscriber (+) button at (1216, 836)
    print("Clicking Add Subscriber button at (1216, 836)...")
    page.mouse.click(1216, 836)
    page.wait_for_timeout(2000)
    page.screenshot(path=str(SCREEN_DIR / "tg_add_sub_modal.png"))
    
    # Type bot username
    inp = page.locator("input.selector-search-input").last
    if inp.is_visible():
        print(f"Typing @{BOT_USERNAME} in Add Subscriber modal...")
        inp.fill(f"@{BOT_USERNAME}")
        page.wait_for_timeout(3000)
        page.screenshot(path=str(SCREEN_DIR / "tg_add_sub_typed.png"))
        
    browser.close()
