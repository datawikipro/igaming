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
import pro.datawiki.igaming.capture.liveresult.util.SimilarityUtils;

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

    private static final Pattern SCORE_PATTERN = Pattern.compile("\\b(\\d+)\\s*[:\\-]\\s*(\\d+)\\b");
    private static final Pattern TIME_PATTERN = Pattern.compile("^[0-2]?\\d:[0-5]\\d$");

    public record MatchMetadata(
            Long id,
            String sportName,
            String team1Name,
            String team2Name,
            Long startTime
    ) {}

    public record MatchResultUpdate(
            String score1,
            String score2,
            String matchResult
    ) {}

    @Scheduled(fixedDelayString = "${app.capture.fixed-delay:300000}", initialDelay = 30000)
    public void runCapture() {
        if (!enabled) {
            log.trace("LiveResult result capture is disabled.");
            return;
        }

        log.info("[LiveResult Scraper] Starting scheduled polling of aggregator...");
        try {
            String pendingUrl = aggregatorUrl + "/api/matches/needing-results";
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

                    // Determine if the match is finished. LiveResult marks finished matches with "Завершен", "Финал", "FT", or shows score instead of time
                    boolean finished = text.contains("Завершен") || text.contains("Завершён") || text.contains("Финал") || text.contains("FT") || text.contains("окончен");
                    
                    // Fallback finished detection: if the block contains a score and doesn't contain a scheduled start time indicator, it's highly likely completed
                    if (!finished && text.contains(":") && !text.contains("Ожидается")) {
                        finished = true;
                    }

                    if (!finished) {
                        log.debug("[LiveResult Scraper] Match ID {} matched but is not marked as finished yet.", match.id());
                        continue;
                    }

                    // Extract score matching the standard pattern (e.g. "2 : 1", "2-1")
                    Matcher scoreMatcher = SCORE_PATTERN.matcher(text);
                    String score1 = null;
                    String score2 = null;

                    while (scoreMatcher.find()) {
                        String matchStr = scoreMatcher.group().trim();
                        // Verify that this is not a start time (like "19:00" or "15:30")
                        if (TIME_PATTERN.matcher(matchStr).matches()) {
                            continue;
                        }
                        
                        score1 = scoreMatcher.group(1);
                        score2 = scoreMatcher.group(2);
                        break; // Pick the first score pattern found that is not the start time
                    }

                    if (score1 == null || score2 == null) {
                        // Fallback: check Jsoup selectors for scores
                        Elements scoreElements = candidate.select(".score, .result, .m-score, a[href*=match]");
                        for (Element scoreEl : scoreElements) {
                            String scoreText = scoreEl.text().trim();
                            Matcher m = SCORE_PATTERN.matcher(scoreText);
                            if (m.find() && !TIME_PATTERN.matcher(m.group()).matches()) {
                                score1 = m.group(1);
                                score2 = m.group(2);
                                break;
                            }
                        }
                    }

                    if (score1 != null && score2 != null) {
                        // Determine home/away alignment
                        boolean direct = SimilarityUtils.areTeamsSimilar(match.team1Name(), candidate.text());
                        // If direct contains team1 followed by team2, we assume direct alignment, otherwise we keep score1 for team1
                        
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
            restTemplate.postForLocation(updateUrl, update);
        } catch (Exception e) {
            log.error("[LiveResult Scraper] Failed to send update for match ID {}: {}", matchId, e.getMessage());
        }
    }

    private String mapSportToLiveResult(String aggregatorSport) {
        if (aggregatorSport == null) return "football";
        return switch (aggregatorSport.toUpperCase()) {
            case "FOOTBALL" -> "football";
            case "TENNIS" -> "tennis";
            case "BASKETBALL" -> "basketball";
            case "HOCKEY", "ICE_HOCKEY", "ICE-HOCKEY" -> "hockey";
            case "VOLLEYBALL" -> "volleyball";
            default -> aggregatorSport.toLowerCase();
        };
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
