for app in ['betmgm', 'fanduel', 'bwin', 'unibet', 'bet365', 'caesars', 'pinnacle', 'sbobet', 'dafabet', 'betsson']:
    file = f"igaming-k8s/{app}.yaml"
    import os
    if not os.path.exists(file): continue
    with open(file, "r") as f:
        content = f.read()
    if "APP_VPN_ENABLED" not in content:
        content = content.replace("- name: APP_BOOKMAKER_REGIONS", "- name: APP_VPN_ENABLED\n          value: \"false\"\n        - name: APP_BOOKMAKER_REGIONS")
        with open(file, "w") as f:
            f.write(content)
        print(f"Disabled VPN for {app}")
