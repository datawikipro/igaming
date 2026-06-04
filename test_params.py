from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    try:
        page.goto("https://megapari.com/", timeout=60000)
        page.wait_for_timeout(3000)
        
        urls = [
            '/service-api/LineFeed/Get1x2_VZip?sports=1&lng=en',
            '/service-api/LineFeed/Get1x2_VZip?sports=1&lng=en&partner=192&country=168',
            '/service-api/LineFeed/Get1x2_VZip?sports=1&lng=en&partner=192&country=205',
            '/LineFeed/Get1x2_VZip?sports=1&lng=en&partner=192',
            '/service-api/LineFeed/Get1xMatchByLeague?sports=1&lng=en&partner=192'
        ]
        
        for url in urls:
            result = page.evaluate(f"""async () => {{
                try {{
                    const response = await fetch('{url}', {{ credentials: 'include' }});
                    return {{
                        status: response.status,
                        body: await response.text()
                    }};
                }} catch (e) {{
                    return {{ error: e.toString() }};
                }}
            }}""")
            print(f"URL: {url} -> Status: {result.get('status')}, Error: {result.get('error')}, Length: {len(result.get('body', ''))}, Preview: {result.get('body', '')[:150]}")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
