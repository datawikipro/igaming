## 1. Kambi Multi-Tenant Scaling (Sprint 1 — Ready)

- [x] 1.1 Scaffold `igaming-source-tonybet` using Kambi engine (channel `tonybet`, market `GB`)
- [x] 1.2 Scaffold `igaming-source-stoiximan` using Kambi engine (channel `stoiximan`, market `GR`)
- [x] 1.3 Scaffold `igaming-source-expekt` using Kambi engine (channel `expekt`, market `DK`)
- [x] 1.4 Scaffold `igaming-source-paf` using Kambi engine (channel `paf`, market `FI`)
- [x] 1.5 Scaffold `igaming-source-atg` using Kambi engine (channel `atg`, market `SE`)
- [ ] 1.6 Create K8s deployments and verify line ingestion (`match_cache >= 500`) with 5-min DoD

## 2. CIS & European REST Feeds (Sprint 1 — Ready)

- [x] 2.1 Implement `igaming-source-favbet` via WebClient with browser headers and EU proxy
- [x] 2.2 Implement `igaming-source-parimatch` with EU geo-bypass routing
- [x] 2.3 Implement `igaming-source-vbet` using Digitain/Vbet v1 REST endpoint
- [ ] 2.4 Deploy K8s manifests for Favbet, Parimatch, and Vbet with non-blocking HikariCP
- [ ] 2.5 Verify CIS line depth and DoD stability

## 3. Altenar B2B Platform Engine (Sprint 2 — High Leverage)

- [x] 3.1 Implement base archetype `igaming-source-altenar-base` with WebSocket / REST client
- [x] 3.2 Implement standard Altenar odds mapper (1X2, Over/Under, Asian Handicap)
- [x] 3.3 Connect Brazilian flagships: `bet7k`, `betnacional`, `esportesdasorte`, `vaidebet`
- [x] 3.4 Connect LatAm & EU Altenar books: `apuestatotal`, `novibet`, `betbra`, `betesporte`
- [ ] 3.5 Deploy K8s manifests for Altenar suite and verify 500+ matches per bookmaker

## 4. Crypto & Web3 Sportsbooks (Sprint 2 — Zero DC Block)

- [x] 4.1 Implement `igaming-source-stake` with GraphQL fixture polling
- [x] 4.2 Implement `igaming-source-bcgame` with direct REST line extraction
- [x] 4.3 Implement `igaming-source-cloudbet` with public sportsbook API
- [x] 4.4 Implement `igaming-source-polymarket` with Polygon Web3 prediction market adapter
- [ ] 4.5 Deploy crypto cluster suite and verify live odds streaming into Kafka

## 5. Global Betting Exchanges (Sprint 3)

- [x] 5.1 Implement Java APING REST client in existing scaffold `igaming-source-betfair`
- [x] 5.2 Implement two-sided Back/Lay orderbook mapper and exchange commission deduction
- [x] 5.3 Implement `igaming-source-betdaq` using developer REST API
- [x] 5.4 Extend `igaming-aggregator` to evaluate cross-bookmaker Back vs Lay surebets
- [ ] 5.5 Deploy exchange loaders in `igaming-dev` and verify DoD

## 6. Eastern Europe Regional Leaders (Sprint 3)

- [x] 6.1 Implement `igaming-source-superbet` (Romania / Poland flagship)
- [x] 6.2 Implement `igaming-source-sts` (Poland #1)
- [x] 6.3 Implement `igaming-source-fortuna` and `igaming-source-tipsport` (Czechia / Slovakia)
- [x] 6.4 Implement `igaming-source-mozzartbet` (Balkans #1)
- [ ] 6.5 Deploy Eastern European suite and verify line ingestion

## 7. Independent European Tier-1 (Sprint 4)

- [x] 7.1 Implement `igaming-source-sbtech` base engine for Betway and 10bet
- [x] 7.2 Implement `igaming-source-betvictor` with Playwright XHR response interceptor
- [x] 7.3 Implement `igaming-source-tipico` (Germany / Austria #1)
- [x] 7.4 Implement `igaming-source-betano` (Kaizen Gaming API)
- [ ] 7.5 Deploy Tier-1 EU suite and verify line volume

## 8. [BLOCKED] US & UK Heavyweights Awaiting Residential Proxies

- [ ] 8.1 [BLOCKER] Запросить у разработчика решение по Residential Proxies (US/UK) и настроить egress в кластере
- [ ] 8.2 [AWAITING_RESIDENTIAL] Implement UK heavyweights: William Hill, Ladbrokes, PaddyPower, Skybet, Betfred, Boylesports
- [ ] 8.3 [AWAITING_RESIDENTIAL] Implement US heavyweights: FanDuel, DraftKings, Caesars, ESPN BET, BetOnline
- [ ] 8.4 [AWAITING_RESIDENTIAL] Implement rotating mirror monitoring for 1win and Interwetten CF bypass
- [ ] 8.5 Verify residential line ingestion and full 5-minute zero-error runtime
