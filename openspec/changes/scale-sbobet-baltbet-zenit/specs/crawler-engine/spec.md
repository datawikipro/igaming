# Crawler Engine Delta Specification: `scale-sbobet-baltbet-zenit`

## ADDED REQUIREMENTS

### Requirement: SBOBET Multi-Market Early Market Ingestion
The SBOBET scraper MUST query the complete market line including Early Market (`?od=all`) across at least 15 active sport categories.

### Requirement: Baltbet Parallel Asynchronous Team Enrichment
The Baltbet event discovery service MUST enrich missing team names concurrently using an asynchronous thread pool to avoid sequential N+1 HTTP request bottlenecks.

### Requirement: Zenit Direct HTTP-First Line Ingestion
The Zenit client MUST prioritize fast HTTP GET requests with `imprintHash` session headers and shortened polling intervals (<= 60s) before attempting browser automation fallbacks.
