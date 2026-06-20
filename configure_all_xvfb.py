import os
import re
import glob

K8S_DIR = "igaming-k8s"
TARGET_YAMLS = glob.glob(f"{K8S_DIR}/*.yaml")

XVFB_COMMAND_BLOCK = """        command: ["/bin/sh", "-c"]
        args:
        - |
          Xvfb :99 -ac -screen 0 1920x1080x24 -nolisten tcp &
          sleep 2
          export DISPLAY=:99
          exec java -jar app.jar"""

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    documents = content.split("---")
    updated = False

    for idx, doc in enumerate(documents):
        if "kind: Deployment" in doc and "-crawler" in doc:
            if "Xvfb" in doc:
                continue

            match = re.search(r'(imagePullPolicy:\s*Always\n)', doc)
            if match:
                pos = match.end()
                documents[idx] = doc[:pos] + XVFB_COMMAND_BLOCK + "\n" + doc[pos:]
                doc = documents[idx]
                updated = True
                print(f"[{os.path.basename(filepath)}] Added Xvfb command block.")

            profile_match = re.search(r'(-\s*name:\s*APP_BROWSER_STEALTH_PROFILE\s*\n\s*value:\s*)(\w+)', doc)
            if profile_match:
                old_val = profile_match.group(2)
                if old_val != "XVFB_HEADED":
                    doc = re.sub(
                        r'(-\s*name:\s*APP_BROWSER_STEALTH_PROFILE\s*\n\s*value:\s*)\w+',
                        r'\1XVFB_HEADED',
                        doc
                    )
                    documents[idx] = doc
                    updated = True
                    print(f"[{os.path.basename(filepath)}] Updated stealth profile from {old_val} to XVFB_HEADED.")

    if updated:
        new_content = "---".join(documents)
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)

for f in TARGET_YAMLS:
    process_file(f)
