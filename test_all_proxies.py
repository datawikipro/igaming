import requests

proxies = {
    'http': 'http://127.0.0.1:3128',
    'https': 'http://127.0.0.1:3128',
}

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
}

def test_url(name, url):
    try:
        r = requests.get(url, proxies=proxies, headers=headers, timeout=10)
        print(f"{name} GET {url} -> Status: {r.status_code}, Length: {len(r.content)}")
    except Exception as e:
        print(f"{name} GET {url} -> Error: {e}")

    try:
        r = requests.head(url, proxies=proxies, headers=headers, timeout=10)
        print(f"{name} HEAD {url} -> Status: {r.status_code}")
    except Exception as e:
        print(f"{name} HEAD {url} -> Error: {e}")

test_url("Caesars Web", "https://sportsbook.caesars.com/us/mi/bet/competitions/04f90892-3afa-4e84-acce-5b89f151063d")
test_url("Caesars API", "https://api.americanwagering.com/regions/us/locations/mi/brands/czr/sb/v3/events/04f90892-3afa-4e84-acce-5b89f151063d")
test_url("FanDuel", "https://sportsbook.fanduel.com/navigation/mlb")
test_url("BetMGM", "https://sports.betmgm.com/en/sports/events/12")
test_url("Bwin", "https://sports.bwin.com")
test_url("Betsson", "https://www.betsson.com")
test_url("888sport", "https://www.888sport.com")
