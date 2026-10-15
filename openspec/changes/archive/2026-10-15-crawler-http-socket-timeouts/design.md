# Design: Crawler HTTP Socket Timeouts Protection

## Architecture Decisions

### 1. Explicit `RequestConfig` on Apache HttpClient 5
- In Spring Boot 3.4 / Apache HttpClient 5, timeouts are configured through `RequestConfig`:
  ```java
  RequestConfig requestConfig = RequestConfig.custom()
          .setConnectTimeout(Timeout.ofSeconds(10))
          .setResponseTimeout(Timeout.ofSeconds(20))
          .setConnectionRequestTimeout(Timeout.ofSeconds(5))
          .build();
  ```
- This applies to both the synchronous execution and underlying connection sockets.

### 2. Differentiated Profiles: External vs Internal
- **`restTemplate()` (External Bookmakers)**:
  - Connect Timeout: 10 seconds.
  - Response Timeout (Socket Read Timeout): 20 seconds.
  - Connection Request Timeout: 5 seconds.
  - Max Total Connections: 1000, Max Per Route: 500.
  - Keeps proxy and system properties enabled (`useSystemProperties()`).
- **`internalRestTemplate()` (Cluster-Internal Services)**:
  - Connect Timeout: 5 seconds.
  - Response Timeout: 10 seconds.
  - Connection Request Timeout: 5 seconds.
  - Max Total Connections: 100, Max Per Route: 20.
  - Never uses proxy (`useSystemProperties()` is omitted).

### 3. Factory Level Fallback
- On `HttpComponentsClientHttpRequestFactory`, also call:
  ```java
  requestFactory.setConnectTimeout(10000);
  requestFactory.setConnectionRequestTimeout(5000);
  ```
- This ensures dual-layer enforcement at both HttpClient 5 and Spring ClientHttpRequestFactory abstraction layers.
