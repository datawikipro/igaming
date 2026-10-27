#!/usr/bin/env python3
"""
SmartBet.guru / iGaming Institutional Tactical Neural Network
High-Throughput Multi-Task Training Engine (GPU / CPU Auto-Detection)
Trains 14-16 tactical parameters simultaneously per sport with Focal Multi-Head Loss.
Generates an objective Predictability Leaderboard for institutional betting edges.
"""

import os
import sys
import time
import json
import math
from typing import List, Dict, Any, Tuple

import numpy as np
import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
from torch.utils.data import TensorDataset, DataLoader

# ============================================================================
# Hardware Resolution (RTX 4090 / A100 GPU with CPU Xeon Fallback)
# ============================================================================

DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
IS_CUDA = DEVICE.type == "cuda"
if not IS_CUDA:
    torch.set_num_threads(int(os.environ.get("OMP_NUM_THREADS", "8")))

CACHE_DIR = os.environ.get("CACHE_DIR", "/data/cold-storage/sofascore/cache")
MODEL_DIR = os.environ.get("MODEL_DIR", "/data/cold-storage/sofascore/models")

# ============================================================================
# Neural Network Architecture: MultiTaskTacticalNetV2
# ============================================================================

class ResidualBlock(nn.Module):
    def __init__(self, dim: int, dropout: float = 0.2):
        super().__init__()
        self.block = nn.Sequential(
            nn.Linear(dim, dim),
            nn.LayerNorm(dim),
            nn.SiLU(),
            nn.Dropout(dropout),
            nn.Linear(dim, dim),
            nn.LayerNorm(dim)
        )
        self.act = nn.SiLU()

    def forward(self, x):
        return self.act(x + self.block(x))


class MultiTaskTacticalNetV2(nn.Module):
    """
    State-of-the-art Multi-Task Backbone with High-Throughput Vectorized Output.
    Shared representation captures physical sport dynamics, while the dense projection
    predicts all 14-16 specialized betting markets in a single BLAS GEMM matrix multiply.
    """
    def __init__(self, input_dim: int, num_targets: int, hidden_dim: int = 256, num_blocks: int = 4, dropout: float = 0.25):
        super().__init__()
        self.num_targets = num_targets

        # Input Normalization & Dense Projection
        self.input_layer = nn.Sequential(
            nn.BatchNorm1d(input_dim),
            nn.Linear(input_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.SiLU(),
            nn.Dropout(dropout)
        )

        # Deep Residual Feature Extractor
        self.res_blocks = nn.ModuleList([
            ResidualBlock(hidden_dim, dropout=dropout) for _ in range(num_blocks)
        ])

        # High-Throughput Vectorized Multi-Target Output Layer
        self.head = nn.Sequential(
            nn.Linear(hidden_dim, 128),
            nn.SiLU(),
            nn.Dropout(0.15),
            nn.Linear(128, num_targets)
        )

    def forward(self, x):
        h = self.input_layer(x)
        for block in self.res_blocks:
            h = block(h)
        return self.head(h)


class FocalBinaryLoss(nn.Module):
    """Focal loss down-weights easy examples to focus on tough tactical edges."""
    def __init__(self, gamma: float = 1.5):
        super().__init__()
        self.gamma = gamma

    def forward(self, logits, targets):
        bce = F.binary_cross_entropy_with_logits(logits, targets, reduction="none")
        probs = torch.sigmoid(logits)
        pt = targets * probs + (1.0 - targets) * (1.0 - probs)
        focal_weight = (1.0 - pt) ** self.gamma
        return (focal_weight * bce).mean()

# ============================================================================
# Training & Evaluation Engine
# ============================================================================

def train_sport_multitask(sport: str, epochs: int = 5, batch_size: int = 4096, max_samples: int = 250000):
    print("\n" + "=" * 80)
    print(f" 🚀 MULTI-TASK TRAINING PIPELINE: {sport.upper()} ON {DEVICE.type.upper()}")
    print("=" * 80, flush=True)

    x_path = os.path.join(CACHE_DIR, f"{sport}_multitask_features_X.npy")
    y_path = os.path.join(CACHE_DIR, f"{sport}_multitask_targets_Y.npy")
    meta_path = os.path.join(CACHE_DIR, f"{sport}_targets_meta.json")

    if not (os.path.exists(x_path) and os.path.exists(y_path)):
        print(f" [Error] Precomputed matrix files not found for {sport} in {CACHE_DIR}!")
        return

    X = np.load(x_path)
    Y = np.load(y_path)

    with open(meta_path, "r", encoding="utf-8") as f:
        targets_meta = json.load(f)

    num_samples, input_dim = X.shape
    num_targets = Y.shape[1]

    if max_samples and num_samples > max_samples:
        print(f" Subsampling most recent {max_samples:,} of {num_samples:,} samples for high-velocity convergence...", flush=True)
        X = X[-max_samples:]
        Y = Y[-max_samples:]
        num_samples = max_samples

    split_idx = int(num_samples * 0.85)

    print(f" Loaded {num_samples:,} matches across {num_targets} tactical targets:")
    print(f"   * Train Samples: {split_idx:,} | Validation Samples: {num_samples - split_idx:,}")
    print(f"   * Feature Dimensions: {input_dim}")
    print(f"   * Targets: {', '.join([t['name'] for t in targets_meta])}\n", flush=True)

    train_ds = TensorDataset(torch.from_numpy(X[:split_idx]), torch.from_numpy(Y[:split_idx]))
    val_ds = TensorDataset(torch.from_numpy(X[split_idx:]), torch.from_numpy(Y[split_idx:]))

    bs = batch_size if IS_CUDA else min(4096, batch_size)
    train_loader = DataLoader(train_ds, batch_size=bs, shuffle=True, drop_last=True, pin_memory=IS_CUDA)
    val_loader = DataLoader(val_ds, batch_size=bs * 2, shuffle=False, pin_memory=IS_CUDA)

    model = MultiTaskTacticalNetV2(input_dim=input_dim, num_targets=num_targets, hidden_dim=256, num_blocks=4).to(DEVICE)
    criterion = FocalBinaryLoss(gamma=1.5)
    optimizer = optim.AdamW(model.parameters(), lr=0.003, weight_decay=1e-4)
    scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=epochs, eta_min=1e-5)
    scaler = torch.amp.GradScaler("cuda", enabled=IS_CUDA)

    os.makedirs(MODEL_DIR, exist_ok=True)
    best_path = os.path.join(MODEL_DIR, f"{sport}_tactical_net_best.pt")
    best_score = -float("inf")

    t_sport_start = time.time()

    for epoch in range(1, epochs + 1):
        ep_t0 = time.time()
        model.train()
        train_loss = 0.0

        for X_b, Y_b in train_loader:
            X_b = X_b.to(DEVICE, non_blocking=True)
            Y_b = Y_b.to(DEVICE, non_blocking=True)

            optimizer.zero_grad(set_to_none=True)
            with torch.amp.autocast("cuda", enabled=IS_CUDA):
                logits = model(X_b)
                loss = criterion(logits, Y_b)

            scaler.scale(loss).backward()
            scaler.step(optimizer)
            scaler.update()
            train_loss += loss.item() * len(X_b)

        scheduler.step()
        train_loss /= len(train_ds)

        # ── Comprehensive Multi-Target Validation ─────────────────────────────
        model.eval()
        all_probs = []
        all_targets = []

        with torch.no_grad():
            for X_b, Y_b in val_loader:
                X_b = X_b.to(DEVICE, non_blocking=True)
                with torch.amp.autocast("cuda", enabled=IS_CUDA):
                    logits = model(X_b)
                    probs = torch.sigmoid(logits)
                all_probs.append(probs.cpu().numpy())
                all_targets.append(Y_b.numpy())

        P_val = np.concatenate(all_probs, axis=0)
        T_val = np.concatenate(all_targets, axis=0)

        # Evaluate metrics per target
        target_metrics = []
        score_accumulator = 0.0

        for t_idx in range(num_targets):
            p_col = P_val[:, t_idx]
            y_col = T_val[:, t_idx]

            # Overall Accuracy
            preds = (p_col >= 0.5).astype(np.float32)
            acc_overall = np.mean(preds == y_col) * 100.0

            # High Confidence Cohort (P >= 0.80 for YES or P <= 0.20 for NO)
            mask_high = (p_col >= 0.80) | (p_col <= 0.20)
            n_high = int(np.sum(mask_high))
            if n_high > 0:
                high_preds = (p_col[mask_high] >= 0.5).astype(np.float32)
                acc_high = np.mean(high_preds == y_col[mask_high]) * 100.0
            else:
                acc_high = 0.0

            # 70-80% Confidence Cohort
            mask_mid = ((p_col >= 0.70) & (p_col < 0.80)) | ((p_col > 0.20) & (p_col <= 0.30))
            n_mid = int(np.sum(mask_mid))
            acc_mid = np.mean((p_col[mask_mid] >= 0.5).astype(np.float32) == y_col[mask_mid]) * 100.0 if n_mid > 0 else 0.0

            target_metrics.append({
                "id": t_idx,
                "name": targets_meta[t_idx]["name"],
                "desc": targets_meta[t_idx]["desc"],
                "acc_overall": round(acc_overall, 1),
                "acc_high": round(acc_high, 1),
                "n_high": n_high,
                "acc_mid": round(acc_mid, 1),
                "n_mid": n_mid
            })

            score_accumulator += (acc_high if n_high >= 20 else acc_overall)

        avg_score = score_accumulator / num_targets
        dur = time.time() - ep_t0
        is_best = avg_score > best_score

        if is_best:
            best_score = avg_score
            torch.save({
                "sport": sport,
                "epoch": epoch,
                "model_state_dict": model.state_dict(),
                "input_dim": input_dim,
                "num_targets": num_targets,
                "targets_meta": targets_meta,
                "target_metrics": target_metrics,
                "best_score": round(best_score, 1)
            }, best_path)

        flag = "🔥 [NEW BEST]" if is_best else ""
        print(f" Ep {epoch:2d}/{epochs:2d} ({dur:.1f}s) | Loss: {train_loss:.4f} | Avg Score: {avg_score:.1f}% {flag}", flush=True)

    # ── Final Leaderboard Printing ────────────────────────────────────────────
    ckpt = torch.load(best_path, map_location="cpu", weights_only=False)
    final_metrics = ckpt["target_metrics"]

    print("\n" + "=" * 90)
    print(f" 🏆 INSTITUTIONAL PREDICTABILITY LEADERBOARD: {sport.upper()}")
    print("=" * 90)
    print(f" {'#':<3} {'Target Name':<28} {'Overall':<9} {'Cohort 80%+':<15} {'Cohort 70-80%':<15} {'Verdict':<15}")
    print("-" * 90)

    leaderboard_results = []
    for m in final_metrics:
        # Verdict calculation
        if m["acc_high"] >= 90.0 and m["n_high"] >= 15:
            verdict = "🟢 GOLDEN EDGE"
        elif m["acc_high"] >= 80.0 or m["acc_mid"] >= 75.0:
            verdict = "🟡 TRADABLE"
        else:
            verdict = "🔴 HIGH ENTROPY"

        print(f" {m['id']:<3} {m['name']:<28} {m['acc_overall']:>5.1f}%   {m['acc_high']:>5.1f}% ({m['n_high']:<4})   {m['acc_mid']:>5.1f}% ({m['n_mid']:<4})   {verdict}")
        m["verdict"] = verdict
        leaderboard_results.append(m)

    print("=" * 90)
    print(f" Model saved to: {best_path} (Training time: {(time.time() - t_sport_start)/60:.1f} min)\n", flush=True)

    # Save leaderboard to JSON
    lb_path = os.path.join(MODEL_DIR, f"{sport}_predictability_leaderboard.json")
    with open(lb_path, "w", encoding="utf-8") as f:
        json.dump(leaderboard_results, f, indent=2, ensure_ascii=False)

def main():
    import argparse
    parser = argparse.ArgumentParser(description="Multi-Task Tactical Net GPU Trainer")
    parser.add_argument("--sport", type=str, default=os.environ.get("SPORT", "all"),
                        help="Specific sport to train (football, tennis, table-tennis, basketball, ice-hockey) or 'all'")
    parser.add_argument("--epochs", type=int, default=int(os.environ.get("EPOCHS", "5")),
                        help="Number of epochs to train (default: 5)")
    parser.add_argument("--max-samples", type=int, default=int(os.environ.get("MAX_SAMPLES", "250000")),
                        help="Max recent samples to train on per sport (default: 250000)")
    args = parser.parse_args()

    all_sports = ["ice-hockey", "basketball", "tennis", "table-tennis", "football"]
    target_sports = all_sports if args.sport == "all" else [args.sport]

    for s in target_sports:
        train_sport_multitask(s, epochs=args.epochs, max_samples=args.max_samples)
    print(f"\n🏁 MULTI-TASK TRAINING FOR [{','.join(target_sports)}] COMPLETED SUCCESSFULLY!")

if __name__ == "__main__":
    main()
