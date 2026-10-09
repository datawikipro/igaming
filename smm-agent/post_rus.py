import os
import re
import sys
import time
import base64
import subprocess
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

DEVICE = "100.78.183.101:5555"
STORAGE = Path("smm-agent/storage").resolve()
STORAGE.mkdir(parents=True, exist_ok=True)

def adb(*args, timeout=30):
    cmd = ["adb", "-s", DEVICE] + list(args)
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout)
    return res.stdout.strip()

def ensure_connected():
    subprocess.run(["adb", "connect", DEVICE], capture_output=True, text=True)

def type_unicode(text):
    b64 = base64.b64encode(text.encode("utf-8")).decode("utf-8")
    adb("shell", "am", "broadcast", "-a", "ADB_INPUT_B64", "--es", "msg", b64)

def generate_rus_card():
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
    draw.text((310, 38), "Математический радар ставок", fill=(255, 255, 255), font=font_bold)
    draw.text((40, 90), "Matched Betting & Гарантированная конверсия фрибета SNR (eta = 81.3%)", fill=(140, 160, 190), font=font_subtitle)
    
    # Banner 80% Cash
    draw.rounded_rectangle([(40, 155), (w - 40, 345)], radius=20, fill=(18, 30, 52), outline=(0, 200, 120), width=2)
    draw.rounded_rectangle([(70, 175), (340, 220)], radius=20, fill=(255, 200, 0))
    draw.text((85, 182), "КОНВЕРСИЯ ФРИБЕТА", fill=(10, 14, 22), font=ImageFont.truetype(r"C:\Windows\Fonts\segoeuib.ttf", 20))
    draw.text((360, 180), "80% ГАРАНТИРОВАННЫЙ КЭШ", fill=(255, 215, 0), font=font_bold)
    draw.text((70, 240), "3 000 ₽ Фрибет   ->   2 438 ₽ Чистыми", fill=(255, 255, 255), font=font_big)
    draw.text((70, 300), "Математическое перекрытие: без азарта и риска слива бонуса.", fill=(170, 190, 220), font=font_subtitle)
    
    # Calculation Box
    draw.rounded_rectangle([(40, 370), (w - 40, 940)], radius=20, fill=(15, 20, 30), outline=(35, 45, 65), width=1)
    draw.text((70, 395), "МАТЧ : Зенит vs Спартак Москва (РПЛ)", fill=(0, 220, 255), font=font_bold)
    
    # Leg 1
    draw.rounded_rectangle([(70, 460), (w - 70, 570)], radius=12, fill=(22, 30, 46))
    draw.text((90, 475), "Ставка 1 (Фрибет SNR) : Тотал больше 2.5 на Фонбет", fill=(80, 220, 120), font=font_bold)
    draw.text((90, 520), "Кэф K1 = 5.20 | Фрибет = 3 000 ₽ (Бонус от БК)", fill=(220, 230, 245), font=font_body)
    
    # Leg 2
    draw.rounded_rectangle([(70, 590), (w - 70, 700)], radius=12, fill=(22, 30, 46))
    draw.text((90, 605), "Ставка 2 (Перекрытие) : Тотал меньше 2.5 на Винлайн", fill=(255, 140, 60), font=font_bold)
    draw.text((90, 650), "Кэф K2 = 1.24 | Сумма = 10 161 ₽ (Реальные деньги)", fill=(220, 230, 245), font=font_body)
    
    # Formula & Profit
    draw.text((70, 730), "SNR Формула: eta = (K1 - 1)*(K2 - 1) / K2 = 81.3%", fill=(150, 195, 255), font=font_mono)
    draw.rounded_rectangle([(70, 770), (w - 70, 840)], radius=10, fill=(20, 45, 30))
    draw.text((90, 785), "Чистая прибыль: +2 438.71 ₽ при ЛЮБОМ счете матча !", fill=(80, 255, 140), font=font_bold)
    
    # CTA Box
    draw.rounded_rectangle([(40, 880), (w - 40, 1140)], radius=20, fill=(12, 38, 28), outline=(30, 180, 100), width=2)
    draw.text((70, 910), "БЕСПЛАТНЫЙ КАЛЬКУЛЯТОР ФРИБЕТОВ", fill=(200, 255, 225), font=font_bold)
    draw.text((70, 970), "Линии 52 букмекеров и автоматический расчет перекрытия:", fill=(210, 240, 225), font=font_subtitle)
    draw.text((70, 1020), "rus.smartbet.guru/tools/freebet-calculator", fill=(255, 255, 255), font=font_big)
    
    # Disclaimer
    draw.rectangle([(0, 1180), (w, h)], fill=(12, 16, 24))
    draw.text((50, 1200), "18+ | Азартные игры несут финансовые риски. Играйте ответственно.", fill=(170, 180, 195), font=font_small)
    draw.text((50, 1235), "Не ставьте больше, чем готовы потерять.", fill=(170, 180, 195), font=font_small)
    
    card_path = STORAGE / "rus_coupon.png"
    img.save(str(card_path), quality=95)
    print(f"RUS card generated: {card_path}")
    return card_path

def push_and_index_media(card_path):
    ensure_connected()
    print("Pushing card to /sdcard/Pictures/rus_coupon.png...")
    subprocess.run(["adb", "-s", DEVICE, "push", str(card_path), "/sdcard/Pictures/rus_coupon.png"], capture_output=True, text=True)
    
    print("Broadcasting MEDIA_SCANNER_SCAN_FILE...")
    adb("shell", "am", "broadcast", "-a", "android.intent.action.MEDIA_SCANNER_SCAN_FILE", "-d", "file:///sdcard/Pictures/rus_coupon.png")
    time.sleep(2)
    
    res = adb("shell", "content", "query", "--uri", "content://media/external/images/media")
    for line in res.split("\n"):
        if "rus_coupon.png" in line:
            m = re.search(r"_id=(\d+)", line)
            if m:
                media_id = m.group(1)
                adb("shell", "content", "update", "--uri", f"content://media/external/images/media/{media_id}", "--bind", "is_pending:i:0")
                print(f"MediaStore ID {media_id} active.")
                break
    print("Media successfully pushed and indexed.")

def publish_rus_post():
    ensure_connected()
    card = generate_rus_card()
    push_and_index_media(card)
    
    caption = (
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
    
    print("\n1. Tapping '+' (New post) in bottom bar (540, 1704)...")
    adb("shell", "input", "tap", "540", "1704")
    time.sleep(3)
    
    print("2. Selecting latest photo in gallery (180, 800)...")
    adb("shell", "input", "tap", "180", "800")
    time.sleep(2)
    
    print("3. Tapping 'Next' in picker (960, 165)...")
    adb("shell", "input", "tap", "960", "165")
    time.sleep(3)
    
    print("4. Tapping 'Next' in filters (850, 1680)...")
    adb("shell", "input", "tap", "850", "1680")
    time.sleep(3)
    
    print("5. Tapping caption field (540, 1104)...")
    adb("shell", "input", "tap", "540", "1104")
    time.sleep(1.5)
    
    print("6. Typing localized caption via Unicode ADBKeyBoard...")
    type_unicode(caption)
    time.sleep(2)
    
    print("7. Hiding keyboard...")
    adb("shell", "input", "keyevent", "4")
    time.sleep(1.5)
    
    print("8. Tapping Share button (540, 1662)...")
    adb("shell", "input", "tap", "540", "1662")
    time.sleep(6)
    
    print("9. Checking for any prompts and taking confirmation screenshot...")
    adb("shell", "screencap", "-p", "/sdcard/rus_published.png")
    out = STORAGE / "rus_published.png"
    subprocess.run(["adb", "-s", DEVICE, "pull", "/sdcard/rus_published.png", str(out)], capture_output=True, text=True)
    print(f"Saved confirmation screenshot to: {out}")

if __name__ == "__main__":
    publish_rus_post()
