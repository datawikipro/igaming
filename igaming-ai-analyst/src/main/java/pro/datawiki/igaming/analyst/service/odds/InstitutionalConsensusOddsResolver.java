package pro.datawiki.igaming.analyst.service.odds;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Institutional Consensus Odds Resolver.
 * <p>
 * Eliminates "Arbitrage Poisoning" / "Winner's Curse" in the AI Analyst by strictly pricing
 * recommendations on true market consensus rather than raw max() across bookmakers.
 * <p>
 * Key Invariants:
 * 1. Sharp Anchor: Pinnacle / Bet365 / Betfair quotes serve as gold standard market anchor.
 * 2. Robust Median Consensus: Used when sharp books are absent.
 * 3. Outlier Filter: Quotes exceeding +12% above consensus are purged as palpable/stale errors.
 * 4. Quorum Rule: Single soft-bookmaker quotes (without sharp anchor) are vetoed.
 * 5. Price Improvement Cap: Executable odd is capped at consensus * 1.06.
 */
@Slf4j
@Service("institutionalConsensusOddsResolver")
@Primary
@RequiredArgsConstructor
public class InstitutionalConsensusOddsResolver implements MarketOddsResolver {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String ODDS_MATCH_PREFIX = "odds:match:";

    /**
     * Maximum allowed divergence above consensus (+12%). Quotes above this are treated as
     * palpable errors, mistranslated lines, or stale bookmaker quotes.
     */
    private static final double MAX_OUTLIER_DIVERGENCE_PCT = 12.0;

    /**
     * Maximum allowed execution price improvement over consensus (+6%).
     * Protects Kelly stake sizing and expected value from phantom line inflation.
     */
    private static final double PRICE_IMPROVEMENT_CAP_PCT = 6.0;

    /**
     * Tier-1 sharp bookmakers whose pricing sets market consensus.
     */
    private static final Set<String> SHARP_BOOKMAKERS = Set.of("pinnacle", "bet365", "betfair");

    public record BookmakerQuote(double value, String bookmaker, boolean isSharp) {}

    @Override
    public Optional<MarketQuoteDto> resolveQuote(Long matchId, String targetMarket) {
        if (matchId == null || targetMarket == null || targetMarket.isBlank()) {
            return Optional.empty();
        }

        Map<Object, Object> rawEntries;
        try {
            rawEntries = redisTemplate.opsForHash().entries(ODDS_MATCH_PREFIX + matchId);
        } catch (Exception e) {
            log.error("[CONSENSUS-RESOLVER] Failed to read Redis odds for matchId={}: {}", matchId, e.getMessage());
            return Optional.empty();
        }

        if (rawEntries == null || rawEntries.isEmpty()) {
            return Optional.empty();
        }

        List<BookmakerQuote> matchingQuotes = new ArrayList<>();
        List<BookmakerQuote> win1Quotes = new ArrayList<>();
        List<BookmakerQuote> drawQuotes = new ArrayList<>();
        List<BookmakerQuote> win2Quotes = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
            if (!(entry.getValue() instanceof String json)) continue;

            try {
                JsonNode node = objectMapper.readTree(json);
                if (node.path("isSuspended").asBoolean(false)
                        || node.path("isLive").asBoolean(false)
                        || node.path("live").asBoolean(false)
                        || node.path("eventUrl").asText("").contains("/live")) {
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
                boolean isSharp = isSharpBookmaker(bmName);

                String typeCode = node.path("typeCode").asText("").toUpperCase();
                Double param = node.has("param") && !node.get("param").isNull() ? node.path("param").asDouble() : null;

                BookmakerQuote quote = new BookmakerQuote(value, bmName, isSharp);

                pro.datawiki.igaming.dto.BetType quoteBet = parseQuoteBetType(typeCode, param);
                if (quoteBet instanceof pro.datawiki.igaming.dto.market.MatchResultBet mrb) {
                    if (mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.WIN1 || mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.WIN1_2WAY) {
                        win1Quotes.add(quote);
                    } else if (mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.DRAW) {
                        drawQuotes.add(quote);
                    } else if (mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.WIN2 || mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.WIN2_2WAY) {
                        win2Quotes.add(quote);
                    }
                }

                if (isMarketMatch(targetMarket, typeCode, param)) {
                    matchingQuotes.add(quote);
                }
            } catch (Exception e) {
                log.debug("[CONSENSUS-RESOLVER] Failed parsing quote for matchId={}, market={}: {}", matchId, targetMarket, e.getMessage());
            }
        }

        // 1. Direct market matching with sharp anchor & median consensus guard
        if (!matchingQuotes.isEmpty()) {
            Optional<MarketQuoteDto> resolved = resolveFromQuotes(matchId, targetMarket, matchingQuotes);
            if (resolved.isPresent()) {
                return resolved;
            }
        }

        // 2. Synthetic Double Chance derivation if direct 1X/X2 quote is absent in line
        if (isDoubleChance1x(targetMarket)) {
            return deriveSyntheticDoubleChance(matchId, "DOUBLE_CHANCE_1X", win1Quotes, drawQuotes);
        } else if (isDoubleChanceX2(targetMarket)) {
            return deriveSyntheticDoubleChance(matchId, "DOUBLE_CHANCE_X2", win2Quotes, drawQuotes);
        }

        return Optional.empty();
    }

    private Optional<MarketQuoteDto> resolveFromQuotes(Long matchId, String targetMarket, List<BookmakerQuote> quotes) {
        // Deduplicate quotes per bookmaker (take the most conservative/highest valid for that bookmaker)
        Map<String, BookmakerQuote> perBookmaker = new LinkedHashMap<>();
        for (BookmakerQuote q : quotes) {
            String bmKey = q.bookmaker().toLowerCase().trim();
            BookmakerQuote existing = perBookmaker.get(bmKey);
            if (existing == null || q.value() > existing.value()) {
                perBookmaker.put(bmKey, q);
            }
        }

        List<BookmakerQuote> uniqueQuotes = new ArrayList<>(perBookmaker.values());
        int quorumCount = uniqueQuotes.size();

        // Separate Sharp quotes vs Soft quotes
        List<BookmakerQuote> sharpQuotes = uniqueQuotes.stream()
                .filter(BookmakerQuote::isSharp)
                .toList();

        boolean hasSharpAnchor = !sharpQuotes.isEmpty();
        double consensus;
        String mode;

        if (hasSharpAnchor) {
            consensus = calculateMedian(sharpQuotes.stream().map(BookmakerQuote::value).toList());
            mode = "SHARP_ANCHORED";
        } else {
            // Quorum Rule: At least 2 independent soft bookmakers required!
            if (quorumCount < 2) {
                log.warn("[CONSENSUS-GUARD] VETO for matchId={}, market={}: Insufficient quorum ({} bookmaker, no sharp anchor). Line cannot be verified.",
                        matchId, targetMarket, quorumCount);
                return Optional.empty();
            }
            consensus = calculateMedian(uniqueQuotes.stream().map(BookmakerQuote::value).toList());
            mode = "MEDIAN_CONSENSUS";
        }

        if (consensus <= 1.01) {
            return Optional.empty();
        }

        // Outlier filtering: discard any quote exceeding +12% from consensus or suspiciously low (< -30%)
        double maxAllowedOdd = consensus * (1.0 + (MAX_OUTLIER_DIVERGENCE_PCT / 100.0));
        double minAllowedOdd = consensus * 0.70;

        List<BookmakerQuote> validQuotes = new ArrayList<>();
        boolean outlierDetected = false;

        for (BookmakerQuote q : uniqueQuotes) {
            if (q.value() > maxAllowedOdd) {
                outlierDetected = true;
                double divPct = ((q.value() - consensus) / consensus) * 100.0;
                log.warn("[CONSENSUS-GUARD] Discarded OUTLIER quote: matchId={}, market={}, bm={}, odd={}, consensus={}, divergence=+{}%",
                        matchId, targetMarket, q.bookmaker(), q.value(), consensus, String.format(Locale.US, "%.1f", divPct));
            } else if (q.value() < minAllowedOdd) {
                outlierDetected = true;
                log.warn("[CONSENSUS-GUARD] Discarded CORRUPTED low quote: matchId={}, market={}, bm={}, odd={}, consensus={}",
                        matchId, targetMarket, q.bookmaker(), q.value(), consensus);
            } else {
                validQuotes.add(q);
            }
        }

        if (validQuotes.isEmpty()) {
            log.warn("[CONSENSUS-GUARD] All quotes discarded as outliers for matchId={}, market={}", matchId, targetMarket);
            return Optional.empty();
        }

        // Pick best quote among valid quotes
        BookmakerQuote bestValid = validQuotes.stream()
                .max(Comparator.comparingDouble(BookmakerQuote::value))
                .orElse(validQuotes.get(0));

        // Cap execution price at consensus * 1.06 (allow up to +6% price improvement over consensus)
        double cappedOdd = Math.min(bestValid.value(), consensus * (1.0 + (PRICE_IMPROVEMENT_CAP_PCT / 100.0)));
        cappedOdd = Math.round(cappedOdd * 1000.0) / 1000.0;

        double finalDivergencePct = ((cappedOdd - consensus) / consensus) * 100.0;

        return Optional.of(MarketQuoteDto.of(
                cappedOdd,
                bestValid.bookmaker(),
                Math.round(consensus * 1000.0) / 1000.0,
                Math.round(finalDivergencePct * 10.0) / 10.0,
                quorumCount,
                hasSharpAnchor,
                outlierDetected,
                mode
        ));
    }

    private Optional<MarketQuoteDto> deriveSyntheticDoubleChance(
            Long matchId, String dcMarket, List<BookmakerQuote> leg1Quotes, List<BookmakerQuote> drawQuotes
    ) {
        if (leg1Quotes == null || leg1Quotes.isEmpty() || drawQuotes == null || drawQuotes.isEmpty()) {
            return Optional.empty();
        }

        double medLeg1 = calculateMedian(leg1Quotes.stream().map(BookmakerQuote::value).toList());
        double medDraw = calculateMedian(drawQuotes.stream().map(BookmakerQuote::value).toList());

        if (medLeg1 <= 1.01 || medDraw <= 1.01) {
            return Optional.empty();
        }

        // Derive clean synthetic price without outlier multiplier
        double synthetic = (1.0 / ((1.0 / medLeg1) + (1.0 / medDraw))) * 0.95;
        double rounded = Math.round(synthetic * 100.0) / 100.0;

        if (rounded <= 1.01) {
            return Optional.empty();
        }

        boolean sharpLeg = leg1Quotes.stream().anyMatch(BookmakerQuote::isSharp) || drawQuotes.stream().anyMatch(BookmakerQuote::isSharp);
        int totalQuorum = Math.min(leg1Quotes.size(), drawQuotes.size());

        return Optional.of(MarketQuoteDto.of(
                rounded,
                "Synthetic Consensus",
                rounded,
                0.0,
                totalQuorum,
                sharpLeg,
                false,
                "SYNTHETIC_CONSENSUS"
        ));
    }

    private boolean isSharpBookmaker(String bmName) {
        if (bmName == null) return false;
        String lower = bmName.toLowerCase().trim();
        for (String sharp : SHARP_BOOKMAKERS) {
            if (lower.contains(sharp)) return true;
        }
        return false;
    }

    private double calculateMedian(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        List<Double> sorted = values.stream().filter(v -> v != null && v > 1.01).sorted().toList();
        if (sorted.isEmpty()) return 0.0;

        int size = sorted.size();
        if (size % 2 == 1) {
            return sorted.get(size / 2);
        } else {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        }
    }

    private boolean isDoubleChance1x(String m) {
        if (m == null || m.isBlank()) return false;
        pro.datawiki.igaming.dto.BetType bet = pro.datawiki.igaming.dto.BetTypeRegistry.fromCode(m);
        return bet instanceof pro.datawiki.igaming.dto.market.MatchResultBet mrb && mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.DC_1X;
    }

    private boolean isDoubleChanceX2(String m) {
        if (m == null || m.isBlank()) return false;
        pro.datawiki.igaming.dto.BetType bet = pro.datawiki.igaming.dto.BetTypeRegistry.fromCode(m);
        return bet instanceof pro.datawiki.igaming.dto.market.MatchResultBet mrb && mrb.outcome() == pro.datawiki.igaming.dto.market.MatchResultBet.Outcome.DC_X2;
    }

    private pro.datawiki.igaming.dto.BetType parseQuoteBetType(String typeCode, Double param) {
        if (typeCode == null || typeCode.isBlank()) return null;
        String tc = typeCode.toUpperCase().trim();
        if (param != null && !tc.contains(String.valueOf(param))) {
            return pro.datawiki.igaming.dto.BetTypeRegistry.fromCode(tc + "_" + param);
        }
        return pro.datawiki.igaming.dto.BetTypeRegistry.fromCode(tc);
    }

    private boolean isMarketMatch(String targetMarket, String typeCode, Double param) {
        if (targetMarket == null || typeCode == null) return false;
        pro.datawiki.igaming.dto.BetType targetBet = pro.datawiki.igaming.dto.BetTypeRegistry.fromCode(targetMarket);
        pro.datawiki.igaming.dto.BetType quoteBet = parseQuoteBetType(typeCode, param);
        if (targetBet == null || quoteBet == null) return false;

        if (targetBet instanceof pro.datawiki.igaming.dto.market.MatchResultBet tmrb && quoteBet instanceof pro.datawiki.igaming.dto.market.MatchResultBet qmrb) {
            return tmrb.outcome() == qmrb.outcome();
        }
        if (targetBet instanceof pro.datawiki.igaming.dto.market.TotalBet ttb && quoteBet instanceof pro.datawiki.igaming.dto.market.TotalBet qtb) {
            return ttb.direction() == qtb.direction() && (ttb.param() == null || qtb.param() == null || Math.abs(ttb.param() - qtb.param()) < 0.05);
        }
        if (targetBet instanceof pro.datawiki.igaming.dto.market.HandicapBet thb && quoteBet instanceof pro.datawiki.igaming.dto.market.HandicapBet qhb) {
            return thb.outcome() == qhb.outcome() && (thb.param() == null || qhb.param() == null || Math.abs(thb.param() - qhb.param()) < 0.05);
        }
        if (targetBet instanceof pro.datawiki.igaming.dto.market.BinaryMarketBet tbmb && quoteBet instanceof pro.datawiki.igaming.dto.market.BinaryMarketBet qbmb) {
            return tbmb.marketType() == qbmb.marketType() && tbmb.outcome() == qbmb.outcome();
        }
        return targetBet.equals(quoteBet);
    }
}
