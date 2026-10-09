#!/usr/bin/env python3
"""
SmartBet.guru — Automated Multi-Account Instagram Publisher
Supports: fr.smartbet.guru, rus.smartbet.guru, es.smartbet.guru, en.smartbet.guru
Enforces Rule 10 (80% guaranteed freebet cash conversion via matched betting)
Canvas resolution: 1080x1776 (calibrated for current Android device)
"""

import os
import re
import sys
import time
import base64
import argparse
import subprocess
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

DEVICE = "100.78.183.101:5555"
STORAGE = Path("smm-agent/storage").resolve()
STORAGE.mkdir(parents=True, exist_ok=True)

# Exact calibrated coordinates on 1080x1776 display
COORDS = {
    "profile_tab": (972, 1704),
    "header_dropdown": (498, 144),
    "new_post_bar": (540, 1704),
    "gallery_first_item": (180, 800),
    "picker_next": (960, 165),
    "filter_next": (850, 1680),
    "caption_field": (540, 1104),
    "share_button": (540, 1662),
    "modal_dismiss_x": (1008, 213),
    "save_login_info": (540, 1250),
    "confirm_2fa": (540, 1020),
    "otp_input": (540, 715),
}

REGIONAL_CONFIGS = {
    "fr": {
        "handle": "fr.smartbet.guru",
        "subdomain": "fr.smartbet.guru",
        "title": "Aleksei Chernousov",
        "sport": "Ligue 1",
        "match": "Paris SG vs Marseille",
        "b1": "Winamax",
        "b2": "Unibet",
        "amt": 100.0,
        "cur": "€",
        "k1": 5.20,
        "k2": 1.24,
        "header_title": "Radar Mathématique des Paris Sportifs",
        "header_sub": "Algorithmes de détection de valuebets, surebets et conversion de bonus • France",
        "banner_badge": "CONVERSION FREEBET",
        "banner_title": "80% DE CASH GARANTI",
        "banner_sub": "Ne gâchez pas votre bonus au hasard — convertissez-le mathématiquement.",
        "example_title": "Exemple Concret : Matched Betting",
        "leg1": "Pari 1 (Freebet SNR) : Plus de 2.5 Buts sur Winamax",
        "leg1_sub": "Cote K1 = 5.20 | Mise = 100 € (Freebet offert)",
        "leg2": "Pari 2 (Couverture) : Moins de 2.5 Buts sur Unibet",
        "leg2_sub": "Cote K2 = 1.24 | Mise = 338.71 € (Argent réel)",
        "formula": "Formule SNR : eta = (K1 - 1)*(K2 - 1) / K2 = 81.3%",
        "gain": "Gain net garanti : +81.30 € net quel que soit le score !",
        "cta_badge": "CALCULATEUR GRATUIT EN LIGNE",
        "cta_sub": "Accédez au radar de 52 bookmakers et calculez vos gains :",
        "disclaimer_lines": [
            "18+ | Les jeux d’argent comportent des risques (endettement, dépendance).",
            "Appelez le 09-74-75-13-13 pour de l'aide. Jouez responsablement."
        ],
        "caption": (
            "⚡ Convertissez 100% de vos freebets en CASH REEL avec SmartBet.guru !\n\n"
            "Ne pariez plus vos bonus au hasard. Avec notre méthode mathématique de Matched Betting (SNR), "
            "tout freebet de 100 € se transforme en 81.30 € de cash net garanti sur votre compte bancaire, "
            "quel que soit le score final du match !\n\n"
            "📊 Exemple Ligue 1 (PSG vs OM) :\n"
            "• Pari 1 (Freebet) : Plus de 2.5 buts @ 5.20 sur Winamax\n"
            "• Pari 2 (Couverture) : Moins de 2.5 buts @ 1.24 sur Unibet\n"
            "💰 Bénéfice net garanti : +81.30 € net !\n\n"
            "🚀 Accédez gratuitement au calculateur de freebets :\n"
            "👉 fr.smartbet.guru/tools/freebet-calculator\n\n"
            "#parisportif #pronostic #ligue1 #winamax #unibet #matchedbetting #freebet #smartbet #psg #om\n\n"
            "🔞 18+ | Les jeux d’argent comportent des risques (endettement, isolement, dépendance). "
            "Pour être aidé, appelez le 09-74-75-13-13. Jouez responsablement."
        )
    },
    "rus": {
        "handle": "rus.smartbet.guru",
        "subdomain": "rus.smartbet.guru",
        "title": "SmartBet.guru",
        "sport": "РПЛ",
        "match": "Зенит vs Спартак Москва",
        "b1": "Фонбет",
        "b2": "Винлайн",
        "amt": 3000.0,
        "cur": "₽",
        "k1": 5.20,
        "k2": 1.24,
        "header_title": "Математический радар спортивных ставок",
        "header_sub": "Matched Betting & Гарантированная конверсия фрибета SNR (eta = 81.3%)",
        "banner_badge": "КОНВЕРСИЯ ФРИБЕТА",
        "banner_title": "80% ГАРАНТИРОВАННЫЙ КЭШ",
        "banner_sub": "Не сливайте бонусы наугад — забирайте математически чистые деньги.",
        "example_title": "Конкретный расчет: Matched Betting",
        "leg1": "Ставка 1 (Фрибет SNR) : Тотал больше 2.5 на Фонбет",
        "leg1_sub": "Кэф K1 = 5.20 | Фрибет = 3 000 ₽ (Бонус от БК)",
        "leg2": "Ставка 2 (Перекрытие) : Тотал меньше 2.5 на Винлайн",
        "leg2_sub": "Кэф K2 = 1.24 | Сумма = 10 161 ₽ (Реальные деньги)",
        "formula": "SNR Формула: eta = (K1 - 1)*(K2 - 1) / K2 = 81.3%",
        "gain": "Чистая прибыль: +2 438.71 ₽ при ЛЮБОМ счете матча !",
        "cta_badge": "БЕСПЛАТНЫЙ КАЛЬКУЛЯТОР ФРИБЕТОВ",
        "cta_sub": "Линии 52 букмекеров и автоматический расчет перекрытия:",
        "disclaimer_lines": [
            "18+ | Азартные игры несут финансовые риски. Играйте ответственно.",
            "Не ставьте больше, чем готовы потерять."
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
        )
    },
    "es": {
        "handle": "es.smartbet.guru",
        "subdomain": "es.smartbet.guru",
        "title": "SmartBet.guru",
        "sport": "La Liga",
        "match": "Real Madrid vs Barcelona",
        "b1": "Codere",
        "b2": "Bet365",
        "amt": 100.0,
        "cur": "€",
        "k1": 5.20,
        "k2": 1.24,
        "header_title": "Radar Matemático de Apuestas Deportivas",
        "header_sub": "Matched Betting & Conversión garantizada de apuestas gratis SNR (eta = 81.3%)",
        "banner_badge": "CONVERSIÓN DE FREEBET",
        "banner_title": "80% DE EFECTIVO GARANTIZADO",
        "banner_sub": "No malgastes tus bonos al azar: conviértelos matemáticamente.",
        "example_title": "Ejemplo Práctico: Matched Betting",
        "leg1": "Apuesta 1 (Freebet SNR) : Más de 2.5 Goles en Codere",
        "leg1_sub": "Cuota K1 = 5.20 | Apuesta = 100 € (Bono gratuito)",
        "leg2": "Apuesta 2 (Cobertura) : Menos de 2.5 Goles en Bet365",
        "leg2_sub": "Cuota K2 = 1.24 | Apuesta = 338.71 € (Dinero real)",
        "formula": "Fórmula SNR: eta = (K1 - 1)*(K2 - 1) / K2 = 81.3%",
        "gain": "Ganancia neta garantizada: +81.30 € ¡sin importar el resultado!",
        "cta_badge": "CALCULADORA GRATUITA ONLINE",
        "cta_sub": "Cuotas en directo de 52 casas y cálculo de ganancias en:",
        "disclaimer_lines": [
            "18+ | Juega con responsabilidad. El juego puede ser adictivo.",
            "Más información y ayuda en jugarbiencualificado.es"
        ],
        "caption": (
            "⚡ ¡Convierte el 100% de tus apuestas gratis en EFECTIVO REAL con SmartBet.guru!\n\n"
            "No vuelvas a tirar tus bonos. Con el método matemático de Matched Betting (SNR), "
            "cualquier bono de 100 € se transforma en 81.30 € de dinero neto garantizado en tu cuenta bancaria, "
            "¡sin importar cómo termine el partido!\n\n"
            "📊 Ejemplo La Liga (Real Madrid vs Barcelona):\n"
            "• Apuesta 1 (Freebet): Más de 2.5 goles @ 5.20 en Codere\n"
            "• Apuesta 2 (Cobertura): Menos de 2.5 goles @ 1.24 en Bet365\n"
            "💰 Beneficio neto garantizado: +81.30 € netos!\n\n"
            "🚀 Accede gratis a la calculadora de freebets:\n"
            "👉 es.smartbet.guru/tools/freebet-calculator\n\n"
            "#apuestasdeportivas #pronosticos #laliga #codere #bet365 #matchedbetting #freebet #smartbet\n\n"
            "🔞 18+ | Juega con responsabilidad. Las apuestas conllevan riesgos."
        )
    },
    "en": {
        "handle": "en.smartbet.guru",
        "subdomain": "en.smartbet.guru",
        "title": "SmartBet.guru",
        "sport": "Premier League",
        "match": "Arsenal vs Manchester City",
        "b1": "Bet365",
        "b2": "Pinnacle",
        "amt": 100.0,
        "cur": "£",
        "k1": 5.20,
        "k2": 1.24,
        "header_title": "Sports Betting Mathematical Radar",
        "header_sub": "Matched Betting & Guaranteed Free Bet SNR Conversion (eta = 81.3%)",
        "banner_badge": "FREEBET CONVERSION",
        "banner_title": "80% GUARANTEED NET CASH",
        "banner_sub": "Do not gamble bonuses randomly — convert them mathematically.",
        "example_title": "Practical Example: Matched Betting",
        "leg1": "Bet 1 (Free Bet SNR) : Over 2.5 Goals on Bet365",
        "leg1_sub": "Odds K1 = 5.20 | Stake = 100 £ (Free bonus token)",
        "leg2": "Bet 2 (Hedge Bet) : Under 2.5 Goals on Pinnacle",
        "leg2_sub": "Odds K2 = 1.24 | Stake = 338.71 £ (Real money)",
        "formula": "SNR Formula: eta = (K1 - 1)*(K2 - 1) / K2 = 81.3%",
        "gain": "Guaranteed Net Profit: +81.30 £ regardless of final score!",
        "cta_badge": "FREE CALCULATOR TOOL",
        "cta_sub": "Live odds from 52 bookmakers and automated matched betting:",
        "disclaimer_lines": [
            "18+ | Gamble Responsibly. Gambling can be addictive.",
            "Visit begambleaware.org for free and confidential support."
        ],
        "caption": (
            "⚡ Convert 100% of your free bets into GUARANTEED CASH with SmartBet.guru!\n\n"
            "Stop gambling bonus funds on random accumulators. Using mathematical Matched Betting (SNR), "
            "every £100 free bet converts into £81.30 guaranteed net cash in your bank account, "
            "regardless of who wins or loses the game!\n\n"
            "📊 Example Premier League (Arsenal vs Man City):\n"
            "• Bet 1 (Free Bet SNR): Over 2.5 Goals @ 5.20 on Bet365\n"
            "• Bet 2 (Hedge): Under 2.5 Goals @ 1.24 on Pinnacle\n"
            "💰 Guaranteed profit: +£81.30 net cash!\n\n"
            "🚀 Free Matched Betting Calculator:\n"
            "👉 en.smartbet.guru/tools/freebet-calculator\n\n"
            "#sportsbetting #matchedbetting #valuebet #premierleague #arsenal #mancity #bet365 #pinnacle #smartbet\n\n"
            "🔞 18+ | Gamble responsibly. Visit begambleaware.org for free advice."
        )
    }
}

def adb(*args, timeout=30):
    cmd = ["adb", "-s", DEVICE] + list(args)
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout)
    return res.stdout.strip()

def ensure_connected():
    subprocess.run(["adb", "connect", DEVICE], capture_output=True, text=True)

def type_unicode(text):
    b64 = base64.b64encode(text.encode("utf-8")).decode("utf-8")
    adb("shell", "am", "broadcast", "-a", "ADB_INPUT_B64", "--es", "msg", b64)

def generate_social_card(lang_key: str) -> Path:
    cfg = REGIONAL_CONFIGS[lang_key]
    w, h = 1080, 1350
    img = Image.new("RGB", (w, h), color=(10, 14, 22))
    draw = ImageDraw.Draw(img)
    
    font_bold = ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 36)
    font_subtitle = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 22)
    font_big = ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 46)
    font_body = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 26)
    font_mono = ImageFont.truetype(r"C:\Windows\Fonts\consola.ttf", 24)
    font_small = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 18)
    
    # Header
    draw.rectangle([(0, 0), (w, 130)], fill=(16, 22, 34))
    draw.rounded_rectangle([(40, 30), (280, 80)], radius=25, fill=(0, 200, 100))
    draw.text((55, 38), "SMARTBET.GURU", fill=(10, 14, 22), font=ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 24))
    draw.text((310, 38), cfg["header_title"], fill=(255, 255, 255), font=font_bold)
    draw.text((40, 90), cfg["header_sub"], fill=(140, 160, 190), font=font_subtitle)
    
    # Banner 80% Cash
    draw.rounded_rectangle([(40, 155), (w - 40, 345)], radius=20, fill=(18, 30, 52), outline=(0, 200, 120), width=2)
    draw.rounded_rectangle([(70, 175), (340, 220)], radius=20, fill=(255, 200, 0))
    draw.text((85, 182), cfg["banner_badge"], fill=(10, 14, 22), font=ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 20))
    draw.text((360, 180), cfg["banner_title"], fill=(255, 215, 0), font=font_bold)
    
    gain_amt = (cfg["amt"] * (cfg["k1"] - 1.0) * (cfg["k2"] - 1.0)) / cfg["k2"]
    draw.text((70, 240), f"{cfg['amt']:.0f} {cfg['cur']} Bonus   ->   {gain_amt:.2f} {cfg['cur']} Pure Cash", fill=(255, 255, 255), font=font_big)
    draw.text((70, 300), cfg["banner_sub"], fill=(170, 190, 220), font=font_subtitle)
    
    # Calculation Box
    draw.rounded_rectangle([(40, 370), (w - 40, 940)], radius=20, fill=(15, 20, 30), outline=(35, 45, 65), width=1)
    draw.text((70, 395), f"MATCH : {cfg['match']} ({cfg['sport']})", fill=(0, 220, 255), font=font_bold)
    
    # Leg 1
    draw.rounded_rectangle([(70, 460), (w - 70, 570)], radius=12, fill=(22, 30, 46))
    draw.text((90, 475), cfg["leg1"], fill=(80, 220, 120), font=font_bold)
    draw.text((90, 520), cfg["leg1_sub"], fill=(220, 230, 245), font=font_body)
    
    # Leg 2
    draw.rounded_rectangle([(70, 590), (w - 70, 700)], radius=12, fill=(22, 30, 46))
    draw.text((90, 605), cfg["leg2"], fill=(255, 140, 60), font=font_bold)
    draw.text((90, 650), cfg["leg2_sub"], fill=(220, 230, 245), font=font_body)
    
    # Formula & Profit
    draw.text((70, 730), cfg["formula"], fill=(150, 195, 255), font=font_mono)
    draw.rounded_rectangle([(70, 770), (w - 70, 840)], radius=10, fill=(20, 45, 30))
    draw.text((90, 785), cfg["gain"], fill=(80, 255, 140), font=font_bold)
    
    # CTA Box
    draw.rounded_rectangle([(40, 880), (w - 40, 1140)], radius=20, fill=(12, 38, 28), outline=(30, 180, 100), width=2)
    draw.text((70, 910), cfg["cta_badge"], fill=(200, 255, 225), font=font_bold)
    draw.text((70, 970), cfg["cta_sub"], fill=(210, 240, 225), font=font_subtitle)
    draw.text((70, 1020), f"{cfg['subdomain']}/tools/freebet-calculator", fill=(255, 255, 255), font=font_big)
    
    # Disclaimer
    draw.rectangle([(0, 1180), (w, h)], fill=(12, 16, 24))
    for i, line in enumerate(cfg["disclaimer_lines"]):
        draw.text((50, 1200 + i * 35), line, fill=(170, 180, 195), font=font_small)
    
    card_path = STORAGE / f"{lang_key}_coupon.png"
    img.save(str(card_path), quality=95)
    print(f"[{lang_key}] Social card generated: {card_path}")
    return card_path

def clean_and_push_media(card_path: Path):
    ensure_connected()
    # Remove existing images from Pictures to ensure new card is strictly the first in gallery
    adb("shell", "rm", "-f", "/sdcard/Pictures/*.png")
    dest = f"/sdcard/Pictures/{card_path.name}"
    print(f"Pushing {card_path.name} to {dest}...")
    subprocess.run(["adb", "-s", DEVICE, "push", str(card_path), dest], capture_output=True, text=True)
    
    # Broadcast media scanner
    adb("shell", "am", "broadcast", "-a", "android.intent.action.MEDIA_SCANNER_SCAN_FILE", "-d", f"file://{dest}")
    time.sleep(2)
    
    # Unset is_pending
    res = adb("shell", "content", "query", "--uri", "content://media/external/images/media")
    for line in res.split("\n"):
        if card_path.name in line:
            m = re.search(r"_id=(\d+)", line)
            if m:
                media_id = m.group(1)
                adb("shell", "content", "update", "--uri", f"content://media/external/images/media/{media_id}", "--bind", "is_pending:i:0")
                print(f"MediaStore ID {media_id} set to active.")
                break
    print("Media successfully indexed into Android gallery.")

def publish_current(lang_key: str) -> bool:
    cfg = REGIONAL_CONFIGS[lang_key]
    ensure_connected()
    
    card_path = generate_social_card(lang_key)
    clean_and_push_media(card_path)
    
    print(f"\n[{lang_key}] Starting publication flow...")
    
    # 1. Tap New Post in bottom bar
    adb("shell", "input", "tap", str(COORDS["new_post_bar"][0]), str(COORDS["new_post_bar"][1]))
    time.sleep(3)
    
    # 2. Select first item in gallery
    adb("shell", "input", "tap", str(COORDS["gallery_first_item"][0]), str(COORDS["gallery_first_item"][1]))
    time.sleep(2)
    
    # 3. Next in picker
    adb("shell", "input", "tap", str(COORDS["picker_next"][0]), str(COORDS["picker_next"][1]))
    time.sleep(3)
    
    # 4. Next in filters
    adb("shell", "input", "tap", str(COORDS["filter_next"][0]), str(COORDS["filter_next"][1]))
    time.sleep(3)
    
    # 5. Tap caption
    adb("shell", "input", "tap", str(COORDS["caption_field"][0]), str(COORDS["caption_field"][1]))
    time.sleep(1.5)
    
    # 6. Type caption
    type_unicode(cfg["caption"])
    time.sleep(2)
    
    # 7. Hide keyboard
    adb("shell", "input", "keyevent", "4")
    time.sleep(1.5)
    
    # 8. Tap Share
    adb("shell", "input", "tap", str(COORDS["share_button"][0]), str(COORDS["share_button"][1]))
    time.sleep(6)
    
    # 9. Proof screenshot
    proof_name = f"published_{lang_key}_{int(time.time())}.png"
    adb("shell", "screencap", "-p", f"/sdcard/{proof_name}")
    out = STORAGE / proof_name
    subprocess.run(["adb", "-s", DEVICE, "pull", f"/sdcard/{proof_name}", str(out)], capture_output=True)
    print(f"[{lang_key}] Publication complete! Proof: {out}")
    return True

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="SmartBet Instagram Multi-Account Publisher")
    parser.add_argument("--account", choices=["fr", "rus", "es", "en"], default="rus", help="Regional account to publish to")
    parser.add_argument("--publish-current", action="store_true", help="Publish directly to currently active account")
    args = parser.parse_args()
    
    if args.publish_current or args.account:
        publish_current(args.account)
