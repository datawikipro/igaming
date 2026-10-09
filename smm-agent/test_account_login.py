import subprocess
import time
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

DEVICE = "100.78.183.101:5555"
STORAGE = Path("smm-agent/storage").resolve()
STORAGE.mkdir(parents=True, exist_ok=True)

def adb(*args):
    cmd = ["adb", "-s", DEVICE] + list(args)
    return subprocess.run(cmd, capture_output=True, text=True).stdout.strip()

def tap(x, y, sleep_sec=1.5):
    print(f"Tap ({x}, {y})")
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(sleep_sec)

def type_text(txt, sleep_sec=1.0):
    print(f"Type '{txt}'")
    # For special characters in password like !@, use adb shell input text with escaping or keyevents if needed
    # In Android, ! and @:
    # '!' can sometimes be tricky with bash/sh, but via adb input text:
    escaped = txt.replace("&", "\\&").replace(" ", "%s")
    adb("shell", "input", "text", escaped)
    time.sleep(sleep_sec)

def shot(name):
    adb("shell", "screencap", "-p", f"/sdcard/{name}")
    out = STORAGE / name
    subprocess.run(["adb", "-s", DEVICE, "pull", f"/sdcard/{name}", str(out)], capture_output=True)
    print(f"Screenshot -> {out}")
    return out

def login(username, password="Racoiaws12!@"):
    print(f"\n--- Logging into {username} ---")
    subprocess.run(["adb", "connect", DEVICE], capture_output=True)
    
    # 1. Tap username field [51,552][945,636] -> (498, 594)
    tap(498, 594)
    # Clear any existing text
    for _ in range(30):
        adb("shell", "input", "keyevent", "67") # KEYCODE_DEL
    time.sleep(0.5)
    type_text(username)
    
    # 2. Tap password field [51,792][945,876] -> (498, 834)
    tap(498, 834)
    for _ in range(30):
        adb("shell", "input", "keyevent", "67")
    time.sleep(0.5)
    type_text(password)
    
    # Hide keyboard
    adb("shell", "input", "keyevent", "111") # ESC
    time.sleep(1)
    
    # 3. Tap Log in button [36,936][1044,1062] -> (540, 999)
    tap(540, 999, sleep_sec=5)
    
    shot(f"after_login_{username}.png")
    
    # Dump UI
    adb("shell", "uiautomator", "dump", "/sdcard/login_res.xml")
    out_xml = STORAGE / f"login_res_{username}.xml"
    subprocess.run(["adb", "-s", DEVICE, "pull", "/sdcard/login_res.xml", str(out_xml)], capture_output=True)
    if out_xml.exists():
        tree = ET.parse(out_xml)
        for node in tree.iter("node"):
            txt = node.attrib.get("text", "") or node.attrib.get("content-desc", "")
            if txt.strip():
                print(f"  UI: {txt.strip():40} {node.attrib.get('bounds')}")

if __name__ == "__main__":
    user = sys.argv[1] if len(sys.argv) > 1 else "rus.smartbet.guru"
    login(user)
