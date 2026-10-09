import subprocess

print("=== PLANE AI WORKERS STATUS ===")
for i in range(18):
    out = subprocess.getoutput(f"kubectl logs -n plane plane-ai-worker-{i} --tail=1 2>&1")
    first_line = out.strip().split("\n")[-1] if out.strip() else "<empty>"
    print(f"worker-{i:2d}: {first_line}")
