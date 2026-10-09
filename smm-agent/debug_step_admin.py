import json
import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()

with sync_playwright() as p:
    b = p.chromium.launch_persistent_context(str(STORAGE_DIR), headless=True, viewport={"width": 1280, "height": 900})
    page = b.pages[0] if b.pages else b.new_page()
    page.goto("https://web.telegram.org/k/#-4371643544")
    time.sleep(4)
    page.locator(".chat-info").first.click()
    time.sleep(2)
    page.mouse.click(1228, 44) # Pencil
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/step1_pencil.png")

    # Scroll and click Administrators
    clicked = page.evaluate("""() => {
        const rows = Array.from(document.querySelectorAll('.sidebar-right .row-clickable, .sidebar-right .item, .sidebar-right .row'));
        const adminRow = rows.find(r => r.innerText && r.innerText.includes('Administrators'));
        if (adminRow) {
            adminRow.scrollIntoView({ block: 'center' });
            adminRow.click();
            return true;
        }
        return false;
    }""")
    print("Admin row found and clicked:", clicked)
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/step2_admin_clicked.png")
    
    # Check buttons
    btns = page.evaluate("""() => {
        return Array.from(document.querySelectorAll('.sidebar-right button')).map(b => ({
            text: b.innerText,
            aria: b.getAttribute('aria-label'),
            className: b.className
        }));
    }""")
    print("Buttons found:\n", json.dumps(btns, indent=2))
    b.close()
