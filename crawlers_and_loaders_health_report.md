# 📋 Отчет о состоянии краулеров и лоадеров букмекеров

**Дата проверки:** 2026-06-09 23:08:01

## 📊 Сводная статистика

* **Всего букмекеров в отчете:** 32
* **🟢 Полностью исправные БК (без ошибок):** 0
* **🔴 БК с ошибками/сбоями:** 32
* **⚪ Не развернутые БК (нет подов):** 0

### 🔍 Обзор по всем букмекерам

| Букмекер | Статус | Краулер Pod (Готовность / Рестарты) | Лоадер Pod (Готовность / Рестарты) |
| :--- | :--- | :--- | :--- |
| `1xbet` | 🔴 | `2/2` (рестартов: 6) | `2/2` (рестартов: 0) |
| `888starz` | 🔴 | `2/2` (рестартов: 7) | `2/2` (рестартов: 6) |
| `baltbet` | 🔴 | `2/2` (рестартов: 0) | `1/2` (рестартов: 0) |
| `bet365` | 🔴 | `2/2` (рестартов: 4) | `1/2` (рестартов: 6) |
| `betandyou` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 6) |
| `betboom` | 🔴 | `2/2` (рестартов: 6) | `2/2` (рестартов: 6) |
| `betcity` | 🔴 | `2/2` (рестартов: 7) | `1/2` (рестартов: 0) |
| `betcity-com` | 🔴 | `1/1` (рестартов: 6) | `1/2` (рестартов: 7) |
| `betm` | 🔴 | `2/2` (рестартов: 0) | `1/2` (рестартов: 0) |
| `bettery` | 🔴 | `2/2` (рестартов: 7) | `2/2` (рестартов: 6) |
| `dafabet` | 🔴 | `2/2` (рестартов: 7) | `1/2` (рестартов: 7) |
| `fansport` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 6) |
| `fon-bet-by` | 🔴 | `0/0` (рестартов: 0) **Pending** | `0/0` (рестартов: 0) **Pending** |
| `fon-bet-kz` | 🔴 | `0/0` (рестартов: 0) **Pending** | `0/0` (рестартов: 0) **Pending** |
| `fon-bet-ru` | 🔴 | `2/2` (рестартов: 7) | `1/2` (рестартов: 7) |
| `leon` | 🔴 | `2/2` (рестартов: 6) | `2/2` (рестартов: 6) |
| `ligastavok` | 🔴 | `2/2` (рестартов: 6) | `2/2` (рестартов: 6) |
| `linebet` | 🔴 | `2/2` (рестартов: 6) | `2/2` (рестартов: 6) |
| `marathonbet` | 🔴 | `2/2` (рестартов: 6) | `2/2` (рестартов: 6) |
| `marathonbet-by` | 🔴 | `2/2` (рестартов: 6) | `2/2` (рестартов: 6) |
| `marathonbet-com` | 🔴 | `2/2` (рестартов: 7) | `2/2` (рестартов: 6) |
| `megapari` | 🔴 | `2/2` (рестартов: 7) | `2/2` (рестартов: 6) |
| `melbet` | 🔴 | `2/2` (рестартов: 6) | `1/2` (рестартов: 7) |
| `olimpbet` | 🔴 | `0/0` (рестартов: 0) **Pending** | `0/0` (рестартов: 0) **Pending** |
| `olimpbet-kz` | 🔴 | `0/0` (рестартов: 0) **Pending** | `1/2` (рестартов: 0) |
| `pari` | 🔴 | `0/0` (рестартов: 0) **Pending** | `0/0` (рестартов: 0) **Pending** |
| `pinnacle` | 🔴 | `0/0` (рестартов: 0) **Pending** | `2/2` (рестартов: 6) |
| `sbobet` | 🔴 | `0/0` (рестартов: 0) **Pending** | `2/2` (рестартов: 7) |
| `sportbet` | 🔴 | `0/0` (рестартов: 0) **Pending** | `1/2` (рестартов: 6) |
| `tennisi` | 🔴 | `0/0` (рестартов: 0) **Pending** | `2/2` (рестартов: 6) |
| `winline` | 🔴 | `2/2` (рестартов: 0) | `2/2` (рестартов: 6) |
| `zenit` | 🔴 | `2/2` (рестартов: 6) | `1/2` (рестартов: 6) |

---

## 🔴 Детализация ошибок и проблемных БК

### 🗂️ Букмекер: `1xbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-1xbet-crawler-58d98dc65c-m4chn` (2/2, Running, рестартов: 6, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T19:59:54.932Z  WARN 1 --- [igaming-source-1xbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T19:59:54.933Z  WARN 1 --- [igaming-source-1xbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T19:59:55.079Z  WARN 1 --- [igaming-source-1xbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T19:59:55.132Z  WARN 1 --- [igaming-source-1xbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:01:13.474Z  INFO 1 --- [igaming-source-1xbet] [ loader-sched-2] .i.s.c.e.x.s.AbstractXbetFamilyApiClient : Fetch succeeded. Preview (first 100 chars): {"Id":0,"Success":true,"Error":"","ErrorCode":0,"Guid":"","Value":[{"R":300,"SC":{"FS":{"S1":2},"PS"
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: Connection to igaming-source-1xbet-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Caused by: java.net.ConnectException: Connection refused
2026-06-09T19:56:31.464Z  WARN 1 --- [igaming-source-1xbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:56:31.465Z ERROR 1 --- [igaming-source-1xbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-1xbet-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:56:31.477Z ERROR 1 --- [igaming-source-1xbet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-1xbet-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-1xbet-loader-857978f795-rmp8r` (2/2, Running, рестартов: 0, возраст: 7m)

---

### 🗂️ Букмекер: `888starz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-888starz-crawler-798fffcdbf-xtg4x` (2/2, Running, рестартов: 7, возраст: 30m)
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:51.277Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:51.277Z ERROR 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:51.296Z ERROR 1 --- [igaming-source-888starz] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:51.298Z  WARN 1 --- [igaming-source-888starz] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-888starz-loader-5cc664d97c-qwcln` (2/2, Running, рестартов: 6, возраст: 28m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:00:44.877Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:44.878Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:45.078Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:45.133Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:09.725Z  WARN 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:09.725Z ERROR 1 --- [igaming-source-888starz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : The connection attempt failed.
2026-06-09T19:57:09.737Z ERROR 1 --- [igaming-source-888starz] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [The connection attempt failed.] [n/a]
    ```

---

### 🗂️ Букмекер: `baltbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-baltbet-crawler-b969bf884-x97fk` (2/2, Running, рестартов: 0, возраст: 2m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-baltbet-loader-77466c6698-jwd2x` (1/2, Running, рестартов: 0, возраст: 7m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:04:58.780Z  WARN 1 --- [igaming-source-baltbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:58.783Z  WARN 1 --- [igaming-source-baltbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:59.015Z  WARN 1 --- [igaming-source-baltbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:59.096Z  WARN 1 --- [igaming-source-baltbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```

---

### 🗂️ Букмекер: `bet365`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-bet365-crawler-7f69d7984-lx6z2` (2/2, Running, рестартов: 4, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:00:01.061Z  WARN 1 --- [igaming-source-bet365] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:01.061Z  WARN 1 --- [igaming-source-bet365] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:01.177Z  WARN 1 --- [igaming-source-bet365] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:01.205Z  WARN 1 --- [igaming-source-bet365] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:02:38.580Z ERROR 1 --- [igaming-source-bet365] [ loader-sched-1] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://www.bet365.com: Blocked by Cloudflare/security system
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: Connection to igaming-source-bet365-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Caused by: java.net.ConnectException: Connection refused
2026-06-09T19:57:18.813Z  WARN 1 --- [igaming-source-bet365] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:18.813Z ERROR 1 --- [igaming-source-bet365] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-bet365-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:57:18.841Z ERROR 1 --- [igaming-source-bet365] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-bet365-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-bet365-loader-6b576d8db9-bn9lr` (1/2, Running, рестартов: 6, возраст: 30m)
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is starting up
2026-06-09T19:58:43.068Z  WARN 1 --- [igaming-source-bet365] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:58:43.069Z ERROR 1 --- [igaming-source-bet365] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is starting up
2026-06-09T19:58:43.076Z ERROR 1 --- [igaming-source-bet365] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is starting up] [n/a]
2026-06-09T19:58:43.076Z  WARN 1 --- [igaming-source-bet365] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is starting up] [n/a]
    ```
* **Pod:** `igaming-source-bet365-loader-75d6bc8797-hzpjb` (1/2, Running, рестартов: 6, возраст: 19m)
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:41.782Z  WARN 1 --- [igaming-source-bet365] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:41.782Z ERROR 1 --- [igaming-source-bet365] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:41.789Z ERROR 1 --- [igaming-source-bet365] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:41.790Z  WARN 1 --- [igaming-source-bet365] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```

---

### 🗂️ Букмекер: `betandyou`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betandyou-crawler-6fb88bd8f8-dfnmm` (2/2, Running, рестартов: 0, возраст: 7m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:04:41.104Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:41.104Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:41.363Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:41.488Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betandyou-loader-597bdc67dc-srvcn` (2/2, Running, рестартов: 6, возраст: 28m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:00:41.876Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:41.884Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:42.008Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:42.044Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:10.503Z  WARN 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:10.507Z ERROR 1 --- [igaming-source-betandyou] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : The connection attempt failed.
2026-06-09T19:57:10.513Z ERROR 1 --- [igaming-source-betandyou] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [The connection attempt failed.] [n/a]
    ```

---

### 🗂️ Букмекер: `betboom`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betboom-crawler-6df57fb6df-nx4lz` (2/2, Running, рестартов: 6, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:00:54.480Z  WARN 1 --- [igaming-source-betboom] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:54.480Z  WARN 1 --- [igaming-source-betboom] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:54.603Z  WARN 1 --- [igaming-source-betboom] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:54.635Z  WARN 1 --- [igaming-source-betboom] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:01:54.876Z ERROR 1 --- [igaming-source-betboom] [ loader-sched-3] p.d.i.s.b.service.BetboomBrowserClient   : Error fetching events from page https://betboom.ru/sport/prematch: Cannot find object to call __adopt__: page@93a007d92194aaedb524d26c3ed7d65a
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: Connection to igaming-source-betboom-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Caused by: java.net.ConnectException: Connection refused
2026-06-09T19:57:16.276Z  WARN 1 --- [igaming-source-betboom] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:16.277Z ERROR 1 --- [igaming-source-betboom] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-betboom-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:57:16.297Z ERROR 1 --- [igaming-source-betboom] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-betboom-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betboom-loader-647fb6f846-4jc8f` (2/2, Running, рестартов: 6, возраст: 28m)
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:26.372Z  WARN 1 --- [igaming-source-betboom] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:26.372Z ERROR 1 --- [igaming-source-betboom] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : The connection attempt failed.
2026-06-09T19:57:26.388Z ERROR 1 --- [igaming-source-betboom] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [The connection attempt failed.] [n/a]
    ```

---

### 🗂️ Букмекер: `betcity`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betcity-crawler-55644bf67c-s8p6f` (2/2, Running, рестартов: 7, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-6.2.1.jar!/:6.2.1]
2026-06-09T20:06:01.023Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23245368: java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null
java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null
2026-06-09T20:06:08.810Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23236466: java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null
2026-06-09T20:06:09.596Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-2] i.s.c.e.b.s.AbstractBetcityFamilyService : Error extracting factors for Betcity event 23256488: java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:58:15.149Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:58:15.150Z ERROR 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-betcity-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-betcity-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betcity-loader-6c68c59564-s9jvs` (1/2, Running, рестартов: 0, возраст: 4m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:06:08.670Z ERROR 1 --- [igaming-source-betcity] [     virtual-46] .c.e.AbstractBookmakerPerformanceTracker : UNEXPECTED ERROR in bookmaker betcity: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null
java.lang.NullPointerException: Cannot invoke "pro.datawiki.igaming.source.core.engine.betcity.dto.BetcityOutcome.getKf()" because "v" is null
    ```

---

### 🗂️ Букмекер: `betcity-com`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betcity-com-crawler-6d574669c8-pr867` (1/1, Running, рестартов: 6, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:04:01.544Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:01.544Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:01.758Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:01.829Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:05:37.944Z ERROR 1 --- [igaming-source-betcity] [ loader-sched-3] p.d.i.s.b.service.BetCityApiClient       : Error fetching line info
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Failed to fetch logs: Error from server (BadRequest): previous terminated container "igaming-source-betcity-com-crawler" in pod "igaming-source-betcity-com-crawler-6d574669c8-pr867" not found
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betcity-com-loader-6b4664cc66-w6bqd` (1/2, Running, рестартов: 7, возраст: 30m)
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:58:19.919Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:58:19.921Z ERROR 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-betcity-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-betcity-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```

---

### 🗂️ Букмекер: `betm`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-betm-crawler-5bc75456f7-mgmz2` (2/2, Running, рестартов: 0, возраст: 7m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-betm-loader-788bcfc94b-z5fnj` (1/2, Running, рестартов: 0, возраст: 8m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:05:16.658Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:16.658Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:16.834Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:16.894Z  WARN 1 --- [igaming-source-betcity] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```

---

### 🗂️ Букмекер: `bettery`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-bettery-crawler-6dd8bc8c49-t4r4w` (2/2, Running, рестартов: 7, возраст: 30m)
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:58:08.426Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:58:08.428Z ERROR 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-bettery-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-bettery-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-bettery-loader-586b68f556-7wlh4` (2/2, Running, рестартов: 6, возраст: 31m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:00:35.709Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:35.709Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:35.888Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:35.945Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:57:07.853Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:07.859Z ERROR 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-bettery-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-bettery-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```
* **Pod:** `igaming-source-bettery-loader-95484bd9c-49h97` (1/2, Running, рестартов: 7, возраст: 29m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:04:28.892Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:28.892Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:29.096Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:29.154Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:58:43.024Z  WARN 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:58:43.024Z ERROR 1 --- [igaming-source-bettery] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is starting up
org.hibernate.exception.GenericJDBCException: unable to obtain isolated JDBC connection [FATAL: the database system is starting up] [n/a]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:63) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```

---

### 🗂️ Букмекер: `dafabet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-dafabet-crawler-86b6b4488b-d79kx` (2/2, Running, рестартов: 7, возраст: 31m)
  * **Ошибки в текущих логах:**
    ```text
	partitioner.availability.timeout.ms = 0
	request.timeout.ms = 30000
	sasl.login.connect.timeout.ms = null
	sasl.login.read.timeout.ms = null
	socket.connection.setup.timeout.max.ms = 30000
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: database "igaming_dafabet" does not exist
	at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2733) ~[postgresql-42.7.4.jar!/:42.7.4]
2026-06-09T19:54:42.122Z  WARN 1 --- [igaming-source-dafabet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 3D000
2026-06-09T19:54:42.122Z ERROR 1 --- [igaming-source-dafabet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: database "igaming_dafabet" does not exist
2026-06-09T19:54:42.136Z ERROR 1 --- [igaming-source-dafabet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: database "igaming_dafabet" does not exist] [n/a]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-dafabet-loader-5d677df78d-jzbln` (1/2, Running, рестартов: 7, возраст: 29m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:02:22.473Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=100, NEW=100, PENDING=0, PROCESSED=0, FAILED=0
2026-06-09T20:03:20.105Z  INFO 1 --- [igaming-source-dafabet] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Dafabet DB Stats: Total=125, NEW=0, PENDING=0, PROCESSED=125, FAILED=0
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:56:00.214Z  WARN 1 --- [igaming-source-dafabet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:56:00.215Z ERROR 1 --- [igaming-source-dafabet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : The connection attempt failed.
2026-06-09T19:56:00.225Z ERROR 1 --- [igaming-source-dafabet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [The connection attempt failed.] [n/a]
    ```
* **Pod:** `igaming-source-dafabet-loader-66b5ff58c7-rxtwv` (1/2, Running, рестартов: 7, возраст: 31m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: Error from server (NotFound): pods "igaming-source-dafabet-loader-66b5ff58c7-rxtwv" not found
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Failed to fetch logs: Error from server (NotFound): pods "igaming-source-dafabet-loader-66b5ff58c7-rxtwv" not found
    ```

---

### 🗂️ Букмекер: `fansport`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fansport-crawler-6d7bdd4b99-6lzrx` (2/2, Running, рестартов: 0, возраст: 8m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fansport-loader-64957cdc8d-w8tsc` (2/2, Running, рестартов: 6, возраст: 29m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:00:12.167Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:12.180Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:12.325Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:12.373Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:00:28.512Z ERROR 1 --- [igaming-source-fansport] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://fan-sport.com: Remote host terminated the handshake
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:04.831Z  WARN 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:04.832Z ERROR 1 --- [igaming-source-fansport] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : The connection attempt failed.
2026-06-09T19:57:04.857Z ERROR 1 --- [igaming-source-fansport] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [The connection attempt failed.] [n/a]
    ```

---

### 🗂️ Букмекер: `fon-bet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-by-crawler-5df8cbcb9f-pkrvc` (0/0, Pending, рестартов: 0, возраст: 8m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-by-loader-56bf65d66c-wrqzb` (0/0, Pending, рестартов: 0, возраст: 8m)
* **Pod:** `igaming-source-fon-bet-by-loader-748bd9f988-xnx8r` (1/2, Running, рестартов: 7, возраст: 29m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:04:10.114Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:10.114Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:10.314Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:10.348Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:58:44.358Z  WARN 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:58:44.360Z ERROR 1 --- [igaming-source-fon-bet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is starting up
org.hibernate.exception.GenericJDBCException: unable to obtain isolated JDBC connection [FATAL: the database system is starting up] [n/a]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:63) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```

---

### 🗂️ Букмекер: `fon-bet-kz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-kz-crawler-7dc7fb7449-zj6sx` (0/0, Pending, рестартов: 0, возраст: 8m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-kz-loader-56c465c554-wpvb4` (0/0, Pending, рестартов: 0, возраст: 8m)
* **Pod:** `igaming-source-fon-bet-kz-loader-5985bdfff6-kgs2n` (1/2, Running, рестартов: 7, возраст: 29m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:03:55.430Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:03:55.430Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:03:55.592Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:03:55.630Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:58:26.303Z  WARN 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:58:26.305Z ERROR 1 --- [igaming-source-fon-bet-kz] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-fon-bet-kz-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-fon-bet-kz-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```

---

### 🗂️ Букмекер: `fon-bet-ru`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-fon-bet-ru-crawler-754d57d4c6-9ftnd` (2/2, Running, рестартов: 7, возраст: 20m)
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:59:45.709Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:45.710Z ERROR 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
org.hibernate.exception.GenericJDBCException: unable to obtain isolated JDBC connection [FATAL: the database system is not yet accepting connections
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:63) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-fon-bet-ru-loader-549d458d45-dmw6p` (1/2, Running, рестартов: 7, возраст: 29m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:04:38.228Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:38.228Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:38.446Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:38.506Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:58:58.050Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:58:58.050Z ERROR 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
org.hibernate.exception.GenericJDBCException: unable to obtain isolated JDBC connection [FATAL: the database system is not yet accepting connections
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:63) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```
* **Pod:** `igaming-source-fon-bet-ru-loader-6f5fc57c6-c9jqn` (2/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:00:44.947Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:44.952Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:45.116Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:45.156Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:57:03.212Z  WARN 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:03.213Z ERROR 1 --- [igaming-source-fon-bet-ru] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-fon-bet-ru-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-fon-bet-ru-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```

---

### 🗂️ Букмекер: `leon`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-leon-crawler-99b7d5d89-jznld` (2/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:04:47.502Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:47.502Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:47.682Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:47.738Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:31.796Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:31.797Z ERROR 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:31.814Z ERROR 1 --- [igaming-source-leon] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:31.823Z  WARN 1 --- [igaming-source-leon] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-leon-loader-56c5c8d96d-zj68h` (2/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:01:05.844Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:05.844Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:01:05.976Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:06.014Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: Connection to igaming-source-leon-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Caused by: java.net.ConnectException: Connection refused
2026-06-09T19:57:16.375Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:16.375Z ERROR 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-leon-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:57:16.409Z ERROR 1 --- [igaming-source-leon] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-leon-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```
* **Pod:** `igaming-source-leon-loader-fdb8d7b49-snlq7` (1/2, Running, рестартов: 7, возраст: 29m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:06:48.064Z  INFO 1 --- [igaming-source-leon] [ loader-sched-3] p.d.i.s.c.s.AbstractApiErrorTracker      : Leon DB Stats: Total=104, NEW=104, PENDING=1, PROCESSED=0, FAILED=0
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:31.025Z  WARN 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:31.025Z ERROR 1 --- [igaming-source-leon] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:31.031Z ERROR 1 --- [igaming-source-leon] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:31.032Z  WARN 1 --- [igaming-source-leon] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```

---

### 🗂️ Букмекер: `ligastavok`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-ligastavok-crawler-df7487976-6pkr9` (2/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:01:39.951Z DEBUG 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
2026-06-09T20:02:29.475Z  INFO 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokDiagnosticService  : Window properties: Filtered: ["oncontentvisibilityautostatechange","onformdata","onloadeddata","onloadedmetadata","onpopstate","__getFile","__LS_RENDER_MODE__","__featureHubOverride","dataLayer","__appStatus","__config","__localization","__redirectRules","__state"] | All keys: window, self, document, name, location, customElements, history, navigation, locationbar, menubar, personalbar, scrollbars, statusbar, toolbar, status, closed, frames, length, top, opener, parent, frameElement, navigator, origin, external, screen, innerWidth, innerHeight, scrollX, pageXOffset, scrollY, pageYOffset, visualViewport, screenX, screenY, outerWidth, outerHeight, devicePixelRatio, event, clientInformation, screenLeft, screenTop, styleMedia, onsearch, onappinstalled, onbeforeinstallprompt, onabort, onbeforeinput, onbeforematch, onbeforetoggle, onblur, oncancel, oncanplay, oncanplaythrough, onchange, onclick, onclose, oncommand, oncontentvisibilityautostatechange, oncontextlost, oncontextmenu, oncontextrestored, oncuechange, ondblclick, ondrag, ondragend, ondragenter, ondragleave, ondragover, ondragstart, ondrop, ondurationchange, onemptied, onended, onerror, onfocus, onformdata, oninput, oninvalid, onkeydown, onkeypress, onkeyup, onload, onloadeddata, onloadedmetadata, onloadstart, onmousedown, onmouseenter, onmouseleave, onmousemove, onmouseout, onmouseover, onmouseup, onmousewheel, onpause, onplay, onplaying, onprogress, onratechange, onreset, onresize, onscroll, onscrollend, onsecuritypolicyviolation, onseeked, onseeking, onselect, onslotchange, onstalled, onsubmit, onsuspend, ontimeupdate, ontoggle, onvolumechange, onwaiting, onwebkitanimationend, onwebkitanimationiteration, onwebkitanimationstart, onwebkittransitionend, onwheel, onauxclick, ongotpointercapture, onlostpointercapture, onpointerdown, onpointermove, onpointerup, onpointercancel, onpointerover, onpointerout, onpointerenter, onpointerleave, onselectstart, onselectionchange, onanimationcancel, onanimationend, onanimationiteration, onanimationstart, ontransitionrun, ontransitionstart, ontransitionend, ontransitioncancel, onbeforexrselect, onafterprint, onbeforeprint, onbeforeunload, onhashchange, onlanguagechange, onmessage, onmessageerror, onoffline, ononline, onpagehide, onpageshow, onpopstate, onrejectionhandled, onstorage, onunhandledrejection, onunload, isSecureContext, crossOriginIsolated, scheduler, performance, trustedTypes, crypto, indexedDB, localStorage, sessionStorage, alert, atob, blur, btoa, cancelAnimationFrame, cancelIdleCallback, captureEvents, clearInterval, clearTimeout, close, confirm, createImageBitmap, fetch, find, focus, getComputedStyle, getSelection, matchMedia, moveBy, moveTo, open, postMessage, print, prompt, queueMicrotask, releaseEvents, reportError, requestAnimationFrame, requestIdleCallback, resizeBy, resizeTo, scroll, scrollBy, scrollTo, setInterval, setTimeout, stop, structuredClone, webkitCancelAnimationFrame, webkitRequestAnimationFrame, chrome, crashReport, cookieStore, ondevicemotion, ondeviceorientation, ondeviceorientationabsolute, onpointerrawupdate, caches, documentPictureInPicture, sharedStorage, fetchLater, getScreenDetails, queryLocalFonts, showDirectoryPicker, showOpenFilePicker, showSaveFilePicker, originAgentCluster, viewport, onpageswap, onpagereveal, credentialless, fence, launchQueue, speechSynthesis, onscrollsnapchange, onscrollsnapchanging, ongamepadconnected, ongamepaddisconnected, webkitRequestFileSystem, webkitResolveLocalFileSystemURL, __getFile, __LS_RENDER_MODE__, __featureHubOverride, $R, $_TSR, addScripts, monitorAppLoading, dataLayer, __appStatus, __config, addScriptsWithHandlers, IMask, __localization, __redirectRules, __state
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:57:10.562Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:10.562Z ERROR 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-ligastavok-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-ligastavok-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-ligastavok-loader-7df96dc4b6-ftw6n` (2/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:00:39.647Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:39.647Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:39.772Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:39.834Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:01:38.095Z DEBUG 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:57:08.349Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:08.388Z ERROR 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-ligastavok-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-ligastavok-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```
* **Pod:** `igaming-source-ligastavok-loader-85d6c74db-8l687` (1/2, Running, рестартов: 7, возраст: 29m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:05:05.067Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:05.067Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:05.241Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:05.295Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:05:54.040Z DEBUG 1 --- [igaming-source-ligastavok] [           main] p.d.i.s.l.s.LigastavokPageNavigator      : Browser Console [error]: Failed to load resource: the server responded with a status of 401 (Unauthorized)
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:59:21.821Z  WARN 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:21.821Z ERROR 1 --- [igaming-source-ligastavok] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
org.hibernate.exception.GenericJDBCException: unable to obtain isolated JDBC connection [FATAL: the database system is not yet accepting connections
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:63) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```

---

### 🗂️ Букмекер: `linebet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-linebet-crawler-58754d5d7-m5sx5` (2/2, Running, рестартов: 6, возраст: 33m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:05:05.786Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:05.786Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:05.985Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:06.069Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:44.117Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:44.118Z ERROR 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:44.129Z ERROR 1 --- [igaming-source-linebet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:44.133Z  WARN 1 --- [igaming-source-linebet] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-linebet-loader-688bd9699b-5gwp6` (2/2, Running, рестартов: 6, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:00:11.559Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:00:11.559Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:00:11.704Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:00:11.761Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:04.853Z  WARN 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:04.853Z ERROR 1 --- [igaming-source-linebet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : The connection attempt failed.
2026-06-09T19:57:04.866Z ERROR 1 --- [igaming-source-linebet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [The connection attempt failed.] [n/a]
    ```

---

### 🗂️ Букмекер: `marathonbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-crawler-76d777577f-rz4lj` (2/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:04:46.621Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:46.621Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:46.807Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:46.859Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:26.659Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:26.659Z ERROR 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:26.705Z ERROR 1 --- [igaming-source-marathonbet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:26.721Z  WARN 1 --- [igaming-source-marathonbet] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-loader-b9f576588-c5csv` (2/2, Running, рестартов: 6, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:01:21.592Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:21.592Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:01:22.174Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:22.321Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:02:07.226Z ERROR 1 --- [igaming-source-marathonbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://www.marathonbet.ru: No route to host
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:21.689Z  WARN 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:21.690Z ERROR 1 --- [igaming-source-marathonbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-marathonbet-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:57:21.735Z ERROR 1 --- [igaming-source-marathonbet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-marathonbet-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```

---

### 🗂️ Букмекер: `marathonbet-by`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-by-crawler-6f696db68d-rjpxs` (2/2, Running, рестартов: 6, возраст: 31m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:05:14.437Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:14.442Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:14.637Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:14.691Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:45.342Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:45.342Z ERROR 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:45.350Z ERROR 1 --- [igaming-source-marathonbet-by] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:45.351Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-by-loader-6975bc944c-dzdxq` (2/2, Running, рестартов: 6, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:01:22.312Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:22.313Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:01:22.947Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:22.970Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:29.484Z  WARN 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:29.485Z ERROR 1 --- [igaming-source-marathonbet-by] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-marathonbet-by-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:57:29.494Z ERROR 1 --- [igaming-source-marathonbet-by] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-marathonbet-by-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```

---

### 🗂️ Букмекер: `marathonbet-com`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-marathonbet-com-crawler-548458c88-vqhjv` (2/2, Running, рестартов: 7, возраст: 31m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:05:19.107Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:19.107Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:19.344Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:19.426Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: Connection to igaming-source-marathonbet-com-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Caused by: java.net.ConnectException: Connection refused
2026-06-09T19:58:34.307Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:58:34.307Z ERROR 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-marathonbet-com-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:58:34.314Z ERROR 1 --- [igaming-source-marathonbet-com] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-marathonbet-com-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-marathonbet-com-loader-67cb9ddd88-tgzmf` (2/2, Running, рестартов: 6, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:01:40.204Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:40.205Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:01:40.349Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:40.459Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:26.931Z  WARN 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:26.931Z ERROR 1 --- [igaming-source-marathonbet-com] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : The connection attempt failed.
2026-06-09T19:57:26.946Z ERROR 1 --- [igaming-source-marathonbet-com] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [The connection attempt failed.] [n/a]
    ```

---

### 🗂️ Букмекер: `megapari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-megapari-crawler-5bf45b49b8-xmwf7` (2/2, Running, рестартов: 7, возраст: 31m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:05:15.174Z  WARN 1 --- [igaming-source-megapari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:15.174Z  WARN 1 --- [igaming-source-megapari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:15.446Z  WARN 1 --- [igaming-source-megapari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:15.547Z  WARN 1 --- [igaming-source-megapari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:07:12.757Z  WARN 1 --- [igaming-source-megapari] [ loader-sched-2] p.d.i.s.core.browser.BrowserExecutor     : Page fetch failed for https://megapari.com/service-api/LiveFeed/Get1x2_VZip?sports=1&count=1000&lng=en&mode=4&country=207&partner=192&virtualSports=true, falling back to direct navigation: Error {
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is starting up
2026-06-09T19:58:50.711Z  WARN 1 --- [igaming-source-megapari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:58:50.711Z ERROR 1 --- [igaming-source-megapari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:58:50.718Z ERROR 1 --- [igaming-source-megapari] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:58:50.719Z  WARN 1 --- [igaming-source-megapari] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-megapari-loader-6c47c8c7d5-v2n6t` (2/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: Error from server (NotFound): pods "igaming-source-megapari-loader-6c47c8c7d5-v2n6t" not found
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Failed to fetch logs: Error from server (NotFound): pods "igaming-source-megapari-loader-6c47c8c7d5-v2n6t" not found
    ```
* **Pod:** `igaming-source-megapari-loader-c46fbffd9-8jkzq` (1/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:04:40.139Z ERROR 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://megapari.com: Remote host terminated the handshake
2026-06-09T20:04:40.140Z  WARN 1 --- [igaming-source-megapari] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : ❌ [Attempt 1/3] Connectivity check failed for 'megapari.com': javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: Connection to igaming-source-megapari-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Caused by: java.net.ConnectException: Connection refused
2026-06-09T19:58:55.163Z  WARN 1 --- [igaming-source-megapari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:58:55.174Z ERROR 1 --- [igaming-source-megapari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:58:55.210Z ERROR 1 --- [igaming-source-megapari] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```

---

### 🗂️ Букмекер: `melbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-melbet-crawler-8547f65849-pgrvw` (2/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:05:15.981Z  WARN 1 --- [igaming-source-melbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:15.982Z  WARN 1 --- [igaming-source-melbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:16.173Z  WARN 1 --- [igaming-source-melbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:16.239Z  WARN 1 --- [igaming-source-melbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:45.577Z  WARN 1 --- [igaming-source-melbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:45.577Z ERROR 1 --- [igaming-source-melbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:45.583Z ERROR 1 --- [igaming-source-melbet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:45.584Z  WARN 1 --- [igaming-source-melbet] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-melbet-loader-7784759f76-ngkm6` (1/2, Running, рестартов: 7, возраст: 32m)
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:14.426Z  WARN 1 --- [igaming-source-melbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:14.426Z ERROR 1 --- [igaming-source-melbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:14.436Z ERROR 1 --- [igaming-source-melbet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:14.439Z  WARN 1 --- [igaming-source-melbet] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```
* **Pod:** `igaming-source-melbet-loader-d89dd59bd-gvmnt` (0/0, Pending, рестартов: 0, возраст: 9m)

---

### 🗂️ Букмекер: `olimpbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-olimpbet-crawler-8944c87-24mzv` (0/0, Pending, рестартов: 0, возраст: 9m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-olimpbet-loader-7b89896d6d-pdc49` (0/0, Pending, рестартов: 0, возраст: 9m)

---

### 🗂️ Букмекер: `olimpbet-kz`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-olimpbet-kz-crawler-55b5bfc69d-2ml9t` (0/0, Pending, рестартов: 0, возраст: 9m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-olimpbet-kz-loader-9dd856fc-4kppm` (1/2, Running, рестартов: 0, возраст: 6m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:06:03.788Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:06:03.788Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:06:03.963Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:06:04.049Z  WARN 1 --- [igaming-source-olimpbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```

---

### 🗂️ Букмекер: `pari`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pari-crawler-c47b6bcbf-bslz7` (0/0, Pending, рестартов: 0, возраст: 9m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pari-loader-656c4dd8cf-dk6p5` (0/0, Pending, рестартов: 0, возраст: 9m)
* **Pod:** `igaming-source-pari-loader-6bd54c4c5d-vcc9z` (1/2, Running, рестартов: 7, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:05:13.475Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:13.475Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:13.751Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:13.848Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
2026-06-09T19:58:30.072Z  WARN 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:58:30.079Z ERROR 1 --- [igaming-source-pari] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-pari-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
org.hibernate.exception.JDBCConnectionException: unable to obtain isolated JDBC connection [Connection to igaming-source-pari-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
	at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:100) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58) ~[hibernate-core-6.6.4.Final.jar!/:6.6.4.Final]
    ```

---

### 🗂️ Букмекер: `pinnacle`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-pinnacle-crawler-5c4cf4f658-kcds2` (0/0, Pending, рестартов: 0, возраст: 9m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-pinnacle-loader-845fc5557b-pzhtx` (2/2, Running, рестартов: 6, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:01:25.460Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:25.460Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:01:26.035Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:26.154Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:04:21.729Z ERROR 1 --- [igaming-source-pinnacle] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://pinnacle.com: Read timed out
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:25.908Z  WARN 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:25.909Z ERROR 1 --- [igaming-source-pinnacle] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-pinnacle-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:57:25.933Z ERROR 1 --- [igaming-source-pinnacle] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-pinnacle-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```

---

### 🗂️ Букмекер: `sbobet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sbobet-crawler-59dcf6666b-9mg5r` (0/0, Pending, рестартов: 0, возраст: 9m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sbobet-loader-6d9d6455c9-k4gx5` (2/2, Running, рестартов: 7, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: Error from server (NotFound): pods "igaming-source-sbobet-loader-6d9d6455c9-k4gx5" not found
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Failed to fetch logs: Error from server (NotFound): pods "igaming-source-sbobet-loader-6d9d6455c9-k4gx5" not found
    ```
* **Pod:** `igaming-source-sbobet-loader-79b69b94c8-r6sd7` (1/2, Running, рестартов: 7, возраст: 30m)
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:56:15.760Z  WARN 1 --- [igaming-source-sbobet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:56:15.761Z ERROR 1 --- [igaming-source-sbobet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : The connection attempt failed.
2026-06-09T19:56:15.767Z ERROR 1 --- [igaming-source-sbobet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [The connection attempt failed.] [n/a]
    ```

---

### 🗂️ Букмекер: `sportbet`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-sportbet-crawler-645b9b55d4-qgwxj` (0/0, Pending, рестартов: 0, возраст: 9m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-sportbet-loader-f4d8b7b44-jqnrh` (1/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:04:05.087Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:04:05.088Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:04:05.300Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:04:05.354Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:04:38.812Z ERROR 1 --- [igaming-source-sportbet] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://sportbet.ru: Remote host terminated the handshake
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:03.280Z  WARN 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:03.280Z ERROR 1 --- [igaming-source-sportbet] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:03.312Z ERROR 1 --- [igaming-source-sportbet] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:03.323Z  WARN 1 --- [igaming-source-sportbet] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```

---

### 🗂️ Букмекер: `tennisi`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-tennisi-crawler-cf475fbd4-8md55` (0/0, Pending, рестартов: 0, возраст: 9m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-tennisi-loader-5b54d88954-xwc8l` (2/2, Running, рестартов: 6, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:05:27.144Z  WARN 1 --- [igaming-source-tennisi] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:27.941Z  WARN 1 --- [igaming-source-tennisi] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:27.941Z ERROR 1 --- [igaming-source-tennisi] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-tennisi-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:57:27.949Z ERROR 1 --- [igaming-source-tennisi] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-tennisi-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```

---

### 🗂️ Букмекер: `winline`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-winline-crawler-5b8fd4fdd8-sjksd` (2/2, Running, рестартов: 0, возраст: 4m)
  * **Ошибки в текущих логах:**
    ```text
Failed to fetch logs: Error from server (NotFound): pods "igaming-source-winline-crawler-5b8fd4fdd8-sjksd" not found
    ```
* **Pod:** `igaming-source-winline-crawler-6c5667b6b4-qkfgv` (0/0, Pending, рестартов: 0, возраст: 3m)
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-winline-loader-955ddcf9c-24k6z` (2/2, Running, рестартов: 6, возраст: 30m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:01:38.880Z  WARN 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:01:38.880Z  WARN 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:01:39.026Z  WARN 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:01:39.067Z  WARN 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:07:41.551Z  WARN 1 --- [igaming-source-winline] [ loader-sched-2] p.d.i.s.core.service.VpnManagerService   : Failed to report proxy success: I/O error on POST request for "http://service-proxy-backend.service-proxy.svc.cluster.local/api/v1/proxy/report-success": Connection reset
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: The connection attempt failed.
Caused by: java.net.SocketTimeoutException: Connect timed out
2026-06-09T19:57:28.505Z  WARN 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:28.505Z ERROR 1 --- [igaming-source-winline] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-winline-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:57:28.517Z ERROR 1 --- [igaming-source-winline] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-winline-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```

---

### 🗂️ Букмекер: `zenit`

#### 🕷️ Краулер (Crawler):
* **Pod:** `igaming-source-zenit-crawler-5f75984c46-rrxcr` (2/2, Running, рестартов: 6, возраст: 21m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:06:11.992Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-1] p.d.i.s.c.s.AbstractApiErrorTracker      : Zenit API fetch stats: failed to load 1 out of 6 pages in the last cycle. Reasons: {Empty response from browser=1}
2026-06-09T20:07:02.174Z ERROR 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.core.browser.BrowserExecutor     : Request error for https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50: Error {
  name='Error
  stack='Error: net::ERR_HTTP_RESPONSE_CODE_FAILURE at https://zenit.win/ajax/line/printer/react?all=1&onlyview=0&timeline=0&tournaments_mode=0&lang_id=1&timezone=3&length=50
2026-06-09T20:07:02.196Z  WARN 1 --- [igaming-source-zenit] [ loader-sched-4] p.d.i.s.zenit.service.ZenitApiClient     : Failed to fetch data, empty response
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: Connection to igaming-source-zenit-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
Caused by: java.net.ConnectException: Connection refused
2026-06-09T19:57:17.247Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 08001
2026-06-09T19:57:17.247Z ERROR 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : Connection to igaming-source-zenit-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
2026-06-09T19:57:17.259Z ERROR 1 --- [igaming-source-zenit] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to igaming-source-zenit-db:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.] [n/a]
    ```
#### 📥 Лоадер (Loader):
* **Pod:** `igaming-source-zenit-loader-6d6dcd6594-z74vp` (1/2, Running, рестартов: 6, возраст: 32m)
  * **Ошибки в текущих логах:**
    ```text
2026-06-09T20:05:03.470Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Warning Code: 0, SQLState: 00000
2026-06-09T20:05:03.471Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk8t14xdumqr8918elshfmxbn2l" of relation "league_cache" does not exist, skipping
2026-06-09T20:05:03.662Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "uk6k7py0ytg93arkeip5y2rb6t8" of relation "match_cache" does not exist, skipping
2026-06-09T20:05:03.742Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : constraint "ukime6r48dbr3o67dwqn897fjqh" of relation "sport_cache" does not exist, skipping
2026-06-09T20:05:46.708Z ERROR 1 --- [igaming-source-zenit] [ctivity-checker] p.d.i.s.core.service.VpnManagerService   : Connectivity check failed to https://zenit.win: Read timed out
    ```
  * **Ошибки до последнего перезапуска:**
    ```text
Caused by: org.postgresql.util.PSQLException: FATAL: the database system is not yet accepting connections
2026-06-09T19:59:43.170Z  WARN 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 57P03
2026-06-09T19:59:43.170Z ERROR 1 --- [igaming-source-zenit] [           main] o.h.engine.jdbc.spi.SqlExceptionHelper   : FATAL: the database system is not yet accepting connections
2026-06-09T19:59:43.186Z ERROR 1 --- [igaming-source-zenit] [           main] j.LocalContainerEntityManagerFactoryBean : Failed to initialize JPA EntityManagerFactory: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
2026-06-09T19:59:43.190Z  WARN 1 --- [igaming-source-zenit] [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' defined in class path resource [org/springframework/boot/autoconfigure/orm/jpa/HibernateJpaConfiguration.class]: [PersistenceUnit: default] Unable to build Hibernate SessionFactory; nested exception is org.hibernate.exception.GenericJDBCException: Unable to open JDBC Connection for DDL execution [FATAL: the database system is not yet accepting connections
    ```

---

## 🟢 Исправные БК (Без ошибок)

Нет полностью исправных БК.
## ⚪ Не развернутые букмекеры (Нет подов)

Все букмекеры развернуты в системе.
