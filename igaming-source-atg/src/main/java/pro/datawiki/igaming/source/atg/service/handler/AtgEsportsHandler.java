package pro.datawiki.igaming.source.atg.service.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiBetOffer;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(5)
public class AtgEsportsHandler extends AbstractAtgMarketHandler {

    private static final Pattern LINE_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern MAP_AFTER_KEYWORD = Pattern.compile("(?i)(?:map|karta|kartan)[\\s_-]?([1-5])(?![0-9])");
    private static final Pattern MAP_BEFORE_KEYWORD = Pattern.compile("(?i)(?:^|[^0-9a-zA-Z])([1-5])(?:st|nd|rd|th|:a|\\.a|:e|\\.e)?\\s*(?:map|karta|kartan)");
    private static final Pattern MAP_ORDINAL_WORD = Pattern.compile("(?i)\\b(first|second|third|fourth|fifth|första|andra|tredje|fjärde|femte)\\s*(?:map|karta|kartan)");

    public static boolean isEsports(SportType sportType) {
        if (sportType == null) return false;
        return switch (sportType) {
            case ESPORTS, CS2, DOTA2, LEAGUE_OF_LEGENDS, VALORANT, STARCRAFT,
                 FIGHTING_GAMES, MOBILE_LEGENDS, CROSSFIRE, RAINBOW_SIX,
                 ROCKET_LEAGUE, CALL_OF_DUTY, OVERWATCH, PUBG -> true;
            default -> false;
        };
    }

    @Override
    public boolean supports(KambiBetOffer betOffer, String marketName, SportType sportType) {
        if (marketName == null && (betOffer == null || betOffer.getCriterion() == null)) {
            return false;
        }
        if (sportType == SportType.BOXING || sportType == SportType.MMA) {
            return false;
        }

        StringBuilder combined = new StringBuilder();
        if (marketName != null) {
            combined.append(marketName.toUpperCase(Locale.ROOT)).append(" ");
        }
        if (betOffer != null && betOffer.getCriterion() != null) {
            if (betOffer.getCriterion().getEnglishLabel() != null) {
                combined.append(betOffer.getCriterion().getEnglishLabel().toUpperCase(Locale.ROOT)).append(" ");
            }
            if (betOffer.getCriterion().getLabel() != null) {
                combined.append(betOffer.getCriterion().getLabel().toUpperCase(Locale.ROOT)).append(" ");
            }
        }
        String m = combined.toString();

        if (m.contains("CORNER") || m.contains("HÖRN") || m.contains("CARD") || m.contains("KORT") || m.contains("BOOKING")) {
            return false;
        }

        if (isEsports(sportType)) {
            return m.contains("MAP") || m.contains("KART") || m.contains("ROUND") || m.contains("RUND")
                    || m.contains("WINNER") || m.contains("VINNARE") || m.contains("MATCH")
                    || m.contains("MONEYLINE") || m.contains("HEAD TO HEAD") || m.contains("H2H")
                    || m.contains("TOTAL") || m.contains("ÖVER/UNDER") || m.contains("OVER/UNDER")
                    || m.contains("ANTAL") || m.contains("HANDICAP") || m.contains("HANDIKAPP") || m.contains("SPREAD");
        }

        return m.contains("MAP") || m.contains("KART") || m.contains("ROUND") || m.contains("RUND");
    }

    @Override
    public void handle(KambiEvent event, KambiBetOffer betOffer, KambiOutcome outcome,
                       String marketName, SportType sportType, List<OddItem> items) {
        double odds = extractDecimalOdds(outcome);
        if (odds <= 1.0) return;

        String mUpper = marketName != null ? marketName.toUpperCase(Locale.ROOT) : "";
        if (betOffer != null && betOffer.getCriterion() != null && betOffer.getCriterion().getEnglishLabel() != null) {
            mUpper = (mUpper + " " + betOffer.getCriterion().getEnglishLabel().toUpperCase(Locale.ROOT)).trim();
        }

        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        String label = outcome.getLabel() != null ? outcome.getLabel().trim() : "";
        String engLabel = outcome.getEnglishLabel() != null ? outcome.getEnglishLabel().trim() : "";
        String upperLabel = label.toUpperCase(Locale.ROOT);
        String upperEng = engLabel.toUpperCase(Locale.ROOT);

        Double line = extractLine(outcome);
        String runnerName = !label.isEmpty() ? label : (!engLabel.isEmpty() ? engLabel : "Outcome " + outcome.getId());

        BetScope mapScope = resolveMapScope(mUpper);
        boolean isSpecificMap = mapScope != BetScope.FULL_MATCH;

        // 1. Map Round Totals (CS2 / Esports rounds on a map)
        boolean isRoundTotal = (mUpper.contains("ROUND") || mUpper.contains("RUND"))
                && (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("ÖVER/UNDER") || mUpper.contains("ANTAL"));
        if (isRoundTotal) {
            if ("OT_OVER".equals(type) || upperLabel.startsWith("OVER") || upperLabel.startsWith("ÖVER") || upperLabel.startsWith(">")
                    || upperEng.startsWith("OVER") || upperEng.startsWith(">")) {
                addTotal(items, outcome, marketName, runnerName, odds, mapScope, BetSubject.MATCH,
                        TotalBet.Direction.OVER, line, false, StatType.ROUNDS);
                return;
            } else if ("OT_UNDER".equals(type) || upperLabel.startsWith("UNDER") || upperLabel.startsWith("<")
                    || upperEng.startsWith("UNDER") || upperEng.startsWith("<")) {
                addTotal(items, outcome, marketName, runnerName, odds, mapScope, BetSubject.MATCH,
                        TotalBet.Direction.UNDER, line, false, StatType.ROUNDS);
                return;
            }
        }

        // 2. Map Round Handicap (CS2 / Esports round spread on a map)
        boolean isRoundHandicap = (mUpper.contains("ROUND") || mUpper.contains("RUND"))
                && (mUpper.contains("HANDICAP") || mUpper.contains("HANDIKAPP") || mUpper.contains("SPREAD"));
        if (isRoundHandicap) {
            if (isTeam1Outcome(outcome, event, type, label, engLabel)) {
                addHandicap(items, outcome, marketName, runnerName, odds, mapScope, HandicapBet.Outcome.TEAM1, line, false, StatType.ROUNDS);
                return;
            } else if (isTeam2Outcome(outcome, event, type, label, engLabel)) {
                addHandicap(items, outcome, marketName, runnerName, odds, mapScope, HandicapBet.Outcome.TEAM2, line, false, StatType.ROUNDS);
                return;
            }
        }

        // 3. Maps Total (Overall match maps count, e.g. Over/Under 2.5 maps)
        boolean isMapsTotal = !isSpecificMap && (mUpper.contains("MAP") || mUpper.contains("KART"))
                && (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("ÖVER/UNDER") || mUpper.contains("ANTAL"));
        if (isMapsTotal) {
            if ("OT_OVER".equals(type) || upperLabel.startsWith("OVER") || upperLabel.startsWith("ÖVER") || upperLabel.startsWith(">")
                    || upperEng.startsWith("OVER") || upperEng.startsWith(">")) {
                addTotal(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, BetSubject.MATCH,
                        TotalBet.Direction.OVER, line, false, StatType.MAPS);
                return;
            } else if ("OT_UNDER".equals(type) || upperLabel.startsWith("UNDER") || upperLabel.startsWith("<")
                    || upperEng.startsWith("UNDER") || upperEng.startsWith("<")) {
                addTotal(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, BetSubject.MATCH,
                        TotalBet.Direction.UNDER, line, false, StatType.MAPS);
                return;
            }
        }

        // 4. Maps Handicap (Overall match map handicap, e.g. -1.5 / +1.5 maps)
        boolean isMapsHandicap = !isSpecificMap && (mUpper.contains("MAP") || mUpper.contains("KART"))
                && (mUpper.contains("HANDICAP") || mUpper.contains("HANDIKAPP") || mUpper.contains("SPREAD"));
        if (isMapsHandicap) {
            if (isTeam1Outcome(outcome, event, type, label, engLabel)) {
                addHandicap(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, line, false, StatType.MAPS);
                return;
            } else if (isTeam2Outcome(outcome, event, type, label, engLabel)) {
                addHandicap(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, line, false, StatType.MAPS);
                return;
            }
        }

        // 5. Individual Map Winner (Map 1..5 Winner)
        if (isSpecificMap) {
            boolean hasDraw = hasDrawOutcome(betOffer);
            if (isTeam1Outcome(outcome, event, type, label, engLabel)) {
                MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                addMatchResult(items, outcome, marketName, runnerName, odds, mapScope, res, StatType.MATCH);
                return;
            } else if (isDrawOutcome(outcome)) {
                addMatchResult(items, outcome, marketName, runnerName, odds, mapScope, MatchResultBet.Outcome.DRAW, StatType.MATCH);
                return;
            } else if (isTeam2Outcome(outcome, event, type, label, engLabel)) {
                MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                addMatchResult(items, outcome, marketName, runnerName, odds, mapScope, res, StatType.MATCH);
                return;
            }
        }

        // 6. Overall Match Winner for Esports (2-way / 3-way)
        if (isEsports(sportType) && (mUpper.contains("MATCH") || mUpper.contains("WINNER") || mUpper.contains("VINNARE")
                || mUpper.contains("MONEYLINE") || mUpper.contains("HEAD TO HEAD") || mUpper.contains("H2H") || mUpper.contains("1X2"))) {
            boolean hasDraw = hasDrawOutcome(betOffer);
            if (isTeam1Outcome(outcome, event, type, label, engLabel)) {
                MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                addMatchResult(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, res, StatType.MATCH);
            } else if (isDrawOutcome(outcome)) {
                addMatchResult(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, StatType.MATCH);
            } else if (isTeam2Outcome(outcome, event, type, label, engLabel)) {
                MatchResultBet.Outcome res = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                addMatchResult(items, outcome, marketName, runnerName, odds, BetScope.FULL_MATCH, res, StatType.MATCH);
            }
        }
    }

    public static BetScope resolveMapScope(String text) {
        if (text == null || text.isBlank()) {
            return BetScope.FULL_MATCH;
        }
        Matcher m1 = MAP_AFTER_KEYWORD.matcher(text);
        if (m1.find()) {
            try {
                return mapIndexToScope(Integer.parseInt(m1.group(1)));
            } catch (NumberFormatException ignored) {}
        }
        Matcher m2 = MAP_BEFORE_KEYWORD.matcher(text);
        if (m2.find()) {
            try {
                return mapIndexToScope(Integer.parseInt(m2.group(1)));
            } catch (NumberFormatException ignored) {}
        }
        Matcher m3 = MAP_ORDINAL_WORD.matcher(text);
        if (m3.find()) {
            String word = m3.group(1).toLowerCase(Locale.ROOT);
            return switch (word) {
                case "first", "första" -> BetScope.MAP_1;
                case "second", "andra" -> BetScope.MAP_2;
                case "third", "tredje" -> BetScope.MAP_3;
                case "fourth", "fjärde" -> BetScope.MAP_4;
                case "fifth", "femte" -> BetScope.MAP_5;
                default -> BetScope.FULL_MATCH;
            };
        }
        return BetScope.FULL_MATCH;
    }

    private static BetScope mapIndexToScope(int mapIdx) {
        return switch (mapIdx) {
            case 1 -> BetScope.MAP_1;
            case 2 -> BetScope.MAP_2;
            case 3 -> BetScope.MAP_3;
            case 4 -> BetScope.MAP_4;
            case 5 -> BetScope.MAP_5;
            default -> BetScope.FULL_MATCH;
        };
    }

    private Double extractLine(KambiOutcome outcome) {
        if (outcome == null) return 0.0;
        Double line = outcome.getLine();
        if (line != null && Math.abs(line) > 100.0) {
            line = line / 1000.0;
        }
        if (line != null && line != 0.0) {
            return line;
        }
        String label = outcome.getLabel();
        if (label != null) {
            Matcher m = LINE_PATTERN.matcher(label);
            if (m.find()) {
                try {
                    return Double.parseDouble(m.group(1));
                } catch (Exception ignored) {}
            }
        }
        String eng = outcome.getEnglishLabel();
        if (eng != null) {
            Matcher m = LINE_PATTERN.matcher(eng);
            if (m.find()) {
                try {
                    return Double.parseDouble(m.group(1));
                } catch (Exception ignored) {}
            }
        }
        return 0.0;
    }

    private boolean isTeam1Outcome(KambiOutcome outcome, KambiEvent event, String type, String label, String engLabel) {
        if ("OT_ONE".equals(type) || "OT_1".equals(type) || "1".equals(label) || "1".equals(engLabel)) {
            return true;
        }
        if ("HOME".equalsIgnoreCase(outcome.getParticipant()) || "TEAM1".equalsIgnoreCase(outcome.getParticipant())) {
            return true;
        }
        if (event != null && event.getHomeName() != null && !event.getHomeName().isBlank()) {
            String home = event.getHomeName().trim().toUpperCase(Locale.ROOT);
            if (label.equalsIgnoreCase(home) || engLabel.equalsIgnoreCase(home)
                    || (outcome.getParticipant() != null && outcome.getParticipant().trim().equalsIgnoreCase(home))) {
                return true;
            }
        }
        return false;
    }

    private boolean isTeam2Outcome(KambiOutcome outcome, KambiEvent event, String type, String label, String engLabel) {
        if ("OT_TWO".equals(type) || "OT_2".equals(type) || "2".equals(label) || "2".equals(engLabel)) {
            return true;
        }
        if ("AWAY".equalsIgnoreCase(outcome.getParticipant()) || "TEAM2".equalsIgnoreCase(outcome.getParticipant())) {
            return true;
        }
        if (event != null && event.getAwayName() != null && !event.getAwayName().isBlank()) {
            String away = event.getAwayName().trim().toUpperCase(Locale.ROOT);
            if (label.equalsIgnoreCase(away) || engLabel.equalsIgnoreCase(away)
                    || (outcome.getParticipant() != null && outcome.getParticipant().trim().equalsIgnoreCase(away))) {
                return true;
            }
        }
        return false;
    }

    private boolean isDrawOutcome(KambiOutcome outcome) {
        if (outcome == null) return false;
        String type = outcome.getType() != null ? outcome.getType().toUpperCase(Locale.ROOT) : "";
        if ("OT_DRAW".equals(type) || "OT_CROSS".equals(type) || "OT_X".equals(type)) {
            return true;
        }
        String label = outcome.getLabel() != null ? outcome.getLabel().trim().toUpperCase(Locale.ROOT) : "";
        String eng = outcome.getEnglishLabel() != null ? outcome.getEnglishLabel().trim().toUpperCase(Locale.ROOT) : "";
        return "DRAW".equals(label) || "DRAW".equals(eng)
                || "X".equals(label) || "X".equals(eng)
                || "OAVGJORT".equals(label) || "OAVGJORT".equals(eng)
                || "TIE".equals(label) || "TIE".equals(eng);
    }

    private boolean hasDrawOutcome(KambiBetOffer betOffer) {
        return betOffer != null && betOffer.getOutcomes() != null
                && betOffer.getOutcomes().stream().anyMatch(this::isDrawOutcome);
    }
}
