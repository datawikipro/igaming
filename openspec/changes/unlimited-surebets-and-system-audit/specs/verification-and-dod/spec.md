## ADDED Requirements

### Requirement: Bookmaker Line Volume Verification Threshold
A bookmaker source service (crawler/loader) is considered operational and verified if and only if its local match cache contains at least 500 active matches (`SELECT count(*) FROM match_cache >= 500`).

#### Scenario: Line verification pass
- **WHEN** the bookmaker's database contains 500 or more valid matches with non-expired start times
- **THEN** the bookmaker service passes line filling verification.

#### Scenario: Running pod with deficient line
- **WHEN** a crawler/loader pod is in status `Running 1/1` but contains fewer than 500 matches in `match_cache`
- **THEN** the task MUST NOT be marked complete and is classified as an incomplete line filling defect.

### Requirement: Host Server Night-Time Maintenance Window
Physical host maintenance or actions requiring a host reboot of `xeon-srv` are strictly forbidden between 22:00 and 10:00 MSK to maintain residential noise compliance.

#### Scenario: Night-time operation request
- **WHEN** an action or update requires rebooting host `xeon-srv` between 22:00 and 10:00 local time
- **THEN** the AI assistant SHALL postpone the host restart until after 10:00, notifying the user.
