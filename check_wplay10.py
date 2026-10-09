import urllib.request
import re
import sys
import time

sys.stdout.reconfigure(encoding='utf-8')

start_time = time.time()
print("Fetching Wplay home page for tournaments and sports...")
req = urllib.request.Request("https://apuestas.wplay.co/es", headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"})
html = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')

# 1. Collect all events from home page directly
events = {}
for m in re.finditer(r'href="(/es/e/(\d+)/([^"]+))"', html):
    href, ev_id, slug = m.groups()
    if ev_id not in events:
        events[ev_id] = (slug, href)

print(f"Direct events from home page: {len(events)}")

# 2. Collect tournaments
tournaments = list(set(re.findall(r'href="(/es/t/\d+/[^"]+)"', html)))
print(f"Tournaments found: {len(tournaments)}")

# 3. Collect from sport pages (/es/s/...)
sports = list(set(re.findall(r'href="(/es/s/[A-Z0-9]+/[^"]+)"', html)))
print(f"Sports found: {len(sports)}")

for s in sports:
    if len(events) >= 600:
        break
    try:
        s_req = urllib.request.Request(f"https://apuestas.wplay.co{s}", headers={"User-Agent": "Mozilla/5.0"})
        s_html = urllib.request.urlopen(s_req, timeout=10).read().decode('utf-8', errors='ignore')
        for m in re.finditer(r'href="(/es/e/(\d+)/([^"]+))"', s_html):
            href, ev_id, slug = m.groups()
            events[ev_id] = (slug, href)
        print(f"After sport {s.split('/')[-1]}: {len(events)} events (time: {time.time()-start_time:.1f}s)")
    except Exception as e:
        print(f"Error sport {s}: {e}")

# If still needed, iterate tournaments
for t in tournaments:
    if len(events) >= 600:
        break
    try:
        t_req = urllib.request.Request(f"https://apuestas.wplay.co{t}", headers={"User-Agent": "Mozilla/5.0"})
        t_html = urllib.request.urlopen(t_req, timeout=10).read().decode('utf-8', errors='ignore')
        for m in re.finditer(r'href="(/es/e/(\d+)/([^"]+))"', t_html):
            href, ev_id, slug = m.groups()
            events[ev_id] = (slug, href)
    except Exception as e:
        pass

print(f"\nFinal count of events collected: {len(events)} in {time.time()-start_time:.2f} seconds!")
for ev_id, (slug, href) in list(events.items())[:5]:
    print(f"  {ev_id}: {slug} -> {href}")
