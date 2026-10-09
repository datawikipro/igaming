import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

PROFILE_DIR = Path("smm-agent/storage/boosty_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()

with sync_playwright() as p:
    browser = p.firefox.launch_persistent_context(
        user_data_dir=str(PROFILE_DIR),
        headless=True,
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.add_init_script('''
        window.__oauthUrl = null;
        window.open = function(url) {
            window.__oauthUrl = url;
            return null;
        };
    ''')
    page.goto("https://boosty.to", timeout=40000, wait_until="commit")
    page.wait_for_timeout(3000)
    
    signup = page.locator("button:has-text('SIGN UP'), a:has-text('SIGN UP')").first
    if signup.is_visible():
        signup.click(force=True)
        page.wait_for_timeout(2000)
        
    page.locator('button[data-provider="google"]').click()
    page.wait_for_timeout(2000)
    
    url = page.evaluate('window.__oauthUrl')
    print("Intercepted OAuth URL:", url)
    if url:
        page.goto(url, wait_until="domcontentloaded", timeout=40000)
        page.wait_for_timeout(4000)
        page.screenshot(path=str(SCREEN_DIR / "boosty_google_screen.png"))
        print("Page URL:", page.url)
        print("Page Title:", page.title())
        
    browser.close()
