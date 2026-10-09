import requests, re
r = requests.get('https://boosty.to', timeout=10)
print('CF-IPCountry:', r.headers.get('cf-ipcountry'))
m = re.search(r'"countryCode":"([^"]+)"', r.text)
if m:
    print('CountryCode:', m.group(1))
