#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Mobile Device Pool & Isolation Manager
Rule 6 (Network Proxy Routing), Rule 12 (Dedicated Static Proxies), Rule 13 (Mobile Pools Isolation)

Manages pools of Android devices (Reddroid, Cloud Android, Physical/ADB devices):
1. SMM_POSTER: Strictly for Social Media posting (Threads, Instagram, TikTok, Reddit) via foreign PureVPN proxies.
   Bookmaker betting apps are STRICTLY FORBIDDEN from being installed or run on these devices!
2. CAPTURE_RU: Strictly for capturing Russian bookmakers (Winline, Fonbet, Pari, Betcity) via direct residential SPb IP.
3. CAPTURE_GLOBAL: Strictly for capturing International bookmakers (Pinnacle, Bet365, Betano, 1xBet) via respective country proxies.
"""

from dataclasses import dataclass, field
from datetime import datetime, timezone
from enum import Enum
import logging
import os
import re
import subprocess
from typing import Dict, List, Optional, Tuple

logger = logging.getLogger("MobileDevicePool")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")


class PoolType(str, Enum):
    SMM_POSTER = "smm_poster"
    CAPTURE_RU = "capture_ru"
    CAPTURE_GLOBAL = "capture_global"


class DeviceStatus(str, Enum):
    ONLINE = "online"
    OFFLINE = "offline"
    BUSY = "busy"
    UNAUTHORIZED = "unauthorized"
    UNKNOWN = "unknown"


class IsolationViolationError(RuntimeError):
    """Raised when an operation violates security or anti-fraud isolation rules between pools."""
    pass


@dataclass
class AndroidDevice:
    device_id: str                          # e.g., "100.78.183.101:5555" or "emulator-5554"
    pool_type: PoolType                     # Pool assignment
    assigned_proxy: Optional[str] = None     # e.g., "http://purevpn-nl.proxy:3128" or "direct-spb"
    geo_country: str = "RU"                 # "RU", "US", "NL", "UK", "BR"
    status: DeviceStatus = DeviceStatus.UNKNOWN
    model: str = "Pixel 7 Pro"
    screen_width: int = 1080
    screen_height: int = 2400
    installed_packages: List[str] = field(default_factory=list)
    last_seen: Optional[datetime] = None


class MobileDevicePoolManager:
    """
    Orchestrates Android device pools with strict anti-fraud isolation enforcement.
    """

    def __init__(self, adb_binary_path: str = "adb"):
        self.adb_bin = adb_binary_path
        self.devices: Dict[str, AndroidDevice] = {}
        self._init_default_inventory()

    def _init_default_inventory(self) -> None:
        """Pre-configures default device inventory according to cluster topology."""
        # Пул 1: SMM Постинг (Instagram, Threads, Reddit) - Зарубежные статические ноды PureVPN
        self.register_device(AndroidDevice(
            device_id="mobile-smm-us:5555",
            pool_type=PoolType.SMM_POSTER,
            assigned_proxy="http://purevpn-us-ca.proxy:3128",
            geo_country="US",
            model="Samsung Galaxy S23 Ultra"
        ))
        self.register_device(AndroidDevice(
            device_id="mobile-smm-nl:5555",
            pool_type=PoolType.SMM_POSTER,
            assigned_proxy="http://purevpn-nl.proxy:3128",
            geo_country="NL",
            model="Google Pixel 8 Pro"
        ))

        # Пул 2: Захват котировок/купонов РФ БК (Винлайн, Фонбет, Пари) - Чистый домашний IP СПб
        self.register_device(AndroidDevice(
            device_id="mobile-capture-ru-1:5555",
            pool_type=PoolType.CAPTURE_RU,
            assigned_proxy="direct-spb",
            geo_country="RU",
            model="Xiaomi 13T Pro"
        ))
        self.register_device(AndroidDevice(
            device_id="mobile-capture-ru-2:5555",
            pool_type=PoolType.CAPTURE_RU,
            assigned_proxy="direct-spb",
            geo_country="RU",
            model="Honor Magic6"
        ))

        # Пул 3: Захват котировок/купонов зарубежных БК (Pinnacle, Betano, Bet365)
        self.register_device(AndroidDevice(
            device_id="mobile-capture-global-uk:5555",
            pool_type=PoolType.CAPTURE_GLOBAL,
            assigned_proxy="http://purevpn-uk.proxy:3128",
            geo_country="UK",
            model="OnePlus 12"
        ))
        self.register_device(AndroidDevice(
            device_id="mobile-capture-global-br:5555",
            pool_type=PoolType.CAPTURE_GLOBAL,
            assigned_proxy="http://purevpn-br.proxy:3128",
            geo_country="BR",
            model="Motorola Edge 50 Ultra"
        ))

    def register_device(self, device: AndroidDevice) -> None:
        """Registers a device into the pool."""
        self.devices[device.device_id] = device
        logger.info(f"Registered device [{device.device_id}] in pool [{device.pool_type.value}] (Geo: {device.geo_country})")

    def get_devices_by_pool(self, pool_type: PoolType) -> List[AndroidDevice]:
        """Returns all devices belonging to a specific pool."""
        return [d for d in self.devices.values() if d.pool_type == pool_type]

    def validate_action_allowed(self, device_id: str, action_target_package: str, is_betting_app: bool) -> None:
        """
        STRICT ISOLATION GUARD:
        Ensures betting apps NEVER touch SMM posting phones, and RU apps NEVER touch foreign phones.
        """
        device = self.devices.get(device_id)
        if not device:
            raise ValueError(f"Device [{device_id}] not found in registered inventory.")

        # ПРАВИЛО 1: Запрет БК на телефонах соцсетей
        if device.pool_type == PoolType.SMM_POSTER and is_betting_app:
            error_msg = (
                f"SECURITY VIOLATION: Attempted to install/run betting app [{action_target_package}] "
                f"on SMM Posting device [{device_id}]. Betting apps trigger anti-fraud bans in Meta/TikTok! "
                f"Use a dedicated CAPTURE_RU or CAPTURE_GLOBAL device instead."
            )
            logger.error(error_msg)
            raise IsolationViolationError(error_msg)

        # ПРАВИЛО 2: Запрет РФ БК на зарубежных IP
        if is_betting_app and "ru." in action_target_package and device.pool_type == PoolType.CAPTURE_GLOBAL:
            error_msg = (
                f"ROUTING VIOLATION: Russian bookmaker [{action_target_package}] cannot be launched "
                f"on Global device [{device_id}] (Geo: {device.geo_country}). RU books require direct SPb IP!"
            )
            logger.error(error_msg)
            raise IsolationViolationError(error_msg)

    def run_adb_command(self, device_id: str, command: List[str], timeout_seconds: int = 15) -> Tuple[int, str, str]:
        """
        Executes an ADB command against a specific device with a strict timeout to prevent hangs.
        """
        full_cmd = [self.adb_bin, "-s", device_id] + command
        try:
            res = subprocess.run(
                full_cmd,
                capture_output=True,
                text=True,
                timeout=timeout_seconds,
                check=False
            )
            return res.returncode, res.stdout.strip(), res.stderr.strip()
        except subprocess.TimeoutExpired:
            logger.warning(f"ADB command timed out after {timeout_seconds}s on device [{device_id}]: {' '.join(full_cmd)}")
            return -1, "", f"Timeout after {timeout_seconds}s"
        except FileNotFoundError:
            # ADB binary not installed locally, return simulated response if in container
            logger.debug(f"ADB binary not found at [{self.adb_bin}]. Returning mock response for testing.")
            return 0, "mock_output", ""

    def connect_device(self, device_id: str) -> bool:
        """Connects to a TCP/IP ADB device (e.g. Reddroid or Cloud Android)."""
        ret, stdout, stderr = self.run_adb_command(device_id, ["connect", device_id])
        if "connected" in stdout.lower():
            if device_id in self.devices:
                self.devices[device_id].status = DeviceStatus.ONLINE
                self.devices[device_id].last_seen = datetime.now(timezone.utc)
            logger.info(f"Successfully connected to ADB device [{device_id}]")
            return True
        logger.warning(f"Failed to connect to [{device_id}]: {stdout} {stderr}")
        return False

    def capture_screenshot(self, device_id: str, local_save_path: str) -> bool:
        """Takes a clean high-resolution screenshot from the device."""
        os.makedirs(os.path.dirname(os.path.abspath(local_save_path)), exist_ok=True)
        ret, stdout, stderr = self.run_adb_command(device_id, ["exec-out", "screencap", "-p"])
        # If real adb binary is available, we write binary output
        try:
            full_cmd = [self.adb_bin, "-s", device_id, "exec-out", "screencap", "-p"]
            with open(local_save_path, "wb") as f:
                res = subprocess.run(full_cmd, stdout=f, stderr=subprocess.PIPE, timeout=15)
                if res.returncode == 0 and os.path.getsize(local_save_path) > 0:
                    logger.info(f"Captured screenshot from [{device_id}] -> {local_save_path} ({os.path.getsize(local_save_path)} bytes)")
                    return True
        except Exception as e:
            logger.error(f"Error capturing screenshot on [{device_id}]: {e}")
        return False
