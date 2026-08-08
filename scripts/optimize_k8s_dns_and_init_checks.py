import glob
import re

files = glob.glob(r"igaming-k8s/*.yaml")
count = 0

for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # 1. Update until loop in db-schema-check to have max 5 retries (10 seconds max wait)
    old_until_pattern = r'until nc -z -v -w 3 "\$HOST\.igaming-source\.svc\.cluster\.local\." 5432 >/dev/null 2>&1; do\s+echo "Waiting for PostgreSQL \(\$HOST\) to accept queries\..."\s+sleep 2\s+done'
    
    new_until_block = '''MAX_RETRIES=5
          COUNT=0
          until nc -z -v -w 3 "$HOST.igaming-source.svc.cluster.local." 5432 >/dev/null 2>&1 || [ $COUNT -ge $MAX_RETRIES ]; do
            echo "Waiting for PostgreSQL ($HOST) to accept queries..."
            sleep 2
            COUNT=$((COUNT + 1))
          done'''
    
    updated_content = re.sub(old_until_pattern, new_until_block, content)
    
    # 2. Add dnsConfig ndots:1 to deployment pod specs to speed up musl libc DNS resolution
    # Find imagePullSecrets: \n - name: regcred and insert dnsConfig
    dns_config_block = '''imagePullSecrets:
      - name: regcred
      dnsConfig:
        options:
        - name: ndots
          value: "1"'''
    
    updated_content = updated_content.replace(
        "imagePullSecrets:\n      - name: regcred",
        dns_config_block
    )
    
    if updated_content != content:
        with open(f, 'w', encoding='utf-8') as out_f:
            out_f.write(updated_content)
        count += 1
        print(f"Optimized DNS & init retry limit in {f}")

print(f"Total manifests optimized: {count}")
