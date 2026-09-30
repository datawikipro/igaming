package pro.datawiki.igaming.source.fanduel.service.mapper;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.TotalBet;

@Component
@Order(20)
public class FanDuelTotalMarketHandler implements FanDuelMarketHandler {

    @Override
    public boolean supports(FanDuelMarketContext ctx) {
        String mUpper = ctx.getMarketName().toUpperCase();
        return mUpper.contains("TOTAL")
                || mUpper.contains("OVER/UNDER")
                || mUpper.contains("O/U");
    }

    @Override
    public BetType map(FanDuelMarketContext ctx) {
        String runner = ctx.getRunnerName() != null ? ctx.getRunnerName() : "";
        String rUpper = runner.toUpperCase();

        TotalBet.Direction direction = null;
        if (rUpper.startsWith("OVER") || "O".equals(rUpper) || rUpper.startsWith("O ") || rUpper.contains("OVER")) {
            direction = TotalBet.Direction.OVER;
        } else if (rUpper.startsWith("UNDER") || "U".equals(rUpper) || rUpper.startsWith("U ") || rUpper.contains("UNDER")) {
            direction = TotalBet.Direction.UNDER;
        }

        if (direction == null) return null;

        Double line = ctx.getLine();
        if (line == null) line = 0.0;

        BetSubject subject = resolveSubject(ctx);
        boolean isAsian = isQuarterAsian(line);

        return new TotalBet(ctx.getScope(), subject, direction, line, isAsian, ctx.getStatType());
    }

    private BetSubject resolveSubject(FanDuelMarketContext ctx) {
        String mUpper = ctx.getMarketName().toUpperCase();
        String team1 = ctx.getTeam1();
        String team2 = ctx.getTeam2();

        boolean hasTeam1 = (team1 != null && !team1.isBlank() && mUpper.contains(team1.toUpperCase()))
                || mUpper.contains("HOME TEAM TOTAL") || mUpper.startsWith("HOME TOTAL") || mUpper.contains("TEAM 1 TOTAL");
        boolean hasTeam2 = (team2 != null && !team2.isBlank() && mUpper.contains(team2.toUpperCase()))
                || mUpper.contains("AWAY TEAM TOTAL") || mUpper.startsWith("AWAY TOTAL") || mUpper.contains("TEAM 2 TOTAL");

        if (hasTeam1 && !hasTeam2) return BetSubject.TEAM1;
        if (hasTeam2 && !hasTeam1) return BetSubject.TEAM2;

        return BetSubject.MATCH;
    }

    private boolean isQuarterAsian(Double line) {
        if (line == null) return false;
        double rem = Math.abs(line) % 1.0;
        return Math.abs(rem - 0.25) < 0.001 || Math.abs(rem - 0.75) < 0.001;
    }
}
