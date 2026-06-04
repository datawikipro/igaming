# 📋 Отчет о состоянии краулеров и лоадеров букмекеров

**Дата проверки:** 2026-06-05 02:07:38

## 📊 Сводная статистика

* **Всего букмекеров в отчете:** 30
* **🟢 Полностью исправные БК (без ошибок):** 3
* **🔴 БК с ошибками/сбоями:** 27
* **⚪ Не развернутые БК (нет подов):** 0

### 🔍 Обзор по всем букмекерам

| Букмекер | Статус | Краулер Pod (Готовность / Рестарты) | Лоадер Pod (Готовность / Рестарты) |
| :--- | :--- | :--- | :--- |
| `1xbet` | 🔴 | `2/2` (рестартов: 0) | `0/2` (рестартов: 0) **Pending** |
| `888starz` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `baltbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betandyou` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
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
| `linebet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet-by` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet-com` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `megapari` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `melbet` | 🔴 | `0/2` (рестартов: 0) **Pending** | `1/2` (рестартов: 0) |
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
* **Pod:** `igaming-source-1xbet-crawler-766b6d74d6-66544` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:05:52.205Z ERROR 1 --- [igaming-source-1xbet] [ loader-sched-3] p.d.i.s.betb2b.service.Betb2bApiClient   : All fetch attempts failed for LIVE
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-1xbet-loader-58c888685d-tnvn5` (0/2, Pending, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```
* **Pod:** `igaming-source-1xbet-loader-67475b9f49-5ljd2` (2/2, Running, рестартов: 0, возраст: 16m)

---

### 🗂️ Букмекер: `888starz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-888starz-crawler-86b8c66fc7-zp7kk` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
HTML Error Response (Access Denied/Forbidden): Title='500 Internal Server Error', H1='500 Internal Server Error' (Contains HTTP status 500)
Sample log line: 2026-06-04T23:05:21.110Z  WARN 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-888starz-loader-595d49f658-mzvhp` (2/2, Running, рестартов: 0, возраст: 20m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:57:17.086Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://888starz.bet: Read timed out
2026-06-04T22:57:17.086Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host '888starz.bet' is unreachable through current proxy! Triggering rotation...
2026-06-04T23:05:41.109Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://888starz.bet: Unexpected end of file from server
    ```
* **Pod:** `igaming-source-888starz-loader-6785c94bfd-xbv46` (0/2, Pending, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```

---

### 🗂️ Букмекер: `baltbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-baltbet-crawler-9fbc48887-9sg6c` (2/2, Running, рестартов: 0, возраст: 23m)
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
* **Pod:** `igaming-source-betandyou-crawler-6f95c6b87b-dbj28` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
HTML Error Response (Access Denied/Forbidden): Title='500 Internal Server Error', H1='500 Internal Server Error' (Contains HTTP status 500)
Sample log line: 2026-06-04T23:05:28.440Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch faile
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betandyou-loader-795cd985b7-9tfl4` (2/2, Running, рестартов: 0, возраст: 9m)
* **Pod:** `igaming-source-betandyou-loader-7c49477b5b-fwgtj` (1/2, Running, рестартов: 0, возраст: 2m)

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
2026-06-04T23:05:25.282Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23258915: null
2026-06-04T23:05:37.685Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.81, NewValue=1.31, ExistingName='X2', NewName='X2'
2026-06-04T23:05:47.338Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259378, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.72, NewValue=1.28, ExistingName='X2', NewName='X2'
2026-06-04T23:05:52.391Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 22841617: null
2026-06-04T23:05:54.660Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-1] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23266977: null
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betcity-loader-785d5df9dd-ltskb` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:05:44.014Z ERROR 1 --- [igaming-source-betcity] [  virtual-10467] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.8, ExistingName='Tm', NewName='Tm'
2026-06-04T23:05:44.098Z ERROR 1 --- [igaming-source-betcity] [  virtual-10467] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.9, ExistingName='Tb', NewName='Tb'
2026-06-04T23:05:44.216Z ERROR 1 --- [igaming-source-betcity] [  virtual-10467] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.8, ExistingName='Tm', NewName='Tm'
2026-06-04T23:05:44.366Z ERROR 1 --- [igaming-source-betcity] [  virtual-10467] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.9, ExistingName='Tb', NewName='Tb'
2026-06-04T23:05:49.710Z ERROR 1 --- [igaming-source-betcity] [  virtual-10467] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.81, NewValue=1.31, ExistingName='X2', NewName='X2'
    ```

---

### 🗂️ Букмекер: `betm`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betm-crawler-7897659bf6-cgwzs` (2/2, Running, рестартов: 0, возраст: 7h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:05:59.136Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23258915: null
2026-06-04T23:06:04.812Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.77, NewValue=1.31, ExistingName='X2', NewName='X2'
2026-06-04T23:06:14.518Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23262731, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=2.0, NewValue=1.88, ExistingName='Tm', NewName='Tm'
2026-06-04T23:06:14.544Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23262731, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.71, NewValue=1.83, ExistingName='Tb', NewName='Tb'
2026-06-04T23:06:14.628Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23262731, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.98, NewValue=1.8, ExistingName='Tm', NewName='Tm'
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betm-loader-d647dbbdf-h58jq` (2/2, Running, рестартов: 1, возраст: 10h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:06:09.537Z ERROR 1 --- [igaming-source-betcity] [    virtual-274] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.8, ExistingName='Tm', NewName='Tm'
2026-06-04T23:06:09.561Z ERROR 1 --- [igaming-source-betcity] [    virtual-274] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.9, ExistingName='Tb', NewName='Tb'
2026-06-04T23:06:09.577Z ERROR 1 --- [igaming-source-betcity] [    virtual-274] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.8, ExistingName='Tm', NewName='Tm'
2026-06-04T23:06:09.593Z ERROR 1 --- [igaming-source-betcity] [    virtual-274] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.9, ExistingName='Tb', NewName='Tb'
2026-06-04T23:06:14.452Z ERROR 1 --- [igaming-source-betcity] [    virtual-274] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23259377, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.77, NewValue=1.31, ExistingName='X2', NewName='X2'
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
* **Pod:** `igaming-source-fansport-crawler-69586b9795-5bjd2` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
HTML Error Response (Access Denied/Forbidden): Title='500 Internal Server Error', H1='500 Internal Server Error' (Contains HTTP status 500)
Sample log line: 2026-06-04T23:06:09.446Z  WARN 1 --- [igaming-source-fansport] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for http
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fansport-loader-5f5698c67c-g9f5b` (2/2, Running, рестартов: 0, возраст: 10m)
* **Pod:** `igaming-source-fansport-loader-78b6cd8567-6cbsc` (0/2, Pending, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```

---

### 🗂️ Букмекер: `fon-bet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-by-crawler-5cfc5cbd5d-c8gsx` (2/2, Running, рестартов: 5, возраст: 3h)
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
2026-06-04T23:02:05.725Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line05-w.kzac51-resources.kz/ma/events/listBase?lang=ru&scopeMarket=1800&ts=1780614114968, falling back to direct navigation: Error {
  name='Error
  stack='Error: net::ERR_CONNECTION_CLOSED at https://fonbet.kz/
2026-06-04T23:02:14.693Z ERROR 1 --- [igaming-source-fon-bet-kz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fonbet.kz: Remote host terminated the handshake
2026-06-04T23:02:14.700Z ERROR 1 --- [igaming-source-fon-bet-kz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'fonbet.kz' is unreachable through current proxy! Triggering rotation...
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
2026-06-04T23:05:07.995Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line05w.bk6bba-resources.com/events/list?lang=ru&scopeMarket=1600&version=77839648150&ts=1780614303822, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T23:05:56.736Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line06w.bk6bba-resources.com/events/list?lang=ru&scopeMarket=1600&version=77839540817&ts=1780614352973, falling back to direct navigation: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-ru-loader-6cc99f76f7-8f68l` (2/2, Running, рестартов: 0, возраст: 3h)

---

### 🗂️ Букмекер: `leon`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-leon-crawler-54cbb59bfc-wnxkk` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:22:15.524Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4106, NEW=2, PENDING=5, PROCESSED=4099, FAILED=0
2026-06-04T22:23:15.607Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4106, NEW=10, PENDING=13, PROCESSED=4083, FAILED=0
2026-06-04T22:24:15.720Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4111, NEW=0, PENDING=3, PROCESSED=4106, FAILED=1
2026-06-04T22:25:15.542Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4123, NEW=0, PENDING=3, PROCESSED=4119, FAILED=1
2026-06-04T22:26:15.514Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4126, NEW=0, PENDING=3, PROCESSED=4122, FAILED=1
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-leon-loader-685c48b699-zw2ch` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:21:51.440Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4106, NEW=0, PENDING=3, PROCESSED=4103, FAILED=0
2026-06-04T22:22:51.496Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4106, NEW=0, PENDING=4, PROCESSED=4102, FAILED=0
2026-06-04T22:23:51.483Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4108, NEW=1, PENDING=3, PROCESSED=4104, FAILED=0
2026-06-04T22:24:51.427Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4114, NEW=0, PENDING=3, PROCESSED=4110, FAILED=1
2026-06-04T22:25:51.419Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4125, NEW=0, PENDING=3, PROCESSED=4121, FAILED=1
    ```

---

### 🗂️ Букмекер: `ligastavok`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-ligastavok-crawler-55d4b4b9f-hll6p` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:49:24.715Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__TSR_ROUTER__","__LS_CONTAINER_ONLY_HYDRATION__","fhConfig"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, IMask, __state, __TSR_ROUTER__, __LS_CONTAINER_ONLY_HYDRATION__, changeTheme, fhConfig, fhContext
2026-06-04T22:51:35.237Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-1] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-ligastavok-loader-578f758947-6c82w` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
  name='Error
  stack='Error: net::ERR_TIMED_OUT at https://www.ligastavok.ru/bets/live
2026-06-04T22:34:29.067Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 2 failed: Error {
2026-06-04T22:34:59.085Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Navigation attempt 3 failed: Error {
2026-06-04T22:35:08.095Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
    ```

---

### 🗂️ Букмекер: `linebet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-linebet-crawler-b88f9b7bf-8gh2g` (2/2, Running, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
HTML Error Response (Access Denied/Forbidden): Title='HTML Page' (Contains HTTP status 500)
Sample log line: 2026-06-04T23:06:13.677Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed:
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-linebet-loader-845ff9d975-rcgpw` (2/2, Running, рестартов: 0, возраст: 10m)
* **Pod:** `igaming-source-linebet-loader-dc69fdfc5-5th5j` (1/2, Running, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: error: error from server (NotFound): pods "igaming-source-linebet-loader-dc69fdfc5-5th5j" not found in namespace "igaming-dev"
    ```

---

### 🗂️ Букмекер: `marathonbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-crawler-d4c579db5-g7xpg` (2/2, Running, рестартов: 0, возраст: 48m)
  * **Ошибки в текущих логах:**
    ```text
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
2026-06-04T22:57:24.695Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/react/event/menu/prematch: Error {
  name='Error
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-loader-54455f96bb-g8j7h` (2/2, Running, рестартов: 0, возраст: 48m)
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
* **Pod:** `igaming-source-marathonbet-by-crawler-5c8f6b7ff-fzfwb` (2/2, Running, рестартов: 0, возраст: 48m)
  * **Ошибки в текущих логах:**
    ```text
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-by-loader-59f4f6d495-f24c8` (2/2, Running, рестартов: 0, возраст: 48m)
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
* **Pod:** `igaming-source-marathonbet-com-crawler-68c9f4c6f6-mmq7s` (2/2, Running, рестартов: 0, возраст: 48m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:03:27.244Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: HTTP check returned status 451
2026-06-04T23:03:27.244Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-04T23:03:39.158Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.marathonbet.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-04T23:06:28.004Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-com-loader-6b5956ccd-7xj7n` (2/2, Running, рестартов: 0, возраст: 48m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:05:24.233Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: HTTP check returned status 451
2026-06-04T23:05:24.233Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-04T23:05:35.655Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
2026-06-04T23:05:37.081Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.marathonbet.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
    ```

---

### 🗂️ Букмекер: `megapari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-megapari-crawler-78c667497d-2pzc9` (2/2, Running, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
HTML Error Response (Access Denied/Forbidden): Title='500 Internal Server Error', H1='500 Internal Server Error' (Contains HTTP status 500)
Sample log line: 2026-06-04T23:06:15.780Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.betb2b.service.Betb2bApiClient   : Fetch returned error JSON:
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-megapari-loader-68847848c-cg9t6` (2/2, Running, рестартов: 0, возраст: 21m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:56:42.629Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-2] com.zaxxer.hikari.pool.PoolBase          : HikariPool-1 - Failed to validate connection org.postgresql.jdbc.PgConnection@27d0376f (This connection has been closed.). Possibly consider using a shorter maxLifetime value.
    ```
* **Pod:** `igaming-source-megapari-loader-ff75cbb7d-fgnb6` (0/2, Pending, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```

---

### 🗂️ Букмекер: `melbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-melbet-crawler-764d4cb7d4-58fd4` (0/2, Pending, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
Container state is ContainerCreating: 
    ```
* **Pod:** `igaming-source-melbet-crawler-7c98747c8f-5hmdx` (2/2, Running, рестартов: 0, возраст: 5m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:02:50.679Z ERROR 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://melbet.ru: No route to host
2026-06-04T23:02:50.680Z  WARN 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'melbet.ru': java.net.NoRouteToHostException: No route to host
2026-06-04T23:02:56.852Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://melbet.ru/livefeed/get1x2_vzip?count=1000&sports=1&lng=en&mode=4&virtualSports=true: Error {
  name='Error
  stack='Error: net::ERR_TIMED_OUT at https://melbet.ru/livefeed/get1x2_vzip?count=1000&sports=1&lng=en&mode=4&virtualSports=true
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-melbet-loader-7df7ffbfb9-j87g8` (1/2, Running, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: error: error from server (NotFound): pods "igaming-source-melbet-loader-7df7ffbfb9-j87g8" not found in namespace "igaming-dev"
    ```
* **Pod:** `igaming-source-melbet-loader-f44f6bd76-xv5gh` (2/2, Running, рестартов: 0, возраст: 21m)
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
2026-06-04T23:05:59.970Z  WARN 1 --- [igaming-source-pari] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line-lb01-w.pb06e2-resources.com/events/list?lang=ru&scopeMarket=2300&version=77839666925&ts=1780614356143, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T23:06:24.209Z  WARN 1 --- [igaming-source-pari] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line-nx-w.pb06e2-resources.ru/events/list?lang=ru&scopeMarket=2300&version=77839558109&ts=1780614380307, falling back to direct navigation: Error {
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
2026-06-04T22:57:39.656Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:57:39.683Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:57:39.708Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:57:39.773Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:57:39.796Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
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
2026-06-04T23:06:02.524Z  WARN 1 --- [igaming-source-sbobet] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET API fetch stats: failed to load 1 out of 1 pages in the last cycle. Reasons: {HTML instead of JSON=1}
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
2026-06-04T23:01:05.744Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://sportbet.ru: Read timed out
2026-06-04T23:01:05.744Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'sportbet.ru' is unreachable through current proxy! Triggering rotation...
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
* **Pod:** `igaming-source-tennisi-crawler-978bdf5f7-n8lpn` (2/2, Running, рестартов: 0, возраст: 1h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:57:47.521Z  WARN 1 --- [igaming-source-tennisi] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": service-proxy-backend.service-proxy.svc.cluster.local:80 failed to respond
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-tennisi-loader-6b9945fb7f-8kkrz` (2/2, Running, рестартов: 0, возраст: 1h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:20:03.469Z ERROR 1 --- [igaming-source-tennisi] [    virtual-731] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485478, SemanticKey=TOTAL_OVER_0.5, ExistingValue=1.85, NewValue=3.1, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:20:03.381Z ERROR 1 --- [igaming-source-tennisi] [    virtual-732] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485419, SemanticKey=TOTAL_UNDER_0.5, ExistingValue=1.34, NewValue=1.06, ExistingOriginalName='TM', NewOriginalName='TM', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:20:03.472Z ERROR 1 --- [igaming-source-tennisi] [    virtual-732] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485419, SemanticKey=TOTAL_OVER_0.5, ExistingValue=3.0, NewValue=7.3, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:20:03.382Z ERROR 1 --- [igaming-source-tennisi] [    virtual-733] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485243, SemanticKey=TOTAL_UNDER_0.5, ExistingValue=1.62, NewValue=1.18, ExistingOriginalName='TM', NewOriginalName='TM', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:20:03.533Z ERROR 1 --- [igaming-source-tennisi] [    virtual-733] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485243, SemanticKey=TOTAL_OVER_0.5, ExistingValue=2.15, NewValue=4.3, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
    ```

---

### 🗂️ Букмекер: `winline`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-winline-crawler-6c497467d7-fj9vv` (2/2, Running, рестартов: 0, возраст: 3h)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-winline-loader-6d7c49b666-dqp2q` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:02:39.607Z ERROR 1 --- [igaming-source-winline] [   virtual-9549] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15950703, SemanticKey=HANDICAP_1_1.5, ExistingValue=1.96, NewValue=1.8, ExistingOriginalName='1', NewOriginalName='1', ExistingGroup='Фора [a]', NewGroup='4-й период фора [b]'
2026-06-04T23:02:39.608Z ERROR 1 --- [igaming-source-winline] [   virtual-9549] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15950703, SemanticKey=HANDICAP_2_-1.5, ExistingValue=1.76, NewValue=1.9, ExistingOriginalName='2', NewOriginalName='2', ExistingGroup='Фора [a]', NewGroup='4-й период фора [b]'
2026-06-04T23:02:40.141Z ERROR 1 --- [igaming-source-winline] [   virtual-9562] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15943640, SemanticKey=SET_1_WIN1, ExistingValue=2.0, NewValue=3.4, ExistingOriginalName='1', NewOriginalName='1', ExistingGroup='1-й сет исход 1X2 после [b] @Z@', NewGroup='1-й сет исход 1X2 после [b] @Z@'
2026-06-04T23:02:40.142Z ERROR 1 --- [igaming-source-winline] [   virtual-9562] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15943640, SemanticKey=SET_1_DRAW, ExistingValue=3.6, NewValue=2.5, ExistingOriginalName='X', NewOriginalName='X', ExistingGroup='1-й сет исход 1X2 после [b] @Z@', NewGroup='1-й сет исход 1X2 после [b] @Z@'
2026-06-04T23:02:40.142Z ERROR 1 --- [igaming-source-winline] [   virtual-9562] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15943640, SemanticKey=SET_1_WIN2, ExistingValue=2.75, NewValue=2.2, ExistingOriginalName='2', NewOriginalName='2', ExistingGroup='1-й сет исход 1X2 после [b] @Z@', NewGroup='1-й сет исход 1X2 после [b] @Z@'
    ```

---

### 🗂️ Букмекер: `zenit`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-zenit-crawler-88dc8cdf9-d8zjc` (2/2, Running, рестартов: 0, возраст: 3h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T23:04:22.153Z ERROR 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50: Error {
  name='Error
  stack='Error: net::ERR_HTTP_RESPONSE_CODE_FAILURE at https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50
2026-06-04T23:04:22.153Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.zenit.service.ZenitApiClient     : Failed to fetch data, empty response
2026-06-04T23:06:47.891Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Zenit API fetch stats: failed to load 1 out of 6 pages in the last cycle. Reasons: {Empty response from browser=1}
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-zenit-loader-7cf77966d7-v9l2n` (2/2, Running, рестартов: 0, возраст: 3h)

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
