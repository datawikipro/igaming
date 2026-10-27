#!/usr/bin/env python3
"""
test_full_scenario_simulation.py
Simulates a pre-match evaluation with 9-scenario cross-product inference in PyTorch.
"""

import gzip
import json
import time
import torch
import torch.nn as nn
import numpy as np

import sys
sys.path.append("/opt/sofascore-harvester")
sys.path.append("/tmp")

from ml_inference_service import MultiTaskTacticalNetV2, extract_runtime_lineup_and_injury_features
from test_projected_lineup_engine import build_team_projected_scenarios

print("Loading model and team profiles...")
ckpt = torch.load("/data/cold-storage/sofascore/models/football_tactical_net_best.pt", map_location="cpu", weights_only=False)
model = MultiTaskTacticalNetV2(291, 16)
model.load_state_dict(ckpt["model_state_dict"])
model.eval()

with gzip.open('/data/cold-storage/sofascore/cache/team_lineup_profiles.json.gz', 'rt', encoding='utf-8') as f:
    PROFILES = json.load(f)
dummy_macro = [0.5] * 259

scenarios_h, prof_h = build_team_projected_scenarios("Inter Milan and the Italian national football team")
scenarios_a, prof_a = build_team_projected_scenarios("Arsenal (Maestro)")

print(f"Team 1 ({prof_h['team_name']}): {len(scenarios_h)} scenarios")
print(f"Team 2 ({prof_a['team_name']}): {len(scenarios_a)} scenarios")

# Build Cross-Product Matrix of 3 x 3 = 9 Scenarios
scenario_vectors = []
scenario_labels = []

t0 = time.perf_counter()
for s_h in scenarios_h:
    for s_a in scenarios_a:
        label = f"{s_h['name']} vs {s_a['name']}"
        l_feats = extract_runtime_lineup_and_injury_features(
            s_h["starters"], s_a["starters"], s_h.get("bench", []), s_a.get("bench", [])
        )
        full_vec = np.array(dummy_macro + l_feats, dtype=np.float32)
        scenario_vectors.append(full_vec)
        scenario_labels.append(label)

batch_tensor = torch.from_numpy(np.array(scenario_vectors)).float()
prep_ms = (time.perf_counter() - t0) * 1000

# Vectorized PyTorch GEMM
t0 = time.perf_counter()
with torch.no_grad():
    raw_logits = model(batch_tensor)
    probs = torch.sigmoid(raw_logits).numpy()
infer_ms = (time.perf_counter() - t0) * 1000

print(f"\n⚡ Performance: Feature prep = {prep_ms:.2f}ms | Pure PyTorch batch({len(scenario_vectors)}, 291) inference = {infer_ms:.2f}ms")

# Analyze 1X2 Probabilities across all 9 scenarios
p1_scores = probs[:, 0]  # winner_1x2_p1
px_scores = probs[:, 1]  # winner_1x2_px
p2_scores = probs[:, 2]  # winner_1x2_p2
u35_scores = probs[:, 9] # total_goals_under_3_5

print("\n--- 📊 MULTI-SCENARIO LINEUP SIMULATION RESULTS (9 SCENARIOS) ---")
for i, label in enumerate(scenario_labels):
    print(f" Scenario {i+1:2d} [{label:32}]: P1={p1_scores[i]*100:4.1f}% | PX={px_scores[i]*100:4.1f}% | P2={p2_scores[i]*100:4.1f}% | Under3.5={u35_scores[i]*100:4.1f}%")

p1_mean = float(np.mean(p1_scores))
p1_std = float(np.std(p1_scores))
u35_mean = float(np.mean(u35_scores))
resilience_u35 = float(np.mean(u35_scores >= 0.70) * 100)

print(f"\n🎯 Tactical Resilience Analysis:")
print(f" * Primary Under 3.5 Consensus: {u35_mean*100:.1f}% (Resilience: {resilience_u35:.1f}% across all 9 squad rotations)")
print(f" * Home Win Sensitivity: Mean = {p1_mean*100:.1f}% ± {p1_std*100:.1f}% (Min: {np.min(p1_scores)*100:.1f}%, Max: {np.max(p1_scores)*100:.1f}%)")
