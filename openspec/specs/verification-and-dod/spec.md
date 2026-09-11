# Verification and Definition of Done Specification

## Purpose
Defines the mandatory acceptance criteria, automated GitOps deployment verification, live health probing, and autonomous self-repair protocols required for all tasks.

## Requirements

### Requirement: Full Kubernetes Definition of Done
A task or change is considered completed if and only if the updated application successfully deploys to Kubernetes (`igaming-dev`), reaches `Running` status, passes Actuator health probes, and operates cleanly without unhandled exceptions for at least 5 consecutive minutes.

#### Scenario: Readiness and Liveness probe verification
- **WHEN** the pod enters `Running 1/1` state in Kubernetes
- **THEN** HTTP requests to `/actuator/health/readiness` and `/actuator/health/liveness` return HTTP 200 `UP`.

#### Scenario: 5-Minute continuous error-free log monitoring
- **WHEN** the pod has reached `Running` status
- **THEN** logs are monitored for 5 full minutes; if remaining wait time exists, a background timer via `schedule` is mandatory, and no task may be closed until the full 5 minutes pass with zero unhandled exceptions.

---

### Requirement: Autonomous Repair Protocol
When working on a feature, if the underlying baseline service or dependency is in a broken state (`CrashLoopBackOff`, DB connection errors), feature development must halt immediately to perform an isolated repair.

#### Scenario: Broken baseline detection
- **WHEN** an AI agent inspects a service and finds it failing in the target environment
- **THEN** the feature task is paused, a repair task `[REPAIR] Восстановление модуля <module>` is created on branch `fix/<module>`, and the base module is repaired and verified green before feature work resumes.
