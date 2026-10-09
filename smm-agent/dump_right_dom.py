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

    # Click pencil
    page.mouse.click(1228, 44)
    page.wait_for_timeout(2000)

    # Dump DOM hierarchy of right sidebar
    dom_summary = page.evaluate("""() => {
        const rightCol = document.querySelector('#column-right');
        if (!rightCol) return 'no column-right';
        
        function inspectNode(node, depth=0) {
            if (depth > 6) return '';
            let s = '  '.repeat(depth) + node.tagName + (node.className ? '.' + node.className.split(' ').join('.') : '');
            if (node.innerText && node.children.length === 0) {
                s += ' ["' + node.innerText.trim().replace(/\\n/g, ' ') + '"]';
            }
            let children = Array.from(node.children).map(c => inspectNode(c, depth + 1)).filter(Boolean).join('\\n');
            return s + (children ? '\\n' + children : '');
        }
        return inspectNode(rightCol);
    }""")
    
    with open("storage/right_dom.txt", "w", encoding="utf-8") as f:
        f.write(dom_summary)
    print("DOM summary saved to storage/right_dom.txt (length:", len(dom_summary), ")")

    browser.close()
