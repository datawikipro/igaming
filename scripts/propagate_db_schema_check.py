#!/usr/bin/env python3
"""
Propagates the fail-fast db-schema-check initContainer pattern across all K8s manifests in igaming-k8s/.
Ensures every crawler and loader deployment has a fast-path DDL presence check using standard K8s DNS service names.
"""

import os
import re
import glob

K8S_DIR = r"c:\Users\chernousov_a\IdeaProjects\igaming\igaming-k8s"

INIT_CONTAINER_TEMPLATE = """      - name: db-schema-check
        image: postgres:15-alpine
        imagePullPolicy: IfNotPresent
        env:
        - name: PGPASSWORD
          value: postgres
        command:
        - /bin/sh
        - -c
        - |
          echo "🔍 Checking database schema readiness..."
          HOST="{db_service_name}.igaming-source.svc.cluster.local"
          DB="{db_name}"
          USER="postgres"
          export PGPASSWORD="postgres"

          until psql -h "$HOST" -U "$USER" -d "$DB" -c "SELECT 1;" >/dev/null 2>&1; do
            echo "Waiting for PostgreSQL ($HOST) to accept queries..."
            sleep 2
          done

          EXISTS=$(psql -h "$HOST" -U "$USER" -d "$DB" -tAc "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'match_cache');" 2>/dev/null | tr -d '[:space:]')

          if [ "$EXISTS" = "t" ]; then
            echo "✅ Database tables already exist. Fast-starting application with ddl-auto=none..."
            exit 0
          fi

          echo "⚙️ Fresh database detected! Initializing schema tables..."
          psql -h "$HOST" -U "$USER" -d "$DB" -c "
          CREATE TABLE IF NOT EXISTS match_cache (
              id BIGSERIAL PRIMARY KEY,
              external_id VARCHAR(255) NOT NULL UNIQUE,
              bookmaker VARCHAR(50),
              sport_id INT,
              sport_name VARCHAR(255),
              league_name VARCHAR(255),
              format_info VARCHAR(255),
              team1 VARCHAR(255),
              team2 VARCHAR(255),
              score1 VARCHAR(255),
              score2 VARCHAR(255),
              is_live BOOLEAN DEFAULT FALSE,
              start_time BIGINT,
              potential_end_time BIGINT,
              event_url VARCHAR(1024),
              status VARCHAR(20) DEFAULT 'NEW',
              json_payload TEXT,
              payload_hash VARCHAR(64),
              updated_at TIMESTAMP,
              created_at TIMESTAMP,
              last_changed_at TIMESTAMP,
              external_updated_at BIGINT,
              version INT DEFAULT 0
          );
          CREATE TABLE IF NOT EXISTS league_cache (
              id BIGSERIAL PRIMARY KEY,
              external_id VARCHAR(255) NOT NULL UNIQUE,
              sport_name VARCHAR(255),
              region_name VARCHAR(255),
              league_name VARCHAR(255),
              url VARCHAR(1024),
              status VARCHAR(50),
              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
          );
          CREATE TABLE IF NOT EXISTS sport_cache (
              id BIGSERIAL PRIMARY KEY,
              external_id INT UNIQUE NOT NULL,
              name VARCHAR(255),
              kind VARCHAR(255),
              parent_id INT,
              updated_at TIMESTAMP
          );
          CREATE TABLE IF NOT EXISTS unmapped_bet (
              id BIGSERIAL PRIMARY KEY,
              sport_name VARCHAR(255),
              raw_name VARCHAR(255) NOT NULL,
              group_name VARCHAR(255),
              event_id VARCHAR(255),
              created_at TIMESTAMP
          );
          CREATE INDEX IF NOT EXISTS idx_mc_status_updated ON match_cache(status, updated_at);
          CREATE INDEX IF NOT EXISTS idx_mc_is_live ON match_cache(is_live);
          CREATE INDEX IF NOT EXISTS idx_mc_last_changed ON match_cache(last_changed_at);
          CREATE INDEX IF NOT EXISTS idx_mc_bookmaker ON match_cache(bookmaker);
          CREATE INDEX IF NOT EXISTS idx_mc_sport_live ON match_cache(sport_name, is_live);
          CREATE INDEX IF NOT EXISTS idx_mc_potential_end_time ON match_cache(potential_end_time);
          "
          echo "✅ Database schema initialization completed successfully!"
        resources:
          requests:
            cpu: 10m
            memory: 16Mi
          limits:
            cpu: 100m
            memory: 64Mi
"""

def extract_db_info(content, filename):
    # Match service name for db
    match_svc = re.search(r"name:\s*(igaming-source-[a-z0-9\-]+-db)", content)
    db_service_name = match_svc.group(1) if match_svc else None
    
    # Match POSTGRES_DB
    match_db = re.search(r"POSTGRES_DB\s*\n\s*value:\s*([a-zA-Z0-9_\-]+)", content)
    db_name = match_db.group(1) if match_db else None

    if not db_service_name:
        base = os.path.basename(filename).replace(".yaml", "").replace(".ru", "").replace(".com", "")
        db_service_name = f"igaming-source-{base}-db"
    
    if not db_name:
        base = os.path.basename(filename).replace(".yaml", "").replace(".ru", "").replace(".com", "").replace("-", "_")
        db_name = f"igaming_{base}"

    return db_service_name, db_name

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Skip files that aren't bookmaker manifests
    if "kind: Deployment" not in content or "StatefulSet" not in content:
        return False, "Skipped (not a bookmaker deployment manifest)"

    db_service_name, db_name = extract_db_info(content, filepath)
    
    # If db-schema-check already exists in both deployments, skip
    if content.count("name: db-schema-check") >= 2:
        return False, "Already up to date"

    init_container_code = INIT_CONTAINER_TEMPLATE.format(
        db_service_name=db_service_name,
        db_name=db_name
    )

    # Insert init_container_code under initContainers:
    # We find 'initContainers:' and insert the snippet right below
    new_content = ""
    lines = content.splitlines(keepends=True)
    i = 0
    modified = False
    
    while i < len(lines):
        line = lines[i]
        new_content += line
        if line.strip() == "initContainers:":
            # Check if next line is already db-schema-check
            if i + 1 < len(lines) and "db-schema-check" in lines[i+1]:
                i += 1
                continue
            new_content += init_container_code
            modified = True
        i += 1

    if modified:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        return True, f"Added db-schema-check for {db_service_name} ({db_name})"
    else:
        return False, "Could not locate initContainers: block"

def main():
    yaml_files = glob.glob(os.path.join(K8S_DIR, "*.yaml"))
    updated = 0
    skipped = 0
    
    for yf in yaml_files:
        is_mod, msg = process_file(yf)
        print(f"[{'UPDATED' if is_mod else 'SKIPPED'}] {os.path.basename(yf)}: {msg}")
        if is_mod:
            updated += 1
        else:
            skipped += 1
            
    print(f"\nSummary: {updated} files updated, {skipped} skipped.")

if __name__ == "__main__":
    main()
