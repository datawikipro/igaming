## ADDED Requirements

### Requirement: Dedicated Static Proxies and PureVPN Quota Management
All social media automation profiles, browser automation instances, and geo-sensitive bookmaker scrapers MUST be mapped 1:1 to dedicated static proxy nodes via Kubernetes Service DNS (e.g. `http://purevpn-us-ca.proxy:3128`, `http://purevpn-nl.proxy:3128`). Multi-account PureVPN pools MUST enforce a strict lease ceiling of 8 nodes per account (80% of allowed capacity), reserving 2 connection slots as an anti-exhaustion buffer.

#### Scenario: Dedicated proxy leasing for browser sessions
- **WHEN** a social media automation or scraping pod initializes
- **THEN** it resolves its designated proxy by Kubernetes DNS service name without dynamic IP rotation across sessions.

#### Scenario: PureVPN account quota enforcement
- **WHEN** the proxy manager backend provisions or monitors active PureVPN client nodes
- **THEN** it prevents allocating more than 8 active VPN tunnels per subscription account to guarantee quota stability.

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
