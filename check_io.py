import os
import glob

procs = []
for p in glob.glob("/proc/[0-9]*/io"):
    try:
        pid = p.split("/")[2]
        with open(f"/proc/{pid}/comm") as fc:
            comm = fc.read().strip()
        with open(p) as f:
            for line in f:
                if line.startswith("write_bytes:"):
                    wb = int(line.split(":")[1])
                    procs.append((wb, pid, comm))
    except Exception:
        pass

procs.sort(reverse=True)
for wb, pid, comm in procs[:25]:
    print(f"{pid:>7} {comm:<22} {wb / 1024 / 1024:.2f} MB")
