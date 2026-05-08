from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    
    def handle_request(request):
        if "baltbet.ru" in request.url:
            print(f"Request: {request.url}")
            print(f"Headers: {request.headers}")

    page.on("request", handle_request)
    
    page.goto("https://baltbet.ru/")
    page.wait_for_timeout(5000)
    browser.close()

with sync_playwright() as playwright:
    run(playwright)
