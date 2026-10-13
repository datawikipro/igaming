# Cloud and LLM Pollers Specification

## Purpose
Monitors cloud resources, Kubernetes cluster node health, and AI provider quota balances across infrastructure accounts.

## Requirements

### Requirement: Cloud and Node Polling
`cloud-poller` must continuously check worker node and master node availability across GCP and external clouds.

#### Scenario: Node health and label inspection
- **WHEN** `cloud-poller` executes its scheduled polling loop
- **THEN** it checks Kubernetes Node objects via Fabric8 client, inspects `Ready` conditions, and ignores control-plane taints appropriately.

---

### Requirement: LLM Quota and Balance Tracking
`llm-poller` must monitor API quotas, token consumption, and rate limits for active AI model providers.

#### Scenario: Quota exhaustion detection
- **WHEN** an AI provider returns rate limits or low bucket balance
- **THEN** `llm-poller` emits alerts and triggers failover / rotation to alternative available keys.
