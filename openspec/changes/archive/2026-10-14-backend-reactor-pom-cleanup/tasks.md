## 1. Root POM Reactor Expansion

- [x] 1.1 Add shared base libraries (`igaming-dto`, `auth-base`, `igaming-source-core`, `aggregator-domain`) to `<modules>` in root `pom.xml`.
- [x] 1.2 Add aggregator core pipeline modules (`aggregator-ingestion`, `aggregator-normalizer`, `aggregator-enrichment`, `aggregator-odds-sync`, `aggregator-surebet`, `aggregator-api`) to `<modules>`.
- [x] 1.3 Add application services, operator and bots (`igaming-portal`, `igaming-bot`, `service-proxy-backend`, etc.) to `<modules>`.
- [x] 1.4 Add all active bookmaker crawler modules (`igaming-source-*`) to `<modules>`.

## 2. Parameterization and Standards

- [x] 2.1 Add `<docker.registry>` property to root `pom.xml` and configure `jib-maven-plugin` under `<pluginManagement>`.
- [x] 2.2 Replace hardcoded IP `100.78.183.101:30500` in child POMs with `${docker.registry}` or standard DNS service names.
- [x] 2.3 Align Spring Boot version to 3.4.1 in legacy service POMs (`igaming-portal`, `igaming-boosty-bot`, `igaming-syndicate-bots`, `igaming-vip-bot`, `igaming-vk-donut-bot`).

## 3. Verification & Validation

- [x] 3.1 Validate OpenSpec change compliance (`openspec validate backend-reactor-pom-cleanup`).
- [x] 3.2 Verify reactor compilation from root (`mvn.cmd -DskipTests compile -pl igaming-dto,aggregator-domain,aggregator-ingestion,aggregator-surebet`).
