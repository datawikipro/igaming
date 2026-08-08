import glob

files = glob.glob(r"igaming-k8s/*.yaml")
count = 0
for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    new_content = content.replace(
        'psql -h "$HOST" -U "$USER" -d "$DB" -c "SELECT 1;"',
        'psql -h "$HOST" -U "$USER" -d postgres -c "SELECT 1;"'
    )
    
    if new_content != content:
        with open(f, 'w', encoding='utf-8') as out_f:
            out_f.write(new_content)
        count += 1

print(f"Updated readiness check to '-d postgres' in {count} manifests.")
