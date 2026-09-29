#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — SMM & Stealth Browser Agents
Browser Manager: Firefox / Camoufox Persistent Context, Redis Profile Sync & Human Kinematics
Task #54 [smm-runner] & Rule 9 (AGENTS.md)

Strict Guarantees:
1. NO INCOGNITO: Never use browser.new_context(). Always launch_persistent_context().
2. PERSISTENT STORAGE: Archive cookies.sqlite, storage/default (IndexedDB, LocalStorage),
   and cache in Redis key `smm:profile:<account_id>`.
3. HUMAN MOTOR CONTROL: Cubic Bezier curves, realistic micro-jitter, randomized delays.
4. US PROXY: Cluster proxy (http://100.83.113.50:3128 -> outline-us 100.66.190.4).
"""

import asyncio
from dataclasses import dataclass, field
import io
import logging
import math
import os
import random
import shutil
import tarfile
import time
from typing import Any, Dict, List, Optional, Tuple

logger = logging.getLogger("BrowserManager")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")

# Default US Proxy via ru-proxy transparent cluster router (Rule 6)
DEFAULT_US_PROXY = os.getenv("US_PROXY", "http://100.83.113.50:3128")
DEFAULT_REDIS_URL = os.getenv("REDIS_URL", "redis://igaming-redis:6379/0")
DEFAULT_USER_DATA_DIR_BASE = os.getenv("SMM_DATA_DIR", "/data/smm_profiles")


# ==============================================================================
# 1. Configuration Dataclass
# ==============================================================================

@dataclass
class BrowserConfig:
    account_id: str
    user_data_dir: Optional[str] = None
    redis_url: str = DEFAULT_REDIS_URL
    proxy_server: Optional[str] = DEFAULT_US_PROXY
    headless: bool = False
    viewport_width: int = 1280
    viewport_height: int = 800
    locale: str = "en-US"
    timezone_id: str = "America/New_York"
    user_agent: Optional[str] = None
    use_camoufox: bool = False
    extra_firefox_prefs: Dict[str, Any] = field(default_factory=dict)
    redis_profile_ttl: int = 30 * 86400  # 30 days

    def get_effective_user_data_dir(self) -> str:
        if self.user_data_dir:
            return self.user_data_dir
        return os.path.join(DEFAULT_USER_DATA_DIR_BASE, self.account_id)


# ==============================================================================
# 2. Cubic Bezier Curves & Human Kinematics
# ==============================================================================

def generate_cubic_bezier_trajectory(
    p0: Tuple[float, float],
    p3: Tuple[float, float],
    num_points: int = 25,
    deviation_scale: float = 0.2
) -> List[Tuple[float, float]]:
    """
    Generates a realistic 2D mouse trajectory between p0 and p3 using a cubic Bezier curve:
    B(t) = (1-t)^3 * P0 + 3*(1-t)^2 * t * P1 + 3*(1-t) * t^2 * P2 + t^3 * P3

    Incorporates:
    - Randomized control points P1, P2 with lateral deviation proportional to distance
    - Non-linear smoothstep / cosine easing for human acceleration/deceleration
    - Subtle micro-jitter (tremor) along the path
    """
    x0, y0 = p0
    x3, y3 = p3
    dx = x3 - x0
    dy = y3 - y0
    distance = math.hypot(dx, dy)

    if distance < 1.0 or num_points <= 2:
        return [p0, p3]

    # Calculate perpendicular normal vector for realistic curvature
    nx = -dy / distance
    ny = dx / distance

    # Random deviation magnitude
    dev1 = (random.random() - 0.5) * distance * deviation_scale
    dev2 = (random.random() - 0.5) * distance * deviation_scale

    # Two intermediate control points
    p1 = (
        x0 + dx * 0.33 + nx * dev1,
        y0 + dy * 0.33 + ny * dev1,
    )
    p2 = (
        x0 + dx * 0.66 + nx * dev2,
        y0 + dy * 0.66 + ny * dev2,
    )

    trajectory: List[Tuple[float, float]] = []

    for i in range(num_points):
        # Linear parameter t from 0.0 to 1.0
        t_linear = i / (num_points - 1)

        # Smoothstep / Cosine ease-in-out profile
        # Slow start, fast middle, slow precision approach at the end
        t_eased = (1.0 - math.cos(math.pi * t_linear)) / 2.0

        inv_t = 1.0 - t_eased

        # Cubic Bezier formula
        bx = (
            (inv_t ** 3) * p0[0]
            + 3 * (inv_t ** 2) * t_eased * p1[0]
            + 3 * inv_t * (t_eased ** 2) * p2[0]
            + (t_eased ** 3) * p3[0]
        )
        by = (
            (inv_t ** 3) * p0[1]
            + 3 * (inv_t ** 2) * t_eased * p1[1]
            + 3 * inv_t * (t_eased ** 2) * p2[1]
            + (t_eased ** 3) * p3[1]
        )

        # Add human micro-tremor (except at exact endpoints)
        if 0 < i < num_points - 1:
            jitter_x = (random.random() - 0.5) * 0.8
            jitter_y = (random.random() - 0.5) * 0.8
            bx += jitter_x
            by += jitter_y

        trajectory.append((round(bx, 1), round(by, 1)))

    return trajectory


class HumanInteractionHelper:
    """Provides human-like mouse, keyboard, and scroll interactions for Playwright Page."""

    def __init__(self, page: Any):
        self.page = page
        self.current_x: float = 100.0
        self.current_y: float = 100.0

    async def mouse_move(
        self,
        target_x: float,
        target_y: float,
        steps: int = 25,
        min_sleep: float = 0.005,
        max_sleep: float = 0.015,
    ) -> None:
        """Moves mouse pointer along a cubic Bezier curve to (target_x, target_y)."""
        trajectory = generate_cubic_bezier_trajectory(
            (self.current_x, self.current_y),
            (target_x, target_y),
            num_points=steps,
        )

        for x, y in trajectory:
            await self.page.mouse.move(x, y)
            self.current_x = x
            self.current_y = y
            await asyncio.sleep(random.uniform(min_sleep, max_sleep))

    async def click(
        self,
        selector: Optional[str] = None,
        coords: Optional[Tuple[float, float]] = None,
    ) -> None:
        """
        Moves to element or coordinates via Bezier curve, hesitates naturally,
        holds mouse down for realistic duration (50-120ms), and releases.
        """
        target_x: float = 0.0
        target_y: float = 0.0

        if selector:
            element = await self.page.wait_for_selector(selector, state="visible", timeout=10000)
            if not element:
                raise ValueError(f"Element not found for selector: {selector}")
            box = await element.bounding_box()
            if not box:
                raise ValueError(f"Bounding box not found for selector: {selector}")

            # Pick randomized click point inside target (25% to 75% offset)
            target_x = box["x"] + box["width"] * random.uniform(0.25, 0.75)
            target_y = box["y"] + box["height"] * random.uniform(0.25, 0.75)
        elif coords:
            target_x, target_y = coords
        else:
            raise ValueError("Either selector or coords must be provided")

        # Move to target
        await self.mouse_move(target_x, target_y)

        # Hesitation pause before click
        await asyncio.sleep(random.uniform(0.10, 0.28))

        # Mouse down, hold, mouse up
        await self.page.mouse.down()
        await asyncio.sleep(random.uniform(0.05, 0.12))
        await self.page.mouse.up()

    async def type_text(
        self,
        selector: str,
        text: str,
        min_delay_ms: int = 50,
        max_delay_ms: int = 160,
        typo_rate: float = 0.02,
    ) -> None:
        """
        Types text with realistic human cadence, variable delays,
        and occasional human typos with immediate Backspace correction.
        """
        await self.click(selector=selector)
        await asyncio.sleep(random.uniform(0.15, 0.35))

        for char in text:
            # Emulate occasional typo
            if typo_rate > 0 and random.random() < typo_rate and char.isalpha():
                typo_char = random.choice("abcdefghijklmnopqrstuvwxyz")
                await self.page.keyboard.type(typo_char)
                await asyncio.sleep(random.uniform(0.10, 0.25))
                await self.page.keyboard.press("Backspace")
                await asyncio.sleep(random.uniform(0.08, 0.18))

            await self.page.keyboard.type(char)
            # Sleep between keystrokes
            delay_sec = random.uniform(min_delay_ms, max_delay_ms) / 1000.0
            await asyncio.sleep(delay_sec)

    async def scroll(
        self,
        delta_y: int = 350,
        steps: int = 5,
        pause_between: Tuple[float, float] = (0.2, 0.6),
    ) -> None:
        """Smoothly scrolls page downwards or upwards in several chunks."""
        step_delta = delta_y / steps
        for _ in range(steps):
            # Introduce small randomized variations to each scroll tick
            chunk = step_delta * random.uniform(0.85, 1.15)
            await self.page.mouse.wheel(0, chunk)
            await asyncio.sleep(random.uniform(pause_between[0], pause_between[1]))


# ==============================================================================
# 3. Persistent Profile Storage (Tarball + Redis)
# ==============================================================================

class ProfileSyncManager:
    """
    Handles packing/unpacking of Firefox persistent profile folders
    (cookies.sqlite, IndexedDB storage/default, LocalStorage, Cache API)
    and synchronizes them with Redis key `smm:profile:<account_id>`.
    """

    PROFILE_KEY_PREFIX = "smm:profile:"

    @classmethod
    def get_redis_key(cls, account_id: str) -> str:
        return f"{cls.PROFILE_KEY_PREFIX}{account_id}"

    @classmethod
    def pack_profile_to_bytes(cls, profile_dir: str) -> bytes:
        """Archives essential profile files into a compressed tar.gz in memory."""
        if not os.path.exists(profile_dir):
            return b""

        buf = io.BytesIO()
        with tarfile.open(fileobj=buf, mode="w:gz") as tar:
            for root, dirs, files in os.walk(profile_dir):
                for f in files:
                    full_path = os.path.join(root, f)
                    rel_path = os.path.relpath(full_path, profile_dir)

                    # Skip socket files, lock files, and huge temporary cache junk
                    if f.endswith((".lock", ".parentlock", "lock")) or "startupCache" in rel_path:
                        continue

                    try:
                        tar.add(full_path, arcname=rel_path)
                    except (OSError, IOError) as e:
                        logger.warning(f"Could not add {rel_path} to profile archive: {e}")

        buf.seek(0)
        return buf.getvalue()

    @classmethod
    def unpack_profile_from_bytes(cls, archive_bytes: bytes, target_dir: str) -> bool:
        """Extracts tar.gz archive into target_dir."""
        if not archive_bytes:
            return False

        os.makedirs(target_dir, exist_ok=True)
        buf = io.BytesIO(archive_bytes)
        try:
            with tarfile.open(fileobj=buf, mode="r:gz") as tar:
                # Safe extraction preventing path traversal
                for member in tar.getmembers():
                    member_path = os.path.normpath(os.path.join(target_dir, member.name))
                    if not member_path.startswith(os.path.abspath(target_dir)):
                        raise SecurityError(f"Directory traversal detected in archive: {member.name}")
                tar.extractall(target_dir)
            logger.info(f"Successfully unpacked persistent profile into {target_dir}")
            return True
        except Exception as e:
            logger.error(f"Failed to unpack profile archive: {e}")
            return False

    @classmethod
    def restore_from_redis(cls, redis_client: Any, account_id: str, target_dir: str) -> bool:
        """Fetches profile archive from Redis and restores to target_dir."""
        if redis_client is None:
            logger.info("Redis client not configured; using local directory only.")
            return False

        key = cls.get_redis_key(account_id)
        try:
            archive_bytes = redis_client.get(key)
            if archive_bytes:
                logger.info(f"Found existing profile in Redis [{key}] ({len(archive_bytes)} bytes)")
                return cls.unpack_profile_from_bytes(archive_bytes, target_dir)
            else:
                logger.info(f"No existing profile in Redis for [{key}]. Starting fresh.")
                return False
        except Exception as e:
            logger.error(f"Error restoring profile from Redis: {e}")
            return False

    @classmethod
    def save_to_redis(
        cls,
        redis_client: Any,
        account_id: str,
        profile_dir: str,
        ttl_seconds: int = 30 * 86400,
    ) -> bool:
        """Packs profile_dir to tar.gz and uploads to Redis with TTL."""
        if redis_client is None:
            logger.info("Redis client not configured; skipping remote sync.")
            return False

        key = cls.get_redis_key(account_id)
        try:
            archive_bytes = cls.pack_profile_to_bytes(profile_dir)
            if not archive_bytes:
                logger.warning(f"Profile directory {profile_dir} is empty; nothing to save.")
                return False

            redis_client.set(key, archive_bytes, ex=ttl_seconds)
            logger.info(f"Saved persistent profile [{key}] to Redis ({len(archive_bytes)} bytes, TTL={ttl_seconds}s)")
            return True
        except Exception as e:
            logger.error(f"Error saving profile to Redis: {e}")
            return False


# ==============================================================================
# 4. Stealth Persistent Browser Session (Playwright Firefox / Camoufox)
# ==============================================================================

class StealthBrowserSession:
    """
    Context manager for Playwright Firefox / Camoufox with strict persistent profile enforcement.
    Guarantees:
    - Never calls new_context()
    - Uses launch_persistent_context(user_data_dir=...)
    - Restores profile from Redis on enter, saves profile to Redis on exit
    - Sets up US Proxy routing
    """

    def __init__(self, config: BrowserConfig):
        self.config = config
        self.user_data_dir = config.get_effective_user_data_dir()
        self.playwright: Any = None
        self.context: Any = None
        self.page: Any = None
        self.human: Optional[HumanInteractionHelper] = None
        self.redis_client: Any = None

    def _init_redis(self) -> None:
        """Connects to Redis if available."""
        try:
            import redis
            if self.config.redis_url:
                self.redis_client = redis.Redis.from_url(self.config.redis_url)
                self.redis_client.ping()
                logger.info(f"Connected to Redis: {self.config.redis_url}")
        except Exception as e:
            logger.warning(f"Redis connection unavailable ({e}); running in local persistence mode.")
            self.redis_client = None

    async def __aenter__(self) -> "StealthBrowserSession":
        self._init_redis()

        # 1. Restore profile from Redis if available
        os.makedirs(self.user_data_dir, exist_ok=True)
        ProfileSyncManager.restore_from_redis(
            self.redis_client,
            self.config.account_id,
            self.user_data_dir,
        )

        # 2. Configure launch options
        proxy_options = None
        if self.config.proxy_server:
            proxy_options = {
                "server": self.config.proxy_server,
                "bypass": "localhost,127.0.0.1,igaming-*,*.local",
            }
            logger.info(f"Configuring US Proxy route: {self.config.proxy_server}")

        # Firefox stealth preferences
        firefox_user_prefs = {
            "marionette.enabled": False,
            "dom.webdriver.enabled": False,
            "media.peerconnection.enabled": True,
            "network.http.sendRefererHeader": 2,
            "privacy.resistFingerprinting": False,  # ResistFingerprinting can break canvas/audio on social media
        }
        firefox_user_prefs.update(self.config.extra_firefox_prefs)

        launch_args = [
            "-width", str(self.config.viewport_width),
            "-height", str(self.config.viewport_height),
        ]

        # 3. Launch Persistent Context (STRICT RULE 9: NO new_context)
        from playwright.async_api import async_playwright

        self.playwright = await async_playwright().start()

        logger.info(f"Launching Firefox Persistent Context for account '{self.config.account_id}' in {self.user_data_dir}")

        launch_kwargs = {
            "user_data_dir": self.user_data_dir,
            "headless": self.config.headless,
            "viewport": {"width": self.config.viewport_width, "height": self.config.viewport_height},
            "proxy": proxy_options,
            "locale": self.config.locale,
            "timezone_id": self.config.timezone_id,
            "user_agent": self.config.user_agent,
            "firefox_user_prefs": firefox_user_prefs,
            "args": launch_args,
            "accept_downloads": True,
        }

        # Check for installed firefox binary if playwright default differs
        import glob
        firefox_bin = os.getenv("FIREFOX_BIN")
        if not (firefox_bin and os.path.isfile(firefox_bin)):
            candidates = glob.glob("/ms-playwright/firefox-*/firefox/firefox")
            if candidates:
                firefox_bin = sorted(candidates)[-1]
            else:
                firefox_bin = None

        if firefox_bin and os.path.isfile(firefox_bin):
            logger.info(f"Using Firefox binary: {firefox_bin}")
            launch_kwargs["executable_path"] = firefox_bin

        self.context = await self.playwright.firefox.launch_persistent_context(**launch_kwargs)

        # Stealth init script to mask automation attributes
        await self.context.add_init_script("""
            // Mask navigator.webdriver
            Object.defineProperty(Object.getPrototypeOf(navigator), 'webdriver', {
                get: () => undefined,
            });
            window.navigator.chrome = undefined;
        """)

        # Get or create primary page
        pages = self.context.pages
        if pages:
            self.page = pages[0]
        else:
            self.page = await self.context.new_page()

        self.human = HumanInteractionHelper(self.page)
        return self

    async def __aexit__(self, exc_type: Any, exc_val: Any, exc_tb: Any) -> None:
        logger.info(f"Closing browser session for account '{self.config.account_id}'...")

        try:
            if self.context:
                await self.context.close()
        except Exception as e:
            logger.warning(f"Error closing context: {e}")

        try:
            if self.playwright:
                await self.playwright.stop()
        except Exception as e:
            logger.warning(f"Error stopping playwright: {e}")

        # 4. Save updated profile back to Redis
        ProfileSyncManager.save_to_redis(
            self.redis_client,
            self.config.account_id,
            self.user_data_dir,
            ttl_seconds=self.config.redis_profile_ttl,
        )
        logger.info(f"Session closed and profile synced for '{self.config.account_id}'.")


# Security helper
class SecurityError(Exception):
    pass
