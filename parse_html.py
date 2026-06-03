import re

with open("olimpbet_kz.html", "r", encoding="utf-8") as f:
    content = f.read()

urls = re.findall(r'https?://[^\s"\'>]+', content)
for u in set(urls):
    print(u)
