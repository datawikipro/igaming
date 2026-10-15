# Change Proposal: Crawler HTTP Socket Timeouts Protection

## Why
Currently in `igaming-source-core`:
1. **Missing Socket Read Timeout (`responseTimeout`)**: In `RestTemplateConfig`, the external `RestTemplate` (used by all 80 bookmaker crawlers to query bookmaker API lines and event cards) only sets `setConnectTimeout(10000)` on the `HttpComponentsClientHttpRequestFactory`. It does **not** configure Apache HttpClient 5's `RequestConfig` `responseTimeout` (socket read timeout).
2. **Half-Open TCP Connection Hangs**: When an external bookmaker endpoint, Cloudflare challenge, or upstream proxy stalls or freezes during packet transmission, the connection socket hangs indefinitely (up to the OS-level TCP timeout of 15+ minutes). This blocks crawler threads, exhausts connection pools, and halts match updates.
3. **Missing Connection Request Timeout**: If the connection pool is saturated, threads could potentially block waiting for a lease without a bounded pool acquisition timeout.

## What Changes
1. **Configure RequestConfig on External RestTemplate**:
   - Set `connectTimeout` to 10 seconds.
   - Set `responseTimeout` (socket read timeout) to 20 seconds.
   - Set `connectionRequestTimeout` (pool lease timeout) to 5 seconds.
   - Attach `defaultRequestConfig` to `HttpClients.custom()`.
2. **Configure RequestConfig on Internal RestTemplate**:
   - Set `connectTimeout` to 5 seconds.
   - Set `responseTimeout` to 10 seconds.
   - Set `connectionRequestTimeout` to 5 seconds.
   - Ensure internal k8s service communication (`service-proxy-backend`, `aggregator`) remains fast, responsive, and immune to hanging calls.

## Impact
- **Specs Impact**: Modifies `crawler-engine` specification requirement.
- **Crawlers Impact**: Eliminates hung crawler threads across all 80 bookmaker crawlers.
- **Backward Compatibility**: Fully backward-compatible; prevents threads from being deadlocked by non-responsive endpoints.
