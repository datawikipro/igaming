# 📋 Отчет о состоянии краулеров и лоадеров букмекеров

**Дата проверки:** 2026-06-09 16:48:52

## 📊 Сводная статистика

* **Всего букмекеров в отчете:** 32
* **🟢 Полностью исправные БК (без ошибок):** 2
* **🔴 БК с ошибками/сбоями:** 30
* **⚪ Не развернутые БК (нет подов):** 0

### 🔍 Обзор по всем букмекерам

| Букмекер | Статус | Краулер Pod (Готовность / Рестарты) | Лоадер Pod (Готовность / Рестарты) |
| :--- | :--- | :--- | :--- |
| `1xbet` | 🔴 | `2/2` (рестартов: 4) | `2/2` (рестартов: 0) |
| `888starz` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `baltbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `bet365` | 🔴 | `2/2` (рестартов: 1) | `2/2` (рестартов: 0) |
| `betandyou` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betboom` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `betcity` | 🔴 | `2/2` (рестартов: 1) | `2/2` (рестартов: 0) |
| `betcity-com` | 🔴 | - | `2/2` (рестартов: 0) |
| `betm` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `bettery` | 🔴 | `2/2` (рестартов: 4) | `2/2` (рестартов: 0) |
| `dafabet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `fansport` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `fon-bet-by` | 🟢 | `2/2` (рестартов: 2) | `2/2` (рестартов: 0) |
| `fon-bet-kz` | 🟢 | `2/2` (рестартов: 1) | `2/2` (рестартов: 0) |
| `fon-bet-ru` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `leon` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `ligastavok` | 🔴 | `2/2` (рестартов: 2) | `2/2` (рестартов: 3) |
| `linebet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `marathonbet` | 🔴 | `2/2` (рестартов: 12) | `2/2` (рестартов: 0) |
| `marathonbet-by` | 🔴 | `2/2` (рестартов: 2) | `2/2` (рестартов: 0) |
| `marathonbet-com` | 🔴 | `2/2` (рестартов: 7) | `2/2` (рестартов: 0) |
| `megapari` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `melbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `olimpbet` | 🔴 | `2/2` (рестартов: 1) | `2/2` (рестартов: 0) |
| `olimpbet-kz` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `pari` | 🔴 | `2/2` (рестартов: 5) | `2/2` (рестартов: 0) |
| `pinnacle` | 🔴 | `2/2` (рестартов: 0) | `1/2` (рестартов: 469) |
| `sbobet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `sportbet` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 1) |
| `tennisi` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |
| `winline` | 🔴 | `2/2` (рестартов: 1) | `2/2` (рестартов: 0) |
| `zenit` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 0) |

---

## 🔴 Детализация ошибок и проблемных БК

### 🗂️ Букмекер: `1xbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-1xbet-crawler-6968f84799-qfk5p` (2/2, Running, рестартов: 4, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:44:26.315Z ERROR 1 --- [igaming-source-1xbet] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:44:26.316Z ERROR 1 --- [igaming-source-1xbet] [ loader-sched-4] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 727601310: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:44:26.320Z  WARN 1 --- [igaming-source-1xbet] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:44:26.328Z ERROR 1 --- [igaming-source-1xbet] [ loader-sched-4] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726206208: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:44:26.334Z ERROR 1 --- [igaming-source-1xbet] [ loader-sched-4] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726340921: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T06:20:56.604Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-4] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":34744,"HL":true,"RLI":["5af2
2026-06-09T06:26:03.513Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":125,"SC":{"FS":{},"PS":[{"Ke
2026-06-09T06:26:09.637Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"B":34431,"HL":true,"RLI":["5af2
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-1xbet-loader-7d47c8bc45-4w94x` (2/2, Running, рестартов: 0, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:44:20.787Z  WARN 1 --- [igaming-source-1xbet] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:44:20.787Z ERROR 1 --- [igaming-source-1xbet] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:44:20.802Z ERROR 1 --- [igaming-source-1xbet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for 1xbet: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `888starz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-888starz-crawler-7d5b54f67f-mp8hh` (2/2, Running, рестартов: 0, возраст: 7h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:40:38.265Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:40:38.278Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726340921: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:40:38.292Z  WARN 1 --- [igaming-source-888starz] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:40:38.306Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726206205: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:40:38.338Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-2] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726206199: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-888starz-loader-6f45cb6f4c-dn2hm` (2/2, Running, рестартов: 0, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:44:28.565Z  WARN 1 --- [igaming-source-888starz] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:44:28.565Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:44:28.587Z ERROR 1 --- [igaming-source-888starz] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for 888starz: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `baltbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-baltbet-crawler-57d69ffd57-6kb88` (2/2, Running, рестартов: 0, возраст: 7h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:45:08.887Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.e.b.s.BaltbetEventDiscoverer   : Error processing Baltbet event 30496172: JDBC exception executing SQL [select sc1_0.id,sc1_0.external_id,sc1_0.kind,sc1_0.name,sc1_0.parent_id,sc1_0.updated_at from sport_cache sc1_0 where sc1_0.external_id=?] [ERROR: relation "sport_cache" does not exist
2026-06-09T13:45:08.901Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:45:08.901Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "sport_cache" does not exist
2026-06-09T13:45:08.901Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.e.b.s.BaltbetEventDiscoverer   : Error processing Baltbet event 30496177: JDBC exception executing SQL [select sc1_0.id,sc1_0.external_id,sc1_0.kind,sc1_0.name,sc1_0.parent_id,sc1_0.updated_at from sport_cache sc1_0 where sc1_0.external_id=?] [ERROR: relation "sport_cache" does not exist
2026-06-09T13:45:08.916Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-4] p.d.i.s.c.e.b.s.BaltbetEventDiscoverer   : Error processing Baltbet event 30482618: JDBC exception executing SQL [select sc1_0.id,sc1_0.external_id,sc1_0.kind,sc1_0.name,sc1_0.parent_id,sc1_0.updated_at from sport_cache sc1_0 where sc1_0.external_id=?] [ERROR: relation "sport_cache" does not exist
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-baltbet-loader-868b69cfd5-4qzhq` (2/2, Running, рестартов: 0, возраст: 19h)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:44:55.863Z  WARN 1 --- [igaming-source-baltbet] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:44:55.863Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:44:55.866Z ERROR 1 --- [igaming-source-baltbet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for baltbet: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `bet365`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-bet365-crawler-6f6d7ffc8f-qgmzb` (2/2, Running, рестартов: 1, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:41:31.031Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.bet365.com: HTTP check returned status 451
2026-06-09T13:41:31.031Z  WARN 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 3/3] Connectivity check failed for 'www.bet365.com': java.lang.RuntimeException: HTTP check returned status 451
2026-06-09T13:41:31.031Z ERROR 1 --- [igaming-source-bet365] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.bet365.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T13:41:37.517Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.bet365.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T13:41:47.670Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.bet365.com: Blocked by Cloudflare/security system
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
HTML Error Response (Access Denied/Forbidden): Title='HTML Page'
Sample log line: 	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-bet365-loader-57d646cddd-tx7nm` (2/2, Running, рестартов: 0, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:45:28.706Z  WARN 1 --- [igaming-source-bet365] [ loader-sched-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:45:28.706Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:45:28.728Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for bet365: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `betandyou`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betandyou-crawler-6c86c54d78-prxp4` (2/2, Running, рестартов: 0, возраст: 19h)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: error: http2: client connection lost
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betandyou-loader-6d659cc594-kbjlf` (2/2, Running, рестартов: 0, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:45:46.466Z  WARN 1 --- [igaming-source-betandyou] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:45:46.466Z ERROR 1 --- [igaming-source-betandyou] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:45:46.486Z ERROR 1 --- [igaming-source-betandyou] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for betandyou: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `betboom`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betboom-crawler-6df57fb6df-56v5v` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:40:15.143Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/prematch: Object doesn't exist: frame@aedc95d2cbfdfa210e9c1cd0eb0afa14
2026-06-09T13:41:15.602Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://siteapi.betboom.ru: Remote host terminated the handshake
2026-06-09T13:41:15.602Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'siteapi.betboom.ru' is unreachable through current proxy! Triggering rotation...
2026-06-09T13:45:17.076Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-4] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/live: Object doesn't exist: frame@e7199f9dda73022c227519190de9fab4
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betboom-loader-674b6f9687-4nfz4` (2/2, Running, рестартов: 0, возраст: 1d)

---

### 🗂️ Букмекер: `betcity`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betcity-crawler-55644bf67c-skvc7` (2/2, Running, рестартов: 1, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:45:34.316Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23305420: java.lang.NullPointerException
java.lang.NullPointerException: null
2026-06-09T13:45:36.291Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305426, Group=Фора по партиям, SemanticKey=HANDICAP_1_1.5, ExistingValue=2.05, NewValue=1.62, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T13:45:36.291Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305426, Group=Фора по партиям, SemanticKey=HANDICAP_2_-1.5, ExistingValue=1.69, NewValue=2.15, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T13:45:37.894Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23315709, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.48, NewValue=1.16, ExistingName='X2', NewName='X2'
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
2026-06-09T13:45:32.216Z ERROR 1 --- [igaming-source-betcity] [  virtual-37781] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305424, Group=Фора по партиям, SemanticKey=HANDICAP_1_-1.5, ExistingValue=1.95, NewValue=2.45, ExistingName='Kf_F1', NewName='Kf_F1'
2026-06-09T13:45:32.217Z ERROR 1 --- [igaming-source-betcity] [  virtual-37781] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305424, Group=Фора по партиям, SemanticKey=HANDICAP_2_1.5, ExistingValue=1.76, NewValue=1.49, ExistingName='Kf_F2', NewName='Kf_F2'
2026-06-09T13:45:32.230Z ERROR 1 --- [igaming-source-betcity] [  virtual-37781] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: null
java.lang.NullPointerException: null
2026-06-09T13:45:37.626Z ERROR 1 --- [igaming-source-betcity] [  virtual-37789] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23305426, Group=Фора по партиям, SemanticKey=HANDICAP_1_1.5, ExistingValue=2.05, NewValue=1.62, ExistingName='Kf_F1', NewName='Kf_F1'
    ```

---

### 🗂️ Букмекер: `betcity-com`

#### 🕷️ Краулер (Crawler): *Не запущен*
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betcity-com-loader-7d87b48d59-2x58p` (2/2, Running, рестартов: 0, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T08:21:48.142Z  WARN 1 --- [igaming-source-betcity] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T08:21:48.142Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T08:21:48.256Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.b.scheduler.MatchLoadScheduler   : Error during scheduled Betcity match loading: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `betm`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betm-crawler-5bc75456f7-fgxbk` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:45:32.283Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23315709, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.48, NewValue=1.16, ExistingName='X2', NewName='X2'
2026-06-09T13:46:43.291Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23209765: java.lang.NullPointerException
java.lang.NullPointerException: null
2026-06-09T13:46:43.630Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23219374: java.lang.NullPointerException
2026-06-09T13:46:44.586Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23247842: java.lang.NullPointerException
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betm-loader-788bcfc94b-9qc52` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:45:35.729Z ERROR 1 --- [igaming-source-betcity] [  virtual-37789] p.d.i.s.c.engine.AbstractOddsProcessor   : CRITICAL DUPLICATE COEFFICIENT ERROR: Mapping collision detected! Event=23315709, Group=Промежуточный исход, SemanticKey=DC_X2, ExistingValue=1.48, NewValue=1.16, ExistingName='X2', NewName='X2'
2026-06-09T13:46:46.266Z ERROR 1 --- [igaming-source-betcity] [  virtual-37815] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betm: null
java.lang.NullPointerException: null
    ```

---

### 🗂️ Букмекер: `bettery`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-bettery-crawler-6dd8bc8c49-r55zk` (2/2, Running, рестартов: 4, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:45:35.924Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-4] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Error during fetch from mirror https://line51.at58f5-resources.com: Unexpected end-of-input: expected close marker for Object (start marker at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 222853])
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T12:10:41.497Z ERROR 1 --- [igaming-source-bettery] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://line01.at58f5-resources.com/events/list?lang=ru&scopeMarket=501&version=78103420610&ts=1781007035785: Object doesn't exist: response@fc16f323a5830561bf0da3cdcf4720ac
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-bettery-loader-586b68f556-zk5d2` (2/2, Running, рестартов: 0, возраст: 5h)

---

### 🗂️ Букмекер: `dafabet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-dafabet-crawler-7587bc68cd-9hhgp` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:46:25.764Z  WARN 1 --- [igaming-source-dafabet] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:46:25.764Z ERROR 1 --- [igaming-source-dafabet] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:46:25.766Z ERROR 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.dafabet.service.MatchService     : Failed to process Dafabet event ID 1.28741923E8: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:46:25.770Z ERROR 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.dafabet.service.MatchService     : Failed to process Dafabet event ID 1.28741731E8: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:46:25.774Z ERROR 1 --- [igaming-source-dafabet] [ loader-sched-4] p.d.i.s.dafabet.service.MatchService     : Failed to process Dafabet event ID 1.28781744E8: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-dafabet-loader-86996dcdb8-2nbx7` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:46:52.904Z  WARN 1 --- [igaming-source-dafabet] [ loader-sched-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:46:52.904Z ERROR 1 --- [igaming-source-dafabet] [ loader-sched-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:46:52.913Z ERROR 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for dafabet: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `fansport`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fansport-crawler-7b85fc596c-55q9g` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:43:35.299Z  WARN 1 --- [igaming-source-fansport] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:43:35.299Z ERROR 1 --- [igaming-source-fansport] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:43:35.301Z ERROR 1 --- [igaming-source-fansport] [ loader-sched-4] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726340921: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:43:35.305Z ERROR 1 --- [igaming-source-fansport] [ loader-sched-4] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726206205: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:43:35.311Z ERROR 1 --- [igaming-source-fansport] [ loader-sched-4] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726206199: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fansport-loader-66988776c9-mkttc` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:46:25.587Z  WARN 1 --- [igaming-source-fansport] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:46:25.587Z ERROR 1 --- [igaming-source-fansport] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:46:25.708Z ERROR 1 --- [igaming-source-fansport] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for fansport: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `fon-bet-ru`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-ru-crawler-754d57d4c6-wqt9n` (2/2, Running, рестартов: 0, возраст: 5h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:46:06.779Z ERROR 1 --- [igaming-source-fon-bet-ru] [ loader-sched-2] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Error during fetch from mirror https://line-lb52.bk6bba-resources.ru: Unexpected end-of-input: was expecting closing quote for a string value
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-ru-loader-6f5fc57c6-x8wxc` (2/2, Running, рестартов: 0, возраст: 5h)

---

### 🗂️ Букмекер: `leon`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-leon-crawler-86f7c76b6-j5g7z` (2/2, Running, рестартов: 0, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:47:04.662Z ERROR 1 --- [igaming-source-leon] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:47:04.725Z ERROR 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.l.service.LeonDiscoveryService   : Failed to save match metadata for event 1970324852178137: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:47:04.744Z  WARN 1 --- [igaming-source-leon] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:47:04.761Z ERROR 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.l.service.LeonDiscoveryService   : Failed to save match metadata for event 1970324852178136: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:47:05.685Z ERROR 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.l.service.LeonDiscoveryService   : Failed to save match metadata for event 1970324850514110: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-leon-loader-6c5bb74d48-zd7fg` (2/2, Running, рестартов: 0, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:47:27.403Z  WARN 1 --- [igaming-source-leon] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:47:27.403Z ERROR 1 --- [igaming-source-leon] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:47:27.435Z ERROR 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.l.scheduler.MatchLoadScheduler   : Error during match card loading
org.springframework.dao.InvalidDataAccessResourceUsageException: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
] [ERROR: relation "match_cache" does not exist
    ```

---

### 🗂️ Букмекер: `ligastavok`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-ligastavok-crawler-df7487976-szbpv` (2/2, Running, рестартов: 2, возраст: 4h)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-ligastavok-loader-7df96dc4b6-pz4p9` (2/2, Running, рестартов: 3, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:35:04.021Z DEBUG 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
2026-06-09T13:35:28.136Z  INFO 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state","__TSR_ROUTER__","__LS_CONTAINER_ONLY_HYDRATION__","__metaTags","fhConfig"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, __localization, __redirectRules, IMask, __state, enableReduxLogs, disableReduxLogs, __TSR_ROUTER__, __LS_CONTAINER_ONLY_HYDRATION__, __metaTags, changeTheme, fhConfig, fhContext
2026-06-09T13:37:17.864Z  WARN 1 --- [igaming-source-ligastavok] [ loader-sched-4] p.d.i.s.l.s.LigastavokStateExtractor     : Failed to refresh serviceId map: Error {
  name='Error
  stack='Error:Execution context was destroyed, most likely because of a navigation
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
* **Pod:** `igaming-source-linebet-crawler-6685db986b-szff6` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:45:11.247Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-3] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726206208: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:45:11.249Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:45:11.249Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:45:11.252Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-3] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726340921: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:45:11.255Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-3] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726206205: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-linebet-loader-64b5c49656-7cngq` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:46:55.817Z  WARN 1 --- [igaming-source-linebet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:46:55.817Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:46:55.818Z ERROR 1 --- [igaming-source-linebet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for linebet: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `marathonbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-crawler-76d777577f-j5sdv` (2/2, Running, рестартов: 12, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:45:18.062Z ERROR 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.ru: No route to host
2026-06-09T13:45:18.063Z  WARN 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.ru': java.net.NoRouteToHostException: No route to host
2026-06-09T13:45:23.015Z ERROR 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.ru: Read timed out
2026-06-09T13:45:23.015Z  WARN 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.ru': java.net.SocketTimeoutException: Read timed out
2026-06-09T13:45:31.226Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/betting/Chess/Friendly+Tournaments/Dvorkovich+Memorial+Tournament+-+1672970: Error {
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T12:47:41.170Z ERROR 1 --- [igaming-source-marathonbet] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.ru/su/react/event/menu/prematch: Error {
  name='Error
  stack='Error: A "socket" was not created for HTTP request before 15000ms
    at Timeout.u [as _onTimeout] (/tmp/playwright-java-3948051983564597771/package/lib/utilsBundleImpl/index.js:7:1401)
    at listOnTimeout (node:internal/timers:573:17)
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-loader-6b7886bb67-zdw8q` (2/2, Running, рестартов: 0, возраст: 4h)

---

### 🗂️ Букмекер: `marathonbet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-by-crawler-6f696db68d-c9m59` (2/2, Running, рестартов: 2, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:34:12.920Z ERROR 1 --- [igaming-source-marathonbet-by] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/Football/Ecuador/Serie+A+-+341061: Error {
  name='Error
  stack='Error: net::ERR_ADDRESS_UNREACHABLE at https://www.marathonbet.by/su/betting/Football/Ecuador/Serie+A+-+341061
2026-06-09T13:35:31.795Z ERROR 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/react/event/menu/prematch: Unexpected character ('<' (code 60)): expected a valid value (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
2026-06-09T13:35:31.795Z ERROR 1 --- [igaming-source-marathonbet-by] [ loader-sched-3] p.d.i.s.m.s.MarathonDiscoveryService     : Cannot fetch prematch menu, aborting league queueing
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: Connection to igaming-source-marathonbet-by-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Caused by: java.net.ConnectException: Connection refused
2026-06-09T08:55:30.808Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T08:55:30.808Z ERROR 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-marathonbet-by-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T08:55:30.815Z ERROR 1 --- [igaming-source-marathonbet-by] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-marathonbet-by-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-by-loader-6f89755dcd-n2nct` (2/2, Running, рестартов: 0, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:44:18.856Z  WARN 1 --- [igaming-source-marathonbet-by] [ virtual-306510] p.d.i.s.m.service.MarathonbetApiClient   : Error fetching all markets for event 29102352 after 1 attempt(s): Playwright connection closed
2026-06-09T13:44:18.856Z ERROR 1 --- [igaming-source-marathonbet-by] [ virtual-306510] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/e-Sports/Rainbow+Six+Siege/China+League/Main+Event/Best+of+3+maps/Leviatan+Esports+vs+Four+Angry+Man+-+29102352: Playwright connection closed
2026-06-09T13:44:18.856Z ERROR 1 --- [igaming-source-marathonbet-by] [ virtual-306511] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/e-Sports/Rainbow+Six+Siege/China+League/Main+Event/Best+of+3+maps/Titan+Esports+Club+vs+One+Coin+-+29102353: Playwright connection closed
2026-06-09T13:44:24.240Z ERROR 1 --- [igaming-source-marathonbet-by] [ virtual-306512] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/e-Sports/Rainbow+Six+Siege/Europe+MENA+League/Main+Event/One+map/Fnatic+vs+Geekay+Esports+-+29114646: Playwright connection closed
2026-06-09T13:44:24.241Z ERROR 1 --- [igaming-source-marathonbet-by] [ virtual-306513] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.by/su/betting/e-Sports/Rainbow+Six+Siege/Europe+MENA+League/Main+Event/One+map/Virtus.pro+vs+Rebels+Gaming+-+29114638: Playwright connection closed
    ```

---

### 🗂️ Букмекер: `marathonbet-com`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-com-crawler-548458c88-cjlp7` (2/2, Running, рестартов: 7, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:32:24.899Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: Remote host terminated the handshake
2026-06-09T13:32:24.899Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T13:32:29.730Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.marathonbet.com/su/betting/Badminton/World+Tour/Super+500/Sydney%2C+Australia/Men+-+16452538: Error {
  name='Error
  stack='Error: net::ERR_CONNECTION_CLOSED at https://www.marathonbet.com/su/betting/Badminton/World+Tour/Super+500/Sydney%2C+Australia/Men+-+16452538
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
* **Pod:** `igaming-source-marathonbet-com-loader-7b8c888fcd-mtprv` (2/2, Running, рестартов: 0, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:14:40.672Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.com: Read timed out
2026-06-09T13:14:40.674Z  WARN 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'www.marathonbet.com': java.net.SocketTimeoutException: Read timed out
2026-06-09T13:15:10.715Z ERROR 1 --- [igaming-source-marathonbet-com] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'www.marathonbet.com' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T13:16:46.107Z ERROR 1 --- [igaming-source-marathonbet-com] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.marathonbet.com' is unreachable through current proxy! Triggering rotation...
    ```

---

### 🗂️ Букмекер: `megapari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-megapari-crawler-5f7755fbc8-h7kgm` (2/2, Running, рестартов: 0, возраст: 7h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:43:44.811Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:43:44.811Z ERROR 1 --- [igaming-source-megapari] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:43:44.825Z ERROR 1 --- [igaming-source-megapari] [ loader-sched-4] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726340921: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:43:44.852Z ERROR 1 --- [igaming-source-megapari] [ loader-sched-4] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726206205: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:43:44.882Z ERROR 1 --- [igaming-source-megapari] [ loader-sched-4] .d.i.s.c.e.x.s.XbetFamilyEventDiscoverer : Error processing Xbet Family event 726206199: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-megapari-loader-588c867fd5-hvtm4` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:47:21.274Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:47:21.274Z ERROR 1 --- [igaming-source-megapari] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:47:21.277Z ERROR 1 --- [igaming-source-megapari] [ loader-sched-4] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for megapari: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `melbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-melbet-crawler-656b6f97d9-cl9mc` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:46:41.902Z ERROR 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://melbet.ru: No route to host
2026-06-09T13:46:41.902Z  WARN 1 --- [igaming-source-melbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'melbet.ru': java.net.NoRouteToHostException: No route to host
2026-06-09T13:46:47.814Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://melbet.ru/LiveFeed/Get1xMatchByLeague?sports=1: Error {
  name='Error
  stack='Error: net::ERR_TIMED_OUT at https://melbet.ru/LiveFeed/Get1xMatchByLeague?sports=1
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-melbet-loader-6f978dd5cf-tt79k` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:47:21.875Z  WARN 1 --- [igaming-source-melbet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:47:21.875Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:47:21.879Z ERROR 1 --- [igaming-source-melbet] [ loader-sched-3] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for melbet: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `olimpbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-olimpbet-crawler-777944fddf-fwjfn` (2/2, Running, рестартов: 1, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:32:49.485Z ERROR 1 --- [igaming-source-olimpbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.olimp.bet: Read timed out
2026-06-09T13:32:49.485Z ERROR 1 --- [igaming-source-olimpbet] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'www.olimp.bet' is unreachable through current proxy! Triggering rotation...
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Failed to fetch logs: Error from server (BadRequest): previous terminated container "igaming-source-olimpbet-crawler" in pod "igaming-source-olimpbet-crawler-777944fddf-fwjfn" not found
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-olimpbet-loader-9bddb778b-lmlb2` (2/2, Running, рестартов: 0, возраст: 4h)

---

### 🗂️ Букмекер: `olimpbet-kz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-olimpbet-kz-crawler-6dd575656c-xkqr4` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:47:30.891Z ERROR 1 --- [igaming-source-olimpbet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:47:30.894Z ERROR 1 --- [igaming-source-olimpbet] [ loader-sched-3] p.d.i.s.c.e.o.s.OlimpbetEventDiscoverer  : Error processing Olimpbet V2 event: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:47:30.895Z  WARN 1 --- [igaming-source-olimpbet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:47:30.895Z ERROR 1 --- [igaming-source-olimpbet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "sport_cache" does not exist
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-olimpbet-kz-loader-5df68c6444-8j77m` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:48:05.477Z  WARN 1 --- [igaming-source-olimpbet] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:48:05.477Z ERROR 1 --- [igaming-source-olimpbet] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:48:05.478Z ERROR 1 --- [igaming-source-olimpbet] [ loader-sched-2] p.d.i.s.o.scheduler.MatchLoadScheduler   : Error during scheduled Olimpbet match loading: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `pari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pari-crawler-c47b6bcbf-ttcrl` (2/2, Running, рестартов: 5, возраст: 4h)
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T13:16:55.727Z ERROR 1 --- [igaming-source-pari] [ loader-sched-3] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Error during fetch from mirror https://line-lb51-w.pb06e2-resources.com: Unexpected end-of-input: was expecting closing quote for a string value
2026-06-09T13:17:09.015Z ERROR 1 --- [igaming-source-pari] [ loader-sched-4] .s.c.e.f.s.AbstractFonbetFamilyApiClient : Error during fetch from mirror https://line-nx-w.pb06e2-resources.ru: Unexpected end-of-input: was expecting closing quote for a string value
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pari-loader-656c4dd8cf-dk8vl` (2/2, Running, рестартов: 0, возраст: 4h)

---

### 🗂️ Букмекер: `pinnacle`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pinnacle-crawler-5c4cf4f658-btkxl` (2/2, Running, рестартов: 0, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:41:15.605Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: Remote host terminated the handshake
2026-06-09T13:41:15.605Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-1] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'pinnacle.com' is unreachable through current proxy! Triggering rotation...
2026-06-09T13:41:28.711Z ERROR 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: Read timed out
2026-06-09T13:41:28.712Z  WARN 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'pinnacle.com': java.net.SocketTimeoutException: Read timed out
2026-06-09T13:41:36.191Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.p.service.PinnacleApiClient      : Error fetching Pinnacle fixtures for sportId: 29: I/O error on GET request for "https://guest.api.pinnacle.com/v1/fixtures": Unable to tunnel through proxy. Proxy returns "HTTP/1.0 500 Unable to connect"
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pinnacle-loader-5dfd58bcf-swhmc` (1/2, Running, рестартов: 469, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:46:55.248Z  WARN 1 --- [igaming-source-pinnacle] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'genericMatchLoadScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/lib/igaming-source-core-0.0.1-SNAPSHOT.jar!/pro/datawiki/igaming/source/core/scheduler/GenericMatchLoadScheduler.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-06-09T13:46:55.484Z ERROR 1 --- [igaming-source-pinnacle] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 
APPLICATION FAILED TO START
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T13:46:55.248Z  WARN 1 --- [igaming-source-pinnacle] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'genericMatchLoadScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/lib/igaming-source-core-0.0.1-SNAPSHOT.jar!/pro/datawiki/igaming/source/core/scheduler/GenericMatchLoadScheduler.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-06-09T13:46:55.484Z ERROR 1 --- [igaming-source-pinnacle] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 
APPLICATION FAILED TO START
    ```
* **Pod:** `igaming-source-pinnacle-loader-6b7bd98f8-t5b48` (1/2, Running, рестартов: 49, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:43:33.195Z  WARN 1 --- [igaming-source-pinnacle] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'genericMatchLoadScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/lib/igaming-source-core-0.0.1-SNAPSHOT.jar!/pro/datawiki/igaming/source/core/scheduler/GenericMatchLoadScheduler.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-06-09T13:43:33.485Z ERROR 1 --- [igaming-source-pinnacle] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 
APPLICATION FAILED TO START
2026-06-09T13:43:34.213Z ERROR 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: Remote host terminated the handshake
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T13:43:33.195Z  WARN 1 --- [igaming-source-pinnacle] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'genericMatchLoadScheduler' defined in URL [jar:nested:/app/app.jar/!BOOT-INF/lib/igaming-source-core-0.0.1-SNAPSHOT.jar!/pro/datawiki/igaming/source/core/scheduler/GenericMatchLoadScheduler.class]: Unsatisfied dependency expressed through constructor parameter 0: No qualifying bean of type 'pro.datawiki.igaming.source.core.engine.AbstractBaseBookmakerService' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-06-09T13:43:33.485Z ERROR 1 --- [igaming-source-pinnacle] [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 
APPLICATION FAILED TO START
2026-06-09T13:43:34.213Z ERROR 1 --- [igaming-source-pinnacle] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: Remote host terminated the handshake
    ```

---

### 🗂️ Букмекер: `sbobet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sbobet-crawler-65f7888d97-9sqnw` (2/2, Running, рестартов: 0, возраст: 1d)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:47:08.594Z  WARN 1 --- [igaming-source-sbobet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:47:08.594Z ERROR 1 --- [igaming-source-sbobet] [ loader-sched-3] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:47:08.607Z ERROR 1 --- [igaming-source-sbobet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Failed to report match stats for SBOBET: JDBC exception executing SQL [select count(*) from match_cache mc1_0] [ERROR: relation "match_cache" does not exist
2026-06-09T13:47:55.260Z ERROR 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.sbobet.service.MatchService      : Failed to process SBOBET event ID 10043364: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
2026-06-09T13:47:55.340Z ERROR 1 --- [igaming-source-sbobet] [ loader-sched-4] p.d.i.s.sbobet.service.MatchService      : Failed to process SBOBET event ID 10054670: JDBC exception executing SQL [select mc1_0.id,mc1_0.bookmaker,mc1_0.created_at,mc1_0.event_url,mc1_0.external_id,mc1_0.external_updated_at,f1_0.match_id,f1_0.id,f1_0.factor_id,f1_0.name,f1_0.value,mc1_0.format_info,mc1_0.is_live,mc1_0.json_payload,mc1_0.last_changed_at,mc1_0.league_name,mc1_0.payload_hash,mc1_0.potential_end_time,mc1_0.score1,mc1_0.score2,mc1_0.sport_id,mc1_0.sport_name,mc1_0.start_time,mc1_0.status,mc1_0.team1,mc1_0.team2,mc1_0.updated_at,mc1_0.version from match_cache mc1_0 left join match_factor f1_0 on mc1_0.id=f1_0.match_id where mc1_0.external_id=? and mc1_0.bookmaker=?] [ERROR: relation "match_cache" does not exist
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sbobet-loader-5f48ddc648-jndhg` (2/2, Running, рестартов: 0, возраст: 19h)
  * **Ошибки в текущих логах:**
    ```text
] [ERROR: relation "match_cache" does not exist
2026-06-09T13:48:27.645Z  WARN 1 --- [igaming-source-sbobet] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T13:48:27.645Z ERROR 1 --- [igaming-source-sbobet] [ loader-sched-2] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "match_cache" does not exist
2026-06-09T13:48:27.646Z ERROR 1 --- [igaming-source-sbobet] [ loader-sched-2] p.d.i.s.c.s.GenericMatchLoadScheduler    : Error during scheduled match loading for sbobet: JDBC exception executing SQL [SELECT mc.* FROM match_cache mc
    ```

---

### 🗂️ Букмекер: `sportbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sportbet-crawler-65f58886cb-khjmn` (2/2, Running, рестартов: 0, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:45:23.432Z ERROR 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://sportbet.ru: No route to host
2026-06-09T13:45:23.433Z  WARN 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'sportbet.ru': java.net.NoRouteToHostException: No route to host
2026-06-09T13:45:39.561Z ERROR 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'sportbet.ru' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
2026-06-09T13:46:11.561Z ERROR 1 --- [igaming-source-sportbet] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'sportbet.ru' is unreachable through current proxy! Triggering rotation...
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sportbet-loader-7b7c6f9895-hctp6` (2/2, Running, рестартов: 1, возраст: 4h)

---

### 🗂️ Букмекер: `tennisi`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-tennisi-crawler-cf475fbd4-2jxtn` (2/2, Running, рестартов: 0, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:45:12.926Z ERROR 1 --- [igaming-source-tennisi] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://tennisi.bet: Connect timed out
2026-06-09T13:45:12.926Z ERROR 1 --- [igaming-source-tennisi] [ loader-sched-4] p.d.i.s.core.service.VpnManagerService   : Scheduled proxy check failed: target host 'tennisi.bet' is unreachable through current proxy! Triggering rotation...
2026-06-09T13:45:18.325Z ERROR 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://tennisi.bet: No route to host
2026-06-09T13:45:18.325Z  WARN 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'tennisi.bet': java.net.NoRouteToHostException: No route to host
2026-06-09T13:45:34.494Z ERROR 1 --- [igaming-source-tennisi] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : 🚫 REGIONAL RESTRICTION DETECTED: Cannot reach target host 'tennisi.bet' after 3 attempts. Please check VPN pool status at http://igaming-aggregator/api/v1/vpn-pool/status
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-tennisi-loader-5c6ddd486d-x6sc9` (2/2, Running, рестартов: 0, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:13:33.711Z ERROR 1 --- [igaming-source-tennisi] [   virtual-5590] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2357458635, SemanticKey=TOTAL_UNDER_3.5, ExistingValue=1.62, NewValue=1.5, ExistingOriginalName='TM', NewOriginalName='TM', ExistingGroup='Тотал', NewGroup='Тотал'
2026-06-09T13:13:33.712Z ERROR 1 --- [igaming-source-tennisi] [   virtual-5590] p.d.i.s.t.s.c.TennisiOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT for Tennisi: Tried to map 1 coefficient twice with DIFFERENT values! Event=2357458635, SemanticKey=TOTAL_OVER_3.5, ExistingValue=2.15, NewValue=2.4, ExistingOriginalName='TB', NewOriginalName='TB', ExistingGroup='Тотал', NewGroup='Тотал'
    ```

---

### 🗂️ Букмекер: `winline`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-winline-crawler-6c58955554-vqc44` (2/2, Running, рестартов: 1, возраст: 7h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:43:10.346Z ERROR 1 --- [igaming-source-winline] [ loader-sched-1] p.d.i.s.w.service.WinlineApiClient       : Fatal error during Winline live extraction: Object doesn't exist: response@3aad98d3773fe94db12b007e2eb5248b
com.microsoft.playwright.PlaywrightException: Object doesn't exist: response@3aad98d3773fe94db12b007e2eb5248b
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T10:46:15.662Z ERROR 1 --- [igaming-source-winline] [ loader-sched-4] p.d.i.s.c.engine.AbstractMatchService    : Error saving metadata for winline event: Transaction silently rolled back because it has been marked as rollback-only
2026-06-09T10:46:15.675Z  WARN 1 --- [igaming-source-winline] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 42P01
2026-06-09T10:46:15.675Z ERROR 1 --- [igaming-source-winline] [ loader-sched-4] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: relation "sport_cache" does not exist
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-winline-loader-686c4dd9db-5z64z` (2/2, Running, рестартов: 0, возраст: 20h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:40:05.637Z ERROR 1 --- [igaming-source-winline] [  virtual-56780] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15972261, SemanticKey=TEAM2_TOTAL_OVER_13.5, ExistingValue=1.6, NewValue=1.85, ExistingOriginalName='Больше', NewOriginalName='Больше', ExistingGroup='Тотал [a] команды 2', NewGroup='Тотал [a] эйсов команды 2'
2026-06-09T13:40:05.637Z ERROR 1 --- [igaming-source-winline] [  virtual-56780] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15972261, SemanticKey=TEAM2_TOTAL_UNDER_13.5, ExistingValue=2.15, NewValue=1.85, ExistingOriginalName='Меньше', NewOriginalName='Меньше', ExistingGroup='Тотал [a] команды 2', NewGroup='Тотал [a] эйсов команды 2'
2026-06-09T13:40:08.585Z ERROR 1 --- [igaming-source-winline] [  virtual-56790] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981573, SemanticKey=PERIOD_3_HANDICAP_1_2.5, ExistingValue=1.54, NewValue=1.47, ExistingOriginalName='1', NewOriginalName='1', ExistingGroup='3-й период фора [b] раундов', NewGroup='3-й период 2 половина фора [b]'
2026-06-09T13:40:08.586Z ERROR 1 --- [igaming-source-winline] [  virtual-56790] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981573, SemanticKey=PERIOD_3_HANDICAP_2_-2.5, ExistingValue=2.26, NewValue=2.5, ExistingOriginalName='2', NewOriginalName='2', ExistingGroup='3-й период фора [b] раундов', NewGroup='3-й период 2 половина фора [b]'
2026-06-09T13:40:08.678Z ERROR 1 --- [igaming-source-winline] [  virtual-56790] p.d.i.s.w.s.c.WinlineOddsProcessor       : DUPLICATE COEFFICIENT ATTEMPT: Tried to map 1 coefficient twice with DIFFERENT values! Event=15981573, SemanticKey=PERIOD_3_HANDICAP_1_3.5, ExistingValue=1.35, NewValue=1.32, ExistingOriginalName='1', NewOriginalName='1', ExistingGroup='3-й период фора [b] раундов', NewGroup='3-й период 2 половина фора [b]'
    ```

---

### 🗂️ Букмекер: `zenit`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-zenit-crawler-7ff978fd4b-wgsfr` (2/2, Running, рестартов: 0, возраст: 4h)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T13:48:20.985Z ERROR 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50: Error {
  name='Error
  stack='Error: net::ERR_HTTP_RESPONSE_CODE_FAILURE at https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50
2026-06-09T13:48:20.986Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.zenit.service.ZenitApiClient     : Failed to fetch data, empty response
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-zenit-loader-7b788c4988-c4zkv` (2/2, Running, рестартов: 0, возраст: 4h)

---

## 🟢 Исправные БК (Без ошибок)

* `fon-bet-by`:
  * Crawler pod `igaming-source-fon-bet-by-crawler-5df8cbcb9f-6bvvr` (2/2, Running, restarts: 2, age: 5h)
  * Loader pod `igaming-source-fon-bet-by-loader-68cc55f84b-tglz6` (2/2, Running, restarts: 0, age: 20h)
* `fon-bet-kz`:
  * Crawler pod `igaming-source-fon-bet-kz-crawler-7dc7fb7449-dlm56` (2/2, Running, restarts: 1, age: 5h)
  * Loader pod `igaming-source-fon-bet-kz-loader-56c465c554-v2whg` (2/2, Running, restarts: 0, age: 5h)

---

## ⚪ Не развернутые букмекеры (Нет подов)

Все букмекеры развернуты в системе.
