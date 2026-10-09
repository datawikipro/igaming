import time
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    b = p.firefox.launch(headless=True, proxy={'server': 'http://100.83.113.50:3128'})
    page = b.new_page()
    page.goto('https://x.com/i/flow/login', timeout=40000)
    time.sleep(4)
    
    # Check form HTML
    form_html = page.evaluate("""() => {
        const form = document.querySelector('form');
        return form ? form.outerHTML : document.body.innerHTML;
    }""")
    print("Form HTML length:", len(form_html))
    with open("smm-agent/storage/tw_form.html", "w", encoding="utf-8") as f:
        f.write(form_html)
    print("Saved form HTML to smm-agent/storage/tw_form.html")
    b.close()
