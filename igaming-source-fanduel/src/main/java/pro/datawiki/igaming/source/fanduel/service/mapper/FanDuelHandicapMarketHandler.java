package pro.datawiki.igaming.source.fanduel.service.mapper;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.HandicapBet;

@Component
@Order(30)
public class FanDuelHandicapMarketHandler implements FanDuelMarketHandler {

    @Override
    public boolean supports(FanDuelMarketContext ctx) {
        String mUpper = ctx.getMarketName().toUpperCase();
        return mUpper.contains("SPREAD")
                || mUpper.contains("HANDICAP")
                || mUpper.contains("PUCK LINE")
                || mUpper.contains("RUN LINE")
                || mUpper.contains("HCAP");
    }

    @Override
    public BetType map(FanDuelMarketContext ctx) {
        String runner = ctx.getRunnerName() != null ? ctx.getRunnerName() : "";
        String rUpper = runner.toUpperCase();

        String team1 = ctx.getTeam1();
        String team2 = ctx.getTeam2();

        HandicapBet.Outcome outcome = null;
        if (isTeam1(rUpper, team1)) {
            outcome = HandicapBet.Outcome.TEAM1;
        } else if (isTeam2(rUpper, team2)) {
            outcome = HandicapBet.Outcome.TEAM2;
        } else if (rUpper.contains("DRAW") || rUpper.contains("TIE")) {
            outcome = HandicapBet.Outcome.DRAW;
        }

        if (outcome == null) return null;

        Double line = ctx.getLine();
        if (line == null) line = 0.0;

        boolean isAsian = isQuarterAsian(line);

        return new HandicapBet(ctx.getScope(), outcome, line, isAsian, ctx.getStatType());
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

    private boolean isQuarterAsian(Double line) {
        if (line == null) return false;
        double rem = Math.abs(line) % 1.0;
        return Math.abs(rem - 0.25) < 0.001 || Math.abs(rem - 0.75) < 0.001;
    }
}
