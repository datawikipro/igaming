## 1. Network Infrastructure & Routing Optimization

- [x] 1.1 Persist US Gateway routing in `sing-box` on `ru-proxy` (`outline-us`)
- [ ] 1.2 Implement automated health-check and latency probing for US endpoints
- [ ] 1.3 Design fallback routing hook for future Residential US Proxy integration

## 2. Flagship Loader: Bovada (`igaming-source-bovada`)

- [x] 2.1 Scaffold Maven module `igaming-source-bovada` in backend POM
- [x] 2.2 Implement DTO models for Bovada JSON API
- [x] 2.3 Implement `BovadaApiClient` via Spring WebClient with proxying
- [x] 2.4 Implement `BovadaBetTypeMapper` (Moneyline, Point Spread, Totals)
- [x] 2.5 Implement `BovadaLoaderService` extending `AbstractBaseBookmakerService`
- [x] 2.6 Configure `application.properties` with non-blocking HikariCP
- [x] 2.7 Create K8s deployment and PostgreSQL StatefulSet manifests
- [ ] 2.8 Build and deploy to `igaming-dev` via Jib
- [ ] 2.9 Verify `match_cache >= 500` threshold and 5-minute zero-error runtime

## 3. US Sharp Books Loaders (Circa Sports, SuperBook, BookMaker.eu)

- [ ] 3.1 Implement `igaming-source-circasports` (Las Vegas sharpest benchmark)
- [x] 3.2 Implement `igaming-source-bookmaker` (DGS market opening line feeder)
- [ ] 3.3 Implement `igaming-source-superbook` (Westgate Las Vegas lines)

## 4. US Betting Exchanges & P2P Orderbooks (Sporttrade, Novig, ProphetX)

- [ ] 4.1 Implement `igaming-source-sporttrade` orderbook parser
- [x] 4.2 Implement `igaming-source-novig` zero-commission exchange feed
- [ ] 4.3 Implement `igaming-source-prophetx` sweepstakes exchange loader
- [ ] 4.4 Extend `aggregator-core` to calculate net return with exchange commission

## 5. US Kambi Ecosystem (BetRivers, Bally Bet, PlaySugarHouse)

- [x] 5.1 Implement `igaming-source-betrivers` using Kambi Offering API
- [x] 5.2 Implement `igaming-source-ballybet` using Kambi feed
- [x] 5.3 Configure channel and jurisdiction parameters for US states (NY, PA, NJ)

## 6. Social & Sweepstakes Sportsbooks (Fliff & Prop Powerhouses)

- [ ] 6.1 Implement `igaming-source-fliff` sweepstakes mobile API parser
- [ ] 6.2 Test and normalize Fliff point spreads and moneyline markets

## 7. Mini-Markets, Player Props & Micro-Betting Engine (DFS & In-Play Fast Markets)

- [ ] 7.1 Design canonical athlete dictionary and `athlete_cache` table
- [ ] 7.2 Implement `igaming-source-prizepicks` player prop line extractor
- [ ] 7.3 Implement `igaming-source-underdog` fantasy props extractor
- [ ] 7.4 Implement `igaming-source-sleeper` DFS props extractor
- [ ] 7.5 Implement micro-betting in-play fast market classifier in `aggregator-core`

## 8. Global Arbitrage Integration: Asian Brokerage Gateway

- [ ] 8.1 Implement `igaming-source-betinasia` (Black platform / OrbitX Betfair White Label feed)
- [ ] 8.2 Connect Pinnacle & Sbobet Asian syndicate lines into global matching matrix
- [ ] 8.3 Validate cross-continent 3-way arbitrage (Asia Sharp vs US Retail vs EU Exchange)

## 9. Protected Tier-1 US Books (Playwright Stealth & Residential Egress)

- [ ] 9.1 Implement `igaming-source-draftkings` session crawler
- [ ] 9.2 Implement `igaming-source-fanduel` with DataDome evasion
- [ ] 9.3 Implement `igaming-source-betonline` with Cloudflare Turnstile handler
- [ ] 9.4 Implement `igaming-source-caesars` OpenAPI extractor
- [ ] 9.5 Implement `igaming-source-espnbet` theScore engine parser
- [ ] 9.6 Implement residential proxy pool integration for Fanatics, Hard Rock, BetMGM

## 10. Aggregator Matching & UI Presentation

- [x] 10.1 Enable US sports event taxonomies (NFL, NBA, MLB, NHL, NCAA) in `igaming-aggregator`
- [x] 10.2 Add American Odds format (+150 / -110) toggle in portal gateway and web UI
- [ ] 10.3 Update Chrome Extension to support coupon auto-fill for Bovada and US books
