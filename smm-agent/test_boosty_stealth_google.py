import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

PROFILE_DIR = Path("smm-agent/storage/boosty_stealth_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()

EMAIL = "www.smartbet.guru@gmail.com"
PASSWORD = "Racoiaws12"

firefox_user_prefs = {
    "marionette.enabled": False,
    "dom.webdriver.enabled": False,
    "media.peerconnection.enabled": True,
    "network.http.sendRefererHeader": 2,
    "privacy.resistFingerprinting": False,
}

with sync_playwright() as p:
    browser = p.firefox.launch_persistent_context(
        user_data_dir=str(PROFILE_DIR),
        headless=True,
        firefox_user_prefs=firefox_user_prefs,
        user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0",
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.add_init_script('''
        window.__oauthUrl = null;
        window.open = function(url) {
            window.__oauthUrl = url;
            return null;
        };
        Object.defineProperty(navigator, 'webdriver', {
            get: () => false,
        });
    ''')
    
    page.goto("https://boosty.to", timeout=40000, wait_until="commit")
    page.wait_for_timeout(3000)
    
    cookie_btn = page.locator("button:has-text('I AGREE'), button:has-text('СОГЛАСЕН')").first
    if cookie_btn.is_visible():
        cookie_btn.click(force=True)
        page.wait_for_timeout(1000)
        
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
        page.wait_for_timeout(3000)
        
        # 1. Fill email
        email_inp = page.locator('input[type="email"], input[name="identifier"]').first
        if email_inp.is_visible():
            print("Entering email:", EMAIL)
            email_inp.fill(EMAIL)
            page.locator('button:has-text("Next"), #identifierNext').first.click()
            
            print("Waiting for password input...")
            try:
                page.wait_for_selector('input[type="password"], input[name="Passwd"]', timeout=15000)
            except Exception as e:
                print("Wait timed out...")
                
            page.screenshot(path=str(SCREEN_DIR / "boosty_stealth_after_email.png"))
            
            pwd_inp = page.locator('input[type="password"], input[name="Passwd"]').first
            if pwd_inp.is_visible():
                print("SUCCESS: Password input is visible! Entering password...")
                pwd_inp.fill(PASSWORD)
                page.locator('button:has-text("Next"), #passwordNext').first.click()
                page.wait_for_timeout(10000)
                page.screenshot(path=str(SCREEN_DIR / "boosty_stealth_after_password.png"))
                print("URL after password:", page.url)
            else:
                print("Password input not visible! Page title:", page.title())
                
    browser.close()
