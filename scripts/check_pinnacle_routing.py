import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')

def run_ssh(host, cmd):
    return subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        f'root@{host}', cmd
    ], capture_output=True, text=True, encoding='utf-8', errors='replace')

print("=== Check sing-box rules for pinnacle on ru-proxy (100.83.113.50) ===")
r = run_ssh('100.83.113.50', 'grep -i -C 3 pinnacle /etc/sing-box/config.json')
print(r.stdout if r.stdout else "No pinnacle in sing-box config")

print("=== Direct test to pinnacle via outline-vpn-eu-nl (100.79.1.73) ===")
r_nl = run_ssh('100.79.1.73', 'curl -I -s --max-time 10 https://guest.api.arcadia.pinnacle.com/0.1/sports/29/markets/straight')
print("From NL:", r_nl.stdout[:200], r_nl.stderr)

print("=== Direct test to pinnacle via ru-proxy local curl ===")
r_ru = run_ssh('100.83.113.50', 'curl -I -s --max-time 10 https://guest.api.arcadia.pinnacle.com/0.1/sports/29/markets/straight')
print("From ru-proxy:", r_ru.stdout[:200], r_ru.stderr)
