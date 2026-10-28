## 1. DTO and Domain Entity Model Updates

- [x] 1.1 Add `bookmakerUrls` map to `CanonicalMatchHeaderDto` in `igaming-dto`
- [x] 1.2 Add `bookmaker_urls` column and helper methods to `MdmMatch` in `igaming-mdm-service`
- [x] 1.3 Add `bookmaker_urls` column and helper methods to `Match` in `aggregator-domain`

## 2. Ingestion and Resolution Pipeline

- [x] 2.1 Update `MdmMatchResolutionService` in `igaming-mdm-service` to persist `eventUrl` in `MdmMatch.bookmakerUrls`
- [x] 2.2 Update `MatchAggregatorService` and `OddsUpsertManager` in `aggregator-domain` to persist bookmaker URLs into `Match.bookmakerUrls`
- [x] 2.3 Map `bookmakerUrls` into `CanonicalMatchHeaderDto` in `toHeaderDto`

## 3. Crawlers and Ground Truth URL Generation

- [x] 3.1 Ensure `MatchCache` and `OddsUpdateRequest` consistently receive openable `eventUrl` in `igaming-source-winline`, `igaming-source-betboom`, `igaming-source-betcity`, and `igaming-source-fon-bet-ru`
- [x] 3.2 Ensure `igaming-capture-sofascore` supplies canonical SofaScore match deep links
- [x] 3.3 Ensure `AbstractMatchEnricher` and `AbstractMatchService` in `igaming-source-core` propagate `eventUrl` into `MatchCache` and `OddsUpdateRequest`
- [x] 3.4 Support `AbstractAltenarOddsMapper` and `AbstractSBTechOddsMapper` with automatic `eventUrl` generation across 15+ bookmakers
- [x] 3.5 Update standalone crawlers (`bet9ja`, `betman`, `crickex`, `interwetten`, `norsktipping`, `swisslos`, `tab`, `veikkaus`, `winner`) to emit openable `eventUrl`
- [x] 3.6 Verify 100% of all 80 crawler modules emit openable match deep links

## 4. Portal Gateway API & Fallback Synthesis

- [x] 4.1 Update `PortalMatchController` in `igaming-portal` to aggregate `bookmakerUrls` from MDM header, Redis flat odds, SofaScore, FlashScore, LiveResult, and FBref on `GET /api/v1/matches/{id}`
- [x] 4.2 Implement universal `synthesizeBookmakerUrl` covering all 70+ bookmakers and jurisdictions (RU, BY, KZ, EU, GB, US, LatAm, Africa, Asia, Crypto)
- [x] 4.3 Verify backend compilation (`mvn compile`) across affected modules

## 5. Frontend Match Card & Odds Grid UI

- [x] 5.1 Add interactive bookmaker and stats/ground-truth link badges (SofaScore, FlashScore, LiveResult, FBref) in `MatchDetailHeader.tsx`
- [x] 5.2 Make bookmaker rows in `MatchOddsGrid.tsx` clickable to open direct match URLs in a new tab
- [x] 5.3 Register all 70+ bookmakers and stats providers in `BookmakerRegistry.ts` with deep-link sanitization

## 6. Multi-Bookmaker Deep Link and Affiliate Audit & Bugfix (Oct 28)

- [x] 6.1 Audit Russian licensed bookmakers (fix Baltbet 405 Method Not Allowed -> /prematch?search=, Tennisi 404 -> /sport/{sport}, Zenit .win vs .bet, Olimpbet/Marathonbet 301/302 loops)
- [x] 6.2 Audit BetB2B & 1xBet family (fix Paripesa 'pari' substring collision, leak of 1xbet.com domain across 8 brands, add live /live/ vs /line/ support)
- [x] 6.3 Audit Tier-1 International bookmakers (fix Stoiximan domain hijacking to Betano, Betfair eventId in /market/ mismatch, Betway 404 /event/ -> /en/sports/evt/, Betsson Techsson /en/sportsbook/, BetVictor plural /events/, Stake missing eventUrl in OddsMapper)
- [x] 6.4 Audit CIS (BY/KZ) and Americas (fix Marathonbet BY K8s DNS leak, Olimpbet KZ api4 host leak, prevent .com leakage for BY/KZ/LatAm in /go routes and BookmakerRegistry.inferBookmaker)
- [x] 6.5 Verify 100% test pass on BookmakerRegistry test suite (12/12 suites passing) and compilation of backend modules
