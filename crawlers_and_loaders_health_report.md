# 📋 Отчет о состоянии краулеров и лоадеров букмекеров

**Дата проверки:** 2026-06-05 01:33:45

## 📊 Сводная статистика

* **Всего букмекеров в отчете:** 30
* **🟢 Полностью исправные БК (без ошибок):** 5
* **🔴 БК с ошибками/сбоями:** 25
* **⚪ Не развернутые БК (нет подов):** 0

### 🔍 Обзор по всем букмекерам

| Букмекер | Статус | Краулер Pod (Готовность / Рестарты) | Лоадер Pod (Готовность / Рестарты) |
| :--- | :--- | :--- | :--- |
| `1xbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `888starz` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `baltbet` | 🔴 | `1/1` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betandyou` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betboom` | 🟢 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
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
| `megapari` | 🔴 | `2/2` (рестартов: 0) | `1/2` (рестартов: 0) |
| `melbet` | 🔴 | `2/2` (рестартов: 0) | `1/2` (рестартов: 0) |
| `olimpbet` | 🟢 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `olimpbet-kz` | 🟢 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `pari` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `pinnacle` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `sbobet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `sportbet` | 🟢 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `tennisi` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `winline` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `zenit` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |

---

## 🔴 Детализация ошибок и проблемных БК

### 🗂️ Букмекер: `1xbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-1xbet-crawler-84dfd48b57-pwvmd` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:31:38.396Z  WARN 1 --- [igaming-source-1xbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 403
  name='Error
  stack='Error: Error: HTTP status 403
2026-06-04T22:31:41.115Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-3] p.d.i.s.betb2b.service.Betb2bApiClient   : Fetch succeeded. Preview (first 100 chars): {"type":"feed/NotAcceptableException","title":"NotAcceptable","status":406,"detail":"Error occurred 
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-1xbet-loader-558bd44569-qggk4` (2/2, Running, рестартов: 0, возраст: 8m)
* **Pod:** `igaming-source-1xbet-loader-784bcb9c5f-lm4qs` (1/2, Running, рестартов: 0, возраст: 2m)

---

### 🗂️ Букмекер: `888starz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-888starz-crawler-7c8b4d8d6c-xpgxt` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:32:16.585Z  WARN 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 406
  name='Error
  stack='Error: Error: HTTP status 406
2026-06-04T22:32:23.175Z  WARN 1 --- [igaming-source-888starz] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Absolute page fetch failed: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-888starz-loader-68c7fb9878-q6slh` (2/2, Running, рестартов: 0, возраст: 8m)
* **Pod:** `igaming-source-888starz-loader-6d84c5595f-zrvng` (1/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:32:21.933Z ERROR 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://888starz.bet: Read timed out
2026-06-04T22:32:21.936Z  WARN 1 --- [igaming-source-888starz] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for '888starz.bet': java.net.SocketTimeoutException: Read timed out
    ```

---

### 🗂️ Букмекер: `baltbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-baltbet-crawler-6cb5cf94d8-z9l5d` (1/1, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:29:53.303Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457337: Unable to connect to Redis
2026-06-04T22:29:53.362Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457229: Unable to connect to Redis
2026-06-04T22:29:53.454Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457282: Unable to connect to Redis
2026-06-04T22:29:53.495Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30457327: Unable to connect to Redis
2026-06-04T22:29:53.514Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-3] p.d.i.s.core.service.RedisFactorService  : Failed to save factors to Redis for match 30450830: Unable to connect to Redis
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-baltbet-loader-84fc95f496-4cjs4` (2/2, Running, рестартов: 0, возраст: 2h)

---

### 🗂️ Букмекер: `betandyou`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betandyou-crawler-674d9cf49b-59sbb` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:31:38.129Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 406
  name='Error
  stack='Error: Error: HTTP status 406
2026-06-04T22:31:39.150Z  INFO 1 --- [igaming-source-betandyou] [ loader-sched-3] p.d.i.s.betb2b.service.Betb2bApiClient   : Fetch succeeded. Preview (first 100 chars): {"type":"feed/NotAcceptableException","title":"NotAcceptable","status":406,"detail":"Error occurred 
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betandyou-loader-5b666db8dd-m979n` (2/2, Running, рестартов: 0, возраст: 8m)
* **Pod:** `igaming-source-betandyou-loader-bcd64d865-lpcdd` (1/2, Running, рестартов: 0, возраст: 2m)

---

### 🗂️ Букмекер: `betcity`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betcity-crawler-55f6664474-qtlh8` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:32:20.070Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23205689: null
2026-06-04T22:32:20.472Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23214750: null
2026-06-04T22:32:20.898Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23214751: null
2026-06-04T22:32:21.424Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23214758: null
2026-06-04T22:32:21.868Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.e.b.s.BetcityEventDiscoverer   : Error processing Betcity event 23221779: null
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betcity-loader-785d5df9dd-ltskb` (2/2, Running, рестартов: 0, возраст: 2h)

---

### 🗂️ Букмекер: `betm`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betm-crawler-7897659bf6-cgwzs` (2/2, Running, рестартов: 0, возраст: 7h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:31:55.260Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258912, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_UNDER_0.0, ExistingValue=1.9, NewValue=1.76, ExistingName='Tm', NewName='Tm'
2026-06-04T22:31:55.282Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258912, Group=Индивидуальный тотал 1, SemanticKey=TEAM1_TOTAL_OVER_0.0, ExistingValue=1.8, NewValue=1.95, ExistingName='Tb', NewName='Tb'
2026-06-04T22:31:55.303Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258912, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_UNDER_0.0, ExistingValue=1.95, NewValue=1.85, ExistingName='Tm', NewName='Tm'
2026-06-04T22:31:55.328Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258912, Group=Индивидуальный тотал 2, SemanticKey=TEAM2_TOTAL_OVER_0.0, ExistingValue=1.76, NewValue=1.85, ExistingName='Tb', NewName='Tb'
2026-06-04T22:31:55.435Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-4] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23258912, Group=Фора по партиям, SemanticKey=HANDICAP_1_-2.5, ExistingValue=1.95, NewValue=5.1, ExistingName='Kf_F1', NewName='Kf_F1'
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betm-loader-d647dbbdf-h58jq` (2/2, Running, рестартов: 0, возраст: 9h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:32:02.274Z ERROR 1 --- [igaming-source-betcity] [  virtual-30077] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23274856, Group=Фора по партиям, SemanticKey=HANDICAP_1_1.5, ExistingValue=1.95, NewValue=1.57, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-04T22:32:02.276Z ERROR 1 --- [igaming-source-betcity] [  virtual-30077] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23274856, Group=Фора по партиям, SemanticKey=HANDICAP_2_-1.5, ExistingValue=1.76, NewValue=2.25, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-04T22:32:02.276Z ERROR 1 --- [igaming-source-betcity] [  virtual-30077] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23274856, Group=Фора по партиям, SemanticKey=HANDICAP_1_2.5, ExistingValue=1.85, NewValue=1.19, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-04T22:32:02.276Z ERROR 1 --- [igaming-source-betcity] [  virtual-30077] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23274856, Group=Фора по партиям, SemanticKey=HANDICAP_2_-2.5, ExistingValue=1.85, NewValue=4.1, ExistingName='Kf_F2', NewName='Kf_F2'
    ```

---

### 🗂️ Букмекер: `bettery`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-bettery-crawler-7c9d7dd99f-2tvjz` (2/2, Running, рестартов: 9, возраст: 9h)
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
* **Pod:** `igaming-source-fansport-crawler-67fcdc568c-9f666` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:32:36.734Z  WARN 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 406
  name='Error
  stack='Error: Error: HTTP status 406
2026-06-04T22:32:40.018Z  WARN 1 --- [igaming-source-fansport] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Absolute page fetch failed: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fansport-loader-679488f688-k6kpz` (1/2, Running, рестартов: 0, возраст: 2m)
* **Pod:** `igaming-source-fansport-loader-77bbc988bb-w8lms` (2/2, Running, рестартов: 0, возраст: 8m)

---

### 🗂️ Букмекер: `fon-bet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-by-crawler-5cfc5cbd5d-c8gsx` (2/2, Running, рестартов: 4, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:30:21.491Z  WARN 1 --- [igaming-source-fon-bet-by] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line22.by0e87-resources.by/ma/events/list?lang=ru&scopeMarket=700&version=77838669914&ts=1780612213857, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:31:15.847Z ERROR 1 --- [igaming-source-fon-bet-by] [ loader-sched-3] p.d.i.s.c.e.f.s.JsonSchemaValidator      : JSON schema validation failed! Errors: [$: required property 'lineQueues' not found]
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-04T21:59:18.164Z  WARN 1 --- [igaming-source-fon-bet-by] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line21.by0e87-resources.by/ma/events/list?lang=ru&scopeMarket=700&version=77837449052&ts=1780610351985, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-by-loader-6896679699-74qsd` (2/2, Running, рестартов: 0, возраст: 2h)

---

### 🗂️ Букмекер: `fon-bet-kz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-kz-crawler-b4fd69968-n9xzq` (2/2, Running, рестартов: 1, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:27:13.664Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line21-w.kzac51-resources.kz/ma/events/list?lang=ru&scopeMarket=1800&version=77838406125&ts=1780612029805, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:31:25.790Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
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
* **Pod:** `igaming-source-fon-bet-kz-loader-756ddc9764-w8vpn` (2/2, Running, рестартов: 0, возраст: 2h)

---

### 🗂️ Букмекер: `fon-bet-ru`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-ru-crawler-6ffd85df54-2kc28` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:29:51.501Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line05w.bk6bba-resources.com/events/list?lang=ru&scopeMarket=1600&version=77838746321&ts=1780612187106, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:30:59.950Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line05w.bk6bba-resources.com/events/list?lang=ru&scopeMarket=1600&version=77838777911&ts=1780612255954, falling back to direct navigation: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-ru-loader-6cc99f76f7-8f68l` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:18:57.254Z  WARN 1 --- [igaming-source-fon-bet-ru] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```

---

### 🗂️ Букмекер: `leon`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-leon-crawler-54cbb59bfc-wnxkk` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T21:48:15.479Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4140, NEW=0, PENDING=3, PROCESSED=4135, FAILED=2
2026-06-04T21:50:15.493Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4141, NEW=0, PENDING=3, PROCESSED=4136, FAILED=2
2026-06-04T21:51:15.473Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4141, NEW=0, PENDING=8, PROCESSED=4132, FAILED=1
2026-06-04T21:52:15.477Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4147, NEW=0, PENDING=3, PROCESSED=4143, FAILED=1
2026-06-04T21:53:15.488Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4150, NEW=0, PENDING=3, PROCESSED=4146, FAILED=1
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-leon-loader-685c48b699-zw2ch` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T21:48:51.425Z  INFO 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4140, NEW=0, PENDING=3, PROCESSED=4135, FAILED=2
2026-06-04T21:49:51.440Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4141, NEW=0, PENDING=3, PROCESSED=4136, FAILED=2
2026-06-04T21:51:51.421Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4146, NEW=0, PENDING=3, PROCESSED=4142, FAILED=1
2026-06-04T21:52:51.432Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4150, NEW=0, PENDING=3, PROCESSED=4146, FAILED=1
2026-06-04T21:53:51.458Z  INFO 1 --- [igaming-source-leon] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=4152, NEW=0, PENDING=3, PROCESSED=4148, FAILED=1
    ```

---

### 🗂️ Букмекер: `ligastavok`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-ligastavok-crawler-55d4b4b9f-hll6p` (2/2, Running, рестартов: 0, возраст: 1h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:28:59.877Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, IMask
2026-06-04T22:31:07.470Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-ligastavok-loader-578f758947-6c82w` (2/2, Running, рестартов: 0, возраст: 1h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:27:44.736Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, IMask
2026-06-04T22:29:50.821Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
2026-06-04T22:31:13.696Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: net::ERR_CONTENT_LENGTH_MISMATCH
2026-06-04T22:32:26.082Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://ligastavok.ru: Read timed out
2026-06-04T22:32:26.083Z ERROR 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'ligastavok.ru' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `linebet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-linebet-crawler-b98c98fb4-p2lvl` (2/2, Running, рестартов: 0, возраст: 2m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:32:05.520Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 406
  name='Error
  stack='Error: Error: HTTP status 406
2026-06-04T22:32:06.412Z  INFO 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.betb2b.service.Betb2bApiClient   : Fetch succeeded. Preview (first 100 chars): {"type":"feed/NotAcceptableException","title":"NotAcceptable","status":406,"detail":"Error occurred 
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-linebet-loader-5d46889f4d-pxc5l` (2/2, Running, рестартов: 0, возраст: 8m)
* **Pod:** `igaming-source-linebet-loader-fc8c6d4c9-skqb4` (1/2, Running, рестартов: 0, возраст: 3m)

---

### 🗂️ Букмекер: `marathonbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-crawler-d4c579db5-g7xpg` (2/2, Running, рестартов: 0, возраст: 14m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:20:03.242Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/react/event/menu/prematch: Cannot find object to call __adopt__: page@2a81643b9f6544686a2c4779cf6ff660
2026-06-04T22:20:03.242Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-2] p.d.i.s.m.s.MarathonDiscoveryService     : Cannot fetch prematch menu, aborting league queueing
2026-06-04T22:25:03.745Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Cricket/Women/Twenty20/2nd+Twenty20+-+3192596: Cannot find object to call __adopt__: page@ebfc6c3e9778ccb5d22698f56da814b4
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-loader-54455f96bb-g8j7h` (2/2, Running, рестартов: 0, возраст: 14m)
  * **Ошибки в текущих логах:**
    ```text
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
    ```

---

### 🗂️ Букмекер: `marathonbet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-by-crawler-5c8f6b7ff-fzfwb` (2/2, Running, рестартов: 0, возраст: 14m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:22:44.333Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.m.s.MarathonDiscoveryService     : Failed to save event 28960333: Transaction silently rolled back because it has been marked as rollback-only
2026-06-04T22:22:44.417Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23505
2026-06-04T22:22:44.417Z ERROR 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: duplicate key value violates unique constraint "uk6k7py0ytg93arkeip5y2rb6t8"
2026-06-04T22:22:44.440Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.m.s.MarathonDiscoveryService     : Failed to save event 28960349: Transaction silently rolled back because it has been marked as rollback-only
2026-06-04T22:22:44.570Z  WARN 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.m.s.MarathonDiscoveryService     : Failed to save event 28960347: Transaction silently rolled back because it has been marked as rollback-only
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-by-loader-59f4f6d495-f24c8` (2/2, Running, рестартов: 0, возраст: 14m)
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
* **Pod:** `igaming-source-marathonbet-com-crawler-68c9f4c6f6-mmq7s` (2/2, Running, рестартов: 0, возраст: 14m)
  * **Ошибки в текущих логах:**
    ```text
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-com-loader-6b5956ccd-7xj7n` (2/2, Running, рестартов: 0, возраст: 14m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:29:39.597Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: HTTP check returned status 451
2026-06-04T22:29:39.597Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-04T22:29:49.971Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
2026-06-04T22:29:51.139Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.marathonbet.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
    ```

---

### 🗂️ Букмекер: `megapari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-megapari-crawler-d67fbc5c-hcjk4` (2/2, Running, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:31:38.084Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Relative page fetch failed: Error {
  message='Error: HTTP status 406
  name='Error
  stack='Error: Error: HTTP status 406
2026-06-04T22:31:40.479Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Absolute page fetch failed: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-megapari-loader-765478549f-djxk7` (1/2, Running, рестартов: 0, возраст: 3m)
* **Pod:** `igaming-source-megapari-loader-cd5f9cc8f-5cv5g` (2/2, Running, рестартов: 0, возраст: 9m)

---

### 🗂️ Букмекер: `melbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-melbet-crawler-778868c55f-mg28s` (2/2, Running, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:33:01.096Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.betb2b.service.Betb2bApiClient   : All fetch attempts failed for LIVE
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-melbet-loader-66bdb4549b-td44m` (1/2, Running, рестартов: 0, возраст: 3m)
* **Pod:** `igaming-source-melbet-loader-7664c9496f-lcz4n` (2/2, Running, рестартов: 0, возраст: 9m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: Error from server (NotFound): pods "igaming-source-melbet-loader-7664c9496f-lcz4n" not found
    ```

---

### 🗂️ Букмекер: `pari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pari-crawler-5bd7bbfdcb-gw25t` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:31:04.733Z  WARN 1 --- [igaming-source-pari] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line-lb01-w.pb06e2-resources.com/events/list?lang=ru&scopeMarket=2300&version=77838779706&ts=1780612260923, falling back to direct navigation: Error {
  message='TypeError: Failed to fetch
  name='Error
  stack='Error: TypeError: Failed to fetch
2026-06-04T22:32:13.945Z  WARN 1 --- [igaming-source-pari] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://line-lb01-w.pb06e2-resources.com/events/list?lang=ru&scopeMarket=2300&version=77838814047&ts=1780612329379, falling back to direct navigation: Error {
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pari-loader-8646dd7647-gfs25` (2/2, Running, рестартов: 0, возраст: 2h)

---

### 🗂️ Букмекер: `pinnacle`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pinnacle-crawler-79477f6847-m7f7c` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:24:19.079Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:24:19.296Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:24:19.564Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:24:19.779Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-04T22:24:20.075Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pinnacle-loader-986b558c4-mccfj` (2/2, Running, рестартов: 0, возраст: 2h)

---

### 🗂️ Букмекер: `sbobet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sbobet-crawler-7bc7c6b577-ftvdv` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:32:02.515Z  WARN 1 --- [igaming-source-sbobet] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET API fetch stats: failed to load 1 out of 1 pages in the last cycle. Reasons: {HTML instead of JSON=1}
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sbobet-loader-8b5665776-4dmql` (2/2, Running, рестартов: 0, возраст: 2h)

---

### 🗂️ Букмекер: `tennisi`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-tennisi-crawler-978bdf5f7-n8lpn` (2/2, Running, рестартов: 0, возраст: 27m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-tennisi-loader-6b9945fb7f-8kkrz` (2/2, Running, рестартов: 0, возраст: 27m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:19:36.606Z ERROR 1 --- [igaming-source-tennisi] [    virtual-709] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485205, SemanticKey=TOTAL_OVER_0.5, ExistingValue=3.7, NewValue=1.42, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:19:41.835Z ERROR 1 --- [igaming-source-tennisi] [    virtual-710] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485204, SemanticKey=TOTAL_UNDER_0.5, ExistingValue=2.2, NewValue=1.38, ExistingOriginalName='TM', NewOriginalName='TM', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:19:41.836Z ERROR 1 --- [igaming-source-tennisi] [    virtual-710] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485204, SemanticKey=TOTAL_OVER_0.5, ExistingValue=1.6, NewValue=2.8, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:19:41.840Z ERROR 1 --- [igaming-source-tennisi] [    virtual-711] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485209, SemanticKey=TOTAL_UNDER_0.5, ExistingValue=1.28, NewValue=1.02, ExistingOriginalName='TM', NewOriginalName='TM', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-04T22:19:41.843Z ERROR 1 --- [igaming-source-tennisi] [    virtual-711] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2352485209, SemanticKey=TOTAL_OVER_0.5, ExistingValue=3.3, NewValue=10.0, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
    ```

---

### 🗂️ Букмекер: `winline`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-winline-crawler-6c497467d7-fj9vv` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
  message='Timeout 15000ms exceeded.
  name='TimeoutError
  stack='TimeoutError: Timeout 15000ms exceeded.
com.microsoft.playwright.TimeoutError: Error {
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-winline-loader-6d7c49b666-dqp2q` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:12:37.804Z ERROR 1 --- [igaming-source-winline] [   virtual-8399] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15944182, SemanticKey=TOTAL_UNDER_20.5, ExistingValue=1.99, NewValue=1.89, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='4-й период тотал [b]', NewGroup='5-й период тотал [b]'
2026-06-04T22:12:37.832Z ERROR 1 --- [igaming-source-winline] [   virtual-8399] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15944182, SemanticKey=TOTAL_OVER_22.5, ExistingValue=2.76, NewValue=2.87, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='4-й период тотал [b]', NewGroup='5-й период тотал [b]'
2026-06-04T22:12:37.833Z ERROR 1 --- [igaming-source-winline] [   virtual-8399] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15944182, SemanticKey=TOTAL_UNDER_22.5, ExistingValue=1.39, NewValue=1.37, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='4-й период тотал [b]', NewGroup='5-й период тотал [b]'
2026-06-04T22:12:37.903Z ERROR 1 --- [igaming-source-winline] [   virtual-8399] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15944182, SemanticKey=TOTAL_OVER_19.5, ExistingValue=1.47, NewValue=1.55, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='4-й период тотал [b]', NewGroup='5-й период тотал [b]'
2026-06-04T22:12:37.904Z ERROR 1 --- [igaming-source-winline] [   virtual-8399] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15944182, SemanticKey=TOTAL_UNDER_19.5, ExistingValue=2.49, NewValue=2.3, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='4-й период тотал [b]', NewGroup='5-й период тотал [b]'
    ```

---

### 🗂️ Букмекер: `zenit`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-zenit-crawler-88dc8cdf9-d8zjc` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:31:11.958Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
2026-06-04T22:31:47.893Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Zenit API fetch stats: failed to load 1 out of 6 pages in the last cycle. Reasons: {Empty response from browser=1}
2026-06-04T22:33:13.110Z ERROR 1 --- [igaming-source-zenit] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50: Error {
  name='Error
  stack='Error: net::ERR_ABORTED; maybe frame was detached?
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-zenit-loader-7cf77966d7-v9l2n` (2/2, Running, рестартов: 0, возраст: 2h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-04T22:18:31.147Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```

---

## 🟢 Исправные БК (Без ошибок)

* `betboom`:
  * Crawler pod `igaming-source-betboom-crawler-685b8c5fc-kmlq8` (2/2, Running, restarts: 0, age: 2h)
  * Loader pod `igaming-source-betboom-loader-5d784467db-8dxn5` (2/2, Running, restarts: 0, age: 2h)
* `betcity-com`:
  * Loader pod `igaming-source-betcity-com-loader-5565d767fb-gwtvl` (2/2, Running, restarts: 0, age: 1h)
* `olimpbet`:
  * Crawler pod `igaming-source-olimpbet-crawler-769f8554b6-nrjrj` (2/2, Running, restarts: 0, age: 2h)
  * Loader pod `igaming-source-olimpbet-loader-6f979799b-glnkp` (2/2, Running, restarts: 0, age: 2h)
* `olimpbet-kz`:
  * Crawler pod `igaming-source-olimpbet-kz-crawler-859459bdd6-j2g49` (2/2, Running, restarts: 0, age: 1h)
  * Loader pod `igaming-source-olimpbet-kz-loader-fc7959846-t56cf` (2/2, Running, restarts: 0, age: 1h)
* `sportbet`:
  * Crawler pod `igaming-source-sportbet-crawler-6d5f976fd7-24pqb` (2/2, Running, restarts: 0, age: 2h)
  * Loader pod `igaming-source-sportbet-loader-7484dd7dcd-k4qts` (2/2, Running, restarts: 0, age: 2h)

---

## ⚪ Не развернутые букмекеры (Нет подов)

Все букмекеры развернуты в системе.
