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
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse public Bet365 site: {}.", e.getMessage(), e);
        }

        if (list.isEmpty()) {
            log.warn("Scraped 0 active events from Bet365 (no mock fallback).");
        } else {
            log.info("Successfully scraped {} active events from Bet365", list.size());
        }

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

                }
            }

            Element dateEl = group.selectFirst(".cpm-MarketFixtureDateHeader");
            String dateStr = dateEl != null ? dateEl.text().trim() : "";

            // Get active fixtures using the selector rule
            boolean hasDetails100 = !group.select(".cpm-ParticipantFixtureDetails100").isEmpty();
            Elements rawFixtures = hasDetails100 ? group.select(".cpm-ParticipantFixtureDetails100") : group.select(".cpm-MarketFixture");
            
            List<Bet365Event> groupEvents = new ArrayList<>();
            
            for (Element rf : rawFixtures) {
                if (rf.hasClass("Hidden") || rf.hasClass("Divider") || rf.hasClass("rcl-MarketCouponAdvancedBase_Divider")) {
                    continue;
                }
                
                // Avoid nested double-counting (ParticipantFixtureDetails inside MarketFixture)
                if (rf.hasClass("cpm-ParticipantFixtureDetails") && hasParentWithClass(rf, "cpm-MarketFixture")) {
                    continue;
                }
                
                Elements teamsEl = hasDetails100 ? rf.select(".cpm-ParticipantFixtureDetails100_Team") : rf.select(".cpm-ParticipantFixtureDetails_Team");
                List<String> teams = new ArrayList<>();
                for (Element tEl : teamsEl) {
                    String tText = tEl.text().trim();
                    if (!tText.isEmpty() && !teams.contains(tText)) {
                        teams.add(tText);
                    }
                }
                
                if (teams.size() >= 2) {
                    Element closesEl = hasDetails100 ? rf.selectFirst(".cpm-ParticipantFixtureDetails100_BookCloses") : rf.selectFirst(".cpm-ParticipantFixtureDetails_BookCloses");
                    String closesStr = closesEl != null ? closesEl.text().trim() : "";
                    
                    boolean isLive = dateStr.toLowerCase().contains("live") || closesStr.toLowerCase().contains("live");
                    long startTime = parseSwedishDate(dateStr, closesStr);
                    
                    String eventId = generateEventId(sport, teams.get(0), teams.get(1), startTime);
                    
                    Bet365Event event = new Bet365Event();
                    event.setId(eventId);
                    event.setSport(sport);
                    event.setLeague(league);
                    event.setHomeTeam(teams.get(0));
                    event.setAwayTeam(teams.get(1));
                    event.setStartTime(startTime);
                    event.setLive(isLive);
                    
                    groupEvents.add(event);
                }
            }
            
            int N = groupEvents.size();
            if (N == 0) {
                continue;
            }
            
            eventsList.addAll(groupEvents);
            
            // Parse odds columns
            Elements rawCols = group.select(".cpm-MarketOdds");
            for (Element rc : rawCols) {
                Element hdrEl = rc.selectFirst(".cpm-MarketOddsHeader");
                String marketName = hdrEl != null ? hdrEl.text().trim() : "Match Winner";
                if (marketName.isEmpty()) {
                    marketName = "Match Winner";
                }
                
                Elements oddsElements = rc.select(".cpm-ParticipantOdds_Odds");
                List<Double> oddsVals = new ArrayList<>();
                for (Element oe : oddsElements) {
                    try {
                        oddsVals.add(Double.parseDouble(oe.text().trim()));
                    } catch (NumberFormatException ignored) {}
                }
                
                int K = oddsVals.size();
                if (K == 0) {
                    continue;
                }
                
                // Map odds to groupEvents using index rules
                if (K == N * 2) {
                    for (int i = 0; i < N; i++) {
                        String eventId = groupEvents.get(i).getId();
                        String homeTeam = groupEvents.get(i).getHomeTeam();
                        String awayTeam = groupEvents.get(i).getAwayTeam();
                        
                        List<Bet365Odd> eventOdds = oddsCache.computeIfAbsent(eventId, k -> new ArrayList<>());
                        eventOdds.add(new Bet365Odd("1", marketName, homeTeam, oddsVals.get(i * 2)));
                        eventOdds.add(new Bet365Odd("2", marketName, awayTeam, oddsVals.get(i * 2 + 1)));
                    }
                } else if (K == N * 3) {
                    for (int i = 0; i < N; i++) {
                        String eventId = groupEvents.get(i).getId();
                        String homeTeam = groupEvents.get(i).getHomeTeam();
                        String awayTeam = groupEvents.get(i).getAwayTeam();
                        
                        List<Bet365Odd> eventOdds = oddsCache.computeIfAbsent(eventId, k -> new ArrayList<>());
                        eventOdds.add(new Bet365Odd("1", marketName, homeTeam, oddsVals.get(i * 3)));
                        eventOdds.add(new Bet365Odd("X", marketName, "Draw", oddsVals.get(i * 3 + 1)));
                        eventOdds.add(new Bet365Odd("2", marketName, awayTeam, oddsVals.get(i * 3 + 2)));
                    }
                } else if (K == N) {
                    String outcomeNameCode = "1";
                    if (marketName.equals("2") || marketName.toLowerCase().contains("away")) {
                        outcomeNameCode = "2";
                    } else if (marketName.equals("X") || marketName.toLowerCase().contains("draw") || marketName.toLowerCase().contains("oavgjort")) {
                        outcomeNameCode = "X";
                    }
                    
                    for (int i = 0; i < N; i++) {
                        String eventId = groupEvents.get(i).getId();
                        String homeTeam = groupEvents.get(i).getHomeTeam();
                        String awayTeam = groupEvents.get(i).getAwayTeam();
                        
                        String outcomeName = homeTeam;
                        if ("2".equals(outcomeNameCode)) {
                            outcomeName = awayTeam;
                        } else if ("X".equals(outcomeNameCode)) {
                            outcomeName = "Draw";
                        }
                        
                        List<Bet365Odd> eventOdds = oddsCache.computeIfAbsent(eventId, k -> new ArrayList<>());
                        eventOdds.add(new Bet365Odd(outcomeNameCode, sport.equals("Tennis") || sport.equals("Esports") ? "Match Winner" : "1X2", outcomeName, oddsVals.get(i)));
                    }
                }
            }
        }
    }

    private boolean hasParentWithClass(Element element, String className) {
        Element parent = element.parent();
        while (parent != null) {
            if (parent.hasClass(className)) {
                return true;
            }
            parent = parent.parent();
        }
        return false;
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
