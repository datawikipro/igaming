# Kubernetes Infrastructure Specification

## Purpose
Governs cluster resource placement, network addressing policies, non-blocking connection management, and container build standards across all igaming microservices.

## Requirements

### Requirement: Strict DNS Service Names Rule
All inter-service communications, database connections, initContainers, configuration files, and deployment manifests must exclusively use Kubernetes DNS service names. Hardcoded IP addresses are strictly prohibited.

#### Scenario: Database connection configuration
- **WHEN** configuring a database host in `application.properties` or environment variables
- **THEN** the host is set using standard K8s DNS (e.g. `igaming-source-winline-db` or `igaming-source-winline-db.igaming-source.svc.cluster.local`) rather than raw IP addresses.

#### Scenario: InitContainer readiness probe
- **WHEN** an initContainer waits for database readiness
- **THEN** it resolves the service by DNS hostname and loops until the connection succeeds (`until psql -h "$SERVICE_DNS_NAME" ...; do sleep 2; done`).

---

### Requirement: Node Scheduling Placement
Workloads must be scheduled on nodes matching their availability and storage requirements via explicit `nodeSelector` / affinity rules.

#### Scenario: PostgreSQL StatefulSet scheduling
- **WHEN** deploying any database (`*-db`)
- **THEN** `nodeSelector` requires `node-type=master`.

#### Scenario: Crawlers and loaders scheduling
- **WHEN** deploying crawler or loader pods
- **THEN** `nodeSelector` allows `standard` and `spot` nodes with preference for `standard` on primary workers.

---

### Requirement: Non-Blocking HikariCP and Hibernate Settings
All Spring Boot applications connecting to PostgreSQL must configure non-blocking Hikari initialization to avoid thread blocking during startup.

#### Scenario: Application database startup
- **WHEN** the Spring Boot application initializes `HikariDataSource`
- **THEN** properties `initialization-fail-timeout=0`, `connection-timeout=5000`, `validation-timeout=3000`, and `hibernate.temp.use_jdbc_metadata_defaults=false` prevent startup deadlocks.

---

### Requirement: PowerShell Jib Build Parameter Quoting
Maven Jib container builds executed under Windows PowerShell must enclose all dotted `-D` parameters in double quotes.

#### Scenario: Building and pushing container image
- **WHEN** executing Maven Jib in PowerShell
- **THEN** command arguments are formatted as `mvn.cmd -pl <module> jib:build "-Djib.to.image=ghcr.io/datawikipro/<module>:latest" "-Djib.to.auth.username=datawikipro" "-Djib.to.auth.password=<token>" -DskipTests`.
