import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()

def save_single_channel(peer_id, username):
    print(f"\n==========================================")
    print(f"[tg] Finalizing public username @{username} for channel {peer_id}...")
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
        print("Waiting for edit container...")
        page.wait_for_timeout(4000)

        # 3. Click Channel Type row
        coords = page.evaluate("""() => {
            const rows = Array.from(document.querySelectorAll('.edit-peer-container .row-clickable, .row-clickable'));
            for (const r of rows) {
                if (r.innerText && r.innerText.includes('Channel Type')) {
                    const rect = r.getBoundingClientRect();
                    return { x: Math.round(rect.x + rect.width / 2), y: Math.round(rect.y + rect.height / 2) };
                }
            }
            return null;
        }""")
        print("Channel Type coords:", coords)
        if not coords:
            print("Failed to find Channel Type row")
            page.screenshot(path=f"storage/tg_fail_{username}.png")
            browser.close()
            return False

        page.mouse.click(coords['x'], coords['y'])
        page.wait_for_timeout(2000)

        # 4. Click 'Public Channel'
        page.evaluate("""() => {
            const labels = Array.from(document.querySelectorAll('.sidebar-right label, .sidebar-right .radio, .sidebar-right .item'));
            const publicOpt = labels.find(l => l.innerText && l.innerText.includes('Public Channel'));
            if (publicOpt) publicOpt.click();
        }""")
        page.wait_for_timeout(1500)

        # 5. Type username cleanly
        page.evaluate("""(uname) => {
            const inputs = Array.from(document.querySelectorAll('.sidebar-right input[type="text"]'));
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

        # 6. Click save checkmark button STRICTLY inside .sidebar-right
        print("Clicking save checkmark inside .sidebar-right...")
        save_btn = page.locator(".sidebar-right button.btn-circle, .sidebar-right .btn-corner")
        if save_btn.count() > 0:
            save_btn.last.click()
            print("Clicked save_btn.last!")
        else:
            page.mouse.click(950, 855)
            print("Clicked fallback (950, 855)!")

        page.wait_for_timeout(3000)

        # 7. Also in the Edit screen, click the checkmark button if needed
        page.evaluate("""() => {
            const checkBtn = document.querySelector('.edit-peer-container .sidebar-header button.btn-icon:last-child');
            if (checkBtn) checkBtn.click();
        }""")
        page.wait_for_timeout(2000)

        page.screenshot(path=f"storage/tg_final_confirmed_{username}.png")
        print(f"[+] Confirmed screenshot saved to storage/tg_final_confirmed_{username}.png")
        browser.close()
        return True

if __name__ == "__main__":
    save_single_channel("-4371643544", "SmartBetGuruFR")
