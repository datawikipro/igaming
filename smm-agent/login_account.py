import subprocess
import time
import sys
from pathlib import Path

DEVICE = "100.78.183.101:5555"
STORAGE = Path("smm-agent/storage").resolve()

def adb(*args):
    cmd = ["adb", "-s", DEVICE] + list(args)
    return subprocess.run(cmd, capture_output=True, text=True).stdout.strip()

def tap(x, y, sleep_sec=1):
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(sleep_sec)

def take_shot(name):
    adb("shell", "screencap", "-p", f"/sdcard/{name}")
    out = STORAGE / name
    subprocess.run(["adb", "-s", DEVICE, "pull", f"/sdcard/{name}", str(out)], capture_output=True)
    print(f"Screenshot saved: {out}")
    return out

def login(username, password="Racoiaws12!@"):
    subprocess.run(["adb", "connect", DEVICE], capture_output=True)
    print(f"Attempting login for: {username}")
    
    # 1. Tap username field clear button
    tap(920, 594, 1)
    
    # 2. Tap username field and type
    tap(540, 594, 1)
    # Clear any leftover text
    for _ in range(30):
        adb("shell", "input", "keyevent", "67")
    adb("shell", "input", "text", username)
    time.sleep(1)
    
    # 3. Tap password field
    tap(540, 834, 1)
    escaped_pw = password.replace("!", "\\!").replace("@", "\\@")
    adb("shell", "input", "text", escaped_pw)
    time.sleep(1)
    
    # 4. Hide keyboard
    adb("shell", "input", "keyevent", "4")
    time.sleep(1)
    
    # 5. Tap Log in button
    tap(540, 999, 4)
    
    # 6. Capture result
    take_shot(f"login_result_{username}.png")

if __name__ == "__main__":
    user = sys.argv[1] if len(sys.argv) > 1 else "es.smartbet.guru"
    login(user)
