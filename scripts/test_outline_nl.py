import json
import subprocess
import time

TEST_CONFIG = {
  "log": {"level": "info"},
  "inbounds": [
    {
      "type": "mixed",
      "tag": "test-in",
      "listen": "127.0.0.1",
      "listen_port": 13128
    }
  ],
  "outbounds": [
    {
      "type": "shadowsocks",
      "tag": "outline-nl",
      "server": "34.158.66.184",
      "server_port": 443,
      "method": "chacha20-ietf-poly1305",
      "password": "sCemjIchu65FMLM8ixhJeL"
    }
  ]
}

with open('/tmp/test_nl_ss.json', 'w') as f:
    json.dump(TEST_CONFIG, f)

p = subprocess.Popen(['sing-box', 'run', '-c', '/tmp/test_nl_ss.json'])
time.sleep(2)
try:
    res = subprocess.run(['curl', '-x', 'http://127.0.0.1:13128', '-s', '-I', '--max-time', '5', 'https://www.google.com'], capture_output=True, text=True)
    print("Curl result via outline-nl:")
    print(res.stdout[:200])
    print(res.stderr[:200])
finally:
    p.kill()
