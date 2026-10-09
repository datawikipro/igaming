import os
import time
from playwright.sync_api import sync_playwright

artifact_dir = r"C:\Users\chernousov_a\.gemini\antigravity\brain\800bb1d7-ce35-4b2e-bc44-05201de9200c"

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={"width": 1440, "height": 900})
    page.goto("https://smartbet.guru/ru", wait_until="networkidle")
    
    # Click "ЛАЙВ" button
    live_btn = page.locator("button:has-text('ЛАЙВ'), button:has-text('Лайв')").first
    if live_btn.is_visible():
        print("Found Лайв button, clicking...")
        live_btn.click()
        time.sleep(2)
        live_screenshot = os.path.join(artifact_dir, "smartbet_live_full.png")
        page.screenshot(path=live_screenshot, full_page=True)
        print(f"Saved live screenshot to {live_screenshot}")
    else:
        print("Лайв button not found")
        
    browser.close()
