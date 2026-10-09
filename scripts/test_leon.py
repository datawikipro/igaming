import requests

s = requests.Session()
s.headers.update({
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
    "Accept": "application/json, text/plain, */*",
    "Accept-Language": "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
    "Referer": "https://leon.ru/",
    "Origin": "https://leon.ru"
})

# Visit homepage first to acquire ServicePipe cookies
r0 = s.get("https://leon.ru/", timeout=15)
print("Homepage:", r0.status_code, "Cookies:", s.cookies.get_dict())

# Now try betline/sports
r1 = s.get("https://leon.ru/api-2/betline/sports?ctag=ru-RU", timeout=15)
print("Sports:", r1.status_code, r1.text[:300])

# Try events/all
r2 = s.get("https://leon.ru/api-2/betline/events/all?ctag=ru-RU&flags=reg,onl,ext,hcr,fcl", timeout=15)
print("Events:", r2.status_code, r2.text[:300])
