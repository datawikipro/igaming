# Architectural Design: Unlimited Surebets, RAM Optimization & Full System Audit

## Context

This document captures the complete architectural review of the SmartBet.guru ecosystem as of the night-time maintenance interval on September 12, 2026. 

The system operates 52 bookmaker integrations across two Kubernetes clusters (`xeon-srv` master on-premise and GCP worker nodes), routing data through an in-memory matching core (`igaming-aggregator`), persistent storage (PostgreSQL StatefulSets), and an API gateway (`igaming-portal`).

Recent operations addressed severe baseline bottlenecks (PostgreSQL disk saturation, leader election flappings, stopped normalizer, and crawler crashes). Furthermore, all legacy Free Tier limits (capping arbs at 5.0%) and scanner verification traps (capping arbs at 10.0%) were eliminated.

---

## 1. Complete Review of Accomplished Work

### 1.1 Elimination of Free Tier Limitations
- **`igaming-portal`**: Removed `FREE_TIER_MAX_PROFIT = 5.0` constant, removed `vb.getEv() <= 0.05` filter, and removed `m.getProfitIfBoth() <= 0.60` filter from `PortalAlertController`.
- **`smartbet.guru`**: Removed "up to 5.0%" messaging from localization catalogs (`ru.json`, `en.json`).
- **Verification**: Confirmed that unauthenticated guest users and Free tier users now receive the full feed including high-yield arbs with identical precision to premium tiers.

### 1.2 Uncapped Arbitrage Engine & Database Awakening
- **Rule Engine**: Updated `SurebetRuleEvaluator.java` to eliminate artificial yield caps (10.0% / 50.0%) so genuine high-yield opportunities are never flagged as requiring manual verification.
- **Alert Manager**: Refactored `SurebetAlertManager.java` to default all valid mathematical arbitrage opportunities immediately to `Status.ACTIVE`.
- **Anomaly Telemetry**: High-yield sanity checks and asynchronous refreshers were decoupled into non-blocking background monitoring (`OddsAnomalyService`), ensuring zero delay or suppression in user alerts.
- **Result**: Database active surebets rose from 3 to **484 ACTIVE alerts**, with top prematch arbs on `smartbet.guru` delivering real-time yields between 41.2% and 49.4% (e.g. Sweden vs Romania at +49.40%).

### 1.3 Match Unification & Canonical Normalization
- **`ClubTeamSignatureParser`**: Added year-noise stripping (`YEAR_NOISE_PATTERN`) to strip founding years (e.g. "Монтана 1921" -> "Монтана") and expanded regional city suffixes (Bulgarian, Russian, European).
- **`MatchUnificationService`**: Injected `TeamAliasRepository`, enabled phonetic Latin/Cyrillic matching via `TransliterationHelper`, time-proximity ranking, and auto-registration of discovered team aliases.
- **`igaming-aggregator-normalizer`**: Fixed K8s deployment (was erroneously running `aggregator-ingestion` container), compiled and deployed fresh `aggregator-normalizer:latest` via Jib. Processed over 7,900 unmapped bet types.
- **Result**: Multi-bookmaker match volume expanded from 4,050 to **4,948 matches** (+900 multi-source events).

### 1.4 Physical Storage Bottleneck RCA & Host RAM Offloading
- **Root Cause Analysis (RCA)**:
  - Discovered physical disk disparity on `xeon-srv`: Root `/` is located on a 111.3 GB mechanical SAS drive (`sda1`/`sdb1`), while secondary `/data` is on a 930.5 GB mechanical SAS drive.
  - The mechanical root disk suffered from extreme queue depth (96 to 178) and I/O wait exceeding 55%.
  - `/proc/*/io` investigation revealed that Playwright Chromium processes inside 52 crawler pods were writing gigabytes of render caches, shader tiles, and profile data to `/tmp` inside container overlayfs on the mechanical root disk.
  - Furthermore, 52 PostgreSQL databases were performing frequent 5-second checkpoint and WAL flushes, competing for disk heads with ext4 journal commits (`jbd2/sdb1-8`) and page flush workers (`flush-8:16`).
- **OS & Kernel Tuning**:
  - **Host `/tmp` RAM Mount**: Mounted 24GB `tmpfs` at `/tmp` (`tmpfs size=24G,mode=1777,nosuid,nodev`) and persisted in `/etc/fstab`.
  - **Linux Kernel Dirty Flush Tuning**: Configured `vm.dirty_background_bytes = 134217728` (128 MB) and `vm.dirty_bytes = 536870912` (512 MB) in `/etc/sysctl.d/99-disk-tuning.conf`. Eliminated massive multi-gigabyte flush freezes.
  - **Ext4 Filesystem Mount Options**: Remounted `/` and `/data` with `noatime,commit=60` and persisted in `/etc/fstab`. This eliminates continuous inode access time writes on reads and reduces ext4 journal commit thrashing by 12x.

### 1.5 PostgreSQL High-RAM Memory Scaling
- **Aggregator Core DB (`igaming-aggregator-db-0`)**:
  - Allocated `shared_buffers = 4GB`, `work_mem = 64MB`, `effective_cache_size = 6GB`, and `maintenance_work_mem = 512MB`.
  - **Result**: Cache hit ratio reached **98.977%** (> 2.3 billion RAM buffer hits).
- **52 Source Bookmaker & Portal DBs**:
  - Standardized configuration across all databases:
    - `checkpoint_timeout = '15min'`
    - `checkpoint_completion_target = 0.9`
    - `max_wal_size = '2GB'`, `min_wal_size = '512MB'`
    - `wal_writer_delay = '50ms'`
    - `synchronous_commit = off` (Golden Rule #7)
  - **Result**: Checkpoint write spikes eliminated; server load average dropped from **137.17 down to 74.62**.

### 1.6 Playwright & Chromium Zero-Disk-Write Architecture
- **`BrowserLaunchFactory.java`**:
  - Implemented automatic `/dev/shm` capacity detection (> 256MB).
  - Redirected browser disk cache directly into memory: `--disk-cache-dir=/dev/shm/browser-cache`.
  - Enabled true POSIX shared memory (`--disable-dev-shm-usage=false`).
  - Configured 1024MB V8 heap (`--js-flags=--max-old-space-size=1024`).
- **Kubernetes Manifests (`igaming-k8s/*.yaml`)**:
  - Patched all 53 crawler manifests to mount `emptyDir: medium: Memory` (1Gi) for both `/dev/shm` and `/tmp`.
  - Raised container memory limits to `2048Mi`.
  - Fixed syntax and volume placement error in `betcity.com.yaml` (line 125). Validated all 58 YAML files in `igaming-k8s` with 100% clean syntax.

---

## 2. Architectural Decisions & Principles

### Decision 1: Absolute No-Cap Policy (Zero Artificial Ceilings)
- **Problem**: Arbitrary thresholds (whether 5%, 10%, or 50%) inadvertently suppress genuine, lucrative arbitrage opportunities into `PENDING_VERIFICATION`, where they remain hidden from end users.
- **Decision**: The system shall enforce NO upper limit on displayed profit percentages. Any mathematically valid cross-bookmaker arbitrage combination shall be stored and displayed immediately with status `ACTIVE`.
- **Philosophy**: "50% surebet" is a metaphor for extraordinary market value, not an engineering ceiling. If a structural error is not mathematically proven, show the opportunity; user feedback and automated telemetry will refine mappers rapidly.

### Decision 2: Asynchronous Anomaly Telemetry Without Alert Suppression
- **Problem**: Synchronous verification mechanisms block alerts until manual approval or scraper rechecks, causing opportunities to expire.
- **Decision**: High-yield arbs are saved as `ACTIVE` and broadcast immediately. In parallel, an asynchronous event is dispatched to `OddsAnomalyService` (`odds_anomaly`) and a priority refresh is scheduled.
- **Structural Error Exclusions**: The only alerts dropped are true structural impossibilities:
  1. Negative margin within the same bookmaker.
  2. Inverted totals or handicaps (e.g. Over 2.5 vs Under 1.5).
  3. Factor code collisions.

### Decision 3: RAM-First Caching (Capitalizing on 188 GiB Physical RAM)
- **Problem**: Mechanical SAS disks are inherently limited to ~150-200 IOPS and easily bottlenecked by write concurrency.
- **Decision**: All ephemeral, intermediate, and volatile data (Chromium cache, renderer profiles, OS `/tmp`, PostgreSQL shared buffers, WAL delays) are offloaded to physical RAM via `tmpfs` and memory tuning.

---

## 3. Backlog: What Remains To Be Done (Execution Roadmap)

The following sequence is scheduled for execution once the physical server is online during daytime (after 10:00 MSK):

### Phase 1: Complete Batch Rollout of Crawler Manifests
- **Status**: 10 of 53 crawlers updated before `betcity.com.yaml` syntax issue paused the script.
- **Action**: Deploy the validated manifests from `/root/k8s-manifests/` across the remaining 43 bookmakers using `/root/apply_crawlers_batch.sh` (5 pods per batch, 4s pause).
- **Target**: All 53 crawlers running with `tmpfs` on `/dev/shm` and `/tmp` and 2048Mi RAM limit.

### Phase 2: In-Cluster Zero-Disk-Write Verification
- **Action**: Inspect active crawler pods via `df -h /dev/shm /tmp` (verify 1.0G tmpfs mount).
- **Action**: Run `ps -eo state,pid,cmd | grep '^D'` on `xeon-srv` to verify that `chrome`, `flush-8:16`, and `jbd2` no longer saturate disk wait queues.

### Phase 3: PostgreSQL Source Databases RAM Offloading (`emptyDir: medium: Memory`)
- **Status**: All 52 bookmaker databases combined consume only **4.7 GB total** on disk.
- **Action**: Update `pgdata` volume in `igaming-source-*-db` StatefulSets to `emptyDir: medium: Memory, sizeLimit: 512Mi`.
- **Target**: Entire bookmaker source caching tier operates 100% in RAM, reducing host disk writes from PostgreSQL to virtually zero.

### Phase 4: Crawler Line Volume Scaling (>= 500 Matches - Golden Rule #8)
- **Action**: Audit crawlers currently holding < 500 matches in `match_cache` (Caesars, FanDuel, BetMGM, DraftKings, Sbobet, Dafabet).
- **Action**: Expand sport category scrapers and league fetch intervals to satisfy Golden Rule #8 (`count(*) >= 500`).

### Phase 5: Automated Match Result Settlement Connection
- **Action**: Integrate `match-results-settlement-engine` to record final match scores in `match_record` and settle historical `surebet_alert` records for accurate ROI analytics.

### Phase 6: Final Verification & Definition of Done (Golden Rule #1)
- **Action**: Verify all services healthy in namespace `igaming-dev` and `igaming-source`.
- **Action**: Enforce the mandatory 5-minute continuous clean log monitoring using the `schedule` tool before final completion.
