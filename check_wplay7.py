import urllib.request
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

url = "https://apuestas.wplay.co/es/t/19274/Internacionales"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
html = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')

# Extract events
# Pattern: /es/e/(\d+)/([^"]+)
event_matches = re.findall(r'href="(/es/e/(\d+)/([^"]+))"', html)
print(f"Events found in tournament: {len(event_matches)}")

# Let's inspect each unique event
seen = set()
for href, ev_id, slug in event_matches:
    if ev_id in seen:
        continue
    seen.add(ev_id)
    # Match name from slug
    name = slug.replace('-', ' ')
    print(f"Event ID: {ev_id}, Name: {name}, URL: {href}")

# Let's see how markets and odds are associated with events in this HTML:
# In HTML, let's find blocks with class containing "ev-" or "mkt-"
ev_blocks = re.findall(r'<tr[^>]*data-ev_id="(\d+)"[^>]*>.*?</tr>', html, re.DOTALL)
print(f"Tr blocks with data-ev_id: {len(ev_blocks)}")

if not ev_blocks:
    # Try div or other container
    ev_containers = re.findall(r'class="[^"]*ev-(\d+)[^"]*"', html)
    print(f"Containers with ev-<id>: {len(set(ev_containers))}")
