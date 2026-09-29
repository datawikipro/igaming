#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — SMM & Stealth Browser Agents
Captcha Solver & Fallback noVNC Orchestrator (Task #76 [stealth-novnc])

Architecture:
1. Tier 1: Passive Avoidance (Camoufox Gecko, persistent profiles, warm cache, human Bezier curves).
2. Tier 2: Automated Solvers:
   - CapSolver API (Turnstile, ReCaptcha v2/v3, HCaptcha, GeeTest).
   - OpenCV Slider Puzzle Matcher (Canny edge + template correlation + humanized drag trajectory).
3. Tier 3: Zero-Install noVNC Fallback:
   - Alert sent to Telegram with screenshot & direct link to https://smartbet.guru/admin/browsers/{service}.
   - Operator solves challenge directly in web console without AnyDesk.
"""

import asyncio
import base64
import json
import logging
import math
import os
import random
import time
from typing import Dict, List, Optional, Tuple

import requests

logger = logging.getLogger("CaptchaSolver")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")

CAPSOLVER_API_URL = "https://api.capsolver.com"
CAPSOLVER_API_KEY = os.getenv("CAPSOLVER_API_KEY", "")

TELEGRAM_BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "8391441164:AAHJmCpBPP--7OfqW0OujtkX7UryJGfe6Yo")
TELEGRAM_ADMIN_CHAT_ID = os.getenv("TELEGRAM_ADMIN_CHAT_ID", "-1002244889900")
SMARTBET_ADMIN_URL = os.getenv("SMARTBET_ADMIN_URL", "https://smartbet.guru/admin/browsers")


# ==============================================================================
# 1. CapSolver API Client
# ==============================================================================

class CapSolverClient:
    """
    Client for automated challenge resolution via CapSolver API.
    Supports Turnstile, ReCaptcha v2/v3, hCaptcha, and GeeTest.
    """

    def __init__(self, api_key: Optional[str] = None):
        self.api_key = api_key or CAPSOLVER_API_KEY
        self.session = requests.Session()

    def solve_turnstile(self, website_url: str, website_key: str, action: Optional[str] = None) -> Optional[str]:
        """Solves Cloudflare Turnstile challenge and returns token."""
        task_payload = {
            "type": "AntiTurnstileTaskProxyLess",
            "websiteURL": website_url,
            "websiteKey": website_key,
        }
        if action:
            task_payload["action"] = action

        return self._create_and_wait_task(task_payload)

    def solve_recaptcha_v2(self, website_url: str, website_key: str, is_invisible: bool = False) -> Optional[str]:
        """Solves Google reCAPTCHA v2 challenge."""
        task_payload = {
            "type": "ReCaptchaV2TaskProxyLess",
            "websiteURL": website_url,
            "websiteKey": website_key,
            "isInvisible": is_invisible
        }
        return self._create_and_wait_task(task_payload)

    def solve_hcaptcha(self, website_url: str, website_key: str) -> Optional[str]:
        """Solves hCaptcha challenge."""
        task_payload = {
            "type": "HCaptchaTaskProxyLess",
            "websiteURL": website_url,
            "websiteKey": website_key
        }
        return self._create_and_wait_task(task_payload)

    def _create_and_wait_task(self, task_data: Dict) -> Optional[str]:
        if not self.api_key:
            logger.warning("CapSolver API key not configured; skipping automated solve.")
            return None

        try:
            create_resp = self.session.post(
                f"{CAPSOLVER_API_URL}/createTask",
                json={"clientKey": self.api_key, "task": task_data},
                timeout=15
            ).json()

            if create_resp.get("errorId", 0) != 0:
                logger.error(f"CapSolver createTask error: {create_resp.get('errorDescription')}")
                return None

            task_id = create_resp.get("taskId")
            logger.info(f"CapSolver task created: {task_id}, awaiting solution...")

            # Poll for solution up to 45 seconds
            start_time = time.time()
            while time.time() - start_time < 45:
                time.sleep(2.0)
                res_resp = self.session.post(
                    f"{CAPSOLVER_API_URL}/getTaskResult",
                    json={"clientKey": self.api_key, "taskId": task_id},
                    timeout=15
                ).json()

                status = res_resp.get("status")
                if status == "ready":
                    solution = res_resp.get("solution", {})
                    token = solution.get("token") or solution.get("gRecaptchaResponse")
                    logger.info(f"CapSolver solved task {task_id} in {time.time() - start_time:.1f}s")
                    return token
                elif status == "failed":
                    logger.error(f"CapSolver task {task_id} failed: {res_resp.get('errorDescription')}")
                    return None

            logger.warning(f"CapSolver task {task_id} timed out after 45s.")
            return None

        except Exception as e:
            logger.error(f"CapSolver request failed: {e}")
            return None


# ==============================================================================
# 2. OpenCV Slider Puzzle Solver (Template Matching & Bezier Drag Curve)
# ==============================================================================

class SliderPuzzleSolver:
    """
    Solves sliding puzzle / jigsaw captchas using computer vision (OpenCV)
    and generates human-like mouse kinematics with Bezier curves.
    """

    @staticmethod
    def find_slider_offset(background_bytes: bytes, piece_bytes: bytes) -> Optional[int]:
        """
        Calculates the horizontal pixel offset where the puzzle piece fits.
        """
        try:
            import cv2
            import numpy as np

            # Decode images from raw bytes
            bg_arr = np.frombuffer(background_bytes, dtype=np.uint8)
            piece_arr = np.frombuffer(piece_bytes, dtype=np.uint8)

            bg_img = cv2.imdecode(bg_arr, cv2.IMREAD_COLOR)
            piece_img = cv2.imdecode(piece_arr, cv2.IMREAD_COLOR)

            if bg_img is None or piece_img is None:
                logger.error("Failed to decode puzzle images")
                return None

            # Convert to grayscale and apply edge detection
            bg_gray = cv2.cvtColor(bg_img, cv2.COLOR_BGR2GRAY)
            piece_gray = cv2.cvtColor(piece_img, cv2.COLOR_BGR2GRAY)

            bg_edges = cv2.Canny(bg_gray, 100, 200)
            piece_edges = cv2.Canny(piece_gray, 100, 200)

            # Template matching on edge maps
            res = cv2.matchTemplate(bg_edges, piece_edges, cv2.TM_CCOEFF_NORMED)
            _, max_val, _, max_loc = cv2.minMaxLoc(res)

            target_x = max_loc[0]
            logger.info(f"OpenCV Slider match found: X={target_x} (confidence={max_val:.2f})")
            return target_x

        except ImportError:
            logger.warning("OpenCV not installed in current environment; falling back to heuristic offset.")
            return None
        except Exception as e:
            logger.error(f"Slider puzzle detection failed: {e}")
            return None

    @staticmethod
    def generate_human_drag_track(distance: int) -> List[Tuple[int, int, float]]:
        """
        Generates realistic mouse movement steps (dx, dy, sleep_sec)
        modeling human acceleration, jitter, deceleration, and slight overshoot.
        """
        track: List[Tuple[int, int, float]] = []
        current = 0
        mid = distance * 4 / 5
        v = 0
        t = 0.2

        # Slight overshoot to emulate natural human motor control
        target_distance = distance + random.randint(2, 6)

        while current < target_distance:
            if current < mid:
                a = random.uniform(2.0, 3.5)
            else:
                a = -random.uniform(2.5, 4.0)

            v0 = v
            v = v0 + a * t
            move = int(v0 * t + 0.5 * a * (t ** 2))
            if move <= 0:
                move = 1

            current += move
            # Add slight Y micro-jitter (1px up/down)
            jitter_y = random.choice([-1, 0, 0, 1])
            sleep_time = random.uniform(0.010, 0.025)

            track.append((move, jitter_y, sleep_time))

        # Micro-correction backward to fix overshoot
        overshoot = current - distance
        for _ in range(overshoot):
            track.append((-1, 0, random.uniform(0.030, 0.060)))

        return track


# ==============================================================================
# 3. Fallback noVNC Telegram Notifier
# ==============================================================================

class TelegramFallbackNotifier:
    """
    Sends emergency challenge notification to Telegram with direct 1-click link
    to https://smartbet.guru/admin/browsers/{service} when automated solvers fail.
    """

    @staticmethod
    def notify_operator(
        service_name: str,
        challenge_type: str,
        screenshot_bytes: Optional[bytes] = None,
        timeout_seconds: int = 90
    ) -> bool:
        if not TELEGRAM_BOT_TOKEN or not TELEGRAM_ADMIN_CHAT_ID:
            logger.warning("Telegram Bot Token or Admin Chat ID not set; cannot send alert.")
            return False

        web_vnc_url = f"{SMARTBET_ADMIN_URL}?pod={service_name}"
        caption = (
            f"🚨 <b>Внимание: Требуется Ручной Обход Капчи!</b>\n\n"
            f"🤖 <b>Сервис:</b> <code>{service_name}</code>\n"
            f"🛡️ <b>Тип Защиты:</b> <code>{challenge_type}</code>\n"
            f"⏱️ <b>Окно решения:</b> {timeout_seconds} сек до таймаута\n\n"
            f"👉 <b>Открыть noVNC Консоль:</b> <a href='{web_vnc_url}'>Решить в браузере (без AnyDesk)</a>"
        )

        inline_keyboard = {
            "inline_keyboard": [
                [{"text": "🖥️ Открыть noVNC в Веб-Админке", "url": web_vnc_url}]
            ]
        }

        try:
            if screenshot_bytes:
                # Send photo with button
                files = {"photo": ("challenge.png", screenshot_bytes, "image/png")}
                data = {
                    "chat_id": TELEGRAM_ADMIN_CHAT_ID,
                    "caption": caption,
                    "parse_mode": "HTML",
                    "reply_markup": json.dumps(inline_keyboard)
                }
                resp = requests.post(
                    f"https://api.telegram.org/bot{TELEGRAM_BOT_TOKEN}/sendPhoto",
                    data=data,
                    files=files,
                    timeout=15
                )
            else:
                # Send text message
                payload = {
                    "chat_id": TELEGRAM_ADMIN_CHAT_ID,
                    "text": caption,
                    "parse_mode": "HTML",
                    "reply_markup": inline_keyboard
                }
                resp = requests.post(
                    f"https://api.telegram.org/bot{TELEGRAM_BOT_TOKEN}/sendMessage",
                    json=payload,
                    timeout=15
                )

            if resp.status_code == 200:
                logger.info(f"Telegram operator alert sent successfully for {service_name}.")
                return True
            else:
                logger.error(f"Telegram notification returned HTTP {resp.status_code}: {resp.text}")
                return False

        except Exception as e:
            logger.error(f"Failed to dispatch Telegram operator alert: {e}")
            return False


# ==============================================================================
# 4. Master Orchestrator Function
# ==============================================================================

async def handle_challenge_flow(
    page,
    service_name: str,
    challenge_type: str = "Cloudflare Turnstile",
    capsolver_key: Optional[str] = None
) -> bool:
    """
    Full 3-tier challenge handling flow:
    1. Attempts CapSolver API.
    2. If failed and is slider: attempts OpenCV slider drag.
    3. If failed: takes screenshot, sends Telegram alert, waits for human operator via noVNC.
    """
    logger.info(f"Starting challenge resolution for '{service_name}' ({challenge_type})...")
    client = CapSolverClient(api_key=capsolver_key)

    # 1. CapSolver resolution attempt
    if "turnstile" in challenge_type.lower():
        token = client.solve_turnstile(website_url=page.url, website_key="0x4AAAAAA")
        if token:
            await page.evaluate(f"token => window.turnstile && window.turnstile.solve && window.turnstile.solve(token)", token)
            logger.info("Turnstile solved via CapSolver token injection.")
            return True

    # 2. Slider puzzle attempt
    if "slider" in challenge_type.lower() or "geetest" in challenge_type.lower():
        try:
            bg_elem = await page.query_selector(".geetest_canvas_bg")
            piece_elem = await page.query_selector(".geetest_canvas_slice")
            if bg_elem and piece_elem:
                bg_bytes = await bg_elem.screenshot()
                piece_bytes = await piece_elem.screenshot()
                offset = SliderPuzzleSolver.find_slider_offset(bg_bytes, piece_bytes)
                if offset:
                    track = SliderPuzzleSolver.generate_human_drag_track(offset)
                    slider_handle = await page.query_selector(".geetest_slider_button")
                    if slider_handle:
                        box = await slider_handle.bounding_box()
                        await page.mouse.move(box["x"] + 10, box["y"] + 10)
                        await page.mouse.down()
                        for dx, dy, delay in track:
                            await page.mouse.move(page.mouse._x + dx, page.mouse._y + dy)
                            await asyncio.sleep(delay)
                        await page.mouse.up()
                        logger.info("Slider dragged with OpenCV trajectory.")
                        await asyncio.sleep(2.0)
                        return True
        except Exception as e:
            logger.warning(f"Slider drag failed: {e}")

    # 3. Fallback Tier: Operator noVNC alert
    logger.warning(f"Automated resolution unsuccessful for {service_name}. Triggering Tier 3 (noVNC operator alert)...")
    try:
        screenshot_data = await page.screenshot(type="png")
    except Exception:
        screenshot_data = None

    TelegramFallbackNotifier.notify_operator(
        service_name=service_name,
        challenge_type=challenge_type,
        screenshot_bytes=screenshot_data,
        timeout_seconds=90
    )

    # Operator grace period (poll page state to see if challenge is completed manually)
    for _ in range(45):
        await asyncio.sleep(2.0)
        # Check if challenge element disappeared or URL changed
        if "challenge" not in page.url and not (await page.query_selector("iframe[src*='challenges']")):
            logger.info("Challenge resolved manually by operator in noVNC web console!")
            return True

    logger.error(f"Challenge resolution timed out for {service_name}.")
    return False


if __name__ == "__main__":
    print("=== SmartBet.guru Stealth Captcha Solver & noVNC Fallback Module Initialized ===")
    print(f"CapSolver Client Key Configured: {'YES' if CAPSOLVER_API_KEY else 'NO'}")
    print(f"Telegram Notifier Bot Token: {'YES' if TELEGRAM_BOT_TOKEN else 'NO'}")
    print(f"Admin Console URL: {SMARTBET_ADMIN_URL}")
