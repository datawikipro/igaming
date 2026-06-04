from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    try:
        page.goto("https://megapari.com/", timeout=60000)
        page.wait_for_timeout(3000)
        
        tests = [
            '?sports=1&lng=en',
            '?sports=1&lng=en&partner=192',
            '?sports=1&lng=en&partner=1',
            '?sports=1&lng=en&ref=192',
            '?sports=1&lng=en&country=205',
            '?sports=1&lng=en&partner=192&country=168',
            '?sports=1&lng=en&partner=192&country=205&mode=4',
            '?sports=1&lng=en&partner=192&virtualSports=true'
        ]
        
        for t in tests:
            url = f'/service-api/LineFeed/Get1x2_VZip{t}'
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
            print(f"Query: {t} -> Status: {result.get('status')}, Body: {result.get('body', '')[:100]}")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
