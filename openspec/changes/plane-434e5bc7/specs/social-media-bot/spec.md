# Social Media and Bot Specification (Delta)

## ADDED Requirements

### Requirement: Multilingual Telegram Regional Network Management
The SMM automation suite must manage regional Telegram broadcast channels for Russian (RU), English (EN), French (FR), and Spanish (ES) audiences, configure public usernames, grant administrator rights to `@smartbet_guru_bot`, and link discussion groups for audience feedback.

#### Scenario: Channel administration and public username assignment
- **WHEN** configuring regional Telegram channels via `telegram_web_manager.py`
- **THEN** English (`-3960368887`), France (`-4371643544`), and España (`-4346736376`) channels have `@smartbet_guru_bot` promoted as administrator with post and management privileges, and public usernames (`@SmartBetGuruEN`, `@SmartBetGuruFR`, `@SmartBetGuruES`) are verified.

#### Scenario: Discussion group creation and comment stream monitoring
- **WHEN** channel publications receive comments in linked discussion groups
- **THEN** the bot captures incoming comments, parses context from the parent post, routes inquiries to Patron CRM queue, and provides AI-prompter assistance.

---

### Requirement: Multilingual Native Card Publishing and Site Action Buttons
The `ChannelPosterScheduler` must format arbitrage and promo signals in the regional channel's native language, including 80% freebet guaranteed cash math and interactive inline buttons with affiliate tracking.

#### Scenario: Localized signal broadcasting with 80% freebet calculation
- **WHEN** an arbitrage or freebet opportunity is published to a regional channel
- **THEN** the message includes localized team and market details, calculated 80% guaranteed cash conversion, mandatory responsible gambling disclaimer in the target language, and inline buttons linking to the SmartBet calculator and bookmakers.
