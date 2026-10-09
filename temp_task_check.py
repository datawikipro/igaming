
import subprocess
out = subprocess.check_output(["ctr", "-n", "k8s.io", "tasks", "list"]).decode()
counts = {}
for line in out.strip().splitlines()[1:]:
    parts = line.split()
    if len(parts) >= 3:
        st = parts[2]
        counts[st] = counts.get(st, 0) + 1
print("TASK COUNTS:", counts)
