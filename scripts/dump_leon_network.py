import subprocess
import sys

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

script = """
import asyncio
from playwright.async_api import async_playwright

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(
            headless=True,
            args=['--no-sandbox', '--disable-gpu']
        )
        context = await browser.new_context()
        page = await context.new_page()
        
        async def on_response(response):
            url = response.url
            if any(k in url for k in ['api', 'sports', 'events', 'betline', 'feed', 'graphql']):
                print(f"[RESP {response.status}] {url}")
                try:
                    text = await response.text()
                    print(f"       Body: {text[:200]}")
                except Exception:
                    pass

        page.on('response', on_response)
        
        print("Navigating to https://leon.ru...")
        try:
            await page.goto("https://leon.ru", timeout=30000)
            await asyncio.sleep(10)
        except Exception as e:
            print("Goto error:", e)
            
        await browser.close()

asyncio.run(main())
"""

res = subprocess.run([
    'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', XEON_HOST,
    f"PLAYWRIGHT_BROWSERS_PATH=/data/playwright-browsers python3 -c {subprocess.list2cmdline([script])}"
], capture_output=True, text=True, encoding='utf-8', errors='replace')

print("STDOUT:", res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
