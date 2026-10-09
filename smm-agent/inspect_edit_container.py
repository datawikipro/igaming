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

    # Click header
    page.locator(".chat-info").first.click()
    page.wait_for_timeout(2000)

    # Click pencil
    page.mouse.click(1228, 44)
    page.wait_for_timeout(2000)

    # Inspect all children of .edit-peer-container
    edit_items = page.evaluate("""() => {
        const editContainer = document.querySelector('.edit-peer-container');
        if (!editContainer) return [];
        const elements = editContainer.querySelectorAll('.sidebar-left-section-container, .row, .item, .menu-item, .btn-menu-item, button, .input-field, .radio, .checkbox, [class*="peer-type"], [class*="channel-type"]');
        return Array.from(elements).map((el, i) => {
            const r = el.getBoundingClientRect();
            return {
                i, tag: el.tagName, className: el.className,
                text: el.innerText ? el.innerText.trim().replace(/\\n/g, ' | ') : '',
                x: Math.round(r.x + r.width/2), y: Math.round(r.y + r.height/2),
                w: Math.round(r.width), h: Math.round(r.height)
            };
        });
    }""")

    with open("storage/edit_items.json", "w", encoding="utf-8") as f:
        import json
        json.dump(edit_items, f, indent=2, ensure_ascii=False)
    
    print(f"Saved {len(edit_items)} items to storage/edit_items.json")
    browser.close()
