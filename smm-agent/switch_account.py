import subprocess
import time
import sys
from pathlib import Path

DEVICE = "100.78.183.101:5555"
STORAGE = Path("smm-agent/storage").resolve()

def adb_cmd(*args):
    cmd = ["adb", "-s", DEVICE] + list(args)
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=20)
    return res.stdout.strip()

def switch_to_account(account_target):
    subprocess.run(["adb", "connect", DEVICE], capture_output=True, text=True)
    print(f"Switching to account: {account_target}...")
    
    # Tap Profile tab
    adb_cmd("shell", "input", "tap", "972", "1680")
    time.sleep(2)
    
    # Long press Profile tab to open account switcher
    adb_cmd("shell", "input", "swipe", "972", "1680", "972", "1680", "1500")
    time.sleep(2)
    
    if account_target == "rus":
        adb_cmd("shell", "input", "tap", "450", "439")
    elif account_target == "es":
        adb_cmd("shell", "input", "tap", "450", "1404")
    elif account_target == "fr":
        adb_cmd("shell", "input", "tap", "450", "276")
    elif account_target == "en":
        # Scroll down
        adb_cmd("shell", "input", "swipe", "500", "1400", "500", "600", "400")
        time.sleep(1)
        adb_cmd("shell", "input", "tap", "450", "1400")
        
    time.sleep(3)
    
    # Take screenshot
    adb_cmd("shell", "screencap", "-p", f"/sdcard/ig_{account_target}.png")
    dest = STORAGE / f"ig_{account_target}.png"
    subprocess.run(["adb", "-s", DEVICE, "pull", f"/sdcard/ig_{account_target}.png", str(dest)], capture_output=True, text=True)
    print(f"Done! Screenshot saved to {dest}")

if __name__ == "__main__":
    target = sys.argv[1] if len(sys.argv) > 1 else "rus"
    switch_to_account(target)
