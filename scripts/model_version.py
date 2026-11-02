"""
SmartBet.guru / iGaming Institutional MLOps Versioning Standard
Format: v{MAJOR}.{IDEOLOGY}.{VECTOR}.{LOADER}.{ITERATION}
Example: v0.3.2.1.1

Components:
1. MAJOR (0): Core Network Architecture (MultiTaskTacticalNetV2 with Focal Multi-Head Loss)
2. IDEOLOGY (3): Tactical Reasoning Framework (0=Basic, 1=Consensus, 2=Cohorts, 3=Scenario Decision Trees & 7 Institutional Risk Ladders)
3. VECTOR (1): Vector Space Dimension & Feature Engineering (420-dim multi-sport tactical vector)
4. LOADER (1): Data Source & Parquet/ClickHouse Harvester Schema
5. ITERATION (1+): Incremental data sync / daily training run within current schema
"""

import os
import json
import time
from datetime import datetime
from typing import Dict, Any, Optional

REGISTRY_FILE = "/data/cold-storage/sofascore/models/model_version_registry.json"

class ModelVersion:
    MAJOR = 0
    IDEOLOGY = 3
    VECTOR = 3
    LOADER = 1
    ITERATION = 1

    @classmethod
    def get_version_string(cls, iteration: Optional[int] = None) -> str:
        it = iteration if iteration is not None else cls.ITERATION
        return f"v{cls.MAJOR}.{cls.IDEOLOGY}.{cls.VECTOR}.{cls.LOADER}.{it}"

    @classmethod
    def get_version_info(cls, iteration: Optional[int] = None) -> Dict[str, Any]:
        v_str = cls.get_version_string(iteration)
        return {
            "version": v_str,
            "major": cls.MAJOR,
            "major_desc": "MultiTaskTacticalNetV2 ResNet Backbone with Multi-Head Focal Loss",
            "ideology": cls.IDEOLOGY,
            "ideology_desc": "Scenario Decision Trees & 7 Institutional Risk Ladders (Post-Cohort Architecture)",
            "vector": cls.VECTOR,
            "vector_desc": "420-dim Refined Tactical Multi-Sport Vector (Player Archetypes + Dynamic Bench Depth + Poisson)",
            "loader": cls.LOADER,
            "loader_desc": "SofaScore + Canonical Bookmaker Lineup/Odds Parquet Harvester",
            "iteration": iteration if iteration is not None else cls.ITERATION,
            "timestamp": datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S UTC")
        }

    @classmethod
    def record_training_run(cls, sport: str, metrics: Dict[str, Any], samples_count: int, iteration: Optional[int] = None) -> Dict[str, Any]:
        os.makedirs(os.path.dirname(REGISTRY_FILE), exist_ok=True)
        registry = []
        if os.path.exists(REGISTRY_FILE):
            try:
                with open(REGISTRY_FILE, "r") as f:
                    registry = json.load(f)
            except Exception:
                registry = []

        v_info = cls.get_version_info(iteration)
        entry = {
            "version": v_info["version"],
            "sport": sport,
            "timestamp": v_info["timestamp"],
            "samples_count": samples_count,
            "version_info": v_info,
            "golden_edges_count": sum(1 for m in metrics.values() if isinstance(m, dict) and "GOLDEN" in str(m.get("verdict", ""))),
            "tradable_count": sum(1 for m in metrics.values() if isinstance(m, dict) and "TRADABLE" in str(m.get("verdict", ""))),
            "metrics_summary": metrics
        }
        registry.append(entry)
        with open(REGISTRY_FILE, "w") as f:
            json.dump(registry, f, indent=2)
        return entry

    @classmethod
    def get_history(cls, sport: Optional[str] = None) -> list:
        if not os.path.exists(REGISTRY_FILE):
            return []
        try:
            with open(REGISTRY_FILE, "r") as f:
                data = json.load(f)
                if sport:
                    return [d for d in data if d.get("sport") == sport]
                return data
        except Exception:
            return []


if __name__ == "__main__":
    print("CURRENT VERSION:", ModelVersion.get_version_string())
    print("VERSION INFO:", json.dumps(ModelVersion.get_version_info(), indent=2))
