import glob
for file in glob.glob("igaming-k8s/*.yaml"):
    with open(file, "r") as f:
        content = f.read()
    if 'value: "",INT' in content or 'value: INT,"",LATAM' in content or 'value: INT,EU,""' in content or 'value: "",' in content or ',""' in content:
        content = content.replace('"",INT', 'EU,INT').replace('INT,""', 'INT,EU').replace('"",', 'EU,').replace(',""', ',EU')
        with open(file, "w") as f:
            f.write(content)
        print(f"Fixed yaml syntax for {file}")
