import urllib.request
import json

try:
    url = "https://olimpbet.kz/api/sports"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        data = response.read().decode('utf-8')
        parsed = json.loads(data)
        for sport in parsed.get('items', []):
            print(f'SPORT_NAMES.put("{sport.get("id")}", "{sport.get("name")}");')
except Exception as e:
    print(f"Error fetching: {e}")
