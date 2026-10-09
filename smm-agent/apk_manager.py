#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Bookmaker APK Manager & Auto-Provisioning Engine
Rule 6 (Network Proxy Routing), Rule 10 (80% Freebet Cash), Rule 13 (Mobile Capture Automation)

Automates fetching, caching, and installing official APK files for both Russian and Global bookmakers.
Enforces that:
- Russian bookmakers (Winline, Fonbet, Pari) are installed ONLY on CAPTURE_RU devices.
- International bookmakers (Pinnacle, Betano, Bet365, 1xBet) are installed ONLY on CAPTURE_GLOBAL devices.
- SMM_POSTER devices reject any attempt to install betting APKs.
"""

from dataclasses import dataclass
import logging
import os
from pathlib import Path
import shutil
import subprocess
import time
from typing import Dict, List, Optional, Tuple

try:
    import requests
except ImportError:
    requests = None

from mobile_device_pool import (
    AndroidDevice,
    DeviceStatus,
    IsolationViolationError,
    MobileDevicePoolManager,
    PoolType,
)

logger = logging.getLogger("APKManager")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")


@dataclass
class BookmakerAppMetadata:
    key: str                                    # e.g., "winline", "fonbet", "pinnacle"
    name: str                                   # Human readable name
    package_name: str                           # Android package id
    launcher_activity: str                      # Main activity to launch
    download_url: str                           # Official direct APK link or Aurora store ID
    region: str                                 # "RU", "UK", "NL", "BR", "GLOBAL"
    allowed_pool: PoolType                      # CAPTURE_RU or CAPTURE_GLOBAL
    theme_color: str                            # Hex color for coupon mockups (#F37021, #E30613, etc.)


BOOKMAKER_CATALOG: Dict[str, BookmakerAppMetadata] = {
    # --- РОССИЙСКИЕ БУКМЕКЕРЫ (ЦУПИС / ЕРАИ) ---
    "winline": BookmakerAppMetadata(
        key="winline",
        name="Winline",
        package_name="ru.winline.client",
        launcher_activity="ru.winline.client.ui.splash.SplashActivity",
        download_url="https://winline.ru/app/winline.apk",
        region="RU",
        allowed_pool=PoolType.CAPTURE_RU,
        theme_color="#F37021"  # Winline фирменный оранжевый
    ),
    "fonbet": BookmakerAppMetadata(
        key="fonbet",
        name="Фонбет",
        package_name="ru.fonbet.app",
        launcher_activity="ru.fonbet.app.SplashActivity",
        download_url="https://apps.fonbet.ru/fonbet.apk",
        region="RU",
        allowed_pool=PoolType.CAPTURE_RU,
        theme_color="#E30613"  # Фонбет фирменный красный
    ),
    "pari": BookmakerAppMetadata(
        key="pari",
        name="Pari",
        package_name="ru.pari.mobile",
        launcher_activity="ru.pari.mobile.ui.MainActivity",
        download_url="https://pari.ru/apps/android/pari.apk",
        region="RU",
        allowed_pool=PoolType.CAPTURE_RU,
        theme_color="#00A859"  # Pari зеленый
    ),
    "betcity": BookmakerAppMetadata(
        key="betcity",
        name="Бетсити",
        package_name="ru.betcity.client",
        launcher_activity="ru.betcity.client.MainActivity",
        download_url="https://betcity.ru/app/betcity.apk",
        region="RU",
        allowed_pool=PoolType.CAPTURE_RU,
        theme_color="#1E88E5"  # Бетсити синий
    ),
    "leon": BookmakerAppMetadata(
        key="leon",
        name="Леон",
        package_name="ru.leon.app",
        launcher_activity="ru.leon.app.ui.MainActivity",
        download_url="https://leon.ru/app/leon.apk",
        region="RU",
        allowed_pool=PoolType.CAPTURE_RU,
        theme_color="#212121"  # Леон темный
    ),

    # --- МЕЖДУНАРОДНЫЕ И ОФФШОРНЫЕ БУКМЕКЕРЫ ---
    "pinnacle": BookmakerAppMetadata(
        key="pinnacle",
        name="Pinnacle",
        package_name="com.pinnacle.sports",
        launcher_activity="com.pinnacle.sports.MainActivity",
        download_url="https://www.pinnacle.com/static/apps/pinnacle.apk",
        region="NL",
        allowed_pool=PoolType.CAPTURE_GLOBAL,
        theme_color="#002D62"  # Pinnacle темно-синий
    ),
    "1xbet": BookmakerAppMetadata(
        key="1xbet",
        name="1xBet",
        package_name="org.xbet.client",
        launcher_activity="org.xbet.client.MainActivity",
        download_url="https://1xbet.com/download/1xbet.apk",
        region="GLOBAL",
        allowed_pool=PoolType.CAPTURE_GLOBAL,
        theme_color="#1B609E"  # 1xBet лазурно-синий
    ),
    "betano": BookmakerAppMetadata(
        key="betano",
        name="Betano (LATAM/Brazil)",
        package_name="com.betano.app.br",
        launcher_activity="com.betano.app.MainActivity",
        download_url="https://br.betano.com/android/betano.apk",
        region="BR",
        allowed_pool=PoolType.CAPTURE_GLOBAL,
        theme_color="#FF5A00"  # Betano оранжевый
    ),
    "bet365": BookmakerAppMetadata(
        key="bet365",
        name="Bet365 (UK)",
        package_name="com.bet365.sports",
        launcher_activity="com.bet365.sports.AppActivity",
        download_url="aurora:com.bet365.sports",
        region="UK",
        allowed_pool=PoolType.CAPTURE_GLOBAL,
        theme_color="#006644"  # Bet365 зеленый
    ),
    "melbet": BookmakerAppMetadata(
        key="melbet",
        name="Melbet",
        package_name="org.melbet.client",
        launcher_activity="org.melbet.client.MainActivity",
        download_url="https://melbet.com/download/melbet.apk",
        region="GLOBAL",
        allowed_pool=PoolType.CAPTURE_GLOBAL,
        theme_color="#F39C12"  # Melbet желтый
    ),
}


class BookmakerAPKManager:
    """
    Manages downloading, caching, and installing official APKs on virtual devices.
    """

    def __init__(self, storage_dir: Optional[str] = None):
        self.storage_dir = Path(storage_dir or os.path.join(os.path.dirname(os.path.abspath(__file__)), "storage", "apks"))
        self.storage_dir.mkdir(parents=True, exist_ok=True)

    def get_cached_apk_path(self, bookmaker_key: str) -> Path:
        """Returns the local path where APK should be stored."""
        meta = BOOKMAKER_CATALOG.get(bookmaker_key)
        if not meta:
            raise KeyError(f"Unknown bookmaker key [{bookmaker_key}]. Available: {list(BOOKMAKER_CATALOG.keys())}")
        return self.storage_dir / f"{meta.package_name}.apk"

    def download_apk(self, bookmaker_key: str, force_redownload: bool = False) -> Path:
        """
        Downloads official APK file directly from bookmaker CDN or creates a placeholder if testing.
        """
        meta = BOOKMAKER_CATALOG[bookmaker_key]
        dest_path = self.get_cached_apk_path(bookmaker_key)

        if dest_path.exists() and not force_redownload:
            logger.info(f"APK for [{meta.name}] already cached at: {dest_path}")
            return dest_path

        logger.info(f"Downloading official APK for [{meta.name}] from: {meta.download_url}...")
        
        # If the URL is Aurora Store package reference
        if meta.download_url.startswith("aurora:"):
            logger.info(f"Bookmaker [{meta.name}] uses Aurora Store client ID [{meta.package_name}]")
            # Create stub / placeholder package for automated testing
            dest_path.write_text(f"MOCK_AURORA_APK:{meta.package_name}")
            return dest_path

        # Direct HTTP/HTTPS download
        if requests:
            try:
                # Use browser-like User-Agent to bypass simple curl blockers
                headers = {"User-Agent": "Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 Mobile Safari/537.36"}
                response = requests.get(meta.download_url, headers=headers, stream=True, timeout=30)
                if response.status_code == 200:
                    with open(dest_path, "wb") as f:
                        for chunk in response.iter_content(chunk_size=1024 * 1024):
                            if chunk:
                                f.write(chunk)
                    logger.info(f"Successfully downloaded [{meta.name}] APK ({dest_path.stat().st_size} bytes)")
                    return dest_path
                else:
                    logger.warning(f"Download returned HTTP {response.status_code} for [{meta.name}]. Creating cached stub.")
            except Exception as e:
                logger.warning(f"Could not connect to CDN for [{meta.name}]: {e}. Creating cached stub for offline testing.")
        
        # Fallback / offline testing stub
        dest_path.write_text(f"STUB_APK_DATA:{meta.package_name}")
        return dest_path

    def install_on_device(
        self,
        device_id: str,
        bookmaker_key: str,
        pool_manager: MobileDevicePoolManager,
        auto_grant_permissions: bool = True
    ) -> bool:
        """
        Installs APK on the target device via ADB with permission validation.
        """
        meta = BOOKMAKER_CATALOG[bookmaker_key]

        # 1. Enforce strict isolation
        pool_manager.validate_action_allowed(
            device_id=device_id,
            action_target_package=meta.package_name,
            is_betting_app=True
        )

        device = pool_manager.devices[device_id]
        if meta.allowed_pool != device.pool_type:
            raise IsolationViolationError(
                f"Cannot install [{meta.name}] on device [{device_id}]: "
                f"Bookmaker requires pool [{meta.allowed_pool.value}], but device belongs to [{device.pool_type.value}]."
            )

        apk_path = self.download_apk(bookmaker_key)

        logger.info(f"Installing [{meta.name}] ({meta.package_name}) on device [{device_id}]...")
        flags = ["install", "-r"]
        if auto_grant_permissions:
            flags.append("-g")  # Auto-grant runtime permissions (ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE, etc.)
        flags.append(str(apk_path))

        ret, stdout, stderr = pool_manager.run_adb_command(device_id, flags, timeout_seconds=60)
        
        # In real ADB, "Success" indicates clean install. In mock test, ret == 0.
        if "success" in stdout.lower() or ret == 0:
            if meta.package_name not in device.installed_packages:
                device.installed_packages.append(meta.package_name)
            logger.info(f"Successfully installed [{meta.name}] on [{device_id}]")
            return True

        logger.error(f"Failed to install [{meta.name}] on [{device_id}]: {stdout} | {stderr}")
        return False

    def launch_app(
        self,
        device_id: str,
        bookmaker_key: str,
        pool_manager: MobileDevicePoolManager
    ) -> bool:
        """Launches the bookmaker application into foreground."""
        meta = BOOKMAKER_CATALOG[bookmaker_key]
        pool_manager.validate_action_allowed(device_id, meta.package_name, is_betting_app=True)

        component = f"{meta.package_name}/{meta.launcher_activity}"
        logger.info(f"Launching [{meta.name}] on [{device_id}] via {component}...")
        ret, stdout, stderr = pool_manager.run_adb_command(
            device_id,
            ["shell", "am", "start", "-n", component],
            timeout_seconds=15
        )
        return ret == 0

    def stop_app(
        self,
        device_id: str,
        bookmaker_key: str,
        pool_manager: MobileDevicePoolManager
    ) -> bool:
        """Force stops the application to clear background memory and live streams."""
        meta = BOOKMAKER_CATALOG[bookmaker_key]
        logger.info(f"Force stopping [{meta.name}] ({meta.package_name}) on [{device_id}]...")
        ret, stdout, stderr = pool_manager.run_adb_command(
            device_id,
            ["shell", "am", "force-stop", meta.package_name],
            timeout_seconds=10
        )
        return ret == 0

    def batch_provision_pool(
        self,
        pool_type: PoolType,
        pool_manager: MobileDevicePoolManager
    ) -> Dict[str, Dict[str, bool]]:
        """
        Installs all appropriate bookmaker APKs onto all devices in the selected pool.
        """
        results: Dict[str, Dict[str, bool]] = {}
        devices = pool_manager.get_devices_by_pool(pool_type)

        target_books = [
            key for key, meta in BOOKMAKER_CATALOG.items()
            if meta.allowed_pool == pool_type
        ]

        logger.info(f"=== Starting Batch Provisioning for Pool [{pool_type.value}] ({len(devices)} devices, {len(target_books)} books) ===")
        for dev in devices:
            results[dev.device_id] = {}
            for book_key in target_books:
                try:
                    success = self.install_on_device(dev.device_id, book_key, pool_manager)
                    results[dev.device_id][book_key] = success
                except Exception as e:
                    logger.error(f"Error provisioning [{book_key}] on [{dev.device_id}]: {e}")
                    results[dev.device_id][book_key] = False

        return results
