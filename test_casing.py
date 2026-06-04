from playwright.sync_api import sync_playwright
def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    try:
        page.goto('https://megapari.com/')
        page.wait_for_timeout(3000)
        urls = [
            '/service-api/LineFeed/Get1x2_VZip?sports=1&count=40&lng=en&mode=4&partner=192&getEmpty=true&virtualSports=true&countryFirst=true',
            '/service-api/LineFeed/Get1x2_VZip?sports=1&count=40&lng=en&mode=4&partner=192&getEmpty=true&virtualSports=true&countryFirst=true&country=207',
            '/service-api/LiveFeed/Get1x2_VZip?sports=1&count=40&lng=en&mode=4&partner=192&getEmpty=true&virtualSports=true&countryFirst=true',
            '/service-api/LiveFeed/Get1x2_VZip?sports=1&count=40&lng=en&mode=4&partner=192&getEmpty=true&virtualSports=true&countryFirst=true&country=207'
        ]
        for url in urls:
            result = page.evaluate(f"""async () => {{
                try {{
                    const response = await fetch("{url}");
                    return {{ status: response.status, body: await response.text() }};
                }} catch (e) {{
                    return {{ error: e.toString() }};
                }}
            }}""")
            print(f"URL: {url} -> Status: {result.get('status')}, Length: {len(result.get('body', ''))}, Preview: {result.get('body', '')[:200]}")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        browser.close()
with sync_playwright() as playwright:
    run(playwright)
