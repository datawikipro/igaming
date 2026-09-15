# Delta Spec: In-Memory Storage & Java Schema Management

## ADDED REQUIREMENTS

### Requirement: In-Memory PostgreSQL for Source Operational Caching
Every bookmaker database in `igaming-source` MUST mount storage on `emptyDir: { medium: Memory }` with a 1Gi size limit and physical disk write operations disabled via server arguments.

#### Scenario: In-memory database initialization
- **WHEN** the source PostgreSQL StatefulSet starts in Kubernetes
- **THEN** PostgreSQL mounts storage on `emptyDir` RAM disk (`tmpfs`) with `fsync=off` and `synchronous_commit=off`, producing zero physical disk write I/O.

### Requirement: Java-Driven Schema Auto-Migration
Every crawler and loader application MUST execute `DatabaseMigrationRunner` on startup before Hibernate EntityManager initialization to ensure schema consistency.

#### Scenario: Pre-Hibernate database schema initialization
- **WHEN** a bookmaker source crawler or loader initializes its Spring application context
- **THEN** `DatabaseMigrationRunner` executes high-precedence idempotent DDL ensuring `match_cache`, `match_factor`, `league_cache`, and all columns exist before Hibernate metadata validation.

### Requirement: Headless Browser Process Lifecycle & Zombie Reaping
All pod specifications executing browser automation MUST enable `shareProcessNamespace: true` so orphaned browser processes are reaped by the pod pause container.

#### Scenario: Browser automation zombie process containment
- **WHEN** a browser-based crawler terminates or crashes Chrome subprocesses
- **THEN** PID namespace sharing allows the pod pause container (PID 1) to harvest defunct processes, preventing process table exhaustion.
