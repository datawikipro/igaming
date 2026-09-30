package pro.datawiki.igaming.source.draftkings.service.mapper;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;

@Component
@Order(6)
public class DraftKingsEsportsMarketHandler implements DraftKingsMarketHandler {

    @Override
    public boolean supports(DraftKingsMarketContext ctx) {
        if (isEsports(ctx.getSportType())) return true;
        String combined = ((ctx.getCategoryName() != null ? ctx.getCategoryName() : "") + " " + ctx.getMarketName()).toUpperCase();
        return combined.contains("MAP 1") || combined.contains("MAP 2") || combined.contains("MAP 3")
                || combined.contains("TOTAL MAPS") || combined.contains("MAP HANDICAP")
                || combined.contains("FIRST BLOOD") || combined.contains("FIRST TOWER")
                || combined.contains("TOTAL ROUNDS") || combined.contains("ROUND HANDICAP");
    }

    @Override
    public BetType map(DraftKingsMarketContext ctx) {
        String mUpper = ctx.getMarketName().toUpperCase();
        String participant = ctx.getOutcome().getParticipant();
        String label = ctx.getOutcome().getLabel();
        String runner = participant != null ? participant : (label != null ? label : "");
        String rUpper = runner.toUpperCase();

        Double line = ctx.getLine() != null ? ctx.getLine() : 0.0;
        String team1 = ctx.getTeam1();
        String team2 = ctx.getTeam2();

        // 1. First Blood
        if (mUpper.contains("FIRST BLOOD")) {
            BinaryMarketBet.Outcome bOutcome = null;
            if (isTeam1(rUpper, team1)) bOutcome = BinaryMarketBet.Outcome.TEAM1;
            else if (isTeam2(rUpper, team2)) bOutcome = BinaryMarketBet.Outcome.TEAM2;
            else if ("YES".equals(rUpper)) bOutcome = BinaryMarketBet.Outcome.YES;
            else if ("NO".equals(rUpper)) bOutcome = BinaryMarketBet.Outcome.NO;

            if (bOutcome != null) {
                return new BinaryMarketBet(ctx.getScope(), BetSubject.MATCH, BinaryMarketBet.MarketType.FIRST_BLOOD, bOutcome, StatType.FIRST_BLOOD);
            }
        }

        // 2. Total Maps
        if (mUpper.contains("TOTAL MAPS")) {
            TotalBet.Direction dir = resolveDirection(rUpper);
            if (dir != null) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, dir, line, false, StatType.MAPS);
            }
        }

        // 3. Map Handicap
        if (mUpper.contains("MAP HANDICAP") || mUpper.contains("MAP SPREAD")) {
            HandicapBet.Outcome outcome = resolveHandicapOutcome(rUpper, team1, team2);
            if (outcome != null) {
                return new HandicapBet(BetScope.FULL_MATCH, outcome, line, false, StatType.MAPS);
            }
        }

        // 4. Rounds (Total Rounds, Round Handicap)
        if (mUpper.contains("ROUNDS") || mUpper.contains("ROUND")) {
            if (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER")) {
                TotalBet.Direction dir = resolveDirection(rUpper);
                if (dir != null) {
                    return new TotalBet(ctx.getScope(), BetSubject.MATCH, dir, line, false, StatType.ROUNDS);
                }
            } else if (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD")) {
                HandicapBet.Outcome outcome = resolveHandicapOutcome(rUpper, team1, team2);
                if (outcome != null) {
                    return new HandicapBet(ctx.getScope(), outcome, line, false, StatType.ROUNDS);
                }
            }
        }

        // 5. Kills
        if (mUpper.contains("KILL") || mUpper.contains("KILLS")) {
            if (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER")) {
                TotalBet.Direction dir = resolveDirection(rUpper);
                if (dir != null) {
                    return new TotalBet(ctx.getScope(), BetSubject.MATCH, dir, line, false, StatType.KILLS);
                }
            } else if (mUpper.contains("HANDICAP") || mUpper.contains("SPREAD")) {
                HandicapBet.Outcome outcome = resolveHandicapOutcome(rUpper, team1, team2);
                if (outcome != null) {
                    return new HandicapBet(ctx.getScope(), outcome, line, false, StatType.KILLS);
                }
            }
        }

        // 6. Map Winner (Map 1 Winner, Map 2 Winner, etc.)
        if (ctx.getScope().name().startsWith("MAP_") && (mUpper.contains("WINNER") || mUpper.contains("MONEYLINE") || mUpper.contains("2-WAY"))) {
            if (isTeam1(rUpper, team1)) {
                return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.WIN1_2WAY, StatType.MATCH);
            } else if (isTeam2(rUpper, team2)) {
                return new MatchResultBet(ctx.getScope(), MatchResultBet.Outcome.WIN2_2WAY, StatType.MATCH);
            }
        }

        return null;
    }

    private TotalBet.Direction resolveDirection(String rUpper) {
        if (rUpper.startsWith("OVER") || "O".equals(rUpper) || rUpper.contains("OVER")) {
            return TotalBet.Direction.OVER;
        } else if (rUpper.startsWith("UNDER") || "U".equals(rUpper) || rUpper.contains("UNDER")) {
            return TotalBet.Direction.UNDER;
        }
        return null;
    }

    private HandicapBet.Outcome resolveHandicapOutcome(String rUpper, String team1, String team2) {
        if (isTeam1(rUpper, team1)) return HandicapBet.Outcome.TEAM1;
        if (isTeam2(rUpper, team2)) return HandicapBet.Outcome.TEAM2;
        if (rUpper.contains("DRAW") || rUpper.contains("TIE")) return HandicapBet.Outcome.DRAW;
        return null;
    }

    private boolean isEsports(SportType st) {
        if (st == null) return false;
        return st == SportType.ESPORTS || st == SportType.CS2 || st == SportType.DOTA2
                || st == SportType.LEAGUE_OF_LEGENDS || st == SportType.VALORANT
                || st == SportType.RAINBOW_SIX || st == SportType.ROCKET_LEAGUE
                || st == SportType.CALL_OF_DUTY || st == SportType.OVERWATCH
                || st == SportType.PUBG || st == SportType.MOBILE_LEGENDS
                || st == SportType.STARCRAFT;
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
}
