import sys
import time
import random
from pathlib import Path
from playwright.sync_api import sync_playwright, TimeoutError as PWTimeout

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

BASE_DIR = Path(__file__).resolve().parent
STORAGE_DIR = BASE_DIR / "storage" / "twitter_profile"
STORAGE_DIR.mkdir(parents=True, exist_ok=True)

CREDENTIALS = {
    "username": "smartbetguru",
    "email": "www.smartbet.guru@gmail.com",
    "password": "Racoiaws12!@",
}

def human_type(page, selector_or_locator, text: str):
    if isinstance(selector_or_locator, str):
        loc = page.locator(selector_or_locator).first
    else:
        loc = selector_or_locator
    loc.click()
    for ch in text:
        page.keyboard.type(ch)
        time.sleep(random.uniform(0.04, 0.12))

def wait_and_screenshot(page, name: str, timeout: int = 3000):
    time.sleep(timeout / 1000)
    out = BASE_DIR / "storage" / f"{name}.png"
    page.screenshot(path=str(out))
    print(f"[screenshot] {out}")
    return out

def run():
    print("Launching Chromium for Twitter login...")
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(STORAGE_DIR),
            headless=True,
            args=[
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-dev-shm-usage",
            ],
            viewport={"width": 1280, "height": 900},
            locale="en-US",
            proxy={"server": "http://100.83.113.50:3128"},
            user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        )
        page = browser.pages[0] if browser.pages else browser.new_page()

        print("Navigating to https://x.com/home...")
        page.goto("https://x.com/home", wait_until="domcontentloaded", timeout=60000)
        time.sleep(4)
        print("Current URL:", page.url)

        if "/home" in page.url and "/login" not in page.url and "/i/flow" not in page.url:
            print("Already logged in to X / Twitter! ✅")
            wait_and_screenshot(page, "tw_already_home")
            browser.close()
            return True

        print("Navigating to https://x.com/login...")
        page.goto("https://x.com/login", wait_until="domcontentloaded", timeout=60000)
        time.sleep(5)
        wait_and_screenshot(page, "tw_login_page")

        # Step 1: Username / email
        print("Looking for username / email input...")
        try:
            page.wait_for_selector("input[autocomplete='username'], input[name='text']", timeout=30000)
            inp = page.locator("input[autocomplete='username'], input[name='text']").first
            human_type(page, inp, CREDENTIALS["email"])
            page.keyboard.press("Enter")
            print("Entered email, waiting...")
            time.sleep(4)
            wait_and_screenshot(page, "tw_after_email")
        except Exception as e:
            print("Error entering email:", e)
            wait_and_screenshot(page, "tw_email_err")

        # Check for challenge or password
        print("Checking for challenge / password screen...")
        time.sleep(2)
        cur_text = page.locator("body").inner_text()
        print("Page text snippet:", cur_text[:200].replace("\n", " "))

        if "Enter your phone number or username" in cur_text or "phone or username" in cur_text:
            print("Unusual activity challenge detected: entering handle '@smartbetguru'...")
            try:
                inp_user = page.locator("input[data-testid='ocfEnterTextTextInput'], input[name='text']").first
                human_type(page, inp_user, CREDENTIALS["username"])
                page.keyboard.press("Enter")
                time.sleep(4)
                wait_and_screenshot(page, "tw_after_challenge")
            except Exception as e:
                print("Error entering username challenge:", e)

        # Step 2: Password
        print("Looking for password input...")
        try:
            page.wait_for_selector("input[type='password'], input[name='password']", timeout=20000)
            pwd_inp = page.locator("input[type='password'], input[name='password']").first
            human_type(page, pwd_inp, CREDENTIALS["password"])
            page.keyboard.press("Enter")
            print("Entered password, waiting for login...")
            time.sleep(6)
            wait_and_screenshot(page, "tw_after_password")
        except Exception as e:
            print("Error entering password:", e)
            wait_and_screenshot(page, "tw_pwd_err")

        print("Checking final state...")
        time.sleep(3)
        final_url = page.url
        print("Final URL:", final_url)
        wait_and_screenshot(page, "tw_final_state")

        if "/home" in final_url:
            print("✅ Successfully logged in to Twitter/X as @smartbetguru!")
            browser.close()
            return True
        else:
            print("Login flow completed with URL:", final_url)
            browser.close()
            return False

if __name__ == "__main__":
    run()
