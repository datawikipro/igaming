## ADDED Requirements

### Requirement: Container Image Registry Parameterization & Monorepo Reactor Build
All microservice Maven builds and Jib container plugins MUST reference image registries through parameterized POM properties (`${docker.registry}`) or standard DNS endpoints without hardcoding static IP addresses. The root reactor POM MUST include all backend modules and crawlers in topological build order.

#### Scenario: Parameterized Jib container build
- **WHEN** building a container image via Jib in any module
- **THEN** the target image name resolves through `${docker.registry}` or standard DNS service names rather than hardcoded raw IP addresses (`100.78.183.101:30500`).

#### Scenario: Complete monorepo reactor build execution
- **WHEN** executing a multi-module command from the repository root (e.g. `mvn -pl <module> test` or `mvn compile`)
- **THEN** Maven resolves the target module within the root reactor without missing project errors.
