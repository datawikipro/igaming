package pro.datawiki.igaming.capture.sofascore.stats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.sport.basketball.BasketballSport;
import pro.datawiki.igaming.dto.sport.hockey.IceHockeySport;

import java.util.List;

/**
 * Institutional statistical engine for computing advanced metrics in Ice Hockey (KHL / NHL)
 * and Basketball (NBA / EuroLeague).
 */
@Component
@Slf4j
public class AdvancedStatsCalculator {

    // ─────────────────────────────────────────────────────────────────────────
    // Ice Hockey (Corsi, Fenwick, HDCF, Goaltender GSAx)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes institutional Ice Hockey metrics from raw match boxscore data.
     *
     * @param homeShotsOnGoal home shots on goal
     * @param awayShotsOnGoal away shots on goal
     * @param homeMissedShots home missed shots (wide/post)
     * @param awayMissedShots away missed shots (wide/post)
     * @param homeBlockedShots home shot attempts blocked by opponent
     * @param awayBlockedShots away shot attempts blocked by opponent
     * @param homeSlotShots home shots originating from high danger slot/crease zone
     * @param awaySlotShots away shots originating from high danger slot/crease zone
     * @param homeGoals home actual goals scored
     * @param awayGoals away actual goals scored
     * @param homeExpectedGoalsAgainst expected goals against home goalie (xGA)
     * @param awayExpectedGoalsAgainst expected goals against away goalie (xGA)
     * @param homeGoalieName home starting netminder
     * @param awayGoalieName away starting netminder
     * @return populated {@link IceHockeyAdvancedMetrics}
     */
    public IceHockeyAdvancedMetrics computeHockeyMetrics(
            int homeShotsOnGoal, int awayShotsOnGoal,
            int homeMissedShots, int awayMissedShots,
            int homeBlockedShots, int awayBlockedShots,
            int homeSlotShots, int awaySlotShots,
            int homeGoals, int awayGoals,
            double homeExpectedGoalsAgainst, double awayExpectedGoalsAgainst,
            String homeGoalieName, String awayGoalieName
    ) {
        // Corsi = Shots on Goal + Missed Shots + Blocked Shots (All Shot Attempts)
        double homeCorsiFor = homeShotsOnGoal + homeMissedShots + homeBlockedShots;
        double awayCorsiFor = awayShotsOnGoal + awayMissedShots + awayBlockedShots;

        double homeCorsiPct = IceHockeySport.calculateCorsiForPct(homeCorsiFor, awayCorsiFor);
        double awayCorsiPct = IceHockeySport.calculateCorsiForPct(awayCorsiFor, homeCorsiFor);

        // Fenwick = Shots on Goal + Missed Shots (Unblocked Shot Attempts)
        double homeFenwickFor = homeShotsOnGoal + homeMissedShots;
        double awayFenwickFor = awayShotsOnGoal + awayMissedShots;

        double homeFenwickPct = IceHockeySport.calculateFenwickForPct(homeFenwickFor, awayFenwickFor);
        double awayFenwickPct = IceHockeySport.calculateFenwickForPct(awayFenwickFor, homeFenwickFor);

        // High Danger Chances (HDCF)
        double homeHdcf = Math.max(0, homeSlotShots);
        double awayHdcf = Math.max(0, awaySlotShots);

        double homeHdcfPct = IceHockeySport.calculateHdcfPct(homeHdcf, awayHdcf);
        double awayHdcfPct = IceHockeySport.calculateHdcfPct(awayHdcf, homeHdcf);

        // Goaltender GSAx (Goals Saved Above Expected): xGA - actual GA
        // Home goalie faces away shots/goals; away goalie faces home shots/goals
        double homeGoalieGa = (double) awayGoals;
        double awayGoalieGa = (double) homeGoals;

        double homeGoalieGsax = IceHockeySport.calculateGoalieGsax(homeExpectedGoalsAgainst, homeGoalieGa);
        double awayGoalieGsax = IceHockeySport.calculateGoalieGsax(awayExpectedGoalsAgainst, awayGoalieGa);

        int homeGoalieSaves = Math.max(0, awayShotsOnGoal - awayGoals);
        int awayGoalieSaves = Math.max(0, homeShotsOnGoal - homeGoals);

        return IceHockeyAdvancedMetrics.builder()
                .homeCorsiFor(round2(homeCorsiFor))
                .homeCorsiAgainst(round2(awayCorsiFor))
                .homeCorsiPct(round2(homeCorsiPct))
                .awayCorsiFor(round2(awayCorsiFor))
                .awayCorsiAgainst(round2(homeCorsiFor))
                .awayCorsiPct(round2(awayCorsiPct))
                .homeFenwickFor(round2(homeFenwickFor))
                .homeFenwickAgainst(round2(awayFenwickFor))
                .homeFenwickPct(round2(homeFenwickPct))
                .awayFenwickFor(round2(awayFenwickFor))
                .awayFenwickAgainst(round2(homeFenwickFor))
                .awayFenwickPct(round2(awayFenwickPct))
                .homeHdcf(round2(homeHdcf))
                .homeHdca(round2(awayHdcf))
                .homeHdcfPct(round2(homeHdcfPct))
                .awayHdcf(round2(awayHdcf))
                .awayHdca(round2(homeHdcf))
                .awayHdcfPct(round2(awayHdcfPct))
                .homeGoalieName(homeGoalieName)
                .homeGoalieShotsAgainst(awayShotsOnGoal)
                .homeGoalieSaves(homeGoalieSaves)
                .homeGoalieGoalsAgainst(homeGoalieGa)
                .homeGoalieExpectedGoalsAgainst(round2(homeExpectedGoalsAgainst))
                .homeGoalieGsax(round2(homeGoalieGsax))
                .awayGoalieName(awayGoalieName)
                .awayGoalieShotsAgainst(homeShotsOnGoal)
                .awayGoalieSaves(awayGoalieSaves)
                .awayGoalieGoalsAgainst(awayGoalieGa)
                .awayGoalieExpectedGoalsAgainst(round2(awayExpectedGoalsAgainst))
                .awayGoalieGsax(round2(awayGoalieGsax))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Basketball (Pace, ORtg, DRtg, NetRtg, TS%, Bench Rotation Depth)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes institutional Basketball metrics from raw team boxscore data.
     *
     * @param homePoints points scored by home team
     * @param awayPoints points scored by away team
     * @param homeFga field goal attempts by home team
     * @param awayFga field goal attempts by away team
     * @param homeFta free throw attempts by home team
     * @param awayFta free throw attempts by away team
     * @param homeOrb offensive rebounds by home team
     * @param awayOrb offensive rebounds by away team
     * @param homeTov turnovers by home team
     * @param awayTov turnovers by away team
     * @param homeMinutesPlayed total team minutes played (240 for 48m regulation, 200 for 40m EuroLeague)
     * @param homeBenchMinutes list of minutes played by home bench players
     * @param awayBenchMinutes list of minutes played by away bench players
     * @param homeBenchPoints points scored by home bench
     * @param awayBenchPoints points scored by away bench
     * @return populated {@link BasketballAdvancedMetrics}
     */
    public BasketballAdvancedMetrics computeBasketballMetrics(
            int homePoints, int awayPoints,
            int homeFga, int awayFga,
            int homeFta, int awayFta,
            int homeOrb, int awayOrb,
            int homeTov, int awayTov,
            double homeMinutesPlayed,
            List<Double> homeBenchMinutes, List<Double> awayBenchMinutes,
            int homeBenchPoints, int awayBenchPoints
    ) {
        // Possessions = FGA + 0.44 * FTA - ORB + TOV
        double homePoss = BasketballSport.estimatePossessions(homeFga, homeFta, homeOrb, homeTov);
        double awayPoss = BasketballSport.estimatePossessions(awayFga, awayFta, awayOrb, awayTov);

        // Average possessions normalized to 48 minutes
        double effectiveMinutes = (homeMinutesPlayed > 0) ? (homeMinutesPlayed / 5.0) : 48.0;
        double avgPoss = (homePoss + awayPoss) / 2.0;
        double pace = BasketballSport.calculatePace(avgPoss, effectiveMinutes);

        // Offensive & Defensive Ratings
        double homeOrtg = BasketballSport.calculateOffensiveRating(homePoints, homePoss);
        double awayOrtg = BasketballSport.calculateOffensiveRating(awayPoints, awayPoss);

        double homeDrtg = BasketballSport.calculateDefensiveRating(awayPoints, awayPoss);
        double awayDrtg = BasketballSport.calculateDefensiveRating(homePoints, homePoss);

        double homeNetRtg = BasketballSport.calculateNetRating(homeOrtg, homeDrtg);
        double awayNetRtg = BasketballSport.calculateNetRating(awayOrtg, awayDrtg);

        // True Shooting %: PTS / (2 * (FGA + 0.44 * FTA)) * 100
        double homeTs = BasketballSport.calculateTrueShootingPct(homePoints, homeFga, homeFta);
        double awayTs = BasketballSport.calculateTrueShootingPct(awayPoints, awayFga, awayFta);

        // Bench Rotation Depth: count of bench players with >= 10.0 minutes
        int homeBenchDepth = BasketballSport.calculateBenchRotationDepth(homeBenchMinutes);
        int awayBenchDepth = BasketballSport.calculateBenchRotationDepth(awayBenchMinutes);

        double homeBenchShare = (homePoints > 0) ? ((double) homeBenchPoints / homePoints) : 0.0;
        double awayBenchShare = (awayPoints > 0) ? ((double) awayBenchPoints / awayPoints) : 0.0;

        double homeTotalBenchMin = (homeBenchMinutes != null)
                ? homeBenchMinutes.stream().mapToDouble(Double::doubleValue).sum()
                : 0.0;
        double awayTotalBenchMin = (awayBenchMinutes != null)
                ? awayBenchMinutes.stream().mapToDouble(Double::doubleValue).sum()
                : 0.0;

        return BasketballAdvancedMetrics.builder()
                .pace(round2(pace))
                .homePossessions(round2(homePoss))
                .awayPossessions(round2(awayPoss))
                .homeOffensiveRating(round2(homeOrtg))
                .awayOffensiveRating(round2(awayOrtg))
                .homeDefensiveRating(round2(homeDrtg))
                .awayDefensiveRating(round2(awayDrtg))
                .homeNetRating(round2(homeNetRtg))
                .awayNetRating(round2(awayNetRtg))
                .homeTrueShootingPct(round2(homeTs))
                .awayTrueShootingPct(round2(awayTs))
                .homeBenchRotationDepth(homeBenchDepth)
                .awayBenchRotationDepth(awayBenchDepth)
                .homeBenchPoints(homeBenchPoints)
                .awayBenchPoints(awayBenchPoints)
                .homeBenchScoringShare(round2(homeBenchShare))
                .awayBenchScoringShare(round2(awayBenchShare))
                .homeBenchMinutes(round2(homeTotalBenchMin))
                .awayBenchMinutes(round2(awayTotalBenchMin))
                .build();
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
