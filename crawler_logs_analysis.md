# 🕵️ Анализ логов кроулеров и лоадеров

**Время анализа:** 2026-07-10 11:12:41

## 📌 Сводная таблица по всем БК

| Букмекер | Краулер Статус | Лоадер Статус | Анализ ошибок краулера | Анализ ошибок лоадера |
| :--- | :--- | :--- | :--- | :--- |
| `1xbit` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-07-10T07:57:47.912Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for ... |
| `baltbet` | Running | Running | 2026-07-10T08:10:06.773Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.source.baltbet.BaltbetApplication  : Starting BaltbetApplication... | 2026-07-10T07:58:27.689Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat fo... |
| `betboom` | Running | Running | 2026-07-10T08:02:05.833Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuratio... | 2026-07-10T07:58:52.426Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat fo... |
| `betcity` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-07-10T07:58:18.329Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat fo... |
| `betm` | Running | Failed | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-07-09T19:02:54.920Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93ar... |
| `bettery` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-07-10T07:59:24.914Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat fo... |
| `betwinner` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-07-10T07:58:05.962Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat ... |
| `dafabet` | Failed | N/A | ERROR: Error from server (BadRequest): container "igaming-source-dafabet-crawler" in pod "igaming-source-dafabet-crawler-787db4976b-vtbqm" is terminat... | - |
| `digitain` | Failed | N/A | 	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']<br>	Database driver: undefined/unknown<br>	Database version: 15.1... | - |
| `fanduel` | Failed | N/A | 2026-07-09T19:03:19.092Z  WARN 1 --- [igaming-source-fanduel] [           main] o.h.e.j.e.i.JdbcEnvironmentInitiator     : HHH000342: Could not obtain... | - |
| `fon-bet-kz` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-07-10T07:58:05.049Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat... |
| `fon-bet-ru` | Running | Running |  | 2026-07-10T07:57:47.876Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat... |
| `marathonbet` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-07-10T07:57:48.862Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbea... |
| `marathonbet-by` | Running | Running | TIMEOUT: Command timed out after 15 seconds | 2026-07-10T07:58:34.294Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heart... |
| `marathonbet-com` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-07-10T07:59:18.394Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send hear... |
| `melbet-com` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-07-10T07:57:59.458Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat... |
| `mrgreen` | Running | Running | 2026-07-10T08:10:05.886Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-2] p.d.i.s.s.service.Sport888ApiClient      : Failed to intercept JSON f... | 2026-07-10T07:58:00.359Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat f... |
| `nordicbet` | N/A | Failed | - | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... |
| `olimpbet` | Running | Running | 2026-07-10T08:11:27.156Z  INFO 1 --- [igaming-source-olimpbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown in... | OK (No errors found in last 100 lines) |
| `pari` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-07-10T08:00:11.928Z  WARN 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for b... |
| `pinnacle` | Failed | N/A | OK (No errors found in last 100 lines) | - |
| `spinbetter` | Failed | N/A | OK (No errors found in last 100 lines) | - |
| `zenit` | Running | Running | OK (No errors found in last 100 lines) | 2026-07-10T07:59:47.371Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for ... |


## 📝 Детальные логи и контекст по проблемным БК

### 🗂️ Букмекер: `1xbit`

#### 🕷️ Crawler (`igaming-source-1xbit-crawler-7c8cdd6855-cqfhb` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-10T08:08:39.502Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.source.betb2b.Betb2bApplication    : Starting Betb2bApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-10T08:08:39.514Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.source.betb2b.Betb2bApplication    : The following 1 profile is active: "league-crawler"
2026-07-10T08:08:45.682Z  INFO 1 --- [igaming-source-1xbit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:08:45.682Z  INFO 1 --- [igaming-source-1xbit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-10T08:08:46.629Z  INFO 1 --- [igaming-source-1xbit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 880 ms. Found 7 JPA repository interfaces.
2026-07-10T08:08:49.496Z  INFO 1 --- [igaming-source-1xbit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:08:49.510Z  INFO 1 --- [igaming-source-1xbit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-10T08:08:49.613Z  INFO 1 --- [igaming-source-1xbit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 49 ms. Found 0 Redis repository interfaces.
2026-07-10T08:08:51.096Z  INFO 1 --- [igaming-source-1xbit] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=dff05cfc-8476-3d3e-944a-a76586f4c35e
2026-07-10T08:08:55.104Z  INFO 1 --- [igaming-source-1xbit] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3059 (http)
2026-07-10T08:08:55.173Z  INFO 1 --- [igaming-source-1xbit] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-07-10T08:08:55.174Z  INFO 1 --- [igaming-source-1xbit] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-07-10T08:08:55.649Z  INFO 1 --- [igaming-source-1xbit] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-07-10T08:08:55.655Z  INFO 1 --- [igaming-source-1xbit] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 15709 ms
2026-07-10T08:08:59.083Z  INFO 1 --- [igaming-source-1xbit] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-10T08:08:59.559Z  INFO 1 --- [igaming-source-1xbit] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-10T08:08:59.704Z  INFO 1 --- [igaming-source-1xbit] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-10T08:09:01.926Z  INFO 1 --- [igaming-source-1xbit] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-10T08:09:02.127Z  INFO 1 --- [igaming-source-1xbit] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-10T08:09:02.159Z  INFO 1 --- [igaming-source-1xbit] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-10T08:09:03.351Z  WARN 1 --- [igaming-source-1xbit] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-07-10T08:09:03.463Z  INFO 1 --- [igaming-source-1xbit] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-10T08:09:09.111Z  INFO 1 --- [igaming-source-1xbit] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-10T08:09:09.504Z  INFO 1 --- [igaming-source-1xbit] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:09:11.376Z  INFO 1 --- [igaming-source-1xbit] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-10T08:09:19.141Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-10T08:09:19.176Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to '1xbit.com' (no proxy)...
2026-07-10T08:09:20.464Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ [Startup] Direct connection to '1xbit.com' works — no proxy needed.
2026-07-10T08:09:20.465Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.core.service.VpnManagerService   : Proxy system properties cleared — using direct connection.
2026-07-10T08:09:20.490Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-10T08:09:20.781Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-10T08:09:20.783Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-10T08:09:20.826Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-10T08:09:20.831Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-10T08:09:20.838Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-10T08:09:20.840Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-10T08:09:22.052Z  WARN 1 --- [igaming-source-1xbit] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-07-10T08:09:26.632Z  INFO 1 --- [igaming-source-1xbit] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-07-10T08:09:27.046Z  INFO 1 --- [igaming-source-1xbit] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3059 (http) with context path '/'
2026-07-10T08:09:27.150Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.source.betb2b.Betb2bApplication    : Started Betb2bApplication in 51.902 seconds (process running for 54.683)
2026-07-10T08:09:27.163Z  INFO 1 --- [igaming-source-1xbit] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting periodic event discovery for Betb2b Family...
2026-07-10T08:09:27.163Z  INFO 1 --- [igaming-source-1xbit] [ loader-sched-2] .d.i.s.c.e.x.s.AbstractXbetFamilyService : Starting Xbet Family event discovery for bookmaker: 1xbit...
2026-07-10T08:09:27.164Z  INFO 1 --- [igaming-source-1xbit] [ loader-sched-2] .s.c.e.x.s.AbstractBetB2BFamilyApiClient : Rewrote URL from https://1xbit.com/LiveFeed/Get1xMatchByLeague to https://1xbit.com/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&lng=en&partner=104 (useServiceApi=true)
2026-07-10T08:09:27.164Z  INFO 1 --- [igaming-source-1xbit] [ loader-sched-2] .s.c.e.x.s.AbstractBetB2BFamilyApiClient : Fetching LIVE from service-api URL: https://1xbit.com/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&lng=en&partner=104
2026-07-10T08:09:27.224Z  INFO 1 --- [igaming-source-1xbit] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-07-10T08:09:27.930Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '1xbit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/1xbit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:28.026Z  INFO 1 --- [igaming-source-1xbit] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker '1xbit' (display: '1xBit')
2026-07-10T08:09:28.045Z  WARN 1 --- [igaming-source-1xbit] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '1xbit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/1xbit/heartbeat": igaming-aggregator
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-10T08:09:30.732Z  INFO 1 --- [igaming-source-1xbit] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-10T08:09:32.422Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://1xbit.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=104&virtualSports=true
2026-07-10T08:09:32.825Z ERROR 1 --- [igaming-source-1xbit] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://1xbit.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=104&virtualSports=true: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://1xbit.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=104&virtualSports=true
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://1xbit.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=104&virtualSports=true
    at FrameSession._navigate (/tmp/playwright-java-9188807481566454677/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-9188807481566454677/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://1xbit.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=104&virtualSports=true", waiting until "domcontentloaded"

2026-07-10T08:09:32.825Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-1xbit'
2026-07-10T08:09:32.950Z ERROR 1 --- [igaming-source-1xbit] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-1xbit' — pool exhausted.
2026-07-10T08:09:32.951Z ERROR 1 --- [igaming-source-1xbit] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker '1xbit'. Terminating pod with exit code 1.
2026-07-10T08:09:34.959Z  INFO 1 --- [igaming-source-1xbit] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-07-10T08:09:34.963Z  INFO 1 --- [igaming-source-1xbit] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
2026-07-10T08:10:04.981Z  INFO 1 --- [igaming-source-1xbit] [ionShutdownHook] o.s.c.support.DefaultLifecycleProcessor  : Shutdown phase 1073741823 ends with 1 bean still running after timeout of 30000ms: [taskScheduler]
2026-07-10T08:10:05.006Z  INFO 1 --- [igaming-source-1xbit] [ionShutdownHook] p.d.i.s.core.browser.BrowserService      : Shutting down Universal Browser Service
2026-07-10T08:10:05.391Z  INFO 1 --- [igaming-source-1xbit] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:10:05.395Z  INFO 1 --- [igaming-source-1xbit] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-10T08:10:05.402Z  INFO 1 --- [igaming-source-1xbit] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

```
#### 📥 Loader (`igaming-source-1xbit-loader-67d8868df9-nft96` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:57:47.912Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '1xbit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/1xbit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:00:47.918Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '1xbit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/1xbit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:03:47.929Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '1xbit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/1xbit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:06:47.935Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '1xbit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/1xbit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:47.950Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '1xbit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/1xbit/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:03:47.929Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '1xbit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/1xbit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:06:47.935Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '1xbit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/1xbit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:47.950Z  WARN 1 --- [igaming-source-1xbit] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '1xbit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/1xbit/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `baltbet`

#### 🕷️ Crawler (`igaming-source-baltbet-crawler-6968678df4-h5ppz` - Running)
**Анализ ошибок:**
```text
2026-07-10T08:10:06.773Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.source.baltbet.BaltbetApplication  : Starting BaltbetApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-10T08:10:06.777Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.source.baltbet.BaltbetApplication  : The following 1 profile is active: "league-crawler"
2026-07-10T08:10:11.225Z  INFO 1 --- [igaming-source-baltbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:10:11.226Z  INFO 1 --- [igaming-source-baltbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-10T08:10:11.934Z  INFO 1 --- [igaming-source-baltbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 676 ms. Found 7 JPA repository interfaces.
2026-07-10T08:10:13.625Z  INFO 1 --- [igaming-source-baltbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:10:13.629Z  INFO 1 --- [igaming-source-baltbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-10T08:10:13.675Z  INFO 1 --- [igaming-source-baltbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 18 ms. Found 0 Redis repository interfaces.
2026-07-10T08:10:14.933Z  INFO 1 --- [igaming-source-baltbet] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=be061408-cd25-38d9-835a-154dfa5e5655
2026-07-10T08:10:18.476Z  INFO 1 --- [igaming-source-baltbet] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3043 (http)
2026-07-10T08:10:18.558Z  INFO 1 --- [igaming-source-baltbet] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-07-10T08:10:18.558Z  INFO 1 --- [igaming-source-baltbet] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-07-10T08:10:18.980Z  INFO 1 --- [igaming-source-baltbet] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-07-10T08:10:18.984Z  INFO 1 --- [igaming-source-baltbet] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 11939 ms
2026-07-10T08:10:22.044Z  INFO 1 --- [igaming-source-baltbet] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-10T08:10:22.391Z  INFO 1 --- [igaming-source-baltbet] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-10T08:10:22.558Z  INFO 1 --- [igaming-source-baltbet] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-10T08:10:24.054Z  INFO 1 --- [igaming-source-baltbet] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-10T08:10:24.247Z  INFO 1 --- [igaming-source-baltbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-10T08:10:24.325Z  INFO 1 --- [igaming-source-baltbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-10T08:10:25.536Z  INFO 1 --- [igaming-source-baltbet] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-10T08:10:30.298Z  INFO 1 --- [igaming-source-baltbet] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-10T08:10:30.545Z  INFO 1 --- [igaming-source-baltbet] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:10:30.643Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-10T08:10:31.655Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'events.baltbet.ru' (no proxy)...
2026-07-10T08:10:32.510Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.core.service.VpnManagerService   : ❌ [Startup] Direct connection to 'events.baltbet.ru' failed — requesting proxy from pool...
2026-07-10T08:10:32.511Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:10:32.779Z  WARN 1 --- [igaming-source-baltbet] [           main] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:10:32.840Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-10T08:10:34.133Z  INFO 1 --- [igaming-source-baltbet] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-10T08:10:39.457Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-10T08:10:39.460Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-10T08:10:39.468Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-10T08:10:39.470Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-10T08:10:39.471Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-10T08:10:39.473Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-10T08:10:40.352Z  WARN 1 --- [igaming-source-baltbet] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-07-10T08:10:44.228Z  INFO 1 --- [igaming-source-baltbet] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-07-10T08:10:44.549Z  INFO 1 --- [igaming-source-baltbet] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3043 (http) with context path '/'
2026-07-10T08:10:44.639Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.source.baltbet.BaltbetApplication  : Started BaltbetApplication in 40.963 seconds (process running for 43.321)
2026-07-10T08:10:44.646Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Updating sports dictionaries...
2026-07-10T08:10:44.647Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.b.service.BaltbetApiClient       : Fetching sports dictionary from https://events.baltbet.ru/api/prematch/sports
2026-07-10T08:10:44.647Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-07-10T08:10:44.649Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.b.scheduler.MatchFetchScheduler  : Updating sports dictionaries...
2026-07-10T08:10:44.649Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.b.service.BaltbetApiClient       : Fetching sports dictionary from https://events.baltbet.ru/api/prematch/sports
2026-07-10T08:10:44.845Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'baltbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/baltbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:45.331Z  INFO 1 --- [igaming-source-baltbet] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'baltbet' (display: 'Baltbet')
2026-07-10T08:10:45.334Z  WARN 1 --- [igaming-source-baltbet] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'baltbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/baltbet/heartbeat": igaming-aggregator
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-10T08:10:47.670Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: BASIC and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-10T08:10:48.974Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://events.baltbet.ru/api/prematch/sports: Cannot find object to call __adopt__: page@1afa15027490bda2597f77e9447b4771
2026-07-10T08:10:48.975Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.b.service.BaltbetApiClient       : Failed to fetch Baltbet sports from https://events.baltbet.ru/api/prematch/sports
2026-07-10T08:10:48.976Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.b.service.BaltbetApiClient       : Fetching sports dictionary from https://events.baltbet.ru/api/live/sports
2026-07-10T08:10:49.349Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://events.baltbet.ru/api/live/sports: Object doesn't exist: request@7465560da3490c4ea6c850a0dae4dd4d
2026-07-10T08:10:49.440Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://events.baltbet.ru/api/prematch/sports
2026-07-10T08:10:49.834Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.b.service.BaltbetApiClient       : Failed to fetch Baltbet sports from https://events.baltbet.ru/api/live/sports
2026-07-10T08:10:49.836Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Sports dictionaries updated successfully
2026-07-10T08:10:49.836Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting scheduled LIVE fetch job
2026-07-10T08:10:49.836Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.b.service.BaltbetApiClient       : Fetching LIVE snapshot (init) from https://events.baltbet.ru/api/live/table/fetch
2026-07-10T08:10:50.538Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://events.baltbet.ru/api/prematch/sports: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://events.baltbet.ru/api/prematch/sports
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://events.baltbet.ru/api/prematch/sports
    at FrameSession._navigate (/tmp/playwright-java-15962637050744328480/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-15962637050744328480/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://events.baltbet.ru/api/prematch/sports", waiting until "domcontentloaded"

2026-07-10T08:10:50.538Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-baltbet'
2026-07-10T08:10:50.625Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-baltbet' — pool exhausted.
2026-07-10T08:10:50.626Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'baltbet'. Terminating pod with exit code 1.
2026-07-10T08:10:50.631Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://events.baltbet.ru/api/live/table/fetch
2026-07-10T08:10:50.891Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://events.baltbet.ru/api/live/table/fetch: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://events.baltbet.ru/api/live/table/fetch
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://events.baltbet.ru/api/live/table/fetch
    at FrameSession._navigate (/tmp/playwright-java-15962637050744328480/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-15962637050744328480/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://events.baltbet.ru/api/live/table/fetch", waiting until "domcontentloaded"

2026-07-10T08:10:50.892Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #2 for service 'igaming-source-baltbet'
2026-07-10T08:10:50.894Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-baltbet' — pool exhausted.
2026-07-10T08:10:50.895Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'baltbet'. Terminating pod with exit code 1.
2026-07-10T08:10:52.639Z  INFO 1 --- [igaming-source-baltbet] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-07-10T08:10:52.643Z  INFO 1 --- [igaming-source-baltbet] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
2026-07-10T08:11:22.652Z  INFO 1 --- [igaming-source-baltbet] [ionShutdownHook] o.s.c.support.DefaultLifecycleProcessor  : Shutdown phase 1073741823 ends with 1 bean still running after timeout of 30000ms: [taskScheduler]
2026-07-10T08:11:22.691Z  INFO 1 --- [igaming-source-baltbet] [ionShutdownHook] p.d.i.s.core.browser.BrowserService      : Shutting down Universal Browser Service
2026-07-10T08:11:22.900Z  INFO 1 --- [igaming-source-baltbet] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:11:22.923Z  INFO 1 --- [igaming-source-baltbet] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-10T08:11:22.931Z  INFO 1 --- [igaming-source-baltbet] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

```
#### 📥 Loader (`igaming-source-baltbet-loader-96dc8994-rz7cv` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:58:27.689Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'baltbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/baltbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:01:27.706Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'baltbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/baltbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:04:27.716Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'baltbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/baltbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:27.727Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'baltbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/baltbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:27.737Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'baltbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/baltbet/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:11:24.501Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:11:24.501Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:11:24.510Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
```

---

### 🗂️ Букмекер: `betboom`

#### 🕷️ Crawler (`igaming-source-betboom-crawler-6cc56bf66c-xnbg6` - Running)
**Анализ ошибок:**
```text
2026-07-10T08:02:05.833Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:02:05.845Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:03:06.342Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:03:06.342Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:03:06.353Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:03:18.307Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-07-10T08:03:18.307Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-07-10T08:03:18.307Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-07-10T08:03:18.606Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/live: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://betboom.ru/sport/live
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://betboom.ru/sport/live
    at FrameSession._navigate (/tmp/playwright-java-11680021087844923902/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-11680021087844923902/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://betboom.ru/sport/live", waiting until "load"

2026-07-10T08:04:06.835Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:04:06.836Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:04:06.847Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:04:15.445Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betboom': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betboom/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:05:07.358Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:05:07.358Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:05:07.367Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:05:42.607Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-07-10T08:05:42.607Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-07-10T08:05:42.607Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-07-10T08:05:42.900Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/live: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://betboom.ru/sport/live
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://betboom.ru/sport/live
    at FrameSession._navigate (/tmp/playwright-java-11680021087844923902/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-11680021087844923902/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://betboom.ru/sport/live", waiting until "load"

2026-07-10T08:06:07.856Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:06:07.856Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:06:07.871Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:07:08.375Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:07:08.375Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:07:08.386Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:07:15.461Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betboom': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betboom/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:08:06.901Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-07-10T08:08:06.901Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-07-10T08:08:06.902Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-07-10T08:08:07.191Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/live: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://betboom.ru/sport/live
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://betboom.ru/sport/live
    at FrameSession._navigate (/tmp/playwright-java-11680021087844923902/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-11680021087844923902/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://betboom.ru/sport/live", waiting until "load"

2026-07-10T08:08:08.873Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:08:08.873Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:08:08.886Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:09:09.526Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:09:09.527Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:09:09.536Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:10:10.039Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:10:10.039Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:10:10.049Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:10:15.472Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betboom': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betboom/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:26.388Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching prematch data from betboom.ru...
2026-07-10T08:10:26.388Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Fetching prematch events via Playwright browser...
2026-07-10T08:10:26.388Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/prematch...
2026-07-10T08:10:26.793Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/prematch: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://betboom.ru/sport/prematch
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://betboom.ru/sport/prematch
    at FrameSession._navigate (/tmp/playwright-java-11680021087844923902/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-11680021087844923902/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://betboom.ru/sport/prematch", waiting until "load"

2026-07-10T08:10:31.192Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-07-10T08:10:31.193Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-07-10T08:10:31.193Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-07-10T08:10:31.613Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/live: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://betboom.ru/sport/live
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://betboom.ru/sport/live
    at FrameSession._navigate (/tmp/playwright-java-11680021087844923902/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-11680021087844923902/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://betboom.ru/sport/live", waiting until "load"

2026-07-10T08:11:10.547Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:11:10.547Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:11:10.557Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-10T08:12:11.045Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:12:11.045Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:12:11.056Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.

```
#### 📥 Loader (`igaming-source-betboom-loader-887bcf8f6-dr5r9` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:58:52.426Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betboom': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betboom/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:01:52.438Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betboom': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betboom/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:04:52.452Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betboom': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betboom/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:52.462Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betboom': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betboom/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:52.478Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betboom': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betboom/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:11:48.137Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : [Scheduled] Direct connection failed and no proxy configured. Requesting from pool...
2026-07-10T08:11:48.137Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-10T08:11:48.147Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
```

---

### 🗂️ Букмекер: `betcity`

#### 🕷️ Crawler (`igaming-source-betcity-crawler-6d967f4669-fp4gg` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-10T08:07:06.801Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.source.betcity.BetCityApplication  : Starting BetCityApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-10T08:07:06.805Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.source.betcity.BetCityApplication  : The following 1 profile is active: "dev"
2026-07-10T08:07:10.291Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:07:10.291Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-10T08:07:10.899Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 589 ms. Found 7 JPA repository interfaces.
2026-07-10T08:07:12.320Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:07:12.321Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-10T08:07:12.405Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 12 ms. Found 0 Redis repository interfaces.
2026-07-10T08:07:13.394Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=c30f536b-6d40-34ba-b858-57cf86e30ae6
2026-07-10T08:07:16.104Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3041 (http)
2026-07-10T08:07:16.124Z  INFO 1 --- [igaming-source-betcity] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-07-10T08:07:16.124Z  INFO 1 --- [igaming-source-betcity] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-07-10T08:07:16.424Z  INFO 1 --- [igaming-source-betcity] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-07-10T08:07:16.483Z  INFO 1 --- [igaming-source-betcity] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 9421 ms
2026-07-10T08:07:18.729Z  INFO 1 --- [igaming-source-betcity] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-10T08:07:18.990Z  INFO 1 --- [igaming-source-betcity] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-10T08:07:19.108Z  INFO 1 --- [igaming-source-betcity] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-10T08:07:20.195Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-10T08:07:20.319Z  INFO 1 --- [igaming-source-betcity] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-10T08:07:20.400Z  INFO 1 --- [igaming-source-betcity] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-10T08:07:21.386Z  INFO 1 --- [igaming-source-betcity] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-10T08:07:25.710Z  INFO 1 --- [igaming-source-betcity] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-10T08:07:25.987Z  INFO 1 --- [igaming-source-betcity] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:07:26.101Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-10T08:07:27.107Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'ad.betcity.ru' (no proxy)...
2026-07-10T08:07:28.032Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ [Startup] Direct connection to 'ad.betcity.ru' works — no proxy needed.
2026-07-10T08:07:28.033Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.core.service.VpnManagerService   : Proxy system properties cleared — using direct connection.
2026-07-10T08:07:28.066Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-10T08:07:29.310Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-10T08:07:34.521Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-10T08:07:34.523Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-10T08:07:34.526Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-10T08:07:34.528Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-10T08:07:34.582Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-10T08:07:34.584Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-10T08:07:35.392Z  WARN 1 --- [igaming-source-betcity] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-07-10T08:07:39.325Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-07-10T08:07:39.688Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3041 (http) with context path '/'
2026-07-10T08:07:39.793Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.source.betcity.BetCityApplication  : Started BetCityApplication in 35.667 seconds (process running for 38.006)
2026-07-10T08:07:39.802Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting scheduled PREMATCH fetch job
2026-07-10T08:07:39.802Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.b.service.BetCityApiClient       : Fetching PREMATCH from https://ad.betcity.ru/d/off/events?rev=6
2026-07-10T08:07:39.803Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-07-10T08:07:39.804Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting scheduled LIVE fetch job
2026-07-10T08:07:39.804Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.b.service.BetCityApiClient       : Fetching LIVE from https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1
2026-07-10T08:07:40.392Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'betcity' (display: 'Betcity')
2026-07-10T08:07:40.682Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betcity': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betcity/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:40.683Z  WARN 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betcity': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betcity/heartbeat": igaming-aggregator
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-10T08:07:43.303Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-10T08:07:45.705Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://ad.betcity.ru/d/off/events?rev=6: Object doesn't exist: frame@291bf6cc46d8cc12b5288141f34b5c27
2026-07-10T08:07:45.784Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.b.service.BetCityApiClient       : Failed to fetch data or empty response
2026-07-10T08:07:45.791Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1: Cannot find command to respond: 5
2026-07-10T08:07:45.792Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.b.service.BetCityApiClient       : Failed to fetch data or empty response
2026-07-10T08:10:09.793Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting scheduled LIVE fetch job
2026-07-10T08:10:09.794Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.b.service.BetCityApiClient       : Fetching LIVE from https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1
2026-07-10T08:10:10.282Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1
2026-07-10T08:10:10.717Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1
    at FrameSession._navigate (/tmp/playwright-java-1682266100935903610/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-1682266100935903610/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1", waiting until "domcontentloaded"

2026-07-10T08:10:10.718Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-betcity'
2026-07-10T08:10:10.936Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-betcity' — pool exhausted.
2026-07-10T08:10:10.982Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'betcity'. Terminating pod with exit code 1.
2026-07-10T08:10:13.005Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-07-10T08:10:13.016Z  INFO 1 --- [igaming-source-betcity] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
2026-07-10T08:10:43.028Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] o.s.c.support.DefaultLifecycleProcessor  : Shutdown phase 1073741823 ends with 1 bean still running after timeout of 30000ms: [taskScheduler]
2026-07-10T08:10:43.078Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] p.d.i.s.core.browser.BrowserService      : Shutting down Universal Browser Service
2026-07-10T08:10:43.489Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:10:43.494Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-10T08:10:43.505Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

```
#### 📥 Loader (`igaming-source-betcity-loader-68dcfcf676-8rggz` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:58:18.329Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betcity': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betcity/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:01:18.342Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betcity': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betcity/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:04:18.359Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betcity': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betcity/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:18.381Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betcity': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betcity/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:18.397Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betcity': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betcity/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:04:18.359Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betcity': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betcity/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:18.381Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betcity': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betcity/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:18.397Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betcity': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betcity/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `betm`

#### 🕷️ Crawler (`igaming-source-betm-crawler-7649859cd9-bn89x` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-10T08:07:56.253Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.source.betcity.BetCityApplication  : Starting BetCityApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-10T08:07:56.259Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.source.betcity.BetCityApplication  : The following 1 profile is active: "dev"
2026-07-10T08:07:59.737Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:07:59.737Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-10T08:08:00.249Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 496 ms. Found 7 JPA repository interfaces.
2026-07-10T08:08:01.736Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:08:01.738Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-10T08:08:01.829Z  INFO 1 --- [igaming-source-betcity] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 74 ms. Found 0 Redis repository interfaces.
2026-07-10T08:08:03.040Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=c30f536b-6d40-34ba-b858-57cf86e30ae6
2026-07-10T08:08:06.728Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3048 (http)
2026-07-10T08:08:06.749Z  INFO 1 --- [igaming-source-betcity] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-07-10T08:08:06.749Z  INFO 1 --- [igaming-source-betcity] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-07-10T08:08:07.145Z  INFO 1 --- [igaming-source-betcity] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-07-10T08:08:07.149Z  INFO 1 --- [igaming-source-betcity] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 10607 ms
2026-07-10T08:08:09.967Z  INFO 1 --- [igaming-source-betcity] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-10T08:08:10.329Z  INFO 1 --- [igaming-source-betcity] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-10T08:08:10.479Z  INFO 1 --- [igaming-source-betcity] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-10T08:08:11.972Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-10T08:08:12.147Z  INFO 1 --- [igaming-source-betcity] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-10T08:08:12.176Z  INFO 1 --- [igaming-source-betcity] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-10T08:08:13.586Z  INFO 1 --- [igaming-source-betcity] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-10T08:08:19.646Z  INFO 1 --- [igaming-source-betcity] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-10T08:08:20.079Z  INFO 1 --- [igaming-source-betcity] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:08:20.273Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-10T08:08:21.997Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'ad.betcity.ru' (no proxy)...
2026-07-10T08:08:23.253Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ [Startup] Direct connection to 'ad.betcity.ru' works — no proxy needed.
2026-07-10T08:08:23.254Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.core.service.VpnManagerService   : Proxy system properties cleared — using direct connection.
2026-07-10T08:08:23.298Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-10T08:08:24.884Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-10T08:08:32.201Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-10T08:08:32.222Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-10T08:08:32.232Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-10T08:08:32.238Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-10T08:08:32.240Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-10T08:08:32.245Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-10T08:08:33.500Z  WARN 1 --- [igaming-source-betcity] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-07-10T08:08:41.000Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-07-10T08:08:41.696Z  INFO 1 --- [igaming-source-betcity] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3048 (http) with context path '/'
2026-07-10T08:08:41.879Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.source.betcity.BetCityApplication  : Started BetCityApplication in 48.916 seconds (process running for 51.487)
2026-07-10T08:08:41.911Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting scheduled PREMATCH fetch job
2026-07-10T08:08:41.912Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.b.service.BetCityApiClient       : Fetching PREMATCH from https://ad.betcity.ru/d/off/events?rev=6
2026-07-10T08:08:41.912Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting scheduled LIVE fetch job
2026-07-10T08:08:41.913Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.b.service.BetCityApiClient       : Fetching LIVE from https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1
2026-07-10T08:08:41.913Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-07-10T08:08:42.908Z  INFO 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'betm' (display: 'BetM')
2026-07-10T08:08:43.071Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betm': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betm/heartbeat": igaming-aggregator
2026-07-10T08:08:43.077Z  WARN 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betm': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betm/heartbeat": igaming-aggregator: No address associated with hostname
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-10T08:08:46.773Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-10T08:08:49.988Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1: Object doesn't exist: frame@2f01b6906d7ddf187252e56394d3f58f
2026-07-10T08:08:49.989Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.b.service.BetCityApiClient       : Failed to fetch data or empty response
2026-07-10T08:08:50.361Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://ad.betcity.ru/d/off/events?rev=6: Error {
  message='getaddrinfo ENOTFOUND proxy-vpn-pool.service-proxy.svc.cluster.local
  name='Error
  stack='Error: getaddrinfo ENOTFOUND proxy-vpn-pool.service-proxy.svc.cluster.local
    at GetAddrInfoReqWrap.onlookupall [as oncomplete] (node:dns:118:26)
}
Call log:
- → POST https://ad.betcity.ru/d/off/events?rev=6
-   user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36
-   accept: application/json, text/plain, */*
-   accept-encoding: gzip,deflate,br
-   Accept-Language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7
-   Content-Type: application/x-www-form-urlencoded
-   content-length: 0

2026-07-10T08:08:50.363Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-betcity'
2026-07-10T08:08:50.639Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-betcity' — pool exhausted.
2026-07-10T08:08:50.639Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'betm'. Terminating pod with exit code 1.
2026-07-10T08:08:52.670Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-07-10T08:08:52.700Z  INFO 1 --- [igaming-source-betcity] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
2026-07-10T08:09:22.733Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] o.s.c.support.DefaultLifecycleProcessor  : Shutdown phase 1073741823 ends with 1 bean still running after timeout of 30000ms: [taskScheduler]
2026-07-10T08:09:22.789Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] p.d.i.s.core.browser.BrowserService      : Shutting down Universal Browser Service
2026-07-10T08:09:23.179Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:09:23.198Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-10T08:09:23.205Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

```
#### 📥 Loader (`igaming-source-betm-loader-7f88fb8847-lw9nd` - Failed)
**Анализ ошибок:**
```text
2026-07-09T19:02:54.920Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-07-09T19:02:54.940Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-07-09T19:02:54.941Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-07-09T19:03:27.666Z  WARN 1 --- [igaming-source-betcity] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betm': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betm/heartbeat": igaming-aggregator
2026-07-09T19:03:27.666Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betm': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betm/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-09T19:03:51.453Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-09T19:03:51.465Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-09T19:03:51.485Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
```

---

### 🗂️ Букмекер: `bettery`

#### 🕷️ Crawler (`igaming-source-bettery-crawler-575c778858-758n4` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-09T22:54:29.413Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.source.fonbet.FonbetApplication    : Starting FonbetApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-09T22:54:29.430Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.source.fonbet.FonbetApplication    : The following 1 profile is active: "dev"
2026-07-09T22:54:38.021Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-09T22:54:38.022Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-09T22:54:39.744Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 1647 ms. Found 7 JPA repository interfaces.
2026-07-09T22:54:44.392Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-09T22:54:44.399Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-09T22:54:44.601Z  INFO 1 --- [igaming-source-bettery] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 88 ms. Found 0 Redis repository interfaces.
2026-07-09T22:54:48.216Z  INFO 1 --- [igaming-source-bettery] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=74788a06-dcbd-3576-8e9a-9f0c136fad88
2026-07-09T22:54:56.074Z  WARN 1 --- [igaming-source-bettery] [           main] io.undertow.websockets.jsr               : UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used
2026-07-09T22:54:56.311Z  INFO 1 --- [igaming-source-bettery] [           main] io.undertow.servlet                      : Initializing Spring embedded WebApplicationContext
2026-07-09T22:54:56.319Z  INFO 1 --- [igaming-source-bettery] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 26378 ms
2026-07-09T22:55:06.142Z  INFO 1 --- [igaming-source-bettery] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-09T22:55:06.948Z  INFO 1 --- [igaming-source-bettery] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-09T22:55:07.370Z  INFO 1 --- [igaming-source-bettery] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-09T22:55:11.492Z  INFO 1 --- [igaming-source-bettery] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-09T22:55:12.024Z  INFO 1 --- [igaming-source-bettery] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-09T22:55:12.243Z  INFO 1 --- [igaming-source-bettery] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-09T22:55:16.274Z  INFO 1 --- [igaming-source-bettery] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-09T22:55:30.097Z  INFO 1 --- [igaming-source-bettery] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-09T22:55:30.851Z  INFO 1 --- [igaming-source-bettery] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-09T22:55:34.456Z  INFO 1 --- [igaming-source-bettery] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-09T22:55:46.145Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-09T22:55:46.180Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-09T22:55:46.219Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-09T22:55:46.229Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-09T22:55:46.257Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-09T22:55:46.273Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-09T22:55:56.681Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-09T22:55:56.877Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'bettery.ru' (no proxy)...
2026-07-09T22:55:59.427Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ [Startup] Direct connection to 'bettery.ru' works — no proxy needed.
2026-07-09T22:55:59.439Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.service.VpnManagerService   : Proxy system properties cleared — using direct connection.
2026-07-09T22:55:59.523Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-09T22:55:59.578Z  INFO 1 --- [igaming-source-bettery] [           main] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Initializing FonbetApiClient with Universal Browser Service
2026-07-09T22:55:59.592Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.c.e.f.s.JsonSchemaValidator      : Loaded Fonbet schema from /fonbet/urls-schema.json
2026-07-09T22:55:59.673Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.f.scheduler.MatchFetchScheduler  : Warming up Fonbet API client...
2026-07-09T22:55:59.675Z  INFO 1 --- [igaming-source-bettery] [           main] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Fetching dynamic line URL list via full-page navigation: https://bettery.ru/urls.json
2026-07-09T22:55:59.685Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-09T22:56:09.120Z  INFO 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-09T22:56:14.684Z  WARN 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://bettery.ru/urls.json
2026-07-09T22:56:16.305Z ERROR 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://bettery.ru/urls.json: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://bettery.ru/urls.json
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://bettery.ru/urls.json
    at FrameSession._navigate (/tmp/playwright-java-17864615335552179404/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-17864615335552179404/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://bettery.ru/urls.json", waiting until "domcontentloaded"

2026-07-09T22:56:16.336Z  WARN 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-bettery'
2026-07-09T22:56:17.746Z ERROR 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-bettery' — pool exhausted.
2026-07-09T22:56:17.759Z ERROR 1 --- [igaming-source-bettery] [           main] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'bettery'. Terminating pod with exit code 1.

```
#### 📥 Loader (`igaming-source-bettery-loader-86cf9447d5-jsptt` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:59:24.914Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'bettery': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/bettery/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:02:24.938Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'bettery': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/bettery/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:05:24.951Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'bettery': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/bettery/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:08:24.988Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'bettery': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/bettery/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:11:25.012Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'bettery': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/bettery/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:05:24.951Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'bettery': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/bettery/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:08:24.988Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'bettery': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/bettery/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:11:25.012Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'bettery': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/bettery/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `betwinner`

#### 🕷️ Crawler (`igaming-source-betwinner-crawler-75cf86bf54-zf6zp` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-10T08:09:33.194Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.source.betb2b.Betb2bApplication    : Starting Betb2bApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-10T08:09:33.198Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.source.betb2b.Betb2bApplication    : The following 1 profile is active: "league-crawler"
2026-07-10T08:09:36.687Z  INFO 1 --- [igaming-source-betwinner] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:09:36.688Z  INFO 1 --- [igaming-source-betwinner] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-10T08:09:37.214Z  INFO 1 --- [igaming-source-betwinner] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 503 ms. Found 7 JPA repository interfaces.
2026-07-10T08:09:38.807Z  INFO 1 --- [igaming-source-betwinner] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:09:38.809Z  INFO 1 --- [igaming-source-betwinner] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-10T08:09:38.899Z  INFO 1 --- [igaming-source-betwinner] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 16 ms. Found 0 Redis repository interfaces.
2026-07-10T08:09:39.883Z  INFO 1 --- [igaming-source-betwinner] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=dff05cfc-8476-3d3e-944a-a76586f4c35e
2026-07-10T08:09:42.706Z  INFO 1 --- [igaming-source-betwinner] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3057 (http)
2026-07-10T08:09:42.727Z  INFO 1 --- [igaming-source-betwinner] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-07-10T08:09:42.727Z  INFO 1 --- [igaming-source-betwinner] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-07-10T08:09:43.106Z  INFO 1 --- [igaming-source-betwinner] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-07-10T08:09:43.108Z  INFO 1 --- [igaming-source-betwinner] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 9714 ms
2026-07-10T08:09:45.406Z  INFO 1 --- [igaming-source-betwinner] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-10T08:09:45.628Z  INFO 1 --- [igaming-source-betwinner] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-10T08:09:45.788Z  INFO 1 --- [igaming-source-betwinner] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-10T08:09:46.898Z  INFO 1 --- [igaming-source-betwinner] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-10T08:09:47.007Z  INFO 1 --- [igaming-source-betwinner] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-10T08:09:47.031Z  INFO 1 --- [igaming-source-betwinner] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-10T08:09:48.019Z  WARN 1 --- [igaming-source-betwinner] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-07-10T08:09:48.191Z  INFO 1 --- [igaming-source-betwinner] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-10T08:09:53.235Z  INFO 1 --- [igaming-source-betwinner] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-10T08:09:53.484Z  INFO 1 --- [igaming-source-betwinner] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:09:54.797Z  INFO 1 --- [igaming-source-betwinner] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-10T08:10:00.102Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-10T08:10:00.185Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'betwinner.com' (no proxy)...
2026-07-10T08:10:01.565Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ [Startup] Direct connection to 'betwinner.com' works — no proxy needed.
2026-07-10T08:10:01.565Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.core.service.VpnManagerService   : Proxy system properties cleared — using direct connection.
2026-07-10T08:10:01.588Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-10T08:10:01.920Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-10T08:10:01.922Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-10T08:10:01.924Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-10T08:10:01.926Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-10T08:10:01.928Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-10T08:10:01.983Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-10T08:10:02.988Z  WARN 1 --- [igaming-source-betwinner] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-07-10T08:10:06.996Z  INFO 1 --- [igaming-source-betwinner] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-07-10T08:10:07.314Z  INFO 1 --- [igaming-source-betwinner] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3057 (http) with context path '/'
2026-07-10T08:10:07.407Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.source.betb2b.Betb2bApplication    : Started Betb2bApplication in 36.521 seconds (process running for 38.62)
2026-07-10T08:10:07.486Z  INFO 1 --- [igaming-source-betwinner] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting periodic event discovery for Betb2b Family...
2026-07-10T08:10:07.486Z  INFO 1 --- [igaming-source-betwinner] [ loader-sched-2] .d.i.s.c.e.x.s.AbstractXbetFamilyService : Starting Xbet Family event discovery for bookmaker: betwinner...
2026-07-10T08:10:07.487Z  INFO 1 --- [igaming-source-betwinner] [ loader-sched-2] .s.c.e.x.s.AbstractBetB2BFamilyApiClient : Rewrote URL from https://betwinner.com/LiveFeed/Get1xMatchByLeague to https://betwinner.com/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&lng=en&partner=120 (useServiceApi=true)
2026-07-10T08:10:07.488Z  INFO 1 --- [igaming-source-betwinner] [ loader-sched-2] .s.c.e.x.s.AbstractBetB2BFamilyApiClient : Fetching LIVE from service-api URL: https://betwinner.com/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&lng=en&partner=120
2026-07-10T08:10:07.491Z  INFO 1 --- [igaming-source-betwinner] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-07-10T08:10:07.993Z  INFO 1 --- [igaming-source-betwinner] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'betwinner' (display: 'BetWinner')
2026-07-10T08:10:08.382Z  WARN 1 --- [igaming-source-betwinner] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betwinner': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betwinner/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:08.382Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betwinner': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betwinner/heartbeat": igaming-aggregator
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-10T08:10:11.397Z  INFO 1 --- [igaming-source-betwinner] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-10T08:10:13.385Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://betwinner.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=120&virtualSports=true
2026-07-10T08:10:13.785Z ERROR 1 --- [igaming-source-betwinner] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://betwinner.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=120&virtualSports=true: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://betwinner.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=120&virtualSports=true
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://betwinner.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=120&virtualSports=true
    at FrameSession._navigate (/tmp/playwright-java-2438233976810550661/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-2438233976810550661/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://betwinner.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=120&virtualSports=true", waiting until "domcontentloaded"

2026-07-10T08:10:13.786Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-betwinner'
2026-07-10T08:10:13.931Z ERROR 1 --- [igaming-source-betwinner] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-betwinner' — pool exhausted.
2026-07-10T08:10:13.931Z ERROR 1 --- [igaming-source-betwinner] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'betwinner'. Terminating pod with exit code 1.
2026-07-10T08:10:15.989Z  INFO 1 --- [igaming-source-betwinner] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-07-10T08:10:15.992Z  INFO 1 --- [igaming-source-betwinner] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
2026-07-10T08:10:46.003Z  INFO 1 --- [igaming-source-betwinner] [ionShutdownHook] o.s.c.support.DefaultLifecycleProcessor  : Shutdown phase 1073741823 ends with 1 bean still running after timeout of 30000ms: [taskScheduler]
2026-07-10T08:10:46.033Z  INFO 1 --- [igaming-source-betwinner] [ionShutdownHook] p.d.i.s.core.browser.BrowserService      : Shutting down Universal Browser Service
2026-07-10T08:10:46.406Z  INFO 1 --- [igaming-source-betwinner] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:10:46.410Z  INFO 1 --- [igaming-source-betwinner] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-10T08:10:46.421Z  INFO 1 --- [igaming-source-betwinner] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

```
#### 📥 Loader (`igaming-source-betwinner-loader-85788f85b8-z92q4` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:58:05.962Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betwinner': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betwinner/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:01:05.971Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betwinner': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betwinner/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:04:05.981Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betwinner': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betwinner/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:05.989Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betwinner': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betwinner/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:05.996Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betwinner': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betwinner/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:04:05.981Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betwinner': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betwinner/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:05.989Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betwinner': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betwinner/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:05.996Z  WARN 1 --- [igaming-source-betwinner] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betwinner': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betwinner/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `dafabet`

#### 🕷️ Crawler (`igaming-source-dafabet-crawler-787db4976b-vtbqm` - Failed)
**Анализ ошибок:**
```text
ERROR: Error from server (BadRequest): container "igaming-source-dafabet-crawler" in pod "igaming-source-dafabet-crawler-787db4976b-vtbqm" is terminated
```

---

### 🗂️ Букмекер: `digitain`

#### 🕷️ Crawler (`igaming-source-digitain-crawler-6fc99949d8-2jmbz` - Failed)
**Анализ ошибок:**
```text
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-09T19:02:11.289Z  INFO 1 --- [igaming-source-digitain] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-09T19:02:11.462Z  INFO 1 --- [igaming-source-digitain] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-09T19:02:12.615Z  INFO 1 --- [igaming-source-digitain] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'melbet.ru' (no proxy)...
2026-07-09T19:02:14.006Z  INFO 1 --- [igaming-source-digitain] [           main] p.d.i.s.core.service.VpnManagerService   : ❌ [Startup] Direct connection to 'melbet.ru' failed — requesting proxy from pool...
2026-07-09T19:02:14.008Z  INFO 1 --- [igaming-source-digitain] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-07-09T19:02:14.514Z  WARN 1 --- [igaming-source-digitain] [           main] p.d.i.s.core.service.VpnManagerService   : VPN pool API unavailable. No proxy config retrieved.
2026-07-09T19:02:15.278Z  WARN 1 --- [igaming-source-digitain] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'digitainScraperScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/classes/!/pro/datawiki/igaming/source/digitain/scheduler/DigitainScraperScheduler.class]: Unsatisfied dependency expressed through constructor parameter 1: Error creating bean with name 'digitainApiClient' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/classes/!/pro/datawiki/igaming/source/digitain/service/DigitainApiClient.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'browserExecutor': Injection of autowired dependencies failed
2026-07-09T19:02:15.279Z  INFO 1 --- [igaming-source-digitain] [           main] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-09T19:02:15.299Z  INFO 1 --- [igaming-source-digitain] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-09T19:02:15.312Z  INFO 1 --- [igaming-source-digitain] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-07-09T19:02:15.320Z  INFO 1 --- [igaming-source-digitain] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2026-07-09T19:02:15.425Z  INFO 1 --- [igaming-source-digitain] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-07-09T19:02:15.509Z ERROR 1 --- [igaming-source-digitain] [           main] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'digitainScraperScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/classes/!/pro/datawiki/igaming/source/digitain/scheduler/DigitainScraperScheduler.class]: Unsatisfied dependency expressed through constructor parameter 1: Error creating bean with name 'digitainApiClient' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/classes/!/pro/datawiki/igaming/source/digitain/service/DigitainApiClient.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'browserExecutor': Injection of autowired dependencies failed
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:804) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:240) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1377) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1214) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:563) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:523) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:336) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:289) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:334) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:199) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.instantiateSingleton(DefaultListableBeanFactory.java:1122) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingleton(DefaultListableBeanFactory.java:1093) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:1030) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:987) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:627) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:752) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:439) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:318) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1361) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1350) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at pro.datawiki.igaming.source.digitain.DigitainApplication.main(DigitainApplication.java:15) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:102) ~[app.jar:0.0.1-SNAPSHOT]
	at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:64) ~[app.jar:0.0.1-SNAPSHOT]
	at org.springframework.boot.loader.launch.JarLauncher.main(JarLauncher.java:40) ~[app.jar:0.0.1-SNAPSHOT]
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'digitainApiClient' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/classes/!/pro/datawiki/igaming/source/digitain/service/DigitainApiClient.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'browserExecutor': Injection of autowired dependencies failed
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:804) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:240) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1377) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1214) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:563) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:523) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:336) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:289) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:334) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:199) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:254) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1631) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1519) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:913) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791) ~[spring-beans-6.2.1.jar!/:6.2.1]
	... 26 common frames omitted
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'browserExecutor': Injection of autowired dependencies failed
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.postProcessProperties(AutowiredAnnotationBeanPostProcessor.java:515) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1441) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:600) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:523) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:336) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:289) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:334) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:199) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1573) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1519) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:913) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791) ~[spring-beans-6.2.1.jar!/:6.2.1]
	... 40 common frames omitted
Caused by: org.springframework.util.PlaceholderResolutionException: Could not resolve placeholder 'app.browser.pre-visit-keywords' in value "${app.browser.pre-visit-keywords}"
	at org.springframework.util.PlaceholderResolutionException.withValue(PlaceholderResolutionException.java:81) ~[spring-core-6.2.1.jar!/:6.2.1]
	at org.springframework.util.PlaceholderParser$ParsedValue.resolve(PlaceholderParser.java:416) ~[spring-core-6.2.1.jar!/:6.2.1]
	at org.springframework.util.PlaceholderParser.replacePlaceholders(PlaceholderParser.java:128) ~[spring-core-6.2.1.jar!/:6.2.1]
	at org.springframework.util.PropertyPlaceholderHelper.parseStringValue(PropertyPlaceholderHelper.java:118) ~[spring-core-6.2.1.jar!/:6.2.1]
	at org.springframework.util.PropertyPlaceholderHelper.replacePlaceholders(PropertyPlaceholderHelper.java:114) ~[spring-core-6.2.1.jar!/:6.2.1]
	at org.springframework.core.env.AbstractPropertyResolver.doResolvePlaceholders(AbstractPropertyResolver.java:255) ~[spring-core-6.2.1.jar!/:6.2.1]
	at org.springframework.core.env.AbstractPropertyResolver.resolveRequiredPlaceholders(AbstractPropertyResolver.java:226) ~[spring-core-6.2.1.jar!/:6.2.1]
	at org.springframework.context.support.PropertySourcesPlaceholderConfigurer.lambda$processProperties$0(PropertySourcesPlaceholderConfigurer.java:201) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.resolveEmbeddedValue(AbstractBeanFactory.java:963) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1541) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1519) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:785) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.inject(AutowiredAnnotationBeanPostProcessor.java:768) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.annotation.InjectionMetadata.inject(InjectionMetadata.java:146) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.postProcessProperties(AutowiredAnnotationBeanPostProcessor.java:509) ~[spring-beans-6.2.1.jar!/:6.2.1]
	... 51 common frames omitted


```

---

### 🗂️ Букмекер: `fanduel`

#### 🕷️ Crawler (`igaming-source-fanduel-crawler-745b4fd6b6-f4blv` - Failed)
**Анализ ошибок:**
```text
2026-07-09T19:03:19.092Z  WARN 1 --- [igaming-source-fanduel] [           main] o.h.e.j.e.i.JdbcEnvironmentInitiator     : HHH000342: Could not obtain connection to query metadata

org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [HikariPool-1 - Connection is not available, request timed out after 30002ms (total=0, active=0, idle=0, waiting=0)] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:94) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcIsolationDelegate.delegateWork(JdbcIsolationDelegate.java:116) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator.getJdbcEnvironmentUsingJdbcMetadata(JdbcEnvironmentInitiator.java:320) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator.initiateService(JdbcEnvironmentInitiator.java:129) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator.initiateService(JdbcEnvironmentInitiator.java:81) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.boot.registry.internal.StandardServiceRegistryImpl.initiateService(StandardServiceRegistryImpl.java:130) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.service.internal.AbstractServiceRegistryImpl.createService(AbstractServiceRegistryImpl.java:263) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.service.internal.AbstractServiceRegistryImpl.initializeService(AbstractServiceRegistryImpl.java:238) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.service.internal.AbstractServiceRegistryImpl.getService(AbstractServiceRegistryImpl.java:215) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.boot.model.relational.Database.<init>(Database.java:45) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.boot.internal.InFlightMetadataCollectorImpl.getDatabase(InFlightMetadataCollectorImpl.java:226) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.boot.internal.InFlightMetadataCollectorImpl.<init>(InFlightMetadataCollectorImpl.java:194) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.boot.model.process.spi.MetadataBuildingProcess.complete(MetadataBuildingProcess.java:171) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.metadata(EntityManagerFactoryBuilderImpl.java:1431) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.build(EntityManagerFactoryBuilderImpl.java:1502) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.springframework.orm.jpa.vendor.SpringHibernateJpaPersistenceProvider.createContainerEntityManagerFactory(SpringHibernateJpaPersistenceProvider.java:66) ~[spring-orm-6.2.1.jar!/:6.2.1]
	at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.createNativeEntityManagerFactory(LocalContainerEntityManagerFactoryBean.java:390) ~[spring-orm-6.2.1.jar!/:6.2.1]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.buildNativeEntityManagerFactory(AbstractEntityManagerFactoryBean.java:419) ~[spring-orm-6.2.1.jar!/:6.2.1]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.afterPropertiesSet(AbstractEntityManagerFactoryBean.java:400) ~[spring-orm-6.2.1.jar!/:6.2.1]
	at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.afterPropertiesSet(LocalContainerEntityManagerFactoryBean.java:366) ~[spring-orm-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1855) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1804) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:601) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:523) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:336) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:289) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:334) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:204) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:970) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:627) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:752) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:439) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:318) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1361) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1350) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at pro.datawiki.igaming.source.fanduel.FanDuelApplication.main(FanDuelApplication.java:16) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:102) ~[app.jar:0.0.1-SNAPSHOT]
	at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:64) ~[app.jar:0.0.1-SNAPSHOT]
	at org.springframework.boot.loader.launch.JarLauncher.main(JarLauncher.java:40) ~[app.jar:0.0.1-SNAPSHOT]
Caused by: java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30002ms (total=0, active=0, idle=0, waiting=0)
	at com.zaxxer.hikari.pool.HikariPool.createTimeoutException(HikariPool.java:686) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:179) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:144) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.HikariDataSource.getConnection(HikariDataSource.java:127) ~[HikariCP-5.1.0.jar!/:na]
	at org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl.getConnection(DatasourceConnectionProviderImpl.java:126) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess.obtainConnection(JdbcEnvironmentInitiator.java:467) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcIsolationDelegate.delegateWork(JdbcIsolationDelegate.java:61) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	... 40 common frames omitted
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
	at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:364) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.core.ConnectionFactory.openConnection(ConnectionFactory.java:54) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.jdbc.PgConnection.<init>(PgConnection.java:273) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.Driver.makeConnection(Driver.java:446) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.Driver.connect(Driver.java:298) ~[postgresql-42.7.4.jar!/:42.7.4]
	at com.zaxxer.hikari.util.DriverDataSource.getConnection(DriverDataSource.java:137) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.PoolBase.newConnection(PoolBase.java:360) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.PoolBase.newPoolEntry(PoolBase.java:202) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.HikariPool.createPoolEntry(HikariPool.java:461) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.HikariPool$PoolEntryCreator.call(HikariPool.java:724) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.HikariPool$PoolEntryCreator.call(HikariPool.java:703) ~[HikariCP-5.1.0.jar!/:na]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: java.net.UnknownHostException: igaming-source-fanduel-db.igaming-dev.svc.cluster.local
	at java.base/sun.nio.ch.NioSocketImpl.connect(Unknown Source) ~[na:na]
	at java.base/java.net.SocksSocketImpl.connect(Unknown Source) ~[na:na]
	at java.base/java.net.Socket.connect(Unknown Source) ~[na:na]
	at org.postgresql.core.PGStream.createSocket(PGStream.java:260) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.core.PGStream.<init>(PGStream.java:121) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.core.v3.ConnectionFactoryImpl.tryConnect(ConnectionFactoryImpl.java:140) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:268) ~[postgresql-42.7.4.jar!/:42.7.4]
	... 14 common frames omitted

2026-07-09T19:03:19.211Z  WARN 1 --- [igaming-source-fanduel] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-07-09T19:03:19.252Z  INFO 1 --- [igaming-source-fanduel] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 12.0
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-09T19:03:23.295Z  INFO 1 --- [igaming-source-fanduel] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-09T19:03:53.340Z  WARN 1 --- [igaming-source-fanduel] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-07-09T19:03:53.340Z ERROR 1 --- [igaming-source-fanduel] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : HikariPool-1 - Connection is not available, request timed out after 30000ms (total=0, active=0, idle=0, waiting=0)
2026-07-09T19:03:53.340Z ERROR 1 --- [igaming-source-fanduel] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : The connection attempt failed.
2026-07-09T19:03:53.356Z ERROR 1 --- [igaming-source-fanduel] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [HikariPool-1 - Connection is not available, request timed out after 30000ms (total=0, active=0, idle=0, waiting=0)] [n/a]
2026-07-09T19:03:53.360Z  WARN 1 --- [igaming-source-fanduel] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [HikariPool-1 - Connection is not available, request timed out after 30000ms (total=0, active=0, idle=0, waiting=0)] [n/a]
2026-07-09T19:03:53.361Z  INFO 1 --- [igaming-source-fanduel] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-09T19:03:54.582Z  INFO 1 --- [igaming-source-fanduel] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

```

---

### 🗂️ Букмекер: `fon-bet-kz`

#### 🕷️ Crawler (`igaming-source-fon-bet-kz-crawler-78999ccdd7-qsb9t` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-10T08:10:27.319Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.source.fonbet.FonbetApplication    : Starting FonbetApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-10T08:10:27.322Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.source.fonbet.FonbetApplication    : The following 1 profile is active: "dev"
2026-07-10T08:10:30.991Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:10:30.991Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-10T08:10:31.528Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 519 ms. Found 7 JPA repository interfaces.
2026-07-10T08:10:33.008Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:10:33.010Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-10T08:10:33.091Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 66 ms. Found 0 Redis repository interfaces.
2026-07-10T08:10:34.085Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=74788a06-dcbd-3576-8e9a-9f0c136fad88
2026-07-10T08:10:36.612Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] io.undertow.websockets.jsr               : UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used
2026-07-10T08:10:36.696Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] io.undertow.servlet                      : Initializing Spring embedded WebApplicationContext
2026-07-10T08:10:36.697Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 9175 ms
2026-07-10T08:10:39.509Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-10T08:10:39.701Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-10T08:10:39.801Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-10T08:10:40.789Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-10T08:10:40.899Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-10T08:10:40.920Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-10T08:10:41.993Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-10T08:10:46.196Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-10T08:10:46.603Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:10:47.822Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-10T08:10:51.335Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-10T08:10:51.337Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-10T08:10:51.383Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-10T08:10:51.385Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-10T08:10:51.387Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-10T08:10:51.390Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-10T08:10:54.203Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-10T08:10:54.225Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'fonbet.kz' (no proxy)...
2026-07-10T08:10:55.363Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ [Startup] Direct connection to 'fonbet.kz' works — no proxy needed.
2026-07-10T08:10:55.363Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.service.VpnManagerService   : Proxy system properties cleared — using direct connection.
2026-07-10T08:10:55.392Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-10T08:10:55.400Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Initializing FonbetApiClient with Universal Browser Service
2026-07-10T08:10:55.402Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.c.e.f.s.JsonSchemaValidator      : Loaded Fonbet schema from /fonbet/urls-schema.json
2026-07-10T08:10:55.413Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.f.scheduler.MatchFetchScheduler  : Warming up Fonbet API client...
2026-07-10T08:10:55.413Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Fetching dynamic line URL list via full-page navigation: https://fonbet.kz/urls.json
2026-07-10T08:10:55.414Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-10T08:10:57.802Z  INFO 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-10T08:10:59.783Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://fonbet.kz/urls.json
2026-07-10T08:11:00.186Z ERROR 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://fonbet.kz/urls.json: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://fonbet.kz/urls.json
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://fonbet.kz/urls.json
    at FrameSession._navigate (/tmp/playwright-java-13312408724935659359/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-13312408724935659359/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://fonbet.kz/urls.json", waiting until "domcontentloaded"

2026-07-10T08:11:00.187Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-fon-bet-kz'
2026-07-10T08:11:00.608Z ERROR 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-fon-bet-kz' — pool exhausted.
2026-07-10T08:11:00.609Z ERROR 1 --- [igaming-source-fon-bet-kz] [           main] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'fon-bet-kz'. Terminating pod with exit code 1.

```
#### 📥 Loader (`igaming-source-fon-bet-kz-loader-79c99485bf-fwvlk` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:58:05.049Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-kz': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-kz/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:01:05.078Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-kz': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-kz/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:04:05.101Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-kz': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-kz/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:05.122Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-kz': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-kz/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:05.148Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-kz': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-kz/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:04:05.101Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-kz': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-kz/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:05.122Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-kz': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-kz/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:05.148Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-kz': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-kz/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `fon-bet-ru`

#### 🕷️ Crawler (`igaming-source-fon-bet-ru-crawler-bccf5f4dc-9jvvh` - Running)
**Анализ ошибок:**
```text

```
#### 📥 Loader (`igaming-source-fon-bet-ru-loader-6d4f785bf-ck45c` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:57:47.876Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-ru': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-ru/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:00:47.913Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-ru': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-ru/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:03:47.934Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-ru': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-ru/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:06:47.954Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-ru': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-ru/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:47.975Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-ru': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-ru/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:03:47.934Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-ru': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-ru/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:06:47.954Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-ru': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-ru/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:47.975Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'fon-bet-ru': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/fon-bet-ru/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `marathonbet`

#### 🕷️ Crawler (`igaming-source-marathonbet-crawler-6f49d56f9c-fz79x` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-10T08:08:16.648Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.m.MarathonbetApplication         : Starting MarathonbetApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-10T08:08:16.655Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.m.MarathonbetApplication         : The following 1 profile is active: "league-crawler"
2026-07-10T08:08:21.924Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:08:21.925Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-10T08:08:22.714Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 765 ms. Found 7 JPA repository interfaces.
2026-07-10T08:08:24.802Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:08:24.806Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-10T08:08:24.920Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 52 ms. Found 0 Redis repository interfaces.
2026-07-10T08:08:26.493Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=9169e222-892c-3096-83b1-1e277aa5dddf
2026-07-10T08:08:30.141Z  WARN 1 --- [igaming-source-marathonbet] [           main] io.undertow.websockets.jsr               : UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used
2026-07-10T08:08:30.234Z  INFO 1 --- [igaming-source-marathonbet] [           main] io.undertow.servlet                      : Initializing Spring embedded WebApplicationContext
2026-07-10T08:08:30.236Z  INFO 1 --- [igaming-source-marathonbet] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 13287 ms
2026-07-10T08:08:35.228Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-10T08:08:35.679Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-10T08:08:35.948Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-10T08:08:37.732Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-10T08:08:37.968Z  INFO 1 --- [igaming-source-marathonbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-10T08:08:38.044Z  INFO 1 --- [igaming-source-marathonbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-10T08:08:39.345Z  WARN 1 --- [igaming-source-marathonbet] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-07-10T08:08:39.575Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-10T08:08:46.747Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-10T08:08:47.230Z  INFO 1 --- [igaming-source-marathonbet] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:08:49.605Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-10T08:08:58.080Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-10T08:08:58.173Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'www.marathonbet.ru' (no proxy)...
2026-07-10T08:08:59.807Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ [Startup] Direct connection to 'www.marathonbet.ru' works — no proxy needed.
2026-07-10T08:08:59.808Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : Proxy system properties cleared — using direct connection.
2026-07-10T08:08:59.855Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-10T08:09:02.207Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-10T08:09:02.215Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-10T08:09:02.244Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-10T08:09:02.249Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-10T08:09:02.253Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-10T08:09:02.258Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-10T08:09:07.548Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-07-10T08:09:07.960Z  INFO 1 --- [igaming-source-marathonbet] [           main] io.undertow                              : starting server: Undertow - 2.3.18.Final
2026-07-10T08:09:08.036Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.xnio                                 : XNIO version 3.8.16.Final
2026-07-10T08:09:08.076Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.xnio.nio                             : XNIO NIO Implementation Version 3.8.16.Final
2026-07-10T08:09:08.548Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.jboss.threads                        : JBoss Threads version 3.5.0.Final
2026-07-10T08:09:08.695Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.b.w.e.undertow.UndertowWebServer     : Undertow started on port 3036 (http) with context path '/'
2026-07-10T08:09:08.856Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.m.MarathonbetApplication         : Started MarathonbetApplication in 55.623 seconds (process running for 58.396)
2026-07-10T08:09:08.868Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'marathonbet' (display: 'Marathonbet')
2026-07-10T08:09:08.890Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.m.service.MarathonbetApiClient   : Fetching prematch menu from: https://www.marathonbet.ru/su/react/event/menu/prematch
2026-07-10T08:09:08.922Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-07-10T08:09:09.833Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet/heartbeat": igaming-aggregator
2026-07-10T08:09:09.838Z  WARN 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet/heartbeat": igaming-aggregator: No address associated with hostname
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-10T08:09:13.467Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-10T08:09:15.301Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/react/event/menu/prematch: Error {
  message='getaddrinfo ENOTFOUND proxy-vpn-pool.service-proxy.svc.cluster.local
  name='Error
  stack='Error: getaddrinfo ENOTFOUND proxy-vpn-pool.service-proxy.svc.cluster.local
    at GetAddrInfoReqWrap.onlookupall [as oncomplete] (node:dns:118:26)
}
Call log:
- → GET https://www.marathonbet.ru/su/react/event/menu/prematch
-   user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.7827.155 Safari/537.36
-   accept: */*
-   accept-encoding: gzip,deflate,br
-   Accept-Language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7

2026-07-10T08:09:15.303Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-marathonbet'
2026-07-10T08:09:15.481Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-marathonbet' — pool exhausted.
2026-07-10T08:09:15.490Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'marathonbet'. Terminating pod with exit code 1.
2026-07-10T08:09:17.499Z  INFO 1 --- [igaming-source-marathonbet] [ionShutdownHook] o.s.b.w.e.undertow.UndertowWebServer     : Commencing graceful shutdown. Waiting for active requests to complete
2026-07-10T08:09:17.501Z  INFO 1 --- [igaming-source-marathonbet] [ionShutdownHook] o.s.b.w.e.undertow.UndertowWebServer     : Graceful shutdown complete
2026-07-10T08:09:17.501Z  INFO 1 --- [igaming-source-marathonbet] [ionShutdownHook] io.undertow                              : stopping server: Undertow - 2.3.18.Final
2026-07-10T08:09:47.547Z  INFO 1 --- [igaming-source-marathonbet] [ionShutdownHook] o.s.c.support.DefaultLifecycleProcessor  : Shutdown phase 1073741823 ends with 1 bean still running after timeout of 30000ms: [taskScheduler]
2026-07-10T08:09:47.575Z  INFO 1 --- [igaming-source-marathonbet] [ionShutdownHook] p.d.i.s.core.browser.BrowserService      : Shutting down Universal Browser Service
2026-07-10T08:09:47.939Z  INFO 1 --- [igaming-source-marathonbet] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:09:47.944Z  INFO 1 --- [igaming-source-marathonbet] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-10T08:09:47.954Z  INFO 1 --- [igaming-source-marathonbet] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

```
#### 📥 Loader (`igaming-source-marathonbet-loader-698577bfb8-8zsh5` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:57:48.862Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:00:48.886Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:03:48.893Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:06:48.898Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:48.908Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:03:48.893Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:06:48.898Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:48.908Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `marathonbet-by`

#### 🕷️ Crawler (`igaming-source-marathonbet-by-crawler-848b8878f7-m5nq8` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```
#### 📥 Loader (`igaming-source-marathonbet-by-loader-54f5bc88c8-qft2z` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:58:34.294Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-by': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-by/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:01:34.317Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-by': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-by/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:04:34.338Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-by': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-by/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:34.358Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-by': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-by/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:34.381Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-by': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-by/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:04:34.338Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-by': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-by/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:34.358Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-by': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-by/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:34.381Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-by': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-by/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `marathonbet-com`

#### 🕷️ Crawler (`igaming-source-marathonbet-com-crawler-7cc8d5b4f-qrz84` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-10T08:07:11.509Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.m.MarathonbetApplication         : Starting MarathonbetApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-10T08:07:11.513Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.m.MarathonbetApplication         : The following 1 profile is active: "league-crawler"
2026-07-10T08:07:15.771Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:07:15.772Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-10T08:07:16.389Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 598 ms. Found 7 JPA repository interfaces.
2026-07-10T08:07:18.185Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:07:18.187Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-10T08:07:18.288Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 81 ms. Found 0 Redis repository interfaces.
2026-07-10T08:07:19.393Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=9169e222-892c-3096-83b1-1e277aa5dddf
2026-07-10T08:07:22.384Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] io.undertow.websockets.jsr               : UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used
2026-07-10T08:07:22.477Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] io.undertow.servlet                      : Initializing Spring embedded WebApplicationContext
2026-07-10T08:07:22.479Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 10684 ms
2026-07-10T08:07:25.497Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-10T08:07:25.783Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-10T08:07:25.905Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-10T08:07:27.002Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-10T08:07:27.102Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-10T08:07:27.178Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-10T08:07:28.173Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-07-10T08:07:28.293Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-10T08:07:32.887Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-10T08:07:33.226Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:07:34.772Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-10T08:07:41.787Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-10T08:07:41.874Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'www.marathonbet.com' (no proxy)...
2026-07-10T08:07:42.942Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ [Startup] Direct connection to 'www.marathonbet.com' works — no proxy needed.
2026-07-10T08:07:42.943Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : Proxy system properties cleared — using direct connection.
2026-07-10T08:07:42.970Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-10T08:07:44.393Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-10T08:07:44.395Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-10T08:07:44.399Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-10T08:07:44.401Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-10T08:07:44.404Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-10T08:07:44.406Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-10T08:07:49.189Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-07-10T08:07:49.575Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] io.undertow                              : starting server: Undertow - 2.3.18.Final
2026-07-10T08:07:49.594Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.xnio                                 : XNIO version 3.8.16.Final
2026-07-10T08:07:49.611Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.xnio.nio                             : XNIO NIO Implementation Version 3.8.16.Final
2026-07-10T08:07:50.010Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] org.jboss.threads                        : JBoss Threads version 3.5.0.Final
2026-07-10T08:07:50.201Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] o.s.b.w.e.undertow.UndertowWebServer     : Undertow started on port 3037 (http) with context path '/'
2026-07-10T08:07:50.368Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.m.MarathonbetApplication         : Started MarathonbetApplication in 41.765 seconds (process running for 44.291)
2026-07-10T08:07:50.371Z  INFO 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'marathonbet-com' (display: 'Marathonbet (COM)')
2026-07-10T08:07:50.466Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.m.service.MarathonbetApiClient   : Fetching prematch menu from: https://www.marathonbet.com/su/react/event/menu/prematch
2026-07-10T08:07:50.474Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-07-10T08:07:51.176Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-com/heartbeat": igaming-aggregator
2026-07-10T08:07:51.179Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-com/heartbeat": igaming-aggregator: No address associated with hostname
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-10T08:07:54.183Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-10T08:07:56.007Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/react/event/menu/prematch: Error {
  message='getaddrinfo ENOTFOUND proxy-vpn-pool.service-proxy.svc.cluster.local
  name='Error
  stack='Error: getaddrinfo ENOTFOUND proxy-vpn-pool.service-proxy.svc.cluster.local
    at GetAddrInfoReqWrap.onlookupall [as oncomplete] (node:dns:118:26)
}
Call log:
- → GET https://www.marathonbet.com/su/react/event/menu/prematch
-   user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.7827.155 Safari/537.36
-   accept: */*
-   accept-encoding: gzip,deflate,br
-   Accept-Language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7

2026-07-10T08:07:56.009Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-marathonbet-com'
2026-07-10T08:07:56.136Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-marathonbet-com' — pool exhausted.
2026-07-10T08:07:56.136Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'marathonbet-com'. Terminating pod with exit code 1.
2026-07-10T08:07:58.144Z  INFO 1 --- [igaming-source-marathonbet-com] [ionShutdownHook] o.s.b.w.e.undertow.UndertowWebServer     : Commencing graceful shutdown. Waiting for active requests to complete
2026-07-10T08:07:58.146Z  INFO 1 --- [igaming-source-marathonbet-com] [ionShutdownHook] o.s.b.w.e.undertow.UndertowWebServer     : Graceful shutdown complete
2026-07-10T08:07:58.146Z  INFO 1 --- [igaming-source-marathonbet-com] [ionShutdownHook] io.undertow                              : stopping server: Undertow - 2.3.18.Final
2026-07-10T08:08:28.162Z  INFO 1 --- [igaming-source-marathonbet-com] [ionShutdownHook] o.s.c.support.DefaultLifecycleProcessor  : Shutdown phase 1073741823 ends with 1 bean still running after timeout of 30000ms: [taskScheduler]
2026-07-10T08:08:28.192Z  INFO 1 --- [igaming-source-marathonbet-com] [ionShutdownHook] p.d.i.s.core.browser.BrowserService      : Shutting down Universal Browser Service
2026-07-10T08:08:28.519Z  INFO 1 --- [igaming-source-marathonbet-com] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:08:28.525Z  INFO 1 --- [igaming-source-marathonbet-com] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-10T08:08:28.570Z  INFO 1 --- [igaming-source-marathonbet-com] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

```
#### 📥 Loader (`igaming-source-marathonbet-com-loader-95748b5dc-cpkgv` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:59:18.394Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:02:18.416Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:05:18.433Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:08:18.462Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:11:18.477Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-com/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:05:18.433Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:08:18.462Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:11:18.477Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'marathonbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/marathonbet-com/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `melbet-com`

#### 🕷️ Crawler (`igaming-source-melbet-com-crawler-8665b959c8-xslk7` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-10T08:08:04.166Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.source.betb2b.Betb2bApplication    : Starting Betb2bApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-10T08:08:04.172Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.source.betb2b.Betb2bApplication    : The following 1 profile is active: "league-crawler"
2026-07-10T08:08:08.299Z  INFO 1 --- [igaming-source-melbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:08:08.300Z  INFO 1 --- [igaming-source-melbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-10T08:08:08.994Z  INFO 1 --- [igaming-source-melbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 618 ms. Found 7 JPA repository interfaces.
2026-07-10T08:08:10.907Z  INFO 1 --- [igaming-source-melbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-10T08:08:10.910Z  INFO 1 --- [igaming-source-melbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-10T08:08:10.999Z  INFO 1 --- [igaming-source-melbet-com] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 17 ms. Found 0 Redis repository interfaces.
2026-07-10T08:08:12.207Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=dff05cfc-8476-3d3e-944a-a76586f4c35e
2026-07-10T08:08:15.471Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3058 (http)
2026-07-10T08:08:15.498Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-07-10T08:08:15.499Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-07-10T08:08:15.883Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-07-10T08:08:15.885Z  INFO 1 --- [igaming-source-melbet-com] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 11496 ms
2026-07-10T08:08:18.812Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-10T08:08:19.106Z  INFO 1 --- [igaming-source-melbet-com] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-10T08:08:19.286Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-10T08:08:20.604Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-10T08:08:20.775Z  INFO 1 --- [igaming-source-melbet-com] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-10T08:08:20.800Z  INFO 1 --- [igaming-source-melbet-com] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-10T08:08:21.785Z  WARN 1 --- [igaming-source-melbet-com] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-07-10T08:08:21.906Z  INFO 1 --- [igaming-source-melbet-com] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-10T08:08:27.180Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-10T08:08:27.490Z  INFO 1 --- [igaming-source-melbet-com] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:08:29.111Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-10T08:08:35.984Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-10T08:08:36.074Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'melbet.com' (no proxy)...
2026-07-10T08:08:37.748Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ [Startup] Direct connection to 'melbet.com' works — no proxy needed.
2026-07-10T08:08:37.749Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.core.service.VpnManagerService   : Proxy system properties cleared — using direct connection.
2026-07-10T08:08:37.790Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-10T08:08:38.270Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-10T08:08:38.274Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-10T08:08:38.292Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-10T08:08:38.298Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-10T08:08:38.301Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-10T08:08:38.303Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-10T08:08:39.793Z  WARN 1 --- [igaming-source-melbet-com] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-07-10T08:08:44.869Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-07-10T08:08:45.210Z  INFO 1 --- [igaming-source-melbet-com] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3058 (http) with context path '/'
2026-07-10T08:08:45.378Z  INFO 1 --- [igaming-source-melbet-com] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting periodic event discovery for Betb2b Family...
2026-07-10T08:08:45.378Z  INFO 1 --- [igaming-source-melbet-com] [ loader-sched-2] .d.i.s.c.e.x.s.AbstractXbetFamilyService : Starting Xbet Family event discovery for bookmaker: melbet-com...
2026-07-10T08:08:45.379Z  INFO 1 --- [igaming-source-melbet-com] [ loader-sched-2] .s.c.e.x.s.AbstractBetB2BFamilyApiClient : Rewrote URL from https://melbet.com/LiveFeed/Get1xMatchByLeague to https://melbet.com/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&lng=en&partner=110 (useServiceApi=true)
2026-07-10T08:08:45.379Z  INFO 1 --- [igaming-source-melbet-com] [ loader-sched-2] .s.c.e.x.s.AbstractBetB2BFamilyApiClient : Fetching LIVE from service-api URL: https://melbet.com/service-api/LiveFeed/Get1x2_VZip?virtualSports=true&lng=en&partner=110
2026-07-10T08:08:45.380Z  INFO 1 --- [igaming-source-melbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-07-10T08:08:45.385Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.source.betb2b.Betb2bApplication    : Started Betb2bApplication in 44.105 seconds (process running for 46.298)
2026-07-10T08:08:45.973Z  INFO 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'melbet-com' (display: 'Melbet (COM)')
2026-07-10T08:08:46.366Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'melbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/melbet-com/heartbeat": igaming-aggregator
2026-07-10T08:08:46.368Z  WARN 1 --- [igaming-source-melbet-com] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'melbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/melbet-com/heartbeat": igaming-aggregator: No address associated with hostname
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-10T08:08:49.706Z  INFO 1 --- [igaming-source-melbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-10T08:08:52.370Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://melbet.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=110&virtualSports=true
2026-07-10T08:08:52.887Z ERROR 1 --- [igaming-source-melbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://melbet.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=110&virtualSports=true: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://melbet.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=110&virtualSports=true
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://melbet.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=110&virtualSports=true
    at FrameSession._navigate (/tmp/playwright-java-9858141539005329510/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-9858141539005329510/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://melbet.com/service-api/LiveFeed/Get1x2_VZip?count=1000&lng=en&mode=4&country=207&partner=110&virtualSports=true", waiting until "domcontentloaded"

2026-07-10T08:08:52.888Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-melbet-com'
2026-07-10T08:08:53.105Z ERROR 1 --- [igaming-source-melbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-melbet-com' — pool exhausted.
2026-07-10T08:08:53.107Z ERROR 1 --- [igaming-source-melbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'melbet-com'. Terminating pod with exit code 1.
2026-07-10T08:08:55.116Z  INFO 1 --- [igaming-source-melbet-com] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-07-10T08:08:55.120Z  INFO 1 --- [igaming-source-melbet-com] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
2026-07-10T08:09:25.134Z  INFO 1 --- [igaming-source-melbet-com] [ionShutdownHook] o.s.c.support.DefaultLifecycleProcessor  : Shutdown phase 1073741823 ends with 1 bean still running after timeout of 30000ms: [taskScheduler]
2026-07-10T08:09:25.160Z  INFO 1 --- [igaming-source-melbet-com] [ionShutdownHook] p.d.i.s.core.browser.BrowserService      : Shutting down Universal Browser Service
2026-07-10T08:09:25.608Z  INFO 1 --- [igaming-source-melbet-com] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-10T08:09:25.613Z  INFO 1 --- [igaming-source-melbet-com] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-10T08:09:25.678Z  INFO 1 --- [igaming-source-melbet-com] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

```
#### 📥 Loader (`igaming-source-melbet-com-loader-7667cc45b7-2nfgn` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:57:59.458Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'melbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/melbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:00:59.469Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'melbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/melbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:03:59.479Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'melbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/melbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:06:59.490Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'melbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/melbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:59.498Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'melbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/melbet-com/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:03:59.479Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'melbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/melbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:06:59.490Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'melbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/melbet-com/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:59.498Z  WARN 1 --- [igaming-source-melbet-com] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'melbet-com': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/melbet-com/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `mrgreen`

#### 🕷️ Crawler (`igaming-source-mrgreen-crawler-6bc45cf98d-2d4cv` - Running)
**Анализ ошибок:**
```text
2026-07-10T08:10:05.886Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-2] p.d.i.s.s.service.Sport888ApiClient      : Failed to intercept JSON from https://www.888sport.com/ within 30s
2026-07-10T08:10:52.783Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '888sport': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/888sport/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:53.462Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-1] p.d.i.s.s.service.Sport888ApiClient      : Failed to intercept JSON from https://www.888sport.com/ within 30s
2026-07-10T08:11:42.161Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-1] p.d.i.s.s.service.Sport888ApiClient      : Failed to intercept JSON from https://www.888sport.com/ within 30s
```
**Последние строки логов:**
```text
2026-07-10T08:12:06.988Z  INFO 1 --- [igaming-source-888sport] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : [Intercept DBG] JSON URL: https://spectate-web.888sport.com/spectate/inplay-req/featured (contentType=application/json; charset=UTF-8, status=200)
2026-07-10T08:12:06.989Z  INFO 1 --- [igaming-source-888sport] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : [Intercept DBG] JSON URL: https://spectate-web.888sport.com/spectate/carousel_requests/getCarousel (contentType=application/json; charset=UTF-8, status=200)
2026-07-10T08:12:08.264Z  INFO 1 --- [igaming-source-888sport] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : [Intercept DBG] JSON URL: https://odds-api.checkd-dev.com/prod/evoke/football/trending-bet-builders?location=global&language=en (contentType=application/json, status=200)
```
#### 📥 Loader (`igaming-source-mrgreen-loader-67759cc95f-54njh` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:58:00.359Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '888sport': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/888sport/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:01:00.385Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '888sport': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/888sport/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:04:00.407Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '888sport': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/888sport/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:00.428Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '888sport': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/888sport/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:00.449Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '888sport': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/888sport/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:04:00.407Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '888sport': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/888sport/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:07:00.428Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '888sport': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/888sport/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:10:00.449Z  WARN 1 --- [igaming-source-888sport] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker '888sport': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/888sport/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `nordicbet`

#### 📥 Loader (`igaming-source-nordicbet-loader-69d6d48f45-wdkb5` - Failed)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-09T18:59:14.878Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.source.betsson.BetssonApplication  : Starting BetssonApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-09T18:59:14.884Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.source.betsson.BetssonApplication  : The following 1 profile is active: "match-loader"
2026-07-09T18:59:26.915Z  INFO 1 --- [igaming-source-betsson] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-09T18:59:26.919Z  INFO 1 --- [igaming-source-betsson] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-09T18:59:29.388Z  INFO 1 --- [igaming-source-betsson] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 2319 ms. Found 7 JPA repository interfaces.
2026-07-09T18:59:34.618Z  INFO 1 --- [igaming-source-betsson] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-09T18:59:34.628Z  INFO 1 --- [igaming-source-betsson] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-09T18:59:34.954Z  INFO 1 --- [igaming-source-betsson] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 132 ms. Found 0 Redis repository interfaces.
2026-07-09T18:59:38.574Z  INFO 1 --- [igaming-source-betsson] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=6cde169d-7ff5-3067-8cb8-fe9278c1bf0f
2026-07-09T18:59:49.453Z  INFO 1 --- [igaming-source-betsson] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3070 (http)
2026-07-09T18:59:49.631Z  INFO 1 --- [igaming-source-betsson] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-07-09T18:59:49.632Z  INFO 1 --- [igaming-source-betsson] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-07-09T18:59:50.609Z  INFO 1 --- [igaming-source-betsson] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-07-09T18:59:50.622Z  INFO 1 --- [igaming-source-betsson] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 35198 ms
2026-07-09T18:59:58.677Z  INFO 1 --- [igaming-source-betsson] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-09T18:59:59.290Z  INFO 1 --- [igaming-source-betsson] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-09T18:59:59.688Z  INFO 1 --- [igaming-source-betsson] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-09T19:00:03.209Z  INFO 1 --- [igaming-source-betsson] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-09T19:00:03.610Z  INFO 1 --- [igaming-source-betsson] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-09T19:00:03.686Z  INFO 1 --- [igaming-source-betsson] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-09T19:00:11.784Z  WARN 1 --- [igaming-source-betsson] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-07-09T19:00:12.292Z  INFO 1 --- [igaming-source-betsson] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-09T19:00:28.208Z  INFO 1 --- [igaming-source-betsson] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-09T19:00:31.138Z  WARN 1 --- [igaming-source-betsson] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-07-09T19:00:31.138Z  WARN 1 --- [igaming-source-betsson] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-07-09T19:00:31.625Z  WARN 1 --- [igaming-source-betsson] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-07-09T19:00:31.627Z  WARN 1 --- [igaming-source-betsson] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-07-09T19:00:31.728Z  WARN 1 --- [igaming-source-betsson] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-07-09T19:00:31.729Z  WARN 1 --- [igaming-source-betsson] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-07-09T19:00:31.934Z  INFO 1 --- [igaming-source-betsson] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-09T19:00:33.121Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-09T19:00:35.207Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.s.core.service.VpnManagerService   : VPN is disabled via app.vpn.enabled=false. Skipping proxy initialization.
2026-07-09T19:00:35.402Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-09T19:00:38.299Z  INFO 1 --- [igaming-source-betsson] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-09T19:00:52.370Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-09T19:00:52.378Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-09T19:00:52.906Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-09T19:00:52.934Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-09T19:00:52.948Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-09T19:00:52.967Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-09T19:00:54.097Z  WARN 1 --- [igaming-source-betsson] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2026-07-09T19:01:04.378Z  INFO 1 --- [igaming-source-betsson] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-07-09T19:01:05.215Z  INFO 1 --- [igaming-source-betsson] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 3070 (http) with context path '/'
2026-07-09T19:01:05.409Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.source.betsson.BetssonApplication  : Started BetssonApplication in 117.085 seconds (process running for 122.444)
2026-07-09T19:01:06.480Z  INFO 1 --- [igaming-source-betsson] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'betsson' (display: 'Betsson')
2026-07-09T19:01:06.968Z  WARN 1 --- [igaming-source-betsson] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betsson': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betsson/heartbeat": igaming-aggregator
2026-07-09T19:01:06.974Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'betsson': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/betsson/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-09T19:01:10.930Z  INFO 1 --- [igaming-source-betsson] [nio-3070-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-07-09T19:01:10.967Z  INFO 1 --- [igaming-source-betsson] [nio-3070-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-07-09T19:01:10.979Z  INFO 1 --- [igaming-source-betsson] [nio-3070-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 9 ms
2026-07-09T19:03:50.183Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@48efb29b (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
2026-07-09T19:03:50.225Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@7f006ff3 (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
2026-07-09T19:03:50.230Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@4a4e61e2 (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
2026-07-09T19:03:50.308Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@2f8db5ae (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
2026-07-09T19:03:50.309Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@6cd7751c (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
2026-07-09T19:03:50.312Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@1054f082 (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
2026-07-09T19:03:50.313Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@29acd21b (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
2026-07-09T19:03:50.314Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@6b4e3245 (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
2026-07-09T19:03:50.323Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@3ce6c5a5 (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
2026-07-09T19:03:50.324Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@b01f25b (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
2026-07-09T19:03:51.742Z  INFO 1 --- [igaming-source-betsson] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2026-07-09T19:03:51.761Z  INFO 1 --- [igaming-source-betsson] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
2026-07-09T19:04:20.030Z  WARN 1 --- [igaming-source-betsson] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-07-09T19:04:20.031Z ERROR 1 --- [igaming-source-betsson] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : HikariPool-1 - Connection is not available, request timed out after 30002ms (total=0, active=0, idle=0, waiting=0)
2026-07-09T19:04:20.031Z ERROR 1 --- [igaming-source-betsson] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-nordicbet-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-07-09T19:04:20.038Z ERROR 1 --- [igaming-source-betsson] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for betsson: Could not open JPA EntityManager for transaction
2026-07-09T19:04:20.305Z  INFO 1 --- [igaming-source-betsson] [ionShutdownHook] p.d.i.s.core.browser.BrowserService      : Shutting down Universal Browser Service
2026-07-09T19:04:20.316Z  INFO 1 --- [igaming-source-betsson] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-09T19:04:20.367Z  INFO 1 --- [igaming-source-betsson] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-09T19:04:20.832Z  INFO 1 --- [igaming-source-betsson] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

```

---

### 🗂️ Букмекер: `olimpbet`

#### 🕷️ Crawler (`igaming-source-olimpbet-crawler-65bf9c8667-8pn89` - Running)
**Анализ ошибок:**
```text
2026-07-10T08:11:27.156Z  INFO 1 --- [igaming-source-olimpbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-10T08:11:34.788Z  INFO 1 --- [igaming-source-olimpbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-07-10T08:11:34.793Z  INFO 1 --- [igaming-source-olimpbet] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2026-07-10T08:11:34.823Z  INFO 1 --- [igaming-source-olimpbet] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-07-10T08:11:34.845Z ERROR 1 --- [igaming-source-olimpbet] [           main] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [HikariPool-1 - Connection is not available, request timed out after 30000ms (total=0, active=0, idle=0, waiting=0)] [n/a]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1808) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:601) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:523) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:336) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:289) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:334) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:204) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:970) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:627) ~[spring-context-6.2.1.jar!/:6.2.1]
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:146) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:752) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:439) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:318) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1361) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1350) ~[spring-boot-3.4.1.jar!/:3.4.1]
	at pro.datawiki.igaming.source.olimpbet.OlimpbetApplication.main(OlimpbetApplication.java:15) ~[!/:0.0.1-SNAPSHOT]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]
	at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:102) ~[app.jar:0.0.1-SNAPSHOT]
	at org.springframework.boot.loader.launch.Launcher.launch(Launcher.java:64) ~[app.jar:0.0.1-SNAPSHOT]
	at org.springframework.boot.loader.launch.JarLauncher.main(JarLauncher.java:40) ~[app.jar:0.0.1-SNAPSHOT]
Caused by: jakarta.persistence.PersistenceException: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [HikariPool-1 - Connection is not available, request timed out after 30000ms (total=0, active=0, idle=0, waiting=0)] [n/a]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.buildNativeEntityManagerFactory(AbstractEntityManagerFactoryBean.java:431) ~[spring-orm-6.2.1.jar!/:6.2.1]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.afterPropertiesSet(AbstractEntityManagerFactoryBean.java:400) ~[spring-orm-6.2.1.jar!/:6.2.1]
	at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.afterPropertiesSet(LocalContainerEntityManagerFactoryBean.java:366) ~[spring-orm-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1855) ~[spring-beans-6.2.1.jar!/:6.2.1]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1804) ~[spring-beans-6.2.1.jar!/:6.2.1]
	... 20 common frames omitted
Caused by: org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [HikariPool-1 - Connection is not available, request timed out after 30000ms (total=0, active=0, idle=0, waiting=0)] [n/a]
	at org.hibernate.exception.internal.SQLExceptionTypeDelegate.convert(SQLExceptionTypeDelegate.java:51) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:94) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl.getIsolatedConnection(DdlTransactionIsolatorNonJtaImpl.java:74) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl.getIsolatedConnection(DdlTransactionIsolatorNonJtaImpl.java:39) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.tool.schema.internal.exec.ImprovedExtractionContextImpl.getJdbcConnection(ImprovedExtractionContextImpl.java:63) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.tool.schema.extract.spi.ExtractionContext.getQueryResults(ExtractionContext.java:43) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.tool.schema.extract.internal.SequenceInformationExtractorLegacyImpl.extractMetadata(SequenceInformationExtractorLegacyImpl.java:39) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.tool.schema.extract.internal.DatabaseInformationImpl.initializeSequences(DatabaseInformationImpl.java:66) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.tool.schema.extract.internal.DatabaseInformationImpl.<init>(DatabaseInformationImpl.java:60) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.tool.schema.internal.Helper.buildDatabaseInformation(Helper.java:185) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.tool.schema.internal.AbstractSchemaMigrator.doMigration(AbstractSchemaMigrator.java:93) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator.performDatabaseAction(SchemaManagementToolCoordinator.java:280) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator.lambda$process$5(SchemaManagementToolCoordinator.java:144) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at java.base/java.util.HashMap.forEach(Unknown Source) ~[na:na]
	at org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator.process(SchemaManagementToolCoordinator.java:141) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.boot.internal.SessionFactoryObserverForSchemaExport.sessionFactoryCreated(SessionFactoryObserverForSchemaExport.java:37) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.internal.SessionFactoryObserverChain.sessionFactoryCreated(SessionFactoryObserverChain.java:35) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.internal.SessionFactoryImpl.<init>(SessionFactoryImpl.java:324) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.boot.internal.SessionFactoryBuilderImpl.build(SessionFactoryBuilderImpl.java:463) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.build(EntityManagerFactoryBuilderImpl.java:1506) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.springframework.orm.jpa.vendor.SpringHibernateJpaPersistenceProvider.createContainerEntityManagerFactory(SpringHibernateJpaPersistenceProvider.java:66) ~[spring-orm-6.2.1.jar!/:6.2.1]
	at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.createNativeEntityManagerFactory(LocalContainerEntityManagerFactoryBean.java:390) ~[spring-orm-6.2.1.jar!/:6.2.1]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.buildNativeEntityManagerFactory(AbstractEntityManagerFactoryBean.java:419) ~[spring-orm-6.2.1.jar!/:6.2.1]
	... 24 common frames omitted
Caused by: java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30000ms (total=0, active=0, idle=0, waiting=0)
	at com.zaxxer.hikari.pool.HikariPool.createTimeoutException(HikariPool.java:686) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:179) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:144) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.HikariDataSource.getConnection(HikariDataSource.java:127) ~[HikariCP-5.1.0.jar!/:na]
	at org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl.getConnection(DatasourceConnectionProviderImpl.java:126) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess.obtainConnection(JdbcEnvironmentInitiator.java:467) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl.getIsolatedConnection(DdlTransactionIsolatorNonJtaImpl.java:46) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	... 44 common frames omitted
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
	at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:364) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.core.ConnectionFactory.openConnection(ConnectionFactory.java:54) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.jdbc.PgConnection.<init>(PgConnection.java:273) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.Driver.makeConnection(Driver.java:446) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.Driver.connect(Driver.java:298) ~[postgresql-42.7.4.jar!/:42.7.4]
	at com.zaxxer.hikari.util.DriverDataSource.getConnection(DriverDataSource.java:137) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.PoolBase.newConnection(PoolBase.java:360) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.PoolBase.newPoolEntry(PoolBase.java:202) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.HikariPool.createPoolEntry(HikariPool.java:461) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.HikariPool$PoolEntryCreator.call(HikariPool.java:724) ~[HikariCP-5.1.0.jar!/:na]
	at com.zaxxer.hikari.pool.HikariPool$PoolEntryCreator.call(HikariPool.java:703) ~[HikariCP-5.1.0.jar!/:na]
	at java.base/java.util.concurrent.FutureTask.run(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source) ~[na:na]
	at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
Caused by: java.net.SocketTimeoutException: Connect timed out
	at java.base/sun.nio.ch.NioSocketImpl.timedFinishConnect(Unknown Source) ~[na:na]
	at java.base/sun.nio.ch.NioSocketImpl.connect(Unknown Source) ~[na:na]
	at java.base/java.net.SocksSocketImpl.connect(Unknown Source) ~[na:na]
	at java.base/java.net.Socket.connect(Unknown Source) ~[na:na]
	at org.postgresql.core.PGStream.createSocket(PGStream.java:260) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.core.PGStream.<init>(PGStream.java:121) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.core.v3.ConnectionFactoryImpl.tryConnect(ConnectionFactoryImpl.java:140) ~[postgresql-42.7.4.jar!/:42.7.4]
	at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:268) ~[postgresql-42.7.4.jar!/:42.7.4]
	... 14 common frames omitted


```
#### 📥 Loader (`igaming-source-olimpbet-loader-6f57bf5cbf-tcm9c` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
:: Spring Boot ::                (v3.4.1)
2026-07-10T08:12:31.079Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.olimpbet.OlimpbetApplication     : Starting OlimpbetApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-10T08:12:31.084Z  INFO 1 --- [igaming-source-olimpbet] [           main] p.d.i.s.olimpbet.OlimpbetApplication     : The following 1 profile is active: "dev"
```

---

### 🗂️ Букмекер: `pari`

#### 🕷️ Crawler (`igaming-source-pari-crawler-79877465df-kxc26` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-07-09T22:54:45.871Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.source.fonbet.FonbetApplication    : Starting FonbetApplication v0.0.1-SNAPSHOT using Java 21.0.11 with PID 1 (/app/app.jar started by root in /app)
2026-07-09T22:54:45.895Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.source.fonbet.FonbetApplication    : The following 1 profile is active: "dev"
2026-07-09T22:54:55.485Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-09T22:54:55.491Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-07-09T22:54:57.494Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 1900 ms. Found 7 JPA repository interfaces.
2026-07-09T22:55:01.597Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-07-09T22:55:01.616Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-09T22:55:01.808Z  INFO 1 --- [igaming-source-pari] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 98 ms. Found 0 Redis repository interfaces.
2026-07-09T22:55:05.163Z  INFO 1 --- [igaming-source-pari] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=74788a06-dcbd-3576-8e9a-9f0c136fad88
2026-07-09T22:55:15.760Z  WARN 1 --- [igaming-source-pari] [           main] io.undertow.websockets.jsr               : UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used
2026-07-09T22:55:16.012Z  INFO 1 --- [igaming-source-pari] [           main] io.undertow.servlet                      : Initializing Spring embedded WebApplicationContext
2026-07-09T22:55:16.019Z  INFO 1 --- [igaming-source-pari] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 29339 ms
2026-07-09T22:55:25.847Z  INFO 1 --- [igaming-source-pari] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-07-09T22:55:26.606Z  INFO 1 --- [igaming-source-pari] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-07-09T22:55:27.014Z  INFO 1 --- [igaming-source-pari] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-07-09T22:55:30.455Z  INFO 1 --- [igaming-source-pari] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-07-09T22:55:30.805Z  INFO 1 --- [igaming-source-pari] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-07-09T22:55:30.961Z  INFO 1 --- [igaming-source-pari] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-07-09T22:55:34.197Z  INFO 1 --- [igaming-source-pari] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-07-09T22:55:49.369Z  INFO 1 --- [igaming-source-pari] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-07-09T22:55:50.173Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-07-09T22:55:50.173Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-07-09T22:55:50.243Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-07-09T22:55:50.246Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-07-09T22:55:50.272Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-07-09T22:55:50.281Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-07-09T22:55:50.329Z  INFO 1 --- [igaming-source-pari] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-07-09T22:55:54.727Z  INFO 1 --- [igaming-source-pari] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-07-09T22:56:06.321Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-07-09T22:56:06.343Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-07-09T22:56:06.415Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-07-09T22:56:06.433Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-07-09T22:56:06.448Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-07-09T22:56:06.471Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-07-09T22:56:14.995Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.browser.BrowserContextManager  : Loaded stealth.js script for browser injection (180462 bytes)
2026-07-09T22:56:15.183Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.service.VpnManagerService   : 🔍 [Startup] Checking direct connectivity to 'pari.ru' (no proxy)...
2026-07-09T22:56:17.376Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ [Startup] Direct connection to 'pari.ru' works — no proxy needed.
2026-07-09T22:56:17.376Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.service.VpnManagerService   : Proxy system properties cleared — using direct connection.
2026-07-09T22:56:17.512Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-07-09T22:56:17.559Z  INFO 1 --- [igaming-source-pari] [           main] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Initializing FonbetApiClient with Universal Browser Service
2026-07-09T22:56:17.573Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.c.e.f.s.JsonSchemaValidator      : Loaded Fonbet schema from /fonbet/urls-schema.json
2026-07-09T22:56:17.629Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.f.scheduler.MatchFetchScheduler  : Warming up Fonbet API client...
2026-07-09T22:56:17.629Z  INFO 1 --- [igaming-source-pari] [           main] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Fetching dynamic line URL list via full-page navigation: https://pari.ru/urls.json
2026-07-09T22:56:17.640Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-07-09T22:56:23.772Z  INFO 1 --- [igaming-source-pari] [           main] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: XVFB_HEADED and proxy: http://proxy-vpn-pool.service-proxy.svc.cluster.local:3128 (engine: chromium)
2026-07-09T22:56:27.382Z  WARN 1 --- [igaming-source-pari] [           main] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://pari.ru/urls.json
2026-07-09T22:56:28.332Z ERROR 1 --- [igaming-source-pari] [           main] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://pari.ru/urls.json: Error {
  message='net::ERR_PROXY_CONNECTION_FAILED at https://pari.ru/urls.json
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://pari.ru/urls.json
    at FrameSession._navigate (/tmp/playwright-java-6634584529041829654/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-6634584529041829654/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://pari.ru/urls.json", waiting until "domcontentloaded"

2026-07-09T22:56:28.334Z  WARN 1 --- [igaming-source-pari] [           main] p.d.i.s.core.service.VpnManagerService   : 🔄 Proxy rotation attempt #1 for service 'igaming-source-pari'
2026-07-09T22:56:29.649Z ERROR 1 --- [igaming-source-pari] [           main] p.d.i.s.core.service.VpnManagerService   : 🚨 Proxy pool API returned no proxy (503) for service 'igaming-source-pari' — pool exhausted.
2026-07-09T22:56:29.650Z ERROR 1 --- [igaming-source-pari] [           main] p.d.i.s.core.browser.BrowserExecutor     : 🚨 All proxies exhausted for bookmaker 'pari'. Terminating pod with exit code 1.

```
#### 📥 Loader (`igaming-source-pari-loader-7cc474459c-jrd9m` - Running)
**Анализ ошибок:**
```text
2026-07-10T08:00:11.928Z  WARN 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'pari': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/pari/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:03:11.945Z  WARN 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'pari': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/pari/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:06:11.958Z  WARN 1 --- [igaming-source-pari] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'pari': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/pari/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:11.975Z  WARN 1 --- [igaming-source-pari] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'pari': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/pari/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:12:11.994Z  WARN 1 --- [igaming-source-pari] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'pari': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/pari/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:06:11.958Z  WARN 1 --- [igaming-source-pari] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'pari': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/pari/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:09:11.975Z  WARN 1 --- [igaming-source-pari] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'pari': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/pari/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:12:11.994Z  WARN 1 --- [igaming-source-pari] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'pari': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/pari/heartbeat": igaming-aggregator: No address associated with hostname
```

---

### 🗂️ Букмекер: `pinnacle`

#### 🕷️ Crawler (`igaming-source-pinnacle-crawler-559b76fb9b-6tqgc` - Failed)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
unable to retrieve container logs for containerd://3889a3690278beb2764bc472c5e5baff89e447bc5c1c8bb5c9b45d592222df2b
```

---

### 🗂️ Букмекер: `spinbetter`

#### 🕷️ Crawler (`igaming-source-spinbetter-crawler-7556b4bd69-tw58l` - Failed)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
unable to retrieve container logs for containerd://b6ea0575e496a655e5c2a59e29a0c1af6d25e2d515667af17049d6928426491c
```

---

### 🗂️ Букмекер: `zenit`

#### 🕷️ Crawler (`igaming-source-zenit-crawler-66fcdb668f-tfbfg` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
2026-07-10T08:12:32.582Z  INFO 1 --- [igaming-source-zenit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-07-10T08:12:32.616Z  INFO 1 --- [igaming-source-zenit] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 14 ms. Found 0 Redis repository interfaces.
2026-07-10T08:12:33.741Z  INFO 1 --- [igaming-source-zenit] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=a13b794c-b8ed-351c-a4ad-5d2cfae87775
```
#### 📥 Loader (`igaming-source-zenit-loader-5ff8f6f768-8jrp9` - Running)
**Анализ ошибок:**
```text
2026-07-10T07:59:47.371Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'zenit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/zenit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:02:47.397Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'zenit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/zenit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:05:47.417Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'zenit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/zenit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:08:47.434Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'zenit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/zenit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:11:47.447Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'zenit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/zenit/heartbeat": igaming-aggregator: No address associated with hostname
```
**Последние строки логов:**
```text
2026-07-10T08:05:47.417Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'zenit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/zenit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:08:47.434Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'zenit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/zenit/heartbeat": igaming-aggregator: No address associated with hostname
2026-07-10T08:11:47.447Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Failed to send heartbeat for bookmaker 'zenit': I/O error on POST request for "http://igaming-aggregator/api/bookmakers/zenit/heartbeat": igaming-aggregator: No address associated with hostname
```

---

