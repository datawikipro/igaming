import os

def fix_duplicate_env(content):
    lines = content.split('\n')
    out_lines = []
    i = 0
    while i < len(lines):
        line = lines[i]
        
        if "- name: APP_AGGREGATOR_URL" in line:
            # Check what the next lines are
            # We might have `value:` on the next line
            next_idx = i + 1
            has_value = False
            if next_idx < len(lines) and "value:" in lines[next_idx]:
                has_value = True
                next_idx += 1
                
            # Now check if the line at next_idx is ANOTHER APP_AGGREGATOR_URL
            if next_idx < len(lines) and "- name: APP_AGGREGATOR_URL" in lines[next_idx]:
                # It's a duplicate! Skip the FIRST one
                i = next_idx
                continue
                
        out_lines.append(line)
        i += 1
        
    return '\n'.join(out_lines)

def process_files():
    for root, dirs, files in os.walk('.'):
        if 'k8s' not in root:
            continue
        for file in files:
            if file.endswith('.yaml'):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                new_content = fix_duplicate_env(content)
                
                if new_content != content:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    print(f"Updated {path}")

if __name__ == "__main__":
    process_files()
