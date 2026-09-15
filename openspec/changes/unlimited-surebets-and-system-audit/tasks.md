# Implementation Tasks: Unlimited Surebets, RAM Optimization & Full System Audit

## 1. Elimination of Free Tier Restrictions (Portal & UI)

- [x] 1.1 Remove `FREE_TIER_MAX_PROFIT = 5.0` and profit gating in `PortalAlertController.java`
- [x] 1.2 Remove value bet EV cap (`<= 0.05`) and middle bet profit cap (`<= 0.60`) in `PortalAlertController.java`
- [x] 1.3 Update frontend localization dictionaries (`messages/ru.json`, `messages/en.json`) to remove legacy "up to 5.0%" limitations
- [x] 1.4 Verify guest and Free tier consumers receive unrestricted live and prematch surebet streams

## 2. Uncapped Arbitrage Engine & Database Awakening

- [x] 2.1 Update `SurebetRuleEvaluator.java` to eliminate artificial yield caps (10.0% / 50.0%) so genuine high-yield opportunities are never flagged as requiring manual verification
- [x] 2.2 Refactor `SurebetAlertManager.java` to default all valid mathematical arbitrage opportunities immediately to `Status.ACTIVE`
- [x] 2.3 Ensure structural errors (single-bookmaker negative margin, inverted totals) are isolated to `odds_anomaly` while genuine market discrepancies remain public
- [x] 2.4 Verify database state transition: active surebets in `surebet_alert` expanded from 3 to 484 ACTIVE alerts with top yields up to +49.4%

## 3. Match Unification & Canonical Data Pipeline

- [x] 3.1 Implement year-noise stripping (`YEAR_NOISE_PATTERN`) and expanded regional suffixes in `ClubTeamSignatureParser.java`
- [x] 3.2 Add phonetic transliteration and dynamic `TeamAliasRepository` learning in `MatchUnificationService.java`
- [x] 3.3 Fix deployment image mapping for `igaming-aggregator-normalizer` in Kubernetes (swapped from ingestion to normalizer)
- [x] 3.4 Build and deploy fresh `ghcr.io/datawikipro/igaming-aggregator-normalizer:latest` via Jib and process unmapped factors backlog (> 7,900 items)
- [x] 3.5 Verify multi-bookmaker match unification growth (expanded from 4,050 to 4,948 matches)

## 4. RAM Utilization, OS Kernel & Database Optimization

- [x] 4.1 Tune Aggregator Core PostgreSQL DB (`shared_buffers = 4GB`, `work_mem = 64MB`, `effective_cache_size = 6GB`) achieving 98.977% cache hit ratio
- [x] 4.2 Standardize 52 Source PostgreSQL DBs (`synchronous_commit = off`, `checkpoint_timeout = 15min`, `checkpoint_completion_target = 0.9`, `max_wal_size = 2GB`, `wal_writer_delay = 50ms`), cutting load average from 137.17 to 74.62
- [x] 4.3 Configure host `/tmp` as 24GB `tmpfs` in `/etc/fstab`
- [x] 4.4 Configure Linux kernel dirty buffer flushes (`vm.dirty_background_bytes = 128MB`, `vm.dirty_bytes = 512MB`) in `/etc/sysctl.d/99-disk-tuning.conf`
- [x] 4.5 Remount ext4 filesystems (`/` and `/data`) with `noatime,commit=60` and persist in `/etc/fstab` to eliminate journal thrashing
- [x] 4.6 Reconfigure `kube-controller-manager` and `kube-scheduler` leader election leases (120s duration / 60s renew) to eliminate controller restart loops
- [x] 4.7 Update `BrowserLaunchFactory.java` to auto-detect `/dev/shm` > 256MB, set `--disk-cache-dir=/dev/shm/browser-cache`, enable POSIX shm, and set 1024MB V8 heap
- [x] 4.8 Patch all 53 crawler manifests in `igaming-k8s` with `emptyDir: medium: Memory` (1Gi) for `/dev/shm` and `/tmp`, raise memory limit to 2048Mi, and fix `betcity.com.yaml` line 125 syntax error

## 5. Daytime Execution Backlog (Post-10:00 AM MSK Roadmap)

- [ ] 5.1 Resume batch rollout of the 53 crawler manifests via `/root/apply_crawlers_batch.sh` once the server is online
- [ ] 5.2 Verify in-cluster zero disk writes for Playwright & Chromium (`df -h /dev/shm /tmp` inside crawler pods and collapse of `D`-state disk queues)
- [ ] 5.3 Migrate 52 source PostgreSQL DBs `pgdata` volume to `emptyDir: medium: Memory` (total footprint 4.7 GB) for pure in-memory source caching
- [ ] 5.4 Audit crawlers/loaders with line depth < 500 matches (Caesars, FanDuel, BetMGM, DraftKings, Sbobet, Dafabet) to satisfy Golden Rule #8
- [ ] 5.5 Connect and verify automated match settlement engine (`match-results-settlement-engine`) to settle historical `surebet_alert` records
- [ ] 5.6 Run end-to-end Definition of Done verification with the mandatory 5-minute continuous clean log timer (`schedule`)

## 6. OpenSpec Specifications & Architectural Alignment

- [x] 6.1 Draft proposal, architectural design, and delta specs (`k8s-infrastructure`, `crawler-engine`, `aggregator-core`, `portal-gateway`, `verification-and-dod`)
- [x] 6.2 Validate all OpenSpec specifications using `openspec validate --specs` (6 of 6 passed)
