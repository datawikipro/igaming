# 📋 Отчет о состоянии краулеров и лоадеров букмекеров

**Дата проверки:** 2026-06-05 02:04:28

## 📊 Сводная статистика

* **Всего букмекеров в отчете:** 30
* **🟢 Полностью исправные БК (без ошибок):** 4
* **🔴 БК с ошибками/сбоями:** 26
* **⚪ Не развернутые БК (нет подов):** 0

### 🔍 Обзор по всем букмекерам

| Букмекер | Статус | Краулер Pod (Готовность / Рестарты) | Лоадер Pod (Готовность / Рестарты) |
| :--- | :--- | :--- | :--- |
| `1xbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `888starz` | 🔴 | `0/2` (рестартов: 0) **Pending** | `2/2` (рестартов: 0) |
| `baltbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betandyou` | 🔴 | `2/2` (рестартов: 0) | `0/2` (рестартов: 0) **Pending** |
| `betboom` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betcity` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betcity-com` | 🟢 | - | `2/2` (рестартов: 0) |
| `betm` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 1) |
| `bettery` | 🔴 | `2/2` (рестартов: 9) | `2/2` (рестартов: 0) |
| `fansport` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `fon-bet-by` | 🔴 | `2/2` (рестартов: 5) | `2/2` (рестартов: 0) |
| `fon-bet-kz` | 🔴 | `2/2` (рестартов: 2) | `2/2` (рестартов: 0) |
| `fon-bet-ru` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `leon` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `ligastavok` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `linebet` | 🟢 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet-by` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet-com` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `megapari` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `melbet` | 🔴 | `2/2` (рестартов: 0) | `1/2` (рестартов: 0) |
| `olimpbet` | 🟢 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `olimpbet-kz` | 🟢 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `pari` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `pinnacle` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `sbobet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `sportbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `tennisi` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `winline` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `zenit` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |

---

## 🔴 Детализация ошибок и проблемных БК

### 🗂️ Букмекер: `1xbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-1xbet-crawler-54c96958df-9npl9` (2/2, Running, рестартов: 0, возраст: 1m)
  * **Ошибки в текущих логах:**
    ```text
HTML Error Response (Access Denied/Forbidden): Title='500 Internal Server Error', H1='500 Internal Server Error' (Contains HTTP status 500)
Sample log line: 2026-06-04T23:02:36.223Z  WARN 1 --- [igaming-source-1xbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: E
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-1xbet-loader-67475b9f49-5ljd2` (2/2, Running, рестартов: 0, возраст: 13m)
* **Pod:** `igaming-source-1xbet-loader-7c79666b76-gctn4` (1/2, Running, рестартов: 0, возраст: 1m)

---

### 🗂️ Букмекер: `888starz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-888starz-crawler-588c4dbf5d-gnnfj` (0/2, Pending, рестартов: 0, возраст: 1m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```
* **Pod:** `igaming-source-888starz-crawler-7ff8b7d5bf-xzhhn` (2/2, Running, рестартов: 0, возраст: 8m)
  * **Ошибки в текущих логах:**
    ```text
HTML Error Response (Access Denied/Forbidden): Title='HTML Page'
Sample log line: 2026-06-04T23:01:42.583Z  WARN 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch returned HTML i
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-888starz-loader-595d49f658-mzvhp` (2/2, Running, рестартов: 0, возраст: 17m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:57:17.086Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://888starz.bet: Read timed out
2026-06-04T22:57:17.086Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host '888starz.bet' is unreachable through current proxy! Triggering rotation...
    ```
* **Pod:** `igaming-source-888starz-loader-5bf66c68b8-4m6tz` (1/2, Running, рестартов: 0, возраст: 1m)

---

### 🗂️ Букмекер: `baltbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-baltbet-crawler-9fbc48887-9sg6c` (2/2, Running, рестартов: 0, возраст: 20m)
  * **Ошибки в текущих логах:**
    ```text
	transaction.timeout.ms = 60000
2026-06-04T22:52:58.513Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457450: Unable to connect to Redis
2026-06-04T22:52:58.646Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457451: Unable to connect to Redis
2026-06-04T22:52:58.737Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457339: Unable to connect to Redis
2026-06-04T22:52:59.164Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457478: Unable to connect to Redis
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-baltbet-loader-84fc95f496-4cjs4` (2/2, Running, рестартов: 0, возраст: 3h)

---

### 🗂️ Букмекер: `betandyou`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betandyou-crawler-789fbd875c-m8d64` (2/2, Running, рестартов: 0, возраст: 1m)
  * **Ошибки в текущих логах:**
    ```text
HTML Error Response (Access Denied/Forbidden): Title='500 Internal Server Error', H1='500 Internal Server Error' (Contains HTTP status 500)
Sample log line: 2026-06-04T23:02:47.939Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch faile
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betandyou-loader-6c94cb7f7-bttc8` (0/2, Pending, рестартов: 0, возраст: 1m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```
* **Pod:** `igaming-source-betandyou-loader-795cd985b7-9tfl4` (2/2, Running, рестартов: 0, возраст: 6m)

---

### 🗂️ Букмекер: `betboom`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betboom-crawler-685b8c5fc-kmlq8` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:03:05.091Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Error clicking/extracting sport Кибер: Object doesn't exist: frame@acf711c0e6ca742c04ec57ed62844d85
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betboom-loader-5d784467db-8dxn5` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:36:13.128Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://siteapi.betboom.ru: Read timed out
2026-06-04T22:36:13.128Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'siteapi.betboom.ru' is unreachable through current proxy! Triggering rotation...
2026-06-04T22:57:27.939Z  WARN 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": service-proxy-backend.service-proxy.svc.cluster.local:80 failed to respond
    ```

---

### 🗂️ Букмекер: `betcity`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betcity-crawler-55f6664474-qtlh8` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:03:04.655Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23214750: null
2026-06-04T23:03:06.814Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23214751: null
2026-06-04T23:03:07.187Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23221779: null
2026-06-04T23:03:09.372Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 22967425: null
2026-06-04T23:03:09.695Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23262287: null
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betcity-loader-785d5df9dd-ltskb` (2/2, Running, рестартов: 0, возраст: 3h)

---

### 🗂️ Букмекер: `betm`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betm-crawler-7897659bf6-cgwzs` (2/2, Running, рестартов: 0, возраст: 7h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:02:39.340Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258913, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.83, NewValue=2.96, ExistingName='X2', NewName='X2'
2026-06-04T23:02:42.299Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23258915: null
2026-06-04T23:02:43.077Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23259376: null
2026-06-04T23:02:43.440Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23259377: null
2026-06-04T23:02:44.282Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23259378: null
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betm-loader-d647dbbdf-h58jq` (2/2, Running, рестартов: 1, возраст: 10h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:02:42.613Z ERROR 1 --- [igaming-source-betcity] [    virtual-168] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258913, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.95, NewValue=1.76, ExistingName='Tm', NewName='Tm'
2026-06-04T23:02:42.635Z ERROR 1 --- [igaming-source-betcity] [    virtual-168] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258913, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.76, NewValue=1.95, ExistingName='Tb', NewName='Tb'
2026-06-04T23:02:42.712Z ERROR 1 --- [igaming-source-betcity] [    virtual-168] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258913, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.76, ExistingName='Tm', NewName='Tm'
2026-06-04T23:02:42.735Z ERROR 1 --- [igaming-source-betcity] [    virtual-168] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258913, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.95, ExistingName='Tb', NewName='Tb'
2026-06-04T23:02:46.381Z ERROR 1 --- [igaming-source-betcity] [    virtual-168] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258913, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.83, NewValue=2.96, ExistingName='X2', NewName='X2'
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-04T22:57:26.139Z  INFO 1 --- [igaming-source-betcity] [ionShutdownHook] o.a.k.clients.producer.KafkaProducer     : [Producer clientId=igaming-source-betcity-producer-1] Closing the Kafka producer with timeoutMillis = 30000 ms.
    ```

---

### 🗂️ Букмекер: `bettery`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-bettery-crawler-7c9d7dd99f-2tvjz` (2/2, Running, рестартов: 9, возраст: 10h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:02:29.713Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line51.at58f5-resources.com/events/list?lang=ru&scopeMarket=501&version=77839492071&ts=1780614138529, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T23:03:06.045Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-4] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Error during fetch from mirror https://line51.at58f5-resources.com: Unexpected end-of-input within/between Object entries
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-04T21:46:57.451Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1780609617132: Object doesn't exist: frame@156fd501f46488cc9fdc896c6af30b4c
2026-06-04T21:46:57.582Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1780609617130: Cannot invoke "com.microsoft.playwright.impl.FrameImpl.navigateImpl(String, com.microsoft.playwright.Frame$NavigateOptions)" because "this.mainFrame" is null
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-bettery-loader-7cf85bf458-5v4kr` (2/2, Running, рестартов: 0, возраст: 12h)

---

### 🗂️ Букмекер: `fansport`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fansport-crawler-7df8df89f8-xgqn9` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
HTML Error Response (Access Denied/Forbidden): Title='500 Internal Server Error', H1='500 Internal Server Error' (Contains HTTP status 500)
Sample log line: 2026-06-04T23:02:55.040Z  WARN 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Absolute page fetch failed
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fansport-loader-5f5698c67c-g9f5b` (2/2, Running, рестартов: 0, возраст: 7m)
* **Pod:** `igaming-source-fansport-loader-64657969d9-jgjfm` (0/2, Pending, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```

---

### 🗂️ Букмекер: `fon-bet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-by-crawler-5cfc5cbd5d-c8gsx` (2/2, Running, рестартов: 5, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
	transaction.timeout.ms = 60000
2026-06-04T22:59:01.216Z  WARN 1 --- [igaming-source-fon-bet-by] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : FonbetFamily API fetch stats: failed to load 13 out of 15 pages in the last cycle. Reasons: {Empty response=13}
2026-06-04T22:59:32.128Z  WARN 1 --- [igaming-source-fon-bet-by] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line11.by0e87-resources.by/ma/events/listBase?lang=ru&scopeMarket=700&ts=1780613967175, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-04T22:51:54.724Z  WARN 1 --- [igaming-source-fon-bet-by] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line22.by0e87-resources.by/ma/events/list?lang=ru&scopeMarket=700&version=77839235428&ts=1780613502948, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-by-loader-6896679699-74qsd` (2/2, Running, рестартов: 0, возраст: 3h)

---

### 🗂️ Букмекер: `fon-bet-kz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-kz-crawler-b4fd69968-n9xzq` (2/2, Running, рестартов: 2, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:01:08.533Z ERROR 1 --- [igaming-source-fon-bet-kz] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fonbet.kz: Read timed out
2026-06-04T23:01:08.540Z ERROR 1 --- [igaming-source-fon-bet-kz] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'fonbet.kz' is unreachable through current proxy! Triggering rotation...
2026-06-04T23:01:08.882Z ERROR 1 --- [igaming-source-fon-bet-kz] [ loader-sched-3] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Error during fetch from mirror https://line01-w.kzac51-resources.kz: Unexpected end-of-input: was expecting closing quote for a string value
2026-06-04T23:01:14.731Z ERROR 1 --- [igaming-source-fon-bet-kz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fonbet.kz: Remote host terminated the handshake
2026-06-04T23:01:14.731Z  WARN 1 --- [igaming-source-fon-bet-kz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'fonbet.kz': javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-04T22:56:10.534Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line21-w.kzac51-resources.kz/ma/events/list?lang=ru&scopeMarket=1800&version=77839324912&ts=1780613765811, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:56:27.101Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line21-w.kzac51-resources.kz/ma/events/list?lang=ru&scopeMarket=1800&version=77839214490&ts=1780613781328, falling back to direct navigation: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-kz-loader-756ddc9764-w8vpn` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:57:39.172Z ERROR 1 --- [igaming-source-fon-bet-kz] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fonbet.kz: Read timed out
2026-06-04T22:57:39.174Z ERROR 1 --- [igaming-source-fon-bet-kz] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'fonbet.kz' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `fon-bet-ru`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-ru-crawler-6ffd85df54-2kc28` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:00:31.796Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line05w.bk6bba-resources.com/events/list?lang=ru&scopeMarket=1600&version=77839537707&ts=1780614028387, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T23:00:36.223Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-4] .i.s.c.e.f.s.AbstractFonbetFamilyService : Error saving metadata for event 65580529: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect): [pro.datawiki.igaming.source.core.domain.MatchCache#50555]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-ru-loader-6cc99f76f7-8f68l` (2/2, Running, рестартов: 0, возраст: 3h)

---

### 🗂️ Букмекер: `leon`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-leon-crawler-54cbb59bfc-wnxkk` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:19:15.533Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4106, NEW=0, PENDING=3, PROCESSED=4103, FAILED=0
2026-06-04T22:20:15.516Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4106, NEW=0, PENDING=4, PROCESSED=4102, FAILED=0
2026-06-04T22:22:15.524Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4106, NEW=2, PENDING=5, PROCESSED=4099, FAILED=0
2026-06-04T22:23:15.607Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4106, NEW=10, PENDING=13, PROCESSED=4083, FAILED=0
2026-06-04T22:24:15.720Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4111, NEW=0, PENDING=3, PROCESSED=4106, FAILED=1
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-leon-loader-685c48b699-zw2ch` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:18:51.503Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4105, NEW=0, PENDING=3, PROCESSED=4102, FAILED=0
2026-06-04T22:19:51.431Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4106, NEW=0, PENDING=3, PROCESSED=4103, FAILED=0
2026-06-04T22:22:51.496Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4106, NEW=0, PENDING=4, PROCESSED=4102, FAILED=0
2026-06-04T22:23:51.483Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4108, NEW=1, PENDING=3, PROCESSED=4104, FAILED=0
2026-06-04T22:24:51.427Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4114, NEW=0, PENDING=3, PROCESSED=4110, FAILED=1
    ```

---

### 🗂️ Букмекер: `ligastavok`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-ligastavok-crawler-55d4b4b9f-hll6p` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:46:49.422Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__TSR_ROUTER__","__LS_CONTAINER_ONLY_HYDRATION__","fhConfig"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, IMask, __state, __TSR_ROUTER__, __LS_CONTAINER_ONLY_HYDRATION__, changeTheme, fhConfig, fhContext
2026-06-04T22:48:58.751Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-ligastavok-loader-578f758947-6c82w` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:33:59.037Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 1 failed: Error {
  name='Error
  stack='Error: net::ERR_TIMED_OUT at https://www.ligastavok.ru/bets/live
2026-06-04T22:34:29.067Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 2 failed: Error {
2026-06-04T22:34:59.085Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 3 failed: Error {
    ```

---

### 🗂️ Букмекер: `marathonbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-crawler-d4c579db5-g7xpg` (2/2, Running, рестартов: 0, возраст: 45m)
  * **Ошибки в текущих логах:**
    ```text
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-loader-54455f96bb-g8j7h` (2/2, Running, рестартов: 0, возраст: 45m)
  * **Ошибки в текущих логах:**
    ```text
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
2026-06-04T22:30:39.242Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```

---

### 🗂️ Букмекер: `marathonbet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-by-crawler-5c8f6b7ff-fzfwb` (2/2, Running, рестартов: 0, возраст: 45m)
  * **Ошибки в текущих логах:**
    ```text
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-by-loader-59f4f6d495-f24c8` (2/2, Running, рестартов: 0, возраст: 45m)
  * **Ошибки в текущих логах:**
    ```text
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
    ```

---

### 🗂️ Букмекер: `marathonbet-com`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-com-crawler-68c9f4c6f6-mmq7s` (2/2, Running, рестартов: 0, возраст: 45m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:59:25.818Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.m.s.MarathonDiscoveryService     : Cannot fetch prematch menu, aborting league queueing
2026-06-04T22:59:32.668Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Handball/Portugal/Men/2nd+Division+-+4317003: Error {
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://www.marathonbet.com/su/betting/Handball/Portugal/Men/2nd+Division+-+4317003
2026-06-04T23:01:18.232Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: HTTP check returned status 451
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-com-loader-6b5956ccd-7xj7n` (2/2, Running, рестартов: 0, возраст: 45m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:01:13.442Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: Read timed out
2026-06-04T23:01:13.443Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.com': java.net.SocketTimeoutException: Read timed out
2026-06-04T23:01:52.498Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: HTTP check returned status 451
2026-06-04T23:01:52.498Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-04T23:03:24.226Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `megapari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-megapari-crawler-5b8977b96-mlgx6` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:03:06.531Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Absolute page fetch failed: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T23:03:29.731Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.betb2b.service.Betb2bApiClient   : Fetch returned error JSON: {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-megapari-loader-68847848c-cg9t6` (2/2, Running, рестартов: 0, возраст: 18m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:56:42.629Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@27d0376f (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
    ```
* **Pod:** `igaming-source-megapari-loader-6c5b68c7f6-h845f` (0/2, Pending, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```

---

### 🗂️ Букмекер: `melbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-melbet-crawler-7c98747c8f-5hmdx` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:02:50.679Z ERROR 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://melbet.ru: No route to host
2026-06-04T23:02:50.680Z  WARN 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'melbet.ru': java.net.NoRouteToHostException: No route to host
2026-06-04T23:02:56.852Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://melbet.ru/livefeed/get1x2_vzip?count=1000&sports=1&lng=en&mode=4&virtualSports=true: Error {
  name='Error
  stack='Error: net::ERR_TIMED_OUT at https://melbet.ru/livefeed/get1x2_vzip?count=1000&sports=1&lng=en&mode=4&virtualSports=true
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-melbet-loader-6f5c465fdc-78f6s` (1/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: error: error from server (NotFound): pods "igaming-source-melbet-loader-6f5c465fdc-78f6s" not found in namespace "igaming-dev"
    ```
* **Pod:** `igaming-source-melbet-loader-f44f6bd76-xv5gh` (2/2, Running, рестартов: 0, возраст: 18m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:56:42.471Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-4] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@712e4b18 (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
    ```

---

### 🗂️ Букмекер: `pari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pari-crawler-5bd7bbfdcb-gw25t` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:01:24.809Z  WARN 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line-lb01-w.pb06e2-resources.com/events/list?lang=ru&scopeMarket=2300&version=77839555917&ts=1780614081281, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T23:02:33.970Z  WARN 1 --- [igaming-source-pari] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line-lb01-w.pb06e2-resources.com/events/list?lang=ru&scopeMarket=2300&version=77839584744&ts=1780614147926, falling back to direct navigation: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pari-loader-8646dd7647-gfs25` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:57:06.592Z  WARN 1 --- [igaming-source-pari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```

---

### 🗂️ Букмекер: `pinnacle`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pinnacle-crawler-79477f6847-m7f7c` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:54:38.760Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:54:38.836Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:54:38.871Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:54:38.922Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:54:38.946Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pinnacle-loader-986b558c4-mccfj` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:56:54.670Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: Read timed out
2026-06-04T22:56:54.671Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'pinnacle.com' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `sbobet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sbobet-crawler-7bc7c6b577-ftvdv` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:02:27.370Z ERROR 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : [Intercept] Error navigating to https://www.sbobet.com/ru-RU/euro/volleyball: Error {
  name='Error
  stack='Error: net::ERR_PROXY_CONNECTION_FAILED at https://www.sbobet.com/ru-RU/euro/volleyball
2026-06-04T23:02:27.370Z ERROR 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.sbobet.com/ru-RU/euro/volleyball: Error {
2026-06-04T23:04:02.514Z  WARN 1 --- [igaming-source-sbobet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET API fetch stats: failed to load 1 out of 1 pages in the last cycle. Reasons: {HTML instead of JSON=1}
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sbobet-loader-8b5665776-4dmql` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:57:17.081Z ERROR 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.sbobet.com: Read timed out
2026-06-04T22:57:17.081Z ERROR 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.sbobet.com' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `sportbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sportbet-crawler-6d5f976fd7-24pqb` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:58:39.356Z ERROR 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://sportbet.ru: No route to host
2026-06-04T22:58:39.356Z  WARN 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'sportbet.ru': java.net.NoRouteToHostException: No route to host
2026-06-04T22:59:41.866Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'sportbet.ru' is unreachable through current proxy! Triggering rotation...
2026-06-04T22:59:49.994Z ERROR 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'sportbet.ru' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-04T23:01:05.744Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://sportbet.ru: Read timed out
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sportbet-loader-7484dd7dcd-k4qts` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:57:06.415Z  WARN 1 --- [igaming-source-sportbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```

---

### 🗂️ Букмекер: `tennisi`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-tennisi-crawler-978bdf5f7-n8lpn` (2/2, Running, рестартов: 0, возраст: 57m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:57:47.521Z  WARN 1 --- [igaming-source-tennisi] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": service-proxy-backend.service-proxy.svc.cluster.local:80 failed to respond
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-tennisi-loader-6b9945fb7f-8kkrz` (2/2, Running, рестартов: 0, возраст: 57m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:19:58.167Z ERROR 1 --- [igaming-source-tennisi] [    virtual-729] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485482, SemanticKey=TOTAL_OVER_0.5, ExistingValue=1.4, NewValue=1.75, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:20:03.366Z ERROR 1 --- [igaming-source-tennisi] [    virtual-730] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485480, SemanticKey=TOTAL_UNDER_0.5, ExistingValue=1.28, NewValue=1.02, ExistingOriginalName='TM', NewOriginalName='TM', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:20:03.408Z ERROR 1 --- [igaming-source-tennisi] [    virtual-730] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485480, SemanticKey=TOTAL_OVER_0.5, ExistingValue=3.3, NewValue=10.0, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:20:03.380Z ERROR 1 --- [igaming-source-tennisi] [    virtual-731] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485478, SemanticKey=TOTAL_UNDER_0.5, ExistingValue=1.85, NewValue=1.32, ExistingOriginalName='TM', NewOriginalName='TM', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:20:03.459Z ERROR 1 --- [igaming-source-tennisi] [    virtual-731] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485478, SemanticKey=TOTAL_OVER_0.5, ExistingValue=1.85, NewValue=3.1, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
    ```

---

### 🗂️ Букмекер: `winline`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-winline-crawler-6c497467d7-fj9vv` (2/2, Running, рестартов: 0, возраст: 3h)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-winline-loader-6d7c49b666-dqp2q` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:00:01.654Z ERROR 1 --- [igaming-source-winline] [   virtual-9446] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15943640, SemanticKey=SET_1_WIN1, ExistingValue=1.17, NewValue=2.1, ExistingOriginalName='1', NewOriginalName='1', ExistingGroup='1-й сет исход 12', NewGroup='1-й сет исход 1X2 после [b] @Z@'
2026-06-04T23:00:01.654Z ERROR 1 --- [igaming-source-winline] [   virtual-9446] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15943640, SemanticKey=SET_1_DRAW, ExistingValue=1.53, NewValue=2.4, ExistingOriginalName='X', NewOriginalName='X', ExistingGroup='1-й сет исход 1X2 после [b] @Z@', NewGroup='1-й сет исход 1X2 после [b] @Z@'
2026-06-04T23:00:01.654Z ERROR 1 --- [igaming-source-winline] [   virtual-9446] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15943640, SemanticKey=SET_1_WIN2, ExistingValue=4.55, NewValue=4.0, ExistingOriginalName='2', NewOriginalName='2', ExistingGroup='1-й сет исход 12', NewGroup='1-й сет исход 1X2 после [b] @Z@'
2026-06-04T23:01:12.342Z ERROR 1 --- [igaming-source-winline] [   virtual-9471] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15948093, SemanticKey=TEAM2_TOTAL_OVER_0.5, ExistingValue=1.74, NewValue=2.59, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a] (после 5 иннингов) команды 2', NewGroup='4-й тайм тотал [b] команды 2'
2026-06-04T23:01:12.342Z ERROR 1 --- [igaming-source-winline] [   virtual-9471] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15948093, SemanticKey=TEAM2_TOTAL_UNDER_0.5, ExistingValue=1.97, NewValue=1.4, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a] (после 5 иннингов) команды 2', NewGroup='4-й тайм тотал [b] команды 2'
    ```

---

### 🗂️ Букмекер: `zenit`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-zenit-crawler-88dc8cdf9-d8zjc` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:01:47.892Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Zenit API fetch stats: failed to load 1 out of 5 pages in the last cycle. Reasons: {Empty response from browser=1}
2026-06-04T23:04:22.153Z ERROR 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50: Error {
  name='Error
  stack='Error: net::ERR_HTTP_RESPONSE_CODE_FAILURE at https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50
2026-06-04T23:04:22.153Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.zenit.service.ZenitApiClient     : Failed to fetch data, empty response
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-zenit-loader-7cf77966d7-v9l2n` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:30:39.185Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```

---

## 🟢 Исправные БК (Без ошибок)

* `betcity-com`:
  * Loader pod `igaming-source-betcity-com-loader-5565d767fb-gwtvl` (2/2, Running, restarts: 0, age: 1h)
* `linebet`:
  * Crawler pod `igaming-source-linebet-crawler-74d56d4944-rznpg` (2/2, Running, restarts: 0, age: 2m)
  * Loader pod `igaming-source-linebet-loader-845ff9d975-rcgpw` (2/2, Running, restarts: 0, age: 7m)
  * Loader pod `igaming-source-linebet-loader-f76f7b6b7-fxt5p` (1/2, Running, restarts: 0, age: 2m)
* `olimpbet`:
  * Crawler pod `igaming-source-olimpbet-crawler-769f8554b6-nrjrj` (2/2, Running, restarts: 0, age: 2h)
  * Loader pod `igaming-source-olimpbet-loader-6f979799b-glnkp` (2/2, Running, restarts: 0, age: 2h)
* `olimpbet-kz`:
  * Crawler pod `igaming-source-olimpbet-kz-crawler-859459bdd6-j2g49` (2/2, Running, restarts: 0, age: 1h)
  * Loader pod `igaming-source-olimpbet-kz-loader-fc7959846-t56cf` (2/2, Running, restarts: 0, age: 1h)

---

## ⚪ Не развернутые букмекеры (Нет подов)

Все букмекеры развернуты в системе.
