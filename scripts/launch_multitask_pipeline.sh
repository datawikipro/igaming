#!/bin/bash
set -e

echo "=========================================================================="
echo " 🚀 SMARTBET.GURU / iGAMING 5-SPORTS MULTI-TASK NEURAL PIPELINE"
echo " Sports: Table Tennis, Tennis, Basketball, Ice Hockey, Football"
echo " Targets: 14-16 Tactical Parameters per Sport (MTL Residual Architecture)"
echo "=========================================================================="

WORKDIR="/opt/sofascore-harvester"
PYTHON="$WORKDIR/venv/bin/python3"
if [ ! -f "$PYTHON" ]; then
    PYTHON="python3"
fi

CACHE_DIR="/data/cold-storage/sofascore/cache"
MODEL_DIR="/data/cold-storage/sofascore/models"
mkdir -p "$CACHE_DIR" "$MODEL_DIR"

export CACHE_DIR
export MODEL_DIR
export OMP_NUM_THREADS=16

echo ""
echo "=== STEP 0/3: VERIFYING / EXTRACTING LINEUPS & MEDICAL PROTOCOLS CACHE ==="
if [ ! -f "$CACHE_DIR/lineups_v3_cache.json.gz" ]; then
    $PYTHON $WORKDIR/extract_lineups_cache.py
else
    echo " [OK] Found precomputed lineups cache: $CACHE_DIR/lineups_v3_cache.json.gz"
fi

echo ""
echo "=== STEP 1/3: EXTRACTING MULTI-TASK MATRICES (5 SPORTS, 291 DIMS) ==="
for SPORT in ice-hockey basketball tennis table-tennis football; do
    echo ">>> Extracting Sport: $SPORT <<<"
    $PYTHON $WORKDIR/prepare_multitask_matrix.py --sport $SPORT
done

echo ""
echo "=== STEP 2/3: TRAINING MULTI-TASK RESIDUAL NETWORKS & LEADERBOARDS ==="
for SPORT in ice-hockey basketball tennis table-tennis football; do
    echo ">>> Training Sport: $SPORT <<<"
    $PYTHON $WORKDIR/train_multitask_matrix_gpu.py --sport $SPORT --epochs 15
done

echo ""
echo "=== STEP 3/3: RESTARTING INFERENCE SERVICE ==="
systemctl restart igaming-ml-inference || true
sleep 3
curl -s http://127.0.0.1:8085/health | jq . || curl -s http://127.0.0.1:8085/health

echo ""
echo "=========================================================================="
echo " 🎉 ALL 5 SPORTS MULTI-TASK MODELS SUCCESSFULLY TRAINED & ACTIVATED!"
echo "=========================================================================="
