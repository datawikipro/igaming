## ADDED Requirements

### Requirement: Synthetic Arbitrage for Even/Odd Markets

The aggregator surebet engine MUST recognize and process Even/Odd betting markets as canonical two-way arbitrage pairs.

#### Scenario: Detecting Even/Odd surebet across bookmakers
- **WHEN** bookmaker A offers `EVEN_ODD_EVEN` at odds $O_{even}$ and bookmaker B offers `EVEN_ODD_ODD` at odds $O_{odd}$ for the same match event
- **THEN** the engine evaluates the two-way arbitrage condition $\frac{1}{O_{even}} + \frac{1}{O_{odd}} < 1.0$, and if satisfied, creates a `Surebet` entity with optimal stake distribution $S_{even} = \frac{B \cdot \frac{1}{O_{even}}}{\frac{1}{O_{even}} + \frac{1}{O_{odd}}}$ and $S_{odd} = B - S_{even}$.

#### Scenario: BetTypeMatcher pairs Even/Odd outcomes
- **WHEN** the `BetTypeMatcher` evaluates candidate bet pairs
- **THEN** `EVEN_ODD_EVEN` is exclusively matched against `EVEN_ODD_ODD` (and vice versa) on the same normalized match event, preventing cross-market false positives.

---

### Requirement: Synthetic Arbitrage for BTTS (Both Teams To Score) Markets

The aggregator surebet engine MUST recognize BTTS markets as two-way arbitrage pairs using the same formula structure as Even/Odd.

#### Scenario: Detecting BTTS surebet
- **WHEN** bookmaker A offers `BTTS_YES` at odds $O_{yes}$ and bookmaker B offers `BTTS_NO` at odds $O_{no}$ for the same football match
- **THEN** the engine applies the two-way formula $\frac{1}{O_{yes}} + \frac{1}{O_{no}} < 1.0$, and upon detection instantiates a `Surebet` with `betType = BTTS`, stores it in PostgreSQL with `status = ACTIVE`, and broadcasts it via Redis/WebSocket.

#### Scenario: BTTS market normalization from raw bookmaker strings
- **WHEN** a raw odds update arrives with market strings such as `"Обе забьют: Да"`, `"BTTS: Yes"`, `"Both Teams Score"`, `"Обе команды забьют – Да"`, or `"GG"` 
- **THEN** the `BetTypeNormalizer` maps these strings to the canonical `BTTS_YES` type; similarly `"Обе забьют: Нет"`, `"BTTS: No"`, `"NG"` are mapped to `BTTS_NO`.

---

### Requirement: Synthetic Arbitrage for CS2 Map Totals

The aggregator engine MUST support map-total markets in CS2/esports disciplines with integer-boundary corridor (Middle) detection.

#### Scenario: Detecting standard CS2 map total surebet (Over/Under same line)
- **WHEN** bookmaker A offers `MAP_TOTAL_OVER` at threshold $T$ with odds $O_A^{over}$ and bookmaker B offers `MAP_TOTAL_UNDER` at the same threshold $T$ with odds $O_B^{under}$
- **THEN** the engine evaluates $\frac{1}{O_A^{over}} + \frac{1}{O_B^{under}} < 1.0$ and upon detection creates a `Surebet` entity tagged with `discipline = CS2` and `betType = MAP_TOTAL`.

#### Scenario: Detecting CS2 map corridor (Middle) between different thresholds
- **WHEN** bookmaker A offers `MAP_TOTAL_OVER` at threshold $T_A$ and bookmaker B offers `MAP_TOTAL_UNDER` at threshold $T_B$ where $T_A < T_B$ (e.g., Over 2.5 vs Under 3.5)
- **THEN** the `MiddleDetector` recognizes a winning corridor of width $T_B - T_A$ maps, computes $P(\text{double win}) = P(T_A < X \leq T_B)$, generates a `Middle` event with `maxRisk = |S_A - S_B \cdot R_B|`, and publishes it with `corridor_width` and `double_win_probability` fields.

#### Scenario: CS2 map total normalization
- **WHEN** raw market strings such as `"Карты: Больше 2.5"`, `"Maps O2.5"`, `"Total Maps Over 2.5"`, `"Кол-во карт: больше"`, `"More than 2 maps"` arrive
- **THEN** `BetTypeNormalizer` extracts the numeric threshold and maps to `MAP_TOTAL_OVER` with `handicap = 2.5`; strings `"Карты: Меньше 2.5"`, `"Maps U2.5"`, `"Total Maps Under 2.5"` map to `MAP_TOTAL_UNDER` with `handicap = 2.5`.

---

### Requirement: Synthetic Arbitrage for Statistical Totals (Corners, Cards, Shots)

The aggregator engine MUST detect arbitrage and corridor opportunities in statistical total markets including corners, yellow cards, and shots on target.

#### Scenario: Detecting statistical total surebet (same line)
- **WHEN** bookmaker A offers `STAT_TOTAL_OVER` at threshold $T$ with subtype `CORNERS` and bookmaker B offers `STAT_TOTAL_UNDER` at the same threshold $T$ for the same match
- **THEN** the engine evaluates $\frac{1}{O_A^{over}} + \frac{1}{O_B^{under}} < 1.0$ and creates a `Surebet` entity with `betType = STAT_TOTAL`, `statSubtype = CORNERS`, and status `ACTIVE`.

#### Scenario: Detecting statistical total corridor (Middle)
- **WHEN** bookmaker A offers `STAT_TOTAL_OVER` at threshold $T_A$ and bookmaker B offers `STAT_TOTAL_UNDER` at threshold $T_B > T_A$ for the same statistical dimension (e.g., corners: Over 9.5 vs Under 11.5)
- **THEN** the `MiddleDetector` recognizes a Middle with corridor $[T_A, T_B]$, computes double-win probability using historical distribution data for the `statSubtype`, and generates a `Middle` event with fields `stat_subtype`, `corridor_min`, `corridor_max`.

#### Scenario: Statistical total market normalization
- **WHEN** raw market strings such as `"Угловые: Больше 9.5"`, `"Corners Over 9.5"`, `"Total Corners O9.5"` arrive
- **THEN** `BetTypeNormalizer` maps to `STAT_TOTAL_OVER` with `statSubtype = CORNERS`, `handicap = 9.5`; similarly `"Жёлтые карточки: Больше 3.5"` → `STAT_TOTAL_OVER` / `statSubtype = CARDS`; `"Броски по воротам: Больше 12.5"` → `STAT_TOTAL_OVER` / `statSubtype = SHOTS`.

---

### Requirement: Extended BetType Enum and Canonical Normalization

The domain layer MUST define canonical `BetType` enum values for all synthetic arbitrage markets and provide a `BetTypeNormalizer` that maps raw bookmaker strings to canonical types.

#### Scenario: BetType enum coverage
- **WHEN** the system is configured
- **THEN** the `BetType` enum contains at minimum: `EVEN_ODD_EVEN`, `EVEN_ODD_ODD`, `BTTS_YES`, `BTTS_NO`, `MAP_TOTAL_OVER`, `MAP_TOTAL_UNDER`, `STAT_TOTAL_OVER`, `STAT_TOTAL_UNDER` in addition to existing types.

#### Scenario: BetTypeMatcher symmetric pair registration
- **WHEN** `BetTypeMatcher` initializes
- **THEN** it registers the following exclusive counter-pairs: `(EVEN_ODD_EVEN, EVEN_ODD_ODD)`, `(BTTS_YES, BTTS_NO)`, `(MAP_TOTAL_OVER, MAP_TOTAL_UNDER)`, `(STAT_TOTAL_OVER, STAT_TOTAL_UNDER)` — each pair is symmetric (A matches B and B matches A) and isolated from cross-pair matching.
