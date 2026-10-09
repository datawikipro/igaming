import base64
import subprocess
import time

DEVICE = "100.78.183.101:5555"

def adb_cmd(*args):
    cmd = ["adb", "-s", DEVICE] + list(args)
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=20)
    return res.stdout.strip()

def type_unicode(text):
    b64 = base64.b64encode(text.encode("utf-8")).decode("utf-8")
    adb_cmd("shell", "am", "broadcast", "-a", "ADB_INPUT_B64", "--es", "msg", b64)

caption = (
    "⚡ Convertissez 100% de vos freebets en CASH REEL avec SmartBet.guru !\n\n"
    "Ne pariez plus vos bonus au hasard. Avec notre methode mathematique de Matched Betting (SNR), "
    "tout freebet de 100 € se transforme en 81.30 € de cash net garanti sur votre compte bancaire, "
    "quel que soit le score final du match !\n\n"
    "📊 Exemple Ligue 1 (PSG vs OM) :\n"
    "• Pari 1 (Freebet) : Plus de 2.5 buts @ 5.20 sur Winamax\n"
    "• Pari 2 (Couverture) : Moins de 2.5 buts @ 1.24 sur Unibet\n"
    "💰 Benefice net garanti : +81.30 € net !\n\n"
    "🚀 Accedez gratuitement au calculateur de freebets :\n"
    "👉 fr.smartbet.guru/tools/freebet-calculator\n\n"
    "#parisportif #pronostic #ligue1 #winamax #unibet #matchedbetting #freebet #smartbet #psg #om\n\n"
    "🔞 18+ | Les jeux d’argent comportent des risques (endettement, isolement, dependance). "
    "Pour etre aide, appelez le 09-74-75-13-13. Jouez responsablement."
)

print("Tapping caption field (540, 1104)...")
adb_cmd("shell", "input", "tap", "540", "1104")
time.sleep(1.5)

print("Typing Unicode caption via AdbIME...")
type_unicode(caption)
time.sleep(2)

print("Hiding keyboard...")
adb_cmd("shell", "input", "keyevent", "4")
time.sleep(1.5)

print("Taking screenshot of post preview with caption...")
adb_cmd("shell", "screencap", "-p", "/sdcard/post_captioned.png")
subprocess.run(["adb", "-s", DEVICE, "pull", "/sdcard/post_captioned.png", "smm-agent/storage/post_captioned.png"], capture_output=True, text=True)

print("Tapping Share button (540, 1662)...")
adb_cmd("shell", "input", "tap", "540", "1662")
time.sleep(5)

print("Taking screenshot after share...")
adb_cmd("shell", "screencap", "-p", "/sdcard/after_share.png")
subprocess.run(["adb", "-s", DEVICE, "pull", "/sdcard/after_share.png", "smm-agent/storage/after_share.png"], capture_output=True, text=True)
print("Done!")
