package pro.datawiki.igaming.source.core.engine.xbet.strategy;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.TeamToScoreBet;

import java.util.Set;

@Component
public class XbetTeamToScoreStrategy implements XbetFactorStrategy {

    private static final Set<String> SUPPORTED = Set.of("178", "179", "182", "183");

    @Override
    public boolean supports(String factorCode) {
        return factorCode != null && SUPPORTED.contains(factorCode);
    }

    @Override
    public BetType map(String factorCode, Double param) {
        return switch (factorCode) {
            case "178" -> new TeamToScoreBet(BetScope.FULL_MATCH, BetSubject.TEAM1, TeamToScoreBet.Outcome.YES);
            case "179" -> new TeamToScoreBet(BetScope.FULL_MATCH, BetSubject.TEAM1, TeamToScoreBet.Outcome.NO);
            case "182" -> new TeamToScoreBet(BetScope.FULL_MATCH, BetSubject.TEAM2, TeamToScoreBet.Outcome.YES);
            case "183" -> new TeamToScoreBet(BetScope.FULL_MATCH, BetSubject.TEAM2, TeamToScoreBet.Outcome.NO);
            default -> null;
        };
    }

    @Override
    public String describe(String factorCode, Double param) {
        return switch (factorCode) {
            case "178" -> "T1_TO_SCORE_YES";
            case "179" -> "T1_TO_SCORE_NO";
            case "182" -> "T2_TO_SCORE_YES";
            case "183" -> "T2_TO_SCORE_NO";
            default -> "T_" + factorCode;
        };
    }
}
