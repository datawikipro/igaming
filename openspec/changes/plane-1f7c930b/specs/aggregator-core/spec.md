## ADDED Requirements

### Requirement: Weather Conditions Enrichment for Outdoor Venues

The aggregator enrichment service MUST enrich open-air football and tennis matches with stadium coordinates and weather forecasts, evaluating severe conditions (rain, snowfall, wind > 20 km/h, extreme heat) that distort scoring expectations and shift totals distributions towards Under.

#### Scenario: Resolving stadium geocoordinates and roof status
- **WHEN** an upcoming match is selected for weather enrichment
- **THEN** `StadiumLocationService` resolves the venue name, geographic coordinates (latitude, longitude), and roof status (`OPEN`, `RETRACTABLE`, `INDOOR`).

#### Scenario: Querying weather forecast for match start time
- **WHEN** stadium coordinates and scheduled match start time are available
- **THEN** `WeatherApiClient` requests the forecast from OpenWeatherMap API (or Open-Meteo fallback) for the target epoch timestamp, obtaining temperature, wind speed, wind gusts, humidity, and precipitation.

#### Scenario: Evaluating wind > 20 km/h and adverse weather impact on match totals
- **WHEN** an open-air venue forecast indicates wind speed exceeding 20 km/h, rain, snow, or extreme heat (> 32°C)
- **THEN** `WeatherImpactCalculator` sets `underBias = true`, assigns the appropriate `riskFactor` (`HIGH_WIND`, `HEAVY_RAIN`, `SNOWFALL`, `EXTREME_HEAT`), calculates the negative `totalsAdjustment`, and generates a tactical summary.

#### Scenario: Neutralizing weather effects for closed or indoor roofs
- **WHEN** a match is hosted at a venue with a closed retractable roof or indoor dome
- **THEN** weather impact is neutralized (`underBias = false`, `overBias = false`, `totalsAdjustment = 0.0`).

#### Scenario: Publishing canonical weather summary to Kafka and persisting to database
- **WHEN** weather enrichment is completed
- **THEN** a canonical event is published to Kafka topic `sports.canonical.weather` with key `matchId` and serialized into `match_record.weather_json`.
