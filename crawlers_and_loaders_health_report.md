# 📋 Отчет о состоянии краулеров и лоадеров букмекеров

**Дата проверки:** 2026-06-05 01:50:16

## 📊 Сводная статистика

* **Всего букмекеров в отчете:** 30
* **🟢 Полностью исправные БК (без ошибок):** 3
* **🔴 БК с ошибками/сбоями:** 27
* **⚪ Не развернутые БК (нет подов):** 0

### 🔍 Обзор по всем букмекерам

| Букмекер | Статус | Краулер Pod (Готовность / Рестарты) | Лоадер Pod (Готовность / Рестарты) |
| :--- | :--- | :--- | :--- |
| `1xbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `888starz` | 🔴 | `2/2` (рестартов: 0) | `1/2` (рестартов: 0) |
| `baltbet` | 🔴 | `1/1` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betandyou` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betboom` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betcity` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betcity-com` | 🟢 | - | `2/2` (рестартов: 0) |
| `betm` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `bettery` | 🔴 | `2/2` (рестартов: 9) | `2/2` (рестартов: 0) |
| `fansport` | 🔴 | `2/2` (рестартов: 0) | `1/2` (рестартов: 0) |
| `fon-bet-by` | 🔴 | `2/2` (рестартов: 4) | `2/2` (рестартов: 0) |
| `fon-bet-kz` | 🔴 | `2/2` (рестартов: 1) | `2/2` (рестартов: 0) |
| `fon-bet-ru` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `leon` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `ligastavok` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `linebet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet-by` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet-com` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `megapari` | 🔴 | `0/2` (рестартов: 0) **Pending** | `0/2` (рестартов: 0) **Failed** |
| `melbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
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
* **Pod:** `igaming-source-1xbet-crawler-85cfc5d75d-p8n7k` (2/2, Running, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:47:20.618Z  WARN 1 --- [igaming-source-1xbet] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 406
  name='Error
  stack='Error: Error: HTTP status 406
2026-06-04T22:47:21.656Z  WARN 1 --- [igaming-source-1xbet] [ loader-sched-1] p.d.i.s.betb2b.service.Betb2bApiClient   : Fetch returned NotAcceptable error JSON: {"type":"feed/NotAcceptableException","title":"NotAcceptable","status":406,"detail":"Error occurred during request execution. Contact the developer."}
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-1xbet-loader-786fdc58ff-kg2xz` (2/2, Running, рестартов: 0, возраст: 13m)
* **Pod:** `igaming-source-1xbet-loader-bf7f44c5b-6kq2l` (0/2, Pending, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```

---

### 🗂️ Букмекер: `888starz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-888starz-crawler-7f766f6ffd-9pjj9` (2/2, Running, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:48:23.892Z  WARN 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 406
  name='Error
  stack='Error: Error: HTTP status 406
2026-06-04T22:48:40.279Z  WARN 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Absolute page fetch failed: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-888starz-loader-595d49f658-mzvhp` (1/2, Running, рестартов: 0, возраст: 3m)
* **Pod:** `igaming-source-888starz-loader-7c7cdf6bf6-9x7bh` (2/2, Running, рестартов: 0, возраст: 13m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:48:34.012Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://888starz.bet: Unexpected end of file from server
2026-06-04T22:48:34.012Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host '888starz.bet' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `baltbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-baltbet-crawler-6cb5cf94d8-z9l5d` (1/1, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:46:22.585Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457391: Unable to connect to Redis
2026-06-04T22:46:22.594Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457392: Unable to connect to Redis
2026-06-04T22:46:22.602Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457434: Unable to connect to Redis
2026-06-04T22:46:22.610Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457437: Unable to connect to Redis
2026-06-04T22:46:22.623Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457393: Unable to connect to Redis
    ```
* **Pod:** `igaming-source-baltbet-crawler-9fbc48887-9sg6c` (0/2, Pending, рестартов: 0, возраст: 5m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-baltbet-loader-84fc95f496-4cjs4` (2/2, Running, рестартов: 0, возраст: 3h)

---

### 🗂️ Букмекер: `betandyou`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betandyou-crawler-658f9f6bff-8gvf4` (2/2, Running, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:46:59.242Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.b.service.Betb2bEventDiscoverer  : [betandyou] Betb2b response parsed but Value is null/empty (success=null, error=null)
2026-06-04T22:47:08.032Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 406
  name='Error
  stack='Error: Error: HTTP status 406
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betandyou-loader-5d4697d76-mbnjz` (2/2, Running, рестартов: 0, возраст: 3m)

---

### 🗂️ Букмекер: `betboom`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betboom-crawler-685b8c5fc-kmlq8` (2/2, Running, рестартов: 0, возраст: 3h)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betboom-loader-5d784467db-8dxn5` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:36:13.128Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://siteapi.betboom.ru: Read timed out
2026-06-04T22:36:13.128Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'siteapi.betboom.ru' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `betcity`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betcity-crawler-55f6664474-qtlh8` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:48:33.895Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23258913: null
2026-06-04T22:48:41.960Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23258914: null
2026-06-04T22:48:48.964Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23266977: null
2026-06-04T22:48:51.247Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23231603: null
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betcity-loader-785d5df9dd-ltskb` (2/2, Running, рестартов: 0, возраст: 3h)

---

### 🗂️ Букмекер: `betm`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betm-crawler-7897659bf6-cgwzs` (2/2, Running, рестартов: 0, возраст: 7h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:48:40.322Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23258913: null
2026-06-04T22:48:55.508Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23258914: null
2026-06-04T22:49:03.006Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23266977: null
2026-06-04T22:49:05.145Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23231603: null
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betm-loader-d647dbbdf-h58jq` (2/2, Running, рестартов: 0, возраст: 9h)

---

### 🗂️ Букмекер: `bettery`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-bettery-crawler-7c9d7dd99f-2tvjz` (2/2, Running, рестартов: 9, возраст: 9h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:45:59.082Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line51.at58f5-resources.com/events/list?lang=ru&scopeMarket=501&version=77838962767&ts=1780613148806, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:47:02.597Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-1] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Error during fetch from mirror https://line51.at58f5-resources.com: Unexpected end-of-input: expected close marker for Object (start marker at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 3974680])
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-04T21:46:57.451Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1780609617132: Object doesn't exist: frame@156fd501f46488cc9fdc896c6af30b4c
2026-06-04T21:46:57.582Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://line01.at58f5-resources.com/events/listBase?lang=ru&scopeMarket=501&ts=1780609617130: Cannot invoke "com.microsoft.playwright.impl.FrameImpl.navigateImpl(String, com.microsoft.playwright.Frame$NavigateOptions)" because "this.mainFrame" is null
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-bettery-loader-7cf85bf458-5v4kr` (2/2, Running, рестартов: 0, возраст: 12h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:47:39.643Z  WARN 1 --- [igaming-source-bettery] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```

---

### 🗂️ Букмекер: `fansport`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fansport-crawler-68c95cfc57-4bq2s` (2/2, Running, рестартов: 0, возраст: 5m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:44:41.974Z  WARN 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 406
  name='Error
  stack='Error: Error: HTTP status 406
2026-06-04T22:44:44.672Z  WARN 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Absolute page fetch failed: Error {
    ```
* **Pod:** `igaming-source-fansport-crawler-8c5b69466-nh7jx` (0/2, Pending, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fansport-loader-5cc964c9fd-mvtql` (1/2, Running, рестартов: 0, возраст: 3m)
* **Pod:** `igaming-source-fansport-loader-694579b78f-mg4mq` (2/2, Running, рестартов: 0, возраст: 13m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: error: error from server (NotFound): pods "igaming-source-fansport-loader-694579b78f-mg4mq" not found in namespace "igaming-dev"
    ```

---

### 🗂️ Букмекер: `fon-bet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-by-crawler-5cfc5cbd5d-c8gsx` (2/2, Running, рестартов: 4, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:46:59.814Z  WARN 1 --- [igaming-source-fon-bet-by] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line22.by0e87-resources.by/ma/events/list?lang=ru&scopeMarket=700&version=77839091848&ts=1780613212085, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:47:30.831Z  WARN 1 --- [igaming-source-fon-bet-by] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-04T21:59:18.164Z  WARN 1 --- [igaming-source-fon-bet-by] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line21.by0e87-resources.by/ma/events/list?lang=ru&scopeMarket=700&version=77837449052&ts=1780610351985, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-by-loader-6896679699-74qsd` (2/2, Running, рестартов: 0, возраст: 3h)

---

### 🗂️ Букмекер: `fon-bet-kz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-kz-crawler-b4fd69968-n9xzq` (2/2, Running, рестартов: 1, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:43:33.710Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line21-w.kzac51-resources.kz/ma/events/list?lang=ru&scopeMarket=1800&version=77838994711&ts=1780613008767, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:46:09.364Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line21-w.kzac51-resources.kz/ma/events/list?lang=ru&scopeMarket=1800&version=77838976649&ts=1780613164677, falling back to direct navigation: Error {
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-04T22:06:35.517Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line05-w.kzac51-resources.kz/ma/events/list?lang=ru&scopeMarket=1800&version=77837712915&ts=1780610791383, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:06:35.626Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line05-w.kzac51-resources.kz/ma/events/list?lang=ru&scopeMarket=1800&version=77837852096&ts=1780610791424, falling back to direct navigation: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-kz-loader-756ddc9764-w8vpn` (2/2, Running, рестартов: 0, возраст: 3h)

---

### 🗂️ Букмекер: `fon-bet-ru`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-ru-crawler-6ffd85df54-2kc28` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:47:36.597Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line06w.bk6bba-resources.com/events/list?lang=ru&scopeMarket=1600&version=77839094473&ts=1780613252345, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:47:54.784Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line05w.bk6bba-resources.com/events/list?lang=ru&scopeMarket=1600&version=77839231169&ts=1780613270690, falling back to direct navigation: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-ru-loader-6cc99f76f7-8f68l` (2/2, Running, рестартов: 0, возраст: 3h)

---

### 🗂️ Букмекер: `leon`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-leon-crawler-54cbb59bfc-wnxkk` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:04:15.524Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4107, NEW=0, PENDING=7, PROCESSED=4100, FAILED=0
2026-06-04T22:05:15.556Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4117, NEW=0, PENDING=3, PROCESSED=4114, FAILED=0
2026-06-04T22:07:15.756Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4121, NEW=0, PENDING=3, PROCESSED=4118, FAILED=0
2026-06-04T22:08:15.551Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4122, NEW=0, PENDING=3, PROCESSED=4119, FAILED=0
2026-06-04T22:09:15.514Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4123, NEW=0, PENDING=3, PROCESSED=4120, FAILED=0
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-leon-loader-685c48b699-zw2ch` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:04:51.427Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4115, NEW=0, PENDING=3, PROCESSED=4112, FAILED=0
2026-06-04T22:05:51.422Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4117, NEW=0, PENDING=3, PROCESSED=4114, FAILED=0
2026-06-04T22:06:51.425Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4120, NEW=0, PENDING=3, PROCESSED=4117, FAILED=0
2026-06-04T22:07:51.439Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4121, NEW=0, PENDING=3, PROCESSED=4118, FAILED=0
2026-06-04T22:08:51.420Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4123, NEW=0, PENDING=3, PROCESSED=4120, FAILED=0
    ```

---

### 🗂️ Букмекер: `ligastavok`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-ligastavok-crawler-55d4b4b9f-hll6p` (2/2, Running, рестартов: 0, возраст: 1h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:36:22.102Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state
2026-06-04T22:36:51.009Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: net::ERR_TIMED_OUT
2026-06-04T22:36:51.032Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Error loading script: https://static-cdn.sportsapi.ru/sites-static/assets/stable-vendors-JMdergPs.js
2026-06-04T22:36:52.124Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.l.s.LigastavokDiagnosticService  : Browser is using IP: error: Failed to fetch
2026-06-04T22:38:38.517Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-ligastavok-loader-578f758947-6c82w` (2/2, Running, рестартов: 0, возраст: 1h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:31:16.488Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, IMask
2026-06-04T22:32:26.082Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://ligastavok.ru: Read timed out
2026-06-04T22:32:26.083Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'ligastavok.ru' is unreachable through current proxy! Triggering rotation...
2026-06-04T22:32:40.982Z  WARN 1 --- [igaming-source-ligastavok] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'ligastavok.ru': java.net.SocketTimeoutException: Read timed out
2026-06-04T22:33:11.249Z ERROR 1 --- [igaming-source-ligastavok] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'ligastavok.ru' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
    ```

---

### 🗂️ Букмекер: `linebet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-linebet-crawler-598b69fbc9-k9w65` (2/2, Running, рестартов: 0, возраст: 4m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:47:52.316Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.b.service.Betb2bEventDiscoverer  : [linebet] Betb2b response parsed but Value is null/empty (success=null, error=null)
2026-06-04T22:48:08.943Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 406
  name='Error
  stack='Error: Error: HTTP status 406
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-linebet-loader-58976994c-4rfkq` (2/2, Running, рестартов: 0, возраст: 14m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: error: error from server (NotFound): pods "igaming-source-linebet-loader-58976994c-4rfkq" not found in namespace "igaming-dev"
    ```
* **Pod:** `igaming-source-linebet-loader-79d585c5b7-765hr` (2/2, Running, рестартов: 0, возраст: 4m)

---

### 🗂️ Букмекер: `marathonbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-crawler-d4c579db5-g7xpg` (2/2, Running, рестартов: 0, возраст: 31m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:43:55.318Z  WARN 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
2026-06-04T22:46:11.918Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/e-Sports/LoL/LCK+Challengers+League+-+10877808: Error {
  name='TargetClosedError
  stack='TargetClosedError:Target page, context or browser has been closed
Error
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-loader-54455f96bb-g8j7h` (2/2, Running, рестартов: 0, возраст: 31m)
  * **Ошибки в текущих логах:**
    ```text
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
    ```

---

### 🗂️ Букмекер: `marathonbet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-by-crawler-5c8f6b7ff-fzfwb` (2/2, Running, рестартов: 0, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-by-loader-59f4f6d495-f24c8` (2/2, Running, рестартов: 0, возраст: 30m)
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
* **Pod:** `igaming-source-marathonbet-com-crawler-68c9f4c6f6-mmq7s` (2/2, Running, рестартов: 0, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:47:41.609Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: HTTP check returned status 451
2026-06-04T22:47:41.609Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-04T22:47:53.088Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.marathonbet.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-04T22:47:59.531Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
2026-06-04T22:48:24.327Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Volleyball/Uruguay/Women/Division+A+-+10142813: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-com-loader-6b5956ccd-7xj7n` (2/2, Running, рестартов: 0, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:48:52.939Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: Connect timed out
2026-06-04T22:48:52.939Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.com': java.net.SocketTimeoutException: Connect timed out
2026-06-04T22:48:53.016Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Tennis/ATP+Challenger+Tour/Centurion%2C+South+Africa/Singles/Quarter+Final/A.Donski+vs+T.Duran+-+28949955: Error {
  name='Error
  stack='Error: net::ERR_TIMED_OUT at https://www.marathonbet.com/su/betting/Tennis/ATP+Challenger+Tour/Centurion%2C+South+Africa/Singles/Quarter+Final/A.Donski+vs+T.Duran+-+28949955
    ```

---

### 🗂️ Букмекер: `megapari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-megapari-crawler-7bfd5d796f-xcn9t` (0/2, Pending, рестартов: 0, возраст: 4m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```
* **Pod:** `igaming-source-megapari-crawler-8d67d5969-r27lt` (2/2, Running, рестартов: 0, возраст: 6m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:44:46.213Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Absolute page fetch failed: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:44:47.130Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.betb2b.service.Betb2bApiClient   : Fetch succeeded. Preview (first 100 chars): {"type":"feed/NotAcceptableException","title":"NotAcceptable","status":406,"detail":"Error occurred 
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-megapari-loader-555bf6b69b-lcpl6` (0/2, Failed, рестартов: 0, возраст: 14m)
  * **Ошибки в текущих логах:**
    ```text
Container state is Terminated: Exit Code: 143
    ```
* **Pod:** `igaming-source-megapari-loader-68847848c-cg9t6` (2/2, Running, рестартов: 0, возраст: 4m)

---

### 🗂️ Букмекер: `melbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-melbet-crawler-7d9f5ff456-ncf2r` (2/2, Running, рестартов: 0, возраст: 4m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:47:44.900Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.betb2b.service.Betb2bApiClient   : All fetch attempts failed for LIVE
2026-06-04T22:47:54.017Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://melbet.ru/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en: Error {
  name='Error
  stack='Error: net::ERR_TIMED_OUT at https://melbet.ru/service-api/LineFeed/Get1x2_VZip?virtualSports=true&sports=1&lng=en
2026-06-04T22:47:54.885Z ERROR 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://melbet.ru: Connect timed out
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-melbet-loader-6cc8d64ff-gm9bt` (2/2, Running, рестартов: 0, возраст: 14m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: error: error from server (NotFound): pods "igaming-source-melbet-loader-6cc8d64ff-gm9bt" not found in namespace "igaming-dev"
    ```
* **Pod:** `igaming-source-melbet-loader-f44f6bd76-xv5gh` (1/2, Running, рестартов: 0, возраст: 4m)

---

### 🗂️ Букмекер: `pari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pari-crawler-5bd7bbfdcb-gw25t` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:47:51.371Z  WARN 1 --- [igaming-source-pari] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line-lb01-w.pb06e2-resources.com/events/list?lang=ru&scopeMarket=2300&version=77839229930&ts=1780613268093, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:48:00.124Z  WARN 1 --- [igaming-source-pari] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line-nx-w.pb06e2-resources.ru/events/list?lang=ru&scopeMarket=2300&version=77839103954&ts=1780613276412, falling back to direct navigation: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pari-loader-8646dd7647-gfs25` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:36:43.011Z ERROR 1 --- [igaming-source-pari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pari.ru: Read timed out
2026-06-04T22:36:43.012Z ERROR 1 --- [igaming-source-pari] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'pari.ru' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `pinnacle`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pinnacle-crawler-79477f6847-m7f7c` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:40:34.018Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:40:34.042Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:40:34.070Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:40:34.102Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:40:34.126Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-4] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pinnacle-loader-986b558c4-mccfj` (2/2, Running, рестартов: 0, возраст: 2h)

---

### 🗂️ Букмекер: `sbobet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sbobet-crawler-7bc7c6b577-ftvdv` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:48:02.514Z  WARN 1 --- [igaming-source-sbobet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET API fetch stats: failed to load 1 out of 1 pages in the last cycle. Reasons: {HTML instead of JSON=1}
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sbobet-loader-8b5665776-4dmql` (2/2, Running, рестартов: 0, возраст: 2h)

---

### 🗂️ Букмекер: `sportbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sportbet-crawler-6d5f976fd7-24pqb` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:48:11.968Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://sportbet.ru: Connect timed out
2026-06-04T22:48:11.968Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'sportbet.ru' is unreachable through current proxy! Triggering rotation...
2026-06-04T22:48:13.637Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-1] p.d.i.s.s.service.SportbetApiClient      : Error fetching line info
org.springframework.web.client.ResourceAccessException: I/O error on GET request for "https://sportbet.ru/sport/v1/fixtures-tree-live": No route to host
	at org.springframework.web.client.RestTemplate.createResourceAccessException(RestTemplate.java:926) ~[spring-web-6.2.1.jar!/:6.2.1]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sportbet-loader-7484dd7dcd-k4qts` (2/2, Running, рестартов: 0, возраст: 2h)

---

### 🗂️ Букмекер: `tennisi`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-tennisi-crawler-978bdf5f7-n8lpn` (2/2, Running, рестартов: 0, возраст: 43m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-tennisi-loader-6b9945fb7f-8kkrz` (2/2, Running, рестартов: 0, возраст: 43m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:19:52.576Z ERROR 1 --- [igaming-source-tennisi] [    virtual-720] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485234, SemanticKey=TOTAL_UNDER_0.5, ExistingValue=1.9, NewValue=1.45, ExistingOriginalName='TM', NewOriginalName='TM', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:19:52.578Z ERROR 1 --- [igaming-source-tennisi] [    virtual-720] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485234, SemanticKey=TOTAL_OVER_0.5, ExistingValue=1.8, NewValue=2.55, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:19:52.557Z ERROR 1 --- [igaming-source-tennisi] [    virtual-721] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485231, SemanticKey=TOTAL_UNDER_0.5, ExistingValue=1.62, NewValue=1.12, ExistingOriginalName='TM', NewOriginalName='TM', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:19:52.595Z ERROR 1 --- [igaming-source-tennisi] [    virtual-721] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485231, SemanticKey=TOTAL_OVER_0.5, ExistingValue=2.15, NewValue=5.3, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:19:52.558Z ERROR 1 --- [igaming-source-tennisi] [    virtual-722] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485226, SemanticKey=TOTAL_UNDER_0.5, ExistingValue=1.28, NewValue=1.04, ExistingOriginalName='TM', NewOriginalName='TM', ExistingGroup='Тотал', NewGroup='Тотал'
    ```

---

### 🗂️ Букмекер: `winline`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-winline-crawler-6c497467d7-fj9vv` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
  name='TimeoutError
  stack='TimeoutError: Timeout 15000ms exceeded.
com.microsoft.playwright.TimeoutError: Error {
  message='Timeout 15000ms exceeded.
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-winline-loader-6d7c49b666-dqp2q` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:43:08.549Z ERROR 1 --- [igaming-source-winline] [   virtual-9118] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15944182, SemanticKey=HANDICAP_1_5.5, ExistingValue=1.47, NewValue=1.5, ExistingOriginalName='1', NewOriginalName='1', ExistingGroup='4-й период фора [b]', NewGroup='5-й период фора [b]'
2026-06-04T22:43:08.550Z ERROR 1 --- [igaming-source-winline] [   virtual-9118] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15944182, SemanticKey=HANDICAP_2_-5.5, ExistingValue=2.5, NewValue=2.42, ExistingOriginalName='2', NewOriginalName='2', ExistingGroup='4-й период фора [b]', NewGroup='5-й период фора [b]'
2026-06-04T22:43:08.550Z ERROR 1 --- [igaming-source-winline] [   virtual-9118] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15944182, SemanticKey=TEAM2_TOTAL_OVER_10.5, ExistingValue=1.22, NewValue=1.26, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='4-й период тотал [b] команды 2', NewGroup='5-й период тотал [b] команды 2'
2026-06-04T22:43:08.550Z ERROR 1 --- [igaming-source-winline] [   virtual-9118] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15944182, SemanticKey=TEAM2_TOTAL_UNDER_10.5, ExistingValue=3.81, NewValue=3.5, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='4-й период тотал [b] команды 2', NewGroup='5-й период тотал [b] команды 2'
2026-06-04T22:43:08.550Z ERROR 1 --- [igaming-source-winline] [   virtual-9118] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15944182, SemanticKey=TOTAL_OVER_21.5, ExistingValue=2.22, NewValue=2.12, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='5-й период тотал [b]', NewGroup='4-й период тотал [b]'
    ```

---

### 🗂️ Букмекер: `zenit`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-zenit-crawler-88dc8cdf9-d8zjc` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:46:47.892Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Zenit API fetch stats: failed to load 1 out of 6 pages in the last cycle. Reasons: {Empty response from browser=1}
2026-06-04T22:48:47.384Z ERROR 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50: Error {
  name='Error
  stack='Error: net::ERR_HTTP_RESPONSE_CODE_FAILURE at https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50
2026-06-04T22:48:47.384Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.zenit.service.ZenitApiClient     : Failed to fetch data, empty response
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-zenit-loader-7cf77966d7-v9l2n` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:18:31.147Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```

---

## 🟢 Исправные БК (Без ошибок)

* `betcity-com`:
  * Loader pod `igaming-source-betcity-com-loader-5565d767fb-gwtvl` (2/2, Running, restarts: 0, age: 1h)
* `olimpbet`:
  * Crawler pod `igaming-source-olimpbet-crawler-769f8554b6-nrjrj` (2/2, Running, restarts: 0, age: 2h)
  * Loader pod `igaming-source-olimpbet-loader-6f979799b-glnkp` (2/2, Running, restarts: 0, age: 2h)
* `olimpbet-kz`:
  * Crawler pod `igaming-source-olimpbet-kz-crawler-859459bdd6-j2g49` (2/2, Running, restarts: 0, age: 1h)
  * Loader pod `igaming-source-olimpbet-kz-loader-fc7959846-t56cf` (2/2, Running, restarts: 0, age: 1h)

---

## ⚪ Не развернутые букмекеры (Нет подов)

Все букмекеры развернуты в системе.
