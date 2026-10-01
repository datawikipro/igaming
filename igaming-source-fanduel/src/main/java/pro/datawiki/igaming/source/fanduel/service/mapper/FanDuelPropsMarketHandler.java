package pro.datawiki.igaming.source.fanduel.service.mapper;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.BinaryMarketBet;
import pro.datawiki.igaming.dto.market.CorrectScoreBet;
import pro.datawiki.igaming.dto.market.StatType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(40)
public class FanDuelPropsMarketHandler implements FanDuelMarketHandler {

    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+)\\s*[-:]\\s*(\\d+)");

    @Override
    public boolean supports(FanDuelMarketContext ctx) {
        String mUpper = ctx.getMarketName().toUpperCase();
        return mUpper.contains("BOTH TEAMS TO SCORE")
                || mUpper.contains("BOTH TEAMS WILL SCORE")
                || mUpper.contains("BTTS")
                || mUpper.contains("GOAL / NO GOAL")
                || mUpper.contains("ODD/EVEN")
                || mUpper.contains("ODD OR EVEN")
                || mUpper.contains("CORRECT SCORE");
    }

    @Override
    public BetType map(FanDuelMarketContext ctx) {
        String mUpper = ctx.getMarketName().toUpperCase();
        String runner = ctx.getRunnerName() != null ? ctx.getRunnerName() : "";
        String rUpper = runner.toUpperCase();

        // 1. Both Teams to Score
        if (mUpper.contains("BOTH TEAMS TO SCORE") || mUpper.contains("BOTH TEAMS WILL SCORE")
                || mUpper.contains("BTTS") || mUpper.contains("GOAL / NO GOAL")) {
            if ("YES".equals(rUpper) || rUpper.startsWith("YES") || "GG".equals(rUpper)) {
                return new BinaryMarketBet(ctx.getScope(), BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.YES, StatType.MATCH);
            } else if ("NO".equals(rUpper) || rUpper.startsWith("NO") || "NG".equals(rUpper)) {
                return new BinaryMarketBet(ctx.getScope(), BetSubject.MATCH, BinaryMarketBet.MarketType.BTTS, BinaryMarketBet.Outcome.NO, StatType.MATCH);
            }
        }

        // 2. Odd / Even
        if (mUpper.contains("ODD/EVEN") || mUpper.contains("ODD OR EVEN")) {
            if (rUpper.contains("ODD")) {
                return new BinaryMarketBet(ctx.getScope(), BetSubject.MATCH, BinaryMarketBet.MarketType.ODD_EVEN, BinaryMarketBet.Outcome.ODD, StatType.MATCH);
            } else if (rUpper.contains("EVEN")) {
                return new BinaryMarketBet(ctx.getScope(), BetSubject.MATCH, BinaryMarketBet.MarketType.ODD_EVEN, BinaryMarketBet.Outcome.EVEN, StatType.MATCH);
            }
        }

        // 3. Correct Score
        if (mUpper.contains("CORRECT SCORE")) {
            Matcher matcher = SCORE_PATTERN.matcher(runner);
            if (matcher.find()) {
                int s1 = Integer.parseInt(matcher.group(1));
                int s2 = Integer.parseInt(matcher.group(2));
                return new CorrectScoreBet(ctx.getScope(), s1, s2, false);
            } else if (rUpper.contains("OTHER") || rUpper.contains("ANY OTHER")) {
                return new CorrectScoreBet(ctx.getScope(), 99, 99, true);
            }
        }

        return null;
    }
}
