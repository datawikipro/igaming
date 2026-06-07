package pro.datawiki.igaming.source.bet365.service;

import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.bet365.config.Bet365Config;
import pro.datawiki.igaming.source.core.browser.BrowserService;
import org.springframework.context.ApplicationEventPublisher;
import pro.datawiki.igaming.source.core.notification.ScrapingFailureEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class Bet365ApiClient {

    private final RestTemplate restTemplate;
    private final Bet365Config bet365Config;
    private final BrowserService browserService;
    private final ApplicationEventPublisher eventPublisher;

    // Cache to share scraped odds between getEvents (discovery) and getEventOdds (retrieval)
    private final Map<String, List<Bet365Odd>> oddsCache = new ConcurrentHashMap<>();

    public Bet365EventsResponse getEvents() {
        log.debug("Initializing browser-based extraction for Bet365 events...");
        
        List<Bet365Event> list = new ArrayList<>();
        
        try {
            String targetUrl = bet365Config.getApi().getBaseUrl();
            if (targetUrl != null && !targetUrl.contains("mock")) {
                log.info("Scraping public Bet365 site dynamically from: {}", targetUrl);
                // Wait 18 seconds for Cloudflare, WebSockets, and React layout to fully render
                String body = browserService.navigateAndGetBody(targetUrl, 18000, "bet365-lobby");
                log.debug("Retrieved Bet365 page body length: {}", body != null ? body.length() : 0);
                
                if (body != null && !body.isEmpty()) {
                    parseHtmlAndPopulateCache(body, list);
                    if (list.isEmpty()) {
                        eventPublisher.publishEvent(new ScrapingFailureEvent(this, "bet365", "DOM parsing failed: 0 events parsed", targetUrl));
                        throw new RuntimeException("DOM parsing failed: Non-empty HTML body received but parsed 0 events from Bet365. DOM layout might have changed.");
                    }
                } else {
                    log.warn("Blank body retrieved from Bet365 lobby. Likely network or Turnstile block.");
                    eventPublisher.publishEvent(new ScrapingFailureEvent(this, "bet365", "Blank body retrieved (network/block)", targetUrl));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse public Bet365 site: {}.", e.getMessage(), e);
            String targetUrl = bet365Config.getApi().getBaseUrl();
            if (targetUrl == null || targetUrl.isEmpty() || targetUrl.contains("mock")) {
                targetUrl = "https://www.bet365.com";
            }
            eventPublisher.publishEvent(new ScrapingFailureEvent(this, "bet365", "Exception: " + e.getMessage(), targetUrl));
            if (e instanceof RuntimeException && e.getMessage() != null && e.getMessage().contains("DOM parsing failed")) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException("DOM parsing failed: Unexpected error during HTML parsing - " + e.getMessage(), e);
            }
        }

        log.info("Successfully scraped {} active events from Bet365", list.size());

        Bet365EventsResponse response = new Bet365EventsResponse();
        response.setEvents(list);
        return response;
    }

    public Bet365OddsResponse getEventOdds(String eventId) {
        log.debug("Retrieving Bet365 event odds for: {}", eventId);
        
        List<Bet365Odd> odds = oddsCache.get(eventId);
        
        if (odds == null) {
            log.debug("Event {} not found in local scraped odds cache.", eventId);
            odds = new ArrayList<>();
        } else {
            log.debug("Found {} cached odds for event {}", odds.size(), eventId);
        }

        Bet365OddsResponse response = new Bet365OddsResponse();
        response.setEventId(eventId);
        response.setOdds(odds);
        return response;
    }

    private void parseHtmlAndPopulateCache(String html, List<Bet365Event> eventsList) {
        Document doc = Jsoup.parse(html);
        Elements groups = doc.select(".gl-MarketGroupContainer");
        
        for (Element group : groups) {
            String sport = "Soccer"; // Default
            String league = "Popular League";

            // Resolve sport/league name from ancestors and headers
            Element headerImageEl = null;
            Element headerTitleEl = null;
            
            Element curr = group;
            while (curr != null && !curr.tagName().equalsIgnoreCase("body")) {
                headerImageEl = curr.selectFirst(".cpm-Header_HeaderImage");
                headerTitleEl = curr.selectFirst(".cpm-Header_Title");
                if (headerImageEl != null || headerTitleEl != null) {
                    break;
                }
                
                // Check preceding siblings
                Element prev = curr.previousElementSibling();
                while (prev != null) {
                    headerImageEl = prev.selectFirst(".cpm-Header_HeaderImage");
                    headerTitleEl = prev.selectFirst(".cpm-Header_Title");
                    if (headerImageEl != null || headerTitleEl != null) {
                        break;
                    }
                    if (prev.hasClass("cpm-Header")) {
                        headerTitleEl = prev;
                        break;
                    }
                    prev = prev.previousElementSibling();
                }
                if (headerImageEl != null || headerTitleEl != null) {
                    break;
                }
                curr = curr.parent();
            }

            if (headerTitleEl != null) {
                league = headerTitleEl.text().trim();
                // Strip "Se alla" or other suffix if present
                if (league.endsWith("Se alla")) {
                    league = league.substring(0, league.length() - 7).trim();
                }
            } else {
                // Fallback to old header resolution
                Element groupHeader = group.selectFirst(".gl-MarketGroupButton");
                if (groupHeader == null && group.parent() != null) {
                    groupHeader = group.parent().selectFirst(".gl-MarketGroupButton");
                }
                if (groupHeader != null) {
                    league = groupHeader.text().trim();
                }
            }

            // Determine sport
            String sportDetermined = null;
            if (headerImageEl != null) {
                String style = headerImageEl.attr("style");
                if (style != null) {
                    String styleLower = style.toLowerCase();
                    if (styleLower.contains("tennis")) {
                        sportDetermined = "Tennis";
                    } else if (styleLower.contains("esports") || styleLower.contains("esport")) {
                        sportDetermined = "Esports";
                    } else if (styleLower.contains("soccer") || styleLower.contains("football")) {
                        sportDetermined = "Soccer";
                    } else if (styleLower.contains("golf") || styleLower.contains("pga")) {
                        sportDetermined = "Golf";
                    } else if (styleLower.contains("basket")) {
                        sportDetermined = "Basketball";
                    }
                }
            }

            if (sportDetermined == null) {
                // Check class name of the group
                String classNameLower = group.className().toLowerCase();
                if (classNameLower.contains("golf")) {
                    sportDetermined = "Golf";
                } else if (classNameLower.contains("esport")) {
                    sportDetermined = "Esports";
                } else if (classNameLower.contains("tennis")) {
                    sportDetermined = "Tennis";
                } else if (classNameLower.contains("soccer") || classNameLower.contains("football")) {
                    sportDetermined = "Soccer";
                }
            }

            if (sportDetermined == null) {
                // Check league name
                String leagueLower = league.toLowerCase();
                if (leagueLower.contains("tennis") || leagueLower.contains("open") || leagueLower.contains("franska öppna")) {
                    sportDetermined = "Tennis";
                } else if (leagueLower.contains("esport") || leagueLower.contains("valorant") || leagueLower.contains("cs2") || leagueLower.contains("league of legends") || leagueLower.contains("dota")) {
                    sportDetermined = "Esports";
                } else if (leagueLower.contains("basket") || leagueLower.contains("nba")) {
                    sportDetermined = "Basketball";
                } else if (leagueLower.contains("fotboll") || leagueLower.contains("soccer") || leagueLower.contains("champions league") || leagueLower.contains("allsvenskan")) {
                    sportDetermined = "Soccer";
                }
            }

            if (sportDetermined != null) {
                sport = sportDetermined;
            }

            Element dateEl = group.selectFirst(".cpm-MarketFixtureDateHeader");
            String dateStr = dateEl != null ? dateEl.text().trim() : "";

            // Find all team blocks in this group (both cpm-ParticipantFixtureDetails and cpm-ParticipantFixtureDetails100)
            Elements teamBlocks = group.select(".cpm-ParticipantFixtureDetails, .cpm-ParticipantFixtureDetails100");
            if (teamBlocks.isEmpty()) continue;

            // Find all odds columns in this group
            Elements oddsColumns = group.select(".cpm-MarketOdds");

            for (int matchIdx = 0; matchIdx < teamBlocks.size(); matchIdx++) {
                Element teamBlock = teamBlocks.get(matchIdx);
                Elements teamEls = teamBlock.select(".cpm-ParticipantFixtureDetails_Team, .cpm-ParticipantFixtureDetails100_Team, .cpm-ParticipantLabelGolfScore_Name");
                if (teamEls.isEmpty()) continue;

                String team1 = teamEls.get(0).text().trim();
                String team2 = teamEls.size() > 1 ? teamEls.get(1).text().trim() : "";

                Element timeEl = teamBlock.selectFirst(".cpm-ParticipantFixtureDetails_BookCloses, .cpm-ParticipantFixtureDetails100_BookCloses");
                String timeStr = timeEl != null ? timeEl.text().trim() : "";

                boolean isLive = dateStr.toLowerCase().contains("live") || timeStr.toLowerCase().contains("live");
                long startTime = parseSwedishDate(dateStr, timeStr);

                String eventId = generateEventId(sport, team1, team2, startTime);

                Bet365Event event = new Bet365Event();
                event.setId(eventId);
                event.setSport(sport);
                event.setLeague(league);
                event.setHomeTeam(team1);
                event.setAwayTeam(team2);
                event.setStartTime(startTime);
                event.setLive(isLive);

                eventsList.add(event);

                // Initialize odds list for this event
                List<Bet365Odd> eventOdds = new ArrayList<>();

                for (Element col : oddsColumns) {
                    Element headerEl = col.selectFirst(".cpm-MarketOddsHeader");
                    String marketName = headerEl != null ? headerEl.text().trim() : "Match Winner";

                    Elements oddEls = col.select(".gl-Participant_General, .cpm-ParticipantOdds");
                    
                    if (oddEls.size() == teamBlocks.size() * 2) {
                        // 2 outcomes per match
                        if (matchIdx * 2 + 1 < oddEls.size()) {
                            Element odd1El = oddEls.get(matchIdx * 2);
                            Element odd2El = oddEls.get(matchIdx * 2 + 1);

                            addOdd(eventOdds, "1", marketName, team1, odd1El);
                            addOdd(eventOdds, "2", marketName, team2, odd2El);
                        }
                    } else if (oddEls.size() == teamBlocks.size()) {
                        // 1 outcome per match
                        if (matchIdx < oddEls.size()) {
                            Element oddEl = oddEls.get(matchIdx);
                            String outcomeCode = "1";
                            String outcomeName = team1;
                            
                            String marketLower = marketName.toLowerCase();
                            if (marketName.equals("X") || marketLower.contains("oavgjort") || marketLower.contains("draw")) {
                                outcomeCode = "X";
                                outcomeName = "Draw";
                            } else if (marketName.equals("2")) {
                                outcomeCode = "2";
                                outcomeName = team2;
                            }
                            addOdd(eventOdds, outcomeCode, marketName, outcomeName, oddEl);
                        }
                    }
                }

                if (!eventOdds.isEmpty()) {
                    oddsCache.put(eventId, eventOdds);
                }
            }
        }
    }

    private void addOdd(List<Bet365Odd> eventOdds, String code, String marketName, String outcomeName, Element oddEl) {
        try {
            Element valEl = oddEl.selectFirst(".cpm-ParticipantOdds_Odds");
            String valStr = valEl != null ? valEl.text().trim() : oddEl.text().trim();
            if (valStr.isEmpty()) return;

            double val = Double.parseDouble(valStr);

            Element handicapEl = oddEl.selectFirst(".cpm-ParticipantOdds_Handicap");
            if (handicapEl != null && !handicapEl.text().trim().isEmpty()) {
                outcomeName = outcomeName + " (" + handicapEl.text().trim() + ")";
            }

            eventOdds.add(new Bet365Odd(code, marketName, outcomeName, val));
        } catch (Exception ignored) {}
    }

    private long parseSwedishDate(String dateStr, String timeStr) {
        if (dateStr == null) dateStr = "";
        if (timeStr == null) timeStr = "";
        
        String cleanDate = dateStr.toLowerCase().trim();
        String cleanTime = timeStr.toLowerCase().trim();

        if (cleanDate.contains("live") || cleanTime.contains("live") || (cleanDate.isEmpty() && cleanTime.isEmpty())) {
            return System.currentTimeMillis();
        }

        try {
            int currentYear = java.time.LocalDate.now().getYear();
            int month = java.time.LocalDate.now().getMonthValue();
            int day = java.time.LocalDate.now().getDayOfMonth();
            int hour = 12;
            int minute = 0;

            // Extract time (HH:mm)
            java.util.regex.Matcher timeMatcher = java.util.regex.Pattern.compile("(\\d{2}):(\\d{2})").matcher(cleanTime);
            if (timeMatcher.find()) {
                hour = Integer.parseInt(timeMatcher.group(1));
                minute = Integer.parseInt(timeMatcher.group(2));
            } else {
                timeMatcher = java.util.regex.Pattern.compile("(\\d{2}):(\\d{2})").matcher(cleanDate);
                if (timeMatcher.find()) {
                    hour = Integer.parseInt(timeMatcher.group(1));
                    minute = Integer.parseInt(timeMatcher.group(2));
                }
            }

            // Extract day and month (e.g. "07 jun")
            String[] swMonths = {"jan", "feb", "mar", "apr", "maj", "jun", "jul", "aug", "sep", "okt", "nov", "dec"};
            java.util.regex.Matcher dateMatcher = java.util.regex.Pattern.compile("(\\d+)\\s+([a-zåäö]+)").matcher(cleanDate);
            if (dateMatcher.find()) {
                day = Integer.parseInt(dateMatcher.group(1));
                String monthName = dateMatcher.group(2);
                for (int i = 0; i < swMonths.length; i++) {
                    if (monthName.startsWith(swMonths[i])) {
                        month = i + 1;
                        break;
                    }
                }
            }

            java.time.LocalDateTime ldt = java.time.LocalDateTime.of(currentYear, month, day, hour, minute);
            return ldt.atZone(java.time.ZoneId.of("Europe/Stockholm")).toInstant().toEpochMilli();
        } catch (Exception e) {
            return System.currentTimeMillis() + 3600000; // fallback: starts in 1 hour
        }
    }

    private String generateEventId(String sport, String team1, String team2, long startTime) {
        try {
            String input = sport + "|" + team1 + "|" + team2 + "|" + startTime;
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return "b365_" + hexString.substring(0, 16);
        } catch (Exception e) {
            return "b365_" + Math.abs((team1 + team2 + startTime).hashCode());
        }
    }

    @Getter
    @Setter
    public static class Bet365EventsResponse {
        private List<Bet365Event> events;
    }

    @Getter
    @Setter
    public static class Bet365Event {
        private String id;
        private String sport;
        private String league;
        private String homeTeam;
        private String awayTeam;
        private long startTime;
        private boolean isLive;
    }

    @Getter
    @Setter
    public static class Bet365OddsResponse {
        private String eventId;
        private List<Bet365Odd> odds;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Bet365Odd {
        private String code;
        private String marketName;
        private String outcomeName;
        private double value;
    }
}
