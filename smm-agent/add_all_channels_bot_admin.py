import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()
BOT_USERNAME = "smartbet_guru_bot"

CHANNELS = [
    "SmartBet.guru | English",
    "SmartBet.guru | France",
    "SmartBet.guru | España",
]

def add_bot_to_channel(page, channel_name):
    print(f"\n==========================================")
    print(f"Adding @{BOT_USERNAME} as admin to {channel_name}...")
    print(f"==========================================")
    
    # Go to bot chat
    page.goto(f"https://web.telegram.org/k/#?tgaddr=tg%3A%2F%2Fresolve%3Fdomain%3D{BOT_USERNAME}", timeout=45000, wait_until="domcontentloaded")
    page.wait_for_timeout(3000)
    
    # 1. Click top header (500, 45) to open Bot Info
    page.mouse.click(500, 45)
    page.wait_for_timeout(2000)
    
    # 2. Click 'Add to Group or Channel'
    add_row = page.locator("div.row-clickable:has-text('Add to Group or Channel')").first
    if not add_row.is_visible():
        print(f"Could not find Add to Group or Channel row!")
        return False
    add_row.click()
    page.wait_for_timeout(2000)
    
    # 3. Click target channel in modal
    target = page.locator(f".popup-container .chatlist-chat:has-text('{channel_name}'), .modal .chatlist-chat:has-text('{channel_name}')").first
    if not target.is_visible():
        print(f"Channel {channel_name} not found in Add modal!")
        return False
    target.click()
    page.wait_for_timeout(2000)
    
    # 4. Click checkmark in header (top right of sidebar)
    check_btn = page.locator(".sidebar-header button.appear-zoom, .sidebar-header button:has-text(''), button.primary").first
    if check_btn.is_visible():
        check_btn.click()
    else:
        page.mouse.click(1228, 44)
    page.wait_for_timeout(2000)
    
    # 5. Click 'ADD' in the confirmation popup
    add_btn = page.locator("button:has-text('ADD'), button:has-text('Add'), button:has-text('ДОБАВИТЬ')").last
    if add_btn.is_visible():
        print(f"Clicking ADD confirmation button for {channel_name}...")
        add_btn.click()
    else:
        print(f"ADD confirmation button not visible, checking dialog...")
        
    page.wait_for_timeout(3000)
    safe_name = channel_name.replace(" ", "_").replace("|", "_")
    page.screenshot(path=str(SCREEN_DIR / f"tg_added_{safe_name}.png"))
    print(f"Successfully processed {channel_name}!")
    return True

def verify_channel_admins(page, channel_name):
    print(f"\nVerifying admins for {channel_name}...")
    page.goto("https://web.telegram.org/k/", timeout=45000, wait_until="domcontentloaded")
    page.wait_for_timeout(3000)
    
    # Select channel
    for a in page.locator(".chatlist a").all():
        if channel_name in a.inner_text():
            a.click()
            break
    page.wait_for_timeout(2000)
    
    # Click header
    page.mouse.click(500, 45)
    page.wait_for_timeout(2000)
    
    # Click pencil
    page.mouse.click(1228, 43)
    page.wait_for_timeout(2000)
    
    # Scroll down
    page.mouse.move(1000, 500)
    page.mouse.wheel(0, 400)
    page.wait_for_timeout(2000)
    
    # Click Administrators
    page.mouse.click(1000, 545)
    page.wait_for_timeout(2000)
    
    safe_name = channel_name.replace(" ", "_").replace("|", "_")
    page.screenshot(path=str(SCREEN_DIR / f"tg_verified_admins_{safe_name}.png"))
    
    # Read admin names
    admins = page.evaluate('''() => {
        return Array.from(document.querySelectorAll('.selector-user, .chatlist-chat, .peer-title')).map(el => el.innerText.trim()).filter(Boolean);
    }''')
    print(f"Admins in {channel_name}: {admins}")

def main():
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(STORAGE_DIR),
            headless=True,
            args=["--no-sandbox", "--disable-blink-features=AutomationControlled"],
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()
        
        for ch in CHANNELS:
            add_bot_to_channel(page, ch)
            time.sleep(2)
            
        print("\nAll channels processed! Now verifying...")
        for ch in CHANNELS:
            try:
                verify_channel_admins(page, ch)
            except Exception as e:
                print(f"Error verifying {ch}: {e}")
                
        browser.close()

if __name__ == "__main__":
    main()
