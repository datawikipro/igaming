#!/usr/bin/env python3
"""
Twitter / X poster for SmartBet.guru
Runs on xeon-srv (direct US routing via outline-us, no proxy needed).
Stores session in /opt/smm-agent/storage/twitter_profile/
Usage:
    python3 twitter_post.py --post     # post signal
    python3 twitter_post.py --login    # interactive login only
"""
import argparse
import json
import os
import sys
import time
import random
from pathlib import Path
from datetime import datetime

from playwright.sync_api import sync_playwright, TimeoutError as PWTimeout

# ── Config ────────────────────────────────────────────────────────────────────
STORAGE_DIR = Path("/opt/smm-agent/storage/twitter_profile")
STORAGE_DIR.mkdir(parents=True, exist_ok=True)

HISTORY_FILE = Path("/opt/smm-agent/storage/published_history.json")

CREDENTIALS = {
    "username": "smartbetguru",
    "email":    "www.smartbet.guru@gmail.com",
    "password": "Racoiaws12!@",
}

# ── Signal templates ──────────────────────────────────────────────────────────
SIGNALS = [
    {
        "lang": "en",
        "text": (
            "🎯 FREE BET ARBITRAGE ALERT\n\n"
            "⚽ Liverpool vs Chelsea — Premier League\n"
            "📌 Bet365 £100 Free Bet → £82 GUARANTEED cash\n\n"
            "✅ How: Cover with lay bet at odds 5.5 / lay 1.24\n"
            "💰 Net profit: +£82 regardless of result\n\n"
            "Free bet = 82% real money via matched betting formula\n"
            "All maths at SmartBet.guru 👇\n\n"
            "#matchedbetting #freebet #arbitrage #smartbet #betting"
        ),
    },
    {
        "lang": "en",
        "text": (
            "📊 SURE BET SCANNER — Live update\n\n"
            "⚽ Man City vs Arsenal\n"
            "🏆 ROI: +3.2% guaranteed\n"
            "📌 Back: Man City @2.10 (Pinnacle)\n"
            "📌 Lay: Draw+Away @2.05 (Bet365)\n\n"
            "No risk. Both bets cover all outcomes.\n"
            "Full calculator → SmartBet.guru\n\n"
            "#surebet #arbitrage #betfair #valuebet"
        ),
    },
    {
        "lang": "en",
        "text": (
            "🔥 VALUE BET of the day\n\n"
            "⚽ Real Madrid vs Barcelona — La Liga\n"
            "📌 Model probability: Real Madrid win 58%\n"
            "📌 Market odds: 2.10 → implied 47%\n"
            "📈 Edge: +11% expected value\n\n"
            "Kelly stake: 5.5% of bankroll\n"
            "Track all +EV bets FREE at SmartBet.guru 👇\n\n"
            "#valuebet #EV #sportsbetting #betsmarter"
        ),
    },
]

# ── Deduplication ─────────────────────────────────────────────────────────────

def load_history() -> list:
    if HISTORY_FILE.exists():
        return json.loads(HISTORY_FILE.read_text())
    return []

def save_history(history: list):
    HISTORY_FILE.write_text(json.dumps(history, indent=2, ensure_ascii=False))

def pick_signal(history: list) -> dict:
    used_texts = {h["content"] for h in history if h.get("platform") == "twitter"}
    for sig in SIGNALS:
        if sig["text"] not in used_texts:
            return sig
    # All used — rotate oldest
    return SIGNALS[0]

# ── Browser helpers ───────────────────────────────────────────────────────────

def human_type(page, selector, text: str):
    page.click(selector)
    for ch in text:
        page.keyboard.type(ch)
        time.sleep(random.uniform(0.04, 0.12))

def wait_and_screenshot(page, path: str, timeout: int = 5000):
    time.sleep(timeout / 1000)
    page.screenshot(path=path)
    print(f"[screenshot] {path}")

# ── Login flow ────────────────────────────────────────────────────────────────

def login(page):
    print("[twitter] Navigating to x.com/login ...")
    page.goto("https://x.com/login", wait_until="domcontentloaded", timeout=60000)
    wait_and_screenshot(page, "/tmp/tw_01_loaded.png")

    # Username / email field
    print("[twitter] Entering username ...")
    page.wait_for_selector("input[autocomplete='username']", timeout=30000)
    human_type(page, "input[autocomplete='username']", CREDENTIALS["email"])
    page.keyboard.press("Enter")
    wait_and_screenshot(page, "/tmp/tw_02_user_entered.png", 3000)

    # Handle "Enter your phone or username" challenge
    unusual = page.locator("text=Enter your phone number or username").first
    if unusual.is_visible():
        print("[twitter] Unusual login challenge — entering username ...")
        inp = page.locator("input[data-testid='ocfEnterTextTextInput']").first
        human_type(page, inp, CREDENTIALS["username"])
        page.keyboard.press("Enter")
        time.sleep(2)

    # Password
    print("[twitter] Entering password ...")
    page.wait_for_selector("input[type='password']", timeout=20000)
    human_type(page, "input[type='password']", CREDENTIALS["password"])
    page.keyboard.press("Enter")
    wait_and_screenshot(page, "/tmp/tw_03_password_entered.png", 5000)

    # Verify we're logged in
    try:
        page.wait_for_url("**/home", timeout=20000)
        print("[twitter] ✅ Logged in successfully!")
    except PWTimeout:
        wait_and_screenshot(page, "/tmp/tw_04_login_fail.png")
        raise RuntimeError("Login failed — check /tmp/tw_04_login_fail.png")

# ── Post flow ─────────────────────────────────────────────────────────────────

def post_tweet(page, text: str):
    print("[twitter] Navigating to home ...")
    page.goto("https://x.com/home", wait_until="domcontentloaded", timeout=60000)
    time.sleep(3)
    wait_and_screenshot(page, "/tmp/tw_05_home.png")

    # Click compose box
    print("[twitter] Opening compose box ...")
    compose_selectors = [
        "[data-testid='tweetTextarea_0']",
        "div[role='textbox'][aria-label*='tweet']",
        "div[role='textbox'][aria-label*='Tweet']",
        ".public-DraftEditor-content",
    ]
    compose = None
    for sel in compose_selectors:
        try:
            page.wait_for_selector(sel, timeout=10000)
            compose = page.locator(sel).first
            break
        except PWTimeout:
            continue

    if compose is None:
        # Try clicking the big compose button
        page.locator("a[href='/compose/post'], a[href='/compose/tweet']").first.click()
        time.sleep(2)
        page.wait_for_selector("[data-testid='tweetTextarea_0']", timeout=10000)
        compose = page.locator("[data-testid='tweetTextarea_0']").first

    compose.click()
    time.sleep(1)

    # Type tweet text (character by character for human feel)
    print(f"[twitter] Typing tweet ({len(text)} chars) ...")
    for ch in text:
        page.keyboard.type(ch)
        time.sleep(random.uniform(0.02, 0.08))

    wait_and_screenshot(page, "/tmp/tw_06_typed.png")

    # Click Post button
    print("[twitter] Clicking Post button ...")
    post_btn = page.locator("[data-testid='tweetButtonInline'], [data-testid='tweetButton']").first
    post_btn.wait_for(state="visible", timeout=10000)
    post_btn.click()
    time.sleep(5)
    wait_and_screenshot(page, "/tmp/tw_07_posted.png")
    print("[twitter] ✅ Tweet posted!")

# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--login", action="store_true", help="Login only")
    parser.add_argument("--post", action="store_true", help="Post signal")
    args = parser.parse_args()

    with sync_playwright() as p:
        # Use system Chrome if available (avoids playwright install)
        import shutil
        chrome_paths = [
            "/opt/google/chrome/chrome",
            "/usr/bin/google-chrome",
            "/usr/bin/chromium-browser",
            "/usr/bin/chromium",
        ]
        exe = next((p for p in chrome_paths if shutil.which(p) or os.path.isfile(p)), None)

        launch_kwargs = dict(
            user_data_dir=str(STORAGE_DIR),
            headless=True,
            args=[
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
            ],
            viewport={"width": 1280, "height": 900},
            locale="en-US",
            proxy={"server": os.environ.get("PROXY_SERVER", "http://100.83.113.50:3128")},
            user_agent=(
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
                "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            ),
        )
        if exe:
            print(f"[twitter] Using system Chrome: {exe}")
            launch_kwargs["executable_path"] = exe

        browser = p.chromium.launch_persistent_context(**launch_kwargs)
        page = browser.pages[0] if browser.pages else browser.new_page()

        # Check if already logged in
        page.goto("https://x.com/home", wait_until="domcontentloaded", timeout=60000)
        time.sleep(3)
        if "/login" in page.url or "/i/flow" in page.url:
            print("[twitter] Not logged in — starting login flow ...")
            login(page)
        else:
            print("[twitter] Already logged in ✅")

        if args.login:
            print("[twitter] Login-only mode done.")
            browser.close()
            return

        if args.post:
            history = load_history()
            signal = pick_signal(history)
            post_tweet(page, signal["text"])
            # Record
            history.append({
                "platform": "twitter",
                "lang": signal["lang"],
                "content": signal["text"],
                "published_at": datetime.utcnow().isoformat(),
            })
            save_history(history)
            print(f"[twitter] Recorded in history. Total: {len(history)}")

        browser.close()

if __name__ == "__main__":
    main()
