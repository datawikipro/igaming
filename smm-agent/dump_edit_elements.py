import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()

with sync_playwright() as p:
    browser = p.chromium.launch_persistent_context(
        user_data_dir=str(STORAGE_DIR),
        headless=True,
        args=["--disable-blink-features=AutomationControlled", "--no-sandbox"],
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.goto("https://web.telegram.org/k/#-3960368887")
    page.wait_for_timeout(3000)

    # Click chat info header
    page.locator(".chat-info").first.click()
    page.wait_for_timeout(2000)

    # Click pencil edit icon
    page.mouse.click(1228, 44)
    page.wait_for_timeout(2000)

    page.screenshot(path="storage/debug_edit_state.png")

    # Find all clickable elements in sidebar
    elements = page.locator(".sidebar-slider-item.active *").all()
    print(f"Total elements in active slider: {len(elements)}")
    with open("storage/debug_slider_elements.txt", "w", encoding="utf-8") as f:
        for el in elements:
            try:
                tag = el.evaluate("e => e.tagName")
                txt = el.evaluate("e => e.innerText")
                cls = el.evaluate("e => e.className")
                if txt and len(txt.strip()) > 0 and len(txt.strip()) < 100:
                    f.write(f"<{tag} class='{cls}'>: {txt.strip()}\n")
            except:
                pass
    browser.close()
print("DUMP COMPLETED")
