from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    try:
        print("Navigating to 1xbet.rs...")
        page.goto("https://1xbet.rs/sr/line/football/", timeout=60000)
        page.wait_for_timeout(5000)
        
        # Test fetching without country
        url = '/service-api/LineFeed/Get1x2_VZip?sports=1&lng=en&partner=321'
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
        print(f"Status: {result.get('status')}")
        print(f"Has Value: {has_value}")
        print(f"Body length: {len(body)}")
        print(f"Body preview: {body[:300]}")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
