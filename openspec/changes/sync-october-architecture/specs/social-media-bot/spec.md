## ADDED Requirements

### Requirement: Isolated Mobile Virtual Phone Ecosystem and Native Coupon Capture
The automation framework MUST maintain strict isolation between the Social Media phone pool (operating solely through US/NL/UK static proxies) and the Capture pools. The Capture pool MUST be separated into RU Capture (running on direct home ISP IP for TsUPIS bookmakers) and Global Capture (running via regional country proxies). Applications MUST be provisioned directly via ADB (`adb install -r -g`) or Aurora Store.

#### Scenario: Mobile coupon capture for marketing publication
- **WHEN** an arbitrage or matched betting opportunity is scheduled for publication
- **THEN** the capture worker automatedly launches the bookmaker mobile app, selects the betting market, takes a native coupon screenshot, and embeds it into the post payload alongside the 80% freebet cash calculation and responsible gambling disclaimer.

#### Scenario: Device pool isolation enforcement
- **WHEN** a virtual phone device is assigned to the Social Media pool
- **THEN** bookmaker betting applications are strictly excluded from installation on that device to prevent platform anti-fraud flags.

---

### Requirement: Automated Multi-Platform Video Shorts and Reels Generation
The social media system MUST support automated rendering and publishing of vertical short-form videos (9:16 aspect ratio) for YouTube Shorts, Instagram Reels, and TikTok featuring live surebets, line anomalies, and freebet conversions.

#### Scenario: Video shorts automated compilation
- **WHEN** the video compilation pipeline triggers with an arbitrage alert
- **THEN** it generates synchronized audio commentary, renders dynamic motion graphics with match odds and coupon cards, outputs an MP4 vertical video, and schedules it across connected social channels.

---

### Requirement: Persistent Boosty OAuth Authentication and Community Broadcasting
The publishing system MUST manage persistent authentication state and access tokens for Boosty, allowing automated broadcast of premium predictions, arbitrage digests, and development devlog updates to paid supporters.

#### Scenario: Boosty token renewal and posting
- **WHEN** a scheduled post for Boosty subscribers is prepared
- **THEN** the publisher verifies active session tokens, refreshes them via the background auth listener if required, and dispatches the formatted publication via the Boosty API.
