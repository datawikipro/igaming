import os
import re
import sys
import time
import subprocess
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

DEVICE = "100.78.183.101:5555"
STORAGE = Path("smm-agent/storage").resolve()
STORAGE.mkdir(parents=True, exist_ok=True)

def adb_cmd(*args):
    cmd = ["adb", "-s", DEVICE] + list(args)
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=20)
    return res.stdout.strip()

def ensure_device():
    subprocess.run(["adb", "connect", DEVICE], capture_output=True, text=True)

def generate_social_card(account="fr.smartbet.guru"):
    # Generate 1080x1350 Instagram portrait card
    w, h = 1080, 1350
    img = Image.new("RGB", (w, h), color=(10, 14, 22))
    draw = ImageDraw.Draw(img)
    
    # Try Windows fonts
    font_bold = ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 36)
    font_subtitle = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 22)
    font_big = ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 46)
    font_body = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 26)
    font_mono = ImageFont.truetype(r"C:\Windows\Fonts\consola.ttf", 24)
    font_small = ImageFont.truetype(r"C:\Windows\Fonts\segoeui.ttf", 18)
    
    # Header
    draw.rectangle([(0, 0), (w, 130)], fill=(16, 22, 34))
    draw.rounded_rectangle([(40, 30), (280, 80)], radius=25, fill=(0, 180, 255))
    draw.text((55, 38), "SMARTBET.GURU", fill=(10, 14, 22), font=ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 24))
    draw.text((310, 38), "Sports Betting Mathematical Radar", fill=(255, 255, 255), font=font_bold)
    draw.text((40, 90), "Matched Betting & Guaranteed Free Bet SNR Conversion (eta = 80%)", fill=(140, 160, 190), font=font_subtitle)
    
    # Banner 80% Cash
    draw.rounded_rectangle([(40, 155), (w - 40, 345)], radius=20, fill=(18, 30, 52), outline=(0, 170, 255), width=2)
    draw.rounded_rectangle([(70, 175), (320, 220)], radius=20, fill=(255, 200, 0))
    draw.text((85, 182), "CONVERSION 80%", fill=(10, 14, 22), font=ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 20))
    draw.text((340, 180), "GUARANTEED NET CASH", fill=(255, 215, 0), font=font_bold)
    draw.text((70, 240), "100 € Freebet   ->   81.30 € Pure Cash", fill=(255, 255, 255), font=font_big)
    draw.text((70, 300), "Mathematical matched betting: zero gamble, pure arbitrage proof.", fill=(170, 190, 220), font=font_subtitle)
    
    # Calculation Box
    draw.rounded_rectangle([(40, 370), (w - 40, 940)], radius=20, fill=(15, 20, 30), outline=(35, 45, 65), width=1)
    draw.text((70, 395), "MATCH : Paris SG vs Marseille (Ligue 1)", fill=(0, 220, 255), font=font_bold)
    
    # Leg 1
    draw.rounded_rectangle([(70, 460), (w - 70, 570)], radius=12, fill=(22, 30, 46))
    draw.text((90, 475), "Pari 1 (Freebet SNR) : Plus de 2.5 Buts sur Winamax", fill=(80, 220, 120), font=font_bold)
    draw.text((90, 520), "Cote K1 = 5.20 | Mise = 100 € (Freebet offert)", fill=(220, 230, 245), font=font_body)
    
    # Leg 2
    draw.rounded_rectangle([(70, 590), (w - 70, 700)], radius=12, fill=(22, 30, 46))
    draw.text((90, 605), "Pari 2 (Couverture) : Moins de 2.5 Buts sur Unibet", fill=(255, 140, 60), font=font_bold)
    draw.text((90, 650), "Cote K2 = 1.24 | Mise = 338.71 € (Argent reel)", fill=(220, 230, 245), font=font_body)
    
    # Formula & Profit
    draw.text((70, 730), "SNR Formula: eta = (K1 - 1)*(K2 - 1) / K2 = 81.3%", fill=(150, 195, 255), font=font_mono)
    draw.rounded_rectangle([(70, 770), (w - 70, 840)], radius=10, fill=(20, 45, 30))
    draw.text((90, 785), "Gain Net Garanti: +81.30 € quel que soit le score final !", fill=(80, 255, 140), font=font_bold)
    
    # CTA Box
    draw.rounded_rectangle([(40, 880), (w - 40, 1140)], radius=20, fill=(12, 38, 28), outline=(30, 180, 100), width=2)
    draw.text((70, 910), "CALCULATEUR GRATUIT EN LIGNE", fill=(200, 255, 225), font=font_bold)
    draw.text((70, 970), "Accedez au radar de 52 bookmakers et calculez vos gains :", fill=(210, 240, 225), font=font_subtitle)
    draw.text((70, 1020), f"{account}/tools/freebet-calculator", fill=(255, 255, 255), font=font_big)
    
    # Disclaimer
    draw.rectangle([(0, 1180), (w, h)], fill=(12, 16, 24))
    draw.text((50, 1200), "18+ | Les jeux d'argent comportent des risques (endettement, dependance).", fill=(170, 180, 195), font=font_small)
    draw.text((50, 1235), "Appelez le 09-74-75-13-13 pour de l'aide. Jouez responsablement.", fill=(170, 180, 195), font=font_small)
    
    card_path = STORAGE / "test_coupon.png"
    img.save(str(card_path), quality=95)
    print(f"Social card generated: {card_path}")
    return card_path

def push_and_index_media(card_path):
    ensure_device()
    print("Pushing card to /sdcard/Pictures/coupon.png...")
    subprocess.run(["adb", "-s", DEVICE, "push", str(card_path), "/sdcard/Pictures/coupon.png"], capture_output=True, text=True)
    
    print("Broadcasting MEDIA_SCANNER_SCAN_FILE...")
    adb_cmd("shell", "am", "broadcast", "-a", "android.intent.action.MEDIA_SCANNER_SCAN_FILE", "-d", "file:///sdcard/Pictures/coupon.png")
    time.sleep(2)
    
    # Clear is_pending if any
    res = adb_cmd("shell", "content", "query", "--uri", "content://media/external/images/media")
    for line in res.split("\n"):
        if "coupon.png" in line:
            m = re.search(r"_id=(\d+)", line)
            if m:
                media_id = m.group(1)
                adb_cmd("shell", "content", "update", "--uri", f"content://media/external/images/media/{media_id}", "--bind", "is_pending:i:0")
                print(f"MediaStore ID {media_id} marked active (is_pending=0).")
                break
    print("Media successfully pushed and indexed.")

if __name__ == "__main__":
    p = generate_social_card()
    push_and_index_media(p)
