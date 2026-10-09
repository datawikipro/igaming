import requests, re

r = requests.get('https://boosty.to/static/js/main.js', timeout=10)
oauth = re.findall(r'oauth[a-zA-Z0-9_\-/]*', r.text, re.IGNORECASE)
print("OAuth strings:", list(set(oauth))[:20])

# Search for vk, twitch, google, ok, mailru
providers = re.findall(r'["\'](vk|twitch|google|youtube|ok|mailru|phone)["\']', r.text)
print("Known providers:", set(providers))
