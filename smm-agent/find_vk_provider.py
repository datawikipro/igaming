import requests, re

r = requests.get('https://boosty.to/static/js/main.js', timeout=10)
# Find the code around oauthProviders or where providers list is rendered
idx = r.text.find('oauthProviders')
if idx != -1:
    print("Around oauthProviders:")
    print(r.text[max(0, idx-200):idx+300])

idx_vk = r.text.find('providerCode:"vk"')
if idx_vk == -1:
    idx_vk = r.text.find('"vk"')
if idx_vk != -1:
    print("\nAround vk:")
    print(r.text[max(0, idx_vk-150):idx_vk+250])
