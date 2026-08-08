import glob
import re

files = glob.glob(r"igaming-k8s/*.yaml")
count = 0

for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Replace igaming-source-{bm}-db.igaming-dev.svc.cluster.local -> igaming-source-{bm}-db
    new_content = re.sub(r'igaming-source-([a-z0-9\-]+)-db\.igaming-dev\.svc\.cluster\.local', r'igaming-source-\1-db', content)
    
    if new_content != content:
        with open(f, 'w', encoding='utf-8') as out_f:
            out_f.write(new_content)
        count += 1
        print(f"Fixed igaming-dev namespace in {f}")

print(f"Total files fixed: {count}")
