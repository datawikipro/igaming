# Implementation Tasks: Sport-Polymorphic Team Signatures

## 1. Domain Model & Enums
- [x] 1.1 Create `AgeCategory`, `Gender`, `TeamTier`, `EsportsRosterType` enums. <!-- id: 1.1 -->
- [x] 1.2 Create `TeamSignature` sealed interface in `aggregator-domain`. <!-- id: 1.2 -->
- [x] 1.3 Implement `ClubTeamSignature`, `TennisParticipantSignature`, `EsportsTeamSignature`, `CombatParticipantSignature`. <!-- id: 1.3 -->

## 2. Strict Bookmaker Regular Expression Parsers
- [x] 2.1 Implement `ClubTeamSignatureParser` with Winline, Betcity, Leon, Fonbet, Zenit rules. <!-- id: 2.1 -->
- [x] 2.2 Implement `TennisSignatureParser` for singles/doubles with name inversion & initials handling. <!-- id: 2.2 -->
- [x] 2.3 Implement `EsportsSignatureParser` for org tags and player duel market isolation. <!-- id: 2.3 -->
- [x] 2.4 Implement `TeamSignatureFactory` routing raw inputs by sport. <!-- id: 2.4 -->

## 3. Integration & Team Unification Pipeline
- [x] 3.1 Update `TeamUnificationService` to use `TeamSignatureFactory` for deterministic matching. <!-- id: 3.1 -->
- [x] 3.2 Update `TeamNameNormalizer` to leverage polymorphic signatures. <!-- id: 3.2 -->

## 4. Testing & Verification
- [x] 4.1 Write comprehensive unit tests for `ClubTeamSignatureParser`, `TennisSignatureParser`, and `EsportsSignatureParser`. <!-- id: 4.1 -->
- [x] 4.2 Verify zero false positive matching on edge cases (e.g. ИРАК vs ИРАН, U19 vs Main, Singles vs Doubles). <!-- id: 4.2 -->
- [x] 4.3 Run full test suite with `mvn test`. <!-- id: 4.3 -->
