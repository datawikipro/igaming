from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    
    page.goto("https://baltbet.ru/")
    page.wait_for_timeout(3000)
    
    # execute fetch in the context of the page
    result = page.evaluate("""
        async () => {
            const res = await fetch("https://events.baltbet.ru/api/prematch/events/hot?offset=all", {
                headers: {
                    "Accept": "application/json, text/plain, */*"
                }
            });
            return {
                status: res.status,
                text: await res.text()
            };
        }
    """)
    
    print(f"Status: {result['status']}")
    print(f"Body snippet: {result['text'][:200]}")
    
    browser.close()

with sync_playwright() as playwright:
    run(playwright)
