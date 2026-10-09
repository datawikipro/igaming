import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

storage_dir = Path("smm-agent/storage/twitter_profile").resolve()

with sync_playwright() as p:
    browser = p.chromium.launch_persistent_context(
        user_data_dir=str(storage_dir),
        headless=True,
        args=["--disable-blink-features=AutomationControlled", "--no-sandbox"],
        proxy={"server": "http://100.83.113.50:3128"},
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.goto("https://x.com", wait_until="networkidle", timeout=60000)
    time.sleep(5)
    print("URL:", page.url)
    print("Title:", page.title())
    content = page.content()
    print("Content length:", len(content))
    print("First 500 chars of HTML:")
    print(content[:500])
    
    # Check if there are any error messages or scripts
    body_text = page.locator("body").inner_text()
    print("Body text length:", len(body_text))
    print("Body text snippet:", repr(body_text[:300]))

    page.screenshot(path="smm-agent/storage/tw_dom_test.png")
    browser.close()
