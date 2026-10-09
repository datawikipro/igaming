#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — GUI Interactive Instagram Login (Option 2)
Launches visible Firefox on Windows desktop with proxy and persistent profile.
Pre-fills credentials, allows user to verify/interact, automatically captures session cookies on login.
"""

import asyncio
import glob
import logging
import os
import sys
import time

from playwright.async_api import async_playwright

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [gui-instagram] %(message)s"
)
logger = logging.getLogger("GuiInstagramLogin")

DEFAULT_PROXY = "http://100.83.113.50:3128"
DEFAULT_PROFILE_DIR = r"c:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\profiles\instagram"
DEFAULT_STORAGE_PATH = r"c:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\profiles\instagram_storage.json"


async def main():
    username = os.getenv("INSTAGRAM_USER", "fr.smartbet.guru")
    password = os.getenv("INSTAGRAM_PASS", "Racoiaws12!@")
    proxy_url = os.getenv("INSTAGRAM_PROXY", DEFAULT_PROXY)
    profile_dir = DEFAULT_PROFILE_DIR
    storage_path = DEFAULT_STORAGE_PATH

    os.makedirs(profile_dir, exist_ok=True)
    os.makedirs(os.path.dirname(storage_path), exist_ok=True)

    logger.info("==================================================================")
    logger.info("Starting Visible GUI Instagram Login (Option 2)")
    logger.info(f"Target Username: {username}")
    logger.info(f"Persistent Profile: {profile_dir}")
    logger.info(f"Proxy: {proxy_url}")
    logger.info("==================================================================")

    # Locate Playwright Firefox binary
    firefox_bin = None
    candidates = glob.glob(os.path.expanduser(r"~\AppData\Local\ms-playwright\firefox-*\firefox\firefox.exe"))
    if candidates:
        firefox_bin = sorted(candidates)[-1]
        logger.info(f"Using Firefox binary: {firefox_bin}")

    async with async_playwright() as p:
        launch_kwargs = {
            "user_data_dir": profile_dir,
            "headless": False,  # Visible browser window on Windows desktop!
            "proxy": {"server": proxy_url} if proxy_url else None,
            "viewport": {"width": 1280, "height": 850},
            "locale": "fr-FR",
            "timezone_id": "Europe/Paris",
            "firefox_user_prefs": {
                "marionette.enabled": False,
                "dom.webdriver.enabled": False,
                "media.peerconnection.enabled": True,
            },
        }
        if firefox_bin:
            launch_kwargs["executable_path"] = firefox_bin

        logger.info("Opening Firefox window on your screen...")
        context = await p.firefox.launch_persistent_context(**launch_kwargs)

        pages = context.pages
        page = pages[0] if pages else await context.new_page()

        logger.info("Navigating to https://www.instagram.com/...")
        await page.goto("https://www.instagram.com/", timeout=45000, wait_until="domcontentloaded")
        await asyncio.sleep(2.0)

        # Check if already logged in
        home_loc = page.locator("svg[aria-label='Home'], svg[aria-label='Accueil'], svg[aria-label='Direct'], a[href*='/direct/inbox/']").first
        if await home_loc.is_visible(timeout=3000):
            logger.info("🎉 Already logged in! Saving session state...")
            await context.storage_state(path=storage_path)
            logger.info(f"Session saved to {storage_path}")
            await asyncio.sleep(3.0)
            await context.close()
            return True

        # Try to fill login form
        try:
            user_input = page.locator("input[name='email'], input[name='username']").first
            pass_input = page.locator("input[name='pass'], input[name='password']").first

            if await user_input.is_visible(timeout=5000):
                logger.info(f"Pre-filling login: {username} and password...")
                await user_input.click()
                await user_input.fill(username)
                await asyncio.sleep(0.5)
                await pass_input.click()
                await pass_input.fill(password)
                logger.info("Credentials pre-filled! You can verify them on screen.")
        except Exception as e:
            logger.info(f"Note on pre-fill: {e}")

        logger.info("==================================================================")
        logger.info("ОКНО БРАУЗЕРА ОТКРЫТО НА ВАШЕМ ЭКРАНЕ.")
        logger.info("Вы можете войти, проверить логин/пароль или подтвердить 2FA/чекпоинт.")
        logger.info("Скрипт ожидает завершения входа (таймаут 5 минут)...")
        logger.info("==================================================================")

        # Poll for successful login
        max_wait_seconds = 300
        start_wait = time.time()
        logged_in = False

        while time.time() - start_wait < max_wait_seconds:
            try:
                # Check for home icon or direct link or profile link
                home = page.locator("svg[aria-label='Home'], svg[aria-label='Accueil'], svg[aria-label='Direct'], svg[aria-label='Reels'], a[href*='/direct/inbox/']").first
                if await home.is_visible(timeout=1000):
                    logged_in = True
                    break

                # Also check URL
                curr_url = page.url
                if "instagram.com" in curr_url and "/accounts/login" not in curr_url and "/accounts/emailsignup" not in curr_url and len(curr_url.split("/")) <= 4:
                    # Often root url with feed
                    if await page.locator("svg[aria-label='Home'], svg[aria-label='Accueil']").first.is_visible(timeout=1000):
                        logged_in = True
                        break
            except Exception:
                pass

            await asyncio.sleep(2.0)

        if logged_in:
            logger.info("🎉 ВХОД УСПЕШНО ВЫПОЛНЕН! Сохраняю персистентную сессию...")
            await asyncio.sleep(2.0)
            await context.storage_state(path=storage_path)
            logger.info(f"Сессия успешно сохранена в: {storage_path}")
            logger.info("Профиль сохранен в persistent user_data_dir.")
            await asyncio.sleep(3.0)
            await context.close()
            return True
        else:
            logger.warning("Время ожидания входа истекло (5 минут). Сохраняю текущее состояние...")
            await context.storage_state(path=storage_path)
            await context.close()
            return False


if __name__ == "__main__":
    success = asyncio.run(main())
    sys.exit(0 if success else 1)
