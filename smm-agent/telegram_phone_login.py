#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Telegram Web Phone Number Login Listener
"""

import os
import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

PHONE_NUMBER = "+79137671550"

PROFILE_DIR = Path(r"C:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\storage\telegram_web_profile")
PROFILE_DIR.mkdir(parents=True, exist_ok=True)

STORAGE_DIR = Path(r"C:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\storage")
CODE_FILE = STORAGE_DIR / "tg_code.txt"
TWO_FA_FILE = STORAGE_DIR / "tg_2fa.txt"

for f in [CODE_FILE, TWO_FA_FILE]:
    if f.exists():
        f.unlink()

ARTIFACT_DIR = Path(r"C:\Users\chernousov_a\.gemini\antigravity\brain\d15af757-f652-4d03-85f1-43d9e1c99936")
PHONE_STEP_IMG = ARTIFACT_DIR / "tg_phone_step.png"
CODE_STEP_IMG = ARTIFACT_DIR / "tg_code_step.png"
LOGGED_IN_IMG = ARTIFACT_DIR / "tg_logged_in.png"

def main():
    print(f"Starting Telegram Web Phone Login for {PHONE_NUMBER}...", flush=True)
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(PROFILE_DIR),
            headless=True,
            args=["--disable-blink-features=AutomationControlled", "--no-sandbox"]
        )
        page = browser.new_page()
        page.set_viewport_size({"width": 1280, "height": 850})
        
        print("Opening https://web.telegram.org/k/ ...", flush=True)
        page.goto("https://web.telegram.org/k/", wait_until="domcontentloaded", timeout=45000)
        time.sleep(5)
        
        if page.query_selector("input.input-search-input"):
            print(">>> ALREADY LOGGED IN! <<<", flush=True)
            page.screenshot(path=str(LOGGED_IN_IMG))
            browser.close()
            return
            
        # Click "LOG IN BY PHONE NUMBER"
        print("Clicking 'LOG IN BY PHONE NUMBER'...", flush=True)
        btn = page.locator("text=/LOG IN BY PHONE NUMBER|Вход по номеру телефона/i").first
        btn.click(timeout=15000)
        time.sleep(2)
        page.screenshot(path=str(PHONE_STEP_IMG))
        
        # Find phone input
        # In Telegram Web K, there are two inputs or a contenteditable/input for phone number
        inputs = page.query_selector_all("input, div[contenteditable='true']")
        for idx, inp in enumerate(inputs):
            print(f"Input [{idx}]: tag={inp.evaluate('el => el.tagName')} class={inp.get_attribute('class')} text={inp.inner_text() if inp.evaluate('el => el.tagName')=='DIV' else inp.input_value()}", flush=True)
            
        # Focus the phone input (usually the second input or the one with phone number)
        phone_inp = None
        for inp in inputs:
            cls = inp.get_attribute("class") or ""
            # The phone input in Web K usually has +... or is the second input-field-input
            if "input-field-input" in cls:
                phone_inp = inp  # take the last one (first is country, second is phone)
                
        if phone_inp:
            print("Filling phone number...", flush=True)
            phone_inp.click()
            # Select all and replace with full phone number
            page.keyboard.press("Control+A")
            page.keyboard.press("Backspace")
            time.sleep(0.5)
            page.keyboard.type(PHONE_NUMBER, delay=50)
            time.sleep(1)
            page.screenshot(path=str(PHONE_STEP_IMG))
            
            # Click Next button or press Enter
            print("Submitting phone number...", flush=True)
            next_btn = page.query_selector("button.btn-primary:visible")
            if next_btn:
                next_btn.click()
            else:
                page.keyboard.press("Enter")
                
            time.sleep(5)
            page.screenshot(path=str(CODE_STEP_IMG))
            print(f">>> CODE REQUESTED! Screenshot saved to {CODE_STEP_IMG} <<<", flush=True)
        else:
            print("ERROR: Could not find phone input element!", flush=True)
            browser.close()
            return
            
        # Now wait for code from user via CODE_FILE
        print("Waiting for user to provide 5-digit Telegram code (via storage/tg_code.txt)...", flush=True)
        start = time.time()
        while time.time() - start < 600:
            time.sleep(1.5)
            
            if page.query_selector("input.input-search-input"):
                print(">>> SUCCESSFULLY LOGGED IN TO TELEGRAM! <<<", flush=True)
                time.sleep(3)
                page.screenshot(path=str(LOGGED_IN_IMG))
                browser.close()
                return
                
            if CODE_FILE.exists():
                code = CODE_FILE.read_text(encoding="utf-8").strip()
                CODE_FILE.unlink()
                if code:
                    print(f"Entering verification code: {code}", flush=True)
                    code_inp = page.query_selector("input[type='tel']:visible, input.input-field-input:visible")
                    if code_inp:
                        code_inp.click()
                    page.keyboard.type(code, delay=80)
                    time.sleep(4)
                    page.screenshot(path=str(CODE_STEP_IMG))
                    
                    # Check if 2FA password appeared
                    pwd_el = page.query_selector("input[type='password']:visible")
                    if pwd_el:
                        print(">>> 2FA PASSWORD REQUIRED! Waiting for storage/tg_2fa.txt <<<", flush=True)
                        
            if TWO_FA_FILE.exists():
                pwd = TWO_FA_FILE.read_text(encoding="utf-8").strip()
                TWO_FA_FILE.unlink()
                if pwd:
                    print("Entering 2FA password...", flush=True)
                    pwd_el = page.query_selector("input[type='password']:visible")
                    if pwd_el:
                        pwd_el.fill(pwd)
                        pwd_el.press("Enter")
                    else:
                        page.keyboard.type(pwd, delay=50)
                        page.keyboard.press("Enter")
                    time.sleep(5)
                    page.screenshot(path=str(LOGGED_IN_IMG))
                    
        print("Timeout waiting for code/login.", flush=True)
        browser.close()

if __name__ == "__main__":
    main()
