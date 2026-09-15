## ADDED Requirements

### Requirement: Playwright and Chromium Zero-Disk-Write Policy
Crawlers utilizing headless Chromium or Playwright automation must store all browser profiles, raster caches, shader disks, and temporary session artifacts exclusively in volatile RAM (`tmpfs`), preventing disk wear and disk queue saturation on host storage.

#### Scenario: Container volume mounts for browser ephemeral data
- **WHEN** a crawler pod deployment is specified in Kubernetes manifests
- **THEN** the container specifies `volumeMounts` for `/dev/shm` and `/tmp`, mapped to `emptyDir` volumes with `medium: Memory` and `sizeLimit: 1Gi`.

#### Scenario: Automated browser launch profile configuration
- **WHEN** `BrowserLaunchFactory` initializes Chromium or Playwright
- **THEN** it detects available `/dev/shm` capacity (> 256MB) and automatically applies flags `--disk-cache-dir=/dev/shm/browser-cache`, `--disable-dev-shm-usage=false` (POSIX shared memory), and `--js-flags=--max-old-space-size=1024`.

#### Scenario: Memory limit allocation for RAM caching
- **WHEN** running crawler pods with RAM-backed browser caching
- **THEN** container memory limit is allocated to at least 2048Mi, accommodating V8 heaps, browser rendering buffers, and shared memory without triggering Kubernetes OOM killer.
