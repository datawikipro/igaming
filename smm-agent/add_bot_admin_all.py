import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()

def add_bot_as_admin(peer_id, channel_name):
    print(f"\n==========================================")
    print(f"[tg] Adding @smartbet_guru_bot as Admin to {channel_name} ({peer_id})...")
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

        # 3. Find and click Administrators row
        coords = page.evaluate("""() => {
            const rows = Array.from(document.querySelectorAll('.row-clickable, .row, .item'));
            for (const r of rows) {
                if (r.innerText && r.innerText.includes('Administrators')) {
                    const rect = r.getBoundingClientRect();
                    return { x: Math.round(rect.x + rect.width / 2), y: Math.round(rect.y + rect.height / 2) };
                }
            }
            return null;
        }""")
        print("Administrators coords:", coords)
        if not coords:
            print("Failed to find Administrators row")
            page.screenshot(path=f"storage/tg_admin_fail_{channel_name}.png")
            browser.close()
            return False

        page.mouse.click(coords['x'], coords['y'])
        page.wait_for_timeout(2000)

        # 4. Click 'Add Administrator' button
        print("Looking for Add Administrator button...")
        add_clicked = page.evaluate("""() => {
            const btns = Array.from(document.querySelectorAll('.sidebar-right button, .sidebar-right .row, .sidebar-right .item'));
            const addBtn = btns.find(b => b.innerText && (b.innerText.includes('Add Administrator') || b.innerText.includes('Add Admin')));
            if (addBtn) {
                addBtn.click();
                return { clicked: true, text: addBtn.innerText };
            }
            return { clicked: false, count: btns.length };
        }""")
        print("Add admin button:", add_clicked)
        page.wait_for_timeout(2000)

        # 5. Search for bot
        print("Searching for @smartbet_guru_bot...")
        page.keyboard.type("@smartbet_guru_bot")
        page.wait_for_timeout(2500)

        # 6. Select the bot in search results
        print("Clicking bot in search results...")
        bot_selected = page.evaluate("""() => {
            const items = Array.from(document.querySelectorAll('.sidebar-right .peer-title, .sidebar-right .user-title, .sidebar-right .item, .sidebar-right .chatlist-chat'));
            const botItem = items.find(i => i.innerText && (i.innerText.includes('smartbet_guru_bot') || i.innerText.includes('SmartBet')));
            if (botItem) {
                botItem.click();
                return { found: true, text: botItem.innerText };
            }
            return { found: false, count: items.length };
        }""")
        print("Bot selected:", bot_selected)
        page.wait_for_timeout(2000)

        # 7. Click save checkmark button (top right or floating bottom right)
        print("Saving admin permissions...")
        page.evaluate("""() => {
            const btns = Array.from(document.querySelectorAll('.sidebar-right button.btn-circle, .sidebar-right .sidebar-header button.btn-icon'));
            const check = btns.find(b => b.innerText && b.innerText.includes('\ue9b8') || b.classList.contains('btn-circle'));
            if (check) check.click();
        }""")
        page.mouse.click(950, 855)
        page.wait_for_timeout(3000)

        page.screenshot(path=f"storage/tg_admin_added_{channel_name}.png")
        print(f"[+] Saved admin screenshot to storage/tg_admin_added_{channel_name}.png")
        browser.close()
        return True

if __name__ == "__main__":
    for peer, name in [
        ("-3960368887", "English"),
        ("-4371643544", "France"),
        ("-4346736376", "Espana")
    ]:
        add_bot_as_admin(peer, name)
