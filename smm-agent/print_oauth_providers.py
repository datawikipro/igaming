import requests

r = requests.get('https://boosty.to/static/js/main.js')
idx = r.text.find('"oauthProviders":')
print(r.text[idx:idx+1500])
