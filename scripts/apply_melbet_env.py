import subprocess

cmd = (
    "kubectl -n igaming-source set env deployment/igaming-source-melbet-com-loader "
    'JAVA_TOOL_OPTIONS- '
    'APP_PROXY_HOST="100.83.113.50" '
    'APP_PROXY_PORT="3128" '
    'APP_VPN_FORCE="true" '
    'APP_PROXY_DIRECT="false" '
    'JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Dhttp.proxyHost=100.83.113.50 -Dhttp.proxyPort=3128 -Dhttps.proxyHost=100.83.113.50 -Dhttps.proxyPort=3128 -Dspring.kafka.producer.properties.enable.idempotence=false"'
)

res = subprocess.run(
    ["ssh", "-o", "BatchMode=yes", "root@100.78.183.101", cmd],
    capture_output=True,
    text=True
)
print("Returncode:", res.returncode)
print("STDOUT:", res.stdout)
print("STDERR:", res.stderr)
