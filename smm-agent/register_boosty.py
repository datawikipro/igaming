#!/usr/bin/env python3
import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

PROFILE_DIR = Path("smm-agent/storage/boosty_profile").resolve()
PROFILE_DIR.mkdir(parents=True, exist_ok=True)
SCREEN_DIR = Path("smm-agent/storage").resolve()

def open_boosty():
    with sync_playwright() as p:
        browser = p.firefox.launch_persistent_context(
            user_data_dir=str(PROFILE_DIR),
            headless=True,
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()
        
        print("1. Opening https://boosty.to...")
        page.goto("https://boosty.to", timeout=40000, wait_until="commit")
        time.sleep(6)
        page.screenshot(path=str(SCREEN_DIR / "boosty_home.png"))
        print(f"Title: {page.title()} | URL: {page.url}")
        
        # Look for login / registration buttons
        print("Looking for login/registration buttons...")
        login_btn = page.locator("button:has-text('Войти'), a:has-text('Войти'), button:has-text('Log in'), a:has-text('Log in'), button:has-text('Стать автором')").first
        if login_btn.is_visible():
            print(f"Found button: '{login_btn.inner_text()}'. Clicking it...")
            login_btn.click()
            time.sleep(4)
            page.screenshot(path=str(SCREEN_DIR / "boosty_login_modal.png"))
            
            # Print visible text in modal
            modal_text = page.locator("body").inner_text()
            print("Modal snippet:", modal_text[:400].replace('\n', ' -- '))
        else:
            print("Login button not found directly. Dumping buttons:")
            for b in page.locator("header button, header a, nav a, nav button").all():
                try:
                    print("  Btn:", b.inner_text().strip())
                except Exception:
                    pass
                    
        browser.close()

if __name__ == "__main__":
    open_boosty()
