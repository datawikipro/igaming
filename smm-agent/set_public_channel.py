import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()

def set_public_username(peer_id, username):
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(STORAGE_DIR),
            headless=True,
            args=["--disable-blink-features=AutomationControlled", "--no-sandbox"],
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()
        page.goto(f"https://web.telegram.org/k/#{peer_id}")
        page.wait_for_timeout(3000)

        # 1. Click header
        page.locator(".chat-info").first.click()
        page.wait_for_timeout(2000)

        # 2. Click pencil
        page.mouse.click(1228, 44)
        page.wait_for_timeout(2000)

        # 3. Click Channel Type row
        coords = page.evaluate("""() => {
            const rows = Array.from(document.querySelectorAll('.edit-peer-container .row-clickable'));
            for (const r of rows) {
                if (r.innerText && r.innerText.includes('Channel Type')) {
                    const rect = r.getBoundingClientRect();
                    return { x: Math.round(rect.x + rect.width / 2), y: Math.round(rect.y + rect.height / 2) };
                }
            }
            return null;
        }""")
        if not coords:
            print(f"Could not find Channel Type row for {peer_id}")
            browser.close()
            return False

        page.mouse.click(coords['x'], coords['y'])
        page.wait_for_timeout(2000)

        # 4. Click 'Public Channel' radio
        print("Clicking 'Public Channel'...")
        page.evaluate("""() => {
            const labels = Array.from(document.querySelectorAll('label, .radio, .item'));
            const publicOpt = labels.find(l => l.innerText && l.innerText.includes('Public Channel'));
            if (publicOpt) {
                publicOpt.click();
            }
        }""")
        page.wait_for_timeout(1500)

        # 5. Find the username input and type username
        print(f"Typing username: {username}...")
        input_info = page.evaluate("""(uname) => {
            const inputs = Array.from(document.querySelectorAll('.sidebar-slider-item:last-child input[type="text"], .sidebar-right input[type="text"]'));
            // Filter out search input (x < 400)
            const target = inputs.find(inp => {
                const r = inp.getBoundingClientRect();
                return r.x > 500 && r.width > 0;
            });
            if (target) {
                target.focus();
                target.value = uname;
                target.dispatchEvent(new Event('input', { bubbles: true }));
                target.dispatchEvent(new Event('change', { bubbles: true }));
                return { found: true, placeholder: target.placeholder, val: target.value };
            }
            return { found: false, total: inputs.length };
        }""", username)
        print("Input info:", input_info)
        page.wait_for_timeout(1500)

        # Also type via keyboard to ensure Telegram's React/Vue state updates
        page.keyboard.press("End")
        page.keyboard.type(username)
        page.wait_for_timeout(1500)

        page.screenshot(path=f"storage/tg_set_user_{username}.png")
        print(f"Screenshot saved to storage/tg_set_user_{username}.png")

        # 6. Click the checkmark / save button (top right of sidebar)
        print("Clicking save checkmark...")
        save_clicked = page.evaluate("""() => {
            const btns = Array.from(document.querySelectorAll('.sidebar-slider-item:last-child .sidebar-header button, .sidebar-right .sidebar-header button'));
            // Save button is usually the checkmark icon at top right (x > 1150)
            const saveBtn = btns.find(b => {
                const r = b.getBoundingClientRect();
                return r.x > 1150 && r.y < 100 && r.width > 0;
            });
            if (saveBtn) {
                saveBtn.click();
                return { clicked: true, className: saveBtn.className };
            }
            return { clicked: false, count: btns.length };
        }""")
        print("Save clicked:", save_clicked)
        page.wait_for_timeout(2000)

        # Also click back/save if needed
        page.screenshot(path=f"storage/tg_after_save_{username}.png")
        print(f"Screenshot saved to storage/tg_after_save_{username}.png")
        browser.close()
        return True

if __name__ == "__main__":
    set_public_username("-3960368887", "SmartBetGuruEN")
