#!/bin/sh
CONFIG_DIR="/config"
HEARTBEAT_INTERVAL="${HEARTBEAT_INTERVAL:-30}"
TEST_URL="${VPN_TEST_URL:-https://ya.ru}"
CONSECUTIVE_FAILURES=0
MAX_FAILURES=3

while [ ! -f "${CONFIG_DIR}/metadata.env" ]; do sleep 2; done
. "${CONFIG_DIR}/metadata.env"
while ! nc -z localhost 3128 2>/dev/null; do sleep 2; done

echo "=== VPN Pool Healthcheck Starting (${VPN_TYPE}) ==="

EXTERNAL_IP=""
DETECTED_COUNTRY=""
DETECTED_CITY=""

send_heartbeat() {
    POD_IP=$(hostname -i 2>/dev/null || echo "${POD_IP:-}")
    # Use short timeout for heartbeat to avoid hanging
    HTTP_CODE=$(curl -s --max-time 15 -o /dev/null -w "%{http_code}" --connect-timeout 5 -X POST "${BACKEND_URL}/api/v1/proxy/heartbeat" \
        -H "Content-Type: application/json" \
        -d "{\"podName\": \"${POD_NAME}\", \"healthStatus\": \"$1\", \"podIp\": \"${POD_IP}\", \"externalIp\": \"${EXTERNAL_IP:-}\", \"detectedCountry\": \"${DETECTED_COUNTRY:-}\", \"detectedCity\": \"${DETECTED_CITY:-}\"}")
    
    if [ "$HTTP_CODE" = "404" ]; then
        echo "!!! Lease lost or node deleted (404). Exiting pod to trigger re-lease."
        sleep 30
        exit 1
    fi
}

cleanup() {
    echo "Terminating. Releasing lease..."
    curl -sf --max-time 15 --connect-timeout 5 -X DELETE "${BACKEND_URL}/api/v1/proxy/lease/${POD_NAME}" > /dev/null 2>&1
    exit 0
}
trap cleanup TERM INT QUIT

# Initial heartbeat to mark as joined
send_heartbeat "UNKNOWN"

while true; do
    # Try Geo IP detection inside the loop, don't let it block heartbeats
    if [ -z "$EXTERNAL_IP" ]; then
        GEO_RESPONSE=$(curl -sf --max-time 15 --proxy "http://127.0.0.1:3128" --connect-timeout 5 "https://ipinfo.io/json" 2>/dev/null || echo "")
        if [ -n "$GEO_RESPONSE" ]; then
            EXTERNAL_IP=$(echo "$GEO_RESPONSE" | grep -o '"ip":"[^"]*"' | sed 's/"ip":"//;s/"//')
            DETECTED_COUNTRY=$(echo "$GEO_RESPONSE" | grep -o '"country":"[^"]*"' | sed 's/"country":"//;s/"//')
            DETECTED_CITY=$(echo "$GEO_RESPONSE" | grep -o '"city":"[^"]*"' | sed 's/"city":"//;s/"//')
            echo "Detected location: ${DETECTED_CITY}, ${DETECTED_COUNTRY} (${EXTERNAL_IP})"
        fi
    fi

    HTTP_CODE=$(curl -sf --max-time 15 -o /dev/null -w "%{http_code}" --proxy "http://127.0.0.1:3128" --connect-timeout 10 "${TEST_URL}" 2>/dev/null || echo "0")
    if [ "$HTTP_CODE" -gt 0 ] 2>/dev/null; then
        CONSECUTIVE_FAILURES=0
        send_heartbeat "HEALTHY"
    else
        CONSECUTIVE_FAILURES=$((CONSECUTIVE_FAILURES + 1))
        echo "Healthcheck failure ($CONSECUTIVE_FAILURES/$MAX_FAILURES) for ${TEST_URL}"
        if [ "$CONSECUTIVE_FAILURES" -ge "$MAX_FAILURES" ]; then
            send_heartbeat "DEAD"
        else
            send_heartbeat "DEGRADED"
        fi
    fi
    
    sleep "${HEARTBEAT_INTERVAL}" &
    wait $!
done
