import subprocess

script = """
kubectl logs -n igaming-source igaming-source-bet365-crawler-79686cd75b-xs5h8 -c igaming-source-bet365-crawler --tail=200
"""
res = subprocess.run(["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", script],
                     capture_output=True, text=True)

for line in res.stdout.splitlines():
    if "DOM parsing failed. First 2000 chars of HTML body:" in line:
        body = line.split("DOM parsing failed. First 2000 chars of HTML body:")[1]
        print("Captured body prefix (1000 chars):")
        print(body[:1000])
        import re
        classes = set(re.findall(r'class="([^"]+)"', body))
        print("Classes found:", classes)
