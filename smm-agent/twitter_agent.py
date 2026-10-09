#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Twitter / X Automation & Verification Agent
Checks Twitter credentials for @smartbetguru, verifies login, and publishes SEO signals.
Rule 6 (Cluster Proxy), Rule 9 (Firefox / Camoufox Persistent Context), Rule 10 (80% Freebet Cash)
"""

import asyncio
import logging
import os
from pathlib import Path
import sys
import time
from typing import Dict, Optional, Tuple

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

from playwright.async_api import async_playwright

logger = logging.getLogger("TwitterAgent")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] [TwitterAgent] %(message)s")

PROXY_SERVER = os.getenv("CLUSTER_PROXY", "http://100.83.113.50:3128")
STORAGE_DIR = Path(__file__).parent / "storage"
TWITTER_PROFILE_DIR = STORAGE_DIR / "twitter_profile"
TWITTER_PROFILE_DIR.mkdir(parents=True, exist_ok=True)

TWITTER_USERNAME = os.getenv("TWITTER_USERNAME", "smartbetguru")
TWITTER_EMAIL = os.getenv("TWITTER_EMAIL", "www.smartbet.guru@gmail.com")
TWITTER_PASSWORD = os.getenv("TWITTER_PASSWORD", "Racoiaws12!@")


class TwitterAgent:
    def __init__(
        self,
        username: str = TWITTER_USERNAME,
        email: str = TWITTER_EMAIL,
        password: str = TWITTER_PASSWORD,
        profile_dir: Path = TWITTER_PROFILE_DIR,
        proxy_server: str = PROXY_SERVER
    ):
        self.username = username
        self.email = email
        self.password = password
        self.profile_dir = profile_dir
        self.proxy_server = proxy_server

    async def verify_login_status(self, headless: bool = True) -> Tuple[bool, str]:
        """
        Launches persistent browser context, navigates to x.com, and checks if already logged in.
        If not logged in, attempts automated login flow with credentials.
        """
        logger.info(f"Checking Twitter/X login status for @{self.username}...")
        proxy_config = {"server": self.proxy_server} if self.proxy_server else None

        async with async_playwright() as p:
            # Rule 9: Firefox persistent context
            try:
                browser = await p.firefox.launch_persistent_context(
                    user_data_dir=str(self.profile_dir),
                    headless=headless,
                    proxy=proxy_config,
                    viewport={"width": 1280, "height": 800},
                    user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0"
                )
            except Exception as e:
                logger.error(f"Failed to launch Firefox persistent context: {e}")
                return False, f"Browser launch failed: {e}"

            page = browser.pages[0] if browser.pages else await browser.new_page()

            try:
                # 1. Navigate to login directly
                logger.info("Navigating to https://x.com/i/flow/login...")
                try:
                    await page.goto("https://x.com/i/flow/login", timeout=60000, wait_until="commit")
                except Exception as e:
                    logger.warning(f"Navigation commit warning: {e}")

                logger.info("Waiting for login input field...")
                try:
                    await page.wait_for_selector('input[autocomplete="username"], input[name="text"], input[name="password"]', timeout=30000)
                except Exception as e:
                    logger.warning(f"Timeout waiting for input: {e}")

                await asyncio.sleep(3)
                current_url = page.url
                logger.info(f"Current page URL: {current_url}")
                await page.screenshot(path=str(STORAGE_DIR / "twitter_screen_1.png"))

                # Check for username / email input
                username_input = page.locator('input[autocomplete="username"], input[name="text"]')
                if await username_input.count() > 0 and await username_input.is_visible():
                    logger.info("Entering username/email into login input...")
                    # Try username first
                    await username_input.fill(self.username)
                    await asyncio.sleep(1)

                    # Click Next
                    next_button = page.locator('button:has-text("Next"), div[role="button"]:has-text("Next")')
                    if await next_button.count() > 0:
                        await next_button.first.click()
                        await asyncio.sleep(4)

                    await page.screenshot(path=str(STORAGE_DIR / "twitter_after_username.png"))

                    # Sometimes Twitter asks for phone/email for security confirmation
                    challenge_input = page.locator('input[data-testid="ocfEnterTextTextInput"], input[name="text"]')
                    if await challenge_input.count() > 0 and await challenge_input.is_visible():
                        logger.info("Twitter requested secondary confirmation (email). Filling email...")
                        await challenge_input.fill(self.email)
                        await asyncio.sleep(1)
                        next_btn2 = page.locator('button:has-text("Next"), div[role="button"]:has-text("Next")')
                        if await next_btn2.count() > 0:
                            await next_btn2.first.click()
                            await asyncio.sleep(4)

                    # Now enter password
                    pwd_input = page.locator('input[name="password"]')
                    if await pwd_input.count() > 0 and await pwd_input.is_visible():
                        logger.info("Password input field found. Submitting password...")
                        await pwd_input.fill(self.password)
                        await asyncio.sleep(1)

                        login_btn = page.locator('button:has-text("Log in"), div[role="button"]:has-text("Log in")')
                        if await login_btn.count() > 0:
                            await login_btn.first.click()
                            await asyncio.sleep(7)

                        await page.screenshot(path=str(STORAGE_DIR / "twitter_after_login.png"))

                        # Check if successful
                        if "/home" in page.url:
                            logger.info("Successfully logged into Twitter!")
                            await browser.close()
                            return True, "Login successful"

                        # Check if 2FA code is needed
                        body_text = await page.inner_text("body")
                        if "confirmation code" in body_text.lower() or "check your email" in body_text.lower():
                            logger.info("Twitter requested email verification code!")
                            await browser.close()
                            return False, "Needs email 2FA verification code"

                # Check current state
                await page.screenshot(path=str(STORAGE_DIR / "twitter_final_state.png"))
                await browser.close()
                return False, f"Login incomplete, URL: {page.url}"

            except Exception as e:
                logger.error(f"Error during Twitter login flow: {e}")
                await page.screenshot(path=str(STORAGE_DIR / "twitter_error.png"))
                await browser.close()
                return False, str(e)


async def main():
    agent = TwitterAgent()
    success, msg = await agent.verify_login_status(headless=True)
    print(f"RESULT: {success} | Message: {msg}")


if __name__ == "__main__":
    asyncio.run(main())
