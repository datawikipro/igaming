import urllib.request
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

# 1. Fetch main page
url = "https://apuestas.wplay.co/es"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"})
html = urllib.request.urlopen(req, timeout=15).read().decode("utf-8", errors="ignore")

# Find all links to tournaments
tournaments = set(re.findall(r'href="(/es/t/\d+/[^"]+)"', html))
print(f"Total tournaments on home page: {len(tournaments)}")

# Find all sports links
sport_links = set(re.findall(r'href="(/es/[a-z]+/[^"]+)"', html))
sports = [l for l in sport_links if not l.startswith('/es/t/') and not l.startswith('/es/e/')]
print(f"Other sport/category links: {len(sports)}")
for s in sorted(sports)[:20]:
    print("  ", s)

# Let's check live page: /es/en-vivo or similar?
for candidate in ['/es/en-vivo', '/es/live', '/es/in-play', '/es/deportes-en-vivo', '/es/hoy', '/es/proximos']:
    try:
        c_req = urllib.request.Request(f"https://apuestas.wplay.co{candidate}", headers={"User-Agent": "Mozilla/5.0"})
        resp = urllib.request.urlopen(c_req, timeout=5)
        print(f"Candidate {candidate}: HTTP {resp.status}")
    except urllib.error.HTTPError as e:
        print(f"Candidate {candidate}: HTTP {e.code}")
    except Exception as e:
        print(f"Candidate {candidate}: Error {e}")
