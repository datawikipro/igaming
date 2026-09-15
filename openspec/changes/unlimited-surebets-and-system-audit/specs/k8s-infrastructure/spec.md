## ADDED Requirements

### Requirement: Mechanical Disk Bottleneck Mitigation and RAM Offloading
Physical server storage on mechanical SAS disks (`xeon-srv`) must be protected from high queue depth by utilizing abundant host RAM (188 GiB total, > 100 GiB available) for volatile and intermediate file operations.

#### Scenario: Host ephemeral mount on RAM tmpfs
- **WHEN** temporary files are created on the host by system services or diagnostics
- **THEN** `/tmp` is mounted as a 24GB in-memory filesystem (`tmpfs size=24G,mode=1777,nosuid,nodev`) and persisted in `/etc/fstab`.

#### Scenario: Ext4 filesystem mount options for mechanical storage
- **WHEN** mounting root (`/`) or storage (`/data`) on mechanical SAS disks
- **THEN** mount options `noatime,commit=60` must be active and persisted in `/etc/fstab`, reducing ext4 journal commit thrashing by 12x and eliminating continuous access-time inode writes.

#### Scenario: Linux kernel dirty page flush pacing
- **WHEN** write-heavy batch operations or database flushes occur
- **THEN** kernel dirty parameters are enforced via `/etc/sysctl.d/99-disk-tuning.conf` with `vm.dirty_background_bytes = 134217728` (128MB) and `vm.dirty_bytes = 536870912` (512MB), preventing multi-gigabyte disk write stalls.

### Requirement: High-RAM PostgreSQL Engine Scaling
All PostgreSQL instances must utilize available RAM to maximize buffer hit ratios and minimize synchronous disk writes.

#### Scenario: Aggregator Core database memory sizing
- **WHEN** the central `igaming-aggregator-db-0` starts
- **THEN** PostgreSQL parameters are set to `shared_buffers = 4GB`, `work_mem = 64MB`, `effective_cache_size = 6GB`, and `maintenance_work_mem = 512MB`, maintaining a cache hit ratio exceeding 98%.

#### Scenario: Bookmaker and Portal database checkpoint smoothing
- **WHEN** 52 source bookmaker databases or portal databases write transaction logs and checkpoints
- **THEN** parameters `checkpoint_timeout = 15min`, `checkpoint_completion_target = 0.9`, `max_wal_size = 2GB`, `min_wal_size = 512MB`, and `synchronous_commit = off` prevent simultaneous disk write spikes.

### Requirement: Control-Plane Leader Election Lease Durability
Kubernetes static control-plane components (`kube-controller-manager`, `kube-scheduler`) must tolerate temporary I/O latency without restarting or dropping leader leases.

#### Scenario: Lease duration and renew deadline
- **WHEN** configuring controller-manager or scheduler leader election flags
- **THEN** `--leader-elect-lease-duration=120s` and `--leader-elect-renew-deadline=60s` prevent spurious leader election loss.
