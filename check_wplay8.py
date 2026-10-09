import urllib.request
import re
import sys
import urllib.parse

sys.stdout.reconfigure(encoding='utf-8')

url = "https://apuestas.wplay.co/es/e/33035765/Atl%C3%A9tico-Nacional-v-Millonarios"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
html = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')

print("Event page length:", len(html))

# Let's find market headers / names
# Look for market containers or headers
mkts = re.findall(r'<div[^>]*class="[^"]*market[^"]*"[^>]*>.*?</div>', html, re.DOTALL)
print(f"Market divs: {len(mkts)}")

# Find all price buttons
buttons = re.findall(r'<button[^>]*class="[^"]*price[^"]*"[^>]*title="([^"]*)"[^>]*>.*?class="price dec"[^>]*>([^<]+)<', html, re.DOTALL)
print(f"Prices with titles: {len(buttons)}")
for title, price in buttons[:15]:
    print(f"  Outcome: {title.strip()} -> {price.strip()}")

# Find date / start time
time_match = re.search(r'data-time="([^"]+)"', html)
if time_match:
    print("Time data:", time_match.group(1))
else:
    times = re.findall(r'(\d{1,2}:\d{2}\s+(?:AM|PM|am|pm)?|\d{1,2}\s+(?:Ene|Feb|Mar|Abr|May|Jun|Jul|Ago|Sep|Oct|Nov|Dic)[^<]*)', html)
    print("Time candidates:", times[:5])
