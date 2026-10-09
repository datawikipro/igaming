import subprocess
import time

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

def run_ssh(cmd, timeout=40):
    full_cmd = [
        'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        XEON_HOST, cmd
    ]
    res = subprocess.run(full_cmd, capture_output=True, text=True, timeout=timeout)
    return res.stdout, res.stderr, res.returncode

print("=== 1. SCALING INGESTION TO 0 REPLICAS ===")
out, err, _ = run_ssh("kubectl scale deployment/igaming-aggregator-ingestion -n igaming-dev --replicas=0")
print(out.strip())

print("Waiting for pods to terminate...")
for _ in range(20):
    out, _, _ = run_ssh("kubectl get pods -n igaming-dev -l app=igaming-aggregator-ingestion --no-headers")
    if not out.strip():
        print("All pods terminated.")
        break
    time.sleep(2)

print("\n=== 2. WAITING FOR KAFKA GROUP TO BECOME EMPTY ===")
state_cmd = (
    "kubectl exec -n igaming-master kafka-0 -- sh -c "
    "\"echo '127.0.0.1 kafka.igaming-master.svc.cluster.local' > /tmp/myhosts && "
    "KAFKA_OPTS='-Djdk.net.hosts.file=/tmp/myhosts' /opt/kafka/bin/kafka-consumer-groups.sh "
    "--bootstrap-server localhost:9092 --timeout 15000 --describe --group aggregator-ingestion-v2-updates --state\""
)

for attempt in range(25):
    out, _, _ = run_ssh(state_cmd)
    print(f"[{attempt+1}] State output:\n" + out.strip())
    if "Empty" in out or "Dead" in out:
        print("Group is Empty! Ready to reset offsets.")
        break
    time.sleep(3)
else:
    print("Warning: Group did not become Empty within timeout, trying reset anyway...")

print("\n=== 3. RESETTING OFFSETS TO LATEST ===")
reset_cmd = (
    "kubectl exec -n igaming-master kafka-0 -- sh -c "
    "\"KAFKA_OPTS='-Djdk.net.hosts.file=/tmp/myhosts' /opt/kafka/bin/kafka-consumer-groups.sh "
    "--bootstrap-server localhost:9092 --timeout 30000 --group aggregator-ingestion-v2-updates "
    "--topic odds.updates --reset-offsets --to-latest --execute\""
)
out, err, code = run_ssh(reset_cmd, timeout=60)
print(out)
if err: print("ERR:", err)

print("\n=== 4. SCALING INGESTION BACK TO 2 REPLICAS ===")
out, err, _ = run_ssh("kubectl scale deployment/igaming-aggregator-ingestion -n igaming-dev --replicas=2")
print(out.strip())

print("Waiting for rollout...")
out, err, _ = run_ssh("kubectl rollout status deployment/igaming-aggregator-ingestion -n igaming-dev --timeout=120s", timeout=130)
print(out.strip())

print("\n=== RESET PROCESS COMPLETE ===")
