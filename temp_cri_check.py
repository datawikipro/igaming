
import os, subprocess, glob

ps = subprocess.check_output(["ps", "-eo", "args"]).decode()
live_ids = set()
for line in ps.splitlines():
    if "containerd-shim-runc-v2" in line and "-id" in line:
        parts = line.split()
        if "-id" in parts:
            live_ids.add(parts[parts.index("-id") + 1])

all_dirs = [os.path.basename(p) for p in glob.glob("/run/containerd/io.containerd.grpc.v1.cri/containers/*")]
dead_dirs = [cid for cid in all_dirs if cid not in live_ids]
print(f"Total container dirs: {len(all_dirs)}, Live: {len(all_dirs) - len(dead_dirs)}, Dead: {len(dead_dirs)}")

all_sandboxes = [os.path.basename(p) for p in glob.glob("/run/containerd/io.containerd.grpc.v1.cri/sandboxes/*")]
dead_sandboxes = [sid for sid in all_sandboxes if sid not in live_ids]
print(f"Total sandboxes: {len(all_sandboxes)}, Live: {len(all_sandboxes) - len(dead_sandboxes)}, Dead: {len(dead_sandboxes)}")
