#!/usr/bin/env bash
set -euo pipefail

cd /data/igaming
tar -xzf aggregator_fixes.tar.gz

MVN=$(command -v mvn || echo /usr/local/bin/mvn)
echo "Using Maven: $MVN"

echo "=== 1. Building igaming-dto & aggregator-domain ==="
timeout 180s $MVN -B -f igaming-dto/pom.xml clean install -DskipTests
timeout 180s $MVN -B -f aggregator-domain/pom.xml clean install -DskipTests

for mod in aggregator-ingestion aggregator-normalizer aggregator-surebet aggregator-api; do
    img="100.78.183.101:30500/igaming-${mod}:latest"
    echo "=== 2. Building & pushing ${mod} -> ${img} ==="
    timeout 240s $MVN -B -f "${mod}/pom.xml" compile com.google.cloud.tools:jib-maven-plugin:3.4.1:build \
        -Djib.from.image=gcr.io/distroless/java21-debian12 \
        -Djib.to.image="${img}" \
        -Djib.allowInsecureRegistries=true \
        -DskipTests
done

echo "=== 3. Updating aggregator-ingestion-config ConfigMap ==="
kubectl get cm aggregator-ingestion-config -n igaming-dev -o json | python3 -c '
import sys, json
cm = json.load(sys.stdin)
props = cm["data"]["application.properties"]
props = props.replace("app.aggregator.features.fuzzy-matching-enabled=false", "app.aggregator.features.fuzzy-matching-enabled=${FUZZY_MATCHING_ENABLED:true}")
props = props.replace("/api/v1/llm/generate", "/api/v1/igaming/generate")
cm["data"]["application.properties"] = props
for k in ["resourceVersion", "uid", "creationTimestamp", "managedFields"]:
    cm["metadata"].pop(k, None)
json.dump(cm, sys.stdout)
' | kubectl apply -f -

echo "=== 4. Rolling out aggregator deployments in igaming-dev ==="
kubectl set image -n igaming-dev \
    deploy/igaming-aggregator-ingestion=100.78.183.101:30500/igaming-aggregator-ingestion:latest \
    deploy/igaming-aggregator-normalizer=100.78.183.101:30500/igaming-aggregator-normalizer:latest \
    deploy/igaming-aggregator-surebet=100.78.183.101:30500/igaming-aggregator-surebet:latest \
    deploy/igaming-aggregator-api=100.78.183.101:30500/igaming-aggregator-api:latest || true

for dep in igaming-aggregator-ingestion igaming-aggregator-normalizer igaming-aggregator-surebet igaming-aggregator-api; do
    kubectl rollout restart -n igaming-dev "deploy/${dep}"
done

for dep in igaming-aggregator-ingestion igaming-aggregator-normalizer igaming-aggregator-surebet igaming-aggregator-api; do
    echo "Waiting for ${dep}..."
    kubectl rollout status -n igaming-dev "deploy/${dep}" --timeout=180s
done

echo "=== ALL AGGREGATOR DEPLOYMENTS ROLLED OUT SUCCESSFULLY ==="
