import sys
import time
import random
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

BASE_DIR = Path(__file__).resolve().parent
STORAGE_DIR = BASE_DIR / "storage" / "twitter_firefox_profile"
STORAGE_DIR.mkdir(parents=True, exist_ok=True)

CREDENTIALS = {
    "username": "smartbetguru",
    "email": "www.smartbet.guru@gmail.com",
    "password": "Racoiaws12!@",
}

def human_type(page, locator, text: str):
    locator.click()
    time.sleep(0.5)
    for ch in text:
        page.keyboard.type(ch)
        time.sleep(random.uniform(0.04, 0.12))

def screenshot(page, name: str):
    p = BASE_DIR / "storage" / f"{name}.png"
    page.screenshot(path=str(p))
    print(f"[screenshot] {p}")
    return p

def run_login():
    print("Launching Firefox with persistent context...")
    with sync_playwright() as p:
        browser = p.firefox.launch_persistent_context(
            user_data_dir=str(STORAGE_DIR),
            headless=True,
            proxy={"server": "http://100.83.113.50:3128"},
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()

        print("Navigating to https://x.com...")
        page.goto("https://x.com", wait_until="networkidle", timeout=60000)
        time.sleep(4)
        screenshot(page, "tw_fx_01_loaded")

        # Check if already logged in
        if "/home" in page.url:
            print("Already logged in to X / Twitter! ✅")
            screenshot(page, "tw_fx_home")
            browser.close()
            return True

        # Find input for email or username
        print("Looking for 'Email or username' input...")
        inp = page.locator("input[autocomplete='username'], input[name='text'], input[type='text']").first
        if not inp.is_visible():
            # If not on main screen, try clicking Sign in button
            sign_in = page.locator("a[href='/login'], button:has-text('Sign in'), a:has-text('Sign in')").first
            if sign_in.is_visible():
                print("Clicking Sign In button...")
                sign_in.click()
                time.sleep(4)
                inp = page.locator("input[autocomplete='username'], input[name='text']").first

        print("Entering email...")
        human_type(page, inp, CREDENTIALS["email"])
        screenshot(page, "tw_fx_02_email_entered")
        
        # Click Continue button explicitly
        print("Clicking Continue button...")
        btn_continue = page.locator("button:has-text('Continue')").first
        if btn_continue.is_visible():
            btn_continue.click()
        else:
            page.keyboard.press("Enter")
        time.sleep(5)
        screenshot(page, "tw_fx_03_after_email_submit")

        # Check if asked for username challenge or password
        page_text = page.locator("body").inner_text()
        print("Page text snippet:", page_text[:200].replace("\n", " "))

        if "Enter your phone number or username" in page_text or "phone or username" in page_text:
            print("Challenge detected: entering handle '@smartbetguru'...")
            challenge_inp = page.locator("input[data-testid='ocfEnterTextTextInput'], input[name='text']").first
            human_type(page, challenge_inp, CREDENTIALS["username"])
            screenshot(page, "tw_fx_04_challenge_entered")
            c_btn = page.locator("button:has-text('Next'), button:has-text('Continue')").first
            if c_btn.is_visible():
                c_btn.click()
            else:
                page.keyboard.press("Enter")
            time.sleep(5)
            screenshot(page, "tw_fx_05_after_challenge")

        # Password input
        print("Looking for password input...")
        try:
            page.wait_for_selector("input[type='password'], input[name='password']", timeout=15000)
            pwd_inp = page.locator("input[type='password'], input[name='password']").first
            print("Entering password...")
            pwd_inp.fill(CREDENTIALS["password"])
            screenshot(page, "tw_fx_06_password_entered")
            
            # Click Log in / Next button
            login_btn = page.locator("button:has-text('Log in'), button:has-text('Sign in'), button:has-text('Continue')").first
            if login_btn.is_visible():
                login_btn.click()
            else:
                page.keyboard.press("Enter")
            time.sleep(8)
            screenshot(page, "tw_fx_07_after_password")
        except Exception as e:
            print("Error finding password input:", e)
            screenshot(page, "tw_fx_pwd_err")

        # Check result
        print("Current URL after login:", page.url)
        time.sleep(3)
        screenshot(page, "tw_fx_08_final")
        
        if "/home" in page.url:
            print("🎉 Successfully logged into Twitter/X as @smartbetguru!")
            browser.close()
            return True
        else:
            print("Login flow finished at URL:", page.url)
            browser.close()
            return False

if __name__ == "__main__":
    run_login()
