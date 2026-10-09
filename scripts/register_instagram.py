#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Automated Instagram Account Registration via Playwright Firefox + IMAP 2FA
Registers Instagram profile on provided email and stores persistent browser profile.
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

# UTF-8 stdout configuration for Windows
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

# Ensure smm-agent is on sys.path
SMM_AGENT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "smm-agent"))
if SMM_AGENT_DIR not in sys.path:
    sys.path.insert(0, SMM_AGENT_DIR)

from playwright.async_api import async_playwright

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [instagram-reg] %(message)s"
)
logger = logging.getLogger("InstagramRegister")

DEFAULT_CAPSOLVER_KEY = "CAP-8A23FEECCC1A89B1B316D3EB036857865346F35405A6209B05F9E4DCAB430A3E"
DEFAULT_PROXY = "http://100.83.113.50:3128"
DEFAULT_PROFILE_DIR = r"c:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\profiles\instagram"
DEFAULT_STORAGE_PATH = r"c:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\profiles\instagram_storage.json"
DEFAULT_THREADS_STORAGE_PATH = r"c:\Users\chernousov_a\IdeaProjects\igaming\smm-agent\profiles\threads_storage.json"
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


def poll_imap_for_registration_code(
    email_user: str,
    email_pass: str,
    timeout_sec: int = 120,
    poll_interval: int = 3
) -> Optional[str]:
    """
    Polls Gmail via IMAP for incoming Instagram registration confirmation code.
    """
    logger.info(f"Starting IMAP poller for {email_user} registration code...")
    start_time = time.time()

    while time.time() - start_time < timeout_sec:
        try:
            mail = imaplib.IMAP4_SSL("imap.gmail.com", 993)
            mail.login(email_user, email_pass)
            mail.select("INBOX")

            status, messages = mail.search(None, "ALL")
            if status == "OK" and messages[0]:
                msg_ids = messages[0].split()
                # Check recent 5 messages
                for mid in reversed(msg_ids[-5:]):
                    mid_str = mid.decode()
                    res, data = mail.fetch(mid, "(RFC822)")
                    if res != "OK":
                        continue
                    raw_email = data[0][1]
                    msg = email.message_from_bytes(raw_email)

                    from_hdr = decode_str(msg.get("From", "")).lower()
                    subject = decode_str(msg.get("Subject", ""))
                    
                    logger.debug(f"Email {mid_str}: From={from_hdr} | Subj={subject}")

                    is_insta = any(k in from_hdr for k in ["instagram", "meta", "security"])
                    is_subj = any(k in subject.lower() for k in ["code", "código", "confir", "instagram", "inscri"])

                    if is_insta or is_subj:
                        # Check code in subject
                        match = re.search(r"\b(\d{6})\b", subject)
                        if match:
                            code = match.group(1)
                            logger.info(f"--> Found 6-digit confirmation code in SUBJECT [{subject}]: {code}")
                            mail.logout()
                            return code

                        # Check body
                        body = get_body_text(msg).replace("&nbsp;", " ")
                        match_spaced = re.search(r"\b(\d{3})\s+(\d{3})\b", body)
                        if match_spaced:
                            code = match_spaced.group(1) + match_spaced.group(2)
                            logger.info(f"--> Found spaced 6-digit code in BODY: {code}")
                            mail.logout()
                            return code

                        match_body = re.search(r"\b(\d{6})\b", body)
                        if match_body:
                            code = match_body.group(1)
                            logger.info(f"--> Found 6-digit code in BODY: {code}")
                            mail.logout()
                            return code

            mail.logout()
        except Exception as e:
            logger.warning(f"IMAP poll error: {e}")

        elapsed = int(time.time() - start_time)
        logger.info(f"Waiting for confirmation email... ({elapsed}s / {timeout_sec}s)")
        time.sleep(poll_interval)

    logger.warning("Timeout reached waiting for confirmation code via IMAP.")
    return None


async def human_type(locator, text: str, min_delay: float = 0.05, max_delay: float = 0.12):
    await locator.click()
    await asyncio.sleep(random.uniform(0.15, 0.3))
    for char in text:
        await locator.type(char, delay=random.uniform(min_delay, max_delay) * 1000)
    await asyncio.sleep(random.uniform(0.2, 0.4))


async def run_instagram_registration(
    email_address: str,
    password: str,
    fullname: str,
    username: str,
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
    logger.info("Starting Automated Instagram Registration")
    logger.info(f"Email: {email_address}")
    logger.info(f"Username: {username}")
    logger.info(f"Full Name: {fullname}")
    logger.info(f"Proxy: {proxy_url}")
    logger.info("==================================================================")

    proxy_config = {"server": proxy_url} if proxy_url else None

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

        context = await p.firefox.launch_persistent_context(**launch_kwargs)
        pages = context.pages
        page = pages[0] if pages else await context.new_page()

        try:
            logger.info("Step 1: Navigating to https://www.instagram.com/accounts/emailsignup/...")
            await page.goto("https://www.instagram.com/accounts/emailsignup/", timeout=40000, wait_until="domcontentloaded")
            await asyncio.sleep(random.uniform(3.0, 5.0))
            await page.screenshot(path=os.path.join(SCRATCH_DIR, "reg_step1_landing.png"))

            # Fill inputs
            logger.info("Step 2: Locating input fields...")
            inputs = page.locator("input")
            total_inputs = await inputs.count()
            logger.info(f"Found {total_inputs} inputs on page")

            # Input 0: Email
            logger.info(f"Filling Email: {email_address}...")
            email_field = inputs.nth(0)
            await human_type(email_field, email_address)

            # Input 1: Password
            logger.info("Filling Password...")
            pass_field = page.locator("input[type='password']").first
            await human_type(pass_field, password)

            # Birthday selection: Jour, Mois, Année
            logger.info("Step 3: Selecting Birthday (15 mai 1995)...")
            try:
                # Jour
                jour_btn = page.locator("div:has-text('Jour')").last
                await jour_btn.click()
                await asyncio.sleep(0.5)
                # Select day '15'
                day_opt = page.locator("div[role='button']:has-text('15'), span:has-text('15'), div:text-is('15')").last
                await day_opt.click()
                await asyncio.sleep(0.5)

                # Mois
                mois_btn = page.locator("div:has-text('Mois')").last
                await mois_btn.click()
                await asyncio.sleep(0.5)
                # Select 'mai' (May)
                mois_opt = page.locator("div:has-text('mai'), span:has-text('mai'), div:text-is('mai')").last
                await mois_opt.click()
                await asyncio.sleep(0.5)

                # Année
                annee_btn = page.locator("div:has-text('Année')").last
                await annee_btn.click()
                await asyncio.sleep(0.5)
                # Select '1995'
                annee_opt = page.locator("div:has-text('1995'), span:has-text('1995'), div:text-is('1995')").last
                await annee_opt.click()
                await asyncio.sleep(0.5)
                logger.info("Birthday selected successfully!")
            except Exception as e:
                logger.warning(f"Note on birthday selection: {e}")

            # Input 2: Full Name
            logger.info(f"Filling Full Name: {fullname}...")
            fullname_field = inputs.nth(2)
            await human_type(fullname_field, fullname)

            # Input 3: Username
            logger.info(f"Filling Username: {username}...")
            username_field = inputs.nth(3)
            await human_type(username_field, username)

            await asyncio.sleep(1.0)
            await page.screenshot(path=os.path.join(SCRATCH_DIR, "reg_step2_filled.png"))
            logger.info("Saved filled form screenshot to scratch/reg_step2_filled.png")

            # Step 4: Click Envoyer (Submit)
            logger.info("Step 4: Clicking 'Envoyer' submit button...")
            submit_btn = page.locator("button:has-text('Envoyer'), div[role='button']:has-text('Envoyer')").first
            await submit_btn.click()

            await asyncio.sleep(random.uniform(5.0, 7.0))
            # Check for "Aidez-nous à confirmer que c'est bien vous" modal with 'Suivant'
            suivant_btn = page.locator("button:has-text('Suivant'), div[role='button']:has-text('Suivant')").first
            try:
                if await suivant_btn.is_visible(timeout=8000):
                    logger.info("Modal 'Aidez-nous à confirmer que c\'est bien vous' detected! Checking reCAPTCHA...")
                    capsolver_key = os.getenv("CAPSOLVER_API_KEY", DEFAULT_CAPSOLVER_KEY)
                    try:
                        from captcha_solver import handle_challenge_flow
                        logger.info("Solving reCAPTCHA via CapSolver API...")
                        solved = await handle_challenge_flow(page, service_name="smm-bot-meta", challenge_type="reCAPTCHA v2", capsolver_key=capsolver_key)
                        logger.info(f"handle_challenge_flow returned: {solved}")
                        await asyncio.sleep(2.0)
                    except Exception as e:
                        logger.warning(f"CapSolver execution notice: {e}")

                    # Poll for Suivant button activation
                    for _ in range(12):
                        disabled = await suivant_btn.get_attribute("aria-disabled")
                        if disabled != "true":
                            logger.info("Suivant button is enabled! Clicking...")
                            await suivant_btn.click()
                            await asyncio.sleep(5.0)
                            break
                        await asyncio.sleep(1.0)

                    await page.screenshot(path=os.path.join(SCRATCH_DIR, "reg_step3b_after_suivant.png"))
            except Exception as e:
                logger.info(f"Suivant check: {e}")

            # Step 5: Check for confirmation code input or captcha
            logger.info("Step 5: Checking for confirmation code screen...")
            code_input_selectors = [
                "input[name='email_confirmation_code']",
                "input[name='confirmationCode']",
                "input[name='code']",
                "input[type='tel']",
                "input[placeholder*='code' i]",
                "input[placeholder*='Code']",
            ]
            code_field = None
            for wait_attempt in range(15):
                for sel in code_input_selectors:
                    loc = page.locator(sel).first
                    if await loc.is_visible(timeout=1000):
                        code_field = loc
                        logger.info(f"Found confirmation code input: {sel}")
                        break
                if code_field:
                    break
                current_inputs = page.locator("input")
                cnt = await current_inputs.count()
                if cnt == 1:
                    code_field = current_inputs.first
                    logger.info("Found single input on confirmation screen!")
                    break
                await asyncio.sleep(2.0)

            if code_field:
                logger.info("Polling IMAP for confirmation code from Instagram...")
                code = poll_imap_for_registration_code(
                    email_user=email_address,
                    email_pass=email_pass,
                    timeout_sec=180,
                    poll_interval=3
                )
                if code:
                    logger.info(f"Entering confirmation code: {code}...")
                    await human_type(code_field, code)
                    await asyncio.sleep(1.0)
                    confirm_btn = page.locator("button:has-text('Suivant'), button:has-text('Confirmer'), button:has-text('Envoyer'), div[role='button']:has-text('Suivant'), button[type='submit']").first
                    await confirm_btn.click()
                    await asyncio.sleep(7.0)
                    await page.screenshot(path=os.path.join(SCRATCH_DIR, "reg_step4_confirmed.png"))
                else:
                    logger.error("Failed to receive registration code via IMAP.")
                    return False

            # Check final URL and logged in status
            await asyncio.sleep(5.0)
            await page.screenshot(path=os.path.join(SCRATCH_DIR, "reg_final_state.png"))
            current_url = page.url
            logger.info(f"Registration finished with URL: {current_url}")

            await context.storage_state(path=storage_path)
            logger.info(f"Saved persistent storage state to {storage_path}")

            # Step 6: Verify Threads connection
            try:
                logger.info("Step 6: Navigating to https://www.threads.net/ for 1-click connect...")
                await page.goto("https://www.threads.net/", timeout=35000, wait_until="domcontentloaded")
                await asyncio.sleep(5.0)
                await page.screenshot(path=os.path.join(SCRATCH_DIR, "threads_landing.png"))

                # Check for connect/login button
                threads_login_btn = page.locator("div[role='button']:has-text('Continuer avec Instagram'), div[role='button']:has-text('Se connecter avec Instagram'), button:has-text('Continuer'), div:has-text('Se connecter')").first
                if await threads_login_btn.is_visible(timeout=5000):
                    logger.info("Clicking Connect with Instagram on Threads...")
                    await threads_login_btn.click()
                    await asyncio.sleep(6.0)

                await page.screenshot(path=os.path.join(SCRATCH_DIR, "threads_authenticated.png"))
                logger.info(f"Threads URL: {page.url}")
                await context.storage_state(path=DEFAULT_THREADS_STORAGE_PATH)
                logger.info(f"Saved persistent Threads storage state to {DEFAULT_THREADS_STORAGE_PATH}")
            except Exception as e:
                logger.warning(f"Threads verification notice: {e}")

            return True

        except Exception as e:
            logger.error(f"Error during Instagram registration: {e}", exc_info=True)
            await page.screenshot(path=os.path.join(SCRATCH_DIR, "reg_exception.png"))
            return False
        finally:
            await context.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--email", default="www.smartbet.guru@gmail.com")
    parser.add_argument("--email-pass", default="ujsrxyyvssfjfxpb")
    parser.add_argument("--username", default="fr.smartbet.guru")
    parser.add_argument("--fullname", default="SmartBet Guru")
    parser.add_argument("--password", default="Racoiaws12!@")
    parser.add_argument("--proxy", default=DEFAULT_PROXY)
    parser.add_argument("--profile-dir", default=DEFAULT_PROFILE_DIR)
    parser.add_argument("--storage-path", default=DEFAULT_STORAGE_PATH)
    parser.add_argument("--headless", action="store_true", default=True)

    args = parser.parse_args()

    success = asyncio.run(run_instagram_registration(
        email_address=args.email,
        email_pass=args.email_pass,
        username=args.username,
        fullname=args.fullname,
        password=args.password,
        proxy_url=args.proxy,
        profile_dir=args.profile_dir,
        storage_path=args.storage_path,
        headless=args.headless,
    ))

    sys.exit(0 if success else 1)
