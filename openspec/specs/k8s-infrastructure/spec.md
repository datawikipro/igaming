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

---

### Requirement: Dedicated Static Proxies and PureVPN Quota Management
All social media automation profiles, browser automation instances, and geo-sensitive bookmaker scrapers MUST be mapped 1:1 to dedicated static proxy nodes via Kubernetes Service DNS (e.g. `http://purevpn-us-ca.proxy:3128`, `http://purevpn-nl.proxy:3128`). Multi-account PureVPN pools MUST enforce a strict lease ceiling of 8 nodes per account (80% of allowed capacity), reserving 2 connection slots as an anti-exhaustion buffer.

#### Scenario: Dedicated proxy leasing for browser sessions
- **WHEN** a social media automation or scraping pod initializes
- **THEN** it resolves its designated proxy by Kubernetes DNS service name without dynamic IP rotation across sessions.

#### Scenario: PureVPN account quota enforcement
- **WHEN** the proxy manager backend provisions or monitors active PureVPN client nodes
- **THEN** it prevents allocating more than 8 active VPN tunnels per subscription account to guarantee quota stability.

#### Scenario: Multi-Network Dedicated Egress Services
- **WHEN** a container requires egress through a specific country or proxy network
- **THEN** it connects directly via dedicated Kubernetes ClusterIP services (`de-proxy.proxy.svc.cluster.local:3128` for Germany, `us-proxy.proxy.svc.cluster.local:3128` for USA, `fi-proxy.proxy.svc.cluster.local:3128` for Finland), eliminating host-level interception and enabling per-container network binding.

---

### Requirement: Regional TSPU/RKN Censorship Bypass and Egress Routing
All cluster egress traffic bound for censorship-blocked sportsbook endpoints or OpenVPN management domains (`*.ptoserver.com`, `*.purevpn.com`) MUST be transparently steered through encrypted tunnels (Shadowsocks in Finland `outline-fi` or Shadowsocks/VLESS in the US/EU) on cluster edge routers (`sing-box` on `ru-proxy`).

#### Scenario: OpenVPN connection under TSPU deep packet inspection
- **WHEN** an internal VPN pod attempts connection to a PureVPN server hostname
- **THEN** the edge router transparently forwards the handshake traffic through `outline-fi`, circumventing DPI throttling.

---

### Requirement: Autonomous DAG Dependency Task Orchestration
Autonomous AI development task execution across worker pools (`plane-ai-worker-0..17`) MUST be governed by a DAG dependency orchestrator resolving dependency graphs and executing tasks with sub-millisecond dispatching.

#### Scenario: Dependent issue dispatching
- **WHEN** a parent issue in Plane transitions to `Завершено`
- **THEN** the autonomous orchestrator evaluates `dependency_graph.yaml`, unblocks waiting child issues, and dispatches them immediately to available AI developer workers.

---

### Requirement: Kubernetes Startup Probe Standardization
Microservices and source modules with significant startup initialization times (such as Spring Boot JPA schema validation and HikariCP connection warming) MUST declare `startupProbe` with adequate failure thresholds before `livenessProbe` and `readinessProbe` activate.

#### Scenario: Service startup probe evaluation
- **WHEN** a service deployment starts up
- **THEN** Kubernetes evaluates the `startupProbe` first, withholding liveness restart triggers until the service successfully passes initial probe checks.

---

### Requirement: Container Image Registry Parameterization & Monorepo Reactor Build
All microservice Maven builds and Jib container plugins MUST reference image registries through parameterized POM properties (`${docker.registry}`) or standard DNS endpoints without hardcoding static IP addresses. The root reactor POM MUST include all backend modules and crawlers in topological build order.

#### Scenario: Parameterized Jib container build
- **WHEN** building a container image via Jib in any module
- **THEN** the target image name resolves through `${docker.registry}` or standard DNS service names rather than hardcoded raw IP addresses (`100.78.183.101:30500`).

#### Scenario: Complete monorepo reactor build execution
- **WHEN** executing a multi-module command from the repository root (e.g. `mvn -pl <module> test` or `mvn compile`)
- **THEN** Maven resolves the target module within the root reactor without missing project errors.


