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
