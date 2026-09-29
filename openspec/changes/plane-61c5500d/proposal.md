# Proposal: [smm-tg] Ревитализация и автоматизация Telegram-канала SmartBet.guru

## Context
Plane Task ID: `61c5500d-110e-4f3b-ab4e-34ea8b2c6d9a`
Target Service: `igaming-bot` (Telegram Bot & Social Media Automation)
Specification: `social-media-bot` (`openspec/specs/social-media-bot/spec.md`)

## Problem Statement & Architectural Diagnosis
1. **Broken Channel Arb Distribution (`ChannelPosterScheduler`)**:
   - `ChannelPosterScheduler` attempted to query `portalClient.getActiveChannels()` via an unconfigured URL targeting `http://igaming-admin-backend/api/v1/admin/channels/active`.
   - The service `igaming-admin-backend` does not exist in the cluster, leading to constant `ResourceAccessException` / `I/O error` failures every 60 seconds and preventing any surebets from being posted to the official channel (`@smartbet_guru`).
2. **Missing SMM Automation for Telegram (`SocialMediaContentScheduler`)**:
   - `SocialMediaContentScheduler` publishes daily scheduled content (Blog at 09:00, Surebets at 12:00/18:00/21:00, Dev Progress at 15:00) through `List<SocialMediaPoster>`.
   - Implementations existed only for Facebook, Instagram, and Threads; Telegram was completely absent. As a result, the Telegram channel received zero automated SMM content or responsible gambling disclaimers.
3. **Outdated DTO Deserialization Crash (`BookmakerRegion`)**:
   - The existing pod had an outdated dependency missing the `GR` (Greece) region in `BookmakerRegion`, resulting in JSON deserialization errors during aggregator catalog sync.
4. **Lack of Direct Database Entity for Channels**:
   - `igaming-bot` already connects directly to PostgreSQL (`portal-postgres:5432/igaming_portal`), but did not have the `TelegramChannel` JPA entity or repository, relying instead on a non-existent external REST endpoint.

## Proposed Changes
1. **Direct JPA Channel Persistence & Fallback Seeding**:
   - Add `TelegramChannel` entity and `TelegramChannelRepository` to `igaming-bot`.
   - Introduce `TelegramChannelService` that queries the database and auto-seeds the default channel (`@smartbet_guru`) if none exist, with configurable intervals and yield thresholds.
   - Refactor `ChannelPosterScheduler` to use `TelegramChannelService`.
2. **Implement `TelegramSocialMediaPosterService`**:
   - Implement `SocialMediaPoster` for Telegram to broadcast scheduled blog articles, dev progress updates, and surebet digests to the channel with the required responsible gambling disclaimer.
3. **Rebuild with Latest `igaming-dto`**:
   - Resolve `BookmakerRegion` deserialization errors.
4. **Update Configuration and Manifests**:
   - Configure non-blocking HikariCP properties (`initialization-fail-timeout=0`, `connection-timeout=5000`, `validation-timeout=3000`).
   - Add channel configuration properties (`app.telegram.channel.chat-id`, `app.telegram.channel.enabled`).
