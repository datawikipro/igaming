import time
from pathlib import Path
from playwright.sync_api import sync_playwright

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()
BOT_USERNAME = "smartbet_guru_bot"

with sync_playwright() as p:
    browser = p.chromium.launch_persistent_context(
        user_data_dir=str(STORAGE_DIR),
        headless=True,
        args=["--no-sandbox", "--disable-blink-features=AutomationControlled"],
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.goto("https://web.telegram.org/k/", timeout=45000, wait_until="domcontentloaded")
    page.wait_for_timeout(4000)
    
    # 1. Select English channel
    for a in page.locator(".chatlist a").all():
        if "English" in a.inner_text():
            a.click()
            break
    page.wait_for_timeout(2000)
    
    # Clear any chat input draft
    chat_input = page.locator(".input-message-input").first
    if chat_input.is_visible():
        chat_input.fill("")
        
    # 2. Click top header
    page.mouse.click(500, 45)
    page.wait_for_timeout(2000)
    
    # 3. Click Pencil at (1228, 43)
    page.mouse.click(1228, 43)
    page.wait_for_timeout(2000)
    
    # 4. Scroll down in edit sidebar
    page.mouse.move(1000, 500)
    page.mouse.wheel(0, 400)
    page.wait_for_timeout(2000)
    
    # 5. Click Administrators row
    page.mouse.click(1000, 545)
    page.wait_for_timeout(2000)
    
    # 6. Click Add Administrator (+) button at (1216, 836)
    page.mouse.click(1216, 836)
    page.wait_for_timeout(2000)
    page.screenshot(path=str(SCREEN_DIR / "tg_debug_1_modal.png"))
    
    # 7. Use the exact selector we found: input.selector-search-input
    # There are two: one in sidebar (x=929), one in popup modal (x=455)
    # The modal one is the LAST one or has x=455
    inp = page.locator("input.selector-search-input").last
    print("Found selector-search-input:", inp.is_visible())
    inp.click()
    page.keyboard.type(f"@{BOT_USERNAME}")
    page.wait_for_timeout(3000)
    page.screenshot(path=str(SCREEN_DIR / "tg_debug_2_typed.png"))
    
    # Inspect all elements in modal
    elements = page.evaluate('''() => {
        return Array.from(document.querySelectorAll('body *')).filter(el => {
            const r = el.getBoundingClientRect();
            return r.left >= 300 && r.right <= 900 && r.top >= 100 && r.bottom <= 800 && el.innerText && el.innerText.includes('smartbet');
        }).map(el => ({
            tag: el.tagName,
            cls: el.className,
            text: el.innerText.substring(0, 50),
            rect: el.getBoundingClientRect()
        }));
    }''')
    print("Found matching elements:", elements)
    
    browser.close()
