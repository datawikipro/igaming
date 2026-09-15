# Tasks: B2B Odds API, Affiliate CPA Engine, and Bookmaker Ops Hub

## 1. B2B Odds API Microservice (`igaming-b2b-api`)
- [x] Create Maven module `igaming-b2b-api` with Spring Boot 3.4.1 and Jib packaging
- [x] Implement in-memory API key quota management (`B2bQuotaService`)
- [x] Implement B2B Odds controller (`/api/v1/odds/surebets`, `/api/v1/odds/valuebets`, `/api/v1/quota`)
- [x] Verify compilation: `mvn.cmd test-compile -pl igaming-b2b-api`
- [x] Create K8s manifest `igaming-k8s/b2b-api.yaml`

## 2. Affiliate CPA Microservice under Cloudflare Access (`igaming-affiliate-service`)
- [x] Create Maven module `igaming-affiliate-service` with JPA, HikariCP, PostgreSQL
- [x] Implement domain entities: `AffiliatePartner`, `AffiliateOffer`, `AffiliateClick`, `AffiliateConversion`
- [x] Implement Cloudflare Zero Trust Access security interceptor (`Cf-Access-Authenticated-User-Email`)
- [x] Implement 302 redirect tracking endpoint `/r/{code}` with SHA-256 IP hashing
- [x] Implement postback webhook receiver `/api/affiliate/postback/{bookmaker}`
- [x] Implement Admin CRUD controller `/api/affiliate/admin/**`
- [x] Verify compilation: `mvn.cmd test-compile -pl igaming-affiliate-service`
- [x] Create K8s manifest `igaming-k8s/affiliate-service.yaml` with PostgreSQL StatefulSet

## 3. Bookmaker Ops Hub Backend (`igaming-aggregator` & `igaming-portal`)
- [x] Create DTOs: `BookmakerFleetStatsDto`, `BookmakerLeagueStatDto`, `FleetOverviewDto` in `igaming-dto`
- [x] Add sport and top leagues projection queries to `OddsActualRepository`
- [x] Implement `BookmakerFleetService` with 52+ catalog registry, Golden Rule #8 logic, and 30s Redis caching
- [x] Add endpoints `/api/bookmakers/fleet` and `/api/bookmakers/fleet/refresh` to `BookmakerController`
- [x] Add proxy routes in `igaming-portal` (`PortalBookmakerController`)
- [x] Verify clean builds: `mvn.cmd test-compile` across all backend modules

## 4. Bookmaker Ops Dashboard Frontend (`smartbet.guru`)
- [x] Create `BookmakerOpsDashboard.tsx` and `BookmakerOpsDashboard.css`
- [x] Implement summary KPI cards (Total, Online, DoD Compliant $\ge 500$, Active Matches, Active Odds)
- [x] Implement Golden Rule #8 visual compliance banner and progress bar
- [x] Implement search and filter pills (Status, Region, Engine)
- [x] Implement interactive table with expandable sport breakdowns and top leagues
- [x] Implement auto-refresh toggle (15s) and manual cache refresh
- [x] Create page `/[locale]/admin/bookmakers`
- [x] Add Ops Hub and Affiliate Hub navigation cards to `/[locale]/admin` dashboard

## 5. B2B Developer Portal Web Application (`smartbet-api-portal` / `api.smartbet.guru`)
- [x] Create standalone Next.js 14 project in `c:\Users\chernousov_a\WebstormProjects\igaming\smartbet-api-portal`
- [x] Configure standalone containerization and internal B2B API proxy rewrites (`next.config.mjs`, `Dockerfile`)
- [x] Implement high-tech developer dark theme (`src/app/globals.css`, `Navbar.tsx`, `Footer.tsx`)
- [x] Create Landing Page with interactive code tabs, live fleet metrics, and feature pillars (`src/app/page.tsx`)
- [x] Create OpenAPI 3.1 schema specification (`src/lib/openapi.json`)
- [x] Implement interactive API Playground with live request execution and multi-language code generator (`ApiPlayground.tsx`, `docs/page.tsx`)
- [x] Implement Pricing & Tier comparison table (`pricing/page.tsx`)
- [x] Implement Developer Console with API key management, quota usage meters, and webhooks (`dashboard/page.tsx`)
- [x] Create Kubernetes manifest `igaming-k8s/api-portal.yaml`
- [x] Register module in Web CI script `restart-web-ci.ps1`