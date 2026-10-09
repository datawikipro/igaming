#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Instagram Authentication Script (Playwright Firefox + IMAP 2FA)
Rule 6 (Proxy: 100.83.113.50:3128) & Rule 9 (Firefox Persistent Context, No Incognito)
"""

import argparse
import asyncio
import email
from email.header import decode_header
import glob
import imaplib
import json
import logging
import os
import random
import re
import sys
import time
from typing import Optional

from playwright.async_api import async_playwright

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [instagram-login] %(message)s"
)
logger = logging.getLogger("InstagramLogin")

DEFAULT_PROXY = "http://100.83.113.50:3128"
DEFAULT_PROFILE_DIR = r"c:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\profiles\instagram"
DEFAULT_STORAGE_PATH = r"c:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\profiles\instagram_storage.json"
SCRATCH_DIR = r"c:\Users\chernousov_a\IdeaProjects\igaming\scratch"


def decode_str(val) -> str:
    if not val:
        return ""
    try:
        parts = decode_header(val)
        res = ""
        for s, enc in parts:
            if isinstance(s, bytes):
                res += s.decode(enc or "utf-8", errors="ignore")
            else:
                res += str(s)
        return res
    except Exception:
        return str(val)


def get_body_text(msg: email.message.Message) -> str:
    body = ""
    if msg.is_multipart():
        for part in msg.walk():
            ctype = part.get_content_type()
            cdispo = str(part.get("Content-Disposition"))
            if ctype in ("text/plain", "text/html") and "attachment" not in cdispo:
                payload = part.get_payload(decode=True)
                if payload:
                    body += payload.decode(part.get_content_charset() or "utf-8", errors="ignore") + "\n"
    else:
        payload = msg.get_payload(decode=True)
        if payload:
            body = payload.decode(msg.get_content_charset() or "utf-8", errors="ignore")
    return body


def poll_imap_for_instagram_code(
    email_user: str,
    email_pass: str,
    since_epoch: float,
    timeout_sec: int = 90,
    poll_interval: int = 3
) -> Optional[str]:
    """
    Polls Gmail via IMAP for incoming Instagram verification code.
    Looks for 6-digit codes in recent emails from Instagram / Meta.
    """
    logger.info(f"Starting IMAP poller for {email_user} (since epoch: {since_epoch})...")
    start_time = time.time()
    seen_ids = set()

    while time.time() - start_time < timeout_sec:
        try:
            mail = imaplib.IMAP4_SSL("imap.gmail.com", 993)
            mail.login(email_user, email_pass)
            mail.select("INBOX")

            # Search ALL messages
            status, messages = mail.search(None, "ALL")
            if status == "OK" and messages[0]:
                msg_ids = messages[0].split()
                # Check the last 8 messages
                for mid in reversed(msg_ids[-8:]):
                    mid_str = mid.decode()
                    res, data = mail.fetch(mid, "(RFC822)")
                    if res != "OK":
                        continue
                    raw_email = data[0][1]
                    msg = email.message_from_bytes(raw_email)

                    from_hdr = decode_str(msg.get("From", "")).lower()
                    subject = decode_str(msg.get("Subject", ""))
                    date_hdr = msg.get("Date", "")
                    
                    logger.debug(f"Checking email ID {mid_str}: From={from_hdr}, Subj={subject}")

                    # Check if relevant sender
                    is_insta = any(k in from_hdr for k in ["instagram", "meta", "facebook", "security"])
                    is_insta_subj = any(k in subject.lower() for k in ["instagram", "código", "code", "security"])

                    if is_insta or is_insta_subj:
                        # Extract code from Subject first
                        match = re.search(r"\b(\d{6})\b", subject)
                        if match:
                            code = match.group(1)
                            logger.info(f"--> Found 6-digit code in SUBJECT [{subject}]: {code}")
                            mail.logout()
                            return code

                        # Check body
                        body = get_body_text(msg)
                        # Often Instagram says "<p style="...">123 456</p>" or "123456 is your Instagram code"
                        # Handle spaced 6-digit codes like "123 456"
                        body_clean = body.replace("&nbsp;", " ")
                        match_spaced = re.search(r"\b(\d{3})\s+(\d{3})\b", body_clean)
                        if match_spaced:
                            code = match_spaced.group(1) + match_spaced.group(2)
                            logger.info(f"--> Found spaced 6-digit code in BODY: {code}")
                            mail.logout()
                            return code

                        match_body = re.search(r"\b(\d{6})\b", body_clean)
                        if match_body:
                            code = match_body.group(1)
                            logger.info(f"--> Found 6-digit code in BODY: {code}")
                            mail.logout()
                            return code

            mail.logout()
        except Exception as e:
            logger.warning(f"IMAP poll error: {e}")

        logger.info(f"Waiting for Instagram email... ({int(time.time() - start_time)}s / {timeout_sec}s elapsed)")
        time.sleep(poll_interval)

    logger.warning("Timeout reached waiting for 2FA code via IMAP.")
    return None


async def human_type(locator, text: str, min_delay: float = 0.05, max_delay: float = 0.15):
    """Types text character by character with randomized delays."""
    await locator.click()
    await asyncio.sleep(random.uniform(0.2, 0.4))
    for char in text:
        await locator.type(char, delay=random.uniform(min_delay, max_delay) * 1000)
    await asyncio.sleep(random.uniform(0.3, 0.6))


async def run_instagram_login(
    username: str,
    password: str,
    email_user: str,
    email_pass: str,
    proxy_url: str = DEFAULT_PROXY,
    profile_dir: str = DEFAULT_PROFILE_DIR,
    storage_path: str = DEFAULT_STORAGE_PATH,
    headless: bool = True
):
    os.makedirs(profile_dir, exist_ok=True)
    os.makedirs(SCRATCH_DIR, exist_ok=True)
    os.makedirs(os.path.dirname(storage_path), exist_ok=True)

    logger.info("==================================================================")
    logger.info(f"Starting Instagram Login Workflow for: {username}")
    logger.info(f"Profile Directory: {profile_dir}")
    logger.info(f"Proxy URL: {proxy_url}")
    logger.info(f"Headless: {headless}")
    logger.info("==================================================================")

    proxy_config = {"server": proxy_url} if proxy_url else None

    # Find firefox binary if present in AppData
    firefox_bin = None
    candidates = glob.glob(os.path.expanduser(r"~\AppData\Local\ms-playwright\firefox-*\firefox\firefox.exe"))
    if candidates:
        firefox_bin = sorted(candidates)[-1]
        logger.info(f"Found Firefox binary: {firefox_bin}")

    async with async_playwright() as p:
        launch_kwargs = {
            "user_data_dir": profile_dir,
            "headless": headless,
            "proxy": proxy_config,
            "viewport": {"width": 1280, "height": 800},
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

        logger.info("Launching persistent Firefox context...")
        context = await p.firefox.launch_persistent_context(**launch_kwargs)

        await context.add_init_script("""
            Object.defineProperty(Object.getPrototypeOf(navigator), 'webdriver', {
                get: () => undefined,
            });
        """)

        pages = context.pages
        page = pages[0] if pages else await context.new_page()

        try:
            logger.info("Step 1: Navigating to https://www.instagram.com/...")
            await page.goto("https://www.instagram.com/", timeout=45000, wait_until="domcontentloaded")
            await asyncio.sleep(random.uniform(3.0, 5.0))
            await page.screenshot(path=os.path.join(SCRATCH_DIR, "step1_landing.png"))
            logger.info("Saved landing screenshot to scratch/step1_landing.png")

            # Handle Cookie Consent Banner
            cookie_btn_selectors = [
                "button:has-text('Decline optional cookies')",
                "button:has-text('Refuser les cookies facultatifs')",
                "button:has-text('Only allow essential cookies')",
                "button:has-text('Allow all cookies')",
                "button:has-text('Autoriser tous les cookies')",
                "button:has-text('Allow essential and optional cookies')",
                "[aria-label='Decline optional cookies']",
                "[aria-label='Allow all cookies']",
            ]
            for sel in cookie_btn_selectors:
                try:
                    btn = page.locator(sel).first
                    if await btn.is_visible(timeout=2000):
                        logger.info(f"Clicking cookie banner button: {sel}")
                        await btn.click()
                        await asyncio.sleep(2.0)
                        break
                except Exception:
                    pass

            # Check if already logged in
            home_icon = page.locator("svg[aria-label='Home'], svg[aria-label='Accueil'], svg[aria-label='Direct'], a[href*='/direct/inbox/']").first
            try:
                if await home_icon.is_visible(timeout=3000):
                    logger.info("SUCCESS: Already logged in to Instagram!")
                    await page.screenshot(path=os.path.join(SCRATCH_DIR, "insta_logged_in.png"))
                    await context.storage_state(path=storage_path)
                    logger.info(f"Saved storage state to {storage_path}")
                    return True
            except Exception:
                pass

            # Step 2: Fill Login Credentials
            logger.info("Step 2: Looking for login form inputs (name='email' or name='username')...")
            username_input = page.locator("input[name='email'], input[name='username']").first
            password_input = page.locator("input[name='pass'], input[name='password']").first

            await username_input.wait_for(state="visible", timeout=15000)
            await password_input.wait_for(state="visible", timeout=15000)

            logger.info(f"Typing username/email: {username}...")
            await human_type(username_input, username)

            logger.info("Typing password...")
            await human_type(password_input, password)

            submit_time = time.time()
            logger.info("Submitting login form via Enter key...")
            await password_input.press("Enter")
            await asyncio.sleep(1.0)

            # If still on page, try clicking the visible button
            try:
                btn = page.get_by_role("button", name="Se connecter")
                if await btn.is_visible(timeout=1000):
                    logger.info("Clicking visible 'Se connecter' button...")
                    await btn.click()
            except Exception:
                pass

            await asyncio.sleep(random.uniform(4.0, 6.0))
            await page.screenshot(path=os.path.join(SCRATCH_DIR, "step2_login_submitted.png"))
            logger.info("Saved post-submission screenshot to scratch/step2_login_submitted.png")

            # Step 3: Check for 2FA verification or security challenge
            logger.info("Step 3: Checking for 2FA / Security Code prompt...")
            code_input_selectors = [
                "input[name='verificationCode']",
                "input[type='tel']",
                "input[name='security_code']",
                "input[aria-label*='Code']",
                "input[placeholder*='Code']",
                "input[placeholder*='code']",
            ]
            code_input = None
            for sel in code_input_selectors:
                loc = page.locator(sel).first
                try:
                    if await loc.is_visible(timeout=3000):
                        code_input = loc
                        logger.info(f"Found 2FA verification code input with selector: {sel}")
                        break
                except Exception:
                    pass

            if code_input:
                logger.info("2FA Challenge active! Fetching verification code from Gmail via IMAP...")
                code = poll_imap_for_instagram_code(
                    email_user=email_user,
                    email_pass=email_pass,
                    since_epoch=submit_time - 30.0,
                    timeout_sec=90,
                    poll_interval=3
                )
                if not code:
                    logger.error("Failed to retrieve 2FA code from email!")
                    await page.screenshot(path=os.path.join(SCRATCH_DIR, "error_2fa_timeout.png"))
                    return False

                logger.info(f"Submitting 2FA code: {code}...")
                await human_type(code_input, code)
                confirm_btn = page.locator("button[type='button']:has-text('Confirm'), button:has-text('Confirmer'), button[type='submit']").first
                await confirm_btn.click()
                await asyncio.sleep(5.0)
                await page.screenshot(path=os.path.join(SCRATCH_DIR, "step3_2fa_submitted.png"))

            # Step 4: Dismiss Post-Login Dialogs ("Save Your Login Info?", "Turn on Notifications?")
            for dialog_text in ["Not Now", "Plus tard", "Not now"]:
                try:
                    btn = page.locator(f"button:has-text('{dialog_text}'), div[role='button']:has-text('{dialog_text}')").first
                    if await btn.is_visible(timeout=4000):
                        logger.info(f"Dismissing dialog button '{dialog_text}'")
                        await btn.click()
                        await asyncio.sleep(2.0)
                except Exception:
                    pass

            # Wait and verify logged-in state
            await asyncio.sleep(5.0)
            await page.screenshot(path=os.path.join(SCRATCH_DIR, "insta_final_state.png"))
            logger.info("Saved final state screenshot to scratch/insta_final_state.png")

            # Check if logged in
            current_url = page.url
            logger.info(f"Current page URL: {current_url}")

            # Check page content for signs of success
            page_text = await page.content()
            if "login" not in current_url and ("accounts/onetap" in current_url or "instagram.com" in current_url):
                # Verify navigation icons or profile avatar exist
                nav_icons = page.locator("svg[aria-label='Home'], svg[aria-label='Accueil'], svg[aria-label='Direct'], svg[aria-label='Reels'], svg[aria-label='Profile']").first
                if await nav_icons.is_visible(timeout=5000) or "/accounts/onetap" in current_url or page.url.rstrip("/") == "https://www.instagram.com":
                    logger.info("🎉 SUCCESS! Instagram login completed and verified!")
                    await context.storage_state(path=storage_path)
                    logger.info(f"Persisted storage state to {storage_path}")
                    return True

            logger.warning(f"Login completed with URL: {current_url}. Please inspect scratch/insta_final_state.png")
            await context.storage_state(path=storage_path)
            return True

        except Exception as e:
            logger.error(f"Error during Instagram login execution: {e}", exc_info=True)
            await page.screenshot(path=os.path.join(SCRATCH_DIR, "insta_login_exception.png"))
            return False
        finally:
            logger.info("Closing browser context...")
            await context.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Instagram Automated Login via Playwright")
    parser.add_argument("--username", default="fr.smartbet.guru")
    parser.add_argument("--password", default="Racoiaws12!@")
    parser.add_argument("--email", default="www.smartbet.guru@gmail.com")
    parser.add_argument("--email-pass", default="ujsrxyyvssfjfxpb")
    parser.add_argument("--proxy", default=DEFAULT_PROXY)
    parser.add_argument("--profile-dir", default=DEFAULT_PROFILE_DIR)
    parser.add_argument("--storage-path", default=DEFAULT_STORAGE_PATH)
    parser.add_argument("--headless", action="store_true", default=True)

    args = parser.parse_args()

    success = asyncio.run(run_instagram_login(
        username=args.username,
        password=args.password,
        email_user=args.email,
        email_pass=args.email_pass,
        proxy_url=args.proxy,
        profile_dir=args.profile_dir,
        storage_path=args.storage_path,
        headless=args.headless,
    ))

    sys.exit(0 if success else 1)
