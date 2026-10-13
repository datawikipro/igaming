# Accounts Platform Specification

## Purpose
Provides centralized multi-tenant account management, credential storage, and task tracking models across infrastructure modules.

## Requirements

### Requirement: Central Domain Entities
Shared entities in `accounts-domain` must define standard schemas for accounts, credentials, and tasks with JPA and Jackson annotations.

#### Scenario: AI task tracking schema
- **WHEN** an AI task lifecycle event occurs
- **THEN** `AiTask` entity records task ID, title, status (`PENDING`, `IN_PROGRESS`, `DONE`, `FAILED`, `STUCK`), branch name, PR link, timestamps, and associated comments (`AiTaskComment`).

---

### Requirement: REST API and Persistence
`accounts-service` must expose secure REST endpoints with Undertow and PostgreSQL persistence.

#### Scenario: Fetch and update AI task state
- **WHEN** a client queries `/api/ai-tasks` or updates status via `/api/ai-tasks/{id}`
- **THEN** `accounts-service` persists state transitions transactionally and returns JSON DTOs.
