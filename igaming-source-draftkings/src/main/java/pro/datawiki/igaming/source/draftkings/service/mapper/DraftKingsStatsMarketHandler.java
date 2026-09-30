package pro.datawiki.igaming.source.draftkings.service.mapper;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;

@Component
@Order(5)
public class DraftKingsStatsMarketHandler implements DraftKingsMarketHandler {

    @Override
    public boolean supports(DraftKingsMarketContext ctx) {
        StatType st = ctx.getStatType();
        return st == StatType.CORNERS
                || st == StatType.YELLOW_CARDS
                || st == StatType.CARDS
                || st == StatType.FOULS
                || st == StatType.OFFSIDES
                || st == StatType.SHOTS_ON_TARGET
                || st == StatType.SHOTS_ON_GOAL;
    }

    @Override
    public BetType map(DraftKingsMarketContext ctx) {
        String mUpper = ctx.getMarketName().toUpperCase();
        String participant = ctx.getOutcome().getParticipant();
        String label = ctx.getOutcome().getLabel();
        String runner = participant != null ? participant : (label != null ? label : "");
        String rUpper = runner.toUpperCase();

        StatType st = ctx.getStatType();
        Double line = ctx.getLine() != null ? ctx.getLine() : 0.0;
        String team1 = ctx.getTeam1();
        String team2 = ctx.getTeam2();

        // 1. Stats Totals (Over / Under)
        if (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("O/U")) {
            TotalBet.Direction dir = null;
            if (rUpper.startsWith("OVER") || "O".equals(rUpper) || rUpper.contains("OVER")) {
                dir = TotalBet.Direction.OVER;
            } else if (rUpper.startsWith("UNDER") || "U".equals(rUpper) || rUpper.contains("UNDER")) {
                dir = TotalBet.Direction.UNDER;
            }

            if (dir != null) {
                BetSubject subject = resolveSubject(mUpper, team1, team2);
                boolean isAsian = isQuarterAsian(line);
                return new TotalBet(ctx.getScope(), subject, dir, line, isAsian, st);
            }
        }

        // 2. Stats Handicap / Spread
        if (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD") || mUpper.contains("HCAP")) {
            HandicapBet.Outcome outcome = null;
            if (isTeam1(rUpper, team1)) {
                outcome = HandicapBet.Outcome.TEAM1;
            } else if (isTeam2(rUpper, team2)) {
                outcome = HandicapBet.Outcome.TEAM2;
            } else if (rUpper.contains("DRAW") || rUpper.contains("TIE")) {
                outcome = HandicapBet.Outcome.DRAW;
            }

            if (outcome != null) {
                boolean isAsian = isQuarterAsian(line);
                return new HandicapBet(ctx.getScope(), outcome, line, isAsian, st);
            }
        }

        // 3. Stats Match Result (1X2 / 3-Way / Most)
        if (mUpper.contains("1X2") || mUpper.contains("RESULT") || mUpper.contains("MOST") || mUpper.contains("WINNER")) {
            if (rUpper.contains("DRAW") || rUpper.contains("TIE") || "X".equals(rUpper)) {
                return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.DRAW, st);
            }
            if (isTeam1(rUpper, team1)) {
                return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.WIN1, st);
            }
            if (isTeam2(rUpper, team2)) {
                return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.WIN2, st);
            }
        }

        return null;
    }

    private BetSubject resolveSubject(String mUpper, String team1, String team2) {
        boolean hasTeam1 = (team1 != null && !team1.isBlank() && mUpper.contains(team1.toUpperCase()))
                || mUpper.contains("HOME");
        boolean hasTeam2 = (team2 != null && !team2.isBlank() && mUpper.contains(team2.toUpperCase()))
                || mUpper.contains("AWAY");

        if (hasTeam1 && !hasTeam2) return BetSubject.TEAM1;
        if (hasTeam2 && !hasTeam1) return BetSubject.TEAM2;

        return BetSubject.MATCH;
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
