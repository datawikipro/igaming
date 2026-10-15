# Crawler Engine Specification

## ADDED Requirements

### Requirement: Resilient HTTP Socket and Connection Timeouts
All bookmaker source modules MUST configure Apache HttpClient 5 request configurations with explicit connect, response (socket read), and connection lease timeouts to prevent crawler worker thread exhaustion during external network stalls.

#### Scenario: External API socket read timeout
- **WHEN** an external bookmaker API endpoint, proxy, or upstream challenge hangs without transmitting data
- **THEN** Apache HttpClient 5 SHALL terminate the connection after a maximum 20-second response timeout, preventing thread starvation.

#### Scenario: Internal service non-proxy timeout
- **WHEN** internal Kubernetes service communication occurs via `internalRestTemplate`
- **THEN** strict 5-second connect and 10-second response timeouts SHALL be enforced without proxy intervention.

#### Scenario: Connection pool lease timeout
- **WHEN** threads request a pooled connection from `PoolingHttpClientConnectionManager`
- **THEN** a connection lease timeout of 5 seconds SHALL be enforced, preventing indefinite pool contention.
