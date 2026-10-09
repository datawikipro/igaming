import urllib.request
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

url = "https://apuestas.wplay.co/es"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"})
html = urllib.request.urlopen(req, timeout=15).read().decode("utf-8", errors="ignore")

# Find first occurrence of data-ev_id
pos = html.find("data-ev_id")
if pos != -1:
    print("Found data-ev_id at pos", pos)
    print(html[pos-100:pos+1500])
else:
    print("data-ev_id not found")
