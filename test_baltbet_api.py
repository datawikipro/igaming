from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    
    response = context.request.get(
        "https://events.baltbet.ru/api/prematch/events/hot?offset=all",
        headers={
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
            "Accept": "application/json, text/plain, */*",
            "Referer": "https://baltbet.ru/",
            "Origin": "https://baltbet.ru",
            "Sec-Fetch-Mode": "cors",
            "Sec-Fetch-Site": "same-site"
        }
    )
    
    print(f"Status: {response.status}")
    print(f"Headers: {response.headers}")
    print(f"Body: {response.text()}")
    
    browser.close()

with sync_playwright() as playwright:
    run(playwright)
