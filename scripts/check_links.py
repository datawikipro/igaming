from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page()
    page.goto('https://smartbet.guru/ru', wait_until='networkidle')
    links = page.locator('a:has-text("БК"), a[target="_blank"]').all()
    print(f'Total external bookmaker links found: {len(links)}')
    for i, l in enumerate(links[:8]):
        href = l.get_attribute('href')
        txt = l.inner_text().strip()
        print(f'Link {i+1}: text="{txt}" href="{href}"')
    browser.close()
