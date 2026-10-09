from playwright.sync_api import sync_playwright
from pathlib import Path

PROFILE_DIR = Path("smm-agent/storage/boosty_profile").resolve()

with sync_playwright() as p:
    browser = p.firefox.launch_persistent_context(
        user_data_dir=str(PROFILE_DIR),
        headless=True,
        viewport={"width": 1280, "height": 900}
    )
    page = browser.pages[0] if browser.pages else browser.new_page()
    page.on("console", lambda m: print(f"[CONSOLE {m.type}] {m.text}"))
    
    # Intercept window.open
    page.add_init_script('''
        window.__openedUrls = [];
        const origOpen = window.open;
        window.open = function(url, target, features) {
            console.log("INTERCEPTED window.open URL:", url);
            window.__openedUrls.push(url);
            return origOpen ? origOpen.apply(this, arguments) : null;
        };
    ''')
    
    page.goto("https://boosty.to", timeout=40000, wait_until="commit")
    page.wait_for_timeout(4000)
    
    page.locator("button:has-text('SIGN UP'), a:has-text('SIGN UP')").first.click(force=True)
    page.wait_for_timeout(2000)
    
    print("Clicking Google button...")
    page.locator('button[data-provider="google"]').click()
    page.wait_for_timeout(4000)
    
    opened = page.evaluate('window.__openedUrls')
    print("Window opened URLs:", opened)
    browser.close()
