import requests, json, re

r = requests.get('https://boosty.to', timeout=10)
m = re.search(r'<script type="text/plain" id="app-config">(.*?)</script>', r.text)
if m:
    cfg = json.loads(m.group(1))
    print(json.dumps(cfg, indent=2))
else:
    print("app-config not found")
