import subprocess
import json

test_singbox = {
    "log": {"level": "info"},
    "inbounds": [
        {
            "type": "mixed",
            "tag": "test-in",
            "listen": "127.0.0.1",
            "listen_port": 31299
        }
    ],
    "outbounds": [
        {
            "type": "shadowsocks",
            "tag": "test-us",
            "server": "100.110.168.10",
            "server_port": 443,
            "method": "chacha20-ietf-poly1305",
            "password": "qZORoKkMxgUdJu7dZrGVY7",
            "bind_interface": "tailscale0"
        },
        {
            "type": "shadowsocks",
            "tag": "test-nl",
            "server": "34.158.66.184",
            "server_port": 443,
            "method": "chacha20-ietf-poly1305",
            "password": "sCemjIchu65FMLM8ixhJeL"
        }
    ],
    "route": {
        "rules": [
            {
                "inbound": ["test-in"],
                "outbound": "test-us"
            }
        ]
    }
}

runner = f"""
import json, subprocess, time

with open('/tmp/test_singbox.json', 'w') as f:
    json.dump({json.dumps(test_singbox)}, f)

# Run sing-box in background
proc = subprocess.Popen(['/usr/bin/sing-box', 'run', '-c', '/tmp/test_singbox.json'])
time.sleep(2)
try:
    res = subprocess.run(['curl', '-x', 'http://127.0.0.1:31299', 'https://oauth2.googleapis.com/token', '-v', '--connect-timeout', '6'], capture_output=True, text=True)
    print("STDOUT:", res.stdout[:300])
    print("STDERR:", res.stderr[-500:])
    print("CODE:", res.returncode)
finally:
    proc.terminate()
"""

cmd = ['ssh', '-i', 'C:\\Users\\chernousov_a\\.ssh\\id_ed25519', '-o', 'BatchMode=yes', 'prodius@100.83.113.50', f'sudo python3 -c {subprocess.list2cmdline([runner])}']
res = subprocess.run(cmd, capture_output=True, text=True)
print("RESULT:")
print(res.stdout)
print(res.stderr)
