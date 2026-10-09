import json
import time
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    b = p.firefox.launch(headless=True, proxy={'server': 'http://100.83.113.50:3128'})
    page = b.new_page()
    page.goto('https://x.com/i/flow/login', timeout=40000)
    time.sleep(4)
    data = page.evaluate("""() => {
        const inputs = Array.from(document.querySelectorAll('input')).map(i => ({
            tag: i.tagName, type: i.type, name: i.name, id: i.id, placeholder: i.placeholder,
            className: i.className, autocomplete: i.autocomplete, dataTestId: i.getAttribute('data-testid')
        }));
        const buttons = Array.from(document.querySelectorAll('button, [role="button"]')).map(b => ({
            tag: b.tagName, text: b.innerText, role: b.getAttribute('role'), dataTestId: b.getAttribute('data-testid')
        }));
        return { inputs, buttons };
    }""")
    print("INPUTS:", json.dumps(data["inputs"], indent=2))
    print("BUTTONS:", json.dumps(data["buttons"], indent=2))
    b.close()
