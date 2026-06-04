from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    
    def handle_request(request):
        url = request.url.lower()
        if "api" in url or "sport" in url or "menu" in url:
            if not any(x in url for x in [".js", ".css", ".png", ".jpg", ".woff", ".svg", ".gif", "analytics"]):
                print(f"Request: {request.method} {request.url}")

    page.on("request", handle_request)
    
    try:
        print("Navigating to olimpbet.kz...")
        page.goto("https://olimpbet.kz/", timeout=60000)
        page.wait_for_timeout(15000)
        print("Done waiting.")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
