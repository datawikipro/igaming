package pro.datawiki.igaming.capture.sofascore.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service orchestrating the capture, computation, and Kafka streaming of institutional
 * sports metrics for Ice Hockey (KHL / NHL) and Basketball (NBA / EuroLeague).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedStatsCaptureService {

    private final AdvancedStatsCalculator statsCalculator;
    private final AdvancedMetricsKafkaPublisher kafkaPublisher;

    /**
     * Processes and publishes advanced institutional metrics for an Ice Hockey match (KHL / NHL).
     *
     * @param provider data source name ("SOFASCORE", "KHL_OFFICIAL", "NHL_API")
     * @param leagueName "KHL", "NHL", etc.
     * @param externalMatchId unique external fixture ID
     * @param matchStartTimeEpochMs match start epoch
     * @param homeTeam home team name
     * @param awayTeam away team name
     * @param homeShotsOnGoal home shots on goal
     * @param awayShotsOnGoal away shots on goal
     * @param homeMissedShots home missed shots
     * @param awayMissedShots away missed shots
     * @param homeBlockedShots home blocked attempts
     * @param awayBlockedShots away blocked attempts
     * @param homeSlotShots high-danger slot chances
     * @param awaySlotShots high-danger slot chances
     * @param homeGoals home goals
     * @param awayGoals away goals
     * @param homeExpectedGoalsAgainst home goalie xGA
     * @param awayExpectedGoalsAgainst away goalie xGA
     * @param homeGoalieName home starting netminder
     * @param awayGoalieName away starting netminder
     * @return published {@link CanonicalAdvancedMetricsEvent}
     */
    public CanonicalAdvancedMetricsEvent captureAndPublishHockeyStats(
            String provider, String leagueName, String externalMatchId,
            Long matchStartTimeEpochMs, String homeTeam, String awayTeam,
            int homeShotsOnGoal, int awayShotsOnGoal,
            int homeMissedShots, int awayMissedShots,
            int homeBlockedShots, int awayBlockedShots,
            int homeSlotShots, int awaySlotShots,
            int homeGoals, int awayGoals,
            double homeExpectedGoalsAgainst, double awayExpectedGoalsAgainst,
            String homeGoalieName, String awayGoalieName
    ) {
        log.info("[AdvancedStatsCapture] Processing Ice Hockey metrics for {}/{} ({} vs {})",
                leagueName, externalMatchId, homeTeam, awayTeam);

        IceHockeyAdvancedMetrics metrics = statsCalculator.computeHockeyMetrics(
                homeShotsOnGoal, awayShotsOnGoal,
                homeMissedShots, awayMissedShots,
                homeBlockedShots, awayBlockedShots,
                homeSlotShots, awaySlotShots,
                homeGoals, awayGoals,
                homeExpectedGoalsAgainst, awayExpectedGoalsAgainst,
                homeGoalieName, awayGoalieName
        );

        CanonicalAdvancedMetricsEvent event = CanonicalAdvancedMetricsEvent.builder()
                .eventId("adv-hky-" + UUID.randomUUID())
                .provider(provider != null ? provider : "SOFASCORE")
                .sport("ICE_HOCKEY")
                .leagueName(leagueName)
                .matchExternalId(externalMatchId)
                .matchStartTimeEpochMs(matchStartTimeEpochMs != null ? matchStartTimeEpochMs : System.currentTimeMillis())
                .team1Name(homeTeam)
                .team2Name(awayTeam)
                .status("FINISHED")
                .hockeyMetrics(metrics)
                .capturedAtEpochMs(System.currentTimeMillis())
                .build();

        kafkaPublisher.publish(event);
        return event;
    }

    /**
     * Processes and publishes advanced institutional metrics for a Basketball match (NBA / EuroLeague).
     *
     * @param provider data source name ("SOFASCORE", "NBA_API", "EUROLEAGUE_API")
     * @param leagueName "NBA", "EuroLeague", etc.
     * @param externalMatchId unique external fixture ID
     * @param matchStartTimeEpochMs match start epoch
     * @param homeTeam home team name
     * @param awayTeam away team name
     * @param homePoints home points
     * @param awayPoints away points
     * @param homeFga home field goal attempts
     * @param awayFga away field goal attempts
     * @param homeFta home free throw attempts
     * @param awayFta away free throw attempts
     * @param homeOrb home offensive rebounds
     * @param awayOrb away offensive rebounds
     * @param homeTov home turnovers
     * @param awayTov away turnovers
     * @param totalTeamMinutes total team minutes (240 for NBA 48m, 200 for EuroLeague 40m)
     * @param homeBenchMinutes minutes of bench players
     * @param awayBenchMinutes minutes of bench players
     * @param homeBenchPoints points scored by home bench
     * @param awayBenchPoints points scored by away bench
     * @return published {@link CanonicalAdvancedMetricsEvent}
     */
    public CanonicalAdvancedMetricsEvent captureAndPublishBasketballStats(
            String provider, String leagueName, String externalMatchId,
            Long matchStartTimeEpochMs, String homeTeam, String awayTeam,
            int homePoints, int awayPoints,
            int homeFga, int awayFga,
            int homeFta, int awayFta,
            int homeOrb, int awayOrb,
            int homeTov, int awayTov,
            double totalTeamMinutes,
            List<Double> homeBenchMinutes, List<Double> awayBenchMinutes,
            int homeBenchPoints, int awayBenchPoints
    ) {
        log.info("[AdvancedStatsCapture] Processing Basketball metrics for {}/{} ({} vs {})",
                leagueName, externalMatchId, homeTeam, awayTeam);

        BasketballAdvancedMetrics metrics = statsCalculator.computeBasketballMetrics(
                homePoints, awayPoints,
                homeFga, awayFga,
                homeFta, awayFta,
                homeOrb, awayOrb,
                homeTov, awayTov,
                totalTeamMinutes,
                homeBenchMinutes, awayBenchMinutes,
                homeBenchPoints, awayBenchPoints
        );

        CanonicalAdvancedMetricsEvent event = CanonicalAdvancedMetricsEvent.builder()
                .eventId("adv-bkb-" + UUID.randomUUID())
                .provider(provider != null ? provider : "SOFASCORE")
                .sport("BASKETBALL")
                .leagueName(leagueName)
                .matchExternalId(externalMatchId)
                .matchStartTimeEpochMs(matchStartTimeEpochMs != null ? matchStartTimeEpochMs : System.currentTimeMillis())
                .team1Name(homeTeam)
                .team2Name(awayTeam)
                .status("FINISHED")
                .basketballMetrics(metrics)
                .capturedAtEpochMs(System.currentTimeMillis())
                .build();

        kafkaPublisher.publish(event);
        return event;
    }
}
