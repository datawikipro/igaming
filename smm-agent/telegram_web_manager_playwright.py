#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Telegram Web Autonomous Client & Channel Manager
"""

import os
import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

PROFILE_DIR = Path(r"C:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\storage\telegram_web_profile")
PROFILE_DIR.mkdir(parents=True, exist_ok=True)

TWO_FA_FILE = Path(r"C:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\storage\tg_2fa.txt")
if TWO_FA_FILE.exists():
    TWO_FA_FILE.unlink()

ARTIFACT_DIR = Path(r"C:\Users\chernousov_a\.gemini\antigravity\brain\d15af757-f652-4d03-85f1-43d9e1c99936")
QR_PATH = ARTIFACT_DIR / "telegram_qr_live.png"
QR_CARD_PATH = ARTIFACT_DIR / "telegram_qr_card_live.png"
LOGGED_IN_PATH = ARTIFACT_DIR / "telegram_logged_in.png"
TWO_FA_SCREEN_PATH = ARTIFACT_DIR / "telegram_2fa_screen.png"

def wait_for_login_and_listen(timeout_seconds=600):
    print("=" * 60, flush=True)
    print("Starting Telegram Web K Persistent Client...", flush=True)
    print(f"Profile directory: {PROFILE_DIR}", flush=True)
    print("=" * 60, flush=True)
    
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(PROFILE_DIR),
            headless=True,
            args=[
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox"
            ]
        )
        page = browser.new_page()
        page.set_viewport_size({"width": 1280, "height": 850})
        
        print("Opening https://web.telegram.org/k/ ...", flush=True)
        page.goto("https://web.telegram.org/k/", wait_until="domcontentloaded", timeout=45000)
        
        # Wait for QR container or search input
        for _ in range(15):
            if page.query_selector("input.input-search-input") or page.query_selector("div[class*='qrContainer']"):
                break
            time.sleep(1)
        time.sleep(2)
        
        # Check if already logged in
        if page.query_selector("input.input-search-input"):
            print(">>> STATUS: ALREADY LOGGED IN TO TELEGRAM! <<<", flush=True)
            page.screenshot(path=str(LOGGED_IN_PATH))
            browser.close()
            return True
            
        # Capture QR Code card
        card_el = page.query_selector("div[class*='_card_']")
        if card_el:
            card_el.screenshot(path=str(QR_CARD_PATH))
            print(f"Captured clean QR Card: {QR_CARD_PATH}", flush=True)
        page.screenshot(path=str(QR_PATH))
        print(f"Captured full screen with QR: {QR_PATH}", flush=True)
        
        print("\n>>> QR CODE IS READY FOR SCANNING! <<<", flush=True)
        
        start_time = time.time()
        last_snap = start_time
        notified_2fa = False
        
        while time.time() - start_time < timeout_seconds:
            time.sleep(2)
            
            # 1. Check if logged in completely
            if page.query_selector("input.input-search-input"):
                print("\n" + "=" * 60, flush=True)
                print(">>> SUCCESS! USER SCAN DETECTED AND LOGGED IN! <<<", flush=True)
                print("=" * 60, flush=True)
                time.sleep(4)
                page.screenshot(path=str(LOGGED_IN_PATH))
                print(f"Saved logged in screenshot: {LOGGED_IN_PATH}", flush=True)
                browser.close()
                return True
                
            # 2. Check if 2FA password field appeared
            pwd_el = page.query_selector("input[type='password']:visible")
            if pwd_el:
                if not notified_2fa:
                    print(">>> QR SCANNED! 2FA CLOUD PASSWORD SCREEN DETECTED! <<<", flush=True)
                    page.screenshot(path=str(TWO_FA_SCREEN_PATH))
                    notified_2fa = True
                if TWO_FA_FILE.exists():
                    pwd = TWO_FA_FILE.read_text(encoding="utf-8").strip()
                    TWO_FA_FILE.unlink()
                    if pwd:
                        print("Entering 2FA password...", flush=True)
                        pwd_el.fill(pwd)
                        pwd_el.press("Enter")
                        time.sleep(5)
                continue
                
            # 3. Periodically refresh QR code screenshot every 15s
            if time.time() - last_snap > 15:
                card_el = page.query_selector("div[class*='_card_']")
                if card_el:
                    card_el.screenshot(path=str(QR_CARD_PATH))
                page.screenshot(path=str(QR_PATH))
                last_snap = time.time()
                elapsed = int(time.time() - start_time)
                print(f"[{elapsed}s / {timeout_seconds}s] Refreshed QR code... waiting for scan.", flush=True)
                
        print("Timed out waiting for QR scan.", flush=True)
        browser.close()
        return False

if __name__ == "__main__":
    timeout = int(sys.argv[1]) if len(sys.argv) > 1 else 600
    wait_for_login_and_listen(timeout)
