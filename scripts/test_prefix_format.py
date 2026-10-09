import json
import subprocess

CONFIG = {
  "log": {"level": "info"},
  "outbounds": [
    {
      "type": "shadowsocks",
      "tag": "test-ss",
      "server": "34.158.66.184",
      "server_port": 443,
      "method": "chacha20-ietf-poly1305",
      "password": "sCemjIchu65FMLM8ixhJeL"
    }
  ]
}

# Test format 1: prefix as hex bytes or string
for prefix_val in ["\u0016\u0003\u0001\u0000\u0000", [22, 3, 1, 0, 0], [0x16, 0x03, 0x01, 0x00, 0x00]]:
    CONFIG["outbounds"][0]["prefix"] = prefix_val
    with open('/tmp/test_ss.json', 'w') as f:
        json.dump(CONFIG, f)
    res = subprocess.run(['sing-box', 'check', '-c', '/tmp/test_ss.json'], capture_output=True, text=True)
    print(f"Prefix {type(prefix_val)}: code={res.returncode}, err={res.stderr.strip()}")
