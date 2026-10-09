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
    page.goto("https://x.com", wait_until="networkidle", timeout=60000)
    time.sleep(3)
    
    # 1. Click loginButton
    print("Looking for loginButton...")
    login_btn = page.locator('a[data-testid="loginButton"], a[href="/login"]').first
    if login_btn.is_visible():
        print("Clicking loginButton:", login_btn)
        login_btn.click()
        time.sleep(4)
    else:
        print("loginButton not found, navigating directly to /i/flow/login...")
        page.goto("https://x.com/i/flow/login", wait_until="networkidle", timeout=60000)
        time.sleep(4)
    
    page.screenshot(path="smm-agent/storage/tw_login_modal_opened.png")
    
    # 2. Enter username
    print("Finding username input...")
    username_input = page.locator('input[autocomplete="username"]').first
    username_input.wait_for(state="visible", timeout=15000)
    username_input.fill("smartbetguru")
    time.sleep(1)
    
    # Click Next
    print("Clicking Next button...")
    next_btn = page.locator('button:has-text("Next")').first
    next_btn.click()
    time.sleep(4)
    page.screenshot(path="smm-agent/storage/tw_after_next.png")
    
    # 3. Check for unusual activity email check
    email_input = page.locator('input[data-testid="ocfEnterTextTextInput"]').first
    if email_input.is_visible():
        print("Unusual activity: entering email...")
        email_input.fill("www.smartbet.guru@gmail.com")
        page.locator('button:has-text("Next")').first.click()
        time.sleep(4)
        page.screenshot(path="smm-agent/storage/tw_after_email_check.png")
    
    # 4. Enter password
    print("Looking for password input...")
    pwd_input = page.locator('input[name="password"]').first
    if pwd_input.is_visible():
        print("Entering password...")
        pwd_input.fill("Racoiaws12")
        time.sleep(1)
        page.screenshot(path="smm-agent/storage/tw_pwd_filled.png")
        login_submit = page.locator('button[data-testid="LoginForm_Login_Button"]').first
        login_submit.click()
        time.sleep(7)
        page.screenshot(path="smm-agent/storage/tw_after_login_submit.png")
        print("URL after login submit:", page.url)
    else:
        print("Password input not visible! Current text snippet:")
        print(page.locator("body").inner_text()[:400].replace("\n", " "))
    
    browser.close()
