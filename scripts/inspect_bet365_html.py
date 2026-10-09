import subprocess

script = """
kubectl exec -n proxy purevpn-uk-direct-68bc5b7dc5-nswh7 -c curl-tester -- curl -sL --connect-timeout 10 -x http://127.0.0.1:3128 https://www.bet365.com
"""
res = subprocess.run(["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", script],
                     capture_output=True, text=True)
html = res.stdout
print(f"HTML length: {len(html)}")
import re
classes = set(re.findall(r'class="([^"]+)"', html))
print(f"Found {len(classes)} classes:")
for c in sorted(classes)[:30]:
    print(" ", c)
