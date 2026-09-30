package pro.datawiki.igaming.source.sbobet.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(60)
public class SbobetCorrectScoreHandler extends AbstractSbobetMarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)\\s*[-:]\\s*(\\d+)");

    @Override
    public boolean supports(String marketKey) {
        if (marketKey == null) return false;
        String k = marketKey.toLowerCase();
        return (k.contains("correct_score") || k.contains("correctscore") || k.contains("exact_score") || k.contains("exactscore")) && !isStats(k);
    }

    @Override
    public boolean supports(String marketKey, SportType sportType) {
        return supports(marketKey);
    }

    @Override
    public void handle(JsonNode marketNode, List<OddItem> items) {
        if (marketNode == null) return;
        boolean isHalf1 = marketNode.has("_isHalf1") || marketNode.path("isHalf1").asBoolean(false);
        BetScope scope = isHalf1 ? BetScope.HALF_1 : BetScope.FULL_MATCH;
        String groupName = isHalf1 ? "correct_score_half_1" : "correct_score";

        if (marketNode.isObject()) {
            marketNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                double val = entry.getValue().asDouble(0.0);
                parseAndAddScore(items, groupName, scope, key, val);
            });
        } else if (marketNode.isArray()) {
            for (JsonNode itemNode : marketNode) {
                String score = itemNode.path("score").asText(itemNode.path("name").asText());
                double val = itemNode.path("odds").asDouble(itemNode.path("value").asDouble(0.0));
                parseAndAddScore(items, groupName, scope, score, val);
            }
        }
    }

    private void parseAndAddScore(List<OddItem> items, String groupName, BetScope scope, String scoreStr, double val) {
        Matcher matcher = SCORE_PATTERN.matcher(scoreStr);
        if (matcher.find()) {
            try {
                int s1 = Integer.parseInt(matcher.group(1));
                int s2 = Integer.parseInt(matcher.group(2));
                addOddItem(items, groupName, s1 + "-" + s2, val,
                        new CorrectScoreBet(scope, s1, s2, false));
            } catch (NumberFormatException ignored) {}
        } else if (scoreStr.toUpperCase().contains("OTHER") || scoreStr.toUpperCase().contains("ANY")) {
            addOddItem(items, groupName, "ANY_OTHER", val,
                    new CorrectScoreBet(scope, -1, -1, true));
        }
    }
}
