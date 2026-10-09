#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
setup_telegram_channels.py

Automates setting up the 3 Telegram channels:
1. Adds @smartbet_guru_bot as administrator with full rights
2. Configures public link (@SmartBetGuruEN, @SmartBetGuruFR, @SmartBetGuruES)
Uses existing authenticated Telegram Web session in storage/telegram_web_profile.
"""

import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()
BOT_USERNAME = "@smartbet_guru_bot"

CHANNELS = [
    {"name": "English", "peer_id": "-3960368887", "target_username": "SmartBetGuruEN"},
    {"name": "France",  "peer_id": "-4371643544", "target_username": "SmartBetGuruFR"},
    {"name": "España",  "peer_id": "-4346736376", "target_username": "SmartBetGuruES"},
]

def setup():
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(STORAGE_DIR),
            headless=True,
            args=["--disable-blink-features=AutomationControlled", "--no-sandbox"],
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()

        print("[tg] Checking Telegram Web session...")
        page.goto("https://web.telegram.org/k/", wait_until="domcontentloaded", timeout=60000)
        time.sleep(4)
        page.screenshot(path="storage/tg_check_session.png")

        for ch in CHANNELS:
            print(f"\n[tg] Processing channel {ch['name']} (peer: {ch['peer_id']})...")
            page.goto(f"https://web.telegram.org/k/#{ch['peer_id']}", wait_until="domcontentloaded", timeout=60000)
            time.sleep(3)
            page.screenshot(path=f"storage/tg_{ch['name']}_opened.png")

            # Click on the channel header to open profile/details
            header = page.locator(".chat-info, .top .peer-title, .chat-info-name").first
            if header.is_visible():
                header.click()
                time.sleep(2)
                page.screenshot(path=f"storage/tg_{ch['name']}_profile.png")
            else:
                print(f"[tg] Header not found for {ch['name']}")

        browser.close()

if __name__ == "__main__":
    setup()
