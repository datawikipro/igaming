#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Automated CapSolver Poller & Instagram Automation Runner
Waits for CapSolver account balance activation, then autonomously launches
Instagram registration/login with CapSolver AI challenge resolution and IMAP OTP.
"""

import asyncio
import logging
import os
import subprocess
import sys
import time
import requests

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [capsolver-wait] %(message)s"
)
logger = logging.getLogger("CapSolverWatcher")

CAPSOLVER_KEY = "CAP-8A23FEECCC1A89B1B316D3EB036857865346F35405A6209B05F9E4DCAB430A3E"


def check_balance() -> tuple[bool, float, str]:
    try:
        resp = requests.post(
            "https://api.capsolver.com/getBalance",
            json={"clientKey": CAPSOLVER_KEY},
            timeout=10
        ).json()

        if resp.get("errorId", 0) == 0:
            balance = float(resp.get("balance", 0.0))
            return True, balance, "OK"
        else:
            desc = resp.get("errorDescription", "")
            return False, 0.0, desc
    except Exception as e:
        return False, 0.0, str(e)


def main():
    logger.info("==================================================================")
    logger.info("Watching CapSolver balance activation...")
    logger.info(f"Client Key: {CAPSOLVER_KEY[:15]}...{CAPSOLVER_KEY[-6:]}")
    logger.info("==================================================================")

    max_checks = 60  # Wait up to 15 minutes (15s interval)
    for attempt in range(1, max_checks + 1):
        ok, balance, msg = check_balance()
        if ok and balance > 0:
            logger.info("==================================================================")
            logger.info(f"🎉 CAPSOLVER АКТИВИРОВАН! Баланс: ${balance:.2f}")
            logger.info("Запускаю автоматическую регистрацию Instagram с CapSolver...")
            logger.info("==================================================================")

            # Set environment variable
            env = os.environ.copy()
            env["CAPSOLVER_API_KEY"] = CAPSOLVER_KEY

            script_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "register_instagram.py")
            cmd = [sys.executable, script_path]
            res = subprocess.run(cmd, env=env)
            logger.info(f"Instagram registration process finished with return code {res.returncode}")
            return res.returncode
        else:
            logger.info(f"[Попытка {attempt}/{max_checks}] Баланс еще не зачислен ({msg}). Ожидаю 15 сек...")
            time.sleep(15)

    logger.warning("Время ожидания пополнения истекло. Запустите скрипт позже, когда средства поступят.")
    return 1


if __name__ == "__main__":
    sys.exit(main())
