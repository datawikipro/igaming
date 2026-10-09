import sys
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
    page.on("response", lambda resp: print(f"[RESP {resp.status}] {resp.url}"))
    
    resp = page.goto("https://x.com", timeout=45000)
    print("Page response status:", resp.status if resp else "None")
    print("Page URL:", page.url)
    page.screenshot(path="smm-agent/storage/tw_firefox_test.png")
    print("Screenshot saved!")
    browser.close()
