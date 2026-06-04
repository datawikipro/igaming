from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(
        headless=True,
        proxy={"server": "http://127.0.0.1:3128"}
    )
    page = browser.new_page()
    
    def handle_request(request):
        url = request.url.lower()
        if "1xbet" in url or "pb06e2" in url or "traincdn" in url or "feed" in url or "match" in url:
            if not any(x in url for x in [".js", ".css", ".png", ".jpg", ".woff", ".svg", ".gif", "banner", "analytics", "clickstream"]):
                print(f"Request: {request.method} {request.url}")

    page.on("request", handle_request)
    
    try:
        print("Navigating to football section...")
        page.goto("https://1xbet.rs/sr/line/football/", timeout=60000)
        page.wait_for_timeout(20000)
        print("Done waiting.")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
