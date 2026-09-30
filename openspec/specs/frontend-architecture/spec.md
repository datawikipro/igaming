# Frontend Architecture Specification

## Purpose
Specifies the architecture, localization, data hydration, routing, and UI styling foundation for the Next.js 14 SmartBet.guru portal, as well as strict isolation between customer-facing interfaces and internal infrastructure admin dashboards.

## Requirements

### Requirement: Next.js App Router and Localization
The frontend application must utilize Next.js 14 App Router with `[locale]` dynamic routing for multi-language support (via `next-intl`), SWR for real-time data synchronization, and custom CSS token variables.

#### Scenario: Localized page routing
- **WHEN** a user visits `/[locale]/arbs` or `/[locale]/valuebets`
- **THEN** localized messages from `messages/{locale}.json` are rendered with server-side layout resolution.

#### Scenario: SEO and OpenGraph metadata
- **WHEN** rendering dynamic match, team, or league routes
- **THEN** `generateMetadata()` provides unique page titles, descriptions, and schema.org JSON-LD structured metadata.

---

### Requirement: Separation of Customer-Facing Portal and Internal Operator Admin
The iGaming frontend ecosystem must maintain strict architectural separation between the customer-facing public portal (`smartbet.guru`) and the internal operator infrastructure dashboard (`igaming-admin-frontend`).

#### Scenario: Customer-facing portal boundary (smartbet.guru)
- **WHEN** users browse `smartbet.guru`
- **THEN** the application provides public functionality including odds comparison, surebet/valuebet scanners, 80% guaranteed freebet cash calculators, promo directory, blog content, and affiliate link redirects, strictly excluding internal operational tools, remote desktop streams, and internal patron ticketing.

#### Scenario: Dedicated operator admin portal boundary (igaming-admin-frontend)
- **WHEN** administrators manage cluster operations, bots, and patron relationships
- **THEN** access is routed to `igaming-admin-frontend` (K8s namespace `accounts`), providing account management (`/accounts`), cluster node telemetry (`/nodes`), autonomous AI developer task inspection (`/ai-tasks`), social channel broadcasting (`/channels`), interactive noVNC stealth browser consoles (`/browsers`), and the multi-tier Patron CRM (`/feedback`).
