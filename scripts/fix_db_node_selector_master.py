import glob
import re

files = glob.glob(r"igaming-k8s/*.yaml")
count = 0

for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Split by '---' to isolate StatefulSet blocks
    parts = content.split('---')
    modified = False
    
    for i in range(len(parts)):
        block = parts[i]
        if 'kind: StatefulSet' in block and '-db' in block:
            if 'node-type: standard' in block:
                parts[i] = block.replace('node-type: standard', 'node-type: master')
                modified = True
    
    if modified:
        new_content = '---'.join(parts)
        with open(f, 'w', encoding='utf-8') as out_f:
            out_f.write(new_content)
        count += 1
        print(f"Fixed DB StatefulSet nodeSelector to master in {f}")

print(f"Total DB StatefulSets updated to node-type: master: {count}")
