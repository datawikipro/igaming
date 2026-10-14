# Change Proposal: Auth & Gateway Unification (igaming-portal + auth-base)

## Why
Currently in `igaming-portal`:
1. **Redundant Authentication Parsing & Boilerplate**: Multiple controllers (`PortalBookmakerController`, `PortalAlertController`, `SocialFeedController`, `VirtualWalletController`) duplicate logic extracting bearer tokens from the `Authorization` header and inspecting the `access_token` cookie.
2. **Duplicated Auth Client Logic & Private Wrapper**: `PortalBookmakerController` had its own hardcoded HTTP exchange to `authUrl + "/auth/session"` using an internal static class `SessionResponseWrapper` instead of delegating to `AuthServiceClient`.
3. **Inefficient Serialization & Missing DTO Typing**: `AuthServiceClient` created a new Jackson `ObjectMapper` on every incoming token verification / session lookup call (`new ObjectMapper()`), converting raw untyped `Map<String, Object>` payloads instead of leveraging typed responses and Spring-managed serialization.
4. **Storm Protection & Session Caching**: The portal gateway receives continuous polling requests for surebet alerts, valuebets, and bookmakers from frontend clients. Every hit triggered un-cached HTTP network roundtrips to `igaming-auth-microservice`, putting unnecessary strain on the auth service and introducing latency into time-sensitive arbitrage delivery.

## What Changes
1. **Centralized Request Token & Identity Resolver**:
   - Enhance `AuthServiceClient` with unified helper methods:
     - `extractToken(HttpServletRequest request)`: extracts the JWT token seamlessly from either `Authorization: Bearer <token>` or `access_token` cookie.
     - `resolveUserId(HttpServletRequest request)`: extracts identity prioritizing trusted `X-Bot-User-Id` (for internal bots) followed by JWT token resolution.
     - `resolveUser(HttpServletRequest request)`: resolves user session (`PortalUserDto`) from request.
2. **Spring-Managed ObjectMapper & DTO Unification**:
   - Inject Spring's `ObjectMapper` into `AuthServiceClient` rather than instantiating a new instance on every invocation.
   - Introduce typed `SubscriptionCheckResponse` to avoid raw map parsing for `/subscriptions/check/{feature}`.
   - Refactor `PortalBookmakerController` to remove hardcoded `authUrl`, duplicated `validateToken()`, and redundant `SessionResponseWrapper`, delegating directly to `authServiceClient.getUserSession(token)`.
3. **High-Performance In-Memory Session Caching**:
   - Introduce Caffeine-backed short-lived caching (15-30s TTL) for `getUserSession(token)` and `hasPremiumSubscription(token)` in `AuthServiceClient`, dramatically lowering inter-service network traffic and eliminating latency spikes during peak polling.
4. **Refactor Controllers to Use Centralized Auth**:
   - Update `PortalBookmakerController`, `SocialFeedController`, `VirtualWalletController`, and `PortalAlertController` to eliminate duplicated cookie parsing loops and leverage `AuthServiceClient` directly.

## Impact
- **Specs Impact**: Modifies requirement in `portal-gateway` specification.
- **Microservices Impact**: `igaming-portal` code is streamlined, DRY, and significantly more resilient under high concurrency.
- **Backward Compatibility**: Fully backward-compatible REST API contracts for all consumers (Next.js web frontend, Chrome extension, Telegram bots).
