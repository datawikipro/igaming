## ADDED Requirements

### Requirement: Cluster-Wide Minimum Line Volume (>= 500 Active Matches)
All 52 bookmaker crawlers and loaders SHALL maintain a minimum active line volume of 500 matches in their dedicated `match_cache` table (`SELECT count(*) FROM match_cache >= 500`). Running pods with fewer than 500 matches in a production or dev cluster SHALL be treated as incomplete and degraded.

#### Scenario: Line volume verification
- **WHEN** the periodic cluster line audit executes across all 52 bookmakers
- **THEN** every bookmaker database reports `SELECT count(*) FROM match_cache >= 500` and `status` as `HEALTHY (>=500)`.

#### Scenario: Automated crawler recovery on line depletion
- **WHEN** active match count in `match_cache` drops below 500 for a running bookmaker
- **THEN** the discovery scheduler triggers immediate prematch sport tree expansion and category pagination to replenish the line.

### Requirement: Resilient Centralized Proxy Routing for Crawler Feeds
Crawlers SHALL strictly route traffic according to target bookmaker jurisdictional filtering via the centralized cluster router `ru-proxy`:
1. Russian licensed TSUPIS bookmakers (Winline, Fonbet, Pari, Betcity, Baltbet, Olimpbet, Tennisi, Leon) SHALL route directly through domestic home IP (`188.242.33.93`) to prevent ISP / RKN blocking.
2. Offshore and European bookmakers (Pinnacle, Sbobet, Bet365, Bwin, Betsson, Betsafe, Nordicbet, MrGreen, 888starz, 1x-clones) SHALL route via EU Netherlands Shadowsocks proxy (`34.158.66.184:443`).

#### Scenario: Routing Russian bookmaker traffic
- **WHEN** a crawler for a Russian bookmaker issues HTTP or browser requests
- **THEN** traffic egresses directly via domestic ISP IP without foreign VPN headers.

#### Scenario: Routing offshore bookmaker traffic
- **WHEN** a crawler for an offshore or European bookmaker issues requests
- **THEN** traffic egresses through EU NL proxy with TLS Client Hello masking to avoid RKN blocks and bookmaker geo-fencing.
