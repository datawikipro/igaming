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

    # 1. Click header
    page.locator(".chat-info").first.click()
    page.wait_for_timeout(2000)

    # 2. Click pencil
    page.mouse.click(1228, 44)
    page.wait_for_timeout(2000)

    # Inspect all clickable rows inside .edit-peer-container .sidebar-content
    rows = page.evaluate("""() => {
        const editContainer = document.querySelector('.edit-peer-container .sidebar-content');
        if (!editContainer) return [];
        
        // Find elements that look like menu rows
        const candidateRows = [];
        const walker = document.createTreeWalker(editContainer, NodeFilter.SHOW_ELEMENT);
        while (walker.nextNode()) {
            const el = walker.currentNode;
            if (el.innerText && (el.innerText.includes('Channel Type') || el.innerText.includes('Administrators') || el.innerText.includes('Discussion'))) {
                const r = el.getBoundingClientRect();
                candidateRows.push({
                    tag: el.tagName,
                    className: el.className,
                    text: el.innerText.trim().replace(/\\n/g, ' | '),
                    x: Math.round(r.x + r.width/2),
                    y: Math.round(r.y + r.height/2),
                    w: Math.round(r.width),
                    h: Math.round(r.height),
                    hasOnClick: typeof el.onclick === 'function' || el.getAttribute('onclick') !== null
                });
            }
        }
        return candidateRows;
    }""")

    with open("storage/tg_menu_rows.json", "w", encoding="utf-8") as f:
        import json
        json.dump(rows, f, indent=2, ensure_ascii=False)

    print(f"Dumped {len(rows)} menu candidates")
    browser.close()
