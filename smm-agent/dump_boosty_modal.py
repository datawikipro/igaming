import sys
from pathlib import Path
from playwright.sync_api import sync_playwright

PROFILE_DIR = Path("smm-agent/storage/boosty_profile").resolve()

with sync_playwright() as p:
    browser = p.firefox.launch_persistent_context(
        user_data_dir=str(PROFILE_DIR),
        headless=True,
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.goto("https://boosty.to", timeout=40000, wait_until="commit")
    page.wait_for_timeout(4000)
    
    signup = page.locator("button:has-text('SIGN UP'), a:has-text('SIGN UP')").first
    if signup.is_visible():
        signup.click(force=True)
        page.wait_for_timeout(3000)
        
    html = page.evaluate('''() => {
        const dialog = document.querySelector('[role="dialog"]') || document.querySelector('.modal') || document.body;
        return dialog.innerHTML;
    }''')
    with open("smm-agent/storage/boosty_modal.html", "w", encoding="utf-8") as f:
        f.write(html)
    print("Saved modal HTML, length:", len(html))
    browser.close()
