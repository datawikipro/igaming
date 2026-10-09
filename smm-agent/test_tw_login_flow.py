import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

storage_dir = Path("smm-agent/storage/twitter_firefox_profile").resolve()

with sync_playwright() as p:
    browser = p.firefox.launch_persistent_context(
        user_data_dir=str(storage_dir),
        headless=True,
        proxy={"server": "http://100.83.113.50:3128"},
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    print("Navigating to https://x.com/i/flow/login...")
    page.goto("https://x.com/i/flow/login", wait_until="networkidle", timeout=60000)
    time.sleep(5)
    print("URL:", page.url)
    print("Title:", page.title())
    out = Path("smm-agent/storage/tw_flow_login.png").resolve()
    page.screenshot(path=str(out))
    print("Screenshot saved to:", out)
    browser.close()
