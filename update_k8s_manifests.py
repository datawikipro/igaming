import os
import re

k8s_dir = r"c:\Users\chernousov_a\IdeaProjects\igaming\igaming-k8s"
files = sorted([f for f in os.listdir(k8s_dir) if f.endswith(".yaml")])

processed_count = 0
skipped_count = 0

for filename in files:
    filepath = os.path.join(k8s_dir, filename)
    with open(filepath, "r", encoding="utf-8") as fp:
        raw = fp.read()

    # Only process manifests that contain an igaming-source loader
    if "igaming-source-" not in raw or ("-loader" not in raw and "match-loader" not in raw):
        skipped_count += 1
        continue

    # 1. Clean up legacy poll vars from existing content across the entire file
    cleaned = re.sub(
        r"[ \t]*-[ \t]*name:[ \t]*APP_ODDS_REFRESH_LIVE_POLL_MS\r?\n[ \t]*value:[ \t]*[^\r\n]+\r?\n?",
        "",
        raw,
    )
    cleaned = re.sub(
        r"[ \t]*-[ \t]*name:[ \t]*APP_ODDS_REFRESH_PREMATCH_POLL_MS\r?\n[ \t]*value:[ \t]*[^\r\n]+\r?\n?",
        "",
        cleaned,
    )

    # Check if refresher is already present
    if "-refresher" in cleaned or "odds-refresher" in cleaned:
        # Just write back cleaned without duplicate refresher
        with open(filepath, "w", encoding="utf-8") as fp:
            fp.write(cleaned)
        print(f"Cleaned legacy vars in {filename} (refresher already present)")
        processed_count += 1
        continue

    # Split documents by ---
    docs = re.split(r"\n---\r?\n", cleaned)

    loader_svc = None
    loader_dep = None

    for doc in docs:
        if "kind: Service" in doc and "-loader" in doc:
            loader_svc = doc
        elif "kind: Deployment" in doc and "-loader" in doc:
            loader_dep = doc

    if not loader_svc or not loader_dep:
        print(f"Warning: could not find both loader Service and Deployment in {filename}")
        with open(filepath, "w", encoding="utf-8") as fp:
            fp.write(cleaned)
        continue

    # Generate refresher service
    refresher_svc = loader_svc
    refresher_svc = re.sub(
        r"name:\s*(igaming-source-[a-zA-Z0-9\.\-]+)-loader",
        r"name: \1-refresher",
        refresher_svc,
    )
    refresher_svc = re.sub(
        r"app:\s*(igaming-source-[a-zA-Z0-9\.\-]+)-loader",
        r"app: \1-refresher",
        refresher_svc,
    )

    # Generate refresher deployment
    refresher_dep = loader_dep
    refresher_dep = re.sub(
        r"name:\s*(igaming-source-[a-zA-Z0-9\.\-]+)-loader",
        r"name: \1-refresher",
        refresher_dep,
    )
    refresher_dep = re.sub(
        r"app:\s*(igaming-source-[a-zA-Z0-9\.\-]+)-loader",
        r"app: \1-refresher",
        refresher_dep,
    )

    # Replace roles & profiles
    refresher_dep = re.sub(
        r"value:\s*dev,match-loader",
        r"value: dev,odds-refresher",
        refresher_dep,
    )
    refresher_dep = re.sub(
        r"value:\s*match-loader",
        r"value: odds-refresher",
        refresher_dep,
    )

    # Point Redis to cluster Redis
    refresher_dep = re.sub(
        r"(-\s*name:\s*SPRING_DATA_REDIS_HOST\r?\n\s*value:\s*)\S+",
        r"\g<1>igaming-redis.igaming-dev.svc.cluster.local",
        refresher_dep,
    )

    # Remove APP_MATCH_LOADER_POLL_DELAY_MS if present in refresher
    refresher_dep = re.sub(
        r"[ \t]*-[ \t]*name:[ \t]*APP_MATCH_LOADER_POLL_DELAY_MS\r?\n[ \t]*value:[ \t]*[^\r\n]+\r?\n?",
        "",
        refresher_dep,
    )

    # Adjust resources to lightweight sentry profile (20m / 180Mi request, 200m / 350Mi limit)
    refresher_dep = re.sub(
        r"requests:\s*\r?\n\s*cpu:\s*\S+\s*\r?\n\s*memory:\s*\S+",
        "requests:\n            cpu:  20m\n            memory:  180Mi",
        refresher_dep,
    )
    refresher_dep = re.sub(
        r"limits:\s*\r?\n\s*cpu:\s*\S+\s*\r?\n\s*memory:\s*\S+",
        "limits:\n            cpu:  200m\n            memory:  350Mi",
        refresher_dep,
    )

    # Remove redis sidecar container if present in refresher
    refresher_dep = re.sub(
        r"[ \t]*-[ \t]*name:[ \t]*redis\r?\n[ \t]*image:[ \t]*redis:[^\r\n]+\r?\n(?:[ \t]+[^\r\n]+\r?\n)*?(?=[ \t]*(?:volumes:|ports:|volumeMounts:|\Z))",
        "",
        refresher_dep,
    )

    # Simplify initContainer to lightweight db-wait with pg_isready
    db_host_m = re.search(r"HOST=\"?([a-zA-Z0-9\.\-]+-db)\"?", loader_dep)
    if not db_host_m:
        db_host_m = re.search(r"([a-zA-Z0-9\.\-]+-db)", loader_dep)
    db_host = db_host_m.group(1) if db_host_m else "localhost"

    simple_init = f"""      initContainers:
      - name: db-wait
        image: postgres:15-alpine
        imagePullPolicy: IfNotPresent
        env:
        - name: PGPASSWORD
          value: postgres
        command:
        - /bin/sh
        - -c
        - |
          echo "Waiting for PostgreSQL ({db_host}) to accept queries..."
          until pg_isready -h {db_host}.igaming-source.svc.cluster.local -p 5432 -U postgres; do
            sleep 2
          done
          echo "Database is ready!"
        resources:
          requests:
            cpu: 10m
            memory: 32Mi
          limits:
            cpu: 100m
            memory: 64Mi"""

    refresher_dep = re.sub(
        r"[ \t]*initContainers:\r?\n(?:[ \t]+[^\r\n]+\r?\n)*?(?=[ \t]*containers:)",
        simple_init + "\n",
        refresher_dep,
    )

    # Append refresher Service and Deployment before the DB service
    # Find position of DB Service or append before last service
    db_svc_index = -1
    for i, doc in enumerate(docs):
        if "-db" in doc and "kind: Service" in doc:
            db_svc_index = i
            break

    if db_svc_index != -1:
        docs.insert(db_svc_index, refresher_svc.strip())
        docs.insert(db_svc_index + 1, refresher_dep.strip())
    else:
        docs.append(refresher_svc.strip())
        docs.append(refresher_dep.strip())

    new_content = "\n---\n".join([d.strip() for d in docs if d.strip()]) + "\n"

    with open(filepath, "w", encoding="utf-8") as fp:
        fp.write(new_content)

    print(f"Successfully added refresher template and cleaned legacy vars in {filename}")
    processed_count += 1

print(f"\nCompleted! Processed: {processed_count}, Skipped: {skipped_count}")
