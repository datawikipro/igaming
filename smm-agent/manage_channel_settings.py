#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
manage_channel_settings.py

Inspects and automates Telegram channel edit screen via Telegram Web K.
Uses page.evaluate for fast single-call DOM inspection.
"""

import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

STORAGE_DIR = Path("smm-agent/storage/telegram_web_profile").resolve()

def inspect_channel(peer_id="-3960368887"):
    with sync_playwright() as p:
        browser = p.chromium.launch_persistent_context(
            user_data_dir=str(STORAGE_DIR),
            headless=True,
            args=["--disable-blink-features=AutomationControlled", "--no-sandbox"],
            viewport={"width": 1280, "height": 900}
        )
        page = browser.pages[0] if browser.pages else browser.new_page()

        print(f"[tg] Opening channel {peer_id}...")
        page.goto(f"https://web.telegram.org/k/#{peer_id}", wait_until="domcontentloaded", timeout=60000)
        time.sleep(3)

        # 1. Click header to open sidebar info
        print("[tg] Opening sidebar info...")
        page.locator(".chat-info, .top .peer-title").first.click()
        time.sleep(2)

        # 2. Click the edit pencil button (the button on the right in sidebar-header)
        print("[tg] Clicking edit pencil button...")
        pencil = page.locator(".sidebar-header button.btn-icon").nth(1)
        if pencil.is_visible():
            pencil.click()
        else:
            page.mouse.click(1228, 44)
        time.sleep(2)

        # 3. Fast JS evaluation of all visible rows in the active slider
        rows_info = page.evaluate("""() => {
            const activeSlider = document.querySelector('.sidebar-slider-item.active') || document.querySelector('.sidebar-content');
            if (!activeSlider) return {error: 'no active slider'};
            const rows = Array.from(activeSlider.querySelectorAll('.row, .item, .menu-item, .btn-menu-item, button, .list-item'));
            return rows.map((r, i) => {
                const rect = r.getBoundingClientRect();
                return {
                    index: i,
                    tag: r.tagName,
                    text: r.innerText.trim().replace(/\\n/g, ' | '),
                    x: Math.round(rect.x + rect.width / 2),
                    y: Math.round(rect.y + rect.height / 2),
                    visible: rect.width > 0 && rect.height > 0
                };
            }).filter(item => item.visible && item.text.length > 0);
        }""")

        import json
        with open("storage/tg_channel_items.json", "w", encoding="utf-8") as f:
            json.dump(rows_info, f, indent=2, ensure_ascii=False)

        print(f"[tg] Found {len(rows_info)} visible interactive items. Saved to storage/tg_channel_items.json.")
        for r in rows_info:
            safe_text = r['text'].encode('ascii', errors='replace').decode('ascii')[:60]
            print(f"  [{r['index']:02d}] ({r['x']}, {r['y']}) <{r['tag']}> {safe_text}")

        page.screenshot(path="storage/tg_edit_inspected.png")
        browser.close()

if __name__ == "__main__":
    inspect_channel()
