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

    # Scroll down
    page.evaluate("""() => {
        const scrollers = document.querySelectorAll('.sidebar-right .scrollable, .sidebar-right .sidebar-content, .sidebar-right');
        scrollers.forEach(s => s.scrollTop += 600);
    }""")
    time.sleep(2)
    
    # Click Subscribers
    print("Clicking Subscribers...")
    page.mouse.click(1084, 424)
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/tg_subscribers_view.png")
    
    # Check buttons inside subscribers view
    btns = page.evaluate("""() => {
        return Array.from(document.querySelectorAll('.sidebar-right button')).map(b => ({
            text: b.innerText,
            aria: b.getAttribute('aria-label'),
            className: b.className,
            rect: b.getBoundingClientRect()
        }));
    }""")
    print("Buttons inside subscribers view:\n", json.dumps(btns, indent=2))
    
    # Click Add Subscribers button (the visible floating circle button)
    print("Clicking Add Subscribers button...")
    page.evaluate("""() => {
        const btn = document.querySelector('.sidebar-right button.is-visible, button[aria-label="Add Subscribers"], button[aria-label="Add Members"]');
        if (btn) btn.click();
    }""")
    time.sleep(3)
    page.screenshot(path="smm-agent/storage/tg_add_subscriber_modal.png")
    
    # Type smartbet_guru_bot into search
    print("Typing smartbet_guru_bot into search...")
    inp = page.locator(".popup input.selector-search-input, .popup input[type='search'], .popup input").first
    if inp.is_visible():
        inp.click()
        page.keyboard.type("smartbet_guru_bot", delay=80)
        time.sleep(3)
        page.screenshot(path="smm-agent/storage/tg_search_bot_in_subscribers.png")
        
        items = page.evaluate("""() => {
            return Array.from(document.querySelectorAll('.popup .chatlist-chat, .popup .row-clickable, .popup .selector-item, .popup .selector-user-item')).map(el => ({
                text: el.innerText.replace(/\\n/g, ' '),
                className: el.className
            }));
        }""")
        print("Popup items after typing in subscribers search:\n", json.dumps(items, indent=2, ensure_ascii=False))
    b.close()
