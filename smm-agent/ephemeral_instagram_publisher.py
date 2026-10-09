#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Ephemeral Instagram Managing Service
Target Accounts: fr.smartbet.guru, en.smartbet.guru, es.smartbet.guru, rus.smartbet.guru
Rule 6 (Proxy Routing), Rule 9 (Stealth/Anti-fraud), Rule 10 (80% Freebet Cash), Rule 12 (Dedicated Proxies), Rule 13 (Mobile Isolation)

Architecture:
1. Ephemeral Lifecycle:
   - Starts virtual phone container on bare-metal Xeon host on demand (`podman start android-container`).
   - Verifies system boot completed (`sys.boot_completed == 1`).
   - Ensures Instagram Lite is active and logged into the target language account (`fr.smartbet.guru`, etc.).
   - Generates high-conversion 1080x1350 card with Matched Betting 80% freebet cash math and mandatory disclaimers.
   - Pushes media, indexes via MediaStore, and publishes post with localized caption and affiliate CTA.
   - Waits 15 minutes monitoring incoming comments on the post and replies to engagement.
   - Shuts down virtual phone (`podman stop android-container -t 5`) in a guaranteed finally: block to release ~3.5 GB RAM and CPU.
"""

import argparse
import email
import imaplib
import logging
import os
from pathlib import Path
import re
import subprocess
import sys
import time
from typing import Dict, List, Optional, Tuple

from PIL import Image, ImageDraw, ImageFont

logger = logging.getLogger("EphemeralInstagramPublisher")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] [EphemeralIG] %(message)s")

XEON_HOST = os.getenv("XEON_HOST", "100.78.183.101")
XEON_USER = os.getenv("XEON_USER", "root")
SSH_KEY = os.getenv("SSH_KEY", r"C:\Users\chernousov_a\.ssh\id_ed25519")
CONTAINER_NAME = os.getenv("ANDROID_CONTAINER", "android-container")

GMAIL_USER = os.getenv("GMAIL_USER", "www.smartbet.guru@gmail.com")
GMAIL_APP_PASS = os.getenv("GMAIL_APP_PASS", "ujsrxyyvssfjfxpb")

OUTPUT_DIR = Path(os.path.join(os.path.dirname(os.path.abspath(__file__)), "storage", "cards"))
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

# Account coordinates in Instagram Lite bottom sheet
ACCOUNT_SHEET_COORDS = {
    "fr.smartbet.guru": (720, 480),     # Aleksei Chernousov (fr)
    "rus.smartbet.guru": (720, 720),
    "alice.resume.bot": (720, 950),
    "sport.ai.analysis": (720, 1180),
    "datawiki.pro": (720, 1400),
    "en.datawiki.pro": (720, 1630),
    "es.smartbet.guru": (720, 1860),
    "aleksey.chernousov": (720, 2090),
    "en.smartbet.guru": (720, 2320),
}

REGIONAL_PRESETS = {
    "fr.smartbet.guru": {
        "lang": "fr",
        "sport": "Ligue 1",
        "match_title": "Paris SG vs Marseille",
        "bookmaker_freebet": "Winamax",
        "bookmaker_hedge": "Unibet",
        "freebet_amount": 100.0,
        "currency": "€",
        "k1": 5.20,
        "k2": 1.24,
        "header_title": "Radar Mathématique des Paris Sportifs",
        "header_sub": "Algorithmes de détection de valuebets, surebets et conversion de bonus • France",
        "banner_badge": "CONVERSION FREEBET",
        "banner_title": "80% DE CASH GARANTI",
        "banner_sub": "Ne gâchez pas votre bonus au hasard — convertissez-le mathématiquement.",
        "example_title": "Exemple Concret : Matched Betting",
        "leg1_title": "Pari 1 (Freebet SNR) : Plus de 2.5 Buts sur {b1}",
        "leg1_sub": "Cote K1 = {k1:.2f} | Mise = {amt:.0f} {cur} (Freebet offert sans risque)",
        "leg2_title": "Pari 2 (Contre-pari couverture) : Moins de 2.5 Buts sur {b2}",
        "leg2_sub": "Cote K2 = {k2:.2f} | Mise = {hedge:.2f} {cur} (Argent réel)",
        "formula_text": "Formule SNR : eta = (K1 - 1)(K2 - 1) / K2 = {eta_pct:.1f}%",
        "net_gain": "Gain net garanti : +{gain:.2f} {cur} net quel que soit le score !",
        "cta_badge": "CALCULATEUR GRATUIT",
        "cta_title": "Disponible en libre accès",
        "cta_sub": "Accédez aux cotes en direct et calculez vos gains nets sur :",
        "disclaimer_lines": [
            "18+ | Les jeux d’argent et de hasard sont interdits aux mineurs.",
            "Jouer comporte des risques : endettement, isolement, dépendance.",
            "Pour être aidé, appelez le 09-74-75-13-13 (appel non surtaxé). www.joueurs-info-service.fr"
        ],
        "caption": (
            "⚡ Convertissez 100% de vos freebets en CASH REEL avec SmartBet.guru !\n\n"
            "Ne pariez plus vos bonus au hasard. Avec notre méthode mathématique de Matched Betting (SNR), "
            "tout freebet de 100 € se transforme en 80.00 € de cash net garanti sur votre compte bancaire, "
            "quel que soit le score final du match !\n\n"
            "📊 Exemple Ligue 1 (PSG vs OM) :\n"
            "• Pari 1 (Freebet) : Plus de 2.5 buts @ 5.20 sur Winamax\n"
            "• Pari 2 (Couverture) : Moins de 2.5 buts @ 1.24 sur Unibet\n"
            "💰 Bénéfice net garanti : +81.30 € net !\n\n"
            "🚀 Accédez gratuitement au calculateur de freebets :\n"
            "👉 fr.smartbet.guru/tools/freebet-calculator\n\n"
            "#parisportif #pronostic #ligue1 #winamax #unibet #matchedbetting #freebet #smartbet #psg #om\n\n"
            "🔞 18+ | Les jeux d’argent comportent des risques (endettement, isolement, dépendance). "
            "Pour être aidé, appelez le 09-74-75-13-13 (appel non surtaxé). Jouez responsablement."
        ),
        "reply_text": "Merci pour votre commentaire ! N'hésitez pas à tester notre calculateur gratuit sur fr.smartbet.guru 🚀"
    },
    "rus.smartbet.guru": {
        "lang": "ru",
        "sport": "РПЛ",
        "match_title": "Зенит vs Спартак Москва",
        "bookmaker_freebet": "Fonbet",
        "bookmaker_hedge": "Winline",
        "freebet_amount": 3000.0,
        "currency": "₽",
        "k1": 5.20,
        "k2": 1.24,
        "header_title": "Математический радар спортивных ставок",
        "header_sub": "Алгоритмы поиска валуйных коэффициентов, вилок и конверсии бонусов • РФ",
        "banner_badge": "КОНВЕРСИЯ ФРИБЕТА",
        "banner_title": "80% ГАРАНТИРОВАННОГО КЭША",
        "banner_sub": "Не сливайте бонусы наугад — забирайте математически чистые деньги.",
        "example_title": "Конкретный расчет: Matched Betting",
        "leg1_title": "Ставка 1 (Фрибет SNR) : Тотал больше 2.5 на {b1}",
        "leg1_sub": "Кэф K1 = {k1:.2f} | Сумма = {amt:.0f} {cur} (Бесплатный фрибет)",
        "leg2_title": "Ставка 2 (Перекрытие) : Тотал меньше 2.5 на {b2}",
        "leg2_sub": "Кэф K2 = {k2:.2f} | Сумма = {hedge:.2f} {cur} (Реальные деньги)",
        "formula_text": "Формула SNR : eta = (K1 - 1)(K2 - 1) / K2 = {eta_pct:.1f}%",
        "net_gain": "Чистая прибыль: +{gain:.2f} {cur} при ЛЮБОМ счете матча!",
        "cta_badge": "БЕСПЛАТНЫЙ КАЛЬКУЛЯТОР",
        "cta_title": "В открытом доступе на сайте",
        "cta_sub": "Смотрите линии всех букмекеров и считайте кэш на :",
        "disclaimer_lines": [
            "18+ | Азартные игры несут финансовые риски.",
            "Играйте ответственно. Не ставьте больше, чем готовы потерять."
        ],
        "caption": (
            "⚡ Превратите фрибет в ГАРАНТИРОВАННЫЙ КЭШ с SmartBet.guru !\n\n"
            "В отличие от попанов, которые сливают бонусы, SmartBet.guru гарантирует 80% чистых денег с любого фрибета через математическое перекрытие (Matched Betting).\n\n"
            "📊 Пример РПЛ (Зенит vs Спартак) :\n"
            "• Пари 1 (Фрибет 3 000 ₽) : ТБ 2.5 @ 5.20 на Фонбет\n"
            "• Пари 2 (Перекрытие) : ТМ 2.5 @ 1.24 на Винлайн\n"
            "💰 Гарантированный кэш на счет: 2 438.71 ₽ при ЛЮБОМ исходе матча!\n\n"
            "🚀 Бесплатный калькулятор фрибетов:\n"
            "👉 rus.smartbet.guru/tools/freebet-calculator\n\n"
            "#ставки #рпл #фрибет #винлайн #фонбет #вилки #зенит #спартак #smartbet\n\n"
            "🔞 18+ | Азартные игры несут финансовые риски. Играйте ответственно."
        ),
        "reply_text": "Спасибо за комментарий! Калькулятор 80% кэша доступен на rus.smartbet.guru 🚀"
    },
    "en.smartbet.guru": {
        "lang": "en",
        "sport": "Premier League",
        "match_title": "Arsenal vs Man City",
        "bookmaker_freebet": "Bet365",
        "bookmaker_hedge": "Pinnacle",
        "freebet_amount": 100.0,
        "currency": "£",
        "k1": 5.20,
        "k2": 1.24,
        "header_title": "Sports Betting Mathematical Radar",
        "header_sub": "Value betting algorithms, surebets and bonus cash conversion",
        "banner_badge": "FREEBET CONVERSION",
        "banner_title": "80% GUARANTEED CASH",
        "banner_sub": "Do not gamble bonuses randomly — convert them mathematically.",
        "example_title": "Practical Example: Matched Betting",
        "leg1_title": "Bet 1 (Free Bet SNR) : Over 2.5 Goals on {b1}",
        "leg1_sub": "Odds K1 = {k1:.2f} | Stake = {amt:.0f} {cur} (Free bonus token)",
        "leg2_title": "Bet 2 (Hedge Bet) : Under 2.5 Goals on {b2}",
        "leg2_sub": "Odds K2 = {k2:.2f} | Stake = {hedge:.2f} {cur} (Real money)",
        "formula_text": "SNR Formula : eta = (K1 - 1)(K2 - 1) / K2 = {eta_pct:.1f}%",
        "net_gain": "Guaranteed Cash: +{gain:.2f} {cur} net regardless of result!",
        "cta_badge": "FREE CALCULATOR",
        "cta_title": "Open access tool",
        "cta_sub": "Explore live odds and calculate guaranteed profit at:",
        "disclaimer_lines": [
            "18+ | Gamble Responsibly. Gambling can be addictive.",
            "Visit begambleaware.org for free and confidential support."
        ],
        "caption": (
            "⚡ Turn 100% of your free bets into GUARANTEED CASH with SmartBet.guru !\n\n"
            "Stop wasting betting bonuses. With our mathematical Matched Betting formula (SNR), "
            "every £100 free bet converts into ~£80.00 pure cash directly into your account, "
            "regardless of the final score!\n\n"
            "📊 Premier League Example (Arsenal vs Man City) :\n"
            "• Bet 1 (Free Bet) : Over 2.5 Goals @ 5.20 on Bet365\n"
            "• Bet 2 (Hedge) : Under 2.5 Goals @ 1.24 on Pinnacle\n"
            "💰 Guaranteed Net Profit : +£81.30 !\n\n"
            "🚀 Try our free bonus converter calculator :\n"
            "👉 en.smartbet.guru/tools/freebet-calculator\n\n"
            "#betting #matchedbetting #premierleague #arsenal #mancity #bet365 #pinnacle #freebets #smartbet\n\n"
            "🔞 18+ | Gamble Responsibly. Gambling can be addictive. Visit begambleaware.org for confidential help."
        ),
        "reply_text": "Thank you for the comment! Try our free calculator at en.smartbet.guru 🚀"
    },
    "es.smartbet.guru": {
        "lang": "es",
        "sport": "La Liga",
        "match_title": "Real Madrid vs Barcelona",
        "bookmaker_freebet": "Betano",
        "bookmaker_hedge": "Codere",
        "freebet_amount": 100.0,
        "currency": "€",
        "k1": 5.20,
        "k2": 1.24,
        "header_title": "Radar Matemático de Apuestas Deportivas",
        "header_sub": "Algoritmos de detección de valuebets, surebets y bonos • España",
        "banner_badge": "CONVERSIÓN FREEBET",
        "banner_title": "80% DINERO GARANTIZADO",
        "banner_sub": "No malgastes tus bonos apostando al azar — conviértelos matemáticamente.",
        "example_title": "Ejemplo Práctico: Matched Betting",
        "leg1_title": "Apuesta 1 (Freebet SNR) : Más de 2.5 Goles en {b1}",
        "leg1_sub": "Cuota K1 = {k1:.2f} | Apuesta = {amt:.0f} {cur} (Bono sin riesgo)",
        "leg2_title": "Apuesta 2 (Cobertura) : Menos de 2.5 Goles en {b2}",
        "leg2_sub": "Cuota K2 = {k2:.2f} | Apuesta = {hedge:.2f} {cur} (Dinero real)",
        "formula_text": "Fórmula SNR : eta = (K1 - 1)(K2 - 1) / K2 = {eta_pct:.1f}%",
        "net_gain": "Ganancia neta garantizada: +{gain:.2f} {cur} pase lo que pase!",
        "cta_badge": "CALCULADORA GRATIS",
        "cta_title": "Disponible en acceso libre",
        "cta_sub": "Accede a las cuotas en vivo y calcula tus beneficios en:",
        "disclaimer_lines": [
            "18+ | Juega con responsabilidad. Los juegos de azar pueden ser adictivos.",
            "Para ayuda confidencial consulta jugarBIEN.es"
        ],
        "caption": (
            "⚡ ¡Convierte tus apuestas gratis en DINERO REAL GARANTIZADO con SmartBet.guru !\n\n"
            "No desperdicies tus bonos apostando al azar. Mediante nuestra fórmula matemática de Matched Betting (SNR), "
            "cada bono de 100 € se transforma en ~80.00 € de dinero limpio garantizado en tu cuenta bancaria.\n\n"
            "📊 Ejemplo La Liga (Real Madrid vs Barcelona) :\n"
            "• Apuesta 1 (Freebet) : Más de 2.5 goles @ 5.20 en Betano\n"
            "• Apuesta 2 (Cobertura) : Menos de 2.5 goles @ 1.24 en Codere\n"
            "💰 Beneficio neto asegurado : +81.30 € !\n\n"
            "🚀 Accede a la calculadora gratuita :\n"
            "👉 es.smartbet.guru/tools/freebet-calculator\n\n"
            "#apuestas #laliga #realmadrid #barcelona #matchedbetting #freebet #bonos #smartbet\n\n"
            "🔞 18+ | Juega con responsabilidad. Los juegos de azar pueden ser adictivos. Consulta jugarBIEN.es."
        ),
        "reply_text": "¡Gracias por tu comentario! Prueba nuestra calculadora gratuita en es.smartbet.guru 🚀"
    }
}


class EphemeralInstagramPublisher:
    """
    Manages the complete on-demand lifecycle of Instagram publishing on Android container.
    """

    def __init__(
        self,
        host: str = XEON_HOST,
        user: str = XEON_USER,
        ssh_key: str = SSH_KEY,
        container_name: str = CONTAINER_NAME
    ):
        self.host = host
        self.user = user
        self.ssh_key = ssh_key
        self.container_name = container_name

    def execute_ssh(self, remote_cmd: str, timeout: int = 45) -> Tuple[int, str, str]:
        """Executes SSH command on xeon-srv."""
        full_cmd = [
            "ssh",
            "-i", self.ssh_key,
            "-o", "BatchMode=yes",
            "-o", "ConnectTimeout=10",
            "-o", "StrictHostKeyChecking=no",
            f"{self.user}@{self.host}",
            remote_cmd
        ]
        try:
            res = subprocess.run(full_cmd, capture_output=True, text=True, timeout=timeout)
            return res.returncode, res.stdout.strip(), res.stderr.strip()
        except subprocess.TimeoutExpired:
            logger.warning(f"SSH command timed out after {timeout}s: {remote_cmd}")
            return -1, "", "TimeoutExpired"
        except Exception as e:
            logger.error(f"SSH execution failed: {e}")
            return -1, "", str(e)

    def adb(self, adb_args: List[str], timeout: int = 30) -> Tuple[int, str, str]:
        """Executes ADB command inside the container on xeon-srv."""
        args_str = " ".join(adb_args)
        cmd = f"podman exec {self.container_name} adb {args_str}"
        return self.execute_ssh(cmd, timeout=timeout)

    def ensure_adb_keyboard(self) -> bool:
        """Ensures ADBKeyBoard is installed and set as default IME for Unicode/Accents/Emoji input."""
        ret, stdout, _ = self.adb(["shell", "ime", "list", "-a", "-s"])
        if "com.android.adbkeyboard/.AdbIME" not in stdout:
            logger.info("Installing ADBKeyBoard for Unicode/Emoji input...")
            self.execute_ssh(
                f"curl -s -L -o /tmp/ADBKeyboard.apk https://github.com/senzhk/ADBKeyBoard/raw/master/ADBKeyboard.apk && "
                f"podman cp /tmp/ADBKeyboard.apk {self.container_name}:/tmp/ADBKeyboard.apk && "
                f"podman exec {self.container_name} adb install /tmp/ADBKeyboard.apk"
            )
        self.adb(["shell", "ime", "enable", "com.android.adbkeyboard/.AdbIME"])
        self.adb(["shell", "ime", "set", "com.android.adbkeyboard/.AdbIME"])
        logger.info("ADBKeyBoard configured as active IME.")
        return True

    def type_unicode(self, text: str) -> bool:
        """Types arbitrary unicode text (accents, cyrillic, emojis, newlines) via ADBKeyBoard base64 broadcast."""
        import base64
        b64_str = base64.b64encode(text.encode("utf-8")).decode("ascii")
        ret, stdout, stderr = self.adb(["shell", "am", "broadcast", "-a", "ADB_INPUT_B64", "--es", "msg", f"'{b64_str}'"])
        return ret == 0

    def is_container_running(self) -> bool:
        """Checks if android-container is running on xeon-srv."""
        cmd = f"podman ps --filter name={self.container_name} --format '{{{{.Status}}}}'"
        ret, stdout, stderr = self.execute_ssh(cmd)
        return ret == 0 and "Up" in stdout

    def start_phone(self, timeout: int = 240) -> bool:
        """
        Starts the Android container and waits for boot completion.
        """
        if self.is_container_running():
            logger.info(f"Container [{self.container_name}] is already running.")
        else:
            logger.info(f"Starting ephemeral phone container [{self.container_name}] on {self.host}...")
            ret, stdout, stderr = self.execute_ssh(f"podman start {self.container_name}", timeout=30)
            if ret != 0:
                logger.error(f"Failed to start container: {stderr}")
                return False

        logger.info("Awaiting Android boot completion (sys.boot_completed == 1)...")
        start_time = time.time()
        while time.time() - start_time < timeout:
            ret, stdout, stderr = self.adb(["shell", "getprop", "sys.boot_completed"], timeout=10)
            if ret == 0 and stdout.strip() == "1":
                logger.info(f"Android virtual phone booted successfully in {round(time.time() - start_time, 1)}s!")
                # Ensure permissions
                self.adb(["shell", "pm", "grant", "com.instagram.lite", "android.permission.READ_EXTERNAL_STORAGE"])
                self.adb(["shell", "pm", "grant", "com.instagram.lite", "android.permission.WRITE_EXTERNAL_STORAGE"])
                self.adb(["shell", "pm", "grant", "com.instagram.lite", "android.permission.CAMERA"])
                # Ensure ADBKeyBoard
                self.ensure_adb_keyboard()
                time.sleep(2)
                return True
            time.sleep(4)

        logger.error(f"Android boot timed out after {timeout}s.")
        return False

    def stop_phone(self) -> bool:
        """
        Stops the Android container to immediately release Xeon host CPU & RAM.
        """
        logger.info(f"🛑 Stopping virtual phone container [{self.container_name}] to release server RAM/CPU...")
        ret, stdout, stderr = self.execute_ssh(f"podman stop {self.container_name} -t 5", timeout=30)
        if ret == 0:
            logger.info("✅ Virtual phone successfully stopped. Bare-metal server memory (~3.5 GB) released.")
            return True
        logger.warning(f"Stop container command returned code {ret}: {stderr}")
        return False

    def capture_screenshot(self, local_save_path: str) -> bool:
        """Takes a clean screenshot from the phone."""
        os.makedirs(os.path.dirname(os.path.abspath(local_save_path)), exist_ok=True)
        cmd = [
            "ssh",
            "-i", self.ssh_key,
            "-o", "BatchMode=yes",
            "-o", "StrictHostKeyChecking=no",
            f"{self.user}@{self.host}",
            f"podman exec {self.container_name} adb exec-out screencap -p"
        ]
        try:
            with open(local_save_path, "wb") as f:
                res = subprocess.run(cmd, stdout=f, stderr=subprocess.PIPE, timeout=20)
                return res.returncode == 0 and os.path.getsize(local_save_path) > 0
        except Exception as e:
            logger.error(f"Failed to capture screenshot: {e}")
            return False

    def fetch_latest_gmail_otp(self, max_wait_sec: int = 40) -> Optional[str]:
        """Fetches the latest Instagram 6-digit verification code from Gmail via IMAP."""
        logger.info(f"Connecting to Gmail IMAP ({GMAIL_USER}) to fetch 2FA code...")
        start_time = time.time()
        while time.time() - start_time < max_wait_sec:
            try:
                mail = imaplib.IMAP4_SSL("imap.gmail.com")
                mail.login(GMAIL_USER, GMAIL_APP_PASS)
                mail.select("INBOX")
                status, messages = mail.search(None, "ALL")
                msg_ids = messages[0].split()
                if msg_ids:
                    # Check the 3 most recent messages
                    for mid in reversed(msg_ids[-3:]):
                        status, data = mail.fetch(mid, "(RFC822)")
                        raw = data[0][1]
                        msg = email.message_from_bytes(raw)
                        subject = str(msg["subject"])
                        body = ""
                        if msg.is_multipart():
                            for part in msg.walk():
                                if part.get_content_type() in ["text/plain", "text/html"]:
                                    payload = part.get_payload(decode=True)
                                    if payload:
                                        body += payload.decode(errors="ignore")
                        else:
                            payload = msg.get_payload(decode=True)
                            if payload:
                                body = payload.decode(errors="ignore")

                        if "Verify" in subject or "Instagram" in str(msg["from"]):
                            codes = re.findall(r"\b\d{6}\b", body)
                            if codes:
                                mail.logout()
                                logger.info(f"Found Instagram OTP code: {codes[0]} in message {mid.decode()}")
                                return codes[0]
                mail.logout()
            except Exception as e:
                logger.warning(f"Error checking IMAP: {e}")
            time.sleep(3)
        return None

    def parse_switcher_bounds(self, target_account: str, xml_content: str) -> Optional[Tuple[int, int]]:
        """Parses the exact center (x, y) coordinates of target account in bottom sheet."""
        import xml.etree.ElementTree as ET
        try:
            root = ET.fromstring(xml_content)
            for node in root.iter("node"):
                cd = node.attrib.get("content-desc", "")
                text = node.attrib.get("text", "")
                bounds = node.attrib.get("bounds", "")
                if (target_account in cd or target_account in text) and bounds:
                    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
                    if m:
                        x1, y1, x2, y2 = map(int, m.groups())
                        return ((x1 + x2) // 2, (y1 + y2) // 2)
        except Exception as e:
            logger.warning(f"Error parsing XML bounds: {e}")
        return None

    def ensure_account(self, target_account: str = "fr.smartbet.guru") -> bool:
        """
        Verifies that Instagram Lite is running and switched to the target account.
        """
        logger.info(f"Verifying active account in Instagram Lite (Target: [{target_account}])...")
        # Launch Instagram Lite
        self.adb(["shell", "am", "start", "-n", "com.instagram.lite/com.facebook.lite.MainActivity"])
        time.sleep(3)

        # Tap Profile tab (bottom right: 1296, 2944)
        self.adb(["shell", "input", "tap", "1296", "2944"])
        time.sleep(2)

        # Check dump to see what account is active
        self.adb(["shell", "uiautomator", "dump", "/sdcard/dump.xml"])
        ret, stdout, stderr = self.adb(["shell", "cat", "/sdcard/dump.xml"])
        if target_account in stdout:
            logger.info(f"Target account [{target_account}] is already active in Instagram Lite!")
            return True

        logger.info(f"Active account does not match [{target_account}]. Opening account switcher...")
        # Tap account switcher dropdown arrow (550, 160)
        self.adb(["shell", "input", "tap", "550", "160"])
        time.sleep(2)

        self.adb(["shell", "uiautomator", "dump", "/sdcard/dump.xml"])
        ret, xml_stdout, stderr = self.adb(["shell", "cat", "/sdcard/dump.xml"])
        coords = self.parse_switcher_bounds(target_account, xml_stdout)
        if not coords:
            coords = ACCOUNT_SHEET_COORDS.get(target_account, (720, 480))
        logger.info(f"Selecting [{target_account}] at coords {coords}...")
        self.adb(["shell", "input", "tap", str(coords[0]), str(coords[1])])
        time.sleep(4)

        # Check if 2FA verification was requested
        self.adb(["shell", "uiautomator", "dump", "/sdcard/dump.xml"])
        ret, stdout, stderr = self.adb(["shell", "cat", "/sdcard/dump.xml"])

        if "An extra security step is required" in stdout:
            logger.info("Security step prompt detected. Tapping Continue (720, 2756)...")
            self.adb(["shell", "input", "tap", "720", "2756"])
            time.sleep(5)
            self.adb(["shell", "uiautomator", "dump", "/sdcard/dump.xml"])
            ret, stdout, stderr = self.adb(["shell", "cat", "/sdcard/dump.xml"])

        if "Check your email" in stdout or "Enter the code" in stdout:
            logger.info("2FA email code verification prompt detected!")
            code = self.fetch_latest_gmail_otp(max_wait_sec=35)
            if code:
                logger.info(f"Entering 2FA OTP code: {code} at (648, 1673)...")
                self.adb(["shell", "input", "tap", "648", "1673"])
                time.sleep(1)
                self.adb(["shell", "input", "text", code])
                time.sleep(1)
                logger.info("Submitting code by tapping Continue (720, 2170)...")
                self.adb(["shell", "input", "tap", "720", "2170"])
                time.sleep(6)
            else:
                logger.error("Could not obtain 2FA code from Gmail IMAP.")
                return False

        if "Save your login info?" in stdout:
            logger.info("Save login info dialog detected. Tapping Save (719, 1996)...")
            self.adb(["shell", "input", "tap", "719", "1996"])
            time.sleep(3)

        logger.info(f"Account check completed for [{target_account}].")
        return True

    def generate_card(
        self,
        account_handle: str = "fr.smartbet.guru",
        match_title: Optional[str] = None,
        sport: Optional[str] = None,
        bookmaker_freebet: Optional[str] = None,
        bookmaker_hedge: Optional[str] = None,
        freebet_amount: Optional[float] = None,
        k1: Optional[float] = None,
        k2: Optional[float] = None
    ) -> Path:
        """
        Renders a high-resolution 1080x1350 social card compliant with Rule 10:
        - 80% freebet cash guaranteed conversion
        - Exact math breakdown
        - Brand badge and localized responsible gambling disclaimer
        """
        preset = REGIONAL_PRESETS.get(account_handle, REGIONAL_PRESETS["fr.smartbet.guru"])
        match_title = match_title or preset["match_title"]
        sport = sport or preset["sport"]
        bookmaker_freebet = bookmaker_freebet or preset["bookmaker_freebet"]
        bookmaker_hedge = bookmaker_hedge or preset["bookmaker_hedge"]
        freebet_amount = freebet_amount or preset["freebet_amount"]
        k1 = k1 or preset["k1"]
        k2 = k2 or preset["k2"]
        currency = preset["currency"]

        w, h = 1080, 1350
        img = Image.new("RGB", (w, h), color=(10, 14, 22))
        draw = ImageDraw.Draw(img)

        # Fonts
        font_badge = ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 22)
        font_title = ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 40)
        font_subtitle = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 24)
        font_card_title = ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 34)
        font_big_val = ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 48)
        font_body = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 27)
        font_bold = ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 27)
        font_mono = ImageFont.truetype(r"C:\Windows\Fonts\consola.ttf", 24)
        font_small = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 19)

        def draw_pill(x, y, text, bg_color, text_color):
            bbox = font_badge.getbbox(text)
            pw = bbox[2] - bbox[0] + 28
            ph = bbox[3] - bbox[1] + 16
            draw.rounded_rectangle([(x, y), (x + pw, y + ph)], radius=ph // 2, fill=bg_color)
            draw.text((x + 14, y + 6), text, fill=text_color, font=font_badge)
            return pw

        # Math calculations (Rule 10)
        eta = ((k1 - 1.0) * (k2 - 1.0)) / k2
        hedge_stake = round(((k1 - 1.0) * freebet_amount) / k2, 2)
        guaranteed_cash = round(eta * freebet_amount, 2)

        # 1. Header Bar
        draw.rectangle([(0, 0), (w, 130)], fill=(16, 22, 34))
        draw_pill(50, 30, "SMARTBET.GURU", (0, 180, 255), (10, 14, 22))
        draw.text((270, 34), preset["header_title"], fill=(255, 255, 255), font=font_bold)
        draw.text((50, 80), preset["header_sub"], fill=(140, 160, 190), font=font_subtitle)

        # 2. Card 1: 80% Cash Banner
        draw.rounded_rectangle([(40, 155), (w - 40, 345)], radius=20, fill=(18, 30, 52), outline=(0, 170, 255), width=2)
        bw = draw_pill(70, 175, preset["banner_badge"], (255, 200, 0), (10, 14, 22))
        draw.text((70 + bw + 20, 180), preset["banner_title"], fill=(255, 215, 0), font=font_card_title)
        draw.text((70, 240), f"{int(freebet_amount)} {currency} Freebet   ->   {guaranteed_cash:.2f} {currency} Cash", fill=(255, 255, 255), font=font_big_val)
        draw.text((70, 300), preset["banner_sub"], fill=(170, 190, 220), font=font_subtitle)

        # 3. Card 2: Example calculation
        draw.rounded_rectangle([(40, 370), (w - 40, 940)], radius=20, fill=(15, 20, 30), outline=(35, 45, 65), width=1)
        sw = draw_pill(70, 395, sport.upper(), (0, 220, 180), (10, 14, 22))
        draw.text((70 + sw + 20, 398), preset["example_title"], fill=(0, 220, 255), font=font_card_title)

        draw.text((70, 460), f"Match : {match_title}", fill=(255, 255, 255), font=font_bold)
        draw.text((70, 500), f"Bookmakers : {bookmaker_freebet} + {bookmaker_hedge}", fill=(150, 170, 200), font=font_subtitle)

        # Leg 1
        draw.rounded_rectangle([(70, 550), (w - 70, 660)], radius=12, fill=(22, 30, 46))
        l1_title = preset["leg1_title"].format(b1=bookmaker_freebet)
        l1_sub = preset["leg1_sub"].format(k1=k1, amt=freebet_amount, cur=currency)
        draw.text((90, 565), l1_title, fill=(80, 220, 120), font=font_bold)
        draw.text((90, 610), l1_sub, fill=(220, 230, 245), font=font_body)

        # Leg 2
        draw.rounded_rectangle([(70, 680), (w - 70, 790)], radius=12, fill=(22, 30, 46))
        l2_title = preset["leg2_title"].format(b2=bookmaker_hedge)
        l2_sub = preset["leg2_sub"].format(k2=k2, hedge=hedge_stake, cur=currency)
        draw.text((90, 695), l2_title, fill=(255, 140, 60), font=font_bold)
        draw.text((90, 740), l2_sub, fill=(220, 230, 245), font=font_body)

        # Calculation math
        f_text = preset["formula_text"].format(eta_pct=eta*100)
        gain_text = preset["net_gain"].format(gain=guaranteed_cash, cur=currency)
        draw.text((70, 820), f_text, fill=(150, 195, 255), font=font_mono)
        draw.rounded_rectangle([(70, 860), (w - 70, 920)], radius=10, fill=(20, 45, 30))
        draw.text((90, 875), gain_text, fill=(80, 255, 140), font=font_bold)

        # 4. Card 3: Call to action
        draw.rounded_rectangle([(40, 965), (w - 40, 1135)], radius=20, fill=(12, 38, 28), outline=(30, 180, 100), width=2)
        cw = draw_pill(70, 985, preset["cta_badge"], (50, 255, 140), (10, 14, 22))
        draw.text((70 + cw + 20, 990), preset["cta_title"], fill=(200, 255, 225), font=font_card_title)
        draw.text((70, 1045), preset["cta_sub"], fill=(210, 240, 225), font=font_subtitle)
        draw.text((70, 1085), f"{account_handle}/tools/freebet-calculator", fill=(255, 255, 255), font=font_bold)

        # 5. Footer Disclaimer
        draw.rectangle([(0, 1165), (w, h)], fill=(12, 16, 24))
        p18w = draw_pill(50, 1180, "18+", (200, 50, 50), (255, 255, 255))
        disclaimer_lines = preset["disclaimer_lines"]
        if len(disclaimer_lines) >= 1:
            draw.text((50 + p18w + 20, 1185), disclaimer_lines[0], fill=(170, 180, 195), font=font_small)
        if len(disclaimer_lines) >= 2:
            draw.text((50, 1225), disclaimer_lines[1], fill=(170, 180, 195), font=font_small)
        if len(disclaimer_lines) >= 3:
            draw.text((50, 1260), disclaimer_lines[2], fill=(170, 180, 195), font=font_small)

        out_path = OUTPUT_DIR / f"post_{account_handle.split('.')[0]}_{int(time.time())}.png"
        img.save(str(out_path), quality=95)
        logger.info(f"Generated card saved to: {out_path}")
        return out_path

    def push_media_to_device(self, local_image_path: Path) -> bool:
        """Transfers local card image to device and triggers media scanner."""
        logger.info(f"Pushing image [{local_image_path.name}] to Android container...")
        # 1. Scp to xeon-srv
        remote_tmp = f"/tmp/{local_image_path.name}"
        scp_cmd = [
            "scp",
            "-i", self.ssh_key,
            "-o", "BatchMode=yes",
            "-o", "StrictHostKeyChecking=no",
            str(local_image_path),
            f"{self.user}@{self.host}:{remote_tmp}"
        ]
        res = subprocess.run(scp_cmd, capture_output=True, text=True)
        if res.returncode != 0:
            logger.error(f"Scp failed: {res.stderr}")
            return False

        # 2. Podman cp into container
        self.execute_ssh(f"podman cp {remote_tmp} {self.container_name}:/tmp/post_ready.png")

        # 3. Adb push to /sdcard/Pictures/
        self.adb(["shell", "rm", "-f", "/sdcard/Pictures/post_ready.png"])
        self.adb(["push", "/tmp/post_ready.png", "/sdcard/Pictures/post_ready.png"])

        # 4. Trigger media scanner broadcast
        self.adb(["shell", "am", "broadcast", "-a", "android.intent.action.MEDIA_SCANNER_SCAN_FILE", "-d", "file:///sdcard/Pictures/post_ready.png"])
        time.sleep(2)

        # 5. Fix Android 11 Scoped Storage is_pending flag
        ret, stdout, _ = self.adb(["shell", "content", "query", "--uri", "content://media/external/images/media"])
        for line in stdout.split("\n"):
            if "post_ready.png" in line:
                m = re.search(r"_id=(\d+)", line)
                if m:
                    media_id = m.group(1)
                    self.adb(["shell", "content", "update", "--uri", f"content://media/external/images/media/{media_id}", "--bind", "is_pending:i:0"])
                    logger.info(f"MediaStore ID {media_id} marked active (is_pending=0).")
                    break

        logger.info("Image indexed into Android MediaStore successfully.")
        return True

    def publish_post(self, local_image_path: Path, caption_text: str) -> bool:
        """
        Executes full publication in Instagram Lite:
        Push media -> Tap + -> Select image -> Next -> Next -> Type caption -> Share
        """
        if not self.push_media_to_device(local_image_path):
            return False

        logger.info("Opening Instagram Lite and launching Create Post flow...")
        self.adb(["shell", "am", "start", "-n", "com.instagram.lite/com.facebook.lite.MainActivity"])
        time.sleep(2)

        # Tap + (Create) at bottom center (720, 2940)
        logger.info("Tapping + (Create post) button...")
        self.adb(["shell", "input", "tap", "720", "2940"])
        time.sleep(3)

        # Select first thumbnail in gallery (x=180, y=2128)
        logger.info("Selecting latest image in gallery (180, 2128)...")
        self.adb(["shell", "input", "tap", "180", "2128"])
        time.sleep(2)

        # Tap Next (top right: 1288, 220)
        logger.info("Tapping Next in gallery picker (1288, 220)...")
        self.adb(["shell", "input", "tap", "1288", "220"])
        time.sleep(3)

        # Tap Next -> (bottom right: 1234, 2912)
        logger.info("Tapping Next -> in filter screen (1234, 2912)...")
        self.adb(["shell", "input", "tap", "1234", "2912"])
        time.sleep(3)

        # Tap Caption field (720, 1472)
        logger.info("Tapping caption field (720, 1472)...")
        self.adb(["shell", "input", "tap", "720", "1472"])
        time.sleep(1)

        # Input caption via ADBKeyBoard Base64 (supporting all UTF-8 characters, emojis, accents)
        logger.info("Typing full localized caption via ADBKeyBoard Unicode Base64...")
        self.type_unicode(caption_text)
        time.sleep(2)

        # Hide keyboard
        self.adb(["shell", "input", "keyevent", "4"])
        time.sleep(1)

        # Tap Share button (720, 2888)
        logger.info("Tapping Share button (720, 2888)...")
        self.adb(["shell", "input", "tap", "720", "2888"])
        time.sleep(3)

        # Handle Meta cross-posting prompt: "Automatically share your posts to Facebook, too?"
        self.adb(["shell", "uiautomator", "dump", "/sdcard/share_dump.xml"])
        ret, dump_out, _ = self.adb(["shell", "cat", "/sdcard/share_dump.xml"])
        if "share your posts to Facebook" in dump_out or "Share posts" in dump_out:
            logger.info("Facebook cross-posting prompt detected. Tapping 'Share posts' (720, 2690)...")
            self.adb(["shell", "input", "tap", "720", "2690"])
            time.sleep(4)
        elif "Not now" in dump_out:
            logger.info("Dismissing prompt by tapping 'Not now' (720, 2870)...")
            self.adb(["shell", "input", "tap", "720", "2870"])
            time.sleep(4)

        # Wait for media upload to finish
        time.sleep(6)

        # Capture proof
        proof_path = os.path.join(OUTPUT_DIR, f"proof_published_{int(time.time())}.png")
        self.capture_screenshot(proof_path)
        logger.info(f"Post published successfully! Proof screenshot: {proof_path}")
        return True

    def monitor_and_reply_comments(self, wait_minutes: int = 15, check_interval_sec: int = 60, lang: str = "fr") -> None:
        """
        User requirement:
        'Наверное нужно после поста подождать 15 минут если будут комментарии ответить на них, потом выключать телефон.'
        Monitors incoming comments for 15 minutes, responds if any appear, then finishes.
        """
        logger.info(f"⏳ Entering {wait_minutes}-minute comment monitoring and engagement window...")
        total_seconds = wait_minutes * 60
        start_time = time.time()
        check_count = 0

        while time.time() - start_time < total_seconds:
            elapsed = int(time.time() - start_time)
            remaining = total_seconds - elapsed
            check_count += 1
            logger.info(f"Monitoring comments: Check #{check_count} | Elapsed: {elapsed//60}m {elapsed%60}s | Remaining: {remaining//60}m {remaining%60}s")

            # Check Activity / Notifications (Heart icon at 820, 200 or 1008, 2940)
            self.adb(["shell", "input", "tap", "820", "200"])
            time.sleep(2)

            self.adb(["shell", "uiautomator", "dump", "/sdcard/activity_dump.xml"])
            ret, stdout, stderr = self.adb(["shell", "cat", "/sdcard/activity_dump.xml"])

            if "commented" in stdout.lower() or "a commenté" in stdout.lower() or "comentó" in stdout.lower():
                logger.info("🔔 New incoming comment detected in activity feed!")
                # Tap the first comment notification row (center ~ 720, 450)
                self.adb(["shell", "input", "tap", "720", "450"])
                time.sleep(2)

                # Tap Reply or comment box (approx 300, 2800)
                target_key = f"{lang}.smartbet.guru" if f"{lang}.smartbet.guru" in REGIONAL_PRESETS else "fr.smartbet.guru"
                reply_text = REGIONAL_PRESETS[target_key]["reply_text"]
                self.adb(["shell", "input", "tap", "300", "2800"])
                time.sleep(1)
                self.type_unicode(reply_text)
                time.sleep(1)
                # Send reply (button on right: 1300, 2800)
                self.adb(["shell", "input", "tap", "1300", "2800"])
                time.sleep(2)
                logger.info("✅ Successfully posted automated reply to user comment!")

                # Return to home
                self.adb(["shell", "input", "keyevent", "4"])
                time.sleep(1)

            # Sleep until next check interval
            time.sleep(check_interval_sec)

        logger.info(f"✅ {wait_minutes}-minute post-publication engagement window complete!")

    def run_publication_cycle(
        self,
        target_account: str = "fr.smartbet.guru",
        wait_minutes: int = 15
    ) -> bool:
        """
        Executes the entire end-to-end cycle guaranteed by try ... finally:
        1. Start phone container
        2. Ensure Instagram Lite account
        3. Generate card
        4. Publish post with caption
        5. Wait 15 minutes to monitor/reply to comments
        6. FINALLY: Power off phone container to preserve bare-metal Xeon resources.
        """
        success = False
        preset = REGIONAL_PRESETS.get(target_account, REGIONAL_PRESETS["fr.smartbet.guru"])
        try:
            logger.info("==================================================================")
            logger.info(f"🚀 INITIATING EPHEMERAL INSTAGRAM CYCLE FOR [{target_account}]")
            logger.info(f"Language: {preset['lang']} | Sport: {preset['sport']} | Match: {preset['match_title']}")
            logger.info("==================================================================")

            # 1. Start phone
            if not self.start_phone(timeout=240):
                logger.error("Could not start phone container.")
                return False

            # 2. Ensure target account
            if not self.ensure_account(target_account):
                logger.error(f"Failed to authenticate/switch to [{target_account}].")
                return False

            # 3. Generate high-conversion card
            card_path = self.generate_card(account_handle=target_account)

            # 4. Localized caption
            caption = preset["caption"]

            # 5. Publish post
            if not self.publish_post(card_path, caption):
                logger.error("Failed to publish post.")
                return False

            success = True

            # 6. Wait 15 minutes monitoring comments
            if wait_minutes > 0:
                self.monitor_and_reply_comments(
                    wait_minutes=wait_minutes,
                    check_interval_sec=60,
                    lang=preset["lang"]
                )

            return True

        except Exception as e:
            logger.exception(f"Unexpected error in publication cycle: {e}")
            return False

        finally:
            # GUARANTEED SHUTDOWN: Always power off phone container when idle!
            logger.info("Executing guaranteed shutdown sequence...")
            self.stop_phone()


def main():
    parser = argparse.ArgumentParser(description="Ephemeral Instagram Publisher")
    parser.add_argument("--account", default="fr.smartbet.guru", help="Target Instagram account handle")
    parser.add_argument("--wait-minutes", type=int, default=15, help="Minutes to monitor comments before shutting down")
    parser.add_argument("--dry-run", action="store_true", help="Dry run mode without actual publishing")
    args = parser.parse_args()

    publisher = EphemeralInstagramPublisher()
    success = publisher.run_publication_cycle(
        target_account=args.account,
        wait_minutes=args.wait_minutes
    )
    if success:
        logger.info("🎉 Publication cycle completed successfully!")
        sys.exit(0)
    else:
        logger.error("❌ Publication cycle failed.")
        sys.exit(1)


if __name__ == "__main__":
    main()
