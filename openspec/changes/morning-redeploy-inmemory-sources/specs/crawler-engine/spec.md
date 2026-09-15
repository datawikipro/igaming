# Delta Spec: In-Memory Storage & Java Schema Management

## ADDED REQUIREMENTS

### Requirement: In-Memory PostgreSQL for Source Operational Caching
- **SHALL**: Every bookmaker database in `igaming-source` MUST mount storage on `emptyDir: { medium: Memory }` with a 1Gi size limit.
- **SHALL**: Physical disk write operations (`fsync`, `full_page_writes`) MUST be disabled via server arguments (`fsync=off`, `synchronous_commit=off`, `wal_level=minimal`).
- **SHALL**: Bookmaker line scraping caches (`match_cache`, `match_factor`) MUST operate with zero disk I/O to prevent server load spikes.

### Requirement: Java-Driven Schema Auto-Migration
- **SHALL**: Every crawler and loader application MUST execute `DatabaseMigrationRunner` on startup before Hibernate EntityManager initialization.
- **SHALL**: Database tables `match_cache`, `match_factor`, `league_cache`, `sport_cache`, `unmapped_bet`, and `unmapped_sport` MUST be created idempotently with all required indexes.
- **SHALL**: The `match_factor` table MUST define surrogate primary key `id BIGSERIAL` and column `factor_id VARCHAR(255)`.
- **SHALL**: The `unmapped_sport` table MUST include column `bookmaker VARCHAR(255)`.

### Requirement: Headless Browser Process Lifecycle & Zombie Reaping
- **SHALL**: All pod specifications executing browser automation (`APP_BROWSER_STEALTH_PROFILE`) MUST enable `shareProcessNamespace: true`.
- **SHALL**: Orphaned browser and renderer child processes MUST be reaped by the pod PID 1 pause container upon exit to prevent process table exhaustion.
