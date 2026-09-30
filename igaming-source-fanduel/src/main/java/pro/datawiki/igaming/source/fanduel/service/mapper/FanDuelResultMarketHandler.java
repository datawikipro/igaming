package pro.datawiki.igaming.source.fanduel.service.mapper;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.MatchResultBet;

@Component
@Order(10)
public class FanDuelResultMarketHandler implements FanDuelMarketHandler {

    @Override
    public boolean supports(FanDuelMarketContext ctx) {
        String mUpper = ctx.getMarketName().toUpperCase();
        return mUpper.contains("MONEYLINE")
                || mUpper.contains("MONEY LINE")
                || mUpper.contains("MATCH RESULT")
                || mUpper.contains("MATCH BETTING")
                || mUpper.contains("3-WAY")
                || mUpper.contains("2-WAY")
                || mUpper.contains("MATCH WINNER")
                || mUpper.contains("HEAD TO HEAD")
                || mUpper.contains("DRAW NO BET")
                || mUpper.contains("DOUBLE CHANCE")
                || mUpper.contains("WIN-DRAW-WIN")
                || mUpper.endsWith(" WINNER")
                || mUpper.equals("1X2")
                || mUpper.contains("1X2");
    }

    @Override
    public BetType map(FanDuelMarketContext ctx) {
        String mUpper = ctx.getMarketName().toUpperCase();
        String runner = ctx.getRunnerName() != null ? ctx.getRunnerName() : "";
        String rUpper = runner.toUpperCase();

        String team1 = ctx.getTeam1();
        String team2 = ctx.getTeam2();

        // 1. Double Chance
        if (mUpper.contains("DOUBLE CHANCE")) {
            if (is1X(rUpper, team1)) {
                return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.DC_1X, ctx.getStatType());
            } else if (is12(rUpper, team1, team2)) {
                return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.DC_12, ctx.getStatType());
            } else if (isX2(rUpper, team2)) {
                return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.DC_X2, ctx.getStatType());
            }
        }

        // 2. Draw No Bet (2-Way)
        if (mUpper.contains("DRAW NO BET")) {
            if (isTeam1(rUpper, team1)) {
                return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.WIN1_2WAY, ctx.getStatType());
            } else if (isTeam2(rUpper, team2)) {
                return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.WIN2_2WAY, ctx.getStatType());
            }
        }

        // 3. 3-Way Match Result / 1X2 / Soccer Moneyline
        boolean is3Way = mUpper.contains("3-WAY") || mUpper.contains("1X2") || mUpper.contains("WIN-DRAW-WIN")
                || (ctx.getSportType() == SportType.FOOTBALL && !mUpper.contains("2-WAY") && !mUpper.contains("DRAW NO BET"));

        if (isDraw(rUpper)) {
            return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.DRAW, ctx.getStatType());
        }

        if (isTeam1(rUpper, team1)) {
            MatchResultBet.Outcome outcome = is3Way ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
            return new MatchResultBet(ctx.getScope(), outcome, ctx.getStatType());
        } else if (isTeam2(rUpper, team2)) {
            MatchResultBet.Outcome outcome = is3Way ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
            return new MatchResultBet(ctx.getScope(), outcome, ctx.getStatType());
        }

        return null;
    }

    private boolean isTeam1(String rUpper, String team1) {
        if ("1".equals(rUpper) || "HOME".equals(rUpper)) return true;
        if (team1 != null && !team1.isBlank() && rUpper.equalsIgnoreCase(team1)) return true;
        if (team1 != null && !team1.isBlank() && rUpper.contains(team1.toUpperCase())) return true;
        return false;
    }

    private boolean isTeam2(String rUpper, String team2) {
        if ("2".equals(rUpper) || "AWAY".equals(rUpper)) return true;
        if (team2 != null && !team2.isBlank() && rUpper.equalsIgnoreCase(team2)) return true;
        if (team2 != null && !team2.isBlank() && rUpper.contains(team2.toUpperCase())) return true;
        return false;
    }

    private boolean isDraw(String rUpper) {
        return "X".equals(rUpper) || "DRAW".equals(rUpper) || "TIE".equals(rUpper) || rUpper.contains("DRAW") || rUpper.contains("TIE");
    }

    private boolean is1X(String rUpper, String team1) {
        if ("1X".equals(rUpper) || rUpper.contains("1X") || rUpper.contains("1 OR X")) return true;
        if (rUpper.contains("HOME OR DRAW") || rUpper.contains("DRAW OR HOME")) return true;
        if (team1 != null && rUpper.contains(team1.toUpperCase()) && rUpper.contains("DRAW")) return true;
        return false;
    }

    private boolean is12(String rUpper, String team1, String team2) {
        if ("12".equals(rUpper) || rUpper.contains("12") || rUpper.contains("1 OR 2")) return true;
        if (rUpper.contains("HOME OR AWAY") || rUpper.contains("AWAY OR HOME")) return true;
        if (team1 != null && team2 != null && rUpper.contains(team1.toUpperCase()) && rUpper.contains(team2.toUpperCase())) return true;
        return false;
    }

    private boolean isX2(String rUpper, String team2) {
        if ("X2".equals(rUpper) || rUpper.contains("X2") || rUpper.contains("X OR 2")) return true;
        if (rUpper.contains("DRAW OR AWAY") || rUpper.contains("AWAY OR DRAW")) return true;
        if (team2 != null && rUpper.contains(team2.toUpperCase()) && rUpper.contains("DRAW")) return true;
        return false;
    }
}
