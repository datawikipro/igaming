## ADDED Requirements

### Requirement: Native Jib Container Entrypoint Preservation
Kubernetes manifests for modules built with Maven Jib SHALL NOT override container entrypoints with shell wrappers pointing to nonexistent monolithic jar files (such as `exec java -jar app.jar`), preserving Jib's default exploded classpath entrypoint.

#### Scenario: Deployment of Jib container
- **WHEN** a crawler or loader deployment is created or updated in K8s
- **THEN** the pod template relies on Jib's native entrypoint without `command: ["/bin/sh", "-c"]` wrapper overriding `app.jar`.

### Requirement: Actuator Health Probes Standard
Kubernetes container definitions for Spring Boot services SHALL configure readiness and liveness probes against Spring Boot Actuator endpoints (`/actuator/health/readiness` and `/actuator/health/liveness`).

#### Scenario: Pod health monitoring
- **WHEN** Kubelet executes startup, liveness, and readiness probes
- **THEN** requests target `/actuator/health/readiness` and `/actuator/health/liveness` on the designated service port.
