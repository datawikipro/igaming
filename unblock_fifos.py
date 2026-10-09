
import os, glob

print("Scanning for FIFOs in /run/containerd...")
fifos = glob.glob("/run/containerd/io.containerd.grpc.v1.cri/containers/*/io/*/*")
print(f"Found {len(fifos)} FIFOs")

unblocked = 0
for f in fifos:
    try:
        # Open in non-blocking write mode and immediately close
        fd = os.open(f, os.O_WRONLY | os.O_NONBLOCK)
        os.close(fd)
        unblocked += 1
    except Exception as e:
        pass

print(f"Wrote to and closed {unblocked} FIFOs")
