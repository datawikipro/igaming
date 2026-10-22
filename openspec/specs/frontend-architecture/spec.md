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

---

### Requirement: Jurisdictional Regulatory Licensing and National Badges
The frontend application must provide dynamic jurisdictional licensing tags and country-specific regulatory filtering, ensuring users in strictly regulated markets (such as France / ANJ, Spain / DGOJ, Germany / GGL, United Kingdom / UKGC, Russia / ЕРАИ, Brazil / SPA, Colombia / Coljuegos) can instantly identify and isolate bookmakers holding legal state licenses in their jurisdiction.

#### Scenario: Jurisdictional licensed-only filtering
- **WHEN** a user selects an active country (e.g. France `FR`, Spain `ES`, Germany `DE`, or UK `GB`) and enables the "Licensed Only" filter
- **THEN** the filter button dynamically adapts its label (e.g. `🇫🇷 ANJ Only`, `🇪🇸 DGOJ Only`, `🇩🇪 GGL Only`) and filters the bookmaker list and arbitrage cards to only display bookmakers holding valid regulatory licenses in that territory.

#### Scenario: Regulatory badge display on bookmaker lists and arbitrage cards
- **WHEN** bookmakers are rendered in search results, settings modals, arbitrage outcome cards, or direct match cards modals
- **THEN** each bookmaker displays its primary jurisdictional authority badge (e.g. `🇫🇷 ANJ`, `🇪🇸 DGOJ`, `🇩🇪 GGL`, `🇬🇧 UKGC`, `🇷🇺 ЕРАИ`) along with special tags for betting exchanges (`📈 P2P`) and crypto support (`🪙 Crypto`).

---

### Requirement: Non-Duplicate Multilingual SEO Metadata and Crawler Directives
The application must generate 100% unique, fully localized `<title>` and `<meta name="description">` tags across all supported languages (`ru`, `en`, `de`, `fr`, `es`, `pt`) to eliminate cross-language duplication penalties in Google Search Console and Yandex Webmaster, while explicitly instructing search crawlers to ignore administrative and embedded utility routes.

#### Scenario: Bookmaker review localization
- **WHEN** search engine bots crawl `/bookmakers/[slug]` under different locales (e.g. `/de/`, `/fr/`, `/es/`, `/pt/`, `/ru/`, `/en/`)
- **THEN** each route outputs distinct native language titles and descriptions tailored to local search queries (e.g., "Leon Erfahrungen" for DE, "Avis Leon" for FR, "Opinión Leon" for ES, "Avaliação Leon" for PT).

#### Scenario: Administrative and embed route exclusion
- **WHEN** search engines visit `/admin` routes or `/referral/widget`
- **THEN** response headers/tags include `robots: { index: false, follow: false, nocache: true }` preventing indexing of internal consoles or iframe widgets.

---

### Requirement: Betting Hypothesis Laboratory Interface
The customer-facing portal (`smartbet.guru`) SHALL provide an interactive «Лаборатория гипотез» (`/[locale]/hypotheses`) allowing users to configure betting rules, run forward-testing paper simulations, and inspect quantitative analytics.

#### Scenario: Visual hypothesis builder
- **WHEN** a user opens the hypothesis creation wizard
- **THEN** the interface provides multi-select filters for sports, leagues, bookmakers, target odds sliders, EV percentage thresholds, and staking model selectors (Flat, % Bankroll, Kelly Criterion).

#### Scenario: Strategy performance visualization
- **WHEN** a user inspects a hypothesis detail view
- **THEN** the view renders an interactive Equity curve, monthly ROI breakdown, win/loss distribution, and an audit table of all virtual bets placed under the strategy.

---

### Requirement: Lineup Steam Alert Badges and Tactical Trading Plan UI
The customer-facing portal (`smartbet.guru`) MUST display actionable visual steam alert indicators on matches experiencing lineup-driven odds lag and render interactive Tactical Trading Plan modal cards.

#### Scenario: Visual steam alert badge on match and odds lists
- **WHEN** a match has an active `SteamReversalSignal`
- **THEN** match cards, surebet outcome cards, and odds comparison tables display a pulsing `⚡ Сигнал разворота` badge showing shock score and edge percentage.

#### Scenario: Interactive Trading Plan exploration
- **WHEN** a user clicks on a steam reversal badge or card
- **THEN** the interface opens `TradingPlanModal` featuring the lineup catalyst breakdown, lagging bookmakers with stale high odds, resilient market recommendations, and a phased timeline for in-play hedging.

---

### Requirement: Regional Subdomain Routing and Market Localization
The application must support dedicated regional subdomains (e.g., `ca.smartbet.guru`, `canada.smartbet.guru`, `kz.smartbet.guru`, `ru.smartbet.guru`, `br.smartbet.guru`) that decouple geographical market/jurisdiction from UI language.

#### Scenario: Subdomain region detection and header propagation
- **WHEN** a client makes an HTTP request to a regional host (e.g., `ca.smartbet.guru` or `canada.smartbet.guru`)
- **THEN** Next.js `middleware.ts` extracts the market code (`CA`), propagates `x-smartbet-region: CA` request headers to Server Components, writes the `smartbet_region=CA` cookie, and allows public search engine indexing (omitting `noindex` applied to internal proxy domains).

#### Scenario: Regional root language resolution
- **WHEN** a visitor accesses the root path (`/`) on a regional subdomain (e.g., `ca.smartbet.guru/`)
- **THEN** middleware evaluates the client's `Accept-Language` header against supported regional locales (e.g. `fr` vs `en` for Canada) and redirects to the highest-priority supported locale (`/fr` for French preference, `/en` default), ensuring French-speaking Canadians see Canada content in French rather than France-specific content.

#### Scenario: Local Champions and currency hydration
- **WHEN** browsing an active regional market
- **THEN** `PreferencesContext` and server-side hydration prioritize domestic licensed bookmakers under "Local Champions" (e.g. Sports Interaction, Bet99, Betano CA under AGCO/iGO license), sets regional default currency (e.g. `CAD`), and filters surebet line queries by target region.

---

### Requirement: Mobile-First Visual Hierarchy and Organic Community Integration
The mobile interface (< 768px viewport) must prioritize core arbitrage data accessibility on the first screen while seamlessly integrating community feedback channels without intrusive promotional banners.

#### Scenario: First fold arbitrage feed visibility
- **WHEN** a user visits the homepage on a mobile device (< 768px)
- **THEN** header height is restricted to 56px, Hero section typography is compact, the top opportunities carousel renders mini-cards (175px width), and the first arbitrage opportunity card is visible above or within the initial screen fold without requiring excessive vertical scrolling.

#### Scenario: Non-intrusive community engagement (Telegram and Boosty)
- **WHEN** navigating tabs and scanner options on mobile
- **THEN** Telegram bot/channel alerts and Boosty feature order/subscription links are rendered as compact, organic segmented pill buttons (`[✈ Telegram]` and `[✨ Boosty • Фичи]`) alongside segmented controls, and accessible via dedicated cards within the mobile navigation drawer.

#### Scenario: Responsive arb card ergonomics
- **WHEN** rendering multi-outcome surebets (2-way and 3-way) on viewports < 576px
- **THEN** redundant full match titles in league badges are suppressed in favor of compact sport/league indicators, secondary action buttons (`[↗]`, `[↻]`) render as compact 24px icon buttons, and safety badges condense to single-score indicator (`Shield X%`), preventing horizontal card overflow and bookmaker name clipping.

---

### Requirement: Betting Calculators Suite (Arbitrage, Kelly, Matched Betting, Accumulators & Systems)
The portal must provide a comprehensive, fully-localized betting calculator suite supporting up to 16 events with responsive controls, paginated combinations, and educational transparency.

#### Scenario: Accumulator & System Bet Calculator (up to 16 events)
- **WHEN** a user accesses `/[locale]/calculator?tab=express` or `/[locale]/tools/express-system-calculator`
- **THEN** the calculator supports up to 16 events with compact/detailed views, computes standard $k$ of $n$ combinations and named full cover systems (Trixie, Patent, Yankee, Lucky 15, Canadian, Heinz, Super Heinz, Goliath), provides paginated combination breakdown for memory safety, simulates "what-if" error scenarios, and transparently displays the compounded bookmaker vig tax alongside the fair payout benchmark.
