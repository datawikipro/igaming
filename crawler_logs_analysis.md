# 🕵️ Анализ логов кроулеров и лоадеров

**Время анализа:** 2026-06-09 23:11:50

## 📌 Сводная таблица по всем БК

| Букмекер | Краулер Статус | Лоадер Статус | Анализ ошибок краулера | Анализ ошибок лоадера |
| :--- | :--- | :--- | :--- | :--- |
| `1xbet` | Running | Running | 	sasl.login.connect.timeout.ms = null<br>	sasl.login.read.timeout.ms = null<br>	socket.connection.setup.timeout.max.ms = 30000<br>	socket.connection.s... | 	sasl.login.connect.timeout.ms = null<br>	sasl.login.read.timeout.ms = null<br>	socket.connection.setup.timeout.max.ms = 30000<br>	socket.connection.s... |
| `888starz` | Running | Running | 2026-06-09T20:07:18.874Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Bro... | 2026-06-09T20:00:44.878Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr89... |
| `baltbet` | Running | Running | OK (No errors found in last 100 lines) | 	sasl.login.refresh.window.factor = 0.8<br>	sasl.login.refresh.window.jitter = 0.05<br>	sasl.login.retry.backoff.max.ms = 10000<br>	sasl.login.retry.b... |
| `bet365` | Running | Running |         .imgbck{position: absolute; top: 0; left: 0; background-image: url("https://members.bet365.com/staticcontentapi/static/crossdomain/header.jpg"... | TIMEOUT: Command timed out after 15 seconds |
| `betandyou` | Running | Running | 2026-06-09T20:04:41.579Z  INFO 1 --- [igaming-source-betandyou] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityMan... | 	Isolation level: undefined/unknown<br>	Minimum pool size: undefined/unknown<br>	Maximum pool size: undefined/unknown<br>2026-06-09T20:00:41.047Z  INF... |
| `betboom` | Running | Running | 2026-06-09T20:01:42.722Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching prematc... | 	sasl.login.connect.timeout.ms = null<br>	sasl.login.read.timeout.ms = null<br>	socket.connection.setup.timeout.max.ms = 30000<br>	socket.connection.s... |
| `betcity` | Running | Running | 2026-06-09T20:11:14.154Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='... | 2026-06-09T20:11:13.740Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1730] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGro... |
| `betcity-com` | Running | Running | 	at org.springframework.scheduling.support.ScheduledMethodRunnable.runInternal(ScheduledMethodRunnable.java:130) ~[spring-context-6.2.1.jar!/:6.2.1]<b... | OK (No errors found in last 100 lines) |
| `betm` | Running | Running | 2026-06-09T20:10:46.996Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='... | 2026-06-09T20:10:46.858Z DEBUG 1 --- [igaming-source-betcity] [    virtual-180] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGro... |
| `bettery` | Running | Running | 2026-06-09T20:10:27.565Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuratio... | <br> :: Spring Boot ::                (v3.4.1)<br><br>2026-06-09T20:04:08.856Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.source.fon... |
| `dafabet` | Running | Running | 2026-06-09T20:07:31.017Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=125... | 2026-06-09T20:07:20.128Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=125... |
| `fansport` | Running | Running | 2026-06-09T20:07:08.576Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o... | 	Autocommit mode: undefined/unknown<br>	Isolation level: undefined/unknown<br>	Minimum pool size: undefined/unknown<br>	Maximum pool size: undefined/u... |
| `fon-bet-by` | Running | Running | TIMEOUT: Command timed out after 15 seconds | 2026-06-09T20:04:10.114Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr... |
| `fon-bet-kz` | Running | Running | 2026-06-09T20:09:22.810Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr... | 2026-06-09T20:03:55.430Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr... |
| `fon-bet-ru` | Running | Running | 2026-06-09T20:11:40.353Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: ... | 	sasl.login.connect.timeout.ms = null<br>	sasl.login.read.timeout.ms = null<br>	socket.connection.setup.timeout.max.ms = 30000<br>	socket.connection.s... |
| `leon` | Running | Running |  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_)| | | | | || (_| |  ) ) ) )<br>  '  |____| .__... | 	max.request.size = 1048576<br>	metadata.max.age.ms = 300000<br>	metadata.max.idle.ms = 300000<br>	metadata.recovery.strategy = none<br>	metric.report... |
| `ligastavok` | Running | Running | 2026-06-09T20:07:58.858Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filte... | 2026-06-09T20:05:30.853Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.core.browser.BrowserService      : Launching browser with p... |
| `linebet` | Running | Running | 2026-06-09T20:07:06.356Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation:... | 2026-06-09T19:59:50.590Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.source.betb2b.Betb2bApplication    : Starting Betb2bApplication ... |
| `marathonbet` | Running | Running | 2026-06-09T20:04:46.621Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumq... | 2026-06-09T20:00:47.784Z  INFO 1 --- [igaming-source-marathonbet] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationCont... |
| `marathonbet-by` | Running | Running | 2026-06-09T20:05:14.442Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xd... | 2026-06-09T20:01:22.313Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xd... |
| `marathonbet-com` | Running | Running | 2026-06-09T20:04:42.638Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] io.undertow.servlet                      : Initializing Spring... | 2026-06-09T20:01:02.815Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] io.undertow.servlet                      : Initializing Spring... |
| `megapari` | Running | Running | 2026-06-09T20:08:07.234Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for http... |   .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_)| |... |
| `melbet` | Running | Running | 2026-06-09T20:08:18.088Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS ... | OK (No errors found in last 100 lines) |
| `olimpbet` | Running | N/A | 2026-06-09T20:10:00.535Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr89... | - |
| `olimpbet-kz` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-06-09T20:06:03.788Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr89... |
| `pari` | Running | Running | OK (No errors found in last 100 lines) |  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )<br>  '  |____| .__|_| |_|_| |_\__, | / / / /<br> =========|_|==============|___/=/_/_/_/<br><br> :: Spring ... |
| `pinnacle` | Running | Running | 2026-06-09T20:11:36.895Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr89... | 2026-06-09T20:01:04.996Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level ca... |
| `sbobet` | N/A | Running | - | OK (No errors found in last 100 lines) |
| `sportbet` | N/A | Running | - | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... |
| `tennisi` | N/A | Running | - | 2026-06-09T20:00:47.872Z  INFO 1 --- [igaming-source-tennisi] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data module... |
| `winline` | Running | Running | 2026-06-09T20:07:54.214Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation:... | 2026-06-09T20:01:39.025Z  WARN 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLSta... |
| `zenit` | Running | Running | 2026-06-09T20:11:11.991Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Zenit API fetch stats: failed... | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... |


## 📝 Детальные логи и контекст по проблемным БК

### 🗂️ Букмекер: `1xbet`

#### 🕷️ Crawler (`igaming-source-1xbet-crawler-58d98dc65c-m4chn` - Running)
**Анализ ошибок:**
```text
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
```
**Последние строки логов:**
```text
2026-06-09T20:10:27.763Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-1xbet, host: 1xbet.com)
2026-06-09T20:11:27.766Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host '1xbet.com' is still reachable...
2026-06-09T20:11:36.603Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-1xbet, host: 1xbet.com)
```
#### 📥 Loader (`igaming-source-1xbet-loader-857978f795-rmp8r` - Running)
**Анализ ошибок:**
```text
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
```
**Последние строки логов:**
```text
2026-06-09T20:10:16.517Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host '1xbet.com' is still reachable...
2026-06-09T20:10:45.197Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 599 (service: igaming-source-1xbet, host: 1xbet.com)
2026-06-09T20:11:27.962Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='1xbet' name='Xbet-family'
```

---

### 🗂️ Букмекер: `888starz`

#### 🕷️ Crawler (`igaming-source-888starz-crawler-798fffcdbf-xtg4x` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:07:18.874Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:07:18.875Z  WARN 1 --- [igaming-source-888starz] [           main] p.d.i.s.core.browser.BrowserService      : XVFB_HEADED requested but DISPLAY missing. Falling back to HEADLESS_STEALTH.
2026-06-09T20:07:19.002Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: 888starz.bet -> 94.241.134.13
2026-06-09T20:07:19.015Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://888starz.bet'...
2026-06-09T20:07:20.528Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:07:20.567Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:07:20.611Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:07:20.651Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:07:20.672Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:07:20.791Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:07:24.477Z ERROR 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://888starz.bet: Remote host terminated the handshake
2026-06-09T20:07:24.536Z  WARN 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for '888starz.bet': javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
2026-06-09T20:07:24.536Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:07:28.384Z  WARN 1 --- [igaming-source-888starz] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:07:29.555Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for '888starz.bet'...
2026-06-09T20:07:29.563Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: 888starz.bet -> 94.241.134.13
2026-06-09T20:07:29.574Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://888starz.bet'...
2026-06-09T20:07:38.613Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host '888starz.bet' is reachable (HTTP 203). Proxy connectivity verified.
2026-06-09T20:07:41.645Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-888starz, host: 888starz.bet)
2026-06-09T20:07:52.773Z  INFO 1 --- [igaming-source-888starz] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:07:56.919Z  INFO 1 --- [igaming-source-888starz] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3055 (http) with context path '/'
2026-06-09T20:07:57.356Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.source.betb2b.Betb2bApplication    : Started Betb2bApplication in 174.813 seconds (process running for 182.565)
2026-06-09T20:07:57.484Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting periodic event discovery for Betb2b Family...
2026-06-09T20:07:57.486Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .d.i.s.c.e.x.s.AbstractXbetFamilyService : Starting Xbet Family event discovery for bookmaker: 888starz...
2026-06-09T20:07:57.486Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Rewrote URL from https://888starz.bet/LiveFeed/Get1xMatchByLeague?sports=1 to https://888starz.bet/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=233 (useServiceApi=true)
2026-06-09T20:07:57.510Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetching LIVE from service-api URL: https://888starz.bet/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=233
2026-06-09T20:07:57.625Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-06-09T20:07:57.847Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='888starz' name='Xbet-family'
2026-06-09T20:07:59.574Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker '888starz' (display: 'Xbet-family')
2026-06-09T20:07:59.849Z  INFO 1 --- [igaming-source-888starz] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='888starz' name='Xbet-family'
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-06-09T20:08:10.353Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.15.48:3128
2026-06-09T20:08:15.169Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Pre-visiting homepage to establish session: https://888starz.bet
2026-06-09T20:08:27.291Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host '888starz.bet' is still reachable...
2026-06-09T20:08:34.803Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-888starz, host: 888starz.bet)
2026-06-09T20:08:37.144Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://888starz.bet/bff-api/config/group/get?groups=b.core,d.core&lang=en
2026-06-09T20:08:38.469Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/bff-api/config/group/get?groups=b.core,d.core&lang=en (status=200)
2026-06-09T20:08:38.816Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://888starz.bet/bff-api/config/group/get?groups=d.customize&lang=en
2026-06-09T20:08:38.832Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://888starz.bet/analytics-module-api/v1/analytics?projectId=789&domain=888starz.bet
2026-06-09T20:08:39.220Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/fc
2026-06-09T20:08:39.515Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/analytics-module-api/v1/analytics?projectId=789&domain=888starz.bet (status=200)
2026-06-09T20:08:39.594Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/bff-api/config/group/get?groups=d.customize&lang=en (status=200)
2026-06-09T20:08:40.125Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://888starz.bet/bff-api/config/group/get?groups=d.technical,d.global&lang=en
2026-06-09T20:08:40.170Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/fc (status=204)
2026-06-09T20:08:40.171Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/ab.json
2026-06-09T20:08:43.462Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/ab.json (status=200)
2026-06-09T20:08:43.465Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/bff-api/config/group/get?groups=d.technical,d.global&lang=en (status=200)
2026-06-09T20:08:43.494Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json
2026-06-09T20:08:43.494Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json
2026-06-09T20:08:43.512Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json
2026-06-09T20:08:44.464Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json (status=200)
2026-06-09T20:08:45.014Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json (status=200)
2026-06-09T20:08:45.034Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json (status=200)
2026-06-09T20:08:48.408Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json
2026-06-09T20:08:48.605Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://888starz.bet/bff-api/config/v2/contacts.json?lang=en&country=218&isVipUser=false
2026-06-09T20:08:48.606Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Detected country code from request: 218
2026-06-09T20:08:49.775Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Homepage redirect status: https://888starz.bet -> https://888starz.bet/en/block, isCrossDomain=false
2026-06-09T20:08:49.786Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Normalized BetB2B relative fetch URL: /service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=233&virtualSports=true
2026-06-09T20:08:49.786Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Executing API fetch from page context: /service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=233&virtualSports=true
2026-06-09T20:08:49.850Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://888starz.bet/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=233&virtualSports=true
2026-06-09T20:08:51.556Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json (status=200)
2026-06-09T20:08:51.576Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/bff-api/config/v2/contacts.json?lang=en&country=218&isVipUser=false (status=200)
2026-06-09T20:08:51.626Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=233&virtualSports=true (status=200)
2026-06-09T20:08:51.881Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":3},"PS"
2026-06-09T20:08:51.882Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Successfully fetched data from service-api URL
2026-06-09T20:09:00.016Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : [888starz/EU] Discovered 33 live events
2026-06-09T20:09:00.017Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Rewrote URL from https://888starz.bet/LineFeed/Get1xMatchByLeague?sports=1 to https://888starz.bet/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=233 (useServiceApi=true)
2026-06-09T20:09:00.017Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetching PREMATCH from service-api URL: https://888starz.bet/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=233
2026-06-09T20:09:00.421Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Pre-visiting homepage to establish session: https://888starz.bet
2026-06-09T20:09:23.645Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://888starz.bet/bff-api/config/group/get?groups=b.core,d.core&lang=en
2026-06-09T20:09:24.019Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Homepage redirect status: https://888starz.bet -> https://888starz.bet/en/block, isCrossDomain=false
2026-06-09T20:09:24.020Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Normalized BetB2B relative fetch URL: /service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=233&virtualSports=true
2026-06-09T20:09:24.020Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Executing API fetch from page context: /service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=233&virtualSports=true
2026-06-09T20:09:24.043Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://888starz.bet/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=233&virtualSports=true
2026-06-09T20:09:25.194Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/bff-api/config/group/get?groups=b.core,d.core&lang=en (status=200)
2026-06-09T20:09:25.617Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://888starz.bet/bff-api/config/group/get?groups=d.customize&lang=en
2026-06-09T20:09:25.624Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://888starz.bet/analytics-module-api/v1/analytics?projectId=789&domain=888starz.bet
2026-06-09T20:09:25.624Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://888starz.bet/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=233&virtualSports=true (status=200)
2026-06-09T20:09:26.107Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://888starz.bet/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/fc
2026-06-09T20:09:26.229Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":10235,"GVE":1,"HL":true,"RLI
2026-06-09T20:09:26.229Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Successfully fetched data from service-api URL
2026-06-09T20:09:32.640Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : [888starz/EU] Discovered 50 prematch events
2026-06-09T20:09:34.806Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host '888starz.bet' is still reachable...
2026-06-09T20:09:45.038Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://888starz.bet: Unexpected end of file from server
2026-06-09T20:09:45.038Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host '888starz.bet' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:09:45.039Z  WARN 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.48 (config: vpngate-public-vpn-197). Requesting rotation...
2026-06-09T20:09:45.310Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 854 (service: igaming-source-888starz, host: 888starz.bet)
2026-06-09T20:09:45.310Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:09:45.710Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.62' (config: vpngate-vpn572583473, ID: 580, healthy proxies: 9)
2026-06-09T20:09:45.710Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.62:3128
2026-06-09T20:09:45.711Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.62:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:09:45.713Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host '888starz.bet' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:09:45.713Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for '888starz.bet'...
2026-06-09T20:09:45.816Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: 888starz.bet -> 94.241.134.13
2026-06-09T20:09:45.816Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://888starz.bet'...
2026-06-09T20:09:47.365Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host '888starz.bet' is reachable (HTTP 203). Proxy connectivity verified.
2026-06-09T20:09:47.465Z  INFO 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-888starz, host: 888starz.bet)
2026-06-09T20:10:45.714Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host '888starz.bet' is still reachable...
2026-06-09T20:10:47.619Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-888starz, host: 888starz.bet)
2026-06-09T20:10:57.977Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='888starz' name='Xbet-family'

```
#### 📥 Loader (`igaming-source-888starz-loader-5cc664d97c-qwcln` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:00:44.878Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:45.065Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:45.078Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:45.132Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:45.133Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
```
**Последние строки логов:**
```text
2026-06-09T20:10:36.133Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='888starz' name='Xbet-family'
2026-06-09T20:10:57.929Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host '888starz.bet' is still reachable...
2026-06-09T20:11:03.758Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-888starz, host: 888starz.bet)
```

---

### 🗂️ Букмекер: `baltbet`

#### 🕷️ Crawler (`igaming-source-baltbet-crawler-b969bf884-x97fk` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
2026-06-09T20:11:34.376Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'events.baltbet.ru' is still reachable...
2026-06-09T20:11:36.481Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] i.s.c.e.b.s.AbstractBaltbetFamilyService : Fetching real team names for Baltbet event 30502623
2026-06-09T20:11:36.490Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.b.service.BaltbetApiClient       : Fetching grouping event details from https://events.baltbet.ru/api/grouping/event/30502623
```
#### 📥 Loader (`igaming-source-baltbet-loader-77466c6698-jwd2x` - Running)
**Анализ ошибок:**
```text
	sasl.login.refresh.window.factor = 0.8
	sasl.login.refresh.window.jitter = 0.05
	sasl.login.retry.backoff.max.ms = 10000
	sasl.login.retry.backoff.ms = 100
	sasl.mechanism = GSSAPI
	sasl.oauthbearer.clock.skew.seconds = 30
	sasl.oauthbearer.expected.audience = null
	sasl.oauthbearer.expected.issuer = null
	sasl.oauthbearer.jwks.endpoint.refresh.ms = 3600000
	sasl.oauthbearer.jwks.endpoint.retry.backoff.max.ms = 10000
	sasl.oauthbearer.jwks.endpoint.retry.backoff.ms = 100
	sasl.oauthbearer.jwks.endpoint.url = null
	sasl.oauthbearer.scope.claim.name = scope
	sasl.oauthbearer.sub.claim.name = sub
	sasl.oauthbearer.token.endpoint.url = null
	security.protocol = PLAINTEXT
	security.providers = null
	send.buffer.bytes = 131072
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.3]
	ssl.endpoint.identification.algorithm = https
	ssl.engine.factory.class = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.certificate.chain = null
	ssl.keystore.key = null
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLSv1.3
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.certificates = null
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	transaction.timeout.ms = 60000
	transactional.id = null
	value.serializer = class org.springframework.kafka.support.serializer.JsonSerializer

2026-06-09T20:09:28.518Z  INFO 1 --- [igaming-source-baltbet] [     virtual-50] o.a.k.c.t.i.KafkaMetricsCollector        : initializing Kafka metrics collector
2026-06-09T20:09:28.655Z  INFO 1 --- [igaming-source-baltbet] [     virtual-50] o.a.k.clients.producer.KafkaProducer     : [Producer clientId=igaming-source-baltbet-producer-1] Instantiated an idempotent producer.
2026-06-09T20:09:28.904Z  INFO 1 --- [igaming-source-baltbet] [     virtual-50] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 3.8.1
2026-06-09T20:09:28.904Z  INFO 1 --- [igaming-source-baltbet] [     virtual-50] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: 70d6ff42debf7e17
2026-06-09T20:09:28.904Z  INFO 1 --- [igaming-source-baltbet] [     virtual-50] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1781035768897
2026-06-09T20:09:30.675Z  INFO 1 --- [igaming-source-baltbet] [tbet-producer-1] org.apache.kafka.clients.Metadata        : [Producer clientId=igaming-source-baltbet-producer-1] Cluster ID: MkU3OEVBNTcwNTJENDM2Qg
2026-06-09T20:09:30.685Z  INFO 1 --- [igaming-source-baltbet] [tbet-producer-1] o.a.k.c.p.internals.TransactionManager   : [Producer clientId=igaming-source-baltbet-producer-1] ProducerId set to 3238 with epoch 0
2026-06-09T20:09:30.923Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] .c.e.AbstractBookmakerPerformanceTracker : [baltbet] Performance Report - Pages processed: 1, Skipped: 0, Not found: 0, Known errors: {}
2026-06-09T20:09:30.924Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:09:34.784Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 4 baltbet matches
2026-06-09T20:09:38.297Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 baltbet matches
2026-06-09T20:09:41.666Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:09:44.949Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 baltbet matches
2026-06-09T20:09:48.352Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:09:51.843Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:09:55.259Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:09:58.488Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 baltbet matches
2026-06-09T20:10:01.770Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:10:05.147Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:10:08.292Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:10:16.616Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'events.baltbet.ru' is still reachable...
2026-06-09T20:10:17.632Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 baltbet matches
2026-06-09T20:10:21.676Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:10:25.143Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:10:26.629Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://events.baltbet.ru: Read timed out
2026-06-09T20:10:26.630Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'events.baltbet.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:10:26.630Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.64 (config: vpngate-vpn573163202). Requesting rotation...
2026-06-09T20:10:27.067Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported block for config 1412 (service: igaming-source-baltbet, host: events.baltbet.ru)
2026-06-09T20:10:27.068Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:10:27.476Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.60' (config: vpngate-vpn332519676, ID: 444, healthy proxies: 8)
2026-06-09T20:10:27.476Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.60:3128
2026-06-09T20:10:27.477Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.60:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:10:27.478Z  INFO 1 --- [igaming-source-baltbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'events.baltbet.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:10:27.478Z  INFO 1 --- [igaming-source-baltbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'events.baltbet.ru'...
2026-06-09T20:10:27.583Z  INFO 1 --- [igaming-source-baltbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: events.baltbet.ru -> 185.71.67.253
2026-06-09T20:10:27.584Z  INFO 1 --- [igaming-source-baltbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://events.baltbet.ru'...
2026-06-09T20:10:28.361Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:10:28.403Z  INFO 1 --- [igaming-source-baltbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'events.baltbet.ru' is reachable (HTTP 404). Proxy connectivity verified.
2026-06-09T20:10:28.528Z  INFO 1 --- [igaming-source-baltbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-baltbet, host: events.baltbet.ru)
2026-06-09T20:10:31.663Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] .c.e.AbstractBookmakerPerformanceTracker : [baltbet] Performance Report - Pages processed: 23, Skipped: 0, Not found: 0, Known errors: {}
2026-06-09T20:10:31.664Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:10:36.367Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:10:39.979Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:10:44.480Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 baltbet matches
2026-06-09T20:10:54.240Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:11:03.856Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:11:07.293Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:11:10.967Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 baltbet matches
2026-06-09T20:11:14.526Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 baltbet matches
2026-06-09T20:11:18.138Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 baltbet matches
2026-06-09T20:11:21.686Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:11:27.478Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'events.baltbet.ru' is still reachable...
2026-06-09T20:11:28.387Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches
2026-06-09T20:11:28.537Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-baltbet, host: events.baltbet.ru)
2026-06-09T20:11:32.252Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] .c.e.AbstractBookmakerPerformanceTracker : [baltbet] Performance Report - Pages processed: 17, Skipped: 0, Not found: 0, Known errors: {}
2026-06-09T20:11:32.252Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 baltbet matches
2026-06-09T20:11:35.709Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 baltbet matches

```

---

### 🗂️ Букмекер: `bet365`

#### 🕷️ Crawler (`igaming-source-bet365-crawler-7f69d7984-lx6z2` - Running)
**Анализ ошибок:**
```text
        .imgbck{position: absolute; top: 0; left: 0; background-image: url("https://members.bet365.com/staticcontentapi/static/crossdomain/header.jpg"); background-repeat: no-repeat;}
        a{text-decoration:none;color:#CC3300}
        a:hover{text-decoration:underline}
        .hdg2{text-align:left;padding:5px 5px 5px 20px;background-color:#ddd;font-weight:bold;color:#737373}
        .box3{border-left:5px solid #ddd;border-right:5px solid #ddd;border-bottom:5px solid #ddd;padding:10px}
        .mb10{margin-bottom:10px}
        .top a{color:#666666;font-size:10px}
   
2026-06-09T20:10:04.678Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.bet365.service.Bet365ApiClient   : Failed to parse public Bet365 site: DOM parsing failed: Non-empty HTML body received but parsed 0 events from Bet365. DOM layout might have changed..

java.lang.RuntimeException: DOM parsing failed: Non-empty HTML body received but parsed 0 events from Bet365. DOM layout might have changed.
	at pro.datawiki.igaming.source.bet365.service.Bet365ApiClient.getEvents(Bet365ApiClient.java:56) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.bet365.service.Bet365DiscoveryService.discoverEvents(Bet365DiscoveryService.java:29) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.bet365.service.MatchService.discoverEvents(MatchService.java:60) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359) ~[spring-aop-6.2.1.jar!/:6.2.1]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:723) ~[spring-aop-6.2.1.jar!/:6.2.1]
	at pro.datawiki.igaming.source.bet365.service.MatchService$$SpringCGLIB$$1.discoverEvents(<generated>) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.bet365.scheduler.MatchFetchScheduler.scheduleFetch(MatchFetchScheduler.java:29) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.runInternal(ScheduledMethodRunnable.java:130) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.lambda$run$2(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at io.micrometer.observation.Observation.observe(Observation.java:498) ~[micrometer-observation-1.14.2.jar!/:1.14.2]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.run(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.config.Task$OutcomeTrackingRunnable.run(Task.java:85) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.runAndReset(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-06-09T20:10:05.337Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.b.scheduler.MatchFetchScheduler  : Error during Bet365 discovery cycle

java.lang.RuntimeException: DOM parsing failed: Non-empty HTML body received but parsed 0 events from Bet365. DOM layout might have changed.
	at pro.datawiki.igaming.source.bet365.service.Bet365ApiClient.getEvents(Bet365ApiClient.java:56) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.bet365.service.Bet365DiscoveryService.discoverEvents(Bet365DiscoveryService.java:29) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.bet365.service.MatchService.discoverEvents(MatchService.java:60) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359) ~[spring-aop-6.2.1.jar!/:6.2.1]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:723) ~[spring-aop-6.2.1.jar!/:6.2.1]
	at pro.datawiki.igaming.source.bet365.service.MatchService$$SpringCGLIB$$1.discoverEvents(<generated>) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.bet365.scheduler.MatchFetchScheduler.scheduleFetch(MatchFetchScheduler.java:29) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.runInternal(ScheduledMethodRunnable.java:130) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.lambda$run$2(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at io.micrometer.observation.Observation.observe(Observation.java:498) ~[micrometer-observation-1.14.2.jar!/:1.14.2]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.run(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.config.Task$OutcomeTrackingRunnable.run(Task.java:85) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.runAndReset(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-06-09T20:10:05.339Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.b.scheduler.MatchFetchScheduler  : DOM parsing failed (attempt 1/3). Rotating proxy and retrying on next cycle.
2026-06-09T20:10:05.342Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.62 (config: vpngate-vpn572583473). Requesting rotation...
2026-06-09T20:10:05.509Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported block for config 580 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T20:10:05.510Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:10:05.900Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.67' (config: vpngate-vpn269589023, ID: 808, healthy proxies: 7)
2026-06-09T20:10:05.900Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.67:3128
2026-06-09T20:10:05.901Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.67:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:10:05.906Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.bet365.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:10:05.918Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.bet365.com'...
2026-06-09T20:10:05.937Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.bet365.com -> 104.18.32.61
2026-06-09T20:10:05.937Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.bet365.com'...
2026-06-09T20:10:15.460Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.bet365.com' is reachable (HTTP 403). Proxy connectivity verified.
2026-06-09T20:10:15.584Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 808 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T20:10:20.937Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.bet365.service.Bet365ApiClient   : Scraping public Bet365 site dynamically from: https://www.bet365.com
2026-06-09T20:10:20.941Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.15.62:3128 to http://10.42.14.67:3128. Recreating browser...
2026-06-09T20:10:23.540Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.14.67:3128
2026-06-09T20:10:42.031Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T20:10:45.540Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 808 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T20:11:00.286Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://www.bet365.com. Rotating proxy.
2026-06-09T20:11:00.287Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.67 (config: vpngate-vpn269589023). Requesting rotation...
2026-06-09T20:11:00.441Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported block for config 808 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T20:11:00.441Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:11:00.930Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.60' (config: vpngate-vpn332519676, ID: 444, healthy proxies: 3)
2026-06-09T20:11:00.930Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.60:3128
2026-06-09T20:11:00.930Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.60:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:11:00.931Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.bet365.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:11:00.931Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.bet365.com'...
2026-06-09T20:11:00.939Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.bet365.com: Blocked by Cloudflare/security system
2026-06-09T20:11:00.939Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.bet365.service.Bet365ApiClient   : Blank body retrieved from Bet365 lobby. Likely network or Turnstile block.
2026-06-09T20:11:00.939Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.bet365.service.Bet365ApiClient   : Successfully scraped 0 active events from Bet365
2026-06-09T20:11:00.940Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.b.s.Bet365DiscoveryService       : Bet365 API returned empty event list.
2026-06-09T20:11:01.115Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.bet365.com -> 172.64.155.195
2026-06-09T20:11:01.115Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.bet365.com'...
2026-06-09T20:11:01.610Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.bet365.com' is reachable (HTTP 403). Proxy connectivity verified.
2026-06-09T20:11:01.753Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T20:11:15.941Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.bet365.service.Bet365ApiClient   : Scraping public Bet365 site dynamically from: https://www.bet365.com
2026-06-09T20:11:15.941Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.14.67:3128 to http://10.42.15.60:3128. Recreating browser...
2026-06-09T20:11:19.861Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.15.60:3128

```
#### 📥 Loader (`igaming-source-bet365-loader-6b576d8db9-bn9lr` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```

---

### 🗂️ Букмекер: `betandyou`

#### 🕷️ Crawler (`igaming-source-betandyou-crawler-6fb88bd8f8-dfnmm` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:04:41.579Z  INFO 1 --- [igaming-source-betandyou] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:04:46.833Z  INFO 1 --- [igaming-source-betandyou] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:05:09.152Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:05:14.298Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.48' (config: vpngate-public-vpn-197, ID: 854, healthy proxies: 11)
2026-06-09T20:05:14.298Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.48:3128
2026-06-09T20:05:14.299Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.48:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:05:14.323Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'betandyou.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:05:14.323Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'betandyou.com'...
2026-06-09T20:05:14.377Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:05:14.377Z  WARN 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.browser.BrowserService      : XVFB_HEADED requested but DISPLAY missing. Falling back to HEADLESS_STEALTH.
2026-06-09T20:05:14.730Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: betandyou.com -> 178.253.28.174
2026-06-09T20:05:14.730Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://betandyou.com'...
2026-06-09T20:05:15.164Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:05:15.188Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:05:15.204Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:05:15.220Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:05:15.232Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:05:15.238Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:05:20.309Z  WARN 1 --- [igaming-source-betandyou] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:05:30.450Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'betandyou.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:05:36.103Z  INFO 1 --- [igaming-source-betandyou] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:05:37.404Z  INFO 1 --- [igaming-source-betandyou] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3053 (http) with context path '/'
2026-06-09T20:05:37.575Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.source.betb2b.Betb2bApplication    : Started Betb2bApplication in 123.056 seconds (process running for 127.987)
2026-06-09T20:05:37.603Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting periodic event discovery for Betb2b Family...
2026-06-09T20:05:37.612Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .d.i.s.c.e.x.s.AbstractXbetFamilyService : Starting Xbet Family event discovery for bookmaker: betandyou...
2026-06-09T20:05:37.613Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Rewrote URL from https://betandyou.com/LiveFeed/Get1xMatchByLeague?sports=1 to https://betandyou.com/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=5 (useServiceApi=true)
2026-06-09T20:05:37.616Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetching LIVE from service-api URL: https://betandyou.com/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=5
2026-06-09T20:05:37.617Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-06-09T20:05:37.954Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T20:05:38.211Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:05:40.364Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'betandyou' (display: 'Xbet-family')
2026-06-09T20:05:40.526Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-06-09T20:05:49.201Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.15.48:3128
2026-06-09T20:05:53.621Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Pre-visiting homepage to establish session: https://betandyou.com
2026-06-09T20:06:06.013Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://v3.traincdn.com/genfiles/banners-admin-api/all/d7aba7db8bb5f5c20a7547548c8b7de01920x248dn.webp
2026-06-09T20:06:07.544Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:06:15.844Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:06:23.785Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://betandyou.com/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=5&virtualSports=true, falling back to direct navigation: Error {
  message='Timeout 30000ms exceeded.
  name='TimeoutError
  stack='TimeoutError: Timeout 30000ms exceeded.
    at ProgressController.run (/tmp/playwright-java-15556371832066174462/package/lib/server/progress.js:78:26)
    at Frame.goto (/tmp/playwright-java-15556371832066174462/package/lib/server/frames.js:517:23)
    at FrameDispatcher.goto (/tmp/playwright-java-15556371832066174462/package/lib/server/dispatchers/frameDispatcher.js:81:119)
    at FrameDispatcher._handleCommand (/tmp/playwright-java-15556371832066174462/package/lib/server/dispatchers/dispatcher.js:94:40)
    at DispatcherConnection.dispatch (/tmp/playwright-java-15556371832066174462/package/lib/server/dispatchers/dispatcher.js:365:39)
}
Call log:
- navigating to "https://betandyou.com/", waiting until "load"

2026-06-09T20:06:23.904Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://betandyou.com/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=5&virtualSports=true
2026-06-09T20:06:25.461Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://betandyou.com/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=5&virtualSports=true (status=200)
2026-06-09T20:06:28.168Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":3},"PS"
2026-06-09T20:06:28.168Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Successfully fetched data from service-api URL
2026-06-09T20:06:44.642Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : [betandyou/EU] Discovered 30 live events
2026-06-09T20:06:44.688Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Rewrote URL from https://betandyou.com/LineFeed/Get1xMatchByLeague?sports=1 to https://betandyou.com/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=5 (useServiceApi=true)
2026-06-09T20:06:44.688Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetching PREMATCH from service-api URL: https://betandyou.com/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=5
2026-06-09T20:06:47.086Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Pre-visiting homepage to establish session: https://betandyou.com
2026-06-09T20:06:56.092Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://v3.traincdn.com/genfiles/banners-admin-api/all/d7aba7db8bb5f5c20a7547548c8b7de01920x248dn.webp
2026-06-09T20:07:15.911Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:07:17.118Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://betandyou.com/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=5&virtualSports=true, falling back to direct navigation: Error {
  message='Timeout 30000ms exceeded.
  name='TimeoutError
  stack='TimeoutError: Timeout 30000ms exceeded.
    at ProgressController.run (/tmp/playwright-java-15556371832066174462/package/lib/server/progress.js:78:26)
    at Frame.goto (/tmp/playwright-java-15556371832066174462/package/lib/server/frames.js:517:23)
    at FrameDispatcher.goto (/tmp/playwright-java-15556371832066174462/package/lib/server/dispatchers/frameDispatcher.js:81:119)
    at FrameDispatcher._handleCommand (/tmp/playwright-java-15556371832066174462/package/lib/server/dispatchers/dispatcher.js:94:40)
    at DispatcherConnection.dispatch (/tmp/playwright-java-15556371832066174462/package/lib/server/dispatchers/dispatcher.js:365:39)
}
Call log:
- navigating to "https://betandyou.com/", waiting until "load"

2026-06-09T20:07:17.309Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://betandyou.com/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=5&virtualSports=true
2026-06-09T20:07:18.673Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://betandyou.com/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=5&virtualSports=true (status=200)
2026-06-09T20:07:22.253Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":10362,"GVE":1,"HL":true,"RLI
2026-06-09T20:07:22.254Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Successfully fetched data from service-api URL
2026-06-09T20:07:31.622Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : [betandyou/EU] Discovered 50 prematch events
2026-06-09T20:07:35.383Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:08:35.388Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:08:38.105Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T20:08:57.122Z ERROR 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://betandyou.com: Read timed out
2026-06-09T20:08:57.123Z ERROR 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'betandyou.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:08:57.123Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.48 (config: vpngate-public-vpn-197). Requesting rotation...
2026-06-09T20:08:57.422Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported block for config 854 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:08:57.423Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:58.093Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.65' (config: vpngate-vpn503500791, ID: 950, healthy proxies: 10)
2026-06-09T20:08:58.094Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.65:3128
2026-06-09T20:08:58.095Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.65:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:58.095Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'betandyou.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:58.096Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'betandyou.com'...
2026-06-09T20:08:58.199Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: betandyou.com -> 178.253.28.174
2026-06-09T20:08:58.199Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://betandyou.com'...
2026-06-09T20:09:03.389Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'betandyou.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:09:03.569Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 950 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:09:58.103Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:10:05.275Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 950 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:11:05.276Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:11:11.726Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 950 (service: igaming-source-betandyou, host: betandyou.com)

```
#### 📥 Loader (`igaming-source-betandyou-loader-597bdc67dc-srvcn` - Running)
**Анализ ошибок:**
```text
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:00:41.047Z  INFO 1 --- [igaming-source-betandyou] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:00:41.876Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:41.884Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:42.007Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:42.008Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:42.043Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:42.044Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:00:42.100Z  INFO 1 --- [igaming-source-betandyou] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:00:46.044Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:00:49.614Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy 'proxy-vpn-pool.service-proxy.svc.cluster.local' (config: vpngate-vpn537213471, ID: 601, healthy proxies: 15)
2026-06-09T20:00:49.615Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: proxy-vpn-pool.service-proxy.svc.cluster.local:3128
2026-06-09T20:00:49.615Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:00:49.670Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'betandyou.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:00:49.670Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'betandyou.com'...
2026-06-09T20:00:49.864Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:00:50.000Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: betandyou.com -> 178.253.28.174
2026-06-09T20:00:50.000Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://betandyou.com'...
2026-06-09T20:00:54.870Z  INFO 1 --- [igaming-source-betandyou] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:00:56.704Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'betandyou.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:00:57.904Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:01:09.545Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:01:09.549Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:01:09.566Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:01:09.610Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:01:09.611Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:01:09.624Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:01:13.641Z  WARN 1 --- [igaming-source-betandyou] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:01:25.588Z  INFO 1 --- [igaming-source-betandyou] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:01:27.370Z  INFO 1 --- [igaming-source-betandyou] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3053 (http) with context path '/'
2026-06-09T20:01:27.814Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.source.betb2b.Betb2bApplication    : Started Betb2bApplication in 88.864 seconds (process running for 91.05)
2026-06-09T20:01:28.110Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T20:01:30.406Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'betandyou' (display: 'Xbet-family')
2026-06-09T20:01:30.567Z  INFO 1 --- [igaming-source-betandyou] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T20:01:57.630Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:01:58.799Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:02:58.809Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:03:00.874Z  INFO 1 --- [igaming-source-betandyou] [nio-3053-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:03:00.876Z  INFO 1 --- [igaming-source-betandyou] [nio-3053-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:03:00.891Z  INFO 1 --- [igaming-source-betandyou] [nio-3053-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 15 ms
2026-06-09T20:03:04.627Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:04:04.628Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:04:28.282Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T20:04:33.035Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:05:33.036Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:05:44.309Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:06:44.311Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:07:00.748Z ERROR 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://betandyou.com: Read timed out
2026-06-09T20:07:00.752Z ERROR 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'betandyou.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:07:00.753Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: proxy-vpn-pool.service-proxy.svc.cluster.local (config: vpngate-vpn537213471). Requesting rotation...
2026-06-09T20:07:02.740Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported block for config 601 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:07:02.745Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:07:15.986Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.66' (config: vpngate-patito, ID: 513, healthy proxies: 9)
2026-06-09T20:07:15.986Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.66:3128
2026-06-09T20:07:15.987Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.66:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:07:15.989Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'betandyou.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:07:15.991Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'betandyou.com'...
2026-06-09T20:07:16.176Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: betandyou.com -> 178.253.28.174
2026-06-09T20:07:16.177Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://betandyou.com'...
2026-06-09T20:07:27.410Z ERROR 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://betandyou.com: Remote host terminated the handshake
2026-06-09T20:07:27.412Z  WARN 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'betandyou.com': javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
2026-06-09T20:07:27.415Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:07:28.381Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T20:07:32.416Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'betandyou.com'...
2026-06-09T20:07:32.417Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: betandyou.com -> 178.253.28.174
2026-06-09T20:07:32.417Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://betandyou.com'...
2026-06-09T20:07:46.093Z ERROR 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://betandyou.com: Remote host terminated the handshake
2026-06-09T20:07:46.094Z  WARN 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'betandyou.com': javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
2026-06-09T20:07:46.095Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:07:51.096Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Resolving DNS for 'betandyou.com'...
2026-06-09T20:07:51.332Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] DNS resolved: betandyou.com -> 178.253.28.174
2026-06-09T20:07:51.334Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Checking HTTP connectivity to 'https://betandyou.com'...
2026-06-09T20:08:01.347Z ERROR 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://betandyou.com: Read timed out
2026-06-09T20:08:01.348Z  WARN 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 3/3] Connectivity check failed for 'betandyou.com': java.net.SocketTimeoutException: Read timed out
2026-06-09T20:08:01.349Z ERROR 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'betandyou.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T20:08:01.349Z  WARN 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity verification failed: 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'betandyou.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status. Will try to rotate on next failure.
2026-06-09T20:08:15.992Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:08:26.007Z ERROR 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://betandyou.com: Read timed out
2026-06-09T20:08:26.008Z ERROR 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'betandyou.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:08:26.008Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.66 (config: vpngate-patito). Requesting rotation...
2026-06-09T20:08:26.973Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 513 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:08:26.974Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:29.320Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.60' (config: vpngate-vpn332519676, ID: 444, healthy proxies: 10)
2026-06-09T20:08:29.321Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.60:3128
2026-06-09T20:08:29.321Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.60:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:29.324Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'betandyou.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:29.324Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'betandyou.com'...
2026-06-09T20:08:29.471Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: betandyou.com -> 178.253.28.174
2026-06-09T20:08:29.471Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://betandyou.com'...
2026-06-09T20:08:31.524Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'betandyou.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:08:32.617Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:09:29.325Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:09:30.990Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:10:28.481Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T20:10:30.993Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:10:32.998Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T20:11:33.000Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T20:11:37.527Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-betandyou, host: betandyou.com)

```

---

### 🗂️ Букмекер: `betboom`

#### 🕷️ Crawler (`igaming-source-betboom-crawler-6df57fb6df-nx4lz` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:01:42.722Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching prematch data from betboom.ru...
2026-06-09T20:01:42.723Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Fetching prematch events via Playwright browser...
2026-06-09T20:01:42.749Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-06-09T20:01:42.749Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-06-09T20:01:42.749Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-06-09T20:01:42.748Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/prematch...
2026-06-09T20:01:42.773Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-06-09T20:01:42.801Z  INFO 1 --- [igaming-source-betboom] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betboom' name='Betboom'
2026-06-09T20:01:42.874Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betboom' name='Betboom'
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-06-09T20:01:50.335Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: BASIC and proxy: http://10.42.14.64:3128
2026-06-09T20:01:54.876Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/prematch: Cannot find object to call __adopt__: page@93a007d92194aaedb524d26c3ed7d65a
2026-06-09T20:02:12.617Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T20:02:13.850Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T20:02:46.250Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Could not find Redux store on https://betboom.ru/sport/live
2026-06-09T20:03:13.897Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T20:03:14.810Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T20:03:48.462Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-06-09T20:03:48.463Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-06-09T20:03:48.471Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-06-09T20:04:14.815Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T20:04:19.464Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T20:04:35.209Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/live: Error {
  message='Timeout 45000ms exceeded.
  name='TimeoutError
  stack='TimeoutError: Timeout 45000ms exceeded.
    at ProgressController.run (/tmp/playwright-java-6730906091339946598/package/lib/server/progress.js:78:26)
    at Frame.goto (/tmp/playwright-java-6730906091339946598/package/lib/server/frames.js:517:23)
    at FrameDispatcher.goto (/tmp/playwright-java-6730906091339946598/package/lib/server/dispatchers/frameDispatcher.js:81:119)
    at FrameDispatcher._handleCommand (/tmp/playwright-java-6730906091339946598/package/lib/server/dispatchers/dispatcher.js:94:40)
    at DispatcherConnection.dispatch (/tmp/playwright-java-6730906091339946598/package/lib/server/dispatchers/dispatcher.js:365:39)
}
Call log:
- navigating to "https://betboom.ru/sport/live", waiting until "load"

2026-06-09T20:04:43.048Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betboom' name='Betboom'
2026-06-09T20:05:19.477Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T20:05:35.250Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-06-09T20:05:35.258Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-06-09T20:05:35.278Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-06-09T20:05:37.583Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T20:06:23.425Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Could not find Redux store on https://betboom.ru/sport/live
2026-06-09T20:06:37.586Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T20:06:42.442Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T20:06:54.946Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching prematch data from betboom.ru...
2026-06-09T20:06:54.947Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Fetching prematch events via Playwright browser...
2026-06-09T20:06:54.947Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/prematch...
2026-06-09T20:07:24.127Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-06-09T20:07:24.145Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-06-09T20:07:24.145Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-06-09T20:07:37.730Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/prematch: Object doesn't exist: frame@d7b1aeea1e211fe3c28beaba4fcbd451
2026-06-09T20:07:42.447Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T20:07:43.268Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betboom' name='Betboom'
2026-06-09T20:07:43.890Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T20:08:11.197Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.b.service.BetboomBrowserClient   : Could not find Redux store on https://betboom.ru/sport/live
2026-06-09T20:08:43.897Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T20:08:45.059Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T20:09:11.288Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-06-09T20:09:11.289Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-06-09T20:09:11.289Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-06-09T20:09:45.079Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T20:09:45.917Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T20:09:58.974Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Found 15 active sports with matches
2026-06-09T20:10:05.268Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Clicking sport: Футбол (expected matches: 18)
2026-06-09T20:10:15.138Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Successfully loaded and mapped 18 matches for Футбол
2026-06-09T20:10:16.163Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Clicking sport: Теннис (expected matches: 36)
2026-06-09T20:10:27.324Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Successfully loaded and mapped 36 matches for Теннис
2026-06-09T20:10:27.480Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Clicking sport: Кибер (expected matches: 2)
2026-06-09T20:10:43.601Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betboom' name='Betboom'
2026-06-09T20:10:45.926Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T20:10:50.972Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Subscription check timed out for Кибер
2026-06-09T20:10:50.987Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Баскетбол
2026-06-09T20:10:51.004Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Хоккей
2026-06-09T20:10:51.022Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Настольный теннис
2026-06-09T20:10:51.033Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Киберфутбол
2026-06-09T20:10:51.044Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Волейбол
2026-06-09T20:10:51.052Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Кибербаскетбол
2026-06-09T20:10:51.080Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Киберхоккей
2026-06-09T20:10:51.092Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Футзал
2026-06-09T20:10:51.105Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Бейсбол
2026-06-09T20:10:51.133Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Американский футбол
2026-06-09T20:10:51.143Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Крикет
2026-06-09T20:10:51.164Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Боулинг
2026-06-09T20:10:51.165Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Completed page collection. Total unique events collected: 54
2026-06-09T20:10:55.941Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://siteapi.betboom.ru: Read timed out
2026-06-09T20:10:55.942Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'siteapi.betboom.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:10:55.942Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.64 (config: vpngate-vpn573163202). Requesting rotation...
2026-06-09T20:10:56.268Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 1412 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T20:10:56.268Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:10:56.634Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.47' (config: vpngate-2i6, ID: 853, healthy proxies: 8)
2026-06-09T20:10:56.634Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.47:3128
2026-06-09T20:10:56.634Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.47:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:10:56.638Z  INFO 1 --- [igaming-source-betboom] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'siteapi.betboom.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:10:56.638Z  INFO 1 --- [igaming-source-betboom] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'siteapi.betboom.ru'...
2026-06-09T20:10:56.705Z  INFO 1 --- [igaming-source-betboom] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: siteapi.betboom.ru -> 185.65.148.119
2026-06-09T20:10:56.706Z  INFO 1 --- [igaming-source-betboom] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://siteapi.betboom.ru'...
2026-06-09T20:10:59.037Z  INFO 1 --- [igaming-source-betboom] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'siteapi.betboom.ru' is reachable (HTTP 404). Proxy connectivity verified.
2026-06-09T20:10:59.160Z  INFO 1 --- [igaming-source-betboom] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T20:11:25.456Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.c.engine.AbstractMatchService    : Discovered 54 live betboom events
2026-06-09T20:11:25.459Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled live fetch completed: 54 events discovered

```
#### 📥 Loader (`igaming-source-betboom-loader-647fb6f846-4jc8f` - Running)
**Анализ ошибок:**
```text
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
```
**Последние строки логов:**
```text
2026-06-09T20:11:22.944Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 17 betboom matches
2026-06-09T20:11:27.044Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betboom matches
2026-06-09T20:11:29.258Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 3 betboom matches
```

---

### 🗂️ Букмекер: `betcity`

#### 🕷️ Crawler (`igaming-source-betcity-crawler-55644bf67c-s8p6f` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:11:14.154Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767011942' blockGroupName='IT_T2'
2026-06-09T20:11:14.154Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:14.633Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767012301' blockGroupName='IT_T1'
2026-06-09T20:11:14.633Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:14.725Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767012302' blockGroupName='IT_T2'
2026-06-09T20:11:14.725Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:15.386Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772316301' blockGroupName='IT_T1'
2026-06-09T20:11:15.386Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:15.469Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772316302' blockGroupName='IT_T2'
2026-06-09T20:11:15.469Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:15.941Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780311901' blockGroupName='IT_T1'
2026-06-09T20:11:15.941Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:16.119Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780311902' blockGroupName='IT_T2'
2026-06-09T20:11:16.120Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:16.618Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='3-х очк. попадания. Индивидуальный тотал' dataKey='14780313381' blockGroupName='IT_T1'
2026-06-09T20:11:16.619Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='3-х очк. попадания. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:16.706Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='3-х очк. попадания. Индивидуальный тотал' dataKey='14780313382' blockGroupName='IT_T2'
2026-06-09T20:11:16.706Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='3-х очк. попадания. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:17.116Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780512751' blockGroupName='IT_T1'
2026-06-09T20:11:17.116Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:17.199Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780512752' blockGroupName='IT_T2'
2026-06-09T20:11:17.199Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:17.388Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766187931' blockGroupName='IT_T1'
2026-06-09T20:11:17.388Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:17.432Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766187932' blockGroupName='IT_T2'
2026-06-09T20:11:17.439Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:17.483Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766187941' blockGroupName='IT_T1'
2026-06-09T20:11:17.483Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:17.526Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766187942' blockGroupName='IT_T2'
2026-06-09T20:11:17.526Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:21.340Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766190961' blockGroupName='IT_T1'
2026-06-09T20:11:21.340Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:21.381Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766190962' blockGroupName='IT_T2'
2026-06-09T20:11:21.381Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:21.422Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766190971' blockGroupName='IT_T1'
2026-06-09T20:11:21.423Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:21.464Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766190972' blockGroupName='IT_T2'
2026-06-09T20:11:21.464Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:21.588Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305024, Group=Фора по партиям, SemanticKey=HANDICAP_1_-1.5, ExistingValue=1.62, NewValue=2.0, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T20:11:21.588Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305024, Group=Фора по партиям, SemanticKey=HANDICAP_2_1.5, ExistingValue=2.15, NewValue=1.72, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T20:11:24.640Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766191971' blockGroupName='IT_T1'
2026-06-09T20:11:24.641Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:24.682Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766191972' blockGroupName='IT_T2'
2026-06-09T20:11:24.682Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:24.723Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766191981' blockGroupName='IT_T1'
2026-06-09T20:11:24.723Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:24.764Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766191982' blockGroupName='IT_T2'
2026-06-09T20:11:24.765Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:24.891Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305025, Group=Фора по партиям, SemanticKey=HANDICAP_1_-1.5, ExistingValue=1.9, NewValue=2.75, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T20:11:24.892Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305025, Group=Фора по партиям, SemanticKey=HANDICAP_2_1.5, ExistingValue=1.8, NewValue=1.39, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T20:11:28.858Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767007831' blockGroupName='IT_T1'
2026-06-09T20:11:28.858Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:28.900Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767007832' blockGroupName='IT_T2'
2026-06-09T20:11:28.900Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:28.942Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767007841' blockGroupName='IT_T1'
2026-06-09T20:11:28.943Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:28.985Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767007842' blockGroupName='IT_T2'
2026-06-09T20:11:28.985Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:29.026Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767007851' blockGroupName='IT_T1'
2026-06-09T20:11:29.026Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:29.887Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013671' blockGroupName='IT_T1'
2026-06-09T20:11:29.887Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:29.929Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013672' blockGroupName='IT_T2'
2026-06-09T20:11:29.929Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:29.970Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013681' blockGroupName='IT_T1'
2026-06-09T20:11:29.971Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:29.971Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23308165, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.93, NewValue=1.8, ExistingName='Tm', NewName='Tm'
2026-06-09T20:11:29.991Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23308165, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.78, NewValue=1.9, ExistingName='Tb', NewName='Tb'
2026-06-09T20:11:30.012Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013682' blockGroupName='IT_T2'
2026-06-09T20:11:30.012Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:30.013Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23308165, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.8, ExistingName='Tm', NewName='Tm'
2026-06-09T20:11:30.035Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23308165, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.9, ExistingName='Tb', NewName='Tb'
2026-06-09T20:11:30.062Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013691' blockGroupName='IT_T1'
2026-06-09T20:11:30.062Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:30.107Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013692' blockGroupName='IT_T2'
2026-06-09T20:11:30.107Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:31.658Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772003541' blockGroupName='IT_T1'
2026-06-09T20:11:31.658Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:31.713Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772003542' blockGroupName='IT_T2'
2026-06-09T20:11:31.713Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:31.754Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772003551' blockGroupName='IT_T1'
2026-06-09T20:11:31.754Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:31.801Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772003552' blockGroupName='IT_T2'
2026-06-09T20:11:31.812Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:34.993Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772004551' blockGroupName='IT_T1'
2026-06-09T20:11:34.994Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:35.039Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772004552' blockGroupName='IT_T2'
2026-06-09T20:11:35.039Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:35.085Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772004561' blockGroupName='IT_T1'
2026-06-09T20:11:35.085Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:35.131Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772004562' blockGroupName='IT_T2'
2026-06-09T20:11:35.131Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:37.100Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14773897561' blockGroupName='IT_T1'
2026-06-09T20:11:37.106Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:37.156Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14773897562' blockGroupName='IT_T2'
2026-06-09T20:11:37.156Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:37.225Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14773897571' blockGroupName='IT_T1'
2026-06-09T20:11:37.226Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:37.294Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14773897572' blockGroupName='IT_T2'
2026-06-09T20:11:37.294Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]

```
#### 📥 Loader (`igaming-source-betcity-loader-6c68c59564-s9jvs` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:11:13.740Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1730] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:13.825Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1730] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766694652' blockGroupName='IT_T2'
2026-06-09T20:11:13.826Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1730] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:14.395Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 15 betcity matches
2026-06-09T20:11:16.165Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1743] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='3-х очк. попадания. Индивидуальный тотал' dataKey='14766696071' blockGroupName='IT_T1'
2026-06-09T20:11:16.186Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1743] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='3-х очк. попадания. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:16.279Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1755] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767011941' blockGroupName='IT_T1'
2026-06-09T20:11:16.282Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1755] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:16.368Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1755] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767011942' blockGroupName='IT_T2'
2026-06-09T20:11:16.368Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1755] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:16.397Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1753] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767010211' blockGroupName='IT_T1'
2026-06-09T20:11:16.397Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1753] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:16.397Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1743] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='3-х очк. попадания. Индивидуальный тотал' dataKey='14766696072' blockGroupName='IT_T2'
2026-06-09T20:11:16.397Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1743] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='3-х очк. попадания. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:16.492Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1753] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767010212' blockGroupName='IT_T2'
2026-06-09T20:11:16.492Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1753] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:16.525Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1758] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767012301' blockGroupName='IT_T1'
2026-06-09T20:11:16.526Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1758] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:16.612Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1758] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767012302' blockGroupName='IT_T2'
2026-06-09T20:11:16.612Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1758] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:16.879Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 20 betcity matches
2026-06-09T20:11:18.561Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1770] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='3-х очк. попадания. Индивидуальный тотал' dataKey='14780313381' blockGroupName='IT_T1'
2026-06-09T20:11:18.561Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1770] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='3-х очк. попадания. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:18.591Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1763] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772316301' blockGroupName='IT_T1'
2026-06-09T20:11:18.591Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1763] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:18.596Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1767] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780311901' blockGroupName='IT_T1'
2026-06-09T20:11:18.596Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1767] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:18.679Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1770] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='3-х очк. попадания. Индивидуальный тотал' dataKey='14780313382' blockGroupName='IT_T2'
2026-06-09T20:11:18.680Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1770] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='3-х очк. попадания. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:18.681Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1773] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780512751' blockGroupName='IT_T1'
2026-06-09T20:11:18.681Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1773] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:18.681Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1767] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780311902' blockGroupName='IT_T2'
2026-06-09T20:11:18.681Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1767] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:18.699Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1763] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14772316302' blockGroupName='IT_T2'
2026-06-09T20:11:18.699Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1763] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:18.764Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1773] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780512752' blockGroupName='IT_T2'
2026-06-09T20:11:18.765Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1773] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:18.985Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 18 betcity matches
2026-06-09T20:11:20.652Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 12 betcity matches
2026-06-09T20:11:21.051Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betcity' name='Betcity'
2026-06-09T20:11:22.362Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1805] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766187931' blockGroupName='IT_T1'
2026-06-09T20:11:22.362Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1805] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:22.407Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1805] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766187932' blockGroupName='IT_T2'
2026-06-09T20:11:22.413Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1805] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:22.456Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1805] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766187941' blockGroupName='IT_T1'
2026-06-09T20:11:22.456Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1805] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:22.503Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1805] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766187942' blockGroupName='IT_T2'
2026-06-09T20:11:22.503Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1805] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:25.664Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ad.betcity.ru' is still reachable...
2026-06-09T20:11:26.115Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] .c.e.AbstractBookmakerPerformanceTracker : [betcity] Performance Report - Pages processed: 396, Skipped: 0, Not found: 0, Known errors: {}
2026-06-09T20:11:26.115Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 18 betcity matches
2026-06-09T20:11:28.104Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1824] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766190961' blockGroupName='IT_T1'
2026-06-09T20:11:28.105Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1824] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:28.167Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1824] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766190962' blockGroupName='IT_T2'
2026-06-09T20:11:28.169Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1824] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:28.230Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1824] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766190971' blockGroupName='IT_T1'
2026-06-09T20:11:28.233Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1824] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:28.284Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1824] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766190972' blockGroupName='IT_T2'
2026-06-09T20:11:28.286Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1824] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:28.430Z ERROR 1 --- [igaming-source-betcity] [   virtual-1824] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305024, Group=Фора по партиям, SemanticKey=HANDICAP_1_-1.5, ExistingValue=1.62, NewValue=2.0, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T20:11:28.433Z ERROR 1 --- [igaming-source-betcity] [   virtual-1824] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305024, Group=Фора по партиям, SemanticKey=HANDICAP_2_1.5, ExistingValue=2.15, NewValue=1.72, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T20:11:28.792Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-betcity, host: ad.betcity.ru)
2026-06-09T20:11:31.522Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 23 betcity matches
2026-06-09T20:11:33.580Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1859] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767007831' blockGroupName='IT_T1'
2026-06-09T20:11:33.647Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1859] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:33.652Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013671' blockGroupName='IT_T1'
2026-06-09T20:11:33.653Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:33.679Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1850] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766191971' blockGroupName='IT_T1'
2026-06-09T20:11:33.679Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1850] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:33.810Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1850] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766191972' blockGroupName='IT_T2'
2026-06-09T20:11:33.812Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1850] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:33.858Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1859] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767007832' blockGroupName='IT_T2'
2026-06-09T20:11:33.873Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1859] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:33.859Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013672' blockGroupName='IT_T2'
2026-06-09T20:11:33.875Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:33.899Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1850] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766191981' blockGroupName='IT_T1'
2026-06-09T20:11:33.900Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1850] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:33.916Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1859] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767007841' blockGroupName='IT_T1'
2026-06-09T20:11:33.916Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1859] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:33.917Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013681' blockGroupName='IT_T1'
2026-06-09T20:11:33.932Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:33.932Z ERROR 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23308165, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.93, NewValue=1.8, ExistingName='Tm', NewName='Tm'
2026-06-09T20:11:33.947Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1850] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766191982' blockGroupName='IT_T2'
2026-06-09T20:11:33.948Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1850] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:33.956Z ERROR 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23308165, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.78, NewValue=1.9, ExistingName='Tb', NewName='Tb'
2026-06-09T20:11:33.962Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1859] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767007842' blockGroupName='IT_T2'
2026-06-09T20:11:33.980Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1859] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:33.980Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013682' blockGroupName='IT_T2'
2026-06-09T20:11:33.982Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:33.982Z ERROR 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23308165, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.8, ExistingName='Tm', NewName='Tm'
2026-06-09T20:11:34.002Z ERROR 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23308165, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.9, ExistingName='Tb', NewName='Tb'
2026-06-09T20:11:34.024Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1859] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767007851' blockGroupName='IT_T1'
2026-06-09T20:11:34.024Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1859] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:34.024Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013691' blockGroupName='IT_T1'
2026-06-09T20:11:34.032Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:11:34.076Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767013692' blockGroupName='IT_T2'
2026-06-09T20:11:34.076Z DEBUG 1 --- [igaming-source-betcity] [   virtual-1863] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:11:34.087Z ERROR 1 --- [igaming-source-betcity] [   virtual-1850] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305025, Group=Фора по партиям, SemanticKey=HANDICAP_1_-1.5, ExistingValue=1.9, NewValue=2.75, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T20:11:34.090Z ERROR 1 --- [igaming-source-betcity] [   virtual-1850] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305025, Group=Фора по партиям, SemanticKey=HANDICAP_2_1.5, ExistingValue=1.8, NewValue=1.39, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T20:11:38.500Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betcity matches

```

---

### 🗂️ Букмекер: `betcity-com`

#### 🕷️ Crawler (`igaming-source-betcity-com-crawler-6d574669c8-pr867` - Running)
**Анализ ошибок:**
```text
	at org.springframework.scheduling.support.ScheduledMethodRunnable.runInternal(ScheduledMethodRunnable.java:130) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.lambda$run$2(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at io.micrometer.observation.Observation.observe(Observation.java:498) ~[micrometer-observation-1.14.2.jar!/:1.14.2]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.run(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.config.Task$OutcomeTrackingRunnable.run(Task.java:85) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.runAndReset(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: java.net.UnknownHostException: ad.betsbc.com
	at java.base/java.net.InetAddress$CachedLookup.get(Unknown Source) ~[na:na]
	at java.base/java.net.InetAddress.getAllByName0(Unknown Source) ~[na:na]
	at java.base/java.net.InetAddress.getAllByName(Unknown Source) ~[na:na]
	at org.apache.hc.client5.http.SystemDefaultDnsResolver.resolve(SystemDefaultDnsResolver.java:48) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.io.DefaultHttpClientConnectionOperator.connect(DefaultHttpClientConnectionOperator.java:169) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager.connect(PoolingHttpClientConnectionManager.java:490) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.InternalExecRuntime.connectEndpoint(InternalExecRuntime.java:164) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.InternalExecRuntime.connectEndpoint(InternalExecRuntime.java:174) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ConnectExec.execute(ConnectExec.java:144) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ExecChainElement.execute(ExecChainElement.java:51) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ProtocolExec.execute(ProtocolExec.java:192) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ExecChainElement.execute(ExecChainElement.java:51) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ContentCompressionExec.execute(ContentCompressionExec.java:150) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ExecChainElement.execute(ExecChainElement.java:51) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.HttpRequestRetryExec.execute(HttpRequestRetryExec.java:113) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ExecChainElement.execute(ExecChainElement.java:51) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.RedirectExec.execute(RedirectExec.java:110) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ExecChainElement.execute(ExecChainElement.java:51) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.InternalHttpClient.doExecute(InternalHttpClient.java:174) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.CloseableHttpClient.execute(CloseableHttpClient.java:87) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.CloseableHttpClient.execute(CloseableHttpClient.java:55) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.classic.HttpClient.executeOpen(HttpClient.java:183) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.springframework.http.client.HttpComponentsClientHttpRequest.executeInternal(HttpComponentsClientHttpRequest.java:99) ~[spring-web-6.2.1.jar!/:6.2.1]
	at org.springframework.http.client.AbstractStreamingClientHttpRequest.executeInternal(AbstractStreamingClientHttpRequest.java:71) ~[spring-web-6.2.1.jar!/:6.2.1]
	at org.springframework.http.client.AbstractClientHttpRequest.execute(AbstractClientHttpRequest.java:81) ~[spring-web-6.2.1.jar!/:6.2.1]
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:900) ~[spring-web-6.2.1.jar!/:6.2.1]
	... 18 common frames omitted

2026-06-09T20:11:38.785Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting scheduled LIVE fetch job
2026-06-09T20:11:38.787Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.b.service.BetCityApiClient       : Fetching LIVE from https://ad.betsbc.com/d/on_air/bets?rev=8&add=dep_event&template=1
2026-06-09T20:11:38.977Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.b.service.BetCityApiClient       : Error fetching line info

org.springframework.web.client.ResourceAccessException: I/O error on GET request for "https://ad.betsbc.com/d/on_air/bets": ad.betsbc.com: No address associated with hostname
	at org.springframework.web.client.RestTemplate.createResourceAccessException(RestTemplate.java:926) ~[spring-web-6.2.1.jar!/:6.2.1]
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:906) ~[spring-web-6.2.1.jar!/:6.2.1]
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:801) ~[spring-web-6.2.1.jar!/:6.2.1]
	at org.springframework.web.client.RestTemplate.exchange(RestTemplate.java:683) ~[spring-web-6.2.1.jar!/:6.2.1]
	at pro.datawiki.igaming.source.betcity.service.BetCityApiClient.fetchLine(BetCityApiClient.java:41) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.betcity.scheduler.MatchFetchScheduler.fetchLiveMatches(MatchFetchScheduler.java:24) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.runInternal(ScheduledMethodRunnable.java:130) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.lambda$run$2(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at io.micrometer.observation.Observation.observe(Observation.java:498) ~[micrometer-observation-1.14.2.jar!/:1.14.2]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.run(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.config.Task$OutcomeTrackingRunnable.run(Task.java:85) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.runAndReset(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: java.net.UnknownHostException: ad.betsbc.com: No address associated with hostname
	at java.base/java.net.Inet6AddressImpl.lookupAllHostAddr(Native Method) ~[na:na]
	at java.base/java.net.Inet6AddressImpl.lookupAllHostAddr(Unknown Source) ~[na:na]
	at java.base/java.net.InetAddress$PlatformResolver.lookupByName(Unknown Source) ~[na:na]
	at java.base/java.net.InetAddress.getAddressesFromNameService(Unknown Source) ~[na:na]
	at java.base/java.net.InetAddress$NameServiceAddresses.get(Unknown Source) ~[na:na]
	at java.base/java.net.InetAddress.getAllByName0(Unknown Source) ~[na:na]
	at java.base/java.net.InetAddress.getAllByName(Unknown Source) ~[na:na]
	at org.apache.hc.client5.http.SystemDefaultDnsResolver.resolve(SystemDefaultDnsResolver.java:48) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.io.DefaultHttpClientConnectionOperator.connect(DefaultHttpClientConnectionOperator.java:169) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager.connect(PoolingHttpClientConnectionManager.java:490) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.InternalExecRuntime.connectEndpoint(InternalExecRuntime.java:164) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.InternalExecRuntime.connectEndpoint(InternalExecRuntime.java:174) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ConnectExec.execute(ConnectExec.java:144) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ExecChainElement.execute(ExecChainElement.java:51) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ProtocolExec.execute(ProtocolExec.java:192) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ExecChainElement.execute(ExecChainElement.java:51) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ContentCompressionExec.execute(ContentCompressionExec.java:150) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ExecChainElement.execute(ExecChainElement.java:51) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.HttpRequestRetryExec.execute(HttpRequestRetryExec.java:113) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ExecChainElement.execute(ExecChainElement.java:51) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.RedirectExec.execute(RedirectExec.java:110) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.ExecChainElement.execute(ExecChainElement.java:51) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.InternalHttpClient.doExecute(InternalHttpClient.java:174) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.CloseableHttpClient.execute(CloseableHttpClient.java:87) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.impl.classic.CloseableHttpClient.execute(CloseableHttpClient.java:55) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.apache.hc.client5.http.classic.HttpClient.executeOpen(HttpClient.java:183) ~[httpclient5-5.4.1.jar!/:5.4.1]
	at org.springframework.http.client.HttpComponentsClientHttpRequest.executeInternal(HttpComponentsClientHttpRequest.java:99) ~[spring-web-6.2.1.jar!/:6.2.1]
	at org.springframework.http.client.AbstractStreamingClientHttpRequest.executeInternal(AbstractStreamingClientHttpRequest.java:71) ~[spring-web-6.2.1.jar!/:6.2.1]
	at org.springframework.http.client.AbstractClientHttpRequest.execute(AbstractClientHttpRequest.java:81) ~[spring-web-6.2.1.jar!/:6.2.1]
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:900) ~[spring-web-6.2.1.jar!/:6.2.1]
	... 18 common frames omitted

2026-06-09T20:11:39.012Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betcity-com' name='Betcity'

```
#### 📥 Loader (`igaming-source-betcity-com-loader-6b4664cc66-w6bqd` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
2026-06-09T20:06:36.297Z  INFO 1 --- [igaming-source-betcity] [nio-3042-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 31 ms
2026-06-09T20:08:32.693Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betcity-com' name='Betcity'
2026-06-09T20:11:32.853Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betcity-com' name='Betcity'
```

---

### 🗂️ Букмекер: `betm`

#### 🕷️ Crawler (`igaming-source-betm-crawler-5bc75456f7-mgmz2` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:10:46.996Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780488322' blockGroupName='IT_T2'
2026-06-09T20:10:47.001Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:47.001Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318650, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=2.0, NewValue=1.9, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:47.033Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318650, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.72, NewValue=1.8, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:48.789Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318650, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=6.94, NewValue=10.5, ExistingName='X2', NewName='X2'
2026-06-09T20:10:50.284Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780517571' blockGroupName='IT_T1'
2026-06-09T20:10:50.285Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:50.314Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780517572' blockGroupName='IT_T2'
2026-06-09T20:10:50.314Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:50.342Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780517581' blockGroupName='IT_T1'
2026-06-09T20:10:50.342Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:50.342Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318674, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.76, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:50.366Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318674, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.95, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:50.396Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780517582' blockGroupName='IT_T2'
2026-06-09T20:10:50.396Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:50.397Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318674, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.95, NewValue=1.85, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:50.415Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318674, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.76, NewValue=1.85, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:50.566Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23318674: java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null

java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.extractParams(BetcityOddsProcessor.java:99) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.extractOddsFromSection(BetcityOddsProcessor.java:78) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.processOdds(BetcityOddsProcessor.java:41) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.AbstractBetcityFamilyService.extractFactors(AbstractBetcityFamilyService.java:121) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.AbstractBetcityFamilyService.saveOrUpdateMatchMetadata(AbstractBetcityFamilyService.java:70) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.AbstractBetcityFamilyService.lambda$discoverEvents$0(AbstractBetcityFamilyService.java:53) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityEventDiscoverer.discover(BetcityEventDiscoverer.java:57) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.AbstractBetcityFamilyService.discoverEvents(AbstractBetcityFamilyService.java:51) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.betcity.scheduler.MatchFetchScheduler.fetchLiveMatches(MatchFetchScheduler.java:26) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.runInternal(ScheduledMethodRunnable.java:130) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.lambda$run$2(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at io.micrometer.observation.Observation.observe(Observation.java:498) ~[micrometer-observation-1.14.2.jar!/:1.14.2]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.run(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.config.Task$OutcomeTrackingRunnable.run(Task.java:85) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.runAndReset(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-06-09T20:10:51.816Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23199401, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.23, NewValue=1.05, ExistingName='X2', NewName='X2'
2026-06-09T20:10:53.731Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770513531' blockGroupName='IT_T1'
2026-06-09T20:10:53.732Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:53.836Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770513532' blockGroupName='IT_T2'
2026-06-09T20:10:53.837Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:54.203Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515291' blockGroupName='IT_T1'
2026-06-09T20:10:54.203Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:54.232Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515292' blockGroupName='IT_T2'
2026-06-09T20:10:54.232Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:54.260Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515311' blockGroupName='IT_T1'
2026-06-09T20:10:54.260Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:54.260Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23283813, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=2.46, NewValue=1.02, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:54.275Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23283813, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.48, NewValue=9.8, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:54.290Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515312' blockGroupName='IT_T2'
2026-06-09T20:10:54.290Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:54.291Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23283813, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=3.3, NewValue=1.1, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:54.305Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23283813, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.28, NewValue=5.7, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:54.319Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515301' blockGroupName='IT_T1'
2026-06-09T20:10:54.319Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:54.346Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515302' blockGroupName='IT_T2'
2026-06-09T20:10:54.347Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:55.151Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23283813: java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null

java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.extractParams(BetcityOddsProcessor.java:99) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.extractOddsFromSection(BetcityOddsProcessor.java:78) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.processOdds(BetcityOddsProcessor.java:41) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.AbstractBetcityFamilyService.extractFactors(AbstractBetcityFamilyService.java:121) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.AbstractBetcityFamilyService.saveOrUpdateMatchMetadata(AbstractBetcityFamilyService.java:70) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.AbstractBetcityFamilyService.lambda$discoverEvents$0(AbstractBetcityFamilyService.java:53) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityEventDiscoverer.discover(BetcityEventDiscoverer.java:57) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.AbstractBetcityFamilyService.discoverEvents(AbstractBetcityFamilyService.java:51) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.betcity.scheduler.MatchFetchScheduler.fetchLiveMatches(MatchFetchScheduler.java:26) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.runInternal(ScheduledMethodRunnable.java:130) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.lambda$run$2(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at io.micrometer.observation.Observation.observe(Observation.java:498) ~[micrometer-observation-1.14.2.jar!/:1.14.2]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.run(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.config.Task$OutcomeTrackingRunnable.run(Task.java:85) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.runAndReset(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]

2026-06-09T20:10:55.429Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14765787891' blockGroupName='IT_T1'
2026-06-09T20:10:55.430Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:55.532Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14765787892' blockGroupName='IT_T2'
2026-06-09T20:10:55.532Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:55.822Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : [betm] Discovered 134 live events
2026-06-09T20:10:55.822Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.b.scheduler.MatchFetchScheduler  : Finished LIVE discovery job, discovered: 134
2026-06-09T20:11:23.734Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ad.betcity.ru' is still reachable...
2026-06-09T20:11:24.735Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-betcity, host: ad.betcity.ru)

```
#### 📥 Loader (`igaming-source-betm-loader-788bcfc94b-z5fnj` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:10:46.858Z DEBUG 1 --- [igaming-source-betcity] [    virtual-180] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:46.858Z ERROR 1 --- [igaming-source-betcity] [    virtual-180] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318615, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.95, NewValue=1.85, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:46.872Z ERROR 1 --- [igaming-source-betcity] [    virtual-180] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318615, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.76, NewValue=1.85, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:47.160Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ad.betcity.ru' is still reachable...
2026-06-09T20:10:48.402Z ERROR 1 --- [igaming-source-betcity] [    virtual-180] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318615, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.95, NewValue=2.99, ExistingName='X2', NewName='X2'
2026-06-09T20:10:49.213Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 betm matches
2026-06-09T20:10:49.923Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-betcity, host: ad.betcity.ru)
2026-06-09T20:10:50.320Z DEBUG 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780476811' blockGroupName='IT_T1'
2026-06-09T20:10:50.323Z DEBUG 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:50.353Z DEBUG 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780476812' blockGroupName='IT_T2'
2026-06-09T20:10:50.353Z DEBUG 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:50.385Z DEBUG 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780476821' blockGroupName='IT_T1'
2026-06-09T20:10:50.387Z DEBUG 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:50.387Z ERROR 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318616, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.85, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:50.405Z ERROR 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318616, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.85, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:50.425Z DEBUG 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780476822' blockGroupName='IT_T2'
2026-06-09T20:10:50.425Z DEBUG 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:50.425Z ERROR 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318616, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.85, NewValue=1.76, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:50.440Z ERROR 1 --- [igaming-source-betcity] [    virtual-182] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318616, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.85, NewValue=1.95, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:51.757Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 betm matches
2026-06-09T20:10:52.923Z DEBUG 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780517571' blockGroupName='IT_T1'
2026-06-09T20:10:52.923Z DEBUG 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:52.936Z DEBUG 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780488311' blockGroupName='IT_T1'
2026-06-09T20:10:52.936Z DEBUG 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:52.954Z DEBUG 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780517572' blockGroupName='IT_T2'
2026-06-09T20:10:52.964Z DEBUG 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:52.979Z DEBUG 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780488312' blockGroupName='IT_T2'
2026-06-09T20:10:52.981Z DEBUG 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:53.000Z DEBUG 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780517581' blockGroupName='IT_T1'
2026-06-09T20:10:53.000Z DEBUG 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:53.000Z ERROR 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318674, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.76, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:53.013Z DEBUG 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780488321' blockGroupName='IT_T1'
2026-06-09T20:10:53.014Z DEBUG 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:53.014Z ERROR 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318650, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=2.0, NewValue=1.76, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:53.014Z ERROR 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318674, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.95, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:53.028Z ERROR 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318650, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.72, NewValue=1.95, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:53.028Z DEBUG 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780517582' blockGroupName='IT_T2'
2026-06-09T20:10:53.029Z DEBUG 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:53.029Z ERROR 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318674, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.95, NewValue=1.85, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:53.041Z DEBUG 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14780488322' blockGroupName='IT_T2'
2026-06-09T20:10:53.042Z DEBUG 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:53.042Z ERROR 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318650, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=2.0, NewValue=1.9, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:53.042Z ERROR 1 --- [igaming-source-betcity] [    virtual-184] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318674, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.76, NewValue=1.85, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:53.056Z ERROR 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318650, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.72, NewValue=1.8, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:53.164Z ERROR 1 --- [igaming-source-betcity] [    virtual-184] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null

java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.extractParams(BetcityOddsProcessor.java:99) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.extractOddsFromSection(BetcityOddsProcessor.java:78) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.processOdds(BetcityOddsProcessor.java:41) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityMatchEnricher.processOdds(BetcityMatchEnricher.java:35) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityMatchEnricher.processOdds(BetcityMatchEnricher.java:15) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.AbstractMatchEnricher.enrichAndPush(AbstractMatchEnricher.java:43) ~[igaming-source-core-0.0.1-SNAPSHOT.jar!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.AbstractBetcityFamilyService.loadSingleMatchCard(AbstractBetcityFamilyService.java:140) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService.lambda$loadMatchCards$1(AbstractBaseBookmakerService.java:105) ~[igaming-source-core-0.0.1-SNAPSHOT.jar!/:0.0.1-SNAPSHOT]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.lang.VirtualThread.run(Unknown Source) ~[na:na]

2026-06-09T20:10:53.495Z ERROR 1 --- [igaming-source-betcity] [    virtual-185] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23199401, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.23, NewValue=1.05, ExistingName='X2', NewName='X2'
2026-06-09T20:10:54.551Z ERROR 1 --- [igaming-source-betcity] [    virtual-183] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23318650, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=6.94, NewValue=10.5, ExistingName='X2', NewName='X2'
2026-06-09T20:10:55.617Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 betm matches
2026-06-09T20:10:56.853Z DEBUG 1 --- [igaming-source-betcity] [    virtual-188] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770513531' blockGroupName='IT_T1'
2026-06-09T20:10:56.853Z DEBUG 1 --- [igaming-source-betcity] [    virtual-188] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:56.873Z DEBUG 1 --- [igaming-source-betcity] [    virtual-192] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14765787891' blockGroupName='IT_T1'
2026-06-09T20:10:56.873Z DEBUG 1 --- [igaming-source-betcity] [    virtual-192] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:56.895Z DEBUG 1 --- [igaming-source-betcity] [    virtual-188] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770513532' blockGroupName='IT_T2'
2026-06-09T20:10:56.895Z DEBUG 1 --- [igaming-source-betcity] [    virtual-188] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:56.897Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515291' blockGroupName='IT_T1'
2026-06-09T20:10:56.897Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:56.900Z DEBUG 1 --- [igaming-source-betcity] [    virtual-192] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14765787892' blockGroupName='IT_T2'
2026-06-09T20:10:56.905Z DEBUG 1 --- [igaming-source-betcity] [    virtual-192] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:56.933Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515292' blockGroupName='IT_T2'
2026-06-09T20:10:56.936Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:56.972Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515311' blockGroupName='IT_T1'
2026-06-09T20:10:56.972Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:56.973Z ERROR 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23283813, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=2.46, NewValue=1.02, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:56.991Z ERROR 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23283813, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.48, NewValue=9.8, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:57.008Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515312' blockGroupName='IT_T2'
2026-06-09T20:10:57.008Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:57.008Z ERROR 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23283813, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=3.3, NewValue=1.1, ExistingName='Tm', NewName='Tm'
2026-06-09T20:10:57.022Z ERROR 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23283813, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.28, NewValue=5.7, ExistingName='Tb', NewName='Tb'
2026-06-09T20:10:57.035Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515301' blockGroupName='IT_T1'
2026-06-09T20:10:57.035Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T20:10:57.063Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14770515302' blockGroupName='IT_T2'
2026-06-09T20:10:57.064Z DEBUG 1 --- [igaming-source-betcity] [    virtual-190] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T20:10:57.500Z ERROR 1 --- [igaming-source-betcity] [    virtual-190] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null

java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.extractParams(BetcityOddsProcessor.java:99) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.extractOddsFromSection(BetcityOddsProcessor.java:78) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityOddsProcessor.processOdds(BetcityOddsProcessor.java:41) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityMatchEnricher.processOdds(BetcityMatchEnricher.java:35) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.BetcityMatchEnricher.processOdds(BetcityMatchEnricher.java:15) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.AbstractMatchEnricher.enrichAndPush(AbstractMatchEnricher.java:43) ~[igaming-source-core-0.0.1-SNAPSHOT.jar!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.betcity.service.AbstractBetcityFamilyService.loadSingleMatchCard(AbstractBetcityFamilyService.java:140) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService.lambda$loadMatchCards$1(AbstractBaseBookmakerService.java:105) ~[igaming-source-core-0.0.1-SNAPSHOT.jar!/:0.0.1-SNAPSHOT]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.lang.VirtualThread.run(Unknown Source) ~[na:na]

2026-06-09T20:10:57.530Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 8 betm matches

```

---

### 🗂️ Букмекер: `bettery`

#### 🕷️ Crawler (`igaming-source-bettery-crawler-6dd8bc8c49-t4r4w` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:10:27.565Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:10:27.873Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.66' (config: vpngate-patito, ID: 513, healthy proxies: 7)
2026-06-09T20:10:27.874Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.66:3128
2026-06-09T20:10:27.874Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.66:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:10:27.876Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'bettery.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:10:27.879Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'bettery.ru'...
2026-06-09T20:10:27.996Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: bettery.ru -> 185.65.151.131
2026-06-09T20:10:27.996Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://bettery.ru'...
2026-06-09T20:10:28.052Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://bettery.ru/urls.json
2026-06-09T20:10:28.424Z ERROR 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://bettery.ru: No route to host
2026-06-09T20:10:28.428Z  WARN 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'bettery.ru': java.net.NoRouteToHostException: No route to host
2026-06-09T20:10:28.429Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:10:33.430Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'bettery.ru'...
2026-06-09T20:10:33.430Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: bettery.ru -> 185.65.151.131
2026-06-09T20:10:33.432Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://bettery.ru'...
2026-06-09T20:10:36.028Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.c.e.f.s.JsonSchemaValidator      : JSON schema validation successful
2026-06-09T20:10:36.030Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Mirrors refreshed from urls.json. LB mirrors: 2, Direct mirrors: 0
2026-06-09T20:10:36.030Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] .s.c.e.f.s.AbstractFonbetFamilyApiClient : First LB mirror: https://line01.at58f5-resources.com
2026-06-09T20:10:36.030Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] .s.c.e.f.s.AbstractFonbetFamilyApiClient : First Direct mirror: none
2026-06-09T20:10:36.030Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Attempting fetch from mirror (1 of 2): https://line01.at58f5-resources.com (ver: 0)
2026-06-09T20:10:36.030Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.14.45:3128 to http://10.42.14.66:3128. Recreating browser...
2026-06-09T20:10:36.503Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.14.66:3128
2026-06-09T20:10:36.549Z ERROR 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://bettery.ru: No route to host
2026-06-09T20:10:36.549Z  WARN 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'bettery.ru': java.net.NoRouteToHostException: No route to host
2026-06-09T20:10:36.549Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:10:37.862Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Pre-visiting homepage to establish session: https://bettery.ru
2026-06-09T20:10:41.551Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Resolving DNS for 'bettery.ru'...
2026-06-09T20:10:41.551Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] DNS resolved: bettery.ru -> 185.65.151.131
2026-06-09T20:10:41.551Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Checking HTTP connectivity to 'https://bettery.ru'...
2026-06-09T20:10:45.936Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1781035836030, falling back to direct navigation: Error {
  message='net::ERR_TIMED_OUT at https://bettery.ru/
  name='Error
  stack='Error: net::ERR_TIMED_OUT at https://bettery.ru/
    at FrameSession._navigate (/tmp/playwright-java-2524228618611714787/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-2524228618611714787/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://bettery.ru/", waiting until "load"

2026-06-09T20:10:49.628Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1781035836030
2026-06-09T20:10:49.809Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1781035836030: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1781035836030
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1781035836030
    at FrameSession._navigate (/tmp/playwright-java-2524228618611714787/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-2524228618611714787/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1781035836030", waiting until "domcontentloaded"

2026-06-09T20:10:49.810Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.66 (config: vpngate-patito). Requesting rotation...
2026-06-09T20:10:50.121Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 513 (service: igaming-source-bettery, host: bettery.ru)
2026-06-09T20:10:50.121Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:10:50.532Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.67' (config: vpngate-vpn269589023, ID: 808, healthy proxies: 8)
2026-06-09T20:10:50.535Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.67:3128
2026-06-09T20:10:50.536Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.67:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:10:50.542Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'bettery.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:10:50.547Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'bettery.ru'...
2026-06-09T20:10:50.547Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: bettery.ru -> 185.65.151.131
2026-06-09T20:10:50.547Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://bettery.ru'...
2026-06-09T20:10:50.551Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Mirror https://line01.at58f5-resources.com returned empty/null response for URL: https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1781035836030
2026-06-09T20:10:50.551Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Mirror https://line01.at58f5-resources.com attempt 1 failed. Trying next available mirror.
2026-06-09T20:10:51.054Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Attempting fetch from mirror (2 of 2): https://line51.at58f5-resources.com (ver: 0)
2026-06-09T20:10:51.054Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.14.66:3128 to http://10.42.14.67:3128. Recreating browser...
2026-06-09T20:10:51.265Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.14.67:3128
2026-06-09T20:10:51.562Z ERROR 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://bettery.ru: Connect timed out
2026-06-09T20:10:51.563Z  WARN 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 3/3] Connectivity check failed for 'bettery.ru': java.net.SocketTimeoutException: Connect timed out
2026-06-09T20:10:51.563Z ERROR 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'bettery.ru' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T20:10:51.563Z  WARN 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity verification failed: 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'bettery.ru' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status. Will try to rotate on next failure.
2026-06-09T20:10:52.388Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Pre-visiting homepage to establish session: https://bettery.ru
2026-06-09T20:11:00.563Z ERROR 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://bettery.ru: Read timed out
2026-06-09T20:11:00.565Z  WARN 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'bettery.ru': java.net.SocketTimeoutException: Read timed out
2026-06-09T20:11:00.566Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:11:04.903Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : FonbetFamily API fetch stats: failed to load 4 out of 8 pages in the last cycle. Reasons: {Empty response=4}
2026-06-09T20:11:05.568Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'bettery.ru'...
2026-06-09T20:11:05.676Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: bettery.ru -> 185.65.151.131
2026-06-09T20:11:05.676Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://bettery.ru'...
2026-06-09T20:11:15.694Z ERROR 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://bettery.ru: Read timed out
2026-06-09T20:11:15.694Z  WARN 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'bettery.ru': java.net.SocketTimeoutException: Read timed out
2026-06-09T20:11:15.694Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:11:20.694Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Resolving DNS for 'bettery.ru'...
2026-06-09T20:11:20.695Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] DNS resolved: bettery.ru -> 185.65.151.131
2026-06-09T20:11:20.695Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Checking HTTP connectivity to 'https://bettery.ru'...
2026-06-09T20:11:22.394Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line51.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1781035851053, falling back to direct navigation: Error {
  message='Timeout 30000ms exceeded.
  name='TimeoutError
  stack='TimeoutError: Timeout 30000ms exceeded.
    at ProgressController.run (/tmp/playwright-java-2524228618611714787/package/lib/server/progress.js:78:26)
    at Frame.goto (/tmp/playwright-java-2524228618611714787/package/lib/server/frames.js:517:23)
    at FrameDispatcher.goto (/tmp/playwright-java-2524228618611714787/package/lib/server/dispatchers/frameDispatcher.js:81:119)
    at FrameDispatcher._handleCommand (/tmp/playwright-java-2524228618611714787/package/lib/server/dispatchers/dispatcher.js:94:40)
    at DispatcherConnection.dispatch (/tmp/playwright-java-2524228618611714787/package/lib/server/dispatchers/dispatcher.js:365:39)
}
Call log:
- navigating to "https://bettery.ru/", waiting until "load"

2026-06-09T20:11:27.875Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'bettery.ru' is still reachable...
2026-06-09T20:11:32.023Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 808 (service: igaming-source-bettery, host: bettery.ru)
2026-06-09T20:11:32.068Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'bettery.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:11:32.311Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 808 (service: igaming-source-bettery, host: bettery.ru)

```
#### 📥 Loader (`igaming-source-bettery-loader-95484bd9c-49h97` - Running)
**Анализ ошибок:**
```text

 :: Spring Boot ::                (v3.4.1)

2026-06-09T20:04:08.856Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.source.fonbet.FonbetApplication    : Starting FonbetApplication v0.0.1-SNAPSHOT using Java 21.0.10 with PID 1 (/app/app.jar started by root in /app)
2026-06-09T20:04:08.867Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.source.fonbet.FonbetApplication    : The following 1 profile is active: "dev"
2026-06-09T20:04:11.899Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:04:11.900Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-06-09T20:04:12.447Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 529 ms. Found 7 JPA repository interfaces.
2026-06-09T20:04:13.978Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:04:13.984Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T20:04:14.048Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 33 ms. Found 0 Redis repository interfaces.
2026-06-09T20:04:15.269Z  INFO 1 --- [igaming-source-bettery] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=1dfb7a37-3018-3172-a323-bb8c4835e8c8
2026-06-09T20:04:18.550Z  WARN 1 --- [igaming-source-bettery] [           main] io.undertow.websockets.jsr               : UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used
2026-06-09T20:04:18.603Z  INFO 1 --- [igaming-source-bettery] [           main] io.undertow.servlet                      : Initializing Spring embedded WebApplicationContext
2026-06-09T20:04:18.605Z  INFO 1 --- [igaming-source-bettery] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 9547 ms
2026-06-09T20:04:22.141Z  INFO 1 --- [igaming-source-bettery] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:04:22.302Z  INFO 1 --- [igaming-source-bettery] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:04:22.384Z  INFO 1 --- [igaming-source-bettery] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:04:23.298Z  INFO 1 --- [igaming-source-bettery] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:04:23.406Z  INFO 1 --- [igaming-source-bettery] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:04:24.211Z  INFO 1 --- [igaming-source-bettery] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@104cf647
2026-06-09T20:04:24.246Z  INFO 1 --- [igaming-source-bettery] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:04:24.548Z  INFO 1 --- [igaming-source-bettery] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:04:28.315Z  INFO 1 --- [igaming-source-bettery] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:04:28.892Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:28.892Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:29.093Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:29.096Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:29.154Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:29.154Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:04:29.229Z  INFO 1 --- [igaming-source-bettery] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:04:30.774Z  INFO 1 --- [igaming-source-bettery] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:04:34.686Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:04:34.688Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:04:34.705Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:04:34.710Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:04:34.714Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:04:34.716Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:04:37.654Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:04:49.444Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.48' (config: vpngate-public-vpn-197, ID: 854, healthy proxies: 11)
2026-06-09T20:04:49.444Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.48:3128
2026-06-09T20:04:49.445Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.48:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:04:49.465Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'bettery.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:04:49.465Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'bettery.ru'...
2026-06-09T20:04:49.515Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:04:49.516Z  WARN 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.browser.BrowserService      : XVFB_HEADED requested but DISPLAY missing. Falling back to HEADLESS_STEALTH.
2026-06-09T20:04:49.540Z  INFO 1 --- [igaming-source-bettery] [           main] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Initializing FonbetApiClient with Universal Browser Service
2026-06-09T20:04:49.544Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.e.f.s.JsonSchemaValidator      : Loaded Fonbet schema from /fonbet/urls-schema.json
2026-06-09T20:04:49.577Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: bettery.ru -> 185.65.151.131
2026-06-09T20:04:49.578Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://bettery.ru'...
2026-06-09T20:04:50.658Z  WARN 1 --- [igaming-source-bettery] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:04:53.078Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'bettery.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:04:54.927Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-bettery, host: bettery.ru)
2026-06-09T20:04:56.777Z  INFO 1 --- [igaming-source-bettery] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:04:57.462Z  INFO 1 --- [igaming-source-bettery] [           main] io.undertow                              : starting server: Undertow - 2.3.18.Final
2026-06-09T20:04:57.530Z  INFO 1 --- [igaming-source-bettery] [           main] org.xnio                                 : XNIO version 3.8.16.Final
2026-06-09T20:04:57.584Z  INFO 1 --- [igaming-source-bettery] [           main] org.xnio.nio                             : XNIO NIO Implementation Version 3.8.16.Final
2026-06-09T20:04:58.147Z  INFO 1 --- [igaming-source-bettery] [           main] org.jboss.threads                        : JBoss Threads version 3.5.0.Final
2026-06-09T20:04:58.476Z  INFO 1 --- [igaming-source-bettery] [           main] o.s.b.w.e.undertow.UndertowWebServer     : Undertow started on port 3042 (http) with context path '/'
2026-06-09T20:04:58.615Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.source.fonbet.FonbetApplication    : Started FonbetApplication in 51.879 seconds (process running for 53.875)
2026-06-09T20:04:58.617Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'bettery' (display: 'Fonbet')
2026-06-09T20:04:58.723Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bettery' name='Fonbet'
2026-06-09T20:04:58.772Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bettery' name='Fonbet'
2026-06-09T20:05:28.578Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'bettery.ru' is still reachable...
2026-06-09T20:05:38.330Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-bettery, host: bettery.ru)
2026-06-09T20:06:38.332Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'bettery.ru' is still reachable...
2026-06-09T20:06:55.005Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-bettery, host: bettery.ru)
2026-06-09T20:07:06.814Z  INFO 1 --- [igaming-source-bettery] [  XNIO-1 task-2] io.undertow.servlet                      : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:07:06.816Z  INFO 1 --- [igaming-source-bettery] [  XNIO-1 task-2] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:07:06.819Z  INFO 1 --- [igaming-source-bettery] [  XNIO-1 task-2] o.s.web.servlet.DispatcherServlet        : Completed initialization in 3 ms
2026-06-09T20:07:55.017Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'bettery.ru' is still reachable...
2026-06-09T20:07:58.847Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bettery' name='Fonbet'
2026-06-09T20:08:05.041Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://bettery.ru: Read timed out
2026-06-09T20:08:05.041Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'bettery.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:08:05.041Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.48 (config: vpngate-public-vpn-197). Requesting rotation...
2026-06-09T20:08:13.824Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported block for config 854 (service: igaming-source-bettery, host: bettery.ru)
2026-06-09T20:08:13.824Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:15.698Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.60' (config: vpngate-vpn332519676, ID: 444, healthy proxies: 8)
2026-06-09T20:08:15.698Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.60:3128
2026-06-09T20:08:15.699Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.60:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:15.700Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'bettery.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:15.701Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'bettery.ru'...
2026-06-09T20:08:15.771Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: bettery.ru -> 185.65.151.131
2026-06-09T20:08:15.772Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://bettery.ru'...
2026-06-09T20:08:16.581Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'bettery.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:08:18.622Z  INFO 1 --- [igaming-source-bettery] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-bettery, host: bettery.ru)
2026-06-09T20:09:15.701Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'bettery.ru' is still reachable...
2026-06-09T20:09:16.719Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-bettery, host: bettery.ru)
2026-06-09T20:10:16.729Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'bettery.ru' is still reachable...
2026-06-09T20:10:17.584Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-bettery, host: bettery.ru)
2026-06-09T20:10:58.935Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bettery' name='Fonbet'
2026-06-09T20:11:17.585Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'bettery.ru' is still reachable...
2026-06-09T20:11:18.547Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-bettery, host: bettery.ru)

```

---

### 🗂️ Букмекер: `dafabet`

#### 🕷️ Crawler (`igaming-source-dafabet-crawler-86b6b4488b-d79kx` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:07:31.017Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=125, NEW=0, PENDING=0, PROCESSED=125, FAILED=0
2026-06-09T20:08:30.890Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=125, NEW=0, PENDING=0, PROCESSED=125, FAILED=0
2026-06-09T20:09:30.900Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=125, NEW=0, PENDING=0, PROCESSED=125, FAILED=0
2026-06-09T20:10:30.891Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=150, NEW=0, PENDING=0, PROCESSED=150, FAILED=0
2026-06-09T20:11:30.884Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=150, NEW=0, PENDING=0, PROCESSED=150, FAILED=0
```
**Последние строки логов:**
```text
2026-06-09T20:11:29.553Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.d.service.DafabetApiClient       : Navigating to https://www.dafabet.com/en/sports and setting up WebSocket frames interceptor...
2026-06-09T20:11:30.763Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.d.service.DafabetApiClient       : Waiting 25 seconds for WebSocket data snapshot stream to accumulate...
2026-06-09T20:11:30.884Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=150, NEW=0, PENDING=0, PROCESSED=150, FAILED=0
```
#### 📥 Loader (`igaming-source-dafabet-loader-5d677df78d-jzbln` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:07:20.128Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=125, NEW=0, PENDING=0, PROCESSED=125, FAILED=0
2026-06-09T20:08:20.092Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=125, NEW=0, PENDING=0, PROCESSED=125, FAILED=0
2026-06-09T20:09:20.106Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=125, NEW=0, PENDING=0, PROCESSED=125, FAILED=0
2026-06-09T20:10:20.090Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=128, NEW=7, PENDING=0, PROCESSED=121, FAILED=0
2026-06-09T20:11:20.093Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=150, NEW=0, PENDING=0, PROCESSED=150, FAILED=0
```
**Последние строки логов:**
```text
2026-06-09T20:10:30.013Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 6 dafabet matches
2026-06-09T20:11:20.093Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=150, NEW=0, PENDING=0, PROCESSED=150, FAILED=0
2026-06-09T20:11:22.799Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='dafabet' name='Dafabet'
```

---

### 🗂️ Букмекер: `fansport`

#### 🕷️ Crawler (`igaming-source-fansport-crawler-6d7bdd4b99-6lzrx` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:07:08.576Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:07:08.673Z  INFO 1 --- [igaming-source-fansport] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:07:16.452Z  INFO 1 --- [igaming-source-fansport] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:07:59.180Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:05.310Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.60' (config: vpngate-vpn332519676, ID: 444, healthy proxies: 7)
2026-06-09T20:08:05.321Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.60:3128
2026-06-09T20:08:05.322Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.60:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:05.340Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'fan-sport.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:05.423Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'fan-sport.com'...
2026-06-09T20:08:05.672Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:08:05.681Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: fan-sport.com -> 92.204.167.26
2026-06-09T20:08:05.689Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://fan-sport.com'...
2026-06-09T20:08:05.689Z  WARN 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.browser.BrowserService      : XVFB_HEADED requested but DISPLAY missing. Falling back to HEADLESS_STEALTH.
2026-06-09T20:08:07.727Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:08:07.753Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:08:07.784Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:08:07.789Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:08:07.833Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:08:07.836Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:08:08.791Z ERROR 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fan-sport.com: Remote host terminated the handshake
2026-06-09T20:08:08.815Z  WARN 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'fan-sport.com': javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
2026-06-09T20:08:08.820Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:08:11.140Z  WARN 1 --- [igaming-source-fansport] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:08:13.823Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'fan-sport.com'...
2026-06-09T20:08:13.824Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: fan-sport.com -> 92.204.167.26
2026-06-09T20:08:13.824Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://fan-sport.com'...
2026-06-09T20:08:16.221Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'fan-sport.com' is reachable (HTTP 203). Proxy connectivity verified.
2026-06-09T20:08:18.576Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:08:21.154Z  INFO 1 --- [igaming-source-fansport] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:08:21.536Z  INFO 1 --- [igaming-source-fansport] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3054 (http) with context path '/'
2026-06-09T20:08:21.709Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting periodic event discovery for Betb2b Family...
2026-06-09T20:08:21.710Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .d.i.s.c.e.x.s.AbstractXbetFamilyService : Starting Xbet Family event discovery for bookmaker: fansport...
2026-06-09T20:08:21.710Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Rewrote URL from https://fan-sport.com/LiveFeed/Get1xMatchByLeague?sports=1 to https://fan-sport.com/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=110 (useServiceApi=true)
2026-06-09T20:08:21.711Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetching LIVE from service-api URL: https://fan-sport.com/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=110
2026-06-09T20:08:21.712Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-06-09T20:08:21.755Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.source.betb2b.Betb2bApplication    : Started Betb2bApplication in 178.071 seconds (process running for 185.971)
2026-06-09T20:08:21.932Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='fansport' name='Xbet-family'
2026-06-09T20:08:23.230Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'fansport' (display: 'Xbet-family')
2026-06-09T20:08:23.453Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='fansport' name='Xbet-family'
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-06-09T20:08:27.653Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.15.60:3128
2026-06-09T20:08:29.325Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Pre-visiting homepage to establish session: https://fan-sport.com
2026-06-09T20:08:33.116Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/bff-api/config/group/get?groups=b.core,d.core&lang=en
2026-06-09T20:08:33.520Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/bff-api/config/group/get?groups=b.core,d.core&lang=en (status=200)
2026-06-09T20:08:33.592Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/bff-api/config/group/get?groups=d.customize&lang=en
2026-06-09T20:08:33.596Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/analytics-module-api/v1/analytics?projectId=1099&domain=fan-sport.com
2026-06-09T20:08:33.768Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/bff-api/config/group/get?groups=d.customize&lang=en (status=200)
2026-06-09T20:08:33.776Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/fc
2026-06-09T20:08:33.783Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/analytics-module-api/v1/analytics?projectId=1099&domain=fan-sport.com (status=200)
2026-06-09T20:08:33.976Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/fc (status=204)
2026-06-09T20:08:33.983Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/ab.json
2026-06-09T20:08:34.005Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/bff-api/config/group/get?groups=d.technical,d.global&lang=en
2026-06-09T20:08:34.142Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/ab.json (status=200)
2026-06-09T20:08:34.172Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json
2026-06-09T20:08:34.173Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json
2026-06-09T20:08:34.359Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/bff-api/config/group/get?groups=d.technical,d.global&lang=en (status=200)
2026-06-09T20:08:34.502Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json (status=200)
2026-06-09T20:08:34.511Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json (status=200)
2026-06-09T20:08:35.928Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json
2026-06-09T20:08:36.162Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/bff-api/config/v2/contacts.json?lang=en&country=172&isVipUser=false
2026-06-09T20:08:36.165Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Detected country code from request: 172
2026-06-09T20:08:36.200Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json (status=200)
2026-06-09T20:08:36.489Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/bff-api/config/v2/contacts.json?lang=en&country=172&isVipUser=false (status=200)
2026-06-09T20:08:37.384Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Homepage redirect status: https://fan-sport.com -> https://fan-sport.com/en/block, isCrossDomain=false
2026-06-09T20:08:37.385Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Normalized BetB2B relative fetch URL: /service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=172&partner=110&virtualSports=true
2026-06-09T20:08:37.385Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Executing API fetch from page context: /service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=172&partner=110&virtualSports=true
2026-06-09T20:08:37.427Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=172&partner=110&virtualSports=true
2026-06-09T20:08:37.653Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=172&partner=110&virtualSports=true (status=200)
2026-06-09T20:08:37.943Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":3},"PS"
2026-06-09T20:08:37.944Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Successfully fetched data from service-api URL
2026-06-09T20:08:47.383Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : [fansport/EU] Discovered 33 live events
2026-06-09T20:08:47.385Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Rewrote URL from https://fan-sport.com/LineFeed/Get1xMatchByLeague?sports=1 to https://fan-sport.com/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=110 (useServiceApi=true)
2026-06-09T20:08:47.386Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetching PREMATCH from service-api URL: https://fan-sport.com/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=110
2026-06-09T20:08:47.909Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Pre-visiting homepage to establish session: https://fan-sport.com
2026-06-09T20:08:51.684Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:08:51.806Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/bff-api/config/group/get?groups=b.core,d.core&lang=en
2026-06-09T20:08:52.201Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/bff-api/config/group/get?groups=b.core,d.core&lang=en (status=200)
2026-06-09T20:08:52.569Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Homepage redirect status: https://fan-sport.com -> https://fan-sport.com/en/block, isCrossDomain=false
2026-06-09T20:08:52.570Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Normalized BetB2B relative fetch URL: /service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=172&partner=110&virtualSports=true
2026-06-09T20:08:52.570Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Executing API fetch from page context: /service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=172&partner=110&virtualSports=true
2026-06-09T20:08:52.839Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/bff-api/config/group/get?groups=d.customize&lang=en
2026-06-09T20:08:52.858Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/analytics-module-api/v1/analytics?projectId=1099&domain=fan-sport.com
2026-06-09T20:08:52.950Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/bff-api/config/group/get?groups=d.customize&lang=en (status=200)
2026-06-09T20:08:53.482Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/analytics-module-api/v1/analytics?projectId=1099&domain=fan-sport.com (status=200)
2026-06-09T20:08:53.517Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=172&partner=110&virtualSports=true
2026-06-09T20:08:53.575Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/fc
2026-06-09T20:08:53.735Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:08:53.865Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=172&partner=110&virtualSports=true (status=200)
2026-06-09T20:08:53.880Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/fc (status=204)
2026-06-09T20:08:53.882Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/ab.json
2026-06-09T20:08:54.048Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://fan-sport.com/bff-api/config/group/get?groups=d.technical,d.global&lang=en
2026-06-09T20:08:54.061Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://fan-sport.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/ab.json (status=200)
2026-06-09T20:08:54.073Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":10266,"GVE":1,"HL":true,"RLI
2026-06-09T20:08:54.074Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Successfully fetched data from service-api URL
2026-06-09T20:09:00.346Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : [fansport/EU] Discovered 50 prematch events
2026-06-09T20:09:53.736Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:09:55.420Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:10:55.424Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:10:56.920Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:11:22.077Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='fansport' name='Xbet-family'

```
#### 📥 Loader (`igaming-source-fansport-loader-64957cdc8d-w8tsc` - Running)
**Анализ ошибок:**
```text
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:00:11.498Z  INFO 1 --- [igaming-source-fansport] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:00:12.167Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:12.180Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:12.324Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:12.325Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:12.373Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:12.373Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:00:12.417Z  INFO 1 --- [igaming-source-fansport] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:00:14.303Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:00:24.491Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy 'proxy-vpn-pool.service-proxy.svc.cluster.local' (config: vpngate-vpn537213471, ID: 601, healthy proxies: 15)
2026-06-09T20:00:24.491Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: proxy-vpn-pool.service-proxy.svc.cluster.local:3128
2026-06-09T20:00:24.497Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:00:24.541Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'fan-sport.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:00:24.542Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'fan-sport.com'...
2026-06-09T20:00:24.726Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:00:24.774Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: fan-sport.com -> 92.204.167.26
2026-06-09T20:00:24.775Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://fan-sport.com'...
2026-06-09T20:00:28.512Z ERROR 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fan-sport.com: Remote host terminated the handshake
2026-06-09T20:00:28.527Z  WARN 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'fan-sport.com': javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
2026-06-09T20:00:28.528Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:00:30.029Z  INFO 1 --- [igaming-source-fansport] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:00:33.532Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'fan-sport.com'...
2026-06-09T20:00:33.538Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: fan-sport.com -> 92.204.167.26
2026-06-09T20:00:33.545Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://fan-sport.com'...
2026-06-09T20:00:42.992Z ERROR 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fan-sport.com: Unexpected end of file from server
2026-06-09T20:00:43.009Z  WARN 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'fan-sport.com': java.net.SocketException: Unexpected end of file from server
2026-06-09T20:00:43.017Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:00:46.924Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:00:46.949Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:00:47.002Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:00:47.025Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:00:47.050Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:00:47.063Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:00:48.023Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Resolving DNS for 'fan-sport.com'...
2026-06-09T20:00:48.029Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] DNS resolved: fan-sport.com -> 92.204.167.26
2026-06-09T20:00:48.029Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Checking HTTP connectivity to 'https://fan-sport.com'...
2026-06-09T20:00:50.990Z  WARN 1 --- [igaming-source-fansport] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:01:03.670Z  INFO 1 --- [igaming-source-fansport] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:01:04.705Z  INFO 1 --- [igaming-source-fansport] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3054 (http) with context path '/'
2026-06-09T20:01:04.954Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.source.betb2b.Betb2bApplication    : Started Betb2bApplication in 76.593 seconds (process running for 78.329)
2026-06-09T20:01:05.451Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='fansport' name='Xbet-family'
2026-06-09T20:01:06.508Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'fansport' (display: 'Xbet-family')
2026-06-09T20:01:06.639Z  INFO 1 --- [igaming-source-fansport] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='fansport' name='Xbet-family'
2026-06-09T20:01:11.669Z ERROR 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fan-sport.com: Unexpected end of file from server
2026-06-09T20:01:11.670Z  WARN 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 3/3] Connectivity check failed for 'fan-sport.com': java.net.SocketException: Unexpected end of file from server
2026-06-09T20:01:11.674Z ERROR 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'fan-sport.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T20:01:11.674Z  WARN 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity verification failed: 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'fan-sport.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status. Will try to rotate on next failure.
2026-06-09T20:01:34.868Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:01:56.229Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:02:54.817Z  INFO 1 --- [igaming-source-fansport] [nio-3054-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:02:54.820Z  INFO 1 --- [igaming-source-fansport] [nio-3054-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:02:54.850Z  INFO 1 --- [igaming-source-fansport] [nio-3054-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 29 ms
2026-06-09T20:02:56.244Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:02:58.995Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:03:59.002Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:04:05.614Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='fansport' name='Xbet-family'
2026-06-09T20:04:28.534Z ERROR 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fan-sport.com: Remote host terminated the handshake
2026-06-09T20:04:28.534Z ERROR 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'fan-sport.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:04:28.534Z  WARN 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: proxy-vpn-pool.service-proxy.svc.cluster.local (config: vpngate-vpn537213471). Requesting rotation...
2026-06-09T20:04:29.685Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 601 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:04:29.685Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:04:41.606Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.48' (config: vpngate-public-vpn-197, ID: 854, healthy proxies: 11)
2026-06-09T20:04:41.607Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.48:3128
2026-06-09T20:04:41.609Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.48:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:04:41.612Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'fan-sport.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:04:41.613Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'fan-sport.com'...
2026-06-09T20:04:41.811Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: fan-sport.com -> 92.204.167.26
2026-06-09T20:04:41.812Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://fan-sport.com'...
2026-06-09T20:04:53.558Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'fan-sport.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:04:55.770Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:05:41.613Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:05:53.005Z ERROR 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fan-sport.com: Unexpected end of file from server
2026-06-09T20:05:53.005Z ERROR 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'fan-sport.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:05:53.005Z  WARN 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.48 (config: vpngate-public-vpn-197). Requesting rotation...
2026-06-09T20:05:57.652Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 854 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:05:57.653Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:06:05.665Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.49' (config: vpngate-vpn560167748, ID: 1083, healthy proxies: 10)
2026-06-09T20:06:05.666Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.49:3128
2026-06-09T20:06:05.666Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.49:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:06:05.666Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'fan-sport.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:06:05.667Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'fan-sport.com'...
2026-06-09T20:06:05.731Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: fan-sport.com -> 92.204.167.26
2026-06-09T20:06:05.731Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://fan-sport.com'...
2026-06-09T20:06:11.149Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'fan-sport.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:06:15.824Z  INFO 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:07:05.667Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:07:06.111Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='fansport' name='Xbet-family'
2026-06-09T20:07:17.133Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:08:17.135Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:08:26.906Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:09:26.911Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:09:33.226Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:10:06.226Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='fansport' name='Xbet-family'
2026-06-09T20:10:33.227Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T20:10:38.418Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T20:11:38.422Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...

```

---

### 🗂️ Букмекер: `fon-bet-by`

#### 🕷️ Crawler (`igaming-source-fon-bet-by-crawler-5df8cbcb9f-pkrvc` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```
#### 📥 Loader (`igaming-source-fon-bet-by-loader-748bd9f988-xnx8r` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:04:10.114Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:10.313Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:10.314Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:10.347Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:10.348Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
```
**Последние строки логов:**
```text
2026-06-09T20:10:35.696Z  INFO 1 --- [igaming-source-fon-bet-by] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-fon-bet-by, host: fonbet.by)
2026-06-09T20:11:35.702Z  INFO 1 --- [igaming-source-fon-bet-by] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fonbet.by' is still reachable...
2026-06-09T20:11:36.969Z  INFO 1 --- [igaming-source-fon-bet-by] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-fon-bet-by, host: fonbet.by)
```

---

### 🗂️ Букмекер: `fon-bet-kz`

#### 🕷️ Crawler (`igaming-source-fon-bet-kz-crawler-7dc7fb7449-zj6sx` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:09:22.810Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:09:22.999Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:09:23.001Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:09:23.061Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:09:23.061Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
```
**Последние строки логов:**
```text
.container {
2026-06-09T20:11:27.348Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Using 6 fallback line mirrors
2026-06-09T20:11:31.194Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
```
#### 📥 Loader (`igaming-source-fon-bet-kz-loader-5985bdfff6-kgs2n` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:03:55.430Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:03:55.592Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:03:55.592Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:03:55.629Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:03:55.630Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
```
**Последние строки логов:**
```text
2026-06-09T20:10:28.503Z  INFO 1 --- [igaming-source-fon-bet-kz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-fon-bet-kz, host: fonbet.kz)
2026-06-09T20:11:28.504Z  INFO 1 --- [igaming-source-fon-bet-kz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fonbet.kz' is still reachable...
2026-06-09T20:11:31.365Z  INFO 1 --- [igaming-source-fon-bet-kz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-fon-bet-kz, host: fonbet.kz)
```

---

### 🗂️ Букмекер: `fon-bet-ru`

#### 🕷️ Crawler (`igaming-source-fon-bet-ru-crawler-754d57d4c6-9ftnd` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:11:40.353Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:40.354Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65686215) already exists.
2026-06-09T20:11:40.354Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65686215, ignoring to prevent log noise.
2026-06-09T20:11:40.374Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65686215: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:40.500Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:40.500Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65686216) already exists.
2026-06-09T20:11:40.500Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65686216, ignoring to prevent log noise.
2026-06-09T20:11:40.522Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65686216: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:40.622Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:40.622Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65686279) already exists.
2026-06-09T20:11:40.622Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65686279, ignoring to prevent log noise.
2026-06-09T20:11:40.649Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65686279: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:40.738Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:40.738Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65686283) already exists.
2026-06-09T20:11:40.739Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65686283, ignoring to prevent log noise.
2026-06-09T20:11:40.761Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65686283: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:40.840Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:40.840Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65686291) already exists.
2026-06-09T20:11:40.840Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65686291, ignoring to prevent log noise.
2026-06-09T20:11:40.862Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65686291: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:40.966Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:40.966Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65686264) already exists.
2026-06-09T20:11:40.966Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65686264, ignoring to prevent log noise.
2026-06-09T20:11:40.995Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65686264: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:41.261Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:41.262Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65686292) already exists.
2026-06-09T20:11:41.262Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65686292, ignoring to prevent log noise.
2026-06-09T20:11:41.303Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65686292: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:41.465Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:41.465Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65703408) already exists.
2026-06-09T20:11:41.466Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65703408, ignoring to prevent log noise.
2026-06-09T20:11:41.486Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65703408: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:41.588Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:41.588Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65686284) already exists.
2026-06-09T20:11:41.588Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65686284, ignoring to prevent log noise.
2026-06-09T20:11:41.609Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65686284: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:41.687Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:41.687Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65706732) already exists.
2026-06-09T20:11:41.688Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65706732, ignoring to prevent log noise.
2026-06-09T20:11:41.708Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65706732: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:41.786Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:41.786Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65707438) already exists.
2026-06-09T20:11:41.786Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65707438, ignoring to prevent log noise.
2026-06-09T20:11:41.807Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65707438: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:41.891Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:41.892Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65703655) already exists.
2026-06-09T20:11:41.893Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65703655, ignoring to prevent log noise.
2026-06-09T20:11:41.919Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65703655: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:41.990Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:41.991Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65703706) already exists.
2026-06-09T20:11:41.995Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65703706, ignoring to prevent log noise.
2026-06-09T20:11:42.019Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65703706: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:42.093Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:42.093Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65681844) already exists.
2026-06-09T20:11:42.093Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65681844, ignoring to prevent log noise.
2026-06-09T20:11:42.120Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65681844: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:42.190Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:42.192Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65682065) already exists.
2026-06-09T20:11:42.192Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65682065, ignoring to prevent log noise.
2026-06-09T20:11:42.218Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65682065: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:42.298Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:42.298Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65703712) already exists.
2026-06-09T20:11:42.299Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65703712, ignoring to prevent log noise.
2026-06-09T20:11:42.319Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65703712: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:42.382Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:42.382Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65703717) already exists.
2026-06-09T20:11:42.385Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65703717, ignoring to prevent log noise.
2026-06-09T20:11:42.405Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65703717: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:42.481Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:42.482Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65681845) already exists.
2026-06-09T20:11:42.483Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65681845, ignoring to prevent log noise.
2026-06-09T20:11:42.510Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65681845: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:42.584Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:42.584Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65681846) already exists.
2026-06-09T20:11:42.585Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65681846, ignoring to prevent log noise.
2026-06-09T20:11:42.606Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65681846: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T20:11:42.682Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:42.682Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
  Detail: Key (external_id)=(65703713) already exists.
2026-06-09T20:11:42.683Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.MatchPersistenceService      : Duplicate key while saving match metadata for 65703713, ignoring to prevent log noise.
2026-06-09T20:11:42.705Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65703713: Transaction silently rolled back because it has been marked as rollback-only

```
#### 📥 Loader (`igaming-source-fon-bet-ru-loader-549d458d45-dmw6p` - Running)
**Анализ ошибок:**
```text
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
```
**Последние строки логов:**
```text
2026-06-09T20:11:36.830Z  INFO 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 16 fon-bet-ru matches
2026-06-09T20:11:40.322Z  INFO 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 13 fon-bet-ru matches
2026-06-09T20:11:43.692Z  INFO 1 --- [igaming-source-fon-bet-ru] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 26 fon-bet-ru matches
```

---

### 🗂️ Букмекер: `leon`

#### 🕷️ Crawler (`igaming-source-leon-crawler-99b7d5d89-jznld` - Running)
**Анализ ошибок:**
```text
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-06-09T20:02:40.796Z  INFO 1 --- [igaming-source-leon] [           main] p.d.igaming.source.leon.LeonApplication  : Starting LeonApplication v0.0.1-SNAPSHOT using Java 21.0.10 with PID 1 (/app/app.jar started by root in /app)
2026-06-09T20:02:41.022Z  INFO 1 --- [igaming-source-leon] [           main] p.d.igaming.source.leon.LeonApplication  : The following 2 profiles are active: "dev", "league-crawler"
2026-06-09T20:03:09.094Z  INFO 1 --- [igaming-source-leon] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:03:09.094Z  INFO 1 --- [igaming-source-leon] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-06-09T20:03:13.596Z  INFO 1 --- [igaming-source-leon] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 4225 ms. Found 7 JPA repository interfaces.
2026-06-09T20:03:25.128Z  INFO 1 --- [igaming-source-leon] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:03:25.167Z  INFO 1 --- [igaming-source-leon] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T20:03:25.940Z  INFO 1 --- [igaming-source-leon] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 268 ms. Found 0 Redis repository interfaces.
2026-06-09T20:03:34.982Z  INFO 1 --- [igaming-source-leon] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=90888f1a-ae56-3d12-8be6-f6e25f70227e
2026-06-09T20:03:56.113Z  INFO 1 --- [igaming-source-leon] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3037 (http)
2026-06-09T20:03:56.442Z  INFO 1 --- [igaming-source-leon] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-06-09T20:03:56.480Z  INFO 1 --- [igaming-source-leon] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-06-09T20:03:57.098Z  INFO 1 --- [igaming-source-leon] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-06-09T20:03:57.110Z  INFO 1 --- [igaming-source-leon] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 74103 ms
2026-06-09T20:04:05.517Z  INFO 1 --- [igaming-source-leon] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:04:06.730Z  INFO 1 --- [igaming-source-leon] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:04:07.154Z  INFO 1 --- [igaming-source-leon] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:04:15.206Z  INFO 1 --- [igaming-source-leon] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:04:16.019Z  INFO 1 --- [igaming-source-leon] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:04:18.836Z  INFO 1 --- [igaming-source-leon] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@35cf91df
2026-06-09T20:04:18.842Z  INFO 1 --- [igaming-source-leon] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:04:19.233Z  WARN 1 --- [igaming-source-leon] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T20:04:20.253Z  INFO 1 --- [igaming-source-leon] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:04:46.120Z  INFO 1 --- [igaming-source-leon] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:04:47.502Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:47.502Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:47.682Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:47.682Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:47.737Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:47.738Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:04:47.811Z  INFO 1 --- [igaming-source-leon] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:04:54.982Z  INFO 1 --- [igaming-source-leon] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:05:37.035Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:05:49.256Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.48' (config: vpngate-public-vpn-197, ID: 854, healthy proxies: 12)
2026-06-09T20:05:49.270Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.48:3128
2026-06-09T20:05:49.274Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.48:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:05:49.305Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'leon.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:05:49.310Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'leon.ru'...
2026-06-09T20:05:49.499Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: leon.ru -> 186.2.163.26
2026-06-09T20:05:49.499Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://leon.ru'...
2026-06-09T20:05:49.780Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:05:53.621Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:05:53.681Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:05:53.684Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:05:53.845Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:05:53.927Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:05:53.992Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:05:55.796Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'leon.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:05:56.283Z  WARN 1 --- [igaming-source-leon] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:05:59.234Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-leon, host: leon.ru)
2026-06-09T20:06:17.140Z  INFO 1 --- [igaming-source-leon] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:06:19.267Z  INFO 1 --- [igaming-source-leon] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3037 (http) with context path '/'
2026-06-09T20:06:19.753Z  INFO 1 --- [igaming-source-leon] [           main] p.d.igaming.source.leon.LeonApplication  : Started LeonApplication in 232.131 seconds (process running for 245.294)
2026-06-09T20:06:19.976Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='leon' name='Leon'
2026-06-09T20:06:22.515Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'leon' (display: 'Leon')
2026-06-09T20:06:22.770Z  INFO 1 --- [igaming-source-leon] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='leon' name='Leon'
2026-06-09T20:06:49.728Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T20:06:55.788Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-leon, host: leon.ru)
2026-06-09T20:07:20.237Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=360, NEW=349, PENDING=7, PROCESSED=4, FAILED=0
2026-06-09T20:07:55.791Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T20:08:05.820Z ERROR 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://leon.ru: Read timed out
2026-06-09T20:08:05.822Z ERROR 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'leon.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:08:05.822Z  WARN 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.48 (config: vpngate-public-vpn-197). Requesting rotation...
2026-06-09T20:08:13.784Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported block for config 854 (service: igaming-source-leon, host: leon.ru)
2026-06-09T20:08:13.786Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:18.109Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.49' (config: vpngate-vpn560167748, ID: 1083, healthy proxies: 9)
2026-06-09T20:08:18.109Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.49:3128
2026-06-09T20:08:18.109Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.49:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:18.110Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'leon.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:18.110Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'leon.ru'...
2026-06-09T20:08:18.178Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: leon.ru -> 186.2.163.26
2026-06-09T20:08:18.178Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://leon.ru'...
2026-06-09T20:08:19.837Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=652, NEW=569, PENDING=1, PROCESSED=82, FAILED=0
2026-06-09T20:08:20.988Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'leon.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:08:21.225Z  INFO 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-leon, host: leon.ru)
2026-06-09T20:09:18.114Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T20:09:19.810Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=867, NEW=510, PENDING=10, PROCESSED=347, FAILED=0
2026-06-09T20:09:20.126Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='leon' name='Leon'
2026-06-09T20:09:20.135Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-leon, host: leon.ru)
2026-06-09T20:10:19.823Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=1094, NEW=361, PENDING=10, PROCESSED=723, FAILED=0
2026-06-09T20:10:20.147Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T20:10:23.179Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-leon, host: leon.ru)
2026-06-09T20:11:19.785Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=1425, NEW=572, PENDING=2, PROCESSED=851, FAILED=0
2026-06-09T20:11:23.184Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T20:11:26.089Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-leon, host: leon.ru)

```
#### 📥 Loader (`igaming-source-leon-loader-fdb8d7b49-snlq7` - Running)
**Анализ ошибок:**
```text
	max.request.size = 1048576
	metadata.max.age.ms = 300000
	metadata.max.idle.ms = 300000
	metadata.recovery.strategy = none
	metric.reporters = []
	metrics.num.samples = 2
	metrics.recording.level = INFO
	metrics.sample.window.ms = 30000
	partitioner.adaptive.partitioning.enable = true
	partitioner.availability.timeout.ms = 0
	partitioner.class = null
	partitioner.ignore.keys = false
	receive.buffer.bytes = 32768
	reconnect.backoff.max.ms = 1000
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 2147483647
	retry.backoff.max.ms = 1000
	retry.backoff.ms = 100
	sasl.client.callback.handler.class = null
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.login.callback.handler.class = null
	sasl.login.class = null
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	sasl.login.refresh.buffer.seconds = 300
	sasl.login.refresh.min.period.seconds = 60
	sasl.login.refresh.window.factor = 0.8
	sasl.login.refresh.window.jitter = 0.05
	sasl.login.retry.backoff.max.ms = 10000
	sasl.login.retry.backoff.ms = 100
	sasl.mechanism = GSSAPI
	sasl.oauthbearer.clock.skew.seconds = 30
	sasl.oauthbearer.expected.audience = null
	sasl.oauthbearer.expected.issuer = null
	sasl.oauthbearer.jwks.endpoint.refresh.ms = 3600000
	sasl.oauthbearer.jwks.endpoint.retry.backoff.max.ms = 10000
	sasl.oauthbearer.jwks.endpoint.retry.backoff.ms = 100
	sasl.oauthbearer.jwks.endpoint.url = null
	sasl.oauthbearer.scope.claim.name = scope
	sasl.oauthbearer.sub.claim.name = sub
	sasl.oauthbearer.token.endpoint.url = null
	security.protocol = PLAINTEXT
	security.providers = null
	send.buffer.bytes = 131072
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.3]
	ssl.endpoint.identification.algorithm = https
	ssl.engine.factory.class = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.certificate.chain = null
	ssl.keystore.key = null
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLSv1.3
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.certificates = null
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	transaction.timeout.ms = 60000
	transactional.id = null
	value.serializer = class org.springframework.kafka.support.serializer.JsonSerializer

2026-06-09T20:06:56.852Z  INFO 1 --- [igaming-source-leon] [     virtual-54] o.a.k.c.t.i.KafkaMetricsCollector        : initializing Kafka metrics collector
2026-06-09T20:06:56.947Z  INFO 1 --- [igaming-source-leon] [     virtual-54] o.a.k.clients.producer.KafkaProducer     : [Producer clientId=igaming-source-leon-producer-1] Instantiated an idempotent producer.
2026-06-09T20:06:57.194Z  INFO 1 --- [igaming-source-leon] [     virtual-54] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 3.8.1
2026-06-09T20:06:57.195Z  INFO 1 --- [igaming-source-leon] [     virtual-54] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: 70d6ff42debf7e17
2026-06-09T20:06:57.195Z  INFO 1 --- [igaming-source-leon] [     virtual-54] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1781035617185
2026-06-09T20:06:58.323Z  INFO 1 --- [igaming-source-leon] [leon-producer-1] org.apache.kafka.clients.Metadata        : [Producer clientId=igaming-source-leon-producer-1] Cluster ID: MkU3OEVBNTcwNTJENDM2Qg
2026-06-09T20:06:58.325Z  INFO 1 --- [igaming-source-leon] [leon-producer-1] o.a.k.c.p.internals.TransactionManager   : [Producer clientId=igaming-source-leon-producer-1] ProducerId set to 3234 with epoch 0
2026-06-09T20:07:31.804Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T20:07:39.142Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-leon, host: leon.ru)
2026-06-09T20:07:47.959Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=564, NEW=513, PENDING=1, PROCESSED=51, FAILED=0
2026-06-09T20:07:56.038Z  INFO 1 --- [igaming-source-leon] [nio-3037-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:07:56.038Z  INFO 1 --- [igaming-source-leon] [nio-3037-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:07:56.056Z  INFO 1 --- [igaming-source-leon] [nio-3037-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 17 ms
2026-06-09T20:08:39.146Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T20:08:41.724Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-leon, host: leon.ru)
2026-06-09T20:08:47.958Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=767, NEW=661, PENDING=10, PROCESSED=97, FAILED=0
2026-06-09T20:08:48.410Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='leon' name='Leon'
2026-06-09T20:09:41.725Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T20:09:44.301Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-leon, host: leon.ru)
2026-06-09T20:09:47.952Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=966, NEW=387, PENDING=10, PROCESSED=569, FAILED=0
2026-06-09T20:10:02.524Z ERROR 1 --- [igaming-source-leon] [    virtual-720] p.d.i.s.c.s.MappingConflictService       : MAPPING CONFLICT: leon:FOOTBALL market='ИТОГОВЫЙ ПРОХОД' outcome='1' mappers=[FootballResultMapper->WIN1, FootballSpecialMapper->TEAM1_QUALIFY_YES] event=null
2026-06-09T20:10:02.610Z ERROR 1 --- [igaming-source-leon] [    virtual-720] p.d.i.s.c.s.MappingConflictService       : MAPPING CONFLICT: leon:FOOTBALL market='ИТОГОВЫЙ ПРОХОД' outcome='2' mappers=[FootballResultMapper->WIN2, FootballSpecialMapper->TEAM2_QUALIFY_YES] event=null
2026-06-09T20:10:44.303Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T20:10:46.815Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-leon, host: leon.ru)
2026-06-09T20:10:47.966Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=1238, NEW=435, PENDING=6, PROCESSED=797, FAILED=0

```

---

### 🗂️ Букмекер: `ligastavok`

#### 🕷️ Crawler (`igaming-source-ligastavok-crawler-df7487976-6pkr9` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:07:58.858Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__metaTags"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, __metaTags, IMask
2026-06-09T20:10:08.769Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
2026-06-09T20:10:35.658Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__metaTags"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, __metaTags, IMask
```
**Последние строки логов:**
```text
2026-06-09T20:10:56.019Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ligastavok.ru' is still reachable...
2026-06-09T20:11:03.625Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check: target host 'ligastavok.ru' is reachable (HTTP 200).
2026-06-09T20:11:03.883Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 600 (service: igaming-source-ligastavok, host: ligastavok.ru)
```
#### 📥 Loader (`igaming-source-ligastavok-loader-85d6c74db-8l687` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:05:30.853Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://10.42.14.66:3128
2026-06-09T20:05:31.306Z  INFO 1 --- [igaming-source-ligastavok] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'ligastavok.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:05:40.784Z  INFO 1 --- [igaming-source-ligastavok] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-ligastavok, host: ligastavok.ru)
2026-06-09T20:05:46.719Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 1/3 to: https://www.ligastavok.ru/bets/live
2026-06-09T20:05:54.040Z DEBUG 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
2026-06-09T20:06:02.537Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokPageNavigator      : Performing human-like interactions...
2026-06-09T20:06:11.049Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 2/3 to: https://www.ligastavok.ru/bets/live
2026-06-09T20:06:36.922Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokPageNavigator      : Performing human-like interactions...
2026-06-09T20:06:39.987Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokPageNavigator      : Page loaded successfully! Title: Ставки на спорт - Букмекерская компания «Лига Ставок»
2026-06-09T20:06:39.992Z DEBUG 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokStateExtractor     : Initial state extraction: 2 chars
2026-06-09T20:06:39.995Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokDiagnosticService  : Current page title: Ставки на спорт - Букмекерская компания «Лига Ставок»
2026-06-09T20:06:39.999Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokDiagnosticService  : Page HTML preview: <html lang="ru" data-ls-theme="light" data-ls-layout="wideDesktop" style="--desktop-header_height: 72px;"><head><meta content="width=device-width, minimum-scale=1.0, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" data-rh="true" name="viewport"><link href="https://static-cdn.sportsapi.ru/sites-static/assets/main-ZETUmMIC.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/digital-numbers/default.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/inter/eef448f8701b018041909cc5a65813a9fbfe3e67.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/roboto/fe1c74cff1ecc70aa56210c77f4ba18dbd554ad8.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/tt-travels/8162ec097b0b8281629b34902ebd1d9f44aef69b.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/tt-travels-italic/1998219a39b6760f62e96a255a833366ce36e234.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/squad/29b9bc4c8fdc75484c6e6d6f263302895d7b9d61.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/toxigenesis/0de360f2b1a7dcb801f786ddc3ee5f0ec93f338e.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/tt-firs-neue/7e2bae64562cfb11c60f954f29f7e00e2b838a27.css" rel="stylesheet" data-precedence="default"><meta content="#d9dde5" name="theme-color"><meta content="23c0f7606a900d40" name="yandex-verification"><link href="/manifest.webapp" rel="manifest"><link href="https://static-cdn.sportsapi.ru" rel="preconnect"><link href="https://api.ligastavok.ru" rel="preconnect"><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/stable-vendors-JMdergPs.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/preload-helper-C04rNUBL.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/stable-state-BbzIiuV0.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/stable-ui-sQfMFC69.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/dist-iFZTiX7H.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/stable-core-BntMVExV.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/transport-UuxM7RgX.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/logger-BGrIgmAO.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/read-BQXOUvhq.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/main-Ulkp8hhs.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/is-defined-CcaKlZVJ.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/rolldown-runtime-vlOxniiV.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/main-CbQ8Mg0v.js" crossorigin="anonymous"></script><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/main-Z4vfOus4.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/stable-vendors-JMdergPs.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/stable-core-BntMVExV.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/rolldown-runtime-vlOxniiV.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/stable-ui-sQfMFC69.js"><link crossorigin="" rel="stylesheet" href="https://static-cdn.sportsapi.ru/sites-static/assets/stable-ui-DvSXOqRk.css"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/dist-iFZTiX7H.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/web-vitals-Cmp376N8.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/smoothscroll-rFKoMZ0w.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/not-found-Dz8vd4bg.js"><link crossor
2026-06-09T20:06:40.002Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__TSR_ROUTER__","__LS_CONTAINER_ONLY_HYDRATION__","fhConfig","__metaTags"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, IMask, enableReduxLogs, disableReduxLogs, __TSR_ROUTER__, __LS_CONTAINER_ONLY_HYDRATION__, changeTheme, fhConfig, fhContext, __metaTags
2026-06-09T20:06:58.316Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokDiagnosticService  : Browser is using IP: 80.44.223.44
2026-06-09T20:06:58.912Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:06:58.914Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:06:58.917Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:06:59.241Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:06:59.248Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:06:59.251Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:06:59.892Z  WARN 1 --- [igaming-source-ligastavok] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:07:04.089Z  INFO 1 --- [igaming-source-ligastavok] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:07:04.622Z  INFO 1 --- [igaming-source-ligastavok] [           main] io.undertow                              : starting server: Undertow - 2.3.18.Final
2026-06-09T20:07:04.668Z  INFO 1 --- [igaming-source-ligastavok] [           main] org.xnio                                 : XNIO version 3.8.16.Final
2026-06-09T20:07:04.741Z  INFO 1 --- [igaming-source-ligastavok] [           main] org.xnio.nio                             : XNIO NIO Implementation Version 3.8.16.Final
2026-06-09T20:07:05.049Z  INFO 1 --- [igaming-source-ligastavok] [           main] org.jboss.threads                        : JBoss Threads version 3.5.0.Final
2026-06-09T20:07:05.179Z  INFO 1 --- [igaming-source-ligastavok] [           main] o.s.b.w.e.undertow.UndertowWebServer     : Undertow started on port 8086 (http) with context path '/'
2026-06-09T20:07:05.285Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.LigastavokApplication          : Started LigastavokApplication in 146.479 seconds (process running for 148.799)
2026-06-09T20:07:05.296Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending periodic heartbeat for bookmaker 'LIGASTAVOK'
2026-06-09T20:07:05.303Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'LIGASTAVOK' (display: 'Ligastavok')
2026-06-09T20:07:05.306Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] .s.l.s.LigastavokBrowserLifecycleManager : Rotating Ligastavok page view to: https://www.ligastavok.ru/bets/football (cycle #1)
2026-06-09T20:07:05.757Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='LIGASTAVOK' name='Ligastavok'
2026-06-09T20:07:05.994Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='LIGASTAVOK' name='Ligastavok'
2026-06-09T20:07:21.120Z  WARN 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokStateExtractor     : Failed to refresh serviceId map: Error {
  message='Execution context was destroyed, most likely because of a navigation
  name='Error
  stack='Error:Execution context was destroyed, most likely because of a navigation
Error
    at captureRawStack (/tmp/playwright-java-10725584507292437949/package/lib/utils/stackTrace.js:50:17)
    at LongStandingScope._race (/tmp/playwright-java-10725584507292437949/package/lib/utils/manualPromise.js:91:52)
    at LongStandingScope.race (/tmp/playwright-java-10725584507292437949/package/lib/utils/manualPromise.js:84:17)
    at FrameExecutionContext._raceAgainstContextDestroyed (/tmp/playwright-java-10725584507292437949/package/lib/server/javascript.js:49:40)
    at FrameExecutionContext.evaluateWithArguments (/tmp/playwright-java-10725584507292437949/package/lib/server/javascript.js:61:17)
    at Object.evaluateExpression (/tmp/playwright-java-10725584507292437949/package/lib/server/javascript.js:241:26)
    at async Frame.evaluateExpression (/tmp/playwright-java-10725584507292437949/package/lib/server/frames.js:598:19)
    at async FrameDispatcher.evaluateExpression (/tmp/playwright-java-10725584507292437949/package/lib/server/dispatchers/frameDispatcher.js:91:55)
    at async LongStandingScope._race (/tmp/playwright-java-10725584507292437949/package/lib/utils/manualPromise.js:96:14)
    at async FrameDispatcher._handleCommand (/tmp/playwright-java-10725584507292437949/package/lib/server/dispatchers/dispatcher.js:96:14)
    at async DispatcherConnection.dispatch (/tmp/playwright-java-10725584507292437949/package/lib/server/dispatchers/dispatcher.js:365:22)
}
2026-06-09T20:07:35.228Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ligastavok.ru' is still reachable...
2026-06-09T20:07:36.027Z  INFO 1 --- [igaming-source-ligastavok] [  XNIO-1 task-2] io.undertow.servlet                      : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:07:36.031Z  INFO 1 --- [igaming-source-ligastavok] [  XNIO-1 task-2] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:07:36.043Z  INFO 1 --- [igaming-source-ligastavok] [  XNIO-1 task-2] o.s.web.servlet.DispatcherServlet        : Completed initialization in 7 ms
2026-06-09T20:08:02.583Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://ligastavok.ru: Read timed out
2026-06-09T20:08:02.583Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'ligastavok.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:08:02.583Z  WARN 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.66 (config: vpngate-patito). Requesting rotation...
2026-06-09T20:08:04.001Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: net::ERR_EMPTY_RESPONSE
2026-06-09T20:08:13.719Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 513 (service: igaming-source-ligastavok, host: ligastavok.ru)
2026-06-09T20:08:13.720Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:15.832Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.49' (config: vpngate-vpn560167748, ID: 1083, healthy proxies: 8)
2026-06-09T20:08:15.832Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.49:3128
2026-06-09T20:08:15.833Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.49:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:15.833Z  INFO 1 --- [igaming-source-ligastavok] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'ligastavok.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:15.833Z  INFO 1 --- [igaming-source-ligastavok] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'ligastavok.ru'...
2026-06-09T20:08:15.900Z  INFO 1 --- [igaming-source-ligastavok] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: ligastavok.ru -> 178.248.235.134
2026-06-09T20:08:15.900Z  INFO 1 --- [igaming-source-ligastavok] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://ligastavok.ru'...
2026-06-09T20:08:22.784Z  INFO 1 --- [igaming-source-ligastavok] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'ligastavok.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:08:23.216Z  INFO 1 --- [igaming-source-ligastavok] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-ligastavok, host: ligastavok.ru)
2026-06-09T20:08:45.307Z  WARN 1 --- [igaming-source-ligastavok] [ loader-sched-2] .s.l.s.LigastavokBrowserLifecycleManager : WebSocket listener appears unhealthy (no frames for 120000ms). Reconnecting...
2026-06-09T20:08:50.366Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokBrowserClient      : Starting WebSocket listener for Ligastavok...
2026-06-09T20:08:50.367Z  WARN 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.14.66:3128 to http://10.42.15.49:3128. Recreating browser...
2026-06-09T20:08:51.281Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://10.42.15.49:3128
2026-06-09T20:08:52.778Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 1/3 to: https://www.ligastavok.ru/bets/live
2026-06-09T20:08:54.629Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
2026-06-09T20:08:59.769Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Performing human-like interactions...
2026-06-09T20:09:09.111Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 2/3 to: https://www.ligastavok.ru/bets/live
2026-06-09T20:09:15.834Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ligastavok.ru' is still reachable...
2026-06-09T20:09:20.464Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check: target host 'ligastavok.ru' is reachable (HTTP 200).
2026-06-09T20:09:20.641Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-ligastavok, host: ligastavok.ru)
2026-06-09T20:09:25.854Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Performing human-like interactions...
2026-06-09T20:09:28.998Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Page loaded successfully! Title: Официальный сайт Букмекерской компании Лига Ставок
2026-06-09T20:09:29.004Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokStateExtractor     : Initial state extraction: 2 chars
2026-06-09T20:09:29.017Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Current page title: Официальный сайт Букмекерской компании Лига Ставок
2026-06-09T20:09:29.033Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Page HTML preview: <html lang="ru" data-ls-theme="light" data-ls-layout="wideDesktop" style="--desktop-header_height: 72px;"><head><meta content="width=device-width, minimum-scale=1.0, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" data-rh="true" name="viewport"><link href="https://static-cdn.sportsapi.ru/sites-static/assets/main-ZETUmMIC.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/digital-numbers/b79ee7b5d976a193bb2ad7a3f50bbd6f0670ffaa.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/inter/eef448f8701b018041909cc5a65813a9fbfe3e67.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/roboto/fe1c74cff1ecc70aa56210c77f4ba18dbd554ad8.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/tt-travels/8162ec097b0b8281629b34902ebd1d9f44aef69b.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/tt-travels-italic/1998219a39b6760f62e96a255a833366ce36e234.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/squad/29b9bc4c8fdc75484c6e6d6f263302895d7b9d61.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/toxigenesis/default.css" rel="stylesheet" data-precedence="default"><link crossorigin="anonymous" href="/new-data/tt-firs-neue/7e2bae64562cfb11c60f954f29f7e00e2b838a27.css" rel="stylesheet" data-precedence="default"><meta content="#d9dde5" name="theme-color"><meta content="23c0f7606a900d40" name="yandex-verification"><link href="/manifest.webapp" rel="manifest"><link href="https://static-cdn.sportsapi.ru" rel="preconnect"><link href="https://api.ligastavok.ru" rel="preconnect"><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/stable-vendors-JMdergPs.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/preload-helper-C04rNUBL.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/stable-state-BbzIiuV0.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/stable-ui-sQfMFC69.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/dist-iFZTiX7H.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/stable-core-BntMVExV.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/transport-UuxM7RgX.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/logger-BGrIgmAO.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/read-BQXOUvhq.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/main-Ulkp8hhs.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/is-defined-CcaKlZVJ.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/rolldown-runtime-vlOxniiV.js" crossorigin="anonymous"></script><script defer="" type="module" src="https://static-cdn.sportsapi.ru/sites-static/assets/main-CbQ8Mg0v.js" crossorigin="anonymous"></script><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/main-Z4vfOus4.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/stable-vendors-JMdergPs.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/stable-core-BntMVExV.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/rolldown-runtime-vlOxniiV.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/stable-ui-sQfMFC69.js"><link crossorigin="" rel="stylesheet" href="https://static-cdn.sportsapi.ru/sites-static/assets/stable-ui-DvSXOqRk.css"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/dist-iFZTiX7H.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/web-vitals-Cmp376N8.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/smoothscroll-rFKoMZ0w.js"><link crossorigin="" rel="modulepreload" as="script" href="https://static-cdn.sportsapi.ru/sites-static/assets/not-found-Dz8vd4bg.js"><link crossor
2026-06-09T20:09:29.047Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__metaTags","__TSR_ROUTER__","__LS_CONTAINER_ONLY_HYDRATION__","fhConfig"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, __metaTags, IMask, enableReduxLogs, disableReduxLogs, __TSR_ROUTER__, __LS_CONTAINER_ONLY_HYDRATION__, changeTheme, fhConfig, fhContext
2026-06-09T20:09:30.964Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Browser is using IP: 116.106.207.125
2026-06-09T20:10:05.995Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending periodic heartbeat for bookmaker 'LIGASTAVOK'
2026-06-09T20:10:06.090Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='LIGASTAVOK' name='Ligastavok'
2026-06-09T20:10:20.645Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ligastavok.ru' is still reachable...
2026-06-09T20:10:27.880Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check: target host 'ligastavok.ru' is reachable (HTTP 200).
2026-06-09T20:10:28.094Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-ligastavok, host: ligastavok.ru)
2026-06-09T20:11:28.097Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ligastavok.ru' is still reachable...
2026-06-09T20:11:30.983Z  WARN 1 --- [igaming-source-ligastavok] [ loader-sched-3] .s.l.s.LigastavokBrowserLifecycleManager : WebSocket listener appears unhealthy (no frames for 120000ms). Reconnecting...
2026-06-09T20:11:35.382Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check: target host 'ligastavok.ru' is reachable (HTTP 200).
2026-06-09T20:11:36.015Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokBrowserClient      : Starting WebSocket listener for Ligastavok...
2026-06-09T20:11:36.392Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 1/3 to: https://www.ligastavok.ru/bets/live
2026-06-09T20:11:36.464Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-ligastavok, host: ligastavok.ru)
2026-06-09T20:11:38.052Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
2026-06-09T20:11:40.673Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokPageNavigator      : Performing human-like interactions...

```

---

### 🗂️ Букмекер: `linebet`

#### 🕷️ Crawler (`igaming-source-linebet-crawler-58754d5d7-m5sx5` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:07:06.356Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:07:26.019Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://linebet.com: Read timed out
2026-06-09T20:07:26.029Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'linebet.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:07:26.029Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.66 (config: vpngate-patito). Requesting rotation...
2026-06-09T20:07:26.598Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy failure: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-blocked": Connection reset
2026-06-09T20:07:26.598Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:07:28.615Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://linebet.com/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=189&virtualSports=true, falling back to direct navigation: Error {
  message='Timeout 30000ms exceeded.
  name='TimeoutError
  stack='TimeoutError: Timeout 30000ms exceeded.
    at ProgressController.run (/tmp/playwright-java-1939774627697611353/package/lib/server/progress.js:78:26)
    at Frame.goto (/tmp/playwright-java-1939774627697611353/package/lib/server/frames.js:517:23)
    at FrameDispatcher.goto (/tmp/playwright-java-1939774627697611353/package/lib/server/dispatchers/frameDispatcher.js:81:119)
    at FrameDispatcher._handleCommand (/tmp/playwright-java-1939774627697611353/package/lib/server/dispatchers/dispatcher.js:94:40)
    at DispatcherConnection.dispatch (/tmp/playwright-java-1939774627697611353/package/lib/server/dispatchers/dispatcher.js:365:39)
}
Call log:
- navigating to "https://linebet.com/", waiting until "load"

2026-06-09T20:07:28.759Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://linebet.com/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=189&virtualSports=true
2026-06-09T20:07:42.502Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=189&virtualSports=true (status=200)
2026-06-09T20:07:43.873Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.47' (config: vpngate-2i6, ID: 853, healthy proxies: 8)
2026-06-09T20:07:43.873Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.47:3128
2026-06-09T20:07:43.873Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.47:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:07:43.946Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'linebet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:07:43.965Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'linebet.com'...
2026-06-09T20:07:44.056Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: linebet.com -> 103.151.135.62
2026-06-09T20:07:44.088Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://linebet.com'...
2026-06-09T20:07:44.719Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":3},"PS"
2026-06-09T20:07:44.738Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Successfully fetched data from service-api URL
2026-06-09T20:07:51.673Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'linebet.com' is reachable (HTTP 203). Proxy connectivity verified.
2026-06-09T20:08:01.107Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:08:12.189Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : [linebet/EU] Discovered 28 live events
2026-06-09T20:08:12.190Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Rewrote URL from https://linebet.com/LineFeed/Get1xMatchByLeague?sports=1 to https://linebet.com/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=189 (useServiceApi=true)
2026-06-09T20:08:12.191Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetching PREMATCH from service-api URL: https://linebet.com/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en&partner=189
2026-06-09T20:08:12.191Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.14.66:3128 to http://10.42.15.47:3128. Recreating browser...
2026-06-09T20:08:13.114Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.15.47:3128
2026-06-09T20:08:13.809Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Pre-visiting homepage to establish session: https://linebet.com
2026-06-09T20:08:37.119Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://linebet.com/bff-api/config/group/get?groups=b.core,d.core&lang=en
2026-06-09T20:08:39.312Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/bff-api/config/group/get?groups=b.core,d.core&lang=en (status=200)
2026-06-09T20:08:39.701Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://linebet.com/bff-api/config/group/get?groups=d.customize&lang=en
2026-06-09T20:08:39.702Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://linebet.com/analytics-module-api/v1/analytics?projectId=650&domain=linebet.com
2026-06-09T20:08:40.496Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/bff-api/config/group/get?groups=d.customize&lang=en (status=200)
2026-06-09T20:08:40.545Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://linebet.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/fc
2026-06-09T20:08:40.579Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/analytics-module-api/v1/analytics?projectId=650&domain=linebet.com (status=200)
2026-06-09T20:08:41.148Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/fc (status=204)
2026-06-09T20:08:41.169Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://linebet.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/ab.json
2026-06-09T20:08:41.360Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://linebet.com/bff-api/config/group/get?groups=d.technical,d.global&lang=en
2026-06-09T20:08:42.069Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/ab.json (status=200)
2026-06-09T20:08:42.139Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://linebet.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json
2026-06-09T20:08:42.163Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://linebet.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json
2026-06-09T20:08:42.377Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/bff-api/config/group/get?groups=d.technical,d.global&lang=en (status=200)
2026-06-09T20:08:43.422Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json (status=200)
2026-06-09T20:08:43.422Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json (status=200)
2026-06-09T20:08:43.947Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:08:49.862Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: POST https://linebet.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json
2026-06-09T20:08:50.190Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://linebet.com/bff-api/config/v2/contacts.json?lang=en&country=218&isVipUser=false
2026-06-09T20:08:50.202Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Detected country code from request: 218
2026-06-09T20:08:50.643Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/fatman-api/a6f69e4388362d761ee5bb073edb23ae3d9341fb/event.json (status=200)
2026-06-09T20:08:50.995Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/bff-api/config/v2/contacts.json?lang=en&country=218&isVipUser=false (status=200)
2026-06-09T20:08:51.346Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Homepage redirect status: https://linebet.com -> https://linebet.com/en/block, isCrossDomain=false
2026-06-09T20:08:51.365Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Normalized BetB2B relative fetch URL: /service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=189&virtualSports=true
2026-06-09T20:08:51.365Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Executing API fetch from page context: /service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=189&virtualSports=true
2026-06-09T20:08:51.527Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright request: GET https://linebet.com/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=189&virtualSports=true
2026-06-09T20:08:52.212Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Playwright response: https://linebet.com/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=218&partner=189&virtualSports=true (status=200)
2026-06-09T20:08:53.008Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":10268,"GVE":1,"HL":true,"RLI
2026-06-09T20:08:53.018Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Successfully fetched data from service-api URL
2026-06-09T20:08:58.948Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://linebet.com: Read timed out
2026-06-09T20:08:58.961Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'linebet.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:08:58.965Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.47 (config: vpngate-2i6). Requesting rotation...
2026-06-09T20:08:59.280Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported block for config 853 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:08:59.280Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:59.851Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.45' (config: vpngate-vpn197764580, ID: 1259, healthy proxies: 8)
2026-06-09T20:08:59.851Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.45:3128
2026-06-09T20:08:59.851Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.45:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:59.879Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'linebet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:59.879Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'linebet.com'...
2026-06-09T20:08:59.988Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: linebet.com -> 84.38.4.62
2026-06-09T20:08:59.988Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://linebet.com'...
2026-06-09T20:09:00.143Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : [linebet/EU] Discovered 50 prematch events
2026-06-09T20:09:03.903Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'linebet.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:09:04.100Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1259 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:09:37.144Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T20:09:59.879Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:10:13.502Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://linebet.com: Read timed out
2026-06-09T20:10:13.503Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'linebet.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:10:13.503Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.45 (config: vpngate-vpn197764580). Requesting rotation...
2026-06-09T20:10:13.899Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported block for config 1259 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:10:13.900Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:10:14.324Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.60' (config: vpngate-vpn332519676, ID: 444, healthy proxies: 9)
2026-06-09T20:10:14.326Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.60:3128
2026-06-09T20:10:14.326Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.60:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:10:14.327Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'linebet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:10:14.327Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'linebet.com'...
2026-06-09T20:10:14.406Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: linebet.com -> 84.38.4.62
2026-06-09T20:10:14.410Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://linebet.com'...
2026-06-09T20:10:15.897Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'linebet.com' is reachable (HTTP 203). Proxy connectivity verified.
2026-06-09T20:10:16.018Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:11:14.328Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:11:16.197Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-linebet, host: linebet.com)

```
#### 📥 Loader (`igaming-source-linebet-loader-688bd9699b-5gwp6` - Running)
**Анализ ошибок:**
```text
2026-06-09T19:59:50.590Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.source.betb2b.Betb2bApplication    : Starting Betb2bApplication v0.0.1-SNAPSHOT using Java 21.0.10 with PID 1 (/app/app.jar started by root in /app)
2026-06-09T19:59:50.594Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.source.betb2b.Betb2bApplication    : The following 1 profile is active: "match-loader"
2026-06-09T19:59:53.851Z  INFO 1 --- [igaming-source-linebet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T19:59:53.851Z  INFO 1 --- [igaming-source-linebet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-06-09T19:59:54.299Z  INFO 1 --- [igaming-source-linebet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 429 ms. Found 7 JPA repository interfaces.
2026-06-09T19:59:55.547Z  INFO 1 --- [igaming-source-linebet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T19:59:55.554Z  INFO 1 --- [igaming-source-linebet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T19:59:55.594Z  INFO 1 --- [igaming-source-linebet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 22 ms. Found 0 Redis repository interfaces.
2026-06-09T19:59:56.493Z  INFO 1 --- [igaming-source-linebet] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=80cdccdd-ae3d-377b-a07f-e9be1c096f6b
2026-06-09T19:59:59.184Z  INFO 1 --- [igaming-source-linebet] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3052 (http)
2026-06-09T19:59:59.269Z  INFO 1 --- [igaming-source-linebet] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-06-09T19:59:59.272Z  INFO 1 --- [igaming-source-linebet] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-06-09T19:59:59.648Z  INFO 1 --- [igaming-source-linebet] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-06-09T19:59:59.657Z  INFO 1 --- [igaming-source-linebet] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 8867 ms
2026-06-09T20:00:02.302Z  INFO 1 --- [igaming-source-linebet] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:00:02.679Z  INFO 1 --- [igaming-source-linebet] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:00:02.903Z  INFO 1 --- [igaming-source-linebet] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:00:04.307Z  INFO 1 --- [igaming-source-linebet] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:00:04.473Z  INFO 1 --- [igaming-source-linebet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:00:05.588Z  INFO 1 --- [igaming-source-linebet] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@630febd7
2026-06-09T20:00:05.591Z  INFO 1 --- [igaming-source-linebet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:00:05.726Z  WARN 1 --- [igaming-source-linebet] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T20:00:05.827Z  INFO 1 --- [igaming-source-linebet] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:00:11.003Z  INFO 1 --- [igaming-source-linebet] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:00:11.559Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:11.559Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:11.704Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:11.704Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:11.760Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:11.761Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:00:11.824Z  INFO 1 --- [igaming-source-linebet] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:00:14.279Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:00:20.412Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy 'proxy-vpn-pool.service-proxy.svc.cluster.local' (config: vpngate-vpn537213471, ID: 601, healthy proxies: 15)
2026-06-09T20:00:20.412Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: proxy-vpn-pool.service-proxy.svc.cluster.local:3128
2026-06-09T20:00:20.425Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:00:20.445Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'linebet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:00:20.468Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'linebet.com'...
2026-06-09T20:00:20.636Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: linebet.com -> 103.151.135.62
2026-06-09T20:00:20.636Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://linebet.com'...
2026-06-09T20:00:20.686Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:00:24.465Z  INFO 1 --- [igaming-source-linebet] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:00:30.361Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'linebet.com' is reachable (HTTP 203). Proxy connectivity verified.
2026-06-09T20:00:32.259Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:00:41.861Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:00:41.884Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:00:41.945Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:00:41.971Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:00:41.998Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:00:42.012Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:00:45.665Z  WARN 1 --- [igaming-source-linebet] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:00:58.125Z  INFO 1 --- [igaming-source-linebet] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:00:59.545Z  INFO 1 --- [igaming-source-linebet] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3052 (http) with context path '/'
2026-06-09T20:00:59.849Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.source.betb2b.Betb2bApplication    : Started Betb2bApplication in 71.429 seconds (process running for 73.179)
2026-06-09T20:01:00.154Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T20:01:01.987Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'linebet' (display: 'Xbet-family')
2026-06-09T20:01:02.131Z  INFO 1 --- [igaming-source-linebet] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T20:01:29.738Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:01:31.710Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:02:31.727Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:02:33.463Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:02:49.961Z  INFO 1 --- [igaming-source-linebet] [nio-3052-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:02:49.961Z  INFO 1 --- [igaming-source-linebet] [nio-3052-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:02:49.966Z  INFO 1 --- [igaming-source-linebet] [nio-3052-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 5 ms
2026-06-09T20:03:33.465Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:03:44.213Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:04:00.284Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T20:04:44.216Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:05:01.168Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:06:01.169Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:06:34.916Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:07:00.466Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T20:07:34.918Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:07:45.270Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 601 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:08:45.278Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:08:55.371Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://linebet.com: Read timed out
2026-06-09T20:08:55.373Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'linebet.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:08:55.374Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: proxy-vpn-pool.service-proxy.svc.cluster.local (config: vpngate-vpn537213471). Requesting rotation...
2026-06-09T20:08:56.118Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported block for config 601 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:08:56.119Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:56.628Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.60' (config: vpngate-vpn332519676, ID: 444, healthy proxies: 10)
2026-06-09T20:08:56.629Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.60:3128
2026-06-09T20:08:56.629Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.60:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:56.632Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'linebet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:56.632Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'linebet.com'...
2026-06-09T20:08:56.702Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: linebet.com -> 84.38.4.62
2026-06-09T20:08:56.703Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://linebet.com'...
2026-06-09T20:08:58.304Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'linebet.com' is reachable (HTTP 203). Proxy connectivity verified.
2026-06-09T20:08:58.474Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:09:56.631Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:09:58.261Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T20:10:00.565Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T20:10:58.265Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T20:10:59.648Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-linebet, host: linebet.com)

```

---

### 🗂️ Букмекер: `marathonbet`

#### 🕷️ Crawler (`igaming-source-marathonbet-crawler-76d777577f-rz4lj` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:04:46.621Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:46.806Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:46.807Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:46.858Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:46.859Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
```
**Последние строки логов:**
```text
2026-06-09T20:10:31.272Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T20:11:31.301Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T20:11:40.532Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
```
#### 📥 Loader (`igaming-source-marathonbet-loader-b9f576588-c5csv` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:00:47.784Z  INFO 1 --- [igaming-source-marathonbet] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 28867 ms
2026-06-09T20:00:57.247Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:00:58.087Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:00:58.605Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:01:02.394Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:01:02.765Z  INFO 1 --- [igaming-source-marathonbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:01:03.894Z  INFO 1 --- [igaming-source-marathonbet] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@193d7ac7
2026-06-09T20:01:03.919Z  INFO 1 --- [igaming-source-marathonbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:01:04.435Z  WARN 1 --- [igaming-source-marathonbet] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T20:01:04.821Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:01:20.480Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:01:21.592Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:21.592Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:01:22.174Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:22.174Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:22.321Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:22.321Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:01:22.474Z  INFO 1 --- [igaming-source-marathonbet] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:01:27.732Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:02:00.562Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:02:03.691Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.16.38' (config: vpngate-vpn925380463, ID: 570, healthy proxies: 18)
2026-06-09T20:02:03.692Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.16.38:3128
2026-06-09T20:02:03.692Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.16.38:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:02:03.803Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:02:03.803Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.ru'...
2026-06-09T20:02:03.949Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.ru -> 194.190.0.14
2026-06-09T20:02:03.949Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.ru'...
2026-06-09T20:02:04.004Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:02:07.226Z ERROR 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.ru: No route to host
2026-06-09T20:02:07.243Z  WARN 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.ru': java.net.NoRouteToHostException: No route to host
2026-06-09T20:02:07.261Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:02:09.874Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:02:09.907Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:02:09.989Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:02:10.041Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:02:10.066Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:02:10.088Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:02:12.271Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'www.marathonbet.ru'...
2026-06-09T20:02:12.272Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: www.marathonbet.ru -> 194.190.0.14
2026-06-09T20:02:12.283Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://www.marathonbet.ru'...
2026-06-09T20:02:15.513Z ERROR 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.ru: No route to host
2026-06-09T20:02:15.514Z  WARN 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'www.marathonbet.ru': java.net.NoRouteToHostException: No route to host
2026-06-09T20:02:15.514Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:02:20.515Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Resolving DNS for 'www.marathonbet.ru'...
2026-06-09T20:02:20.519Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] DNS resolved: www.marathonbet.ru -> 194.190.0.14
2026-06-09T20:02:20.519Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Checking HTTP connectivity to 'https://www.marathonbet.ru'...
2026-06-09T20:02:23.610Z ERROR 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.ru: No route to host
2026-06-09T20:02:23.610Z  WARN 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 3/3] Connectivity check failed for 'www.marathonbet.ru': java.net.NoRouteToHostException: No route to host
2026-06-09T20:02:23.611Z ERROR 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.marathonbet.ru' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T20:02:23.611Z  WARN 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity verification failed: 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.marathonbet.ru' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status. Will try to rotate on next failure.
2026-06-09T20:02:28.354Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:02:29.729Z  INFO 1 --- [igaming-source-marathonbet] [           main] io.undertow                              : starting server: Undertow - 2.3.18.Final
2026-06-09T20:02:29.822Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.xnio                                 : XNIO version 3.8.16.Final
2026-06-09T20:02:29.988Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.xnio.nio                             : XNIO NIO Implementation Version 3.8.16.Final
2026-06-09T20:02:31.763Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.jboss.threads                        : JBoss Threads version 3.5.0.Final
2026-06-09T20:02:32.859Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.b.w.e.undertow.UndertowWebServer     : Undertow started on port 3036 (http) with context path '/'
2026-06-09T20:02:33.053Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.m.MarathonbetApplication         : Started MarathonbetApplication in 138.725 seconds (process running for 142.039)
2026-06-09T20:02:33.130Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'marathonbet' (display: 'Marathonbet')
2026-06-09T20:02:33.612Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet' name='Marathonbet'
2026-06-09T20:02:33.635Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet' name='Marathonbet'
2026-06-09T20:03:03.000Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T20:03:06.102Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.ru: No route to host
2026-06-09T20:03:06.102Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:03:06.102Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.16.38 (config: vpngate-vpn925380463). Requesting rotation...
2026-06-09T20:03:06.303Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 570 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T20:03:06.303Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:03:06.687Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.47' (config: vpngate-2i6, ID: 853, healthy proxies: 12)
2026-06-09T20:03:06.687Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.47:3128
2026-06-09T20:03:06.687Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.47:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:03:06.692Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:03:06.693Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.ru'...
2026-06-09T20:03:06.765Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.ru -> 194.190.0.14
2026-06-09T20:03:06.766Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.ru'...
2026-06-09T20:03:13.715Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:03:13.812Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T20:03:17.808Z  INFO 1 --- [igaming-source-marathonbet] [  XNIO-1 task-2] io.undertow.servlet                      : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:04:06.692Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T20:04:14.763Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T20:05:14.764Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T20:05:24.686Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T20:05:33.741Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet' name='Marathonbet'
2026-06-09T20:06:24.688Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T20:06:40.914Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T20:07:40.916Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T20:07:51.387Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T20:08:33.838Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet' name='Marathonbet'
2026-06-09T20:08:51.396Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T20:09:04.905Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T20:10:04.908Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T20:10:10.732Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T20:11:10.733Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T20:11:17.164Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T20:11:33.969Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet' name='Marathonbet'

```

---

### 🗂️ Букмекер: `marathonbet-by`

#### 🕷️ Crawler (`igaming-source-marathonbet-by-crawler-6f696db68d-rjpxs` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:05:14.442Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:14.633Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:14.637Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:14.688Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:14.691Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
```
**Последние строки логов:**
```text
2026-06-09T20:06:55.348Z  INFO 1 --- [igaming-source-marathonbet-by] [ loader-sched-2] p.d.i.s.m.service.MarathonbetApiClient   : Collected 297 league nodes from 25 sports (no limits)
2026-06-09T20:07:52.104Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026+-+15014291
2026-06-09T20:09:31.817Z  INFO 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-by' name='Marathonbet'
```
#### 📥 Loader (`igaming-source-marathonbet-by-loader-6975bc944c-dzdxq` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:01:22.313Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:01:22.947Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:22.947Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:22.970Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:22.970Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
```
**Последние строки логов:**
```text
2026-06-09T20:05:36.469Z  INFO 1 --- [igaming-source-marathonbet-by] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-by' name='Marathonbet'
2026-06-09T20:08:36.583Z  INFO 1 --- [igaming-source-marathonbet-by] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-by' name='Marathonbet'
2026-06-09T20:11:36.720Z  INFO 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-by' name='Marathonbet'
```

---

### 🗂️ Букмекер: `marathonbet-com`

#### 🕷️ Crawler (`igaming-source-marathonbet-com-crawler-548458c88-vqhjv` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:04:42.638Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] io.undertow.servlet                      : Initializing Spring embedded WebApplicationContext
2026-06-09T20:04:42.664Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 32422 ms
2026-06-09T20:04:52.460Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:04:53.373Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:04:53.944Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:04:58.157Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:04:58.719Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:05:01.733Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@193d7ac7
2026-06-09T20:05:01.782Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:05:02.262Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T20:05:02.718Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:05:18.155Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:05:19.107Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:19.107Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:19.344Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:19.344Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:19.425Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:19.426Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:05:19.551Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:05:25.035Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:05:51.015Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:06:04.535Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.66' (config: vpngate-patito, ID: 513, healthy proxies: 11)
2026-06-09T20:06:04.536Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.66:3128
2026-06-09T20:06:04.536Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.66:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:06:04.604Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:06:04.604Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T20:06:04.662Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:06:04.814Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.com -> 104.17.119.85
2026-06-09T20:06:04.814Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T20:06:11.376Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:06:11.386Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:06:11.405Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:06:11.455Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:06:11.481Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:06:11.493Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:06:11.940Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.com' is reachable (HTTP 403). Proxy connectivity verified.
2026-06-09T20:06:14.596Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:06:31.383Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:06:32.628Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] io.undertow                              : starting server: Undertow - 2.3.18.Final
2026-06-09T20:06:32.790Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.xnio                                 : XNIO version 3.8.16.Final
2026-06-09T20:06:33.048Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.xnio.nio                             : XNIO NIO Implementation Version 3.8.16.Final
2026-06-09T20:06:35.254Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.jboss.threads                        : JBoss Threads version 3.5.0.Final
2026-06-09T20:06:36.103Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.b.w.e.undertow.UndertowWebServer     : Undertow started on port 3037 (http) with context path '/'
2026-06-09T20:06:36.528Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.m.MarathonbetApplication         : Started MarathonbetApplication in 156.011 seconds (process running for 162.72)
2026-06-09T20:06:36.607Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'marathonbet-com' (display: 'Marathonbet')
2026-06-09T20:06:36.868Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.m.service.MarathonbetApiClient   : Fetching prematch menu from: https://www.marathonbet.com/su/react/event/menu/prematch
2026-06-09T20:06:36.890Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-06-09T20:06:37.025Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T20:06:37.153Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-06-09T20:06:52.877Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.14.66:3128
2026-06-09T20:07:06.440Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:07:15.102Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/react/event/menu/prematch: Error {
  message='Client network socket disconnected before secure TLS connection was established
  name='Error
  stack='Error: Client network socket disconnected before secure TLS connection was established
    at connResetException (node:internal/errors:787:14)
    at TLSSocket.onConnectEnd (node:_tls_wrap:1727:19)
    at TLSSocket.emit (node:events:530:35)
    at endReadableNT (node:internal/streams/readable:1696:12)
    at process.processTicksAndRejections (node:internal/process/task_queues:82:21)
}
Call log:
- → GET https://www.marathonbet.com/su/react/event/menu/prematch
-   user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36
-   accept: */*
-   accept-encoding: gzip,deflate,br
-   Accept-Language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7

2026-06-09T20:07:15.130Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.m.s.MarathonDiscoveryService     : Cannot fetch prematch menu, aborting league queueing
2026-06-09T20:07:15.936Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: Remote host terminated the handshake
2026-06-09T20:07:15.937Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:07:15.937Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.66 (config: vpngate-patito). Requesting rotation...
2026-06-09T20:07:19.527Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 513 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:07:19.538Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:07:26.512Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.47' (config: vpngate-2i6, ID: 853, healthy proxies: 8)
2026-06-09T20:07:26.512Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.47:3128
2026-06-09T20:07:26.512Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.47:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:07:26.514Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:07:26.514Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T20:07:26.705Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.com -> 104.17.119.85
2026-06-09T20:07:26.708Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T20:07:31.569Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:07:35.657Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:08:26.515Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:08:31.620Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:09:31.628Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:09:35.879Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:09:37.317Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T20:10:35.882Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:10:40.264Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:11:40.266Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:11:45.676Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)

```
#### 📥 Loader (`igaming-source-marathonbet-com-loader-67cb9ddd88-tgzmf` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:01:02.815Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] io.undertow.servlet                      : Initializing Spring embedded WebApplicationContext
2026-06-09T20:01:02.818Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 31016 ms
2026-06-09T20:01:12.602Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:01:13.509Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:01:14.094Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:01:17.376Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:01:17.751Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:01:20.450Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@c8531b9
2026-06-09T20:01:20.486Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:01:20.943Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T20:01:21.360Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:01:39.094Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:01:40.204Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:40.205Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:01:40.349Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:40.349Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:40.457Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:40.459Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:01:40.521Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:01:48.742Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:02:18.692Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:02:21.024Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.64' (config: vpngate-vpn573163202, ID: 1412, healthy proxies: 17)
2026-06-09T20:02:21.031Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.64:3128
2026-06-09T20:02:21.032Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.64:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:02:21.096Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:02:21.096Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T20:02:21.165Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:02:21.282Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.com -> 104.17.119.85
2026-06-09T20:02:21.282Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T20:02:27.469Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:02:28.065Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:02:29.903Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:02:29.938Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:02:30.016Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:02:30.021Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:02:30.024Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:02:30.033Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:02:45.255Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:02:46.075Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] io.undertow                              : starting server: Undertow - 2.3.18.Final
2026-06-09T20:02:46.159Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.xnio                                 : XNIO version 3.8.16.Final
2026-06-09T20:02:46.215Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.xnio.nio                             : XNIO NIO Implementation Version 3.8.16.Final
2026-06-09T20:02:47.235Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.jboss.threads                        : JBoss Threads version 3.5.0.Final
2026-06-09T20:02:47.579Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.b.w.e.undertow.UndertowWebServer     : Undertow started on port 3037 (http) with context path '/'
2026-06-09T20:02:47.779Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.m.MarathonbetApplication         : Started MarathonbetApplication in 144.422 seconds (process running for 149.916)
2026-06-09T20:02:47.790Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'marathonbet-com' (display: 'Marathonbet')
2026-06-09T20:02:47.914Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T20:02:47.943Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T20:03:17.700Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:03:20.274Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:03:24.784Z  INFO 1 --- [igaming-source-marathonbet-com] [  XNIO-1 task-2] io.undertow.servlet                      : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:04:20.276Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:04:23.680Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:05:23.681Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:05:36.644Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:05:53.038Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T20:06:36.648Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:06:41.953Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:07:41.955Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:07:51.390Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:08:51.394Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:08:53.163Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T20:08:56.524Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:09:56.525Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:09:58.025Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:10:58.028Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T20:11:08.043Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: Read timed out
2026-06-09T20:11:08.043Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:11:08.043Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.64 (config: vpngate-vpn573163202). Requesting rotation...
2026-06-09T20:11:08.259Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported block for config 1412 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T20:11:08.260Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:11:08.626Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.65' (config: vpngate-vpn503500791, ID: 950, healthy proxies: 8)
2026-06-09T20:11:08.626Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.65:3128
2026-06-09T20:11:08.626Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.65:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:11:08.627Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:11:08.627Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T20:11:13.712Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.com -> 104.17.119.85
2026-06-09T20:11:13.713Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T20:11:16.710Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: HTTP check returned status 451
2026-06-09T20:11:16.710Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-09T20:11:16.711Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:11:21.714Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T20:11:21.714Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: www.marathonbet.com -> 104.17.119.85
2026-06-09T20:11:21.715Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T20:11:24.035Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: HTTP check returned status 451
2026-06-09T20:11:24.036Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'www.marathonbet.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-09T20:11:24.037Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:11:29.037Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T20:11:29.037Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] DNS resolved: www.marathonbet.com -> 104.17.119.85
2026-06-09T20:11:29.037Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T20:11:31.636Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: HTTP check returned status 451
2026-06-09T20:11:31.636Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 3/3] Connectivity check failed for 'www.marathonbet.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-09T20:11:31.637Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.marathonbet.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T20:11:31.637Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity verification failed: 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.marathonbet.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status. Will try to rotate on next failure.

```

---

### 🗂️ Букмекер: `megapari`

#### 🕷️ Crawler (`igaming-source-megapari-crawler-5bf45b49b8-xmwf7` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:08:07.234Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://megapari.com/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=192&virtualSports=true, falling back to direct navigation: Error {
  message='Timeout 30000ms exceeded.
  name='TimeoutError
  stack='TimeoutError: Timeout 30000ms exceeded.
2026-06-09T20:08:12.887Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":10312,"GVE":1,"HL":true,"RLI
```
**Последние строки логов:**
```text
2026-06-09T20:10:33.987Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T20:10:43.412Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T20:11:43.413Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
```
#### 📥 Loader (`igaming-source-megapari-loader-c46fbffd9-8jkzq` - Running)
**Анализ ошибок:**
```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-06-09T20:02:09.546Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.source.betb2b.Betb2bApplication    : Starting Betb2bApplication v0.0.1-SNAPSHOT using Java 21.0.10 with PID 1 (/app/app.jar started by root in /app)
2026-06-09T20:02:09.584Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.source.betb2b.Betb2bApplication    : The following 1 profile is active: "match-loader"
2026-06-09T20:02:29.123Z  INFO 1 --- [igaming-source-megapari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:02:29.124Z  INFO 1 --- [igaming-source-megapari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-06-09T20:02:32.638Z  INFO 1 --- [igaming-source-megapari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 3448 ms. Found 7 JPA repository interfaces.
2026-06-09T20:02:41.800Z  INFO 1 --- [igaming-source-megapari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:02:41.836Z  INFO 1 --- [igaming-source-megapari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T20:02:42.376Z  INFO 1 --- [igaming-source-megapari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 259 ms. Found 0 Redis repository interfaces.
2026-06-09T20:02:47.952Z  INFO 1 --- [igaming-source-megapari] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=80cdccdd-ae3d-377b-a07f-e9be1c096f6b
2026-06-09T20:03:04.546Z  INFO 1 --- [igaming-source-megapari] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3051 (http)
2026-06-09T20:03:04.711Z  INFO 1 --- [igaming-source-megapari] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-06-09T20:03:04.725Z  INFO 1 --- [igaming-source-megapari] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-06-09T20:03:06.715Z  INFO 1 --- [igaming-source-megapari] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-06-09T20:03:06.761Z  INFO 1 --- [igaming-source-megapari] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 56183 ms
2026-06-09T20:03:24.626Z  INFO 1 --- [igaming-source-megapari] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:03:26.230Z  INFO 1 --- [igaming-source-megapari] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:03:27.738Z  INFO 1 --- [igaming-source-megapari] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:03:38.002Z  INFO 1 --- [igaming-source-megapari] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:03:38.859Z  INFO 1 --- [igaming-source-megapari] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:03:45.418Z  INFO 1 --- [igaming-source-megapari] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@69d61a6f
2026-06-09T20:03:45.519Z  INFO 1 --- [igaming-source-megapari] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:03:47.084Z  WARN 1 --- [igaming-source-megapari] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T20:03:47.930Z  INFO 1 --- [igaming-source-megapari] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:04:17.482Z  INFO 1 --- [igaming-source-megapari] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:04:18.687Z  INFO 1 --- [igaming-source-megapari] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:04:26.981Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:04:34.579Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.48' (config: vpngate-public-vpn-197, ID: 854, healthy proxies: 11)
2026-06-09T20:04:34.579Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.48:3128
2026-06-09T20:04:34.579Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.48:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:04:34.625Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'megapari.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:04:34.625Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'megapari.com'...
2026-06-09T20:04:34.737Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: megapari.com -> 83.147.204.197
2026-06-09T20:04:34.737Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://megapari.com'...
2026-06-09T20:04:35.189Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:04:40.139Z ERROR 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://megapari.com: Remote host terminated the handshake
2026-06-09T20:04:40.140Z  WARN 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'megapari.com': javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
2026-06-09T20:04:40.143Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:04:45.174Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'megapari.com'...
2026-06-09T20:04:45.174Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: megapari.com -> 83.147.204.197
2026-06-09T20:04:45.174Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://megapari.com'...
2026-06-09T20:04:45.668Z  INFO 1 --- [igaming-source-megapari] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:04:56.491Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'megapari.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:05:01.978Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T20:05:17.446Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:05:17.471Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:05:17.504Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:05:17.537Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:05:17.539Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:05:17.569Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:05:23.364Z  WARN 1 --- [igaming-source-megapari] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:05:43.620Z  INFO 1 --- [igaming-source-megapari] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:05:45.376Z  INFO 1 --- [igaming-source-megapari] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3051 (http) with context path '/'
2026-06-09T20:05:45.741Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.source.betb2b.Betb2bApplication    : Started Betb2bApplication in 225.55 seconds (process running for 233.063)
2026-06-09T20:05:45.998Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T20:05:48.635Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'megapari' (display: 'Xbet-family')
2026-06-09T20:05:48.836Z  INFO 1 --- [igaming-source-megapari] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T20:05:50.007Z  INFO 1 --- [igaming-source-megapari] [nio-3051-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:05:50.021Z  INFO 1 --- [igaming-source-megapari] [nio-3051-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:05:50.066Z  INFO 1 --- [igaming-source-megapari] [nio-3051-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 45 ms
2026-06-09T20:06:15.665Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T20:06:37.541Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T20:07:37.549Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T20:07:51.386Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T20:08:46.112Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T20:08:51.394Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T20:09:01.414Z ERROR 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://megapari.com: Read timed out
2026-06-09T20:09:01.415Z ERROR 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'megapari.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:09:01.415Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.48 (config: vpngate-public-vpn-197). Requesting rotation...
2026-06-09T20:09:02.388Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported block for config 854 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T20:09:02.389Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:09:03.369Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.62' (config: vpngate-vpn572583473, ID: 580, healthy proxies: 8)
2026-06-09T20:09:03.379Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.62:3128
2026-06-09T20:09:03.381Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.62:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:09:03.388Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'megapari.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:09:03.400Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'megapari.com'...
2026-06-09T20:09:03.476Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: megapari.com -> 83.147.204.197
2026-06-09T20:09:03.480Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://megapari.com'...
2026-06-09T20:09:05.036Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'megapari.com' is reachable (HTTP 203). Proxy connectivity verified.
2026-06-09T20:09:05.141Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T20:10:03.391Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T20:10:06.255Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T20:11:06.257Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T20:11:08.185Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T20:11:46.246Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'

```

---

### 🗂️ Букмекер: `melbet`

#### 🕷️ Crawler (`igaming-source-melbet-crawler-8547f65849-pgrvw` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:08:18.088Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'melbet.ru'...
2026-06-09T20:08:18.191Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch returned HTML (preview: <!DOCTYPE html><html lang="ru"><head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
  )
2026-06-09T20:08:18.191Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Both rewrites failed. Trying original configured URL: https://melbet.ru/LiveFeed/Get1xMatchByLeague?sports=1
2026-06-09T20:08:18.191Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.15.47:3128 to http://10.42.14.45:3128. Recreating browser...
2026-06-09T20:08:18.337Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: melbet.ru -> 213.165.196.159
2026-06-09T20:08:18.340Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://melbet.ru'...
2026-06-09T20:08:18.594Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:08:19.147Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.14.45:3128
2026-06-09T20:08:20.529Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'melbet.ru' is reachable (HTTP 403). Proxy connectivity verified.
2026-06-09T20:08:21.044Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1259 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:08:28.312Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://melbet.ru/LiveFeed/Get1xMatchByLeague?sports=1. Rotating proxy.
2026-06-09T20:08:28.312Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.45 (config: vpngate-vpn197764580). Requesting rotation...
2026-06-09T20:08:29.523Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 1259 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:08:29.523Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:32.562Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.48' (config: vpngate-public-vpn-197, ID: 854, healthy proxies: 6)
2026-06-09T20:08:32.563Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.48:3128
2026-06-09T20:08:32.579Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.48:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:32.596Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'melbet.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:32.678Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'melbet.ru'...
2026-06-09T20:08:32.678Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: melbet.ru -> 213.165.196.159
2026-06-09T20:08:32.678Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://melbet.ru'...
2026-06-09T20:08:32.670Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch returned HTML (preview: <!DOCTYPE html><html lang="ru"><head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
  )
2026-06-09T20:08:32.687Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : All fetch attempts failed for LIVE
2026-06-09T20:08:32.715Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Rewrote URL from https://melbet.ru/LineFeed/Get1xMatchByLeague?sports=1 to https://melbet.ru/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en (useServiceApi=true)
2026-06-09T20:08:32.734Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetching PREMATCH from service-api URL: https://melbet.ru/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en
2026-06-09T20:08:32.788Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.14.45:3128 to http://10.42.15.48:3128. Recreating browser...
2026-06-09T20:08:33.539Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.15.48:3128
2026-06-09T20:08:36.088Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'melbet.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:08:36.413Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:08:56.315Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://melbet.ru/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&virtualSports=true
2026-06-09T20:08:57.054Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : JSON feed request for https://melbet.ru/service-api/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&virtualSports=true returned HTML instead of JSON. Potential geoblock/mirror redirect. Rotating proxy.
2026-06-09T20:08:57.055Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.48 (config: vpngate-public-vpn-197). Requesting rotation...
2026-06-09T20:08:57.223Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 854 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:08:57.229Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:57.827Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.49' (config: vpngate-vpn560167748, ID: 1083, healthy proxies: 7)
2026-06-09T20:08:57.832Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.49:3128
2026-06-09T20:08:57.838Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.49:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:57.847Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'melbet.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:57.855Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'melbet.ru'...
2026-06-09T20:08:57.910Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch returned HTML (preview: <!DOCTYPE html><html lang="ru"><!--StartManifestLink--><!--EndManifestLink--><head>
<!--StartHeadStandartCodes-->
<meta name="google" content="notranslate">
<meta name="google-site-verification" conte)
2026-06-09T20:08:57.919Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Rewrote URL from https://melbet.ru/LineFeed/Get1xMatchByLeague?sports=1 to https://melbet.ru/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en (useServiceApi=false)
2026-06-09T20:08:57.920Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Service-api URL failed/blocked. Trying direct URL: https://melbet.ru/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en
2026-06-09T20:08:57.922Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.15.48:3128 to http://10.42.15.49:3128. Recreating browser...
2026-06-09T20:08:57.952Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: melbet.ru -> 213.165.196.159
2026-06-09T20:08:57.957Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://melbet.ru'...
2026-06-09T20:08:59.631Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.15.49:3128
2026-06-09T20:09:00.588Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'melbet.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:09:00.779Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:09:15.796Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : JSON feed request for https://melbet.ru/LineFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&virtualSports=true returned HTML instead of JSON. Potential geoblock/mirror redirect. Rotating proxy.
2026-06-09T20:09:15.796Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.49 (config: vpngate-vpn560167748). Requesting rotation...
2026-06-09T20:09:16.732Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 1083 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:09:16.737Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:09:17.221Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.67' (config: vpngate-vpn269589023, ID: 808, healthy proxies: 7)
2026-06-09T20:09:17.237Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.67:3128
2026-06-09T20:09:17.237Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.67:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:09:17.282Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'melbet.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:09:17.282Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'melbet.ru'...
2026-06-09T20:09:17.311Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: melbet.ru -> 213.165.196.159
2026-06-09T20:09:17.314Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://melbet.ru'...
2026-06-09T20:09:17.814Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch returned HTML (preview: <!DOCTYPE html><html lang="ru"><!--StartManifestLink--><!--EndManifestLink--><head><link href="//cdn-ya.melbet.ru/version/0.7.472/static/css/main.css" type="text/css" rel="stylesheet">
<!--StartHeadSt)
2026-06-09T20:09:17.825Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Both rewrites failed. Trying original configured URL: https://melbet.ru/LineFeed/Get1xMatchByLeague?sports=1
2026-06-09T20:09:17.825Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.15.49:3128 to http://10.42.14.67:3128. Recreating browser...
2026-06-09T20:09:18.517Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.14.67:3128
2026-06-09T20:09:18.595Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T20:09:20.616Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'melbet.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:09:20.939Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 808 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:09:22.126Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 808 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:09:32.694Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : JSON feed request for https://melbet.ru/LineFeed/Get1xMatchByLeague?sports=1 returned HTML instead of JSON. Potential geoblock/mirror redirect. Rotating proxy.
2026-06-09T20:09:32.709Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.67 (config: vpngate-vpn269589023). Requesting rotation...
2026-06-09T20:09:33.203Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 808 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:09:33.205Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:09:33.595Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.63' (config: vpngate-vpn444753516, ID: 317, healthy proxies: 5)
2026-06-09T20:09:33.606Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.63:3128
2026-06-09T20:09:33.612Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.63:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:09:33.627Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'melbet.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:09:33.637Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'melbet.ru'...
2026-06-09T20:09:33.727Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: melbet.ru -> 213.165.196.159
2026-06-09T20:09:33.732Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://melbet.ru'...
2026-06-09T20:09:34.201Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch returned HTML (preview: <!DOCTYPE html><html lang="ru"><!--StartManifestLink--><!--EndManifestLink--><head><link href="//cdn-ya.melbet.ru/version/0.7.472/static/css/main.css" type="text/css" rel="stylesheet">
<!--StartHeadSt)
2026-06-09T20:09:34.201Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : All fetch attempts failed for PREMATCH
2026-06-09T20:09:37.132Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'melbet.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:09:37.290Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 317 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:09:37.574Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T20:10:22.147Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T20:10:31.461Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 317 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T20:11:31.462Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T20:11:35.956Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 317 (service: igaming-source-melbet, host: melbet.ru)

```
#### 📥 Loader (`igaming-source-melbet-loader-d89dd59bd-gvmnt` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
2026-06-09T20:10:36.597Z  INFO 1 --- [igaming-source-melbet] [nio-3046-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 17 ms
2026-06-09T20:11:00.923Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T20:11:02.020Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-melbet, host: melbet.ru)
```

---

### 🗂️ Букмекер: `olimpbet`

#### 🕷️ Crawler (`igaming-source-olimpbet-crawler-8944c87-24mzv` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:10:00.535Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:10:00.707Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:10:00.708Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:10:00.773Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:10:00.774Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
```
**Последние строки логов:**
```text
2026-06-09T20:11:39.318Z  WARN 1 --- [igaming-source-olimpbet] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:11:45.201Z  INFO 1 --- [igaming-source-olimpbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.olimp.bet' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:11:46.776Z  INFO 1 --- [igaming-source-olimpbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1372 (service: igaming-source-olimpbet, host: www.olimp.bet)
```

---

### 🗂️ Букмекер: `olimpbet-kz`

#### 🕷️ Crawler (`igaming-source-olimpbet-kz-crawler-55b5bfc69d-2ml9t` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-06-09T20:10:16.256Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.olimpbet.OlimpbetApplication     : Starting OlimpbetApplication v0.0.1-SNAPSHOT using Java 21.0.10 with PID 1 (/app/app.jar started by root in /app)
2026-06-09T20:10:16.260Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.olimpbet.OlimpbetApplication     : The following 1 profile is active: "dev"
2026-06-09T20:10:21.011Z  INFO 1 --- [igaming-source-olimpbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:10:21.012Z  INFO 1 --- [igaming-source-olimpbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-06-09T20:10:21.547Z  INFO 1 --- [igaming-source-olimpbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 502 ms. Found 7 JPA repository interfaces.
2026-06-09T20:10:23.157Z  INFO 1 --- [igaming-source-olimpbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:10:23.160Z  INFO 1 --- [igaming-source-olimpbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T20:10:23.238Z  INFO 1 --- [igaming-source-olimpbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 46 ms. Found 0 Redis repository interfaces.
2026-06-09T20:10:24.318Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=c2198c6d-839c-3b3e-b5d8-e587205fcc7d
2026-06-09T20:10:27.910Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3040 (http)
2026-06-09T20:10:27.987Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-06-09T20:10:27.987Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-06-09T20:10:28.482Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-06-09T20:10:28.490Z  INFO 1 --- [igaming-source-olimpbet] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 11926 ms
2026-06-09T20:10:32.202Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:10:32.774Z  INFO 1 --- [igaming-source-olimpbet] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:10:33.017Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:10:34.461Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:10:34.591Z  INFO 1 --- [igaming-source-olimpbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:10:35.788Z  INFO 1 --- [igaming-source-olimpbet] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@27605b87
2026-06-09T20:10:35.792Z  INFO 1 --- [igaming-source-olimpbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:10:36.289Z  INFO 1 --- [igaming-source-olimpbet] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:10:41.443Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:10:42.025Z  INFO 1 --- [igaming-source-olimpbet] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:10:44.766Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:10:51.498Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:10:51.505Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:10:51.512Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:10:51.517Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:10:51.524Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:10:51.537Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:10:51.923Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.core.service.VpnManagerService   : VPN is disabled via app.vpn.enabled=false. Skipping VPN configuration.
2026-06-09T20:10:52.054Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:10:52.761Z  WARN 1 --- [igaming-source-olimpbet] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:10:57.168Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:10:57.634Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3040 (http) with context path '/'
2026-06-09T20:10:57.676Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.olimpbet.OlimpbetApplication     : Started OlimpbetApplication in 44.612 seconds (process running for 48.816)
2026-06-09T20:10:57.737Z  INFO 1 --- [igaming-source-olimpbet] [ loader-sched-2] p.d.i.s.o.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from Olimpbet...
2026-06-09T20:10:57.738Z  INFO 1 --- [igaming-source-olimpbet] [ loader-sched-2] p.d.i.s.o.service.OlimpbetApiClient      : Fetching live line from: https://api4.olimpbet.kz/v2/events?page-size=500&live=true
2026-06-09T20:10:57.738Z  INFO 1 --- [igaming-source-olimpbet] [ loader-sched-3] p.d.i.s.o.scheduler.MatchFetchScheduler  : Scheduled: fetching prematch data from Olimpbet...
2026-06-09T20:10:57.741Z  INFO 1 --- [igaming-source-olimpbet] [ loader-sched-3] p.d.i.s.o.service.OlimpbetApiClient      : Fetching prematch line from: https://api4.olimpbet.kz/v2/events?page-size=500&live=false
2026-06-09T20:10:58.480Z  INFO 1 --- [igaming-source-olimpbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='olimpbet-kz' name='Olimpbet'
2026-06-09T20:10:58.638Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'olimpbet-kz' (display: 'Olimpbet')
2026-06-09T20:10:58.729Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='olimpbet-kz' name='Olimpbet'
2026-06-09T20:11:00.731Z  WARN 1 --- [igaming-source-olimpbet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-09T20:11:00.789Z ERROR 1 --- [igaming-source-olimpbet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "ukime6r48dbr3o67dwqn897fjqh"
  Detail: Key (external_id)=(100) already exists.
2026-06-09T20:11:21.961Z  INFO 1 --- [igaming-source-olimpbet] [ loader-sched-2] p.d.i.s.c.e.o.s.OlimpbetEventDiscoverer  : [olimpbet-kz] Discovered 157 V2 live events
2026-06-09T20:11:21.962Z  INFO 1 --- [igaming-source-olimpbet] [ loader-sched-2] p.d.i.s.o.scheduler.MatchFetchScheduler  : Scheduled live Olimpbet discovery completed: 157 events discovered

```
#### 📥 Loader (`igaming-source-olimpbet-kz-loader-9dd856fc-4kppm` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:06:03.788Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:06:03.963Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:06:03.963Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:06:04.048Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:06:04.049Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
```
**Последние строки логов:**
```text
2026-06-09T20:11:35.845Z  INFO 1 --- [igaming-source-olimpbet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 olimpbet-kz matches
2026-06-09T20:11:41.433Z  INFO 1 --- [igaming-source-olimpbet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 olimpbet-kz matches
2026-06-09T20:11:44.935Z  INFO 1 --- [igaming-source-olimpbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 olimpbet-kz matches
```

---

### 🗂️ Букмекер: `pari`

#### 🕷️ Crawler (`igaming-source-pari-crawler-c47b6bcbf-z4g2t` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
2026-06-09T20:11:45.335Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.15.47:3128
2026-06-09T20:11:47.316Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'pari.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:11:47.577Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-pari, host: pari.ru)
```
#### 📥 Loader (`igaming-source-pari-loader-6bd54c4c5d-vcc9z` - Running)
**Анализ ошибок:**
```text
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-06-09T20:04:04.262Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.source.fonbet.FonbetApplication    : Starting FonbetApplication v0.0.1-SNAPSHOT using Java 21.0.10 with PID 1 (/app/app.jar started by root in /app)
2026-06-09T20:04:04.276Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.source.fonbet.FonbetApplication    : The following 1 profile is active: "dev"
2026-06-09T20:04:18.464Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:04:18.464Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-06-09T20:04:20.044Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 1532 ms. Found 7 JPA repository interfaces.
2026-06-09T20:04:25.866Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:04:25.912Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T20:04:26.070Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 82 ms. Found 0 Redis repository interfaces.
2026-06-09T20:04:29.382Z  INFO 1 --- [igaming-source-pari] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=1dfb7a37-3018-3172-a323-bb8c4835e8c8
2026-06-09T20:04:38.118Z  WARN 1 --- [igaming-source-pari] [           main] io.undertow.websockets.jsr               : UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used
2026-06-09T20:04:38.466Z  INFO 1 --- [igaming-source-pari] [           main] io.undertow.servlet                      : Initializing Spring embedded WebApplicationContext
2026-06-09T20:04:38.494Z  INFO 1 --- [igaming-source-pari] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 33312 ms
2026-06-09T20:04:47.116Z  INFO 1 --- [igaming-source-pari] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:04:47.985Z  INFO 1 --- [igaming-source-pari] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:04:48.355Z  INFO 1 --- [igaming-source-pari] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:04:52.083Z  INFO 1 --- [igaming-source-pari] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:04:52.416Z  INFO 1 --- [igaming-source-pari] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:04:55.208Z  INFO 1 --- [igaming-source-pari] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@7488c183
2026-06-09T20:04:55.216Z  INFO 1 --- [igaming-source-pari] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:04:56.369Z  INFO 1 --- [igaming-source-pari] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:05:12.746Z  INFO 1 --- [igaming-source-pari] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:05:13.475Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:13.475Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:13.751Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:13.751Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:13.848Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:13.848Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:05:13.942Z  INFO 1 --- [igaming-source-pari] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:05:19.476Z  INFO 1 --- [igaming-source-pari] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:05:34.759Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:05:34.785Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:05:34.897Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:05:34.933Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:05:34.977Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:05:35.008Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:05:48.866Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:06:11.596Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.64' (config: vpngate-vpn573163202, ID: 1412, healthy proxies: 11)
2026-06-09T20:06:11.596Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.64:3128
2026-06-09T20:06:11.596Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.64:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:06:11.635Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'pari.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:06:11.655Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'pari.ru'...
2026-06-09T20:06:11.817Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:06:11.818Z  WARN 1 --- [igaming-source-pari] [           main] p.d.i.s.core.browser.BrowserService      : XVFB_HEADED requested but DISPLAY missing. Falling back to HEADLESS_STEALTH.
2026-06-09T20:06:11.830Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: pari.ru -> 91.221.164.236
2026-06-09T20:06:11.830Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://pari.ru'...
2026-06-09T20:06:11.963Z  INFO 1 --- [igaming-source-pari] [           main] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Initializing FonbetApiClient with Universal Browser Service
2026-06-09T20:06:11.986Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.e.f.s.JsonSchemaValidator      : Loaded Fonbet schema from /fonbet/urls-schema.json
2026-06-09T20:06:16.649Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'pari.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:06:16.693Z  WARN 1 --- [igaming-source-pari] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:06:18.212Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-pari, host: pari.ru)
2026-06-09T20:06:33.341Z  INFO 1 --- [igaming-source-pari] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:06:36.048Z  INFO 1 --- [igaming-source-pari] [           main] io.undertow                              : starting server: Undertow - 2.3.18.Final
2026-06-09T20:06:36.215Z  INFO 1 --- [igaming-source-pari] [           main] org.xnio                                 : XNIO version 3.8.16.Final
2026-06-09T20:06:36.324Z  INFO 1 --- [igaming-source-pari] [           main] org.xnio.nio                             : XNIO NIO Implementation Version 3.8.16.Final
2026-06-09T20:06:37.591Z  INFO 1 --- [igaming-source-pari] [           main] org.jboss.threads                        : JBoss Threads version 3.5.0.Final
2026-06-09T20:06:38.356Z  INFO 1 --- [igaming-source-pari] [           main] o.s.b.w.e.undertow.UndertowWebServer     : Undertow started on port 3038 (http) with context path '/'
2026-06-09T20:06:38.805Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.source.fonbet.FonbetApplication    : Started FonbetApplication in 162.41 seconds (process running for 168.224)
2026-06-09T20:06:38.839Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'pari' (display: 'Fonbet')
2026-06-09T20:06:39.032Z  INFO 1 --- [igaming-source-pari] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='pari' name='Fonbet'
2026-06-09T20:06:39.169Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='pari' name='Fonbet'
2026-06-09T20:06:52.203Z  INFO 1 --- [igaming-source-pari] [  XNIO-1 task-2] io.undertow.servlet                      : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:06:52.207Z  INFO 1 --- [igaming-source-pari] [  XNIO-1 task-2] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:06:52.217Z  INFO 1 --- [igaming-source-pari] [  XNIO-1 task-2] o.s.web.servlet.DispatcherServlet        : Completed initialization in 10 ms
2026-06-09T20:07:08.743Z  INFO 1 --- [igaming-source-pari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pari.ru' is still reachable...
2026-06-09T20:07:15.798Z  INFO 1 --- [igaming-source-pari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-pari, host: pari.ru)
2026-06-09T20:08:15.800Z  INFO 1 --- [igaming-source-pari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pari.ru' is still reachable...
2026-06-09T20:08:18.570Z  INFO 1 --- [igaming-source-pari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-pari, host: pari.ru)
2026-06-09T20:09:18.578Z  INFO 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pari.ru' is still reachable...
2026-06-09T20:09:19.582Z  INFO 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-pari, host: pari.ru)
2026-06-09T20:09:39.152Z  INFO 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='pari' name='Fonbet'
2026-06-09T20:10:19.588Z  INFO 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pari.ru' is still reachable...
2026-06-09T20:10:29.608Z ERROR 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pari.ru: Read timed out
2026-06-09T20:10:29.608Z ERROR 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'pari.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:10:29.608Z  WARN 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.64 (config: vpngate-vpn573163202). Requesting rotation...
2026-06-09T20:10:29.929Z  INFO 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported block for config 1412 (service: igaming-source-pari, host: pari.ru)
2026-06-09T20:10:29.930Z  INFO 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:10:30.553Z  INFO 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.48' (config: vpngate-public-vpn-197, ID: 854, healthy proxies: 8)
2026-06-09T20:10:30.554Z  INFO 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.48:3128
2026-06-09T20:10:30.554Z  INFO 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.48:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:10:30.555Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'pari.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:10:30.555Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'pari.ru'...
2026-06-09T20:10:30.675Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: pari.ru -> 91.221.164.236
2026-06-09T20:10:30.683Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://pari.ru'...
2026-06-09T20:10:36.538Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'pari.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:10:36.689Z  INFO 1 --- [igaming-source-pari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-pari, host: pari.ru)
2026-06-09T20:11:30.558Z  INFO 1 --- [igaming-source-pari] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pari.ru' is still reachable...
2026-06-09T20:11:34.819Z  INFO 1 --- [igaming-source-pari] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 854 (service: igaming-source-pari, host: pari.ru)

```

---

### 🗂️ Букмекер: `pinnacle`

#### 🕷️ Crawler (`igaming-source-pinnacle-crawler-5c4cf4f658-b4vq7` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:11:36.895Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:11:37.176Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:11:37.176Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:11:37.296Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:11:37.297Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
```
**Последние строки логов:**
```text
2026-06-09T20:11:37.297Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:11:37.394Z  INFO 1 --- [igaming-source-pinnacle] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:11:42.117Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
```
#### 📥 Loader (`igaming-source-pinnacle-loader-845fc5557b-pzhtx` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:01:04.996Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:01:07.821Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:01:08.173Z  INFO 1 --- [igaming-source-pinnacle] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:01:10.232Z  INFO 1 --- [igaming-source-pinnacle] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@2696b687
2026-06-09T20:01:10.246Z  INFO 1 --- [igaming-source-pinnacle] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:01:10.675Z  WARN 1 --- [igaming-source-pinnacle] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T20:01:10.944Z  INFO 1 --- [igaming-source-pinnacle] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:01:24.536Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:01:25.460Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:25.460Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:01:26.034Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:26.035Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:26.153Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:26.154Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:01:26.302Z  INFO 1 --- [igaming-source-pinnacle] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:01:34.079Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:02:07.369Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:02:10.527Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy 'proxy-vpn-pool.service-proxy.svc.cluster.local' (config: vpngate-vpn343738742, ID: 600, healthy proxies: 18)
2026-06-09T20:02:10.528Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: proxy-vpn-pool.service-proxy.svc.cluster.local:3128
2026-06-09T20:02:10.535Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:02:10.557Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'pinnacle.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:02:10.598Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'pinnacle.com'...
2026-06-09T20:02:10.706Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: pinnacle.com -> 8.6.112.0
2026-06-09T20:02:10.707Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://pinnacle.com'...
2026-06-09T20:02:10.984Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:02:15.769Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:02:15.823Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:02:15.827Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:02:15.890Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:02:16.041Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:02:16.093Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:02:17.873Z  WARN 1 --- [igaming-source-pinnacle] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:02:19.226Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'pinnacle.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:02:19.990Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 600 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T20:02:36.146Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:02:37.784Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3040 (http) with context path '/'
2026-06-09T20:02:38.126Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.pinnacle.PinnacleApplication     : Started PinnacleApplication in 139.236 seconds (process running for 143.346)
2026-06-09T20:02:38.400Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='pinnacle' name='Pinnacle'
2026-06-09T20:02:39.759Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'pinnacle' (display: 'Pinnacle')
2026-06-09T20:02:39.885Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='pinnacle' name='Pinnacle'
2026-06-09T20:03:08.051Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T20:03:11.630Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 600 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T20:03:17.839Z  INFO 1 --- [igaming-source-pinnacle] [nio-3040-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:03:17.839Z  INFO 1 --- [igaming-source-pinnacle] [nio-3040-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:03:17.844Z  INFO 1 --- [igaming-source-pinnacle] [nio-3040-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 3 ms
2026-06-09T20:04:11.632Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T20:04:21.729Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: Read timed out
2026-06-09T20:04:21.730Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'pinnacle.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:04:21.734Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: proxy-vpn-pool.service-proxy.svc.cluster.local (config: vpngate-vpn343738742). Requesting rotation...
2026-06-09T20:04:30.651Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported block for config 600 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T20:04:30.651Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:04:37.483Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.14.66' (config: vpngate-patito, ID: 513, healthy proxies: 10)
2026-06-09T20:04:37.484Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.14.66:3128
2026-06-09T20:04:37.485Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.14.66:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:04:37.486Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'pinnacle.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:04:37.487Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'pinnacle.com'...
2026-06-09T20:04:37.564Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: pinnacle.com -> 8.6.112.0
2026-06-09T20:04:37.565Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://pinnacle.com'...
2026-06-09T20:04:44.784Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'pinnacle.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:04:47.403Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T20:05:37.487Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T20:05:38.503Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='pinnacle' name='Pinnacle'
2026-06-09T20:05:57.484Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T20:06:57.485Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T20:07:07.500Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: Read timed out
2026-06-09T20:07:07.500Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'pinnacle.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:07:07.500Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.14.66 (config: vpngate-patito). Requesting rotation...
2026-06-09T20:07:12.301Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported block for config 513 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T20:07:12.301Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:07:21.386Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.65' (config: vpngate-vpn503500791, ID: 950, healthy proxies: 8)
2026-06-09T20:07:21.389Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.65:3128
2026-06-09T20:07:21.390Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.65:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:07:21.395Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'pinnacle.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:07:21.396Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'pinnacle.com'...
2026-06-09T20:07:21.466Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: pinnacle.com -> 8.6.112.0
2026-06-09T20:07:21.467Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://pinnacle.com'...
2026-06-09T20:07:31.486Z ERROR 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: Read timed out
2026-06-09T20:07:31.487Z  WARN 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'pinnacle.com': java.net.SocketTimeoutException: Read timed out
2026-06-09T20:07:31.488Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:07:36.492Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'pinnacle.com'...
2026-06-09T20:07:36.493Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: pinnacle.com -> 8.6.112.0
2026-06-09T20:07:36.493Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://pinnacle.com'...
2026-06-09T20:07:43.907Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'pinnacle.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:08:00.233Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 950 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T20:08:21.398Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T20:08:29.535Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 950 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T20:08:38.613Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='pinnacle' name='Pinnacle'
2026-06-09T20:09:29.536Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T20:09:36.324Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 950 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T20:10:36.328Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T20:10:43.138Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 950 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T20:11:38.756Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='pinnacle' name='Pinnacle'
2026-06-09T20:11:43.142Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...

```

---

### 🗂️ Букмекер: `sportbet`

#### 📥 Loader (`igaming-source-sportbet-loader-f4d8b7b44-jqnrh` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-06-09T20:02:02.662Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.sportbet.SportbetApplication     : Starting SportbetApplication v0.0.1-SNAPSHOT using Java 21.0.10 with PID 1 (/app/app.jar started by root in /app)
2026-06-09T20:02:02.665Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.sportbet.SportbetApplication     : The following 1 profile is active: "match-loader"
2026-06-09T20:02:17.862Z  INFO 1 --- [igaming-source-sportbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:02:17.862Z  INFO 1 --- [igaming-source-sportbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-06-09T20:02:21.375Z  INFO 1 --- [igaming-source-sportbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 3248 ms. Found 7 JPA repository interfaces.
2026-06-09T20:02:31.327Z  INFO 1 --- [igaming-source-sportbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:02:31.329Z  INFO 1 --- [igaming-source-sportbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T20:02:31.514Z  INFO 1 --- [igaming-source-sportbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 167 ms. Found 0 Redis repository interfaces.
2026-06-09T20:02:37.586Z  INFO 1 --- [igaming-source-sportbet] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=7cadf96d-6dd9-37eb-8585-07c5d04ce1d8
2026-06-09T20:02:52.472Z  INFO 1 --- [igaming-source-sportbet] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3045 (http)
2026-06-09T20:02:52.907Z  INFO 1 --- [igaming-source-sportbet] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-06-09T20:02:52.907Z  INFO 1 --- [igaming-source-sportbet] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-06-09T20:02:55.168Z  INFO 1 --- [igaming-source-sportbet] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-06-09T20:02:55.173Z  INFO 1 --- [igaming-source-sportbet] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 52126 ms
2026-06-09T20:03:13.036Z  INFO 1 --- [igaming-source-sportbet] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:03:14.807Z  INFO 1 --- [igaming-source-sportbet] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:03:15.788Z  INFO 1 --- [igaming-source-sportbet] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:03:26.571Z  INFO 1 --- [igaming-source-sportbet] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:03:26.985Z  INFO 1 --- [igaming-source-sportbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:03:31.385Z  INFO 1 --- [igaming-source-sportbet] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@3dad8bde
2026-06-09T20:03:31.425Z  INFO 1 --- [igaming-source-sportbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:03:33.560Z  WARN 1 --- [igaming-source-sportbet] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T20:03:34.778Z  INFO 1 --- [igaming-source-sportbet] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:04:03.452Z  INFO 1 --- [igaming-source-sportbet] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:04:05.087Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:05.088Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:05.299Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:05.300Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:05.354Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:05.354Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:04:05.428Z  INFO 1 --- [igaming-source-sportbet] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:04:14.933Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:04:32.479Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.49' (config: vpngate-vpn560167748, ID: 1083, healthy proxies: 11)
2026-06-09T20:04:32.480Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.49:3128
2026-06-09T20:04:32.486Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.49:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:04:32.516Z  INFO 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'sportbet.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:04:32.516Z  INFO 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'sportbet.ru'...
2026-06-09T20:04:32.731Z  INFO 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: sportbet.ru -> 178.248.235.178
2026-06-09T20:04:32.731Z  INFO 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://sportbet.ru'...
2026-06-09T20:04:33.132Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:04:38.812Z ERROR 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://sportbet.ru: Remote host terminated the handshake
2026-06-09T20:04:38.821Z  WARN 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'sportbet.ru': javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
2026-06-09T20:04:38.822Z  INFO 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:04:43.828Z  INFO 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'sportbet.ru'...
2026-06-09T20:04:43.832Z  INFO 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: sportbet.ru -> 178.248.235.178
2026-06-09T20:04:43.832Z  INFO 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://sportbet.ru'...
2026-06-09T20:04:43.986Z  INFO 1 --- [igaming-source-sportbet] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:04:47.922Z  INFO 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'sportbet.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:04:52.467Z  INFO 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-sportbet, host: sportbet.ru)
2026-06-09T20:05:12.495Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:05:12.538Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:05:12.541Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:05:12.543Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:05:12.544Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:05:12.546Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:05:17.522Z  WARN 1 --- [igaming-source-sportbet] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:05:38.348Z  INFO 1 --- [igaming-source-sportbet] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:05:40.399Z  INFO 1 --- [igaming-source-sportbet] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3045 (http) with context path '/'
2026-06-09T20:05:40.950Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.sportbet.SportbetApplication     : Started SportbetApplication in 226.378 seconds (process running for 232.541)
2026-06-09T20:05:41.731Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='sportbet' name='Sportbet'
2026-06-09T20:05:43.872Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'sportbet' (display: 'Sportbet')
2026-06-09T20:05:44.112Z  INFO 1 --- [igaming-source-sportbet] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='sportbet' name='Sportbet'
2026-06-09T20:05:45.008Z  INFO 1 --- [igaming-source-sportbet] [nio-3045-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:05:45.008Z  INFO 1 --- [igaming-source-sportbet] [nio-3045-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:05:45.078Z  INFO 1 --- [igaming-source-sportbet] [nio-3045-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 70 ms
2026-06-09T20:06:10.860Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'sportbet.ru' is still reachable...
2026-06-09T20:06:15.966Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-sportbet, host: sportbet.ru)
2026-06-09T20:07:15.981Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'sportbet.ru' is still reachable...
2026-06-09T20:07:21.814Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-sportbet, host: sportbet.ru)
2026-06-09T20:08:21.816Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'sportbet.ru' is still reachable...
2026-06-09T20:08:27.179Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-sportbet, host: sportbet.ru)
2026-06-09T20:08:41.879Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='sportbet' name='Sportbet'
2026-06-09T20:09:27.198Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'sportbet.ru' is still reachable...
2026-06-09T20:09:30.920Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-sportbet, host: sportbet.ru)
2026-06-09T20:10:30.925Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'sportbet.ru' is still reachable...
2026-06-09T20:10:34.806Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-sportbet, host: sportbet.ru)
2026-06-09T20:11:34.809Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'sportbet.ru' is still reachable...
2026-06-09T20:11:38.703Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1083 (service: igaming-source-sportbet, host: sportbet.ru)
2026-06-09T20:11:42.015Z  INFO 1 --- [igaming-source-sportbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='sportbet' name='Sportbet'

```

---

### 🗂️ Букмекер: `tennisi`

#### 📥 Loader (`igaming-source-tennisi-loader-5b54d88954-xwc8l` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:00:47.872Z  INFO 1 --- [igaming-source-tennisi] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:00:47.896Z  INFO 1 --- [igaming-source-tennisi] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T20:00:48.185Z  INFO 1 --- [igaming-source-tennisi] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 125 ms. Found 0 Redis repository interfaces.
2026-06-09T20:00:51.322Z  INFO 1 --- [igaming-source-tennisi] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=df0a439f-7e5b-3150-a033-097be47afa97
2026-06-09T20:01:01.328Z  INFO 1 --- [igaming-source-tennisi] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3047 (http)
2026-06-09T20:01:01.572Z  INFO 1 --- [igaming-source-tennisi] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-06-09T20:01:01.575Z  INFO 1 --- [igaming-source-tennisi] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-06-09T20:01:02.457Z  INFO 1 --- [igaming-source-tennisi] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-06-09T20:01:02.468Z  INFO 1 --- [igaming-source-tennisi] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 31808 ms
2026-06-09T20:01:09.181Z  INFO 1 --- [igaming-source-tennisi] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:01:10.027Z  INFO 1 --- [igaming-source-tennisi] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:01:10.533Z  INFO 1 --- [igaming-source-tennisi] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:01:15.380Z  INFO 1 --- [igaming-source-tennisi] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:01:15.759Z  INFO 1 --- [igaming-source-tennisi] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:01:18.433Z  INFO 1 --- [igaming-source-tennisi] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@55821edf
2026-06-09T20:01:18.454Z  INFO 1 --- [igaming-source-tennisi] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:01:19.037Z  WARN 1 --- [igaming-source-tennisi] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T20:01:19.424Z  INFO 1 --- [igaming-source-tennisi] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:01:38.827Z  INFO 1 --- [igaming-source-tennisi] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:01:39.908Z  INFO 1 --- [igaming-source-tennisi] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:01:47.773Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:01:50.798Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy 'proxy-vpn-pool.service-proxy.svc.cluster.local' (config: vpngate-vpn343738742, ID: 600, healthy proxies: 18)
2026-06-09T20:01:50.811Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: proxy-vpn-pool.service-proxy.svc.cluster.local:3128
2026-06-09T20:01:50.811Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:01:50.814Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'tennisi.bet' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:01:50.814Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'tennisi.bet'...
2026-06-09T20:01:50.960Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: tennisi.bet -> 185.65.149.197
2026-06-09T20:01:50.960Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://tennisi.bet'...
2026-06-09T20:01:51.120Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:01:57.519Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'tennisi.bet' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:01:58.617Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 600 (service: igaming-source-tennisi, host: tennisi.bet)
2026-06-09T20:02:01.230Z  INFO 1 --- [igaming-source-tennisi] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:02:28.350Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:02:28.360Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:02:28.380Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:02:28.400Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:02:28.429Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:02:28.431Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:02:30.283Z  WARN 1 --- [igaming-source-tennisi] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:02:43.598Z  INFO 1 --- [igaming-source-tennisi] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:02:44.677Z  INFO 1 --- [igaming-source-tennisi] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3047 (http) with context path '/'
2026-06-09T20:02:45.028Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.source.tennisi.TennisiApplication  : Started TennisiApplication in 141.941 seconds (process running for 147.694)
2026-06-09T20:02:45.204Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='tennisi' name='Tennisi'
2026-06-09T20:02:46.312Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'tennisi' (display: 'Tennisi')
2026-06-09T20:02:46.399Z  INFO 1 --- [igaming-source-tennisi] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='tennisi' name='Tennisi'
2026-06-09T20:03:14.971Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'tennisi.bet' is still reachable...
2026-06-09T20:03:15.482Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 600 (service: igaming-source-tennisi, host: tennisi.bet)
2026-06-09T20:03:17.863Z  INFO 1 --- [igaming-source-tennisi] [nio-3047-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:03:17.863Z  INFO 1 --- [igaming-source-tennisi] [nio-3047-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:03:17.873Z  INFO 1 --- [igaming-source-tennisi] [nio-3047-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 9 ms
2026-06-09T20:04:15.485Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'tennisi.bet' is still reachable...
2026-06-09T20:04:25.072Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 600 (service: igaming-source-tennisi, host: tennisi.bet)
2026-06-09T20:05:25.076Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'tennisi.bet' is still reachable...
2026-06-09T20:05:27.144Z  WARN 1 --- [igaming-source-tennisi] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
2026-06-09T20:05:45.336Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='tennisi' name='Tennisi'
2026-06-09T20:06:27.146Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'tennisi.bet' is still reachable...
2026-06-09T20:06:39.859Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 600 (service: igaming-source-tennisi, host: tennisi.bet)
2026-06-09T20:07:39.861Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'tennisi.bet' is still reachable...
2026-06-09T20:07:45.355Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 600 (service: igaming-source-tennisi, host: tennisi.bet)
2026-06-09T20:08:45.356Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'tennisi.bet' is still reachable...
2026-06-09T20:08:45.445Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='tennisi' name='Tennisi'
2026-06-09T20:08:52.080Z ERROR 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://tennisi.bet: Unexpected end of file from server
2026-06-09T20:08:52.080Z ERROR 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'tennisi.bet' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:08:52.081Z  WARN 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: proxy-vpn-pool.service-proxy.svc.cluster.local (config: vpngate-vpn343738742). Requesting rotation...
2026-06-09T20:08:52.627Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported block for config 600 (service: igaming-source-tennisi, host: tennisi.bet)
2026-06-09T20:08:52.629Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:08:54.988Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.73' (config: vpngate-vpn311746592, ID: 1230, healthy proxies: 12)
2026-06-09T20:08:54.988Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.73:3128
2026-06-09T20:08:54.992Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.73:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:08:54.996Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'tennisi.bet' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:08:54.996Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'tennisi.bet'...
2026-06-09T20:08:55.061Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: tennisi.bet -> 185.65.149.197
2026-06-09T20:08:55.063Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://tennisi.bet'...
2026-06-09T20:08:55.386Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'tennisi.bet' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:08:56.141Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1230 (service: igaming-source-tennisi, host: tennisi.bet)
2026-06-09T20:09:54.996Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'tennisi.bet' is still reachable...
2026-06-09T20:09:55.502Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1230 (service: igaming-source-tennisi, host: tennisi.bet)
2026-06-09T20:10:55.503Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'tennisi.bet' is still reachable...
2026-06-09T20:11:05.514Z ERROR 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://tennisi.bet: Connect timed out
2026-06-09T20:11:05.514Z ERROR 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'tennisi.bet' is unreachable through current proxy! Triggering rotation...
2026-06-09T20:11:05.514Z  WARN 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.15.73 (config: vpngate-vpn311746592). Requesting rotation...
2026-06-09T20:11:05.699Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 1230 (service: igaming-source-tennisi, host: tennisi.bet)
2026-06-09T20:11:05.699Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:11:06.233Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.65' (config: vpngate-vpn503500791, ID: 950, healthy proxies: 8)
2026-06-09T20:11:06.233Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.65:3128
2026-06-09T20:11:06.233Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.65:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:11:06.234Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'tennisi.bet' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:11:06.234Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'tennisi.bet'...
2026-06-09T20:11:06.337Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: tennisi.bet -> 185.65.149.197
2026-06-09T20:11:06.337Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://tennisi.bet'...
2026-06-09T20:11:09.176Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'tennisi.bet' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:11:09.301Z  INFO 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 950 (service: igaming-source-tennisi, host: tennisi.bet)
2026-06-09T20:11:45.573Z  INFO 1 --- [igaming-source-tennisi] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='tennisi' name='Tennisi'

```

---

### 🗂️ Букмекер: `winline`

#### 🕷️ Crawler (`igaming-source-winline-crawler-6c5667b6b4-qkfgv` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:07:54.214Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T20:08:01.149Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-winline, host: winline.ru)
2026-06-09T20:08:26.685Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.w.service.WinlineApiClient       : Waiting for apiWlb to be available...
2026-06-09T20:08:29.801Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.w.service.WinlineApiClient       : Executing SAPI extraction script for prematch events...
2026-06-09T20:08:31.516Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from winline.ru...
2026-06-09T20:08:31.517Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.service.WinlineApiClient       : Fetching Winline live events via apiWlb SAPI from https://winline.ru/live
2026-06-09T20:08:31.685Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.service.WinlineApiClient       : Navigating to https://winline.ru/live...
2026-06-09T20:08:56.056Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.w.service.WinlineApiClient       : DIAG_PARAM: [{"eventId":15986141,"freeText":"Фора [a] (@NP@)","koef":"0","V":[1.9,1.8],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1454969526,"fullJson":"{\"id\":1454969526,\"idEvent\":15986141,\"idTipMarket\":1073,\"koef\":\"0\",\"V\":[1.9,1.8],\"countV\":2,\"favorite\":0,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781035685869,\"version\":0},\"$loki\":634},\"isLive\":0,\"idLine\":1454969526,\"kol\":2,\"meta\":{\"revision\":0,\"created\":1781035710754,\"version\":0},\"$loki\":16097}"},{"eventId":15986141,"freeText":"Фора [a] (@NP@)","koef":"1.5","V":[1.2,3.9],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1454969536,"fullJson":"{\"id\":1454969536,\"idEvent\":15986141,\"idTipMarket\":1073,\"koef\":\"1.5\",\"V\":[1.2,3.9],\"countV\":2,\"favorite\":2,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781035685869,\"version\":0},\"$loki\":634},\"isLive\":0,\"idLine\":1454969536,\"kol\":2,\"meta\":{\"revision\":0,\"created\":1781035710754,\"version\":0},\"$loki\":16103}"}]
2026-06-09T20:08:58.400Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.w.service.WinlineApiClient       : Successfully extracted 1825 Winline prematch events with 38811 total odds
2026-06-09T20:09:01.507Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T20:09:05.588Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-winline, host: winline.ru)
2026-06-09T20:09:31.790Z ERROR 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.service.WinlineApiClient       : Fatal error during Winline live extraction: Error {
  message='Timeout 60000ms exceeded.
  name='TimeoutError
  stack='TimeoutError: Timeout 60000ms exceeded.
    at ProgressController.run (/tmp/playwright-java-8981021884922202514/package/lib/server/progress.js:78:26)
    at Frame.goto (/tmp/playwright-java-8981021884922202514/package/lib/server/frames.js:517:23)
    at FrameDispatcher.goto (/tmp/playwright-java-8981021884922202514/package/lib/server/dispatchers/frameDispatcher.js:81:119)
    at FrameDispatcher._handleCommand (/tmp/playwright-java-8981021884922202514/package/lib/server/dispatchers/dispatcher.js:94:40)
    at DispatcherConnection.dispatch (/tmp/playwright-java-8981021884922202514/package/lib/server/dispatchers/dispatcher.js:365:39)
}
Call log:
- navigating to "https://winline.ru/live", waiting until "load"


com.microsoft.playwright.TimeoutError: Error {
  message='Timeout 60000ms exceeded.
  name='TimeoutError
  stack='TimeoutError: Timeout 60000ms exceeded.
    at ProgressController.run (/tmp/playwright-java-8981021884922202514/package/lib/server/progress.js:78:26)
    at Frame.goto (/tmp/playwright-java-8981021884922202514/package/lib/server/frames.js:517:23)
    at FrameDispatcher.goto (/tmp/playwright-java-8981021884922202514/package/lib/server/dispatchers/frameDispatcher.js:81:119)
    at FrameDispatcher._handleCommand (/tmp/playwright-java-8981021884922202514/package/lib/server/dispatchers/dispatcher.js:94:40)
    at DispatcherConnection.dispatch (/tmp/playwright-java-8981021884922202514/package/lib/server/dispatchers/dispatcher.js:365:39)
}
Call log:
- navigating to "https://winline.ru/live", waiting until "load"

	at com.microsoft.playwright.impl.WaitableResult.get(WaitableResult.java:52) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.ChannelOwner.runUntil(ChannelOwner.java:120) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.Connection.sendMessage(Connection.java:130) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.ChannelOwner.sendMessage(ChannelOwner.java:106) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.FrameImpl.navigateImpl(FrameImpl.java:463) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.PageImpl.lambda$navigate$46(PageImpl.java:870) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.LoggingSupport.withLogging(LoggingSupport.java:47) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.ChannelOwner.withLogging(ChannelOwner.java:89) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.PageImpl.navigate(PageImpl.java:870) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.PageImpl.navigate(PageImpl.java:42) ~[playwright-1.42.0.jar!/:1.42.0]
	at pro.datawiki.igaming.source.winline.service.WinlineApiClient.fetchEvents(WinlineApiClient.java:83) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.winline.service.WinlineApiClient.fetchLiveEvents(WinlineApiClient.java:61) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.winline.scheduler.MatchFetchScheduler.fetchLiveData(MatchFetchScheduler.java:26) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.runInternal(ScheduledMethodRunnable.java:130) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.lambda$run$2(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at io.micrometer.observation.Observation.observe(Observation.java:498) ~[micrometer-observation-1.14.2.jar!/:1.14.2]
	at org.springframework.scheduling.support.ScheduledMethodRunnable.run(ScheduledMethodRunnable.java:124) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.config.Task$OutcomeTrackingRunnable.run(Task.java:85) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.runAndReset(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: com.microsoft.playwright.TimeoutError: Error {
  message='Timeout 60000ms exceeded.
  name='TimeoutError
  stack='TimeoutError: Timeout 60000ms exceeded.
    at ProgressController.run (/tmp/playwright-java-8981021884922202514/package/lib/server/progress.js:78:26)
    at Frame.goto (/tmp/playwright-java-8981021884922202514/package/lib/server/frames.js:517:23)
    at FrameDispatcher.goto (/tmp/playwright-java-8981021884922202514/package/lib/server/dispatchers/frameDispatcher.js:81:119)
    at FrameDispatcher._handleCommand (/tmp/playwright-java-8981021884922202514/package/lib/server/dispatchers/dispatcher.js:94:40)
    at DispatcherConnection.dispatch (/tmp/playwright-java-8981021884922202514/package/lib/server/dispatchers/dispatcher.js:365:39)
}
Call log:
- navigating to "https://winline.ru/live", waiting until "load"

	at com.microsoft.playwright.impl.Connection.dispatch(Connection.java:254) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.Connection.processOneMessage(Connection.java:211) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.ChannelOwner.runUntil(ChannelOwner.java:118) ~[playwright-1.42.0.jar!/:1.42.0]
	... 25 common frames omitted

2026-06-09T20:09:57.012Z  WARN 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.c.s.SportNormalizationService    : ALARM ALARM new sport: bookmaker='winline', sportName='Австралийский футбол'
2026-06-09T20:10:05.591Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T20:10:08.560Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-winline, host: winline.ru)
2026-06-09T20:10:24.557Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='winline' name='Winline'
2026-06-09T20:10:31.793Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from winline.ru...
2026-06-09T20:10:31.794Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.service.WinlineApiClient       : Fetching Winline live events via apiWlb SAPI from https://winline.ru/live
2026-06-09T20:10:31.953Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.service.WinlineApiClient       : Navigating to https://winline.ru/live...
2026-06-09T20:10:50.299Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.service.WinlineApiClient       : Waiting for apiWlb to be available...
2026-06-09T20:10:53.416Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.service.WinlineApiClient       : Executing SAPI extraction script for live events...
2026-06-09T20:10:56.787Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.service.WinlineApiClient       : DIAG_PARAM: [{"eventId":15953242,"freeText":"Фора [a] (@NP@)","koef":"3","V":[1.13,5],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455354848,"fullJson":"{\"id\":1455354848,\"idEvent\":15953242,\"idTipMarket\":1073,\"koef\":\"3\",\"V\":[1.13,5],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781035838663,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455354848,\"meta\":{\"revision\":1,\"created\":1781035842693,\"version\":0,\"updated\":1781035845768},\"$loki\":30}"},{"eventId":15953242,"freeText":"Фора [a] (@NP@)","koef":"4","V":[9,1.02],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455380720,"fullJson":"{\"id\":1455380720,\"idEvent\":15953242,\"idTipMarket\":1073,\"koef\":\"4\",\"V\":[9,1.02],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781035838663,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455380720,\"meta\":{\"revision\":0,\"created\":1781035845768,\"version\":0},\"$loki\":2647}"},{"eventId":15953242,"freeText":"Фора [a] (@NP@)","koef":"3.5","V":[3.1,1.32],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455371529,"fullJson":"{\"id\":1455371529,\"idEvent\":15953242,\"idTipMarket\":1073,\"koef\":\"3.5\",\"V\":[3.1,1.32],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781035838663,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455371529,\"meta\":{\"revision\":1,\"created\":1781035842693,\"version\":0,\"updated\":1781035845768},\"$loki\":27}"},{"eventId":15953242,"freeText":"Фора [a] (@NP@)","koef":"0","V":[1,10],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455328142,"fullJson":"{\"id\":1455328142,\"idEvent\":15953242,\"idTipMarket\":1073,\"koef\":\"0\",\"V\":[1,10],\"countV\":2,\"favorite\":0,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781035838663,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455328142,\"meta\":{\"revision\":0,\"created\":1781035842693,\"version\":0},\"$loki\":5}"}]
2026-06-09T20:10:56.890Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.service.WinlineApiClient       : Successfully extracted 114 Winline live events with 6080 total odds
2026-06-09T20:11:03.355Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.c.engine.AbstractMatchService    : Discovered 1825 prematch winline events
2026-06-09T20:11:03.356Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled prematch fetch completed: 1825 events discovered
2026-06-09T20:11:04.152Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.c.engine.AbstractMatchService    : Discovered 114 live winline events
2026-06-09T20:11:04.152Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled live fetch completed: 114 events discovered
2026-06-09T20:11:08.561Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T20:11:12.218Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-winline, host: winline.ru)

```
#### 📥 Loader (`igaming-source-winline-loader-955ddcf9c-24k6z` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:01:39.025Z  WARN 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:39.026Z  WARN 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:39.067Z  WARN 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:39.067Z  WARN 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:07:41.551Z  WARN 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
```
**Последние строки логов:**
```text
2026-06-09T20:11:46.004Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T20:11:46.494Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='winline' name='Winline'
2026-06-09T20:11:47.496Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1412 (service: igaming-source-winline, host: winline.ru)
```

---

### 🗂️ Букмекер: `zenit`

#### 🕷️ Crawler (`igaming-source-zenit-crawler-5f75984c46-rrxcr` - Running)
**Анализ ошибок:**
```text
2026-06-09T20:11:11.991Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Zenit API fetch stats: failed to load 1 out of 5 pages in the last cycle. Reasons: {Empty response from browser=1}
```
**Последние строки логов:**
```text
2026-06-09T20:11:43.940Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-2] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : [zenit] Discovered 8 live events
2026-06-09T20:11:43.941Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-2] p.d.i.s.z.scheduler.MatchFetchScheduler  : Finished LIVE discovery job, discovered: 8
2026-06-09T20:11:48.432Z  INFO 1 --- [igaming-source-zenit] [enit-producer-1] org.apache.kafka.clients.NetworkClient   : [Producer clientId=igaming-source-zenit-producer-1] Node -1 disconnected.
```
#### 📥 Loader (`igaming-source-zenit-loader-6d6dcd6594-z74vp` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-06-09T20:02:54.095Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.source.zenit.ZenitApplication      : Starting ZenitApplication v0.0.1-SNAPSHOT using Java 21.0.10 with PID 1 (/app/app.jar started by root in /app)
2026-06-09T20:02:54.115Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.source.zenit.ZenitApplication      : The following 1 profile is active: "match-loader"
2026-06-09T20:03:16.908Z  INFO 1 --- [igaming-source-zenit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:03:16.922Z  INFO 1 --- [igaming-source-zenit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-06-09T20:03:22.659Z  INFO 1 --- [igaming-source-zenit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 5554 ms. Found 7 JPA repository interfaces.
2026-06-09T20:03:30.606Z  INFO 1 --- [igaming-source-zenit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T20:03:30.646Z  INFO 1 --- [igaming-source-zenit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T20:03:31.086Z  INFO 1 --- [igaming-source-zenit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 177 ms. Found 0 Redis repository interfaces.
2026-06-09T20:03:40.718Z  INFO 1 --- [igaming-source-zenit] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=443cb054-c94b-31c5-b8aa-496d699159cd
2026-06-09T20:03:59.918Z  INFO 1 --- [igaming-source-zenit] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3044 (http)
2026-06-09T20:04:00.201Z  INFO 1 --- [igaming-source-zenit] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-06-09T20:04:00.201Z  INFO 1 --- [igaming-source-zenit] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-06-09T20:04:02.388Z  INFO 1 --- [igaming-source-zenit] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-06-09T20:04:02.427Z  INFO 1 --- [igaming-source-zenit] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 67825 ms
2026-06-09T20:04:18.168Z  INFO 1 --- [igaming-source-zenit] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T20:04:19.734Z  INFO 1 --- [igaming-source-zenit] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T20:04:20.790Z  INFO 1 --- [igaming-source-zenit] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T20:04:29.066Z  INFO 1 --- [igaming-source-zenit] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T20:04:30.026Z  INFO 1 --- [igaming-source-zenit] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T20:04:33.894Z  INFO 1 --- [igaming-source-zenit] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@4700963e
2026-06-09T20:04:33.931Z  INFO 1 --- [igaming-source-zenit] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T20:04:34.870Z  WARN 1 --- [igaming-source-zenit] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T20:04:35.506Z  INFO 1 --- [igaming-source-zenit] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T20:05:02.068Z  INFO 1 --- [igaming-source-zenit] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T20:05:03.470Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:03.471Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:03.656Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:03.662Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:03.741Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:03.742Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:05:03.815Z  INFO 1 --- [igaming-source-zenit] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T20:05:10.064Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T20:05:30.004Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.15.47' (config: vpngate-2i6, ID: 853, healthy proxies: 11)
2026-06-09T20:05:30.017Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.15.47:3128
2026-06-09T20:05:30.017Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.15.47:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T20:05:30.084Z  INFO 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'zenit.win' through proxy (3 retries, 5000ms delay)...
2026-06-09T20:05:30.085Z  INFO 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'zenit.win'...
2026-06-09T20:05:30.302Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T20:05:30.304Z  INFO 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: zenit.win -> 185.65.148.74
2026-06-09T20:05:30.304Z  INFO 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://zenit.win'...
2026-06-09T20:05:39.924Z  INFO 1 --- [igaming-source-zenit] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T20:05:46.708Z ERROR 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://zenit.win: Read timed out
2026-06-09T20:05:46.713Z  WARN 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'zenit.win': java.net.SocketTimeoutException: Read timed out
2026-06-09T20:05:46.714Z  INFO 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T20:05:51.731Z  INFO 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'zenit.win'...
2026-06-09T20:05:51.731Z  INFO 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: zenit.win -> 185.65.148.74
2026-06-09T20:05:51.731Z  INFO 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://zenit.win'...
2026-06-09T20:05:55.503Z  INFO 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'zenit.win' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T20:05:58.594Z  INFO 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-zenit, host: zenit.win)
2026-06-09T20:06:04.478Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T20:06:04.526Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T20:06:04.552Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T20:06:04.589Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T20:06:04.613Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T20:06:04.646Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T20:06:08.618Z  WARN 1 --- [igaming-source-zenit] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-06-09T20:06:31.019Z  INFO 1 --- [igaming-source-zenit] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T20:06:33.019Z  INFO 1 --- [igaming-source-zenit] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3044 (http) with context path '/'
2026-06-09T20:06:33.433Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.source.zenit.ZenitApplication      : Started ZenitApplication in 234.093 seconds (process running for 247.686)
2026-06-09T20:06:33.812Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='zenit' name='Zenit'
2026-06-09T20:06:36.741Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'zenit' (display: 'Zenit')
2026-06-09T20:06:37.007Z  INFO 1 --- [igaming-source-zenit] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='zenit' name='Zenit'
2026-06-09T20:06:42.759Z  INFO 1 --- [igaming-source-zenit] [nio-3044-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-06-09T20:06:42.773Z  INFO 1 --- [igaming-source-zenit] [nio-3044-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-06-09T20:06:42.790Z  INFO 1 --- [igaming-source-zenit] [nio-3044-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 14 ms
2026-06-09T20:07:03.375Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'zenit.win' is still reachable...
2026-06-09T20:07:17.144Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-zenit, host: zenit.win)
2026-06-09T20:08:17.158Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'zenit.win' is still reachable...
2026-06-09T20:08:20.694Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-zenit, host: zenit.win)
2026-06-09T20:09:20.696Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'zenit.win' is still reachable...
2026-06-09T20:09:24.785Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-zenit, host: zenit.win)
2026-06-09T20:09:33.944Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='zenit' name='Zenit'
2026-06-09T20:10:24.807Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'zenit.win' is still reachable...
2026-06-09T20:10:28.148Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-zenit, host: zenit.win)
2026-06-09T20:11:28.159Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'zenit.win' is still reachable...
2026-06-09T20:11:31.261Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-zenit, host: zenit.win)

```

---

