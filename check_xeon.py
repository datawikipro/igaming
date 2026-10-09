import subprocess

ssh_cmd = [
    "ssh",
    "-i", r"C:\Users\chernousov_a\.ssh\id_ed25519",
    "root@100.78.183.101",
    """python3 -c '
import subprocess
res = subprocess.check_output(["kubectl", "get", "pods", "-n", "igaming-source", "--no-headers"]).decode()
names = ["betbra", "bet7k", "betesporte", "betnacional", "betway", "esportesdasorte", "vaidebet", "10bet", "bcgame", "betdaq", "matchbook", "fortuna", "bookmaker", "apuestatotal", "bet9ja", "bovada", "crickex", "estave", "favbet", "iddaa", "interwetten", "mozzartbet", "nike"]
header = "%-48s %-8s %-12s %-10s %-8s" % ("POD NAME", "READY", "STATUS", "RESTARTS", "AGE")
print(header)
print("-" * 88)
for line in res.strip().splitlines():
    p = line.split()
    if not p or "-db-" in p[0]: continue
    if any(n in p[0] for n in names):
        print("%-48s %-8s %-12s %-10s %-8s" % (p[0], p[1], p[2], p[3], p[4]))
'"""
]

res = subprocess.run(ssh_cmd, capture_output=True, text=True)
print(res.stdout)
if res.stderr:
    print("STDERR:", res.stderr)
