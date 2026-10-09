import subprocess

cmd = """
cd /data/igaming/igaming-dto
/usr/local/bin/mvn install -DskipTests -B
"""

res = subprocess.run([
    'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
    'root@100.78.183.101', 'bash -c "' + cmd.replace('\r', '').replace('\n', '; ') + '"'
], capture_output=True, text=True, timeout=60)
print('STDOUT:\n', res.stdout[-1500:])
if res.stderr:
    print('STDERR:\n', res.stderr[-1000:])
print('RC:', res.returncode)
