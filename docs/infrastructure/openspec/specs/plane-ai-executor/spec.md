# Plane AI Autonomous Executor Specification

## Purpose
Governs the dispatching, execution, callback reception, and Plane issue lifecycle updates for autonomous AI development tasks executed within the Kubernetes cluster.

## Requirements

### Requirement: Plane Webhook Event Handling
The `plane-ai-executor` service must listen to Plane webhook notifications and trigger K8s AI Job executions when an issue transitions to the `AI разработка` state.

#### Scenario: State transition to AI разработка
- **WHEN** an issue in Plane transitions to state `AI разработка`
- **THEN** `PlaneWebhookController` registers the job context, fetches issue details via Plane REST API, reads `agent.md` from Gitea, and triggers `AiJobLauncherService`.

---

### Requirement: Autonomous K8s Job Execution
The service must deploy isolated ephemeral Kubernetes Jobs running the AI runner container (`agy-browser-runner`) with strict resource limits and timeouts.

#### Scenario: Job launch and configuration
- **WHEN** a task execution is triggered
- **THEN** an ephemeral K8s Job is launched in namespace `plane` with required environment variables (`PLANE_ISSUE_ID`, `PLANE_PROJECT_ID`, `PLANE_WORKSPACE_SLUG`, `CALLBACK_URL`), a maximum 45-minute runtime deadline, and service account access.

---

### Requirement: Execution Callback and Result Publishing
The service must receive completion and failure callbacks from runner jobs, publish execution summaries to Plane comments, update issue states, and upload job logs to S3.

#### Scenario: Successful task completion callback
- **WHEN** the runner finishes work and posts status `done` with PR details to `/callback/plane/{issueId}`
- **THEN** `AiCallbackController` creates a comment on the Plane issue with the PR link, updates the issue state to `Завершено` (or `Уточнение требований`), and uploads execution logs to S3/MinIO.

#### Scenario: Task stuck or clarification needed callback
- **WHEN** the runner encounters a blocker or requires human input and posts status `stuck`
- **THEN** `AiCallbackController` creates a comment with the AI questions and updates the issue state to `Уточнение требований`.
