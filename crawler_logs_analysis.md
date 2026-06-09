# 🕵️ Анализ логов кроулеров и лоадеров

**Время анализа:** 2026-06-09 20:05:25

## 📌 Сводная таблица по всем БК

| Букмекер | Краулер Статус | Лоадер Статус | Анализ ошибок краулера | Анализ ошибок лоадера |
| :--- | :--- | :--- | :--- | :--- |
| `1xbet` | Running | Running | 2026-06-09T16:56:26.254Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (fir... | TIMEOUT: Command timed out after 15 seconds |
| `888starz` | Running | Running | 2026-06-09T16:50:50.415Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-4] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (... | OK (No errors found in last 100 lines) |
| `baltbet` | Running | Running | OK (No errors found in last 100 lines) | TIMEOUT: Command timed out after 15 seconds |
| `bet365` | Running | Running | 2026-06-09T17:02:16.993Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profi... | 2026-06-09T16:25:38.946Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: ... |
| `betandyou` | Running | Running | 2026-06-09T16:52:58.579Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview ... | 2026-06-09T16:34:36.551Z  INFO 1 --- [igaming-source-betandyou] [dyou-producer-1] org.apache.kafka.clients.NetworkClient   : [Producer clientId=igamin... |
| `betboom` | Running | Running | 2026-06-09T16:51:52.456Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not v... | TIMEOUT: Command timed out after 15 seconds |
| `betcity` | Running | Running | 2026-06-09T17:04:39.704Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='... | 2026-06-09T17:03:23.234Z ERROR 1 --- [igaming-source-betcity] [   virtual-2300] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmak... |
| `betcity-com` | Running | Running | 	at java.base/java.lang.reflect.Method.invoke(Unknown Source) ~[na:na]<br>	at org.springframework.scheduling.support.ScheduledMethodRunnable.runIntern... | TIMEOUT: Command timed out after 15 seconds |
| `betm` | Running | Running | 2026-06-09T17:03:33.257Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config... | java.lang.NullPointerException: null<br><br>2026-06-09T17:03:22.107Z ERROR 1 --- [igaming-source-betcity] [   virtual-2297] .c.e.AbstractBookmakerPerf... |
| `bettery` | Running | Running | TIMEOUT: Command timed out after 15 seconds | OK (No errors found in last 100 lines) |
| `dafabet` | Running | Running | 2026-06-09T17:00:47.941Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=571... | 2026-06-09T17:00:35.797Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=571... |
| `fansport` | Running | Running | 2026-06-09T16:55:05.897Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-4] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (... | TIMEOUT: Command timed out after 15 seconds |
| `fon-bet-by` | Running | Running | OK (No errors found in last 100 lines) | OK (No errors found in last 100 lines) |
| `fon-bet-kz` | Running | Running |     at ProgressController.run (/tmp/playwright-java-6275616256835819471/package/lib/server/progress.js:78:26)<br>    at Frame.goto (/tmp/playwright-ja... | OK (No errors found in last 100 lines) |
| `fon-bet-ru` | Running | Running | OK (No errors found in last 100 lines) | OK (No errors found in last 100 lines) |
| `leon` | Running | Running | 2026-06-09T17:00:07.392Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4398, NEW... | 2026-06-09T17:00:36.452Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4400, NEW... |
| `ligastavok` | Running | Running | 2026-06-09T16:59:51.450Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filte... | TIMEOUT: Command timed out after 15 seconds |
| `linebet` | Running | Running | 2026-06-09T16:52:23.017Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (f... | 2026-06-09T16:39:54.544Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation:... |
| `marathonbet` | Running | Running | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... | 2026-06-09T16:59:36.966Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-747] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all... |
| `marathonbet-by` | Running | Running | OK (No errors found in last 100 lines) | 2026-06-09T17:03:30.639Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-902] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for ... |
| `marathonbet-com` | Running | Running | 2026-06-09T17:02:58.209Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Con... | 2026-06-09T16:30:27.920Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-873] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for... |
| `megapari` | Running | Running | 2026-06-09T16:55:37.416Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (... | 2026-06-09T16:34:36.050Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 26 ... |
| `melbet` | Running | Running | 2026-06-09T16:25:01.204Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: ... | TIMEOUT: Command timed out after 15 seconds |
| `olimpbet` | Running | Running | OK (No errors found in last 100 lines) | OK (No errors found in last 100 lines) |
| `olimpbet-kz` | Running | Running | OK (No errors found in last 100 lines) | OK (No errors found in last 100 lines) |
| `pari` | Running | Running | OK (No errors found in last 100 lines) | OK (No errors found in last 100 lines) |
| `pinnacle` | Running | Running | 2026-06-09T16:58:46.379Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fi... | <br>  .   ____          _            __ _ _<br> /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \<br>( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \<br> \\/  ___)| |_... |
| `sbobet` | Running | Running | 2026-06-09T17:02:49.466Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, ... | 2026-06-09T17:00:29.205Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, ... |
| `sportbet` | Running | Running | OK (No errors found in last 100 lines) | OK (No errors found in last 100 lines) |
| `tennisi` | Running | Running | OK (No errors found in last 100 lines) | OK (No errors found in last 100 lines) |
| `winline` | Running | Running | 2026-06-09T16:57:40.909Z  WARN 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.c.s.SportNormalizationService    : ALARM ALARM new sport: book... | 2026-06-09T17:03:09.672Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEM... |
| `zenit` | Running | Running | 2026-06-09T17:04:02.264Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161492: c2_id 1... | OK (No errors found in last 100 lines) |


## 📝 Детальные логи и контекст по проблемным БК

### 🗂️ Букмекер: `1xbet`

#### 🕷️ Crawler (`igaming-source-1xbet-crawler-58d98dc65c-fjht2` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:56:26.254Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":1,"S2":
2026-06-09T16:56:41.415Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":200,"HL":true,"RLI":["5af2b3
2026-06-09T17:01:52.927Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{},"PS":[{"Ke
2026-06-09T17:02:03.985Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":3477,"HL":true,"RLI":["5af2b
```
**Последние строки логов:**
```text
2026-06-09T17:04:42.561Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host '1xbet.com' is still reachable...
2026-06-09T17:04:42.680Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='1xbet' name='Xbet-family'
2026-06-09T17:04:46.612Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 475 (service: igaming-source-1xbet, host: 1xbet.com)
```
#### 📥 Loader (`igaming-source-1xbet-loader-66fddd86d4-d76dq` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```

---

### 🗂️ Букмекер: `888starz`

#### 🕷️ Crawler (`igaming-source-888starz-crawler-798fffcdbf-66wwr` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:50:50.415Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-4] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":550,"GVE":1,"HL":true,"RLI":
2026-06-09T16:56:00.810Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":1,"S2":
2026-06-09T16:56:11.107Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":229,"GVE":1,"HL":true,"RLI":
2026-06-09T17:01:23.151Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{},"PS":[{"Ke
2026-06-09T17:01:38.828Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":3502,"GVE":1,"HL":true,"RLI"
```
**Последние строки логов:**
```text
2026-06-09T17:03:10.563Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='888starz' name='Xbet-family'
2026-06-09T17:03:51.611Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host '888starz.bet' is still reachable...
2026-06-09T17:03:59.712Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 974 (service: igaming-source-888starz, host: 888starz.bet)
```
#### 📥 Loader (`igaming-source-888starz-loader-55c594dbc-b6drm` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
2026-06-09T17:03:32.295Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-888starz, host: 888starz.bet)
2026-06-09T17:04:32.296Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host '888starz.bet' is still reachable...
2026-06-09T17:04:34.070Z  INFO 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-888starz, host: 888starz.bet)
```

---

### 🗂️ Букмекер: `baltbet`

#### 🕷️ Crawler (`igaming-source-baltbet-crawler-6cd846587f-v5sb5` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
2026-06-09T17:04:03.687Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-1] p.d.i.s.b.scheduler.MatchFetchScheduler  : Finished LIVE discovery job, discovered: 235
2026-06-09T17:04:37.949Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'events.baltbet.ru' is still reachable...
2026-06-09T17:04:40.574Z  INFO 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-baltbet, host: events.baltbet.ru)
```
#### 📥 Loader (`igaming-source-baltbet-loader-5cfd58dcbf-w4nv9` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```

---

### 🗂️ Букмекер: `bet365`

#### 🕷️ Crawler (`igaming-source-bet365-crawler-7f69d7984-bsfjr` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:02:16.993Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.19.102:3128
2026-06-09T17:02:30.624Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T17:02:32.081Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:02:35.705Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://www.bet365.com. Rotating proxy.
2026-06-09T17:02:35.705Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.19.102 (config: vpngate-vpn117306238). Requesting rotation...
2026-06-09T17:02:35.882Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:02:35.882Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T17:02:37.875Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.2.105' (config: vpngate-vpn416596627, ID: 415, healthy proxies: 39)
2026-06-09T17:02:37.875Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.2.105:3128
2026-06-09T17:02:37.875Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.2.105:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:02:37.876Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.bet365.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:02:37.876Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.bet365.com'...
2026-06-09T17:02:37.888Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.bet365.com: Blocked by Cloudflare/security system
2026-06-09T17:02:37.889Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.bet365.service.Bet365ApiClient   : Blank body retrieved from Bet365 lobby. Likely network or Turnstile block.
2026-06-09T17:02:37.889Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.bet365.service.Bet365ApiClient   : Successfully scraped 0 active events from Bet365
2026-06-09T17:02:37.890Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.b.s.Bet365DiscoveryService       : Bet365 API returned empty event list.
2026-06-09T17:02:37.926Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.bet365.com -> 172.64.155.195
2026-06-09T17:02:37.926Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.bet365.com'...
2026-06-09T17:02:47.940Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.bet365.com: Read timed out
2026-06-09T17:02:47.941Z  WARN 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.bet365.com': java.net.SocketTimeoutException: Read timed out
2026-06-09T17:02:47.941Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T17:02:52.890Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.bet365.service.Bet365ApiClient   : Scraping public Bet365 site dynamically from: https://www.bet365.com
2026-06-09T17:02:52.891Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.19.102:3128 to http://10.42.2.105:3128. Recreating browser...
2026-06-09T17:02:52.942Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'www.bet365.com'...
2026-06-09T17:02:52.942Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: www.bet365.com -> 172.64.155.195
2026-06-09T17:02:52.942Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://www.bet365.com'...
2026-06-09T17:02:55.898Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.2.105:3128
2026-06-09T17:02:57.333Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T17:03:03.658Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.bet365.com: HTTP check returned status 451
2026-06-09T17:03:03.658Z  WARN 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'www.bet365.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-09T17:03:03.658Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T17:03:08.658Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Resolving DNS for 'www.bet365.com'...
2026-06-09T17:03:08.688Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] DNS resolved: www.bet365.com -> 172.64.155.195
2026-06-09T17:03:08.688Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Checking HTTP connectivity to 'https://www.bet365.com'...
2026-06-09T17:03:14.035Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.bet365.com: HTTP check returned status 451
2026-06-09T17:03:14.036Z  WARN 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 3/3] Connectivity check failed for 'www.bet365.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-09T17:03:14.036Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.bet365.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T17:03:14.036Z  WARN 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity verification failed: 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.bet365.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status. Will try to rotate on next failure.
2026-06-09T17:03:24.000Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://www.bet365.com. Rotating proxy.
2026-06-09T17:03:24.000Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.2.105 (config: vpngate-vpn416596627). Requesting rotation...
2026-06-09T17:03:24.275Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported block for config 415 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:03:24.275Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T17:03:24.521Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.7.110' (config: vpngate-vpn550746266, ID: 922, healthy proxies: 2)
2026-06-09T17:03:24.521Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.7.110:3128
2026-06-09T17:03:24.521Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.7.110:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:03:24.522Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.bet365.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:03:24.522Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.bet365.com'...
2026-06-09T17:03:24.522Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.bet365.com -> 172.64.155.195
2026-06-09T17:03:24.522Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.bet365.com'...
2026-06-09T17:03:24.539Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.bet365.com: Blocked by Cloudflare/security system
2026-06-09T17:03:24.540Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.bet365.service.Bet365ApiClient   : Blank body retrieved from Bet365 lobby. Likely network or Turnstile block.
2026-06-09T17:03:24.540Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.bet365.service.Bet365ApiClient   : Successfully scraped 0 active events from Bet365
2026-06-09T17:03:24.540Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.b.s.Bet365DiscoveryService       : Bet365 API returned empty event list.
2026-06-09T17:03:25.059Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.bet365.com' is reachable (HTTP 403). Proxy connectivity verified.
2026-06-09T17:03:26.278Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:03:32.081Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T17:03:32.731Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:03:39.540Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.bet365.service.Bet365ApiClient   : Scraping public Bet365 site dynamically from: https://www.bet365.com
2026-06-09T17:03:39.541Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.2.105:3128 to http://10.42.7.110:3128. Recreating browser...
2026-06-09T17:03:39.643Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.7.110:3128
2026-06-09T17:03:54.512Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://www.bet365.com. Rotating proxy.
2026-06-09T17:03:54.513Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.7.110 (config: vpngate-vpn550746266). Requesting rotation...
2026-06-09T17:03:57.837Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported block for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:03:57.837Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T17:04:00.460Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.3.150' (config: vpngate-patito, ID: 513, healthy proxies: 1)
2026-06-09T17:04:00.460Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.3.150:3128
2026-06-09T17:04:00.460Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.3.150:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:04:00.460Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.bet365.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:04:00.460Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.bet365.com'...
2026-06-09T17:04:00.468Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.bet365.com: Blocked by Cloudflare/security system
2026-06-09T17:04:00.468Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.bet365.service.Bet365ApiClient   : Blank body retrieved from Bet365 lobby. Likely network or Turnstile block.
2026-06-09T17:04:00.469Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.bet365.service.Bet365ApiClient   : Successfully scraped 0 active events from Bet365
2026-06-09T17:04:00.469Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.b.s.Bet365DiscoveryService       : Bet365 API returned empty event list.
2026-06-09T17:04:00.629Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.bet365.com -> 104.18.32.61
2026-06-09T17:04:00.629Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.bet365.com'...
2026-06-09T17:04:02.113Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.bet365.com' is reachable (HTTP 403). Proxy connectivity verified.
2026-06-09T17:04:02.820Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:04:15.470Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.bet365.service.Bet365ApiClient   : Scraping public Bet365 site dynamically from: https://www.bet365.com
2026-06-09T17:04:15.470Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.7.110:3128 to http://10.42.3.150:3128. Recreating browser...
2026-06-09T17:04:15.631Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.3.150:3128
2026-06-09T17:04:32.732Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T17:04:34.015Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:04:37.331Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://www.bet365.com. Rotating proxy.
2026-06-09T17:04:37.331Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.3.150 (config: vpngate-patito). Requesting rotation...
2026-06-09T17:04:37.560Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported block for config 513 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:04:37.560Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T17:04:37.824Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.3.246' (config: vpngate-vpn925380463, ID: 570, healthy proxies: 1)
2026-06-09T17:04:37.825Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.3.246:3128
2026-06-09T17:04:37.825Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.3.246:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:04:37.825Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.bet365.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:04:37.825Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.bet365.com'...
2026-06-09T17:04:37.833Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.bet365.com: Blocked by Cloudflare/security system
2026-06-09T17:04:37.834Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.bet365.service.Bet365ApiClient   : Blank body retrieved from Bet365 lobby. Likely network or Turnstile block.
2026-06-09T17:04:37.834Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.bet365.service.Bet365ApiClient   : Successfully scraped 0 active events from Bet365
2026-06-09T17:04:37.834Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.b.s.Bet365DiscoveryService       : Bet365 API returned empty event list.
2026-06-09T17:04:37.872Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.bet365.com -> 172.64.155.195
2026-06-09T17:04:37.872Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.bet365.com'...
2026-06-09T17:04:47.881Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.bet365.com: Connect timed out
2026-06-09T17:04:47.882Z  WARN 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.bet365.com': java.net.SocketTimeoutException: Connect timed out
2026-06-09T17:04:47.882Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...

```
#### 📥 Loader (`igaming-source-bet365-loader-75d6bc8797-5gnhj` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:25:38.946Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:25:47.457Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:26:07.508Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:26:47.461Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:27:29.608Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:28:29.613Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:28:46.114Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:29:07.569Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:29:46.115Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:29:48.777Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:30:48.778Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:30:49.714Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:31:49.720Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:31:50.759Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:32:07.630Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:32:50.760Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:32:51.695Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:33:51.700Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:33:53.155Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:34:53.157Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:35:03.173Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.bet365.com: Read timed out
2026-06-09T16:35:03.173Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.bet365.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T16:35:03.174Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.7.110 (config: vpngate-vpn550746266). Requesting rotation...
2026-06-09T16:35:07.863Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:35:14.189Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported block for config 922 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:35:14.189Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T16:35:28.948Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.19.102' (config: vpngate-vpn117306238, ID: 1175, healthy proxies: 42)
2026-06-09T16:35:28.949Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.19.102:3128
2026-06-09T16:35:28.949Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.19.102:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T16:35:28.951Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.bet365.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T16:35:28.951Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.bet365.com'...
2026-06-09T16:35:29.305Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.bet365.com -> 104.18.32.61
2026-06-09T16:35:29.307Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.bet365.com'...
2026-06-09T16:35:30.924Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.bet365.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T16:35:31.045Z  INFO 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:36:28.951Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:36:30.812Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:37:30.814Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:37:32.403Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:38:07.952Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:38:32.406Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:38:35.481Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:39:35.482Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:39:37.357Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:40:37.358Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:40:39.390Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:41:08.074Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:41:39.391Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:41:41.959Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:42:41.961Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:42:43.686Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:43:43.687Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:43:45.381Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:44:08.137Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:44:45.382Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:44:47.343Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:45:47.345Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:45:50.956Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:46:50.957Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:46:53.144Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:47:08.208Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:47:53.145Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:47:55.089Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:48:55.089Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:48:56.749Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:49:56.750Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:49:58.466Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:50:08.267Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:50:58.467Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:51:00.007Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:52:00.008Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:52:02.125Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:53:02.126Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:53:08.079Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:53:08.330Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:54:08.080Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:54:10.038Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:55:10.039Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:55:12.146Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:56:08.405Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:56:12.147Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:56:13.820Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:57:13.821Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:57:15.678Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:58:15.679Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:58:17.286Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T16:59:08.488Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T16:59:17.287Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T16:59:18.874Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:00:18.876Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T17:00:20.672Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:01:20.674Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T17:01:22.468Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:02:08.552Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='bet365' name='Bet365'
2026-06-09T17:02:22.472Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T17:02:24.233Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:03:24.233Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T17:03:27.111Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)
2026-06-09T17:04:27.111Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.bet365.com' is still reachable...
2026-06-09T17:04:28.718Z  INFO 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-bet365, host: www.bet365.com)

```

---

### 🗂️ Букмекер: `betandyou`

#### 🕷️ Crawler (`igaming-source-betandyou-crawler-6fb88bd8f8-m9ndg` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:52:58.579Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":422,"GVE":1,"HL":true,"RLI":
2026-06-09T16:58:07.593Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":1,"S2":
2026-06-09T16:58:16.834Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":104,"GVE":1,"HL":true,"RLI":
2026-06-09T17:03:27.892Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{},"PS":[{"Ke
2026-06-09T17:03:36.751Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":3384,"GVE":1,"HL":true,"RLI"
```
**Последние строки логов:**
```text
2026-06-09T17:03:49.397Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T17:04:17.799Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T17:04:24.342Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 536 (service: igaming-source-betandyou, host: betandyou.com)
```
#### 📥 Loader (`igaming-source-betandyou-loader-5dcbc7588b-jccpr` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:34:36.551Z  INFO 1 --- [igaming-source-betandyou] [dyou-producer-1] org.apache.kafka.clients.NetworkClient   : [Producer clientId=igaming-source-betandyou-producer-1] Node -1 disconnected.
2026-06-09T16:35:14.161Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T16:35:23.407Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:35:29.324Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:36:29.327Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:36:35.625Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:36:48.222Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T16:36:56.833Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T16:37:03.566Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 22 betandyou matches
2026-06-09T16:37:35.626Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:37:41.131Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:38:14.229Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T16:38:41.132Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:38:46.659Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:39:46.660Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:39:52.794Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:40:52.795Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:40:58.495Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:41:14.262Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T16:41:58.496Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:42:04.095Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:42:07.173Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T16:42:13.440Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 20 betandyou matches
2026-06-09T16:42:25.642Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 29 betandyou matches
2026-06-09T16:43:04.096Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:43:08.681Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:44:08.682Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:44:14.282Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T16:44:14.933Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:44:36.636Z  INFO 1 --- [igaming-source-betandyou] [dyou-producer-1] org.apache.kafka.clients.NetworkClient   : [Producer clientId=igaming-source-betandyou-producer-1] Node -1 disconnected.
2026-06-09T16:45:14.934Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:45:22.227Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:46:22.229Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:46:27.830Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:47:14.497Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T16:47:27.831Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:47:29.066Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 15 betandyou matches
2026-06-09T16:47:33.075Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:47:35.888Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T16:47:41.250Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 6 betandyou matches
2026-06-09T16:47:48.955Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 26 betandyou matches
2026-06-09T16:48:33.076Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:48:38.792Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:49:38.792Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:49:44.358Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:50:14.741Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T16:50:44.360Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:50:49.402Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:51:49.403Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:51:56.193Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:52:52.882Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T16:52:56.194Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:52:59.376Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 27 betandyou matches
2026-06-09T16:53:05.150Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 26 betandyou matches
2026-06-09T16:53:05.829Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:53:14.785Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T16:54:05.832Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:54:11.529Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:55:11.533Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:55:17.273Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:56:14.804Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T16:56:17.274Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:56:22.882Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:57:22.884Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:57:37.844Z ERROR 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://betandyou.com: Remote host terminated the handshake
2026-06-09T16:57:37.845Z ERROR 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'betandyou.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T16:57:37.845Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.19.95 (config: vpngate-vpn667888734). Requesting rotation...
2026-06-09T16:57:38.014Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported block for config 628 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:57:38.016Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T16:57:40.415Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.3.252' (config: vpngate-vpn422139168, ID: 959, healthy proxies: 48)
2026-06-09T16:57:40.420Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.3.252:3128
2026-06-09T16:57:40.421Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.3.252:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T16:57:40.422Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'betandyou.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T16:57:40.422Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'betandyou.com'...
2026-06-09T16:57:40.452Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: betandyou.com -> 178.253.28.174
2026-06-09T16:57:40.452Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://betandyou.com'...
2026-06-09T16:57:45.766Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'betandyou.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T16:57:45.881Z  INFO 1 --- [igaming-source-betandyou] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 959 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T16:58:09.320Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T16:58:14.974Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T16:58:20.829Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T16:58:26.365Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 11 betandyou matches
2026-06-09T16:58:40.422Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:58:45.937Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": service-proxy-backend.service-proxy.svc.cluster.local:80 failed to respond
2026-06-09T16:59:14.830Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T16:59:45.938Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T16:59:52.130Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 959 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T17:00:52.134Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T17:00:57.857Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 959 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T17:01:57.858Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T17:02:03.442Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 959 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T17:02:14.870Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betandyou' name='Xbet-family'
2026-06-09T17:03:03.443Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T17:03:09.689Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 959 (service: igaming-source-betandyou, host: betandyou.com)
2026-06-09T17:03:29.935Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T17:03:36.034Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T17:03:43.972Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 betandyou matches
2026-06-09T17:03:50.883Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 22 betandyou matches
2026-06-09T17:04:09.689Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'betandyou.com' is still reachable...
2026-06-09T17:04:20.212Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 959 (service: igaming-source-betandyou, host: betandyou.com)

```

---

### 🗂️ Букмекер: `betboom`

#### 🕷️ Crawler (`igaming-source-betboom-crawler-6df57fb6df-w9zdx` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:51:52.456Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Крикет
2026-06-09T16:51:52.501Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Боулинг
2026-06-09T16:51:52.802Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Регби союз
2026-06-09T16:51:53.012Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Регби лига
2026-06-09T16:51:53.172Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Австралийский футбол
2026-06-09T16:51:53.244Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Падел-теннис
2026-06-09T16:51:53.580Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Автоспорт
2026-06-09T16:51:53.678Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Мотоспорт
2026-06-09T16:51:53.770Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Велоспорт
2026-06-09T16:51:53.951Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Гольф
2026-06-09T16:51:53.984Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Парусный Спорт 
2026-06-09T16:51:53.986Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Completed page collection. Total unique events collected: 53
2026-06-09T16:51:59.096Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.c.engine.AbstractMatchService    : Discovered 53 prematch betboom events
2026-06-09T16:51:59.096Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled prematch fetch completed: 53 events discovered
2026-06-09T16:52:21.831Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T16:52:25.798Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T16:53:25.799Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T16:53:30.194Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T16:54:30.199Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T16:54:33.414Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T16:54:35.834Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betboom' name='Betboom'
2026-06-09T16:55:33.415Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T16:55:37.752Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T16:56:00.375Z  INFO 1 --- [igaming-source-betboom] [boom-producer-1] org.apache.kafka.clients.NetworkClient   : [Producer clientId=igaming-source-betboom-producer-1] Node -1 disconnected.
2026-06-09T16:56:37.754Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T16:56:40.329Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T16:56:59.098Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching prematch data from betboom.ru...
2026-06-09T16:56:59.098Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.b.service.BetboomBrowserClient   : Fetching prematch events via Playwright browser...
2026-06-09T16:56:59.098Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/prematch...
2026-06-09T16:57:01.568Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/prematch: Object doesn't exist: frame@4c6653a2a9b9847c788215ceef52edd3
2026-06-09T16:57:36.142Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betboom' name='Betboom'
2026-06-09T16:57:40.331Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T16:57:43.132Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T16:58:43.135Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T16:58:46.463Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T16:59:46.466Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T16:59:49.373Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T17:00:36.411Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betboom' name='Betboom'
2026-06-09T17:00:49.376Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T17:00:54.392Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T17:01:54.398Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T17:01:58.335Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T17:02:01.570Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching prematch data from betboom.ru...
2026-06-09T17:02:01.570Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Fetching prematch events via Playwright browser...
2026-06-09T17:02:01.571Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/prematch...
2026-06-09T17:02:11.459Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/live: Object doesn't exist: response@4a6b1a50c5c635837e22eb915c248c2c
2026-06-09T17:02:20.931Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Found 33 active sports with matches
2026-06-09T17:02:21.034Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Clicking sport: Футбол (expected matches: 598)
2026-06-09T17:02:21.757Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Successfully loaded and mapped 3 matches for Футбол
2026-06-09T17:02:21.832Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Clicking sport: Теннис (expected matches: 474)
2026-06-09T17:02:28.473Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Successfully loaded and mapped 52 matches for Теннис
2026-06-09T17:02:28.565Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Clicking sport: Кибер (expected matches: 99)
2026-06-09T17:02:40.167Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Subscription check timed out for Кибер
2026-06-09T17:02:40.174Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Баскетбол
2026-06-09T17:02:40.187Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Хоккей
2026-06-09T17:02:40.193Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Настольный теннис
2026-06-09T17:02:40.199Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Киберфутбол
2026-06-09T17:02:40.206Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Волейбол
2026-06-09T17:02:40.214Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Кибербаскетбол
2026-06-09T17:02:40.226Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Единоборства
2026-06-09T17:02:40.232Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Бокс
2026-06-09T17:02:40.248Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Гандбол
2026-06-09T17:02:40.257Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Футзал
2026-06-09T17:02:40.264Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Водное поло
2026-06-09T17:02:40.273Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Бейсбол
2026-06-09T17:02:40.279Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Снукер
2026-06-09T17:02:40.285Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Бадминтон
2026-06-09T17:02:40.295Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Дартс
2026-06-09T17:02:40.302Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Американский футбол
2026-06-09T17:02:40.311Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Формула-1
2026-06-09T17:02:40.321Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Пляжный футбол
2026-06-09T17:02:40.335Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Флорбол
2026-06-09T17:02:40.342Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Крикет
2026-06-09T17:02:40.356Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Боулинг
2026-06-09T17:02:40.365Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Регби союз
2026-06-09T17:02:40.383Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Регби лига
2026-06-09T17:02:40.392Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Австралийский футбол
2026-06-09T17:02:40.407Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Падел-теннис
2026-06-09T17:02:40.416Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Автоспорт
2026-06-09T17:02:40.434Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Мотоспорт
2026-06-09T17:02:40.461Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Велоспорт
2026-06-09T17:02:40.478Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Гольф
2026-06-09T17:02:40.496Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Sport sidebar locator not visible for Парусный Спорт 
2026-06-09T17:02:40.497Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Completed page collection. Total unique events collected: 55
2026-06-09T17:02:44.654Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.c.engine.AbstractMatchService    : Discovered 55 prematch betboom events
2026-06-09T17:02:44.657Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled prematch fetch completed: 55 events discovered
2026-06-09T17:02:58.337Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T17:03:01.850Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T17:03:11.473Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-06-09T17:03:11.473Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-06-09T17:03:11.473Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-06-09T17:03:28.701Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Could not find Redux store on https://betboom.ru/sport/live
2026-06-09T17:03:36.489Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betboom' name='Betboom'
2026-06-09T17:04:01.866Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'siteapi.betboom.ru' is still reachable...
2026-06-09T17:04:05.288Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betboom, host: siteapi.betboom.ru)
2026-06-09T17:04:28.904Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from betboom.ru...
2026-06-09T17:04:28.904Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Fetching live events via Playwright browser...
2026-06-09T17:04:28.905Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Navigating to https://betboom.ru/sport/live...
2026-06-09T17:04:44.324Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Found 0 active sports with matches
2026-06-09T17:04:44.324Z  INFO 1 --- [igaming-source-betboom] [ loader-sched-2] p.d.i.s.b.service.BetboomBrowserClient   : Completed page collection. Total unique events collected: 0

```
#### 📥 Loader (`igaming-source-betboom-loader-674b6f9687-lqss4` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```

---

### 🗂️ Букмекер: `betcity`

#### 🕷️ Crawler (`igaming-source-betcity-crawler-55644bf67c-vr5nl` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:04:39.704Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795202' blockGroupName='IT_T2'
2026-06-09T17:04:39.704Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:39.875Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795211' blockGroupName='IT_T1'
2026-06-09T17:04:39.875Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:40.050Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795212' blockGroupName='IT_T2'
2026-06-09T17:04:40.050Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:40.137Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795222' blockGroupName='IT_T2'
2026-06-09T17:04:40.137Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:41.322Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 21681590: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:41.644Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797461' blockGroupName='IT_T1'
2026-06-09T17:04:41.645Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:41.710Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797462' blockGroupName='IT_T2'
2026-06-09T17:04:41.711Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:41.784Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797471' blockGroupName='IT_T1'
2026-06-09T17:04:41.784Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:41.845Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797472' blockGroupName='IT_T2'
2026-06-09T17:04:41.846Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:41.909Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797451' blockGroupName='IT_T1'
2026-06-09T17:04:41.909Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:41.962Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797452' blockGroupName='IT_T2'
2026-06-09T17:04:41.963Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:42.016Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797482' blockGroupName='IT_T2'
2026-06-09T17:04:42.016Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:42.879Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 22203780: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:43.200Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 22203699: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:43.487Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 22203700: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:43.771Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801971' blockGroupName='IT_T1'
2026-06-09T17:04:43.771Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:43.837Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801972' blockGroupName='IT_T2'
2026-06-09T17:04:43.837Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:43.890Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801981' blockGroupName='IT_T1'
2026-06-09T17:04:43.890Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:43.987Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801982' blockGroupName='IT_T2'
2026-06-09T17:04:43.987Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:44.057Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801991' blockGroupName='IT_T1'
2026-06-09T17:04:44.057Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:44.115Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801992' blockGroupName='IT_T2'
2026-06-09T17:04:44.115Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:44.180Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767802001' blockGroupName='IT_T1'
2026-06-09T17:04:44.180Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:44.249Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767802002' blockGroupName='IT_T2'
2026-06-09T17:04:44.250Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:45.396Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767810051' blockGroupName='IT_T1'
2026-06-09T17:04:45.396Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:46.667Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767983931' blockGroupName='IT_T1'
2026-06-09T17:04:46.670Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:46.725Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767983932' blockGroupName='IT_T2'
2026-06-09T17:04:46.725Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:46.776Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767983941' blockGroupName='IT_T1'
2026-06-09T17:04:46.776Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:46.844Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767983942' blockGroupName='IT_T2'
2026-06-09T17:04:46.844Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:47.014Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767983921' blockGroupName='IT_T1'
2026-06-09T17:04:47.014Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:47.069Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767983922' blockGroupName='IT_T2'
2026-06-09T17:04:47.069Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:48.223Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23302830: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:48.733Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23155134: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:48.987Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 22517809: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:49.429Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23218980: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:49.925Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767816831' blockGroupName='IT_T1'
2026-06-09T17:04:49.925Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:50.072Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767816832' blockGroupName='IT_T2'
2026-06-09T17:04:50.072Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:50.134Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767816841' blockGroupName='IT_T1'
2026-06-09T17:04:50.135Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:50.229Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767816842' blockGroupName='IT_T2'
2026-06-09T17:04:50.229Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:50.345Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767816851' blockGroupName='IT_T1'
2026-06-09T17:04:50.346Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:50.415Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767816852' blockGroupName='IT_T2'
2026-06-09T17:04:50.415Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:50.550Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767816861' blockGroupName='IT_T1'
2026-06-09T17:04:50.563Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:50.706Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767816862' blockGroupName='IT_T2'
2026-06-09T17:04:50.706Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]

```
#### 📥 Loader (`igaming-source-betcity-loader-78c5958b8f-ctrrj` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:03:23.234Z ERROR 1 --- [igaming-source-betcity] [   virtual-2300] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null

java.lang.NullPointerException: null

2026-06-09T17:03:24.516Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 betcity matches
2026-06-09T17:03:26.812Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 betcity matches
2026-06-09T17:03:28.988Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 5 betcity matches
2026-06-09T17:03:31.223Z ERROR 1 --- [igaming-source-betcity] [   virtual-2312] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null

java.lang.NullPointerException: null

2026-06-09T17:03:31.260Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 3 betcity matches
2026-06-09T17:03:32.480Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2316] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14765785981' blockGroupName='IT_T1'
2026-06-09T17:03:32.481Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2316] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:03:32.508Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2316] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14765785982' blockGroupName='IT_T2'
2026-06-09T17:03:32.508Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2316] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:03:34.014Z ERROR 1 --- [igaming-source-betcity] [   virtual-2315] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null

java.lang.NullPointerException: null

2026-06-09T17:03:34.046Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 3 betcity matches
2026-06-09T17:03:50.893Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ad.betcity.ru' is still reachable...
2026-06-09T17:03:55.390Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 998 (service: igaming-source-betcity, host: ad.betcity.ru)
2026-06-09T17:04:42.615Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795191' blockGroupName='IT_T1'
2026-06-09T17:04:42.615Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:42.668Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795192' blockGroupName='IT_T2'
2026-06-09T17:04:42.668Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:42.766Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795201' blockGroupName='IT_T1'
2026-06-09T17:04:42.766Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:42.821Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795202' blockGroupName='IT_T2'
2026-06-09T17:04:42.821Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:42.876Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795211' blockGroupName='IT_T1'
2026-06-09T17:04:42.876Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:42.929Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795212' blockGroupName='IT_T2'
2026-06-09T17:04:42.929Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:42.995Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795222' blockGroupName='IT_T2'
2026-06-09T17:04:42.995Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:44.002Z ERROR 1 --- [igaming-source-betcity] [   virtual-2318] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null

java.lang.NullPointerException: null

2026-06-09T17:04:44.037Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] .c.e.AbstractBookmakerPerformanceTracker : [betcity] Performance Report - Pages processed: 20, Skipped: 0, Not found: 0, Known errors: {}
2026-06-09T17:04:45.405Z ERROR 1 --- [igaming-source-betcity] [   virtual-2324] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null

java.lang.NullPointerException: null

2026-06-09T17:04:45.406Z ERROR 1 --- [igaming-source-betcity] [   virtual-2323] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null

java.lang.NullPointerException: null

2026-06-09T17:04:45.439Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797461' blockGroupName='IT_T1'
2026-06-09T17:04:45.439Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:45.497Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797462' blockGroupName='IT_T2'
2026-06-09T17:04:45.497Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:45.598Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797471' blockGroupName='IT_T1'
2026-06-09T17:04:45.598Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:45.683Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797472' blockGroupName='IT_T2'
2026-06-09T17:04:45.683Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:45.757Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797451' blockGroupName='IT_T1'
2026-06-09T17:04:45.758Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:45.844Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797452' blockGroupName='IT_T2'
2026-06-09T17:04:45.844Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:46.055Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797482' blockGroupName='IT_T2'
2026-06-09T17:04:46.055Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:47.506Z ERROR 1 --- [igaming-source-betcity] [   virtual-2321] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null

java.lang.NullPointerException: null

2026-06-09T17:04:47.540Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 betcity matches
2026-06-09T17:04:48.862Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2326] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767810051' blockGroupName='IT_T1'
2026-06-09T17:04:48.863Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2326] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:48.914Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801971' blockGroupName='IT_T1'
2026-06-09T17:04:48.914Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:48.976Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801972' blockGroupName='IT_T2'
2026-06-09T17:04:48.976Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:49.066Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801981' blockGroupName='IT_T1'
2026-06-09T17:04:49.067Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:49.122Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801982' blockGroupName='IT_T2'
2026-06-09T17:04:49.122Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:49.350Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801991' blockGroupName='IT_T1'
2026-06-09T17:04:49.350Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:49.414Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801992' blockGroupName='IT_T2'
2026-06-09T17:04:49.414Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:49.546Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767802001' blockGroupName='IT_T1'
2026-06-09T17:04:49.546Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:49.693Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767802002' blockGroupName='IT_T2'
2026-06-09T17:04:49.693Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2325] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:50.270Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 betcity matches
2026-06-09T17:04:51.684Z ERROR 1 --- [igaming-source-betcity] [   virtual-2328] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null

java.lang.NullPointerException: null

2026-06-09T17:04:51.749Z ERROR 1 --- [igaming-source-betcity] [   virtual-2330] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null

java.lang.NullPointerException: null

2026-06-09T17:04:51.764Z ERROR 1 --- [igaming-source-betcity] [   virtual-2329] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null

java.lang.NullPointerException: null


```

---

### 🗂️ Букмекер: `betcity-com`

#### 🕷️ Crawler (`igaming-source-betcity-com-crawler-6d574669c8-gdpd8` - Running)
**Анализ ошибок:**
```text
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

2026-06-09T17:04:37.260Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting scheduled LIVE fetch job
2026-06-09T17:04:37.261Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.b.service.BetCityApiClient       : Fetching LIVE from https://ad.betsbc.com/d/on_air/bets?rev=8&add=dep_event&template=1
2026-06-09T17:04:37.263Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.b.service.BetCityApiClient       : Error fetching line info

org.springframework.web.client.ResourceAccessException: I/O error on GET request for "https://ad.betsbc.com/d/on_air/bets": ad.betsbc.com
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


```
#### 📥 Loader (`igaming-source-betcity-com-loader-6b4664cc66-zltzp` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```

---

### 🗂️ Букмекер: `betm`

#### 🕷️ Crawler (`igaming-source-betm-crawler-5bc75456f7-87f8z` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:03:33.257Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1290 (service: igaming-source-betcity, host: ad.betcity.ru)
2026-06-09T17:03:34.103Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23179224: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:03:36.823Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23249404: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:03:37.089Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14765785981' blockGroupName='IT_T1'
2026-06-09T17:03:37.089Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:03:37.131Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14765785982' blockGroupName='IT_T2'
2026-06-09T17:03:37.131Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:03:37.328Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : [betm] Discovered 206 live events
2026-06-09T17:03:37.328Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.b.scheduler.MatchFetchScheduler  : Finished LIVE discovery job, discovered: 206
2026-06-09T17:03:42.209Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='betm' name='Betcity'
2026-06-09T17:04:33.258Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ad.betcity.ru' is still reachable...
2026-06-09T17:04:35.745Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1290 (service: igaming-source-betcity, host: ad.betcity.ru)
2026-06-09T17:04:37.328Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.b.scheduler.MatchFetchScheduler  : Starting scheduled LIVE fetch job
2026-06-09T17:04:37.330Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.b.service.BetCityApiClient       : Fetching LIVE from https://ad.betcity.ru/d/on_air/bets?rev=8&add=dep_event&template=1
2026-06-09T17:04:42.661Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14762760231' blockGroupName='IT_T1'
2026-06-09T17:04:42.661Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:42.819Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14762760232' blockGroupName='IT_T2'
2026-06-09T17:04:42.819Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:42.884Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14762760241' blockGroupName='IT_T1'
2026-06-09T17:04:42.884Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:42.942Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14762760242' blockGroupName='IT_T2'
2026-06-09T17:04:42.942Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:43.045Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14762760251' blockGroupName='IT_T1'
2026-06-09T17:04:43.045Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:43.133Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14762760252' blockGroupName='IT_T2'
2026-06-09T17:04:43.133Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:43.188Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14762760262' blockGroupName='IT_T2'
2026-06-09T17:04:43.188Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:44.275Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23234925: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:44.835Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795191' blockGroupName='IT_T1'
2026-06-09T17:04:44.835Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:45.036Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795192' blockGroupName='IT_T2'
2026-06-09T17:04:45.036Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:45.099Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795201' blockGroupName='IT_T1'
2026-06-09T17:04:45.099Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:45.163Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795202' blockGroupName='IT_T2'
2026-06-09T17:04:45.164Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:45.225Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795211' blockGroupName='IT_T1'
2026-06-09T17:04:45.225Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:45.292Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795212' blockGroupName='IT_T2'
2026-06-09T17:04:45.293Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:45.351Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795222' blockGroupName='IT_T2'
2026-06-09T17:04:45.351Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:47.065Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 21681590: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:47.681Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797461' blockGroupName='IT_T1'
2026-06-09T17:04:47.681Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:47.742Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797462' blockGroupName='IT_T2'
2026-06-09T17:04:47.742Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:47.815Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797471' blockGroupName='IT_T1'
2026-06-09T17:04:47.815Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:47.926Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797472' blockGroupName='IT_T2'
2026-06-09T17:04:47.926Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:47.981Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797451' blockGroupName='IT_T1'
2026-06-09T17:04:47.981Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:48.038Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797452' blockGroupName='IT_T2'
2026-06-09T17:04:48.038Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:48.091Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797482' blockGroupName='IT_T2'
2026-06-09T17:04:48.091Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:49.131Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 22203780: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:49.839Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 22203699: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:50.289Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 22203700: java.lang.NullPointerException

java.lang.NullPointerException: null

2026-06-09T17:04:50.598Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801971' blockGroupName='IT_T1'
2026-06-09T17:04:50.599Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:50.735Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801972' blockGroupName='IT_T2'
2026-06-09T17:04:50.735Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:50.790Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801981' blockGroupName='IT_T1'
2026-06-09T17:04:50.791Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:50.849Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801982' blockGroupName='IT_T2'
2026-06-09T17:04:50.850Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:50.907Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801991' blockGroupName='IT_T1'
2026-06-09T17:04:50.907Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:50.965Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767801992' blockGroupName='IT_T2'
2026-06-09T17:04:50.966Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:51.092Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767802001' blockGroupName='IT_T1'
2026-06-09T17:04:51.092Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:51.290Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='УГЛ. Индивидуальный тотал' dataKey='14767802002' blockGroupName='IT_T2'
2026-06-09T17:04:51.290Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='УГЛ. Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:52.169Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767810051' blockGroupName='IT_T1'
2026-06-09T17:04:52.169Z DEBUG 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]

```
#### 📥 Loader (`igaming-source-betm-loader-788bcfc94b-cnkvf` - Running)
**Анализ ошибок:**
```text
java.lang.NullPointerException: null

2026-06-09T17:03:22.107Z ERROR 1 --- [igaming-source-betcity] [   virtual-2297] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: null

java.lang.NullPointerException: null

2026-06-09T17:03:22.139Z ERROR 1 --- [igaming-source-betcity] [   virtual-2296] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: null

java.lang.NullPointerException: null

2026-06-09T17:03:23.317Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14779342901' blockGroupName='IT_T1'
2026-06-09T17:03:23.317Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:03:23.326Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766125121' blockGroupName='IT_T1'
2026-06-09T17:03:23.327Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:03:23.353Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766125122' blockGroupName='IT_T2'
2026-06-09T17:03:23.353Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:03:23.381Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766125131' blockGroupName='IT_T1'
2026-06-09T17:03:23.381Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:03:23.383Z ERROR 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305452, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.76, ExistingName='Tm', NewName='Tm'
2026-06-09T17:03:23.385Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14779342902' blockGroupName='IT_T2'
2026-06-09T17:03:23.385Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:03:23.399Z ERROR 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305452, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.95, ExistingName='Tb', NewName='Tb'
2026-06-09T17:03:23.413Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14779342931' blockGroupName='IT_T1'
2026-06-09T17:03:23.413Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:03:23.413Z ERROR 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23317613, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.95, NewValue=1.8, ExistingName='Tm', NewName='Tm'
2026-06-09T17:03:23.413Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14766125132' blockGroupName='IT_T2'
2026-06-09T17:03:23.413Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:03:23.413Z ERROR 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305452, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.85, NewValue=1.76, ExistingName='Tm', NewName='Tm'
2026-06-09T17:03:23.440Z ERROR 1 --- [igaming-source-betcity] [   virtual-2299] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305452, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.85, NewValue=1.95, ExistingName='Tb', NewName='Tb'
2026-06-09T17:03:23.441Z ERROR 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23317613, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.76, NewValue=1.9, ExistingName='Tb', NewName='Tb'
2026-06-09T17:03:23.466Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14779342932' blockGroupName='IT_T2'
2026-06-09T17:03:23.466Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:03:23.466Z ERROR 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23317613, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.95, NewValue=1.85, ExistingName='Tm', NewName='Tm'
2026-06-09T17:03:23.485Z ERROR 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23317613, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.76, NewValue=1.85, ExistingName='Tb', NewName='Tb'
2026-06-09T17:03:23.575Z ERROR 1 --- [igaming-source-betcity] [   virtual-2299] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: null

java.lang.NullPointerException: null

2026-06-09T17:03:25.586Z ERROR 1 --- [igaming-source-betcity] [   virtual-2298] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23317613, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=2.08, NewValue=3.26, ExistingName='X2', NewName='X2'
2026-06-09T17:03:26.633Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 1 betm matches
2026-06-09T17:03:28.950Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 betm matches
2026-06-09T17:03:31.260Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 4 betm matches
2026-06-09T17:03:33.327Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 5 betm matches
2026-06-09T17:03:36.245Z ERROR 1 --- [igaming-source-betcity] [   virtual-2311] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: null

java.lang.NullPointerException: null

2026-06-09T17:03:37.604Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2316] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14765785981' blockGroupName='IT_T1'
2026-06-09T17:03:37.604Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2316] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:03:37.630Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2316] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14765785982' blockGroupName='IT_T2'
2026-06-09T17:03:37.630Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2316] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:03:39.101Z ERROR 1 --- [igaming-source-betcity] [   virtual-2315] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: null

java.lang.NullPointerException: null

2026-06-09T17:03:39.138Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 4 betm matches
2026-06-09T17:03:47.069Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ad.betcity.ru' is still reachable...
2026-06-09T17:03:52.042Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-betcity, host: ad.betcity.ru)
2026-06-09T17:04:48.812Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795191' blockGroupName='IT_T1'
2026-06-09T17:04:48.812Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:48.868Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795192' blockGroupName='IT_T2'
2026-06-09T17:04:48.868Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:48.921Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795201' blockGroupName='IT_T1'
2026-06-09T17:04:48.921Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:48.974Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795202' blockGroupName='IT_T2'
2026-06-09T17:04:48.974Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:49.103Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795211' blockGroupName='IT_T1'
2026-06-09T17:04:49.103Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:49.157Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795212' blockGroupName='IT_T2'
2026-06-09T17:04:49.157Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:49.345Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767795222' blockGroupName='IT_T2'
2026-06-09T17:04:49.345Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2318] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:50.563Z ERROR 1 --- [igaming-source-betcity] [   virtual-2318] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: null

java.lang.NullPointerException: null

2026-06-09T17:04:50.591Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-1] .c.e.AbstractBookmakerPerformanceTracker : [betm] Performance Report - Pages processed: 16, Skipped: 0, Not found: 0, Known errors: {}
2026-06-09T17:04:52.011Z ERROR 1 --- [igaming-source-betcity] [   virtual-2323] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: null

java.lang.NullPointerException: null

2026-06-09T17:04:52.023Z ERROR 1 --- [igaming-source-betcity] [   virtual-2324] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: null

java.lang.NullPointerException: null

2026-06-09T17:04:52.043Z  INFO 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ad.betcity.ru' is still reachable...
2026-06-09T17:04:52.050Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797461' blockGroupName='IT_T1'
2026-06-09T17:04:52.050Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:52.105Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797462' blockGroupName='IT_T2'
2026-06-09T17:04:52.105Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:52.172Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797471' blockGroupName='IT_T1'
2026-06-09T17:04:52.172Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:52.241Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797472' blockGroupName='IT_T2'
2026-06-09T17:04:52.241Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:52.314Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797451' blockGroupName='IT_T1'
2026-06-09T17:04:52.314Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 1' params=[id_bet, Tot]
2026-06-09T17:04:52.369Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797452' blockGroupName='IT_T2'
2026-06-09T17:04:52.369Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]
2026-06-09T17:04:52.464Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] sectionName='Индивидуальный тотал' dataKey='14767797482' blockGroupName='IT_T2'
2026-06-09T17:04:52.464Z DEBUG 1 --- [igaming-source-betcity] [   virtual-2321] p.d.i.s.c.e.b.s.BetcityOddsProcessor     : [INDIV-DEBUG] → enrichedGroupName='Индивидуальный тотал 2' params=[id_bet, Tot]

```

---

### 🗂️ Букмекер: `bettery`

#### 🕷️ Crawler (`igaming-source-bettery-crawler-6dd8bc8c49-9ppjn` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```
#### 📥 Loader (`igaming-source-bettery-loader-586b68f556-gdbkn` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
2026-06-09T17:04:06.802Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 bettery matches
2026-06-09T17:04:39.265Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'bettery.ru' is still reachable...
2026-06-09T17:04:40.445Z  INFO 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-bettery, host: bettery.ru)
```

---

### 🗂️ Букмекер: `dafabet`

#### 🕷️ Crawler (`igaming-source-dafabet-crawler-86b6b4488b-8b2ml` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:00:47.941Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=571, NEW=0, PENDING=0, PROCESSED=571, FAILED=0
2026-06-09T17:01:47.778Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=580, NEW=0, PENDING=0, PROCESSED=580, FAILED=0
2026-06-09T17:02:47.667Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=589, NEW=0, PENDING=0, PROCESSED=589, FAILED=0
2026-06-09T17:03:47.941Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=589, NEW=0, PENDING=0, PROCESSED=589, FAILED=0
2026-06-09T17:04:47.680Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=599, NEW=0, PENDING=0, PROCESSED=599, FAILED=0
```
**Последние строки логов:**
```text
2026-06-09T17:03:58.278Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-2] p.d.i.s.dafabet.service.MatchService     : Dafabet Sport Volleyball: Pushed 0 updates, 0 unchanged.
2026-06-09T17:03:58.278Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-2] p.d.i.s.dafabet.service.MatchService     : Dafabet line scraping completed.
2026-06-09T17:04:47.680Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=599, NEW=0, PENDING=0, PROCESSED=599, FAILED=0
```
#### 📥 Loader (`igaming-source-dafabet-loader-66b5ff58c7-wt65h` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:00:35.797Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=571, NEW=0, PENDING=0, PROCESSED=571, FAILED=0
2026-06-09T17:01:35.724Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=580, NEW=0, PENDING=0, PROCESSED=580, FAILED=0
2026-06-09T17:02:35.716Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=589, NEW=0, PENDING=0, PROCESSED=589, FAILED=0
2026-06-09T17:03:35.770Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=589, NEW=0, PENDING=0, PROCESSED=589, FAILED=0
2026-06-09T17:04:35.826Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=599, NEW=0, PENDING=0, PROCESSED=599, FAILED=0
```
**Последние строки логов:**
```text
2026-06-09T17:04:06.045Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 10 dafabet matches
2026-06-09T17:04:35.826Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=599, NEW=0, PENDING=0, PROCESSED=599, FAILED=0
2026-06-09T17:04:39.875Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='dafabet' name='Dafabet'
```

---

### 🗂️ Букмекер: `fansport`

#### 🕷️ Crawler (`igaming-source-fansport-crawler-6d7bdd4b99-4n8xt` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:55:05.897Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-4] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":1,"S2":
2026-06-09T16:55:24.626Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-4] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":276,"GVE":1,"HL":true,"RLI":
2026-06-09T17:00:39.017Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{},"PS":[{"Ke
2026-06-09T17:00:53.747Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":3547,"GVE":1,"HL":true,"RLI"
```
**Последние строки логов:**
```text
2026-06-09T17:03:51.390Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'fan-sport.com' is still reachable...
2026-06-09T17:03:59.191Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1104 (service: igaming-source-fansport, host: fan-sport.com)
2026-06-09T17:04:56.544Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='fansport' name='Xbet-family'
```
#### 📥 Loader (`igaming-source-fansport-loader-76b54fbdf-k8gkw` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```

---

### 🗂️ Букмекер: `leon`

#### 🕷️ Crawler (`igaming-source-leon-crawler-99b7d5d89-kc9qm` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:00:07.392Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4398, NEW=1, PENDING=1, PROCESSED=4393, FAILED=3
2026-06-09T17:01:07.348Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4400, NEW=19, PENDING=4, PROCESSED=4374, FAILED=3
2026-06-09T17:02:07.391Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4400, NEW=56, PENDING=2, PROCESSED=4339, FAILED=3
2026-06-09T17:03:07.335Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4411, NEW=45, PENDING=1, PROCESSED=4362, FAILED=3
2026-06-09T17:04:07.465Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4417, NEW=29, PENDING=3, PROCESSED=4381, FAILED=4
```
**Последние строки логов:**
```text
2026-06-09T17:04:09.292Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='leon' name='Leon'
2026-06-09T17:04:30.788Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T17:04:33.492Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 475 (service: igaming-source-leon, host: leon.ru)
```
#### 📥 Loader (`igaming-source-leon-loader-56c5c8d96d-9x4lc` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:00:36.452Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4400, NEW=4, PENDING=7, PROCESSED=4386, FAILED=3
2026-06-09T17:01:36.453Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4400, NEW=17, PENDING=2, PROCESSED=4378, FAILED=3
2026-06-09T17:02:36.608Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4401, NEW=66, PENDING=7, PROCESSED=4326, FAILED=3
2026-06-09T17:03:36.515Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4416, NEW=63, PENDING=1, PROCESSED=4348, FAILED=3
2026-06-09T17:04:36.413Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4418, NEW=1, PENDING=1, PROCESSED=4413, FAILED=4
```
**Последние строки логов:**
```text
2026-06-09T17:04:37.159Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='leon' name='Leon'
2026-06-09T17:04:38.262Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'leon.ru' is still reachable...
2026-06-09T17:04:40.852Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 898 (service: igaming-source-leon, host: leon.ru)
```

---

### 🗂️ Букмекер: `ligastavok`

#### 🕷️ Crawler (`igaming-source-ligastavok-crawler-df7487976-b5c7v` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:59:51.450Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__metaTags","__TSR_ROUTER__","__LS_CONTAINER_ONLY_HYDRATION__","fhConfig"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, __metaTags, IMask, enableReduxLogs, disableReduxLogs, __TSR_ROUTER__, __LS_CONTAINER_ONLY_HYDRATION__, changeTheme, fhConfig, fhContext
2026-06-09T17:02:00.554Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
2026-06-09T17:02:36.497Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__TSR_ROUTER__","__LS_CONTAINER_ONLY_HYDRATION__","__metaTags","fhConfig"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, IMask, enableReduxLogs, disableReduxLogs, __TSR_ROUTER__, __LS_CONTAINER_ONLY_HYDRATION__, __metaTags, changeTheme, fhConfig, fhContext
2026-06-09T17:04:38.473Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 503 (Service Temporarily Unavailable)
2026-06-09T17:04:45.494Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
```
**Последние строки логов:**
```text
2026-06-09T17:04:52.847Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokPageNavigator      : Performing human-like interactions...
2026-06-09T17:05:01.532Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'ligastavok.ru' is still reachable...
2026-06-09T17:05:01.779Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 2/3 to: https://www.ligastavok.ru/bets/live
```
#### 📥 Loader (`igaming-source-ligastavok-loader-7df96dc4b6-rlgl8` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```

---

### 🗂️ Букмекер: `linebet`

#### 🕷️ Crawler (`igaming-source-linebet-crawler-58754d5d7-jvs44` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:52:23.017Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":458,"GVE":1,"HL":true,"RLI":
2026-06-09T16:57:30.167Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":1,"S2":
2026-06-09T16:57:40.080Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":141,"GVE":1,"HL":true,"RLI":
2026-06-09T17:02:48.269Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{},"PS":[{"Ke
2026-06-09T17:03:00.525Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":3420,"GVE":1,"HL":true,"RLI"
```
**Последние строки логов:**
```text
2026-06-09T17:03:42.423Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T17:04:12.630Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T17:04:19.016Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-linebet, host: linebet.com)
```
#### 📥 Loader (`igaming-source-linebet-loader-8fc458bdb-tbxj2` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:39:54.544Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:40:01.673Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:41:01.675Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:41:07.737Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:41:40.926Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 linebet matches
2026-06-09T16:41:47.057Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 21 linebet matches
2026-06-09T16:41:53.818Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 27 linebet matches
2026-06-09T16:42:07.738Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:42:14.924Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:42:17.106Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T16:43:14.925Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:43:21.541Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:44:21.543Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:44:28.507Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:45:15.733Z  INFO 1 --- [igaming-source-linebet] [ebet-producer-1] org.apache.kafka.clients.NetworkClient   : [Producer clientId=igaming-source-linebet-producer-1] Node -1 disconnected.
2026-06-09T16:45:17.144Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T16:45:28.508Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:45:37.061Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:46:37.062Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:46:42.446Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:46:58.481Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 linebet matches
2026-06-09T16:47:05.538Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 21 linebet matches
2026-06-09T16:47:11.873Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 26 linebet matches
2026-06-09T16:47:42.446Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:47:58.974Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://linebet.com: Remote host terminated the handshake
2026-06-09T16:47:58.974Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'linebet.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T16:47:58.974Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.19.46 (config: vpngate-2i6). Requesting rotation...
2026-06-09T16:47:59.383Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported block for config 853 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:47:59.383Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T16:48:03.736Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.3.247' (config: vpngate-vpn654677818, ID: 970, healthy proxies: 53)
2026-06-09T16:48:03.736Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.3.247:3128
2026-06-09T16:48:03.736Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.3.247:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T16:48:03.805Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'linebet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T16:48:03.805Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'linebet.com'...
2026-06-09T16:48:03.851Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: linebet.com -> 92.223.84.84
2026-06-09T16:48:03.851Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://linebet.com'...
2026-06-09T16:48:10.409Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'linebet.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T16:48:10.621Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 970 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:48:17.184Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T16:49:03.805Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:49:04.840Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://linebet.com: Remote host terminated the handshake
2026-06-09T16:49:04.840Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'linebet.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T16:49:04.843Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.3.247 (config: vpngate-vpn654677818). Requesting rotation...
2026-06-09T16:49:05.086Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 970 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:49:05.088Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T16:49:07.602Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.3.150' (config: vpngate-patito, ID: 513, healthy proxies: 47)
2026-06-09T16:49:07.602Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.3.150:3128
2026-06-09T16:49:07.602Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.3.150:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T16:49:07.611Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'linebet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T16:49:07.611Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'linebet.com'...
2026-06-09T16:49:07.710Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: linebet.com -> 103.151.135.62
2026-06-09T16:49:07.711Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://linebet.com'...
2026-06-09T16:49:10.505Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'linebet.com' is reachable (HTTP 203). Proxy connectivity verified.
2026-06-09T16:49:10.831Z  INFO 1 --- [igaming-source-linebet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:50:07.610Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:50:10.105Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:51:10.110Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:51:12.463Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:51:17.202Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T16:52:12.464Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:52:15.758Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 20 linebet matches
2026-06-09T16:52:16.269Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:52:21.681Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 linebet matches
2026-06-09T16:52:27.544Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 24 linebet matches
2026-06-09T16:53:16.270Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:53:21.031Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:54:17.314Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T16:54:21.032Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:54:24.904Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:55:24.908Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:55:27.791Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:56:27.792Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:56:31.975Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:57:17.390Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T16:57:31.975Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:57:34.375Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:57:36.435Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 linebet matches
2026-06-09T16:57:42.231Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 linebet matches
2026-06-09T16:57:48.633Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 linebet matches
2026-06-09T16:57:53.867Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 13 linebet matches
2026-06-09T16:58:34.376Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:58:37.839Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T16:59:37.840Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T16:59:42.117Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T17:00:17.426Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T17:00:42.118Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T17:00:46.336Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T17:01:46.336Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T17:01:49.151Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T17:02:49.151Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T17:02:52.306Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T17:02:52.632Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 linebet matches
2026-06-09T17:02:58.658Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 linebet matches
2026-06-09T17:03:04.233Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 linebet matches
2026-06-09T17:03:09.859Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 20 linebet matches
2026-06-09T17:03:17.449Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='linebet' name='Xbet-family'
2026-06-09T17:03:52.307Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T17:03:59.318Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)
2026-06-09T17:04:59.318Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'linebet.com' is still reachable...
2026-06-09T17:05:02.688Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-linebet, host: linebet.com)

```

---

### 🗂️ Букмекер: `marathonbet`

#### 🕷️ Crawler (`igaming-source-marathonbet-crawler-76d777577f-5qnqk` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-06-09T17:00:48.015Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.m.MarathonbetApplication         : Starting MarathonbetApplication v0.0.1-SNAPSHOT using Java 21.0.10 with PID 1 (/app/app.jar started by root in /app)
2026-06-09T17:00:48.021Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.m.MarathonbetApplication         : The following 1 profile is active: "league-crawler"
2026-06-09T17:00:54.140Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T17:00:54.140Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-06-09T17:00:54.874Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 669 ms. Found 7 JPA repository interfaces.
2026-06-09T17:00:58.420Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T17:00:58.422Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T17:00:58.530Z  INFO 1 --- [igaming-source-marathonbet] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 32 ms. Found 0 Redis repository interfaces.
2026-06-09T17:00:59.878Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=05420846-e2d5-3736-9e5d-c2bf5a02614f
2026-06-09T17:01:03.479Z  WARN 1 --- [igaming-source-marathonbet] [           main] io.undertow.websockets.jsr               : UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used
2026-06-09T17:01:03.634Z  INFO 1 --- [igaming-source-marathonbet] [           main] io.undertow.servlet                      : Initializing Spring embedded WebApplicationContext
2026-06-09T17:01:03.638Z  INFO 1 --- [igaming-source-marathonbet] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 15257 ms
2026-06-09T17:01:08.353Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T17:01:09.217Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T17:01:09.412Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T17:01:18.517Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T17:01:19.214Z  INFO 1 --- [igaming-source-marathonbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T17:01:25.030Z  INFO 1 --- [igaming-source-marathonbet] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@5e976553
2026-06-09T17:01:25.034Z  INFO 1 --- [igaming-source-marathonbet] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T17:01:25.668Z  WARN 1 --- [igaming-source-marathonbet] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T17:01:25.912Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T17:01:31.822Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T17:01:32.886Z  INFO 1 --- [igaming-source-marathonbet] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T17:01:35.463Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T17:01:44.113Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T17:01:46.529Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.19.209' (config: vpngate-vpn572583473, ID: 580, healthy proxies: 45)
2026-06-09T17:01:46.530Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.19.209:3128
2026-06-09T17:01:46.531Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.19.209:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:01:46.538Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:01:46.538Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.ru'...
2026-06-09T17:01:46.580Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T17:01:46.832Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.ru -> 194.190.0.14
2026-06-09T17:01:46.832Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.ru'...
2026-06-09T17:01:48.070Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: baltbet-schema.json
2026-06-09T17:01:48.084Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: betcity-schema.json
2026-06-09T17:01:48.100Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: fonbet-schema.json
2026-06-09T17:01:48.156Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: olimpbet-schema.json
2026-06-09T17:01:48.166Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: sportbet-schema.json
2026-06-09T17:01:48.172Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.v.JsonValidationService        : Loaded JSON schema: zenit-schema.json
2026-06-09T17:01:50.537Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.ru' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T17:01:50.934Z  INFO 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T17:01:54.242Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-06-09T17:01:54.693Z  INFO 1 --- [igaming-source-marathonbet] [           main] io.undertow                              : starting server: Undertow - 2.3.18.Final
2026-06-09T17:01:54.768Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.xnio                                 : XNIO version 3.8.16.Final
2026-06-09T17:01:54.841Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.xnio.nio                             : XNIO NIO Implementation Version 3.8.16.Final
2026-06-09T17:01:55.518Z  INFO 1 --- [igaming-source-marathonbet] [           main] org.jboss.threads                        : JBoss Threads version 3.5.0.Final
2026-06-09T17:01:55.684Z  INFO 1 --- [igaming-source-marathonbet] [           main] o.s.b.w.e.undertow.UndertowWebServer     : Undertow started on port 3036 (http) with context path '/'
2026-06-09T17:01:55.869Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.m.MarathonbetApplication         : Started MarathonbetApplication in 70.612 seconds (process running for 75.324)
2026-06-09T17:01:55.882Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.s.BookmakerHeartbeatSender     : Sending startup heartbeat for bookmaker 'marathonbet' (display: 'Marathonbet')
2026-06-09T17:01:55.929Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.m.service.MarathonbetApiClient   : Fetching prematch menu from: https://www.marathonbet.ru/su/react/event/menu/prematch
2026-06-09T17:01:55.929Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Initializing Playwright instance... (attempt 1)
2026-06-09T17:01:55.981Z  INFO 1 --- [igaming-source-marathonbet] [           main] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet' name='Marathonbet'
2026-06-09T17:01:55.990Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet' name='Marathonbet'
Skipping browsers download because `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD` env variable is set
2026-06-09T17:02:00.454Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.19.209:3128
2026-06-09T17:02:08.336Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/react/event/menu/prematch: Object doesn't exist: frame@449f1611cfe0336feb002093e3226728
2026-06-09T17:02:08.337Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.m.s.MarathonDiscoveryService     : Cannot fetch prematch menu, aborting league queueing
2026-06-09T17:02:08.371Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://www.marathonbet.ru/su/betting/Tennis/ITF/Czech+Republic/Ceska+Lipa/Women+-+13806528
2026-06-09T17:02:08.773Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Tennis/ITF/Czech+Republic/Ceska+Lipa/Women+-+13806528: Cannot invoke "com.microsoft.playwright.impl.FrameImpl.navigateImpl(String, com.microsoft.playwright.Frame$NavigateOptions)" because "this.mainFrame" is null
2026-06-09T17:02:08.773Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content returned from browser for league: Чехия. Ческа-Липа. Женщины
2026-06-09T17:02:25.837Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T17:02:28.054Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T17:02:36.389Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://www.marathonbet.ru/su/betting/Volleyball/Internationals/Asian+Nations+Cup/Women/2026/Philippines+-+28931425
2026-06-09T17:02:37.558Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Кубок азиатских наций. Женщины. 2026. Филиппины. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:02:45.648Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Турция. Кайсери. Мужчины. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:02:57.357Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Турция. Кайсери. Женщины. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:03:12.049Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: CPBL. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:03:17.969Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Квинсленд. Премьер-лига 2. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:03:28.055Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T17:03:30.076Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T17:03:37.639Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Смешанные. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:03:52.244Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Италия. Сан-Грегорио-ди-Катания. Женщины. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:03:59.655Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Супер 500. Сидней, Австралия. Мужчины. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:04:26.283Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Супер 500. Сидней, Австралия. Женщины. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:04:30.082Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T17:04:32.158Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T17:04:52.948Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Лига Европы. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:04:56.054Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet' name='Marathonbet'
2026-06-09T17:04:57.862Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Женщины. Чемпионат. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:05:04.739Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Марокко. Касабланка. Женщины. HTML title: БК «Марафон» онлайн букмекер

```
#### 📥 Loader (`igaming-source-marathonbet-loader-6b7886bb67-wbkt8` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:59:36.966Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-747] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916203 (attempt 1)
2026-06-09T16:59:36.966Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-747] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+C/Group+Stage/Lithuania+Women+vs+Liechtenstein+Women+-+28916203: Playwright connection closed
2026-06-09T16:59:36.966Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-747] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916203 (attempt 2)
2026-06-09T16:59:36.966Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-748] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+C/Group+Stage/North+Macedonia+Women+vs+Azerbaijan+Women+-+28916178: Playwright connection closed
2026-06-09T16:59:36.966Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-748] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916178 (attempt 1)
2026-06-09T16:59:36.966Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-748] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+C/Group+Stage/North+Macedonia+Women+vs+Azerbaijan+Women+-+28916178: Playwright connection closed
2026-06-09T16:59:36.966Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-748] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916178 (attempt 2)
2026-06-09T16:59:36.966Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-749] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+C/Group+Stage/Croatia+Women+vs+Bulgaria+Women+-+28916189: Playwright connection closed
2026-06-09T16:59:36.966Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-749] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916189 (attempt 1)
2026-06-09T16:59:36.966Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-749] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+C/Group+Stage/Croatia+Women+vs+Bulgaria+Women+-+28916189: Playwright connection closed
2026-06-09T16:59:36.966Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-749] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916189 (attempt 2)
2026-06-09T16:59:36.967Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-750] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Northern+Ireland/Championship.+Women/St.+James%27+Swifts+Women+vs+Sion+Swifts+Women+-+29064812: Playwright connection closed
2026-06-09T16:59:36.967Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-750] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29064812 (attempt 1)
2026-06-09T16:59:36.967Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-750] p.d.i.s.m.service.MarathonbetApiClient   : Error fetching all markets for event 29064812 after 2 attempt(s): Playwright connection closed
2026-06-09T16:59:36.964Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-740] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+B/Group+Stage/Malta+Women+vs+Turkey+Women+-+28916253: Playwright connection closed
2026-06-09T16:59:36.967Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-740] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916253 (attempt 1)
2026-06-09T16:59:36.967Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-740] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+B/Group+Stage/Malta+Women+vs+Turkey+Women+-+28916253: Playwright connection closed
2026-06-09T16:59:36.967Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-740] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916253 (attempt 2)
2026-06-09T16:59:36.967Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-751] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/USA/WPSL.+Women/Pride+Women+vs+Junction+Women+-+29027695: Playwright connection closed
2026-06-09T16:59:36.967Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-751] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29027695 (attempt 1)
2026-06-09T16:59:36.967Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-751] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/USA/WPSL.+Women/Pride+Women+vs+Junction+Women+-+29027695: Playwright connection closed
2026-06-09T16:59:36.967Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-751] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29027695 (attempt 2)
2026-06-09T16:59:36.964Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-741] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+B/Group+Stage/Northern+Ireland+Women+vs+Switzerland+Women+-+28916251: Playwright connection closed
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-741] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916251 (attempt 1)
2026-06-09T16:59:36.968Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-741] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+B/Group+Stage/Northern+Ireland+Women+vs+Switzerland+Women+-+28916251: Playwright connection closed
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-741] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916251 (attempt 2)
2026-06-09T16:59:36.968Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-752] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/USA/WPSL.+Women/Salvo+Women+vs+Mankato+Women+-+29028813: Playwright connection closed
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-752] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29028813 (attempt 1)
2026-06-09T16:59:36.964Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-742] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+B/Group+Stage/Finland+Women+vs+Portugal+Women+-+28916241: Playwright connection closed
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-742] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916241 (attempt 1)
2026-06-09T16:59:36.968Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-753] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Guatemala+Women+vs+El+Salvador+Women+-+29034966: Playwright connection closed
2026-06-09T16:59:36.964Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-743] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+C/Group+Stage/Belarus+Women+vs+Armenia+Women+-+28916135: Playwright connection closed
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-743] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916135 (attempt 1)
2026-06-09T16:59:36.968Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-743] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+C/Group+Stage/Belarus+Women+vs+Armenia+Women+-+28916135: Playwright connection closed
2026-06-09T16:59:36.968Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-754] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Brazil+Women+vs+USA+Women+-+29034972: Playwright connection closed
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-754] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034972 (attempt 1)
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-743] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916135 (attempt 2)
2026-06-09T16:59:36.968Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-754] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Brazil+Women+vs+USA+Women+-+29034972: Playwright connection closed
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-754] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034972 (attempt 2)
2026-06-09T16:59:36.968Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-755] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Costa+Rica+Women+vs+Canada+Women+-+29034986: Playwright connection closed
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-755] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034986 (attempt 1)
2026-06-09T16:59:36.968Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-755] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Costa+Rica+Women+vs+Canada+Women+-+29034986: Playwright connection closed
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-755] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034986 (attempt 2)
2026-06-09T16:59:36.964Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-738] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+B/Group+Stage/Latvia+Women+vs+Slovakia+Women+-+28916243: Playwright connection closed
2026-06-09T16:59:36.969Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-738] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916243 (attempt 2)
2026-06-09T16:59:36.964Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-744] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+C/Group+Stage/Hungary+Women+vs+Andorra+Women+-+28916180: Playwright connection closed
2026-06-09T16:59:36.968Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-752] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/USA/WPSL.+Women/Salvo+Women+vs+Mankato+Women+-+29028813: Playwright connection closed
2026-06-09T16:59:36.969Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-744] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916180 (attempt 2)
2026-06-09T16:59:36.969Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-752] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29028813 (attempt 2)
2026-06-09T16:59:36.968Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-742] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Internationals/FIFA+World+Cup.+Women/2027/Qualification/Europe/League+B/Group+Stage/Finland+Women+vs+Portugal+Women+-+28916241: Playwright connection closed
2026-06-09T16:59:36.968Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-753] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034966 (attempt 1)
2026-06-09T16:59:36.969Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-742] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28916241 (attempt 2)
2026-06-09T16:59:36.969Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-753] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Guatemala+Women+vs+El+Salvador+Women+-+29034966: Playwright connection closed
2026-06-09T16:59:36.969Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-753] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034966 (attempt 2)
2026-06-09T16:59:37.218Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 18 marathonbet matches
2026-06-09T16:59:47.454Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-757] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Guatemala+Women+vs+El+Salvador+Women+-+29034966: Playwright connection closed
2026-06-09T16:59:47.454Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-757] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034966 (attempt 1)
2026-06-09T16:59:47.454Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-757] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Guatemala+Women+vs+El+Salvador+Women+-+29034966: Playwright connection closed
2026-06-09T16:59:47.454Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-757] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034966 (attempt 2)
2026-06-09T16:59:47.454Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-758] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Brazil+Women+vs+USA+Women+-+29034972: Playwright connection closed
2026-06-09T16:59:47.454Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-758] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034972 (attempt 1)
2026-06-09T16:59:47.455Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-758] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Brazil+Women+vs+USA+Women+-+29034972: Playwright connection closed
2026-06-09T16:59:47.455Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-758] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034972 (attempt 2)
2026-06-09T16:59:47.455Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-759] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Costa+Rica+Women+vs+Canada+Women+-+29034986: Playwright connection closed
2026-06-09T16:59:47.455Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-759] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034986 (attempt 1)
2026-06-09T16:59:47.455Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-759] p.d.i.s.m.service.MarathonbetApiClient   : Error fetching all markets for event 29034986 after 2 attempt(s): Playwright connection closed
2026-06-09T16:59:47.648Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 3 marathonbet matches
2026-06-09T17:00:28.891Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T17:00:33.402Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1016 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T17:01:33.402Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T17:01:37.504Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1016 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T17:02:15.643Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet' name='Marathonbet'
2026-06-09T17:02:37.504Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T17:02:41.900Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1016 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T17:03:41.900Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T17:03:48.431Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1016 (service: igaming-source-marathonbet, host: www.marathonbet.ru)
2026-06-09T17:04:27.324Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-772] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Friendly+Tournaments/Mixed/France/Group+Stage/Congo+DR+U-20+vs+Saudi+Arabia+U-23+-+29034716: Playwright connection closed
2026-06-09T17:04:27.325Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-772] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034716 (attempt 1)
2026-06-09T17:04:27.325Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-772] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Friendly+Tournaments/Mixed/France/Group+Stage/Congo+DR+U-20+vs+Saudi+Arabia+U-23+-+29034716: Playwright connection closed
2026-06-09T17:04:27.325Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-772] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034716 (attempt 2)
2026-06-09T17:04:27.326Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-774] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Friendly+Tournaments/Mixed/France/Group+Stage/Colombia+U-19+vs+Tunisia+U-23+-+29034720: Playwright connection closed
2026-06-09T17:04:27.326Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-774] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034720 (attempt 1)
2026-06-09T17:04:27.326Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-774] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Friendly+Tournaments/Mixed/France/Group+Stage/Colombia+U-19+vs+Tunisia+U-23+-+29034720: Playwright connection closed
2026-06-09T17:04:27.326Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-774] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034720 (attempt 2)
2026-06-09T17:04:27.441Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 2 marathonbet matches
2026-06-09T17:04:42.648Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-777] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Guatemala+Women+vs+El+Salvador+Women+-+29034966: Playwright connection closed
2026-06-09T17:04:42.649Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-777] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034966 (attempt 1)
2026-06-09T17:04:42.649Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-777] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Guatemala+Women+vs+El+Salvador+Women+-+29034966: Playwright connection closed
2026-06-09T17:04:42.649Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-777] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034966 (attempt 2)
2026-06-09T17:04:42.649Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-778] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Brazil+Women+vs+USA+Women+-+29034972: Playwright connection closed
2026-06-09T17:04:42.649Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-778] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034972 (attempt 1)
2026-06-09T17:04:42.649Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-778] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Brazil+Women+vs+USA+Women+-+29034972: Playwright connection closed
2026-06-09T17:04:42.649Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-778] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034972 (attempt 2)
2026-06-09T17:04:42.649Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-779] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Costa+Rica+Women+vs+Canada+Women+-+29034986: Playwright connection closed
2026-06-09T17:04:42.649Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-779] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034986 (attempt 1)
2026-06-09T17:04:42.649Z ERROR 1 --- [igaming-source-marathonbet] [    virtual-779] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Football/Women/Friendlies/Internationals.+Women/Costa+Rica+Women+vs+Canada+Women+-+29034986: Playwright connection closed
2026-06-09T17:04:42.649Z  WARN 1 --- [igaming-source-marathonbet] [    virtual-779] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29034986 (attempt 2)
2026-06-09T17:04:42.958Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 3 marathonbet matches
2026-06-09T17:04:48.431Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.ru' is still reachable...
2026-06-09T17:04:53.191Z  INFO 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1016 (service: igaming-source-marathonbet, host: www.marathonbet.ru)

```

---

### 🗂️ Букмекер: `marathonbet-by`

#### 🕷️ Crawler (`igaming-source-marathonbet-by-crawler-6f696db68d-rdjhj` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
2026-06-09T17:04:42.599Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-2] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Мужчины. LKL. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:04:46.194Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-2] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: Сборные до 19 лет. HTML title: БК «Марафон» онлайн букмекер
2026-06-09T17:04:54.799Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-2] p.d.i.s.m.service.MarathonHtmlParser     : No match rows found with [data-event-treeid] in league: 2-я лига. HTML title: БК «Марафон» онлайн букмекер
```
#### 📥 Loader (`igaming-source-marathonbet-by-loader-7b754469b7-k9p9d` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:03:30.639Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-902] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895210 (attempt 1)
2026-06-09T17:03:30.639Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-902] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/South+Africa+vs+Korea+Republic+-+25895210: Playwright connection closed
2026-06-09T17:03:30.639Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-902] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895210 (attempt 2)
2026-06-09T17:03:30.638Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-898] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Switzerland+vs+Canada+-+25895221: Playwright connection closed
2026-06-09T17:03:30.639Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-898] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895221 (attempt 1)
2026-06-09T17:03:30.639Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-898] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Switzerland+vs+Canada+-+25895221: Playwright connection closed
2026-06-09T17:03:30.639Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-903] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Curacao+vs+Cote+d%27Ivoire+-+25895265: Playwright connection closed
2026-06-09T17:03:30.639Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-898] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895221 (attempt 2)
2026-06-09T17:03:30.639Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-903] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895265 (attempt 1)
2026-06-09T17:03:30.638Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-896] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 27817108 (attempt 1)
2026-06-09T17:03:30.639Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-903] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Curacao+vs+Cote+d%27Ivoire+-+25895265: Playwright connection closed
2026-06-09T17:03:30.639Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-903] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895265 (attempt 2)
2026-06-09T17:03:30.639Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-896] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Colombia+vs+Congo+DR+-+27817108: Playwright connection closed
2026-06-09T17:03:30.639Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-896] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 27817108 (attempt 2)
2026-06-09T17:03:30.639Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-901] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Czech+Republic+vs+Mexico+-+27813518: Playwright connection closed
2026-06-09T17:03:30.639Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-904] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Ecuador+vs+Germany+-+25895262: Playwright connection closed
2026-06-09T17:03:30.639Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-904] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895262 (attempt 1)
2026-06-09T17:03:30.640Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-905] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Tunisia+vs+Netherlands+-+25895351: Playwright connection closed
2026-06-09T17:03:30.639Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-901] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 27813518 (attempt 1)
2026-06-09T17:03:30.640Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-905] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895351 (attempt 1)
2026-06-09T17:03:30.640Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-905] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Tunisia+vs+Netherlands+-+25895351: Playwright connection closed
2026-06-09T17:03:30.640Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-905] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895351 (attempt 2)
2026-06-09T17:03:30.640Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-901] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Czech+Republic+vs+Mexico+-+27813518: Playwright connection closed
2026-06-09T17:03:30.640Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-901] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 27813518 (attempt 2)
2026-06-09T17:03:30.650Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-906] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Japan+vs+Sweden+-+27813080: Playwright connection closed
2026-06-09T17:03:30.652Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-906] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 27813080 (attempt 1)
2026-06-09T17:03:30.653Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-906] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Japan+vs+Sweden+-+27813080: Playwright connection closed
2026-06-09T17:03:30.653Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-906] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 27813080 (attempt 2)
2026-06-09T17:03:30.654Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-907] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Paraguay+vs+Australia+-+25895250: Playwright connection closed
2026-06-09T17:03:30.654Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-907] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895250 (attempt 1)
2026-06-09T17:03:30.654Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-907] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Paraguay+vs+Australia+-+25895250: Playwright connection closed
2026-06-09T17:03:30.654Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-907] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895250 (attempt 2)
2026-06-09T17:03:30.654Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-908] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Turkey+vs+USA+-+27813076: Playwright connection closed
2026-06-09T17:03:30.654Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-908] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 27813076 (attempt 1)
2026-06-09T17:03:30.640Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-904] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Ecuador+vs+Germany+-+25895262: Playwright connection closed
2026-06-09T17:03:30.655Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-904] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895262 (attempt 2)
2026-06-09T17:03:30.655Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-908] p.d.i.s.m.service.MarathonbetApiClient   : Error fetching all markets for event 27813076 after 2 attempt(s): Playwright connection closed
2026-06-09T17:03:31.379Z  INFO 1 --- [igaming-source-marathonbet-by] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 marathonbet-by matches
2026-06-09T17:03:36.674Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-910] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Norway+vs+France+-+25895428: Playwright connection closed
2026-06-09T17:03:36.674Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-910] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895428 (attempt 1)
2026-06-09T17:03:36.674Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-910] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Norway+vs+France+-+25895428: Playwright connection closed
2026-06-09T17:03:36.674Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-910] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895428 (attempt 2)
2026-06-09T17:03:36.674Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-911] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Senegal+vs+Iraq+-+27817071: Playwright connection closed
2026-06-09T17:03:36.674Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-911] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 27817071 (attempt 1)
2026-06-09T17:03:36.674Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-911] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Senegal+vs+Iraq+-+27817071: Playwright connection closed
2026-06-09T17:03:36.674Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-911] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 27817071 (attempt 2)
2026-06-09T17:03:36.675Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-913] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Uruguay+vs+Spain+-+25895416: Playwright connection closed
2026-06-09T17:03:36.675Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-912] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Cape+Verde+vs+Saudi+Arabia+-+25895417: Playwright connection closed
2026-06-09T17:03:36.675Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-913] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895416 (attempt 1)
2026-06-09T17:03:36.675Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-912] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895417 (attempt 1)
2026-06-09T17:03:36.675Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-914] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Egypt+vs+Iran+-+25895388: Playwright connection closed
2026-06-09T17:03:36.675Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-914] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895388 (attempt 1)
2026-06-09T17:03:36.675Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-914] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Egypt+vs+Iran+-+25895388: Playwright connection closed
2026-06-09T17:03:36.675Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-914] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895388 (attempt 2)
2026-06-09T17:03:36.675Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-912] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Cape+Verde+vs+Saudi+Arabia+-+25895417: Playwright connection closed
2026-06-09T17:03:36.675Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-912] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895417 (attempt 2)
2026-06-09T17:03:36.675Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-915] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/New+Zealand+vs+Belgium+-+25895390: Playwright connection closed
2026-06-09T17:03:36.675Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-915] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895390 (attempt 1)
2026-06-09T17:03:36.675Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-915] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/New+Zealand+vs+Belgium+-+25895390: Playwright connection closed
2026-06-09T17:03:36.675Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-915] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895390 (attempt 2)
2026-06-09T17:03:36.676Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-916] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Panama+vs+England+-+25895480: Playwright connection closed
2026-06-09T17:03:36.676Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-916] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895480 (attempt 1)
2026-06-09T17:03:36.676Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-916] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Panama+vs+England+-+25895480: Playwright connection closed
2026-06-09T17:03:36.676Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-916] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895480 (attempt 2)
2026-06-09T17:03:36.675Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-913] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Uruguay+vs+Spain+-+25895416: Playwright connection closed
2026-06-09T17:03:36.676Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-913] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895416 (attempt 2)
2026-06-09T17:03:36.676Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-917] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Croatia+vs+Ghana+-+25895481: Playwright connection closed
2026-06-09T17:03:36.676Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-917] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895481 (attempt 1)
2026-06-09T17:03:36.676Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-917] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Croatia+vs+Ghana+-+25895481: Playwright connection closed
2026-06-09T17:03:36.676Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-917] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895481 (attempt 2)
2026-06-09T17:03:36.676Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-919] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Colombia+vs+Portugal+-+25895463: Playwright connection closed
2026-06-09T17:03:36.676Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-919] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895463 (attempt 1)
2026-06-09T17:03:36.676Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-919] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Colombia+vs+Portugal+-+25895463: Playwright connection closed
2026-06-09T17:03:36.676Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-919] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895463 (attempt 2)
2026-06-09T17:03:36.676Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-918] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Congo+DR+vs+Uzbekistan+-+27817107: Playwright connection closed
2026-06-09T17:03:36.677Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-918] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 27817107 (attempt 1)
2026-06-09T17:03:36.677Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-918] p.d.i.s.m.service.MarathonbetApiClient   : Error fetching all markets for event 27817107 after 2 attempt(s): Playwright connection closed
2026-06-09T17:03:36.677Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-921] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Jordan+vs+Argentina+-+25895449: Playwright connection closed
2026-06-09T17:03:36.678Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-921] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895449 (attempt 1)
2026-06-09T17:03:36.678Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-921] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Jordan+vs+Argentina+-+25895449: Playwright connection closed
2026-06-09T17:03:36.678Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-921] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895449 (attempt 2)
2026-06-09T17:03:36.678Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-920] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Algeria+vs+Austria+-+25895447: Playwright connection closed
2026-06-09T17:03:36.678Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-920] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895447 (attempt 1)
2026-06-09T17:03:36.678Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-920] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Algeria+vs+Austria+-+25895447: Playwright connection closed
2026-06-09T17:03:36.678Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-920] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 25895447 (attempt 2)
2026-06-09T17:03:37.348Z  INFO 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 12 marathonbet-by matches
2026-06-09T17:03:47.691Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-923] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Women/Friendlies/Internationals.+Women/Guatemala+Women+vs+El+Salvador+Women+-+29090777: Playwright connection closed
2026-06-09T17:03:47.691Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-923] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29090777 (attempt 1)
2026-06-09T17:03:47.691Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-924] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Women/Friendlies/Internationals.+Women/Brazil+Women+vs+USA+Women+-+29090783: Playwright connection closed
2026-06-09T17:03:47.691Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-923] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Women/Friendlies/Internationals.+Women/Guatemala+Women+vs+El+Salvador+Women+-+29090777: Playwright connection closed
2026-06-09T17:03:47.691Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-924] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29090783 (attempt 1)
2026-06-09T17:03:47.691Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-923] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29090777 (attempt 2)
2026-06-09T17:03:47.691Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-924] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Women/Friendlies/Internationals.+Women/Brazil+Women+vs+USA+Women+-+29090783: Playwright connection closed
2026-06-09T17:03:47.691Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-924] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29090783 (attempt 2)
2026-06-09T17:03:47.691Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-925] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Women/Friendlies/Internationals.+Women/Costa+Rica+Women+vs+Canada+Women+-+29090797: Playwright connection closed
2026-06-09T17:03:47.691Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-925] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29090797 (attempt 1)
2026-06-09T17:03:47.691Z ERROR 1 --- [igaming-source-marathonbet-by] [    virtual-925] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Women/Friendlies/Internationals.+Women/Costa+Rica+Women+vs+Canada+Women+-+29090797: Playwright connection closed
2026-06-09T17:03:47.691Z  WARN 1 --- [igaming-source-marathonbet-by] [    virtual-925] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29090797 (attempt 2)
2026-06-09T17:03:48.063Z  INFO 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 3 marathonbet-by matches
2026-06-09T17:04:30.490Z  INFO 1 --- [igaming-source-marathonbet-by] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-by' name='Marathonbet'

```

---

### 🗂️ Букмекер: `marathonbet-com`

#### 🕷️ Crawler (`igaming-source-marathonbet-com-crawler-548458c88-lz7mq` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:02:58.209Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'www.marathonbet.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-09T17:02:58.209Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T17:02:59.691Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.2.105' (config: vpngate-vpn416596627, ID: 415, healthy proxies: 32)
2026-06-09T17:02:59.691Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.2.105:3128
2026-06-09T17:02:59.691Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.2.105:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:02:59.692Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:02:59.692Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T17:02:59.692Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.com -> 104.17.119.85
2026-06-09T17:02:59.692Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T17:02:59.707Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Basketball/Philippines/Men/Pre-season+Tournament+-+1863953: Blocked by Cloudflare/security system
2026-06-09T17:02:59.707Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content returned from browser for league: Мужчины. Предсезонный турнир
2026-06-09T17:02:59.756Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.7.138:3128 to http://10.42.2.105:3128. Recreating browser...
2026-06-09T17:03:00.004Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.2.105:3128
2026-06-09T17:03:03.209Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T17:03:03.210Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] DNS resolved: www.marathonbet.com -> 104.17.119.85
2026-06-09T17:03:03.210Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T17:03:06.998Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T17:03:07.091Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 415 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:03:14.938Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T17:03:15.530Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 415 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:03:31.702Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://www.marathonbet.com/su/betting/Football/Friendlies/Internationals+U-23+-+501875. Rotating proxy.
2026-06-09T17:03:31.702Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.2.105 (config: vpngate-vpn416596627). Requesting rotation...
2026-06-09T17:03:31.836Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 415 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:03:31.836Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T17:03:32.861Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.19.99' (config: vpngate-vpn332519676, ID: 444, healthy proxies: 39)
2026-06-09T17:03:32.861Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.19.99:3128
2026-06-09T17:03:32.861Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.19.99:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:03:32.862Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:03:32.862Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T17:03:32.870Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Friendlies/Internationals+U-23+-+501875: Blocked by Cloudflare/security system
2026-06-09T17:03:32.870Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content returned from browser for league: Сборные до 23 лет
2026-06-09T17:03:33.111Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.com -> 104.17.120.85
2026-06-09T17:03:33.111Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T17:03:34.954Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T17:03:35.142Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:03:38.087Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.2.105:3128 to http://10.42.19.99:3128. Recreating browser...
2026-06-09T17:03:38.314Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.19.99:3128
2026-06-09T17:03:51.745Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://www.marathonbet.com/su/betting/e-Sports/Overwatch/Champions+Series+-+18494483. Rotating proxy.
2026-06-09T17:03:51.746Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.19.99 (config: vpngate-vpn332519676). Requesting rotation...
2026-06-09T17:03:52.115Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T17:03:54.060Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported block for config 444 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:03:54.060Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T17:03:55.273Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 444 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:03:57.286Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.7.139' (config: vpngate-vpn367079905, ID: 1290, healthy proxies: 43)
2026-06-09T17:03:57.286Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.7.139:3128
2026-06-09T17:03:57.286Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.7.139:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:03:57.287Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:03:57.287Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T17:03:57.287Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.com -> 104.17.120.85
2026-06-09T17:03:57.287Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T17:03:57.330Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/e-Sports/Overwatch/Champions+Series+-+18494483: Blocked by Cloudflare/security system
2026-06-09T17:03:57.330Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content returned from browser for league: Champions Series
2026-06-09T17:03:57.473Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.19.99:3128 to http://10.42.7.139:3128. Recreating browser...
2026-06-09T17:03:57.614Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.7.139:3128
2026-06-09T17:04:00.265Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T17:04:01.797Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1290 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:04:15.648Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://www.marathonbet.com/su/betting/Badminton/World+Tour/Super+500/Sydney%2C+Australia/Men+-+16452538. Rotating proxy.
2026-06-09T17:04:15.648Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.7.139 (config: vpngate-vpn367079905). Requesting rotation...
2026-06-09T17:04:15.728Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported block for config 1290 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:04:15.729Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T17:04:17.827Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.19.209' (config: vpngate-vpn572583473, ID: 580, healthy proxies: 47)
2026-06-09T17:04:17.827Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.19.209:3128
2026-06-09T17:04:17.827Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.19.209:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:04:17.827Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:04:17.827Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T17:04:17.862Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Badminton/World+Tour/Super+500/Sydney%2C+Australia/Men+-+16452538: Blocked by Cloudflare/security system
2026-06-09T17:04:17.862Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content returned from browser for league: Супер 500. Сидней, Австралия. Мужчины
2026-06-09T17:04:17.966Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.7.139:3128 to http://10.42.19.209:3128. Recreating browser...
2026-06-09T17:04:18.105Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.19.209:3128
2026-06-09T17:04:18.210Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.com -> 104.17.119.85
2026-06-09T17:04:18.210Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T17:04:20.601Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T17:04:20.916Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 580 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:04:29.288Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://www.marathonbet.com/su/betting/Badminton/World+Tour/Super+500/Sydney%2C+Australia/Women+-+16452537. Rotating proxy.
2026-06-09T17:04:29.288Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.19.209 (config: vpngate-vpn572583473). Requesting rotation...
2026-06-09T17:04:29.648Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported block for config 580 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:04:29.648Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T17:04:32.800Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.7.89' (config: vpngate-vpn563702141, ID: 931, healthy proxies: 44)
2026-06-09T17:04:32.800Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.7.89:3128
2026-06-09T17:04:32.801Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.7.89:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:04:32.801Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'www.marathonbet.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:04:32.801Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'www.marathonbet.com'...
2026-06-09T17:04:32.801Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: www.marathonbet.com -> 104.17.119.85
2026-06-09T17:04:32.802Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://www.marathonbet.com'...
2026-06-09T17:04:32.829Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Badminton/World+Tour/Super+500/Sydney%2C+Australia/Women+-+16452537: Blocked by Cloudflare/security system
2026-06-09T17:04:32.830Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content returned from browser for league: Супер 500. Сидней, Австралия. Женщины
2026-06-09T17:04:37.246Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'www.marathonbet.com' is reachable (HTTP 200). Proxy connectivity verified.
2026-06-09T17:04:37.470Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T17:04:37.546Z  INFO 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 931 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:04:37.974Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Proxy changed from http://10.42.19.209:3128 to http://10.42.7.89:3128. Recreating browser...
2026-06-09T17:04:38.119Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserService      : Launching browser with profile: HEADLESS_STEALTH and proxy: http://10.42.7.89:3128
2026-06-09T17:04:50.943Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.m.service.MarathonbetApiClient   : Fetching prematch menu from: https://www.marathonbet.com/su/react/event/menu/prematch
2026-06-09T17:04:52.609Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.m.service.MarathonbetApiClient   : Fetched prematch menu with 29 sports
2026-06-09T17:04:52.610Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.m.service.MarathonbetApiClient   : Collected 296 league nodes from 24 sports (no limits)
2026-06-09T17:04:55.274Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T17:04:58.901Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 931 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:04:59.806Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Potential block detected in content of https://www.marathonbet.com/su/betting/Cricket/ODI/3rd+ODI+-+3179559. Rotating proxy.
2026-06-09T17:04:59.810Z  WARN 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.7.89 (config: vpngate-vpn563702141). Requesting rotation...
2026-06-09T17:04:59.897Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported block for config 931 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:04:59.897Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...

```
#### 📥 Loader (`igaming-source-marathonbet-com-loader-7b8c888fcd-d7pv5` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:30:27.920Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-873] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28805866 (attempt 2)
2026-06-09T16:30:27.920Z ERROR 1 --- [igaming-source-marathonbet-com] [    virtual-874] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Brazil/Serie+A/Chapecoense+vs+Flamengo+-+28805897: Playwright connection closed
2026-06-09T16:30:27.920Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-874] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28805897 (attempt 1)
2026-06-09T16:30:27.920Z ERROR 1 --- [igaming-source-marathonbet-com] [    virtual-870] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Brazil/Serie+A/Coritiba+vs+Palmeiras+-+28805883: Playwright connection closed
2026-06-09T16:30:27.920Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-870] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28805883 (attempt 1)
2026-06-09T16:30:27.920Z ERROR 1 --- [igaming-source-marathonbet-com] [    virtual-870] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Brazil/Serie+A/Coritiba+vs+Palmeiras+-+28805883: Playwright connection closed
2026-06-09T16:30:27.920Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-870] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28805883 (attempt 2)
2026-06-09T16:30:27.920Z ERROR 1 --- [igaming-source-marathonbet-com] [    virtual-875] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Women/Nigeria/Premier+League.+Women/Edo+Queens+Women+vs+Abia+Angels+Women+-+29012438: Playwright connection closed
2026-06-09T16:30:27.920Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-875] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29012438 (attempt 1)
2026-06-09T16:30:27.921Z ERROR 1 --- [igaming-source-marathonbet-com] [    virtual-875] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Women/Nigeria/Premier+League.+Women/Edo+Queens+Women+vs+Abia+Angels+Women+-+29012438: Playwright connection closed
2026-06-09T16:30:27.921Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-875] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29012438 (attempt 2)
2026-06-09T16:30:27.921Z ERROR 1 --- [igaming-source-marathonbet-com] [    virtual-876] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Women/Nigeria/Premier+League.+Women/Rivers+Angels+Women+vs+Bayelsa+Queens+Women+-+29012440: Playwright connection closed
2026-06-09T16:30:27.921Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-876] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29012440 (attempt 1)
2026-06-09T16:30:27.921Z ERROR 1 --- [igaming-source-marathonbet-com] [    virtual-876] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Women/Nigeria/Premier+League.+Women/Rivers+Angels+Women+vs+Bayelsa+Queens+Women+-+29012440: Playwright connection closed
2026-06-09T16:30:27.921Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-876] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29012440 (attempt 2)
2026-06-09T16:30:27.921Z ERROR 1 --- [igaming-source-marathonbet-com] [    virtual-877] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Women/Nigeria/Premier+League.+Women/Robo+Queens+Women+vs+Nasarawa+Amazons+Women+-+29012442: Playwright connection closed
2026-06-09T16:30:27.921Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-877] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29012442 (attempt 1)
2026-06-09T16:30:27.921Z ERROR 1 --- [igaming-source-marathonbet-com] [    virtual-877] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Women/Nigeria/Premier+League.+Women/Robo+Queens+Women+vs+Nasarawa+Amazons+Women+-+29012442: Playwright connection closed
2026-06-09T16:30:27.921Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-877] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 29012442 (attempt 2)
2026-06-09T16:30:27.920Z ERROR 1 --- [igaming-source-marathonbet-com] [    virtual-874] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Football/Brazil/Serie+A/Chapecoense+vs+Flamengo+-+28805897: Playwright connection closed
2026-06-09T16:30:27.922Z  WARN 1 --- [igaming-source-marathonbet-com] [    virtual-874] p.d.i.s.m.service.MarathonbetApiClient   : No HTML content for all markets of event 28805897 (attempt 2)
2026-06-09T16:30:28.669Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 12 marathonbet-com matches
2026-06-09T16:30:45.735Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T16:30:52.166Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:30:57.756Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:31:57.759Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:32:03.313Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:33:03.315Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:33:08.750Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:33:45.857Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T16:34:08.750Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:34:13.981Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:34:34.995Z  INFO 1 --- [igaming-source-marathonbet-com] [-com-producer-1] org.apache.kafka.clients.NetworkClient   : [Producer clientId=igaming-source-marathonbet-com-producer-1] Node -1 disconnected.
2026-06-09T16:35:13.984Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:35:25.147Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:36:25.148Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:36:29.543Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:36:46.008Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T16:37:29.544Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:37:35.211Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:38:35.213Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:38:40.953Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:39:40.956Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:39:45.750Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:39:46.087Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T16:40:45.753Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:40:51.771Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:41:51.773Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:41:56.340Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:42:46.167Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T16:42:56.342Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:43:01.511Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:44:01.513Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:44:07.596Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:44:35.033Z  INFO 1 --- [igaming-source-marathonbet-com] [-com-producer-1] org.apache.kafka.clients.NetworkClient   : [Producer clientId=igaming-source-marathonbet-com-producer-1] Node -1 disconnected.
2026-06-09T16:45:07.597Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:45:12.506Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:45:46.280Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T16:46:12.507Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:46:18.521Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:47:18.527Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:47:24.036Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:48:24.037Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:48:29.395Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:48:46.469Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T16:49:29.402Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:49:34.765Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:50:34.767Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:50:39.266Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:51:39.266Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:51:45.083Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:51:46.586Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T16:52:45.083Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:53:03.053Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:54:03.055Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:54:08.371Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:54:46.662Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T16:55:08.372Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:55:13.749Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:56:13.751Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:56:19.121Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:57:19.128Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:57:24.515Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:57:46.758Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T16:58:24.516Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:58:29.877Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T16:59:29.878Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T16:59:35.939Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:00:35.940Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T17:00:40.867Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:00:47.024Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T17:01:40.868Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T17:01:46.280Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:02:46.281Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T17:02:51.187Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:03:47.107Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='marathonbet-com' name='Marathonbet'
2026-06-09T17:03:51.187Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T17:03:59.843Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)
2026-06-09T17:04:59.846Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'www.marathonbet.com' is still reachable...
2026-06-09T17:05:04.720Z  INFO 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 593 (service: igaming-source-marathonbet-com, host: www.marathonbet.com)

```

---

### 🗂️ Букмекер: `megapari`

#### 🕷️ Crawler (`igaming-source-megapari-crawler-5bf45b49b8-kz26n` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:55:37.416Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":263,"GVE":1,"HL":true,"RLI":
2026-06-09T17:00:44.173Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{},"PS":[{"Ke
2026-06-09T17:00:57.388Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":3543,"GVE":1,"HL":true,"RLI"
```
**Последние строки логов:**
```text
2026-06-09T17:03:42.105Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1297 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T17:04:42.106Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T17:04:44.331Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1297 (service: igaming-source-megapari, host: megapari.com)
```
#### 📥 Loader (`igaming-source-megapari-loader-6c47c8c7d5-8csjj` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:34:36.050Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 26 megapari matches
2026-06-09T16:34:39.148Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:34:43.171Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 22 megapari matches
2026-06-09T16:34:49.173Z ERROR 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://megapari.com: Read timed out
2026-06-09T16:34:49.173Z ERROR 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'megapari.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T16:34:49.174Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.3.51 (config: vpngate-vpn852692839). Requesting rotation...
2026-06-09T16:34:49.942Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported block for config 591 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:34:49.943Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T16:34:54.893Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.3.150' (config: vpngate-patito, ID: 513, healthy proxies: 38)
2026-06-09T16:34:54.894Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.3.150:3128
2026-06-09T16:34:54.894Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.3.150:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T16:34:54.895Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'megapari.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T16:34:54.895Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'megapari.com'...
2026-06-09T16:34:55.199Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: megapari.com -> 83.147.204.197
2026-06-09T16:34:55.199Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://megapari.com'...
2026-06-09T16:35:05.906Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'megapari.com' is reachable (HTTP 203). Proxy connectivity verified.
2026-06-09T16:35:16.792Z  INFO 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:35:26.581Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T16:35:54.895Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:35:58.319Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:36:58.320Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:37:01.164Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:38:01.164Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:38:05.265Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:38:26.844Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T16:39:05.266Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:39:09.608Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:39:44.988Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 20 megapari matches
2026-06-09T16:39:53.666Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 megapari matches
2026-06-09T16:40:00.762Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 23 megapari matches
2026-06-09T16:40:09.610Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:40:14.372Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:41:14.373Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:41:17.159Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:41:26.919Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T16:42:17.160Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:42:21.048Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:42:51.460Z  INFO 1 --- [igaming-source-megapari] [pari-producer-1] org.apache.kafka.clients.NetworkClient   : [Producer clientId=igaming-source-megapari-producer-1] Node -1 disconnected.
2026-06-09T16:43:21.048Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:43:24.612Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:44:24.613Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:44:26.984Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T16:44:28.227Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:45:02.639Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 megapari matches
2026-06-09T16:45:10.136Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 megapari matches
2026-06-09T16:45:17.039Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 21 megapari matches
2026-06-09T16:45:28.227Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:45:30.935Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:46:30.935Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:46:35.132Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:47:27.038Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T16:47:35.133Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:47:38.031Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:48:38.032Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:48:41.593Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:49:41.594Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:49:45.749Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:50:18.601Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 megapari matches
2026-06-09T16:50:25.589Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 megapari matches
2026-06-09T16:50:27.115Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T16:50:33.102Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 28 megapari matches
2026-06-09T16:50:45.750Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:50:48.381Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:51:48.382Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:51:52.426Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:52:52.427Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:53:03.899Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:53:27.464Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T16:54:03.900Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:54:06.865Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:55:06.866Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:55:14.504Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:55:34.986Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 megapari matches
2026-06-09T16:55:42.404Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 megapari matches
2026-06-09T16:55:49.026Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 13 megapari matches
2026-06-09T16:56:14.505Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:56:17.669Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:56:27.507Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T16:57:17.670Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:57:21.161Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:58:21.161Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:58:25.595Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T16:59:25.595Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T16:59:27.572Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T16:59:28.109Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T17:00:28.109Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T17:00:31.447Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T17:00:53.389Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 megapari matches
2026-06-09T17:01:00.295Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 megapari matches
2026-06-09T17:01:07.286Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 megapari matches
2026-06-09T17:01:13.639Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 15 megapari matches
2026-06-09T17:01:31.448Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T17:01:34.405Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T17:02:27.654Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='megapari' name='Xbet-family'
2026-06-09T17:02:34.406Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T17:02:37.503Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T17:03:37.504Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T17:03:42.055Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)
2026-06-09T17:04:42.056Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'megapari.com' is still reachable...
2026-06-09T17:04:46.096Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 513 (service: igaming-source-megapari, host: megapari.com)

```

---

### 🗂️ Букмекер: `melbet`

#### 🕷️ Crawler (`igaming-source-melbet-crawler-8547f65849-x4www` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:25:01.204Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:25:02.702Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://melbet.ru: No route to host
2026-06-09T16:25:02.702Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'melbet.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T16:25:02.702Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reporting failure for proxy: 10.42.2.52 (config: vpngate-vpn918315623). Requesting rotation...
2026-06-09T16:25:03.728Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported block for config 569 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:25:03.729Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T16:25:26.181Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.19.248' (config: vpngate-vpn183954555, ID: 1024, healthy proxies: 49)
2026-06-09T16:25:26.182Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.19.248:3128
2026-06-09T16:25:26.183Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.19.248:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T16:25:26.203Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'melbet.ru' through proxy (3 retries, 5000ms delay)...
2026-06-09T16:25:26.206Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'melbet.ru'...
2026-06-09T16:25:26.289Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: melbet.ru -> 213.165.196.159
2026-06-09T16:25:26.292Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://melbet.ru'...
2026-06-09T16:25:29.459Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ✅ Target host 'melbet.ru' is reachable (HTTP 403). Proxy connectivity verified.
2026-06-09T16:25:29.794Z  INFO 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:26:25.586Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:26:26.194Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:26:30.537Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
2026-06-09T16:27:30.538Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:27:43.036Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:28:43.037Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:28:57.418Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:29:25.659Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:29:57.419Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:30:01.074Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:31:01.076Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:31:04.861Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:32:04.862Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:32:08.245Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:32:25.700Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:33:08.252Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:33:11.935Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:34:11.939Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:34:15.737Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:35:15.738Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:35:25.790Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:35:26.753Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:36:26.755Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:36:30.522Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:37:30.524Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:37:34.480Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:38:25.823Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:38:34.483Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:38:39.421Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:39:39.423Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:39:42.825Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:40:42.829Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:40:46.857Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:41:25.912Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:41:46.859Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:41:50.025Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:42:50.026Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:42:53.689Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:43:53.692Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:43:57.238Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:44:26.011Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:44:57.240Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:45:00.589Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:46:00.589Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:46:05.298Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:47:05.299Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:47:09.407Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:47:26.045Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:48:09.409Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:48:12.702Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:49:12.705Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:49:16.839Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:50:16.841Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:50:21.783Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:50:26.087Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:51:21.785Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:51:26.212Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:52:26.213Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:52:59.217Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:53:26.379Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:53:59.219Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:54:02.917Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:55:02.919Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:55:05.876Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:56:05.877Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:56:09.236Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:56:26.410Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T16:57:09.237Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:57:12.535Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:58:12.536Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:58:15.538Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:59:15.539Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T16:59:19.113Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T16:59:26.447Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T17:00:19.115Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T17:00:22.307Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T17:01:22.316Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T17:01:26.006Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T17:02:26.008Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T17:02:26.478Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='melbet' name='Xbet-family'
2026-06-09T17:02:29.606Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T17:03:29.606Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T17:03:33.285Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)
2026-06-09T17:04:33.286Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'melbet.ru' is still reachable...
2026-06-09T17:04:36.767Z  INFO 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1024 (service: igaming-source-melbet, host: melbet.ru)

```
#### 📥 Loader (`igaming-source-melbet-loader-7784759f76-5gzrv` - Running)
**Анализ ошибок:**
```text
TIMEOUT: Command timed out after 15 seconds
```

---

### 🗂️ Букмекер: `pinnacle`

#### 🕷️ Crawler (`igaming-source-pinnacle-crawler-5c4cf4f658-ls792` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:58:46.379Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T16:58:46.379Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Volleyball (ID: 34)
2026-06-09T16:58:46.645Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T16:58:46.645Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Soccer (ID: 29)
2026-06-09T16:58:46.900Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T16:58:46.900Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Ice Hockey (ID: 19)
2026-06-09T16:58:47.074Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T16:58:47.074Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Tennis (ID: 33)
2026-06-09T16:58:47.074Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.pinnacle.service.MatchService    : Pinnacle line scraping completed.
2026-06-09T16:58:50.238Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T16:59:47.075Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.scheduler.MatchFetchScheduler  : League-crawler: Starting Pinnacle line scraping cycle...
2026-06-09T16:59:47.075Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.pinnacle.service.MatchService    : Starting Pinnacle line scraping for active sports...
2026-06-09T16:59:47.212Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T16:59:47.212Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Basketball (ID: 4)
2026-06-09T16:59:47.347Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T16:59:47.347Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Volleyball (ID: 34)
2026-06-09T16:59:47.451Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T16:59:47.451Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Soccer (ID: 29)
2026-06-09T16:59:47.592Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T16:59:47.592Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Ice Hockey (ID: 19)
2026-06-09T16:59:47.705Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T16:59:47.706Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Tennis (ID: 33)
2026-06-09T16:59:47.706Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.pinnacle.service.MatchService    : Pinnacle line scraping completed.
2026-06-09T16:59:50.239Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T16:59:59.656Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T17:00:18.828Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='pinnacle' name='Pinnacle'
2026-06-09T17:00:47.720Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.scheduler.MatchFetchScheduler  : League-crawler: Starting Pinnacle line scraping cycle...
2026-06-09T17:00:47.720Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : Starting Pinnacle line scraping for active sports...
2026-06-09T17:00:47.938Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:00:47.938Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Basketball (ID: 4)
2026-06-09T17:00:48.134Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:00:48.134Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Volleyball (ID: 34)
2026-06-09T17:00:48.302Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:00:48.303Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Soccer (ID: 29)
2026-06-09T17:00:48.473Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:00:48.473Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Ice Hockey (ID: 19)
2026-06-09T17:00:48.618Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:00:48.619Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Tennis (ID: 33)
2026-06-09T17:00:48.619Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : Pinnacle line scraping completed.
2026-06-09T17:00:59.660Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T17:01:13.456Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T17:01:48.621Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.scheduler.MatchFetchScheduler  : League-crawler: Starting Pinnacle line scraping cycle...
2026-06-09T17:01:48.621Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : Starting Pinnacle line scraping for active sports...
2026-06-09T17:01:48.788Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:01:48.789Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Basketball (ID: 4)
2026-06-09T17:01:48.942Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:01:48.943Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Volleyball (ID: 34)
2026-06-09T17:01:49.064Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:01:49.065Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Soccer (ID: 29)
2026-06-09T17:01:49.176Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:01:49.177Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Ice Hockey (ID: 19)
2026-06-09T17:01:49.287Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:01:49.288Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Tennis (ID: 33)
2026-06-09T17:01:49.288Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : Pinnacle line scraping completed.
2026-06-09T17:02:13.457Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T17:02:31.168Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T17:02:49.288Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.scheduler.MatchFetchScheduler  : League-crawler: Starting Pinnacle line scraping cycle...
2026-06-09T17:02:49.289Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : Starting Pinnacle line scraping for active sports...
2026-06-09T17:02:49.404Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:02:49.405Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Basketball (ID: 4)
2026-06-09T17:02:49.606Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:02:49.606Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Volleyball (ID: 34)
2026-06-09T17:02:49.745Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:02:49.745Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Soccer (ID: 29)
2026-06-09T17:02:49.874Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:02:49.874Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Ice Hockey (ID: 19)
2026-06-09T17:02:50.000Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:02:50.001Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Tennis (ID: 33)
2026-06-09T17:02:50.001Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.pinnacle.service.MatchService    : Pinnacle line scraping completed.
2026-06-09T17:03:18.858Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='pinnacle' name='Pinnacle'
2026-06-09T17:03:31.169Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T17:03:40.593Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T17:03:50.001Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.scheduler.MatchFetchScheduler  : League-crawler: Starting Pinnacle line scraping cycle...
2026-06-09T17:03:50.001Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : Starting Pinnacle line scraping for active sports...
2026-06-09T17:03:50.165Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:03:50.165Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Basketball (ID: 4)
2026-06-09T17:03:50.307Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:03:50.308Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Volleyball (ID: 34)
2026-06-09T17:03:50.479Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:03:50.479Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Soccer (ID: 29)
2026-06-09T17:03:50.724Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:03:50.725Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Ice Hockey (ID: 19)
2026-06-09T17:03:50.983Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:03:50.983Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Tennis (ID: 33)
2026-06-09T17:03:50.984Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : Pinnacle line scraping completed.
2026-06-09T17:04:40.594Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'pinnacle.com' is still reachable...
2026-06-09T17:04:49.901Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Reported success for config 853 (service: igaming-source-pinnacle, host: pinnacle.com)
2026-06-09T17:04:50.985Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.scheduler.MatchFetchScheduler  : League-crawler: Starting Pinnacle line scraping cycle...
2026-06-09T17:04:50.985Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : Starting Pinnacle line scraping for active sports...
2026-06-09T17:04:51.148Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:04:51.148Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Basketball (ID: 4)
2026-06-09T17:04:51.287Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:04:51.288Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Volleyball (ID: 34)
2026-06-09T17:04:51.440Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:04:51.440Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Soccer (ID: 29)
2026-06-09T17:04:51.599Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:04:51.600Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Ice Hockey (ID: 19)
2026-06-09T17:04:51.740Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T17:04:51.740Z  WARN 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : No fixtures found for Pinnacle sport Tennis (ID: 33)
2026-06-09T17:04:51.740Z  INFO 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.pinnacle.service.MatchService    : Pinnacle line scraping completed.

```
#### 📥 Loader (`igaming-source-pinnacle-loader-5dfd58bcf-hbcff` - Running)
**Анализ ошибок:**
```text

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2026-06-09T16:59:33.419Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.pinnacle.PinnacleApplication     : Starting PinnacleApplication v0.0.1-SNAPSHOT using Java 21.0.10 with PID 1 (/app/app.jar started by root in /app)
2026-06-09T16:59:33.422Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.pinnacle.PinnacleApplication     : The following 1 profile is active: "match-loader"
2026-06-09T16:59:36.546Z  INFO 1 --- [igaming-source-pinnacle] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T16:59:36.547Z  INFO 1 --- [igaming-source-pinnacle] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-06-09T16:59:36.948Z  INFO 1 --- [igaming-source-pinnacle] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 376 ms. Found 7 JPA repository interfaces.
2026-06-09T16:59:38.328Z  INFO 1 --- [igaming-source-pinnacle] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode
2026-06-09T16:59:38.330Z  INFO 1 --- [igaming-source-pinnacle] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data Redis repositories in DEFAULT mode.
2026-06-09T16:59:38.422Z  INFO 1 --- [igaming-source-pinnacle] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 76 ms. Found 0 Redis repository interfaces.
2026-06-09T16:59:39.629Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=12960735-5020-3b89-a5b3-15ba3bb68d58
2026-06-09T16:59:42.136Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3040 (http)
2026-06-09T16:59:42.155Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-06-09T16:59:42.155Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2026-06-09T16:59:42.435Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-06-09T16:59:42.436Z  INFO 1 --- [igaming-source-pinnacle] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 8813 ms
2026-06-09T16:59:44.557Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-06-09T16:59:44.728Z  INFO 1 --- [igaming-source-pinnacle] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.4.Final
2026-06-09T16:59:44.825Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-06-09T16:59:45.634Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-06-09T16:59:45.735Z  INFO 1 --- [igaming-source-pinnacle] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-06-09T16:59:46.551Z  INFO 1 --- [igaming-source-pinnacle] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@624be1d4
2026-06-09T16:59:46.554Z  INFO 1 --- [igaming-source-pinnacle] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-06-09T16:59:46.681Z  WARN 1 --- [igaming-source-pinnacle] [           main] org.hibernate.orm.deprecation            : HHH90000025: PostgreSQLDialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-06-09T16:59:46.832Z  INFO 1 --- [igaming-source-pinnacle] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 15.18
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2026-06-09T16:59:50.623Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-06-09T16:59:51.398Z  INFO 1 --- [igaming-source-pinnacle] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T16:59:53.038Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2026-06-09T16:59:58.382Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.service.VpnManagerService   : Refreshing VPN configuration from Proxy Backend...
2026-06-09T17:00:03.835Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.service.VpnManagerService   : VPN Pool assigned proxy '10.42.2.225' (config: vpngate-vpn529590155, ID: 588, healthy proxies: 62)
2026-06-09T17:00:03.835Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.service.VpnManagerService   : Configuring proxy: 10.42.2.225:3128
2026-06-09T17:00:03.836Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.service.VpnManagerService   : ✅ Proxy configured: 10.42.2.225:3128 (non-proxy: igaming-aggregator|localhost|127.*|10.*|172.*|192.168.*|*.cluster.local|*.svc.cluster.local)
2026-06-09T17:00:03.863Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Verifying connectivity to target host 'pinnacle.com' through proxy (3 retries, 5000ms delay)...
2026-06-09T17:00:03.866Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Resolving DNS for 'pinnacle.com'...
2026-06-09T17:00:03.942Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.browser.BrowserService      : Initializing Universal Browser Service (Playwright)
2026-06-09T17:00:03.958Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] DNS resolved: pinnacle.com -> 104.18.42.200
2026-06-09T17:00:03.958Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 1/3] Checking HTTP connectivity to 'https://pinnacle.com'...
2026-06-09T17:00:04.093Z  WARN 1 --- [igaming-source-pinnacle] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'genericMatchLoadScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/lib/igaming-source-core-0.0.1-SNAPSHOT.jar!/pro/datawiki/igaming/source/core/scheduler/GenericMatchLoadScheduler.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
2026-06-09T17:00:04.094Z  INFO 1 --- [igaming-source-pinnacle] [           main] p.d.i.s.core.browser.BrowserService      : Shutting down Universal Browser Service
2026-06-09T17:00:04.166Z  INFO 1 --- [igaming-source-pinnacle] [           main] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-06-09T17:00:04.171Z  INFO 1 --- [igaming-source-pinnacle] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-06-09T17:00:04.191Z  INFO 1 --- [igaming-source-pinnacle] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-06-09T17:00:04.196Z  INFO 1 --- [igaming-source-pinnacle] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2026-06-09T17:00:04.325Z  INFO 1 --- [igaming-source-pinnacle] [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-06-09T17:00:04.415Z ERROR 1 --- [igaming-source-pinnacle] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 

***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 0 of constructor in pro.datawiki.igaming.source.core.scheduler.GenericMatchLoadScheduler required a bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' that could not be found.


Action:

Consider defining a bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' in your configuration.

2026-06-09T17:00:07.086Z ERROR 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: No route to host
2026-06-09T17:00:07.086Z  WARN 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'pinnacle.com': java.net.NoRouteToHostException: No route to host
2026-06-09T17:00:07.087Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T17:00:12.087Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Resolving DNS for 'pinnacle.com'...
2026-06-09T17:00:12.087Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] DNS resolved: pinnacle.com -> 104.18.42.200
2026-06-09T17:00:12.088Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 2/3] Checking HTTP connectivity to 'https://pinnacle.com'...
2026-06-09T17:00:15.148Z ERROR 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: No route to host
2026-06-09T17:00:15.149Z  WARN 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'pinnacle.com': java.net.NoRouteToHostException: No route to host
2026-06-09T17:00:15.149Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Waiting 5000ms before retry...
2026-06-09T17:00:20.149Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Resolving DNS for 'pinnacle.com'...
2026-06-09T17:00:20.150Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] DNS resolved: pinnacle.com -> 104.18.42.200
2026-06-09T17:00:20.150Z  INFO 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : [Attempt 3/3] Checking HTTP connectivity to 'https://pinnacle.com'...
2026-06-09T17:00:23.213Z ERROR 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: No route to host
2026-06-09T17:00:23.214Z  WARN 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 3/3] Connectivity check failed for 'pinnacle.com': java.net.NoRouteToHostException: No route to host
2026-06-09T17:00:23.214Z ERROR 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'pinnacle.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T17:00:23.214Z  WARN 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity verification failed: 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'pinnacle.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status. Will try to rotate on next failure.

```

---

### 🗂️ Букмекер: `sbobet`

#### 🕷️ Crawler (`igaming-source-sbobet-crawler-59dcf6666b-44ldt` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:02:49.466Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, NEW=0, PENDING=0, PROCESSED=304, FAILED=0
2026-06-09T17:03:49.519Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, NEW=0, PENDING=0, PROCESSED=304, FAILED=0
2026-06-09T17:04:49.363Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, NEW=0, PENDING=0, PROCESSED=304, FAILED=0
```
**Последние строки логов:**
```text
2026-06-09T17:04:49.363Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, NEW=0, PENDING=0, PROCESSED=304, FAILED=0
2026-06-09T17:05:11.936Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-2] p.d.i.s.sbobet.service.SbobetApiClient   : Extracted 22 league mappings from SBOBET HTML markup
2026-06-09T17:05:11.980Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-2] p.d.i.s.sbobet.service.SbobetApiClient   : Successfully fetched and transformed SBOBET odds for sport: Soccer — found 22 leagues
```
#### 📥 Loader (`igaming-source-sbobet-loader-6d9d6455c9-6jl6r` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:00:29.205Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, NEW=0, PENDING=0, PROCESSED=304, FAILED=0
2026-06-09T17:01:29.162Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, NEW=0, PENDING=0, PROCESSED=304, FAILED=0
2026-06-09T17:02:29.085Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, NEW=0, PENDING=0, PROCESSED=304, FAILED=0
2026-06-09T17:03:29.158Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, NEW=0, PENDING=0, PROCESSED=304, FAILED=0
2026-06-09T17:04:29.112Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, NEW=0, PENDING=0, PROCESSED=304, FAILED=0
```
**Последние строки логов:**
```text
2026-06-09T17:04:29.112Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=304, NEW=0, PENDING=0, PROCESSED=304, FAILED=0
2026-06-09T17:04:33.561Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='sbobet' name='Sbobet'
2026-06-09T17:05:13.423Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 16 sbobet matches
```

---

### 🗂️ Букмекер: `winline`

#### 🕷️ Crawler (`igaming-source-winline-crawler-6c5667b6b4-lzg4v` - Running)
**Анализ ошибок:**
```text
2026-06-09T16:57:40.909Z  WARN 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.c.s.SportNormalizationService    : ALARM ALARM new sport: bookmaker='winline', sportName='Падел'
2026-06-09T16:57:42.167Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.c.engine.AbstractMatchService    : Discovered 173 live winline events
2026-06-09T16:57:42.167Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled live fetch completed: 173 events discovered
2026-06-09T16:57:54.179Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.c.engine.AbstractMatchService    : Discovered 1856 prematch winline events
2026-06-09T16:57:54.180Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled prematch fetch completed: 1856 events discovered
2026-06-09T16:58:12.805Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T16:58:16.871Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 970 (service: igaming-source-winline, host: winline.ru)
2026-06-09T16:58:42.168Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from winline.ru...
2026-06-09T16:58:42.168Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : Fetching Winline live events via apiWlb SAPI from https://winline.ru/live
2026-06-09T16:58:42.230Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : Navigating to https://winline.ru/live...
2026-06-09T16:59:15.543Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : Waiting for apiWlb to be available...
2026-06-09T16:59:16.872Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T16:59:18.589Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : Executing SAPI extraction script for live events...
2026-06-09T16:59:20.678Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 970 (service: igaming-source-winline, host: winline.ru)
2026-06-09T16:59:23.102Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : DIAG_PARAM: [{"eventId":15799937,"freeText":"@[a]P@ фора [b]","koef":"1/-2.5","V":[2.52,1.5],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":932,"idLine":1455259012,"fullJson":"{\"id\":1455259012,\"idEvent\":15799937,\"idTipMarket\":932,\"koef\":\"1/-2.5\",\"V\":[2.52,1.5],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":932,\"sports\":\"71,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":6,\"freeTextR\":\"@[a]P@ фора [b]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":61,\"meta\":{\"revision\":0,\"created\":1781024327940,\"version\":0},\"$loki\":246},\"isLive\":1,\"state\":1,\"idLine\":1455259012,\"meta\":{\"revision\":1,\"created\":1781024330052,\"version\":0,\"updated\":1781024335840},\"$loki\":3}"},{"eventId":15799937,"freeText":"@[a]P@ фора [b]","koef":"1/-1","V":[1.21,4.22],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":932,"idLine":1455259013,"fullJson":"{\"id\":1455259013,\"idEvent\":15799937,\"idTipMarket\":932,\"koef\":\"1/-1\",\"V\":[1.21,4.22],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":932,\"sports\":\"71,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":6,\"freeTextR\":\"@[a]P@ фора [b]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":61,\"meta\":{\"revision\":0,\"created\":1781024327940,\"version\":0},\"$loki\":246},\"isLive\":1,\"state\":1,\"idLine\":1455259013,\"meta\":{\"revision\":1,\"created\":1781024330052,\"version\":0,\"updated\":1781024335840},\"$loki\":4}"},{"eventId":15799937,"freeText":"@[a]P@ фора [b]","koef":"1/-1.5","V":[1.55,2.4],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":932,"idLine":1455259014,"fullJson":"{\"id\":1455259014,\"idEvent\":15799937,\"idTipMarket\":932,\"koef\":\"1/-1.5\",\"V\":[1.55,2.4],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":932,\"sports\":\"71,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":6,\"freeTextR\":\"@[a]P@ фора [b]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":61,\"meta\":{\"revision\":0,\"created\":1781024327940,\"version\":0},\"$loki\":246},\"isLive\":1,\"state\":1,\"idLine\":1455259014,\"meta\":{\"revision\":1,\"created\":1781024330052,\"version\":0,\"updated\":1781024335840},\"$loki\":5}"},{"eventId":15799937,"freeText":"@[a]P@ фора [b]","koef":"1/-2","V":[1.93,1.84],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":932,"idLine":1455259011,"fullJson":"{\"id\":1455259011,\"idEvent\":15799937,\"idTipMarket\":932,\"koef\":\"1/-2\",\"V\":[1.93,1.84],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":932,\"sports\":\"71,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":6,\"freeTextR\":\"@[a]P@ фора [b]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":61,\"meta\":{\"revision\":0,\"created\":1781024327940,\"version\":0},\"$loki\":246},\"isLive\":1,\"state\":1,\"idLine\":1455259011,\"meta\":{\"revision\":1,\"created\":1781024330052,\"version\":0,\"updated\":1781024335840},\"$loki\":19}"},{"eventId":15799937,"freeText":"@[a]P@ фора [b]","koef":"1/-3","V":[3.93,1.24],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":932,"idLine":1455259015,"fullJson":"{\"id\":1455259015,\"idEvent\":15799937,\"idTipMarket\":932,\"koef\":\"1/-3\",\"V\":[3.93,1.24],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":932,\"sports\":\"71,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":6,\"freeTextR\":\"@[a]P@ фора [b]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":61,\"meta\":{\"revision\":0,\"created\":1781024327940,\"version\":0},\"$loki\":246},\"isLive\":1,\"state\":1,\"idLine\":1455259015,\"meta\":{\"revision\":1,\"created\":1781024330052,\"version\":0,\"updated\":1781024335840},\"$loki\":20}"},{"eventId":15799937,"freeText":"@1P@ фора угловых [a]","koef":"1.5","V":[1.38,2.79],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1010,"idLine":1455259072,"fullJson":"{\"id\":1455259072,\"idEvent\":15799937,\"idTipMarket\":1010,\"koef\":\"1.5\",\"V\":[1.38,2.79],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1010,\"sports\":\"\",\"favorite\":1,\"countV\":2,\"idTipEvent\":16,\"freeTextR\":\"@1P@ фора угловых [a]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":16,\"meta\":{\"revision\":0,\"created\":1781024327941,\"version\":0},\"$loki\":568},\"isLive\":1,\"state\":1,\"idLine\":1455259072,\"meta\":{\"revision\":0,\"created\":1781024330052,\"version\":0},\"$loki\":77}"},{"eventId":15799937,"freeText":"@1P@ фора угловых [a]","koef":"3.5","V":[2.56,1.45],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1010,"idLine":1455259073,"fullJson":"{\"id\":1455259073,\"idEvent\":15799937,\"idTipMarket\":1010,\"koef\":\"3.5\",\"V\":[2.56,1.45],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1010,\"sports\":\"\",\"favorite\":1,\"countV\":2,\"idTipEvent\":16,\"freeTextR\":\"@1P@ фора угловых [a]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":16,\"meta\":{\"revision\":0,\"created\":1781024327941,\"version\":0},\"$loki\":568},\"isLive\":1,\"state\":1,\"idLine\":1455259073,\"meta\":{\"revision\":0,\"created\":1781024330052,\"version\":0},\"$loki\":78}"},{"eventId":15799937,"freeText":"@1P@ фора угловых [a]","koef":"2.5","V":[1.79,1.9],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1010,"idLine":1455259074,"fullJson":"{\"id\":1455259074,\"idEvent\":15799937,\"idTipMarket\":1010,\"koef\":\"2.5\",\"V\":[1.79,1.9],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1010,\"sports\":\"\",\"favorite\":1,\"countV\":2,\"idTipEvent\":16,\"freeTextR\":\"@1P@ фора угловых [a]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":16,\"meta\":{\"revision\":0,\"created\":1781024327941,\"version\":0},\"$loki\":568},\"isLive\":1,\"state\":1,\"idLine\":1455259074,\"meta\":{\"revision\":0,\"created\":1781024330052,\"version\":0},\"$loki\":79}"},{"eventId":15799937,"freeText":"Фора [a] (@NP@)","koef":"4","V":[1.76,2.04],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455259084,"fullJson":"{\"id\":1455259084,\"idEvent\":15799937,\"idTipMarket\":1073,\"koef\":\"4\",\"V\":[1.76,2.04],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024327941,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455259084,\"meta\":{\"revision\":1,\"created\":1781024330052,\"version\":0,\"updated\":1781024335841},\"$loki\":89}"},{"eventId":15799937,"freeText":"Фора [a] (@NP@)","koef":"5","V":[2.75,1.4],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455259085,"fullJson":"{\"id\":1455259085,\"idEvent\":15799937,\"idTipMarket\":1073,\"koef\":\"5\",\"V\":[2.75,1.4],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024327941,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455259085,\"meta\":{\"revision\":1,\"created\":1781024330052,\"version\":0,\"updated\":1781024335841},\"$loki\":90}"}]
2026-06-09T16:59:23.204Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : Successfully extracted 191 Winline live events with 16723 total odds
2026-06-09T16:59:24.214Z  WARN 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.c.s.SportNormalizationService    : ALARM ALARM new sport: bookmaker='winline', sportName='Падел'
2026-06-09T16:59:24.225Z  WARN 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.c.s.SportNormalizationService    : ALARM ALARM new sport: bookmaker='winline', sportName='Падел'
2026-06-09T16:59:26.597Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.c.engine.AbstractMatchService    : Discovered 191 live winline events
2026-06-09T16:59:26.597Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled live fetch completed: 191 events discovered
2026-06-09T17:00:04.396Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='winline' name='Winline'
2026-06-09T17:00:20.679Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T17:00:25.453Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 970 (service: igaming-source-winline, host: winline.ru)
2026-06-09T17:00:26.598Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from winline.ru...
2026-06-09T17:00:26.598Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Fetching Winline live events via apiWlb SAPI from https://winline.ru/live
2026-06-09T17:00:26.852Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Navigating to https://winline.ru/live...
2026-06-09T17:01:01.577Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Waiting for apiWlb to be available...
2026-06-09T17:01:04.670Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Executing SAPI extraction script for live events...
2026-06-09T17:01:09.349Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : DIAG_PARAM: [{"eventId":15799937,"freeText":"Фора [a] (@NP@)","koef":"2","V":[1.1,5.7],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455260815,"fullJson":"{\"id\":1455260815,\"idEvent\":15799937,\"idTipMarket\":1073,\"koef\":\"2\",\"V\":[1.1,5.7],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024432688,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455260815,\"meta\":{\"revision\":0,\"created\":1781024434631,\"version\":0},\"$loki\":106}"},{"eventId":15799937,"freeText":"Фора угловых [a] (@NP@)","koef":"5.5","V":[1.95,1.76],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1079,"idLine":1455259095,"fullJson":"{\"id\":1455259095,\"idEvent\":15799937,\"idTipMarket\":1079,\"koef\":\"5.5\",\"V\":[1.95,1.76],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1079,\"sports\":\"\",\"favorite\":1,\"countV\":2,\"idTipEvent\":16,\"freeTextR\":\"Фора угловых [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":16,\"meta\":{\"revision\":0,\"created\":1781024432688,\"version\":0},\"$loki\":640},\"isLive\":1,\"state\":1,\"idLine\":1455259095,\"meta\":{\"revision\":0,\"created\":1781024434631,\"version\":0},\"$loki\":99}"},{"eventId":15799937,"freeText":"Фора угловых [a] (@NP@)","koef":"6.5","V":[2.47,1.48],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1079,"idLine":1455259094,"fullJson":"{\"id\":1455259094,\"idEvent\":15799937,\"idTipMarket\":1079,\"koef\":\"6.5\",\"V\":[2.47,1.48],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1079,\"sports\":\"\",\"favorite\":1,\"countV\":2,\"idTipEvent\":16,\"freeTextR\":\"Фора угловых [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":16,\"meta\":{\"revision\":0,\"created\":1781024432688,\"version\":0},\"$loki\":640},\"isLive\":1,\"state\":1,\"idLine\":1455259094,\"meta\":{\"revision\":0,\"created\":1781024434631,\"version\":0},\"$loki\":98}"},{"eventId":15799937,"freeText":"Фора угловых [a] (@NP@)","koef":"4.5","V":[1.6,2.17],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1079,"idLine":1455259093,"fullJson":"{\"id\":1455259093,\"idEvent\":15799937,\"idTipMarket\":1079,\"koef\":\"4.5\",\"V\":[1.6,2.17],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1079,\"sports\":\"\",\"favorite\":1,\"countV\":2,\"idTipEvent\":16,\"freeTextR\":\"Фора угловых [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":16,\"meta\":{\"revision\":0,\"created\":1781024432688,\"version\":0},\"$loki\":640},\"isLive\":1,\"state\":1,\"idLine\":1455259093,\"meta\":{\"revision\":0,\"created\":1781024434631,\"version\":0},\"$loki\":97}"},{"eventId":15799937,"freeText":"Фора [a] (@NP@)","koef":"3.5","V":[1.52,2.42],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455259092,"fullJson":"{\"id\":1455259092,\"idEvent\":15799937,\"idTipMarket\":1073,\"koef\":\"3.5\",\"V\":[1.52,2.42],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024432688,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455259092,\"meta\":{\"revision\":0,\"created\":1781024434631,\"version\":0},\"$loki\":96}"},{"eventId":15799937,"freeText":"Фора [a] (@NP@)","koef":"3","V":[1.3,3.2],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455259091,"fullJson":"{\"id\":1455259091,\"idEvent\":15799937,\"idTipMarket\":1073,\"koef\":\"3\",\"V\":[1.3,3.2],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024432688,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455259091,\"meta\":{\"revision\":0,\"created\":1781024434631,\"version\":0},\"$loki\":95}"},{"eventId":15799937,"freeText":"Фора [a] (@NP@)","koef":"6","V":[4.5,1.16],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455259090,"fullJson":"{\"id\":1455259090,\"idEvent\":15799937,\"idTipMarket\":1073,\"koef\":\"6\",\"V\":[4.5,1.16],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024432688,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455259090,\"meta\":{\"revision\":0,\"created\":1781024434631,\"version\":0},\"$loki\":94}"},{"eventId":15799937,"freeText":"Фора [a] (@NP@)","koef":"5.5","V":[3.2,1.3],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455259089,"fullJson":"{\"id\":1455259089,\"idEvent\":15799937,\"idTipMarket\":1073,\"koef\":\"5.5\",\"V\":[3.2,1.3],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024432688,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455259089,\"meta\":{\"revision\":0,\"created\":1781024434631,\"version\":0},\"$loki\":93}"},{"eventId":15799937,"freeText":"Фора [a] (@NP@)","koef":"4.5","V":[2.15,1.65],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455259088,"fullJson":"{\"id\":1455259088,\"idEvent\":15799937,\"idTipMarket\":1073,\"koef\":\"4.5\",\"V\":[2.15,1.65],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024432688,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455259088,\"meta\":{\"revision\":0,\"created\":1781024434631,\"version\":0},\"$loki\":92}"},{"eventId":15799937,"freeText":"Фора [a] (@NP@)","koef":"2.5","V":[1.22,3.8],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455259086,"fullJson":"{\"id\":1455259086,\"idEvent\":15799937,\"idTipMarket\":1073,\"koef\":\"2.5\",\"V\":[1.22,3.8],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024432688,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455259086,\"meta\":{\"revision\":0,\"created\":1781024434631,\"version\":0},\"$loki\":91}"}]
2026-06-09T17:01:09.444Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Successfully extracted 202 Winline live events with 17990 total odds
2026-06-09T17:01:13.553Z  WARN 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.c.s.SportNormalizationService    : ALARM ALARM new sport: bookmaker='winline', sportName='Падел'
2026-06-09T17:01:13.573Z  WARN 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.c.s.SportNormalizationService    : ALARM ALARM new sport: bookmaker='winline', sportName='Падел'
2026-06-09T17:01:18.020Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.c.engine.AbstractMatchService    : Discovered 202 live winline events
2026-06-09T17:01:18.021Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled live fetch completed: 202 events discovered
2026-06-09T17:01:25.454Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T17:01:29.223Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Reported success for config 970 (service: igaming-source-winline, host: winline.ru)
2026-06-09T17:02:18.022Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from winline.ru...
2026-06-09T17:02:18.022Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : Fetching Winline live events via apiWlb SAPI from https://winline.ru/live
2026-06-09T17:02:18.257Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : Navigating to https://winline.ru/live...
2026-06-09T17:02:29.224Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T17:02:32.623Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Reported success for config 970 (service: igaming-source-winline, host: winline.ru)
2026-06-09T17:02:54.180Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled: fetching prematch data from winline.ru...
2026-06-09T17:02:54.180Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Fetching Winline prematch events via apiWlb SAPI from https://winline.ru/stavki/sport/futbol/
2026-06-09T17:02:54.429Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Navigating to https://winline.ru/stavki/sport/futbol/...
2026-06-09T17:02:54.534Z ERROR 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Fatal error during Winline prematch extraction: Object doesn't exist: request@612ef7f7e8bac08d459a4840c678ccc4

com.microsoft.playwright.PlaywrightException: Object doesn't exist: request@612ef7f7e8bac08d459a4840c678ccc4
	at com.microsoft.playwright.impl.Connection.getExistingObject(Connection.java:192) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.BrowserContextImpl.handleEvent(BrowserContextImpl.java:708) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.Connection.dispatch(Connection.java:292) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.Connection.processOneMessage(Connection.java:211) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.ChannelOwner.runUntil(ChannelOwner.java:118) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.Connection.sendMessage(Connection.java:130) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.ChannelOwner.sendMessage(ChannelOwner.java:106) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.FrameImpl.navigateImpl(FrameImpl.java:463) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.PageImpl.lambda$navigate$46(PageImpl.java:870) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.LoggingSupport.withLogging(LoggingSupport.java:47) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.ChannelOwner.withLogging(ChannelOwner.java:89) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.PageImpl.navigate(PageImpl.java:870) ~[playwright-1.42.0.jar!/:1.42.0]
	at com.microsoft.playwright.impl.PageImpl.navigate(PageImpl.java:42) ~[playwright-1.42.0.jar!/:1.42.0]
	at pro.datawiki.igaming.source.winline.service.WinlineApiClient.fetchEvents(WinlineApiClient.java:83) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.winline.service.WinlineApiClient.fetchPrematchEvents(WinlineApiClient.java:65) ~[!/:0.0.1-SNAPSHOT]
	at pro.datawiki.igaming.source.winline.scheduler.MatchFetchScheduler.fetchPrematchData(MatchFetchScheduler.java:40) ~[!/:0.0.1-SNAPSHOT]
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

2026-06-09T17:02:58.150Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : Waiting for apiWlb to be available...
2026-06-09T17:03:01.276Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : Executing SAPI extraction script for live events...
2026-06-09T17:03:04.409Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='winline' name='Winline'
2026-06-09T17:03:05.848Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : DIAG_PARAM: [{"eventId":15932647,"freeText":"Фора [a] (@NP@)","koef":"1.5","V":[4.25,1.17],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455260898,"fullJson":"{\"id\":1455260898,\"idEvent\":15932647,\"idTipMarket\":1073,\"koef\":\"1.5\",\"V\":[4.25,1.17],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024548630,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455260898,\"meta\":{\"revision\":0,\"created\":1781024550711,\"version\":0},\"$loki\":46}"},{"eventId":15932647,"freeText":"@1HT@ фора [a]","koef":"1","V":[6.5,1.08],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":988,"idLine":1455260885,"fullJson":"{\"id\":1455260885,\"idEvent\":15932647,\"idTipMarket\":988,\"koef\":\"1\",\"V\":[6.5,1.08],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":988,\"sports\":\"1,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":6,\"freeTextR\":\"@1HT@ фора [a]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":6,\"meta\":{\"revision\":0,\"created\":1781024548630,\"version\":0},\"$loki\":307},\"isLive\":1,\"state\":1,\"idLine\":1455260885,\"meta\":{\"revision\":0,\"created\":1781024550711,\"version\":0},\"$loki\":33}"},{"eventId":15932647,"freeText":"Фора [a] (@NP@)","koef":"1","V":[3.6,1.23],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455260884,"fullJson":"{\"id\":1455260884,\"idEvent\":15932647,\"idTipMarket\":1073,\"koef\":\"1\",\"V\":[3.6,1.23],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024548630,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455260884,\"meta\":{\"revision\":0,\"created\":1781024550711,\"version\":0},\"$loki\":32}"},{"eventId":15932647,"freeText":"@1HT@ фора [a]","koef":"0","V":[1.79,1.91],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":988,"idLine":1455260883,"fullJson":"{\"id\":1455260883,\"idEvent\":15932647,\"idTipMarket\":988,\"koef\":\"0\",\"V\":[1.79,1.91],\"countV\":2,\"favorite\":0,\"tipLine\":{\"id\":988,\"sports\":\"1,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":6,\"freeTextR\":\"@1HT@ фора [a]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":6,\"meta\":{\"revision\":0,\"created\":1781024548630,\"version\":0},\"$loki\":307},\"isLive\":1,\"state\":1,\"idLine\":1455260883,\"meta\":{\"revision\":0,\"created\":1781024550711,\"version\":0},\"$loki\":31}"},{"eventId":15932647,"freeText":"Фора [a] (@NP@)","koef":"1","V":[1.19,4],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455260882,"fullJson":"{\"id\":1455260882,\"idEvent\":15932647,\"idTipMarket\":1073,\"koef\":\"1\",\"V\":[1.19,4],\"countV\":2,\"favorite\":2,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024548630,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455260882,\"meta\":{\"revision\":0,\"created\":1781024550711,\"version\":0},\"$loki\":30}"},{"eventId":15932647,"freeText":"@2HT@ фора [a]","koef":"0","V":[1.83,1.87],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":958,"idLine":1455260873,"fullJson":"{\"id\":1455260873,\"idEvent\":15932647,\"idTipMarket\":958,\"koef\":\"0\",\"V\":[1.83,1.87],\"countV\":2,\"favorite\":0,\"tipLine\":{\"id\":958,\"sports\":\"\",\"favorite\":1,\"countV\":2,\"idTipEvent\":16,\"freeTextR\":\"@2HT@ фора [a]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":16,\"meta\":{\"revision\":0,\"created\":1781024548630,\"version\":0},\"$loki\":280},\"isLive\":1,\"state\":1,\"idLine\":1455260873,\"meta\":{\"revision\":0,\"created\":1781024550711,\"version\":0},\"$loki\":21}"},{"eventId":15932647,"freeText":"Фора [a] (@NP@)","koef":"0","V":[1.79,1.91],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455260855,"fullJson":"{\"id\":1455260855,\"idEvent\":15932647,\"idTipMarket\":1073,\"koef\":\"0\",\"V\":[1.79,1.91],\"countV\":2,\"favorite\":0,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024548630,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455260855,\"meta\":{\"revision\":0,\"created\":1781024550711,\"version\":0},\"$loki\":3}"}]
2026-06-09T17:03:05.872Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.service.WinlineApiClient       : Successfully extracted 209 Winline live events with 18317 total odds
2026-06-09T17:03:07.865Z  WARN 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.c.s.SportNormalizationService    : ALARM ALARM new sport: bookmaker='winline', sportName='Падел'
2026-06-09T17:03:09.786Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.c.engine.AbstractMatchService    : Discovered 209 live winline events
2026-06-09T17:03:09.787Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled live fetch completed: 209 events discovered
2026-06-09T17:03:32.624Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T17:03:36.424Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 970 (service: igaming-source-winline, host: winline.ru)
2026-06-09T17:04:09.787Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled: fetching live data from winline.ru...
2026-06-09T17:04:09.787Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Fetching Winline live events via apiWlb SAPI from https://winline.ru/live
2026-06-09T17:04:10.139Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Navigating to https://winline.ru/live...
2026-06-09T17:04:36.425Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T17:04:40.670Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 970 (service: igaming-source-winline, host: winline.ru)
2026-06-09T17:05:01.111Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Waiting for apiWlb to be available...
2026-06-09T17:05:04.214Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Executing SAPI extraction script for live events...
2026-06-09T17:05:08.804Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : DIAG_PARAM: [{"eventId":15932647,"freeText":"Фора [a] (@NP@)","koef":"1.5","V":[4.4,1.16],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455260898,"fullJson":"{\"id\":1455260898,\"idEvent\":15932647,\"idTipMarket\":1073,\"koef\":\"1.5\",\"V\":[4.4,1.16],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024670575,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455260898,\"meta\":{\"revision\":2,\"created\":1781024679632,\"version\":0,\"updated\":1781024706754},\"$loki\":46}"},{"eventId":15932647,"freeText":"@1HT@ фора [a]","koef":"1","V":[6.85,1.07],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":988,"idLine":1455260885,"fullJson":"{\"id\":1455260885,\"idEvent\":15932647,\"idTipMarket\":988,\"koef\":\"1\",\"V\":[6.85,1.07],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":988,\"sports\":\"1,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":6,\"freeTextR\":\"@1HT@ фора [a]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":6,\"meta\":{\"revision\":0,\"created\":1781024670574,\"version\":0},\"$loki\":307},\"isLive\":1,\"state\":1,\"idLine\":1455260885,\"meta\":{\"revision\":2,\"created\":1781024679632,\"version\":0,\"updated\":1781024706753},\"$loki\":33}"},{"eventId":15932647,"freeText":"Фора [a] (@NP@)","koef":"1","V":[3.7,1.22],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455260884,"fullJson":"{\"id\":1455260884,\"idEvent\":15932647,\"idTipMarket\":1073,\"koef\":\"1\",\"V\":[3.7,1.22],\"countV\":2,\"favorite\":1,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024670575,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455260884,\"meta\":{\"revision\":2,\"created\":1781024679632,\"version\":0,\"updated\":1781024706753},\"$loki\":32}"},{"eventId":15932647,"freeText":"@1HT@ фора [a]","koef":"0","V":[1.78,1.92],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":988,"idLine":1455260883,"fullJson":"{\"id\":1455260883,\"idEvent\":15932647,\"idTipMarket\":988,\"koef\":\"0\",\"V\":[1.78,1.92],\"countV\":2,\"favorite\":0,\"tipLine\":{\"id\":988,\"sports\":\"1,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":6,\"freeTextR\":\"@1HT@ фора [a]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":6,\"meta\":{\"revision\":0,\"created\":1781024670574,\"version\":0},\"$loki\":307},\"isLive\":1,\"state\":1,\"idLine\":1455260883,\"meta\":{\"revision\":2,\"created\":1781024679632,\"version\":0,\"updated\":1781024706753},\"$loki\":31}"},{"eventId":15932647,"freeText":"Фора [a] (@NP@)","koef":"1","V":[1.19,4],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455260882,"fullJson":"{\"id\":1455260882,\"idEvent\":15932647,\"idTipMarket\":1073,\"koef\":\"1\",\"V\":[1.19,4],\"countV\":2,\"favorite\":2,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024670575,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455260882,\"meta\":{\"revision\":1,\"created\":1781024679632,\"version\":0,\"updated\":1781024706753},\"$loki\":30}"},{"eventId":15932647,"freeText":"@2HT@ фора [a]","koef":"0","V":[1.82,1.88],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":958,"idLine":1455260873,"fullJson":"{\"id\":1455260873,\"idEvent\":15932647,\"idTipMarket\":958,\"koef\":\"0\",\"V\":[1.82,1.88],\"countV\":2,\"favorite\":0,\"tipLine\":{\"id\":958,\"sports\":\"\",\"favorite\":1,\"countV\":2,\"idTipEvent\":16,\"freeTextR\":\"@2HT@ фора [a]\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":16,\"meta\":{\"revision\":0,\"created\":1781024670574,\"version\":0},\"$loki\":280},\"isLive\":1,\"state\":1,\"idLine\":1455260873,\"meta\":{\"revision\":2,\"created\":1781024679632,\"version\":0,\"updated\":1781024706753},\"$loki\":21}"},{"eventId":15932647,"freeText":"Фора [a] (@NP@)","koef":"0","V":[1.79,1.91],"countV":2,"labels":["1","2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""],"idTipMarket":1073,"idLine":1455260855,"fullJson":"{\"id\":1455260855,\"idEvent\":15932647,\"idTipMarket\":1073,\"koef\":\"0\",\"V\":[1.79,1.91],\"countV\":2,\"favorite\":0,\"tipLine\":{\"id\":1073,\"sports\":\"1,2,3,4,5,6,12,13,15,16,19,20,23,24,26,28,29,31,32,34,60,71,155,157,192,193,194,195,196,205,\",\"favorite\":1,\"countV\":2,\"idTipEvent\":3,\"freeTextR\":\"Фора [a] (@NP@)\",\"R\":[\"1\",\"2\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"],\"idTipEventSrc\":3,\"meta\":{\"revision\":0,\"created\":1781024670575,\"version\":0},\"$loki\":634},\"isLive\":1,\"state\":1,\"idLine\":1455260855,\"meta\":{\"revision\":1,\"created\":1781024679632,\"version\":0,\"updated\":1781024706753},\"$loki\":3}"}]
2026-06-09T17:05:08.852Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.service.WinlineApiClient       : Successfully extracted 207 Winline live events with 18580 total odds
2026-06-09T17:05:12.217Z  WARN 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.c.s.SportNormalizationService    : ALARM ALARM new sport: bookmaker='winline', sportName='Падел'
2026-06-09T17:05:14.651Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.c.engine.AbstractMatchService    : Discovered 207 live winline events
2026-06-09T17:05:14.651Z  INFO 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.w.scheduler.MatchFetchScheduler  : Scheduled live fetch completed: 207 events discovered

```
#### 📥 Loader (`igaming-source-winline-loader-587d8b78db-tzklk` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:03:09.672Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TEAM1_TOTAL_OVER_0.5, ExistingValue=1.41, NewValue=2.51, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал [a] команды 1', NewGroup='1-й тайм тотал карточек [a] команды 1'
2026-06-09T17:03:09.672Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TEAM1_TOTAL_UNDER_0.5, ExistingValue=2.57, NewValue=1.43, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал [a] команды 1', NewGroup='1-й тайм тотал карточек [a] команды 1'
2026-06-09T17:03:09.672Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TOTAL_OVER_1.5, ExistingValue=2.44, NewValue=2.54, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал [a]', NewGroup='1-й тайм тотал карточек [a]'
2026-06-09T17:03:09.673Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TOTAL_UNDER_1.5, ExistingValue=1.48, NewValue=1.41, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал [a]', NewGroup='1-й тайм тотал карточек [a]'
2026-06-09T17:03:09.673Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TOTAL_OVER_0.5, ExistingValue=1.26, NewValue=1.37, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал [a]', NewGroup='1-й тайм тотал карточек [a]'
2026-06-09T17:03:09.673Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TOTAL_UNDER_0.5, ExistingValue=3.26, NewValue=2.71, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал [a]', NewGroup='1-й тайм тотал карточек [a]'
2026-06-09T17:03:09.673Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TEAM2_TOTAL_OVER_1.5, ExistingValue=5.85, NewValue=1.41, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a] команды 2', NewGroup='Тотал карточек [a] команды 2'
2026-06-09T17:03:09.673Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TEAM2_TOTAL_UNDER_1.5, ExistingValue=1.08, NewValue=2.57, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a] команды 2', NewGroup='Тотал карточек [a] команды 2'
2026-06-09T17:03:09.673Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TEAM1_TOTAL_OVER_1.5, ExistingValue=1.37, NewValue=2.51, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a] команды 1', NewGroup='Тотал карточек [a] команды 1'
2026-06-09T17:03:09.673Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TEAM1_TOTAL_UNDER_1.5, ExistingValue=2.72, NewValue=1.42, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a] команды 1', NewGroup='Тотал карточек [a] команды 1'
2026-06-09T17:03:09.673Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_OVER_4.5, ExistingValue=4.63, NewValue=3.07, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:03:09.673Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_UNDER_4.5, ExistingValue=1.14, NewValue=1.27, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:03:09.737Z ERROR 1 --- [igaming-source-winline] [   virtual-2830] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15972633, SemanticKey=HALFTIME_TOTAL_OVER_0.5, ExistingValue=1.56, NewValue=1.31, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [a]'
2026-06-09T17:03:09.737Z ERROR 1 --- [igaming-source-winline] [   virtual-2830] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15972633, SemanticKey=HALFTIME_TOTAL_UNDER_0.5, ExistingValue=2.22, NewValue=2.96, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [a]'
2026-06-09T17:03:09.780Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_OVER_3.5, ExistingValue=2.63, NewValue=2.13, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:03:09.781Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_UNDER_3.5, ExistingValue=1.42, NewValue=1.61, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:03:09.781Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_OVER_2.5, ExistingValue=1.68, NewValue=1.44, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:03:09.781Z ERROR 1 --- [igaming-source-winline] [   virtual-2819] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_UNDER_2.5, ExistingValue=2.13, NewValue=2.35, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:03:09.834Z ERROR 1 --- [igaming-source-winline] [   virtual-2830] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15972633, SemanticKey=TOTAL_OVER_2.5, ExistingValue=1.85, NewValue=1.75, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:03:09.834Z ERROR 1 --- [igaming-source-winline] [   virtual-2830] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15972633, SemanticKey=TOTAL_UNDER_2.5, ExistingValue=1.85, NewValue=1.95, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:03:10.309Z ERROR 1 --- [igaming-source-winline] [   virtual-2820] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TOTAL_OVER_1.5, ExistingValue=4.42, NewValue=7.37, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [a]'
2026-06-09T17:03:10.310Z ERROR 1 --- [igaming-source-winline] [   virtual-2820] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TOTAL_UNDER_1.5, ExistingValue=1.12, NewValue=1.04, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [a]'
2026-06-09T17:03:10.310Z ERROR 1 --- [igaming-source-winline] [   virtual-2820] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TOTAL_OVER_0.5, ExistingValue=1.94, NewValue=2.44, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [a]'
2026-06-09T17:03:10.310Z ERROR 1 --- [igaming-source-winline] [   virtual-2820] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TOTAL_UNDER_0.5, ExistingValue=1.77, NewValue=1.48, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [a]'
2026-06-09T17:03:10.830Z ERROR 1 --- [igaming-source-winline] [   virtual-2818] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15976862, SemanticKey=TEAM2_TOTAL_OVER_1.5, ExistingValue=7.88, NewValue=1.38, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a] команды 2', NewGroup='Тотал карточек [a] команды 2'
2026-06-09T17:03:10.830Z ERROR 1 --- [igaming-source-winline] [   virtual-2818] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15976862, SemanticKey=TEAM2_TOTAL_UNDER_1.5, ExistingValue=1.03, NewValue=2.65, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a] команды 2', NewGroup='Тотал карточек [a] команды 2'
2026-06-09T17:03:10.830Z ERROR 1 --- [igaming-source-winline] [   virtual-2818] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15976862, SemanticKey=TOTAL_OVER_3.5, ExistingValue=11.5, NewValue=2.57, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:03:10.830Z ERROR 1 --- [igaming-source-winline] [   virtual-2818] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15976862, SemanticKey=TOTAL_OVER_2.5, ExistingValue=6.87, NewValue=1.55, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:03:10.830Z ERROR 1 --- [igaming-source-winline] [   virtual-2818] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15976862, SemanticKey=TOTAL_UNDER_2.5, ExistingValue=1.05, NewValue=2.18, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:03:11.580Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 winline matches
2026-06-09T17:03:14.484Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 winline matches
2026-06-09T17:03:16.592Z  WARN 1 --- [igaming-source-winline] [   virtual-2885] p.d.i.s.c.s.SportNormalizationService    : ALARM ALARM new sport: bookmaker='winline', sportName='Падел'
2026-06-09T17:03:16.655Z ERROR 1 --- [igaming-source-winline] [   virtual-2880] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15964319, SemanticKey=SET_3_TEAM2_TOTAL_OVER_1.5, ExistingValue=11.0, NewValue=1.9, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='3-й сет тотал [b] брейков команды 2', NewGroup='3-й сет тотал [b] эйсов команды 2'
2026-06-09T17:03:16.999Z ERROR 1 --- [igaming-source-winline] [   virtual-2896] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981755, SemanticKey=PERIOD_2_HANDICAP_1_-2.5, ExistingValue=2.42, NewValue=2.74, ExistingOriginalName='1', NewOriginalName='1', ExistingGroup='2-й период фора [b] раундов', NewGroup='2-й период 2 половина фора [b]'
2026-06-09T17:03:16.999Z ERROR 1 --- [igaming-source-winline] [   virtual-2896] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981755, SemanticKey=PERIOD_2_HANDICAP_2_2.5, ExistingValue=1.47, NewValue=1.4, ExistingOriginalName='2', NewOriginalName='2', ExistingGroup='2-й период фора [b] раундов', NewGroup='2-й период 2 половина фора [b]'
2026-06-09T17:03:17.109Z ERROR 1 --- [igaming-source-winline] [   virtual-2896] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981755, SemanticKey=PERIOD_3_HANDICAP_1_2.5, ExistingValue=1.44, NewValue=1.74, ExistingOriginalName='1', NewOriginalName='1', ExistingGroup='3-й период 2 половина фора [b]', NewGroup='3-й период фора [b] раундов'
2026-06-09T17:03:17.109Z ERROR 1 --- [igaming-source-winline] [   virtual-2896] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981755, SemanticKey=PERIOD_3_HANDICAP_2_-2.5, ExistingValue=2.6, NewValue=1.98, ExistingOriginalName='2', NewOriginalName='2', ExistingGroup='3-й период 2 половина фора [b]', NewGroup='3-й период фора [b] раундов'
2026-06-09T17:03:17.135Z ERROR 1 --- [igaming-source-winline] [   virtual-2896] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981755, SemanticKey=PERIOD_1_HANDICAP_1_2.5, ExistingValue=1.47, NewValue=1.57, ExistingOriginalName='1', NewOriginalName='1', ExistingGroup='1-й период фора [b] раундов', NewGroup='1-й период 2 половина фора [b]'
2026-06-09T17:03:17.135Z ERROR 1 --- [igaming-source-winline] [   virtual-2896] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981755, SemanticKey=PERIOD_1_HANDICAP_2_-2.5, ExistingValue=2.42, NewValue=2.25, ExistingOriginalName='2', NewOriginalName='2', ExistingGroup='1-й период фора [b] раундов', NewGroup='1-й период 2 половина фора [b]'
2026-06-09T17:03:17.135Z ERROR 1 --- [igaming-source-winline] [   virtual-2896] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981755, SemanticKey=PERIOD_1_HANDICAP_1_3.5, ExistingValue=1.27, NewValue=1.42, ExistingOriginalName='1', NewOriginalName='1', ExistingGroup='1-й период фора [b] раундов', NewGroup='1-й период 2 половина фора [b]'
2026-06-09T17:03:17.135Z ERROR 1 --- [igaming-source-winline] [   virtual-2896] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981755, SemanticKey=PERIOD_1_HANDICAP_2_-3.5, ExistingValue=3.23, NewValue=2.65, ExistingOriginalName='2', NewOriginalName='2', ExistingGroup='1-й период фора [b] раундов', NewGroup='1-й период 2 половина фора [b]'
2026-06-09T17:03:17.194Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 winline matches
2026-06-09T17:03:19.939Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 30 winline matches
2026-06-09T17:03:22.290Z  INFO 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 16 winline matches
2026-06-09T17:03:53.544Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T17:03:59.634Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-winline, host: winline.ru)
2026-06-09T17:04:59.635Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'winline.ru' is still reachable...
2026-06-09T17:05:04.348Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 1175 (service: igaming-source-winline, host: winline.ru)
2026-06-09T17:05:10.011Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=TOTAL_OVER_3.5, ExistingValue=3.21, NewValue=8.1, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:10.011Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=TOTAL_UNDER_3.5, ExistingValue=1.25, NewValue=1.03, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:10.011Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=TOTAL_OVER_2.5, ExistingValue=2.05, NewValue=4.11, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:10.011Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=TOTAL_UNDER_2.5, ExistingValue=1.67, NewValue=1.18, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:10.011Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=TEAM2_TOTAL_OVER_1.5, ExistingValue=2.55, NewValue=4.07, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал карточек [a] команды 2', NewGroup='Тотал [a] команды 2'
2026-06-09T17:05:10.011Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=TEAM2_TOTAL_UNDER_1.5, ExistingValue=1.41, NewValue=1.18, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал карточек [a] команды 2', NewGroup='Тотал [a] команды 2'
2026-06-09T17:05:10.011Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=TOTAL_OVER_1.5, ExistingValue=1.32, NewValue=2.03, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:10.011Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=TOTAL_UNDER_1.5, ExistingValue=2.74, NewValue=1.77, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:10.123Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=TEAM1_TOTAL_OVER_1.5, ExistingValue=2.29, NewValue=5.06, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал карточек [a] команды 1', NewGroup='Тотал [a] команды 1'
2026-06-09T17:05:10.123Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=TEAM1_TOTAL_UNDER_1.5, ExistingValue=1.51, NewValue=1.12, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал карточек [a] команды 1', NewGroup='Тотал [a] команды 1'
2026-06-09T17:05:10.201Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=HALFTIME_TEAM2_TOTAL_OVER_0.5, ExistingValue=8.6, NewValue=1.56, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал [b] команды 2', NewGroup='1-й тайм тотал карточек [a] команды 2'
2026-06-09T17:05:11.515Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TEAM2_TOTAL_OVER_0.5, ExistingValue=2.96, NewValue=3.94, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал карточек [a] команды 2', NewGroup='1-й тайм тотал [a] команды 2'
2026-06-09T17:05:11.517Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TEAM2_TOTAL_UNDER_0.5, ExistingValue=1.25, NewValue=1.19, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал карточек [a] команды 2', NewGroup='1-й тайм тотал [a] команды 2'
2026-06-09T17:05:11.517Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TEAM1_TOTAL_OVER_0.5, ExistingValue=2.72, NewValue=5.06, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал карточек [a] команды 1', NewGroup='1-й тайм тотал [a] команды 1'
2026-06-09T17:05:11.517Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TEAM1_TOTAL_UNDER_0.5, ExistingValue=1.29, NewValue=1.12, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал карточек [a] команды 1', NewGroup='1-й тайм тотал [a] команды 1'
2026-06-09T17:05:11.543Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=TOTAL_OVER_4.5, ExistingValue=2.92, NewValue=1.55, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:11.544Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=TOTAL_UNDER_4.5, ExistingValue=1.31, NewValue=2.28, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:11.544Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=TOTAL_OVER_3.5, ExistingValue=2.01, NewValue=1.23, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:11.544Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=TOTAL_UNDER_3.5, ExistingValue=1.71, NewValue=3.46, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:11.568Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TOTAL_OVER_1.5, ExistingValue=4.54, NewValue=8.5, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [a]'
2026-06-09T17:05:11.569Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TOTAL_OVER_0.5, ExistingValue=2.01, NewValue=2.72, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [a]'
2026-06-09T17:05:11.569Z ERROR 1 --- [igaming-source-winline] [   virtual-2966] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877568, SemanticKey=HALFTIME_TOTAL_UNDER_0.5, ExistingValue=1.69, NewValue=1.39, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [a]'
2026-06-09T17:05:11.901Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=TOTAL_OVER_2.5, ExistingValue=1.08, NewValue=1.37, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:05:11.901Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=TOTAL_UNDER_2.5, ExistingValue=5.7, NewValue=2.68, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:05:11.901Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=HALFTIME_TOTAL_OVER_1.5, ExistingValue=1.98, NewValue=1.37, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [b]'
2026-06-09T17:05:11.901Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=HALFTIME_TOTAL_UNDER_1.5, ExistingValue=1.74, NewValue=2.72, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал карточек [a]', NewGroup='1-й тайм тотал [b]'
2026-06-09T17:05:12.218Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=TEAM2_TOTAL_OVER_1.5, ExistingValue=1.56, NewValue=7.65, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал карточек [a] команды 2', NewGroup='Тотал [a] команды 2'
2026-06-09T17:05:12.218Z ERROR 1 --- [igaming-source-winline] [   virtual-2964] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15799937, SemanticKey=TEAM2_TOTAL_UNDER_1.5, ExistingValue=2.26, NewValue=1.04, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал карточек [a] команды 2', NewGroup='Тотал [a] команды 2'
2026-06-09T17:05:12.403Z ERROR 1 --- [igaming-source-winline] [   virtual-2977] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15972633, SemanticKey=HALFTIME_TOTAL_OVER_0.5, ExistingValue=1.31, NewValue=1.57, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал [a]', NewGroup='1-й тайм тотал карточек [a]'
2026-06-09T17:05:12.403Z ERROR 1 --- [igaming-source-winline] [   virtual-2977] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15972633, SemanticKey=HALFTIME_TOTAL_UNDER_0.5, ExistingValue=2.93, NewValue=2.2, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал [a]', NewGroup='1-й тайм тотал карточек [a]'
2026-06-09T17:05:12.420Z ERROR 1 --- [igaming-source-winline] [   virtual-2977] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15972633, SemanticKey=TOTAL_OVER_2.5, ExistingValue=1.86, NewValue=1.75, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:12.420Z ERROR 1 --- [igaming-source-winline] [   virtual-2977] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15972633, SemanticKey=TOTAL_UNDER_2.5, ExistingValue=1.84, NewValue=1.95, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал карточек [a]', NewGroup='Тотал [a]'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TEAM2_TOTAL_OVER_0.5, ExistingValue=3.94, NewValue=1.77, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал [a] команды 2', NewGroup='1-й тайм тотал карточек [a] команды 2'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TEAM2_TOTAL_UNDER_0.5, ExistingValue=1.19, NewValue=1.93, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал [a] команды 2', NewGroup='1-й тайм тотал карточек [a] команды 2'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TEAM1_TOTAL_OVER_0.5, ExistingValue=1.46, NewValue=2.47, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал [a] команды 1', NewGroup='1-й тайм тотал карточек [a] команды 1'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TEAM1_TOTAL_UNDER_0.5, ExistingValue=2.42, NewValue=1.44, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал [a] команды 1', NewGroup='1-й тайм тотал карточек [a] команды 1'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TOTAL_OVER_1.5, ExistingValue=2.63, NewValue=2.47, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал [a]', NewGroup='1-й тайм тотал карточек [a]'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TOTAL_UNDER_1.5, ExistingValue=1.42, NewValue=1.44, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал [a]', NewGroup='1-й тайм тотал карточек [a]'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TOTAL_OVER_0.5, ExistingValue=1.31, NewValue=1.36, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='1-й тайм тотал [a]', NewGroup='1-й тайм тотал карточек [a]'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=HALFTIME_TOTAL_UNDER_0.5, ExistingValue=3.0, NewValue=2.74, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='1-й тайм тотал [a]', NewGroup='1-й тайм тотал карточек [a]'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TEAM2_TOTAL_OVER_1.5, ExistingValue=6.33, NewValue=1.41, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a] команды 2', NewGroup='Тотал карточек [a] команды 2'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TEAM2_TOTAL_UNDER_1.5, ExistingValue=1.07, NewValue=2.57, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a] команды 2', NewGroup='Тотал карточек [a] команды 2'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TEAM1_TOTAL_OVER_1.5, ExistingValue=1.4, NewValue=2.51, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a] команды 1', NewGroup='Тотал карточек [a] команды 1'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TEAM1_TOTAL_UNDER_1.5, ExistingValue=2.6, NewValue=1.42, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a] команды 1', NewGroup='Тотал карточек [a] команды 1'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_OVER_4.5, ExistingValue=5.02, NewValue=3.07, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:05:12.899Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_UNDER_4.5, ExistingValue=1.12, NewValue=1.27, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:05:12.920Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_OVER_2.5, ExistingValue=1.77, NewValue=1.43, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:05:12.920Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_UNDER_2.5, ExistingValue=2.03, NewValue=2.38, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:05:13.037Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_OVER_3.5, ExistingValue=2.82, NewValue=2.11, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:05:13.038Z ERROR 1 --- [igaming-source-winline] [   virtual-2965] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15877570, SemanticKey=TOTAL_UNDER_3.5, ExistingValue=1.37, NewValue=1.62, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a]', NewGroup='Тотал карточек [a]'
2026-06-09T17:05:13.098Z  INFO 1 --- [igaming-source-winline] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 20 winline matches
2026-06-09T17:05:14.896Z  INFO 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.c.aggregator.AggregatorClient    : Heartbeat sent to aggregator: bookmaker='winline' name='Winline'

```

---

### 🗂️ Букмекер: `zenit`

#### 🕷️ Crawler (`igaming-source-zenit-crawler-7ff978fd4b-twkc2` - Running)
**Анализ ошибок:**
```text
2026-06-09T17:04:02.264Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161492: c2_id 1000734 mapped to team2 name: Юлия Грабер
2026-06-09T17:04:02.328Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161691: c1_id = 1045489, c2_id = 997753, team1 = null, team2 = null
2026-06-09T17:04:02.328Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161691: c1_id 1045489 mapped to team1 name: Джорджия Педоне
2026-06-09T17:04:02.328Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161691: c2_id 997753 mapped to team2 name: Майар Шериф
2026-06-09T17:04:02.379Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25157698: c1_id = 1016408, c2_id = 1006635, team1 = null, team2 = null
2026-06-09T17:04:02.379Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25157698: c1_id 1016408 mapped to team1 name: Никола Бартункова
2026-06-09T17:04:02.379Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25157698: c2_id 1006635 mapped to team2 name: Ханне Вандевинкель
2026-06-09T17:04:02.431Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25158851: c1_id = 989698, c2_id = 989371, team1 = null, team2 = null
2026-06-09T17:04:02.431Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25158851: c1_id 989698 mapped to team1 name: Эмма Наварро
2026-06-09T17:04:02.431Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25158851: c2_id 989371 mapped to team2 name: Кэтрин Макналли
2026-06-09T17:04:02.481Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162658: c1_id = 1022631, c2_id = 1262791, team1 = null, team2 = null
2026-06-09T17:04:02.481Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162658: c1_id 1022631 mapped to team1 name: Артем Шкурко
2026-06-09T17:04:02.481Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162658: c2_id 1262791 mapped to team2 name: Вячеслав Селезнев
2026-06-09T17:04:02.581Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162385: c1_id = 536332, c2_id = 1217107, team1 = null, team2 = null
2026-06-09T17:04:02.581Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162385: c1_id 536332 mapped to team1 name: BIG
2026-06-09T17:04:02.581Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162385: c2_id 1217107 mapped to team2 name: B8 Esports
2026-06-09T17:04:02.654Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : [zenit] Discovered 20 live events
2026-06-09T17:04:02.654Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.z.scheduler.MatchFetchScheduler  : Finished LIVE discovery job, discovered: 20
2026-06-09T17:04:21.751Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-2] p.d.i.s.z.scheduler.MatchFetchScheduler  : Starting scheduled PREMATCH fetch job
2026-06-09T17:04:21.751Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-2] p.d.i.s.zenit.service.ZenitApiClient     : Fetching PREMATCH from https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50
2026-06-09T17:04:36.451Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Navigation timed out for https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50
2026-06-09T17:04:36.638Z ERROR 1 --- [igaming-source-zenit] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50: Error {
  message='net::ERR_HTTP_RESPONSE_CODE_FAILURE at https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50
  name='Error
  stack='Error: net::ERR_HTTP_RESPONSE_CODE_FAILURE at https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50
    at FrameSession._navigate (/tmp/playwright-java-11125919211789367507/package/lib/server/chromium/crPage.js:512:35)
    at async Frame._gotoAction (/tmp/playwright-java-11125919211789367507/package/lib/server/frames.js:534:28)
}
Call log:
- navigating to "https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50", waiting until "domcontentloaded"

2026-06-09T17:04:36.638Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-2] p.d.i.s.zenit.service.ZenitApiClient     : Failed to fetch data, empty response
2026-06-09T17:04:57.406Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy validation: checking if target host 'zenit.win' is still reachable...
2026-06-09T17:05:01.043Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Reported success for config 974 (service: igaming-source-zenit, host: zenit.win)
2026-06-09T17:05:02.656Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.z.scheduler.MatchFetchScheduler  : Starting scheduled LIVE fetch job
2026-06-09T17:05:02.656Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.zenit.service.ZenitApiClient     : Fetching LIVE from https://zenit.win/ajax/live/printer/ranked/1781024702?onlyview=0&lang_id=1&timezone=3
2026-06-09T17:05:04.949Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit discover: cmdDict is null = false, sportDict is null = false
2026-06-09T17:05:04.949Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit discover: cmdDict size = 518, sample keys: [1016408, 1006635, 989698, 989371, 991048]
2026-06-09T17:05:04.949Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161647: c1_id = 70190, c2_id = 29835, team1 = null, team2 = null
2026-06-09T17:05:04.949Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161647: c1_id 70190 mapped to team1 name: ДР Конго
2026-06-09T17:05:04.949Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161647: c2_id 29835 mapped to team2 name: Чили
2026-06-09T17:05:05.088Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161644: c1_id = 27736, c2_id = 29841, team1 = null, team2 = null
2026-06-09T17:05:05.088Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161644: c1_id 27736 mapped to team1 name: Эстония
2026-06-09T17:05:05.088Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161644: c2_id 29841 mapped to team2 name: Литва
2026-06-09T17:05:05.162Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162038: c1_id = 32687, c2_id = 63033, team1 = null, team2 = null
2026-06-09T17:05:05.162Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162038: c1_id 32687 mapped to team1 name: Беларусь
2026-06-09T17:05:05.162Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162038: c2_id 63033 mapped to team2 name: Буркина Фасо
2026-06-09T17:05:05.209Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162621: c1_id = 29998, c2_id = 27933, team1 = null, team2 = null
2026-06-09T17:05:05.210Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162621: c1_id 29998 mapped to team1 name: Венгрия
2026-06-09T17:05:05.210Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162621: c2_id 27933 mapped to team2 name: Казахстан
2026-06-09T17:05:05.274Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162362: c1_id = 27990, c2_id = 105096, team1 = null, team2 = null
2026-06-09T17:05:05.274Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162362: c1_id 27990 mapped to team1 name: Россия
2026-06-09T17:05:05.274Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162362: c2_id 105096 mapped to team2 name: Тринидад и Тобаго
2026-06-09T17:05:05.329Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161286: c1_id = 479544, c2_id = 146540, team1 = null, team2 = null
2026-06-09T17:05:05.329Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161286: c1_id 479544 mapped to team1 name: Элитцур Ирони Нетанья
2026-06-09T17:05:05.329Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161286: c2_id 146540 mapped to team2 name: Ирони Нес-Циона
2026-06-09T17:05:05.436Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162673: c1_id = 193014, c2_id = 358885, team1 = null, team2 = null
2026-06-09T17:05:05.436Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162673: c1_id 193014 mapped to team1 name: Маккаби Раанана
2026-06-09T17:05:05.436Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162673: c2_id 358885 mapped to team2 name: Хапоэль Галиль-Элион
2026-06-09T17:05:05.498Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161510: c1_id = 920238, c2_id = 26147, team1 = null, team2 = null
2026-06-09T17:05:05.498Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161510: c1_id 920238 mapped to team1 name: Ювентус Утена
2026-06-09T17:05:05.498Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161510: c2_id 26147 mapped to team2 name: Жальгирис
2026-06-09T17:05:05.551Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162674: c1_id = 472137, c2_id = 484939, team1 = null, team2 = null
2026-06-09T17:05:05.551Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162674: c1_id 472137 mapped to team1 name: Арка Гдыня
2026-06-09T17:05:05.551Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162674: c2_id 484939 mapped to team2 name: Джики Варшава
2026-06-09T17:05:05.620Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162622: c1_id = 59934, c2_id = 1100583, team1 = null, team2 = null
2026-06-09T17:05:05.620Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162622: c1_id 59934 mapped to team1 name: Локомотив-Кубань
2026-06-09T17:05:05.620Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162622: c2_id 1100583 mapped to team2 name: Зенит СПб
2026-06-09T17:05:05.682Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25159926: c1_id = 991048, c2_id = 990457, team1 = null, team2 = null
2026-06-09T17:05:05.682Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25159926: c1_id 991048 mapped to team1 name: Хуберт Хуркач
2026-06-09T17:05:05.683Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25159926: c2_id 990457 mapped to team2 name: Мартон Фучович
2026-06-09T17:05:05.740Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162070: c1_id = 1170664, c2_id = 995212, team1 = null, team2 = null
2026-06-09T17:05:05.741Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162070: c1_id 1170664 mapped to team1 name: Томас Форел
2026-06-09T17:05:05.741Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162070: c2_id 995212 mapped to team2 name: Флоран Бакс
2026-06-09T17:05:05.791Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25160349: c1_id = 990466, c2_id = 992729, team1 = null, team2 = null
2026-06-09T17:05:05.791Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25160349: c1_id 990466 mapped to team1 name: Даниель Альтмайер
2026-06-09T17:05:05.791Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25160349: c2_id 992729 mapped to team2 name: Фрэнсис Тьяфу
2026-06-09T17:05:05.894Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161583: c1_id = 1167249, c2_id = 1001793, team1 = null, team2 = null
2026-06-09T17:05:05.894Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161583: c1_id 1167249 mapped to team1 name: Мика Стоиславлевич
2026-06-09T17:05:05.894Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161583: c2_id 1001793 mapped to team2 name: Донна Векич
2026-06-09T17:05:05.955Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161492: c1_id = 998339, c2_id = 1000734, team1 = null, team2 = null
2026-06-09T17:05:05.956Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161492: c1_id 998339 mapped to team1 name: Андреа Ласаро Гарсия
2026-06-09T17:05:05.956Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161492: c2_id 1000734 mapped to team2 name: Юлия Грабер
2026-06-09T17:05:06.022Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161691: c1_id = 1045489, c2_id = 997753, team1 = null, team2 = null
2026-06-09T17:05:06.023Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161691: c1_id 1045489 mapped to team1 name: Джорджия Педоне
2026-06-09T17:05:06.023Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25161691: c2_id 997753 mapped to team2 name: Майар Шериф
2026-06-09T17:05:06.262Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25157698: c1_id = 1016408, c2_id = 1006635, team1 = null, team2 = null
2026-06-09T17:05:06.262Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25157698: c1_id 1016408 mapped to team1 name: Никола Бартункова
2026-06-09T17:05:06.262Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25157698: c2_id 1006635 mapped to team2 name: Ханне Вандевинкель
2026-06-09T17:05:06.323Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25158851: c1_id = 989698, c2_id = 989371, team1 = null, team2 = null
2026-06-09T17:05:06.323Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25158851: c1_id 989698 mapped to team1 name: Эмма Наварро
2026-06-09T17:05:06.323Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25158851: c2_id 989371 mapped to team2 name: Кэтрин Макналли
2026-06-09T17:05:06.379Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162658: c1_id = 1022631, c2_id = 1262791, team1 = null, team2 = null
2026-06-09T17:05:06.379Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162658: c1_id 1022631 mapped to team1 name: Артем Шкурко
2026-06-09T17:05:06.379Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162658: c2_id 1262791 mapped to team2 name: Вячеслав Селезнев
2026-06-09T17:05:06.429Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162385: c1_id = 536332, c2_id = 1217107, team1 = null, team2 = null
2026-06-09T17:05:06.429Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162385: c1_id 536332 mapped to team1 name: BIG
2026-06-09T17:05:06.429Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : Zenit event 25162385: c2_id 1217107 mapped to team2 name: B8 Esports
2026-06-09T17:05:06.480Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.e.z.s.ZenitEventDiscoverer     : [zenit] Discovered 20 live events
2026-06-09T17:05:06.480Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.z.scheduler.MatchFetchScheduler  : Finished LIVE discovery job, discovered: 20

```
#### 📥 Loader (`igaming-source-zenit-loader-7b788c4988-68h54` - Running)
**Анализ ошибок:**
```text
OK (No errors found in last 100 lines)
```
**Последние строки логов:**
```text
2026-06-09T17:05:05.888Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-1] .c.e.AbstractBookmakerPerformanceTracker : [zenit] Performance Report - Pages processed: 17, Skipped: 0, Not found: 0, Known errors: {}
2026-06-09T17:05:05.889Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 9 zenit matches
2026-06-09T17:05:09.016Z  INFO 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Match-loader processed 7 zenit matches
```

---

