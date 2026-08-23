# Delta Specification: Sport-Polymorphic Team Signatures

## ADDED REQUIREMENTS

### Requirement: Polymorphic Team Signatures
The system SHALL evaluate team identity using sport-specific signatures implementing `sealed interface TeamSignature` rather than monolithic string distance metrics.

#### Scenario: Football Age-Category Matching
- **GIVEN** a team name "Градец Кралове (до19)" from Winline
- **AND** a team name "Градец Кралове (19)" from Betcity
- **WHEN** parsed by `ClubTeamSignatureParser`
- **THEN** both produce `ClubTeamSignature(coreClubName="градец кралове", ageCategory=U19, gender=MALE, teamTier=MAIN)`
- **AND** `signature1.matches(signature2)` returns `true`
- **AND** generates the canonical display name "Градец Кралове U19".

#### Scenario: Tennis Player Name Inversion & Initials
- **GIVEN** a player name "Черной А." from Winline
- **AND** a player name "Андрей Черной" from Betcity
- **WHEN** parsed by `TennisSignatureParser`
- **THEN** both resolve to `player1LastName="черной"` and `player1FirstInitial="а"`
- **AND** `signature1.matches(signature2)` returns `true`.

#### Scenario: Short Name Safety Guarantee
- **GIVEN** the country name "ИРАК" and "ИРАН"
- **WHEN** parsed by `ClubTeamSignatureParser`
- **THEN** `coreClubName` produces "ирак" and "иран" respectively
- **AND** `signature1.matches(signature2)` returns `false`.
