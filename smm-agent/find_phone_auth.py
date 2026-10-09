import requests, re

r = requests.get('https://boosty.to/static/js/main.js')
for m in re.finditer(r'providerCode[:=]"phone"|provider:"phone"|data-provider="phone"|phone_login|/v1/auth/phone', r.text):
    start = max(0, m.start() - 100)
    end = min(len(r.text), m.end() + 200)
    print("Match:", r.text[start:end])
