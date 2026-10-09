import subprocess
SSH_HOST = 'xeon-local'
SSH_CONFIG = r'C:\Users\chernousov_a\.ssh\config'
sql = "SELECT sequence_id, state_id FROM issues WHERE sequence_id >= 50 ORDER BY sequence_id;"
cmd = ['ssh', '-F', SSH_CONFIG, '-o', 'BatchMode=yes', SSH_HOST, 'kubectl exec -i -n plane deployment/plane-db-primary -- psql -U plane -d plane -t -A -F ,']
res = subprocess.run(cmd, input=sql, text=True, capture_output=True, timeout=15)
print('STDOUT:\n', res.stdout[:500])
print('STDERR:\n', res.stderr)
