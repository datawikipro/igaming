import os
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

PROFILE_DIR = Path(r"C:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\storage\telegram_web_profile")
PROFILE_DIR.mkdir(parents=True, exist_ok=True)

QR_PATH = Path(r"C:\Users\chernousov_a\.gemini\antigravity\brain\d15af757-f652-4d03-85f1-43d9e1c99936\telegram_qr.png")
LOGGED_IN_PATH = Path(r"C:\Users\chernousov_a\.gemini\antigravity\brain\d15af757-f652-4d03-85f1-43d9e1c99936\telegram_logged_in.png")

def main():
    print("Starting Telegram Web session listener...")
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(PROFILE_DIR),
            headless=True,
            args=["--disable-blink-features=AutomationControlled"]
        )
        page = browser.new_page()
        page.set_viewport_size({"width": 1280, "height": 800})
        
        print("Navigating to https://web.telegram.org/k/ ...")
        page.goto("https://web.telegram.org/k/", wait_until="domcontentloaded", timeout=45000)
        time.sleep(4)
        
        # Save initial QR screenshot
        page.screenshot(path=str(QR_PATH))
        print(f"QR screenshot saved to: {QR_PATH}")
        
        # Check if already logged in
        if page.query_selector("input.input-search-input") or page.query_selector("#folders-tabs"):
            print("ALREADY LOGGED IN!")
            page.screenshot(path=str(LOGGED_IN_PATH))
            browser.close()
            return
            
        print("Waiting for QR code scan by user (timeout: 300s)...")
        start_time = time.time()
        last_snap = start_time
        
        while time.time() - start_time < 300:
            time.sleep(2)
            
            # Check for logged-in selectors
            is_logged_in = False
            for selector in [
                "input.input-search-input",
                "#folders-tabs",
                ".chatlist-parts",
                "button.btn-icon.sidebar-tools-button",
                ".sidebar-header"
            ]:
                if page.query_selector(selector):
                    is_logged_in = True
                    break
                    
            if is_logged_in:
                print(">>> LOGIN DETECTED! USER SCAN CONFIRMED! <<<")
                time.sleep(3)
                page.screenshot(path=str(LOGGED_IN_PATH))
                print(f"Saved logged in screenshot: {LOGGED_IN_PATH}")
                browser.close()
                return
                
            # Periodically refresh QR screenshot every 20s if QR changed
            if time.time() - last_snap > 20:
                page.screenshot(path=str(QR_PATH))
                last_snap = time.time()
                print(f"Refreshed QR code screenshot at {int(time.time() - start_time)}s")
                
        print("Timed out waiting for login scan.")
        browser.close()

if __name__ == "__main__":
    main()
