import urllib.request
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

url = "https://apuestas.wplay.co/es/t/19274/Internacionales"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
html = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')

# Search for any endpoint or url patterns inside scripts
script_content = "\n".join(re.findall(r'<script[^>]*>(.*?)</script>', html, re.DOTALL))
print("Script content length:", len(script_content))
for line in script_content.split('\n'):
    if any(k in line.lower() for k in ['url', 'endpoint', 'fetch', 'post', 'get', 'api', 'json', 'data']):
        if len(line.strip()) < 150 and not line.strip().startswith('//'):
            print("  ", line.strip())
