import subprocess

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

cmd = (
    "kubectl exec -n igaming-master kafka-0 -- sh -c "
    "\"echo '127.0.0.1 kafka.igaming-master.svc.cluster.local' > /tmp/myhosts && "
    "KAFKA_OPTS='-Djdk.net.hosts.file=/tmp/myhosts' /opt/kafka/bin/kafka-topics.sh "
    "--bootstrap-server localhost:9092 --describe --topic odds.updates\""
)

res = subprocess.run([
    'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
    XEON_HOST, cmd
], capture_output=True, text=True, timeout=60)


print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
