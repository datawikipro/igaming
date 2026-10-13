# VPN and Proxy Infrastructure Specification

## Purpose
Manages split-tunnel network routing, VPN connectivity, Tailscale mesh interconnects, European egress gateways (DE-Proxy), domestic Russian clean IP egress (RuProxy), personal device routing (Nintendo Switch & mobile), and dedicated static proxy pools across all cross-project environments (iGaming, S3 Storage, CareerLab).

---

## Requirements

### Requirement: Centralized Multi-Network Split-Tunnel Routing
The global infrastructure MUST provide isolated, verified egress pathways for workloads and personal devices without reliance on deprecated or decommissioned servers.

#### Scenario: Domestic and TsUPIS bookmaker routing (RuProxy)
- **WHEN** traffic targets Russian TsUPIS/ERAI services, VK, or requires domestic resident IP
- **THEN** traffic routes through `ru-proxy` (`100.83.113.50`) via direct Saint Petersburg domestic ISP (`188.242.33.93`) without VPN encapsulation.

#### Scenario: European and offshore international routing (DE-Proxy-1)
- **WHEN** traffic targets international bookmakers, OpenAI / Gemini LLM APIs, Telegram Bot API, or requires unblocked internet access
- **THEN** traffic routes through `de-proxy-1` in Frankfurt am Main, Germany (AEZA `85.192.24.152`, Tailscale `100.111.55.54`, Tinyproxy on port `3128`), bypassing RKN/TSPU restrictions.

#### Scenario: Kubernetes ClusterIP dedicated proxy services
- **WHEN** containerized workloads in K8s (crawlers, SMM bots, Plane AI workers) require outbound routing to specific geographic regions
- **THEN** they connect via permanent Kubernetes ClusterIP DNS endpoints:
  - `http://de-proxy.proxy.svc.cluster.local:3128` -> Germany (AEZA Frankfurt `85.192.24.152`)
  - `http://us-proxy.proxy.svc.cluster.local:3128` -> United States (`100.111.55.54:3130`)
  - `http://fi-proxy.proxy.svc.cluster.local:3128` -> Finland (`100.111.55.54:3131`)
  - `http://hk-proxy.proxy.svc.cluster.local:3128` -> Hong Kong (`100.109.193.55:3128`)

---

### Requirement: Personal Devices and Nintendo Switch Gateway (cluster-switch-proxy)
The cluster MUST provide a flexible, single-entrypoint proxy service (`cluster-switch-proxy`) on port `3128` for Nintendo Switch and personal mobile devices with switchable upstream modes.

#### Scenario: Nintendo Switch RKN bypass mode (Default)
- **WHEN** Nintendo Switch connects to the Wi-Fi proxy on port `3128` in default mode
- **THEN** upstream routes via `de-proxy-1` (`100.111.55.54:3128`), ensuring unrestricted access to Nintendo eShop, online multiplayer, and media services.

#### Scenario: Domestic Russian IP switch mode
- **WHEN** user requires domestic Russian identity for personal banking or local services
- **THEN** upstream is switched to `ru-proxy` (`100.83.113.50:3128`), routing via the clean SPb resident IP.

#### Scenario: Orion bridge switch mode
- **WHEN** routing traffic into the isolated Orion ecosystem
- **THEN** upstream is switched to `orion-work-gateway` (`10.244.3.196:3128`).

---

### Requirement: Dedicated Static Multi-Account PureVPN Pool
The proxy infrastructure MUST manage dedicated, static egress proxy nodes backed by PureVPN multi-accounts, enforcing a strict 1:1 binding between client workloads (social media bots, sensitive bookmaker scrapers) and fixed Kubernetes Service DNS names, with blind dynamic IP rotation strictly prohibited.

#### Scenario: 8/10 Device quota per PureVPN account
- **WHEN** allocating dedicated proxy nodes for a PureVPN subscription account
- **THEN** the system limits active connections to exactly 8 out of 10 allowed devices (80% quota), leaving 2 slots as a safety buffer against concurrency overflow.

#### Scenario: Geographic 8-node allocation preset
- **WHEN** initializing the baseline node set for an account
- **THEN** 8 dedicated pods are provisioned across strategic geographic regions:
  - 1 in USA (`purevpn-us-ca` in Los Angeles, CA -> Reddit and US Social Media)
  - 3 in Europe (`purevpn-nl` in Amsterdam -> Meta Threads/Instagram #1; `purevpn-de` in Frankfurt -> Patron CRM Desk; `purevpn-uk` in London -> Sports Betting & UK SMM)
  - 3 in Asia (`purevpn-sg` in Singapore -> Asian Hub/SBOBET; `purevpn-jp` in Tokyo -> Far East Gateway; `purevpn-hk` in Hong Kong -> Financial Traffic Hub)
  - 1 in South America (`purevpn-br` in São Paulo -> Latin America / Betano / IG).

#### Scenario: 1:1 Static Kubernetes DNS mapping
- **WHEN** client applications or stealth browsers initiate outbound requests
- **THEN** they connect directly to designated Kubernetes Service DNS endpoints (`http://purevpn-<loc>.proxy:3128`) without ephemeral IP rotation.

---

### Requirement: Physical SIM Gateway for 2FA (Xiaomi 12T Pro)
The infrastructure MUST maintain connectivity to the physical SMS and WhatsApp forwarding gateway for Meta and Instagram 2FA checkpoints.

#### Scenario: 2FA OTP code delivery
- **WHEN** Instagram or Meta triggers a checkpoint requiring WhatsApp or SMS confirmation
- **THEN** the code is received on Xiaomi 12T Pro (`100.114.62.28`, number `+79137671550`) and automatically forwarded via webhook to `auth-inbox-gateway` for consumption by automated agents.

---

### Requirement: Web Management Dashboard and Node Lease Control
The proxy infrastructure must provide an operator dashboard and REST API for real-time monitoring of accounts, node health, active public IPs, and manual geographic node leasing.

#### Scenario: Operator dashboard visualization
- **WHEN** the operator accesses `http://100.78.183.101:30180` (`service-proxy-frontend` on NodePort 30180)
- **THEN** the interface displays account quota consumption (e.g. 8/10), real-time latency and health metrics for each pod, external IP and country flag, and a full catalog of 165+ PureVPN locations with one-click lease reassignment.

---

### Requirement: Bare-Metal Host Night Reboot Ban (22:00 — 10:00)
Because the physical server `xeon-srv` (128 GB RAM) is located in a residential environment, all automated agents and administrators MUST adhere to the physical quiet hours policy.

#### Scenario: Night reboot restriction
- **WHEN** a system update, kernel patch, or recovery operation requires a physical reboot of `xeon-srv` between 22:00 and 10:00
- **THEN** the reboot operation MUST be postponed until after 10:00 to prevent fan noise disturbance.
