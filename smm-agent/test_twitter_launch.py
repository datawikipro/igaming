import sys
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

storage_dir = Path("smm-agent/storage/twitter_profile").resolve()
storage_dir.mkdir(parents=True, exist_ok=True)

print(f"Testing Twitter browser launch with storage: {storage_dir}...")
with sync_playwright() as p:
    browser = p.chromium.launch_persistent_context(
        user_data_dir=str(storage_dir),
        headless=True,
        args=["--disable-blink-features=AutomationControlled", "--no-sandbox"],
        proxy={"server": "http://100.83.113.50:3128"},
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    print("Navigating to https://x.com...")
    page.goto("https://x.com", wait_until="domcontentloaded", timeout=45000)
    print(f"URL: {page.url}")
    print(f"Title: {page.title()}")
    screenshot_path = Path("smm-agent/storage/tw_initial_test.png").resolve()
    page.screenshot(path=str(screenshot_path))
    print(f"Screenshot saved to: {screenshot_path}")
    browser.close()
print("Success!")
