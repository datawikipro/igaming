import sys
import time
import random
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
    page.goto("https://x.com/i/flow/login", wait_until="networkidle", timeout=60000)
    time.sleep(4)
    
    # Enter username 'smartbetguru'
    print("Finding username input...")
    inp = page.locator("input[autocomplete='username'], input[type='text'], input[name='text']").first
    inp.click()
    time.sleep(0.5)
    for ch in "smartbetguru":
        page.keyboard.type(ch)
        time.sleep(random.uniform(0.04, 0.1))
    
    time.sleep(1)
    page.screenshot(path="smm-agent/storage/tw_login_typed_user.png")
    
    # Click Continue button
    print("Clicking Continue button...")
    btn = page.locator("button:has-text('Continue'), button:has-text('Next')").first
    btn.click()
    time.sleep(5)
    
    print("URL after Continue:", page.url)
    text = page.locator("body").inner_text()
    print("Page text snippet:\n", text[:400].replace("\n", " "))
    
    page.screenshot(path="smm-agent/storage/tw_login_step2.png")
    browser.close()
