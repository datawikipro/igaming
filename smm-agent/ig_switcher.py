import subprocess
import time
import sys
import re
import xml.etree.ElementTree as ET
from pathlib import Path

DEVICE = "100.78.183.101:5555"
STORAGE = Path("smm-agent/storage").resolve()
STORAGE.mkdir(parents=True, exist_ok=True)

ACCOUNTS = {
    "fr": ["fr.smartbet.guru", "Aleksei Chernousov"],
    "rus": ["rus.smartbet.guru"],
    "es": ["es.smartbet.guru"],
    "en": ["en.smartbet.guru"]
}

def adb_cmd(*args, timeout=20):
    cmd = ["adb", "-s", DEVICE] + list(args)
    res = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout)
    return res.stdout.strip()

def ensure_device():
    subprocess.run(["adb", "connect", DEVICE], capture_output=True, text=True)

def dump_ui():
    adb_cmd("shell", "uiautomator", "dump", "/sdcard/ui_dump.xml")
    xml_path = STORAGE / "ui_dump.xml"
    subprocess.run(["adb", "-s", DEVICE, "pull", "/sdcard/ui_dump.xml", str(xml_path)], capture_output=True, text=True)
    return xml_path

def find_target_node(xml_path, target_names):
    try:
        tree = ET.parse(xml_path)
    except Exception as e:
        print(f"Error parsing XML: {e}")
        return None
        
    for node in tree.iter("node"):
        text = node.attrib.get("text", "")
        desc = node.attrib.get("content-desc", "")
        combined = f"{text} {desc}"
        bounds = node.attrib.get("bounds", "")
        
        for name in target_names:
            if name.lower() in combined.lower() and bounds:
                m = re.findall(r"\[(\d+),(\d+)\]", bounds)
                if len(m) == 2:
                    x1, y1 = int(m[0][0]), int(m[0][1])
                    x2, y2 = int(m[1][0]), int(m[1][1])
                    cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
                    return cx, cy, combined.strip()
    return None

def switch_to(account_key):
    ensure_device()
    target_names = ACCOUNTS.get(account_key)
    if not target_names:
        print(f"Unknown account key: {account_key}. Choose from: {list(ACCOUNTS.keys())}")
        return False
        
    print(f"\n==========================================")
    print(f" Switching Instagram account to: {account_key} ({target_names[0]})")
    print(f"==========================================")
    
    # Check if switcher bottom sheet is open, if not, open it
    xml_path = dump_ui()
    # Check if switcher already open
    found = find_target_node(xml_path, ["Add Instagram account", "Your Facebook profile", "sport.ai.analysis"])
    if not found:
        print("Opening account switcher via long-press on profile tab...")
        adb_cmd("shell", "input", "tap", "900", "1700")
        time.sleep(1)
        adb_cmd("shell", "input", "swipe", "900", "1700", "900", "1700", "1500")
        time.sleep(2)
        xml_path = dump_ui()
        
    # Check if target is visible directly
    res = find_target_node(xml_path, target_names)
    if not res:
        print("Target not visible in initial sheet view, scrolling down...")
        adb_cmd("shell", "input", "swipe", "500", "1100", "500", "400", "400")
        time.sleep(1.5)
        xml_path = dump_ui()
        res = find_target_node(xml_path, target_names)
        
    if not res:
        print("Target not visible after scroll down, scrolling up...")
        adb_cmd("shell", "input", "swipe", "500", "400", "500", "1100", "400")
        time.sleep(1.5)
        xml_path = dump_ui()
        res = find_target_node(xml_path, target_names)
        
    if res:
        cx, cy, label = res
        print(f"Found target '{label}' at ({cx}, {cy}). Tapping...")
        adb_cmd("shell", "input", "tap", str(cx), str(cy))
        time.sleep(3)
        
        # Check for any "Continue as..." or "Save info" dialogs
        xml_path = dump_ui()
        continue_btn = find_target_node(xml_path, ["Continue as", "Continue", "Save info", "Not now"])
        if continue_btn:
            cx2, cy2, label2 = continue_btn
            print(f"Dismissing dialog '{label2}' at ({cx2}, {cy2})...")
            adb_cmd("shell", "input", "tap", str(cx2), str(cy2))
            time.sleep(2)
            
        # Tap profile icon to navigate to account profile page
        adb_cmd("shell", "input", "tap", "900", "1700")
        time.sleep(2)
        
        # Capture screenshot
        shot_name = f"profile_{account_key}.png"
        adb_cmd("shell", "screencap", "-p", f"/sdcard/{shot_name}")
        local_shot = STORAGE / shot_name
        subprocess.run(["adb", "-s", DEVICE, "pull", f"/sdcard/{shot_name}", str(local_shot)], capture_output=True, text=True)
        print(f"Successfully switched to {account_key}! Screenshot saved to {local_shot}")
        return True
    else:
        print(f"FAILED to find account {account_key} in switcher!")
        return False

if __name__ == "__main__":
    acc = sys.argv[1] if len(sys.argv) > 1 else "rus"
    switch_to(acc)
