#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generates docs/plane-tasks/dependency_graph.yaml for all 81 Plane issues.
Validates DAG properties (no cycles, valid references).
"""
import json
import sys
import os

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

with open("scratch/all_plane_issues.json", encoding="utf-8") as f:
    data = json.load(f)

issues_by_seq = {i["sequence_id"]: i for i in data["issues"]}

# Define dependencies for all tasks
# If a task has no dependencies, depends_on: []
DEPS = {
    # Fail-fast & early crawlers (Already Completed)
    1: [],
    2: [],
    3: [],
    4: [],
    5: [],
    8: [],
    12: [],
    13: [],
    14: [],
    15: [],
    17: [],
    19: [],
    21: [],

    # Infra & Crawlers base WAF (Priority Root)
    49: [],         # [infra-crawler] Базовый OCI-образ (igaming-source-base)
    47: [49],       # [betboom] Обход QRATOR WAF
    48: [49],       # [winline] Устранение сбоя Playwright Chromium и Firefox XVFB

    # Wave 1 Crawlers refactoring
    6: [47],        # [betboom] мапперы (зависит от WAF #47)
    7: [],          # [betcity]
    9: [],          # [zenit]
    10: [],         # [fon-bet-ru]
    11: [],         # [marathonbet]
    16: [],         # [betano]
    18: [],         # [digitain]
    20: [],         # [888sport]
    22: [],         # [sbobet]
    23: [],         # [betway]
    24: [],         # [fanduel]
    25: [],         # [draftkings]

    # Wave 2 Crawlers refactoring
    26: [20],       # [10bet] -> [888sport]
    27: [16],       # [bwin] -> [betano]
    28: [18],       # [apuestatotal] -> [digitain]
    29: [2],        # [bet7k] -> [estrelabet]
    30: [2],        # [betesporte] -> [estrelabet]
    31: [2],        # [betnacional] -> [estrelabet]
    32: [18],       # [vaidebet] -> [digitain]
    33: [2],        # [esportesdasorte] -> [estrelabet]
    34: [3],        # [wplay] -> [codere]
    35: [3],        # [caliente] -> [codere]
    36: [19],       # [atg] -> [unibet / Kambi]
    37: [21],       # [bcgame] -> [betsson]
    38: [21],       # [stake] -> [betsson]
    39: [19],       # [paf] -> [unibet / Kambi]
    40: [14],       # [smarkets] -> [pinnacle]

    # Frontend, Markets, Aggregator & Synthetic Surebets
    41: [],         # [frontend] Архитектурный рефакторинг маппинга исходов
    42: [41],       # [frontend] Новые рынки (Чет/Нечет, BTTS, Периоды/Сеты)
    43: [42],       # [frontend] Интерактивные фильтры вилочного сканера
    44: [41],       # [aggregator] Синтетический арбитраж: мульти-букмекерские формулы
    45: [44],       # [aggregator] Детекция аномалий инвертированных исходов
    46: [45],       # [aggregator] Метрики и дашборд доходности вилок

    # Infra, Auth, SEO & SMM Core (Mostly Completed)
    50: [],         # [infra-redis]
    51: [50],       # [auth-2fa]
    52: [],         # [seo-pages]
    53: [],         # [seo-calc]
    54: [],         # [smm-runner]
    55: [51, 54],   # [auto-reg]
    56: [54],       # [smm-meta]
    57: [],         # [smm-tg]
    58: [54],       # [crowd-reddit]
    59: [54],       # [video-shorts]
    60: [],         # [promo-radar]
    61: [55, 60],   # [affiliate-hub]
    62: [60],       # [freebet-calc]

    # MDM Core & Crawler Ops
    63: [],         # [MDM-CORE] Generic Entity Resolution API
    64: [63],       # [UI-MDM] Reusable Entity Resolution Hub
    65: [63],       # [UI-CRAWLER-OPS] Ingestion Pipeline Dashboard

    # Feedback Hub & Patron CRM (Completed)
    66: [],         # [feedback-core]
    67: [66],       # [patron-ingest]
    68: [66],       # [feedback-nlp]
    69: [66],       # [feedback-admin-ui]
    70: [68, 69],   # [plane-sync-loop]
    71: [67],       # [patron-content]
    72: [67],       # [pod-boosty]
    73: [67],       # [pod-patreon]
    74: [67],       # [pod-vk-donut]
    75: [67],       # [pod-tg-vip]
    76: [54],       # [stealth-novnc]

    # National Teams & Player Faces
    77: [],         # [team-national-model]
    78: [77],       # [team-national-detect]
    79: [77, 78],   # [frontend-team-flags]
    80: [78, 79],   # [country-analytics-seo]
    81: [79],       # [player-faces-tennis]
}

def validate_dag(deps):
    # 1. Check all referenced IDs exist
    for seq, parent_list in deps.items():
        if seq not in issues_by_seq:
            raise ValueError(f"Task #{seq} not found in Plane issues!")
        for p in parent_list:
            if p not in issues_by_seq:
                raise ValueError(f"Parent task #{p} referenced by #{seq} does not exist!")

    # 2. Cycle detection (Tarjan or DFS)
    visited = {}
    def dfs(node, path):
        visited[node] = True
        for p in deps.get(node, []):
            if p in path:
                cycle_str = " -> ".join(map(str, path + [p]))
                raise ValueError(f"Cycle detected in DAG: {cycle_str}")
            if not visited.get(p, False):
                dfs(p, path + [p])

    for node in deps:
        visited.clear()
        dfs(node, [node])

    print("DAG validation passed: 0 cycles, all 81 task references valid!")

validate_dag(DEPS)

# Helper to extract tag/module
def extract_module(title):
    if title.startswith("[") and "]" in title:
        return title[1:title.index("]")]
    return "general"

yaml_lines = [
    "# ============================================================================",
    "# SmartBet.guru (iGaming) - Autonomous Plane Tasks Dependency Graph (DAG)",
    "# Project: IGAMING (2df124d7-25b0-4145-a63c-aafcb0fe0041)",
    "# Total Tasks: 81",
    "# ============================================================================",
    "version: '1.0'",
    "project_id: '2df124d7-25b0-4145-a63c-aafcb0fe0041'",
    "workspace_slug: 'dataplatform'",
    "tasks:"
]

for seq in sorted(issues_by_seq.keys()):
    iss = issues_by_seq[seq]
    name = iss["name"].replace('"', '\\"')
    module = extract_module(iss["name"])
    parents = DEPS.get(seq, [])
    parents_str = "[" + ", ".join(str(p) for p in parents) + "]"
    yaml_lines.append(f"  - sequence_id: {seq}")
    yaml_lines.append(f"    id: '{iss['id']}'")
    yaml_lines.append(f"    module: '{module}'")
    yaml_lines.append(f"    title: \"{name}\"")
    yaml_lines.append(f"    depends_on: {parents_str}")

out_path = "docs/plane-tasks/dependency_graph.yaml"
os.makedirs("docs/plane-tasks", exist_ok=True)
with open(out_path, "w", encoding="utf-8") as f:
    f.write("\n".join(yaml_lines) + "\n")

print(f"Successfully generated {out_path} with {len(issues_by_seq)} tasks.")
