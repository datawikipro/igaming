import subprocess

output = subprocess.check_output(["kubectl", "get", "pods", "-n", "igaming-source"]).decode("utf-8")

status_counts = {}
for line in output.splitlines()[1:]:
    parts = line.split()
    if len(parts) >= 3:
        status = parts[2]
        status_counts[status] = status_counts.get(status, 0) + 1

print("--- IGAMING-SOURCE POD STATUS SUMMARY ---")
for status, count in sorted(status_counts.items(), key=lambda x: x[1], reverse=True):
    print(f"  {status}: {count}")
