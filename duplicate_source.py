import os
import shutil

def duplicate_source(src_dir, dest_dir, old_word, new_word):
    if os.path.exists(dest_dir):
        shutil.rmtree(dest_dir)
    
    # Ignore target directory
    shutil.copytree(src_dir, dest_dir, ignore=shutil.ignore_patterns('target'))
    
    # Rename directories
    for root, dirs, files in os.walk(dest_dir, topdown=False):
        for name in dirs:
            if old_word.lower() in name.lower():
                old_path = os.path.join(root, name)
                new_name = name.replace(old_word.lower(), new_word.lower())
                new_path = os.path.join(root, new_name)
                os.rename(old_path, new_path)
                
    # Rename files
    for root, dirs, files in os.walk(dest_dir, topdown=False):
        for name in files:
            if old_word.lower() in name.lower():
                old_path = os.path.join(root, name)
                new_name = name.replace(old_word.lower(), new_word.lower())
                new_name = new_name.replace(old_word.capitalize(), new_word.capitalize())
                new_path = os.path.join(root, new_name)
                os.rename(old_path, new_path)
                
    # Replace content in files
    for root, dirs, files in os.walk(dest_dir):
        for name in files:
            if name.endswith(('.java', '.xml', '.yaml', '.properties')):
                filepath = os.path.join(root, name)
                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        content = f.read()
                    
                    content = content.replace(old_word.lower(), new_word.lower())
                    content = content.replace(old_word.capitalize(), new_word.capitalize())
                    content = content.replace(old_word.upper(), new_word.upper())
                    
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(content)
                except Exception as e:
                    print(f"Failed to process {filepath}: {e}")

duplicate_source('igaming-source-draftkings', 'igaming-source-betmgm', 'draftkings', 'betmgm')
duplicate_source('igaming-source-draftkings', 'igaming-source-caesars', 'draftkings', 'caesars')
print("Finished duplicating sources.")
