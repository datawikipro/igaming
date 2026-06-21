import requests
import urllib3
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

domains = [
    "1xbet.com", "22bet.com", "888sport.com", "bet365.com", "betandyou.com",
    "betlabel.com", "betwinner.com", "betcity.com", "betmgm.com", "betsson.com",
    "bwin.com", "caesars.com", "dafabet.com", "fanduel.com", "fan-sport.com",
    "ligastavok.ru", "linebet.com", "marathonbet.com", "megapari.com", "melbet.com",
    "pinnacle.com", "rushbet.co", "sportbet.ru", "sunbet.co.za", "fon.bet"
]

headers = {
    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"
}

for d in domains:
    url = f"https://{d}"
    try:
        resp = requests.get(url, headers=headers, timeout=10, verify=False, allow_redirects=True)
        server = resp.headers.get("Server", "").lower()
        if resp.status_code == 200:
            if "cloudflare" in server or "cf-ray" in resp.headers:
                print(f"{d}: Cloudflare (Status 200 but likely Captcha)")
            else:
                print(f"{d}: OK (Status 200)")
        elif resp.status_code in [403, 503] and ("cloudflare" in server or "cf-ray" in resp.headers):
            print(f"{d}: Blocked by Cloudflare (Status {resp.status_code})")
        else:
            print(f"{d}: Error Status {resp.status_code} Server: {server}")
    except requests.exceptions.Timeout:
        print(f"{d}: Timeout (Network blocked or down)")
    except requests.exceptions.ConnectionError:
        print(f"{d}: Connection Error (Network blocked or down)")
    except Exception as e:
        print(f"{d}: Unknown Error - {type(e).__name__}")
