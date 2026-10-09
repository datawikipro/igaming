
with open('/proc/1060313/task/1060319/syscall', 'r') as f:
    sc = f.read().split()

print("Syscall info:", sc)
syscall_num = int(sc[0])
dfd = int(sc[1], 16)
filename_ptr = int(sc[2], 16)
flags = int(sc[3], 16)

print(f"Filename ptr: 0x{filename_ptr:x}")

with open('/proc/1060313/mem', 'rb', buffering=0) as f:
    f.seek(filename_ptr)
    s = f.read(512)
    # read until null byte
    null_idx = s.find(b'\x00')
    if null_idx != -1:
        s = s[:null_idx]
    print("FIFO PATH:", s.decode('utf-8', errors='replace'))
