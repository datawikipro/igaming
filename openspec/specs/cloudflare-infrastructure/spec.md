# Cloudflare Infrastructure & Zero Trust Specification

## Purpose
Governs external ingress routing, Cloudflare Tunnels, Zero Trust Access controls, DNS topology, and regional traffic steering (RKN bypass) across the SmartBet.guru ecosystem.

## Requirements

### Requirement: Cloudflare Tunnel Routing
All external web traffic to internal Kubernetes cluster services must be routed through the primary production Cloudflare Tunnel (`smartbet.guru`, ID `9a924382-f385-4393-ae9f-94005ec0a251`) using Kubernetes internal DNS Service Names.

#### Scenario: Administrative and control UI routing
- **WHEN** an administrator accesses control planes (`llm.smartbet.guru`, `crawler.smartbet.guru`, `proxy.smartbet.guru`, `igaming-admin.smartbet.guru`)
- **THEN** the Cloudflare Tunnel routes traffic directly to the respective cluster service (e.g. `http://llm-frontend.llm.svc.cluster.local.:80`, `http://igaming-crawler-frontend.igaming-dev.svc.cluster.local.:80`).

#### Scenario: Mobile and client portal ingress
- **WHEN** mobile clients or web users request `app.smartbet.guru` or `smartbet.guru`
- **THEN** traffic is directed to the mobile app service (`http://smartbet-mobile-app.igaming-dev.svc.cluster.local.:80`) or edge ingress without exposing internal cluster NodePorts directly to the public internet.

---

### Requirement: Zero Trust Access Application Policies
Administrative control panels, operational tools, and data platform services must be protected by Cloudflare Zero Trust Access policies under the team domain (`shiny-queen-d7ad.cloudflareaccess.com`).

#### Scenario: Admin panel access control
- **WHEN** a request reaches `llm.smartbet.guru`, `crawler.smartbet.guru`, `proxy.smartbet.guru`, or `igaming-admin.smartbet.guru`
- **THEN** Cloudflare Access enforces authentication via `Admin Policy` / `Allow User Email`, passing verified identity headers (`Cf-Access-Authenticated-User-Email` and `Cf-Access-Jwt-Assertion`) to upstream services.

#### Scenario: Webhook and automated integration bypass
- **WHEN** legitimate automated third-party webhooks (e.g. Slack webhooks on `igaming-admin.smartbet.guru`) arrive without interactive browser sessions
- **THEN** specific bypass policies (e.g. `Slack Webhooks Bypass` or `X-Admin-Secret` service tokens) grant entry to designated webhook paths only.

---

### Requirement: Regional Traffic Steering & RKN Throttling Bypass
Traffic originating from within the Russian Federation must be steered to direct server IP endpoints to circumvent regional ISP/RKN throttling and SNI filtering of Cloudflare edge IPs.

#### Scenario: RU visitor redirection
- **WHEN** a visitor with country code `RU` requests `smartbet.guru` or `www.smartbet.guru`
- **THEN** Cloudflare WAF redirect rules trigger a HTTP 302 redirect to `https://ru.smartbet.guru/<path>`, which resolves directly via an unproxied DNS `A` record (`188.242.33.93`).

---

### Requirement: Transport Layer Security (TLS/SSL) Standard
All public hostnames must enforce modern TLS standards with automated certificate renewal and forced HTTPS upgrades.

#### Scenario: Insecure HTTP connection attempt
- **WHEN** a client initiates an unencrypted HTTP connection to any `*.smartbet.guru` endpoint
- **THEN** Cloudflare Edge automatically upgrades the connection via HTTPS 301/308 redirect with TLS 1.3 encryption and Full SSL mode to backend origins.
