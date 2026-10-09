import requests
import json

url = 'https://1xbet.com/LineFeed/Get1x2_VZip?sports=1&count=5&lng=ru&mode=4'
try:
    resp = requests.get(url, timeout=10)
    data = resp.json()
    for game in data.get('Value', []):
        print(f"Game {game.get('I')}: {game.get('O1')} vs {game.get('O2')}")
        for e in game.get('E', []):
            if e.get('T') in [1, 2, 3, 11, 12]:
                print(f"  T={e.get('T')} P={e.get('P')} C={e.get('C')}")
except Exception as e:
    print("Error:", e)
