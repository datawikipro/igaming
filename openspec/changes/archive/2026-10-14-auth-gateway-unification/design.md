# Design: Auth & Gateway Unification

## Architecture Decisions

### 1. Centralized Identity Extraction in `AuthServiceClient`
- Instead of manually parsing headers and iterating over `request.getCookies()` in every controller, `AuthServiceClient` exposes:
  ```java
  public static String extractToken(HttpServletRequest request)
  public Long resolveUserId(HttpServletRequest request)
  public PortalUserDto resolveUser(HttpServletRequest request)
  ```
- This encapsulates:
  - Checking `Authorization` header with `"Bearer "` prefix.
  - Iterating over `Cookie[]` for `access_token`.
  - Checking `X-Bot-User-Id` for bot requests.

### 2. Spring Dependency Injection of `ObjectMapper`
- Replace `new com.fasterxml.jackson.databind.ObjectMapper()` in `AuthServiceClient` with an injected `ObjectMapper` instance.
- This prevents repeated reflection cache rebuilds and GC overhead on high-frequency auth queries.

### 3. Caffeine Caching of Sessions and Subscription Checks
- The portal gateway already has Caffeine on classpath and `spec: expireAfterWrite=15s` configured.
- We utilize Spring Cache (`@Cacheable(value = "portal-user-sessions", unless = "#result == null")` and `@Cacheable(value = "portal-premium-subs", unless = "#result == false")`) or an internal Caffeine `Cache<String, Object>` in `AuthServiceClient` to cache token sessions and premium subscription status for 15-30 seconds.
- An in-memory cache guarantees that high-rate user polling (e.g. 5 requests/sec from a single browser tab) produces exactly 1 upstream HTTP request every 15-30 seconds instead of hundreds.

### 4. Controller Refactoring
- **`PortalBookmakerController`**:
  - Remove `private String authUrl;`
  - Remove `private PortalUserDto validateToken(String token)`
  - Remove inner static class `SessionResponseWrapper`
  - Inject `AuthServiceClient` and use `authServiceClient.getUserSession(token)`
- **`SocialFeedController`**:
  - Replace 20-line `resolveUserId(HttpServletRequest request)` with single call: `authServiceClient.resolveUserId(request)`
- **`VirtualWalletController`**:
  - Replace boilerplate cookie parsing with `authServiceClient.resolveUserId(request)`
- **`PortalAlertController`**:
  - Replace cookie parsing boilerplate with `AuthServiceClient.extractToken(request)`
