import os
import shutil

def generate_skeleton(source_dir, target_name_lower, target_name_camel, target_name_upper):
    target_dir = f"igaming-source-{target_name_lower}"
    if os.path.exists(target_dir):
        print(f"Directory {target_dir} already exists. Skipping.")
        return
    
    shutil.copytree(source_dir, target_dir, ignore=shutil.ignore_patterns('target', '.*'))
    
    source_name_lower = "unibet"
    source_name_camel = "Unibet"
    source_name_upper = "UNIBET"
    
    # Rename directories first (bottom-up to avoid path invalidation)
    for root, dirs, files in os.walk(target_dir, topdown=False):
        for name in dirs:
            if source_name_lower in name:
                new_name = name.replace(source_name_lower, target_name_lower)
                os.rename(os.path.join(root, name), os.path.join(root, new_name))
                
    # Re-walk to rename files and content
    for root, dirs, files in os.walk(target_dir):
        for name in files:
            file_path = os.path.join(root, name)
            
            # read content
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
            except UnicodeDecodeError:
                continue
                
            # replace content
            new_content = content.replace(source_name_lower, target_name_lower)
            new_content = new_content.replace(source_name_camel, target_name_camel)
            new_content = new_content.replace(source_name_upper, target_name_upper)
            
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
                
            # rename file
            if source_name_lower in name or source_name_camel in name:
                new_name = name.replace(source_name_lower, target_name_lower)
                new_name = new_name.replace(source_name_camel, target_name_camel)
                os.rename(file_path, os.path.join(root, new_name))

if __name__ == "__main__":
    generate_skeleton("igaming-source-unibet", "bwin", "Bwin", "BWIN")
    generate_skeleton("igaming-source-unibet", "888sport", "Sport888", "SPORT888")
    generate_skeleton("igaming-source-unibet", "betsson", "Betsson", "BETSSON")
    print("Generation complete.")
