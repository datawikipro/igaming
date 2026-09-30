#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — SMM & Stealth Browser Agent Runner
Task #54 [smm-runner] & Rule 9 (AGENTS.md)

CLI / Entrypoint for running browser agents with persistent profile and cache warmup.
"""

import argparse
import asyncio
import logging
import os
import sys

from browser_manager import BrowserConfig, StealthBrowserSession
from cache_warmup import CacheWarmupManager

logger = logging.getLogger("SMMRunner")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")


async def run_warmup_pipeline(config: BrowserConfig, duration_seconds: int = 120) -> None:
    """Executes persistent context launch, cache warmup, and profile persistence."""
    logger.info(f"=== Starting SMM Agent Warmup Pipeline for Account [{config.account_id}] ===")
    logger.info(f"Effective UserDataDir: {config.get_effective_user_data_dir()}")
    logger.info(f"US Proxy: {config.proxy_server}")

    async with StealthBrowserSession(config) as session:
        warmup_mgr = CacheWarmupManager()
        result = await warmup_mgr.warmup(
            page=session.page,
            human=session.human,
            duration_seconds=duration_seconds,
        )
        logger.info(f"Warmup pipeline completed: {result}")


async def run_stealth_check(config: BrowserConfig) -> None:
    """Runs browser against detection endpoints and logs status."""
    logger.info(f"=== Testing Stealth / Bot Detection Flags for [{config.account_id}] ===")

    async with StealthBrowserSession(config) as session:
        page = session.page
        logger.info("Navigating to detection inspection endpoint...")
        try:
            await page.goto("https://httpbin.org/headers", timeout=20000)
            headers_text = await page.inner_text("body")
            logger.info(f"Outgoing HTTP headers received by remote:\n{headers_text}")
        except Exception as e:
            logger.warning(f"Could not reach httpbin: {e}")

        # Check navigator.webdriver property
        is_webdriver = await page.evaluate("() => navigator.webdriver")
        logger.info(f"navigator.webdriver evaluation: {is_webdriver} (Should be False/Undefined)")
        if is_webdriver:
            logger.error("ALERT: navigator.webdriver is TRUE! Stealth flag failed.")
        else:
            logger.info("SUCCESS: navigator.webdriver is clean (False/Undefined).")


def main() -> None:
    parser = argparse.ArgumentParser(description="SmartBet SMM Stealth Runner")
    parser.add_argument("--account-id", default=os.getenv("ACCOUNT_ID", "default_persona"), help="Account identifier")
    parser.add_argument("--mode", default=os.getenv("SMM_MODE", "warmup"), choices=["warmup", "check", "idle", "auto_reg"], help="Execution mode")
    parser.add_argument("--target-type", default=os.getenv("AUTO_REG_TARGET_TYPE", "bookmaker"), choices=["bookmaker", "affiliate", "forum"], help="Target registration type")
    parser.add_argument("--target-name", default=os.getenv("AUTO_REG_TARGET_NAME", "winline"), help="Target service name")
    parser.add_argument("--locale", default=os.getenv("LOCALE", "us"), choices=["us", "ru", "eu"], help="Jurisdiction locale")
    parser.add_argument("--duration", type=int, default=int(os.getenv("WARMUP_DURATION", "120")), help="Warmup duration in seconds")
    parser.add_argument("--headless", action="store_true", default=os.getenv("HEADLESS", "false").lower() == "true")
    parser.add_argument("--proxy", default=os.getenv("US_PROXY", os.getenv("HTTP_PROXY")), help="Proxy server")
    parser.add_argument("--dry-run", action="store_true", default=os.getenv("DRY_RUN", "false").lower() == "true")
    args = parser.parse_args()

    config = BrowserConfig(
        account_id=args.account_id,
        headless=args.headless,
        proxy_server=args.proxy,
    )

    if args.mode == "warmup":
        asyncio.run(run_warmup_pipeline(config, duration_seconds=args.duration))
    elif args.mode == "check":
        asyncio.run(run_stealth_check(config))
    elif args.mode == "auto_reg":
        from auto_register_engine import AutoRegistrationEngine
        engine = AutoRegistrationEngine(proxy_server=args.proxy, mock_mode=args.dry_run)
        if args.target_type == "bookmaker":
            record = engine.register_bookmaker(args.target_name, locale=args.locale, dry_run=args.dry_run)
        elif args.target_type == "affiliate":
            record = engine.register_affiliate_network(args.target_name, "winline", locale=args.locale, dry_run=args.dry_run)
        else:
            record = engine.register_forum(args.target_name, locale=args.locale, dry_run=args.dry_run)
        logger.info(f"Auto-registration completed: {record.account_id} status={record.status}")
    elif args.mode == "idle":
        logger.info("SMM Runner in idle mode. Keeping container alive for noVNC / interactive use.")
        import time
        while True:
            time.sleep(3600)


if __name__ == "__main__":
    main()
