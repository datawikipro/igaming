## Context

See proposal.md - Why.
The repository contains over 80 microservices and crawlers. Currently, the root `pom.xml` only defines 20 modules, commenting out 13 core libraries and services as "stubs" and leaving over 50 crawler modules unlisted. Furthermore, several crawlers hardcode the private IP `100.78.183.101:30500` directly into their `jib-maven-plugin` configuration.

## Goals / Non-Goals

**Goals:**
- Include all valid backend modules with a `pom.xml` into the root reactor in correct topological build order (shared libraries $\to$ aggregator engines $\to$ gateways & bots $\to$ crawlers).
- Centralize `docker.registry` property in root POM and replace hardcoded IP addresses with `${docker.registry}` or Kubernetes DNS service names.
- Align Spring Boot versions across services to 3.4.1.
- Enable `mvn -pl <any-module> ...` commands to resolve directly from the repository root.

**Non-Goals:**
- Changing business logic or runtime code of any crawler or aggregator service.
- Forcing child microservices to switch `<parent>` from `spring-boot-starter-parent` to `igaming-parent` (keeping Spring Boot starter parent preserves starter dependency trees).

## Decisions

- **Reactor Aggregator without Forced Parent Inheritance**: In Maven, an aggregator POM orchestrates the reactor build graph across `<modules>` independently of child `<parent>` references. Preserving `spring-boot-starter-parent` (version 3.4.1) as child parent retains native Spring Boot dependency management while the root POM drives multi-module compilation.
- **Topological Ordering in `<modules>`**:
  1. Base libraries: `igaming-dto`, `auth-base`, `igaming-source-core`, `aggregator-domain`.
  2. Aggregation & Ingestion pipeline: `aggregator-ingestion`, `aggregator-normalizer`, `aggregator-enrichment`, `aggregator-odds-sync`, `aggregator-surebet`, `aggregator-api`.
  3. Application, Operator & Bot services: `igaming-portal`, `igaming-bot`, `service-proxy-backend`, etc.
  4. Bookmaker Source Crawlers: all 65+ `igaming-source-*` modules.
- **Registry Property Parameterization**: Define `<docker.registry>registry.infra-kube.terminal.lft:30500</docker.registry>` (with fallback to environment override `-Ddocker.registry=...`) in root POM properties.

## Risks / Trade-offs

- **[Risk] Unmaintained or deprecated crawler fails compilation in reactor**: A module that had broken dependencies might fail during full `mvn compile`.
  - **Mitigation**: Validate individual module compilations and fix any missing dependencies or version mismatches during reactor assembly.
