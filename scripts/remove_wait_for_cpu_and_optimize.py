import os
import re

K8S_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "igaming-k8s")

def optimize_manifest(content):
    # 1. Remove wait-for-low-cpu init container
    # Pattern matches from "- name: wait-for-low-cpu" up to the next container or section ("      containers:" or "      - name:")
    pattern_wait_cpu = re.compile(
        r"      - name: wait-for-low-cpu\n(?:        [^\n]*\n)+",
        re.MULTILINE
    )
    content = pattern_wait_cpu.sub("", content)

    # 2. Fix db-schema-check resources
    # Replace large requests in db-schema-check
    pattern_db_schema_res = re.compile(
        r"(name: db-schema-check\n(?:        [^\n]*\n)+?        resources:\n          requests:\n            cpu:\s*)\d+m(\n            memory:\s*)\d+Mi",
        re.MULTILINE
    )
    content = pattern_db_schema_res.sub(r"\g<1>5m\g<2>16Mi", content)

    # 3. Fix main container requests (cpu: 200m/150m/100m -> cpu: 20m, memory: 256Mi)
    # Replace requests under containers
    pattern_container_requests = re.compile(
        r"(requests:\n            cpu:\s*)(?:150m|200m|100m|300m|400m|500m|800m)(\n            memory:\s*)(?:200Mi|384Mi|512Mi|300Mi|400Mi)",
        re.MULTILINE
    )
    content = pattern_container_requests.sub(r"\g<1>20m\g<2>256Mi", content)

    # 4. Replace legacy probes (/api/v1/monitoring/info -> /actuator/health/...)
    content = content.replace("path: /api/v1/monitoring/info", "path: /actuator/health/readiness")
    # For liveness probe section specifically:
    content = re.sub(
        r"(livenessProbe:\n\s+httpGet:\n\s+path: )/actuator/health/readiness",
        r"\g<1>/actuator/health/liveness",
        content
    )

    # 5. Add 'master' to node-type affinity so scheduler has maximum flexibility across all 6 nodes
    pattern_affinity = re.compile(
        r"(matchExpressions:\n\s+- key: node-type\n\s+operator: In\n\s+values:\n\s+- spot\n\s+- standard)(\n(?!\s+- master))",
        re.MULTILINE
    )
    content = pattern_affinity.sub(r"\g<1>\n                - master\g<2>", content)

    return content

def main():
    modified_files = []
    for filename in os.listdir(K8S_DIR):
        if not filename.endswith(".yaml"):
            continue
        filepath = os.path.join(K8S_DIR, filename)
        with open(filepath, "r", encoding="utf-8") as f:
            original = f.read()

        optimized = optimize_manifest(original)
        if optimized != original:
            with open(filepath, "w", encoding="utf-8") as f:
                f.write(optimized)
            modified_files.append(filename)

    print(f"Successfully optimized {len(modified_files)} manifests:")
    for m in modified_files:
        print(f"  - {m}")

if __name__ == "__main__":
    main()
