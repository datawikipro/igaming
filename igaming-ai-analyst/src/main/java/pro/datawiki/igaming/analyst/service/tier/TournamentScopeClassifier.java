package pro.datawiki.igaming.analyst.service.tier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Classifies matches into competition tier scopes (e.g. PRO_SENIOR vs YOUTH_AMATEUR).
 * Protects core institutional strategies from volatile youth and amateur closed leagues.
 * Strictly adheres to Rule 8 (Zero Discriminator Branching) and Open-Closed Principle.
 */
@Slf4j
@Component("tournamentScopeClassifier")
public class TournamentScopeClassifier {

    private static final Pattern YOUTH_LEAGUE_PATTERN = Pattern.compile(
            "(?iU)(?:\\b|_)(u-?1[5-9]|u-?2[0-3]|under\\s*1[5-9]|under\\s*2[0-3]|primavera|juniores|juniors?|youth|juvenil|nachwuchs\\w*|a-junioren|b-junioren|юнош\\w*|молод\\w*|м-лига|до\\s*1[5-9]\\s*лет|до\\s*2[0-3]\\s*лет|резерв\\w*|reserves?)(?:\\b|_)"
    );

    private static final Pattern YOUTH_TEAM_PATTERN = Pattern.compile(
            "(?iU)(?:\\b|_|\\()(u-?1[5-9]|u-?2[0-3]|primavera|juvenil|youth|sub-?1[5-9]|sub-?2[0-3]|reserves?|резерв\\w*|мол(?:од\\w*|\\.)?)(?:\\b|_|\\)|$)"
    );

    private static final Pattern AMATEUR_SERIES_PATTERN = Pattern.compile(
            "(?iU)(?:\\b|_)(liga\\s*pro|лига\\s*про|setka\\s*cup|сетка\\s*кап|tt\\s*cup|т-?т\\s*кап|win\\s*cup|elite\\s*series|space\\s*league|short\\s*football|dream\\s*league)(?:\\b|_)"
    );

    /**
     * Classifies a recommendation and its dossier into TournamentTierScope.
     */
    public TournamentTierScope classify(MatchAiRecommendation rec, Map<String, Object> dossierMeta) {
        // 1. Check explicit MDM metadata from dossierMeta if present
        if (dossierMeta != null) {
            Object ageCatObj = dossierMeta.get("age_category");
            if (ageCatObj == null) {
                ageCatObj = dossierMeta.get("ageCategory");
            }
            if (ageCatObj != null) {
                String ageStr = ageCatObj.toString().trim().toUpperCase();
                if (!ageStr.isEmpty() && !"SENIOR".equals(ageStr) && !"ADULT".equals(ageStr)) {
                    if (ageStr.contains("U1") || ageStr.contains("U2") || ageStr.contains("YOUTH") || ageStr.contains("JUNIOR") || ageStr.contains("RESERVE")) {
                        return TournamentTierScope.YOUTH_AMATEUR;
                    }
                }
            }

            Object tierObj = dossierMeta.get("tier");
            if (tierObj instanceof Number num) {
                int tierVal = num.intValue();
                if (tierVal >= 6) {
                    return TournamentTierScope.YOUTH_AMATEUR;
                }
            } else if (tierObj != null) {
                try {
                    int tierVal = Integer.parseInt(tierObj.toString().trim());
                    if (tierVal >= 6) {
                        return TournamentTierScope.YOUTH_AMATEUR;
                    }
                } catch (NumberFormatException ignored) {}
            }

            Object coverageTierObj = dossierMeta.get("coverage_tier");
            if (coverageTierObj != null) {
                String covStr = coverageTierObj.toString().trim().toUpperCase();
                if (covStr.contains("TIER_6") || covStr.contains("TIER_7") || covStr.contains("YOUTH") || covStr.contains("AMATEUR")) {
                    return TournamentTierScope.YOUTH_AMATEUR;
                }
            }
        }

        // 2. Check recommendation league / tournament fields
        String leagueName = rec != null ? rec.getLeagueName() : null;
        String tournamentSlug = rec != null ? rec.getTournamentSlug() : null;
        String t1 = rec != null ? rec.getTeam1Name() : null;
        String t2 = rec != null ? rec.getTeam2Name() : null;

        if (isYouthOrAmateurText(leagueName, tournamentSlug, t1, t2)) {
            return TournamentTierScope.YOUTH_AMATEUR;
        }

        return TournamentTierScope.PRO_SENIOR;
    }

    /**
     * Textual inspection of league name, tournament slug, and team names for youth or amateur series indicators.
     */
    public boolean isYouthOrAmateurText(String leagueName, String tournamentSlug, String t1, String t2) {
        if (leagueName != null && !leagueName.isBlank()) {
            if (YOUTH_LEAGUE_PATTERN.matcher(leagueName).find() || AMATEUR_SERIES_PATTERN.matcher(leagueName).find()) {
                return true;
            }
        }

        if (tournamentSlug != null && !tournamentSlug.isBlank()) {
            String slugClean = tournamentSlug.replace('-', ' ').replace('_', ' ');
            if (YOUTH_LEAGUE_PATTERN.matcher(slugClean).find() || AMATEUR_SERIES_PATTERN.matcher(slugClean).find()) {
                return true;
            }
        }

        if (t1 != null && !t1.isBlank() && YOUTH_TEAM_PATTERN.matcher(t1).find()) {
            return true;
        }

        if (t2 != null && !t2.isBlank() && YOUTH_TEAM_PATTERN.matcher(t2).find()) {
            return true;
        }

        return false;
    }
}
