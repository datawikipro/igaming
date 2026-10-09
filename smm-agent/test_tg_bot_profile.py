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
    page.wait_for_timeout(5000)
    page.screenshot(path=str(SCREEN_DIR / "tg_bot_direct_chat.png"))
    
    # Click top header (chat info)
    print("Clicking bot header at (500, 45)...")
    page.mouse.click(500, 45)
    page.wait_for_timeout(2000)
    page.screenshot(path=str(SCREEN_DIR / "tg_bot_info_profile.png"))
    
    # Inspect menu buttons or 3 dots in the bot profile
    dots = page.evaluate('''() => {
        return Array.from(document.querySelectorAll('.sidebar-header button, .sidebar button, .btn-icon')).map(b => ({
            cls: b.className,
            title: b.title || b.innerText,
            html: b.innerHTML,
            rect: b.getBoundingClientRect()
        }));
    }''')
    print("Buttons in bot profile:", dots)
    browser.close()
