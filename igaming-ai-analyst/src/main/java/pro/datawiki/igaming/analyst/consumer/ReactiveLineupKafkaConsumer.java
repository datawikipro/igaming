package pro.datawiki.igaming.analyst.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.lineup.CanonicalLineupEvent;
import pro.datawiki.igaming.dto.lineup.Cs2MatchLineupDto;
import pro.datawiki.igaming.dto.lineup.Dota2MatchLineupDto;
import pro.datawiki.igaming.dto.lineup.FootballMatchLineupDto;

/**
 * Event-driven consumer for sport-specific match lineups and rosters.
 * Subscribes to the pattern 'sports.*.lineups' across all disciplines.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveLineupKafkaConsumer {

    @KafkaListener(
            topicPattern = "^sports\\..*\\.lineups$",
            groupId = "${spring.kafka.consumer.group-id:ai-analyst-lineup-group}"
    )
    public void onLineupEvent(Object event) {
        if (event == null) return;

        try {
            if (event instanceof FootballMatchLineupDto football) {
                log.info("⚽ [LINEUP-BUS] Received Football lineup for match #{}: confirmed={}, starters: {} vs {}, formation: {} vs {}",
                        football.getMatchId(), football.isConfirmed(),
                        football.getStartersHome() != null ? football.getStartersHome().size() : 0,
                        football.getStartersAway() != null ? football.getStartersAway().size() : 0,
                        football.getFormationHome(), football.getFormationAway());
            } else if (event instanceof Cs2MatchLineupDto cs2) {
                log.info("🔫 [LINEUP-BUS] Received CS2 lineup & veto for match #{}: confirmed={}, veto actions: {}, players: {} vs {}",
                        cs2.getMatchId(), cs2.isConfirmed(),
                        cs2.getVetoActions() != null ? cs2.getVetoActions().size() : 0,
                        cs2.getTeam1Players() != null ? cs2.getTeam1Players().size() : 0,
                        cs2.getTeam2Players() != null ? cs2.getTeam2Players().size() : 0);
            } else if (event instanceof Dota2MatchLineupDto dota) {
                log.info("🛡️ [LINEUP-BUS] Received Dota 2 draft & lineup for match #{}: confirmed={}, draft picks/bans: {}, players: {} vs {}",
                        dota.getMatchId(), dota.isConfirmed(),
                        dota.getDrafts() != null ? dota.getDrafts().size() : 0,
                        dota.getRadiantPlayers() != null ? dota.getRadiantPlayers().size() : 0,
                        dota.getDirePlayers() != null ? dota.getDirePlayers().size() : 0);
            } else if (event instanceof CanonicalLineupEvent lineup) {
                log.info("📋 [LINEUP-BUS] Received lineup event for match #{}, sport={}",
                        lineup.getMatchId(), lineup.getSportSlug());
            }
        } catch (Exception ex) {
            log.warn("❌ [LINEUP-BUS] Error processing lineup event: {}", ex.getMessage());
        }
    }
}
