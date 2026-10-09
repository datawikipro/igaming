#!/bin/bash
set -e
echo "=== 1. Extracting patch ==="
tar -xzf /root/igaming/surebet_patch.tar.gz -C /root/igaming/

echo "=== 2. Installing aggregator-domain ==="
/usr/local/bin/mvn -f /root/igaming/aggregator-domain/pom.xml install -DskipTests -B

echo "=== 3. Building & Pushing aggregator-surebet with Jib ==="
/usr/local/bin/mvn -f /root/igaming/aggregator-surebet/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build \
  -Djib.from.image=gcr.io/distroless/java21-debian12 \
  -Djib.to.image=100.78.183.101:30500/igaming-aggregator-surebet:latest \
  -Djib.allowInsecureRegistries=true -DskipTests -B

echo "=== 4. Rolling out restart in k8s ==="
kubectl rollout restart deployment igaming-aggregator-surebet -n igaming-dev

echo "=== 5. Waiting for rollout ==="
kubectl rollout status deployment igaming-aggregator-surebet -n igaming-dev --timeout=120s
echo "=== Done! ==="
