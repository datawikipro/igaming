import urllib.request
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

url = "https://apuestas.wplay.co/es/e/33035765/Atl%C3%A9tico-Nacional-v-Millonarios"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
html = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')

# Find first occurrence of "price dec"
pos = html.find('class="price dec"')
if pos != -1:
    print(html[pos-300:pos+300])
