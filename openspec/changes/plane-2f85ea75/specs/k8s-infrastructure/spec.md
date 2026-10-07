# Delta Specification: Kubernetes Infrastructure & Node Pressure Protection

## ADDED Requirements

### Requirement: Bare-Metal Node Pressure Protection and Night Quiet Profile
The primary bare-metal node `xeon-srv` hosting K8s master and worker workloads must be protected against memory, disk, and CPU pressure by enforcing conservative resource profiles, preventing un-paced rolling updates that saturate etcd, and strictly honoring the night quiet window (22:00 to 10:00 MSK).

#### Scenario: Night quiet hours resource governance
- **WHEN** current time is between 22:00 and 10:00 MSK
- **THEN** heavy compilation jobs, un-paced mass pod rollouts, and intensive disk I/O operations are throttled to keep server fan speeds low.

#### Scenario: Database asynchronous commit policy
- **WHEN** deploying PostgreSQL databases for crawler and aggregator sources
- **THEN** `synchronous_commit = off` is enforced to prevent disk I/O saturation on Xeon physical storage.
