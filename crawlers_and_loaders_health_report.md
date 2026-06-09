# 📋 Отчет о состоянии краулеров и лоадеров букмекеров

**Дата проверки:** 2026-06-09 17:22:25

## 📊 Сводная статистика

* **Всего букмекеров в отчете:** 32
* **🟢 Полностью исправные БК (без ошибок):** 3
* **🔴 БК с ошибками/сбоями:** 29
* **⚪ Не развернутые БК (нет подов):** 0

### 🔍 Обзор по всем букмекерам

| Букмекер | Статус | Краулер Pod (Готовность / Рестарты) | Лоадер Pod (Готовность / Рестарты) |
| :--- | :--- | :--- | :--- |
| `1xbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `888starz` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `baltbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `bet365` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betandyou` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betboom` | 🟢 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betcity` | 🔴 | `2/2` (рестартов: 1) | `2/2` (рестартов: 0) |
| `betcity-com` | 🔴 | `1/1` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betm` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `bettery` | 🔴 | `2/2` (рестартов: 4) | `2/2` (рестартов: 0) |
| `dafabet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `fansport` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `fon-bet-by` | 🔴 | `2/2` (рестартов: 2) | `1/2` (рестартов: 0) |
| `fon-bet-kz` | 🔴 | `2/2` (рестартов: 1) | `2/2` (рестартов: 0) |
| `fon-bet-ru` | 🟢 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `leon` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `ligastavok` | 🔴 | `2/2` (рестартов: 2) | `2/2` (рестартов: 3) |
| `linebet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet` | 🔴 | `2/2` (рестартов: 14) | `2/2` (рестартов: 0) |
| `marathonbet-by` | 🔴 | `2/2` (рестартов: 2) | `2/2` (рестартов: 0) |
| `marathonbet-com` | 🔴 | `2/2` (рестартов: 7) | `2/2` (рестартов: 0) |
| `megapari` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `melbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `olimpbet` | 🟢 | `2/2` (рестартов: 1) | `2/2` (рестартов: 0) |
| `olimpbet-kz` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `pari` | 🔴 | `2/2` (рестартов: 5) | `2/2` (рестартов: 0) |
| `pinnacle` | 🔴 | `2/2` (рестартов: 0) | `1/2` (рестартов: 475) |
| `sbobet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `sportbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 1) |
| `tennisi` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `winline` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `zenit` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |

---

## 🔴 Детализация ошибок и проблемных БК

### 🗂️ Букмекер: `1xbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-1xbet-crawler-58d98dc65c-crdch` (2/2, Running, рестартов: 0, возраст: 25m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:05:47.319Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":1453,"HL":true,"RLI":["5af2b
2026-06-09T14:11:00.151Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":1},"PS"
2026-06-09T14:11:11.691Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":1129,"HL":true,"RLI":["5af2b
2026-06-09T14:16:32.655Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":808,"HL":true,"RLI":["5af2b3
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-1xbet-loader-66fddd86d4-6rbvd` (2/2, Running, рестартов: 0, возраст: 25m)
  * **Ошибки в текущих логах:**
    ```text
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
    ```

---

### 🗂️ Букмекер: `888starz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-888starz-crawler-798fffcdbf-8t9n2` (2/2, Running, рестартов: 0, возраст: 20m)
  * **Ошибки в текущих логах:**
    ```text
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-888starz-loader-55c594dbc-7vd4v` (2/2, Running, рестартов: 0, возраст: 19m)
  * **Ошибки в текущих логах:**
    ```text
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
    ```

---

### 🗂️ Букмекер: `baltbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-baltbet-crawler-6cd846587f-vzxd7` (2/2, Running, рестартов: 0, возраст: 19m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:17:14.391Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://events.baltbet.ru/api/grouping/event/30500224: Cannot find object to call __adopt__: page@cb7fc057c265edc333097748d2874ea9
2026-06-09T14:17:14.400Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.b.service.BaltbetApiClient       : Failed to fetch Baltbet grouping event details from https://events.baltbet.ru/api/grouping/event/30500224
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-baltbet-loader-5cfd58dcbf-ccf84` (2/2, Running, рестартов: 0, возраст: 19m)

---

### 🗂️ Букмекер: `bet365`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-bet365-crawler-7f69d7984-r999c` (2/2, Running, рестартов: 0, возраст: 19m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:16:33.848Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.bet365.com: Blocked by Cloudflare/security system
2026-06-09T14:16:37.087Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.bet365.com: No route to host
2026-06-09T14:16:37.089Z  WARN 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.bet365.com': java.net.NoRouteToHostException: No route to host
2026-06-09T14:16:45.150Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.bet365.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T14:19:07.377Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.bet365.com: Connect timed out
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-bet365-loader-75d6bc8797-fxp6l` (2/2, Running, рестартов: 0, возраст: 19m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:05:04.215Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.bet365.com: No route to host
2026-06-09T14:05:04.215Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.bet365.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T14:05:19.807Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.bet365.com: HTTP check returned status 451
2026-06-09T14:05:19.808Z  WARN 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.bet365.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-09T14:05:33.209Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.bet365.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
    ```

---

### 🗂️ Букмекер: `betandyou`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betandyou-crawler-6fb88bd8f8-dlhb9` (2/2, Running, рестартов: 0, возраст: 18m)
  * **Ошибки в текущих логах:**
    ```text
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betandyou-loader-5dcbc7588b-vn755` (2/2, Running, рестартов: 0, возраст: 18m)
  * **Ошибки в текущих логах:**
    ```text
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
    ```

---

### 🗂️ Букмекер: `betcity`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betcity-crawler-55644bf67c-skvc7` (2/2, Running, рестартов: 1, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:18:40.380Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23310168, Group=Фора по партиям, SemanticKey=HANDICAP_1_1.5, ExistingValue=1.9, NewValue=1.62, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T14:18:40.380Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23310168, Group=Фора по партиям, SemanticKey=HANDICAP_2_-1.5, ExistingValue=1.8, NewValue=2.15, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T14:18:41.034Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23305434: java.lang.NullPointerException
java.lang.NullPointerException: null
2026-06-09T14:18:41.855Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305433, Group=Фора по партиям, SemanticKey=HANDICAP_1_2.5, ExistingValue=1.95, NewValue=1.1, ExistingName='Kf_F1', NewName='Kf_F1'
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T08:21:24.035Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T08:21:24.037Z ERROR 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-betcity-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-betcity-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betcity-loader-78c5958b8f-sspm4` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:18:41.656Z ERROR 1 --- [igaming-source-betcity] [  virtual-41137] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23310168, Group=Фора по партиям, SemanticKey=HANDICAP_1_1.5, ExistingValue=1.9, NewValue=1.62, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T14:18:41.658Z ERROR 1 --- [igaming-source-betcity] [  virtual-41137] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23310168, Group=Фора по партиям, SemanticKey=HANDICAP_2_-1.5, ExistingValue=1.8, NewValue=2.15, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T14:18:43.794Z ERROR 1 --- [igaming-source-betcity] [  virtual-41144] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305433, Group=Фора по партиям, SemanticKey=HANDICAP_1_2.5, ExistingValue=1.95, NewValue=1.1, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T14:18:43.795Z ERROR 1 --- [igaming-source-betcity] [  virtual-41144] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305433, Group=Фора по партиям, SemanticKey=HANDICAP_2_-2.5, ExistingValue=1.76, NewValue=5.7, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T14:18:43.800Z ERROR 1 --- [igaming-source-betcity] [  virtual-41144] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null
    ```

---

### 🗂️ Букмекер: `betcity-com`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betcity-com-crawler-6d574669c8-2krbn` (1/1, Running, рестартов: 0, возраст: 5m)
  * **Ошибки в текущих логах:**
    ```text
Caused by: java.net.UnknownHostException: ad.betsbc.com: No address associated with hostname
2026-06-09T14:18:08.480Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.b.service.BetCityApiClient       : Error fetching line info
org.springframework.web.client.ResourceAccessException: I/O error on GET request for "https://ad.betsbc.com/d/on_air/bets": ad.betsbc.com: No address associated with hostname
	at org.springframework.web.client.RestTemplate.createResourceAccessException(RestTemplate.java:926) ~[spring-web-6.2.1.jar!/:6.2.1]
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betcity-com-loader-6b4664cc66-sdr5z` (2/2, Running, рестартов: 0, возраст: 18m)

---

### 🗂️ Букмекер: `betm`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betm-crawler-5bc75456f7-fgxbk` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:19:06.349Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23310168, Group=Фора по партиям, SemanticKey=HANDICAP_1_1.5, ExistingValue=1.85, NewValue=1.57, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T14:19:06.349Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23310168, Group=Фора по партиям, SemanticKey=HANDICAP_2_-1.5, ExistingValue=1.85, NewValue=2.25, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T14:19:06.806Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23305434: java.lang.NullPointerException
java.lang.NullPointerException: null
2026-06-09T14:19:07.473Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305433, Group=Фора по партиям, SemanticKey=HANDICAP_1_2.5, ExistingValue=1.8, NewValue=1.09, ExistingName='Kf_F1', NewName='Kf_F1'
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betm-loader-788bcfc94b-9qc52` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:19:08.807Z ERROR 1 --- [igaming-source-betcity] [  virtual-41411] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23310168, Group=Фора по партиям, SemanticKey=HANDICAP_1_1.5, ExistingValue=1.85, NewValue=1.57, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T14:19:08.809Z ERROR 1 --- [igaming-source-betcity] [  virtual-41411] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23310168, Group=Фора по партиям, SemanticKey=HANDICAP_2_-1.5, ExistingValue=1.85, NewValue=2.25, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T14:19:08.809Z ERROR 1 --- [igaming-source-betcity] [  virtual-41416] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305433, Group=Фора по партиям, SemanticKey=HANDICAP_1_2.5, ExistingValue=1.8, NewValue=1.09, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T14:19:08.809Z ERROR 1 --- [igaming-source-betcity] [  virtual-41416] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305433, Group=Фора по партиям, SemanticKey=HANDICAP_2_-2.5, ExistingValue=1.9, NewValue=6.2, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T14:19:08.810Z ERROR 1 --- [igaming-source-betcity] [  virtual-41416] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305433, Group=Фора по партиям, SemanticKey=HANDICAP_1_1.5, ExistingValue=1.9, NewValue=1.49, ExistingName='Kf_F1', NewName='Kf_F1'
    ```

---

### 🗂️ Букмекер: `bettery`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-bettery-crawler-6dd8bc8c49-r55zk` (2/2, Running, рестартов: 4, возраст: 5h)
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T12:10:41.497Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://line01.at58f5-resources.com/events/list?lang=ru&scopeMarket=501&version=78103420610&ts=1781007035785: Object doesn't exist: response@fc16f323a5830561bf0da3cdcf4720ac
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-bettery-loader-586b68f556-zk5d2` (2/2, Running, рестартов: 0, возраст: 5h)

---

### 🗂️ Букмекер: `dafabet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-dafabet-crawler-86b6b4488b-rm9c9` (2/2, Running, рестартов: 0, возраст: 18m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:09:14.534Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=201, NEW=0, PENDING=0, PROCESSED=201, FAILED=0
2026-06-09T14:10:14.399Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=203, NEW=0, PENDING=0, PROCESSED=203, FAILED=0
2026-06-09T14:11:14.284Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=230, NEW=31, PENDING=0, PROCESSED=199, FAILED=0
2026-06-09T14:12:14.240Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=237, NEW=0, PENDING=0, PROCESSED=237, FAILED=0
2026-06-09T14:13:14.374Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=249, NEW=0, PENDING=0, PROCESSED=249, FAILED=0
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-dafabet-loader-66b5ff58c7-bf4bv` (2/2, Running, рестартов: 0, возраст: 18m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:06:12.378Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=16, NEW=0, PENDING=0, PROCESSED=16, FAILED=0
2026-06-09T14:07:12.180Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=201, NEW=0, PENDING=0, PROCESSED=201, FAILED=0
2026-06-09T14:10:12.267Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=203, NEW=0, PENDING=0, PROCESSED=203, FAILED=0
2026-06-09T14:11:12.249Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=227, NEW=23, PENDING=0, PROCESSED=205, FAILED=0
2026-06-09T14:12:12.178Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=237, NEW=0, PENDING=0, PROCESSED=237, FAILED=0
    ```

---

### 🗂️ Букмекер: `fansport`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fansport-crawler-6d7bdd4b99-hkk5v` (2/2, Running, рестартов: 0, возраст: 18m)
  * **Ошибки в текущих логах:**
    ```text
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
2026-06-09T14:07:25.457Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-3] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":1355,"HL":true,"RLI":["5af2b
2026-06-09T14:12:30.007Z  INFO 1 --- [igaming-source-fansport] [ loader-sched-1] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":1},"PS"
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fansport-loader-76b54fbdf-t5vmp` (2/2, Running, рестартов: 0, возраст: 18m)
  * **Ошибки в текущих логах:**
    ```text
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
    ```

---

### 🗂️ Букмекер: `fon-bet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-by-crawler-5df8cbcb9f-6bvvr` (2/2, Running, рестартов: 2, возраст: 5h)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-by-loader-56bf65d66c-pzdnz` (1/2, Running, рестартов: 0, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
    ```
* **Pod:** `igaming-source-fon-bet-by-loader-68cc55f84b-tglz6` (2/2, Running, рестартов: 0, возраст: 21h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:17:46.360Z ERROR 1 --- [igaming-source-fon-bet-by] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fonbet.by: Read timed out
2026-06-09T14:17:46.360Z ERROR 1 --- [igaming-source-fon-bet-by] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'fonbet.by' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `fon-bet-kz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-kz-crawler-7dc7fb7449-dlm56` (2/2, Running, рестартов: 1, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:15:51.299Z  WARN 1 --- [igaming-source-fon-bet-kz] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Absolute page fetch failed: Error {
  message='Error: HTTP status 403
  name='Error
  stack='Error: Error: HTTP status 403
2026-06-09T14:16:31.098Z ERROR 1 --- [igaming-source-fon-bet-kz] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fonbet.kz: Read timed out
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T08:26:54.953Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T08:26:54.953Z ERROR 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-fon-bet-kz-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-fon-bet-kz-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-kz-loader-56c465c554-v2whg` (2/2, Running, рестартов: 0, возраст: 5h)

---

### 🗂️ Букмекер: `leon`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-leon-crawler-99b7d5d89-8t4md` (2/2, Running, рестартов: 0, возраст: 7m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:13:37.362Z ERROR 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://leon.ru: No route to host
2026-06-09T14:13:37.363Z  WARN 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 2/3] Connectivity check failed for 'leon.ru': java.net.NoRouteToHostException: No route to host
2026-06-09T14:13:45.431Z ERROR 1 --- [igaming-source-leon] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'leon.ru' after 3 attempts. Please check VPN pool status at http://igaming-aggregator:80/api/v1/vpn-pool/status
2026-06-09T14:14:06.035Z ERROR 1 --- [igaming-source-leon] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'leon.ru' is unreachable through current proxy! Triggering rotation...
	delivery.timeout.ms = 120000
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-leon-loader-56c5c8d96d-t5vw5` (2/2, Running, рестартов: 0, возраст: 7m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:14:23.932Z  INFO 1 --- [igaming-source-leon] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=3483, NEW=537, PENDING=3, PROCESSED=2932, FAILED=11
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
    ```

---

### 🗂️ Букмекер: `ligastavok`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-ligastavok-crawler-df7487976-szbpv` (2/2, Running, рестартов: 2, возраст: 5h)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-ligastavok-loader-7df96dc4b6-pz4p9` (2/2, Running, рестартов: 3, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:03:21.429Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__metaTags","__TSR_ROUTER__","__LS_CONTAINER_ONLY_HYDRATION__","fhConfig"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, IMask, enableReduxLogs, disableReduxLogs, __metaTags, __TSR_ROUTER__, __LS_CONTAINER_ONLY_HYDRATION__, changeTheme, fhConfig, fhContext
2026-06-09T14:05:30.636Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T12:42:42.979Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-2] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__TSR_ROUTER__","__LS_CONTAINER_ONLY_HYDRATION__","fhConfig"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, __state, IMask, enableReduxLogs, disableReduxLogs, __TSR_ROUTER__, __LS_CONTAINER_ONLY_HYDRATION__, changeTheme, fhConfig, fhContext
2026-06-09T12:43:41.320Z  WARN 1 --- [igaming-source-ligastavok] [ loader-sched-3] p.d.i.s.l.s.LigastavokStateExtractor     : Failed to refresh serviceId map: Error {
  name='Error
  stack='Error:Execution context was destroyed, most likely because of a navigation
Error
    ```

---

### 🗂️ Букмекер: `linebet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-linebet-crawler-58754d5d7-rdlt7` (2/2, Running, рестартов: 0, возраст: 18m)
  * **Ошибки в текущих логах:**
    ```text
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-linebet-loader-8fc458bdb-lpn62` (2/2, Running, рестартов: 0, возраст: 18m)
  * **Ошибки в текущих логах:**
    ```text
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
    ```

---

### 🗂️ Букмекер: `marathonbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-crawler-76d777577f-j5sdv` (2/2, Running, рестартов: 14, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-loader-6b7886bb67-zdw8q` (2/2, Running, рестартов: 0, возраст: 5h)

---

### 🗂️ Букмекер: `marathonbet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-by-crawler-6f696db68d-c9m59` (2/2, Running, рестартов: 2, возраст: 5h)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-by-loader-6f89755dcd-n2nct` (2/2, Running, рестартов: 0, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:19:12.363Z ERROR 1 --- [igaming-source-marathonbet-by] [ virtual-307587] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/FIFA+World+Cup/2026/Final+Tournament/USA%2C+Canada%2C+Mexico/Group+Stage/Switzerland+vs+Canada+-+25895221: Playwright connection closed
2026-06-09T14:19:12.364Z  WARN 1 --- [igaming-source-marathonbet-by] [ virtual-307595] p.d.i.s.m.service.MarathonbetApiClient   : Error fetching all markets for event 27813080 after 1 attempt(s): Playwright connection closed
2026-06-09T14:19:18.407Z  WARN 1 --- [igaming-source-marathonbet-by] [ virtual-307607] p.d.i.s.m.service.MarathonbetApiClient   : Error fetching all markets for event 27817107 after 1 attempt(s): Playwright connection closed
2026-06-09T14:19:18.407Z ERROR 1 --- [igaming-source-marathonbet-by] [ virtual-307611] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/European+U-19+Championship/2027/Qualification/Greece/Kazakhstan+U-19+vs+Greece+U-19+-+28946991: Playwright connection closed
2026-06-09T14:19:18.407Z ERROR 1 --- [igaming-source-marathonbet-by] [ virtual-307613] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Internationals/European+U-19+Championship/2027/Qualification/Georgia/Georgia+U-19+vs+Iceland+U-19+-+28946977: Playwright connection closed
    ```
* **Pod:** `igaming-source-marathonbet-by-loader-7b754469b7-rnqxl` (1/2, Running, рестартов: 1, возраст: 3m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:19:54.327Z ERROR 1 --- [igaming-source-marathonbet-by] [     virtual-31] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/e-Sports/Counter-Strike+2/Major/IEM/Cologne/Main+Event/Best+of+3+maps/TyLoo+vs+Legacy+-+29105019: Object doesn't exist: frame@b8de490e02f2b981a24e9a603bd6674f
2026-06-09T14:19:54.597Z ERROR 1 --- [igaming-source-marathonbet-by] [     virtual-33] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/e-Sports/LoL/EMEA+Masters/Main+Event/Best+of+3+maps/Frites+Esports+Club+vs+Bomba+-+29110379: Cannot invoke "com.microsoft.playwright.impl.FrameImpl.navigateImpl(String, com.microsoft.playwright.Frame$NavigateOptions)" because "this.mainFrame" is null
    ```

---

### 🗂️ Букмекер: `marathonbet-com`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-com-crawler-548458c88-cjlp7` (2/2, Running, рестартов: 7, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:35:49.309Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T13:36:08.522Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: Read timed out
2026-06-09T13:36:08.522Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.com': java.net.SocketTimeoutException: Read timed out
2026-06-09T13:36:40.187Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.marathonbet.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: Connection to igaming-source-marathonbet-com-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Caused by: java.net.ConnectException: Connection refused
2026-06-09T09:04:37.997Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T09:04:37.997Z ERROR 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-marathonbet-com-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T09:04:38.005Z ERROR 1 --- [igaming-source-marathonbet-com] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-marathonbet-com-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-com-loader-7b8c888fcd-mtprv` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:35:43.749Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: Read timed out
2026-06-09T13:35:43.750Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `megapari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-megapari-crawler-5bf45b49b8-hsd8s` (2/2, Running, рестартов: 0, возраст: 17m)
  * **Ошибки в текущих логах:**
    ```text
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
2026-06-09T14:10:16.675Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-4] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":1184,"HL":true,"RLI":["5af2b
2026-06-09T14:15:30.799Z  INFO 1 --- [igaming-source-megapari] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":1},"PS"
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-megapari-loader-6c47c8c7d5-nszh5` (2/2, Running, рестартов: 0, возраст: 17m)
  * **Ошибки в текущих логах:**
    ```text
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
    ```

---

### 🗂️ Букмекер: `melbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-melbet-crawler-8547f65849-rxh6f` (2/2, Running, рестартов: 0, возраст: 16m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:13:11.441Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : All fetch attempts failed for PREMATCH
2026-06-09T14:19:00.439Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-4] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : All fetch attempts failed for LIVE
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-melbet-loader-7784759f76-6q7cn` (2/2, Running, рестартов: 0, возраст: 16m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:05:19.154Z ERROR 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://melbet.ru: No route to host
2026-06-09T14:05:19.154Z  WARN 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'melbet.ru': java.net.NoRouteToHostException: No route to host
2026-06-09T14:05:35.408Z ERROR 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'melbet.ru' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T14:06:01.905Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'melbet.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T14:06:05.259Z ERROR 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://melbet.ru: Remote host terminated the handshake
    ```

---

### 🗂️ Букмекер: `olimpbet-kz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-olimpbet-kz-crawler-774fb9dcd8-6dt2g` (2/2, Running, рестартов: 0, возраст: 16m)
  * **Ошибки в текущих логах:**
    ```text
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-olimpbet-kz-loader-9cc47dd9c-cmrbj` (2/2, Running, рестартов: 0, возраст: 16m)
  * **Ошибки в текущих логах:**
    ```text
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
	socket.connection.setup.timeout.ms = 10000
	transaction.timeout.ms = 60000
    ```

---

### 🗂️ Букмекер: `pari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pari-crawler-c47b6bcbf-ttcrl` (2/2, Running, рестартов: 5, возраст: 5h)
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T13:16:55.727Z ERROR 1 --- [igaming-source-pari] [ loader-sched-3] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Error during fetch from mirror https://line-lb51-w.pb06e2-resources.com: Unexpected end-of-input: was expecting closing quote for a string value
2026-06-09T13:17:09.015Z ERROR 1 --- [igaming-source-pari] [ loader-sched-4] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Error during fetch from mirror https://line-nx-w.pb06e2-resources.ru: Unexpected end-of-input: was expecting closing quote for a string value
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pari-loader-656c4dd8cf-dk8vl` (2/2, Running, рестартов: 0, возраст: 5h)

---

### 🗂️ Букмекер: `pinnacle`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pinnacle-crawler-5c4cf4f658-btkxl` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:11:51.176Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T14:11:51.237Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 19: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T14:11:51.286Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 33: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T14:11:51.351Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 4: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
2026-06-09T14:11:51.413Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 34: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pinnacle-loader-5dfd58bcf-swhmc` (1/2, Running, рестартов: 475, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:17:53.069Z  WARN 1 --- [igaming-source-pinnacle] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'genericMatchLoadScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/lib/igaming-source-core-0.0.1-SNAPSHOT.jar!/pro/datawiki/igaming/source/core/scheduler/GenericMatchLoadScheduler.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-06-09T14:17:53.776Z ERROR 1 --- [igaming-source-pinnacle] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 
APPLICATION FAILED TO START
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T14:17:53.069Z  WARN 1 --- [igaming-source-pinnacle] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'genericMatchLoadScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/lib/igaming-source-core-0.0.1-SNAPSHOT.jar!/pro/datawiki/igaming/source/core/scheduler/GenericMatchLoadScheduler.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-06-09T14:17:53.776Z ERROR 1 --- [igaming-source-pinnacle] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 
APPLICATION FAILED TO START
    ```
* **Pod:** `igaming-source-pinnacle-loader-6b7bd98f8-t5b48` (1/2, Running, рестартов: 54, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:20:04.920Z  WARN 1 --- [igaming-source-pinnacle] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'genericMatchLoadScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/lib/igaming-source-core-0.0.1-SNAPSHOT.jar!/pro/datawiki/igaming/source/core/scheduler/GenericMatchLoadScheduler.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-06-09T14:20:05.303Z ERROR 1 --- [igaming-source-pinnacle] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 
APPLICATION FAILED TO START
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T14:20:04.920Z  WARN 1 --- [igaming-source-pinnacle] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'genericMatchLoadScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/lib/igaming-source-core-0.0.1-SNAPSHOT.jar!/pro/datawiki/igaming/source/core/scheduler/GenericMatchLoadScheduler.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-06-09T14:20:05.303Z ERROR 1 --- [igaming-source-pinnacle] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 
APPLICATION FAILED TO START
    ```

---

### 🗂️ Букмекер: `sbobet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sbobet-crawler-59dcf6666b-cth5t` (2/2, Running, рестартов: 0, возраст: 16m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:19:18.113Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=251, NEW=0, PENDING=0, PROCESSED=251, FAILED=0
2026-06-09T14:20:18.141Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=252, NEW=1, PENDING=0, PROCESSED=251, FAILED=0
2026-06-09T14:21:18.134Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=252, NEW=0, PENDING=0, PROCESSED=252, FAILED=0
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sbobet-loader-5f48ddc648-jndhg` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: Error from server (NotFound): pods "igaming-source-sbobet-loader-5f48ddc648-jndhg" not found
    ```
* **Pod:** `igaming-source-sbobet-loader-6d9d6455c9-qnkvv` (1/2, Running, рестартов: 0, возраст: 16m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:15:17.002Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=245, NEW=0, PENDING=0, PROCESSED=245, FAILED=0
2026-06-09T14:17:15.755Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=250, NEW=0, PENDING=0, PROCESSED=250, FAILED=0
2026-06-09T14:18:15.777Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-2] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=251, NEW=0, PENDING=0, PROCESSED=251, FAILED=0
2026-06-09T14:20:15.833Z  INFO 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.c.s.AbstractApiErrorTracker      : SBOBET DB Stats: Total=252, NEW=0, PENDING=0, PROCESSED=252, FAILED=0
    ```

---

### 🗂️ Букмекер: `sportbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sportbet-crawler-65f58886cb-khjmn` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:18:03.298Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://sportbet.ru: No route to host
2026-06-09T14:18:03.300Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'sportbet.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T14:18:03.297Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-1] p.d.i.s.s.service.SportbetApiClient      : Error fetching line info
org.springframework.web.client.ResourceAccessException: I/O error on GET request for "https://sportbet.ru/sport/v1/all-fixtures-tree": No route to host
	at org.springframework.web.client.RestTemplate.createResourceAccessException(RestTemplate.java:926) ~[spring-web-6.2.1.jar!/:6.2.1]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sportbet-loader-7b7c6f9895-hctp6` (2/2, Running, рестартов: 1, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:11:06.714Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://sportbet.ru: No route to host
2026-06-09T14:11:06.714Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'sportbet.ru' is unreachable through current proxy! Triggering rotation...
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: Connection to igaming-source-sportbet-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Caused by: java.net.ConnectException: Connection refused
2026-06-09T09:11:47.180Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T09:11:47.180Z ERROR 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-sportbet-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T09:11:47.189Z ERROR 1 --- [igaming-source-sportbet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-sportbet-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```

---

### 🗂️ Букмекер: `tennisi`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-tennisi-crawler-cf475fbd4-2jxtn` (2/2, Running, рестартов: 0, возраст: 4h)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-tennisi-loader-5c6ddd486d-x6sc9` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:58:29.550Z ERROR 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://tennisi.bet: Connect timed out
2026-06-09T13:58:29.550Z ERROR 1 --- [igaming-source-tennisi] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'tennisi.bet' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `winline`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-winline-crawler-6c5667b6b4-8v878` (2/2, Running, рестартов: 0, возраст: 4m)
  * **Ошибки в текущих логах:**
    ```text
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-winline-loader-587d8b78db-t8h22` (2/2, Running, рестартов: 0, возраст: 16m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:11:58.431Z ERROR 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://winline.ru: Unexpected end of file from server
2026-06-09T14:11:58.431Z ERROR 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'winline.ru' is unreachable through current proxy! Triggering rotation...
	delivery.timeout.ms = 120000
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
    ```

---

### 🗂️ Букмекер: `zenit`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-zenit-crawler-7ff978fd4b-wgsfr` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T14:19:46.316Z ERROR 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50: Error {
  name='Error
  stack='Error: net::ERR_HTTP_RESPONSE_CODE_FAILURE at https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50
2026-06-09T14:19:46.316Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.zenit.service.ZenitApiClient     : Failed to fetch data, empty response
2026-06-09T14:20:54.108Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Zenit API fetch stats: failed to load 1 out of 6 pages in the last cycle. Reasons: {Empty response from browser=1}
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-zenit-loader-7b788c4988-c4zkv` (2/2, Running, рестартов: 0, возраст: 5h)

---

## 🟢 Исправные БК (Без ошибок)

* `betboom`:
  * Crawler pod `igaming-source-betboom-crawler-6df57fb6df-56v5v` (2/2, Running, restarts: 0, age: 5h)
  * Loader pod `igaming-source-betboom-loader-674b6f9687-4nfz4` (2/2, Running, restarts: 0, age: 1d)
* `fon-bet-ru`:
  * Crawler pod `igaming-source-fon-bet-ru-crawler-754d57d4c6-wqt9n` (2/2, Running, restarts: 0, age: 5h)
  * Loader pod `igaming-source-fon-bet-ru-loader-6f5fc57c6-x8wxc` (2/2, Running, restarts: 0, age: 5h)
* `olimpbet`:
  * Crawler pod `igaming-source-olimpbet-crawler-777944fddf-fwjfn` (2/2, Running, restarts: 1, age: 5h)
  * Loader pod `igaming-source-olimpbet-loader-9bddb778b-lmlb2` (2/2, Running, restarts: 0, age: 5h)

---

## ⚪ Не развернутые букмекеры (Нет подов)

Все букмекеры развернуты в системе.
