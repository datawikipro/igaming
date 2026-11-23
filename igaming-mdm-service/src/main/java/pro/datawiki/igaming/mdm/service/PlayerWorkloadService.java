package pro.datawiki.igaming.mdm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.athlete.AthleteWorkloadDto;
import pro.datawiki.igaming.mdm.domain.MdmMatch;
import pro.datawiki.igaming.mdm.domain.MdmTeamMembership;
import pro.datawiki.igaming.mdm.repository.MdmMatchRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamMembershipRepository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * High-performance service for calculating cross-team and cross-discipline
 * workload, acute fatigue, rest window, and congestion risk.
 * Considers both Club and National Team appearances, as well as Singles and Doubles matches.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlayerWorkloadService {

    private final MdmTeamMembershipRepository membershipRepository;
    private final MdmMatchRepository matchRepository;

    @Transactional(readOnly = true)
    public AthleteWorkloadDto calculateWorkload(Long playerId) {
        if (playerId == null) {
            return defaultFreshWorkload(null);
        }

        List<MdmTeamMembership> memberships = membershipRepository.findByPlayerId(playerId);
        if (memberships.isEmpty()) {
            return defaultFreshWorkload(playerId);
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime window14d = now.minusDays(14);
        OffsetDateTime window7d = now.minusDays(7);
        OffsetDateTime window72h = now.minusHours(72);

        int totalMinutes72h = 0;
        int totalMinutes7d = 0;
        int totalMinutes14d = 0;
        int matches7d = 0;
        int matches14d = 0;
        int nationalTeamMatches14d = 0;
        int clubMatches14d = 0;
        int doublesMatches14d = 0;
        int singlesMatches14d = 0;

        OffsetDateTime latestMatchTime = null;

        for (MdmTeamMembership m : memberships) {
            if (!Boolean.TRUE.equals(m.getIsActive())) continue;

            Long teamId = m.getTeam().getId();
            boolean isNational = Boolean.TRUE.equals(m.getTeam().getIsNational()) 
                    || "NATIONAL_TEAM".equalsIgnoreCase(m.getMembershipType());
            boolean isStarter = "STARTER".equalsIgnoreCase(m.getSquadRole());

            List<MdmMatch> matches = matchRepository.findRecentFinishedMatchesForTeam(teamId, window14d);
            for (MdmMatch match : matches) {
                OffsetDateTime matchTime = match.getStartTime();
                if (latestMatchTime == null || matchTime.isAfter(latestMatchTime)) {
                    latestMatchTime = matchTime;
                }

                matches14d++;
                if (isNational) {
                    nationalTeamMatches14d++;
                } else {
                    clubMatches14d++;
                }

                // In tennis / racket sports:
                String sportSlug = match.getSport() != null ? match.getSport().getSlug().toLowerCase() : "";
                if (sportSlug.contains("doubles") || "DOUBLES_PAIR".equalsIgnoreCase(m.getMembershipType())) {
                    doublesMatches14d++;
                } else if (sportSlug.contains("tennis")) {
                    singlesMatches14d++;
                }

                // Estimated or actual minutes: 90 for football starter, 75 for tennis best-of-3
                int matchMins = isStarter ? (sportSlug.contains("tennis") ? 95 : 85) : 25;

                totalMinutes14d += matchMins;
                if (matchTime.isAfter(window7d)) {
                    matches7d++;
                    totalMinutes7d += matchMins;
                }
                if (matchTime.isAfter(window72h)) {
                    totalMinutes72h += matchMins;
                }
            }
        }

        Double restHours = null;
        if (latestMatchTime != null) {
            restHours = Math.max(0.0, Duration.between(latestMatchTime, now).toMinutes() / 60.0);
        }

        boolean isCriticalFatigue = (restHours != null && restHours < 65.0 && totalMinutes7d >= 170)
                || (totalMinutes72h >= 170);
        boolean isOverloaded = totalMinutes7d >= 240;
        boolean hasTranscontinentalTravel = nationalTeamMatches14d > 0 && clubMatches14d > 0 && restHours != null && restHours < 96.0;

        String fatigueStatus;
        if (isCriticalFatigue) {
            fatigueStatus = "RED_ZONE";
        } else if (isOverloaded || (restHours != null && restHours < 72.0)) {
            fatigueStatus = "HIGH_LOAD";
        } else if (totalMinutes7d >= 90) {
            fatigueStatus = "NORMAL";
        } else {
            fatigueStatus = "FRESH";
        }

        double acute = Math.max(60.0, totalMinutes7d);
        double chronic = Math.max(120.0, totalMinutes14d * 2.0) / 4.0;
        double acwr = Math.round((acute / chronic) * 100.0) / 100.0;

        return AthleteWorkloadDto.builder()
                .playerId(playerId)
                .minutesLast72h(totalMinutes72h)
                .minutesLast7d(totalMinutes7d)
                .minutesLast14d(totalMinutes14d)
                .matchesLast7d(matches7d)
                .matchesLast14d(matches14d)
                .nationalTeamMatches14d(nationalTeamMatches14d)
                .clubMatches14d(clubMatches14d)
                .doublesMatches14d(doublesMatches14d)
                .singlesMatches14d(singlesMatches14d)
                .lastMatchDate(latestMatchTime != null ? latestMatchTime.toLocalDateTime() : null)
                .restHoursSinceLastMatch(restHours != null ? Math.round(restHours * 10.0) / 10.0 : null)
                .isCriticalFatigue(isCriticalFatigue)
                .isOverloaded(isOverloaded)
                .hasTranscontinentalTravel(hasTranscontinentalTravel)
                .fatigueStatus(fatigueStatus)
                .acuteChronicWorkloadRatio(acwr)
                .build();
    }

    private AthleteWorkloadDto defaultFreshWorkload(Long playerId) {
        return AthleteWorkloadDto.builder()
                .playerId(playerId)
                .minutesLast72h(0)
                .minutesLast7d(0)
                .minutesLast14d(0)
                .matchesLast7d(0)
                .matchesLast14d(0)
                .nationalTeamMatches14d(0)
                .clubMatches14d(0)
                .doublesMatches14d(0)
                .singlesMatches14d(0)
                .restHoursSinceLastMatch(168.0) // 7 days rest
                .isCriticalFatigue(false)
                .isOverloaded(false)
                .hasTranscontinentalTravel(false)
                .fatigueStatus("FRESH")
                .acuteChronicWorkloadRatio(1.0)
                .build();
    }
}
