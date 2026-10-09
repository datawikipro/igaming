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

    # 1. Click header
    page.locator(".chat-info").first.click()
    page.wait_for_timeout(2000)

    # 2. Click pencil
    page.mouse.click(1228, 44)
    page.wait_for_timeout(2000)

    # 3. Find and click "Channel Type"
    # Find text "Channel Type" in the right sidebar
    channel_type = page.locator(".sidebar-slider-item:nth-child(2)").get_by_text("Channel Type")
    if channel_type.count() > 0:
        channel_type.first.click()
        print("Clicked Channel Type via locator")
    else:
        page.mouse.click(845, 637)
        print("Clicked Channel Type via coordinates")

    page.wait_for_timeout(2000)
    page.screenshot(path="storage/tg_channel_type_screen.png")
    print("Screenshot saved to storage/tg_channel_type_screen.png")

    # Let's inspect the elements on this new screen
    items = page.evaluate("""() => {
        const slider = document.querySelector('.sidebar-slider-item.active') || document.querySelector('.sidebar-slider-item:last-child');
        if (!slider) return [];
        return Array.from(slider.querySelectorAll('input, label, .radio, .checkbox, button, .input-field')).map((el, i) => {
            const r = el.getBoundingClientRect();
            return {
                i, tag: el.tagName, type: el.type, name: el.name,
                text: el.innerText ? el.innerText.trim() : (el.placeholder || el.value || ''),
                x: Math.round(r.x + r.width/2), y: Math.round(r.y + r.height/2),
                w: Math.round(r.width), h: Math.round(r.height)
            };
        });
    }""")
    print("Items found:", items)
    browser.close()
