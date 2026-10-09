import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

PROFILE_DIR = Path("smm-agent/storage/boosty_ru_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()

with sync_playwright() as p:
    browser = p.firefox.launch_persistent_context(
        user_data_dir=str(PROFILE_DIR),
        headless=True,
        locale="ru-RU",
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.goto("https://boosty.to", timeout=40000, wait_until="commit")
    page.wait_for_timeout(4000)
    
    # Dismiss cookie banner
    cookie_btn = page.locator("button:has-text('СОГЛАСЕН'), button:has-text('I AGREE')").first
    if cookie_btn.is_visible():
        cookie_btn.click(force=True)
        page.wait_for_timeout(1000)
        
    page.screenshot(path=str(SCREEN_DIR / "boosty_ru_landing.png"))
    
    # Click register / login
    login_btn = page.locator("button:has-text('РЕГИСТРАЦИЯ'), button:has-text('ВОЙТИ'), button:has-text('SIGN UP'), button:has-text('LOG IN')").first
    if login_btn.is_visible():
        login_btn.click(force=True)
        page.wait_for_timeout(3000)
        page.screenshot(path=str(SCREEN_DIR / "boosty_ru_modal.png"))
        
    html = page.evaluate('''() => {
        const dialog = document.querySelector('[role="dialog"]') || document.querySelector('.modal') || document.body;
        return dialog.innerHTML;
    }''')
    with open("smm-agent/storage/boosty_ru_modal.html", "w", encoding="utf-8") as f:
        f.write(html)
        
    browser.close()
