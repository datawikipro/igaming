import urllib.request
import json

endpoints = [
    "https://olimpbet.kz/api/v2/sports",
    "https://olimpbet.kz/api/sports",
    "https://olimpbet.kz/api/v2/events?locale=ru&page-size=2&live=true"
]

for url in endpoints:
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as response:
            data = response.read().decode('utf-8')
            parsed = json.loads(data)
            print(f"URL: {url} -> Status: 200")
            if isinstance(parsed, list):
                print(f"  Length: {len(parsed)}, Excerpt: {parsed[:2]}")
            elif isinstance(parsed, dict):
                print(f"  Keys: {list(parsed.keys())}")
                if "items" in parsed:
                    print(f"  Items Excerpt: {parsed['items'][:1]}")
    except Exception as e:
        print(f"URL: {url} -> Error: {e}")
