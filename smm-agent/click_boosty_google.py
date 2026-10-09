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

def run():
    with sync_playwright() as p:
        ctx = p.firefox.launch_persistent_context(
            user_data_dir=str(PROFILE_DIR),
            headless=True,
            viewport={"width": 1280, "height": 900}
        )
        page = ctx.pages[0] if ctx.pages else ctx.new_page()
        page.goto("https://boosty.to", timeout=40000, wait_until="commit")
        time.sleep(5)
        
        signup = page.locator("button:has-text('SIGN UP'), a:has-text('SIGN UP')").first
        signup.click(force=True)
        time.sleep(3)
        
        print("Clicking Google icon at (500, 274)...")
        # Click Google icon
        try:
            with ctx.expect_page(timeout=10000) as popup_info:
                page.mouse.click(500, 274)
            popup = popup_info.value
            popup.wait_for_load_state("domcontentloaded")
            time.sleep(3)
            popup.screenshot(path=str(SCREEN_DIR / "boosty_google_popup.png"))
            print("Popup URL:", popup.url)
            print("Popup Title:", popup.title())
        except Exception as e:
            print("No popup or error:", e)
            page.screenshot(path=str(SCREEN_DIR / "boosty_after_google_click.png"))
            print("Current page URL:", page.url)
            
        ctx.close()

if __name__ == "__main__":
    run()
