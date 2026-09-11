# Delta Spec: Crawler Engine & Source Verification

## ADDED REQUIREMENTS

### Requirement: Bookmaker Pipeline Reliability & Continuous Log Verification
- **SHALL**: Every active bookmaker loader and crawler must operate with zero unhandled exceptions in logs.
- **SHALL**: When a bookmaker changes its DOM or JSON structure, unmapped bets MUST be logged to `unmapped_bet` table and the loader MUST gracefully continue processing remaining valid markets.
- **SHALL**: Proxy and connection timeouts MUST trigger automatic session regeneration without crashing the main application process.

### Requirement: Horizontal Scaling for International Bookmakers
- **SHALL**: Additional bookmaker modules (`22bet`, `1xbit`, `888starz`, `betandyou`, `fonbet.kz`, `fonbet.by`, `melbet-com`) MUST be deployable into `igaming-source` namespace and leverage SSD-backed worker nodes with `node-type=standard`.
- **SHALL**: Memory footprint per bookmaker loader/crawler pod MUST remain within 512Mi–2Gi limits to maintain cluster stability.
