from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    try:
        page.goto("https://megapari.com/", timeout=60000)
        page.wait_for_timeout(3000)
        
        # Test 1: exact query parameters captured from real traffic
        q1 = '?sports=1&count=40&lng=en&mode=4&country=207&partner=192&getEmpty=true&virtualSports=true&countryFirst=true'
        url1 = f'/service-api/LineFeed/Get1x2_VZip{q1}'
        
        # Test 2: count=1000
        q2 = '?sports=1&count=1000&lng=en&mode=4&country=207&partner=192&getEmpty=true&virtualSports=true&countryFirst=true'
        url2 = f'/service-api/LineFeed/Get1x2_VZip{q2}'
        
        # Test 3: without countryFirst and getEmpty
        q3 = '?sports=1&count=1000&lng=en&mode=4&country=207&partner=192&virtualSports=true'
        url3 = f'/service-api/LineFeed/Get1x2_VZip{q3}'
        
        for url in [url1, url2, url3]:
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
            body = result.get('body', '')
            has_value = '"Value":[' in body and not '"Value":[]' in body
            print(f"URL: {url[:100]}... -> Status: {result.get('status')}, Has Value: {has_value}, Length: {len(body)}, Preview: {body[:250]}")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
