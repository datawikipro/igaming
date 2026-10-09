import time
from pathlib import Path
from playwright.sync_api import sync_playwright

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()

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
    
    # 1. Click English channel
    for a in page.locator(".chatlist a").all():
        if "English" in a.inner_text():
            a.click()
            break
    page.wait_for_timeout(2000)
    
    # 2. Click top header (x=500, y=45)
    page.mouse.click(500, 45)
    page.wait_for_timeout(2000)
    
    # 3. Click Pencil at (958, 45)
    print("Clicking pencil at (958, 45)...")
    page.mouse.click(958, 45)
    page.wait_for_timeout(2000)
    page.screenshot(path=str(SCREEN_DIR / "tg_test_2_after_pencil.png"))
    
    browser.close()
