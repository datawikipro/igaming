## Why

The root Maven reactor `pom.xml` currently has 13 essential services and domain libraries commented out under "stubs", and over 40 active `igaming-source-*` crawlers omitted from `<modules>`. Consequently, running monorepo commands like `mvn -pl aggregator-domain ...` or building downstream services from the root fails with `Could not find the selected project in the reactor`. Furthermore, several service POMs hardcode the registry IP address (`100.78.183.101:30500`) into Jib plugin configurations, violating the strict DNS rule, and Spring Boot versions vary between 3.2.4, 3.3.1, and 3.4.1. Consolidating the root reactor and standardizing dependencies restores full-repository verification and clean build automation.

## What Changes

- **Reactor Completeness**: Uncomment all 13 core modules (`igaming-dto`, `igaming-source-core`, `auth-base`, `aggregator-domain`, `aggregator-ingestion`, `aggregator-normalizer`, `aggregator-enrichment`, `aggregator-odds-sync`, `aggregator-surebet`, `aggregator-api`, `igaming-portal`, `igaming-bot`, `service-proxy-backend`) and add all missing crawler modules (`igaming-source-*`) and support services in correct topological build order (libraries $\to$ core ingestion/aggregation $\to$ gateways/bots $\to$ crawlers).
- **Registry & Jib Parameterization**: Introduce `${docker.registry}` property and configure `jib-maven-plugin` under `<pluginManagement>` in the root POM; eliminate hardcoded IP `100.78.183.101:30500` in child POMs in favor of `${docker.registry}` or standard DNS service names.
- **Spring Boot Version Alignment**: Align legacy modules (`igaming-portal`, bot services) from Spring Boot 3.2.4 / 3.3.1 to 3.4.1.
- **Dependency & Plugin Management**: Centralize common build versions (`lombok.version`, `java.version` 21, `maven-compiler-plugin`, `jib-maven-plugin`) in the root POM.

## Capabilities

### Modified Capabilities
- `k8s-infrastructure`: Add requirement for container image registry parameterization and monorepo reactor build governance, prohibiting raw IP hardcodes in build plugins and requiring complete reactor inclusion.

## Impact

- Root `pom.xml` updated to include all valid submodules.
- Child POMs with hardcoded IP `100.78.183.101:30500` refactored to use `${docker.registry}` or DNS names.
- Builds from repository root (`mvn compile`, `mvn -pl <module> test`) become fully functional across all modules.
