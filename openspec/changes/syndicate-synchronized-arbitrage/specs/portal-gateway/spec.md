## ADDED Requirements

### Requirement: Syndicate Room WebSocket Channels and FX Rates Matrix
The portal gateway SHALL expose real-time STOMP/WebSocket channels (`/topic/syndicate/{groupId}`) for instant proposal broadcasting and dual-ACK aggregation, maintain an active FX cross-rates matrix, and manage dual-ledger balance queries.

#### Scenario: Streaming proposal to syndicate room
- **WHEN** a targeted arbitrage proposal is generated for a syndicate group
- **THEN** the portal pushes a high-priority JSON payload over `/topic/syndicate/{groupId}` with a 4-second TTL countdown.

#### Scenario: FX Cross-rate matrix synchronization
- **WHEN** multi-currency syndicate calculations occur
- **THEN** the portal supplies authoritative cross-currency conversion rates (RUB/BYN/USD/EUR) to the matching pipeline.
