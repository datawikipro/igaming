## 1. BetB2B / 1xBet Family Optimization

- [x] 1.1 Fix hardcoded dummy URL generation in `AbstractXbetFamilyService.java` (`loadSingleMatchCard`)
- [x] 1.2 Implement dynamic `normalizeSportSlug()` and league ID extraction for 1xBet, Melbet, FanSport, SpinBetter
- [x] 1.3 Verify BetB2B line ingestion and odds mapping

## 2. Betcity Feed Upgrade

- [x] 2.1 Update `app.betcity.prematch-url` in `igaming-source-betcity/src/main/resources/application.properties` to `https://ad.betcity.ru/d/off/bets?rev=8&add=dep_event&template=1`
- [x] 2.2 Verify Betcity prematch parser handles full `dep_event` sub-markets (3,484 matches in local cache)

## 3. Marathonbet Throughput Scaling

- [x] 3.1 Increase league batch size in `MatchFetchScheduler.java` from 3 to 25
- [x] 3.2 Set non-blocking `ddl-auto=none` across source modules

## 4. Olimpbet Full Catalog Activation

- [x] 4.1 Remove `/top/` restriction in `igaming-k8s/olimpbet.ru.yaml` (point to full `/line/sports-with-competitions-with-events`)
- [x] 4.2 Set `ddl-auto=none` in `igaming-source-olimpbet`

## 5. Baltbet Full Catalog Activation

- [x] 5.1 Switch `app.baltbet.prematch-url` from `/events/hot` to `/events/all`
- [x] 5.2 Set `ddl-auto=none` in `igaming-source-baltbet`

## 6. Tennisi Multi-Sport Expansion

- [x] 6.1 Implement multi-sport iteration (gameid 1 to 20) in `TennisiApiClient.java`
- [x] 6.2 Set `ddl-auto=none` and configure aggregator URL in `igaming-source-tennisi`

## 7. Zenit, BetBoom, Sbobet & Pinnacle Stabilization

- [x] 7.1 Increase Zenit prematch length to 5000 in `igaming-k8s/zenit.win.yaml`
- [x] 7.2 Set `ddl-auto=none` across `igaming-source-zenit`, `igaming-source-betboom`, `igaming-source-sbobet`, `igaming-source-pinnacle`

## 8. Verification & OpenSpec

- [x] 8.1 Validate OpenSpec specifications (`openspec validate --specs`)
- [x] 8.2 Verify builds and deployment readiness