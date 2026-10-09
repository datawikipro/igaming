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
    
    # 1. Click English channel
    for a in page.locator(".chatlist a").all():
        if "English" in a.inner_text():
            a.click()
            break
    page.wait_for_timeout(2000)
    
    # 2. Click top header (x=500, y=45)
    page.mouse.click(500, 45)
    page.wait_for_timeout(2000)
    
    # 3. Click Pencil at (1228, 43)
    page.mouse.click(1228, 43)
    page.wait_for_timeout(2000)
    
    # 4. Scroll down in edit sidebar
    page.mouse.move(1000, 500)
    page.mouse.wheel(0, 400)
    page.wait_for_timeout(2000)
    
    # 5. Click Administrators row at (1000, 545)
    page.mouse.click(1000, 545)
    page.wait_for_timeout(2000)
    
    # 6. Click Add Administrator (+) button at (1216, 836)
    page.mouse.click(1216, 836)
    page.wait_for_timeout(2000)
    
    # 7. Find search input in the modal:
    # rect was x=455, y=179.
    print(f"Typing @{BOT_USERNAME} in popup search input...")
    # Target input inside popup
    popup_input = page.locator(".popup .selector-search-input, .modal .selector-search-input, .popup input, div[class*='popup'] input").last
    if popup_input.is_visible():
        popup_input.fill(f"@{BOT_USERNAME}")
    else:
        page.mouse.click(550, 195)
        page.keyboard.type(f"@{BOT_USERNAME}")
        
    page.wait_for_timeout(3000)
    page.screenshot(path=str(SCREEN_DIR / "tg_test_7_search_typed.png"))
    
    # Inspect items in modal
    items = page.evaluate('''() => {
        const popup = document.querySelector('.popup') || document.querySelector('.modal') || document.querySelector('.popup-container');
        if (!popup) return [];
        return Array.from(popup.querySelectorAll('.chatlist-chat, .selector-user, div[class*="chat"], div[class*="user"]')).map(el => ({
            text: el.innerText,
            cls: el.className,
            rect: el.getBoundingClientRect()
        }));
    }''')
    print("Modal items found:", items)
    browser.close()
