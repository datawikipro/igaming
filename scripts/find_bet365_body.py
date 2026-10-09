import subprocess

script = """
kubectl logs -n igaming-source igaming-source-bet365-crawler-79686cd75b-xs5h8 -c igaming-source-bet365-crawler | grep 'DOM parsing failed' | tail -n 1
"""
res = subprocess.run(["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", "root@100.78.183.101", script],
                     capture_output=True, text=True)

line = res.stdout
print("Line length:", len(line))
idx = line.find("<body")
if idx != -1:
    print("Found <body>:")
    print(line[idx:idx+1500])
else:
    print("Body not in first 2000 chars, let's see what is in there:")
    print(line[-1000:])
