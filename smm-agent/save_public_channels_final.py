import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()

def save_public_channel(peer_id, username):
    print(f"\n==========================================")
    print(f"[tg] Setting public username @{username} for channel {peer_id}...")
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(STORAGE_DIR),
            headless=True,
            args=["--disable-blink-features=AutomationControlled", "--no-sandbox"],
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()
        page.goto(f"https://web.telegram.org/k/#{peer_id}")
        page.wait_for_timeout(3500)

        # 1. Click header
        page.locator(".chat-info").first.click()
        page.wait_for_timeout(2000)

        # 2. Click pencil
        page.mouse.click(1228, 44)
        page.wait_for_timeout(3500)

        # 3. Find Channel Type row
        coords = page.evaluate("""() => {
            const rows = Array.from(document.querySelectorAll('.row-clickable, .row, .item'));
            for (const r of rows) {
                if (r.innerText && r.innerText.includes('Channel Type')) {
                    const rect = r.getBoundingClientRect();
                    return { x: Math.round(rect.x + rect.width / 2), y: Math.round(rect.y + rect.height / 2) };
                }
            }
            return null;
        }""")
        if not coords:
            print("Failed to find Channel Type row")
            browser.close()
            return False

        page.mouse.click(coords['x'], coords['y'])
        page.wait_for_timeout(2000)

        # 4. Click 'Public Channel'
        page.evaluate("""() => {
            const labels = Array.from(document.querySelectorAll('label, .radio, .item'));
            const publicOpt = labels.find(l => l.innerText && l.innerText.includes('Public Channel'));
            if (publicOpt) {
                publicOpt.click();
            }
        }""")
        page.wait_for_timeout(1500)

        # 5. Clear input and set username
        page.evaluate("""(uname) => {
            const inputs = Array.from(document.querySelectorAll('.sidebar-right input[type="text"], .sidebar-slider-item:last-child input[type="text"]'));
            const target = inputs.find(inp => inp.getBoundingClientRect().x > 500);
            if (target) {
                target.focus();
                target.select();
            }
        }""", username)
        page.keyboard.press("Control+A")
        page.keyboard.press("Backspace")
        page.wait_for_timeout(500)
        page.keyboard.type(username)
        page.wait_for_timeout(1500)

        # 6. Click checkmark button via locator or exact center (950, 855)
        print("Clicking save checkmark...")
        btn = page.locator(".sidebar-slider-item:last-child .btn-corner, .sidebar-slider-item.active .btn-corner, button.btn-circle")
        if btn.count() > 0:
            btn.first.click()
            print("Clicked via locator .btn-corner!")
        else:
            page.mouse.click(950, 855)
            print("Clicked at (950, 855)!")

        page.wait_for_timeout(3000)

        # In Edit screen, also click the top checkmark / save if present, or close edit
        save_edit_btn = page.locator(".edit-peer-container .sidebar-header button.btn-icon").filter(has_text="")
        # Take screenshot of the result
        page.screenshot(path=f"storage/tg_saved_{username}.png")
        print(f"[+] Saved screenshot to storage/tg_saved_{username}.png")

        browser.close()
        return True

if __name__ == "__main__":
    for peer, user in [
        ("-3960368887", "SmartBetGuruEN"),
        ("-4371643544", "SmartBetGuruFR"),
        ("-4346736376", "SmartBetGuruES")
    ]:
        save_public_channel(peer, user)
