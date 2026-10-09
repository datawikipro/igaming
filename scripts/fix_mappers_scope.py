import os
import re

count = 0
for root, dirs, files in os.walk('.'):
    for f in files:
        if f.endswith('.java'):
            filepath = os.path.join(root, f)
            with open(filepath, 'r', encoding='utf-8', errors='ignore') as fp:
                content = fp.read()
            
            # Check for non-public resolveScope that overrides AbstractBetTypeMapper
            pattern = re.compile(r'(private|protected)(\s+BetScope\s+resolveScope\s*\()')
            if pattern.search(content):
                new_content = pattern.sub(r'public\2', content)
                with open(filepath, 'w', encoding='utf-8') as fp:
                    fp.write(new_content)
                print(f"Fixed: {filepath}")
                count += 1

print(f"Done. Fixed {count} files.")
