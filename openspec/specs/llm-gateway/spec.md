# LLM Gateway Specification

## Purpose
Governs the centralized Large Language Model (LLM) routing architecture, API key lifecycle management, provider dynamic leasing, queue scheduling, and model fallback mechanisms across all iGaming intelligent agents and services.

## Requirements

### Requirement: Centralized Provider and Model Registry
All LLM providers (Gemini, DeepSeek, OpenAI, Anthropic, Agent Studio) and their model identifiers must be centrally registered and managed via `igaming-llm-admin`.

#### Scenario: Registering provider API keys
- **WHEN** an administrator registers an API key or OAuth credentials for a provider (e.g. Gemini) via the admin UI (`llm.smartbet.guru`) or REST API (`/api/v1/admin/providers/{providerId}/keys`)
- **THEN** the key is stored with active status, associated with the provider, and immediate email/account resolution is triggered if OAuth credentials are used.

#### Scenario: Model availability query
- **WHEN** an upstream microservice (e.g. `igaming-aggregator` or `igaming-bot`) queries supported models
- **THEN** `igaming-llm-admin` returns active models grouped by provider (`/api/v1/admin/providers/supported`) with sub-millisecond cached latency.

---

### Requirement: Decoupled Queue Scheduling and Dynamic Leases
The gateway must decouple LLM inference requests from provider-specific rate limits using asynchronous message queues (Redis / Kafka) and dynamic lease allocations.

#### Scenario: LLM inference dispatch
- **WHEN** an inference task is submitted to `igaming-llm-gateway`
- **THEN** the task is queued according to routing rules, dispatched to the corresponding worker (`llm-worker-gemini`, `llm-worker-deepseek`), and tracked with dynamic lease timers.

#### Scenario: Rate limit suspension and model fallback
- **WHEN** an LLM provider returns a rate limit (HTTP 429) or quota exhaustion error
- **THEN** the specific API key is placed into model suspension with `suspendedUntil` timestamp, and subsequent requests automatically route to fallback active keys or alternative configured models without dropping tasks.

---

### Requirement: Administrative Control Plane Ingress
The administrative interface for LLM operations (`llm-frontend`) must be accessible via Cloudflare Zero Trust and internal cluster DNS.

#### Scenario: Secure remote management
- **WHEN** operations personnel access `https://llm.smartbet.guru`
- **THEN** access is verified through Cloudflare Zero Trust `Admin Policy`, and the Next.js control center displays real-time leases, providers, key statuses, queue metrics, and routing rules.
