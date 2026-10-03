# Crawler Engine Specification Delta (plane-71601966)

## Purpose
Specifies BetB2B mirror synchronization, direct network routing, and health verification for Melbet International (`melbet-com`).

## ADDED Requirements

### Requirement: BetB2B Mirror Synchronization and Direct Routing
The crawler engine for BetB2B family clones, specifically Melbet International (`melbet-com`), must target verified active mirrors and leverage direct networking or cluster DNS without depending on deprecated proxy pool endpoints.

#### Scenario: Routing BetB2B line feed via accessible mirror
- **WHEN** `igaming-source-melbet-com-crawler` fetches prematch or live line feeds
- **THEN** it connects to `https://1x-bet.com/service-api/LineFeed/Get1x2_Zip` using partner identifier `110` without routing through unavailable proxy services.

#### Scenario: Multi-sport line accumulation
- **WHEN** the BetB2B crawler iterates across sports categories for `melbet-com`
- **THEN** it accumulates active events in `match_cache` reaching at least 500 active events in accordance with the DoD line coverage threshold.

#### Scenario: Actuator health probe verification
- **WHEN** the crawler and loader deployments initialize in K8s
- **THEN** K8s kubelet verifies `/actuator/health/liveness` and `/actuator/health/readiness` returning HTTP 200 before routing traffic.
