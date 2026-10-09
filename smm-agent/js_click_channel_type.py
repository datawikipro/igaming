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

    # 2. Click pencil
    page.mouse.click(1228, 44)
    page.wait_for_timeout(2000)

    # 3. Find element containing "Channel Type" and click it via JS
    res = page.evaluate("""() => {
        const editContainer = document.querySelector('.edit-peer-container');
        if (!editContainer) return 'no edit-peer-container';
        
        // Find all elements containing text 'Channel Type'
        const all = Array.from(editContainer.querySelectorAll('*'));
        const matches = all.filter(el => el.innerText && el.innerText.includes('Channel Type'));
        
        // Find the most specific (deepest or row) container
        const target = matches.find(el => el.classList.contains('row') || el.classList.contains('item') || el.classList.contains('sidebar-left-section-container') || el.tagName === 'DIV');
        
        if (target) {
            target.scrollIntoView();
            target.click();
            // Also trigger dispatchEvent
            target.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));
            return {
                clicked: true,
                tag: target.tagName,
                className: target.className,
                text: target.innerText.replace(/\\n/g, ' ')
            };
        }
        return { clicked: false, count: matches.length };
    }""")
    print("JS Click Result:", res)
    page.wait_for_timeout(2000)

    page.screenshot(path="storage/tg_type_js_clicked.png")
    print("Screenshot saved to storage/tg_type_js_clicked.png")
    browser.close()
