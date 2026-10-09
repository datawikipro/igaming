import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()

with sync_playwright() as p:
    browser = p.chromium.launch_persistent_context(
        user_data_dir=str(STORAGE_DIR),
        headless=True,
        args=["--disable-blink-features=AutomationControlled", "--no-sandbox"],
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.goto("https://web.telegram.org/k/#-3960368887")
    page.wait_for_timeout(3000)

    # Click header
    page.locator(".chat-info").first.click()
    page.wait_for_timeout(2000)

    # Inspect all buttons in .profile-container .sidebar-header
    btn_info = page.evaluate("""() => {
        const header = document.querySelector('.profile-container .sidebar-header');
        if (!header) return {error: 'no header'};
        const btns = Array.from(header.querySelectorAll('button')).map((b, i) => {
            const r = b.getBoundingClientRect();
            return {
                i, className: b.className,
                html: b.outerHTML,
                x: Math.round(r.x + r.width/2),
                y: Math.round(r.y + r.height/2),
                w: Math.round(r.width),
                h: Math.round(r.height),
                visible: r.width > 0 && r.height > 0
            };
        });
        return btns;
    }""")
    print("Buttons in header:", btn_info)

    # Click the visible action button in the header
    for b in btn_info:
        if b.get('visible') and b.get('x') > 1000:
            print(f"Clicking button {b['i']} at ({b['x']}, {b['y']})...")
            page.mouse.click(b['x'], b['y'])
            page.wait_for_timeout(2000)
            break

    page.screenshot(path="storage/tg_after_pencil_click.png")
    print("Screenshot saved to storage/tg_after_pencil_click.png")
    browser.close()
