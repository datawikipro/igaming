import urllib.request
import json

try:
    url = "https://api4.olimpbet.kz/v2/sports"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        data = response.read().decode('utf-8')
        parsed = json.loads(data)
        print("Success! Excerpt of sports mapping:")
        for sport in parsed[:15]:
            print(f"ID: {sport.get('id')} -> Name: {sport.get('name')}")
except Exception as e:
    print(f"Error fetching: {e}")
