import subprocess

XEON_HOST = 'root@100.78.183.101'
SSH_KEY = r'C:\Users\chernousov_a\.ssh\id_ed25519'

cmd = (
    "kubectl exec -n igaming-master kafka-0 -- sh -c "
    "\"KAFKA_OPTS='-Djdk.net.hosts.file=/tmp/myhosts' /opt/kafka/bin/kafka-console-consumer.sh "
    "--bootstrap-server localhost:9092 --topic odds.updates --partition 0 --offset 845000 --max-messages 2\""
)

res = subprocess.run([
    'ssh', '-i', SSH_KEY, '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
    XEON_HOST, cmd
], capture_output=True, text=True, timeout=30)

print("Sample messages from P0:")
print(res.stdout[:1000])
if res.stderr:
    print("STDERR:", res.stderr[:300])
