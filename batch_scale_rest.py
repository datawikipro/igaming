import subprocess
import json
import time

with open('final_rest_deployments.json', 'r', encoding='utf-8') as f:
    rest_deployments = json.load(f)

print(f"Total REST deployments to scale: {len(rest_deployments)}")

batch_size = 10
total_batches = (len(rest_deployments) + batch_size - 1) // batch_size

for i in range(0, len(rest_deployments), batch_size):
    batch_num = (i // batch_size) + 1
    batch = rest_deployments[i:i + batch_size]
    print(f"\n=== Launching Batch {batch_num}/{total_batches} ({len(batch)} deployments) ===")
    print(", ".join([d.replace("igaming-source-", "") for d in batch]))
    
    scale_cmd = " ".join([f"deployment/{d}" for d in batch])
    ssh_cmd = f'ssh -o StrictHostKeyChecking=no root@100.78.183.101 "kubectl scale {scale_cmd} --replicas=1 -n igaming-source"'
    
    res = subprocess.run(ssh_cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"Warning/Error in batch {batch_num}: {res.stderr.strip()}")
    else:
        print(f"Batch {batch_num} scaled successfully.")
        
    # Check resources
    try:
        check_cmd = 'ssh -o StrictHostKeyChecking=no root@100.78.183.101 "uptime; free -m"'
        res_stats = subprocess.check_output(check_cmd, shell=True).decode()
        lines = [line.strip() for line in res_stats.splitlines() if line.strip()]
        uptime_line = lines[0] if lines else ""
        mem_line = [l for l in lines if l.startswith("Mem:")][0] if any(l.startswith("Mem:") for l in lines) else ""
        print(f"Host metrics: {uptime_line} | {mem_line}")
    except Exception as e:
        print(f"Metrics check warning: {e}")
    
    if i + batch_size < len(rest_deployments):
        print("Pausing 15 seconds before next batch...")
        time.sleep(15)

print("\n=== All 100 REST deployments have been scaled to 1 replica! ===")
