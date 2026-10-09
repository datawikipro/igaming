#!/usr/bin/env python3
import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

PROFILE_DIR = Path("smm-agent/storage/boosty_profile").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()

def inspect_modal():
    with sync_playwright() as p:
        browser = p.firefox.launch_persistent_context(
            user_data_dir=str(PROFILE_DIR),
            headless=True,
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()
        page.goto("https://boosty.to", timeout=40000, wait_until="commit")
        time.sleep(5)
        
        cookie = page.locator("button:has-text('I AGREE')").first
        if cookie.is_visible():
            cookie.click(force=True)
            time.sleep(1)
            
        # Switch language to RU
        lang_btn = page.locator("button:has-text('EN'), div:has-text('EN')").first
        if lang_btn.is_visible():
            lang_btn.click(force=True)
            time.sleep(1)
            ru_opt = page.locator("text='RU', text='Русский'").first
            if ru_opt.is_visible():
                ru_opt.click(force=True)
                time.sleep(2)
                
        signup = page.locator("button:has-text('SIGN UP'), a:has-text('SIGN UP'), button:has-text('РЕГИСТРАЦИЯ'), a:has-text('РЕГИСТРАЦИЯ'), button:has-text('ВОЙТИ')").first
        signup.click(force=True)
        time.sleep(3)
        page.screenshot(path=str(SCREEN_DIR / "boosty_modal_ru.png"))
        
        # Dump all buttons and links in the modal
        print("Buttons/links in modal:")
        for el in page.locator("div[role='dialog'] a, div[role='dialog'] button, .modal a, .modal button").all():
            try:
                href = el.get_attribute("href") or ""
                aria = el.get_attribute("aria-label") or ""
                cls = el.get_attribute("class") or ""
                txt = el.inner_text().strip()
                print(f"  El: [{cls}] href='{href}' aria='{aria}' text='{txt}'")
            except Exception:
                pass
                
        browser.close()

if __name__ == "__main__":
    inspect_modal()
