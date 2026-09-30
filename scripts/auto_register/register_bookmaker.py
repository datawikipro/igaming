#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — CLI Runner: Bookmaker Automated Registration
Task #55 [auto-reg]

Usage:
  python3 scripts/auto_register/register_bookmaker.py --bookmaker winline --locale us
  python3 scripts/auto_register/register_bookmaker.py --all --locale us --dry-run
"""

import argparse
import json
import logging
import os
import sys
from pathlib import Path

# Add smm-agent to python path
repo_root = Path(__file__).resolve().parent.parent.parent
sys.path.insert(0, str(repo_root / "smm-agent"))

from auto_register_engine import AutoRegistrationEngine
from persona_factory import PersonaFactory

logger = logging.getLogger("RegisterBookmakerCLI")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")


def main():
    parser = argparse.ArgumentParser(description="Automated Sportsbook Registration CLI")
    parser.add_argument("--bookmaker", "-b", default="winline", help="Target bookmaker (winline, fonbet, pinnacle, etc.)")
    parser.add_argument("--all", action="store_true", help="Register across all supported sportsbooks")
    parser.add_argument("--locale", "-l", default="us", choices=["us", "ru", "eu"], help="Jurisdiction locale")
    parser.add_argument("--dry-run", action="store_true", help="Simulate without network calls")
    parser.add_argument("--mock-2fa", action="store_true", default=True, help="Use mock 2FA gateway if live is unreachable")
    parser.add_argument("--proxy", default=os.getenv("US_PROXY", "http://100.83.113.50:3128"), help="US Proxy URL")
    args = parser.parse_args()

    engine = AutoRegistrationEngine(
        proxy_server=args.proxy,
        mock_mode=args.dry_run or args.mock_2fa
    )

    targets = AutoRegistrationEngine.SUPPORTED_BOOKMAKERS if args.all else [args.bookmaker.lower()]

    logger.info("Starting automated registration for %d sportsbook(s)...", len(targets))
    results = []

    for bk in targets:
        try:
            persona = PersonaFactory.create_bettor_persona(locale=args.locale)
            logger.info("Registering [%s] with persona %s %s (DOB: %s)...",
                        bk, persona.first_name, persona.last_name, persona.date_of_birth)
            record = engine.register_bookmaker(
                bookmaker_id=bk,
                persona=persona,
                locale=args.locale,
                dry_run=args.dry_run
            )
            results.append(record.to_dict())
            logger.info("Successfully registered [%s]: Account ID = %s", bk, record.account_id)
        except Exception as e:
            logger.error("Failed to register [%s]: %s", bk, e)

    print(json.dumps(results, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
