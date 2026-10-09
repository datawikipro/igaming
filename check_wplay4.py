import urllib.request
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

for path in ['/es/live', '/es/s/FOOT/F%C3%BAtbol', '/es/s/BASK/Baloncesto']:
    url = f"https://apuestas.wplay.co{path}"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    try:
        html = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')
        events = set(re.findall(r'/es/e/(\d+)/([^"\'\s>]+)', html))
        odds = re.findall(r'class="price dec"[^>]*>([^<]+)<', html)
        print(f"Path: {path} -> Events: {len(events)}, Odds: {len(odds)}")
        if events:
            print("  Sample event:", list(events)[0])
    except Exception as e:
        print(f"Path: {path} -> Error: {e}")
