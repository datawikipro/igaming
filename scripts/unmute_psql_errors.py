import glob

files = glob.glob(r"igaming-k8s/*.yaml")
count = 0

for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    new_content = content.replace(
        'until psql -h "$HOST" -U "$USER" -d postgres -c "SELECT 1;" >/dev/null 2>&1; do',
        'until psql -h "$HOST" -U "$USER" -d postgres -c "SELECT 1;"; do'
    )
    
    if new_content != content:
        with open(f, 'w', encoding='utf-8') as out_f:
            out_f.write(new_content)
        count += 1

print(f"Unmuted psql error logging in {count} manifests.")
