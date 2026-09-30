# Plane AI Autonomous Workflow Specification

## Purpose
Governs the automated dispatching, execution, verification, and feedback loop for development tasks managed in Plane and executed by autonomous AI agents.

## Requirements

### Requirement: Webhook-Driven Job Triggering
When a task in Plane transitions to the state `AI разработка`, the `plane-ai-executor` service must automatically create and launch an isolated Kubernetes Job (`ai-task-<shortId>`) in the cluster.

#### Scenario: Issue status transition to AI разработка
- **WHEN** an issue in Plane has its state updated to `AI разработка`
- **THEN** `PlaneWebhookController` validates the webhook payload, registers the job context in `AiCallbackController`, and `AiJobLauncherService` submits a K8s Job with budget constraints (45m runtime limit, 1h hard deadline).

---

### Requirement: Spec-Driven AI Execution via OpenSpec
Autonomous AI agent runners launched by Plane must follow the OpenSpec lifecycle (`proposal` -> `specs` -> `design` -> `tasks` -> `apply` -> `verify` -> `archive`) for all proposed codebase modifications.

#### Scenario: Task branch and OpenSpec proposal initialization
- **WHEN** an AI runner container initializes for a task
- **THEN** it creates a dedicated branch `feature/plane-<TASK_ID>`, creates an OpenSpec change proposal in `openspec/changes/plane-<TASK_ID>/`, and implements code according to delta requirements.

#### Scenario: OpenSpec validation prior to PR creation
- **WHEN** the agent finishes implementation and test runs
- **THEN** it executes `openspec validate --specs` (and `openspec validate --all`), ensuring zero spec syntax or schema violations before pushing and creating the GitHub Pull Request.

---

### Requirement: Strict Definition of Done Verification
The AI runner must verify that the updated service runs in Kubernetes (`igaming-dev`) with HTTP 200 Actuator health probes and clean logs for 5 consecutive minutes before reporting task completion.

#### Scenario: Verification and callback reporting
- **WHEN** all tasks in `tasks.md` are completed and the 5-minute Kubernetes health check succeeds
- **THEN** the runner invokes the executor callback endpoint (`POST /callback/plane/{issueId}`), transitions the Plane issue to `Завершено`, attaches the GitHub PR URL, and uploads S3 execution logs.

#### Scenario: Unrecoverable blocker or error escalation
- **WHEN** an unrecoverable error occurs (such as cyclic build failure >= 3 attempts or missing secret credentials)
- **THEN** the runner immediately stops, invokes the callback endpoint with status `STUCK`, transitions the Plane issue to `В работе` / `Блокер`, and posts specific clarifying questions to the issue activity log.
