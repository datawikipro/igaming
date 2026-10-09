import os
import re
import subprocess
import sys

CLONES = ["22bet", "1xbit", "betwinner", "linebet"]
K8S_DIR = r"c:\Users\chernousov_a\IdeaProjects\igaming\igaming-k8s"

JAVA_OPTS_VAL = (
    "-XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0 -XX:+UseG1GC "
    "-XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication "
    "-Dhttp.proxyHost=100.83.113.50 -Dhttp.proxyPort=3128 "
    "-Dhttps.proxyHost=100.83.113.50 -Dhttps.proxyPort=3128 "
    "-Dspring.kafka.producer.properties.enable.idempotence=false"
)

EXTRA_ENVS = """        - name: APP_PROXY_HOST
          value: 100.83.113.50
        - name: APP_PROXY_PORT
          value: "3128"
        - name: APP_VPN_FORCE
          value: "true"
        - name: APP_PROXY_DIRECT
          value: "false"
        - name: SPRING_KAFKA_PRODUCER_PROPERTIES_ENABLE_IDEMPOTENCE
          value: "false"
"""

for bk in CLONES:
    yaml_path = os.path.join(K8S_DIR, f"{bk}.yaml")
    if not os.path.exists(yaml_path):
        print(f"File not found: {yaml_path}")
        continue

    with open(yaml_path, "r", encoding="utf-8") as f:
        content = f.read()

    # 1. Update Image
    content = content.replace("ghcr.io/datawikipro/igaming-source-betb2b:latest", "100.78.183.101:30500/igaming-source-betb2b:latest")

    # 2. Update Aggregator URL
    content = re.sub(
        r"- name: APP_AGGREGATOR_URL\s+value: http://igaming-aggregator(?!\.)",
        "- name: APP_AGGREGATOR_URL\n          value: http://igaming-aggregator.igaming-dev.svc.cluster.local:8080",
        content
    )

    # 3. Update Proxy Backend URL
    content = content.replace("service-proxy-backend.service-proxy.svc.cluster.local", "service-proxy-backend.proxy.svc.cluster.local")

    # 4. Update Live & Prematch URLs
    content = re.sub(
        r"- name: APP_BETB2B_LIVE_URL\s+value: https://[^/]+/LiveFeed/Get1xMatchByLeague",
        "- name: APP_BETB2B_LIVE_URL\n          value: https://1x-bet.com/LiveFeed/Get1xMatchByLeague",
        content
    )
    content = re.sub(
        r"- name: APP_BETB2B_PREMATCH_URL\s+value: https://[^/]+/LineFeed/Get1xMatchByLeague",
        "- name: APP_BETB2B_PREMATCH_URL\n          value: https://1x-bet.com/LineFeed/Get1xMatchByLeague",
        content
    )

    # 5. Update JAVA_OPTS
    content = re.sub(
        r"- name: JAVA_OPTS\s+value: [^\n]+(?:\n\s+[^\n]+)*",
        f"- name: JAVA_OPTS\n          value: {JAVA_OPTS_VAL}",
        content
    )

    # 6. Ensure EXTRA_ENVS are present in containers
    # Add EXTRA_ENVS after JAVA_OPTS if not already present
    if "APP_PROXY_HOST" not in content:
        content = content.replace(
            f"- name: JAVA_OPTS\n          value: {JAVA_OPTS_VAL}",
            f"- name: JAVA_OPTS\n          value: {JAVA_OPTS_VAL}\n{EXTRA_ENVS}"
        )

    with open(yaml_path, "w", encoding="utf-8") as f:
        f.write(content)

    print(f"Patched {yaml_path}")

    # Apply to K8s
    cmd_apply = ["kubectl", "apply", "-f", yaml_path]
    res = subprocess.run(cmd_apply, capture_output=True, text=True)
    print(f"kubectl apply {bk}: {res.stdout.strip()} {res.stderr.strip()}")

print("All 4 BetB2B clones patched and applied!")
