package pro.datawiki.igaming.capture.liveresult.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import pro.datawiki.igaming.capture.liveresult.util.SimilarityUtils;
import pro.datawiki.igaming.dto.SportType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class LiveResultCaptureScheduler {

    private final RestTemplate restTemplate;

    @Value("${app.aggregator.url:http://localhost:3034}")
    private String aggregatorUrl;

    @Value("${app.capture.enabled:true}")
    private boolean enabled;

    private static final Pattern SCORE_PATTERN = Pattern.compile("\\b(\\d{1,3})\\s*[:\\-]\\s*(\\d{1,3})\\b");
    private static final Pattern TIME_PATTERN = Pattern.compile("^[0-2]?\\d:[0-5]\\d$");

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatchMetadata(
            @JsonProperty("id") Long id,
            @JsonProperty("sportName") @JsonAlias("sport_name") String sportName,
            @JsonProperty("team1Name") @JsonAlias("team1_name") String team1Name,
            @JsonProperty("team2Name") @JsonAlias("team2_name") String team2Name,
            @JsonProperty("startTime") @JsonAlias("start_time") Long startTime
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatchResultUpdate(
            @JsonProperty("score1") String score1,
            @JsonProperty("score2") String score2,
            @JsonProperty("matchResult") @JsonAlias("match_result") String matchResult
    ) {}

    @Scheduled(fixedDelayString = "${app.capture.fixed-delay:300000}", initialDelay = 30000)
    public void runCapture() {
        if (!enabled) {
            log.trace("LiveResult result capture is disabled.");
            return;
        }

        log.info("[LiveResult Scraper] Starting scheduled polling of aggregator...");
        try {
            String pendingUrl = aggregatorUrl + "/api/matches/needing/results";
            ResponseEntity<MatchMetadata[]> response = restTemplate.getForEntity(pendingUrl, MatchMetadata[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("[LiveResult Scraper] Aggregator returned non-2xx status: {}", response.getStatusCode());
                return;
            }

            MatchMetadata[] matches = response.getBody();
            if (matches.length == 0) {
                log.info("[LiveResult Scraper] No matches found needing results.");
                return;
            }

            log.info("[LiveResult Scraper] Found {} matches needing results.", matches.length);
            int updatedCount = 0;

            for (MatchMetadata match : matches) {
                try {
                    Optional<MatchResultUpdate> captured = scrapeResult(match);
                    if (captured.isPresent()) {
                        sendResultToAggregator(match.id(), captured.get());
                        updatedCount++;
                    }
                } catch (Exception e) {
                    log.error("[LiveResult Scraper] Failed to scrape match ID {}: {}", match.id(), e.getMessage());
                }
            }

            log.info("[LiveResult Scraper] Done. Updated {}/{} matches.", updatedCount, matches.length);
        } catch (Exception e) {
            log.error("[LiveResult Scraper] General error in capture scheduling loop: {}", e.getMessage(), e);
        }
    }

    private Optional<MatchResultUpdate> scrapeResult(MatchMetadata match) {
        if (match.startTime() == null) {
            return Optional.empty();
        }

        String sport = mapSportToLiveResult(match.sportName());
        
        // LiveResult uses date-based URLs in Europe/Moscow or UTC
        // Format: yyyy-MM-dd
        String dateStr = Instant.ofEpochMilli(match.startTime())
                .atZone(ZoneId.of("Europe/Moscow"))
                .toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        // URL format: https://www.liveresult.ru/{sport}/matches/{date}/
        String url = String.format("https://www.liveresult.ru/%s/matches/%s/", sport, dateStr);

        log.debug("[LiveResult Scraper] Fetching HTML for match ID {}: {} vs {} from {}",
                match.id(), match.team1Name(), match.team2Name(), url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ru,en-US;q=0.7,en;q=0.3")
                    .referrer("https://www.google.com/")
                    .timeout(15000)
                    .get();

            // Resilient lookup: find any elements that could represent a match row
            // We search in standard table rows (tr), list items (li), and div blocks
            Elements candidates = doc.select("tr, div.match-row, div.game, div.match, div.sport-event, div.event-row");
            
            // If no generic rows found, try selecting all div containers with short length
            if (candidates.isEmpty()) {
                candidates = doc.select("div");
            }

            long matchStartMilli = match.startTime();

            for (Element candidate : candidates) {
                String text = candidate.text();
                // A single match row's length should be reasonably short to avoid matching main container divs
                if (text.length() < 35 || text.length() > 300) {
                    continue;
                }

                // Check if this candidate block contains our two team names (direct or reversed)
                boolean homeFound = false;
                boolean awayFound = false;

                // Extract all child text blocks, links or spans to match team names
                Elements childSpans = candidate.select("a, span, div.team, td, .team1, .team2");
                for (Element child : childSpans) {
                    String childText = child.text().trim();
                    if (childText.length() < 2) continue;

                    if (!homeFound && SimilarityUtils.areTeamsSimilar(match.team1Name(), childText)) {
                        homeFound = true;
                    } else if (!awayFound && SimilarityUtils.areTeamsSimilar(match.team2Name(), childText)) {
                        awayFound = true;
                    }
                }

                // Fallback: search raw text of the block for substrings of normalized names
                if (!homeFound || !awayFound) {
                    String norm1 = SimilarityUtils.normalizeTeamName(match.team1Name());
                    String norm2 = SimilarityUtils.normalizeTeamName(match.team2Name());
                    String normText = SimilarityUtils.normalizeTeamName(text);
                    
                    if (normText.contains(norm1)) homeFound = true;
                    if (normText.contains(norm2)) awayFound = true;
                }

                if (homeFound && awayFound) {
                    log.info("[LiveResult Scraper] Found matching match row container for ID {}: '{}'", match.id(), text);

                    // Check if match is still live or scheduled
                    boolean isLive = text.contains("1-й тайм") || text.contains("2-й тайм") || text.contains("Перерыв")
                            || text.contains("Идет") || text.contains("Идёт") || text.contains("LIVE")
                            || text.matches(".*\\b\\d{1,3}'\\b.*");

                    // Determine if the match is finished. LiveResult marks finished matches with "Завершен", "Завершён", "Финал", "FT", or "окончен"
                    boolean finished = !isLive && (text.contains("Завершен") || text.contains("Завершён") 
                            || text.contains("Финал") || text.contains("FT") || text.contains("окончен"));

                    if (!finished) {
                        log.debug("[LiveResult Scraper] Match ID {} matched but is not marked as finished yet (isLive={}).", match.id(), isLive);
                        continue;
                    }

                    String score1 = null;
                    String score2 = null;

                    // First priority: check dedicated score elements
                    Elements scoreElements = candidate.select(".score, .result, .m-score, a[href*=match]");
                    for (Element scoreEl : scoreElements) {
                        String scoreText = scoreEl.text().trim();
                        Matcher m = SCORE_PATTERN.matcher(scoreText);
                        if (m.find() && !TIME_PATTERN.matcher(m.group()).matches()) {
                            try {
                                int s1 = Integer.parseInt(m.group(1));
                                int s2 = Integer.parseInt(m.group(2));
                                if (isValidScore(match.sportName(), s1, s2)) {
                                    score1 = String.valueOf(s1);
                                    score2 = String.valueOf(s2);
                                    break;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    // Fallback to text matching with score pattern
                    if (score1 == null || score2 == null) {
                        Matcher scoreMatcher = SCORE_PATTERN.matcher(text);
                        while (scoreMatcher.find()) {
                            String matchStr = scoreMatcher.group().trim();
                            if (TIME_PATTERN.matcher(matchStr).matches()) {
                                continue;
                            }
                            try {
                                int s1 = Integer.parseInt(scoreMatcher.group(1));
                                int s2 = Integer.parseInt(scoreMatcher.group(2));
                                if (isValidScore(match.sportName(), s1, s2)) {
                                    score1 = String.valueOf(s1);
                                    score2 = String.valueOf(s2);
                                    break;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    if (score1 != null && score2 != null) {
                        String finalScore1 = score1;
                        String finalScore2 = score2;

                        String matchResult = determineMatchResult(finalScore1, finalScore2);
                        if (matchResult == null) {
                            continue;
                        }

                        log.info("[LiveResult Scraper] MATCH SCRAPED: Match ID {} -> score {}:{} (Result: {})",
                                match.id(), finalScore1, finalScore2, matchResult);

                        return Optional.of(new MatchResultUpdate(finalScore1, finalScore2, matchResult));
                    }
                }
            }

            log.info("[LiveResult Scraper] No matching finished events found on page for match ID {}", match.id());

        } catch (Exception e) {
            log.error("[LiveResult Scraper] Exception scraping liveresult.ru for match ID {}: {}", match.id(), e.getMessage());
        }

        return Optional.empty();
    }

    private void sendResultToAggregator(Long matchId, MatchResultUpdate update) {
        String updateUrl = aggregatorUrl + "/api/matches/" + matchId + "/result";
        log.info("[LiveResult Scraper] Sending score update for match ID {} to {}", matchId, updateUrl);
        try {
            restTemplate.postForEntity(updateUrl, update, Void.class);
        } catch (Exception e) {
            log.error("[LiveResult Scraper] Failed to send update for match ID {}: {}", matchId, e.getMessage());
        }
    }

    private boolean isValidScore(String sport, int s1, int s2) {
        if (s1 < 0 || s2 < 0) return false;
        SportType type = SportType.fromAny(sport);
        return switch (type) {
            case BASKETBALL -> s1 <= 220 && s2 <= 220;
            case TENNIS, TABLE_TENNIS -> s1 <= 7 && s2 <= 7;
            case HOCKEY -> s1 <= 25 && s2 <= 25;
            default -> s1 <= 20 && s2 <= 20; // Football, etc.
        };
    }

    private String mapSportToLiveResult(String aggregatorSport) {
        SportType type = SportType.fromAny(aggregatorSport);
        return type == SportType.HOCKEY ? "hockey" : type.getSlug();
    }

    private String determineMatchResult(String score1Str, String score2Str) {
        try {
            int s1 = Integer.parseInt(score1Str.trim());
            int s2 = Integer.parseInt(score2Str.trim());
            if (s1 > s2) return "WIN1";
            if (s1 < s2) return "WIN2";
            return "DRAW";
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
