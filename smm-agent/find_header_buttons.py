import json, sys
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

with open('storage/tg_actual_edit_elements.json', encoding='utf-8') as f:
    items = json.load(f)

for it in items:
    if it['x'] > 500 and it['y'] < 100:
        print(f"{it['tag']}.{it['className']} text='{it['text']}' at ({it['x']}, {it['y']}) size {it['w']}x{it['h']}")
