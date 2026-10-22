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
The AI runner must verify that the updated service runs in Kubernetes (`igaming-dev` or `igaming-source`) with HTTP 200 Actuator health probes and clean logs for 5 consecutive minutes before reporting task completion.

#### Scenario: Verification and callback reporting
- **WHEN** all tasks in `tasks.md` are completed, code modifications are verified, and the 5-minute Kubernetes health check succeeds
- **THEN** the runner invokes the executor callback endpoint (`POST /callback/plane/{issueId}`), transitions the Plane issue to `Завершено`, attaches the GitHub PR URL, and uploads S3 execution logs.

#### Scenario: Unrecoverable blocker or error escalation
- **WHEN** an unrecoverable error occurs (such as cyclic build failure >= 3 attempts, zero code diff, or missing secret credentials)
- **THEN** the runner immediately stops, invokes the callback endpoint with status `STUCK`, transitions the Plane issue to `В работе` / `Блокер`, and posts specific clarifying questions to the issue activity log.

---

### Requirement: Anti-Phantom Gate & Non-Empty Code Modification Guarantee
Autonomous AI workers must never complete, merge, or deploy a task if `git diff origin/{base_branch} HEAD` contains zero modified code, manifest, or configuration files outside of `openspec/`.

#### Scenario: Agent finishes without code modifications
- **WHEN** an AI runner finishes its execution loop and `git diff` shows only changes in `openspec/` or zero modified code files
- **THEN** the Anti-Phantom Gate triggers, rejects automatic merge, halts execution, sets status to `STUCK`, and logs an escalation notice to Plane.

#### Scenario: Incomplete OpenSpec checklist
- **WHEN** the runner completes all iterations but `tasks.md` still contains unchecked items (`- [ ]`)
- **THEN** the task is marked `STUCK` instead of `COMPLETED`, preventing premature merging of partial work.

---

### Requirement: Session Continuity & Context-Enriched Grounding
AI worker runners must preserve conversational memory across iterative steps via CLI session continuation (`--continue`) and receive fully enriched task context (cleaned markdown description, resolved target module directory, and relevant 14 Golden Rules).

#### Scenario: Multi-turn iterative development
- **WHEN** the AI worker executes sequential tasks from `tasks.md`
- **THEN** subsequent steps invoke `agy` with session continuation (`-c` / `--continue`), preventing context amnesia and token waste.

#### Scenario: HTML stripping and target module detection
- **WHEN** Plane issues contain HTML formatted descriptions
- **THEN** the runner converts HTML to clean Markdown and injects exact working directories, key file paths, and local build/test commands into the agent prompt.

---

### Requirement: Stack-Specific E2E Verification & Soak Testing
Verification pipelines must dynamically branch according to the target technology stack (`CRAWLER`, `SMM`, `PORTAL`, `AGGREGATOR`, `FRONTEND_CLIENT`, `FRONTEND_ADMIN`).

#### Scenario: Java Bookmaker Crawler
- **WHEN** a task targets a bookmaker crawler (`igaming-source-<bookmaker>`)
- **THEN** the runner builds a Jib image, launches an isolated test pod in `igaming-source`, verifies 5-minute clean logs, rolls out to production, and performs a 5-minute production soak test.

#### Scenario: Python SMM Automation or Frontend Web
- **WHEN** a task targets SMM scripts or Next.js / React frontends
- **THEN** the runner runs stack-specific compilation/tests (e.g. `py_compile`, `pytest`, `npm test`, `npm run build`), verifies build artifacts, and executes appropriate soak testing without forcing crawler Jib pipelines.
