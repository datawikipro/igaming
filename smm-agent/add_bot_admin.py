#!/usr/bin/env python3
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()
BOT_USERNAME = "smartbet_guru_bot"

CHANNELS = [
    "SmartBet.guru | English",
    "SmartBet.guru | France",
    "SmartBet.guru | España",
]

def add_admin_to_channel(page, channel_name):
    print(f"\n==========================================")
    print(f"Adding @{BOT_USERNAME} to {channel_name}...")
    print(f"==========================================")
    
    # 1. Click channel in chatlist
    print(f"1. Selecting {channel_name}...")
    found = False
    for a in page.locator(".chatlist a").all():
        if channel_name in a.inner_text():
            a.click()
            found = True
            break
    if not found:
        print(f"Channel {channel_name} not found in chatlist!")
        return False
    time.sleep(2)
    
    # Clear any draft text in input
    inp = page.locator(".input-message-input").first
    if inp.is_visible():
        inp.click()
        page.keyboard.press("Control+A")
        page.keyboard.press("Backspace")
        time.sleep(0.5)
        
    # 2. Click header (x=480, y=45) to open Channel Info
    print("2. Opening Channel Info...")
    page.mouse.click(480, 45)
    time.sleep(2)
    
    # 3. Click pencil icon at (1228, 44) to open Edit
    print("3. Opening Edit...")
    page.mouse.click(1228, 44)
    time.sleep(2)
    
    # 4. Scroll down in edit panel
    print("4. Scrolling to Administrators...")
    page.mouse.move(1000, 700)
    page.mouse.wheel(0, 400)
    time.sleep(2)
    
    # 5. Click Administrators (x=1000, y=548)
    print("5. Clicking Administrators...")
    page.mouse.click(1000, 548)
    time.sleep(3)
    
    # 6. Click the blue Add Administrator floating button
    print("6. Locating and clicking Add Admin (+) button...")
    add_btn_rect = page.evaluate('''() => {
        const b = Array.from(document.querySelectorAll('button')).find(btn => 
            btn.className.includes('btn-circle') || btn.className.includes('floating') || btn.innerHTML.includes('user_add') || btn.innerHTML.includes('add')
        );
        if (b) {
            const r = b.getBoundingClientRect();
            return { x: r.x + r.width/2, y: r.y + r.height/2 };
        }
        return null;
    }''')
    
    if add_btn_rect:
        print(f"Found Add Admin button at ({add_btn_rect['x']}, {add_btn_rect['y']})")
        page.mouse.click(add_btn_rect['x'], add_btn_rect['y'])
    else:
        print("Clicking fallback coordinates (950, 830)...")
        page.mouse.click(950, 830)
    time.sleep(3)
    page.screenshot(path=str(SCREEN_DIR / f"tg_add_admin_{channel_name[:15]}_1.png"))
    
    # 7. Type bot username into search
    print(f"7. Searching for @{BOT_USERNAME}...")
    search_inp = page.locator(".input-search-input, input[placeholder*='Search']").last
    if search_inp.is_visible():
        search_inp.fill(f"@{BOT_USERNAME}")
        time.sleep(3)
    else:
        page.keyboard.type(f"@{BOT_USERNAME}")
        time.sleep(3)
        
    page.screenshot(path=str(SCREEN_DIR / f"tg_add_admin_{channel_name[:15]}_2_search.png"))
    
    # 8. Select the bot from search results
    print("8. Selecting bot from search results...")
    bot_row = page.locator(f".selector-user:has-text('{BOT_USERNAME}'), .search-group .chatlist-chat:has-text('{BOT_USERNAME}'), div:has-text('SmartBet')").last
    if bot_row.is_visible():
        bot_row.click()
        time.sleep(2)
    else:
        # Fallback click first search result
        first_res = page.locator(".search-group .chatlist-chat, .selector-user, .chatlist-chat").first
        if first_res.is_visible():
            first_res.click()
            time.sleep(2)
            
    page.screenshot(path=str(SCREEN_DIR / f"tg_add_admin_{channel_name[:15]}_3_rights.png"))
    
    # 9. In the permissions dialog, click Confirm / Save / Checkmark
    print("9. Confirming admin rights (clicking Checkmark / Save)...")
    save_btn = page.locator("button:has-text('Save'), button:has-text('Add'), .sidebar-header button.btn-icon.rp").last
    if save_btn.is_visible():
        save_btn.click()
        time.sleep(2)
    else:
        # Click checkmark in header (top right x=1228, y=44)
        page.mouse.click(1228, 44)
        time.sleep(2)
        
    page.screenshot(path=str(SCREEN_DIR / f"tg_add_admin_{channel_name[:15]}_4_done.png"))
    print(f"✅ Finished adding admin for {channel_name}!")
    
    # Close sidebar panels to return to clean chat view
    for _ in range(3):
        close_btn = page.locator(".sidebar-header button.sidebar-close-button, .sidebar-header .sidebar-back-button").first
        if close_btn.is_visible():
            close_btn.click()
            time.sleep(1)

def main():
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(STORAGE_DIR),
            headless=True,
            args=["--no-sandbox", "--disable-blink-features=AutomationControlled"],
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()
        page.goto("https://web.telegram.org/k/", timeout=45000, wait_until="domcontentloaded")
        time.sleep(5)
        
        for ch in CHANNELS:
            try:
                add_admin_to_channel(page, ch)
            except Exception as e:
                print(f"Error on {ch}: {e}")
                page.screenshot(path=str(SCREEN_DIR / f"tg_err_{ch[:10]}.png"))
                
        browser.close()
        print("\nAll channels processed!")

if __name__ == "__main__":
    main()
