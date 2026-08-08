import glob
import re

files = glob.glob(r"igaming-k8s/*.yaml")
count = 0
for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    new_content = re.sub(r'HOST="([a-zA-Z0-9_\-]+)\.igaming-source\.svc\.cluster\.local"', r'HOST="\1"', content)
    if new_content != content:
        with open(f, 'w', encoding='utf-8') as file:
            file.write(new_content)
        count += 1

print(f"Updated {count} manifests from long FQDN to short DNS service name.")
