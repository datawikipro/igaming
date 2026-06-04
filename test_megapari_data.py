from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    try:
        page.goto("https://megapari.com/", timeout=60000)
        page.wait_for_timeout(3000)
        
        tests = [
            '?lng=en&partner=192',
            '?lng=en&partner=192&sports=1',
            '?lng=en&partner=192&sports=2',
            '?lng=en&partner=192&virtualSports=true',
            '?lng=en&partner=192&gr=824'
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
            body = result.get('body', '')
            has_value = '"Value":[' in body and not '"Value":[]' in body
            print(f"Query: {t} -> Status: {result.get('status')}, Has Value: {has_value}, Length: {len(body)}, Preview: {body[:150]}")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
