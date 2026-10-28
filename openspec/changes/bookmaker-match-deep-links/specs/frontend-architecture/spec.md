## ADDED Requirements

### Requirement: Match Card Bookmaker and Source Deep Links UI
The match detail page and odds comparison grid SHALL render openable direct links to the match across available bookmakers and SofaScore.

#### Scenario: User views match card
- **WHEN** user opens a match card on the website
- **THEN** an interactive group of buttons/badges is displayed with bookmaker and SofaScore logos/names and external link icons
- **AND** clicking any link opens the match on the respective provider's website in a new tab (`target="_blank" rel="noopener noreferrer"`)

#### Scenario: User interacts with odds grid
- **WHEN** user views the odds comparison table for a match
- **THEN** each bookmaker's name is rendered as a clickable link or has an external link button directing to that bookmaker's match page
