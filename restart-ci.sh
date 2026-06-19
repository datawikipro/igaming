#!/usr/bin/env bash
# restart-ci.sh
# Monorepo CI/CD: Build changed modules + Push Docker images + K8s Restart
# Usage: ./restart-ci.sh              (build all)
#        ./restart-ci.sh --only leon  (build only igaming-source-leon)
#        ./restart-ci.sh --only base  (build and push runtime base image)
#        ./restart-ci.sh --only build-base (build and push maven dependency cache image)

set -e

ONLY=""
PARALLEL=4

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --only) ONLY="$2"; shift ;;
        --parallel) PARALLEL="$2"; shift ;;
        *) echo "Unknown parameter passed: $1"; exit 1 ;;
    esac
    shift
done

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PODMAN_CMD="podman --root ~/.local/share/containers-vfs --runroot /run/user/1000/containers-vfs --storage-driver vfs"

echo ""
echo -e "\033[0;36m=== iGaming Monorepo CI ===\033[0m"
echo ""

# ---------------------------------------------------------------
# 0. Git Sync & Branch Detection
# ---------------------------------------------------------------
echo -e "\033[0;36m[Phase 0] Syncing with Git (including submodules)...\033[0m"

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ $? -ne 0 ]; then
    echo "Could not detect Git branch"
    exit 1
fi
echo -e "\033[1;30m  > Current branch: $CURRENT_BRANCH\033[0m"

# Sync all submodules
git submodule foreach --recursive "git add . && (git diff-index --quiet HEAD || git commit -m 'ci: auto-sync local changes' --quiet) && (git cherry 2>/dev/null | grep -q '^+') && git push origin HEAD --quiet 2>/dev/null || true" >/dev/null 2>&1 || true

# Sync parent repo
if ! git status --porcelain | grep -q "^"; then
    # clean
    true
else
    git add . >/dev/null 2>&1 || true
    git commit -m "ci: auto-sync before remote build" --quiet >/dev/null 2>&1 || true
fi

git push origin "$CURRENT_BRANCH" --quiet >/dev/null 2>&1 || {
    echo -e "\033[0;33mWARNING: Git sync/push failed\033[0m"
    echo -e "\033[0;33mProceeding with remote build using existing upstream commits...\033[0m"
}

# ---------------------------------------------------------------
# 1. Ensure GHCR authentication on remote server
# ---------------------------------------------------------------
echo -e "\n\033[0;36m[Phase 1] Authenticating...\033[0m"
GH_TOKEN=$(env -u GITHUB_TOKEN gh auth token 2>/dev/null || true)
if [ -z "$GH_TOKEN" ]; then
    echo -e "\033[0;33mWARNING: Cannot get GitHub token. Proceeding with cached credentials...\033[0m"
else
    echo -e "\033[1;30m  > Logging in to GHCR on remote server...\033[0m"
    if ! ssh chernousov_a@100.89.122.84 "echo '$GH_TOKEN' | $PODMAN_CMD login ghcr.io -u datawikipro --password-stdin 2>&1" >/dev/null 2>&1; then
        echo -e "\033[0;33mWARNING: Remote Docker GHCR login failed. Proceeding anyway using cached credentials...\033[0m"
    else
        echo -e "\033[1;30m  > Remote GHCR login: OK\033[0m"
    fi
fi

# ---------------------------------------------------------------
# 1.5 Build and push base image (if requested)
# ---------------------------------------------------------------
if [ "$ONLY" == "base" ]; then
    echo -e "\033[0;36m[Phase 1.5] Building and pushing base image...\033[0m"
    cd "$ROOT_DIR/igaming-source-core"
    if ! docker build -t ghcr.io/datawikipro/igaming-source-base:latest -f Dockerfile.base .; then
        echo -e "\033[0;31mFATAL: Base image build failed!\033[0m"
        exit 1
    fi
    if ! docker push ghcr.io/datawikipro/igaming-source-base:latest; then
        echo -e "\033[0;31mFATAL: Base image push failed!\033[0m"
        exit 1
    fi
    cd "$ROOT_DIR"
    echo -e "\033[0;32m  Base image: OK\033[0m"
    exit 0
fi

# ---------------------------------------------------------------
# 1.6 Build and push build-base image (if requested)
# ---------------------------------------------------------------
if [ "$ONLY" == "build-base" ]; then
    echo -e "\033[0;36m[Phase 1.6] Building and pushing Maven build-base image...\033[0m"
    IMAGE_TAG="ghcr.io/datawikipro/igaming-build-base:latest"
    REMOTE_PATH="build/igaming"
    REMOTE_CMD="cd $REMOTE_PATH && git fetch origin && git checkout $CURRENT_BRANCH && git pull origin $CURRENT_BRANCH && $PODMAN_CMD build -f Dockerfile.build-base -t $IMAGE_TAG . && $PODMAN_CMD push $IMAGE_TAG"
    
    echo -e "\033[1;30m  > Building build-base on remote server...\033[0m"
    if ! ssh chernousov_a@100.89.122.84 "$REMOTE_CMD"; then
        echo -e "\033[0;31mFATAL: Build-base image build/push failed!\033[0m"
        exit 1
    fi
    echo -e "\033[0;32m  Build-base image: OK\033[0m"
    exit 0
fi

# ---------------------------------------------------------------
# 2. Determine which modules to build
# ---------------------------------------------------------------
JVM_SERVICES=("aggregator-ingestion" "aggregator-normalizer" "aggregator-api" "aggregator-surebet" "aggregator-odds-sync" "aggregator-enrichment" "igaming-bot" "igaming-portal" "igaming-admin-backend" "igaming-llm-gateway" "igaming-llm-admin" "igaming-llm-worker" "service-proxy-backend" "igaming-auth-microservice" "igaming-capture-sofascore" "igaming-capture-liveresult")

CRAWLER_SERVICES=()
for dir in "$ROOT_DIR"/igaming-source-*/; do
    if [ -d "$dir" ]; then
        dir_name=$(basename "$dir")
        if [ "$dir_name" != "igaming-source-core" ]; then
            CRAWLER_SERVICES+=("$dir_name")
        fi
    fi
done

ALL_MODULES=()

if [ -n "$ONLY" ]; then
    IFS=',' read -ra ADDR <<< "$ONLY"
    for part in "${ADDR[@]}"; do
        part=$(echo "$part" | xargs)
        [ -z "$part" ] && continue
        MATCH=""
        if [[ " ${JVM_SERVICES[@]} " =~ " ${part} " ]] || [[ " ${CRAWLER_SERVICES[@]} " =~ " ${part} " ]]; then
            MATCH="$part"
        elif [[ " ${JVM_SERVICES[@]} " =~ " igaming-${part} " ]]; then
            MATCH="igaming-$part"
        elif [[ " ${CRAWLER_SERVICES[@]} " =~ " igaming-source-${part} " ]]; then
            MATCH="igaming-source-$part"
        else
            MATCH="igaming-source-$part"
        fi
        ALL_MODULES+=("$MATCH")
    done
    echo -e "\033[0;33m[Phase 2] Building modules: ${ALL_MODULES[*]}\033[0m"
else
    ALL_MODULES=("${JVM_SERVICES[@]}" "${CRAWLER_SERVICES[@]}")
    echo -e "\033[0;36m[Phase 2] Building ${#ALL_MODULES[@]} modules (parallel=$PARALLEL)...\033[0m"
fi

# ---------------------------------------------------------------
# 3. Build & Push Docker images (all via remote server SSH)
# ---------------------------------------------------------------
echo ""
echo -e "\033[0;36m[Phase 3] Building and pushing images...\033[0m"

SUCCESS=()
FAILED=()

for module in "${ALL_MODULES[@]}"; do
    DOCKERFILE="$module/Dockerfile"
    IMAGE_NAME="$module"
    
    if [[ "$module" == aggregator-* ]]; then
        IMAGE_NAME="igaming-$module"
    fi
    
    IMAGE_TAG="ghcr.io/datawikipro/${IMAGE_NAME}:latest"
    REMOTE_PATH="build/igaming"
    REMOTE_CMD="cd $REMOTE_PATH && git fetch origin master -q && git reset --hard FETCH_HEAD -q && git submodule sync --recursive -q 2>/dev/null ; git submodule update --init --recursive --force -q 2>/dev/null ; $PODMAN_CMD build -f $DOCKERFILE -t $IMAGE_TAG . && $PODMAN_CMD push $IMAGE_TAG"
    
    echo -e "\033[1;30m  > [$module] Building and pushing on remote server using Dockerfile $DOCKERFILE...\033[0m"
    
    set +e
    ssh -o ServerAliveInterval=15 -o ServerAliveCountMax=3 chernousov_a@100.89.122.84 "$REMOTE_CMD"
    RET=$?
    set -e
    
    if [ $RET -ne 0 ]; then
        echo -e "\033[0;31m  [$module] FAIL\033[0m"
        FAILED+=("$module")
    else
        echo -e "\033[0;32m  [$module] OK\033[0m"
        SUCCESS+=("$module")
    fi
done

# ---------------------------------------------------------------
# 4. Kubernetes rollout restart
# ---------------------------------------------------------------
echo ""
echo -e "\033[0;36m[Phase 4] Kubernetes rollout restart...\033[0m"

KUBECTL_CMD="kubectl"

for module in "${SUCCESS[@]}"; do
    if [[ "$module" == igaming-source-* ]]; then
        echo -e "\033[1;30m  > [$module] Staggered family restart (waiting 10s)...\033[0m"
        
        DEPLOYMENT_PREFIXES=()
        if [ "$module" == "igaming-source-fon-bet-ru" ]; then
            DEPLOYMENT_PREFIXES=("igaming-source-fon-bet-ru" "igaming-source-fon-bet-by" "igaming-source-fon-bet-kz" "igaming-source-pari" "igaming-source-bettery")
        elif [ "$module" == "igaming-source-marathonbet" ]; then
            DEPLOYMENT_PREFIXES=("igaming-source-marathonbet" "igaming-source-marathonbet-com" "igaming-source-marathonbet-by")
        elif [ "$module" == "igaming-source-olimpbet" ]; then
            DEPLOYMENT_PREFIXES=("igaming-source-olimpbet" "igaming-source-olimpbet-kz")
        elif [ "$module" == "igaming-source-betcity" ]; then
            DEPLOYMENT_PREFIXES=("igaming-source-betcity" "igaming-source-betcity-com" "igaming-source-betm")
        elif [ "$module" == "igaming-source-betb2b" ]; then
            DEPLOYMENT_PREFIXES=("igaming-source-1xbet" "igaming-source-melbet" "igaming-source-megapari" "igaming-source-linebet" "igaming-source-betandyou" "igaming-source-fansport" "igaming-source-888starz" "igaming-source-22bet" "igaming-source-betwinner" "igaming-source-melbet-com" "igaming-source-1xbit")
        elif [ "$module" == "igaming-source-888sport" ]; then
            DEPLOYMENT_PREFIXES=("igaming-source-888sport" "igaming-source-leovegas" "igaming-source-mrgreen" "igaming-source-betplay")
        elif [ "$module" == "igaming-source-betsson" ]; then
            DEPLOYMENT_PREFIXES=("igaming-source-betsson" "igaming-source-betsafe" "igaming-source-nordicbet")
        else
            DEPLOYMENT_PREFIXES=("$module")
        fi
        
        for prefix in "${DEPLOYMENT_PREFIXES[@]}"; do
            echo -e "\033[1;30m    Restarting K8s deployment: $prefix...\033[0m"
            $KUBECTL_CMD rollout restart deployment "$prefix-crawler" -n igaming-dev >/dev/null 2>&1 || true
            $KUBECTL_CMD rollout restart deployment "$prefix-loader" -n igaming-dev >/dev/null 2>&1 || true
        done
        sleep 10
    else
        echo -e "\033[1;30m  > [$module] Restarting service...\033[0m"
        NS="igaming-dev"
        DEPLOY_NAME="$module"
        
        if [[ "$module" == aggregator-* ]]; then
            DEPLOY_NAME="igaming-$module"
        fi
        
        if [[ "$module" == igaming-llm-* ]]; then
            NS="llm"
            DEPLOY_NAME="${module/igaming-llm-/llm-}"
            $KUBECTL_CMD rollout restart deployment "$DEPLOY_NAME" -n "$NS" >/dev/null 2>&1 || true
        elif [[ "$module" == "service-proxy-backend" ]]; then
            NS="service-proxy"
            $KUBECTL_CMD rollout restart deployment "$DEPLOY_NAME" -n "$NS" >/dev/null 2>&1 || true
        elif [[ "$module" == "igaming-bot" ]]; then
            $KUBECTL_CMD rollout restart deployment "igaming-bot-telegram" -n "$NS" >/dev/null 2>&1 || true
            $KUBECTL_CMD rollout restart deployment "igaming-bot-threads" -n "$NS" >/dev/null 2>&1 || true
        else
            $KUBECTL_CMD rollout restart deployment "$DEPLOY_NAME" -n "$NS" >/dev/null 2>&1 || true
        fi
    fi
    echo -e "\033[1;30m  [$module] restarted\033[0m"
done

# ---------------------------------------------------------------
# Summary
# ---------------------------------------------------------------
echo ""
echo -e "\033[0;36m==============================\033[0m"
echo -e "\033[0;32m  Success: ${#SUCCESS[@]}\033[0m"
if [ ${#FAILED[@]} -gt 0 ]; then
    echo -e "\033[0;31m  Failed:  ${#FAILED[@]}\033[0m"
    echo -e "\033[0;31m  Failed modules:\033[0m"
    for f in "${FAILED[@]}"; do
        echo -e "\033[0;31m    - $f\033[0m"
    done
else
    echo -e "\033[0;32m  Failed:  0\033[0m"
fi
echo -e "\033[0;36m==============================\033[0m"
