## ADDED Requirements

### Requirement: Line Volume Acceptance Gate (>= 500 Matches)
A bookmaker service integration or update SHALL be accepted as Definition of Done compliant ONLY when its dedicated database maintains at least 500 active matches (`SELECT count(*) FROM match_cache >= 500`). A pod running with 0 matches or incomplete line is strictly classified as an unresolved defect.

#### Scenario: Bookmaker task completion gate
- **WHEN** evaluating readiness to mark a bookmaker integration task as complete
- **THEN** both criteria must pass: `match_cache` row count is at least 500 and the pod has run without errors for 5 consecutive minutes.

### Requirement: Physical Host Maintenance Window Restriction
Any administrative action requiring a hardware restart, cold boot, or Wake-on-LAN of the residential physical server (`xeon-srv`) SHALL be strictly prohibited during night hours between 22:00 and 10:00 MSK to maintain acoustic limits in a living apartment.

#### Scenario: Server restart requested during night window
- **WHEN** an incident or configuration change occurs between 22:00 and 10:00 MSK that normally warrants a host reboot
- **THEN** the system and AI agents must defer host restart until after 10:00 MSK and notify the operator.
