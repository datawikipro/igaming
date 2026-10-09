#!/bin/bash
set -e
echo "=== 3. Extracting Surebet files ==="
mkdir -p /root/igaming/aggregator-surebet
tar -xzf /tmp/surebet_patch.tar.gz -C /root/igaming/aggregator-surebet

echo "=== 4. Compiling Surebet with Maven in Podman ==="
ionice -c 2 -n 7 podman run --rm   -v /root/.m2:/root/.m2   -v /root/igaming:/workspace   -w /workspace   docker.io/library/maven:3.9-eclipse-temurin-21   mvn clean package -pl aggregator-surebet -am -DskipTests

JAR_FILE="/root/igaming/aggregator-surebet/target/aggregator-surebet-0.0.2-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: $JAR_FILE not found!"
    exit 1
fi
echo "Found jar: $JAR_FILE ($(stat -c%s $JAR_FILE) bytes)"

echo "=== 5. Building container ==="
BUILD_DIR="/data/tmp/build_surebet"
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"
cp "$JAR_FILE" "$BUILD_DIR/app.jar"

cat << 'EOF' > "$BUILD_DIR/Dockerfile"
FROM docker.io/library/eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

LOCAL_TAG="igaming-aggregator-surebet:latest"
REG_TAG="100.78.183.101:30500/igaming-aggregator-surebet:latest"
GHCR_TAG="ghcr.io/datawikipro/igaming-aggregator-surebet:latest"

ionice -c 2 -n 7 podman build -t "$LOCAL_TAG" -t "$REG_TAG" -t "$GHCR_TAG" "$BUILD_DIR"

TAR_FILE="/data/tmp/igaming-aggregator-surebet.tar"
skopeo copy "containers-storage:$GHCR_TAG" "docker-archive:$TAR_FILE:$GHCR_TAG"
ctr -n k8s.io images import "$TAR_FILE"
rm -f "$TAR_FILE"

skopeo copy "containers-storage:$REG_TAG" "docker-archive:$TAR_FILE:$REG_TAG"
ctr -n k8s.io images import "$TAR_FILE"
rm -f "$TAR_FILE"
rm -rf "$BUILD_DIR"

echo "=== 6. Restarting Surebet in K8s ==="
kubectl rollout restart deployment igaming-aggregator-surebet -n igaming-dev

echo "=== Surebet deployment successfully restarted! ==="
