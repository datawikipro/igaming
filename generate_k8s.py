import os

def generate_k8s(source_name, target_name):
    source_file = f"igaming-k8s/{source_name}.yaml"
    target_file = f"igaming-k8s/{target_name}.yaml"
    
    if not os.path.exists(source_file):
        print(f"Source file {source_file} does not exist.")
        return
        
    with open(source_file, 'r', encoding='utf-8') as f:
        content = f.read()
        
    new_content = content.replace(source_name, target_name)
    
    with open(target_file, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print(f"Generated {target_file}")

if __name__ == "__main__":
    generate_k8s("bet365", "bwin")
    generate_k8s("bet365", "888sport")
    generate_k8s("bet365", "betsson")
