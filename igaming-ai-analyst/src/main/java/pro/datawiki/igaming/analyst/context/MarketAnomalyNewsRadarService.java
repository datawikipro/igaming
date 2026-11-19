package pro.datawiki.igaming.analyst.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Event-Driven News & Insight Radar.
 * <p>
 * Strictly triggered ONLY when an anomalous line movement is detected (e.g. |driftPct| >= 8%),
 * querying Google News RSS (keyless) for fresh injury/suspension/rotation headlines over the last 48h
 * so the Risk/Reward engine can distinguish real team-news insights from empty public hype.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketAnomalyNewsRadarService {

    private static final String NEWS_CACHE_PREFIX = "news:radar:";
    private static final Duration NEWS_CACHE_TTL = Duration.ofHours(6);

    private static final List<String> INSIGHT_KEYWORDS = List.of(
            "injur", "ruled out", "sidelined", "doubtful", "miss ", "misses ", "missing ",
            "suspend", "banned", "withdraw", "withdrew", "pulled out", "rested", "rotation",
            "illness", "sick", "virus", "surgery", "hamstring", "ankle", "knee", "concussion",
            "травм", "пропустит", "не сыграет", "дисквалиф", "снялся", "снялась", "ротац", "дубл"
    );

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${analyst.news.proxy.host:}")
    private String proxyHost;

    @Value("${analyst.news.proxy.port:3128}")
    private int proxyPort;

    public record NewsRadarResult(
            boolean triggered,
            String signalType,
            String headlineSummary,
            List<String> headlines
    ) {
        public static NewsRadarResult notTriggered() {
            return new NewsRadarResult(false, "NOT_TRIGGERED", null, List.of());
        }
    }

    public NewsRadarResult scanAnomalyNews(Long matchId, String team1, String team2, double driftPct) {
        if (matchId == null || Math.abs(driftPct) < 8.0) {
            return NewsRadarResult.notTriggered();
        }
        if ((team1 == null || team1.isBlank()) && (team2 == null || team2.isBlank())) {
            return NewsRadarResult.notTriggered();
        }

        String cacheKey = NEWS_CACHE_PREFIX + matchId;
        try {
            if (redisTemplate != null) {
                String cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null && !cached.isBlank()) {
                    return objectMapper.readValue(cached, NewsRadarResult.class);
                }
            }
        } catch (Exception e) {
            log.debug("[NEWS-RADAR] Cache read error for match #{}: {}", matchId, e.getMessage());
        }

        try {
            String query = buildSearchQuery(team1, team2);
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://news.google.com/rss/search?q=" + encoded + "&hl=en-US&gl=US&ceid=US:en";

            HttpClient.Builder cb = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(4))
                    .followRedirects(HttpClient.Redirect.NORMAL);
            if (proxyHost != null && !proxyHost.isBlank()) {
                cb.proxy(ProxySelector.of(new InetSocketAddress(proxyHost.trim(), proxyPort)));
            }

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(7))
                    .header("User-Agent", "Mozilla/5.0 (compatible; SmartBetNewsRadar/1.0)")
                    .GET()
                    .build();

            HttpResponse<String> resp = cb.build().send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200 || resp.body() == null || resp.body().isBlank()) {
                log.warn("[NEWS-RADAR] Google News RSS non-200 status={} for match #{}", resp.statusCode(), matchId);
                return new NewsRadarResult(true, "RADAR_UNAVAILABLE", null, List.of());
            }

            NewsRadarResult res = classifyRssHeadlines(resp.body());
            if (redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(res), NEWS_CACHE_TTL);
                } catch (Exception ignored) {}
            }
            return res;
        } catch (Exception e) {
            log.warn("[NEWS-RADAR] RSS query failed for match #{} ({} vs {}): {}", matchId, team1, team2, e.getMessage());
            return new NewsRadarResult(true, "RADAR_UNAVAILABLE", null, List.of());
        }
    }

    NewsRadarResult classifyRssHeadlines(String rssXml) {
        List<String> allTitles = new ArrayList<>();
        List<String> insightTitles = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(rssXml.getBytes(StandardCharsets.UTF_8)));

            NodeList items = doc.getElementsByTagName("item");
            int limit = Math.min(items.getLength(), 8);
            for (int i = 0; i < limit; i++) {
                Element el = (Element) items.item(i);
                NodeList titleNodes = el.getElementsByTagName("title");
                if (titleNodes.getLength() == 0) continue;
                String title = titleNodes.item(0).getTextContent();
                if (title == null || title.isBlank()) continue;
                String clean = title.trim();
                allTitles.add(clean);
                String lower = clean.toLowerCase(Locale.ROOT);
                for (String kw : INSIGHT_KEYWORDS) {
                    if (lower.contains(kw)) {
                        insightTitles.add(clean);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[NEWS-RADAR] XML parse error: {}", e.getMessage());
            return new NewsRadarResult(true, "RADAR_PARSE_ERROR", null, List.of());
        }

        if (!insightTitles.isEmpty()) {
            String top = String.join(" | ", insightTitles.subList(0, Math.min(2, insightTitles.size())));
            return new NewsRadarResult(
                    true,
                    "INJURY_OR_ROTATION_INSIGHT",
                    "🚨 NEWS RADAR (инсайд): " + top,
                    insightTitles
            );
        }
        if (!allTitles.isEmpty()) {
            return new NewsRadarResult(
                    true,
                    "NO_INJURY_NEWS_CLEAN_DRIFT",
                    "📰 NEWS RADAR: срочных травм/дисквалификаций в новостях за 48ч не найдено (проверено " + allTitles.size() + " загл.).",
                    allTitles.subList(0, Math.min(3, allTitles.size()))
            );
        }
        return new NewsRadarResult(
                true,
                "NO_NEWS_CLEAN_DRIFT",
                "📰 NEWS RADAR: резонансных новостей за 48ч не найдено — сдвиг кэфа не объясняется новостным инсайдом.",
                List.of()
        );
    }

    private String buildSearchQuery(String team1, String team2) {
        String t1 = sanitizeTeam(team1);
        String t2 = sanitizeTeam(team2);
        if (!t1.isEmpty() && !t2.isEmpty()) {
            return String.format("\"%s\" OR \"%s\" (injury OR lineup OR out OR suspended) when:2d", t1, t2);
        }
        String single = !t1.isEmpty() ? t1 : t2;
        return String.format("\"%s\" (injury OR lineup OR out OR suspended) when:2d", single);
    }

    private String sanitizeTeam(String t) {
        if (t == null) return "";
        return t.replaceAll("[\"']", "").trim();
    }
}
