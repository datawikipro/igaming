# Delta Specification: Deep Point-by-Point Tennis Statistics Capture

## ADDED Requirements

### Requirement: Deep Point-by-Point Tennis Statistics Harvesting
The crawler and capture layer must collect point-by-point performance statistics from Tennis Abstract and Tennis Explorer across all major court surfaces (Hard, Clay, Grass, Indoor).

#### Scenario: Harvesting 1st and 2nd serve and return stats per surface
- **WHEN** a player's statistical harvest is triggered for Tennis Abstract or Tennis Explorer
- **THEN** the harvester extracts 1st serve win %, 2nd serve win %, 1st serve return win %, and 2nd serve return win % segregated by Hard, Clay, Grass, and Indoor surfaces.

#### Scenario: Break point and tiebreak metrics tracking
- **WHEN** point-by-point match records are processed
- **THEN** the system calculates break point conversion %, break points saved %, and tiebreak winning percentage for each court surface.

---

### Requirement: Tennis Kafka Point-by-Point Streaming with MDM Player Mapping
Harvested deep tennis statistics must be correlated with player MDM entities and published to Kafka for downstream consumption by polymorphic `TennisSport` and odds modeling services.

#### Scenario: MDM player resolution and publishing
- **WHEN** surface point-by-point stats are harvested for an athlete
- **THEN** the player name is normalized and resolved against the `PlayerFace` MDM registry, and a `TennisPointByPointEvent` is published to Kafka topic `sports.tennis.point-by-point`.
