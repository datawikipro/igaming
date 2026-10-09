#!/usr/bin/env python3
import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

PROFILE_DIR = Path("smm-agent/storage/boosty_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()

def open_signup():
    with sync_playwright() as p:
        browser = p.firefox.launch_persistent_context(
            user_data_dir=str(PROFILE_DIR),
            headless=True,
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()
        page.goto("https://boosty.to", timeout=40000, wait_until="commit")
        time.sleep(5)
        
        # Click cookie I AGREE if visible
        cookie = page.locator("button:has-text('I AGREE')").first
        if cookie.is_visible():
            cookie.click(force=True)
            time.sleep(1)
            
        # Click SIGN UP
        print("Clicking SIGN UP...")
        signup = page.locator("button:has-text('SIGN UP'), a:has-text('SIGN UP')").first
        signup.click(force=True)
        time.sleep(4)
        page.screenshot(path=str(SCREEN_DIR / "boosty_signup_modal.png"))
        
        # Print modal snippet
        body = page.locator("body").inner_text()
        print("Body text snippet:")
        for line in body.split('\n'):
            if line.strip():
                print("  ", line.strip())
                
        browser.close()

if __name__ == "__main__":
    open_signup()
