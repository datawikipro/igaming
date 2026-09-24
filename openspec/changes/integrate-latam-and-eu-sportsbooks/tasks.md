## 1. LatAm Flagships (Betano & Playtech Engine)

- [ ] 1.1 Scaffold `igaming-source-playtech` base module for Playtech sportsbooks
- [ ] 1.2 Implement `igaming-source-caliente` (Mexico #1, `sports.caliente.mx`)
- [ ] 1.3 Implement `igaming-source-wplay` (Colombia #1, `wplay.co`)
- [ ] 1.4 Implement `igaming-source-betano` (Brazil & Portugal #1, Kaizen Gaming API)
- [ ] 1.5 Implement `igaming-source-codere` (Mexico / Spain)
- [ ] 1.6 Implement `igaming-source-estrelabet` (Brazil / Altenar) **[IN PROGRESS]**

## 2. LatAm Kambi Integration (BetPlay & Rushbet)

- [x] 2.1 Implement `igaming-source-betplay` using Kambi Colombia channel
- [x] 2.2 Implement `igaming-source-rushbet` using Kambi LatAm channel
- [ ] 2.3 Verify line ingestion for Liga BetPlay Dimayor and Copa Libertadores

## 3. European Tier-1 Sportsbooks (Unibet & Tipico)

- [x] 3.1 Implement `igaming-source-unibet` using European Kambi feed
- [ ] 3.2 Implement `igaming-source-tipico` (Germany & Austria #1)
- [ ] 3.3 Validate Bundesliga and Premier League coverage with `match_cache >= 500`

## 4. European P2P Betting Exchanges (Betdaq, Matchbook, Smarkets)

- [ ] 4.1 Implement `igaming-source-betdaq` orderbook scraper
- [x] 4.2 Implement `igaming-source-matchbook` via developer API key
- [x] 4.3 Implement `igaming-source-smarkets` WebSocket / REST orderbook feeder
- [ ] 4.4 Integrate European exchange back/lay lines into cross-market surebet detection

## 5. Africa & Crypto High-Liquidity Platforms

- [ ] 5.1 Implement `igaming-source-sportybet` (Nigeria & Kenya #1)
- [ ] 5.2 Implement `igaming-source-bcgame` crypto sportsbook feeder
- [ ] 5.3 Configure Playwright stealth profile for `igaming-source-stake`

## 6. Multi-Channel Monetization & Commercial API Engine

- [x] 6.1 Implement `AffiliateRedirectController` in `igaming-portal` with SubID attribution
- [x] 6.2 Implement CPA outbound click tracking and conversion webhooks
- [x] 6.3 Implement `B2BOddsApiController` in `igaming-portal` with API Key quota enforcement
- [x] 6.4 Build Matched Betting / Bonus Hunter conversion calculator in portal UI
- [ ] 6.5 Deploy VIP Telegram Signals Bot with premium filter routing
