import os
import glob
import re

def fix_manifests():
    k8s_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "igaming-k8s")
    yaml_files = sorted(glob.glob(os.path.join(k8s_dir, "*.yaml")))
    
    modified_files = []
    
    # Pattern to match the command and args block that executes java -jar app.jar
    pattern = re.compile(
        r"[ \t]*command:\s*\[\s*\"/bin/sh\"\s*,\s*\"-c\"\s*\]\s*\n"
        r"[ \t]*args:\s*\n"
        r"(?:[ \t]*-[ \t]*\|\s*\n)?"
        r"(?:[ \t]*[^\n]*\n)*?"
        r"[ \t]*exec\s+java\s+-jar\s+app\.jar\s*\n",
        re.MULTILINE
    )

    for file_path in yaml_files:
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        
        if "app.jar" in content:
            new_content = pattern.sub("", content)
            if new_content != content:
                with open(file_path, "w", encoding="utf-8") as f:
                    f.write(new_content)
                modified_files.append(os.path.basename(file_path))
            else:
                print(f"WARN: 'app.jar' found in {os.path.basename(file_path)} but pattern didn't match!")
    
    print(f"Successfully cleaned command overrides in {len(modified_files)} files:")
    for fn in modified_files:
        print(f"  - {fn}")

if __name__ == "__main__":
    fix_manifests()
