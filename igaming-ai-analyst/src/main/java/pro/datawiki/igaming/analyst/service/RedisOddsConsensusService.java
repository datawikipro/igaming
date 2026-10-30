package pro.datawiki.igaming.analyst.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisOddsConsensusService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String ODDS_MATCH_PREFIX = "odds:match:";
    private static final String MATCHES_ACTIVE_KEY = "matches:active";

    public record OddQuote(double value, String bookmaker) {}

    public record OddsConsensusDto(
            int bookmakersCount,
            Set<String> bookmakerNames,
            BigDecimal bestWin1,
            BigDecimal avgWin1,
            String bestWin1Bookmaker,
            BigDecimal bestDraw,
            BigDecimal avgDraw,
            String bestDrawBookmaker,
            BigDecimal bestWin2,
            BigDecimal avgWin2,
            String bestWin2Bookmaker,
            BigDecimal bestOver25,
            BigDecimal avgOver25,
            BigDecimal bestUnder25,
            BigDecimal avgUnder25,
            String formattedMarkdown
    ) {}

    /**
     * Retrieves active match IDs currently held in Redis in-memory storage.
     */
    public Set<String> getActiveMatchIds() {
        try {
            Set<String> members = redisTemplate.opsForSet().members(MATCHES_ACTIVE_KEY);
            return members != null ? members : Collections.emptySet();
        } catch (Exception e) {
            log.warn("Failed to retrieve active match IDs from Redis: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Checks if Redis has active odds for a given match.
     */
    public boolean hasActiveOdds(Long matchId) {
        if (matchId == null) return false;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(ODDS_MATCH_PREFIX + matchId));
        } catch (Exception e) {
            log.debug("Redis error checking odds for match {}: {}", matchId, e.getMessage());
            return false;
        }
    }

    /**
     * Aggregates live bookmaker quotes from Redis into a rich consensus breakdown.
     */
    public OddsConsensusDto getConsensus(Long matchId) {
        if (matchId == null) return null;

        Map<Object, Object> rawEntries;
        try {
            rawEntries = redisTemplate.opsForHash().entries(ODDS_MATCH_PREFIX + matchId);
        } catch (Exception e) {
            log.error("Failed to read Redis odds for matchId={}: {}", matchId, e.getMessage());
            return null;
        }

        if (rawEntries == null || rawEntries.isEmpty()) {
            return null;
        }

        Set<String> bookmakers = new TreeSet<>();
        List<OddQuote> win1Quotes = new ArrayList<>();
        List<OddQuote> drawQuotes = new ArrayList<>();
        List<OddQuote> win2Quotes = new ArrayList<>();

        List<OddQuote> dc1xQuotes = new ArrayList<>();
        List<OddQuote> dcX2Quotes = new ArrayList<>();
        List<OddQuote> dc12Quotes = new ArrayList<>();

        Map<Double, List<OddQuote>> totalOverMap = new TreeMap<>();
        Map<Double, List<OddQuote>> totalUnderMap = new TreeMap<>();

        Map<Double, List<OddQuote>> handicap1Map = new TreeMap<>();
        Map<Double, List<OddQuote>> handicap2Map = new TreeMap<>();

        for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
            if (!(entry.getValue() instanceof String json)) continue;

            try {
                JsonNode node = objectMapper.readTree(json);
                if (node.path("isSuspended").asBoolean(false)) {
                    continue;
                }
                double value = node.path("value").asDouble(0.0);
                if (value <= 1.01 || value > 250.0) {
                    continue;
                }

                String bmName = node.path("betSourceName").asText("");
                if (bmName.isBlank()) {
                    bmName = node.path("bookmaker").asText("Bookmaker");
                }
                bookmakers.add(bmName);

                String typeCode = node.path("typeCode").asText("").toUpperCase();
                Double param = node.has("param") && !node.get("param").isNull() ? node.path("param").asDouble() : null;

                OddQuote quote = new OddQuote(value, bmName);

                switch (typeCode) {
                    case "WIN1", "P1", "HOME_WIN" -> win1Quotes.add(quote);
                    case "DRAW", "X" -> drawQuotes.add(quote);
                    case "WIN2", "P2", "AWAY_WIN" -> win2Quotes.add(quote);
                    case "DC_1X", "DOUBLE_CHANCE_1X" -> dc1xQuotes.add(quote);
                    case "DC_X2", "DOUBLE_CHANCE_X2" -> dcX2Quotes.add(quote);
                    case "DC_12", "DOUBLE_CHANCE_12" -> dc12Quotes.add(quote);
                    case "TOTAL_OVER", "TO" -> {
                        if (param != null) totalOverMap.computeIfAbsent(param, k -> new ArrayList<>()).add(quote);
                    }
                    case "TOTAL_UNDER", "TU" -> {
                        if (param != null) totalUnderMap.computeIfAbsent(param, k -> new ArrayList<>()).add(quote);
                    }
                    case "HANDICAP_1", "H1" -> {
                        if (param != null) handicap1Map.computeIfAbsent(param, k -> new ArrayList<>()).add(quote);
                    }
                    case "HANDICAP_2", "H2" -> {
                        if (param != null) handicap2Map.computeIfAbsent(param, k -> new ArrayList<>()).add(quote);
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse odd entry for matchId={}: {}", matchId, e.getMessage());
            }
        }

        if (bookmakers.isEmpty() && win1Quotes.isEmpty() && win2Quotes.isEmpty()) {
            return null;
        }

        OddQuote bestWin1 = findBest(win1Quotes);
        BigDecimal avgWin1 = calcAvg(win1Quotes);

        OddQuote bestDraw = findBest(drawQuotes);
        BigDecimal avgDraw = calcAvg(drawQuotes);

        OddQuote bestWin2 = findBest(win2Quotes);
        BigDecimal avgWin2 = calcAvg(win2Quotes);

        // Standard 2.5 total if available
        OddQuote bestTo25 = totalOverMap.containsKey(2.5) ? findBest(totalOverMap.get(2.5)) : null;
        BigDecimal avgTo25 = totalOverMap.containsKey(2.5) ? calcAvg(totalOverMap.get(2.5)) : null;

        OddQuote bestTu25 = totalUnderMap.containsKey(2.5) ? findBest(totalUnderMap.get(2.5)) : null;
        BigDecimal avgTu25 = totalUnderMap.containsKey(2.5) ? calcAvg(totalUnderMap.get(2.5)) : null;

        // Build Markdown Block
        StringBuilder sb = new StringBuilder();
        sb.append("## 📊 Консенсус котировок букмекеров (Market Odds — Live Redis Ingestion):\n");
        sb.append(String.format("- Число букмекеров в мониторинге: %d (%s)\n",
                bookmakers.size(), String.join(", ", bookmakers)));

        sb.append("- Основные исходы (П1 / X / П2):\n");
        if (bestWin1 != null) {
            sb.append(String.format("  * **П1**: лучший = %.3f (%s), средний = %s\n",
                    bestWin1.value(), bestWin1.bookmaker(), avgWin1 != null ? avgWin1.toPlainString() : "N/A"));
        }
        if (bestDraw != null) {
            sb.append(String.format("  * **X (Ничья)**: лучший = %.3f (%s), средний = %s\n",
                    bestDraw.value(), bestDraw.bookmaker(), avgDraw != null ? avgDraw.toPlainString() : "N/A"));
        }
        if (bestWin2 != null) {
            sb.append(String.format("  * **П2**: лучший = %.3f (%s), средний = %s\n",
                    bestWin2.value(), bestWin2.bookmaker(), avgWin2 != null ? avgWin2.toPlainString() : "N/A"));
        }

        // Double chance
        OddQuote best1x = findBest(dc1xQuotes);
        OddQuote bestX2 = findBest(dcX2Quotes);
        if (best1x != null || bestX2 != null) {
            sb.append("- Двойной шанс:\n");
            if (best1x != null) {
                sb.append(String.format("  * **1X**: лучший = %.3f (%s)\n", best1x.value(), best1x.bookmaker()));
            }
            if (bestX2 != null) {
                sb.append(String.format("  * **X2**: лучший = %.3f (%s)\n", bestX2.value(), bestX2.bookmaker()));
            }
        }

        // Totals (select 2-3 key lines around the median)
        if (!totalOverMap.isEmpty() || !totalUnderMap.isEmpty()) {
            sb.append("- Тоталы матча:\n");
            Set<Double> allTotals = new TreeSet<>(totalOverMap.keySet());
            allTotals.addAll(totalUnderMap.keySet());
            for (Double p : allTotals) {
                OddQuote over = totalOverMap.containsKey(p) ? findBest(totalOverMap.get(p)) : null;
                OddQuote under = totalUnderMap.containsKey(p) ? findBest(totalUnderMap.get(p)) : null;
                if (over != null || under != null) {
                    sb.append(String.format("  * Тотал %.1f: ", p));
                    if (over != null) sb.append(String.format("ТБ = %.3f (%s) ", over.value(), over.bookmaker()));
                    if (under != null) sb.append(String.format("ТМ = %.3f (%s)", under.value(), under.bookmaker()));
                    sb.append("\n");
                }
            }
        }

        // Handicaps (select key lines)
        if (!handicap1Map.isEmpty() || !handicap2Map.isEmpty()) {
            sb.append("- Форы матча:\n");
            Set<Double> allH1 = new TreeSet<>(handicap1Map.keySet());
            for (Double p : allH1) {
                OddQuote h1 = findBest(handicap1Map.get(p));
                Double opp = -p;
                OddQuote h2 = handicap2Map.containsKey(opp) ? findBest(handicap2Map.get(opp)) : null;
                if (h1 != null) {
                    sb.append(String.format("  * Ф1(%.1f) = %.3f (%s)", p, h1.value(), h1.bookmaker()));
                    if (h2 != null) {
                        sb.append(String.format(" | Ф2(%.1f) = %.3f (%s)", opp, h2.value(), h2.bookmaker()));
                    }
                    sb.append("\n");
                }
            }
        }

        return new OddsConsensusDto(
                bookmakers.size(),
                bookmakers,
                bestWin1 != null ? BigDecimal.valueOf(bestWin1.value()).setScale(3, RoundingMode.HALF_UP) : null,
                avgWin1,
                bestWin1 != null ? bestWin1.bookmaker() : null,
                bestDraw != null ? BigDecimal.valueOf(bestDraw.value()).setScale(3, RoundingMode.HALF_UP) : null,
                avgDraw,
                bestDraw != null ? bestDraw.bookmaker() : null,
                bestWin2 != null ? BigDecimal.valueOf(bestWin2.value()).setScale(3, RoundingMode.HALF_UP) : null,
                avgWin2,
                bestWin2 != null ? bestWin2.bookmaker() : null,
                bestTo25 != null ? BigDecimal.valueOf(bestTo25.value()).setScale(3, RoundingMode.HALF_UP) : null,
                avgTo25,
                bestTu25 != null ? BigDecimal.valueOf(bestTu25.value()).setScale(3, RoundingMode.HALF_UP) : null,
                avgTu25,
                sb.toString()
        );
    }

    public record MarketQuote(double value, String bookmaker) {}

    /**
     * Resolves the best live market odds and bookmaker for a specific candidate market.
     * Used in Stage 2 of the Zero-Odds Protocol to match model predictions against real bookmaker lines.
     */
    public MarketQuote findBestMarketQuote(Long matchId, String targetMarket) {
        if (matchId == null || targetMarket == null) return null;

        Map<Object, Object> rawEntries;
        try {
            rawEntries = redisTemplate.opsForHash().entries(ODDS_MATCH_PREFIX + matchId);
        } catch (Exception e) {
            log.error("Failed to read Redis odds for matchId={}: {}", matchId, e.getMessage());
            return null;
        }

        if (rawEntries == null || rawEntries.isEmpty()) {
            return null;
        }

        List<OddQuote> matchingQuotes = new ArrayList<>();
        List<OddQuote> win1Quotes = new ArrayList<>();
        List<OddQuote> drawQuotes = new ArrayList<>();
        List<OddQuote> win2Quotes = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
            if (!(entry.getValue() instanceof String json)) continue;
            try {
                JsonNode node = objectMapper.readTree(json);
                if (node.path("isSuspended").asBoolean(false)) continue;
                double value = node.path("value").asDouble(0.0);
                if (value <= 1.01 || value > 250.0) continue;

                String bmName = node.path("betSourceName").asText("");
                if (bmName.isBlank()) bmName = node.path("bookmaker").asText("Bookmaker");

                String typeCode = node.path("typeCode").asText("").toUpperCase();
                Double param = node.has("param") && !node.get("param").isNull() ? node.path("param").asDouble() : null;

                OddQuote quote = new OddQuote(value, bmName);

                if ("WIN1".equals(typeCode) || "P1".equals(typeCode) || "HOME_WIN".equals(typeCode)) win1Quotes.add(quote);
                if ("DRAW".equals(typeCode) || "X".equals(typeCode)) drawQuotes.add(quote);
                if ("WIN2".equals(typeCode) || "P2".equals(typeCode) || "AWAY_WIN".equals(typeCode)) win2Quotes.add(quote);

                if (isMarketMatch(targetMarket, typeCode, param)) {
                    matchingQuotes.add(quote);
                }
            } catch (Exception e) {
                log.debug("Failed to parse quote for matchId={}, market={}: {}", matchId, targetMarket, e.getMessage());
            }
        }

        if (!matchingQuotes.isEmpty()) {
            OddQuote best = findBest(matchingQuotes);
            if (best != null) {
                return new MarketQuote(best.value(), best.bookmaker());
            }
        }

        // Synthetic Double Chance derivation if direct 1X/X2 quote is missing in bookmaker lines
        if ("DOUBLE_CHANCE_1X".equalsIgnoreCase(targetMarket) || "DC_1X".equalsIgnoreCase(targetMarket) || "1X".equalsIgnoreCase(targetMarket)) {
            OddQuote bestW1 = findBest(win1Quotes);
            OddQuote bestX = findBest(drawQuotes);
            if (bestW1 != null && bestX != null) {
                double synthetic = (1.0 / ((1.0 / bestW1.value()) + (1.0 / bestX.value()))) * 0.95;
                return new MarketQuote(Math.round(synthetic * 100.0) / 100.0, bestW1.bookmaker());
            }
        } else if ("DOUBLE_CHANCE_X2".equalsIgnoreCase(targetMarket) || "DC_X2".equalsIgnoreCase(targetMarket) || "X2".equalsIgnoreCase(targetMarket)) {
            OddQuote bestW2 = findBest(win2Quotes);
            OddQuote bestX = findBest(drawQuotes);
            if (bestW2 != null && bestX != null) {
                double synthetic = (1.0 / ((1.0 / bestW2.value()) + (1.0 / bestX.value()))) * 0.95;
                return new MarketQuote(Math.round(synthetic * 100.0) / 100.0, bestW2.bookmaker());
            }
        }

        return null;
    }

    private boolean isMarketMatch(String targetMarket, String typeCode, Double param) {
        String tm = targetMarket.toUpperCase();
        if (("P1".equals(tm) || "HOME_WIN".equals(tm)) && ("WIN1".equals(typeCode) || "P1".equals(typeCode) || "HOME_WIN".equals(typeCode) || "WIN1_2WAY".equals(typeCode))) return true;
        if (("P2".equals(tm) || "AWAY_WIN".equals(tm)) && ("WIN2".equals(typeCode) || "P2".equals(typeCode) || "AWAY_WIN".equals(typeCode) || "WIN2_2WAY".equals(typeCode))) return true;
        if (("PX".equals(tm) || "DRAW".equals(tm) || "X".equals(tm)) && ("DRAW".equals(typeCode) || "X".equals(typeCode))) return true;
        if (("DOUBLE_CHANCE_1X".equals(tm) || "1X".equals(tm) || "DC_1X".equals(tm)) && ("DC_1X".equals(typeCode) || "DOUBLE_CHANCE_1X".equals(typeCode))) return true;
        if (("DOUBLE_CHANCE_X2".equals(tm) || "X2".equals(tm) || "DC_X2".equals(tm)) && ("DC_X2".equals(typeCode) || "DOUBLE_CHANCE_X2".equals(typeCode))) return true;
        if ((tm.startsWith("TOTAL_OVER") || tm.startsWith("TO_")) && ("TOTAL_OVER".equals(typeCode) || "TO".equals(typeCode) || "ASIAN_TOTAL_OVER".equals(typeCode))) {
            Double threshold = parseTotalThreshold(tm);
            if (threshold != null && param != null && Math.abs(param - threshold) < 0.1) return true;
            if (threshold == null && param != null && Math.abs(param - 2.5) < 0.1) return true;
        }
        if ((tm.startsWith("TOTAL_UNDER") || tm.startsWith("TU_")) && ("TOTAL_UNDER".equals(typeCode) || "TU".equals(typeCode) || "ASIAN_TOTAL_UNDER".equals(typeCode))) {
            Double threshold = parseTotalThreshold(tm);
            if (threshold != null && param != null && Math.abs(param - threshold) < 0.1) return true;
            if (threshold == null && param != null && Math.abs(param - 2.5) < 0.1) return true;
        }
        if ("AH_HOME_0".equals(tm) && ("HANDICAP_1".equals(typeCode) || "H1".equals(typeCode) || "ASIAN_HANDICAP_1".equals(typeCode)) && param != null && Math.abs(param) < 0.1) return true;
        if ("AH_AWAY_0".equals(tm) && ("HANDICAP_2".equals(typeCode) || "H2".equals(typeCode) || "ASIAN_HANDICAP_2".equals(typeCode)) && param != null && Math.abs(param) < 0.1) return true;
        if ("AH_HOME_PLUS_1.5".equals(tm) && ("HANDICAP_1".equals(typeCode) || "H1".equals(typeCode) || "ASIAN_HANDICAP_1".equals(typeCode)) && param != null && Math.abs(param - 1.5) < 0.1) return true;
        if ("AH_AWAY_PLUS_1.5".equals(tm) && ("HANDICAP_2".equals(typeCode) || "H2".equals(typeCode) || "ASIAN_HANDICAP_2".equals(typeCode)) && param != null && Math.abs(param - 1.5) < 0.1) return true;
        if ("AH_HOME_MINUS_1.0".equals(tm) && ("HANDICAP_1".equals(typeCode) || "H1".equals(typeCode) || "ASIAN_HANDICAP_1".equals(typeCode)) && param != null && Math.abs(param + 1.0) < 0.1) return true;
        if ("AH_AWAY_MINUS_1.0".equals(tm) && ("HANDICAP_2".equals(typeCode) || "H2".equals(typeCode) || "ASIAN_HANDICAP_2".equals(typeCode)) && param != null && Math.abs(param + 1.0) < 0.1) return true;
        if ("AH_HOME_MINUS_1.5".equals(tm) && ("HANDICAP_1".equals(typeCode) || "H1".equals(typeCode) || "ASIAN_HANDICAP_1".equals(typeCode)) && param != null && Math.abs(param + 1.5) < 0.1) return true;
        if ("AH_AWAY_MINUS_1.5".equals(tm) && ("HANDICAP_2".equals(typeCode) || "H2".equals(typeCode) || "ASIAN_HANDICAP_2".equals(typeCode)) && param != null && Math.abs(param + 1.5) < 0.1) return true;
        return false;
    }

    private Double parseTotalThreshold(String tm) {
        try {
            String numStr = tm.replaceFirst("^(TOTAL_OVER_|TOTAL_UNDER_|TO_|TU_)", "").trim();
            return Double.parseDouble(numStr);
        } catch (Exception e) {
            return null;
        }
    }

    private OddQuote findBest(List<OddQuote> quotes) {
        if (quotes == null || quotes.isEmpty()) return null;
        return quotes.stream().max(Comparator.comparingDouble(OddQuote::value)).orElse(null);
    }

    private BigDecimal calcAvg(List<OddQuote> quotes) {
        if (quotes == null || quotes.isEmpty()) return null;
        double sum = 0;
        int count = 0;
        for (OddQuote q : quotes) {
            sum += q.value();
            count++;
        }
        if (count == 0) return null;
        return BigDecimal.valueOf(sum / count).setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * Returns all bookmaker IDs that currently offer lines for this match in Redis.
     */
    public Set<String> getAvailableBookmakers(Long matchId) {
        if (matchId == null) return Collections.emptySet();
        Set<String> result = new TreeSet<>();
        try {
            Set<Object> keys = redisTemplate.opsForHash().keys("match:bm_updated:" + matchId);
            if (keys != null) {
                for (Object k : keys) {
                    if (k != null) addBookmakerNormalized(result, k.toString());
                }
            }
        } catch (Exception e) {
            log.debug("Error reading match:bm_updated for matchId={}: {}", matchId, e.getMessage());
        }

        try {
            Set<String> bms = redisTemplate.opsForSet().members("match:bms:" + matchId);
            if (bms != null) {
                for (String b : bms) {
                    addBookmakerNormalized(result, b);
                }
            }
        } catch (Exception e) {
            log.debug("Error reading match:bms for matchId={}: {}", matchId, e.getMessage());
        }

        try {
            Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(ODDS_MATCH_PREFIX + matchId);
            if (rawEntries != null) {
                for (Object val : rawEntries.values()) {
                    if (val instanceof String json) {
                        try {
                            JsonNode node = objectMapper.readTree(json);
                            String bm = node.path("betSourceId").asText(node.path("bookmaker").asText(""));
                            addBookmakerNormalized(result, bm);
                        } catch (Exception e) {
                            log.debug("Failed to parse bookmaker from odd JSON for matchId={}: {}", matchId, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error reading odds:match for matchId={}: {}", matchId, e.getMessage());
        }
        return result;
    }

    private void addBookmakerNormalized(Set<String> result, String raw) {
        if (raw == null || raw.isBlank()) return;
        String clean = raw.toLowerCase().trim();
        result.add(clean);
        if (clean.equals("1xbet") || clean.equals("1x-bet") || clean.startsWith("melbet")) {
            result.add("melbet");
            result.add("melbet-com");
        }
        if (clean.equals("fonbet") || clean.startsWith("fon-bet")) {
            result.add("fonbet");
            result.add("fon-bet-ru");
        }
    }

    /**
     * Extracts direct match/event URLs for all bookmakers offering odds for this match.
     */
    public Map<String, String> getBookmakerUrls(Long matchId, String sportSlug) {
        if (matchId == null) return Collections.emptyMap();
        Map<String, String> urls = new LinkedHashMap<>();
        try {
            // 1. Try dedicated match:bm_urls hash first
            String bmUrlsKey = "match:bm_urls:" + matchId;
            Map<Object, Object> bmEntries = redisTemplate.opsForHash().entries(bmUrlsKey);
            if (bmEntries != null && !bmEntries.isEmpty()) {
                for (Map.Entry<Object, Object> entry : bmEntries.entrySet()) {
                    String bm = String.valueOf(entry.getKey()).toLowerCase().trim();
                    if (bm.equals("1xbet") || bm.equals("1x-bet")) bm = "melbet";
                    String u = String.valueOf(entry.getValue()).trim();
                    if (!u.isBlank() && !urls.containsKey(bm)) {
                        String clean = sanitizeBookmakerEventUrl(bm, u, null);
                        if (clean != null && !clean.isBlank()) {
                            urls.put(bm, clean);
                            if (bm.startsWith("melbet")) {
                                urls.putIfAbsent("melbet", clean);
                                urls.putIfAbsent("melbet-com", clean);
                            }
                        }
                    }
                }
            }

            // 2. Fall back to odds:match hash for any missing bookmakers
            Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(ODDS_MATCH_PREFIX + matchId);
            if (rawEntries != null) {
                for (Object val : rawEntries.values()) {
                    if (val instanceof String json) {
                        try {
                            JsonNode node = objectMapper.readTree(json);
                            String bm = node.path("betSourceId").asText(node.path("bookmaker").asText("")).toLowerCase().trim();
                            if (bm.equals("1xbet") || bm.equals("1x-bet")) bm = "melbet";
                            if (bm.isBlank() || urls.containsKey(bm)) continue;

                            String eventUrl = node.path("eventUrl").asText("");
                            String extId = node.path("externalEventId").asText("");
                            String clean = sanitizeBookmakerEventUrl(bm, eventUrl, extId);
                            if (clean != null && !clean.isBlank()) {
                                urls.put(bm, clean);
                            }
                        } catch (Exception e) {
                            log.debug("Failed to parse event URL from odd JSON for matchId={}: {}", matchId, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error reading bookmaker URLs for matchId={}: {}", matchId, e.getMessage());
        }
        return urls;
    }

    private String sanitizeBookmakerEventUrl(String bm, String rawUrl, String extId) {
        if (rawUrl != null && !rawUrl.isBlank()) {
            rawUrl = rawUrl.trim().replaceAll("(?i)https?://[^/]*1x-?bet\\.com", "https://melbet.ru");
        }
        if (bm.contains("fon") || bm.contains("pari") || (rawUrl != null && (rawUrl.contains("fon.bet") || rawUrl.contains("fonbet") || rawUrl.contains("pari.ru")))) {
            return sanitizeFonbetFamilyUrl(bm, rawUrl != null ? rawUrl : (extId != null ? "https://fon.bet/sports/event/" + extId : null));
        }
        if (bm.contains("leon") || (rawUrl != null && rawUrl.contains("leon.ru"))) {
            return sanitizeLeonUrl(rawUrl, extId);
        }
        if (rawUrl != null && !rawUrl.isBlank()) {
            return rawUrl;
        }
        return extId;
    }

    private String sanitizeLeonUrl(String rawUrl, String extId) {
        String targetId = (extId != null && extId.matches("\\d{15,}")) ? extId.trim() : null;
        if (rawUrl != null && !rawUrl.isBlank()) {
            rawUrl = rawUrl.trim();
            if (rawUrl.matches("(?i)^https?://(?:www\\.)?leon\\.ru(?:/(?:bets|events)?)?/?$")) {
                return targetId != null ? "https://leon.ru/event/" + targetId : null;
            }
            if (targetId == null) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{15,})").matcher(rawUrl);
                while (m.find()) {
                    targetId = m.group(1);
                }
            }
            if (targetId != null) {
                return "https://leon.ru/event/" + targetId;
            }
            return rawUrl.replaceAll("(?i)/bets/football/", "/bets/soccer/")
                    .replaceAll("(?i)/bets/ice-hockey/", "/bets/hockey/")
                    .replaceAll("(?i)/bets/tabletennis/", "/bets/table-tennis/")
                    .replaceAll("(?i)/bets/cyber-sport/", "/bets/esports/");
        }
        return targetId != null ? "https://leon.ru/event/" + targetId : null;
    }

    private String sanitizeFonbetFamilyUrl(String bm, String url) {
        if (url == null || url.isBlank()) return url;
        boolean isPari = bm.contains("pari") || url.contains("pari.ru");
        String host = "fon.bet";
        if (isPari) {
            host = "pari.ru";
        } else if (bm.contains("by") || url.contains("fonbet.by")) {
            host = "fonbet.by";
        } else if (bm.contains("kz") || url.contains("fonbet.kz")) {
            host = "fonbet.kz";
        }
        String section = url.contains("/live/") || url.contains("/live?") ? "live" : "sports";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("/(?:sports|live)/(?:event/)?(?:.*/)?(\\d{4,14})(?:[/?#]|$)").matcher(url);
        if (m.find()) {
            return String.format("https://%s/%s/event/%s", host, section, m.group(1));
        }
        java.util.regex.Matcher showObj = java.util.regex.Pattern.compile("[?&]showObject=(\\d{4,14})").matcher(url);
        if (showObj.find()) {
            return String.format("https://%s/%s/event/%s", host, section, showObj.group(1));
        }
        return url;
    }
}