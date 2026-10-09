import requests
import json

resp = requests.get('https://api4.olimpbet.kz/v2/events?page-size=500&live=false', timeout=10)
data = resp.json()
found = False
for it in data.get('items', []):
    if it.get('id') == 8918131:
        print(json.dumps(it, ensure_ascii=False, indent=2))
        found = True
        break
if not found:
    print("Not found in top 500 prematch")
