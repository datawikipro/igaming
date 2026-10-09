import subprocess
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def run_ssh(cmd, timeout=60):
    print(f"\n>>> SSH: {cmd[:100]}...", flush=True)
    res = subprocess.run([
        'ssh', '-i', SSH_KEY,
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=timeout)
    if res.stdout:
        print("STDOUT:", res.stdout.strip()[:1000], flush=True)
    if res.stderr:
        print("STDERR:", res.stderr.strip()[:1000], flush=True)
    if res.returncode != 0:
        print(f"FAILED with exit code {res.returncode}", flush=True)
        sys.exit(res.returncode)
    return res

deployments = [
    ("igaming-source-winline-crawler", "igaming-source-winline-crawler"),
    ("igaming-source-betboom-crawler", "igaming-source-betboom-crawler"),
    ("igaming-source-sbobet-crawler", "igaming-source-sbobet-crawler"),
    ("igaming-source-betano", "igaming-source-betano"),
    ("igaming-source-ligastavok-crawler", "igaming-source-ligastavok-crawler"),
    ("igaming-source-leon-crawler", "igaming-source-leon-crawler")
]

for dep_name, container_name in deployments:
    print(f"\n==========================================")
    print(f"Patching {dep_name} (container {container_name})")
    print(f"==========================================")
    
    # 1. Fetch current deployment json
    res = run_ssh(f"kubectl get deployment {dep_name} -n igaming-source -o json")
    dep = json.loads(res.stdout)
    
    spec = dep['spec']['template']['spec']
    
    # Ensure volume
    volumes = spec.setdefault('volumes', [])
    if not any(v['name'] == 'playwright-cache' for v in volumes):
        volumes.append({
            'name': 'playwright-cache',
            'hostPath': {
                'path': '/data/playwright-browsers',
                'type': 'Directory'
            }
        })
        print("Added volume playwright-cache")
    
    # Update container
    for c in spec['containers']:
        if c['name'] == container_name:
            v_mounts = c.setdefault('volumeMounts', [])
            if not any(vm['name'] == 'playwright-cache' for vm in v_mounts):
                v_mounts.append({
                    'name': 'playwright-cache',
                    'mountPath': '/root/.cache/ms-playwright'
                })
                print("Added volumeMount /root/.cache/ms-playwright")
            
            env = c.setdefault('env', [])
            env_dict = {e['name']: e for e in env}
            
            # Set PLAYWRIGHT envs
            env_dict['PLAYWRIGHT_BROWSERS_PATH'] = {'name': 'PLAYWRIGHT_BROWSERS_PATH', 'value': '/root/.cache/ms-playwright'}
            env_dict['PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS'] = {'name': 'PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS', 'value': 'true'}
            env_dict['APP_AGGREGATOR_URL'] = {'name': 'APP_AGGREGATOR_URL', 'value': 'http://igaming-aggregator.igaming-dev.svc.cluster.local:8080'}
            
            c['env'] = list(env_dict.values())
            print("Updated env vars")
            break
            
    # Apply back
    patched_json = json.dumps(dep)
    # Save to remote temp file and apply
    run_ssh(f"cat << 'EOF' > /tmp/{dep_name}.json\n{patched_json}\nEOF")
    run_ssh(f"kubectl apply -f /tmp/{dep_name}.json && rm -f /tmp/{dep_name}.json")
    print(f"Applied successfully for {dep_name}")

print("\nAll 4 deployments patched successfully!")
