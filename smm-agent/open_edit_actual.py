import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

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

    # 2. Click the pencil at (958, 48)
    print("Clicking pencil at (958, 48)...")
    page.mouse.click(958, 48)
    page.wait_for_timeout(2000)

    page.screenshot(path="storage/tg_edit_screen_actual.png")
    print("Screenshot saved to storage/tg_edit_screen_actual.png")

    # Let's inspect interactive elements in this edit screen
    elements = page.evaluate("""() => {
        const editContainer = document.querySelector('.edit-peer-container') || document.querySelector('.sidebar-slider-item:last-child');
        if (!editContainer) return [];
        return Array.from(editContainer.querySelectorAll('*')).map((el, i) => {
            const r = el.getBoundingClientRect();
            const text = (el.innerText || el.placeholder || el.value || '').trim().replace(/\\n/g, ' | ');
            return {
                i, tag: el.tagName, className: el.className, text,
                x: Math.round(r.x + r.width/2), y: Math.round(r.y + r.height/2),
                w: Math.round(r.width), h: Math.round(r.height),
                visible: r.width > 0 && r.height > 0
            };
        }).filter(e => e.visible && e.text.length > 0 && e.text.length < 50);
    }""")

    with open("storage/tg_actual_edit_elements.json", "w", encoding="utf-8") as f:
        import json
        json.dump(elements, f, indent=2, ensure_ascii=False)

    print(f"Found {len(elements)} elements")
    browser.close()
