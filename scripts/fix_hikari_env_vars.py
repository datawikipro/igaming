import glob
import re

files = glob.glob(r"igaming-k8s/*.yaml")
count = 0
for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Replace "30000ms" or any "...ms" in SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT with "0"
    new_content = re.sub(
        r'(name:\s*SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT\s*\n\s*value:\s*)"[^"]*"',
        r'\1"0"',
        content
    )
    
    if new_content != content:
        with open(f, 'w', encoding='utf-8') as file:
            file.write(new_content)
        count += 1

print(f"Updated Hikari initialization-fail-timeout env var to '0' in {count} manifests.")
