## ADDED Requirements

### Requirement: Suspended Odds Match Feed Delivery
The portal gateway and aggregator API SHALL include suspended odds with their last recorded values and an explicit `isSuspended: true` indicator in the match odds endpoints instead of filtering them out.

#### Scenario: Client requests match odds for an event with closed markets
- **WHEN** a client calls `GET /api/v1/matches/{id}/odds` or `GET /api/matches/{id}/odds`
- **THEN** outcomes whose bookmaker updates have ceased or been marked suspended are returned with their last known price and `isSuspended: true`.

#### Scenario: Client requests alternative odds
- **WHEN** a client calls `GET /api/odds/alternatives` or `GET /api/matches/{id}/close-odds`
- **THEN** suspended odds are flagged with `isSuspended: true` so the consumer interface can disable quick actions while preserving line context.
