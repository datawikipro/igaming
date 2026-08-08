import glob
import re

files = glob.glob(r"igaming-k8s/*.yaml")
for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    matches = re.findall(r'SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT\s*\n\s*value:\s*"([^"]*)"', content)
    for m in matches:
        if not m.isdigit():
            print(f"Non-digit Hikari value in {f}: {m}")
            # Replace non-digit values with "0"
            content = content.replace(f'value: "{m}"', 'value: "0"')
            with open(f, 'w', encoding='utf-8') as out_f:
                out_f.write(content)
