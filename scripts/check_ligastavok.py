import urllib.request
import re

req = urllib.request.Request(
    'https://www.ligastavok.ru/',
    headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'}
)
try:
    html = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')
    links = set(re.findall(r'href="([^"]+)"', html))
    print("Found links:")
    for l in sorted(links):
        if any(k in l.lower() for k in ['live', 'sport', 'bets', 'match', 'events']):
            print("  ", l)
except Exception as e:
    print("Error:", e)
