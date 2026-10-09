import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

PROFILE_DIR = Path("smm-agent/storage/boosty_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()

with sync_playwright() as p:
    browser = p.firefox.launch_persistent_context(
        user_data_dir=str(PROFILE_DIR),
        headless=True,
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    
    requests = []
    page.on("request", lambda r: requests.append(r.url) if "auth" in r.url or "google" in r.url else None)
    
    popup_holder = []
    browser.on("page", lambda new_p: popup_holder.append(new_p))
    
    page.goto("https://boosty.to", timeout=40000, wait_until="commit")
    page.wait_for_timeout(4000)
    
    signup = page.locator("button:has-text('SIGN UP'), a:has-text('SIGN UP')").first
    if signup.is_visible():
        signup.click(force=True)
        page.wait_for_timeout(2000)
        
    google_btn = page.locator('button[data-provider="google"]')
    print("Google button visible:", google_btn.is_visible())
    
    google_btn.click()
    page.wait_for_timeout(5000)
    
    print("Captured requests:", requests)
    print("New pages count:", len(popup_holder))
    if popup_holder:
        popup = popup_holder[0]
        popup.wait_for_load_state("domcontentloaded")
        print("Popup URL:", popup.url)
        popup.screenshot(path=str(SCREEN_DIR / "boosty_popup_captured.png"))
    else:
        page.screenshot(path=str(SCREEN_DIR / "boosty_after_locator_click.png"))
        print("Current page URL:", page.url)
        
    browser.close()
