## ADDED Requirements

### Requirement: Standardized 512Mi Memory Sizing for Bookmaker Databases
All 52 bookmaker PostgreSQL StatefulSets SHALL allocate a minimum of 512Mi memory limit and 128Mi memory request (`limits: {cpu: 500m, memory: 512Mi}`, `requests: {cpu: 50m, memory: 128Mi}`) with `synchronous_commit = off` to eliminate OOM kills during high-frequency odds updates and large match factor collections.

#### Scenario: Database memory allocation during heavy line loading
- **WHEN** a bookmaker accumulates over 4,000 matches with tens of thousands of factors in `match_factor`
- **THEN** PostgreSQL RSS memory remains stably contained within 512Mi without triggering Linux kernel OOM killer terminations (exit code 137).

### Requirement: Paced Kubernetes Deployment Rollouts
Mass rollouts or restarts across cluster deployments SHALL be executed with a minimum pacing interval of 200 milliseconds between individual resources to prevent write transaction starvation and lease timeouts in the Kubernetes `etcd` control plane.

#### Scenario: Cluster-wide rolling restart execution
- **WHEN** all 101 crawler and loader deployments require rolling restarts
- **THEN** an automated script applies rollout restart sequentially with a minimum 200ms delay per resource, keeping `kube-apiserver` latency flat.
