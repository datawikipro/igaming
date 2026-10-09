import subprocess
import json
import time

CLONES = {
    "22bet": {"partner_id": "151", "db": "igaming-source-22bet-db-0", "dbname": "igaming_22bet"},
    "1xbit": {"partner_id": "104", "db": "igaming-source-1xbit-db-0", "dbname": "igaming_1xbit"},
    "betwinner": {"partner_id": "120", "db": "igaming-source-betwinner-db-0", "dbname": "igaming_betwinner"},
    "linebet": {"partner_id": "189", "db": "igaming-source-linebet-db-0", "dbname": "igaming_linebet"},
}

IMAGE = "100.78.183.101:30500/igaming-source-betb2b:latest"
JAVA_OPTS = (
    "-XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0 -XX:+UseG1GC "
    "-XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication "
    "-Dhttp.proxyHost=100.83.113.50 -Dhttp.proxyPort=3128 "
    "-Dhttps.proxyHost=100.83.113.50 -Dhttps.proxyPort=3128 "
    "-Dspring.kafka.producer.properties.enable.idempotence=false"
)

ENVS = [
    "APP_AGGREGATOR_URL=http://igaming-aggregator.igaming-dev.svc.cluster.local:8080",
    "APP_BETB2B_LIVE_URL=https://1x-bet.com/LiveFeed/Get1xMatchByLeague",
    "APP_BETB2B_PREMATCH_URL=https://1x-bet.com/LineFeed/Get1xMatchByLeague",
    "APP_PROXY_BACKEND_URL=http://service-proxy-backend.proxy.svc.cluster.local",
    "APP_PROXY_HOST=100.83.113.50",
    "APP_PROXY_PORT=3128",
    "APP_VPN_FORCE=true",
    "APP_PROXY_DIRECT=false",
    "SPRING_KAFKA_PRODUCER_PROPERTIES_ENABLE_IDEMPOTENCE=false",
    f"JAVA_OPTS={JAVA_OPTS}"
]

for bk, info in CLONES.items():
    print(f"=== Updating {bk} ===")
    crawler_dep = f"igaming-source-{bk}-crawler"
    loader_dep = f"igaming-source-{bk}-loader"

    # Set image
    cmd_img_c = ["kubectl", "set", "image", f"deployment/{crawler_dep}", f"{crawler_dep}={IMAGE}", "-n", "igaming-source"]
    cmd_img_l = ["kubectl", "set", "image", f"deployment/{loader_dep}", f"{loader_dep}={IMAGE}", "-n", "igaming-source"]
    subprocess.run(cmd_img_c, check=True)
    subprocess.run(cmd_img_l, check=True)

    # Set env vars
    cmd_env_c = ["kubectl", "set", "env", f"deployment/{crawler_dep}", "-n", "igaming-source"] + ENVS
    cmd_env_l = ["kubectl", "set", "env", f"deployment/{loader_dep}", "-n", "igaming-source"] + ENVS
    subprocess.run(cmd_env_c, check=True)
    subprocess.run(cmd_env_l, check=True)

    # Reset match cache status to NEW so loader flushes cached matches
    db_pod = info["db"]
    dbname = info["dbname"]
    cmd_sql = [
        "kubectl", "exec", "-i", "-n", "igaming-source", db_pod, "--",
        "psql", "-U", "postgres", "-d", dbname, "-c",
        "UPDATE match_cache SET status = 'NEW' WHERE status != 'NEW';"
    ]
    res_sql = subprocess.run(cmd_sql, capture_output=True, text=True)
    print(f"DB update for {bk}: {res_sql.stdout.strip()}")

print("All 4 clones successfully configured via kubectl!")
