import os
import time

def get_io():
    io_data = {}
    for pid in os.listdir("/proc"):
        if pid.isdigit():
            try:
                with open(f"/proc/{pid}/io", "r") as f:
                    for line in f:
                        if line.startswith("write_bytes:"):
                            io_data[int(pid)] = int(line.split()[1])
            except Exception:
                pass
    return io_data

io1 = get_io()
time.sleep(3)
io2 = get_io()

rates = []
for pid, w2 in io2.items():
    w1 = io1.get(pid, 0)
    rate_mb = (w2 - w1) / 3.0 / 1024 / 1024
    if rate_mb > 0.05:
        try:
            with open(f"/proc/{pid}/cmdline", "r") as f:
                cmd = f.read().replace("\0", " ")[:80]
        except Exception:
            cmd = "unknown"
        rates.append((rate_mb, pid, cmd))

rates.sort(reverse=True)
print("=== TOP DISK WRITERS (MB/s) ===")
for rate, pid, cmd in rates[:20]:
    print(f"{rate:6.2f} MB/s | PID {pid:<7} | {cmd}")
