import glob
for file in glob.glob("igaming-k8s/*.yaml"):
    with open(file, "r") as f:
        content = f.read()
    if "APP_BOOKMAKER_REGIONS\n          value: EU" in content or "APP_BOOKMAKER_REGIONS\n          value: 'EU'" in content or "APP_BOOKMAKER_REGIONS\n          value: \"EU\"" in content:
        content = content.replace("value: EU", 'value: ""').replace("value: 'EU'", 'value: ""').replace('value: "EU"', 'value: ""')
        with open(file, "w") as f:
            f.write(content)
        print(f"Removed EU proxy requirement for {file}")
