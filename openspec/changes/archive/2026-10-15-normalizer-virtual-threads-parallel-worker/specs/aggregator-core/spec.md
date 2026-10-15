# Aggregator Core Specification

## ADDED Requirements

### Requirement: Parallel Odds Normalization Processing
The entity normalization pipeline MUST execute normalization requests and odds type resolution concurrently on Java 21 virtual threads, preventing scheduler stalling and platform thread exhaustion during remote LLM Gateway calls.

#### Scenario: Parallel odds type batch processing
- **WHEN** a batch of pending `ODDS_TYPE` normalization requests is polled
- **THEN** requests SHALL be processed concurrently in parallel tasks, allowing fast-track in-memory matches to complete without waiting on remote LLM evaluations.

#### Scenario: Virtual thread execution for LLM normalization
- **WHEN** normalization requests require remote LLM Gateway evaluation
- **THEN** execution SHALL be handled by a virtual threads executor without fixed platform thread pool size constraints.
