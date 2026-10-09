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
    page.goto(f"https://web.telegram.org/k/#?tgaddr=tg%3A%2F%2Fresolve%3Fdomain%3D{BOT_USERNAME}", timeout=45000, wait_until="domcontentloaded")
    page.wait_for_timeout(4000)
    
    # Click top header to open Bot Info
    page.mouse.click(500, 45)
    page.wait_for_timeout(2000)
    
    # Click 'Add to Group or Channel'
    print("Clicking 'Add to Group or Channel'...")
    add_row = page.locator("text='Add to Group or Channel'").first
    if add_row.is_visible():
        add_row.click()
    else:
        page.mouse.click(850, 665)
    page.wait_for_timeout(3000)
    page.screenshot(path=str(SCREEN_DIR / "tg_bot_add_to_channel_modal.png"))
    
    # Inspect modal elements
    elements = page.evaluate('''() => {
        return Array.from(document.querySelectorAll('.popup-container *, .modal *')).filter(el => el.innerText).map(el => ({
            tag: el.tagName,
            cls: el.className,
            text: el.innerText.substring(0, 40),
            rect: el.getBoundingClientRect()
        }));
    }''')
    print("Modal elements:", elements[:20])
    
    browser.close()
