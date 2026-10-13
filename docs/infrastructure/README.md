# 🏢 Infrastructure Admin OpenSpec Central Repository

> **Centralized Spec-Driven Architecture & Capability Repository for Infrastructure Admin Platform (Accounts, Plane AI Executor, Cloud/LLM Pollers, VPN/Proxy).**

This standalone private repository acts as the **OpenSpec Central Store** across all infrastructure services and autonomous AI executors.

---

## 📚 Ecosystem Capabilities (Living Specifications)

| Capability Spec | Scope | Description |
|---|---|---|
| [`plane-ai-executor`](openspec/specs/plane-ai-executor/spec.md) | Automation | Plane webhook event handling, autonomous K8s Job execution (`agy-browser-runner`), callback reception, and S3 execution logs |
| [`accounts-platform`](openspec/specs/accounts-platform/spec.md) | Core | Centralized multi-tenant account management, credential storage, JPA entities, Undertow REST API |
| [`cloud-and-llm-pollers`](openspec/specs/cloud-and-llm-pollers/spec.md) | Monitoring | Cloud node polling across GCP/K8s clusters, quota usage tracking, and rate limit alerting |
| [`vpn-proxy-infrastructure`](openspec/specs/vpn-proxy-infrastructure/spec.md) | Networking | Split-tunnel network routing via Sing-Box (`ru-proxy-1` / `100.89.7.59`), Tailscale Exit Node mesh |

---

## 🛠️ Usage & Integration

### 1. Register as Local OpenSpec Store
```bash
openspec store register C:\Users\chernousov_a\IdeaProjects\infrastructure-openspec --id infra
```

### 2. Propose a Change against this Store
```bash
openspec new change <change-id> --store infra
```

### 3. Validate All Specifications
```bash
openspec validate --specs
```
