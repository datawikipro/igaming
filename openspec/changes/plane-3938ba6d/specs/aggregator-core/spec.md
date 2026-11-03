## ADDED Requirements

### Requirement: Reactive Video Broadcast Publishing to Kafka
The enrichment service MUST reactively detect available video streams and iframe players for live and imminent matches, and immediately dispatch `MatchBroadcastDto` events to the Kafka topic `sports.broadcasts.discovered` adhering to Zero Server Bandwidth.

#### Scenario: Discovered stream publishing
- **WHEN** `MatchBroadcastDiscoveryService` discovers new or updated video broadcast streams for a match
- **THEN** it persists the streams to `match.broadcasts_json`, sets `has_broadcast = true`, and emits each `MatchBroadcastDto` to Kafka topic `sports.broadcasts.discovered` keyed by match ID.

#### Scenario: Reactive discovery on raw event arrival
- **WHEN** a live sports event is ingested via Kafka topic `sports.raw.sofascore`
- **THEN** the system triggers immediate stream discovery for the matching event without waiting for the 5-minute scheduled cron interval.

#### Scenario: Zero Server Bandwidth client playback
- **WHEN** video broadcasts are published to Kafka or retrieved via REST
- **THEN** the payload contains strictly player metadata and iframe embed URLs, requiring 0 bytes of server video proxying.
