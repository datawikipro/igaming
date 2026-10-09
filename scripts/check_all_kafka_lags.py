import subprocess
import time

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def run_ssh(remote_cmd):
    full_cmd = [
        'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, remote_cmd
    ]
    res = subprocess.run(full_cmd, capture_output=True, text=True, timeout=60)
    return res.stdout, res.stderr, res.returncode

groups = [
    'aggregator-ingestion-v2-updates',
    'llm-gateway-group',
    'normalizer-llm-group',
    'aggregator-ingestion-v2-outrights'
]

print("=== KAFKA CONSUMER GROUP LAG REPORT ===")
for group in groups:
    for attempt in range(3):
        k_cmd = (
            f"kubectl exec -n igaming-master kafka-0 -- sh -c "
            f"\"echo '127.0.0.1 kafka.igaming-master.svc.cluster.local' > /tmp/myhosts && "
            f"KAFKA_OPTS='-Djdk.net.hosts.file=/tmp/myhosts' /opt/kafka/bin/kafka-consumer-groups.sh "
            f"--bootstrap-server localhost:9092 --timeout 30000 --describe --group {group}\""
        )
        out, err, code = run_ssh(k_cmd)
        if "LAG" in out or "Consumer group" in out:
            lines = out.strip().split("\n")
            total_lag = 0
            for l in lines:
                parts = l.split()
                if len(parts) >= 6 and parts[0] == group:
                    try:
                        lag_val = int(parts[5])
                        total_lag += lag_val
                    except Exception:
                        pass
            print(f"\n--- Group: {group} (Total LAG: {total_lag:,}) ---")
            for l in lines:
                if group in l or "GROUP" in l or "PARTITION" in l:
                    print("  " + l)
            break
        else:
            time.sleep(2)
    else:
        print(f"\n--- Group: {group} (Failed to query after 3 attempts) ---")
        if err:
            print("  Error:", err.strip()[:200])

print("\n=== REPORT FINISHED ===")
