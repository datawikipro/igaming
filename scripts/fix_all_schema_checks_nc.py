import glob
import re

files = glob.glob(r"igaming-k8s/*.yaml")
count = 0

for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Check if file has db-schema-check
    if 'name: db-schema-check' in content:
        # Regex replacement for the until loop inside db-schema-check
        # Replace whatever until loop is currently in db-schema-check with nc check + trailing dot FQDN
        pattern = r'(until\s+(?:psql|nc)[^\n]+\n\s+echo[^\n]+\n\s+sleep\s+\d+\n\s+done)'
        
        # New robust until block: use nc with timeout 3 and direct service DNS name with trailing dot
        new_until_block = '''until nc -z -v -w 3 "$HOST.igaming-source.svc.cluster.local." 5432 >/dev/null 2>&1; do
            echo "Waiting for PostgreSQL ($HOST) to accept queries..."
            sleep 2
          done'''
        
        new_content = re.sub(pattern, new_until_block, content)
        
        # Also replace psql -h "$HOST" with psql -h "$HOST.igaming-source.svc.cluster.local." for 100% musl libc FQDN resolution
        new_content = new_content.replace(
            'EXISTS=$(psql -h "$HOST"',
            'EXISTS=$(psql -h "$HOST.igaming-source.svc.cluster.local."'
        ).replace(
            'psql -h "$HOST" -U "$USER" -d "$DB"',
            'psql -h "$HOST.igaming-source.svc.cluster.local." -U "$USER" -d "$DB"'
        )
        
        if new_content != content:
            with open(f, 'w', encoding='utf-8') as out_f:
                out_f.write(new_content)
            count += 1
            print(f"Updated db-schema-check with nc + FQDN in {f}")

print(f"Total manifests updated: {count}")
