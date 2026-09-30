#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — CLI Runner: Affiliate Partner Network Automated Registration
Task #55 [auto-reg] & Task #61 [affiliate-hub]

Usage:
  python3 scripts/auto_register/register_affiliate.py --network uffiliates --bookmaker winline
  python3 scripts/auto_register/register_affiliate.py --all --dry-run
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

logger = logging.getLogger("RegisterAffiliateCLI")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")


def main():
    parser = argparse.ArgumentParser(description="Automated Affiliate Partner Network Registration CLI")
    parser.add_argument("--network", "-n", default="uffiliates", help="Target affiliate network")
    parser.add_argument("--bookmaker", "-b", default="winline", help="Target bookmaker offer")
    parser.add_argument("--all", action="store_true", help="Register across all supported affiliate networks")
    parser.add_argument("--locale", "-l", default="ru", choices=["us", "ru", "eu"], help="Jurisdiction locale")
    parser.add_argument("--dry-run", action="store_true", help="Simulate without network calls")
    parser.add_argument("--proxy", default=os.getenv("US_PROXY", "http://100.83.113.50:3128"), help="US Proxy URL")
    args = parser.parse_args()

    engine = AutoRegistrationEngine(
        proxy_server=args.proxy,
        mock_mode=args.dry_run
    )

    networks_map = {
        "uffiliates": "winline",
        "fonbet_affiliates": "fonbet",
        "1xpartners": "1xbet",
        "betcity_affiliates": "betcity",
        "pinnacle_affiliates": "pinnacle",
        "melbet_affiliates": "melbet"
    }

    targets = networks_map.items() if args.all else [(args.network.lower(), args.bookmaker.lower())]

    logger.info("Starting automated registration for %d affiliate network(s)...", len(targets))
    results = []

    for net, bk in targets:
        try:
            persona = PersonaFactory.create_affiliate_persona(locale=args.locale)
            logger.info("Registering network [%s] for [%s] with webmaster %s %s (Wallet: %s)...",
                        net, bk, persona.first_name, persona.last_name, persona.wallet_address)
            record = engine.register_affiliate_network(
                network_name=net,
                bookmaker_id=bk,
                persona=persona,
                locale=args.locale,
                dry_run=args.dry_run
            )
            results.append(record.to_dict())
            logger.info("Successfully registered network [%s]: Tracking Link = %s", net, record.tracking_link)
        except Exception as e:
            logger.error("Failed to register network [%s]: %s", net, e)

    print(json.dumps(results, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
