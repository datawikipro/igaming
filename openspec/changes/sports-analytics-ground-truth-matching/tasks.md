## 1. Domain Model & DTOs

- [x] 1.1 Define ReferenceFixtureDto and TeamProfileDto in igaming-dto
- [x] 1.2 Add reference provider fields and indexes to Match and Team entities in aggregator-domain
- [x] 1.3 Add repository queries for searching matches and fixtures by time window and sport in MatchRepository

## 2. Analytics Fixture Providers

- [x] 2.1 Implement MatchFixtureProvider interface and scheduling in igaming-capture-sofascore
- [x] 2.2 Implement SofaScore scheduled fixtures ingestion for football, basketball, hockey, tennis
- [x] 2.3 Implement ESPN scheduled fixtures provider for American leagues and top football
- [x] 2.4 Implement LiveResult scheduled fixtures provider in igaming-capture-liveresult

## 3. Reference-Assisted Match & Team Unification

- [x] 3.1 Implement ReferenceMatchMatcher in aggregator-domain utilizing TeamSignatureFactory
- [x] 3.2 Integrate reference matcher into MatchUnificationService and TeamUnificationService before LLM fallback
- [x] 3.3 Implement automatic TeamAlias registration and cache warming upon reference correlation
- [x] 3.4 Update Team profile metadata enrichment to populate canonical logos and multilingual names

## 4. Verification & Testing

- [x] 4.1 Create unit tests for ReferenceMatchMatcher with cross-bookmaker team name variations
- [x] 4.2 Verify scheduled fixture synchronization and live matching pipeline against sample bookmaker feeds
- [x] 4.3 Validate complete OpenSpec change with openspec validate --specs
