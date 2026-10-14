## ADDED Requirements

### Requirement: High-Throughput Batch Odds Persistence
The aggregator ingestion engine MUST persist normalized odds for any event in atomic batches to both PostgreSQL and Redis, avoiding sequential single-row inserts and commands.

#### Scenario: Batch persisting odds for a match
- **WHEN** an odds update with multiple outcomes is processed for an event
- **THEN** all normalized odds are persisted to PostgreSQL in a single batch operation and written to Redis via pipelined commands in one network round-trip.

#### Scenario: Deterministic deadlock prevention
- **WHEN** concurrent odds updates for multiple events or the same event arrive from different bookmakers
- **THEN** odds records are sorted deterministically before persistence, preventing database transaction deadlocks (`40P01`).

#### Scenario: Fallback on partial batch error
- **WHEN** a batch upsert encounters a database constraint or serialization failure
- **THEN** the system falls back to resilient individual upserts without dropping valid quotes or triggering unhandled exceptions.
