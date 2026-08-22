## 1. BetB2B / 1xBet Family Optimization

- [x] 1.1 Fix hardcoded dummy URL generation in `AbstractXbetFamilyService.java` (`loadSingleMatchCard`)
- [x] 1.2 Implement dynamic `normalizeSportSlug()` and league ID extraction for 1xBet, Melbet, FanSport, SpinBetter
- [x] 1.3 Verify BetB2B line ingestion and odds mapping

## 2. Betcity Feed Upgrade

- [x] 2.1 Update `app.betcity.prematch-url` in `igaming-source-betcity/src/main/resources/application.properties` to `https://ad.betcity.ru/d/off/bets?rev=8&add=dep_event&template=1`
- [x] 2.2 Verify Betcity prematch parser handles full `dep_event` sub-markets

## 3. Marathonbet Throughput Scaling

- [x] 3.1 Increase league batch size in `MatchFetchScheduler.java` from 3 to 25
- [x] 3.2 Optimize league queue interval and verify catalog discovery

## 4. Frontend Sanitization & Verification

- [x] 4.1 Create `bookmakerUrls.ts` in `smartbet.guru` to sanitize and repair bookmaker deep-links
- [x] 4.2 Validate OpenSpec specifications (`openspec validate --specs`)
- [x] 4.3 Verify live responses across all bookmaker endpoints