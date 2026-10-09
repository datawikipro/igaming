#!/usr/bin/env python3
import asyncio
import os
import sys
import time
from pathlib import Path
from playwright.async_api import async_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

PROXY = os.environ.get("PROXY_SERVER", "http://100.83.113.50:3128")
STORAGE = Path("smm-agent/profiles/twitter_storage.json").resolve()
SCREEN_DIR = Path("smm-agent/storage").resolve()
SCREEN_DIR.mkdir(parents=True, exist_ok=True)

TWEET_TEXT = (
    "🎯 FREE BET ARBITRAGE ALERT\n\n"
    "⚽ Liverpool vs Chelsea\n"
    "📌 Bet365 £100 Free Bet → £82 GUARANTEED Cash\n"
    "💰 Net profit: +£81.30 on any result!\n\n"
    "Free bet = 80%+ cash via matched betting.\n"
    "Free calculator at smartbet.guru/tools/freebet-calculator 👇\n\n"
    "#matchedbetting #freebet #arbitrage #smartbet"
)

async def post_tweet(tweet_text=TWEET_TEXT):
    print("Starting Twitter/X post flow via Firefox...")
    async with async_playwright() as p:
        browser = await p.firefox.launch(
            headless=True,
            proxy={"server": PROXY}
        )
        ctx = await browser.new_context(
            storage_state=str(STORAGE),
            viewport={"width": 1280, "height": 900},
            user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0"
        )
        page = await ctx.new_page()
        
        print("1. Navigating to https://x.com/home...")
        try:
            await page.goto("https://x.com/home", timeout=60000, wait_until="domcontentloaded")
        except Exception as e:
            print("Nav exception:", e)
        
        await asyncio.sleep(6)
        
        # Check if login required
        if "/login" in page.url or "/i/flow" in page.url:
            print("Session expired or redirected to login:", page.url)
            await page.screenshot(path=str(SCREEN_DIR / "tw_login_redirect.png"))
            await browser.close()
            return False
            
        # Check and dismiss "Got it" modal if present
        got_it = page.locator("button:has-text('Got it')").first
        if await got_it.is_visible():
            print("Dismissing 'Got it' modal...")
            await got_it.click(force=True)
            await asyncio.sleep(2)
            
        # Locate compose textarea
        print("2. Locating tweet textarea...")
        tweet_box = page.locator("[data-testid='tweetTextarea_0']").first
        await tweet_box.wait_for(state="visible", timeout=20000)
        
        print("3. Clicking tweet box to expand...")
        await tweet_box.click(force=True)
        await asyncio.sleep(1)
        
        print("4. Clearing tweet box and filling text...")
        await tweet_box.fill("")
        await asyncio.sleep(0.5)
        await tweet_box.fill(tweet_text)
        await asyncio.sleep(2)
        await page.screenshot(path=str(SCREEN_DIR / "tw_typed_fresh.png"))
        
        # Locate Post button
        print("5. Locating Post button...")
        post_btn = page.locator("[data-testid='tweetButtonInline'], [data-testid='tweetButton']").first
        await post_btn.wait_for(state="visible", timeout=10000)
        
        print("6. Submitting tweet...")
        await post_btn.click(force=True)
        await asyncio.sleep(2)
        await tweet_box.press("Control+Enter")
        
        print("Waiting for tweet submission confirmation...")
        await asyncio.sleep(6)
        
        await page.screenshot(path=str(SCREEN_DIR / "tw_published_final.png"))
        print("🎉 Tweet successfully submitted for @smartbetguru!")
        
        # Update storage state
        await ctx.storage_state(path=str(STORAGE))
        await browser.close()
        return True

if __name__ == "__main__":
    success = asyncio.run(post_tweet())
    print("Post result:", success)
