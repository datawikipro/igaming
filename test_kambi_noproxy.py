import requests

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
}

def test_url(url, method='GET'):
    print(f"\nTesting {method} (no proxy): {url}")
    try:
        if method == 'HEAD':
            r = requests.head(url, headers=headers, timeout=5)
        else:
            r = requests.get(url, headers=headers, timeout=5)
        print(f"Status Code: {r.status_code}")
        print(f"Content Length: {len(r.content) if r.content else 0}")
    except Exception as e:
        print(f"Error: {e}")

test_url("https://eu-offering.kambicdn.org/offering/v2018/ub/listView/all/all/all/all.json?lang=en_GB&market=GB", 'HEAD')
test_url("https://eu-offering.kambicdn.org/offering/v2018/ub/listView/all/all/all/all.json?lang=en_GB&market=GB", 'GET')
