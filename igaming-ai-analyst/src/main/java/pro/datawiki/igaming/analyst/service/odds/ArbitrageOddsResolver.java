package pro.datawiki.igaming.analyst.service.odds;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Arbitrage Odds Resolver.
 * <p>
 * Implements pure unconstrained maximum-quote resolution across bookmakers.
 * Designed strictly for cross-bookmaker arbitrage scanning, surebet detection,
 * and high-watermark comparison where capturing the single highest quote is required.
 * <p>
 * ISOLATED from the Institutional AI Analyst to prevent "Arbitrage Poisoning" of quantitative models.
 */
@Slf4j
@Component("arbitrageOddsResolver")
@RequiredArgsConstructor
public class ArbitrageOddsResolver implements MarketOddsResolver {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String ODDS_MATCH_PREFIX = "odds:match:";

    public record RawQuote(double value, String bookmaker) {}

    @Override
    public Optional<MarketQuoteDto> resolveQuote(Long matchId, String targetMarket) {
        if (matchId == null || targetMarket == null || targetMarket.isBlank()) {
            return Optional.empty();
        }

        Map<Object, Object> rawEntries;
        try {
            rawEntries = redisTemplate.opsForHash().entries(ODDS_MATCH_PREFIX + matchId);
        } catch (Exception e) {
            log.error("[ARB-RESOLVER] Failed to read Redis odds for matchId={}: {}", matchId, e.getMessage());
            return Optional.empty();
        }

        if (rawEntries == null || rawEntries.isEmpty()) {
            return Optional.empty();
        }

        List<RawQuote> quotes = new ArrayList<>();

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

                if (isMarketMatch(targetMarket, typeCode, param)) {
                    quotes.add(new RawQuote(value, bmName));
                }
            } catch (Exception e) {
                log.debug("[ARB-RESOLVER] Failed to parse quote for matchId={}, market={}: {}", matchId, targetMarket, e.getMessage());
            }
        }

        if (quotes.isEmpty()) {
            return Optional.empty();
        }

        // Pure maximum for arbitrage
        RawQuote maxQuote = quotes.stream()
                .max(Comparator.comparingDouble(RawQuote::value))
                .orElse(quotes.get(0));

        double sum = quotes.stream().mapToDouble(RawQuote::value).sum();
        double avg = sum / quotes.size();
        double divPct = ((maxQuote.value() - avg) / avg) * 100.0;

        return Optional.of(MarketQuoteDto.of(
                maxQuote.value(),
                maxQuote.bookmaker(),
                Math.round(avg * 1000.0) / 1000.0,
                Math.round(divPct * 10.0) / 10.0,
                quotes.size(),
                false,
                false,
                "ARBITRAGE_MAX"
        ));
    }

    private boolean isMarketMatch(String targetMarket, String typeCode, Double param) {
        if (targetMarket == null || typeCode == null) return false;
        pro.datawiki.igaming.dto.BetType targetBet = pro.datawiki.igaming.dto.BetTypeRegistry.fromCode(targetMarket);
        String tc = typeCode.toUpperCase().trim();

        if (targetBet instanceof pro.datawiki.igaming.dto.market.MatchResultBet mrb) {
            return switch (mrb.outcome()) {
                case WIN1, WIN1_2WAY -> "WIN1".equals(tc) || "P1".equals(tc) || "HOME_WIN".equals(tc) || "WIN1_2WAY".equals(tc);
                case WIN2, WIN2_2WAY -> "WIN2".equals(tc) || "P2".equals(tc) || "AWAY_WIN".equals(tc) || "WIN2_2WAY".equals(tc);
                case DRAW -> "DRAW".equals(tc) || "X".equals(tc) || "PX".equals(tc);
                case DC_1X -> "DC_1X".equals(tc) || "DOUBLE_CHANCE_1X".equals(tc) || "1X".equals(tc);
                case DC_X2 -> "DC_X2".equals(tc) || "DOUBLE_CHANCE_X2".equals(tc) || "X2".equals(tc);
                case DC_12 -> "DC_12".equals(tc) || "DOUBLE_CHANCE_12".equals(tc) || "12".equals(tc);
            };
        }

        if (targetBet instanceof pro.datawiki.igaming.dto.market.TotalBet tb) {
            Double threshold = tb.param();
            if (param == null) return false;
            if (Math.abs(param - threshold) >= 0.05) return false;

            boolean isOver = tb.direction() == pro.datawiki.igaming.dto.market.TotalBet.Direction.OVER;
            return isOver
                    ? ("TOTAL_OVER".equals(tc) || "TO".equals(tc) || "ASIAN_TOTAL_OVER".equals(tc))
                    : ("TOTAL_UNDER".equals(tc) || "TU".equals(tc) || "ASIAN_TOTAL_UNDER".equals(tc));
        }

        if (targetBet instanceof pro.datawiki.igaming.dto.market.HandicapBet hb) {
            Double line = hb.param();
            if (param == null) return false;
            if (Math.abs(param - line) >= 0.05) return false;

            return switch (hb.outcome()) {
                case TEAM1 -> "HANDICAP_1".equals(tc) || "H1".equals(tc) || "ASIAN_HANDICAP_1".equals(tc);
                case TEAM2 -> "HANDICAP_2".equals(tc) || "H2".equals(tc) || "ASIAN_HANDICAP_2".equals(tc);
                case DRAW -> "HANDICAP_DRAW".equals(tc) || "HDRAW".equals(tc);
            };
        }

        if (targetBet instanceof pro.datawiki.igaming.dto.market.BinaryMarketBet bmb) {
            if (bmb.marketType() == pro.datawiki.igaming.dto.market.BinaryMarketBet.MarketType.BTTS) {
                return bmb.outcome() == pro.datawiki.igaming.dto.market.BinaryMarketBet.Outcome.YES
                        ? ("BTTS_YES".equals(tc) || "BOTH_TEAMS_SCORE_YES".equals(tc))
                        : ("BTTS_NO".equals(tc) || "BOTH_TEAMS_SCORE_NO".equals(tc));
            }
        }

        return false;
    }
}
