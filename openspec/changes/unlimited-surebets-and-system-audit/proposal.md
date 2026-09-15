# Unlimited Surebets, RAM Optimization & Full System Audit

## Why

The SmartBet.guru ecosystem operates 52 bookmaker integrations across a hybrid infrastructure combining an on-premise physical server (`xeon-srv`) and GCP worker nodes. Over recent development cycles, several critical challenges and paradigm shifts converged:

1. **Elimination of Artificial Yield Ceilings**: The project transitioned away from legacy Free Tier restrictions (formerly capping free users at <= 5.0% profit, EV <= 0.05, and middles <= 0.60). Simultaneously, the arbitrage detection pipeline had an artificial yield cap of 10.0% (and historically misunderstood as a 50.0% ceiling) that trapped genuine high-yield surebets into `PENDING_VERIFICATION` status, hiding them from end users. The core architectural principle has been established: **there is no yield ceiling**. Any mathematically valid return must immediately display with status `ACTIVE`, while suspicious structural errors are routed asynchronously to `OddsAnomalyService`.
2. **Abundant RAM vs. Mechanical Disk Bottleneck**: The physical server `xeon-srv` possesses 188 GiB of physical RAM with over 120 GiB idle/available, yet suffered from acute I/O wait (> 55%) and extreme disk queue depths (96 to 178) on its primary 111.3 GB SAS mechanical drive (`sda1`/`sdb1`). Root-cause telemetry revealed that 52 Playwright Chromium processes were generating gigabytes of cache tiles, shader logs, and profile data in container `/tmp` on the root filesystem, while 52 PostgreSQL instances were causing synchronous ext4 journal thrashing.
3. **Night-Time Maintenance Window**: In compliance with the residential noise restriction (no server reboots or noisy hardware operations between 22:00 and 10:00 MSK), the physical server is offline during night hours. This window is dedicated to conducting an exhaustive architectural review of what has been implemented, documenting all changes in OpenSpec, and outlining the clear execution backlog for the daytime shift.

## What Changes

- **Complete Elimination of Free Tier Restrictions**: Remove all yield, EV, and middle limits in `igaming-portal` and client-facing APIs. All users receive unrestricted real-time access.
- **Uncapped Arbitrage Detection (No-Cap Policy)**:
  - Remove all artificial ceilings (`maxPrematchProfitPercent`, `maxLiveProfitPercent`) from `aggregator-surebet` and `aggregator-api`.
  - Ensure all non-erroneous detected arbitrage opportunities are immediately stored and published with status `ACTIVE`.
  - Relegate high-yield sanity checks and asynchronous refreshers to non-blocking background monitoring (`OddsAnomalyService`), ensuring zero delay or suppression in user alerts.
- **RAM-First Infrastructure & Zero-Disk-Write Architecture**:
  - PostgreSQL Aggregator Core caching: Sized `shared_buffers = 4GB`, `work_mem = 64MB`, `effective_cache_size = 6GB`, achieving **98.977% cache hit ratio**.
  - 52 Source PostgreSQL DBs: Standardized `checkpoint_timeout = 15min`, `checkpoint_completion_target = 0.9`, `wal_writer_delay = 50ms`, `max_wal_size = 2GB`, and `synchronous_commit = off`. Server load average dropped from **137.17 to 74.62**.
  - Host OS & Kernel RAM Offloading: Persisted `tmpfs` 24GB on `/tmp`, smoothed dirty buffer flushes (`vm.dirty_background_bytes = 128MB`, `vm.dirty_bytes = 512MB`), and remounted ext4 storage with `noatime,commit=60`.
  - Crawler Zero-Disk-Write Isolation: Updated all 53 crawler Kubernetes manifests to mount in-memory `emptyDir` (`medium: Memory`) for `/dev/shm` (1Gi) and `/tmp` (1Gi), raised memory limits to 2048Mi, and configured `BrowserLaunchFactory` to direct Chromium disk cache directly into `/dev/shm/browser-cache`.
- **Match Unification & Canonical Normalization**:
  - Enhanced `ClubTeamSignatureParser` with year-noise stripping (`YEAR_NOISE_PATTERN`) and expanded regional city suffixes.
  - Deployed `igaming-aggregator-normalizer` to K8s, resolving a 7,900+ backlog of unmapped factors and expanding multi-bookmaker matches from 4,050 to 4,948 (+900 matches).

## Capabilities

### Modified Capabilities
- `aggregator-core`: Enforces the No-Cap Policy, eliminates `PENDING_VERIFICATION` alert suppression, guarantees immediate `ACTIVE` status for all mathematically valid arbitrage pairs, and handles anomaly telemetry asynchronously.
- `portal-gateway`: Removes subscription tier yield capping (Free tier <= 5.0%, EV caps, middle caps), delivering full real-time arbs to all consumers.
- `verification-and-dod`: Codifies the complete review baseline, defining explicit DoD criteria for uncapped alerts, >= 500 match lines per bookmaker, and error-free 5-minute operation without night-time reboots.
- `k8s-infrastructure`: Establishes high-RAM allocation standards, ext4 `noatime,commit=60` mount options, kernel dirty page pacing, and PostgreSQL buffer scaling.
- `crawler-engine`: Mandates the Playwright & Chromium Zero-Disk-Write Policy using in-memory `/dev/shm` and `/tmp` emptyDir mounts and RAM-directed browser caching.

## Impact

- **Affected Modules**:
  - `igaming-portal` (`PortalAlertController`, DTOs, i18n messages)
  - `aggregator-surebet` (`SurebetRuleEvaluator`, `SurebetAlertManager`, `SurebetScanScheduler`)
  - `aggregator-api` (`SurebetQueryController`, `SurebetAlertRepository`)
  - `aggregator-domain` (`MatchUnificationService`, `ClubTeamSignatureParser`)
  - `igaming-source-core` (`BrowserLaunchFactory`)
  - `igaming-k8s` (All 53 crawler manifests, PostgreSQL configs)
  - `smartbet.guru` (UI displays unrestricted yield cards, badges, and filters)
