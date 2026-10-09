import sys
import json
from playwright.sync_api import sync_playwright
from pathlib import Path

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()

with sync_playwright() as p:
    browser = p.chromium.launch_persistent_context(
        user_data_dir=str(STORAGE_DIR),
        headless=True,
        args=["--no-sandbox", "--disable-blink-features=AutomationControlled"],
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.goto("https://web.telegram.org/k/#?tgaddr=tg%3A%2F%2Fresolve%3Fdomain%3Dsmartbet_guru_bot", timeout=45000, wait_until="domcontentloaded")
    page.wait_for_timeout(4000)
    page.mouse.click(500, 45)
    page.wait_for_timeout(2000)
    
    info = page.evaluate('''() => {
        // find row
        const rows = Array.from(document.querySelectorAll('.sidebar-content div, .sidebar-content a, .sidebar-content button')).filter(e => 
            e.innerText && e.innerText.includes('Add to Group or Channel')
        );
        return rows.map(el => ({
            tag: el.tagName,
            cls: el.className,
            text: el.innerText.trim(),
            rect: el.getBoundingClientRect()
        }));
    }''')
    print("Add rows:", json.dumps(info, indent=2))
    browser.close()
