import urllib.request
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

# Fetch home page to get tournament links
url = "https://apuestas.wplay.co/es"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
html = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')

tournaments = list(set(re.findall(r'href="(/es/t/\d+/[^"]+)"', html)))
print(f"Total tournaments: {len(tournaments)}")

all_events = set()
for t in tournaments[:10]:
    t_url = f"https://apuestas.wplay.co{t}"
    try:
        t_req = urllib.request.Request(t_url, headers={"User-Agent": "Mozilla/5.0"})
        t_html = urllib.request.urlopen(t_req, timeout=10).read().decode('utf-8', errors='ignore')
        t_events = set(re.findall(r'/es/e/(\d+)/([^"\'\s>]+)', t_html))
        print(f"Tournament {t.split('/')[-1]}: {len(t_events)} events")
        all_events.update(t_events)
    except Exception as e:
        print(f"Error {t}: {e}")

print(f"Total unique events across 10 tournaments: {len(all_events)}")
