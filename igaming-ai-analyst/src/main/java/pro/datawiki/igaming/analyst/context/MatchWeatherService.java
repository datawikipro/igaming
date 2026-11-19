package pro.datawiki.igaming.analyst.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Fetches match-hour stadium weather forecasts from free, keyless Open-Meteo API
 * using verified GPS coordinates from mdm_venue / mdm_city.
 * Cached in Redis for 3 hours by rounded coordinate & UTC hour to guarantee zero latency spikes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchWeatherService {

    private static final String WEATHER_CACHE_PREFIX = "weather:openmeteo:";
    private static final Duration CACHE_TTL = Duration.ofHours(3);
    private static final DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${analyst.weather.proxy.host:}")
    private String proxyHost;

    @Value("${analyst.weather.proxy.port:3128}")
    private int proxyPort;

    public record WeatherForecast(
            Double tempC,
            Double windKmh,
            Double precipMm,
            Integer weatherCode,
            String riskLevel,
            String summary
    ) {
        public static WeatherForecast indoor() {
            return new WeatherForecast(null, null, null, null, "INDOOR", "🏟️ Крытая арена — погодный риск отсутствует.");
        }

        public static WeatherForecast unavailable() {
            return new WeatherForecast(null, null, null, null, "UNKNOWN", null);
        }
    }

    public WeatherForecast fetchMatchWeather(double lat, double lon, boolean isIndoor, Long kickoffEpochMs, String sportSlug) {
        if (isIndoor || isIndoorSport(sportSlug)) {
            return WeatherForecast.indoor();
        }
        if (Math.abs(lat) < 0.01 || Math.abs(lon) < 0.01 || kickoffEpochMs == null || kickoffEpochMs <= 0) {
            return WeatherForecast.unavailable();
        }

        // Only forecast within [-6h .. +7 days] of now (Open-Meteo forecast window)
        long nowMs = System.currentTimeMillis();
        long deltaMs = kickoffEpochMs - nowMs;
        if (deltaMs < -6L * 3600_000L || deltaMs > 7L * 24L * 3600_000L) {
            return WeatherForecast.unavailable();
        }

        Instant ko = Instant.ofEpochMilli(kickoffEpochMs);
        String targetHour = HOUR_FMT.format(ko);
        String targetDate = DATE_FMT.format(ko);
        String coordKey = String.format(Locale.US, "%.2f:%.2f:%s", lat, lon, targetHour);
        String redisKey = WEATHER_CACHE_PREFIX + coordKey;

        // 1. Check Redis cache
        try {
            if (redisTemplate != null) {
                String cached = redisTemplate.opsForValue().get(redisKey);
                if (cached != null && !cached.isBlank()) {
                    return objectMapper.readValue(cached, WeatherForecast.class);
                }
            }
        } catch (Exception e) {
            log.debug("[WEATHER] Cache read failed for {}: {}", redisKey, e.getMessage());
        }

        // 2. Call Open-Meteo Forecast API
        try {
            String url = String.format(Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&hourly=temperature_2m,precipitation,wind_speed_10m,weather_code&start_date=%s&end_date=%s&timezone=UTC",
                    lat, lon, targetDate, targetDate);

            HttpClient.Builder cb = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(4))
                    .followRedirects(HttpClient.Redirect.NORMAL);
            if (proxyHost != null && !proxyHost.isBlank()) {
                cb.proxy(ProxySelector.of(new InetSocketAddress(proxyHost.trim(), proxyPort)));
            }

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> resp = cb.build().send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200 || resp.body() == null) {
                log.warn("[WEATHER] Open-Meteo non-200 status={} for ({}, {})", resp.statusCode(), lat, lon);
                return WeatherForecast.unavailable();
            }

            WeatherForecast parsed = parseOpenMeteoResponse(resp.body(), targetHour);
            if (parsed != null && !"UNKNOWN".equals(parsed.riskLevel()) && redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(parsed), CACHE_TTL);
                } catch (Exception ignored) {}
            }
            return parsed != null ? parsed : WeatherForecast.unavailable();
        } catch (Exception e) {
            log.warn("[WEATHER] Open-Meteo fetch failed for ({}, {}): {}", lat, lon, e.getMessage());
            return WeatherForecast.unavailable();
        }
    }

    WeatherForecast parseOpenMeteoResponse(String json, String targetHour) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode hourly = root.path("hourly");
        JsonNode times = hourly.path("time");
        if (!times.isArray() || times.isEmpty()) return WeatherForecast.unavailable();

        int idx = -1;
        for (int i = 0; i < times.size(); i++) {
            if (targetHour.equals(times.get(i).asText())) {
                idx = i;
                break;
            }
        }
        if (idx < 0) idx = Math.min(12, times.size() - 1);

        double temp = hourly.path("temperature_2m").path(idx).asDouble(15.0);
        double precip = hourly.path("precipitation").path(idx).asDouble(0.0);
        double wind = hourly.path("wind_speed_10m").path(idx).asDouble(0.0);
        int code = hourly.path("weather_code").path(idx).asInt(0);

        return classifyWeather(temp, wind, precip, code);
    }

    public static WeatherForecast classifyWeather(double tempC, double windKmh, double precipMm, int code) {
        String risk;
        String note;
        if (precipMm >= 2.5 || code >= 65) {
            risk = "HEAVY_RAIN";
            note = String.format(Locale.US, "🌧️ Ливень/непогода (%.1f°C, осадки %.1fмм/ч, ветер %.0f км/ч) — фактор снижения темпа и точности.", tempC, precipMm, windKmh);
        } else if (windKmh >= 30.0) {
            risk = "HIGH_WIND";
            note = String.format(Locale.US, "💨 Сильный ветер (%.0f км/ч, %.1f°C) — высокий разброс подач/дальних передач.", windKmh, tempC);
        } else if (tempC >= 33.0) {
            risk = "EXTREME_HEAT";
            note = String.format(Locale.US, "🔥 Экстремальная жара (%.1f°C) — риск просадки выносливости во 2-й половине.", tempC);
        } else if (tempC <= 0.0) {
            risk = "FREEZING";
            note = String.format(Locale.US, "❄️ Мороз (%.1f°C) — тяжёлое поле, фактор низового сценария.", tempC);
        } else if (precipMm >= 0.4) {
            risk = "RAIN";
            note = String.format(Locale.US, "🌦️ Дождь (%.1f°C, %.1fмм/ч, ветер %.0f км/ч).", tempC, precipMm, windKmh);
        } else {
            risk = "CLEAR";
            note = String.format(Locale.US, "☀️ Нормальная погода (%.1f°C, ветер %.0f км/ч).", tempC, windKmh);
        }
        return new WeatherForecast(
                Math.round(tempC * 10.0) / 10.0,
                Math.round(windKmh * 10.0) / 10.0,
                Math.round(precipMm * 100.0) / 100.0,
                code,
                risk,
                note
        );
    }

    private boolean isIndoorSport(String sportSlug) {
        if (sportSlug == null) return false;
        String s = sportSlug.toLowerCase(Locale.ROOT);
        return s.contains("basketball") || s.contains("hockey") || s.contains("table-tennis")
                || s.contains("volleyball") || s.contains("handball") || s.contains("cs2")
                || s.contains("dota") || s.contains("esport");
    }
}
