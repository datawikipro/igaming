import os
import sys
import time
from playwright.sync_api import sync_playwright

artifact_dir = r"C:\Users\chernousov_a\.gemini\antigravity\brain\800bb1d7-ce35-4b2e-bc44-05201de9200c"
os.makedirs(artifact_dir, exist_ok=True)

console_messages = []
errors = []

def run():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            viewport={"width": 1440, "height": 900},
            user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
        )
        page = context.new_page()

        page.on("console", lambda msg: console_messages.append(f"[{msg.type}] {msg.text}"))
        page.on("pageerror", lambda err: errors.append(f"[PAGE ERROR] {err}"))
        page.on("response", lambda resp: errors.append(f"[HTTP {resp.status}] {resp.url}") if resp.status >= 400 else None)

        print("Navigating to https://smartbet.guru/ru ...")
        page.goto("https://smartbet.guru/ru", wait_until="networkidle", timeout=30000)
        time.sleep(3)

        # Full page screenshot Prematch
        prematch_screenshot = os.path.join(artifact_dir, "smartbet_prematch_full.png")
        page.screenshot(path=prematch_screenshot, full_page=True)
        print(f"Saved prematch screenshot to {prematch_screenshot}")

        # Check arbs elements
        cards = page.locator("[data-testid='arb-card'], .arb-card, article").all()
        print(f"Found {len(cards)} arb cards/elements on Prematch page")

        # Capture first 3 cards if available
        for i, card in enumerate(cards[:3]):
            card_path = os.path.join(artifact_dir, f"arb_card_prematch_{i+1}.png")
            try:
                card.screenshot(path=card_path)
                print(f"Saved card {i+1} screenshot to {card_path}")
            except Exception as e:
                print(f"Failed to screenshot card {i+1}: {e}")

        # Click on Live tab if available
        try:
            live_btn = page.locator("button:has-text('Live'), a:has-text('Live'), [data-tab='live']").first
            if live_btn.is_visible():
                print("Clicking Live tab...")
                live_btn.click()
                time.sleep(3)
                live_screenshot = os.path.join(artifact_dir, "smartbet_live_full.png")
                page.screenshot(path=live_screenshot, full_page=True)
                print(f"Saved live screenshot to {live_screenshot}")
        except Exception as e:
            print(f"Could not switch to Live tab: {e}")

        browser.close()

    print("\n=== CONSOLE LOGS ===")
    for m in console_messages[-20:]:
        print(m)

    print("\n=== HTTP / PAGE ERRORS (>= 400) ===")
    if not errors:
        print("NONE! Clean 200 HTTP responses.")
    else:
        for err in errors:
            print(err)

if __name__ == "__main__":
    run()
