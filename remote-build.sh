#!/bin/bash
set -e
MODULE=$1
BASE_DIR="$HOME/igaming"

cd "$BASE_DIR"

# Ensure git auth works
gh auth setup-git

# Update core components
echo "Updating DTO and Core..."
cd "$BASE_DIR/igaming-dto" && git pull && mvn install -DskipTests
cd "$BASE_DIR/igaming-source-core" && git pull && mvn install -DskipTests

if [ "$MODULE" == "all" ]; then
  echo "Full build not implemented in this script yet. Use specific modules."
  exit 1
fi

echo "Updating module $MODULE..."
cd "$BASE_DIR/$MODULE"
git pull

JIB_SERVICES=("igaming-aggregator" "igaming-bot" "igaming-portal")

if [[ " ${JIB_SERVICES[@]} " =~ " ${MODULE} " ]]; then
  echo "Building Jib service $MODULE..."
  mvn compile jib:dockerBuild -DskipTests
else
  echo "Packaging $MODULE..."
  mvn package -DskipTests
  echo "Building Docker image for $MODULE..."
  IMAGE_TAG="ghcr.io/datawikipro/$MODULE:latest"
  docker build -t "$IMAGE_TAG" .
fi

echo "Build and Image creation for $MODULE: SUCCESS"
