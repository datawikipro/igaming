import subprocess
import time
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

DEVICE = "100.78.183.101:5555"
STORAGE = Path("smm-agent/storage").resolve()
STORAGE.mkdir(parents=True, exist_ok=True)

def run_adb(*args):
    cmd = ["adb", "-s", DEVICE] + list(args)
    return subprocess.run(cmd, capture_output=True, text=True).stdout.strip()

def ensure_connected():
    subprocess.run(["adb", "connect", DEVICE], capture_output=True, text=True)

def tap(x, y, sleep_sec=2):
    ensure_connected()
    print(f"Tapping ({x}, {y})...")
    run_adb("shell", "input", "tap", str(x), str(y))
    time.sleep(sleep_sec)

def text_input(text, sleep_sec=1):
    ensure_connected()
    print(f"Typing text...")
    run_adb("shell", "input", "text", text)
    time.sleep(sleep_sec)

def swipe(x1, y1, x2, y2, duration_ms=500, sleep_sec=2):
    ensure_connected()
    print(f"Swiping ({x1}, {y1}) -> ({x2}, {y2})...")
    run_adb("shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration_ms))
    time.sleep(sleep_sec)

def keyevent(code, sleep_sec=1):
    ensure_connected()
    print(f"Keyevent {code}...")
    run_adb("shell", "input", "keyevent", str(code))
    time.sleep(sleep_sec)

def take_shot(name="screen.png"):
    ensure_connected()
    run_adb("shell", "screencap", "-p", f"/sdcard/{name}")
    out = STORAGE / name
    subprocess.run(["adb", "-s", DEVICE, "pull", f"/sdcard/{name}", str(out)], capture_output=True)
    print(f"Saved screenshot: {out}")
    return out

def dump_ui():
    ensure_connected()
    run_adb("shell", "uiautomator", "dump", "/sdcard/dump.xml")
    out_xml = STORAGE / "dump.xml"
    subprocess.run(["adb", "-s", DEVICE, "pull", "/sdcard/dump.xml", str(out_xml)], capture_output=True)
    if not out_xml.exists():
        print("dump.xml not found")
        return []
    tree = ET.parse(out_xml)
    items = []
    for node in tree.iter("node"):
        cls = node.attrib.get("class", "")
        txt = node.attrib.get("text", "") or node.attrib.get("content-desc", "")
        bounds = node.attrib.get("bounds", "")
        if "Edit" in cls or "Text" in cls or txt.strip():
            items.append((cls, txt.strip(), bounds))
            print(f"{cls:35} {txt.strip():35} {bounds}")
    return items

if __name__ == "__main__":
    if len(sys.argv) > 2 and sys.argv[1] == "tap":
        tap(int(sys.argv[2]), int(sys.argv[3]))
        take_shot("after_tap.png")
        dump_ui()
    elif len(sys.argv) > 5 and sys.argv[1] == "swipe":
        swipe(int(sys.argv[2]), int(sys.argv[3]), int(sys.argv[4]), int(sys.argv[5]))
        take_shot("after_swipe.png")
        dump_ui()
    elif len(sys.argv) > 2 and sys.argv[1] == "keyevent":
        keyevent(int(sys.argv[2]))
        take_shot("after_key.png")
        dump_ui()
    elif len(sys.argv) > 2 and sys.argv[1] == "type":
        text_input(sys.argv[2])
    elif len(sys.argv) > 1 and sys.argv[1] == "shot":
        take_shot("manual.png")
    else:
        dump_ui()
