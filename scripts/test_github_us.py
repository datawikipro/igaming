import subprocess

runner = """
import subprocess

# Run sing-box with test-us outbound on 31299
proc = subprocess.Popen(['/usr/bin/sing-box', 'run', '-c', '/tmp/test_singbox.json'])
import time; time.sleep(2)
try:
    res = subprocess.run(['curl', '-x', 'http://127.0.0.1:31299', 'https://github.com', '-I', '--connect-timeout', '5'], capture_output=True, text=True)
    print("GITHUB VIA TEST-US:")
    print("STDOUT:", res.stdout[:300])
    print("STDERR:", res.stderr)
    print("CODE:", res.returncode)
finally:
    proc.terminate()
"""

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', f'sudo python3 -c {subprocess.list2cmdline([runner])}']
res = subprocess.run(cmd, capture_output=True, text=True)
print(res.stdout)
