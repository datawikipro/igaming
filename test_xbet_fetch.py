from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(
        headless=True,
        proxy={"server": "http://127.0.0.1:3128"}
    )
    page = browser.new_page()
    
    try:
        print("Navigating to 1xbet.rs...")
        page.goto("https://1xbet.rs/sr/line/football/", timeout=60000)
        page.wait_for_timeout(5000)
        
        url = '/service-api/LineFeed/Get1x2_VZip?sports=1&count=40&lng=en&mode=4&country=168&partner=321&getEmpty=true&virtualSports=true'
        print(f"--- Fetching {url} ---")
        result = page.evaluate(f"""async () => {{
            try {{
                const response = await fetch('{url}', {{
                    credentials: 'include'
                }});
                return {{
                    status: response.status,
                    contentType: response.headers.get('content-type'),
                    body: await response.text()
                }};
            }} catch (e) {{
                return {{ error: e.toString() }};
            }}
        }}""")
        print(f"Status: {result.get('status')}")
        print(f"Content Type: {result.get('contentType')}")
        body = result.get('body', '')
        print(f"Body length: {len(body)}")
        print(f"Body preview: {body[:300]}")
        
    except Exception as e:
        print(f"Error: {e}")
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
